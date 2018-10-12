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
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.sun.jna.platform.win32.Shell32Util;

/**
 * This is an utility class containing Windows {@code KnownFolder} constants.
 *
 * @author Nadahar
 */
@SuppressWarnings("checkstyle:ConstantName")
public class KnownFolders {

	/**
	 * Programs ({@link CSIDL#CSIDL_COMMON_PROGRAMS})
	 * <code>{0139D44E-6AFE-49F2-8690-3DAFCAE6FFB8}</code>
	 */
	public static final GUID FOLDERID_CommonPrograms = new GUID("{0139D44E-6AFE-49F2-8690-3DAFCAE6FFB8}");

	/**
	 * Start Menu ({@link CSIDL#CSIDL_COMMON_STARTMENU})
	 * <code>{A4115719-D62E-491D-AA7C-E74B8BE3B067}</code>
	 */
	public static final GUID FOLDERID_CommonStartMenu = new GUID("{A4115719-D62E-491D-AA7C-E74B8BE3B067}");

	/**
	 * Startup ({@link CSIDL#CSIDL_COMMON_STARTUP})
	 * <code>{82A5EA35-D9CD-47C5-9629-E15D2F714E6E}</code>
	 */
	public static final GUID FOLDERID_CommonStartup = new GUID("{82A5EA35-D9CD-47C5-9629-E15D2F714E6E}");

	/**
	 * Desktop ({@link CSIDL#CSIDL_DESKTOP} or
	 * {@link CSIDL#CSIDL_DESKTOPDIRECTORY})
	 * <code>{B4BFCC3A-DB2C-424C-B029-7FE99A87C641}</code>
	 */
	public static final GUID FOLDERID_Desktop = new GUID("{B4BFCC3A-DB2C-424C-B029-7FE99A87C641}");

	/**
	 * Documents ({@link CSIDL#CSIDL_MYDOCUMENTS})
	 * <code>{FDD39AD0-238F-46AF-ADB4-6C85480369C7}</code>
	 */
	public static final GUID FOLDERID_Documents = new GUID("{FDD39AD0-238F-46AF-ADB4-6C85480369C7}");

	/** Downloads <code>{374DE290-123F-4565-9164-39C4925E467B}</code> */
	public static final GUID FOLDERID_Downloads = new GUID("{374DE290-123F-4565-9164-39C4925E467B}");

	/**
	 * Fonts ({@link CSIDL#CSIDL_FONTS})
	 * <code>{FD228CB7-AE11-4AE3-864C-16F3910AB8FE}</code>
	 */
	public static final GUID FOLDERID_Fonts = new GUID("{FD228CB7-AE11-4AE3-864C-16F3910AB8FE}");

	/**
	 * Local Application Data ({@link CSIDL#CSIDL_LOCAL_APPDATA})
	 * <code>{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}</code>
	 */
	public static final GUID FOLDERID_LocalAppData = new GUID("{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}");

	/**
	 * Music ({@link CSIDL#CSIDL_MYMUSIC})
	 * <code>{4BD8D571-6D19-48D3-BE97-422220080E43}</code>
	 */
	public static final GUID FOLDERID_Music = new GUID("{4BD8D571-6D19-48D3-BE97-422220080E43}");

	/** Original Images <code>{2C36C0AA-5812-4B87-BFD0-4CD0DFB19B39}</code> */
	public static final GUID FOLDERID_OriginalImages = new GUID("{2C36C0AA-5812-4B87-BFD0-4CD0DFB19B39}");

	/** Slide Shows <code>{69D2CF90-FC33-4FB7-9A0C-EBB0F0FCB43C}</code> */
	public static final GUID FOLDERID_PhotoAlbums = new GUID("{69D2CF90-FC33-4FB7-9A0C-EBB0F0FCB43C}");

	/**
	 * Pictures ({@link CSIDL#CSIDL_MYPICTURES})
	 * <code>{33E28130-4E1E-4676-835A-98395C3BC3BB}</code>
	 */
	public static final GUID FOLDERID_Pictures = new GUID("{33E28130-4E1E-4676-835A-98395C3BC3BB}");

	/** Playlists <code>{DE92C1C7-837F-4F69-A3BB-86E631204A23}</code> */
	public static final GUID FOLDERID_Playlists = new GUID("{DE92C1C7-837F-4F69-A3BB-86E631204A23}");

	/**
	 * Profile ({@link CSIDL#CSIDL_PROFILE})
	 * <code>{5E6C858F-0E22-4760-9AFE-EA3317B67173}</code>
	 */
	public static final GUID FOLDERID_Profile = new GUID("{5E6C858F-0E22-4760-9AFE-EA3317B67173}");

	/**
	 * Programs ({@link CSIDL#CSIDL_PROGRAMS})
	 * <code>{A77F5D77-2E2B-44C3-A6A2-ABA601054A51}</code>
	 */
	public static final GUID FOLDERID_Programs = new GUID("{A77F5D77-2E2B-44C3-A6A2-ABA601054A51}");

	/** Public Desktop <code>{C4AA340D-F20F-4863-AFEF-F87EF2E6BA25}</code> */
	public static final GUID FOLDERID_PublicDesktop = new GUID("{C4AA340D-F20F-4863-AFEF-F87EF2E6BA25}");

	/** Public Documents <code>{ED4824AF-DCE4-45A8-81E2-FC7965083634}</code> */
	public static final GUID FOLDERID_PublicDocuments = new GUID("{ED4824AF-DCE4-45A8-81E2-FC7965083634}");

	/** Public Downloads <code>{3D644C9B-1FB8-4F30-9B45-F670235F79C0}</code> */
	public static final GUID FOLDERID_PublicDownloads = new GUID("{3D644C9B-1FB8-4F30-9B45-F670235F79C0}");

