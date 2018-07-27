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
package net.pms.util;

import javax.annotation.concurrent.Immutable;


/**
 * An interface for any object that can expire.
 *
 * @author Nadahar
 */
@Immutable
public interface Expirable {

	/**
	 * @return {@code true} if this instance is currently expired, {@code false}
	 *         otherwise.
	 */
	public boolean isExpired();

	/**
	 * @return The expiration time in milliseconds since midnight, January 1,
	 *         1970.
	 */
	public long getExpirationTime();


	/**
	 * A basic implementation of the {@link Expirable} interface. Even though
	 * not technically abstract, it's meant to be subclassed.
	 *
	 * @author Nadahar
	 */
	@Immutable
	public static class BasicExpirable implements Expirable {

		/** The expiration time */
		protected final long expires;

		/**
		 * Creates a new instance with the specified expiration time.
		 *
		 * @param expires the expiration time in milliseconds since midnight,
		 *            January 1, 1970.
		 */
		protected BasicExpirable(long expires) {
			this.expires = expires;
		}

		@Override
		public boolean isExpired() {
			return expires <= System.currentTimeMillis();
		}

		@Override
		public long getExpirationTime() {
			return expires;
		}

		@Override
		public String toString() {
			return (isExpired() ? "Expired: " : "Expires: ") + StringUtil.formatDateTime(expires);
		}
	}
}
