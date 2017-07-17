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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


/**
 * Represents a reference to the {@code mach_port_t} type.
 */
public class MachPortTRef extends PointerType {

	/**
	 * Creates a new {@link MachPortTRef} without allocating a {@link Pointer};
	 * the internal {@link Pointer} is {@code null}. If you're going to use this
	 * instance as an argument that returns a value, use
	 * {@link #MachPortTRef(boolean)} and set {@code allocate} to {@code true}.
	 */
	public MachPortTRef() {
	}

	/**
	 * Creates a new {@link MachPortTRef}. If you're going to use this instance
	 * as an argument that returns a value, set {@code allocate} to {@code true}.
	 *
	 * @param allocate Whether to allocate {@link Memory} for the internal
	 *            {@link Pointer} or not. If {@code false} the internal
	 *            {@link Pointer} is set to {@code null}.
	 */
	public MachPortTRef(boolean allocate) {
		super(allocate ? new Memory(Native.LONG_SIZE) : Pointer.NULL);
	}

	/**
	 * Creates a new {@link MachPortTRef} from a {@link MachPortT}. Allocates
	 * {@link Memory} for the internal {@link Pointer} and puts the value from
	 * {@code port} in the allocated {@link Memory}.
	 *
	 * @param port the {@link MachPortT} to "convert" to a {@link MachPortTRef}.
	 */
	public MachPortTRef(MachPortT port) {
		this(port != null);
		if (port != null) {
			getPointer().setNativeLong(0, port);
		}
	}

	/**
	 * @return The {@link MachPortT} of this {@link MachPortTRef} or
	 *         {@code null} if the internal {@link Pointer} points to
	 *         {@code null}.
	 */
	public MachPortT getValue() {
		if (getPointer() == null) {
			return null;
		}
		return new MachPortT(getPointer().getNativeLong(0));
	}
}
