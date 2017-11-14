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
package net.pms.platform.macos;

import net.pms.util.jna.JnaLongEnum;

/**
 * This represents the {@code NS_ENUM} with the same name.
 * <p>
 * Use {@link #getValue()} to convert a {@link NSSearchPathDirectory} to its
 * integer value. Use {@link #typeOf} to convert an integer value to a
 * {@link NSSearchPathDirectory}.
 */
public enum NSSearchPathDirectory implements JnaLongEnum<NSSearchPathDirectory> {

	/** Supported applications ({@code Applications}) */
	NSApplicationDirectory(1),

	/** Unsupported applications, demonstration versions ({@code Demos}) */
	NSDemoApplicationDirectory(2),

	/**
	 * Developer applications ({@code Developer/Applications}).
	 *
	 * @deprecated There is no one single Developer directory.
	 */
	NSDeveloperApplicationDirectory(3),

	/** System and network administration applications ({@code Administration}) */
	NSAdminApplicationDirectory(4),

	/**
	 * Various documentation, support, and configuration files, resources (
	 * {@code Library})
	 */
	NSLibraryDirectory(5),

	/**
	 * Developer resources ({@code Developer})
	 *
	 * @deprecated There is no one single Developer directory.
	 */
	NSDeveloperDirectory(6),

	/** User home directories ({@code Users}) */
	NSUserDirectory(7),

	/** Documentation ({@code Documentation}) */
	NSDocumentationDirectory(8),               //

	/** Documents ({@code Documents}) */
	NSDocumentDirectory(9),

	/**
	 * Location of {@code CoreServices} directory (
	 * {@code System/Library/CoreServices})
	 */
	NSCoreServiceDirectory(10),

	/**
	 * Location of autosaved documents ({@code Documents/Autosaved})
	 *
	 * @since OS X 10.6
	 */
	NSAutosavedInformationDirectory(11),

	/** Location of user's desktop */
	NSDesktopDirectory(12),

	/** Location of discardable cache files ({@code Library/Caches}) */
	NSCachesDirectory(13),

	/**
	 * Location of application support files (plug-ins, etc) (
	 * {@code Library/Application Support})
	 */
	NSApplicationSupportDirectory(14),

	/**
	 * Location of the user's "{@code Downloads}" directory
	 *
	 * @since OS X 10.5
	 */
	NSDownloadsDirectory(15),

	/**
	 * Input methods ({@code Library/Input Methods})
	 *
	 * @since OS X 10.6
	 */
	NSInputMethodsDirectory(16),

	/**
	 * Location of user's Movies directory ({@code ~/Movies})
	 *
	 * @since OS X 10.6
	 */
	NSMoviesDirectory(17),

	/**
	 * Location of user's Music directory ({@code ~/Music})
	 *
	 * @since OS X 10.6
	 */
	NSMusicDirectory(18),

	/**
	 * Location of user's Pictures directory ({@code ~/Pictures})
	 *
	 * @since OS X 10.6
	 */
	NSPicturesDirectory(19),

	/**
	 * Location of system's PPDs directory ({@code Library/Printers/PPDs})
	 *
	 * @since OS X 10.6
	 */
	NSPrinterDescriptionDirectory(20),

	/**
	 * Location of user's Public sharing directory ({@code ~/Public})
	 *
	 * @since OS X 10.6
	 */
	NSSharedPublicDirectory(21),

	/**
	 * Location of the PreferencePanes directory for use with System Preferences
	 * ({@code Library/PreferencePanes})
	 *
	 * @since OS X 10.6
	 */
	NSPreferencePanesDirectory(22),

	/**
	 * Location of the user scripts folder for the calling application (
	 * {@code ~/Library/Application Scripts/code-signing-id})
	 *
	 * @since OS X 10.8
	 */
	NSApplicationScriptsDirectory(23),

	/**
	 * For use with {@code NSFileManager}'s
	 * {@code URLForDirectory:inDomain:appropriateForURL:create:error:}
	 *
	 * @since OS X 10.6
	 */
	NSItemReplacementDirectory(99),

	/** All directories where applications can occur */
	NSAllApplicationsDirectory(100),

	/** All directories where resources can occur */
	NSAllLibrariesDirectory(101),

	/**
	 * Location of {@code Trash} directory
	 *
	 * @since OS X 10.8
	 */
	NSTrashDirectory(102);

	private final long value;

	private NSSearchPathDirectory(long value) {
		this.value = value;
	}

	/**
	 * @return The integer value of this {@link Enum}.
	 */
	@Override
	public long getValue() {
		return value;
	}

	/**
	 * Returns the instance corresponding to {@code value} or {@code null}
	 * if there is no corresponding instance.
	 *
	 * @param value the value to lookup.
	 * @return The {@link NSSearchPathDirectory} instance or {@code null}.
	 */
	public static NSSearchPathDirectory typeOf(long value) {
		for (NSSearchPathDirectory entry : NSSearchPathDirectory.values()) {
			if (value == entry.getValue()) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public NSSearchPathDirectory typeForValue(long value) {
		return typeOf(value);
	}
}
