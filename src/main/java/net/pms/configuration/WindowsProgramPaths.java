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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.PMS;
import net.pms.io.SystemUtils;
import net.pms.util.FilePermissions;
import com.sun.jna.Platform;


public class WindowsProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinuxProgramPaths.class);
	private final PlatformExecutableInfo ffmpegInfo;
	private final PlatformExecutableInfo mPlayerInfo;
	private final PlatformExecutableInfo vlcInfo;
	private final PlatformExecutableInfo mEncoderInfo;
	private final PlatformExecutableInfo tsMuxeRInfo;
	private final PlatformExecutableInfo tsMuxeRNewInfo;
	private final PlatformExecutableInfo flacInfo;
	private final PlatformExecutableInfo dcRawInfo;
	private final PlatformExecutableInfo interFrameInfo;

	protected WindowsProgramPaths() {
		// FFmpeg
		Path ffmpeg = null;

		if (Platform.is64Bit()) {
			ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64.exe");
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
			ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg.exe");
			try {
				if (!new FilePermissions(ffmpeg).isExecutable()) {
					LOGGER.trace("Insufficient permission to executable \"{}\"", ffmpeg.toAbsolutePath());
					if (Platform.is64Bit()) {
						ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64.exe");
					}
				}
			} catch (FileNotFoundException e) {
				LOGGER.trace("Executable \"{}\" not found: {}", ffmpeg.toAbsolutePath(), e.getMessage());
				if (Platform.is64Bit()) {
					ffmpeg = Paths.get(PLATFORM_BINARIES_PATH, "ffmpeg64.exe");
				}
			}
		}

		ffmpegInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, ffmpeg.toString());
		if (Platform.is64Bit()) {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, "ffmpeg64.exe");
		} else {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, "ffmpeg.exe");
		}

		// MPlayer
		mPlayerInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		mPlayerInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "mplayer.exe");
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, "mplayer.exe");

		// VLC
		vlcInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		SystemUtils registry = PMS.get().getRegistry();
		String vlcPath = registry.getVlcPath();
		if (vlcPath != null && registry.getVlcVersion() != null) {
			vlcInfo.putPath(ProgramExecutableType.INSTALLED, vlcPath);
		} else {
			vlcInfo.putPath(ProgramExecutableType.INSTALLED, "vlc.exe");
		}

		// MEncoder
		mEncoderInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		mEncoderInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "mencoder.exe");
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, "mencoder.exe");

		// tsMuxeR
		tsMuxeRInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR.exe");
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, "tsMuxeR.exe");

		// tsMuxeRNew
		tsMuxeRNewInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR-new.exe");

		// FLAC
		flacInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		flacInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "flac.exe");

		// DCRaw
		dcRawInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		dcRawInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "dcrawMS.exe");
		dcRawInfo.putPath(ProgramExecutableType.INSTALLED, "dcrawMS.exe");

		// InterFrame
		interFrameInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		interFrameInfo.putPath(ProgramExecutableType.BUNDLED, "interframe/");
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
		return interFrameInfo;
	}
}
