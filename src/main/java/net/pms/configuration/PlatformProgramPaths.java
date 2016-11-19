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

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import com.sun.jna.Platform;
import net.pms.util.FilePermissions;
import net.pms.util.PropertiesUtil;

/**
 * This class keeps track of paths to external programs.
 *
 * @author Nadahar
 */
public abstract class PlatformProgramPaths {

	/**
	 * @return The {@link ExternalProgramInfo} for FFmpeg.
	 */
	public abstract ExternalProgramInfo getFFmpeg();

	/**
	 * @return The {@link ExternalProgramInfo} for MPlayer.
	 */
	public abstract ExternalProgramInfo getMPlayer();

	/**
	 * @return The {@link ExternalProgramInfo} for VLC.
	 */
	public abstract ExternalProgramInfo getVLC();

	/**
	 * @return The {@link ExternalProgramInfo} for MEncoder.
	 */
	public abstract ExternalProgramInfo getMEncoder();

	/**
	 * @return The {@link ExternalProgramInfo} for tsMuxeR.
	 */
	public abstract ExternalProgramInfo gettsMuxeR();

	/**
	 * @return The {@link ExternalProgramInfo} for tsMuxeRNew.
	 */
	public abstract ExternalProgramInfo gettsMuxeRNew();

	/**
	 * @return The {@link ExternalProgramInfo} for FLAC.
	 */
	public abstract ExternalProgramInfo getFLAC();

	/**
	 * @return The {@link ExternalProgramInfo} for DCRaw.
	 */
	public abstract ExternalProgramInfo getDCRaw();

	/**
	 * @return The {@link ExternalProgramInfo} for InterFrame.
	 */
	public abstract ExternalProgramInfo getInterFrame();


	/** The {@link Path} to {@code project.binaries.dir}. */
	protected static final Path BINARIES_PATH = getBinariesPath();

	/** The {@link Path} to the bundled binaries for the current platform. */
	protected static final Path PLATFORM_BINARIES_PATH;

	static {
		String subPath;
		if (Platform.isWindows()) {
			subPath = "win32";
		} else if (Platform.isMac()) {
			subPath = "osx";
		} else {
			subPath = "linux";
		}
		Path binaryFolder = BINARIES_PATH.resolve(subPath);
		boolean ok = true;
		try {
			FilePermissions permission = new FilePermissions(binaryFolder);
			if (!permission.isBrowsable()) {
				ok = false;
			}
		} catch (FileNotFoundException e) {
			ok = false;
		}
		if (!ok) {
			Path developmentBinaryFolder = Paths.get("target/bin", subPath);
			try {
				FilePermissions permission = new FilePermissions(developmentBinaryFolder);
				if (permission.isBrowsable()) {
					binaryFolder = developmentBinaryFolder;
				}
			} catch (FileNotFoundException e) {
				// Nothing to do, keep the original binary folder
			}
		}
		PLATFORM_BINARIES_PATH = binaryFolder.toAbsolutePath();
	}

	/**
	 * Not to be instantiated.
	 */
	protected PlatformProgramPaths() {
	}

	/**
	 * Returns a platform dependent {@link PlatformProgramPaths} instance.
	 *
	 * @param configuration the {@link Configuration} to use for loading custom paths
	 * @return The platform dependent {@link PlatformProgramPaths} instance
	 *
	 * @throws InterruptedException If the operation is interrupted.
	 */
	public static final PlatformProgramPaths get(Configuration configuration) throws InterruptedException {
		return new ConfigurableProgramPaths(configuration);
	}

	/**
	 * Returns the (relative) {@link Path} where binaries can be found. This
	 * {@link Path} differs between the build phase and the test phase.
	 *
	 * @return The path to the binaries folder.
	 */
	protected static Path getBinariesPath() {
		String path = PropertiesUtil.getProjectProperties().get("project.binaries.dir");

		if (StringUtils.isNotBlank(path)) {
			return Paths.get(path);
		}
		return Paths.get("");
	}
}
