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

import java.nio.file.Paths;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * This class keeps track of paths to external programs on macOS.
 *
 * @author Nadahar
 */
public class OSXProgramPaths extends PlatformProgramPaths {
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
	@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
	protected OSXProgramPaths() {
		// FFmpeg
		ffmpegInfo = new ExternalProgramInfo("FFmpeg", ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("ffmpeg"));
		ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("ffmpeg"));

		// MPlayer
		mPlayerInfo = new ExternalProgramInfo("MPlayer", ProgramExecutableType.BUNDLED);
		mPlayerInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("mplayer"));
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mplayer"));

		// VLC
		vlcInfo = new ExternalProgramInfo("VLC", ProgramExecutableType.INSTALLED);
		vlcInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("/Applications/VLC.app/Contents/MacOS/VLC"));

		// MEncoder
		mEncoderInfo = new ExternalProgramInfo("MEncoder", ProgramExecutableType.BUNDLED);
		mEncoderInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("mencoder"));
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("mencoder"));

		// tsMuxeR
		tsMuxeRInfo = new ExternalProgramInfo("tsMuxeR", ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR"));
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("tsMuxeR"));

		// tsMuxeRNew
		tsMuxeRNewInfo = new ExternalProgramInfo("tsMuxeRNew", ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("tsMuxeR-new"));

		// FLAC
		flacInfo = new ExternalProgramInfo("FLAC", ProgramExecutableType.BUNDLED);
		flacInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("flac"));
		flacInfo.putPath(ProgramExecutableType.INSTALLED, Paths.get("flac"));

		// DCRaw
		dcRawInfo = new ExternalProgramInfo("DCRaw", ProgramExecutableType.BUNDLED);
		dcRawInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH.resolve("dcraw"));
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
