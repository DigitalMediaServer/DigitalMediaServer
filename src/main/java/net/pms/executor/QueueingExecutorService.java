package net.pms.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import net.pms.service.Service;

public class QueueingExecutorService extends AbstractExecutorService implements Service {

	/**
	 * The main pool control state, ctl, is an atomic integer packing two
	 * conceptual fields workerCount, indicating the effective number of threads
	 * runState, indicating whether running, shutting down etc
	 *
	 * In order to pack them into one int, we limit workerCount to (2^29)-1
	 * (about 500 million) threads rather than (2^31)-1 (2 billion) otherwise
	 * representable. If this is ever an issue in the future, the variable can
	 * be changed to be an AtomicLong, and the shift/mask constants below
	 * adjusted. But until the need arises, this code is a bit faster and
	 * simpler using an int.
	 *
	 * The workerCount is the number of workers that have been permitted to
	 * start and not permitted to stop. The value may be transiently different
	 * from the actual number of live threads, for example when a ThreadFactory
	 * fails to create a thread when asked, and when exiting threads are still
	 * performing bookkeeping before terminating. The user-visible pool size is
	 * reported as the current size of the workers set.
	 *
	 * The runState provides the main lifecyle control, taking on values:
	 *
	 * RUNNING: Accept new tasks and process queued tasks SHUTDOWN: Don't accept
	 * new tasks, but process queued tasks STOP: Don't accept new tasks, don't
	 * process queued tasks, and interrupt in-progress tasks TIDYING: All tasks
	 * have terminated, workerCount is zero, the thread transitioning to state
	 * TIDYING will run the terminated() hook method TERMINATED: terminated()
	 * has completed
	 *
	 * The numerical order among these values matters, to allow ordered
	 * comparisons. The runState monotonically increases over time, but need not
	 * hit each state. The transitions are:
	 *
	 * RUNNING -> SHUTDOWN On invocation of shutdown(), perhaps implicitly in
	 * finalize() (RUNNING or SHUTDOWN) -> STOP On invocation of shutdownNow()
	 * SHUTDOWN -> TIDYING When both queue and pool are empty STOP -> TIDYING
	 * When pool is empty TIDYING -> TERMINATED When the terminated() hook
	 * method has completed
	 *
	 * Threads waiting in awaitTermination() will return when the state reaches
	 * TERMINATED.
	 *
	 * Detecting the transition from SHUTDOWN to TIDYING is less straightforward
	 * than you'd like because the queue may become empty after non-empty and
	 * vice versa during SHUTDOWN state, but we can only terminate if, after
	 * seeing that it is empty, we see that workerCount is 0 (which sometimes
	 * entails a recheck -- see below).
	 */
	private final AtomicInteger controlState = new AtomicInteger(ctlOf(RUNNING, 0));
	private static final int COUNT_BITS = Integer.SIZE - 3;
	private static final int CAPACITY = (1 << COUNT_BITS) - 1;

	// runState is stored in the high-order bits
	private static final int RUNNING = -1 << COUNT_BITS;
	private static final int SHUTDOWN = 0 << COUNT_BITS;
	private static final int STOP = 1 << COUNT_BITS;
	private static final int TIDYING = 2 << COUNT_BITS;
	private static final int TERMINATED = 3 << COUNT_BITS;

	// Packing and unpacking ctl
	private static int runStateOf(int c) {
		return c & ~CAPACITY;
	}

	private static int workerCountOf(int c) {
		return c & CAPACITY;
	}

	private static int ctlOf(int rs, int wc) {
		return rs | wc;
	}

	/*
	 * Bit field accessors that don't require unpacking ctl. These depend on the
	 * bit layout and on workerCount being never negative.
	 */

	private static boolean runStateLessThan(int c, int s) {
		return c < s;
	}

	private static boolean runStateAtLeast(int c, int s) {
		return c >= s;
	}

	private static boolean isRunning(int c) {
		return c < SHUTDOWN;
	}

	/**
	 * Attempt to CAS-increment the workerCount field of ctl.
	 */
	private boolean compareAndIncrementWorkerCount(int expect) {
		return controlState.compareAndSet(expect, expect + 1);
	}

	/**
	 * Attempt to CAS-decrement the workerCount field of ctl.
	 */
	private boolean compareAndDecrementWorkerCount(int expect) {
		return controlState.compareAndSet(expect, expect - 1);
	}

	/**
	 * Decrements the workerCount field of ctl. This is called only on abrupt
	 * termination of a thread (see processWorkerExit). Other decrements are
	 * performed within getTask.
	 */
	private void decrementWorkerCount() {
		do {} while (!compareAndDecrementWorkerCount(controlState.get()));
	}

	/**
	 * The queue used for holding tasks and handing off to worker threads. We do
	 * not require that workQueue.poll() returning null necessarily means that
	 * workQueue.isEmpty(), so rely solely on isEmpty to see if the queue is
	 * empty (which we must do for example when deciding whether to transition
	 * from SHUTDOWN to TIDYING). This accommodates special-purpose queues such
	 * as DelayQueues for which poll() is allowed to return null even if it may
	 * later return non-null when delays expire.
	 */
	private final Queue<Runnable> incoming;

	/**
	 * Lock held on access to workers set and related bookkeeping. While we
	 * could use a concurrent set of some sort, it turns out to be generally
	 * preferable to use a lock. Among the reasons is that this serializes
	 * interruptIdleWorkers, which avoids unnecessary interrupt storms,
	 * especially during shutdown. Otherwise exiting threads would concurrently
	 * interrupt those that have not yet interrupted. It also simplifies some of
	 * the associated statistics bookkeeping of largestPoolSize etc. We also
	 * hold mainLock on shutdown and shutdownNow, for the sake of ensuring
	 * workers set is stable while separately checking permission to interrupt
	 * and actually interrupting.
	 */
	private final ReentrantLock mainLock = new ReentrantLock();

	/**
	 * Set containing all worker threads in pool. Accessed only when holding
	 * mainLock.
	 */
	private final HashSet<Worker> workers = new HashSet<Worker>();

	/**
	 * Wait condition to support awaitTermination
	 */
	private final Condition termination = mainLock.newCondition();

	/**
	 * Tracks largest attained pool size. Accessed only under mainLock.
	 */
	private int largestPoolSize;

	/**
	 * Counter for completed tasks. Updated only on termination of worker
	 * threads. Accessed only under mainLock.
	 */
	private long completedTaskCount;

	/*
	 * All user control parameters are declared as volatiles so that ongoing
	 * actions are based on freshest values, but without need for locking, since
	 * no internal invariants depend on them changing synchronously with respect
	 * to other actions.
	 */

