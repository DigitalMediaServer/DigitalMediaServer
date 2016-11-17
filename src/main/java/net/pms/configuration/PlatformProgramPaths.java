/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2016 Digital Media Server developers.
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
package net.pms.configuration;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import com.sun.jna.Platform;
import net.pms.util.FileUtil;
import net.pms.util.PropertiesUtil;

public abstract class PlatformProgramPaths {
	public abstract PlatformExecutableInfo getFFmpeg();
	public abstract PlatformExecutableInfo getMPlayer();
	public abstract PlatformExecutableInfo getVLC();
	public abstract PlatformExecutableInfo getMEncoder();
	public abstract PlatformExecutableInfo gettsMuxeR();
	public abstract PlatformExecutableInfo gettsMuxeRNew();
	public abstract PlatformExecutableInfo getFLAC();
	public abstract PlatformExecutableInfo getDCRaw();
	public abstract PlatformExecutableInfo getInterFrame();

	protected static final String BINARIES_PATH = getBinariesPath();
	protected static final String PLATFORM_BINARIES_PATH;

	static {
		if (Platform.isWindows()) {
			PLATFORM_BINARIES_PATH = BINARIES_PATH + "win32" + File.separator;
		} else if (Platform.isMac()) {
			PLATFORM_BINARIES_PATH = BINARIES_PATH + "osx" + File.separator;
		} else {
			PLATFORM_BINARIES_PATH = BINARIES_PATH + "linux" + File.separator;
		}
	}

	/**
	 * Not to be instantiated
	 */
	protected PlatformProgramPaths() {
	}

	/**
	 * Returns a platform dependent {@link PlatformProgramPaths} instance.
	 *
	 * @param configuration the {@link Configuration} to use for loading custom paths
	 * @return The platform dependent {@link PlatformProgramPaths} instance
	 *
	 * @throws InterruptedException
	 */
	public static final PlatformProgramPaths get(Configuration configuration) throws InterruptedException {
		return new ConfigurableProgramPaths(configuration);
	}

	/**
	 * Returns the path where binaries can be found. This path differs between
	 * the build phase and the test phase. The path will end with a slash unless
	 * it is empty.
	 *
	 * @return The path to the binaries folder.
	 */
	protected static String getBinariesPath() {
		String path = PropertiesUtil.getProjectProperties().get("project.binaries.dir");

		if (StringUtils.isNotBlank(path)) {
			return FileUtil.appendPathSeparator(path);
		} else {
			return "";
		}
	}
}
