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
package net.pms.util.jna.macos.types;

import com.sun.jna.NativeLong;


/**
 * Represents the {@code mach_port_t} type.
 */
public class MachPortT extends NativeLong {

	private static final long serialVersionUID = 1L;

	/**
	 * The {@link MachPortT} {@code null} value.
	 */
	public static final int MACH_PORT_NULL = 0;

	/**
	 * Creates a new instance with value {@code 0}.
	 */
	public MachPortT() {
		super(0, true);
	}

	/**
	 * Creates a new instance with value {@code value}.
	 *
	 * @param value the value of the new instance.
	 */
	public MachPortT(int value) {
		super(value, true);
	}

	/**
	 * Creates a new instance with value {@code value}.
	 *
	 * @param value the value of the new instance.
	 */
	public MachPortT(long value) {
		super(value, true);
	}

	/**
	 * Creates a new instance with value {@code value}.
	 *
	 * @param value the value of the new instance.
	 */
	public MachPortT(NativeLong value) {
		super(value.longValue(), true);
	}
}