	/**
	 * Factory for new threads. All threads are created using this factory (via
	 * method addWorker). All callers must be prepared for addWorker to fail,
	 * which may reflect a system or user's policy limiting the number of
	 * threads. Even though it is not treated as an error, failure to create
	 * threads may result in new tasks being rejected or existing ones remaining
	 * stuck in the queue.
	 *
	 * We go further and preserve pool invariants even in the face of errors
	 * such as OutOfMemoryError, that might be thrown while trying to create
	 * threads. Such errors are rather common due to the need to allocate a
	 * native stack in Thread#start, and users will want to perform clean pool
	 * shutdown to clean up. There will likely be enough memory available for
	 * the cleanup code to complete without encountering yet another
	 * OutOfMemoryError.
	 */
	private volatile ThreadFactory threadFactory;

	/**
	 * Handler called when saturated or shutdown in execute.
	 */
	private volatile RejectedExecutionHandler handler;

	/**
	 * Timeout in nanoseconds for idle threads waiting for work. Threads use
	 * this timeout when there are more than corePoolSize present or if
	 * allowCoreThreadTimeOut. Otherwise they wait forever for new work.
	 */
	private volatile long keepAliveTime;

	/**
	 * If false (default), core threads stay alive even when idle. If true, core
	 * threads use keepAliveTime to time out waiting for work.
	 */
	private volatile boolean allowCoreThreadTimeOut;

	/**
	 * Core pool size is the minimum number of workers to keep alive (and not
	 * allow to time out etc) unless allowCoreThreadTimeOut is set, in which
	 * case the minimum is zero.
	 */
	private volatile int corePoolSize;

	/**
	 * Maximum pool size. Note that the actual maximum is internally bounded by
	 * CAPACITY.
	 */
	private volatile int maximumPoolSize;

	/**
	 * The default rejected execution handler
	 */
	private static final RejectedExecutionHandler defaultHandler = new RejectedExecutionHandler.DiscardPolicy();

	protected static class ThreadInfo {
		protected Thread thread;
		protected long idleStart;
	}

	protected static class PoolManager {

		protected volatile int minimumPoolSize;
		protected volatile int corePoolSize;
		protected volatile int corePoolThreshold;
		protected volatile int maximumPoolSize;
		protected volatile long keepAlive;
		protected volatile long coreKeepAlive;

		public int adjust(Collection<ThreadInfo> threads, Collection<ThreadInfo> idle, int queueSize) {

			int add = 0;
			int numIdle = idle.size();
			int numThreads = threads.size() + numIdle; //TODO: (Nad) Wait time..?
			List<ThreadInfo> remove = null;

			int diff = queueSize - numIdle;
			if (diff > 0) {
				if (numThreads < corePoolSize) {
					add = corePoolSize - numThreads;
					diff -= corePoolSize - numThreads;
				}
				if (diff > corePoolThreshold) {
					add += diff;
				}
			} else if (diff < 0 && numIdle > 0) {
				remove = new ArrayList<>();
				long now = System.currentTimeMillis();
				long duration;
				if (numThreads > corePoolSize) {
					duration = keepAlive;
					int nonCore = numThreads - corePoolSize;
					for (ThreadInfo idleThread : idle) { //TODO: (Nad) Sort idle?
						if (nonCore == 0) {
							break;
						}
						if (idleThread.idleStart + duration < now) {
							remove.add(idleThread);
							nonCore--;
							numIdle--;
							diff++;
						}
					}
				}
				if (diff < 0 && numIdle > 0) {
					duration = coreKeepAlive;
					List<ThreadInfo> remainingIdle = new ArrayList<>(idle);
					remainingIdle.removeAll(remove);
					now = System.currentTimeMillis();
					for (ThreadInfo idleThread : remainingIdle) {
						if (idleThread.idleStart + duration < now) {
							remove.add(idleThread);
							numIdle--; // Needed?
						}
					}
				}
			}

			if (numThreads + add > maximumPoolSize) {
				add = maximumPoolSize - numThreads;
			}
			if (remove != null && numThreads - remove.size() < minimumPoolSize) {
				diff = minimumPoolSize - numThreads + remove.size();
				Iterator<ThreadInfo> iterator = remove.iterator();
				while (diff > 0 && iterator.hasNext()) {
					iterator.next();
					iterator.remove();
					diff--;
				}
			}
			if (numThreads + add < minimumPoolSize) {
				add = minimumPoolSize - numThreads;
			}
			return add;
		}
	}

	protected static class Manager extends Thread {

		protected Queue<Runnable> queue;

