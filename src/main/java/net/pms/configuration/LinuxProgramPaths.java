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


/**
 * This class keeps track of paths to external programs on Linux.
 *
 * @author Nadahar
 */
public class LinuxProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinuxProgramPaths.class);
	private final ExternalProgramInfo ffmpegInfo;
	private final ExternalProgramInfo mPlayerInfo;
	private final ExternalProgramInfo vlcInfo;
	private final ExternalProgramInfo mEncoderInfo;
	private final ExternalProgramInfo tsMuxeRInfo;
	private final ExternalProgramInfo tsMuxeRNewInfo;
	private final ExternalProgramInfo flacInfo;
	private final ExternalProgramInfo dcRawInfo;

	/**
	 * Not to be instantiated, call {@link PlatformProgramPaths#get()} instead.
	 *
	 * @throws InterruptedException If the operation is interrupted.
	 */
	@SuppressWarnings("null")
	protected LinuxProgramPaths() throws InterruptedException {
		// FFmpeg
		Path ffmpeg = null;
		if (Platform.is64Bit()) {
			ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64");
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
			ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg");
			try {
				if (!new FilePermissions(ffmpeg).isExecutable()) {
					LOGGER.trace("Insufficient permission to executable \"{}\"", ffmpeg.toAbsolutePath());
					if (Platform.is64Bit()) {
						ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64");
					}
				}
			} catch (FileNotFoundException e) {
				LOGGER.trace("Executable \"{}\" not found: {}", ffmpeg.toAbsolutePath(), e.getMessage());
				if (Platform.is64Bit()) {
					ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64");
				}
			}
		}

		ffmpegInfo = new ExternalProgramInfo("FFmpeg", ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, ffmpeg);

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
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("ffmpeg64"));
		} else {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("ffmpeg"));
		}

		// MPlayer
		mPlayerInfo = new ExternalProgramInfo("MPlayer", ProgramExecutableType.INSTALLED);
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mplayer"));

		// VLC
		vlcInfo = new ExternalProgramInfo("VLC", ProgramExecutableType.INSTALLED);
		vlcInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("vlc"));

		// MEncoder
		mEncoderInfo = new ExternalProgramInfo("MEncoder", ProgramExecutableType.INSTALLED);
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mencoder"));

		// tsMuxeR
		tsMuxeRInfo = new ExternalProgramInfo("tsMuxeR", ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR"));
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("tsMuxeR"));

		// tsMuxeRNew
		tsMuxeRNewInfo = new ExternalProgramInfo("tsMuxeRNew", ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR-new"));
		tsMuxeRNewInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("tsMuxeR-new"));

		// FLAC
		flacInfo = new ExternalProgramInfo("FLAC", ProgramExecutableType.INSTALLED);
		flacInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("flac"));

		// DCRaw
		dcRawInfo = new ExternalProgramInfo("DCRaw", ProgramExecutableType.INSTALLED);
		dcRawInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("dcraw"));
	}

	@Override
	public ExternalProgramInfo getFFmpeg() {
		return ffmpegInfo;
	}

	@Override
	public ExternalProgramInfo getMPlayer() {
		return mPlayerInfo;
	}

	@Override
	public ExternalProgramInfo getVLC() {
		return vlcInfo;
	}

	@Override
	public ExternalProgramInfo getMEncoder() {
		return mEncoderInfo;
	}

	@Override
	public ExternalProgramInfo gettsMuxeR() {
		return tsMuxeRInfo;
	}

	@Override
	public ExternalProgramInfo gettsMuxeRNew() {
		return tsMuxeRNewInfo;
	}

	@Override
	public ExternalProgramInfo getFLAC() {
		return flacInfo;
	}

	@Override
	public ExternalProgramInfo getDCRaw() {
		return dcRawInfo;
	}

	@Override
	public ExternalProgramInfo getInterFrame() {
		return null;
	}
}
