package net.pms.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.sun.jna.Platform;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.WindowsProgramPaths;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.exception.InvalidFileSystemException;
import net.pms.formats.FormatFactory;
import net.pms.formats.v2.SubtitleType;
import net.pms.io.BasicSystemUtils;
import net.pms.util.FilePermissions.FileFlag;
import net.pms.util.StringUtil.LetterCase;
import static net.pms.util.Constants.*;
import static org.apache.commons.lang3.StringUtils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
	private static final ReentrantLock subtitleCacheLock = new ReentrantLock();
	private static final Map<File, File[]> subtitleCache = new HashMap<>();
	private static final int S_ISVTX = 512; // Unix sticky bit mask

	// This class is not instantiable
	private FileUtil() { }

	/**
	 * Resolves a file {@link Path} using a default folder and file name to fill
	 * in what's missing from the specified path. The returned path is
	 * "normalized" by the file system if possible, and is always absolute.
	 *
	 * @param customPath the specified path.
	 * @param defaultFolder the default folder Path.
	 * @param defaultFileName the default file name.
	 * @param options indicating how symbolic links are handled.
	 * @return The resulting file {@link Path}.
	 */
	@Nonnull
	public static Path resolvePathWithDefaults(
		@Nullable String customPath,
		@Nullable String defaultFolder,
		@Nonnull String defaultFileName,
		@Nullable LinkOption... options
	) {
		return resolvePathWithDefaults(
			customPath == null ? null : Paths.get(customPath),
			defaultFolder == null ? null : Paths.get(defaultFolder),
			Paths.get(defaultFileName),
			options
		);
	}

	/**
	 * Resolves a file {@link Path} using a default folder and file name to fill
	 * in what's missing from the specified path. The returned path is
	 * "normalized" by the file system if possible, and is always absolute.
	 *
	 * @param customPath the specified path.
	 * @param defaultFolder the default folder {@link File}.
	 * @param defaultFileName the default file name.
	 * @param options indicating how symbolic links are handled.
	 * @return The resulting file {@link Path}.
	 */
	@Nonnull
	public static Path resolvePathWithDefaults(
		@Nullable String customPath,
		@Nullable File defaultFolder,
		@Nonnull String defaultFileName,
		@Nullable LinkOption... options
	) {
		return resolvePathWithDefaults(
			customPath == null ? null : Paths.get(customPath),
			defaultFolder == null ? null : defaultFolder.toPath(),
			Paths.get(defaultFileName),
			options
		);
	}

	/**
	 * Resolves a file {@link Path} using a default folder and file name to fill
	 * in what's missing from the specified path. The returned path is
	 * "normalized" by the file system if possible, and is always absolute.
	 *
	 * @param customPath the specified {@link File}.
	 * @param defaultFolder the default folder {@link File}.
	 * @param defaultFileName the default file name {@link File}.
	 * @param options indicating how symbolic links are handled.
	 * @return The resulting file {@link Path}.
	 */
	@Nonnull
	public static Path resolvePathWithDefaults(
		@Nullable File customPath,
		@Nullable File defaultFolder,
		@Nonnull File defaultFileName,
		@Nullable LinkOption... options
	) {
		return resolvePathWithDefaults(
			customPath == null ? null : customPath.toPath(),
			defaultFolder == null ? null : defaultFolder.toPath(),
			defaultFileName.toPath(),
			options
		);
	}

	/**
	 * Resolves a file {@link Path} using a default folder and file name to fill
	 * in what's missing from the specified path. The returned path is
	 * "normalized" by the file system if possible, and is always absolute.
	 *
	 * @param customPath the specified path.
	 * @param defaultFolder the default folder {@link Path}.
	 * @param defaultFileName the default file name.
	 * @param options indicating how symbolic links are handled.
	 * @return The resulting file {@link Path}.
	 */
	@Nonnull
	public static Path resolvePathWithDefaults(
		@Nullable String customPath,
		@Nullable Path defaultFolder,
		@Nonnull String defaultFileName,
		@Nullable LinkOption... options
	) {
		return resolvePathWithDefaults(
			customPath == null ? null : Paths.get(customPath),
			defaultFolder,
			Paths.get(defaultFileName),
			options
		);
	}

	/**
	 * Resolves a file {@link Path} using a default folder and file name to fill
	 * in what's missing from the specified path. The returned path is
	 * "normalized" by the file system if possible, and is always absolute.
	 *
	 * @param customPath the specified {@link Path}.
	 * @param defaultFolder the default folder {@link Path}.
	 * @param defaultFileName the default file name {@link Path}.
	 * @param options indicating how symbolic links are handled.
	 * @return The resulting file {@link Path}.
	 */
	@Nonnull
	public static Path resolvePathWithDefaults(
		@Nullable Path customPath,
		@Nullable Path defaultFolder,
		@Nonnull Path defaultFileName,
		@Nullable LinkOption... options
	) {
		/*
		 * This method is (also) called during initialization of DMS, before
		 * the logging is up and running. Every effort is therefore made to
		 * resolve the path.
		 */
		if (defaultFileName == null) {
			throw new IllegalArgumentException("defaultFileName cannot be null");
		}
		if (defaultFileName.getNameCount() == 0) {
			throw new IllegalArgumentException("defaultFileName cannot be empty");
		}
		Path result;
		if (customPath != null) {
			// Try to determine whether customPath is a file or a folder
			if (FileUtil.isFolder(customPath, options)) {
				if (defaultFileName.isAbsolute()) {
					result = customPath.resolve(defaultFileName.getFileName());
				} else {
					result = customPath.resolve(defaultFileName);
				}
			} else {
				result = customPath;
			}
		} else {
			if (defaultFolder == null) {
				result = defaultFileName;
			} else {
				if (defaultFileName.isAbsolute()) {
					result = defaultFolder.resolve(defaultFileName.getFileName());
				} else {
					result = defaultFolder.resolve(defaultFileName);
				}
			}
		}

		try {
			return result.toRealPath(options);
		} catch (IOException e) {
			return result.toAbsolutePath();
		}
	}

	/**
	 * Evaluates whether the specified {@link Path} represents a folder (as opposed
	 * to a file).
	 * <p>
	 * This method uses a two-fold approach: If the path exists the the answer is
	 * acquired from the underlying file system. If the path doesn't exist, the name
	 * of the last element in the path is used to make a "best guess".
	 *
	 * @param folder the {@link Path} to evaluate.
	 * @param options specify {@link LinkOption#NOFOLLOW_LINKS} to not follow links
	 *                while resolving {@code folder}.
	 * @return {@code true} if {@code folder} is evaluated as being a folder,
	 *         {@code false} otherwise.
	 */
	public static boolean isFolder(@Nullable Path folder, LinkOption... options) {
		if (folder == null) {
			return false;
		}
		if (Files.isDirectory(folder, options)) {
			return true;
		}
		if (Files.exists(folder, options)) {
			return false;
		}

		// Guesstimate from file/folder name
		Path name = folder.getFileName();
		if (name == null) {
			// A zero-element path is neither, but for a relative path it would mean the current/working folder
			return true;
		}
		return name.toString().indexOf('.') < 0;
	}

	/**
	 * Evaluates whether the specified {@link Path} represents a file (as opposed to
	 * a folder).
	 * <p>
	 * This method uses a two-fold approach: If the path exists the the answer is
	 * acquired from the underlying file system. If the path doesn't exist, the name
	 * of the last element in the path is used to make a "best guess".
	 *
	 * @param file the {@link Path} to evaluate.
	 * @param options specify {@link LinkOption#NOFOLLOW_LINKS} to not follow links
	 *                while resolving {@code folder}.
	 * @return {@code true} if {@code file} is evaluated as being a file,
	 *         {@code false} otherwise.
	 */
	public static boolean isFile(@Nullable Path file, LinkOption... options) {
		if (file == null) {
			return false;
		}
		if (Files.isRegularFile(file, options)) {
			return true;
		}
		if (Files.exists(file, options)) {
			return false;
		}

		// Guesstimate from file/folder name
		Path name = file.getFileName();
		if (name == null) {
			return false;
		}
		return name.toString().indexOf('.') > -1;
	}

	/**
	 * A simple type holding mount point information for Unix file systems.
	 *
	 * @author Nadahar
	 */
	public static final class UnixMountPoint {
		public String device;
		public String folder;

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof UnixMountPoint)) {
				return false;
			}
			return
				this.device.equals(((UnixMountPoint) obj).device) &&
				this.folder.equals(((UnixMountPoint) obj).folder);
		}

		@Override
		public int hashCode() {
			return device.hashCode() + folder.hashCode();
		}

		@Override
		public String toString() {
			return String.format("Device: \"%s\", folder: \"%s\"", device, folder);
		}
	}

	public static boolean isUrl(String filename) {
		// We're intentionally avoiding stricter URI() methods, which can throw
		// URISyntaxException for psuedo-urls (e.g. librtmp-style urls containing spaces)
		return filename != null && filename.matches("\\S+://.*");
	}

	public static String getProtocol(String filename) {
		// Intentionally avoids URI.getScheme(), see above
		if (isUrl(filename)) {
			return filename.split("://")[0].toLowerCase(Locale.ROOT);
		}
		return null;
	}

	public static String urlJoin(String base, String filename) {
		if (isUrl(filename)) {
			return filename;
		}
		try {
			return new URL(new URL(base), filename).toString();
		} catch (MalformedURLException e) {
			return filename;
		}
	}

	public static String getUrlExtension(String u) {
		// Omit the query string, if any
		return getExtension(substringBefore(u, "?"));
	}

	public static String getUrlExtension(String u, @Nullable LetterCase convertTo, @Nullable Locale locale) {
		// Omit the query string, if any
		return getExtension(substringBefore(u, "?"), convertTo, locale);
	}

	/**
	 * Checks if the extension of the specified file matches one of the
	 * specified extensions.
	 *
	 * @param file the {@link Path} whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions the {@link Collection} of extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable Path file,
		boolean caseSensitive,
		@Nullable Collection<String> extensions
	) {
		if (file == null || extensions == null || extensions.isEmpty()) {
			return false;
		}
		Path fileName = file.getFileName();
		if (fileName == null || isBlank(fileName.toString())) {
			return false;
		}
		return isExtension(file.toString(), caseSensitive, extensions.toArray(new String[extensions.size()]));
	}

	/**
	 * Checks if the extension of the specified file matches one of the
	 * specified extensions.
	 *
	 * @param file the {@link Path} whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions one or more extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable Path file,
		boolean caseSensitive,
		@Nullable String... extensions
	) {
		if (file == null || extensions == null || extensions.length == 0) {
			return false;
		}
		Path fileName = file.getFileName();
		if (fileName == null || isBlank(fileName.toString())) {
			return false;
		}
		return isExtension(fileName.toString(), caseSensitive, extensions);
	}

	/**
	 * Checks if the extension of the specified file matches one of the
	 * specified extensions.
	 *
	 * @param file the {@link File} whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions the {@link Collection} of extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable File file,
		boolean caseSensitive,
		@Nullable Collection<String> extensions
	) {
		if (file == null || file.getName() == null || extensions == null || extensions.isEmpty()) {
			return false;
		}
		return isExtension(file.getName(), caseSensitive, extensions.toArray(new String[extensions.size()]));
	}

	/**
	 * Checks if the extension of the specified file matches one of the
	 * specified extensions.
	 *
	 * @param file the {@link File} whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions one or more extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable File file,
		boolean caseSensitive,
		@Nullable String... extensions
	) {
		if (file == null || file.getName() == null || extensions == null || extensions.length == 0) {
			return false;
		}
		return isExtension(file.getName(), caseSensitive, extensions);
	}

	/**
	 * Checks if the extension of the specified filename matches one of the
	 * specified extensions.
	 *
	 * @param fileName the filename whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions the {@link Collection} of extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable String fileName,
		boolean caseSensitive,
		@Nullable Collection<String> extensions
	) {
		if (extensions == null || extensions.isEmpty()) {
			return false;
		}
		return isExtension(fileName, caseSensitive, extensions.toArray(new String[extensions.size()]));
	}

	/**
	 * Checks if the extension of the specified filename matches one of the
	 * specified extensions.
	 *
	 * @param fileName the filename whose extension to check.
	 * @param caseSensitive {@code true} for a case-sensitive comparison,
	 *            {@code false} for a case-insensitive comparison.
	 * @param extensions one or more extensions to match.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public static boolean isExtension(
		@Nullable String fileName,
		boolean caseSensitive,
		@Nullable String... extensions
	) {
		if (extensions == null || extensions.length == 0) {
			return false;
		}
		String extension = getExtension(fileName, caseSensitive ? null : LetterCase.LOWER, Locale.ROOT);
		if (extension == null) {
			return false;
		}
		for (String matchExtension : extensions) {
			if (matchExtension == null) {
				continue;
			}
			if (!caseSensitive) {
				matchExtension = matchExtension.toLowerCase(Locale.ROOT);
			}
			if (extension.equals(matchExtension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the file extension from the specified {@link File} or
	 * {@code null} if it has no extension.
	 *
	 * @param file the {@link File} from which to extract the extension.
	 * @return The extracted extension or {@code null}.
	 */
	@Nullable
	public static String getExtension(@Nullable File file) {
		return getExtension(file, null, null);
	}

	/**
	 * Returns the file extension from the specified {@link File} or
	 * {@code null} if it has no extension.
	 *
	 * @param file the {@link File} from which to extract the extension.
	 * @param convertTo if {@code null} makes no letter case change to the
	 *            returned {@link String}, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The extracted and potentially letter case converted extension or
	 *         {@code null}.
	 */
	@Nullable
	public static String getExtension(@Nullable File file, @Nullable LetterCase convertTo, @Nullable Locale locale) {
		if (file == null || file.getName() == null) {
			return null;
		}
		return getExtension(file.getName(), convertTo, locale);
	}

	/**
	 * Returns the file extension from the specified {@link Path} or
	 * {@code null} if it has no extension.
	 *
	 * @param path the {@link Path} from which to extract the extension.
	 * @return The extracted extension or {@code null}.
	 */
	@Nullable
	public static String getExtension(@Nullable Path path) {
		return getExtension(path, null, null);
	}

	/**
	 * Returns the file extension from the specified {@link Path} or
	 * {@code null} if it has no extension.
	 *
	 * @param path the {@link Path} from which to extract the extension.
	 * @param convertTo if {@code null} makes no letter case change to the
	 *            returned {@link String}, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The extracted and potentially letter case converted extension or
	 *         {@code null}.
	 */
	@Nullable
	public static String getExtension(@Nullable Path path, @Nullable LetterCase convertTo, @Nullable Locale locale) {
		if (path == null) {
			return null;
		}
		Path fileName = path.getFileName();
		if (fileName == null || isBlank(fileName.toString())) {
			return null;
		}
		return getExtension(fileName.toString(), convertTo, locale);
	}

	/**
	 * Returns the file extension from {@code fileName} or {@code null} if
	 * {@code fileName} has no extension.
	 *
	 * @param fileName the file name from which to extract the extension.
	 * @return The extracted extension or {@code null}.
	 */
	public static String getExtension(@Nullable String fileName) {
		return getExtension(fileName, null, null);
	}

	/**
	 * Returns the file extension from {@code fileName} or {@code null} if
	 * {@code fileName} has no extension.
	 *
	 * @param fileName the file name from which to extract the extension.
	 * @param convertTo if {@code null} makes no letter case change to the
	 *            returned {@link String}, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The extracted and potentially letter case converted extension or
	 *         {@code null}.
	 */
	@Nullable
	public static String getExtension(@Nullable String fileName, @Nullable LetterCase convertTo, @Nullable Locale locale) {
		if (isBlank(fileName)) {
			return null;
		}

		int point = fileName.lastIndexOf('.');
		if (point == -1) {
			return null;
		}
		if (convertTo != null && locale == null) {
			locale = Locale.ROOT;
		}

		String extension = fileName.substring(point + 1);
		if (convertTo == LetterCase.UPPER) {
			return extension.toUpperCase(locale);
		}
		if (convertTo == LetterCase.LOWER) {
			return extension.toLowerCase(locale);
		}
		return extension;
	}

	/**
	 * Returns the name of the specified {@link File} split into two parts: the
	 * name and the extension. Both parts can be {@code null}.
	 *
	 * @param file the {@link File} whose name to split.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(@Nullable File file) {
		return detachExtension(file, null, null);
	}

	/**
	 * Returns the name of the specified {@link File} split into two parts: the
	 * name and the extension. Both parts can be {@code null}.
	 *
	 * @param file the {@link File} whose name to split.
	 * @param convertExtensionTo if {@code null} makes no letter case change to
	 *            the returned extension, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(
		@Nullable File file,
		@Nullable LetterCase convertExtensionTo,
		@Nullable Locale locale
	) {
		String fileName;
		if (file == null || (fileName = file.getName()) == null) {
			return Pair.emptyPair();
		}
		return detachExtension(fileName, convertExtensionTo, locale);
	}

	/**
	 * Returns the name of the specified {@link Path} split into two parts: the
	 * name and the extension. Both parts can be {@code null}.
	 *
	 * @param path the {@link Path} whose name to split.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(@Nullable Path path) {
		return detachExtension(path, null, null);
	}

	/**
	 * Returns the name of the specified {@link Path} split into two parts: the
	 * name and the extension. Both parts can be {@code null}.
	 *
	 * @param path the {@link Path} whose name to split.
	 * @param convertExtensionTo if {@code null} makes no letter case change to
	 *            the returned extension, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(
		@Nullable Path path,
		@Nullable LetterCase convertExtensionTo,
		@Nullable Locale locale
	) {
		Path fileName;
		if (path == null || (fileName = path.getFileName()) == null) {
			return Pair.emptyPair();
		}
		return detachExtension(fileName.toString(), convertExtensionTo, locale);
	}

	/**
	 * Returns the specified file name split into two parts: the name and the
	 * extension. Both parts can be {@code null}.
	 *
	 * @param fileName the file name to split.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(@Nullable String fileName) {
		return detachExtension(fileName, null, null);
	}

	/**
	 * Returns the specified file name split into two parts: the name and the
	 * extension. Both parts can be {@code null}.
	 *
	 * @param fileName the file name to split.
	 * @param convertExtensionTo if {@code null} makes no letter case change to
	 *            the returned extension, otherwise converts the extracted
	 *            extension (if any) to the corresponding letter case.
	 * @param locale the {@link Locale} to use for letter case conversion.
	 *            Defaults to {@link Locale#ROOT} if {@code null}.
	 * @return The {@link Pair} of {@link String}s with the name and the
	 *         extension respectively. The {@link Pair} itself is never
	 *         {@code null}, but both of the contained {@link String}s might be.
	 */
	@Nonnull
	public static Pair<String, String> detachExtension(
		@Nullable String fileName,
		@Nullable LetterCase convertExtensionTo,
		@Nullable Locale locale
	) {
		if (isBlank(fileName)) {
			return Pair.emptyPair();
		}

		int point = fileName.lastIndexOf('.');
		if (point == -1) {
			return new Pair<>(fileName, null);
		}

		if (convertExtensionTo != null && locale == null) {
			locale = Locale.ROOT;
		}

		String name = fileName.substring(0, point);
		if (name.length() == 0) {
			name = null;
		}
		String extension = fileName.substring(point + 1);
		if (extension.length() == 0) {
			extension = null;
		} else if (convertExtensionTo == LetterCase.UPPER) {
			extension = extension.toUpperCase(locale);
		} else if (convertExtensionTo == LetterCase.LOWER) {
			extension = extension.toLowerCase(locale);
		}
		return new Pair<>(name, extension);
	}

	public static String getFileNameWithoutExtension(String f) {
		int point = f.lastIndexOf('.');

		if (point == -1) {
			return f;
		}

		return f.substring(0, point);
	}

	private static final class FormattedNameAndEdition {
		public String formattedName;
		public String edition;

		public FormattedNameAndEdition(String formattedName, String edition) {
			this.formattedName = formattedName;
			this.edition = edition;
		}
	}

	/**
	 * Remove and save edition information to be added later
	 */
	private static FormattedNameAndEdition removeAndSaveEditionToBeAddedLater(String formattedName) {
		String edition = null;
		Matcher m = COMMON_FILE_EDITIONS_PATTERN.matcher(formattedName);
		if (m.find()) {
			edition = m.group().replaceAll("\\.", " ");
			edition = "(" + WordUtils.capitalizeFully(edition) + ")";
			formattedName = formattedName.replaceAll(" - " + COMMON_FILE_EDITIONS, "");
			formattedName = formattedName.replaceAll(COMMON_FILE_EDITIONS, "");
		}
		return new FormattedNameAndEdition(formattedName, edition);
	}

	/**
	 * Capitalize the first letter of each word if the string contains no capital letters
	 */
	private static String convertFormattedNameToTitleCaseParts(String formattedName) {
		if (formattedName.equals(formattedName.toLowerCase())) {
			StringBuilder formattedNameBuilder = new StringBuilder();
			for (String part : formattedName.split(" - ")) {
				if (formattedNameBuilder.length() > 0) {
					formattedNameBuilder.append(" - ");
				}
				formattedNameBuilder.append(convertLowerCaseStringToTitleCase(part));
			}
			formattedName = formattedNameBuilder.toString();
		}
		return formattedName;
	}

	/**
	 * Capitalize the first letter of each word if the string contains no capital letters
	 */
	private static String convertFormattedNameToTitleCase(String formattedName) {
		if (formattedName.equals(formattedName.toLowerCase())) {
			formattedName = convertLowerCaseStringToTitleCase(formattedName);
		}
		return formattedName;
	}

	/**
	 * Remove group name from the beginning of the filename
	 */
	private static String removeGroupNameFromBeginning(String formattedName) {
		if (!"".equals(formattedName) && formattedName.startsWith("[")) {
			Pattern pattern = Pattern.compile("^\\[[^\\]]{0,20}\\][^\\w]*(\\w.*?)\\s*$");
			Matcher matcher = pattern.matcher(formattedName);
			if (matcher.find()) {
				formattedName = matcher.group(1);
			} else if (formattedName.endsWith("]")) {
				pattern = Pattern.compile("^\\[([^\\[\\]]+)\\]\\s*$");
				matcher = pattern.matcher(formattedName);
				if (matcher.find()) {
					formattedName = matcher.group(1);
				}
			}
		}

		return formattedName;
	}

	/**
	 * Remove stuff at the end of the filename like release group, quality, source, etc.
	 */
	private static String removeFilenameEndMetadata(String formattedName) {
		formattedName = formattedName.replaceAll(COMMON_FILE_ENDS_CASE_SENSITIVE, "");
		formattedName = formattedName.replaceAll("(?i)" + COMMON_FILE_ENDS, "");
		return formattedName;
	}

	/**
	 * Strings that only occur after all useful information.
	 * When we encounter one of these strings, the string and everything after
	 * them will be removed.
	 */
	private static final String COMMON_FILE_ENDS = "[\\s\\.]AC3.*|[\\s\\.]REPACK.*|[\\s\\.]480p.*|[\\s\\.]720p.*|[\\s\\.]m-720p.*|[\\s\\.]900p.*|[\\s\\.]1080p.*|[\\s\\.]2160p.*|[\\s\\.]WEB-DL.*|[\\s\\.]HDTV.*|[\\s\\.]DSR.*|[\\s\\.]PDTV.*|[\\s\\.]WS.*|[\\s\\.]HQ.*|[\\s\\.]DVDRip.*|[\\s\\.]TVRiP.*|[\\s\\.]BDRip.*|[\\s\\.]BRRip.*|[\\s\\.]WEBRip.*|[\\s\\.]BluRay.*|[\\s\\.]Blu-ray.*|[\\s\\.]SUBBED.*|[\\s\\.]x264.*|[\\s\\.]Dual[\\s\\.]Audio.*|[\\s\\.]HSBS.*|[\\s\\.]H-SBS.*|[\\s\\.]RERiP.*|[\\s\\.]DIRFIX.*|[\\s\\.]READNFO.*|[\\s\\.]60FPS.*";
	private static final String COMMON_FILE_ENDS_MATCH = ".*[\\s\\.]AC3.*|.*[\\s\\.]REPACK.*|.*[\\s\\.]480p.*|.*[\\s\\.]720p.*|.*[\\s\\.]m-720p.*|.*[\\s\\.]900p.*|.*[\\s\\.]1080p.*|.*[\\s\\.]2160p.*|.*[\\s\\.]WEB-DL.*|.*[\\s\\.]HDTV.*|.*[\\s\\.]DSR.*|.*[\\s\\.]PDTV.*|.*[\\s\\.]WS.*|.*[\\s\\.]HQ.*|.*[\\s\\.]DVDRip.*|.*[\\s\\.]TVRiP.*|.*[\\s\\.]BDRip.*|.*[\\s\\.]BRRip.*|.*[\\s\\.]WEBRip.*|.*[\\s\\.]BluRay.*|.*[\\s\\.]Blu-ray.*|.*[\\s\\.]SUBBED.*|.*[\\s\\.]x264.*|.*[\\s\\.]Dual[\\s\\.]Audio.*|.*[\\s\\.]HSBS.*|.*[\\s\\.]H-SBS.*|.*[\\s\\.]RERiP.*|.*[\\s\\.]DIRFIX.*|.*[\\s\\.]READNFO.*|.*[\\s\\.]60FPS.*";

	/**
	 * Same as above, but they are common words so we reduce the chances of a
	 * false-positive by being case-sensitive.
	 */
	private static final String COMMON_FILE_ENDS_CASE_SENSITIVE = "[\\s\\.]PROPER[\\s\\.].*|[\\s\\.]iNTERNAL[\\s\\.].*|[\\s\\.]LIMITED[\\s\\.].*|[\\s\\.]LiMiTED[\\s\\.].*|[\\s\\.]FESTiVAL[\\s\\.].*|[\\s\\.]NORDIC[\\s\\.].*|[\\s\\.]REAL[\\s\\.].*|[\\s\\.]SUBBED[\\s\\.].*|[\\s\\.]RETAIL[\\s\\.].*|[\\s\\.]EXTENDED[\\s\\.].*|[\\s\\.]NEWEDIT[\\s\\.].*|[\\s\\.]WEB[\\s\\.].*";

	/**
	 * Editions to be added to the end of the prettified name
	 */
	private static final String COMMON_FILE_EDITIONS = "(?i)(?!\\()(Special[\\s\\.]Edition|Unrated|Final[\\s\\.]Cut|Remastered|Extended[\\s\\.]Cut|IMAX[\\s\\.]Edition|Uncensored|Directors[\\s\\.]Cut|Uncut)(?!\\))";
	private static final Pattern COMMON_FILE_EDITIONS_PATTERN = Pattern.compile(COMMON_FILE_EDITIONS);

	public static String getFileNamePrettified(String f) {
		return getFileNamePrettified(f, null);
	}

	/**
	 * Returns the filename after being "prettified", which involves
	 * attempting to strip away certain things like information about the
	 * quality, resolution, codecs, release groups, fansubbers, etc.,
	 * replacing periods with spaces, and various other things to produce a
	 * more "pretty" and standardized filename.
	 *
	 * @param f The filename
	 * @param file The file to possibly be used by the InfoDb
	 *
	 * @return The prettified filename
	 */
	public static String getFileNamePrettified(String f, File file) {
		String fileNameWithoutExtension;
		String formattedName = "";
		String formattedNameTemp;
		String searchFormattedName;
		String edition = "";

		// These are false unless we recognize that we could use some info on the video from IMDb
		boolean isEpisodeToLookup  = false;
		boolean isTVSeriesToLookup = false;
		boolean isMovieToLookup    = false;
		boolean isMovieWithoutYear = false;

		// Remove file extension
		fileNameWithoutExtension = getFileNameWithoutExtension(f);
		formattedName = removeGroupNameFromBeginning(fileNameWithoutExtension);
		searchFormattedName = "";

		if (formattedName.matches(".*[sS]0\\d[eE]\\d\\d([eE]|-[eE])\\d\\d.*")) {
			// This matches scene and most p2p TV episodes within the first 9 seasons that are more than one episode
			isTVSeriesToLookup = true;

			// Rename the season/episode numbers. For example, "S01E01" changes to " - 101"
			// Then strip the end of the episode if it does not have the episode name in the title
			formattedName = formattedName.replaceAll("(?i)[\\s\\.]S0(\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)(" + COMMON_FILE_ENDS + ")", " - $1$2$3-$5$6");
			formattedName = formattedName.replaceAll("[\\s\\.]S0(\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)(" + COMMON_FILE_ENDS_CASE_SENSITIVE + ")", " - $1$2$3-$5$6");
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// If it matches this then it didn't match the previous one, which means there is probably an episode title in the filename
			formattedNameTemp = formattedName.replaceAll("(?i)[\\s\\.]S0(\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)[\\s\\.]", " - $1$2$3-$5$6 - ");
			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.equals(formattedNameTemp)) {
				isEpisodeToLookup = true;
			}

			formattedName = formattedNameTemp;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCaseParts(formattedName);
		} else if (formattedName.matches(".*[sS][1-9]\\d[eE]\\d\\d([eE]|-[eE])\\d\\d.*")) {
			// This matches scene and most p2p TV episodes after their first 9 seasons that are more than one episode
			isTVSeriesToLookup = true;

			// Rename the season/episode numbers. For example, "S11E01" changes to " - 1101"
			formattedName = formattedName.replaceAll("(?i)[\\s\\.]S([1-9]\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)(" + COMMON_FILE_ENDS + ")", " - $1$2$3-$5$6");
			formattedName = formattedName.replaceAll("[\\s\\.]S([1-9]\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)(" + COMMON_FILE_ENDS_CASE_SENSITIVE + ")", " - $1$2$3-$5$6");
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// If it matches this then it didn't match the previous one, which means there is probably an episode title in the filename
			formattedNameTemp = formattedName.replaceAll("(?i)[\\s\\.]S([1-9]\\d)E(\\d)(\\d)([eE]|-[eE])(\\d)(\\d)[\\s\\.]", " - $1$2$3-$5$6 - ");
			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.equals(formattedNameTemp)) {
				isEpisodeToLookup = true;
			}

			formattedName = formattedNameTemp;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCaseParts(formattedName);
		} else if (formattedName.matches(".*[sS]0\\d[eE]\\d\\d.*")) {
			// This matches scene and most p2p TV episodes within the first 9 seasons
			isTVSeriesToLookup = true;
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// Rename the season/episode numbers. For example, "S01E01" changes to " - 101"
			// Then strip the end of the episode if it does not have the episode name in the title
			formattedName = formattedName.replaceAll("(?i)[\\s\\.]S0(\\d)E(\\d)(\\d)(" + COMMON_FILE_ENDS + ")", " - $1$2$3");
			formattedName = formattedName.replaceAll("[\\s\\.]S0(\\d)E(\\d)(\\d)(" + COMMON_FILE_ENDS_CASE_SENSITIVE + ")", " - $1$2$3");

			// If it matches this then it didn't match the previous one, which means there is probably an episode title in the filename
			formattedNameTemp = formattedName.replaceAll("(?i)[\\s\\.]S0(\\d)E(\\d)(\\d)[\\s\\.]", " - $1$2$3 - ");
			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.equals(formattedNameTemp)) {
				isEpisodeToLookup = true;
			}

			formattedName = formattedNameTemp;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCaseParts(formattedName);
		} else if (formattedName.matches(".*[sS][1-9]\\d[eE]\\d\\d.*")) {
			// This matches scene and most p2p TV episodes after their first 9 seasons
			isTVSeriesToLookup = true;

			// Rename the season/episode numbers. For example, "S11E01" changes to " - 1101"
			formattedName = formattedName.replaceAll("(?i)[\\s\\.]S([1-9]\\d)E(\\d)(\\d)(" + COMMON_FILE_ENDS + ")", " - $1$2$3");
			formattedName = formattedName.replaceAll("[\\s\\.]S([1-9]\\d)E(\\d)(\\d)(" + COMMON_FILE_ENDS_CASE_SENSITIVE + ")", " - $1$2$3");
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// If it matches this then it didn't match the previous one, which means there is probably an episode title in the filename
			formattedNameTemp = formattedName.replaceAll("(?i)[\\s\\.]S([1-9]\\d)E(\\d)(\\d)[\\s\\.]", " - $1$2$3 - ");
			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.equals(formattedNameTemp)) {
				isEpisodeToLookup = true;
			}

			formattedName = formattedNameTemp;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCaseParts(formattedName);
		} else if (formattedName.matches(".*[\\s\\.](19|20)\\d\\d[\\s\\.][0-1]\\d[\\s\\.][0-3]\\d[\\s\\.].*")) {
			// This matches scene and most p2p TV episodes that release several times per week
			isTVSeriesToLookup = true;

			// Rename the date. For example, "2013.03.18" changes to " - 2013/03/18"
			formattedName = formattedName.replaceAll("(?i)[\\s\\.](19|20)(\\d\\d)[\\s\\.]([0-1]\\d)[\\s\\.]([0-3]\\d)(" + COMMON_FILE_ENDS + ")", " - $1$2/$3/$4");
			formattedName = formattedName.replaceAll("[\\s\\.](19|20)(\\d\\d)[\\s\\.]([0-1]\\d)[\\s\\.]([0-3]\\d)(" + COMMON_FILE_ENDS_CASE_SENSITIVE + ")", " - $1$2/$3/$4");
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// If it matches this then it didn't match the previous one, which means there is probably an episode title in the filename
			formattedNameTemp = formattedName.replaceAll("(?i)[\\s\\.](19|20)(\\d\\d)[\\s\\.]([0-1]\\d)[\\s\\.]([0-3]\\d)[\\s\\.]", " - $1$2/$3/$4 - ");
			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.equals(formattedNameTemp)) {
				isEpisodeToLookup = true;
			}

			formattedName = formattedNameTemp;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCaseParts(formattedName);
		} else if (formattedName.matches(".*[\\s\\.](19|20)\\d\\d[\\s\\.].*")) {
			// This matches scene and most p2p movies
			isMovieToLookup = true;

			// Rename the year. For example, "2013" changes to " (2013)"
			formattedName = formattedName.replaceAll("[\\s\\.](19|20)(\\d\\d)", " ($1$2)");
			formattedName = removeFilenameEndMetadata(formattedName);
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCase(formattedName);
		} else if (formattedName.matches(".*\\[(19|20)\\d\\d\\].*")) {
			// This matches rarer types of movies
			isMovieToLookup = true;

			// Rename the year. For example, "2013" changes to " (2013)"
			formattedName = formattedName.replaceAll("(?i)\\[(19|20)(\\d\\d)\\].*", " ($1$2)");
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCase(formattedName);
		} else if (formattedName.matches(".*\\((19|20)\\d\\d\\).*")) {
			// This matches rarer types of movies
			isMovieToLookup = true;
			formattedName = removeFilenameEndMetadata(formattedName);

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCase(formattedName);
		} else if (formattedName.matches(".*\\[[0-9a-zA-Z]{8}\\]$")) {
			// This matches anime with a hash at the end of the name
			isTVSeriesToLookup = true;

			// Remove underscores
			formattedName = formattedName.replaceAll("_", " ");

			// Remove stuff at the end of the filename like hash, quality, source, etc.
			formattedName = formattedName.replaceAll("(?i)\\s\\(1280x720.*|\\s\\(1920x1080.*|\\s\\(720x400.*|\\[720p.*|\\[1080p.*|\\[480p.*|\\s\\(BD.*|\\s\\[Blu-Ray.*|\\s\\[DVD.*|\\.DVD.*|\\[[0-9a-zA-Z]{8}\\]$|\\[h264.*|R1DVD.*|\\[BD.*", "");

			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.substring(formattedName.length() - 3).matches("[\\s\\._]\\d\\d")) {
				isEpisodeToLookup = true;
				searchFormattedName = formattedName.substring(0, formattedName.length() - 2) + "S01E" + formattedName.substring(formattedName.length() - 2);
			}

			formattedName = convertFormattedNameToTitleCase(formattedName);
		} else if (formattedName.matches(".*\\[BD\\].*|.*\\[720p\\].*|.*\\[1080p\\].*|.*\\[480p\\].*|.*\\[Blu-Ray.*|.*\\[h264.*")) {
			// This matches anime without a hash in the name
			isTVSeriesToLookup = true;

			// Remove underscores
			formattedName = formattedName.replaceAll("_", " ");

			// Remove stuff at the end of the filename like hash, quality, source, etc.
			formattedName = formattedName.replaceAll("(?i)\\[BD\\].*|\\[720p.*|\\[1080p.*|\\[480p.*|\\[Blu-Ray.*|\\[h264.*", "");

			if (PMS.getConfiguration().isUseInfoFromIMDb() && formattedName.substring(formattedName.length() - 3).matches("[\\s\\._]\\d\\d")) {
				isEpisodeToLookup = true;
				searchFormattedName = formattedName.substring(0, formattedName.length() - 2) + "S01E" + formattedName.substring(formattedName.length() - 2);
			}

			formattedName = convertFormattedNameToTitleCase(formattedName);
		} else if (formattedName.matches(COMMON_FILE_ENDS_MATCH)) {
			// This is probably a movie that doesn't specify a year
			isMovieToLookup = true;
			isMovieWithoutYear = true;
			formattedName = removeFilenameEndMetadata(formattedName);
			FormattedNameAndEdition result = removeAndSaveEditionToBeAddedLater(formattedName);
			formattedName = result.formattedName;
			if (result.edition != null) {
				edition = result.edition;
			}

			// Replace periods with spaces
			formattedName = formattedName.replaceAll("\\.", " ");

			formattedName = convertFormattedNameToTitleCase(formattedName);
		}

		// Remove extra spaces
		formattedName = formattedName.replaceAll("\\s+", " ");

		/*
		 * Add info from IMDb
		 *
		 * We use the Jaro Winkler similarity algorithm to make sure that changes to
		 * movie or TV show names are only made when the difference between the
		 * original and replacement names is less than 10%.
		 * This means we get proper case and special characters without worrying about
		 * incorrect results being used.
		 *
		 * TODO: Make the following logic only happen once.
		 */
		JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
		if (file != null && (isTVSeriesToLookup || isMovieToLookup)) {
			InfoDb.InfoDbData info = PMS.get().infoDb().get(file);
			if (info == null) {
				PMS.get().infoDbAdd(file, isNotBlank(searchFormattedName) ?  searchFormattedName : formattedName);
			} else if (isTVSeriesToLookup) {
				int showNameIndex = indexOf(Pattern.compile("(?i) - \\d\\d\\d.*"), formattedName);
				if (isNotEmpty(info.title) && showNameIndex != -1) {
					String titleFromFilename = formattedName.substring(0, showNameIndex);

					// The following line can run over 100 times in under 1ms
					double similarity = jaroWinklerDistance.apply(titleFromFilename, info.title);
					if (similarity > 0.91) {
						formattedName = info.title + formattedName.substring(showNameIndex);

						if (isEpisodeToLookup && isNotEmpty(info.ep_name)) {
							formattedName += " - " + info.ep_name;
						}
					}
					LOGGER.trace("The similarity between '" + info.title + "' and '" + titleFromFilename + "' is " + similarity);
				}
			} else if (isMovieToLookup && isNotEmpty(info.title) && isNotEmpty(info.year)) {
				double similarity;
				if (isMovieWithoutYear) {
					similarity = jaroWinklerDistance.apply(formattedName, info.title);
					LOGGER.trace("The similarity between '" + info.title + "' and '" + formattedName + "' is " + similarity);
				} else {
					int yearIndex = indexOf(Pattern.compile("\\s\\(\\d{4}\\)"), formattedName);
					String titleFromFilename = formattedName.substring(0, yearIndex);
					similarity = jaroWinklerDistance.apply(titleFromFilename, info.title);
					LOGGER.trace("The similarity between '" + info.title + "' and '" + titleFromFilename + "' is " + similarity);
				}

				if (similarity > 0.91) {
					formattedName = info.title + " (" + info.year + ")";
				}
			}
		}
		formattedName = formattedName.trim();

		// Add the edition information if it exists
		if (!edition.isEmpty()) {
			String substr = formattedName.substring(Math.max(0, formattedName.length() - 2));
			if (" -".equals(substr)) {
				formattedName = formattedName.substring(0, formattedName.length() - 2);
			}
			formattedName += " " + edition;
		}

		return formattedName;
	}

	/**
	 * Converts a lower case string to title case.
	 *
	 * It is not very smart right now so it can be expanded to be more reliable.
	 *
	 * @param value the string to convert
	 *
	 * @return the converted string
	 */
	public static String convertLowerCaseStringToTitleCase(String value) {
		String convertedValue = "";
		boolean loopedOnce = false;

		for (String word : value.split(" ")) {
			if (loopedOnce) {
				switch (word) {
					case "a":
					case "an":
					case "and":
					case "in":
					case "it":
					case "for":
					case "of":
					case "on":
					case "the":
					case "to":
					case "vs":
						convertedValue += ' ' + word;
						break;
					default:
						convertedValue += ' ' + word.substring(0, 1).toUpperCase() + word.substring(1);
						break;
				}
			} else {
				// Always capitalize the first letter of the string
				convertedValue += word.substring(0, 1).toUpperCase() + word.substring(1);
			}
			loopedOnce = true;
		}

		return convertedValue;
	}

	public static int indexOf(Pattern pattern, String s) {
		Matcher matcher = pattern.matcher(s);
		return matcher.find() ? matcher.start() : -1;
	}

	public static File getFileNameWithAddedExtension(File parent, File f, String ext) {
		File ff = new File(parent, f.getName() + ext);

		if (ff.exists()) {
			return ff;
		}

		return null;
	}

	/**
	 * Returns a new {@link File} instance where the file extension has been
	 * replaced.
	 *
	 * @param file the {@link File} for which to replace the extension.
	 * @param extension the new file extension.
	 * @param nullIfNonExisting whether or not to return {@code null} or a
	 *            non-existing {@link File} instance if the constructed
	 *            {@link File} doesn't exist.
	 * @param adjustExtensionCase whether or not to try upper- and lower-case
	 *            variants of the extension. If {@code true} and the constructed
	 *            {@link File} doesn't exist with the given case but does exist
	 *            with an either upper or lower case version of the extension,
	 *            the existing {@link File} instance will be returned.
	 * @return The constructed {@link File} instance or {@code null} if the
	 *         target file doesn't exist and {@code nullIfNonExisting} is true.
	 */
	@Nullable
	public static File replaceExtension(
		@Nullable File file,
		@Nullable String extension,
		boolean nullIfNonExisting,
		boolean adjustExtensionCase
	) {
		if (file == null) {
			return null;
		}
		return replaceExtension(
			file.getParentFile(),
			file.getName(),
			extension,
			nullIfNonExisting,
			adjustExtensionCase
		);
	}

	/**
	 * Returns a new {@link File} instance where the file extension has been
	 * replaced.
	 *
	 * @param folder the {@link File} instance representing the folder for the
	 *            constructed {@link File}. Use {@code null} or an empty string
	 *            for the current folder.
	 * @param file the {@link File} for which to replace the extension. Only the
	 *            file name will be used, its path will be discarded.
	 * @param extension the new file extension.
	 * @param nullIfNonExisting whether or not to return {@code null} or a
	 *            non-existing {@link File} instance if the constructed
	 *            {@link File} doesn't exist.
	 * @param adjustExtensionCase whether or not to try upper- and lower-case
	 *            variants of the extension. If {@code true} and the constructed
	 *            {@link File} doesn't exist with the given case but does exist
	 *            with an either upper or lower case version of the extension,
	 *            the existing {@link File} instance will be returned.
	 * @return The constructed {@link File} instance or {@code null} if the
	 *         target file doesn't exist and {@code nullIfNonExisting} is true.
	 */
	@Nullable
	public static File replaceExtension(
		@Nullable File folder,
		@Nullable File file,
		@Nullable String extension,
		boolean nullIfNonExisting,
		boolean adjustExtensionCase
	) {
		if (file == null) {
			return null;
		}
		return replaceExtension(
			folder,
			file.getName(),
			extension,
			nullIfNonExisting,
			adjustExtensionCase
		);
	}

	/**
	 * Returns a new {@link File} instance where the file extension has been
	 * replaced.
	 *
	 * @param folder the {@link File} instance representing the folder for the
	 *            constructed {@link File}. Use {@code null} or an empty string
	 *            for the current folder.
	 * @param fileName the {@link String} for which to replace the extension.
	 * @param extension the new file extension.
	 * @param nullIfNonExisting whether or not to return {@code null} or a
	 *            non-existing {@link File} instance if the constructed
	 *            {@link File} doesn't exist.
	 * @param adjustExtensionCase whether or not to try upper- and lower-case
	 *            variants of the extension. If {@code true} and the constructed
	 *            {@link File} doesn't exist with the given case but does exist
	 *            with an either upper or lower case version of the extension,
	 *            the existing {@link File} instance will be returned.
	 * @return The constructed {@link File} instance or {@code null} if the
	 *         target file doesn't exist and {@code nullIfNonExisting} is true.
	 */
	@Nullable
	public static File replaceExtension(
		@Nullable File folder,
		@Nullable String fileName,
		@Nullable String extension,
		boolean nullIfNonExisting,
		boolean adjustExtensionCase
	) {
		if (isBlank(fileName)) {
			return null;
		}

		int point = fileName.lastIndexOf('.');

		String baseFileName;
		if (point == -1) {
			baseFileName = fileName;
		} else {
			baseFileName = fileName.substring(0, point);
		}

		if (isBlank(extension)) {
			File result = new File(folder, baseFileName);
			return !nullIfNonExisting || result.exists() ? result : null;
		}

		File result = new File(folder, baseFileName + "." + extension);
		if (result.exists() || !nullIfNonExisting && !adjustExtensionCase) {
			return result;
		}

		if (!Platform.isWindows() && adjustExtensionCase) {
			File adjustedResult = new File(folder, baseFileName + "." + extension.toLowerCase(Locale.ROOT));
			if (adjustedResult.exists()) {
				return adjustedResult;
			}
			adjustedResult = new File(folder, baseFileName + "." + extension.toUpperCase(Locale.ROOT));
			if (adjustedResult.exists()) {
				return adjustedResult;
			}
		}

		return nullIfNonExisting ? null : result;
	}

	/**
	 * Returns the specified path if it exists, otherwise {@code null} is
	 * returned.
	 *
	 * @param path the path to evaluate.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return The corresponding {@link Path} or {@code null} if {@code path}
	 *         doesn't exist.
	 */
	@Nullable
	public static Path existsOrNull(@Nullable String path, @Nullable LinkOption... options) {
		if (path == null) {
			return null;
		}
		try {
			Path asPath = Paths.get(path);
			return Files.exists(asPath, options) ? asPath : null;
		} catch (InvalidPathException e) {
			return null;
		}
	}

	/**
	 * Returns the specified path if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param path the path to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return The corresponding {@link Path} or {@code null} if {@code path}
	 *         doesn't exist or doesn't satisfy {@code requireFlags}.
	 */
	@Nullable
	public static Path existsOrNull(@Nullable String path, @Nullable FileFlag... requireFlags) {
		if (path == null) {
			return null;
		}
		try {
			Path asPath = Paths.get(path);
			return exists(
				asPath,
				requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
			) ? asPath : null;
		} catch (InvalidPathException e) {
			return null;
		}
	}

	/**
	 * Returns the specified path if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param path the path to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return The corresponding {@link Path} or {@code null} if {@code path}
	 *         doesn't exist or doesn't satisfy {@code requireFlags}.
	 */
	@Nullable
	public static Path existsOrNull(
		@Nullable String path,
		@Nullable Set<FileFlag> requireFlags,
		@Nullable LinkOption... options
	) {
		if (path == null) {
			return null;
		}
		try {
			Path asPath = Paths.get(path);
			return exists(asPath, requireFlags, options) ? asPath : null;
		} catch (InvalidPathException e) {
			return null;
		}
	}

	/**
	 * Tests whether the specified path exists.
	 *
	 * @param path the path to evaluate.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return {@code true} if {@code path} exists, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable String path, @Nullable LinkOption... options) {
		if (path == null) {
			return false;
		}
		try {
			return Files.exists(Paths.get(path), options);
		} catch (InvalidPathException e) {
			return false;
		}
	}

	/**
	 * Tests whether the specified path exists and satisfies the specified
	 * {@link FileFlag}s.
	 *
	 * @param path the path to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return {@code true} if {@code path} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable String path, @Nullable FileFlag... requireFlags) {
		if (path == null) {
			return false;
		}
		try {
			return exists(
				Paths.get(path),
				requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
			);
		} catch (InvalidPathException e) {
			return false;
		}
	}

	/**
	 * Tests whether the specified path exists and satisfies the specified
	 * {@link FileFlag}s.
	 *
	 * @param path the path to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return {@code true} if {@code path} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(
		@Nullable String path,
		@Nullable Set<FileFlag> requireFlags,
		@Nullable LinkOption... options
	) {
		if (path == null) {
			return false;
		}
		try {
			return exists(Paths.get(path), requireFlags, options);
		} catch (InvalidPathException e) {
			return false;
		}
	}

	/**
	 * Returns the specified {@link File} if it exists, otherwise {@code null}
	 * is returned.
	 *
	 * @param file the {@link File} to evaluate.
	 * @return The {@link File} or {@code null} if it doesn't exist.
	 */
	@Nullable
	public static File existsOrNull(@Nullable File file) {
		return exists(file) ? file : null;
	}

	/**
	 * Returns the specified {@link File} if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param file the {@link File} to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return The {@link File} or {@code null} if it doesn't exist or doesn't
	 *         satisfy {@code requireFlags}.
	 */
	@Nullable
	public static File existsOrNull(@Nullable File file, @Nullable FileFlag... requireFlags) {
		return exists(
			file,
			requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
		) ? file : null;
	}

	/**
	 * Returns the specified {@link File} if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param file the {@link File} to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @return The {@link File} or {@code null} if it doesn't exist or doesn't
	 *         satisfy {@code requireFlags}.
	 */
	@Nullable
	public static File existsOrNull(@Nullable File file, @Nullable Set<FileFlag> requireFlags) {
		return exists(file, requireFlags) ? file : null;
	}

	/**
	 * Tests whether the specified {@link File} exists, this is a simple forward
	 * to {@link File#exists()}.
	 *
	 * @param file the {@link File} to evaluate.
	 * @return {@code true} if {@code file} exists, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable File file) {
		return file == null ? false : file.exists();
	}

	/**
	 * Tests whether the specified {@link File} exists and satisfies the
	 * specified {@link FileFlag}s.
	 *
	 * @param file the {@link File} to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return {@code true} if {@code file} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable File file, @Nullable FileFlag... requireFlags) {
		return exists(
			file,
			requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
		);
	}

	/**
	 * Tests whether the specified {@link File} exists and satisfies the
	 * specified {@link FileFlag}s.
	 *
	 * @param file the {@link File} to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @return {@code true} if {@code file} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable File file, @Nullable Set<FileFlag> requireFlags) {
		if (file == null) {
			return false;
		}
		if (requireFlags != null && !requireFlags.isEmpty()) {
			try {
				return new FilePermissions(file).hasFlags(requireFlags);
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return file.exists();
	}

	/**
	 * Returns the specified {@link Path} if it exists, otherwise {@code null}
	 * is returned.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @return The {@link Path} or {@code null} if it doesn't exist.
	 */
	@Nullable
	public static Path existsOrNull(@Nullable Path path) {
		return path == null ? null : Files.exists(path) ? path : null;
	}

	/**
	 * Returns the specified {@link Path} if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return The {@link Path} or {@code null} if it doesn't exist or doesn't
	 *         satisfy {@code requireFlags}.
	 */
	@Nullable
	public static Path existsOrNull(@Nullable Path path, @Nullable FileFlag... requireFlags) {
		return exists(
			path,
			requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
		) ? path : null;
	}

	/**
	 * Returns the specified {@link Path} if it exists and satisfies the
	 * specified {@link FileFlag}s, otherwise {@code null} is returned.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return The {@link Path} or {@code null} if it doesn't exist or doesn't
	 *         satisfy {@code requireFlags}.
	 */
	@Nullable
	public static Path existsOrNull(
		@Nullable Path path,
		@Nullable Set<FileFlag> requireFlags,
		@Nullable LinkOption... options
	) {
		return exists(path, requireFlags, options) ? path : null;
	}

	/**
	 * Tests whether the specified {@link Path} exists, this is a simple forward
	 * to {@link Files#exists(Path, LinkOption...)}.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return {@code true} if {@code path} exists, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable Path path, @Nullable LinkOption... options) {
		return path == null ? false : Files.exists(path, options);
	}

	/**
	 * Tests whether the specified {@link Path} exists and satisfies the
	 * specified {@link FileFlag}s.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @param requireFlags the {@link FileFlag}s that must be satisfied or none
	 *            to do a simple existence check.
	 * @return {@code true} if {@code path} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(@Nullable Path path, @Nullable FileFlag... requireFlags) {
		return exists(
			path,
			requireFlags == null ? EnumSet.noneOf(FileFlag.class) : EnumSet.copyOf(Arrays.asList(requireFlags))
		);
	}

	/**
	 * Tests whether the specified {@link Path} exists and satisfies the
	 * specified {@link FileFlag}s.
	 *
	 * @param path the {@link Path} to evaluate.
	 * @param requireFlags the {@link Set} of {@link FileFlag}s that must be
	 *            satisfied or {@code null} to do a simple existence check.
	 * @param options the {@link LinkOption}s to use when resolving.
	 * @return {@code true} if {@code path} exists and satisfies
	 *         {@code requireFlags}, {@code false} otherwise.
	 */
	public static boolean exists(
		@Nullable Path path,
		@Nullable Set<FileFlag> requireFlags,
		@Nullable LinkOption... options
	) {
		if (path == null) {
			return false;
		}
		if (requireFlags != null && !requireFlags.isEmpty()) {
			try {
				return new FilePermissions(path, options).hasFlags(requireFlags);
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return Files.exists(path, options);
	}

	/**
	 * Adds the specified {@link Path} to the specified {@link Collection} if
	 * both are non-{@code null}.
	 *
	 * @param collection the {@link Collection} to add {@code path} to.
	 * @param path the {@link Path} to add.
	 */
	public static void addPathIfNotNull(@Nullable Collection<Path> collection, @Nullable Path path) {
		if (collection != null && path != null) {
			collection.add(path);
		}
	}

	/**
	 * Enumerates and returns a {@link List} of the specified folder and all
	 * subfolders where the current process has browse permission.
	 *
	 * @param startFolder the folder in which to start the enumeration.
	 * @param followLinks if {@code true} links will be followed, if
	 *            {@code false} links won't be followed. Be aware that if links
	 *            are followed and a loop is encountered, a
	 *            {@link FileSystemLoopException} will be logged and the
	 *            returned {@link List} might be incomplete.
	 * @return The {@link List} of the enumerated, browsable folders.
	 */
	@Nonnull
	public static List<Path> findFoldersRecursively(@Nonnull Path startFolder, boolean followLinks) {
		final List<Path> result = new ArrayList<>();
		try {
			if (
				startFolder == null ||
				!new FilePermissions(startFolder).hasFlags(FileFlag.FOLDER, FileFlag.BROWSE)
			) {
				return result;
			}
		} catch (FileNotFoundException e) {
			return result;
		}
		try {
			Files.walkFileTree(
				startFolder,
				followLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class),
				Integer.MAX_VALUE,
				new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						addPathIfNotNull(
							result,
							existsOrNull(dir, EnumSet.of(FileFlag.FOLDER, FileFlag.BROWSE))
						);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				}
			);
		} catch (IOException e) {
			LOGGER.warn(
				"An error occurred while trying enumerate subfolders of \"{}\": {}",
				startFolder,
				e.getMessage()
			);
			LOGGER.trace("", e);
		}
		return result;
	}

	/**
	 * @deprecated Use {@link #isSubtitlesExists(File file, DLNAMediaInfo media)} instead.
	 */
	@Deprecated
	public static boolean doesSubtitlesExists(File file, DLNAMediaInfo media) {
		return isSubtitlesExists(file, media);
	}

	public static boolean isSubtitlesExists(File file, DLNAMediaInfo media) {
		return isSubtitlesExists(file, media, true);
	}

	/**
	 * @deprecated Use {@link #isSubtitlesExists(File file, DLNAMediaInfo media, boolean usecache)} instead.
	 */
	@Deprecated
	public static boolean doesSubtitlesExists(File file, DLNAMediaInfo media, boolean usecache) {
		return isSubtitlesExists(file, media, usecache);
	}

	public static boolean isSubtitlesExists(File file, DLNAMediaInfo media, boolean usecache) {
		if (media != null && media.isExternalSubsParsed()) {
			return media.isExternalSubsExist();
		}

		boolean found = false;
		if (file.exists()) {
			found = browseFolderForSubtitles(file.getAbsoluteFile().getParentFile(), file, media, usecache);
		}
		String alternate = PMS.getConfiguration().getAlternateSubtitlesFolder();

		if (isNotBlank(alternate)) { // https://code.google.com/p/ps3mediaserver/issues/detail?id=737#c5
			File subFolder = new File(alternate);

			if (!subFolder.isAbsolute()) {
				subFolder = new File(file.getParent(), alternate);
				try {
					subFolder = subFolder.getCanonicalFile();
				} catch (IOException e) {
					LOGGER.warn("Could not resolve alternative subtitles folder: {}", e.getMessage());
					LOGGER.trace("", e);
				}
			}

			if (subFolder.exists()) {
				found = browseFolderForSubtitles(subFolder, file, media, usecache) || found;
			}
		}

		if (media != null) {
			media.setExternalSubsExist(found);
			media.setExternalSubsParsed(true);
		}

		return found;
	}

	private static boolean browseFolderForSubtitles(File subFolder, File file, DLNAMediaInfo media, final boolean useCache) {
		boolean found = false;
		final Set<String> supported = SubtitleType.getSupportedFileExtensions();

		File[] allSubs = null;
		// TODO This caching scheme is very restrictive locking the whole cache
		// while populating a single folder. A more effective solution should
		// be implemented.
		subtitleCacheLock.lock();
		try {
			if (useCache) {
				allSubs = subtitleCache.get(subFolder);
			}

			if (allSubs == null) {
				allSubs = subFolder.listFiles(
					new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							String ext = FilenameUtils.getExtension(name).toLowerCase();
							if ("sub".equals(ext)) {
								// Avoid microdvd/vobsub confusion by ignoring sub+idx pairs here since
								// they'll come in unambiguously as vobsub via the idx file anyway
								return replaceExtension(new File(dir, name), "idx", true, true) == null;
							}
							return supported.contains(ext);
						}
					}
				);

				if (allSubs != null) {
					subtitleCache.put(subFolder, allSubs);
				}
			}
		} finally {
			subtitleCacheLock.unlock();
		}

		String fileName = getFileNameWithoutExtension(file.getName()).toLowerCase();
		if (allSubs != null) {
			for (File f : allSubs) {
				if (f.isFile() && !f.isHidden()) {
					String fName = f.getName().toLowerCase();
					for (String ext : supported) {
						if (fName.length() > ext.length() && fName.startsWith(fileName) && endsWithIgnoreCase(fName, "." + ext)) {
							int a = fileName.length();
							int b = fName.length() - ext.length() - 1;
							String code = "";

							if (a <= b) { // handling case with several dots: <video>..<extension>
								code = fName.substring(a, b);
							}

							if (code.startsWith(".")) {
								code = code.substring(1);
							}

							boolean exists = false;
							if (media != null) {
								for (DLNAMediaSubtitle sub : media.getSubtitleTracksList()) {
									if (f.equals(sub.getExternalFile())) {
										exists = true;
									} else if (equalsIgnoreCase(ext, "idx") && sub.getType() == SubtitleType.MICRODVD) { // sub+idx => VOBSUB
										sub.setType(SubtitleType.VOBSUB);
										exists = true;
									} else if (equalsIgnoreCase(ext, "sub") && sub.getType() == SubtitleType.VOBSUB) { // VOBSUB
										try {
											sub.setExternalFile(f, null);
										} catch (FileNotFoundException ex) {
											LOGGER.warn("File not found during external subtitles scan: {}", ex.getMessage());
											LOGGER.trace("", ex);
										}

										exists = true;
									}
								}
							}

							if (!exists) {
								String forcedLang = null;
								DLNAMediaSubtitle sub = new DLNAMediaSubtitle();
								sub.setId(100 + (media == null ? 0 : media.getSubtitleTracksList().size())); // fake id, not used
								if (code.length() == 0 || !Iso639.codeIsValid(code)) {
									sub.setLang(DLNAMediaSubtitle.UND);
									sub.setType(SubtitleType.valueOfFileExtension(ext));
									if (code.length() > 0) {
										sub.setSubtitlesTrackTitleFromMetadata(code);
										if (sub.getSubtitlesTrackTitleFromMetadata().contains("-")) {
											String flavorLang = sub.getSubtitlesTrackTitleFromMetadata().substring(0, sub.getSubtitlesTrackTitleFromMetadata().indexOf('-'));
											String flavorTitle = sub.getSubtitlesTrackTitleFromMetadata().substring(sub.getSubtitlesTrackTitleFromMetadata().indexOf('-') + 1);
											if (Iso639.codeIsValid(flavorLang)) {
												sub.setLang(flavorLang);
												sub.setSubtitlesTrackTitleFromMetadata(flavorTitle);
												forcedLang = flavorLang;
											}
										}
									}
								} else {
									sub.setLang(code);
									sub.setType(SubtitleType.valueOfFileExtension(ext));
									forcedLang = code;
								}

								try {
									sub.setExternalFile(f, forcedLang);
								} catch (FileNotFoundException ex) {
									LOGGER.warn("File not found during external subtitles scan: {}", ex.getMessage());
									LOGGER.trace("", ex);
								}

								found = true;
								if (media != null) {
									media.getSubtitleTracksList().add(sub);
								}
							}
						}
					}
				}
			}
		}

		return found;
	}

	/**
	 * Detects charset/encoding for given file. Not 100% accurate for
	 * non-Unicode files.
	 *
	 * @param file the file for which to detect charset/encoding
	 * @return The match object form the detection process or <code>null</code> if no match was found
	 * @throws IOException
	 */
	@Nonnull
	public static CharsetMatch getFileCharsetMatch(@Nonnull File file) throws IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			CharsetDetector detector = new CharsetDetector();
			detector.setText(in);
			// Results are sorted on descending confidence, so we're only after the first one.
			return detector.detectAll()[0];
		}
	}

	/**
	 * Detects charset/encoding for given file. Not 100% accurate for
	 * non-Unicode files.
	 *
	 * @param file the file for which to detect charset/encoding.
	 * @return The detected {@link Charset} or {@code null} if not detected.
	 * @throws IOException If an IO error occurs during the operation.
	 */
	@Nullable
	public static Charset getFileCharset(@Nullable File file) throws IOException {
		if (file == null) {
			return null;
		}
		CharsetMatch match = getFileCharsetMatch(file);
		try {
			if (Charset.isSupported(match.getName())) {
				LOGGER.debug("Detected charset \"{}\" in file \"{}\"", match.getName(), file.getAbsolutePath());
				return Charset.forName(match.getName());
			}
			LOGGER.debug(
				"Detected charset \"{}\" in file \"{}\", but cannot use it because it's not supported by the Java Virual Machine",
				match.getName(),
				file.getAbsolutePath()
			);
			return null;
		} catch (IllegalCharsetNameException e) {
			LOGGER.debug("Illegal charset \"{}\" deteceted in file \"{}\"", match.getName(), file.getAbsolutePath());
		}
		LOGGER.debug("Found no matching charset for file \"{}\"", file.getAbsolutePath());
		return null;
	}

	/**
	 * Detects charset/encoding for given file. Not 100% accurate for
	 * non-Unicode files.
	 *
	 * @param file the file for which to detect charset/encoding.
	 * @return The name of the detected charset or {@code null} if not detected.
	 * @throws IOException If an IO error occurs during the operation.
	 */
	@Nullable
	public static String getFileCharsetName(@Nullable File file) throws IOException {
		if (file == null) {
			return null;
		}
		CharsetMatch match = getFileCharsetMatch(file);
		try {
			if (Charset.isSupported(match.getName())) {
				LOGGER.debug("Detected charset \"{}\" in file \"{}\"", match.getName(), file.getAbsolutePath());
				return match.getName().toUpperCase(Locale.ROOT);
			}
			LOGGER.debug(
				"Detected charset \"{}\" in file \"{}\", but cannot use it because it's not supported by the Java Virual Machine",
				match.getName(),
				file.getAbsolutePath()
			);
			return null;
		} catch (IllegalCharsetNameException e) {
			LOGGER.debug("Illegal charset \"{}\" deteceted in file \"{}\"", match.getName(), file.getAbsolutePath());
		}
		LOGGER.debug("Found no matching charset for file \"{}\"", file.getAbsolutePath());
		return null;
	}

	/**
	 * Tests if file is UTF-8 encoded with or without BOM.
	 *
	 * @param file File to test
	 * @return True if file is UTF-8 encoded with or without BOM, false otherwise.
	 * @throws IOException
	 */
	public static boolean isFileUTF8(File file) throws IOException {
		return isCharsetUTF8(getFileCharset(file));
	}

	/**
	 * Tests if charset is UTF-8.
	 *
	 * @param charset <code>Charset</code> to test
	 * @return True if charset is UTF-8, false otherwise.
	 */
	public static boolean isCharsetUTF8(Charset charset) {
		return charset != null && charset.equals(StandardCharsets.UTF_8);
	}

	/**
	 * Tests if charset is UTF-8.
	 *
	 * @param charsetName charset name to test
	 * @return True if charset is UTF-8, false otherwise.
	 */
	public static boolean isCharsetUTF8(String charsetName) {
		return equalsIgnoreCase(charsetName, CHARSET_UTF_8);
	}

	/**
	 * Tests if file is UTF-16 encoded.
	 *
	 * @param file File to test
	 * @return True if file is UTF-16 encoded, false otherwise.
	 * @throws IOException
	 */
	public static boolean isFileUTF16(File file) throws IOException {
		return isCharsetUTF16(getFileCharset(file));
	}

	/**
	 * Tests if {@code charset} is {@code UTF-16}.
	 *
	 * @param charset the {@link Charset} to test.
	 * @return {@code true} if {@code charset} is {@code UTF-16}, {@code false}
	 *         otherwise.
	 */
	public static boolean isCharsetUTF16(Charset charset) {
		return charset != null && (charset.equals(StandardCharsets.UTF_16) || charset.equals(StandardCharsets.UTF_16BE) || charset.equals(StandardCharsets.UTF_16LE));
	}

	/**
	 * Tests if {@code charset} is {@code UTF-16}.
	 *
	 * @param charsetName the charset name to test
	 * @return {@code true} if {@code charsetName} is {@code UTF-16},
	 *         {@code false} otherwise.
	 */
	public static boolean isCharsetUTF16(String charsetName) {
		return (equalsIgnoreCase(charsetName, CHARSET_UTF_16LE) || equalsIgnoreCase(charsetName, CHARSET_UTF_16BE));
	}

	/**
	 * Tests if {@code charsetName} is {@code UTF-32}.
	 *
	 * @param charsetName the charset name to test.
	 * @return {@code true} if {@code charsetName} is {@code UTF-32},
	 *         {@code false} otherwise.
	 */
	public static boolean isCharsetUTF32(String charsetName) {
		return (equalsIgnoreCase(charsetName, CHARSET_UTF_32LE) || equalsIgnoreCase(charsetName, CHARSET_UTF_32BE));
	}

	/**
	 * Converts an {@code UTF-16} input file to an {@code UTF-8} output file.
	 * Does not overwrite an existing output file.
	 *
	 * @param inputFile an {@code UTF-16} {@link File}.
	 * @param outputFile the {@code UTF-8} {@link File} after conversion.
	 * @throws IOException If an IO error occurs during the operation.
	 */
	public static void convertFileFromUtf16ToUtf8(File inputFile, File outputFile) throws IOException {
		Charset charset;
		if (inputFile == null) {
			throw new IllegalArgumentException("inputFile cannot be null");
		}

		charset = getFileCharset(inputFile);
		if (isCharsetUTF16(charset)) {
			if (!outputFile.exists()) {
				/*
				 * This is a strange hack, and I'm not sure if it's needed. I
				 * did it this way to conform to the tests, which dictates that
				 * UTF-16LE should produce UTF-8 without BOM while UTF-16BE
				 * should produce UTF-8 with BOM.
				 *
				 * For some reason creating a FileInputStream with UTF_16 produces
				 * an UTF-8 outputfile without BOM, while using UTF_16LE or
				 * UTF_16BE produces an UTF-8 outputfile with BOM.
				 *
				 * @author Nadahar
				 */
				try (BufferedReader reader =
					charset.equals(StandardCharsets.UTF_16LE) ?
						new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_16)) :
						new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), charset))
				) {
					try (BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))
					) {
						int c;

						while ((c = reader.read()) != -1) {
							writer.write(c);
						}
					}

				}
			}
		} else {
			throw new IllegalArgumentException("File is not UTF-16");
		}
	}

	/**
	 * Return a file or folder's permissions.<br><br>
	 *
	 * This should <b>NOT</b> be used for checking e.g. read permissions before
	 * trying to open a file, because you can't assume that the same is true
	 * when you actually open the file. Other threads or processes could have
	 * locked the file (or changed it's permissions) in the meanwhile. Instead,
	 * use e.g <code>FileNotFoundException</code> like this:
	 * <pre><code>
	 * } catch (FileNotFoundException e) {
	 * 	LOGGER.debug("Can't read xxx {}", e.getMessage());
	 * }
	 * </code></pre>
	 * <code>e.getMessage()</code> will contain both the full path to the file
	 * the reason it couldn't be read (e.g. no permission).
	 *
	 * @param file The file or folder to check permissions for
	 * @return A <code>FilePermissions</code> object holding the permissions
	 * @throws FileNotFoundException
	 * @see {@link #getFilePermissions(String)}
	 */
	public static FilePermissions getFilePermissions(File file) throws FileNotFoundException {
		return new FilePermissions(file);
	}

	/**
	 * Like {@link #getFilePermissions(File)} but returns <code>null</code>
	 * instead of throwing <code>FileNotFoundException</code> if the file or
	 * folder isn't found.
	 */
	public static FilePermissions getFilePermissionsNoThrow(File file) {
		try {
			return new FilePermissions(file);
		} catch (FileNotFoundException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Return a file or folder's permissions.<br><br>
	 *
	 * This should <b>NOT</b> be used for checking e.g. read permissions before
	 * trying to open a file, because you can't assume that the same is true
	 * when you actually open the file. Other threads or processes could have
	 * locked the file (or changed it's permissions) in the meanwhile. Instead,
	 * use e.g <code>FileNotFoundException</code> like this:
	 * <pre><code>
	 * } catch (FileNotFoundException e) {
	 * 	LOGGER.debug("Can't read xxx {}", e.getMessage());
	 * }
	 * </code></pre>
	 * <code>e.getMessage()</code> will contain both the full path to the file
	 * the reason it couldn't be read (e.g. no permission).
	 *
	 * @param path The file or folder name to check permissions for
	 * @return A <code>FilePermissions</code> object holding the permissions
	 * @throws FileNotFoundException
	 * @see {@link #getFilePermissions(File)}
	 */
	public static FilePermissions getFilePermissions(String path) throws FileNotFoundException {
		if (path != null) {
			return new FilePermissions(new File(path));
		}
		File file = null;
		return new FilePermissions(file);
	}

	/**
	 * Like {@link #getFilePermissions(String)} but returns <code>null</code>
	 * instead of throwing <code>FileNotFoundException</code> if the file or
	 * folder isn't found.
	 */
	public static FilePermissions getFilePermissionsNoThrow(String path) {
		if (path != null) {
			try {
				return new FilePermissions(new File(path));
			} catch (FileNotFoundException | IllegalArgumentException e) {
				return null;
			}
		}
		return null;
	}

	public static boolean isFileRelevant(File f, PmsConfiguration configuration) {
		String fileName = f.getName().toLowerCase();
		if (
			(
				configuration.isArchiveBrowsing() &&
				(
					fileName.endsWith(".zip") ||
					fileName.endsWith(".cbz") ||
					fileName.endsWith(".rar") ||
					fileName.endsWith(".cbr")
				)
			) ||
			fileName.endsWith(".iso") ||
			fileName.endsWith(".img") ||
			fileName.endsWith(".m3u") ||
			fileName.endsWith(".m3u8") ||
			fileName.endsWith(".pls") ||
			fileName.endsWith(".cue")
		) {
			return true;
		}

		return false;
	}

	public static boolean isFolderRelevant(File f, PmsConfiguration configuration) {
		return isFolderRelevant(f, configuration, Collections.<String>emptySet());
	}

	public static boolean isFolderRelevant(File f, PmsConfiguration configuration, Set<String> ignoreFiles) {
		if (f.isDirectory() && configuration.isHideEmptyFolders()) {
			File[] children = f.listFiles();

			/**
			 * listFiles() returns null if "this abstract pathname does not denote a directory, or if an I/O error occurs".
			 * in this case (since we've already confirmed that it's a directory), this seems to mean the directory is non-readable
			 * http://www.ps3mediaserver.org/forum/viewtopic.php?f=6&t=15135
			 * http://stackoverflow.com/questions/3228147/retrieving-the-underlying-error-when-file-listfiles-return-null
			 */
			if (children == null) {
				LOGGER.warn("Can't list files in non-readable directory: {}", f.getAbsolutePath());
			} else {
				for (File child : children) {
					if (ignoreFiles.contains(child.getAbsolutePath())) {
						continue;
					}

					if (child.isFile()) {
						if (FormatFactory.getAssociatedFormat(child.getName()) != null || isFileRelevant(child, configuration)) {
							return true;
						}
					} else {
						if (isFolderRelevant(child, configuration, ignoreFiles)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static String renameForSorting(String filename) {
		if (PMS.getConfiguration().isPrettifyFilenames()) {
			// This makes anime sort properly
			filename = removeGroupNameFromBeginning(filename);

			// Replace periods and underscores with spaces
			filename = filename.replaceAll("\\.|_", " ");
		}

		if (PMS.getConfiguration().isIgnoreTheWordAandThe()) {
			// Remove "a" and "the" from filename
			filename = filename.replaceAll("^(?i)A[ .]|The[ .]", "");

			// Replace multiple whitespaces with space
			filename = filename.replaceAll("\\s{2,}"," ");
		}

		return filename;
	}

	/**
	 * Attempts to detect the {@link Charset} used in the specified {@link File}
	 * and creates a {@link BufferedReader} using that {@link Charset}. If the
	 * {@link Charset} detection fails, the specified default {@link Charset}
	 * will be used.
	 *
	 * @param file the {@link File} to use.
	 * @param defaultCharset the fallback {@link Charset} it automatic detection
	 *            fails. If {@code null}, the JVM default {@link Charset} will
	 *            be used.
	 * @return The resulting {@link BufferedReaderDetectCharsetResult}.
	 * @throws IOException If an I/O error occurs during the operation.
	 */
	@Nonnull
	public static BufferedReaderDetectCharsetResult createBufferedReaderDetectCharset(
		@Nullable File file,
		@Nullable Charset defaultCharset
	) throws IOException {
		BufferedReader reader;
		Charset fileCharset = getFileCharset(file);
		if (fileCharset != null) {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileCharset));
			return new BufferedReaderDetectCharsetResult(reader, fileCharset, true);
		}
		if (defaultCharset == null) {
			defaultCharset = Charset.defaultCharset();
		}
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), defaultCharset));
		LOGGER.warn(
			"Could not detect character encoding for file \"{}\"; using the default charset \"{}\"",
			file.getAbsolutePath(),
			defaultCharset
		);
		return new BufferedReaderDetectCharsetResult(reader, defaultCharset, false);
	}

	/**
	 * Checks for valid file name syntax. Only the {@link Path#getFileName()}
	 * part is evaluated.
	 *
	 * @param file the {@link Path} to be verified.
	 * @return whether or not the file name is valid.
	 */
	public static boolean isValidFileName(@Nullable Path file) {
		if (file == null) {
			return false;
		}
		Path fileName = file.getFileName();
		if (fileName == null) {
			return false;
		}
		return isValidFileName(fileName.toString());
	}

	/**
	 * Checks for valid file name syntax. Only the {@link File#getName()} part
	 * is evaluated.
	 *
	 * @param file the {@link File} to be verified.
	 * @return whether or not the file name is valid.
	 */
	public static boolean isValidFileName(@Nullable File file) {
		if (file == null) {
			return false;
		}
		return isValidFileName(file.getName());
	}

	/**
	 * Checks for valid file name syntax. Paths are not allowed.
	 *
	 * @param fileName the file name to be verified.
	 * @return whether or not the file name is valid.
	 */
	public static boolean isValidFileName(@Nullable String fileName) {
		if (isBlank(fileName)) {
			return false;
		}
		if (Platform.isWindows()) {
			return fileName.matches("^[^\"*:<>?/\\\\]+$");
		} else if (Platform.isMac()) {
			return fileName.matches("^[^:/]+$");
		}
		// Assuming POSIX
		return fileName.matches("^[A-Za-z0-9._][A-Za-z0-9._-]*$");
	}

	/**
	 * Appends a path separator of the same type last in the string if it's not
	 * already there.
	 *
	 * @param path the path to be modified.
	 * @return The corrected path or {@code null} if {@code path} is
	 *         {@code null}.
	 */
	public static String appendPathSeparator(String path) {
		if (!path.endsWith("\\") && !path.endsWith("/")) {
			if (path.contains("\\")) {
				path += "\\";
			} else {
				path += "/";
			}
		}
		return path;
	}

	/**
	 * Appends a suffix to a filename before the last {@code "."} if there is
	 * one. If not, simply appends the suffix to the filename.
	 *
	 * @param fileName the filename to append to.
	 * @param suffix the suffix to append.
	 * @return The modified filename.
	 */
	@Nonnull
	public static String appendToFileName(@Nonnull String fileName, @Nullable String suffix) {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName cannot be null");
		}
		if (isBlank(suffix)) {
			return fileName;
		}
		int i = fileName.lastIndexOf(".");
		if (i < 0) {
			return fileName + suffix;
		}
		return fileName.substring(0, i) + suffix + fileName.substring(i);
	}

	private static Boolean isAdmin = null;
	private static Object isAdminLock = new Object();

	/**
	 * Determines whether or not the program has admin/root permissions.
	 */
	public static boolean isAdmin() {
		synchronized(isAdminLock) {
			if (isAdmin != null) {
				return isAdmin;
			}
			if (Platform.isWindows()) {
				Version version = BasicSystemUtils.INSTANCE.getOSVersion();
				if (version.isGreaterThanOrEqualTo(5, 1)) {
					try {
						String command = "reg query \"HKU\\S-1-5-19\"";
						Process p = Runtime.getRuntime().exec(command);
						p.waitFor();
						int exitValue = p.exitValue();

						if (0 == exitValue) {
							isAdmin = true;
							return true;
						}
						isAdmin = false;
						return false;
					} catch (IOException | InterruptedException e) {
						LOGGER.error("An error prevented DMS from checking Windows permissions: {}", e.getMessage());
					}
				} else {
					isAdmin = true;
					return true;
				}
			} else if (Platform.isLinux() || Platform.isMac()) {
				try {
					final String command = "id -Gn";
					LOGGER.trace("isAdmin: Executing \"{}\"", command);
					Process p = Runtime.getRuntime().exec(command);
					InputStream is = p.getInputStream();
					int exitValue;
					String exitLine;
					try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
						p.waitFor();
						exitValue = p.exitValue();
						exitLine = br.readLine();
					}
					if (exitValue != 0 || exitLine == null || exitLine.isEmpty()) {
						LOGGER.error("Could not determine root privileges, \"{}\" ended with exit code: {}", command, exitValue);
						isAdmin = false;
						return false;
					}
					LOGGER.trace("isAdmin: \"{}\" returned {}", command, exitLine);
					if
						((Platform.isLinux() && exitLine.matches(".*\\broot\\b.*")) ||
						(Platform.isMac() && exitLine.matches(".*\\badmin\\b.*")))
					{
						LOGGER.trace("isAdmin: DMS has {} privileges", Platform.isLinux() ? "root" : "admin");
						isAdmin = true;
						return true;
					}
					LOGGER.trace("isAdmin: DMS does not have {} privileges", Platform.isLinux() ? "root" : "admin");
					isAdmin = false;
					return false;
				} catch (IOException | InterruptedException e) {
					LOGGER.error("An error prevented DMS from checking {} permissions: {}", Platform.isMac() ? "OS X" : "Linux" ,e.getMessage());
				}
			}
			isAdmin = false;
			return false;
		}
	}

	/**
	 * Finds the {@link UnixMountPoint} for a {@link java.nio.file.Path} given
	 * that the file resides on a Unix file system.
	 *
	 * @param path the {@link java.nio.file.Path} for which to find the Unix mount point.
	 * @return The {@link UnixMountPoint} for the given path.
	 *
	 * @throws InvalidFileSystemException
	 */
	public static UnixMountPoint getMountPoint(Path path) throws InvalidFileSystemException {
		UnixMountPoint mountPoint = new UnixMountPoint();
		FileStore store;
		try {
			store = Files.getFileStore(path);
		} catch (IOException e) {
			throw new InvalidFileSystemException(
				String.format("Could not get Unix mount point for file \"%s\": %s", path.toAbsolutePath(), e.getMessage()),
				e
			);
		}

		try {
			Field entryField = store.getClass().getSuperclass().getDeclaredField("entry");
			Field nameField = entryField.getType().getDeclaredField("name");
			Field dirField = entryField.getType().getDeclaredField("dir");
			entryField.setAccessible(true);
			nameField.setAccessible(true);
			dirField.setAccessible(true);
			mountPoint.device = new String((byte[]) nameField.get(entryField.get(store)), StandardCharsets.UTF_8);
			mountPoint.folder = new String((byte[]) dirField.get(entryField.get(store)), StandardCharsets.UTF_8);
			return mountPoint;
		} catch (NoSuchFieldException e) {
			throw new InvalidFileSystemException(String.format("File \"%s\" is not on a Unix file system", path.isAbsolute()), e);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new InvalidFileSystemException(
				String.format("An error occurred while trying to find mount point for file \"%s\": %s", path.toAbsolutePath(), e.getMessage()),
				e
			);
		}
	}

	/**
	 * Finds the {@link UnixMountPoint} for a {@link java.io.File} given
	 * that the file resides on a Unix file system.
	 *
	 * @param file the {@link java.io.File} for which to find the Unix mount point.
	 * @return The {@link UnixMountPoint} for the given path.
	 *
	 * @throws InvalidFileSystemException
	 */
	public static UnixMountPoint getMountPoint(File file) throws InvalidFileSystemException {
		return getMountPoint(file.toPath());
	}

	public static boolean isUnixStickyBit(Path path) throws IOException, InvalidFileSystemException {
		PosixFileAttributes attr = Files.readAttributes(path, PosixFileAttributes.class);
		try {
			Field st_modeField = attr.getClass().getDeclaredField("st_mode");
			st_modeField.setAccessible(true);
			int st_mode = st_modeField.getInt(attr);
			return (st_mode & S_ISVTX) > 0;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new InvalidFileSystemException("File is not on a Unix file system: " + e.getMessage(), e);
		}
	}

	private static int unixUID = Integer.MIN_VALUE;
	private static Object unixUIDLock = new Object();

	/**
	 * Gets the user ID on Unix based systems. This should not change during a
	 * session and the lookup is expensive, so we cache the result.
	 *
	 * @return The Unix user ID
	 * @throws IOException
	 */
	public static int getUnixUID() throws IOException {
		if (
			Platform.isAIX() || Platform.isFreeBSD() || Platform.isGNU() || Platform.iskFreeBSD() ||
			Platform.isLinux() || Platform.isMac() || Platform.isNetBSD() || Platform.isOpenBSD() ||
			Platform.isSolaris()
		) {
			synchronized (unixUIDLock) {
				if (unixUID < 0) {
					String response;
					Process id;
					id = Runtime.getRuntime().exec("id -u");
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(id.getInputStream(), Charset.defaultCharset()))) {
						response = reader.readLine();
					}
					try {
						unixUID = Integer.parseInt(response);
					} catch (NumberFormatException e) {
						throw new UnsupportedOperationException("Unexpected response from OS: " + response, e);
					}
				}
				return unixUID;
			}
		}
		throw new UnsupportedOperationException("getUnixUID can only be called on Unix based OS'es");
	}

	/**
	 * @return The OS {@code PATH} environment variable as a {@link List} of
	 *         {@link Path}s.
	 */
	@Nonnull
	public static List<Path> getOSPath() {
		List<Path> result = new ArrayList<>();
		String osPath = System.getenv("PATH");
		if (isBlank(osPath)) {
			return result;
		}
		String[] paths = osPath.split(File.pathSeparator);
		for (String path : paths) {
			if (isBlank(path)) {
				continue;
			}
			try {
				result.add(Paths.get(path));
			} catch (InvalidPathException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.warn(
						"Unable to resolve PATH element \"{}\" to a folder, it will be ignored: {}",
						path,
						e.getMessage()
					);
					LOGGER.trace("", e);
				} else {
					LOGGER.warn("Unable to resolve PATH element \"{}\" to a folder, it will be ignored", path);
				}
			}
		}
		return result;
	}

	/**
	 * Tries to find the specified relative file that is both executable and
	 * readable using the system {@code PATH} environment variable while
	 * following symbolic links. Returns the first match in the order of the
	 * system {@code PATH} or {@code null} is no match was found.
	 *
	 * @param relativePath the relative {@link Path} describing the file or
	 *            folder to return.
	 * @return The matched {@link Path} or {@code null} if no match was found.
	 * @throws IllegalArgumentException if {@code relativePath} is absolute.
	 */
	@Nullable
	public static Path findExecutableInOSPath(@Nullable Path relativePath) {
		return findInOSPath(relativePath, true, FileFlag.FILE, FileFlag.READ, FileFlag.EXECUTE);
	}

	/**
	 * Tries to find the specified relative file or folder using the system
	 * {@code PATH} environment variable. Returns the first match in the order
	 * of the system {@code PATH} or {@code null} if no match was found.
	 *
	 * @param relativePath the relative {@link Path} describing the file or
	 *            folder to return.
	 * @param followSymlinks whether or not to follow symbolic links (NIO
	 *            default is {@code true}).
	 * @param requiredFlags zero or more {@link FileFlag}s that specify
	 *            permissions or properties that must be met for a file object
	 *            to match. Use for example {@link FileFlag#FILE} to only find
	 *            files or {@link FileFlag#FOLDER} to only find folders.
	 * @return The matched {@link Path} or {@code null} if no match was found.
	 * @throws IllegalArgumentException if {@code relativePath} is absolute.
	 */
	@Nullable
	public static Path findInOSPath(
		@Nullable Path relativePath,
		boolean followSymlinks,
		FileFlag... requiredFlags
	) {
		if (relativePath == null) {
			return null;
		}
		if (relativePath.isAbsolute()) {
			throw new IllegalArgumentException("relativePath must be relative");
		}

		LinkOption[] options = followSymlinks ? new LinkOption[] {} : new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
		List<Path> osPath = new ArrayList<>();
		osPath.add(null);
		osPath.addAll(getOSPath());
		Path result = null;
		List<String> extensions = new ArrayList<>();
		extensions.add(null);
		if (Platform.isWindows() && getExtension(relativePath) == null) {
			for (String s : WindowsProgramPaths.getWindowsPathExtensions()) {
				if (isNotBlank(s)) {
					extensions.add("." + s);
				}
			}
		}
		for (String extension : extensions) {
			for (Path path : osPath) {
				if (path == null) {
					path = Paths.get("").toAbsolutePath();
				}
				if (extension == null) {
					result = path.resolve(relativePath);
				} else {
					result = path.resolve(relativePath.toString() + extension);
				}
				if (Files.exists(result, options)) {
					if (requiredFlags.length == 0) {
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("Resolved \"{}\" from \"{}\" using OS path", result, relativePath);
						}
						try {
							return result.toRealPath(options);
						} catch (IOException e) {
							LOGGER.warn("Could not get the real path of \"{}\": {}", result, e.getMessage());
							LOGGER.trace("", e);
							return result;
						}
					}
					try {
						if (new FilePermissions(result, options).hasFlags(requiredFlags)) {
							if (LOGGER.isTraceEnabled()) {
								LOGGER.trace("Resolved \"{}\" from \"{}\" using OS path", result, relativePath);
							}
							try {
								return result.toRealPath(options);
							} catch (IOException e) {
								LOGGER.warn("Could not get the real path of \"{}\": {}", result, e.getMessage());
								LOGGER.trace("", e);
								return result;
							}
						}
					} catch (FileNotFoundException e) {
						continue;
					}
				}
			}
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Failed to resolve \"{}\" using OS path", relativePath);
		}
		return null;
	}

	/**
	 * Compares the content of two files and determines if they are identical or
	 * not. This can be a slow operation for large files if they are equal or
	 * the difference is towards the end.
	 * <p>
	 * This method aborts as soon as a difference is found, so it should
	 * normally be quick when comparing unequal files.
	 *
	 * @param file1 the first file to compare.
	 * @param file2 the second file to compare.
	 * @param options the {@link LinkOption}s, if any.
	 * @return {@code true} if the content of the files are identical, if the
	 *         they both point to the same file or if both doesn't exist.
	 *         {@code false} otherwise.
	 * @throws IOException If either {@code file1} or {@code file2} isn't a
	 *             file, or if an error occurs during reading.
	 */
	public static boolean isFileContentEqual(
		@Nullable Path file1,
		@Nullable Path file2,
		LinkOption... options
	) throws IOException {
		if (file1 == file2) {
			return true;
		}
		if (file1 == null || file2 == null) {
			return false;
		}
		boolean exists1 = Files.exists(file1, options);
		if (exists1 != Files.exists(file2, options)) {
			return false;
		}
		if (!exists1) {
			// Consider two non-existing files as equal
			return true;
		}
		BasicFileAttributes attributes1 = Files.readAttributes(file1, BasicFileAttributes.class, options);
		BasicFileAttributes attributes2 = Files.readAttributes(file2, BasicFileAttributes.class, options);
		if (!attributes1.isRegularFile()) {
			throw new IOException("\"" + file1.toAbsolutePath().toString() + "\" isn't a (regular) file");
		}
		if (!attributes2.isRegularFile()) {
			throw new IOException("\"" + file2.toAbsolutePath().toString() + "\" isn't a (regular) file");
		}

		if (attributes1.size() != attributes2.size()) {
			return false;
		}

		if (Files.isSameFile(file1, file2)) {
			return true;
		}

		ByteBuffer buffer1 = ByteBuffer.allocate(4096);
		ByteBuffer buffer2 = ByteBuffer.allocate(4096);
		int count;
		try (
			SeekableByteChannel channel1 = Files.newByteChannel(file1, options);
			SeekableByteChannel channel2 = Files.newByteChannel(file2, options);
		) {
			boolean eof = false;
			while (!eof) {
				while (buffer1.hasRemaining()) {
					count = channel1.read(buffer1);
					if (count == -1) {
						eof = true;
						break;
					}
				}

				while (buffer2.hasRemaining()) {
					count = channel2.read(buffer2);
					if (count == -1) {
						eof = true;
						break;
					}
				}
				buffer1.flip();
				buffer2.flip();
				if (buffer1.remaining() != buffer2.remaining()) {
					return false;
				}
				if (!Arrays.equals(buffer1.array(), buffer2.array())) {
					return false;
				}
				buffer1.clear();
				buffer2.clear();
			}
		}
		return true;
	}

	/**
	 * Asserts that the specified {@link Path} exists, is of the specified
	 * "type" and is readable.
	 *
	 * @param path the {@link Path} to assert is readable.
	 * @param file {@code true} if {@code path} is a file, {@code false} of
	 *            {@code path} is a folder.
	 * @return {@code true} if {@code path} exists, is of the specified "type"
	 *         and is readable, {@code false} otherwise..
	 */
	@Nonnull
	public static boolean assertReadable(@Nullable Path path, boolean file) {
		if (path == null) {
			return false;
		}

		EnumSet<FileFlag> flags = file ?
			EnumSet.of(FileFlag.FILE, FileFlag.READ) :
			EnumSet.of(FileFlag.FOLDER, FileFlag.BROWSE);
		return exists(path, flags);
	}

	/**
	 * Asserts that the specified {@link Path} is writable, creating it and any
	 * parent folders as necessary. The result of the operation is returned as
	 * an {@link AssertResult}.
	 *
	 * @param path the {@link Path} to assert is writable.
	 * @param file {@code true} if {@code path} is a file, {@code false} of
	 *            {@code path} is a folder.
	 * @return The {@link AssertResult}.
	 */
	@Nonnull
	public static AssertResult assertWritable(@Nullable Path path, boolean file) {
		if (path == null) {
			return new AssertResult(false, false, false);
		}

		EnumSet<FileFlag> flags = file ?
			EnumSet.of(FileFlag.FILE, FileFlag.READ, FileFlag.WRITE) :
			EnumSet.of(FileFlag.FOLDER, FileFlag.BROWSE, FileFlag.WRITE);
		try {
			FilePermissions permissions = new FilePermissions(path);
			return new AssertResult(
				permissions.hasFlags(file ? FileFlag.FILE : FileFlag.FOLDER),
				permissions.hasFlags(flags),
				false
			);
		} catch (FileNotFoundException e) {
			// Try to create
			Path parent = path.toAbsolutePath().getParent();
			if (parent == null) {
				return new AssertResult(false, false, false);
			}
			AssertResult result = assertWritable(parent, false);
			if (!result.writable) {
				return new AssertResult(false, false, false);
			}
			try {
				if (file) {
					path = Files.createFile(parent.resolve(path.getFileName()));
				} else {
					path = Files.createDirectory(parent.resolve(path.getFileName()));
				}
				return new AssertResult(true, new FilePermissions(path).hasFlags(flags), true);
			} catch (IOException e1) {
				LOGGER.warn(
					"Failed to create {} \"{}\": {}",
					(file ? "file" : "folder"),
					parent.resolve(path.getFileName()),
					e1.getMessage()
				);
				LOGGER.trace("", e1);
				return new AssertResult(false, false, false);
			}
		}
	}

	/**
	 * A simple container for the the results of {@link FileUtil#assertWritable(Path)}.
	 *
	 * @author Nadahar
	 */
	public static class AssertResult {

		/** {@code true} if the file or folder exists, {@code false} otherwise */
		public final boolean exists;

		/**
		 * {@code true} if the file or folder is writable, {@code false}
		 * otherwise
		 */
		public final boolean writable;

		/**
		 * {@code true} if the file or folder was created, {@code false}
		 * otherwise
		 */
		public final boolean created;

		/**
		 * Creates a new instance using the specified values.
		 *
		 * @param exists {@code true} if the file or folder exists,
		 *            {@code false} otherwise.
		 * @param writable {@code true} if the file or folder is writable,
		 *            {@code false} otherwise.
		 * @param created {@code true} if the file or folder was created,
		 *            {@code false} otherwise.
		 */
		public AssertResult(boolean exists, boolean writable, boolean created) {
			this.exists = exists;
			this.writable = writable;
			this.created = created;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder
				.append("CreateOrAssertResult [exists=").append(exists)
				.append(", writable=").append(writable)
				.append(", created=").append(created).append("]");
			return builder.toString();
		}
	}

	/**
	 * This class holds the results from
	 * {@link FileUtil#createBufferedReaderDetectCharset}.
	 *
	 * @author Nadahar
	 */
	public static class BufferedReaderDetectCharsetResult implements Closeable {
		private final BufferedReader reader;
		private final Charset charset;
		private final boolean successfulDetection;

		/**
		 * Creates a new instance with the given parameters.
		 *
		 * @param reader the {@link BufferedReader}.
		 * @param charset the {@link Charset}.
		 * @param successfulDetection {@code true} is {@link Charset} detection
		 *            was successful, {@code false} otherwise.
		 */
		public BufferedReaderDetectCharsetResult(
			@Nullable BufferedReader reader,
			@Nullable Charset charset,
			boolean successfulDetection
		) {
			this.reader = reader;
			this.charset = charset;
			this.successfulDetection = successfulDetection;
		}

		/**
		 * @return The {@link BufferedReader}.
		 */
		@Nullable
		public BufferedReader getBufferedReader() {
			return reader;
		}

		/**
		 * @return The {@link Charset} used for the {@link BufferedReader}.
		 */
		@Nullable
		public Charset getCharset() {
			return charset;
		}

		/**
		 * @return {@code true} if {@link Charset} detection was successful,
		 *         {@code false} if the default was used..
		 */
		public boolean isSuccessfulDetection() {
			return successfulDetection;
		}

		@Override
		public void close() throws IOException {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
