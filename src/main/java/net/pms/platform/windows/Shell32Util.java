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
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;


/**
 * This utility class implements functions from Windows {@code Shell32.dll}.
 *
 * @author Nadahar
 */
public class Shell32Util {

	/**
	 * Not to be instantiated.
	 */
	private Shell32Util() {
	}

	/**
	 * Gets a special folder {@link Path} in the context of the current user
	 * using {@link ShlObj#SHGFP_TYPE_CURRENT}.
	 *
	 * @param folder the {@link CSIDL}.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getCurrentUserFolderPath(@Nonnull CSIDL folder) {
		return getFolderPath(folder, null, ShlObj.SHGFP_TYPE_CURRENT);
	}

	/**
	 * Gets a special folder {@link Path} in the context of the current user.
	 *
	 * @param folder the {@link CSIDL}.
	 * @param dwFlags {@link ShlObj#SHGFP_TYPE_CURRENT} or
	 *            {@link ShlObj#SHGFP_TYPE_DEFAULT}.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getCurrentUserFolderPath(@Nonnull CSIDL folder, @Nonnull DWORD dwFlags) {
		return getFolderPath(folder, null, dwFlags);
	}

	/**
	 * Gets a special folder {@link Path} in the context of the default user
	 * using {@link ShlObj#SHGFP_TYPE_CURRENT}.
	 *
	 * @param folder the {@link CSIDL}.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getDefaultUserFolderPath(@Nonnull CSIDL folder) {
		return getFolderPath(folder, new HANDLE(new Pointer(-1L)), ShlObj.SHGFP_TYPE_CURRENT);
	}

	/**
	 * Gets a special folder {@link Path} in the context of the default user.
	 *
	 * @param folder the {@link CSIDL}.
	 * @param dwFlags {@link ShlObj#SHGFP_TYPE_CURRENT} or
	 *            {@link ShlObj#SHGFP_TYPE_DEFAULT}.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getDefaultUserFolderPath(@Nonnull CSIDL folder, @Nonnull DWORD dwFlags) {
		return getFolderPath(folder, new HANDLE(new Pointer(-1L)), dwFlags);
	}

	/**
	 * Gets a special folder {@link Path} using
	 * {@link ShlObj#SHGFP_TYPE_CURRENT}.
	 *
	 * @param folder the {@link CSIDL}.
	 * @param hToken an access token that represents a particular user. Use
	 *            {@code null} to use the context of the current user.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getFolderPath(@Nonnull CSIDL folder, @Nullable HANDLE hToken) {
		return getFolderPath(folder, hToken, ShlObj.SHGFP_TYPE_CURRENT);
	}

	/**
	 * Gets a special folder {@link Path}.
	 *
	 * @param folder the {@link CSIDL}.
	 * @param hToken an access token that represents a particular user. Use
	 *            {@code null} to use the context of the current user.
	 * @param dwFlags {@link ShlObj#SHGFP_TYPE_CURRENT} or
	 *            {@link ShlObj#SHGFP_TYPE_DEFAULT}.
	 * @return The special folder or {@code null} if the {@link CSIDL} is
	 *         unknown on this system.
	 */
	@Nullable
	public static Path getFolderPath(@Nonnull CSIDL folder, @Nullable HANDLE hToken, @Nonnull DWORD dwFlags) {
		char[] pszPath = new char[WinDef.MAX_PATH];
		HRESULT hr = Shell32.INSTANCE.SHGetFolderPath(null, folder.getValue(), hToken, dwFlags, pszPath);
		if (!hr.equals(W32Errors.S_OK)) {
			return null;
		}

		return Paths.get(Native.toString(pszPath));
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} entry in the context of the current user.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} as defined in
	 *            {@link KnownFolders}
	 * @return The {@link Path} of the known folder or {@code null} if
	 *         {@code guid} references a {@code KNOWNFOLDERID} which does not
	 *         have a path (such as a folder marked as
	 *         {@code KF_CATEGORY_VIRTUAL}) or that the {@code KNOWNFOLDERID} is
	 *         not present on the system.
	 */
	public static Path getCurrentUserKnownFolderPath(@Nonnull GUID guid) {
		return getKnownFolderPath(guid, ShlObj.KNOWN_FOLDER_FLAG.NONE.getFlag(), null);
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} entry in the context of the current user.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} as defined in
	 *            {@link KnownFolders}
	 * @param flags the flags to use. See {@link ShlObj.KNOWN_FOLDER_FLAG} for
	 *            potential values.
	 * @return The {@link Path} of the known folder or {@code null} if
	 *         {@code guid} references a {@code KNOWNFOLDERID} which does not
	 *         have a path (such as a folder marked as
	 *         {@code KF_CATEGORY_VIRTUAL}) or that the {@code KNOWNFOLDERID} is
	 *         not present on the system.
	 */
	public static Path getCurrentUserKnownFolderPath(@Nonnull GUID guid, int flags) {
		return getKnownFolderPath(guid, flags, null);
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} entry in the context of the default user.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} as defined in
	 *            {@link KnownFolders}
	 * @return The {@link Path} of the known folder or {@code null} if
	 *         {@code guid} references a {@code KNOWNFOLDERID} which does not
	 *         have a path (such as a folder marked as
	 *         {@code KF_CATEGORY_VIRTUAL}) or that the {@code KNOWNFOLDERID} is
	 *         not present on the system.
	 */
	public static Path getDefaultUserKnownFolderPath(@Nonnull GUID guid) {
		return getKnownFolderPath(guid, ShlObj.KNOWN_FOLDER_FLAG.NONE.getFlag(), new HANDLE(new Pointer(-1L)));
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} entry in the context of the default user.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} as defined in
	 *            {@link KnownFolders}
	 * @param flags the flags to use. See {@link ShlObj.KNOWN_FOLDER_FLAG} for
	 *            potential values.
	 * @return The {@link Path} of the known folder or {@code null} if
	 *         {@code guid} references a {@code KNOWNFOLDERID} which does not
	 *         have a path (such as a folder marked as
	 *         {@code KF_CATEGORY_VIRTUAL}) or that the {@code KNOWNFOLDERID} is
	 *         not present on the system.
	 */
	public static Path getDefaultUserKnownFolderPath(@Nonnull GUID guid, int flags) {
		return getKnownFolderPath(guid, flags, new HANDLE(new Pointer(-1L)));
	}

	/**
	 * Retrieves {@link Path} of a known folder identified by the folder's
	 * {@link KnownFolders} entry.
	 *
	 * @param guid the {@code KNOWNFOLDERS} {@link GUID} as defined in
	 *            {@link KnownFolders}
	 * @param flags the flags to use. See {@link ShlObj.KNOWN_FOLDER_FLAG} for
	 *            potential values.
	 * @param hToken an access token that represents a particular user. Use
	 *            {@code null} to use the context of the current user.
	 * @return The {@link Path} of the known folder or {@code null} if
	 *         {@code guid} references a {@code KNOWNFOLDERID} which does not
	 *         have a path (such as a folder marked as
	 *         {@code KF_CATEGORY_VIRTUAL}) or that the {@code KNOWNFOLDERID} is
	 *         not present on the system.
	 */
	@Nullable
	public static Path getKnownFolderPath(@Nonnull GUID guid, int flags, @Nullable HANDLE hToken) {
		PointerByReference outPath = new PointerByReference();
		HRESULT hr = Shell32.INSTANCE.SHGetKnownFolderPath(guid, flags, hToken, outPath);
		if (!W32Errors.SUCCEEDED(hr.intValue())) {
			return null;
		}

		Path result = Paths.get(outPath.getValue().getWideString(0));
		Ole32.INSTANCE.CoTaskMemFree(outPath.getValue());
		return result;
	}
}
