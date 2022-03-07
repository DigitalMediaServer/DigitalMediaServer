/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2022 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.logging;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.Compressor;
import java.io.File;


/**
 * A {@link RollingPolicy} that is tied to {@link AppendingZipCompressor}.
 */
public class AppendingZipRollingPolicy extends RollingPolicyBase {

	/** The {@link Compressor} to use */
	protected final AppendingZipCompressor compressor = new AppendingZipCompressor();

	@Override
	public void rollover() throws RolloverFailure {
		compressor.compress(getActiveFileName(), getActiveFileName() + ".zip", new File(getActiveFileName()).getName());
	}

	@Override
	public String getActiveFileName() {
		return getParentsRawFileProperty();
	}

	@Override
	public CompressionMode getCompressionMode() {
		return CompressionMode.ZIP;
	}

	/**
	 * Sets the {@link Context}.
	 *
	 * @param context the {@link Context} to set.
	 * @throws IllegalStateException If the context has already been set.
	 */
	@Override
	public void setContext(Context context) {
		super.setContext(context);
		if (this.context != null) {
			compressor.setContext(this.context);
		}
	}
}
