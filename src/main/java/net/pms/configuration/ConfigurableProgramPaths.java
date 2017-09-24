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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.util.FileUtil;


/**
 * This class adds configurable/custom paths to {@link PlatformProgramPaths}.
 *
 * @author Nadahar
 */
@ThreadSafe
public class ConfigurableProgramPaths extends PlatformProgramPaths {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableProgramPaths.class);

	/** The {@link Configuration} key for the custom VLC path. */
	public static final String KEY_VLC_PATH         = "vlc_path";

	/** The {@link Configuration} key for the custom MEncoder path. */
	public static final String KEY_MENCODER_PATH    = "mencoder_path";

	/** The {@link Configuration} key for the custom FFmpeg path. */
	public static final String KEY_FFMPEG_PATH      = "ffmpeg_path";

	/** The {@link Configuration} key for the custom MPlayer path. */
	public static final String KEY_MPLAYER_PATH     = "mplayer_path";

	/** The {@link Configuration} key for the custom tsMuxeR path. */
	public static final String KEY_TSMUXER_PATH     = "tsmuxer_path";

	/** The {@link Configuration} key for the custom tsMuxeRNew path. */
	public static final String KEY_TSMUXER_NEW_PATH = "tsmuxer_new_path";

	/** The {@link Configuration} key for the custom FLAC path. */
	public static final String KEY_FLAC_PATH        = "flac_path";

	/** The {@link Configuration} key for the custom DCRaw path. */
	public static final String KEY_DCRAW_PATH       = "dcraw_path";

	/** The {@link Configuration} key for the custom InterFrame path. */
	public static final String KEY_INTERFRAME_PATH  = "interframe_path";

	public static final String KEY_DCRAW_EXECUTABLE_TYPE = "dcraw_executable_type"; //TODO: (Nad) JavaDocs
	public static final String KEY_FFMPEG_EXECUTABLE_TYPE = "ffmpeg_executable_type";
	public static final String KEY_MENCODER_EXECUTABLE_TYPE = "mencoder_executable_type";
	public static final String KEY_TSMUXER_EXECUTABLE_TYPE = "tsmuxer_executable_type";
	public static final String KEY_TSMUXER_NEW_EXECUTABLE_TYPE = "tsmuxer-new_executable_type";
	public static final String KEY_VLC_EXECUTABLE_TYPE = "vlc_executable_type";

	private final FFmpegProgramInfo ffmpegInfo;
	private final ExternalProgramInfo mPlayerInfo;
	private final ExternalProgramInfo vlcInfo;
	private final ExternalProgramInfo mEncoderInfo;
	private final ExternalProgramInfo tsMuxeRInfo;
	private final ExternalProgramInfo tsMuxeRNewInfo;
	private final ExternalProgramInfo flacInfo;
	private final ExternalProgramInfo dcRawInfo;
	private final ExternalProgramInfo interFrameInfo;

	private final Configuration configuration;

	/**
	 * Not to be instantiated, get the {@link ExternalProgramInfo} instances
	 * from {@link PmsConfiguration} instead.
	 *
	 * @param configuration the {@link Configuration} to use for custom paths.
	 */
	protected ConfigurableProgramPaths(@Nullable Configuration configuration) {
		this.configuration = configuration;

		PlatformProgramPaths platformPaths = PlatformProgramPaths.get();
		ffmpegInfo = platformPaths.getFFmpeg().copy();
		mPlayerInfo = platformPaths.getMPlayer().copy();
		vlcInfo = platformPaths.getVLC().copy();
		mEncoderInfo = platformPaths.getMEncoder().copy();
		tsMuxeRInfo = platformPaths.getTsMuxeR().copy();
		tsMuxeRNewInfo = platformPaths.getTsMuxeRNew().copy();
		flacInfo = platformPaths.getFLAC().copy();
		dcRawInfo = platformPaths.getDCRaw().copy();
		interFrameInfo = platformPaths.getInterFrame().copy();
		if (configuration != null) {
			setCustomPathFromConfiguration(ffmpegInfo, KEY_FFMPEG_PATH);
			setCustomPathFromConfiguration(mPlayerInfo, KEY_MPLAYER_PATH);
			setCustomPathFromConfiguration(vlcInfo, KEY_VLC_PATH);
			setCustomPathFromConfiguration(mEncoderInfo, KEY_MENCODER_PATH);
			setCustomPathFromConfiguration(tsMuxeRInfo, KEY_TSMUXER_PATH);
			setCustomPathFromConfiguration(tsMuxeRNewInfo, KEY_TSMUXER_NEW_PATH);
			setCustomPathFromConfiguration(flacInfo, KEY_FLAC_PATH);
			setCustomPathFromConfiguration(dcRawInfo, KEY_DCRAW_PATH);
			setCustomPathFromConfiguration(interFrameInfo, KEY_INTERFRAME_PATH);
		}
	}

	/**
	 * Sets the {@link ProgramExecutableType#CUSTOM} path for the given
	 * {@link ExternalProgramInfo} if it is configured in
	 * {@link #configuration}.
	 *
	 * @param programInfo the {@link ExternalProgramInfo} for which to set
	 *            the {@link ProgramExecutableType#CUSTOM} {@link Path}.
	 * @param customPathKey the {@link Configuration} key to read.
	 */
	protected void setCustomPathFromConfiguration(
		@Nullable ExternalProgramInfo programInfo,
		@Nullable String customPathKey
	) {
		if (programInfo == null || configuration == null) {
			return;
		}
		if (customPathKey == null) {
			ReentrantReadWriteLock lock = programInfo.getLock();
			lock.writeLock().lock();
			try {
				programInfo.remove(ProgramExecutableType.CUSTOM);
				programInfo.setOriginalDefault();
			} finally {
				lock.writeLock().unlock();
			}
			LOGGER.debug("Removed custom {} path", programInfo.getName());
		} else {
			Path custom = null;
			String customPath = configuration.getString(customPathKey);
			if (isNotBlank(customPath)) {
				custom = Paths.get(customPath);
				if (!Files.exists(custom) && !custom.isAbsolute()) {
					Path osPathCustom = FileUtil.findExecutableInOSPath(custom);
					if (osPathCustom != null) {
						custom = osPathCustom;
					}
				}
			}
			ReentrantReadWriteLock lock = programInfo.getLock();
			lock.writeLock().lock();
			try {
				if (custom == null) {
					programInfo.setExecutableInfo(ProgramExecutableType.CUSTOM, null);
					programInfo.setOriginalDefault();
				} else {
					programInfo.setPath(ProgramExecutableType.CUSTOM, Paths.get(customPath));
					programInfo.setDefault(ProgramExecutableType.CUSTOM);
				}
			} finally {
				lock.writeLock().unlock();
			}
			if (custom == null) {
				LOGGER.debug("Cleared custom {} path", programInfo.getName());
			} else {
				LOGGER.debug("Set custom {} path \"{}\"", programInfo.getName(), customPath);
			}
		}
	}

	@Override
	public FFmpegProgramInfo getFFmpeg() {
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
	public ExternalProgramInfo getTsMuxeR() {
		return tsMuxeRInfo;
	}

	@Override
	public ExternalProgramInfo getTsMuxeRNew() {
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

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for FFmpeg
	 * both in {@link #configuration} and {@link #ffmpegInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomFFmpegPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_FFMPEG_PATH, ffmpegInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for MPlayer
	 * both in {@link #configuration} and {@link #mPlayerInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomMPlayerPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_MPLAYER_PATH, mPlayerInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for VLC both
	 * in {@link #configuration} and {@link #vlcInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomVLCPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_VLC_PATH, vlcInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for MEncoder
	 * both in {@link #configuration} and {@link #mEncoderInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomMEncoderPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_MENCODER_PATH, mEncoderInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for tsMuxeR
	 * both in {@link #configuration} and {@link #tsMuxeRInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomTsMuxeRPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_TSMUXER_PATH, tsMuxeRInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for
	 * "tsMuxeR new" both in {@link #configuration} and {@link #tsMuxeRNewInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomTsMuxeRNewPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_TSMUXER_NEW_PATH, tsMuxeRNewInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for FLAC
	 * both in {@link #configuration} and {@link #flacInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomFlacPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_FLAC_PATH, flacInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for DCRaw
	 * both in {@link #configuration} and {@link #dcRawInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomDCRawPath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_DCRAW_PATH, dcRawInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for Interframe
	 * both in {@link #configuration} and {@link #interFrameInfo}.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomInterFramePath(@Nullable Path path) {
		setCustomProgramPath(path, KEY_INTERFRAME_PATH, interFrameInfo);
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for the
	 * specified {@link ExternalProgramInfo} using the specified
	 * {@link Configuration} key. The {@link Path} is first written to
	 * {@link #configuration} before being set in the specified
	 * {@link ExternalProgramInfo} instance.
	 *
	 * @param path the new {@link Path} or {@code null} to clear it.
	 * @param configurationKey the {@link Configuration} key under which to
	 *            store the {@code path}.
	 * @param programInfo the {@link ExternalProgramInfo} in which to set
	 *            {@code path}.
	 */
	protected void setCustomProgramPath(@Nullable Path path, @Nonnull String configurationKey, @Nonnull ExternalProgramInfo programInfo) {
		if (configurationKey == null) {
			throw new IllegalArgumentException("configurationKey cannot be null");
		}
		if (programInfo == null) {
			throw new IllegalArgumentException("programInfo cannot be null");
		}
		if (configuration == null) {
			return;
		}
		if (path == null) {
			configuration.clearProperty(configurationKey);
		} else {
			path = path.toAbsolutePath();
			configuration.setProperty(configurationKey, path.toString());
		}
		setCustomPathFromConfiguration(programInfo, configurationKey);
	}

}