		public Manager(Queue<Runnable> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				synchronized (this) {
					while (!queue.isEmpty()) {

					}
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected static class Worker2 implements Runnable { //TODO: (Nad) Inherit Thread instead?

		protected final QueueingExecutorService manager;

		protected final Thread thread;

		@GuardedBy("this")
		protected Directive directive;

		@GuardedBy("this")
		protected boolean terminate;

		// Only ever modified by manager and self
		protected Runnable currentTask;

		public Worker2(@Nonnull QueueingExecutorService manager) {
			if (manager == null) {
				throw new IllegalArgumentException("manager cannot be null");
			}
			this.manager = manager;
			this.thread = manager.getThreadFactory().newThread(this);
			this.thread.start();
		}

		protected synchronized void terminate() {
			terminate = true;
			notify();
		}

		@Override
		public void run() {
			synchronized (this) {
				while (!terminate) {
					if (currentTask != null) {
						try { //TODO: (Nad) Exception handling for before and after
							manager.beforeExecute(thread, currentTask);
							Throwable thrown = null;
							try {
								currentTask.run();
								//TODO: (Nad) INterrupted?
							} catch (Throwable t) {
								thrown = t;
							} finally {
								manager.afterExecute(currentTask, thrown);
							}
						} finally {
							synchronized (this) {
								currentTask = null;
								directive = null;
							}
						}
					}
					//manager.reportIdle();
					try {
						this.wait();
					} catch (InterruptedException e) {
						return; //TODO: (Nad) Figure out
					}
				}
			}



//			Thread workerThread = Thread.currentThread();
//			Runnable task = worker.firstTask;
//			worker.firstTask = null;
//			worker.unlock(); // allow interrupts
//			boolean completedAbruptly = true;
//			try {
//				while (task != null || (task = getTask()) != null) {
//					worker.lock();
//					// If pool is stopping, ensure thread is interrupted;
//					// if not, ensure thread is not interrupted. This
//					// requires a recheck in second case to deal with
//					// shutdownNow race while clearing interrupt
//					if ((runStateAtLeast(controlState.get(), STOP) || (Thread.interrupted() && runStateAtLeast(controlState.get(), STOP))) && !workerThread.isInterrupted()) {
//						workerThread.interrupt();
//					}
//					try {
//						beforeExecute(workerThread, task);
//						Throwable thrown = null;
//						try {
//							task.run();
//						} catch (RuntimeException x) {
//							thrown = x;
//							throw x;
//						} catch (Error x) {
//							thrown = x;
//							throw x;
//						} catch (Throwable x) {
//							thrown = x;
//							throw new Error(x);
//						} finally {
//							afterExecute(task, thrown);
//						}
//					} finally {
//						task = null;
//						worker.completedTasks++;
//						worker.unlock();
//					}
//				}
//				completedAbruptly = false;
//			} finally {
//				processWorkerExit(worker, completedAbruptly);
//			}
		}

	}

	/**
	 * Class Worker mainly maintains interrupt control state for threads running
	 * tasks, along with other minor bookkeeping. This class opportunistically
	 * extends AbstractQueuedSynchronizer to simplify acquiring and releasing a
	 * lock surrounding each task execution. This protects against interrupts
	 * that are intended to wake up a worker thread waiting for a task from
	 * instead interrupting a task being run. We implement a simple
	 * non-reentrant mutual exclusion lock rather than use ReentrantLock because
	 * we do not want worker tasks to be able to reacquire the lock when they
	 * invoke pool control methods like setCorePoolSize. Additionally, to
	 * suppress interrupts until the thread actually starts running tasks, we
	 * initialize lock state to a negative value, and clear it upon start (in
	 * runWorker).
	 */
	private final class Worker extends AbstractQueuedSynchronizer implements Runnable { //TODO: (Nad) Worker

		/**
		 * This class will never be serialized, but we provide a
		 * serialVersionUID to suppress a javac warning.
		 */
		private static final long serialVersionUID = 1L;

		/** Thread this worker is running in. Null if factory fails. */
		final Thread thread;
		/** Initial task to run. Possibly null. */
		Runnable firstTask;
		/** Per-thread task counter */
		volatile long completedTasks;

		/**
		 * Creates with given first task and thread from ThreadFactory.
		 *
		 * @param firstTask the first task (null if none)
		 */
		Worker(Runnable firstTask) {
			setState(-1); // inhibit interrupts until runWorker
			this.firstTask = firstTask;
			this.thread = getThreadFactory().newThread(this);
		}

		/** Delegates main run loop to outer runWorker */
		@Override
		public void run() {
			runWorker(this);
		}

		// Lock methods
		//
		// The value 0 represents the unlocked state.
		// The value 1 represents the locked state.

		@Override
		protected boolean isHeldExclusively() {
			return getState() != 0;
		}

		@Override
		protected boolean tryAcquire(int unused) {
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}

		@Override
		protected boolean tryRelease(int unused) {
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}

		public void lock() {
			acquire(1);
		}

		public boolean tryLock() {
			return tryAcquire(1);
		}

		public void unlock() {
			release(1);
		}

		public boolean isLocked() {
			return isHeldExclusively();
		}

		void interruptIfStarted() {
			Thread t;
			if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
				try {
					t.interrupt();
				} catch (SecurityException ignore) {}
			}
		}
	}

	/*
	 * Methods for setting control state
	 */

	/**
	 * Transitions runState to given target, or leaves it alone if already at
	 * least the given target.
	 *
	 * @param targetState the desired state, either SHUTDOWN or STOP (but not
	 *            TIDYING or TERMINATED -- use tryTerminate for that)
	 */
	private void advanceRunState(int targetState) {
		for (;;) {
			int c = controlState.get();
			if (runStateAtLeast(c, targetState) || controlState.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) {
				break;
			}
		}
	}

	/**
	 * Transitions to TERMINATED state if either (SHUTDOWN and pool and queue
	 * empty) or (STOP and pool empty). If otherwise eligible to terminate but
	 * workerCount is nonzero, interrupts an idle worker to ensure that shutdown
	 * signals propagate. This method must be called following any action that
	 * might make termination possible -- reducing worker count or removing
	 * tasks from the queue during shutdown. The method is non-private to allow
	 * access from ScheduledThreadPoolExecutor.
	 */
	final void tryTerminate() {
		for (;;) {
			int c = controlState.get();
			if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && !incoming.isEmpty())) {
				return;
			}
			if (workerCountOf(c) != 0) { // Eligible to terminate
				interruptIdleWorkers(ONLY_ONE);
				return;
			}

			final ReentrantLock mainLock = this.mainLock;
			mainLock.lock();
			try {
				if (controlState.compareAndSet(c, ctlOf(TIDYING, 0))) {
					try {
						terminated();
					} finally {
						controlState.set(ctlOf(TERMINATED, 0));
						termination.signalAll();
					}
					return;
				}
			} finally {
				mainLock.unlock();
			}
			// else retry on failed CAS
		}
	}

	/*
	 * Methods for controlling interrupts to worker threads.
	 */

	/**
	 * Interrupts all threads, even if active. Ignores SecurityExceptions (in
	 * which case some threads may remain uninterrupted).
	 */
	private void interruptWorkers() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker w : workers) {
				w.interruptIfStarted();
			}
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Interrupts threads that might be waiting for tasks (as indicated by not
	 * being locked) so they can check for termination or configuration changes.
	 * Ignores SecurityExceptions (in which case some threads may remain
	 * uninterrupted).
	 *
	 * @param onlyOne If true, interrupt at most one worker. This is called only
	 *            from tryTerminate when termination is otherwise enabled but
	 *            there are still other workers. In this case, at most one
	 *            waiting worker is interrupted to propagate shutdown signals in
	 *            case all threads are currently waiting. Interrupting any
	 *            arbitrary thread ensures that newly arriving workers since
	 *            shutdown began will also eventually exit. To guarantee
	 *            eventual termination, it suffices to always interrupt only one
	 *            idle worker, but shutdown() interrupts all idle workers so
	 *            that redundant workers exit promptly, not waiting for a
	 *            straggler task to finish.
	 */
	private void interruptIdleWorkers(boolean onlyOne) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker w : workers) {
				Thread t = w.thread;
				if (!t.isInterrupted() && w.tryLock()) {
					try {
						t.interrupt();
					} catch (SecurityException ignore) {} finally {
						w.unlock();
					}
				}
				if (onlyOne) {
					break;
				}
			}
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Common form of interruptIdleWorkers, to avoid having to remember what the
	 * boolean argument means.
	 */
	private void interruptIdleWorkers() {
		interruptIdleWorkers(false);
	}

	private static final boolean ONLY_ONE = true;

	/**
	 * Invokes the rejected execution handler for the given command.
	 */
	protected void reject(Runnable command) {
		handler.rejectedExecution(command, this);
	}

	/**
	 * Performs any further cleanup following run state transition on invocation
	 * of shutdown.
	 */
	void onShutdown() { //TODO: (Nad) Needed?
	}

