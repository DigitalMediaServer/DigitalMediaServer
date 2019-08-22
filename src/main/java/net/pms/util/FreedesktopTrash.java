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
package net.pms.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import net.pms.exception.InvalidFileSystemException;
import net.pms.util.FileUtil.UnixMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * This is a utility class for handling the "trash bin" according to the
 * freedesktop.org specification.
 *
 * @author Nadahar
 */
public class FreedesktopTrash {
	private static final Logger LOGGER = LoggerFactory.getLogger(FreedesktopTrash.class);
	private static Path homeFolder = null;
	private static Object homeFolderLock = new Object();
	private static final String INFO = "info";
	private static final String FILES = "files";
	private static final SecureRandom RANDOM = new SecureRandom();

	private static String generateRandomFileName(String fileName) {
		if (fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("Invalid file name");
		}

		String prefix;
		String suffix;
		if (fileName.contains(".")) {
			int i = fileName.lastIndexOf('.');
			prefix = fileName.substring(0, i);
			suffix = fileName.substring(i);
		} else {
			prefix = fileName;
			suffix = "";
		}

		long n = RANDOM.nextLong();
		n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
		String newName = prefix + Long.toString(n) + suffix;
		return newName;
	}

	/**
	 * Not to be instantiated.
	 */
	private FreedesktopTrash() {
	}

	private static Path getVerifiedPath(String location) {
		if (location != null && !location.trim().isEmpty()) {
			Path path = Paths.get(location);
			if (Files.exists(path)) {
				return path.toAbsolutePath();
			}
		}
		return null;
	}

	private static Path getHomeFolder() {
		synchronized (homeFolderLock) {
			if (homeFolder == null) {
				homeFolder = getVerifiedPath(System.getenv("XDG_DATA_HOME"));
				if (homeFolder == null) {
					homeFolder = getVerifiedPath(System.getenv("HOME"));
				}
				if (homeFolder == null) {
					homeFolder = getVerifiedPath(System.getProperty("user.home"));
				}
			}
			// Make a copy so we don't have to hold the lock
			return Paths.get(homeFolder.toString());
		}
	}

	private static boolean verifyTrashFolder(Path path, boolean create) {
		FilePermissions permissions;
		try {
			permissions = new FilePermissions(path);
		} catch (FileNotFoundException e) {
			if (create) {
				LOGGER.trace("Trash folder \"{}\" doesn't exist, attempting to create it", path);
				try {
					Files.createDirectories(path);
				} catch (IOException e1) {
					LOGGER.debug("Could not create user trash folder \"{}\": {}", path, e1.getMessage());
					LOGGER.trace("", e1);
					return false;
				}
				try {
					permissions = new FilePermissions(path);
				} catch (FileNotFoundException e1) {
					LOGGER.error("Impossible situation in verifyTrashFolder()", e1);
					return false;
				}
			} else {
				LOGGER.trace("Trash folder \"{}\" doesn't exist", path);
				LOGGER.trace("", e);
				return false;
			}
		}
		if (!(permissions.isBrowsable() && permissions.isWritable() && permissions.isFolder())) {
			if (!permissions.isFolder()) {
				LOGGER.debug("Trash folder \"{}\" is not a folder", path);
			} else {
				LOGGER.debug("Insufficient permissions for trash folder \"{}\": {}", path, permissions.toString());
			}
			return false;
		}
		return true;
	}

