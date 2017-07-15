/*
 * Universal Media Server, for streaming any media to DLNA
 * compatible renderers based on the http://www.ps3mediaserver.org.
 * Copyright (C) 2012 UMS developers.
 *
 * This program is a free software; you can redistribute it and/or
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

package net.pms.configuration;

// a one-stop class for values and methods specific to custom PMS builds
public class Build {
	/**
	 * the name of the subdirectory under which PMS config files are stored for this build.
	 * the default value is "PMS" e.g.
	 *
	 *     Windows:
	 *
	 *         %ALLUSERSPROFILE%\PMS
	 *
	 *     Mac OS X:
	 *
	 *         /home/<username>/Library/Application Support/PMS
	 *
	 *     Linux &c.
	 *
	 *         /home/<username>/.config/PMS
	 *
	 * a custom build can change this to avoid interfering with the config files of other
	 * builds e.g.:
	 *
	 *     PROFILE_DIRECTORY_NAME = "PMS Rendr Edition";
	 *     PROFILE_DIRECTORY_NAME = "pms-mlx";
	 *
	 * Note: custom Windows builds that change this value should change the corresponding "$ALLUSERSPROFILE\PMS"
	 * value in nsis/setup.nsi
	 *
	 * @return The profile directory name
	 */

	private static final String PROFILE_DIRECTORY_NAME = "UMS";

	/**
	 * Returns the {@link #PROFILE_DIRECTORY_NAME} where configuration files
	 * for this version of PMS are stored.
	 *
	 * @return The profile directory name
	 */
	public static String getProfileDirectoryName() {
		return PROFILE_DIRECTORY_NAME;
	}
}
