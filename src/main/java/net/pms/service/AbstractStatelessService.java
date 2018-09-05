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
import javax.annotation.concurrent.ThreadSafe;

/**
 * This is an abstract {@link Service} implementation for stateless services. It
 * has no abstract methods, and simply deals with the boilerplate
 * {@link Service} implementation code. It will always report
 * {@link ServiceState#RUNNING}
 *
 * @author Nadahar
 */
@ThreadSafe
public abstract class AbstractStatelessService implements Service {

	/**
	 * This {@link Service} is stateless and can't be started.
	 *
	 * @return {@code true}.
	 */
	@Override
	public boolean start() {
		return true;
	}

	/**
	 * This {@link Service} is stateless and can't be stopped.
	 *
	 * @return {@code false}.
	 */
	@Override
	public boolean stop() {
		return false;
	}

	/**
	 * This {@link Service} is stateless and can't be stopped. Always returns
	 * immediately.
	 *
	 * @return {@code false}.
	 */
	@Override
	public boolean stopAndWait(long timeout, TimeUnit unit) {
		return false;
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public boolean isStopped() {
		return false;
	}

	@Override
	public boolean isStopping() {
		return false;
	}

	@Override
	public boolean isError() {
		return false;
	}

	@Override
	public ServiceState getServiceState() {
		return ServiceState.RUNNING;
	}

	/**
	 * This {@link Service} is stateless and can't be stopped. Always returns
	 * immediately.
	 *
	 * @return {@code false}.
	 */
	@Override
	public boolean awaitStop(long timeout, @Nonnull TimeUnit unit) {
		return false;
	}
}
