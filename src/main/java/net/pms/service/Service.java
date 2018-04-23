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


/**
 * This interface must be implemented by all instances managed by
 * {@link Services}. {@link #start()} must be called from the default
 * constructor.
 *
 * @author Nadahar
 */
public interface Service {

	/**
	 * Starts this {@link Service}. This will be called automatically by the
	 * constructor, and need only be called if {@link #stop()} has been called
	 * previously.
	 */
	public void start();

	/**
	 * Stops this {@link Service}. This will cause the {@link Service} to
	 * terminate all operations.
	 */
	public void stop();

	/**
	 * @return {@code true} if this {@link Service} is currently started,
	 *         {@code false} otherwise.
	 */
	public boolean isAlive();
}
