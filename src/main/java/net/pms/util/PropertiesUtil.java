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
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.util.FilePermissions.FileFlag;

public class PropertiesUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
	private static final String DEFAULT_PROFILE_FOLDER_NAME;

	/**
	 * General properties for the project.
	 */
	private static final PmsProperties PROJECT_PROPERTIES = new PmsProperties();

	static {
		try {
			// Read project properties resource file.
			PROJECT_PROPERTIES.loadFromResourceFile("/resources/project.properties");
			DEFAULT_PROFILE_FOLDER_NAME = PROJECT_PROPERTIES.get("project.profile.folder.name");
		} catch (IOException e) {
			throw new Error("Could not load \"/resources/project.properties\"", e);
		}
	}

	/**
	 * Returns the project properties object that is constructed from the
	 * "project.properties" file.
	 * <p>
	 * Note that in the Maven "test" phase (e.g. when running DMS from Eclipse)
	 * the file "src/test/resources/project.properties" is used, whereas in
	 * other phases, the file "src/main/resources/project.properties" (e.g. when
	 * packaging the final build) will be used.
	 *
	 * @return The properties object.
	 */
	public static PmsProperties getProjectProperties() {
		return PROJECT_PROPERTIES;
	}

	/**
	 * @return The default profile folder name (e.g "DigitalMediaServer"), not
	 *         the complete profile path.
	 */
	public static String getDefaultProfileFolderName() {
		return DEFAULT_PROFILE_FOLDER_NAME;
	}

	/**
	 * Resolves a file or folder path specified in the project properties. The
	 * property value can have multiple, comma separated entries. The first
	 * non-blank, valid entry that satisifies the specified {@link FileFlag}s
	 * will be returned. If none can be resolved, {@code defaultValue} will be
	 * returned.
	 *
	 * @param key the property key to use.
	 * @param baseFolder the folder from which to resolve the path or
	 *            {@code null} to use the current working folder.
	 * @param defaultValue the default {@link Path} to return if none cane be
	 *            resolved.
	 * @param checkFlags the {@link Set} of {@link FileFlag} that is required to
	 *            be true for the file or folder to resolve.
	 * @param createFolder if {@code true} and {@link FileFlag#FOLDER} is
	 *            specified in {@code checkFlags} and no folder can be resolved,
	 *            an attempt to create the folder will be made.
	 * @param options the {@link LinkOption}s to use when resolving paths.
	 * @return The resolved or default {@link Path}.
	 */
	@Nullable
	public static Path resolvePropertiesPathEntry(
		@Nonnull String key,
		@Nullable Path baseFolder,
		@Nullable Path defaultValue,
		@Nullable Set<FileFlag> checkFlags,
		boolean createFolder,
		@Nullable LinkOption... options
	) {
		return resolvePropertiesPathEntry(key, baseFolder, defaultValue, checkFlags, null, createFolder, options);
	}

	/**
	 * Resolves a file or folder path specified in the project properties. The
	 * property value can have multiple, comma separated entries. The first
	 * non-blank, valid entry that satisfies the specified {@link FileFlag}s
	 * will be returned. If none can be resolved, {@code defaultValue} will be
	 * returned.
	 *
	 * @param key the property key to use.
	 * @param baseFolder the folder from which to resolve the path or
	 *            {@code null} to use the current working folder.
	 * @param defaultValue the default {@link Path} to return if none cane be
	 *            resolved.
	 * @param checkFlags the {@link Set} of {@link FileFlag} that is required to
	 *            be true for the file or folder to resolve.
	 * @param replaceAlls a {@link Map} of source and target regular expressions
	 *            to be replaced in the property value.
	 * @param createFolder if {@code true} and {@link FileFlag#FOLDER} is
	 *            specified in {@code checkFlags} and no folder can be resolved,
	 *            an attempt to create the folder will be made.
	 * @param options the {@link LinkOption}s to use when resolving paths.
	 * @return The resolved or default {@link Path}.
	 */
	@Nullable
	public static Path resolvePropertiesPathEntry(
		@Nonnull String key,
		@Nullable Path baseFolder,
		@Nullable Path defaultValue,
		@Nullable Set<FileFlag> checkFlags,
		@Nullable Map<String, String> replaceAlls,
		boolean createFolder,
		@Nullable LinkOption... options
	) {
		if (baseFolder == null) {
			baseFolder = Paths.get("");
		}
		String value = PROJECT_PROPERTIES.get(key);
		if (replaceAlls != null) {
			for (Entry<String, String> replaceAll : replaceAlls.entrySet()) {
				value = value.replaceAll(replaceAll.getKey(), replaceAll.getValue());
			}
		}
		List<String> pathList = new ArrayList<>(Arrays.asList(value.split("\\s*,\\s*")));
		for (Iterator<String> iterator = pathList.iterator(); iterator.hasNext();) {
			String pathEntry = iterator.next();
			try {
				Path path = Paths.get(pathEntry.trim());
				if (!path.isAbsolute()) {
					path = baseFolder.resolve(path);
				}
				if (checkFlags == null || checkFlags.isEmpty()) {
					return path;
				}
				if (new FilePermissions(path, options).hasFlags(checkFlags)) {
					return path;
				}
			} catch (InvalidPathException e) {
				LOGGER.debug("Invalid properties path specified for \"{}\": \"{}\"", key, pathEntry);
				iterator.remove();
			} catch (FileNotFoundException e) {
				// Nothing to do, keep looking
			}
		}

		// Unable to resolve, create folder if appropriate

		if (createFolder && checkFlags != null && checkFlags.contains(FileFlag.FOLDER)) {
			for (String pathEntry : pathList) {
				if (isNotBlank(pathEntry)) {
					Path path = Paths.get(pathEntry.trim());
					if (!path.isAbsolute()) {
						path = baseFolder.resolve(path);
					}
					if (!Files.exists(path, options)) {
						try {
							Files.createDirectories(path);
							if (new FilePermissions(path, options).hasFlags(checkFlags)) {
								return path;
							}
							// The folder was created but without the required permissions, try to delete it again.
							try {
								Files.delete(path);
							} catch (IOException e) {
								LOGGER.error(
									"Folder \"{}\" was created but didn't satisfy the requirements ({}). " +
									"The attempt to delete unsatisfactory folder failed with: {}",
									path,
									StringUtil.createReadableCombinedString(checkFlags),
									e.getMessage()
								);
								LOGGER.trace("", e);
							}
						} catch (IOException e) {
							LOGGER.error("Could not create folder \"{}\": {}", path, e.getMessage());
							LOGGER.trace("", e);
						}
					}
				}
			}
		}

		return defaultValue;
	}
}
