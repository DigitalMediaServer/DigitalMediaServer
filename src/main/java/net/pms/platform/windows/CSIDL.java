/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.platform.windows;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This {@code enum} represents Windows {@code CSIDL} constants.
 *
 * @author Nadahar
 */
public enum CSIDL {

	/**
	 * The file system directory that is used to store administrative tools for
	 * an individual user. The MMC will save customized consoles to this
	 * directory, and it will roam with the user.
	 * <p>
	 * Corresponds to {@code FOLDERID_AdminTools}.
	 */
	CSIDL_ADMINTOOLS(0x0030),

	/**
	 * The file system directory that corresponds to the user's nonlocalized
	 * Startup program group. This value is recognized in Windows Vista for
	 * backward compatibility, but the folder itself no longer exists.
	 * <p>
	 * Corresponds to {@code FOLDERID_Startup}.
	 */
	CSIDL_ALTSTARTUP(0x001d),

	/**
	 * The file system directory that serves as a common repository for
	 * application-specific data. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Application Data}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_RoamingAppData}.
	 */
	CSIDL_APPDATA(0x001a),

	/**
	 * The virtual folder that contains the objects in the user's
	 * {@code Recycle Bin}.
	 * <p>
	 * Corresponds to {@code FOLDERID_RecycleBinFolder}.
	 */
	CSIDL_BITBUCKET(0x000a),

	/**
	 * The file system directory that acts as a staging area for files waiting
	 * to be written to a CD. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Local Settings\\Application Data\\Microsoft\\CD Burning}.
	 * <p>
	 * Corresponds to {@code FOLDERID_CDBurning}.
	 */
	CSIDL_CDBURN_AREA(0x003b),

	/**
	 * The file system directory that contains administrative tools for all
	 * users of the computer.
	 * <p>
	 * Corresponds to {@code FOLDERID_CommonAdminTools}.
	 */
	CSIDL_COMMON_ADMINTOOLS(0x002f),

	/**
	 * The file system directory that corresponds to the nonlocalized Startup
	 * program group for all users. This value is recognized in Windows Vista
	 * for backward compatibility, but the folder itself no longer exists.
	 * <p>
	 * Corresponds to {@code FOLDERID_CommonAdminTools}.
	 */
	CSIDL_COMMON_ALTSTARTUP(0x001e),

	/**
	 * The file system directory that contains application data for all users. A
	 * typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Application Data}. This
	 * folder is used for application data that is not user specific. For
	 * example, an application can store a spell-check dictionary, a database of
	 * clip art, or a log file in this folder. This information will not roam
	 * and is available to anyone using the computer.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_ProgramData}.
	 */
	CSIDL_COMMON_APPDATA(0x0023),

	/**
	 * The file system directory that contains files and folders that appear on
	 * the desktop for all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Desktop}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_PublicDesktop}.
	 */
	CSIDL_COMMON_DESKTOPDIRECTORY(0x0019),

	/**
	 * The file system directory that contains documents that are common to all
	 * users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Documents}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_PublicDocuments}.
	 */
	CSIDL_COMMON_DOCUMENTS(0x002e),

	/**
	 * The file system directory that serves as a common repository for favorite
	 * items common to all users.
	 * <p>
	 * Corresponds to {@code FOLDERID_Favorites}.
	 */
	CSIDL_COMMON_FAVORITES(0x001f),

	/**
	 * The file system directory that serves as a repository for music files
	 * common to all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Documents\\My Music}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_PublicMusic}.
	 */
	CSIDL_COMMON_MUSIC(0x0035),

	/**
	 * This value is recognized in Windows Vista for backward compatibility, but
	 * the folder itself is no longer used.
	 * <p>
	 * Corresponds to {@code FOLDERID_CommonOEMLinks}.
	 */
	CSIDL_COMMON_OEM_LINKS(0x003a),

	/**
	 * The file system directory that serves as a repository for image files
	 * common to all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Documents\\My Pictures}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_PublicPictures}.
	 */
	CSIDL_COMMON_PICTURES(0x0036),