	private static Path getTrashFolder(Path path) throws InvalidFileSystemException, IOException {

		UnixMountPoint pathMountPoint;
		try {
			pathMountPoint = FileUtil.getMountPoint(path);
		} catch (InvalidFileSystemException e) {
			throw new InvalidFileSystemException("Invalid file system for file: " + path.toAbsolutePath(), e);
		}
		Path homeFolderInst = getHomeFolder();
		Path trashFolder;
		if (homeFolderInst != null) {
			UnixMountPoint homeMountPoint = null;
			try {
				homeMountPoint = FileUtil.getMountPoint(homeFolderInst);
			} catch (InvalidFileSystemException e) {
				LOGGER.trace(e.getMessage(), e);
				// homeMountPoint == null is ok, fails on .equals()
			}
			if (pathMountPoint.equals(homeMountPoint)) {
				// The file is on the same partition as the home folder,
				// use home folder Trash
				trashFolder = Paths.get(homeFolderInst.toString(), ".Trash");
				if (!Files.exists(trashFolder)) {
					// This is outside specification but follows convention
					trashFolder = Paths.get(homeFolderInst.toString(), ".local/share/Trash");
				}
				if (verifyTrashFolder(trashFolder, true)) {
					return trashFolder;
				}
				return null;
			}
		}

		// The file is on a different partition than the home folder
		// or no home folder was found, look for $topdir/.Trash.
		trashFolder = Paths.get(pathMountPoint.folder, ".Trash");
		if (Files.exists(trashFolder, LinkOption.NOFOLLOW_LINKS)) {
			if (!Files.isSymbolicLink(trashFolder)) {
				try {
					if (FileUtil.isUnixStickyBit(trashFolder)) {
						if (verifyTrashFolder(trashFolder, false)) {
							try {
								trashFolder = Paths.get(trashFolder.toString(), String.valueOf(FileUtil.getUnixUID()));
								if (verifyTrashFolder(trashFolder, true)) {
									return trashFolder;
								}
								LOGGER.trace("Could not read or create trash folder \"{}\", trying next option", trashFolder);
							} catch (IOException e) {
								LOGGER.trace("Could not determine user id while resolving trash folder, trying next option", e);
							}
						} else {
							LOGGER.trace("Insufficient permissions for trash folder \"{}\", trying next option", trashFolder);
						}
					} else {
						LOGGER.trace("Trash folder \"{}\" doesn't have sticky bit set, trying next option", trashFolder);
					}
				} catch (IOException e) {
					LOGGER.trace("Could not determine sticky bit for trash folder \"" + trashFolder + "\", trying next option", e);
				}
			} else {
				LOGGER.trace("Trash folder \"{}\" is a symbolic link, trying next option", trashFolder);
			}
		} else {
			LOGGER.trace("Trash folder \"{}\" doesn't exist, trying next option", trashFolder);
		}

		// $topdir/.Trash not found, looking for $topdir/.Trash-$uid
		try {
			trashFolder = Paths.get(pathMountPoint.folder, ".Trash-" + FileUtil.getUnixUID());
		} catch (IOException e) {
			throw new IOException("Could not determine user id while resolving trash folder: " + e.getMessage(), e);
		}
		if (verifyTrashFolder(trashFolder, true)) {
			return trashFolder;
		}
		LOGGER.debug("Unable to read or create trash folder \"{}\"", trashFolder);
		return null;
	}

	private static boolean verifyTrashStructure(Path trashPath) {
		String trashLocation = trashPath.toAbsolutePath().toString();
		return verifyTrashFolder(Paths.get(trashLocation, INFO), true) && verifyTrashFolder(Paths.get(trashLocation, FILES), true);
	}

	/**
	 * Moves the specified {@link File} to the trash bin.
	 *
	 * @param file the {@link File} to move to the trash bin.
	 * @throws InvalidFileSystemException If the file system doesn't support the
	 *             operation.
	 * @throws IOException If an error occurs during the operation.
	 * @throws IllegalArgumentException If {@code file} is {@code null}.
	 */
	public static void moveToTrash(@Nonnull File file) throws InvalidFileSystemException, IOException {
		moveToTrash(file.toPath());
	}

