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

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


/**
 * An implementation of a referenced {@code null}-terminated UTF-16 string using
 * the native byte-order.
 */
public class UTF16StringByReference extends PointerType {

	/**
	 * Creates an unallocated {@link UTF16StringByReference}.
	 */
	public UTF16StringByReference() {
		super();
	}

	/**
	 * Creates a {@link UTF16StringByReference} and allocates space for {
	 * {@code dataSize} bytes plus the size of the {@code null} terminator.
	 *
	 * @param dataSize the size to allocate in bytes excluding the {@code null}
	 *            terminator.
	 */
	public UTF16StringByReference(long dataSize) {
		super(dataSize < 1L ? Pointer.NULL : new Memory(dataSize + 2L));
		if (dataSize > 0L) {
			getPointer().setMemory(0L, 2L, (byte) 0);
		}
	}

	/**
	 * Creates a {@link UTF16StringByReference} containing {@code value} allocated
	 * to {@code value}'s byte length in {@code UTF-16}.
	 *
	 * @param value the string content.
	 */
	public UTF16StringByReference(String value) {
		super();
		if (value != null) {
			setValue(value);
		}
	}

	/**
	 * Sets this {@link UTF16StringByReference}'s content to that of
	 * {@code value}. If there's enough space in the already allocated memory,
	 * the content will be written there. If not a new area will be allocated
	 * and the {@link Pointer} updated.
	 *
	 * @param value the new string content.
	 */
	public void setValue(String value) {
		if (value == null) {
			setPointer(Pointer.NULL);
			return;
		}
		byte[] bytes = value.getBytes(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
			StandardCharsets.UTF_16BE :
			StandardCharsets.UTF_16LE
		);
		if (bytes.length > getAllocatedSize()) {
			setPointer(new Memory(bytes.length + 2));
		}
		getPointer().write(0L, bytes, 0, bytes.length);
		getPointer().setMemory(bytes.length, 2L, (byte) 0);
	}

	/**
	 * Gets this {@link UTF16StringByReference}'s content.
	 *
	 * @return The content as a {@link String}.
	 */
	public String getValue() {
		if (getPointer() == null) {
			return null;
		}

		int allocated = (int) getAllocatedSize();
		if (allocated < 0) {
			return null;
		}
		if (allocated == 0) {
			return "";
		}
		byte[] bytes = new byte[allocated];
		getPointer().read(0, bytes, 0, allocated);

		int length = allocated;
		for (int i = 0; i < bytes.length - 1; i += 2) {
			if (bytes[i] == (byte) 0 && bytes[i + 1] == (byte) 0) {
				length = i;
				break;
			}
		}
		return new String(bytes, 0, length, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
			StandardCharsets.UTF_16BE :
			StandardCharsets.UTF_16LE
		);
	}

	/**
	 * Gets the size in bytes allocated to this {@link UTF16StringByReference}
	 * excluding byte for the {@code null} terminator.
	 *
	 * @return The allocated size in bytes or {@code -1} if unknown.
	 */
	public long getAllocatedSize() {
		if (getPointer() instanceof Memory) {
			return Math.max(((Memory) getPointer()).size() - 2, 0);
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
	 * Calculates the length in bytes of {@code string} as {@code UTF-16}.
	 *
	 * @param string the string to evaluate.
	 * @return the byte-length of {@code string}.
	 */
	public static int getNumberOfBytes(String string) {
		if (string == null) {
			return 0;
		}
		final int length = string.length();
		int byteLength = 0;
		int pointSize;
		for (int offset = 0; offset < length;) {
			int codePoint = string.codePointAt(offset);
			pointSize = Character.charCount(codePoint);
			byteLength += pointSize * 2;
			offset += pointSize;
		}
		return byteLength;
	}
}
