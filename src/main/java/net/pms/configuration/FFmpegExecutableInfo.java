package net.pms.configuration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.pms.encoders.ExecutableErrorType;
import net.pms.io.ListProcessWrapperResult;
import net.pms.util.Version;


/**
 * A class to hold basic information about a FFmpeg executable and its
 * availability status.
 *
 * @author Nadahar
 */
@Immutable
public class FFmpegExecutableInfo extends ExecutableInfo {

	/**
	 * The {@link List} of {@link String} codes for the supported protocols
	 */
	@Nonnull
	protected final List<String> protocols;

	/**
	 * The {@link List} of enabled options in lower-case.
	 */
	@Nonnull
	protected final List<String> enabledOptions;

	/**
	 * The {@link Map} of libraries in lower-case and their corresponding
	 * {@link Version}s.
	 */
	@Nonnull
	protected final Map<String, Version> libraryVersions;

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param available {@code true} if the executable is tested and found
	 *            available, {@code false} if the executable is tested and found
	 *            unavailable or {@code null} if the executable is untested.
	 * @param path the {@link Path} to the executable.
	 * @param version the {@link Version} of the executable or {@code null} if
	 *            unknown.
	 * @param errorType the {@link ExecutableErrorType} if {@code available} is
	 *            {@code false}, {@code null} otherwise.
	 * @param errorText the localized error text if {@code available} is
	 *            {@code false}, {@code null} otherwise.
	 * @param protocols a {@link List} of {@link String}s containing codes for
	 *            the supported protocols for this executable.
	 * @param enabledOptions a {@link List} of enabled options in lower-case.
	 * @param libraryVersions a {@link Map} of libraries in lower-case and their
	 *            corresponding {@link Version}s.
	 */
	public FFmpegExecutableInfo(
		@Nullable Boolean available,
		@Nonnull Path path,
		@Nullable Version version,
		@Nullable ExecutableErrorType errorType,
		@Nullable String errorText,
		@Nullable List<String> protocols,
		@Nullable List<String> enabledOptions,
		@Nullable Map<String, Version> libraryVersions
	) {
		super(available, path, version, errorType, errorText);
		this.protocols = Collections.unmodifiableList(
			protocols == null ? new ArrayList<String>() : new ArrayList<>(protocols)
		);
		this.enabledOptions = Collections.unmodifiableList(
			enabledOptions == null ? new ArrayList<String>() : new ArrayList<>(enabledOptions)
		);
		this.libraryVersions = Collections.unmodifiableMap(
			libraryVersions == null ? new HashMap<String, Version>() : new HashMap<>(libraryVersions)
		);
	}

	/**
	 * @return A new {@link FFmpegExecutableInfoBuilder} initialized with the
	 *         values of this {@link FFmpegExecutableInfo}. When done modifying,
	 *         convert the {@link FFmpegExecutableInfoBuilder} into a new
	 *         {@link FFmpegExecutableInfo} instance with
	 *         {@link FFmpegExecutableInfoBuilder#build()}.
	 */
	@Override
	public FFmpegExecutableInfoBuilder modify() {
		return new FFmpegExecutableInfoBuilder(this);
	}

	/**
	 * Returns a new {@link FFmpegExecutableInfoBuilder} for the specified
	 * executable. Use {@link FFmpegExecutableInfoBuilder#build()} to create a
	 * new {@link FFmpegExecutableInfo} instance once the values are set.
	 *
	 * @param executablePath the {@link Path} to the executable.
	 * @return The new {@link FFmpegExecutableInfoBuilder} instance.
	 */
	public static FFmpegExecutableInfoBuilder build(Path executablePath) {
		return new FFmpegExecutableInfoBuilder(executablePath);
	}

	/**
	 * @return The {@link List} of {@link String} codes for the supported
	 *         protocols.
	 */
	@Nonnull
	public List<String> getProtocols() {
		return protocols;
	}

	/**
	 * @return The {@link List} of enabled options in lower-case.
	 */
	@Nonnull
	public List<String> getEnabledOptions() {
		return enabledOptions;
	}

	/**
	 * @return The {@link Map} of libraries in lower-case and their
	 *         corresponding {@link Version}s.
	 */
	@Nonnull
	public Map<String, Version> getLibraryVersions() {
		return libraryVersions;
	}

	/**
	 * Gets the library {@link Version} for the specified library, or
	 * {@code null} if unknown.
	 *
	 * @param library the library to look up.
	 * @return The {@link Version} or {@code null}.
	 */
	@Nullable
	public Version getLibraryVersion(@Nullable String library) {
		if (isBlank(library)) {
			return null;
		}
		return libraryVersions.get(library.trim().toLowerCase(Locale.ROOT));
	}

