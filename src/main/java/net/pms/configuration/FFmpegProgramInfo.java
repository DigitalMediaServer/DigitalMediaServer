package net.pms.configuration;

import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;


/**
 * This class holds information about the different executable types stored for
 * {@code FFmpeg}. Callers can lock an instance when performing multiple
 * operations in an atomic manner using {@link #getLock()}.
 *
 * @author Nadahar
 */
@ThreadSafe
public class FFmpegProgramInfo extends ExternalProgramInfo {

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
	public FFmpegProgramInfo(String programName, ProgramExecutableType defaultType) {
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
		String programName,
		ProgramExecutableType defaultType,
		Map<ProgramExecutableType, ExecutableInfo> executablesInfo
	) {
		super(programName, defaultType, executablesInfo);
	}

	public void setExecutableInfo(ProgramExecutableType executableType, FFmpegExecutableInfo executableInfo) {
		lock.writeLock().lock();
		try {
			executablesInfo.put(executableType, executableInfo);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	protected FFmpegExecutableInfo createExecutableInfo(@Nonnull Path executablePath) {
		return FFmpegExecutableInfo.build(executablePath).build();
	}
}
