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


/**
 * This class keeps track of paths to external programs on Windows.
 *
 * @author Nadahar
 */
public class WindowsProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinuxProgramPaths.class);
	private final ExternalProgramInfo ffmpegInfo;
	private final ExternalProgramInfo mPlayerInfo;
	private final ExternalProgramInfo vlcInfo;
	private final ExternalProgramInfo mEncoderInfo;
	private final ExternalProgramInfo tsMuxeRInfo;
	private final ExternalProgramInfo tsMuxeRNewInfo;
	private final ExternalProgramInfo flacInfo;
	private final ExternalProgramInfo dcRawInfo;
	private final ExternalProgramInfo interFrameInfo;

	/**
	 * Not to be instantiated, call {@link PlatformProgramPaths#get()} instead.
	 *
	 * @throws InterruptedException If the operation is interrupted.
	 */
	protected WindowsProgramPaths() {
		// FFmpeg
		Path ffmpeg = null;

		if (Platform.is64Bit()) {
			ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64.exe");
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
			ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg.exe");
			try {
				if (!new FilePermissions(ffmpeg).isExecutable()) {
					LOGGER.trace("Insufficient permission to executable \"{}\"", ffmpeg.toAbsolutePath());
					if (Platform.is64Bit()) {
						ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64.exe");
					}
				}
			} catch (FileNotFoundException e) {
				LOGGER.trace("Executable \"{}\" not found: {}", ffmpeg.toAbsolutePath(), e.getMessage());
				if (Platform.is64Bit()) {
					ffmpeg = PLATFORM_BINARIES_PATH.resolve("ffmpeg64.exe");
				}
			}
		}

		ffmpegInfo = new ExternalProgramInfo("FFmpeg", ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, ffmpeg);
		if (Platform.is64Bit()) {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("ffmpeg64.exe"));
		} else {
			ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("ffmpeg.exe"));
		}

		// MPlayer
		mPlayerInfo = new ExternalProgramInfo("MPlayer", ProgramExecutableType.BUNDLED);
		mPlayerInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("mplayer.exe"));
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mplayer.exe"));

		// VLC
		vlcInfo = new ExternalProgramInfo("VLC", ProgramExecutableType.INSTALLED);
		SystemUtils registry = PMS.get().getRegistry();
		Path vlcPath = registry.getVlcPath();
		if (vlcPath != null && registry.getVlcVersion() != null) {
			vlcInfo.putPath(ProgramExecutableType.INSTALLED, vlcPath);
		} else {
			vlcInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("vlc.exe"));
		}

		// MEncoder
		mEncoderInfo = new ExternalProgramInfo("MEncoder", ProgramExecutableType.BUNDLED);
		mEncoderInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("mencoder.exe"));
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mencoder.exe"));

		// tsMuxeR
		tsMuxeRInfo = new ExternalProgramInfo("tsMuxeR", ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR.exe"));
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("tsMuxeR.exe"));

		// tsMuxeRNew
		tsMuxeRNewInfo = new ExternalProgramInfo("tsMuxeRNew", ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR-new.exe"));

		// FLAC
		flacInfo = new ExternalProgramInfo("FLAC", ProgramExecutableType.BUNDLED);
		flacInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("flac.exe"));

		// DCRaw
		dcRawInfo = new ExternalProgramInfo("DCRaw", ProgramExecutableType.BUNDLED);
		dcRawInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("dcrawMS.exe"));
		dcRawInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("dcrawMS.exe"));

		// InterFrame
		interFrameInfo = new ExternalProgramInfo("InterFrame", ProgramExecutableType.BUNDLED);
		interFrameInfo.putPath(ProgramExecutableType.BUNDLED, Paths.get("interframe/"));
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
		return interFrameInfo;
	}
}
