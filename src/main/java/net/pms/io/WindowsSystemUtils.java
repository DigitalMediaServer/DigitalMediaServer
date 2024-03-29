/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
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
package net.pms.io;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.LongByReference;
import java.awt.Toolkit;
import java.io.File;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.platform.windows.CSIDL;
import net.pms.platform.windows.GUID;
import net.pms.platform.windows.KnownFolders;
import net.pms.platform.windows.Shell32Util;
import net.pms.util.FileUtil;
import net.pms.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the Windows specific native functionality. Do not try to instantiate on Linux/MacOS X !
 *
 * @author zsombor
 */
public class WindowsSystemUtils extends BasicSystemUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsSystemUtils.class);

	public interface Kernel32 extends Library {
		Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class);
		Kernel32 SYNC_INSTANCE = (Kernel32) Native.synchronizedLibrary(INSTANCE);

		int GetShortPathNameW(WString lpszLongPath, char[] lpdzShortPath, int cchBuffer);

		int GetWindowsDirectoryW(char[] lpdzShortPath, int uSize);

		boolean GetVolumeInformationW(
			char[] lpRootPathName,
			CharBuffer lpVolumeNameBuffer,
			int nVolumeNameSize,
			LongByReference lpVolumeSerialNumber,
			LongByReference lpMaximumComponentLength,
			LongByReference lpFileSystemFlags,
			CharBuffer lpFileSystemNameBuffer,
			int nFileSystemNameSize
		);

		int SetThreadExecutionState(int EXECUTION_STATE);
		int ES_CONTINUOUS        = 0x80000000;
		int ES_SYSTEM_REQUIRED   = 0x00000001;
		int ES_DISPLAY_REQUIRED  = 0x00000002;
		int ES_AWAYMODE_REQUIRED = 0x00000040;

		int GetACP();
		int GetOEMCP();
		int GetConsoleOutputCP();

		/**
		 * Returns the current version number of the operating system packed as
		 * a {@link DWORD}.
		 * <p>
		 * <b>Note:</b> For Windows 8.1 and later, the returned value depends on
		 * how the application is manifested and might lie. If the application
		 * isn't manifested, the returned value is never higher than Windows 8
		 * (6.2).
		 * <p>
		 * For all platforms, the low-order word contains the version number of
		 * the operating system. The low-order byte of this word specifies the
		 * major version number, in hexadecimal notation. The high-order byte
		 * specifies the minor version (revision) number, in hexadecimal
		 * notation. The high-order bit is zero, the next 7 bits represent the
		 * build number, and the low-order byte is 5.
		 *
		 * @return If the function succeeds, the return value includes the major
		 *         and minor version numbers of the operating system in the low
		 *         order word, and information about the operating system
		 *         platform in the high order word.
		 */
		DWORD GetVersion();
	}

	protected final Path psPing;
	protected final String avsPluginsFolder;
	protected final String kLiteFiltersDir;

	@Override
	public File getAvsPluginsDir() {
		if (avsPluginsFolder == null) {
			return null;
		}
		File pluginsDir = new File(avsPluginsFolder);
		if (!pluginsDir.exists()) {
			pluginsDir = null;
		}
		return pluginsDir;
	}

	/**
	 * The Filters directory for K-Lite Codec Pack, which contains vsfilter.dll
	 */
	@Override
	public File getKLiteFiltersDir() {
		if (kLiteFiltersDir == null) {
			return null;
		}
		File filtersDir = new File(kLiteFiltersDir + "\\Filters");
		if (!filtersDir.exists()) {
			filtersDir = null;
		}
		return filtersDir;
	}

	@Override
	public String getShortPathNameW(String longPathName) {
		boolean unicodeChars;
		try {
			byte b1[] = longPathName.getBytes("UTF-8");
			byte b2[] = longPathName.getBytes("cp1252");
			unicodeChars = b1.length != b2.length;
		} catch (Exception e) {
			return longPathName;
		}

		if (unicodeChars) {
			try {
				WString pathname = new WString(longPathName);

				char test[] = new char[2 + pathname.length() * 2];
				int r = Kernel32.INSTANCE.GetShortPathNameW(pathname, test, test.length);
				if (r > 0) {
					LOGGER.trace("Forcing short path name on " + pathname);
					return Native.toString(test);
				}
				LOGGER.debug("Can't find \"{}\"", pathname);
				return null;

			} catch (Exception e) {
				return longPathName;
			}
		}
		return longPathName;
	}

	@Override
	public String getWindowsDirectory() {
		char test[] = new char[2 + 256 * 2];
		int r = Kernel32.INSTANCE.GetWindowsDirectoryW(test, 256);
		if (r > 0) {
			return Native.toString(test);
		}
		return null;
	}

	@Override
	public String getDiskLabel(File f) {
		String driveName;
		try {
			driveName = f.getCanonicalPath().substring(0, 2) + "\\";

			char[] lpRootPathName_chars = new char[4];
			for (int i = 0; i < 3; i++) {
				lpRootPathName_chars[i] = driveName.charAt(i);
			}
			lpRootPathName_chars[3] = '\0';
			int nVolumeNameSize = 256;
			CharBuffer lpVolumeNameBuffer_char = CharBuffer.allocate(nVolumeNameSize);
			LongByReference lpVolumeSerialNumber = new LongByReference();
			LongByReference lpMaximumComponentLength = new LongByReference();
			LongByReference lpFileSystemFlags = new LongByReference();
			int nFileSystemNameSize = 256;
			CharBuffer lpFileSystemNameBuffer_char = CharBuffer.allocate(nFileSystemNameSize);

			boolean result2 = Kernel32.INSTANCE.GetVolumeInformationW(
				lpRootPathName_chars,
				lpVolumeNameBuffer_char,
				nVolumeNameSize,
				lpVolumeSerialNumber,
				lpMaximumComponentLength,
				lpFileSystemFlags,
				lpFileSystemNameBuffer_char,
				nFileSystemNameSize);
			if (!result2) {
				return null;
			}
			String diskLabel = charString2String(lpVolumeNameBuffer_char);
			return diskLabel;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * For description see <a HREF=
	 * "https://msdn.microsoft.com/en-us/library/windows/desktop/dd318070%28v=vs.85%29.aspx"
	 * >MSDN GetACP</a>
	 *
	 * @return the value from Windows API GetACP()
	 */
	public static int getACP() {
		return Kernel32.INSTANCE.GetACP();
	}

	/**
	 * For description see <a HREF=
	 * "https://msdn.microsoft.com/en-us/library/windows/desktop/dd318114%28v=vs.85%29.aspx"
	 * >MSDN GetOEMCP</a>
	 *
	 * @return the value from Windows API GetOEMCP()
	 */
	public static int getOEMCP() {
		return Kernel32.INSTANCE.GetOEMCP();
	}

	/**
	 * @return The result from the Windows API {@code GetConsoleOutputCP()}.
	 */
	public static int getConsoleOutputCP() {
		return Kernel32.INSTANCE.GetConsoleOutputCP();
	}

	private static String charString2String(CharBuffer buf) {
		char[] chars = buf.array();
		int i;
		for (i = 0; i < chars.length; i++) {
			if (chars[i] == '\0') {
				break;
			}
		}
		return new String(chars, 0, i);
	}

	/**
	 * Only to be instantiated by {@link BasicSystemUtils#createInstance()}.
	 */
	protected WindowsSystemUtils() {
		getVLCRegistryInfo();
		avsPluginsFolder = getAviSynthPluginsFolder();
		aviSynth = avsPluginsFolder != null;
		kLiteFiltersDir = getKLiteFiltersFolder();
		psPing = findPsPing();
	}

	protected void getVLCRegistryInfo() {
		String key = "SOFTWARE\\VideoLAN\\VLC";
		try {
			if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
				key = "SOFTWARE\\Wow6432Node\\VideoLAN\\VLC";
				if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
					return;
				}
			}
			vlcPath = Paths.get(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, ""));
			vlcVersion = new Version(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, "Version"));
		} catch (Win32Exception e) {
			LOGGER.debug("Could not get VLC information from Windows registry: {}", e.getMessage());
			LOGGER.trace("", e);
		}
	}

	protected String getAviSynthPluginsFolder() {
		String key = "SOFTWARE\\AviSynth";
		try {
			if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
				key = "SOFTWARE\\Wow6432Node\\AviSynth";
				if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
					return null;
				}
			}
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, "plugindir2_5");
		} catch (Win32Exception e) {
			LOGGER.debug("Could not get AviSynth information from Windows registry: {}", e.getMessage());
			LOGGER.trace("", e);
		}
		return null;
	}

	protected String getKLiteFiltersFolder() {
		String key = "SOFTWARE\\Wow6432Node\\KLCodecPack";
		try {
			if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
				key = "SOFTWARE\\KLCodecPack";
				if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key)) {
					return null;
				}
			}
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, "installdir");
		} catch (Win32Exception e) {
			LOGGER.debug("Could not get K-Lite Codec Pack information from Windows registry: {}", e.getMessage());
			LOGGER.trace("", e);
		}
		return null;
	}

	protected Path findPsPing() {
		// PsPing
		Path tmpPsPing = null;
		tmpPsPing = FileUtil.findExecutableInOSPath(Paths.get("psping64.exe"));
		if (tmpPsPing == null) {
			tmpPsPing = FileUtil.findExecutableInOSPath(Paths.get("psping.exe"));
		}
		return tmpPsPing;
	}

	@Override
	public String[] getPingCommand(String hostAddress, int count, int packetSize) {
		if (psPing != null) {
			return new String[] {
				psPing.toString(),
				"-w", // warmup
				"0",
				"-i", // interval
				"0",
				"-n", // count
				Integer.toString(count),
				"-l", // size
				Integer.toString(packetSize),
				hostAddress
			};
		}
		return new String[] {
			"ping",
			"-n", // count
			Integer.toString(count),
			"-l", // size
			Integer.toString(packetSize),
			hostAddress
		};
	}

	@Override
	public String parsePingLine(String line) {
		if (psPing != null) {
			int msPos = line.indexOf("ms");

			if (msPos == -1) {
				return null;
			}
			return line.substring(line.lastIndexOf(':', msPos) + 1, msPos).trim();
		}
		return super.parsePingLine(line);
	}

	@Override
	@Nonnull
	protected String getTrayIconName() {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		switch (dpi) {
			case 96:
				return "icon-16.png";
			case 120:
				return "icon-20.png";
			case 144:
				return "icon-24.png";
			case 168:
				return "icon-28.png";
			case 192:
				return "icon-32.png";
			default:
				// This will be scaled, so use a large one for a better end result
				return "icon-256.png";
		}
	}

	@Override
	@Nonnull
	protected Version getOSVersionInternal() {
		DWORD dword = Kernel32.INSTANCE.GetVersion();
		int low = dword.getLow().intValue();
		int high = dword.getHigh().intValue();
		return new Version(low & 0xFF, low >> 8, 0, high >= 0 ? high : 0);
	}

	@Override
	@Nullable
	public String getComputerName() {
		try {
			return Kernel32Util.getComputerName();
		} catch (Win32Exception e) {
			LOGGER.error("The call to Kernel32.getComputerName() failed with: {}", e.getMessage());
			LOGGER.trace("", e);
			return null;
		}
	}

	@Override
	protected Path enumerateDefaultFolders(List<Path> folders) {
		Path desktop = null;
		Version version = getOSVersion();
		if (version.isGreaterThanOrEqualTo(6)) {
			ArrayList<GUID> knownFolders = new ArrayList<>(Arrays.asList(new GUID[]{
				KnownFolders.FOLDERID_Desktop,
				KnownFolders.FOLDERID_Downloads,
				KnownFolders.FOLDERID_Music,
				KnownFolders.FOLDERID_OriginalImages,
				KnownFolders.FOLDERID_PhotoAlbums,
				KnownFolders.FOLDERID_Pictures,
				KnownFolders.FOLDERID_Playlists,
				KnownFolders.FOLDERID_PublicDesktop,
				KnownFolders.FOLDERID_PublicDownloads,
				KnownFolders.FOLDERID_PublicMusic,
				KnownFolders.FOLDERID_PublicPictures,
				KnownFolders.FOLDERID_PublicVideos,
				KnownFolders.FOLDERID_SavedPictures,
				KnownFolders.FOLDERID_Videos,
			}));
			if (version.isGreaterThanOrEqualTo(6, 2)) { // Windows 8
				knownFolders.add(KnownFolders.FOLDERID_Screenshots);
			}
			for (GUID guid : knownFolders) {
				Path folder = Shell32Util.getCurrentUserKnownFolderPath(guid);
				if (folder != null) {
					folders.add(folder);
					if (guid == KnownFolders.FOLDERID_Desktop) {
						desktop = folder;
					}
				} else {
					LOGGER.debug("Default folder \"{}\" not found", guid);
				}
			}
		} else {
			CSIDL[] csidls = {
				CSIDL.CSIDL_COMMON_DESKTOPDIRECTORY,
				CSIDL.CSIDL_COMMON_MUSIC,
				CSIDL.CSIDL_COMMON_PICTURES,
				CSIDL.CSIDL_COMMON_VIDEO,
				CSIDL.CSIDL_DESKTOPDIRECTORY,
				CSIDL.CSIDL_MYMUSIC,
				CSIDL.CSIDL_MYPICTURES,
				CSIDL.CSIDL_MYVIDEO
			};
			for (CSIDL csidl : csidls) {
				Path folder = Shell32Util.getCurrentUserFolderPath(csidl);
				if (folder != null) {
					folders.add(folder);
					if (csidl == CSIDL.CSIDL_DESKTOPDIRECTORY) {
						desktop = folder;
					}
				} else {
					LOGGER.debug("Default folder \"{}\" not found", csidl);
				}
			}
		}
		return desktop;
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} or {@link CSIDL} identifier in the context of the
	 * <b>current user</b>. {@link KnownFolders} are used for more recent
	 * Windows versions, while {@link CSIDL}s are used for older Windows
	 * versions. Most commonly used identifiers have a sibling in the other
	 * type.
	 * <p>
	 * This method will take the current Windows version into account and decide
	 * which identifier to use. If the identifier appropriate for the current
	 * system is {@code null}, {@code null} will be returned. Both should thus
	 * normally be specified.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} identifier as defined
	 *            in {@link KnownFolders}
	 * @param folder the {@link CSIDL} identifier.
	 * @return The {@link Path} of the known folder or {@code null} if the
	 *         identifier is {@code null} or references a known folder which
	 *         does not have a path on the current system.
	 */
	@Nullable
	public Path getCurrentUserKnownFolderPath(@Nullable GUID guid, @Nullable CSIDL folder) {
		return getKnownFolderPath(guid, folder, false);
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} or {@link CSIDL} identifier in the context of the
	 * <b>default user</b>. {@link KnownFolders} are used for more recent
	 * Windows versions, while {@link CSIDL}s are used for older Windows
	 * versions. Most commonly used identifiers have a sibling in the other
	 * type.
	 * <p>
	 * This method will take the current Windows version into account and decide
	 * which identifier to use. If the identifier appropriate for the current
	 * system is {@code null}, {@code null} will be returned. Both should thus
	 * normally be specified.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} identifier as defined
	 *            in {@link KnownFolders}
	 * @param folder the {@link CSIDL} identifier.
	 * @return The {@link Path} of the known folder or {@code null} if the
	 *         identifier is {@code null} or references a known folder which
	 *         does not have a path on the current system.
	 */
	@Nullable
	public Path getDefaultUserKnownFolderPath(@Nullable GUID guid, @Nullable CSIDL folder) {
		return getKnownFolderPath(guid, folder, true);
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} or {@link CSIDL} identifier. {@link KnownFolders}
	 * are used for more recent Windows versions, while {@link CSIDL}s are used
	 * for older Windows versions. Most commonly used identifiers have a sibling
	 * in the other type.
	 * <p>
	 * This method will take the current Windows version into account and decide
	 * which identifier to use. If the identifier appropriate for the current
	 * system is {@code null}, {@code null} will be returned. Both should thus
	 * normally be specified.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} identifier as defined
	 *            in {@link KnownFolders}
	 * @param folder the {@link CSIDL} identifier.
	 * @param defaultUser {@code true} to get the folder in the context of the
	 *            <i>default user</i>, {@code false} to get the folder in the
	 *            context of the <i>current user</i>.
	 * @return The {@link Path} of the known folder or {@code null} if the
	 *         identifier is {@code null} or references a known folder which
	 *         does not have a path on the current system.
	 */
	@Nullable
	public Path getKnownFolderPath(@Nullable GUID guid, @Nullable CSIDL folder, boolean defaultUser) {
		HANDLE hToken = defaultUser ? new HANDLE(new Pointer(-1L)) : null;
		if (getOSVersion().isGreaterThanOrEqualTo(6)) {
			if (guid == null) {
				return null;
			}
			return Shell32Util.getKnownFolderPath(guid, ShlObj.KNOWN_FOLDER_FLAG.NONE.getFlag(), hToken);
		} else {
			if (folder == null) {
				return null;
			}
			return Shell32Util.getFolderPath(folder, hToken);
		}
	}
}