	/**
	 * The file system directory that contains the directories for the common
	 * program groups that appear on the Start menu for all users. A typical
	 * path is
	 * {@code C:\\Documents and Settings\\All Users\\Start Menu\\Programs}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_CommonPrograms}.
	 */
	CSIDL_COMMON_PROGRAMS(0X0017),

	/**
	 * The file system directory that contains the programs and folders that
	 * appear on the Start menu for all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Start Menu}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_CommonStartMenu}.
	 */
	CSIDL_COMMON_STARTMENU(0x0016),

	/**
	 * The file system directory that contains the programs that appear in the
	 * Startup folder for all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Start Menu\\Programs\\Startup}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_CommonStartup}.
	 */
	CSIDL_COMMON_STARTUP(0x0018),

	/**
	 * The file system directory that contains the templates that are available
	 * to all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Templates}.
	 * <p>
	 * Corresponds to {@code FOLDERID_CommonTemplates}.
	 */
	CSIDL_COMMON_TEMPLATES(0x002d),

	/**
	 * The file system directory that serves as a repository for video files
	 * common to all users. A typical path is
	 * {@code C:\\Documents and Settings\\All Users\\Documents\\My Videos}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_PublicVideos}.
	 */
	CSIDL_COMMON_VIDEO(0x0037),

	/**
	 * The folder that represents other computers in your workgroup.
	 * <p>
	 * Corresponds to {@code FOLDERID_NetworkFolder}.
	 */
	CSIDL_COMPUTERSNEARME(0x003d),

	/**
	 * The virtual folder that represents Network Connections, that contains
	 * network and dial-up connections.
	 * <p>
	 * Corresponds to {@code FOLDERID_ConnectionsFolder}.
	 */
	CSIDL_CONNECTIONS(0x0031),

	/**
	 * The virtual folder that contains icons for the Control Panel
	 * applications.
	 * <p>
	 * Corresponds to {@code FOLDERID_ControlPanelFolder}.
	 */
	CSIDL_CONTROLS(0x0003),

	/**
	 * The file system directory that serves as a common repository for Internet
	 * cookies. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Cookies}.
	 * <p>
	 * Corresponds to {@code FOLDERID_Cookies}.
	 */
	CSIDL_COOKIES(0x0021),

	/**
	 * The virtual folder that represents the Windows desktop, the root of the
	 * namespace.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Desktop}.
	 */
	CSIDL_DESKTOP(0x0000),

	/**
	 * The file system directory used to physically store file objects on the
	 * desktop (not to be confused with the desktop folder itself). A typical
	 * path is {@code C:\\Documents and Settings\\username\\Desktop}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Desktop}.
	 */
	CSIDL_DESKTOPDIRECTORY(0x0010),

	/**
	 * The virtual folder that represents My Computer, containing everything on
	 * the local computer: storage devices, printers, and Control Panel. The
	 * folder can also contain mapped network drives.
	 * <p>
	 * Corresponds to {@code FOLDERID_ComputerFolder}.
	 */
	CSIDL_DRIVES(0x0011),

	/**
	 * The file system directory that serves as a common repository for the
	 * user's favorite items. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Favorites}.
	 * <p>
	 * Corresponds to {@code FOLDERID_Favorites}.
	 */
	CSIDL_FAVORITES(0x0006),

	/**
	 * A virtual folder that contains fonts. A typical path is
	 * {@code C:\Windows\Fonts}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Fonts}.
	 */
	CSIDL_FONTS(0x0014),

	/**
	 * The file system directory that serves as a common repository for Internet
	 * history items.
	 * <p>
	 * Corresponds to {@code FOLDERID_History}.
	 */
	CSIDL_HISTORY(0x0022),

	/**
	 * A virtual folder for Internet Explorer.
	 * <p>
	 * Corresponds to {@code FOLDERID_InternetFolder}.
	 */
	CSIDL_INTERNET(0x0001),

	/**
	 * The file system directory that serves as a common repository for
	 * temporary Internet files. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Local Settings\\Temporary Internet Files}.
	 * <p>
	 * Corresponds to {@code FOLDERID_InternetCache}.
	 */
	CSIDL_INTERNET_CACHE(0x0020),

