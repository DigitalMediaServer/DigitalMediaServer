/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2016 Digital Media Server developers.
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


/**
 * Defines a set of executable types for selecting the correct executable path:
 * {@link #Bundled}, {@link #Installed}, {@link #Custom} and {@link #Unknown}.
 * <p/>
 * This class is immutable, final and cannot be sub-classed.
 * </p>
 */
public final class ProgramExecutableType {

	public static final int BUNDLED_INT = 0;
	public static final int INSTALLED_INT = 10;
	public static final int CUSTOM_INT = 20;
	public static final int UNKNOWN_INT = Integer.MIN_VALUE;

	public static final Integer BUNDLED_INTEGER = BUNDLED_INT;
	public static final Integer INSTALLED_INTEGER = INSTALLED_INT;
	public static final Integer CUSTOM_INTEGER = CUSTOM_INT;
	public static final Integer UNKNOWN_INTEGER = UNKNOWN_INT;

	/**
	 * <code>BUNDLED</code> is for executables bundled with DMS.
	 */
	public static final ProgramExecutableType BUNDLED = new ProgramExecutableType(BUNDLED_INT, "Bundled");

	/**
	 * <code>INSTALLED</code> is for executables reachable via the OS path.
	 */
	public static final ProgramExecutableType INSTALLED = new ProgramExecutableType(INSTALLED_INT, "Installed");

	/**
	 * <code>CUSTOM</code> is for custom defined executable paths.
	 */
	public static final ProgramExecutableType CUSTOM = new ProgramExecutableType(CUSTOM_INT, "Custom");

	/**
	 * Use <code>UNKNOWN</code> is if the {@link ProgramExecutableType} is unknown.
	 */
	public static final ProgramExecutableType UNKNOWN = new ProgramExecutableType(UNKNOWN_INT, "Unknown");

	public final int executableTypeInt;
	public final String executableTypeStr;

	/**
	 * Instantiate a {@link ProgramExecutableType} object.
	 */
	private ProgramExecutableType(int executableTypeInt, String executableTypeStr) {
		this.executableTypeInt = executableTypeInt;
		this.executableTypeStr = executableTypeStr;
	}

	/**
	 * Returns the string representation of this {@link ProgramExecutableType}.
	 */
	public String toString() {
		return executableTypeStr;
	}

	/**
	 * Returns the integer representation of this {@link ProgramExecutableType}.
	 */
	public int toInt() {
		return executableTypeInt;
	}

	/**
	 * Convert a {@link ProgramExecutableType} to an {@link Integer} object.
	 *
	 * @return This {@link ProgramExecutableType}'s Integer mapping.
	 */
	public Integer toInteger() {
		switch (executableTypeInt) {
			case BUNDLED_INT:
				return BUNDLED_INTEGER;
			case INSTALLED_INT:
				return INSTALLED_INTEGER;
			case CUSTOM_INT:
				return CUSTOM_INTEGER;
			default:
				return UNKNOWN_INTEGER;
		}
	}

	/**
	 * Convert the string passed as argument to a {@link ProgramExecutableType}. If the conversion
	 * fails, then this method returns {@link #UNKNOWN}.
	 */
	public static ProgramExecutableType toProgramExecutableType(String sArg) {
		return toProgramExecutableType(sArg, ProgramExecutableType.UNKNOWN);
	}

	/**
	 * Convert an integer passed as argument to a {@link ProgramExecutableType}. If the conversion
	 * fails, then this method returns {@link #UNKNOWN}.
	 */
	public static ProgramExecutableType toProgramExecutableType(int val) {
		return toProgramExecutableType(val, ProgramExecutableType.UNKNOWN);
	}

	/**
	 * Convert an integer passed as argument to a {@link ProgramExecutableType}. If the conversion
	 * fails, then this method returns the specified default.
	 */
	public static ProgramExecutableType toProgramExecutableType(int val, ProgramExecutableType defaultExecutableType) {
		switch (val) {
			case BUNDLED_INT:
				return BUNDLED;
			case INSTALLED_INT:
				return INSTALLED;
			case CUSTOM_INT:
				return CUSTOM;
			case UNKNOWN_INT:
				return UNKNOWN;
			default:
				return defaultExecutableType;
		}
	}

	/**
	 * Convert the string passed as argument to a {@link ProgramExecutableType}.
	 * If the conversion fails, then this method returns the specified default.
	 */
	public static ProgramExecutableType toProgramExecutableType(String sArg, ProgramExecutableType defaultExecutableType) {
		if ("Bundled".equalsIgnoreCase(sArg)) {
			return ProgramExecutableType.BUNDLED;
		}
		if ("Installed".equalsIgnoreCase(sArg)) {
			return ProgramExecutableType.INSTALLED;
		}
		if ("Custom".equalsIgnoreCase(sArg)) {
			return ProgramExecutableType.CUSTOM;
		}
		if ("Unknown".equalsIgnoreCase(sArg)) {
			return ProgramExecutableType.UNKNOWN;
		}
		return defaultExecutableType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + executableTypeInt;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProgramExecutableType)) {
			return false;
		}
		ProgramExecutableType other = (ProgramExecutableType) obj;
		if (executableTypeInt != other.executableTypeInt) {
			return false;
		}
		return true;
	}
}
