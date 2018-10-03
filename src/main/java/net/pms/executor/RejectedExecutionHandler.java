package net.pms.executor;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;

public interface RejectedExecutionHandler {

	void rejectedExecution(Runnable runnable, AbstractExecutorService executor);

	/**
	 * A handler for rejected tasks that throws a
	 * {@code RejectedExecutionException}.
	 */
	public static class AbortPolicy implements RejectedExecutionHandler { //TODO: (Nad) Service Clean up reject handlers

		/**
		 * Creates an {@code AbortPolicy}.
		 */
		public AbortPolicy() {
		}

		/**
		 * Always throws RejectedExecutionException.
		 *
		 * @param r the runnable task requested to be executed
		 * @param e the executor attempting to execute this task
		 * @throws RejectedExecutionException always.
		 */
		@Override
		public void rejectedExecution(Runnable runnable, AbstractExecutorService executor) {
			throw new RejectedExecutionException("Task " + runnable + " was rejected from " + executor);
		}
	}

	/**
	 * A handler for rejected tasks that runs the rejected task directly in the
	 * calling thread of the {@code execute} method, unless the executor has
	 * been shut down, in which case the task is discarded.
	 */
	public static class CallerRunsPolicy implements RejectedExecutionHandler {

		/**
		 * Creates a {@code CallerRunsPolicy}.
		 */
		public CallerRunsPolicy() {
		}

		/**
		 * Executes task r in the caller's thread, unless the executor has been
		 * shut down, in which case the task is discarded.
		 *
		 * @param runnable the runnable task requested to be executed
		 * @param executor the executor attempting to execute this task
		 */
		@Override
		public void rejectedExecution(Runnable runnable, AbstractExecutorService executor) {
			if (!executor.isShutdown()) {
				runnable.run();
			}
		}
	}

	/**
	 * A handler for rejected tasks that silently discards the rejected task.
	 */
	public static class DiscardPolicy implements RejectedExecutionHandler {

		/**
		 * Creates a {@code DiscardPolicy}.
		 */
		public DiscardPolicy() {
		}

		/**
		 * Does nothing, which has the effect of discarding task r.
		 *
		 * @param r the runnable task requested to be executed
		 * @param e the executor attempting to execute this task
		 */
		@Override
		public void rejectedExecution(Runnable runnable, AbstractExecutorService executor) {
		}
	}

}
