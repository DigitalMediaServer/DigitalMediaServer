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
package net.pms.logging;

import java.util.HashMap;

/*
 * This class is just a backwards compatible wrapper for LoggingConfig. It can
 * be deleted when deemed safe.
 */
/**
 * @deprecated Use {@link LoggingConfig} instead. Please not that the key
 * "debug.log" has changed name to "default.log" in {@link LoggingConfig}
 * and update your code accordingly. Also note that the main logfile path
 * can be retrieved by simply querying {@code PMS.getConfiguration().getLogFileName()}.
 *
 * @author Nadahar
 * @since 5.2.3
 */
@Deprecated
public class LoggingConfigFileLoader {

	/**
	 * @deprecated Use {@link LoggingConfig#getLogFilePaths()}
	 * @return
	 */
	@Deprecated
	public static HashMap<String, String> getLogFilePaths() {
		HashMap<String, String> logFilePaths = new HashMap<>();
		// Copy logFilePaths from LoggingConfig and change "default.log" to "debug.log" for backwards compatibility.
		logFilePaths.putAll(LoggingConfig.getLogFilePaths());
		if (logFilePaths.containsKey("default.log")) {
			String s = logFilePaths.get("default.log");
			logFilePaths.remove("default.log");
			logFilePaths.put("debug.log", s);
		}
		return logFilePaths;
	}

}