	/**
	 * The file system directory that serves as a data repository for local
	 * (non-roaming) applications. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Local Settings\\Application Data}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_LocalAppData}.
	 */
	CSIDL_LOCAL_APPDATA(0x001c),

	/**
	 * The virtual folder that represents the {@code My Documents} desktop item.
	 * This value is equivalent to {@link #CSIDL_PERSONAL}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Documents}.
	 */
	CSIDL_MYDOCUMENTS(0x0005),

	/**
	 * The file system directory that serves as a common repository for music
	 * files. A typical path is
	 * {@code C:\\Documents and Settings\\User\\My Documents\\My Music}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Music}.
	 */
	CSIDL_MYMUSIC(0x000d),

	/**
	 * The file system directory that serves as a common repository for image
	 * files. A typical path is
	 * {@code C:\\Documents and Settings\\username\\My Documents\\My Pictures}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Pictures}.
	 */
	CSIDL_MYPICTURES(0x0027),

	/**
	 * The file system directory that serves as a common repository for video
	 * files. A typical path is
	 * {@code C:\\Documents and Settings\\username\\My Documents\\My Videos}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Videos}.
	 */
	CSIDL_MYVIDEO(0x000e),

	/**
	 * A file system directory that contains the link objects that may exist in
	 * the My Network Places virtual folder. It is not the same as
	 * {@link #CSIDL_NETWORK}, which represents the network namespace root. A
	 * typical path is {@code C:\\Documents and Settings\\username\\NetHood}.
	 * <p>
	 * Corresponds to {@code FOLDERID_NetHood}.
	 */
	CSIDL_NETHOOD(0x0013),

	/**
	 * A virtual folder that represents Network Neighborhood, the root of the
	 * network namespace hierarchy.
	 * <p>
	 * Corresponds to {@code FOLDERID_NetworkFolder}.
	 */
	CSIDL_NETWORK(0x0012),

	/**
	 * Personal is identical to {@link #CSIDL_MYDOCUMENTS} for Windows XP and
	 * later.
	 */
	CSIDL_PERSONAL(0x0005),

	/**
	 * The virtual folder that contains installed printers.
	 * <p>
	 * Corresponds to {@code FOLDERID_PrintersFolder}.
	 */
	CSIDL_PRINTERS(0x0004),

	/**
	 * The file system directory that contains the link objects that can exist
	 * in the Printers virtual folder. A typical path is
	 * {@code C:\\Documents and Settings\\username\\PrintHood}.
	 * <p>
	 * Corresponds to {@code FOLDERID_PrintHood}.
	 */
	CSIDL_PRINTHOOD(0x001b),

	/**
	 * The user's profile folder. A typical path is {@code C:\\Users\\username}.
	 * Applications should not create files or folders at this level; they
	 * should put their data under the locations referred to by {@link #CSIDL_APPDATA} or
	 * {@link #CSIDL_LOCAL_APPDATA}. However, if you are creating a new Known Folder the
	 * profile root referred to by {@link #CSIDL_PROFILE} is appropriate.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Profile}.
	 */
	CSIDL_PROFILE(0x0028),

	/**
	 * The Program Files folder. A typical path is {@code C:\\Program Files}.
	 * <p>
	 * Corresponds to {@code FOLDERID_ProgramFiles}.
	 */
	CSIDL_PROGRAM_FILES(0x0026),

	/**
	 * The 32-bit Program Files folder on a 64-bit OS. A typical path is
	 * {@code C:\\Program Files (x86)}.
	 * <p>
	 * Corresponds to {@code FOLDERID_ProgramFilesX86}.
	 */
	CSIDL_PROGRAM_FILESX86(0x002a),

	/**
	 * A folder for components that are shared across applications. A typical
	 * path is {@code C:\\Program Files\\Common}. Valid only for Windows XP.
	 * <p>
	 * Corresponds to {@code FOLDERID_ProgramFilesCommon}.
	 */
	CSIDL_PROGRAM_FILES_COMMON(0x002b),

	/**
	 * A folder for 32-bit components that are shared across applications on a
	 * 64-bit OS. A typical path is {@code C:\\Program Files\\Common (x86)}.
	 * Valid only for Windows XP.
	 * <p>
	 * Corresponds to {@code FOLDERID_ProgramFilesCommonX86}.
	 */
	CSIDL_PROGRAM_FILES_COMMONX86(0x002c),