//	/**
//	 * Drains the task queue into a new list, normally using drainTo. But if the
//	 * queue is a DelayQueue or any other kind of queue for which poll or
//	 * drainTo may fail to remove some elements, it deletes them one by one.
//	 */
//	private List<Runnable> drainQueue() {
//		Queue<Runnable> q = incoming;
//		List<Runnable> taskList = new ArrayList<Runnable>();
//		q.drainTo(taskList);
//		if (!q.isEmpty()) {
//			for (Runnable r : q.toArray(new Runnable[0])) {
//				if (q.remove(r)) {
//					taskList.add(r);
//				}
//			}
//		}
//		return taskList;
//	}

	/*
	 * Methods for creating, running and cleaning up after workers
	 */

	/**
	 * Checks if a new worker can be added with respect to current pool state
	 * and the given bound (either core or maximum). If so, the worker count is
	 * adjusted accordingly, and, if possible, a new worker is created and
	 * started, running firstTask as its first task. This method returns false
	 * if the pool is stopped or eligible to shut down. It also returns false if
	 * the thread factory fails to create a thread when asked. If the thread
	 * creation fails, either due to the thread factory returning null, or due
	 * to an exception (typically OutOfMemoryError in Thread#start), we roll
	 * back cleanly.
	 *
	 * @param firstTask the task the new thread should run first (or null if
	 *            none). Workers are created with an initial first task (in
	 *            method execute()) to bypass queuing when there are fewer than
	 *            corePoolSize threads (in which case we always start one), or
	 *            when the queue is full (in which case we must bypass queue).
	 *            Initially idle threads are usually created via
	 *            prestartCoreThread or to replace other dying workers.
	 *
	 * @param core if true use corePoolSize as bound, else maximumPoolSize. (A
	 *            boolean indicator is used here rather than a value to ensure
	 *            reads of fresh values after checking other pool state).
	 * @return true if successful
	 */
	private boolean addWorker(Runnable firstTask, boolean core) {
		retry: for (;;) {
			int c = controlState.get();
			int rs = runStateOf(c);

			// Check if queue empty only if necessary.
			if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !incoming.isEmpty())) {
				return false;
			}

			for (;;) {
				int wc = workerCountOf(c);
				if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize)) {
					return false;
				}
				if (compareAndIncrementWorkerCount(c)) {
					break retry;
				}
				c = controlState.get();  // Re-read ctl
				if (runStateOf(c) != rs) {
					continue retry;
					// else CAS failed due to workerCount change; retry inner
					// loop
				}
			}
		}

		boolean workerStarted = false;
		boolean workerAdded = false;
		Worker w = null;
		try {
			final ReentrantLock mainLock = this.mainLock;
			w = new Worker(firstTask);
			final Thread t = w.thread;
			if (t != null) {
				mainLock.lock();
				try {
					// Recheck while holding lock.
					// Back out on ThreadFactory failure or if
					// shut down before lock acquired.
					int c = controlState.get();
					int rs = runStateOf(c);

					if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
						if (t.isAlive()) {
							throw new IllegalThreadStateException();
						}
						workers.add(w);
						int s = workers.size();
						if (s > largestPoolSize) {
							largestPoolSize = s;
						}
						workerAdded = true;
					}
				} finally {
					mainLock.unlock();
				}
				if (workerAdded) {
					t.start();
					workerStarted = true;
				}
			}
		} finally {
			if (!workerStarted) {
				addWorkerFailed(w);
			}
		}
		return workerStarted;
	}

	/**
	 * Rolls back the worker thread creation. - removes worker from workers, if
	 * present - decrements worker count - rechecks for termination, in case the
	 * existence of this worker was holding up termination
	 */
	private void addWorkerFailed(Worker w) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			if (w != null) {
				workers.remove(w);
			}
			decrementWorkerCount();
			tryTerminate();
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Performs cleanup and bookkeeping for a dying worker. Called only from
	 * worker threads. Unless completedAbruptly is set, assumes that workerCount
	 * has already been adjusted to account for exit. This method removes thread
	 * from worker set, and possibly terminates the pool or replaces the worker
	 * if either it exited due to user task exception or if fewer than
	 * corePoolSize workers are running or queue is non-empty but there are no
	 * workers.
	 *
	 * @param w the worker
	 * @param completedAbruptly if the worker died due to user exception
	 */
	private void processWorkerExit(Worker w, boolean completedAbruptly) {
		if (completedAbruptly) {
			decrementWorkerCount();
		}

		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			completedTaskCount += w.completedTasks;
			workers.remove(w);
		} finally {
			mainLock.unlock();
		}

		tryTerminate();

		int c = controlState.get();
		if (runStateLessThan(c, STOP)) {
			if (!completedAbruptly) {
				int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
				if (min == 0 && !incoming.isEmpty()) {
					min = 1;
				}
				if (workerCountOf(c) >= min) {
					return; // replacement not needed
				}
			}
			addWorker(null, false);
		}
	}

	/**
	 * Performs blocking or timed wait for a task, depending on current
	 * configuration settings, or returns null if this worker must exit because
	 * of any of: 1. There are more than maximumPoolSize workers (due to a call
	 * to setMaximumPoolSize). 2. The pool is stopped. 3. The pool is shutdown
	 * and the queue is empty. 4. This worker timed out waiting for a task, and
	 * timed-out workers are subject to termination (that is,
	 * {@code allowCoreThreadTimeOut || workerCount > corePoolSize}) both before
	 * and after the timed wait.
	 *
	 * @return task, or null if the worker must exit, in which case workerCount
	 *         is decremented
	 */
	private Runnable getTask() {
		boolean timedOut = false; // Did the last poll() time out?

		retry: for (;;) {
			int c = controlState.get();
			int rs = runStateOf(c);

			// Check if queue empty only if necessary.
			if (rs >= SHUTDOWN && (rs >= STOP || incoming.isEmpty())) {
				decrementWorkerCount();
				return null;
			}

			boolean timed;      // Are workers subject to culling?

			for (;;) {
				int wc = workerCountOf(c);
				timed = allowCoreThreadTimeOut || wc > corePoolSize;

				if (wc <= maximumPoolSize && !(timedOut && timed)) {
					break;
				}
				if (compareAndDecrementWorkerCount(c)) {
					return null;
				}
				c = controlState.get();  // Re-read ctl
				if (runStateOf(c) != rs) {
					continue retry;
					// else CAS failed due to workerCount change; retry inner
					// loop
				}
			}

//			try {
				Runnable r = incoming.poll(); //TODO: (Nad) Replaced to compile
//				Runnable r = timed ? incoming.poll(keepAliveTime, TimeUnit.NANOSECONDS) : incoming.take();
				if (r != null) {
					return r;
				}
				timedOut = true;
//			} catch (InterruptedException retry) {
//				timedOut = false;
//			}
		}
	}

	/**
	 * Main worker run loop. Repeatedly gets tasks from queue and executes them,
	 * while coping with a number of issues:
	 *
	 * 1. We may start out with an initial task, in which case we don't need to
	 * get the first one. Otherwise, as long as pool is running, we get tasks
	 * from getTask. If it returns null then the worker exits due to changed
	 * pool state or configuration parameters. Other exits result from exception
	 * throws in external code, in which case completedAbruptly holds, which
	 * usually leads processWorkerExit to replace this thread.
	 *
	 * 2. Before running any task, the lock is acquired to prevent other pool
	 * interrupts while the task is executing, and clearInterruptsForTaskRun
	 * called to ensure that unless pool is stopping, this thread does not have
	 * its interrupt set.
	 *
	 * 3. Each task run is preceded by a call to beforeExecute, which might
	 * throw an exception, in which case we cause thread to die (breaking loop
	 * with completedAbruptly true) without processing the task.
	 *
	 * 4. Assuming beforeExecute completes normally, we run the task, gathering
	 * any of its thrown exceptions to send to afterExecute. We separately
	 * handle RuntimeException, Error (both of which the specs guarantee that we
	 * trap) and arbitrary Throwables. Because we cannot rethrow Throwables
	 * within Runnable.run, we wrap them within Errors on the way out (to the
	 * thread's UncaughtExceptionHandler). Any thrown exception also
	 * conservatively causes thread to die.
	 *
	 * 5. After task.run completes, we call afterExecute, which may also throw
	 * an exception, which will also cause thread to die. According to JLS Sec
	 * 14.20, this exception is the one that will be in effect even if task.run
	 * throws.
	 *
	 * The net effect of the exception mechanics is that afterExecute and the
	 * thread's UncaughtExceptionHandler have as accurate information as we can
	 * provide about any problems encountered by user code.
	 *
	 * @param worker the worker
	 */
	final void runWorker(Worker worker) {
		Thread workerThread = Thread.currentThread();
		Runnable task = worker.firstTask;
		worker.firstTask = null;
		worker.unlock(); // allow interrupts
		boolean completedAbruptly = true;
		try {
			while (task != null || (task = getTask()) != null) {
				worker.lock();
				// If pool is stopping, ensure thread is interrupted;
				// if not, ensure thread is not interrupted. This
				// requires a recheck in second case to deal with
				// shutdownNow race while clearing interrupt
				if ((runStateAtLeast(controlState.get(), STOP) || (Thread.interrupted() && runStateAtLeast(controlState.get(), STOP))) && !workerThread.isInterrupted()) {
					workerThread.interrupt();
				}
				try {
					beforeExecute(workerThread, task);
					Throwable thrown = null;
					try {
						task.run();
					} catch (RuntimeException x) {
						thrown = x;
						throw x;
					} catch (Error x) {
						thrown = x;
						throw x;
					} catch (Throwable x) {
						thrown = x;
						throw new Error(x);
					} finally {
						afterExecute(task, thrown);
					}
				} finally {
					task = null;
					worker.completedTasks++;
					worker.unlock();
				}
			}
			completedAbruptly = false;
		} finally {
			processWorkerExit(worker, completedAbruptly);
		}
	}

	// Public constructors and methods

	/**
	 * Creates a new {@code QueueingExecutorService} with the given initial
	 * parameters and default thread factory and rejected execution handler. It
	 * may be more convenient to use one of the {@link Executors} factory
	 * methods instead of this general purpose constructor.
	 *
	 * @param corePoolSize the number of threads to keep in the pool, even if
	 *            they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 * @param keepAliveTime when the number of threads is greater than the core,
	 *            this is the maximum time that excess idle threads will wait
	 *            for new tasks before terminating.
	 * @param unit the time unit for the {@code keepAliveTime} argument
	 * @param workQueue the queue to use for holding tasks before they are
	 *            executed. This queue will hold only the {@code Runnable} tasks
	 *            submitted by the {@code execute} method.
	 * @throws IllegalArgumentException if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException if {@code workQueue} is null
	 */
	public QueueingExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
		BlockingQueue<Runnable> workQueue) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
	}

	/**
	 * Creates a new {@code QueueingExecutorService} with the given initial
	 * parameters and default rejected execution handler.
	 *
	 * @param corePoolSize the number of threads to keep in the pool, even if
	 *            they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 * @param keepAliveTime when the number of threads is greater than the core,
	 *            this is the maximum time that excess idle threads will wait
	 *            for new tasks before terminating.
	 * @param unit the time unit for the {@code keepAliveTime} argument
	 * @param workQueue the queue to use for holding tasks before they are
	 *            executed. This queue will hold only the {@code Runnable} tasks
	 *            submitted by the {@code execute} method.
	 * @param threadFactory the factory to use when the executor creates a new
	 *            thread
	 * @throws IllegalArgumentException if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException if {@code workQueue} or
	 *             {@code threadFactory} is null
	 */
	public QueueingExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
		BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
	}

	/**
	 * Creates a new {@code QueueingExecutorService} with the given initial
	 * parameters and default thread factory.
	 *
	 * @param corePoolSize the number of threads to keep in the pool, even if
	 *            they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 * @param keepAliveTime when the number of threads is greater than the core,
	 *            this is the maximum time that excess idle threads will wait
	 *            for new tasks before terminating.
	 * @param unit the time unit for the {@code keepAliveTime} argument
	 * @param workQueue the queue to use for holding tasks before they are
	 *            executed. This queue will hold only the {@code Runnable} tasks
	 *            submitted by the {@code execute} method.
	 * @param handler the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException if {@code workQueue} or {@code handler} is
	 *             null
	 */
	public QueueingExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
		BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
	}

	/**
	 * Creates a new {@code QueueingExecutorService} with the given initial
	 * parameters.
	 *
	 * @param corePoolSize the number of threads to keep in the pool, even if
	 *            they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 * @param keepAliveTime when the number of threads is greater than the core,
	 *            this is the maximum time that excess idle threads will wait
	 *            for new tasks before terminating.
	 * @param unit the time unit for the {@code keepAliveTime} argument
	 * @param workQueue the queue to use for holding tasks before they are
	 *            executed. This queue will hold only the {@code Runnable} tasks
	 *            submitted by the {@code execute} method.
	 * @param threadFactory the factory to use when the executor creates a new
	 *            thread
	 * @param handler the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException if {@code workQueue} or
	 *             {@code threadFactory} or {@code handler} is null
	 */
	public QueueingExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
		BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
			throw new IllegalArgumentException();
		}
		if (workQueue == null || threadFactory == null || handler == null) {
			throw new NullPointerException();
		}
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.incoming = workQueue;
		this.keepAliveTime = unit.toNanos(keepAliveTime);
		this.threadFactory = threadFactory;
		this.handler = handler;
	}

	/**
	 * Executes the given task sometime in the future. The task may execute in a
	 * new thread or in an existing pooled thread.
	 *
	 * If the task cannot be submitted for execution, either because this
	 * executor has been shutdown or because its capacity has been reached, the
	 * task is handled by the current {@code RejectedExecutionHandler}.
	 *
	 * @param command the task to execute
	 * @throws RejectedExecutionException at discretion of
	 *             {@code RejectedExecutionHandler}, if the task cannot be
	 *             accepted for execution
	 * @throws NullPointerException if {@code command} is null
	 */
	@Override
	public void execute(Runnable command) {
		if (command == null) {
			throw new NullPointerException();
		}
		/*
		 * Proceed in 3 steps:
		 *
		 * 1. If fewer than corePoolSize threads are running, try to start a new
		 * thread with the given command as its first task. The call to
		 * addWorker atomically checks runState and workerCount, and so prevents
		 * false alarms that would add threads when it shouldn't, by returning
		 * false.
		 *
		 * 2. If a task can be successfully queued, then we still need to
		 * double-check whether we should have added a thread (because existing
		 * ones died since last checking) or that the pool shut down since entry
		 * into this method. So we recheck state and if necessary roll back the
		 * enqueuing if stopped, or start a new thread if there are none.
		 *
		 * 3. If we cannot queue task, then we try to add a new thread. If it
		 * fails, we know we are shut down or saturated and so reject the task.
		 */
		int c = controlState.get();
		if (workerCountOf(c) < corePoolSize) {
			if (addWorker(command, true)) {
				return;
			}
			c = controlState.get();
		}
		if (isRunning(c) && incoming.offer(command)) {
			int recheck = controlState.get();
			if (!isRunning(recheck) && remove(command)) {
				reject(command);
			} else if (workerCountOf(recheck) == 0) {
				addWorker(null, false);
			}
		} else if (!addWorker(command, false)) {
			reject(command);
		}
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are
	 * executed, but no new tasks will be accepted. Invocation has no additional
	 * effect if already shut down.
	 *
	 * <p>
	 * This method does not wait for previously submitted tasks to complete
	 * execution. Use {@link #awaitTermination awaitTermination} to do that.
	 *
	 */
	@Override
	public void shutdown() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			advanceRunState(SHUTDOWN);
			interruptIdleWorkers();
			onShutdown();
		} finally {
			mainLock.unlock();
		}
		tryTerminate();
	}

	/**
	 * Attempts to stop all actively executing tasks, halts the processing of
	 * waiting tasks, and returns a list of the tasks that were awaiting
	 * execution. These tasks are drained (removed) from the task queue upon
	 * return from this method.
	 *
	 * <p>
	 * This method does not wait for actively executing tasks to terminate. Use
	 * {@link #awaitTermination awaitTermination} to do that.
	 *
	 * <p>
	 * There are no guarantees beyond best-effort attempts to stop processing
	 * actively executing tasks. This implementation cancels tasks via
	 * {@link Thread#interrupt}, so any task that fails to respond to interrupts
	 * may never terminate.
	 *
	 */
	@Override
	public List<Runnable> shutdownNow() {
		List<Runnable> tasks;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			advanceRunState(STOP);
			interruptWorkers();
//			tasks = drainQueue(); // TODO: (Nad) replace..
		} finally {
			mainLock.unlock();
		}
		tryTerminate();
