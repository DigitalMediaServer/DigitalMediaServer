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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.io.SimpleProcessWrapper;
import net.pms.io.SimpleProcessWrapper.SimpleProcessWrapperResult;
import net.pms.util.FilePermissions;
import com.sun.jna.Platform;


public class LinuxProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinuxProgramPaths.class);
	private final PlatformExecutableInfo ffmpegInfo;
	private final PlatformExecutableInfo mPlayerInfo;
	private final PlatformExecutableInfo vlcInfo;
	private final PlatformExecutableInfo mEncoderInfo;
	private final PlatformExecutableInfo tsMuxeRInfo;
	private final PlatformExecutableInfo tsMuxeRNewInfo;
	private final PlatformExecutableInfo flacInfo;
	private final PlatformExecutableInfo dcRawInfo;

	protected LinuxProgramPaths() throws InterruptedException {
		// FFmpeg
		Path ffmpeg = null;
		if (Platform.is64Bit()) {
			ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64");
			try {
				if (!new FilePermissions(ffmpeg).isExecutable()) {
					LOGGER.trace("Insufficient permission to executable \"{}\"", ffmpeg.toAbsolutePath());
					LOGGER.trace("Looking for non-64 version");
					ffmpeg = null;
				}
			} catch (FileNotFoundException e) {
				LOGGER.trace("Executable \"{}\" not found: {}", ffmpeg.toAbsolutePath(), e.getMessage());
				LOGGER.trace("Looking for non-64 version");
				ffmpeg = null;
			}
		}
		if (ffmpeg == null) {
			ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg");
			try {
				if (!new FilePermissions(ffmpeg).isExecutable()) {
					LOGGER.trace("Insufficient permission to executable \"{}\"", ffmpeg.toAbsolutePath());
					if (Platform.is64Bit()) {
						ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64");
					}
				}
			} catch (FileNotFoundException e) {
				LOGGER.trace("Executable \"{}\" not found: {}", ffmpeg.toAbsolutePath(), e.getMessage());
				if (Platform.is64Bit()) {
					ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64");
				}
			}
		}

		ffmpegInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, ffmpeg.toString());

		SimpleProcessWrapperResult result64 = null;
		if (Platform.is64Bit()) {
			try {
				result64 = SimpleProcessWrapper.runProcess("ffmpeg64", "-version");
			} catch (IOException e) {
				result64 = null;
			}
		}

		SimpleProcessWrapperResult result = null;
		if (result64 == null) {
			try {
				result = SimpleProcessWrapper.runProcess("ffmpeg", "-version");
			} catch (IOException e) {
				result = null;
			}
		}
		if (Platform.is64Bit() && (result64 != null || result == null)) {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, "ffmpeg64");
		} else {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, "ffmpeg");
		}

		// MPlayer
		mPlayerInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, "mplayer");

		// VLC
		vlcInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		vlcInfo.putPath(ProgramExecutableType.INSTALLED, "vlc");

		// MEncoder
		mEncoderInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, "mencoder");

		// tsMuxeR
		tsMuxeRInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR");
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, "tsMuxeR");

		// tsMuxeRNew
		tsMuxeRNewInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR-new");
		tsMuxeRNewInfo.putPath(ProgramExecutableType.INSTALLED, "tsMuxeR-new");

		// FLAC
		flacInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		flacInfo.putPath(ProgramExecutableType.INSTALLED, "flac");

		// DCRaw
		dcRawInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		dcRawInfo.putPath(ProgramExecutableType.INSTALLED, "dcraw");

	}

	@Override
	public PlatformExecutableInfo getFFmpeg() {
		return ffmpegInfo;
	}

	@Override
	public PlatformExecutableInfo getMPlayer() {
		return mPlayerInfo;
	}

	@Override
	public PlatformExecutableInfo getVLC() {
		return vlcInfo;
	}

	@Override
	public PlatformExecutableInfo getMEncoder() {
		return mEncoderInfo;
	}

	@Override
	public PlatformExecutableInfo gettsMuxeR() {
		return tsMuxeRInfo;
	}

	@Override
	public PlatformExecutableInfo gettsMuxeRNew() {
		return tsMuxeRNewInfo;
	}

	@Override
	public PlatformExecutableInfo getFLAC() {
		return flacInfo;
	}

	@Override
	public PlatformExecutableInfo getDCRaw() {
		return dcRawInfo;
	}

	@Override
	public PlatformExecutableInfo getInterFrame() {
		return null;
	}
}
