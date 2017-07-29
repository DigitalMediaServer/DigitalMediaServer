/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com. Copyright
 * (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * This class is used to represent and compare software version values.
 *
 * @author Nadahar
 */
@Immutable
public final class Version implements Comparable<Version> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);

	/** Used to match the first version element in a hexadecimal version string */
	public static final Pattern HEX_ELEMENT_FIRST = Pattern.compile("([^a-fA-F0-9\\.,_\\s]*([a-fA-F0-9]+))[\\.,_ ]?");

	/**
	 * Used to match the remaining version elements in a hexadecimal version
	 * string
	 */
	public static final Pattern HEX_ELEMENT = Pattern.compile("([a-fA-F0-9]+)[\\.,_ ]?");

	/** Used to match the first version element in a decimal version string */
	public static final Pattern DEC_ELEMENT_FIRST = Pattern.compile("([^\\d\\.,_\\s]*(\\d+))[\\.,_ ]?");

	/** Used to match the remaining version elements in a decimal version string */
	public static final Pattern DEC_ELEMENT = Pattern.compile("(\\d+)[\\.,_ ]?");

	/** Used to match any trailing (non-parsable) content in a version string */
	public static final Pattern ALPHA_ELEMENT = Pattern.compile("\\s*(.*\\S)\\s*");
	private final String versionString;
	private final int[] elementValues;
	private final String[] elements;
	private final String text;

	/**
	 * Creates a new instance by parsing {@code versionString} as decimal
	 * values.
	 *
	 * @param versionString the version {@link String} to parse.
	 */
	public Version(@Nonnull String versionString) {
		this(versionString, false);
	}

	/**
	 * Creates a new instance by parsing {@code versionString}.
	 *
	 * @param versionString the version {@link String} to parse.
	 * @param hexadecimal Whether to parse version numbers as hexadecimal (
	 *            {@code true}) or decimal ({@code false}) values.
	 */
	public Version(@Nonnull String versionString, boolean hexadecimal) {
		if (versionString == null) {
			throw new NullPointerException("Version string can not be null");
		}
		this.versionString = versionString.trim();

		List<String> tmpElements = new ArrayList<>();
		List<Integer> tmpElementValues = new ArrayList<>();
		String tmpText = null;
		String remaining = versionString.trim();
		Matcher matcher = hexadecimal ? HEX_ELEMENT_FIRST.matcher(remaining) : DEC_ELEMENT_FIRST.matcher(remaining);
		if (matcher.lookingAt()) {
			tmpElementValues.add(Integer.parseInt(matcher.group(2), hexadecimal ? 16 : 10));
			tmpElements.add(matcher.group(1));
			remaining = versionString.substring(matcher.end());
			matcher = hexadecimal ? HEX_ELEMENT.matcher(remaining) : DEC_ELEMENT.matcher(remaining);
			while (matcher.lookingAt()) {
				try {
					tmpElementValues.add(Integer.parseInt(matcher.group(1), hexadecimal ? 16 : 10));
					tmpElements.add(matcher.group(1));
					remaining = remaining.substring(matcher.end());
					matcher.reset(remaining);
				} catch (NumberFormatException e) {
					LOGGER.debug("Could not parse version element \"{}\"", matcher.group(2));
				}
			}
		}
		if (isNotBlank(remaining)) {
			matcher = ALPHA_ELEMENT.matcher(remaining);
			if (matcher.lookingAt()) {
				tmpText = matcher.group(1);
			}
		}
		elements = tmpElements.toArray(new String[tmpElements.size()]);
		elementValues = new int[tmpElementValues.size()];
		for (int i = 0; i < tmpElementValues.size(); i++) {
			elementValues[i] = tmpElementValues.get(i).intValue();
		}
		text = tmpText;
	}

	/**
	 * Creates a new instance using the specified values.
	 *
	 * @param major the major version value.
	 * @param minor the minor version value.
	 * @param revision the revision value.
	 * @param build the build value.
	 */
	public Version(int major, int minor, int revision, int build) {
		elementValues = new int[] {major, minor, revision, build};
		elements = new String[] {
			Integer.toString(major),
			Integer.toString(minor),
			Integer.toString(revision),
			Integer.toString(build)
		};
		versionString = StringUtils.join(elementValues, '.');
		text = null;
	}

	/**
	 * Checks if this {@link Version} is less than the specified {@link Version}
	 * .
	 *
	 * @param other the {@link Version} to compare to.
	 * @return {@code true} if this version is less than {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isLessThan(Version other) {
		return compareTo(other) < 0;
	}

	/**
	 * Checks if this {@link Version} is less than the specified {@link Version}
	 * .
	 *
	 * @param other the {@link Version} to compare to.
	 * @param includeTextSuffix if {@code true} any trailing text is compared
	 *            lexicographically if all the element values are equal.
	 * @return {@code true} if this version is less than {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isLessThan(Version other, boolean includeTextSuffix) {
		return compareTo(other, includeTextSuffix) < 0;
	}

	/**
	 * Checks if this {@link Version} is less than or equal to the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @return {@code true} if this version less than or equal to {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isLessThanOrEqualTo(Version other) {
		return compareTo(other) <= 0;
	}

	/**
	 * Checks if this {@link Version} is less than or equal to the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @param includeTextSuffix if {@code true} any trailing text is compared
	 *            lexicographically if all the element values are equal.
	 * @return {@code true} if this version less than or equal to {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isLessThanOrEqualTo(Version other, boolean includeTextSuffix) {
		return compareTo(other, includeTextSuffix) <= 0;
	}

	/**
	 * Checks if this {@link Version} is greater than the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @return {@code true} if this version is greater than {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isGreaterThan(Version other) {
		return compareTo(other) > 0;
	}

	/**
	 * Checks if this {@link Version} is greater than the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @param includeTextSuffix if {@code true} any trailing text is compared
	 *            lexicographically if all the element values are equal.
	 * @return {@code true} if this version is greater than {@code other},
	 *         {@code false} otherwise.
	 */
	public boolean isGreaterThan(Version other, boolean includeTextSuffix) {
		return compareTo(other, includeTextSuffix) > 0;
	}

	/**
	 * Checks if this {@link Version} is less greater or equal to the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @return {@code true} if this version greater than or equal to
	 *         {@code other}, {@code false} otherwise.
	 */
	public boolean isGreaterThanOrEqualTo(Version other) {
		return compareTo(other) >= 0;
	}

	/**
	 * Checks if this {@link Version} is less greater or equal to the specified
	 * {@link Version}.
	 *
	 * @param other the {@link Version} to compare to.
	 * @param includeTextSuffix if {@code true} any trailing text is compared
	 *            lexicographically if all the element values are equal.
	 * @return {@code true} if this version greater than or equal to
	 *         {@code other}, {@code false} otherwise.
	 */
	public boolean isGreaterThanOrEqualTo(Version other, boolean includeTextSuffix) {
		return compareTo(other, includeTextSuffix) >= 0;
	}

	/**
	 * @return the first element value of this {@link Version} or 0 if it
	 *         doesn't exist.
	 */
	public int getMajor() {
		if (elementValues.length > 0) {
			return elementValues[0];
		}
		return 0;
	}

	/**
	 * @return the first element of this {@link Version} or {@code null} if it
	 *         doesn't exist.
	 */
	@Nullable
	public String getMajorString() {
		if (elements.length > 0) {
			return elements[0];
		}
		return null;
	}

	/**
	 * @return the second element value of this {@link Version} or 0 if it
	 *         doesn't exist.
	 */
	public int getMinor() {
		if (elementValues.length > 1) {
			return elementValues[1];
		}
		return 0;
	}

	/**
	 * @return the second element of this {@link Version} or {@code null} if it
	 *         doesn't exist.
	 */
	@Nullable
	public String getMinorString() {
		if (elements.length > 1) {
			return elements[1];
		}
		return null;
	}

	/**
	 * @return the third element value of this {@link Version} or 0 if it
	 *         doesn't exist.
	 */
	public int getRevision() {
		if (elementValues.length > 2) {
			return elementValues[2];
		}
		return 0;
	}

	/**
	 * @return the third element of this {@link Version} or {@code null} if it
	 *         doesn't exist.
	 */
	@Nullable
	public String getRevisionString() {
		if (elements.length > 2) {
			return elements[2];
		}
		return null;
	}

	/**
	 * @return the fourth element value of this {@link Version} or 0 if it
	 *         doesn't exist.
	 */
	public int getBuild() {
		if (elementValues.length > 3) {
			return elementValues[3];
		}
		return 0;
	}

	/**
	 * @return the fourth element of this {@link Version} or {@code null} if it
	 *         doesn't exist.
	 */
	@Nullable
	public String getBuildString() {
		if (elements.length > 4) {
			return elements[4];
		}
		return null;
	}

	/**
	 * @return A copy of all the parsed elements of this {@link Version}.
	 */
	@Nullable
	public String[] getElements() {
		if (elements == null) {
			return null;
		}
		String[] result = new String[elements.length];
		if (elements.length > 0) {
			System.arraycopy(elements, 0, result, 0, elements.length);
		}
		return result;
	}

	/**
	 * @return A copy of all the parsed element values of this {@link Version}.
	 */
	@Nullable
	public int[] getElementValues() {
		if (elementValues == null) {
			return null;
		}
		int[] result = new int[elementValues.length];
		if (elementValues.length > 0) {
			System.arraycopy(elementValues, 0, result, 0, elementValues.length);
		}
		return result;
	}

	/**
	 * @return Any trailing (non-parsable) content or {@code null}.
	 */
	@Nullable
	public String getTextSuffix() {
		return text;
	}

	/**
	 * @return The exact version string as passed to the constructor.
	 */
	@Nonnull
	public String getVersionString() {
		return versionString;
	}

	/**
	 * Returns an array of the element values with trailing zeros removed. That
	 * means that {@code 4.0.2.0} will return {@code [4, 0, 2]}.
	 *
	 * @return A copy of the parsed element values with trailing zeros removed.
	 */
	public int[] getCanonicalElements() {

		int canonicalElementsLength = 0;
		// Find the last (rightmost) non-zero element.
		for (int i = elementValues.length - 1; i >= 0; --i) {
			if (elementValues[i] != 0) {
				canonicalElementsLength = i + 1;
				break;
			}
		}

		int[] result = new int[canonicalElementsLength];
		if (canonicalElementsLength > 0) {
			System.arraycopy(elementValues, 0, result, 0, canonicalElementsLength);
		}
		return result;
	}

	/**
	 * Compares this {@link Version} with the specified {@link Version} for
	 * order. Returns a negative integer, zero, or a positive integer as this
	 * instance is less than, equal to, or greater than the specified instance.
	 *
	 * @param other the {@link Version} to compare to.
	 * @return A negative integer, zero, or a positive integer as this instance
	 *         is less than, equal to, or greater than the specified instance.
	 */
	@Override
	public int compareTo(Version other) {
		return compareTo(other, false);
	}

	/**
	 * Compares this {@link Version} with the specified {@link Version} for
	 * order. Returns a negative integer, zero, or a positive integer as this
	 * instance is less than, equal to, or greater than the specified instance.
	 *
	 * @param other the {@link Version} to compare to.
	 * @param includeTextSuffix also compares the trailing text (if any) if all
	 *            the version numbers are equal. This comparison is done
	 *            lexicographically.
	 * @return A negative integer, zero, or a positive integer as this instance
	 *         is less than, equal to, or greater than the specified instance.
	 */
	public int compareTo(Version other, boolean includeTextSuffix) {
		if (other == null) {
			return -1;
		}
		int maxIdx = Math.max(elementValues.length, other.elementValues.length);
		for (int i = 0; i < maxIdx; i++) {
			int thisValue = i < elementValues.length ? elementValues[i] : 0;
			int otherValue = i < other.elementValues.length ? other.elementValues[i] : 0;
			if (thisValue < otherValue) {
				return -1;
			}
			if (thisValue > otherValue) {
				return 1;
			}
		}

		if (includeTextSuffix && (text != null || other.text != null)) {
			if (text == null) {
				return -1;
			}
			if (other.text == null) {
				return 1;
			}
			return text.compareTo(other.text);
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(getCanonicalElements());
		return result;
	}

	/**
	 * Compares the element values of this {@link Version} with that of
	 * {@code object}. If the number of elements differ, missing element values
	 * are substituted with 0.
	 *
	 * @param object the {@link Object} to compare to.
	 * @return {@code true} if {@code object} is an instance of {@link Version}
	 *         and the element values are equal, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (!(object instanceof Version)) {
			return false;
		}
		return compareTo((Version) object) == 0;
	}

	/**
	 * Compares the element values of this {@link Version} with that of
	 * {@code other}. If the number of elements differ, missing element values
	 * are substituted with 0. If all the element values are equal, any trailing
	 * text will also be compared.
	 *
	 * @param other the {@link Version} to compare to.
	 * @return {@code true} if the element values and any trailing text are
	 *         equal, {@code false} otherwise.
	 */
	public boolean equalsDetailed(Version other) {
		if (equals(other)) {
			if (text == null) {
				return other.text == null;
			}
			return text.equals(other.text);
		}
		return false;
	}

	/**
	 * Compares the unparsed version string of this {@link Version} with that of
	 * {@code other} in either a case sensitive or case insensitive manner.
	 *
	 * @param other the {@link Version} to compare to.
	 * @param caseSensitive whether or not the version string comparison should
	 *            be case sensitive.
	 * @return {@code true} if the version strings are equal, {@code false}
	 *         otherwise.
	 */
	public boolean equalsExcact(Version other, boolean caseSensitive) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!caseSensitive) {
			String thisString = versionString.toLowerCase(Locale.ROOT);
			String otherString = other.versionString.toLowerCase(Locale.ROOT);
			return thisString.equals(otherString);
		}
		return versionString.equals(other.versionString);
	}

	@Nonnull
	@Override
	public String toString() {
		return versionString;
	}

	/**
	 * Gets {@link VS_FIXEDFILEINFO} from the Windows API for a specified
	 * {@link Path}.
	 * <p>
	 * <b>Must only be called on Windows</b>
	 *
	 * @param path the {@link Path} the get file information for.
	 * @param throwLastError whether or not to throw a
	 *            {@link LastErrorException} if it occurs during the operation.
	 * @return The generated {@link VS_FIXEDFILEINFO} instance or {@code null}
	 * @throws IllegalStateException If called on a non-Windows platform.
	 * @throws LastErrorException If {@code throwLastError} is {@code true} and
	 *             {@code LastError} has a non-zero return value during the
	 *             operation.
	 */
	@Nullable
	public static VS_FIXEDFILEINFO getWindowsFileInfo(@Nonnull Path path, boolean throwLastError) {
		if (!Platform.isWindows()) {
			throw new IllegalStateException("getWindowsFileInfo() can only be called on Windows");
		}
		VS_FIXEDFILEINFO fixedFileInfo = null;
		IntByReference ignored = new IntByReference();
		try {
			int infoSize = WindowsVersion.INSTANCE.GetFileVersionInfoSizeW(path.toString(), ignored);
			if (infoSize > 0) {
				Memory data = new Memory(infoSize);
				if (WindowsVersion.INSTANCE.GetFileVersionInfoW(path.toString(), 0, infoSize, data)) {
					PointerByReference lplpBuffer = new PointerByReference();
					IntByReference puLen = new IntByReference();
					if (WindowsVersion.INSTANCE.VerQueryValueW(data, "\\", lplpBuffer, puLen) && puLen.getValue() > 0) {
						fixedFileInfo = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
					}
				}
			}
		} catch (LastErrorException e) {
			if (throwLastError) {
				throw e;
			}
			LOGGER.debug("getWindowsFileInfo for \"{}\" failed with: {}", path, e.getMessage());
			LOGGER.trace("", e);
		}
		return fixedFileInfo;
	}

	private static boolean isFileVersionValid(@Nonnull VS_FIXEDFILEINFO fixedFileInfo) {
		return
			fixedFileInfo.getFileVersionMajor() >= 0 &&
			fixedFileInfo.getFileVersionMinor() >= 0 &&
			fixedFileInfo.getFileVersionRevision() >= 0 &&
			fixedFileInfo.getFileVersionBuild() >= 0 &&
			(
				fixedFileInfo.getFileVersionMajor() > 0 ||
				fixedFileInfo.getFileVersionMinor() > 0 ||
				fixedFileInfo.getFileVersionRevision() > 0 ||
				fixedFileInfo.getFileVersionBuild() > 0
			);
	}

	private static boolean isProductVersionValid(@Nonnull VS_FIXEDFILEINFO fixedFileInfo) {
		return
			fixedFileInfo.getProductVersionMajor() >= 0 &&
			fixedFileInfo.getProductVersionMinor() >= 0 &&
			fixedFileInfo.getProductVersionRevision() >= 0 &&
			fixedFileInfo.getProductVersionBuild() >= 0 &&
			(
				fixedFileInfo.getProductVersionMajor() > 0 ||
				fixedFileInfo.getProductVersionMinor() > 0 ||
				fixedFileInfo.getProductVersionRevision() > 0 ||
				fixedFileInfo.getProductVersionBuild() > 0
			);
	}

	/**
	 * Attempts to retrieve the file's version information using the Windows
	 * API.
	 * <p>
	 * <b>Must only be called on Windows</b>
	 *
	 * @param path the {@link Path} whose version information to retrieve.
	 * @param versionType which version to return.
	 * @return A new {@link Version} instance if successful, {@code null}
	 *         otherwise.
	 */
	@Nullable
	public static Version getWindowsFileVersion(@Nonnull Path path, @Nullable WindowsVersionType versionType) {
		if (!Platform.isWindows()) {
			throw new IllegalStateException("getWindowsFileVersion() can only be called on Windows");
		}
		VS_FIXEDFILEINFO fixedFileInfo = getWindowsFileInfo(path, false);
		if (fixedFileInfo == null) {
			return null;
		}
		if (versionType == null) {
			versionType = WindowsVersionType.PRODUCT_VERSION_FIRST;
		}
		if ((
				versionType == WindowsVersionType.FILE_VERSION_FIRST ||
				versionType == WindowsVersionType.FILE_VERSION_ONLY
			) &&
			isFileVersionValid(fixedFileInfo)
		) {
			return new Version(
				fixedFileInfo.getFileVersionMajor(),
				fixedFileInfo.getFileVersionMinor(),
				fixedFileInfo.getFileVersionRevision(),
				fixedFileInfo.getFileVersionBuild()
			);
		} else if (versionType == WindowsVersionType.FILE_VERSION_ONLY) {
			return null;
		}
		if (isProductVersionValid(fixedFileInfo)) {
			return new Version(
				fixedFileInfo.getProductVersionMajor(),
				fixedFileInfo.getProductVersionMinor(),
				fixedFileInfo.getProductVersionRevision(),
				fixedFileInfo.getProductVersionBuild()
			);
		}
		if (versionType == WindowsVersionType.PRODUCT_VERSION_FIRST && isFileVersionValid(fixedFileInfo)) {
			return new Version(
				fixedFileInfo.getFileVersionMajor(),
				fixedFileInfo.getFileVersionMinor(),
				fixedFileInfo.getFileVersionRevision(),
				fixedFileInfo.getFileVersionBuild()
			);
		}
		return null;
	}

	/**
	 * An {@code enum} used to specify how to retrieve version information from
	 * {@link VS_FIXEDFILEINFO}.
	 */
	public enum WindowsVersionType {

		/**
		 * Returns {@code FileVersion} if it's available, otherwise
		 * return {@code ProductVersion}.
		 */
		FILE_VERSION_FIRST,

		/** Return {@code FileVersion} only. */
		FILE_VERSION_ONLY,

		/**
		 * Return {@code ProductVersion} if it's available, otherwise
		 * return {@code FileVersion}.
		 */
		PRODUCT_VERSION_FIRST,

		/** Return {@code ProductVersion} only. */
		PRODUCT_VERSION_ONLY
	}

	/**
	 * Provides unicode access to the w32 version library.
	 *
	 * @author Nadahar
	 */
	@SuppressWarnings("checkstyle:MethodName")
	public interface WindowsVersion extends StdCallLibrary {

		/** A static instance of the native Windows version library */
		WindowsVersion INSTANCE = Native.loadLibrary("version", WindowsVersion.class, W32APIOptions.UNICODE_OPTIONS);

		/**
		 * Determines whether Windows can retrieve version information for a
		 * specified file. If version information is available, returns the size
		 * in bytes of that information.
		 *
		 * @param fileName the name of the file of interest. The function uses
		 *            the search sequence specified by the LoadLibrary function.
		 * @param ignore (output) a {@link Pointer} to a variable that the
		 *            function sets to zero.
		 * @return If the function succeeds, the return value is the size, in
		 *         bytes, of the file's version information. If the function
		 *         fails, the return value is zero.
		 * @throws LastErrorException if an error occurs.
		 */
		int GetFileVersionInfoSizeW(String fileName, IntByReference ignore) throws LastErrorException;

		/**
		 * Retrieves version information for the specified file.
		 *
		 * @param fileName the name of the file. If a full path is not
		 *            specified, the function uses the search sequence specified
		 *            by the LoadLibrary function.
		 * @param dummy this parameter is ignored.
		 * @param dwLen the size, in bytes, of the buffer pointed to by the
		 *            lpData parameter. Call {@link #GetFileVersionInfoSizeW}
		 *            first to determine the size, in bytes, of a file's version
		 *            information. {@code dwLen} should be equal to or greater
		 *            than that value. If the buffer pointed to by
		 *            {@code lpData} is not large enough, the function truncates
		 *            the file's version information to the size of the buffer.
		 * @param lpData (output) a {@link Pointer} to a buffer that receives
		 *            the file-version information. You can use this value in a
		 *            subsequent call to {@link #VerQueryValueW} to retrieve
		 *            data from the buffer.
		 * @return {@code true} if the call succeeds.
		 * @throws LastErrorException if an error occurs.
		 */
		boolean GetFileVersionInfoW(String fileName, int dummy, int dwLen, Pointer lpData) throws LastErrorException;

		/**
		 * Retrieves specified version information from the specified
		 * version-information resource. To retrieve the appropriate resource,
		 * before you call {@link #VerQueryValueW}, you must first call
		 * {@link #GetFileVersionInfoSizeW}, and then
		 * {@link #GetFileVersionInfoW}.
		 *
		 * @param pBlock the version-information resource returned by the
		 *            GetFileVersionInfo function.
		 * @param lpSubBlock the version-information value to be retrieved.
		 * @param lplpBuffer (output) a reference to a {@link Pointer} to the
		 *            requested version information in the buffer pointed to by
		 *            {@code pBlock}. The memory pointed to by
		 *            {@code lplpBuffer} is freed when the associated
		 *            {@code pBlock} memory is freed.
		 * @param puLen (output) a pointer to the size of the requested data
		 *            pointed to by {@code lplpBuffer}: for version information
		 *            values, the length in characters of the string stored at
		 *            lplpBuffer; for translation array values, the size in
		 *            bytes of the array stored at lplpBuffer; and for root
		 *            block, the size in bytes of the structure.
		 * @return {@code true} if the specified version-information structure
		 *         exists, and version information is available. If the value of
		 *         {@code puLen} is zero, no value is available for the
		 *         specified version-information name. If the specified name
		 *         does not exist or the specified resource is not valid,
		 *         {@code false} is returned.
		 */
		boolean VerQueryValueW(Pointer pBlock, String lpSubBlock, PointerByReference lplpBuffer, IntByReference puLen);
	}
}
