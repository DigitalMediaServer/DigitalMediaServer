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
package net.pms.util.jna;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


/**
 * An implementation of a referenced {@code null}-terminated wide string.
 */
public class WStringByReference extends PointerType {

	/**
	 * Creates an unallocated {@link WStringByReference}.
	 */
	public WStringByReference() {
		super();
	}

	/**
	 * Creates a {@link WStringByReference} and allocates space for {
	 * {@code dataSize} plus the size of the {@code null} terminator.
	 *
	 * @param dataSize the size to allocate in bytes excluding the {@code null}
	 *            terminator.
	 */
	public WStringByReference(long dataSize) {
		super(dataSize < 1 ? Pointer.NULL : new Memory(dataSize + Native.WCHAR_SIZE));
		if (dataSize > 0L) {
			getPointer().setMemory(0L, Native.WCHAR_SIZE, (byte) 0);
		}
	}

	/**
	 * Creates a {@link WStringByReference} containing {@code value} allocated
	 * to {@code value}'s byte length using {@code wchar_t}.
	 *
	 * @param value the string content.
	 */
	public WStringByReference(String value) {
		super();
		if (value != null) {
			setValue(value);
		}
	}

	/**
	 * Sets this {@link WStringByReference}'s content to that of {@code value}.
	 * If there's enough space in the already allocated memory, the content will
	 * be written there. If not a new area will be allocated and the
	 * {@link Pointer} updated.
	 *
	 * @param value the new string content.
	 */
	public void setValue(String value) {
		if (value == null) {
			setPointer(Pointer.NULL);
			return;
		}
		int length = getNumberOfBytes(value);
		if (length > getAllocatedSize()) {
			setPointer(new Memory(length + Native.WCHAR_SIZE));
		}
		getPointer().setWideString(0, value);
	}

	/**
	 * Gets this {@link WStringByReference}'s content.
	 *
	 * @return The content as a {@link String}.
	 */
	public String getValue() {
		return getPointer() == null ? null : getPointer().getWideString(0);
	}

	/**
	 * Gets the size in bytes allocated to this {@link WStringByReference}
	 * excluding byte for the {@code null} terminator.
	 *
	 * @return The allocated size in bytes or {@code -1} if unknown.
	 */
	public long getAllocatedSize() {
		if (getPointer() instanceof Memory) {
			return Math.max(((Memory) getPointer()).size() - Native.WCHAR_SIZE, 0);
		}
		return -1;
	}

	@Override
	public Object fromNative(Object nativeValue, FromNativeContext context) {
		// Always pass along null pointer values
		if (nativeValue == null) {
			return null;
		}
		setPointer((Pointer) nativeValue);
		return this;
	}

	@Override
	public String toString() {
		if (getPointer() == null) {
			return "null";
		}
		return getValue();
	}

	/**
	 * Calculates the length in bytes of {@code string} as a native
	 * {@code wstring}.
	 *
	 * @param string the string to evaluate.
	 * @return the byte-length of {@code string}.
	 */
	public static int getNumberOfBytes(String string) {
		if (string == null) {
			return 0;
		}
		if (Native.WCHAR_SIZE == 4) {
			return string.codePointCount(0, string.length()) * Native.WCHAR_SIZE;
		}
		final int length = string.length();
		int byteLength = 0;
		for (int offset = 0; offset < length;) {
			int codepoint = string.codePointAt(offset);
			byteLength += Character.charCount(codepoint) * Native.WCHAR_SIZE;
			offset += Character.charCount(codepoint);
		}
		return byteLength;
	}
}