//		return tasks;
		return null;
	}

	@Override
	public boolean isShutdown() {
		return !isRunning(controlState.get());
	}

	/**
	 * Returns true if this executor is in the process of terminating after
	 * {@link #shutdown} or {@link #shutdownNow} but has not completely
	 * terminated. This method may be useful for debugging. A return of
	 * {@code true} reported a sufficient period after shutdown may indicate
	 * that submitted tasks have ignored or suppressed interruption, causing
	 * this executor not to properly terminate.
	 *
	 * @return true if terminating but not yet terminated
	 */
	public boolean isTerminating() {
		int c = controlState.get();
		return !isRunning(c) && runStateLessThan(c, TERMINATED);
	}

	@Override
	public boolean isTerminated() {
		return runStateAtLeast(controlState.get(), TERMINATED);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (;;) {
				if (runStateAtLeast(controlState.get(), TERMINATED)) {
					return true;
				}
				if (nanos <= 0) {
					return false;
				}
				nanos = termination.awaitNanos(nanos);
			}
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Invokes {@code shutdown} when this executor is no longer referenced and
	 * it has no threads.
	 */
	@Override
	protected void finalize() {
		shutdown();
	}

	/**
	 * Sets the thread factory used to create new threads.
	 *
	 * @param threadFactory the new thread factory
	 * @throws NullPointerException if threadFactory is null
	 * @see #getThreadFactory
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		if (threadFactory == null) {
			throw new NullPointerException();
		}
		this.threadFactory = threadFactory;
	}

	/**
	 * Returns the thread factory used to create new threads.
	 *
	 * @return the current thread factory
	 * @see #setThreadFactory
	 */
	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}

	/**
	 * Sets a new handler for unexecutable tasks.
	 *
	 * @param handler the new handler
	 * @throws NullPointerException if handler is null
	 * @see #getRejectedExecutionHandler
	 */
	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
	}

	/**
	 * Returns the current handler for unexecutable tasks.
	 *
	 * @return the current handler
	 * @see #setRejectedExecutionHandler
	 */
	public RejectedExecutionHandler getRejectedExecutionHandler() {
		return handler;
	}

	/**
	 * Sets the core number of threads. This overrides any value set in the
	 * constructor. If the new value is smaller than the current value, excess
	 * existing threads will be terminated when they next become idle. If
	 * larger, new threads will, if needed, be started to execute any queued
	 * tasks.
	 *
	 * @param corePoolSize the new core size
	 * @throws IllegalArgumentException if {@code corePoolSize < 0}
	 * @see #getCorePoolSize
	 */
	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0) {
			throw new IllegalArgumentException();
		}
		int delta = corePoolSize - this.corePoolSize;
		this.corePoolSize = corePoolSize;
		if (workerCountOf(controlState.get()) > corePoolSize) {
			interruptIdleWorkers();
		} else if (delta > 0) {
			// We don't really know how many new threads are "needed".
			// As a heuristic, prestart enough new workers (up to new
			// core size) to handle the current number of tasks in
			// queue, but stop if queue becomes empty while doing so.
			int k = Math.min(delta, incoming.size());
			while (k-- > 0 && addWorker(null, true)) {
				if (incoming.isEmpty()) {
					break;
				}
			}
		}
	}

	/**
	 * Returns the core number of threads.
	 *
	 * @return the core number of threads
	 * @see #setCorePoolSize
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}

	/**
	 * Starts a core thread, causing it to idly wait for work. This overrides
	 * the default policy of starting core threads only when new tasks are
	 * executed. This method will return {@code false} if all core threads have
	 * already been started.
	 *
	 * @return {@code true} if a thread was started
	 */
	public boolean prestartCoreThread() {
		return workerCountOf(controlState.get()) < corePoolSize && addWorker(null, true);
	}

	/**
	 * Same as prestartCoreThread except arranges that at least one thread is
	 * started even if corePoolSize is 0.
	 */
	void ensurePrestart() {
		int wc = workerCountOf(controlState.get());
		if (wc < corePoolSize) {
			addWorker(null, true);
		} else if (wc == 0) {
			addWorker(null, false);
		}
	}

	/**
	 * Starts all core threads, causing them to idly wait for work. This
	 * overrides the default policy of starting core threads only when new tasks
	 * are executed.
	 *
	 * @return the number of threads started
	 */
	public int prestartAllCoreThreads() {
		int n = 0;
		while (addWorker(null, true)) {
			++n;
		}
		return n;
	}

	/**
	 * Returns true if this pool allows core threads to time out and terminate
	 * if no tasks arrive within the keepAlive time, being replaced if needed
	 * when new tasks arrive. When true, the same keep-alive policy applying to
	 * non-core threads applies also to core threads. When false (the default),
	 * core threads are never terminated due to lack of incoming tasks.
	 *
	 * @return {@code true} if core threads are allowed to time out, else
	 *         {@code false}
	 *
	 * @since 1.6
	 */
	public boolean allowsCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	/**
	 * Sets the policy governing whether core threads may time out and terminate
	 * if no tasks arrive within the keep-alive time, being replaced if needed
	 * when new tasks arrive. When false, core threads are never terminated due
	 * to lack of incoming tasks. When true, the same keep-alive policy applying
	 * to non-core threads applies also to core threads. To avoid continual
	 * thread replacement, the keep-alive time must be greater than zero when
	 * setting {@code true}. This method should in general be called before the
	 * pool is actively used.
	 *
	 * @param value {@code true} if should time out, else {@code false}
	 * @throws IllegalArgumentException if value is {@code true} and the current
	 *             keep-alive time is not greater than zero
	 *
	 * @since 1.6
	 */
	public void allowCoreThreadTimeOut(boolean value) {
		if (value && keepAliveTime <= 0) {
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		if (value != allowCoreThreadTimeOut) {
			allowCoreThreadTimeOut = value;
			if (value) {
				interruptIdleWorkers();
			}
		}
	}

	/**
	 * Sets the maximum allowed number of threads. This overrides any value set
	 * in the constructor. If the new value is smaller than the current value,
	 * excess existing threads will be terminated when they next become idle.
	 *
	 * @param maximumPoolSize the new maximum
	 * @throws IllegalArgumentException if the new maximum is less than or equal
	 *             to zero, or less than the {@linkplain #getCorePoolSize core
	 *             pool size}
	 * @see #getMaximumPoolSize
	 */
	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
			throw new IllegalArgumentException();
		}
		this.maximumPoolSize = maximumPoolSize;
		if (workerCountOf(controlState.get()) > maximumPoolSize) {
			interruptIdleWorkers();
		}
	}

	/**
	 * Returns the maximum allowed number of threads.
	 *
	 * @return the maximum allowed number of threads
	 * @see #setMaximumPoolSize
	 */
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	/**
	 * Sets the time limit for which threads may remain idle before being
	 * terminated. If there are more than the core number of threads currently
	 * in the pool, after waiting this amount of time without processing a task,
	 * excess threads will be terminated. This overrides any value set in the
	 * constructor.
	 *
	 * @param time the time to wait. A time value of zero will cause excess
	 *            threads to terminate immediately after executing tasks.
	 * @param unit the time unit of the {@code time} argument
	 * @throws IllegalArgumentException if {@code time} less than zero or if
	 *             {@code time} is zero and {@code allowsCoreThreadTimeOut}
	 * @see #getKeepAliveTime
	 */
	public void setKeepAliveTime(long time, TimeUnit unit) {
		if (time < 0) {
			throw new IllegalArgumentException();
		}
		if (time == 0 && allowsCoreThreadTimeOut()) {
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		long keepAliveTime = unit.toNanos(time);
		long delta = keepAliveTime - this.keepAliveTime;
		this.keepAliveTime = keepAliveTime;
		if (delta < 0) {
			interruptIdleWorkers();
		}
	}

	/**
	 * Returns the thread keep-alive time, which is the amount of time that
	 * threads in excess of the core pool size may remain idle before being
	 * terminated.
	 *
	 * @param unit the desired time unit of the result
	 * @return the time limit
	 * @see #setKeepAliveTime
	 */
	public long getKeepAliveTime(TimeUnit unit) {
		return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
	}

	/* User-level queue utilities */

	/**
	 * Returns the task queue used by this executor. Access to the task queue is
	 * intended primarily for debugging and monitoring. This queue may be in
	 * active use. Retrieving the task queue does not prevent queued tasks from
	 * executing.
	 *
	 * @return the task queue
	 */
	public Queue<Runnable> getQueue() {
		return incoming;
	}

	/**
	 * Removes this task from the executor's internal queue if it is present,
	 * thus causing it not to be run if it has not already started.
	 *
	 * <p>
	 * This method may be useful as one part of a cancellation scheme. It may
	 * fail to remove tasks that have been converted into other forms before
	 * being placed on the internal queue. For example, a task entered using
	 * {@code submit} might be converted into a form that maintains
	 * {@code Future} status. However, in such cases, method {@link #purge} may
	 * be used to remove those Futures that have been cancelled.
	 *
	 * @param task the task to remove
	 * @return true if the task was removed
	 */
	public boolean remove(Runnable task) {
		boolean removed = incoming.remove(task);
		tryTerminate(); // In case SHUTDOWN and now empty
		return removed;
	}

	/**
	 * Tries to remove from the work queue all {@link Future} tasks that have
	 * been cancelled. This method can be useful as a storage reclamation
	 * operation, that has no other impact on functionality. Cancelled tasks are
	 * never executed, but may accumulate in work queues until worker threads
	 * can actively remove them. Invoking this method instead tries to remove
	 * them now. However, this method may fail to remove tasks in the presence
	 * of interference by other threads.
	 */
	public void purge() {
		final Queue<Runnable> q = incoming;
		try {
			Iterator<Runnable> it = q.iterator();
			while (it.hasNext()) {
				Runnable r = it.next();
				if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
					it.remove();
				}
			}
		} catch (ConcurrentModificationException fallThrough) {
			// Take slow path if we encounter interference during traversal.
			// Make copy for traversal and call remove for cancelled entries.
			// The slow path is more likely to be O(N*N).
			for (Object r : q.toArray()) {
				if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
					q.remove(r);
				}
			}
		}

		tryTerminate(); // In case SHUTDOWN and now empty
	}

	/* Statistics */

	/**
	 * Returns the current number of threads in the pool.
	 *
	 * @return the number of threads
	 */
	public int getPoolSize() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			// Remove rare and surprising possibility of
			// isTerminated() && getPoolSize() > 0
			return runStateAtLeast(controlState.get(), TIDYING) ? 0 : workers.size();
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Returns the approximate number of threads that are actively executing
	 * tasks.
	 *
	 * @return the number of threads
	 */
	public int getActiveCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			int n = 0;
			for (Worker w : workers) {
				if (w.isLocked()) {
					++n;
				}
			}
			return n;
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Returns the largest number of threads that have ever simultaneously been
	 * in the pool.
	 *
	 * @return the number of threads
	 */
	public int getLargestPoolSize() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			return largestPoolSize;
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Returns the approximate total number of tasks that have ever been
	 * scheduled for execution. Because the states of tasks and threads may
	 * change dynamically during computation, the returned value is only an
	 * approximation.
	 *
	 * @return the number of tasks
	 */
	public long getTaskCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			long n = completedTaskCount;
			for (Worker w : workers) {
				n += w.completedTasks;
				if (w.isLocked()) {
					++n;
				}
			}
			return n + incoming.size();
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Returns the approximate total number of tasks that have completed
	 * execution. Because the states of tasks and threads may change dynamically
	 * during computation, the returned value is only an approximation, but one
	 * that does not ever decrease across successive calls.
	 *
	 * @return the number of tasks
	 */
	public long getCompletedTaskCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			long n = completedTaskCount;
			for (Worker w : workers) {
				n += w.completedTasks;
			}
			return n;
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Returns a string identifying this pool, as well as its state, including
	 * indications of run state and estimated worker and task counts.
	 *
	 * @return a string identifying this pool, as well as its state
	 */
	@Override
	public String toString() {
		long ncompleted;
		int nworkers, nactive;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			ncompleted = completedTaskCount;
			nactive = 0;
			nworkers = workers.size();
			for (Worker w : workers) {
				ncompleted += w.completedTasks;
				if (w.isLocked()) {
					++nactive;
				}
			}
		} finally {
			mainLock.unlock();
		}
		int c = controlState.get();
		String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" : (runStateAtLeast(c, TERMINATED) ? "Terminated" : "Shutting down"));
		return super.toString() + "[" + rs + ", pool size = " + nworkers + ", active threads = " + nactive + ", queued tasks = " +
			incoming.size() + ", completed tasks = " + ncompleted + "]";
	}

	/* Extension hooks */

	/**
	 * Method invoked prior to executing the given Runnable in the given thread.
	 * This method is invoked by thread {@code t} that will execute task
	 * {@code r}, and may be used to re-initialize ThreadLocals, or to perform
	 * logging.
	 *
	 * <p>
	 * This implementation does nothing, but may be customized in subclasses.
	 * Note: To properly nest multiple overridings, subclasses should generally
	 * invoke {@code super.beforeExecute} at the end of this method.
	 *
	 * @param t the thread that will run task {@code r}
	 * @param r the task that will be executed
	 */
	protected void beforeExecute(Thread t, Runnable r) {
	}

	/**
	 * Method invoked upon completion of execution of the given Runnable. This
	 * method is invoked by the thread that executed the task. If non-null, the
	 * Throwable is the uncaught {@code RuntimeException} or {@code Error} that
	 * caused execution to terminate abruptly.
	 *
	 * <p>
	 * This implementation does nothing, but may be customized in subclasses.
	 * Note: To properly nest multiple overridings, subclasses should generally
	 * invoke {@code super.afterExecute} at the beginning of this method.
	 *
	 * <p>
	 * <b>Note:</b> When actions are enclosed in tasks (such as
	 * {@link FutureTask}) either explicitly or via methods such as
	 * {@code submit}, these task objects catch and maintain computational
	 * exceptions, and so they do not cause abrupt termination, and the internal
	 * exceptions are <em>not</em> passed to this method. If you would like to
	 * trap both kinds of failures in this method, you can further probe for
	 * such cases, as in this sample subclass that prints either the direct
	 * cause or the underlying exception if a task has been aborted:
	 *
	 * <pre>
	 *
	 * {
	 * 	&#064;code
	 * 	class ExtendedExecutor extends QueueingExecutorService {
	 *
	 * 		// ...
	 * 		protected void afterExecute(Runnable r, Throwable t) {
	 * 			super.afterExecute(r, t);
	 * 			if (t == null &amp;&amp; r instanceof Future&lt;?&gt;) {
	 * 				try {
	 * 					Object result = ((Future&lt;?&gt;) r).get();
	 * 				} catch (CancellationException ce) {
	 * 					t = ce;
	 * 				} catch (ExecutionException ee) {
	 * 					t = ee.getCause();
	 * 				} catch (InterruptedException ie) {
	 * 					Thread.currentThread().interrupt(); // ignore/reset
	 * 				}
	 * 			}
	 * 			if (t != null)
	 * 				System.out.println(t);
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param r the runnable that has completed
	 * @param t the exception that caused termination, or null if execution
	 *            completed normally
	 */
	protected void afterExecute(Runnable r, Throwable t) {
	}

	/**
	 * Method invoked when the Executor has terminated. Default implementation
	 * does nothing. Note: To properly nest multiple overridings, subclasses
	 * should generally invoke {@code super.terminated} within this method.
	 */
	protected void terminated() {
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopAndWait(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ServiceState getServiceState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean awaitStop(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}
}
