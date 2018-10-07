/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.service;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to manage the shutdown of external processes if they run //TODO: (Nad) JavaDocs
 * longer than their expected run time or hangs. It uses its own thread and
 * internal scheduling to shut down managed processes once their run time
 * expires. A graceful shutdown is initially escalating to less graceful methods
 * until successful. If nothing works, the shutdown is left to the JVM with
 * {@link Process#destroy()} with its known shortcomings.
 *
 * @author Nadahar
 */
@ThreadSafe
public abstract class AbstractSynchronizedService implements Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSynchronizedService.class);

	/** The synchronization object */
	protected final Object lock = new Object();

	/** The {@link ServiceState} of this {@link Service} */
	@GuardedBy("lock")
	@Nonnull
	protected ServiceState state = ServiceState.STOPPED;

	/**
	 * Attempts to start this {@link Service}.
	 * <p>
	 * If the current state is {@link ServiceState#STOPPING}, this method will
	 * wait for the state to reach {@link ServiceState#STOPPED} before starting.
	 *
	 * @return {@code true} if this {@link Service} started, {@code false} if it
	 *         failed to start or the state was already
	 *         {@link ServiceState#RUNNING}.
	 */
	@Override
	public boolean start() {
		return start(30, TimeUnit.SECONDS);
	}

	/**
	 * Attempts to start this {@link Service}.
	 * <p>
	 * If the current state is {@link ServiceState#STOPPING}, this method will
	 * wait for the state to reach {@link ServiceState#STOPPED} before starting.
	 *
	 * @param timeout the maximum time to wait for this {@link Service} to stop.
	 * @param unit the {@link TimeUnit} of the {@code timeout} argument.
	 * @return {@code true} if this {@link Service} started, {@code false} if it
	 *         failed to start or the state was already
	 *         {@link ServiceState#RUNNING}.
	 */
	public boolean start(long timeout, @Nonnull TimeUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit cannot be null");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout cannot be negative");
		}
		boolean result;
		synchronized (lock) {
			if (state == ServiceState.ERROR && !clearErrorOnStart()) {
				LOGGER.error("Attempt to start {} was rejected because it is in an error state", getClass().getSimpleName());
				return false;
			}
			if (state == ServiceState.STOPPING) {
				try {
					awaitStop(timeout, unit);
				} catch (InterruptedException e) {
					LOGGER.warn("{} was interrupted while waiting to start", getClass().getSimpleName());
				}
			}
			if (state == ServiceState.STOPPING) {
				LOGGER.error(
					"Timed out while waiting for the previous {} to stop, start attempt failed",
					getClass().getSimpleName()
				);
				return false;
			} else if (state == ServiceState.RUNNING && LOGGER.isDebugEnabled()) {
				LOGGER.warn("{} is already running, start attempt failed", getClass().getSimpleName());
				return false;
			}
			result = doStart();
			if (result) {
				state = ServiceState.RUNNING;
			}
		}
		return result;
	}

	/**
	 * @return {@code true} if the implementation allows
	 *         {@link #start(long, TimeUnit)} to clear an error state,
	 *         {@code false} otherwise.
	 */
	protected boolean clearErrorOnStart() {
		return false;
	}

	/**
	 * Starts this {@link Service}. Always called with a lock on {@code lock} by
	 * {@link AbstractSynchronizedService}.
	 *
	 * @return {@code true} if this {@link Service} started, {@code false} if it
	 *         failed to start.
	 */
	@GuardedBy("lock")
	protected abstract boolean doStart();

	@Override
	public boolean stop() {
		boolean result;
		synchronized (lock) {
			if (state != ServiceState.RUNNING) {
				if (state == ServiceState.ERROR) {
					LOGGER.error(
						"Failed to initialize shutdown of {} because it is in an error state",
						getClass().getSimpleName()
					);
				}
				return false;
			}
			result = doStop();
			if (result) {
				state = ServiceState.STOPPING;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			if (result) {
				LOGGER.debug("Stopping {}", getClass().getSimpleName());
			} else {
				LOGGER.warn("Failed to initialize shutdown of {}", getClass().getSimpleName());
			}
		}
		return result;
	}

	/**
	 * Initializes the shutdown procedure. Always called with a lock on
	 * {@code lock} by {@link AbstractSynchronizedService}.
	 *
	 * @return {@code true} if the stop sequence was successfully initiated,
	 *         {@code false} otherwise.
	 */
	@GuardedBy("lock")
	protected abstract boolean doStop();

	/**
	 * Sets the state to {@link ServiceState#STOPPING}.
	 */
	protected void setStopping() {
		synchronized (lock) {
			state = ServiceState.STOPPING;
		}
	}

	/**
	 * Performs potential cleanup operation and sets the state to
	 * {@link ServiceState#STOPPED} before notifying any waiting threads that
	 * the {@link Service} has stopped.
	 */
	protected void setStopped() {
		synchronized (lock) {
			doSetStopped();
			state = ServiceState.STOPPED;
			lock.notifyAll();
		}
	}

	/**
	 * Prototype method that performs cleanup operations after stopping. Always
	 * called with a lock on {@code lock} by {@link AbstractSynchronizedService}.
	 */
	@GuardedBy("lock")
	protected void doSetStopped() {
	}

	@Override
	public boolean stopAndWait(long timeout, TimeUnit unit) throws InterruptedException {
		if (unit == null) {
			throw new IllegalArgumentException("unit cannot be null");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout cannot be negative");
		}
		synchronized (lock) {
			if (state == ServiceState.STOPPED) {
				return true;
			}
			stop();
			return awaitStop(timeout, unit);
		}
	}

	/**
	 * Attempts to restart this {@link Service}. This method will trigger a
	 * shutdown and block until {@link ServiceState#STOPPED} is reached or the
	 * specified timeout expires. Only if {@link ServiceState#STOPPED} was
	 * reached, a start attempt will be made.
	 *
	 * @param timeout the maximum time to wait for this {@link Service} to stop.
	 * @param unit the {@link TimeUnit} of the {@code timeout} argument.
	 * @return {@code true} if this operation succeeded, false if either the
	 *         shutdown timed out or the start failed.
	 * @throws InterruptedException If the thread was interrupted while waiting
	 *             for this {@link Service} to stop.
	 */
	public boolean restart(long timeout, TimeUnit unit) {
		boolean result;
		synchronized (lock) {
			try {
				result = stopAndWait(timeout, unit);
			} catch (InterruptedException e) {
				LOGGER.debug("{} was interrupted during restart", getClass().getSimpleName());
				return false;
			}
			if (!result) {
				return false;
			}
			result = start();
		}
		return result;
	}

	@Override
	public boolean isRunning() {
		synchronized (lock) {
			return state == ServiceState.RUNNING;
		}
	}

	@Override
	public boolean isStopped() {
		synchronized (lock) {
			return state == ServiceState.STOPPED;
		}
	}

	@Override
	public boolean isStopping() {
		synchronized (lock) {
			return state == ServiceState.STOPPING;
		}
	}

	@Override
	public boolean isError() {
		synchronized (lock) {
			return state == ServiceState.ERROR;
		}
	}

	@Override
	public ServiceState getServiceState() {
		synchronized (lock) {
			return state;
		}
	}

	@Override
	public boolean awaitStop(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
		if (unit == null) {
			throw new IllegalArgumentException("unit cannot be null");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout cannot be negative");
		}

		long nanos = unit.toNanos(timeout);
		long millis;
		if (nanos == Long.MAX_VALUE) {
			// Ignore the nanoseconds
			nanos = 0;
			millis = unit.toMillis(timeout);
		} else {
			millis = nanos / 1000000; //TODO: (Nad) Check
			nanos %= 1000000;
			// Since wait() can't handle nanoseconds, it's rounded to the closest millisecond.
			if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
				millis++;
			}
		}
		long endTime = System.currentTimeMillis() + millis;

		synchronized (lock) {
			for (;;) {
				if (state == ServiceState.ERROR) {
					LOGGER.error("Aborted waiting for {} to stop since it is in an error state", getClass().getSimpleName());
					return false;
				}
				if (state == ServiceState.STOPPED) {
					return true;
				} else if (System.currentTimeMillis() >= endTime) {
					return false;
				}
				lock.wait(millis);
			}
		}
	}
}
