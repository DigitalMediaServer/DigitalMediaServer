package net.pms.util;

import javax.annotation.concurrent.Immutable;


@Immutable
public interface Expirable {

	public boolean isExpired();

	public long getExpiryTime();


	@Immutable
	public static abstract class AbstractExpirable implements Expirable {
		protected final long expires;

		protected AbstractExpirable(long expires) {
			this.expires = expires;
		}

		@Override
		public boolean isExpired() {
			return expires <= System.currentTimeMillis();
		}

		@Override
		public long getExpiryTime() {
			return expires;
		}

		@Override
		public String toString() {
			return (isExpired() ? "Expired: " : "Expires: ") + StringUtil.formatDateTime(expires);
		}
	}
}