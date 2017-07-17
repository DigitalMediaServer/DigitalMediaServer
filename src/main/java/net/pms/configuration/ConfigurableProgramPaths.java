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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.Platform;


public class ConfigurableProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableProgramPaths.class);

	public static final String KEY_VLC_PATH         = "vlc_path";
	public static final String KEY_MENCODER_PATH    = "mencoder_path";
	public static final String KEY_FFMPEG_PATH      = "ffmpeg_path";
	public static final String KEY_MPLAYER_PATH     = "mplayer_path";
	public static final String KEY_TSMUXER_PATH     = "tsmuxer_path";
	public static final String KEY_TSMUXER_NEW_PATH = "tsmuxer_new_path";
	public static final String KEY_FLAC_PATH        = "flac_path";
	public static final String KEY_DCRAW_PATH       = "dcraw_path";
	public static final String KEY_INTERFRAME_PATH  = "interframe_path";

	private final Configuration configuration;

	private final PlatformProgramPaths platformInstance;

	/**
	 * Not to be called directly, call {@link PlatformProgramPaths#get()} instead.
	 *
	 * @throws InterruptedException
	 */
	protected ConfigurableProgramPaths(Configuration configuration) throws InterruptedException {
		this.configuration = configuration;

		if (Platform.isWindows()) {
			platformInstance = new WindowsProgramPaths();
		} else if (Platform.isMac()) {
			platformInstance = new OSXProgramPaths();
		} else {
			platformInstance = new LinuxProgramPaths();
		}
	}

	protected void addCustomPath(PlatformExecutableInfo programInfo, String parameterString, String programName) {
		String s = configuration.getString(parameterString);
		if (StringUtils.isNotBlank(s)) {
			ReentrantReadWriteLock lock = programInfo.getLock();
			lock.writeLock().lock();
			try {
				programInfo.putPath(ProgramExecutableType.CUSTOM, s);
				programInfo.setDefault(ProgramExecutableType.CUSTOM);
			} finally {
				lock.writeLock().unlock();
			}
			LOGGER.debug("Adding custom {} path \"{}\"", programName, s);
		}
	}

	@Override
	public PlatformExecutableInfo getFFmpeg() {
		PlatformExecutableInfo ffmpeg = platformInstance.getFFmpeg();
		if (ffmpeg != null && configuration != null) {
			addCustomPath(ffmpeg, KEY_FFMPEG_PATH, "FFmpeg");
		}
		return ffmpeg;
	}

	@Override
	public PlatformExecutableInfo getMPlayer() {
		PlatformExecutableInfo mPlayer = platformInstance.getMPlayer();
		if (mPlayer != null && configuration != null) {
			addCustomPath(mPlayer, KEY_MPLAYER_PATH, "MPlayer");
		}
		return mPlayer;
	}

	@Override
	public PlatformExecutableInfo getVLC() {
		PlatformExecutableInfo vlc = platformInstance.getVLC();
		if (vlc != null && configuration != null) {
			addCustomPath(vlc, KEY_VLC_PATH, "VLC");
		}
		return vlc;
	}

	@Override
	public PlatformExecutableInfo getMEncoder() {
		PlatformExecutableInfo mEncoder = platformInstance.getMEncoder();
		if (mEncoder != null && configuration != null) {
			addCustomPath(mEncoder, KEY_MENCODER_PATH, "MEncoder");
		}
		return mEncoder;
	}

	@Override
	public PlatformExecutableInfo gettsMuxeR() {
		PlatformExecutableInfo tsMuxeR = platformInstance.gettsMuxeR();
		if (tsMuxeR != null && configuration != null) {
			addCustomPath(tsMuxeR, KEY_TSMUXER_PATH, "tsMuxeR");
		}
		return tsMuxeR;
	}

	@Override
	public PlatformExecutableInfo gettsMuxeRNew() {
		PlatformExecutableInfo tsMuxeRNew = platformInstance.gettsMuxeRNew();
		if (tsMuxeRNew != null && configuration != null) {
			addCustomPath(tsMuxeRNew, KEY_TSMUXER_NEW_PATH, "tsMuxerNew");
		}
		return tsMuxeRNew;
	}

	@Override
	public PlatformExecutableInfo getFLAC() {
		PlatformExecutableInfo flac = platformInstance.getFLAC();
		if (flac != null && configuration != null) {
			addCustomPath(flac, KEY_FLAC_PATH, "FLAC");
		}
		return flac;
	}

	@Override
	public PlatformExecutableInfo getDCRaw() {
		PlatformExecutableInfo dcRaw = platformInstance.getDCRaw();
		if (dcRaw != null && configuration != null) {
			addCustomPath(dcRaw, KEY_DCRAW_PATH, "DCRaw");
		}
		return dcRaw;
	}

	@Override
	public PlatformExecutableInfo getInterFrame() {
		PlatformExecutableInfo interFrame = platformInstance.getInterFrame();
		if (interFrame != null && configuration != null) {
			addCustomPath(interFrame, KEY_INTERFRAME_PATH, "InterFrame");
		}
		return interFrame;
	}

}