	/**
	 * The file system directory that contains the user's program groups (which
	 * are themselves file system directories). A typical path is
	 * {@code C:\\Documents and Settings\\username\\Start Menu\\Programs}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Programs}.
	 */
	CSIDL_PROGRAMS(0x0002),

	/**
	 * The file system directory that contains shortcuts to the user's most
	 * recently used documents. A typical path is
	 * {@code C:\\Documents and Settings\\username\\My Recent Documents}. To
	 * create a shortcut in this folder, use {@code SHAddToRecentDocs}. In
	 * addition to creating the shortcut, this function updates the Shell's list
	 * of recent documents and adds the shortcut to the
	 * {@code My Recent Documents} submenu of the {@code Start menu}.
	 * <p>
	 * Corresponds to {@code FOLDERID_Recent}.
	 */
	CSIDL_RECENT(0x0008),

	/**
	 * Windows Vista. The file system directory that contains resource data. A
	 * typical path is {@code C:\\Windows\\Resources}.
	 * <p>
	 * Corresponds to {@code FOLDERID_ResourceDir}.
	 */
	CSIDL_RESOURCES(0x0038),

	/**
	 * Localized Resource Directory.
	 * <p>
	 * Corresponds to {@code FOLDERID_LocalizedResourcesDir}.
	 *
	 * @see CSIDL#CSIDL_RESOURCES
	 */
	CSIDL_RESOURCES_LOCALIZED(0x0039),

	/**
	 * The file system directory that contains {@code Send To} menu items. A
	 * typical path is {@code C:\\Documents and Settings\\username\\SendTo}.
	 * <p>
	 * Corresponds to {@code FOLDERID_SendTo}.
	 */
	CSIDL_SENDTO(0x0009),

	/**
	 * The file system directory that contains {@code Start Menu} items. A
	 * typical path is {@code C:\\Documents and Settings\\username\\Start Menu}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_StartMenu}.
	 */
	CSIDL_STARTMENU(0x000b),

	/**
	 * The file system directory that corresponds to the user's Startup program
	 * group. The system starts these programs whenever the associated user logs
	 * on. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Start Menu\\Programs\\Startup}.
	 * <p>
	 * Corresponds to {@link KnownFolders#FOLDERID_Startup}.
	 */
	CSIDL_STARTUP(0x0007),

	/**
	 * The Windows System folder. A typical path is {@code C:\\Windows\\System32}.
	 * <p>
	 * Corresponds to {@code FOLDERID_System}.
	 */
	CSIDL_SYSTEM(0x0025),

	/**
	 * The x86 Windows System Folder on RISC.
	 * <p>
	 * Corresponds to {@code FOLDERID_SystemX86}.
	 */
	CSIDL_SYSTEMX86(0x0029),

	/**
	 * The file system directory that serves as a common repository for document
	 * templates. A typical path is
	 * {@code C:\\Documents and Settings\\username\\Templates}.
	 * <p>
	 * Corresponds to {@code FOLDERID_Templates}.
	 */
	CSIDL_TEMPLATES(0x0015),

	/**
	 * The Windows directory or {@code SYSROOT}. This corresponds to the
	 * {@code %windir%} or {@code %SYSTEMROOT%} environment variables. A typical
	 * path is {@code C:\\Windows}.
	 * <p>
	 * Corresponds to {@code FOLDERID_Windows}.
	 */
	CSIDL_WINDOWS(0x0024);

	private final int value;

	private CSIDL(int value) {
		this.value = value;
	}

	/**
	 * @return The integer ID value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the {@link Path} to a Windows {@link CSIDL} folder.
	 *
	 * @param csidl the {@link CSIDL} instance.
	 * @return The corresponding {@link Path} or {@code null} if {@code csidl}
	 *         does not have a path or that the is not present on the system.
	 */
	@Nullable
	public static Path getWindowsFolder(@Nonnull CSIDL csidl) {
		return Shell32Util.getCurrentUserFolderPath(csidl);
	}
}
