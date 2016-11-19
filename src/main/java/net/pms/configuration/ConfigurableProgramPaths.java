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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.Platform;


/**
 * This class keeps track of configurable/custom paths to external programs.
 *
 * @author Nadahar
 */
public class ConfigurableProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableProgramPaths.class);

	/** The {@link Configuration} key for a custom VLC path. */
	public static final String KEY_VLC_PATH         = "vlc_path";

	/** The {@link Configuration} key for a custom MEncoder path. */
	public static final String KEY_MENCODER_PATH    = "mencoder_path";

	/** The {@link Configuration} key for a custom FFmpeg path. */
	public static final String KEY_FFMPEG_PATH      = "ffmpeg_path";

	/** The {@link Configuration} key for a custom MPlayer path. */
	public static final String KEY_MPLAYER_PATH     = "mplayer_path";

	/** The {@link Configuration} key for a custom tsMuxeR path. */
	public static final String KEY_TSMUXER_PATH     = "tsmuxer_path";

	/** The {@link Configuration} key for a custom tsMuxeRNew path. */
	public static final String KEY_TSMUXER_NEW_PATH = "tsmuxer_new_path";

	/** The {@link Configuration} key for a custom FLAC path. */
	public static final String KEY_FLAC_PATH        = "flac_path";

	/** The {@link Configuration} key for a custom DCRaw path. */
	public static final String KEY_DCRAW_PATH       = "dcraw_path";

	/** The {@link Configuration} key for a custom InterFrame path. */
	public static final String KEY_INTERFRAME_PATH  = "interframe_path";

	private final Configuration configuration;

	private final PlatformProgramPaths platformInstance;

	/**
	 * Not to be instantiated, call {@link PlatformProgramPaths#get()} instead.
	 *
	 * @param configuration the {@link Configuration} to use.
	 * @throws InterruptedException If the operation is interrupted.
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

	/**
	 * Sets the {@link ProgramExecutableType#CUSTOM} path for the given
	 * {@link ExternalProgramInfo} if it is configured in
	 * {@link #configuration}.
	 *
	 * @param programInfo the {@link ExternalProgramInfo} for which to set
	 *            the {@link ProgramExecutableType#CUSTOM} type.
	 * @param customPathKey the {@link Configuration} key to read.
	 */
	protected void setCustomPath(@Nonnull ExternalProgramInfo programInfo, @Nullable String customPathKey) {
		if (programInfo == null) {
			throw new IllegalArgumentException("programInfo cannot be null");
		}
		String customPath = configuration.getString(customPathKey);
		if (StringUtils.isNotBlank(customPath)) {
			ReentrantReadWriteLock lock = programInfo.getLock();
			lock.writeLock().lock();
			try {
				programInfo.putPath(ProgramExecutableType.CUSTOM, Paths.get(customPath));
				programInfo.setDefault(ProgramExecutableType.CUSTOM);
			} finally {
				lock.writeLock().unlock();
			}
			LOGGER.debug("Adding custom {} path \"{}\"", programInfo.getName(), customPath);
		}
	}

	@Override
	public ExternalProgramInfo getFFmpeg() {
		ExternalProgramInfo ffmpeg = platformInstance.getFFmpeg();
		if (ffmpeg != null && configuration != null) {
			setCustomPath(ffmpeg, KEY_FFMPEG_PATH);
		}
		return ffmpeg;
	}

	@Override
	public ExternalProgramInfo getMPlayer() {
		ExternalProgramInfo mPlayer = platformInstance.getMPlayer();
		if (mPlayer != null && configuration != null) {
			setCustomPath(mPlayer, KEY_MPLAYER_PATH);
		}
		return mPlayer;
	}

	@Override
	public ExternalProgramInfo getVLC() {
		ExternalProgramInfo vlc = platformInstance.getVLC();
		if (vlc != null && configuration != null) {
			setCustomPath(vlc, KEY_VLC_PATH);
		}
		return vlc;
	}

	@Override
	public ExternalProgramInfo getMEncoder() {
		ExternalProgramInfo mEncoder = platformInstance.getMEncoder();
		if (mEncoder != null && configuration != null) {
			setCustomPath(mEncoder, KEY_MENCODER_PATH);
		}
		return mEncoder;
	}

	@Override
	public ExternalProgramInfo gettsMuxeR() {
		ExternalProgramInfo tsMuxeR = platformInstance.gettsMuxeR();
		if (tsMuxeR != null && configuration != null) {
			setCustomPath(tsMuxeR, KEY_TSMUXER_PATH);
		}
		return tsMuxeR;
	}

	@Override
	public ExternalProgramInfo gettsMuxeRNew() {
		ExternalProgramInfo tsMuxeRNew = platformInstance.gettsMuxeRNew();
		if (tsMuxeRNew != null && configuration != null) {
			setCustomPath(tsMuxeRNew, KEY_TSMUXER_NEW_PATH);
		}
		return tsMuxeRNew;
	}

	@Override
	public ExternalProgramInfo getFLAC() {
		ExternalProgramInfo flac = platformInstance.getFLAC();
		if (flac != null && configuration != null) {
			setCustomPath(flac, KEY_FLAC_PATH);
		}
		return flac;
	}

	@Override
	public ExternalProgramInfo getDCRaw() {
		ExternalProgramInfo dcRaw = platformInstance.getDCRaw();
		if (dcRaw != null && configuration != null) {
			setCustomPath(dcRaw, KEY_DCRAW_PATH);
		}
		return dcRaw;
	}

	@Override
	public ExternalProgramInfo getInterFrame() {
		ExternalProgramInfo interFrame = platformInstance.getInterFrame();
		if (interFrame != null && configuration != null) {
			setCustomPath(interFrame, KEY_INTERFRAME_PATH);
		}
		return interFrame;
	}

}
