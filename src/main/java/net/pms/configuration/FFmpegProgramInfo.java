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

import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.logging.LogLevel;


/**
 * This class holds information about the different executable types stored for
 * {@code FFmpeg}. Callers can lock an instance when performing multiple
 * operations in an atomic manner using {@link #getLock()}.
 *
 * @author Nadahar
 */
@ThreadSafe
public class FFmpegProgramInfo extends ExternalProgramInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegProgramInfo.class);

	/**
	 * Creates a new instance with the given arguments.
	 *
	 * @param programName the human readable name for the program to which the
	 *            new {@link ExternalProgramInfo} applies, this is not the
	 *            filename of a particular executable, but the general name of
	 *            the program.
	 * @param defaultType the default {@link ProgramExecutableType} for this
	 *            external program.
	 */
	public FFmpegProgramInfo(@Nullable String programName, @Nullable ProgramExecutableType defaultType) {
		super(programName, defaultType);
	}

	/**
	 * Creates a new instance with the given arguments.
	 *
	 * @param programName the human readable name for the program to which the
	 *            new {@link ExternalProgramInfo} applies, this is not the
	 *            filename of a particular executable, but the general name of
	 *            the program.
	 * @param defaultType the default {@link ProgramExecutableType} for this
	 *            external program.
	 * @param executablesInfo a {@link Map} of {@link ProgramExecutableType}s
	 *            with their corresponding {@link ExecutableInfo}s.
	 */
	public FFmpegProgramInfo(
		@Nullable String programName,
		@Nullable ProgramExecutableType defaultType,
		@Nullable Map<ProgramExecutableType, FFmpegExecutableInfo> executablesInfo
	) {
		super(programName, defaultType, executablesInfo);
	}

	/**
	 * Copy constructor, creates a "deep-clone" in that {@link #executablesInfo}
	 * and {@link #lock} are new instances.
	 *
	 * @param programName the human readable name for the program to which the
	 *            new {@link ExternalProgramInfo} applies, this is not the
	 *            filename of a particular executable, but the general name of
	 *            the program.
	 * @param defaultType the default {@link ProgramExecutableType} for this
	 *            external program.
	 * @param originalDefaultType the default {@link ProgramExecutableType} set
	 *            by the original constructor.
	 * @param executablesInfo a {@link Map} of {@link ProgramExecutableType}s
	 *            with their corresponding {@link ExecutableInfo}s.
	 */
	protected FFmpegProgramInfo(
		@Nullable String programName,
		@Nullable ProgramExecutableType defaultType,
		@Nullable ProgramExecutableType originalDefaultType,
		@Nullable Map<ProgramExecutableType, ? extends ExecutableInfo> executablesInfo
	) {
		super(programName, defaultType, originalDefaultType, executablesInfo);
	}

	/**
	 * Sets the {@link FFmpegExecutableInfo} for the specified
	 * {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set.
	 * @param executableInfo the {@link FFmpegExecutableInfo} to set.
	 * @throws IllegalArgumentException If {@code executableInfo} is not
	 *             {@code null} and is not an instance of
	 *             {@link FFmpegExecutableInfo}.
	 */
	@Override
	public void setExecutableInfo(
		@Nonnull ProgramExecutableType executableType,
		@Nullable ExecutableInfo executableInfo
	) {
		if (executableInfo == null || executableInfo instanceof FFmpegExecutableInfo) {
			super.setExecutableInfo(executableType, executableInfo);
		} else {
			throw new IllegalArgumentException("executableInfo must be an instance of FFmpegExecutableInfo");
		}
	}

	@Override
	@Nonnull
	protected FFmpegExecutableInfo createExecutableInfo(@Nonnull Path executablePath) {
		return FFmpegExecutableInfo.build(executablePath).build();
	}
	/**
	 * Returns a "deep-clone" of this instance where mutable objects are copied.
	 *
	 * @return The new {@link FFmpegProgramInfo}.
	 */
	@Override
	@Nonnull
	public FFmpegProgramInfo copy() {
		return new FFmpegProgramInfo(programName, defaultType, originalDefaultType, executablesInfo);
	}

	/**
	 * Returns the FFmpeg parameter value for {@code "-loglevel"} derived from
	 * the current {@link LogLevel}.
	 *
	 * @return The FFmpeg {@code "-loglevel"} parameter value.
	 */
	@Nonnull
	public static String getFFmpegLogLevel() {
		return getFFmpegLogLevel(LOGGER);
	}

	/**
	 * Returns the FFmpeg parameter value for {@code "-loglevel"} derived from
	 * the {@link LogLevel} of the specified {@link Logger}.
	 *
	 * @param logger the {@link Logger} from which to derive the value.
	 * @return The FFmpeg {@code "-loglevel"} parameter value.
	 */
	@Nonnull
	public static String getFFmpegLogLevel(@Nullable Logger logger) {
		if (logger == null) {
			logger = LOGGER;
		}
		return getFFmpegLogLevel(LogLevel.getLogLevel(logger));
	}

	/**
	 * Returns the FFmpeg parameter value for {@code "-loglevel"} derived from
	 * the specified {@link LogLevel}.
	 *
	 * @param level the {@link LogLevel} from which to derive the value.
	 * @return The FFmpeg {@code "-loglevel"} parameter value.
	 */
	@Nonnull
	public static String getFFmpegLogLevel(@Nullable LogLevel level) {
		if (level == null) {
			return "warning";
		}
		switch (level) {
			case OFF:
				return "quiet";
			case ERROR:
				return "error";
			case WARN:
			case INFO:
				return "warning";
			case DEBUG:
			case TRACE:
				return "info";
			case ALL:
				return "verbose";
			default:
				LOGGER.error("Unimplemented enum value \"{}\" in getFFmpegLogLevel()", level.name());
				return "info";
		}
	}

}
