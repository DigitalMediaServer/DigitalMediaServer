package net.pms.configuration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.encoders.ExecutableErrorType;
import net.pms.io.ListProcessWrapperResult;
import net.pms.io.SimpleProcessWrapper;
import net.pms.util.Version;


/**
 * A class to hold basic information about a FFmpeg executable and its
 * availability status.
 *
 * @author Nadahar
 */
@Immutable
public class FFmpegExecutableInfo extends ExecutableInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegExecutableInfo.class);

	/**
	 * The {@link Map} of format name and {@link Set} of {@link MuxingOptions}
	 * pairs for the supported formats.
	 */
	@Nonnull
	protected final Map<String, EnumSet<MuxingOptions>> formats;

	/**
	 * The {@link Map} of codec identifier and {@link Codec} pairs for the
	 * supported codecs.
	 */
	@Nonnull
	protected final Map<String, Codec> codecs;

	/**
	 * The {@link Map} of protocol name and {@link Set} of
	 * {@link ProtocolOptions} pairs for the supported protocols.
	 */
	@Nonnull
	protected final Map<String, EnumSet<ProtocolOptions>> protocols;

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
	 * @param formats a {@link Map} of format name and {@link Set} of
	 *            {@link MuxingOptions} pairs for the supported formats for this
	 *            executable.
	 * @param codecs a {@link Map} of codec identifier and {@link Codec} pairs
	 *            for the supported codecs for this executable.
	 * @param protocols a {@link Map} of protocol name and {@link Set} of
	 *            {@link ProtocolOptions} pairs for the supported protocols for
	 *            this executable.
	 * @param libraryVersions a {@link Map} of libraries in lower-case and their
	 *            corresponding {@link Version}s.
	 */
	public FFmpegExecutableInfo(
		@Nullable Boolean available,
		@Nonnull Path path,
		@Nullable Version version,
		@Nullable ExecutableErrorType errorType,
		@Nullable String errorText,
		@Nullable Map<String, EnumSet<MuxingOptions>> formats,
		@Nullable Map<String, Codec> codecs,
		@Nullable Map<String, EnumSet<ProtocolOptions>> protocols,
		@Nullable Map<String, Version> libraryVersions
	) {
		super(available, path, version, errorType, errorText);
		this.formats = Collections.unmodifiableMap(
			formats == null ? new HashMap<String, EnumSet<MuxingOptions>>() : new HashMap<>(formats)
		);

		this.codecs = Collections.unmodifiableMap(
			codecs == null ? new HashMap<String, Codec>() : new HashMap<>(codecs)
		);

		this.protocols = Collections.unmodifiableMap(
			protocols == null ? new HashMap<String, EnumSet<ProtocolOptions>>() : new HashMap<>(protocols)
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
	 * @return The {@link Map} of format name and {@link Set} of
	 *         {@link MuxingOptions} pairs for the supported protocols.
	 */
	@Nonnull
	public Map<String, EnumSet<MuxingOptions>> getFormats() {
		return formats;
	}

	/**
	 * @return The {@link Map} of codec identifier and {@link Codec} pairs for
	 *         the supported codecs.
	 */
	@Nonnull
	public Map<String, Codec> getCodecs() {
		return codecs;
	}

	/**
	 * @return The {@link Map} of protocol name and {@link Set} of
	 *         {@link ProtocolOptions} pairs for the supported protocols.
	 */
	@Nonnull
	public Map<String, EnumSet<ProtocolOptions>> getProtocols() {
		return protocols;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("FFmpegExecutableInfo [executablePath=").append(executablePath)
			.append(", pathExists=").append(pathExists)
			.append(", available=").append(available)
			.append(", version=").append(version)
			.append(", errorType=").append(errorType)
			.append(", errorText=").append(errorText)
			.append(", formats=").append(formats)
			.append(", codecs=").append(codecs)
			.append(", protocols=").append(protocols)
			.append(", libraryVersions=").append(libraryVersions).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + codecs.hashCode();
		result = prime * result + formats.hashCode();
		result = prime * result + libraryVersions.hashCode();
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
		if (!codecs.equals(other.codecs)) {
			return false;
		}
		if (!formats.equals(other.formats)) {
			return false;
		}
		if (!libraryVersions.equals(other.libraryVersions)) {
			return false;
		}
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
				matcher = libPattern.matcher(output.getOutput().get(i));
				if (matcher.find()) {
					// Library line
					builder.setLibraryVersion(matcher.group(1), new Version(matcher.group(2)));
				}
			}
		}
	}

	public static void determineFormats(@Nonnull FFmpegExecutableInfoBuilder builder) throws InterruptedException {

		ListProcessWrapperResult output = SimpleProcessWrapper.runProcessListOutput(
			30000,
			1000,
			builder.executablePath().toString(),
			"-hide_banner",
			"-formats"
		);
		if (output.getError() != null) {
			LOGGER.error(
				"Failed to determine supported formats for \"{}\": {}",
				builder.executablePath(),
				output.getError().getMessage()
			);
			LOGGER.trace("", output.getError());
			return;
		}
		if (output.getExitCode() != 0) {
			LOGGER.error(
				"Failed to determine supported formats for \"{}\" with exit code {}",
				builder.executablePath(),
				output.getExitCode()
			);
			return;
		}

		builder.formats(new HashMap<String, EnumSet<MuxingOptions>>());

		Pattern formatLine = Pattern.compile("\\s([D ])([E ])\\s+(\\S+)\\s+(.*\\S)\\s*");

		for (String line : output.getOutput()) {
			if (isBlank(line)) {
				continue;
			}
			Matcher matcher = formatLine.matcher(line);
			if (matcher.matches()) {
				EnumSet<MuxingOptions> formats = EnumSet.noneOf(MuxingOptions.class);
				if ("D".equals(matcher.group(1))) {
					formats.add(MuxingOptions.DEMUXING);
				}
				if ("E".equals(matcher.group(2))) {
					formats.add(MuxingOptions.MUXING);
				}
				builder.formats().put(matcher.group(3), formats);
			}
		}
	}

	public static void determineCodecs(@Nonnull FFmpegExecutableInfoBuilder builder) throws InterruptedException {

		builder.codecs(new HashMap<String, CodecBuilder>());

		// Parse -codecs
		ListProcessWrapperResult output = SimpleProcessWrapper.runProcessListOutput(
			30000,
			1000,
			builder.executablePath().toString(),
			"-hide_banner",
			"-codecs"
		);
		if (output.getError() != null) {
			LOGGER.error(
				"Failed to determine supported codecs for \"{}\": {}",
				builder.executablePath(),
				output.getError().getMessage()
			);
			LOGGER.trace("", output.getError());
		} else if (output.getExitCode() != 0) {
			LOGGER.error(
				"Failed to determine supported codecs for \"{}\" with exit code {}",
				builder.executablePath(),
				output.getExitCode()
			);
		} else {
			Pattern codecLine = Pattern.compile("\\s([D\\.])([E\\.])([ASV\\.])([I\\.])([L\\.])([S\\.])\\s+(\\S+)\\s+(.*\\S)\\s*");

			boolean header = true;
			for (String line : output.getOutput()) {
				if (isBlank(line)) {
					continue;
				}
				if (header) {
					if (line.startsWith(" ---")) {
						header = false;
					}
					continue;
				}
				Matcher matcher = codecLine.matcher(line);
				if (matcher.matches()) {
					CodecBuilder codecBuilder = builder.codecs().get(matcher.group(7));
					if (codecBuilder == null) {
						codecBuilder = new CodecBuilder(matcher.group(7), matcher.group(8));
						builder.codecs().put(codecBuilder.identifier(), codecBuilder);
					}
					EnumSet<CodecOptions> options = codecBuilder.options();
					if (options == null) {
						options = EnumSet.noneOf(CodecOptions.class);
						codecBuilder.options(options);
					}
					codecBuilder.isCodec(true);
					if ("D".equals(matcher.group(1))) {
						codecBuilder.hasDecoder(true);
					}
					if ("E".equals(matcher.group(2))) {
						codecBuilder.hasEncoder(true);
					}
					if ("A".equals(matcher.group(3))) {
						codecBuilder.codecType(CodecType.AUDIO);
					} else if ("S".equals(matcher.group(3))) {
						codecBuilder.codecType(CodecType.SUBTITLE);
					} else if ("V".equals(matcher.group(3))) {
						codecBuilder.codecType(CodecType.VIDEO);
					}
					if ("I".equals(matcher.group(4))) {
						options.add(CodecOptions.INTRA_FRAME);
					}
					if ("L".equals(matcher.group(5))) {
						options.add(CodecOptions.LOSSY);
					}
					if ("S".equals(matcher.group(6))) {
						options.add(CodecOptions.LOSSLESS);
					}
				}
			}
		}

		Pattern codersLine = Pattern.compile("\\s([ASV\\.])([F\\.])([S\\.])([X\\.])([B\\.])([D\\.])\\s+(\\S+)\\s+(.*\\S)\\s*");

		// Parse -decoders
		output = SimpleProcessWrapper.runProcessListOutput(
			30000,
			1000,
			builder.executablePath().toString(),
			"-hide_banner",
			"-decoders"
		);
		if (output.getError() != null) {
			LOGGER.error(
				"Failed to determine supported decoders for \"{}\": {}",
				builder.executablePath(),
				output.getError().getMessage()
			);
			LOGGER.trace("", output.getError());
		} else if (output.getExitCode() != 0) {
			LOGGER.error(
				"Failed to determine supported decoders for \"{}\" with exit code {}",
				builder.executablePath(),
				output.getExitCode()
			);
		} else {
			boolean header = true;
			for (String line : output.getOutput()) {
				if (isBlank(line)) {
					continue;
				}
				if (header) {
					if (line.startsWith(" ---")) {
						header = false;
					}
					continue;
				}
				Matcher matcher = codersLine.matcher(line);
				if (matcher.matches()) {
					CodecBuilder codecBuilder = builder.codecs().get(matcher.group(7));
					if (codecBuilder == null) {
						codecBuilder = new CodecBuilder(matcher.group(7), matcher.group(8));
						builder.codecs().put(codecBuilder.identifier(), codecBuilder);
					}
					EnumSet<CoderOptions> options = codecBuilder.decoderOptions();
					if (options == null) {
						options = EnumSet.noneOf(CoderOptions.class);
						codecBuilder.decoderOptions(options);
					}
					codecBuilder.hasDecoder(true);
					if ("A".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.AUDIO);
					} else if ("S".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.SUBTITLE);
					} else if ("V".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.VIDEO);
					}
					if ("F".equals(matcher.group(2))) {
						options.add(CoderOptions.FRAME_LEVEL);
					}
					if ("S".equals(matcher.group(3))) {
						options.add(CoderOptions.SLICE_LEVEL);
					}
					if ("X".equals(matcher.group(4))) {
						options.add(CoderOptions.EXPERIMENTAL);
					}
					if ("B".equals(matcher.group(5))) {
						options.add(CoderOptions.DRAW_HORIZ);
					}
					if ("D".equals(matcher.group(6))) {
						options.add(CoderOptions.DIRECT_1);
					}
				}
			}
		}

		// Parse -encoders
		output = SimpleProcessWrapper.runProcessListOutput(
			30000,
			1000,
			builder.executablePath().toString(),
			"-hide_banner",
			"-encoders"
		);
		if (output.getError() != null) {
			LOGGER.error(
				"Failed to determine supported encoders for \"{}\": {}",
				builder.executablePath(),
				output.getError().getMessage()
			);
			LOGGER.trace("", output.getError());
		} else if (output.getExitCode() != 0) {
			LOGGER.error(
				"Failed to determine supported encoders for \"{}\" with exit code {}",
				builder.executablePath(),
				output.getExitCode()
			);
		} else {
			boolean header = true;
			for (String line : output.getOutput()) {
				if (isBlank(line)) {
					continue;
				}
				if (header) {
					if (line.startsWith(" ---")) {
						header = false;
					}
					continue;
				}
				Matcher matcher = codersLine.matcher(line); //TODO: (Nad) JavaDocs
				if (matcher.matches()) {
					CodecBuilder codecBuilder = builder.codecs().get(matcher.group(7));
					if (codecBuilder == null) {
						codecBuilder = new CodecBuilder(matcher.group(7), matcher.group(8));
						builder.codecs().put(codecBuilder.identifier(), codecBuilder);
					}
					EnumSet<CoderOptions> options = codecBuilder.encoderOptions();
					if (options == null) {
						options = EnumSet.noneOf(CoderOptions.class);
						codecBuilder.encoderOptions(options);
					}
					codecBuilder.hasEncoder(true);
					if ("A".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.AUDIO);
					} else if ("S".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.SUBTITLE);
					} else if ("V".equals(matcher.group(1))) {
						codecBuilder.codecType(CodecType.VIDEO);
					}
					if ("F".equals(matcher.group(2))) {
						options.add(CoderOptions.FRAME_LEVEL);
					}
					if ("S".equals(matcher.group(3))) {
						options.add(CoderOptions.SLICE_LEVEL);
					}
					if ("X".equals(matcher.group(4))) {
						options.add(CoderOptions.EXPERIMENTAL);
					}
					if ("B".equals(matcher.group(5))) {
						options.add(CoderOptions.DRAW_HORIZ);
					}
					if ("D".equals(matcher.group(6))) {
						options.add(CoderOptions.DIRECT_1);
					}
				}
			}
		}
	}

	public static void determineProtocols(@Nonnull FFmpegExecutableInfoBuilder builder) throws InterruptedException {
		ListProcessWrapperResult output = SimpleProcessWrapper.runProcessListOutput(
			30000,
			1000,
			builder.executablePath().toString(),
			"-hide_banner",
			"-protocols"
		);
		if (output.getError() != null) {
			LOGGER.error(
				"Failed to determine supported protocols for \"{}\": {}",
				builder.executablePath(),
				output.getError().getMessage()
			);
			LOGGER.trace("", output.getError());
			return;
		}
		if (output.getExitCode() != 0) {
			LOGGER.error(
				"Failed to determine supported protocols for \"{}\" with exit code {}",
				builder.executablePath(),
				output.getExitCode()
			);
			return;
		}
		boolean inputs = false;
		boolean outputs = false;

		builder.protocols(new HashMap<String, EnumSet<ProtocolOptions>>());

		// Old style - see http://git.videolan.org/?p=ffmpeg.git;a=commitdiff;h=cdc6a87f193b1bf99a640a44374d4f2597118959
		Pattern oldStyle = Pattern.compile("([I\\.])([O\\.])[S\\.]\\s+(.*\\S)\\s*");

		for (String line : output.getOutput()) {
			if (isBlank(line)) {
				continue;
			}
			if ("Input:".equals(line)) {
				inputs = true;
				outputs = false;
			} else if ("Output:".equals(line)) {
				inputs = false;
				outputs = true;
			} else {
				Matcher matcher = oldStyle.matcher(line);
				if (matcher.matches()) {
					EnumSet<ProtocolOptions> options = EnumSet.noneOf(ProtocolOptions.class);
					if ("I".equals(matcher.group(1))) {
						options.add(ProtocolOptions.INPUT);
					}
					if ("O".equals(matcher.group(2))) {
						options.add(ProtocolOptions.OUTPUT);
					}
					if (!options.isEmpty()) {
						builder.protocols().put(matcher.group(3), options);
					}
				} else if (inputs || outputs) {
					String trimmedLine = line.trim();
					EnumSet<ProtocolOptions> options = builder.protocols().get(trimmedLine);
					if (options == null) {
						options = EnumSet.noneOf(ProtocolOptions.class);
						builder.protocols().put(trimmedLine, options);
					}
					options.add(inputs ? ProtocolOptions.INPUT : ProtocolOptions.OUTPUT);
				}
			}
		}
		if (builder.protocols().containsKey("mmsh")) {
			// Workaround for FFmpeg bug: http://ffmpeg.org/trac/ffmpeg/ticket/998
			EnumSet<ProtocolOptions> options = builder.protocols().get("mmsh");
			builder.protocols.put("mms", options);
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

	@Nonnull
	public static String toFormatsString(@Nullable Map<String, EnumSet<MuxingOptions>> formats) {
		if (formats == null || formats.isEmpty()) {
			return "None";
		}
		ArrayList<String> formatEntries = new ArrayList<>(formats.size());
		StringBuilder sb = new StringBuilder();
		boolean first;
		for (Entry<String, EnumSet<MuxingOptions>> entry : formats.entrySet()) {
			sb.setLength(0);
			first = true;
			sb.append(entry.getKey()).append(" (");
			if (entry.getValue().contains(MuxingOptions.DEMUXING)) {
				sb.append("D");
				first = false;
			}
			if (entry.getValue().contains(MuxingOptions.MUXING)) {
				if (!first) {
					sb.append(",");
				}
				sb.append("M");
			}
			sb.append(")");
			formatEntries.add(sb.toString());
		}

		Collections.sort(formatEntries);
		sb.setLength(0);
		first = true;
		for (String format : formatEntries) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(format);
		}

		return sb.toString();
	}

	@Nonnull
	public static String toCodecsString(@Nullable Map<String, CodecBuilder> codecs) {
		if (codecs == null || codecs.isEmpty()) {
			return "None";
		}
		ArrayList<String> formatEntries = new ArrayList<>(codecs.size());
		StringBuilder sb = new StringBuilder();
		boolean first;
		for (CodecBuilder codec : codecs.values()) {
			formatEntries.add(codec.build().toString(true, false));
		}

		Collections.sort(formatEntries);
		sb.setLength(0);
		first = true;
		for (String format : formatEntries) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(format);
		}

		return sb.toString();
	}

	@Nonnull
	public static String toProtocolsString(@Nullable Map<String, EnumSet<ProtocolOptions>> protocols) {
		if (protocols == null || protocols.isEmpty()) {
			return "None";
		}
		ArrayList<String> protocolEntries = new ArrayList<>(protocols.size());
		StringBuilder sb = new StringBuilder();
		boolean first;
		for (Entry<String, EnumSet<ProtocolOptions>> entry : protocols.entrySet()) {
			sb.setLength(0);
			first = true;
			sb.append(entry.getKey()).append(" (");
			if (entry.getValue().contains(ProtocolOptions.INPUT)) {
				sb.append("I");
				first = false;
			}
			if (entry.getValue().contains(ProtocolOptions.OUTPUT)) {
				if (!first) {
					sb.append(",");
				}
				sb.append("O");
			}
			sb.append(")");
			protocolEntries.add(sb.toString());
		}

		Collections.sort(protocolEntries);
		sb.setLength(0);
		first = true;
		for (String protocol : protocolEntries) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(protocol);
		}

		return sb.toString();
	}

	/**
	 * A builder class to build {@link ExecutableInfo} instances by setting
	 * individual values.
	 */
	public static class FFmpegExecutableInfoBuilder extends ExecutableInfoBuilder {

		/**
		 * The {@link Map} of format name and {@link Set} of
		 * {@link MuxingOptions} pairs for the supported formats.
		 */
		@Nullable
		protected Map<String, EnumSet<MuxingOptions>> formats;

		/**
		 * The {@link Map} of codec identifier and {@link CodecBuilder} pairs
		 * for the supported codecs.
		 */
		@Nullable
		protected Map<String, CodecBuilder> codecs;

		/**
		 * The {@link Map} of protocol name and {@link Set} of
		 * {@link ProtocolOptions} pairs for the supported protocols.
		 */
		@Nullable
		protected Map<String, EnumSet<ProtocolOptions>> protocols;

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
			HashMap<String, Codec> builtCodecs = new HashMap<>();
			if (codecs != null) {
				for (Entry<String, CodecBuilder> entry : codecs.entrySet()) {
					builtCodecs.put(entry.getKey(), entry.getValue().build());
				}
			}

			return new FFmpegExecutableInfo(
				available,
				executablePath,
				version,
				errorType,
				errorText,
				formats,
				builtCodecs,
				protocols,
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
		 * @return The {@link Map} of format name and {@link Set} of
		 *         {@link MuxingOptions} pairs for the supported protocols or
		 *         {@code null}.
		 */
		@Nullable
		public Map<String, EnumSet<MuxingOptions>> formats() {
			return formats;
		}

		/**
		 * Sets the {@link Map} of format name and {@link Set} of
		 * {@link MuxingOptions} pairs for the supported formats.
		 *
		 * @param formats the {@link Map} of format name and {@link Set} of
		 *            {@link MuxingOptions} pairs to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder formats(@Nullable Map<String, EnumSet<MuxingOptions>> formats) {
			this.formats = formats;
			return this;
		}

		/**
		 * @return The {@link Map} of codec identifier and {@link CodecBuilder}
		 *         pairs for the supported codecs or {@code null}.
		 */
		@Nullable
		public Map<String, CodecBuilder> codecs() {
			return codecs;
		}

		/**
		 * Sets the {@link Map} of codec identifier and {@link CodecBuilder}
		 * pairs for the supported codecs.
		 *
		 * @param codecs the {@link Map} of codec identifier and
		 *            {@link CodecBuilder} pairs to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder codecs(@Nullable Map<String, CodecBuilder> codecs) {
			this.codecs = codecs;
			return this;
		}

		/**
		 * @return The {@link Map} of protocol name and {@link Set} of
		 *         {@link ProtocolOptions} pairs for the supported protocols or
		 *         {@code null}.
		 */
		@Nullable
		public Map<String, EnumSet<ProtocolOptions>> protocols() {
			return protocols;
		}

		/**
		 * Sets the {@link Map} of protocol name and {@link Set} of
		 * {@link ProtocolOptions} pairs for the supported protocols.
		 *
		 * @param protocols the {@link Map} of protocol name and {@link Set} of
		 *            {@link ProtocolOptions} pairs to set.
		 * @return This {@link FFmpegExecutableInfoBuilder} instance.
		 */
		@Nonnull
		public FFmpegExecutableInfoBuilder protocols(@Nullable Map<String, EnumSet<ProtocolOptions>> protocols) {
			this.protocols = protocols;
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

	public static enum ProtocolOptions {

		/** Protocol supports input */
		INPUT,

		/** Protocol supports output */
		OUTPUT;
	}

	@Immutable
	public static class Codec {

		private final boolean isCodec;

		private final boolean hasDecoder;

		private final boolean hasEncoder;

		@Nonnull
		private final String identifier;

		@Nonnull
		private final String description;

		@Nonnull
		private final CodecType codecType;

		@Nonnull
		private final EnumSet<CodecOptions> options;

		@Nonnull
		private final EnumSet<CoderOptions> decoderOptions;

		@Nonnull
		private final EnumSet<CoderOptions> encoderOptions;

		public Codec(
			boolean isCodec,
			boolean hasDecoder,
			boolean hasEncoder,
			@Nonnull String identifier,
			@Nonnull String description,
			@Nullable CodecType codecType,
			@Nullable EnumSet<CodecOptions> options,
			@Nullable EnumSet<CoderOptions> decoderOptions,
			@Nullable EnumSet<CoderOptions> encoderOptions
		) {
			if (isBlank(identifier)) {
				throw new IllegalArgumentException("identifier cannot be blank");
			}
			if (description == null) {
				throw new IllegalArgumentException("description cannot be null");
			}
			this.isCodec = isCodec;
			this.hasDecoder = hasDecoder;
			this.hasEncoder = hasEncoder;
			this.identifier = identifier;
			this.description = description;
			this.codecType = codecType == null ? CodecType.UNKNOWN : codecType;
			this.options = options == null ? EnumSet.noneOf(CodecOptions.class) : options;
			this.decoderOptions = decoderOptions == null ? EnumSet.noneOf(CoderOptions.class) : decoderOptions;
			this.encoderOptions = encoderOptions == null ? EnumSet.noneOf(CoderOptions.class) : encoderOptions;
		}

		/**
		 * @return {@code true} if this is registered as a codec, {@code false}
		 *         otherwise.
		 */
		public boolean isCodec() {
			return isCodec;
		}

		/**
		 * @return {@code true} if this has a decoder, {@code false} otherwise.
		 */
		public boolean hasDecoder() {
			return hasDecoder;
		}

		/**
		 * @return {@code true} if this has an encoder, {@code false} otherwise.
		 */
		public boolean hasEncoder() {
			return hasEncoder;
		}

		/**
		 * @return The identifier.
		 */
		@Nonnull
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * @return The description.
		 */
		@Nonnull
		public String getDescription() {
			return description;
		}

		/**
		 * @return The {@link CodecType}.
		 */
		@Nonnull
		public CodecType getCodecType() {
			return codecType;
		}

		/**
		 * Determines if this {@link Codec} has the specified
		 * {@link CodecOptions} option set.
		 *
		 * @param option the {@link CodecOptions} to query.
		 * @return {@code true} if the {@link CodecOptions} option exists,
		 *         {@code false} otherwise.
		 */
		public boolean containsOption(@Nullable CodecOptions option) {
			if (option == null) {
				return false;
			}
			return options.contains(option);
		}

		/**
		 * @return An {@link EnumSet} of {@link CodecOptions}.
		 */
		@Nonnull
		public EnumSet<CodecOptions> getOptions() {
			return EnumSet.copyOf(options);
		}

		/**
		 * Determines if the decoder has the specified {@link CoderOptions}
		 * option set.
		 *
		 * @param option the {@link CoderOptions} to query.
		 * @return {@code true} if the {@link CoderOptions} option exists for
		 *         the decoder, {@code false} otherwise.
		 */
		public boolean containsDecoderOption(@Nullable CoderOptions option) {
			if (option == null) {
				return false;
			}
			return decoderOptions.contains(option);
		}

		/**
		 * @return An {@link EnumSet} of the decoder {@link CoderOptions}.
		 */
		@Nonnull
		public EnumSet<CoderOptions> getDecoderOptions() {
			return EnumSet.copyOf(decoderOptions);
		}

		/**
		 * Determines if the encoder has the specified {@link CoderOptions}
		 * option set.
		 *
		 * @param option the {@link CoderOptions} to query.
		 * @return {@code true} if the {@link CoderOptions} option exists for
		 *         the encoder, {@code false} otherwise.
		 */
		public boolean containsEncoderOption(@Nullable CoderOptions option) {
			if (option == null) {
				return false;
			}
			return encoderOptions.contains(option);
		}

		/**
		 * @return An {@link EnumSet} of the encoder {@link CoderOptions}.
		 */
		@Nonnull
		public EnumSet<CoderOptions> getEncoderOptions() {
			return EnumSet.copyOf(encoderOptions);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + codecType.hashCode();
			result = prime * result + decoderOptions.hashCode();
			result = prime * result + description.hashCode();
			result = prime * result + encoderOptions.hashCode();
			result = prime * result + (hasDecoder ? 1231 : 1237);
			result = prime * result + (hasEncoder ? 1231 : 1237);
			result = prime * result + identifier.hashCode();
			result = prime * result + (isCodec ? 1231 : 1237);
			result = prime * result + options.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Codec)) {
				return false;
			}
			Codec other = (Codec) obj;
			if (codecType != other.codecType) {
				return false;
			}
			if (!decoderOptions.equals(other.decoderOptions)) {
				return false;
			}
			if (!description.equals(other.description)) {
				return false;
			}
			if (!encoderOptions.equals(other.encoderOptions)) {
				return false;
			}
			if (hasDecoder != other.hasDecoder) {
				return false;
			}
			if (hasEncoder != other.hasEncoder) {
				return false;
			}
			if (!identifier.equals(other.identifier)) {
				return false;
			}
			if (isCodec != other.isCodec) {
				return false;
			}
			if (!options.equals(other.options)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return toString(false, false);
		}

		@Nonnull
		public String toString(boolean compact, boolean verbose) {
			StringBuilder sb = new StringBuilder();
			if (compact) {
				sb.append(identifier).append(" (");
				sb.append(codecType.name().substring(0, 1));
				if (isCodec) {
					sb.append(",C");
				}
				if (hasDecoder) {
					sb.append(containsDecoderOption(CoderOptions.EXPERIMENTAL) ? ",xD" : ",D");
				}
				if (hasEncoder) {
					sb.append(containsEncoderOption(CoderOptions.EXPERIMENTAL) ? ",xE" : ",E");
				}
				sb.append(")");
			} else {
				sb.append(getClass().getSimpleName()).append("[");
				sb.append(identifier);
				if (verbose) {
					sb.append(" (").append(description).append(")");
				}
				sb.append(": ");
				if (verbose) {
					sb.append("Codec Type=").append(codecType);
					sb.append(", Is Codec=").append(isCodec);
					sb.append(", Has Decoder=").append(hasDecoder);
					sb.append(", Has Encoder=").append(hasEncoder);
				} else {
					sb.append(codecType);
					if (isCodec) {
						sb.append(", Codec");
					}
					if (hasDecoder) {
						sb.append(", Decoder");
					}
					if (hasEncoder) {
						sb.append(", Encoder");
					}
				}
				if (!options.isEmpty()) {
					sb.append(", Options=").append(options);
				}
				if (!decoderOptions.isEmpty()) {
					sb.append(", Decoder Options=").append(decoderOptions);
				}
				if (!encoderOptions.isEmpty()) {
					sb.append(", Encoder Options=").append(encoderOptions);
				}
				sb.append("]");
			}

			return sb.toString();
		}
	}

	public static class CodecBuilder {

		private boolean isCodec;

		private boolean hasDecoder;

		private boolean hasEncoder;

		@Nullable
		private String identifier;

		@Nullable
		private String description;

		@Nullable
		private CodecType codecType;

		@Nullable
		private EnumSet<CodecOptions> options;

		@Nullable
		private EnumSet<CoderOptions> decoderOptions;

		@Nullable
		private EnumSet<CoderOptions> encoderOptions;

		public CodecBuilder() {
		}

		public CodecBuilder(@Nullable String identifier) {
			this.identifier = identifier;
		}

		public CodecBuilder(@Nullable String identifier, @Nullable String description) {
			this.identifier = identifier;
			this.description = description;
		}

		public CodecBuilder(@Nullable Codec codec) {
			if (codec != null) {
				this.isCodec = codec.isCodec;
				this.hasDecoder = codec.hasDecoder;
				this.hasEncoder = codec.hasEncoder;
				this.identifier = codec.identifier;
				this.description = codec.description;
				this.codecType = codec.codecType;
				this.options = EnumSet.copyOf(codec.options);
				this.decoderOptions = EnumSet.copyOf(codec.decoderOptions);
				this.encoderOptions = EnumSet.copyOf(codec.encoderOptions);
			}
		}

		public CodecBuilder(
			boolean isCodec,
			boolean hasDecoder,
			boolean hasEncoder,
			@Nullable String identifier,
			@Nullable String description,
			@Nullable CodecType codecType,
			@Nullable EnumSet<CodecOptions> options,
			@Nullable EnumSet<CoderOptions> decoderOptions,
			@Nullable EnumSet<CoderOptions> encoderOptions
		) {
			this.isCodec = isCodec;
			this.hasDecoder = hasDecoder;
			this.hasEncoder = hasEncoder;
			this.identifier = identifier;
			this.description = description;
			this.codecType = codecType;
			this.options = options;
			this.decoderOptions = decoderOptions;
			this.encoderOptions = encoderOptions;
		}

		/**
		 * @return {@code true} if this is registered as a codec, {@code false}
		 *         otherwise.
		 */
		public boolean isCodec() {
			return isCodec;
		}

		@Nonnull
		public CodecBuilder isCodec(boolean value) {
			isCodec = value;
			return this;
		}

		/**
		 * @return {@code true} if this has a decoder, {@code false} otherwise.
		 */
		public boolean hasDecoder() {
			return hasDecoder;
		}

		@Nonnull
		public CodecBuilder hasDecoder(boolean value) {
			hasDecoder = value;
			return this;
		}

		/**
		 * @return {@code true} if this has an encoder, {@code false} otherwise.
		 */
		public boolean hasEncoder() {
			return hasEncoder;
		}

		@Nonnull
		public CodecBuilder hasEncoder(boolean value) {
			hasEncoder = value;
			return this;
		}

		/**
		 * @return The identifier.
		 */
		@Nullable
		public String identifier() {
			return identifier;
		}

		@Nonnull
		public CodecBuilder identifier(@Nullable String value) {
			identifier = value;
			return this;
		}

		/**
		 * @return The description.
		 */
		@Nullable
		public String description() {
			return description;
		}

		@Nonnull
		public CodecBuilder description(@Nullable String value) {
			description = value;
			return this;
		}

		/**
		 * @return The {@link CodecType}.
		 */
		@Nullable
		public CodecType codecType() {
			return codecType;
		}

		@Nonnull
		public CodecBuilder codecType(@Nullable CodecType value) {
			codecType = value;
			return this;
		}

		/**
		 * @return The {@link EnumSet} of {@link CodecOptions}.
		 */
		@Nullable
		public EnumSet<CodecOptions> options() {
			return options;
		}

		@Nonnull
		public CodecBuilder options(@Nullable EnumSet<CodecOptions> value) {
			options = value;
			return this;
		}

		/**
		 * @return The {@link EnumSet} of the decoder {@link CoderOptions}.
		 */
		@Nullable
		public EnumSet<CoderOptions> decoderOptions() {
			return decoderOptions;
		}

		@Nonnull
		public CodecBuilder decoderOptions(@Nullable EnumSet<CoderOptions> value) {
			decoderOptions = value;
			return this;
		}

		/**
		 * @return The {@link EnumSet} of the encoder {@link CoderOptions}.
		 */
		@Nullable
		public EnumSet<CoderOptions> encoderOptions() {
			return encoderOptions;
		}

		@Nonnull
		public CodecBuilder encoderOptions(@Nullable EnumSet<CoderOptions> value) {
			encoderOptions = value;
			return this;
		}

		/**
		 * Creates a {@link Codec} instance from this {@link CodecBuilder}.
		 *
		 * @return The new {@link Codec} instance.
		 */
		@Nonnull
		public Codec build() {
			return new Codec(
				isCodec,
				hasDecoder,
				hasEncoder,
				identifier,
				description,
				codecType,
				options,
				decoderOptions,
				encoderOptions
			);
		}

		@Override
		public String toString() {
			return "CodecBuilder [isCodec=" + isCodec + ", hasDecoder=" + hasDecoder + ", hasEncoder=" + hasEncoder +
				", identifier=" + identifier + ", description=" + description + ", codecType=" + codecType +
				", options=" + options + ", decoderOptions=" + decoderOptions + ", encoderOptions=" + encoderOptions + "]";
		}
	}

	public static enum CodecType {

		/** Audio */
		AUDIO("Audio"),

		/** Subtitle */
		SUBTITLE("Subtitle"),

		/** Unknown type */
		UNKNOWN("Unknown type"),

		/** Video */
		VIDEO("Video");

		private final String name;

		private CodecType(@Nonnull String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum CodecOptions {

		/** Intra frame-only codec */
		INTRA_FRAME("Intra frame-only"),

		/** Lossless compression */
		LOSSLESS("Lossless"),

		/** Lossy compression */
		LOSSY("Lossy");

		private final String name;

		private CodecOptions(@Nonnull String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum CoderOptions {

		/** Supports direct rendering method 1 */
		DIRECT_1("Direct rendering method 1"),

		/** Supports draw_horiz_band */
		DRAW_HORIZ("Draw horizontal band"),

		/** Codec is experimental */
		EXPERIMENTAL("Experimental"),

		/** Frame-level multithreading */
		FRAME_LEVEL("Frame-level multithreading"),

		/** Slice-level multithreading */
		SLICE_LEVEL("Slice-level multithreading");

		private final String name;

		private CoderOptions(@Nonnull String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum MuxingOptions {

		/** Demuxing */
		DEMUXING("Demuxing"),

		/** Muxing */
		MUXING("Muxing");

		private final String name;

		private MuxingOptions(@Nonnull String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