	/**
	 * Public Music ({@link CSIDL#CSIDL_COMMON_MUSIC})
	 * <code>{3214FAB5-9757-4298-BB61-92A9DEAA44FF}</code>
	 */
	public static final GUID FOLDERID_PublicMusic = new GUID("{3214FAB5-9757-4298-BB61-92A9DEAA44FF}");

	/**
	 * Public Pictures ({@link CSIDL#CSIDL_COMMON_PICTURES})
	 * <code>{B6EBFB86-6907-413C-9AF7-4FC2ABF07CC5}</code>
	 */
	public static final GUID FOLDERID_PublicPictures = new GUID("{B6EBFB86-6907-413C-9AF7-4FC2ABF07CC5}");

	/**
	 * Public Videos ({@link CSIDL#CSIDL_COMMON_VIDEO})
	 * <code>{2400183A-6185-49FB-A2D8-4A392A602BA3}</code>
	 */
	public static final GUID FOLDERID_PublicVideos = new GUID("{2400183A-6185-49FB-A2D8-4A392A602BA3}");

	/**
	 * Roaming Application Data ({@link CSIDL#CSIDL_APPDATA})
	 * <code>{3EB685DB-65F9-4CF6-A03A-E3EF65729F3D}</code>
	 */
	public static final GUID FOLDERID_RoamingAppData = new GUID("{3EB685DB-65F9-4CF6-A03A-E3EF65729F3D}");

	/**
	 * Videos ({@link CSIDL#CSIDL_MYVIDEO})
	 * <code>{18989B1D-99B5-455B-841C-AB7C74E4DDFC}</code>
	 */
	public static final GUID FOLDERID_Videos = new GUID("{18989B1D-99B5-455B-841C-AB7C74E4DDFC}");

	/**
	 * Music (Library) <code>{2112AB0A-C86A-4FFE-A368-0DE96E47012E}</code>
	 *
	 * @since Windows 7
	 */
	public static final GUID FOLDERID_MusicLibrary = new GUID("{2112AB0A-C86A-4FFE-A368-0DE96E47012E}");

	/**
	 * Pictures (Library) <code>{A990AE9F-A03B-4E80-94BC-9912D7504104}</code>
	 *
	 * @since Windows 7
	 */
	public static final GUID FOLDERID_PicturesLibrary = new GUID("{A990AE9F-A03B-4E80-94BC-9912D7504104}");

	/**
	 * Common application data ({@code %ALLUSERSPROFILE%}, %ProgramData%)
	 * <code>{62AB5D82-FDC1-4DC3-A9DD-070D1D495D97}</code>
	 */
	public static final GUID FOLDERID_ProgramData = new GUID("{62AB5D82-FDC1-4DC3-A9DD-070D1D495D97}");

	/**
	 * Videos (Library) <code>{491E922F-5643-4Af4-A7EB-4E7A138D8174}</code>
	 *
	 * @since Windows 7
	 */
	public static final GUID FOLDERID_VideosLibrary = new GUID("{491E922F-5643-4Af4-A7EB-4E7A138D8174}");

	/**
	 * Recorded TV <code>{1A6FDBA2-F42D-4358-A798-B74D745926C5}</code>
	 *
	 * @since Windows 7
	 */
	public static final GUID FOLDERID_RecordedTVLibrary = new GUID("{1A6FDBA2-F42D-4358-A798-B74D745926C5}");

	/** Saved Pictures <code>{3B193882-D3AD-4EAB-965A-69829D1FB59F}</code> */
	public static final GUID FOLDERID_SavedPictures = new GUID("{3B193882-D3AD-4EAB-965A-69829D1FB59F}");

	/**
	 * Saved Pictures Library
	 * <code>{E25B5812-BE88-4BD9-94B0-29233477B6C3}</code>
	 *
	 * @since Windows 7
	 */
	public static final GUID FOLDERID_SavedPicturesLibrary = new GUID("{E25B5812-BE88-4BD9-94B0-29233477B6C3}"); // W7

	/**
	 * Screenshots <code>{B7BEDE81-DF94-4682-A7D8-57A52620B86F}</code>
	 *
	 * @since Windows 8
	 */
	public static final GUID FOLDERID_Screenshots = new GUID("{B7BEDE81-DF94-4682-A7D8-57A52620B86F}");

	/**
	 * Start Menu ({@link CSIDL#CSIDL_STARTMENU})
	 * <code>{625B53C3-AB48-4EC1-BA1F-A1EF4146FC19}</code>
	 */
	public static final GUID FOLDERID_StartMenu = new GUID("{625B53C3-AB48-4EC1-BA1F-A1EF4146FC19}");

	/**
	 * Startup ({@link CSIDL#CSIDL_STARTUP})
	 * <code>{B97D20BB-F46A-4C97-BA10-5E3608430854}</code>
	 */
	public static final GUID FOLDERID_Startup = new GUID("{B97D20BB-F46A-4C97-BA10-5E3608430854}");

	/**
	 * Not to be instantiated.
	 */
	private KnownFolders() {
	}

	/**
	 * Returns the {@link Path} to a Windows {@link KnownFolders} folder.
	 *
	 * @param knownFolder the {@link KnownFolders} constant.
	 * @return The corresponding {@link Path} or {@code null} if
	 *         {@code knownFolder} references a {@code KNOWNFOLDERID} which does
	 *         not have a path or that the {@code KNOWNFOLDERID} is not present
	 *         on the system.
	 */
	@Nullable
	public static Path getWindowsKnownFolder(@Nonnull GUID knownFolder) {
		return Paths.get(Shell32Util.getKnownFolderPath(knownFolder));
	}
}
