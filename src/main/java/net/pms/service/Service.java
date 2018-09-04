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


/**
 * This interface must be implemented by all instances managed by
 * {@link Services}. {@link #start()} must be called from the default
 * constructor.
 *
 * @author Nadahar
 */
public interface Service {

	/**
	 * Attempts to start this {@link Service}.
	 *
	 * @return {@code true} if this {@link Service} started, {@code false} if it
	 *         failed to start or the state was already
	 *         {@link ServiceState#RUNNING}.
	 */
	public boolean start();

	/**
	 * Attempts to stop this {@link Service}. This will cause the
	 * {@link Service} to terminate all operations.
	 *
	 * @return {@code true} if the {@link ServiceState} was
	 *         {@link ServiceState#RUNNING} and the stop sequence was
	 *         successfully initiated, {@code false} otherwise.
	 */
	public boolean stop();

	/**
	 * Attempts to make sure that this {@link Service} reaches the
	 * {@link ServiceState#STOPPED} state before returning. This will cause the
	 * {@link Service} to terminate all operation. If the {@link Service} is
	 * already stopped, this method will return immediately.
	 *
	 * @param timeout the maximum time to wait.
	 * @param unit the {@link TimeUnit} of the {@code timeout} argument.
	 * @return {@code true} if this {@link Service} was or reached the
	 *         {@link ServiceState#STOPPED} state, {@code false} it the
	 *         operation timed out.
	 * @throws InterruptedException If the thread was interrupted while waiting.
	 */
	public boolean stopAndWait(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * @return {@code true} if this {@link Service} is currently running,
	 *         {@code false} otherwise.
	 */
	public boolean isRunning();

	/**
	 * @return {@code true} if this {@link Service} is currently stopping,
	 *         {@code false} otherwise.
	 */
	public boolean isStopping();

	/**
	 * @return {@code true} if this {@link Service} is currently stopped,
	 *         {@code false} otherwise.
	 */
	public boolean isStopped();

	/**
	 * @return The current {@link ServiceState} of this {@link Service}.
	 */
	public ServiceState getServiceState();

	/**
	 * Waits for this {@link Service} to stop. <b>This method does not initiate
	 * stopping of the {@link Service}, so to avoid waiting "forever" make sure
	 * that {@link #stop()} has been or will be called</b>. If the
	 * {@link Service} is already stopped, this method returns immediately.
	 * <p>
	 * To initiate stopping and wait, use {@link #stopAndWait(long, TimeUnit)}
	 * instead.
	 *
	 * @param timeout the maximum time to wait.
	 * @param unit the {@link TimeUnit} of the {@code timeout} argument.
	 * @return {@code true} if the {@link Service} was stopped, {@code false} if
	 *         the operation timed out.
	 * @throws InterruptedException If the thread was interrupted while waiting.
	 */
	public boolean awaitStop(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * The {@link Service} run states.
	 */
	public enum ServiceState {

		/** The {@link Service} is running */
		RUNNING,

		/** The {@link Service} is stopping */
		STOPPING,

		/** The {@link Service} is stopped */
		STOPPED;
	}
}
