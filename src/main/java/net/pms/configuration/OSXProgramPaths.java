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


public class OSXProgramPaths extends PlatformProgramPaths {
	private final PlatformExecutableInfo ffmpegInfo;
	private final PlatformExecutableInfo mPlayerInfo;
	private final PlatformExecutableInfo vlcInfo;
	private final PlatformExecutableInfo mEncoderInfo;
	private final PlatformExecutableInfo tsMuxeRInfo;
	private final PlatformExecutableInfo tsMuxeRNewInfo;
	private final PlatformExecutableInfo flacInfo;
	private final PlatformExecutableInfo dcRawInfo;

	protected OSXProgramPaths() {
		// FFmpeg
		ffmpegInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		ffmpegInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "ffmpeg");
		ffmpegInfo.putPath(ProgramExecutableType.INSTALLED, "ffmpeg");

		// MPlayer
		mPlayerInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		mPlayerInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "mplayer");
		mPlayerInfo.putPath(ProgramExecutableType.INSTALLED, "mplayer");

		// VLC
		vlcInfo = new PlatformExecutableInfo(ProgramExecutableType.INSTALLED);
		vlcInfo.putPath(ProgramExecutableType.INSTALLED, "/Applications/VLC.app/Contents/MacOS/VLC");

		// MEncoder
		mEncoderInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		mEncoderInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "mencoder");
		mEncoderInfo.putPath(ProgramExecutableType.INSTALLED, "mencoder");

		// tsMuxeR
		tsMuxeRInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR");
		tsMuxeRInfo.putPath(ProgramExecutableType.INSTALLED, "tsMuxeR");

		// tsMuxeRNew
		tsMuxeRNewInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		tsMuxeRNewInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "tsMuxeR-new");

		// FLAC
		flacInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		flacInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "flac");
		flacInfo.putPath(ProgramExecutableType.INSTALLED, "flac");

		// DCRaw
		dcRawInfo = new PlatformExecutableInfo(ProgramExecutableType.BUNDLED);
		dcRawInfo.putPath(ProgramExecutableType.BUNDLED, PLATFORM_BINARIES_PATH + "dcraw");
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