	/**
	 * Moves the specified {@link Path} to the trash bin.
	 *
	 * @param path the {@link Path} to move to the trash bin.
	 * @throws InvalidFileSystemException If the file system doesn't support the
	 *             operation.
	 * @throws IOException If an error occurs during the operation.
	 * @throws IllegalArgumentException If {@code path} is {@code null}.
	 */
	public static void moveToTrash(@Nonnull Path path) throws InvalidFileSystemException, IOException {
		if (path == null) {
			throw new IllegalArgumentException("path cannot be null");
		}

		final int limit = 10;
		path = path.toAbsolutePath();
		FilePermissions pathPermissions = new FilePermissions(path);
		if (!pathPermissions.isReadable() || !pathPermissions.isWritable()) {
			throw new IOException("Insufficient permission to delete \"" + path.toString() + "\" - move to trash bin failed");
		}

		Path trashFolder = getTrashFolder(path);
		Path infoFolder = trashFolder.resolve(INFO);
		Path filesFolder = trashFolder.resolve(FILES);
		if (!verifyTrashFolder(infoFolder, true) || !verifyTrashFolder(filesFolder, true)) {
			throw new IOException(
				"Could not move \"" + path.toString() + "\" to trash bin because " +
				"of insufficient permissions for trash bin \"" + trashFolder.toString() + "\""
			);
		}

		// Create the trash info
		List<String> infoContent = new ArrayList<>();
		infoContent.add("[Trash Info]");
		infoContent.add("Path=" + URLEncoder.encode(path.toString(), Charset.defaultCharset().name()));
		infoContent.add("DeletionDate=" + new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(new Date()));

		// Create the trash info file
		Path infoFile = path.getFileName();
		String fileName = infoFile != null ? infoFile.toString() : "";
		int count = 0;
		boolean created = false;
		while (!created && count < limit) {
			infoFile = infoFolder.resolve(fileName + ".trashinfo");
			try {
				Files.write(infoFile, infoContent, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
				created = true;
			} catch (IOException e) {
				fileName = generateRandomFileName(fileName);
				count++;
			}
		}
		if (!created) {
			throw new IOException("Could not find a target filename for \"" + path.toString() + "\" in trash bin");
		}

		Path targetPath = filesFolder.resolve(fileName);
		if (Files.exists(targetPath)) {
			throw new IOException(
				"Could not move \"" + path.toString() + "\" to trash bin since the trash bin \"" +
				trashFolder.toString() + "\" is corrupted"
			);
		}

		// Move the actual files
		Files.move(path, targetPath, StandardCopyOption.ATOMIC_MOVE);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("{}{}\" moved to \"{}\"", Files.isDirectory(path) ? "Folder \"" : "File \"", path, targetPath);
		}
	}

	/**
	 * Tries to determine if FreeDesktop.org Trash specification is
	 * applicable for the given {@link Path}. Support can vary between
	 * partitions on the same computer. Please note that this check will
	 * look for or try to create the necessary folder structure, so this can
	 * be an expensive operation.
	 *
	 * The check could be used to evaluate a systems general ability, but a
	 * better strategy is to attempt {@link #moveToTrash(Path)} and handle
	 * the {@link Exception} if it fails.
	 *
	 * @param path the path for which to evaluate trash bin support
	 * @return The evaluation result
	 */
	public static boolean hasTrash(Path path) {
		try {
			return verifyTrashStructure(getTrashFolder(path));
		} catch (IOException | InvalidFileSystemException e) {
			LOGGER.warn("Error while trying to evaluate trash bin support: " + e.getMessage());
			LOGGER.trace("", e);
			return false;
		}
	}

	/**
	 * Tries to determine if FreeDesktop.org Trash specification is applicable
	 * for the given {@link File}. Support can vary between partitions on the
	 * same computer. Please note that this check will look for or try to create
	 * the necessary folder structure, so this can be an expensive operation.
	 * <p>
	 * The check could be used to evaluate a systems general ability, but a
	 * better strategy is to attempt {@link #moveToTrash(File)} and handle the
	 * {@link Exception} if it fails.
	 *
	 * @param file the {@link File} for which to evaluate trash bin support.
	 * @return The evaluation result.
	 */
	public static boolean hasTrash(File file) {
		return hasTrash(file.toPath());
	}

	/**
	 * Tries to determine if FreeDesktop.org Trash specification is
	 * applicable for the system root. Support can vary between partitions
	 * on the same computer. Please note that this check will look for or
	 * try to create the necessary folder structure, so this can be an
	 * expensive operation.
	 * <p>
	 * The check could be used to evaluate a systems general ability, but a
	 * better strategy is to attempt {@link #moveToTrash(File)} and handle
	 * the {@link Exception} if it fails.
	 *
	 * @return The evaluation result
	 */
	@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
	public static boolean hasTrash() {
		return hasTrash(Paths.get("/"));
	}
}
