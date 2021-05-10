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

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


/**
 * An implementation of a referenced {@code null}-terminated single byte encoded
 * C string.
 */
public class ByteStringByReference extends PointerType {

	/**
	 * Creates an unallocated {@link ByteStringByReference}.
	 */
	public ByteStringByReference() {
		super();
	}

	/**
	 * Creates a {@link ByteStringByReference} and allocates space for {
	 * {@code dataSize} plus the size of the {@code null} terminator.
	 *
	 * @param dataSize the size to allocate in bytes excluding the {@code null}
	 *            terminator.
	 */
	public ByteStringByReference(long dataSize) {
		super(dataSize < 1 ? Pointer.NULL : new Memory(dataSize + 1));
		if (dataSize > 0L) {
			getPointer().setMemory(0L, 1L, (byte) 0);
		}
	}

	/**
	 * Creates a {@link ByteStringByReference} containing {@code value}
	 * allocated to {@code value}'s byte length encoded with {@code UTF-8}.The
	 * new {@link ByteStringByReference} will be encoded with {@code UTF-8}.
	 *
	 * @param value the string content.
	 */
	public ByteStringByReference(String value) {
		super();
		if (value != null) {
			try {
				setValue(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError("UTF-8 is always allowed");
			}
		}
	}

	/**
	 * Creates a {@link ByteStringByReference} containing {@code value} allocated
	 * to {@code value}'s byte length encoded with {@code charset}.
	 *
	 * @param value the string content.
	 * @param charset a supported 8-bit encoded {@link Charset} to use for
	 *            encoding.
	 * @throws UnsupportedEncodingException If the specified {@link Charset} is
	 *             a known multi-byte encoding.
	 */
	public ByteStringByReference(String value, Charset charset) throws UnsupportedEncodingException {
		super();
		if (value != null) {
			setValue(value, charset);
		}
	}

	/**
	 * Creates a {@link ByteStringByReference} containing {@code value}
	 * allocated to {@code value}'s byte length encoded with
	 * {@code charsetName}.
	 *
	 * @param value the string content.
	 * @param charsetName the name of a valid and supported 8-bit encoded
	 *            {@link Charset} to use for encoding.
	 * @throws UnsupportedEncodingException If the specified charset name is
	 *             blank of a known multi-byte encoding.
	 */
	public ByteStringByReference(String value, String charsetName) throws UnsupportedEncodingException {
		super();
		if (value != null) {
			setValue(value, charsetName);
		}
	}

	/**
	 * Sets this {@link ByteStringByReference}'s content to that of {@code value}
	 * using {@code UTF-8} encoding. If there's enough
	 * space in the already allocated memory, the content will be written there.
	 * If not a new area will be allocated and the {@link Pointer} updated.
	 *
	 * @param value the new string content.
	 */
	public void setValue(String value) {
		try {
			setValue(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is always allowed");
		}
	}

	/**
	 * Sets this {@link ByteStringByReference}'s content to that of
	 * {@code value}. If there's enough space in the already allocated memory,
	 * the content will be written there. If not a new area will be allocated
	 * and the {@link Pointer} updated.
	 *
	 * @param value the new string content.
	 * @param charset a supported 8-bit encoded {@link Charset} to use for
	 *            encoding.
	 * @throws UnsupportedEncodingException If the specified {@link Charset} is
	 *             a known multi-byte encoding.
	 */
	public void setValue(String value, Charset charset) throws UnsupportedEncodingException {
		setValue(value, charset.name());
	}

	/**
	 * Sets this {@link ByteStringByReference}'s content to that of
	 * {@code value}. If there's enough space in the already allocated memory,
	 * the content will be written there. If not a new area will be allocated
	 * and the {@link Pointer} updated.
	 *
	 * @param value the new string content.
	 * @param charsetName the name of a valid and supported 8-bit encoded
	 *            {@link Charset} to use for encoding.
	 * @throws UnsupportedEncodingException If the specified charset name is
	 *             blank of a known multi-byte encoding.
	 */
	public void setValue(String value, String charsetName) throws UnsupportedEncodingException {
		if (value == null) {
			setPointer(Pointer.NULL);
			return;
		}
		checkCharset(charsetName);
		byte[] bytes = value.getBytes(charsetName);
		if (bytes.length > getAllocatedSize()) {
			setPointer(new Memory(bytes.length + 1));
		}
		getPointer().write(0, bytes, 0, bytes.length);
		getPointer().setMemory(bytes.length, 1L, (byte) 0);
	}

	/**
	 * Gets this {@link ByteStringByReference}'s content decoded as
	 * {@code UTF-8}. If the content isn't UTF-8 encoded, the result might be an
	 * incorrect representation of the content.
	 *
	 * @return The content as a {@link String}.
	 */
	public String getValue() {
		try {
			return getValue(StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is always allowed");
		}
	}

	/**
	 * Gets this {@link ByteStringByReference}'s content using {@code charset}
	 * for decoding.
	 *
	 * @param charset a supported 8-bit encoded {@link Charset} to use for
	 *            decoding.
	 * @return The content as a {@link String}.
	 * @throws UnsupportedEncodingException If the specified {@link Charset} is
	 *             a known multi-byte encoding.
	 */
	public String getValue(Charset charset) throws UnsupportedEncodingException {
		return getValue(charset.name());
	}

	/**
	 * Gets this {@link ByteStringByReference}'s content using
	 * {@code charsetName} for decoding.
	 *
	 * @param charsetName the name of a valid and supported 8-bit encoded
	 *            {@link Charset} to use for decoding.
	 * @return The content as a {@link String}.
	 * @throws UnsupportedEncodingException If the specified charset name is
	 *             blank of a known multi-byte encoding.
	 */
	public String getValue(String charsetName) throws UnsupportedEncodingException {
		checkCharset(charsetName);
		return getPointer() == null ? null : getPointer().getString(0, charsetName);
	}

	/**
	 * Gets the size in bytes allocated to this {@link ByteStringByReference}
	 * excluding byte for the {@code null} terminator.
	 *
	 * @return The allocated size in bytes or {@code -1} if unknown.
	 */
	public long getAllocatedSize() {
		if (getPointer() instanceof Memory) {
			return Math.max(((Memory) getPointer()).size() - 1, 0);
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

	/**
	 * Checks the {@link Charset} name for the most common multi-byte encodings,
	 * and throws an {@link UnsupportedEncodingException} if the specified
	 * {@link Charset} name is one. This check isn't "complete", it's just meant
	 * as a help to avoid the most obvious "traps".
	 *
	 * @param charsetName the {@link Charset} name to check.
	 * @throws UnsupportedEncodingException
	 */
	protected void checkCharset(String charsetName) throws UnsupportedEncodingException {
		if (isBlank(charsetName)) {
			throw new UnsupportedEncodingException("A 8-bit encoded charset must be specified");
		}
		switch (charsetName.trim().toUpperCase(Locale.ROOT)) {
			case "UTF-16":
			case "UTF-16BE":
			case "UTF-16LE":
			case "UTF-32":
			case "UTF-32BE":
			case "UTF-32LE":
				throw new UnsupportedEncodingException("Multi-byte charsets are not allowed");
			default:
				break;
		}
	}

	@Override
	public String toString() {
		if (getPointer() == null) {
			return "null";
		}
		return getValue();
	}
}