	/**
	 * @return {@code true} if the AAC encoder is experimental, {@code false}
	 *         otherwise or if the version number is unknown.
	 */
	public boolean isAACEncoderExperimental() {
		Version libavcodecVersion = libraryVersions.get("libavcodec");
		if (libavcodecVersion == null) {
			return false;
		}
		if (isLibraryFFmpeg(libavcodecVersion)) {
			// FFMpeg made it non-experimental in version libavcodec 57.16.101 (d9791a8656b5580756d5b7ecc315057e8cd4255e)
			return libavcodecVersion.isLessThanOrEqualTo(new Version(57, 16, 101, 0));
		}
		// Libav has yet to make it non-experimental
		return true;
	}

	@Override
	public String toString() {
		return
			"FFmpegExecutableInfo [executablePath=" + executablePath + ", available=" + available +
			", version=" + version + ", errorType=" + errorType + ", errorText=" + errorText +
			", protocols=" + protocols + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + protocols.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof FFmpegExecutableInfo)) {
			return false;
		}
		FFmpegExecutableInfo other = (FFmpegExecutableInfo) obj;
		if (!protocols.equals(other.protocols)) {
			return false;
		}
		return true;
	}

	/**
	 * Parses the {@code -version} output and puts the result into the specified
	 * {@link FFmpegExecutableInfoBuilder}.
	 *
	 * @param builder The {@link FFmpegExecutableInfoBuilder} to put the parsed
	 *            information in.
	 * @param output The {@link ListProcessWrapperResult} {@code -version}
	 *            output.
	 */
	public static void parseVersions(
		@Nonnull FFmpegExecutableInfoBuilder builder,
		@Nullable ListProcessWrapperResult output
	) {
		if (builder == null) {
			throw new IllegalArgumentException("builder cannot be null");
		}
		if (output == null || output.getOutput().isEmpty()) {
			return;
		}

		Pattern configPattern = Pattern.compile("--enable-(\\S+)");
		Pattern libPattern = Pattern.compile("^\\s*(\\S+)\\s+(\\d+\\.\\s*\\d+\\.\\s*\\d+)\\s+/\\s+(\\d+\\.\\s*\\d+\\.\\s*\\d+)");
		Matcher matcher;
		for (int i = 0; i < output.getOutput().size(); i++) {
			if (i == 0) {
				Pattern pattern = Pattern.compile("^\\S+\\s+version\\s+(.*?)\\s+Copyright", Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(output.getOutput().get(i));
				if (matcher.find() && isNotBlank(matcher.group(1))) {
					builder.version(new Version(matcher.group(1)));
				}
			} else {
				matcher = configPattern.matcher(output.getOutput().get(i));
				if (matcher.find()) {
					// Configuration line
					builder.addEnabledOption(matcher.group(1));
					while (matcher.find()) {
						builder.addEnabledOption(matcher.group(1));
					}
				} else {
					matcher = libPattern.matcher(output.getOutput().get(i));
					if (matcher.find()) {
						// Library line
						builder.setLibraryVersion(matcher.group(1), new Version(matcher.group(2)));
					}
				}
			}
		}
	}

	/**
	 * Checks if a particular library is {@code FFmpeg} or {@code libav} based
	 * on its {@link Version}.
	 *
	 * @param libraryVersion the {@link Version} to evaluate.
	 * @return {@code true} if the specified library version number is FFmpeg,
	 *         {@code false} if it is {@code libav}.
	 * @throws IllegalArgumentException If {@code libraryVersion} is
	 *             {@code null}.
	 */
	public static boolean isLibraryFFmpeg(@Nonnull Version libraryVersion) {
		if (libraryVersion == null) {
			throw new IllegalArgumentException("libraryVersion cannot be null");
		}
		return libraryVersion.getRevision() >= 100;
	}

	/**
	 * A builder class to build {@link ExecutableInfo} instances by setting
	 * individual values.
	 */
	public static class FFmpegExecutableInfoBuilder extends ExecutableInfoBuilder {

		/**
		 * The {@link List} of {@link String}s with codes for the supported
		 * protocols
		 */
		@Nullable
		protected List<String> protocols;

		/**
		 * The {@link List} of enabled options in lower-case.
		 */
		@Nullable
		protected List<String> enabledOptions;

		/**
		 * The {@link Map} of libraries in lower-case and their corresponding
		 * {@link Version}s.
		 */
		@Nullable
		protected Map<String, Version> libraryVersions;

		/**
		 * Creates a new {@link FFmpegExecutableInfoBuilder} with no values set.
		 */
		public FFmpegExecutableInfoBuilder() {
		}

		/**
		 * Creates a new {@link FFmpegExecutableInfoBuilder} for the specified
		 * executable.
		 *
		 * @param executablePath the {@link Path} to the executable.
		 */
		public FFmpegExecutableInfoBuilder(Path executablePath) {
			this.executablePath = executablePath;
		}

		/**
		 * Creates a new {@link FFmpegExecutableInfoBuilder} whose values is
		 * initialized by the specified {@link ExecutableInfo}.
		 *
		 * @param executableInfo the {@link ExecutableInfo} whose values to use.
		 */
		public FFmpegExecutableInfoBuilder(ExecutableInfo executableInfo) {
			this.available = executableInfo.available;
			this.executablePath = executableInfo.executablePath;
			this.version = executableInfo.version;
			this.errorType = executableInfo.errorType;
			this.errorText = executableInfo.errorText;
			if (executableInfo instanceof FFmpegExecutableInfo) {
				this.protocols = ((FFmpegExecutableInfo) executableInfo).protocols;
			}
		}

		/**
		 * Creates a {@link FFmpegExecutableInfo} instance from this
		 * {@link FFmpegExecutableInfoBuilder}.
		 *
		 * @return The new {@link FFmpegExecutableInfo} instance.
		 */
		@Override
		@Nonnull
		public FFmpegExecutableInfo build() {
			return new FFmpegExecutableInfo(
				available,
				executablePath,
				version,
				errorType,
				errorText,
				protocols,
				enabledOptions,
				libraryVersions
			);
		}

		@Override
		@Nonnull
		public FFmpegExecutableInfoBuilder executablePath(Path executablePath) {
			this.executablePath = executablePath;
			return this;
		}

		@Override
		@Nonnull
		public FFmpegExecutableInfoBuilder available(Boolean available) {
			this.available = available;
			return this;
		}

		@Override
		@Nonnull
		public FFmpegExecutableInfoBuilder version(Version version) {
			this.version = version;
			return this;
		}

		@Override
		@Nonnull
		public FFmpegExecutableInfoBuilder errorType(ExecutableErrorType errorType) {
			this.errorType = errorType;
			return this;
		}

		@Override
		public FFmpegExecutableInfoBuilder errorText(String errorText) {
			this.errorText = errorText;
			return this;
		}

		/**
		 * @return The {@link List} of {@link String}s with codes for the
		 *         supported protocols or {@code null}.
		 */
		@Nullable
		public List<String> protocols() {
			return protocols;
		}

		/**
		 * Sets the {@link List} of {@link String}s with codes for the supported
		 * protocols.
		 *
		 * @param protocols the {@link List} of protocol codes to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder protocols(@Nullable List<String> protocols) {
			this.protocols = protocols;
			return this;
		}

		/**
		 * @return The {@link List} of enabled options in lower-case or
		 *         {@code null}.
		 */
		@Nullable
		public List<String> enabledOptions() {
			return enabledOptions;
		}

		/**
		 * Sets the {@link List} of {@link String}s with enabled options in
		 * lower-case.
		 *
		 * @param enabledOptions the {@link List} of lower-case enabled options
		 *            to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder enabledOptions(@Nullable List<String> enabledOptions) {
			this.enabledOptions = enabledOptions;
			return this;
		}

		/**
		 * Adds an enabled option to the list of enabled options.
		 *
		 * @param option the option to add.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder addEnabledOption(@Nullable String option) {
			if (isNotBlank(option)) {
				if (enabledOptions == null) {
					enabledOptions = new ArrayList<>();
				}
				enabledOptions.add(option.trim().toLowerCase(Locale.ROOT));
			}
			return this;
		}

		/**
		 * @return The {@link Map} of libraries in lower-case and their
		 *         corresponding {@link Version}s or {@code null}.
		 */
		@Nullable
		public Map<String, Version> libraryVersions() {
			return libraryVersions;
		}

		/**
		 * Sets the {@link Map} of libraries in lower-case and their
		 * corresponding {@link Version}s.
		 *
		 * @param libraryVersions the {@link Map} of libraries in lower-case and
		 *            their corresponding {@link Version}s to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder libraryVersions(@Nullable Map<String, Version> libraryVersions) {
			this.libraryVersions = libraryVersions;
			return this;
		}

		/**
		 * Sets the {@link Version} of the specified library.
		 *
		 * @param library the library for which to set the {@link Version}.
		 * @param version the {@link Version} to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder setLibraryVersion(@Nullable String library, @Nullable Version version) {
			if (isNotBlank(library)) {
				if (libraryVersions == null) {
					libraryVersions = new HashMap<>();
				}
				libraryVersions.put(library.trim().toLowerCase(Locale.ROOT), version);
			}
			return this;
		}
	}
}
