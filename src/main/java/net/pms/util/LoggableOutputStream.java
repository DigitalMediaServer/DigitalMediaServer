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
package net.pms.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * This is an {@link OutputStream} implementation that stores everything that is
 * written in memory. This is useful for capturing what has been written to the
 * stream for example for logging, but will consume as much memory as has been
 * written. Use in moderation and only when needed.
 * <p>
 * The maximum size that can be written is
 * {@link ByteArrayOutputStream#MAX_ARRAY_SIZE}, which is
 * {@link Integer#MAX_VALUE} - 8. Writing past this will result in an
 * {@link OutOfMemoryError}, although available memory might be exhausted before
 * that point depending on the allocated heap size.
 *
 * @author Nadahar
 */
public class LoggableOutputStream extends OutputStream {

	/** The {@link Charset} used to convert the byte array to a {@link String} */
	protected final Charset logCharset;

	/** The target {@link OutputStream} */
	protected final OutputStream outputStream;

	/** The copy of what has been written to the {@link OutputStream} */
	protected final ByteArrayOutputStream logStream = new ByteArrayOutputStream();

	/**
	 * Creates a new instance wrapping the specified {@link OutputStream}.
	 * {@link #toString()} will be generated using
	 * {@link StandardCharsets#ISO_8859_1}.
	 *
	 * @param outputStream the {@link OutputStream} to write to.
	 */
	public LoggableOutputStream(OutputStream outputStream) {
		this(outputStream, null);
	}

	/**
	 * Creates a new instance wrapping the specified {@link OutputStream}.
	 *
	 * @param outputStream the {@link OutputStream} to write to.
	 * @param logCharset the {@link Charset} to use when generating
	 *            {@link #toString()}.
	 */
	public LoggableOutputStream(OutputStream outputStream, Charset logCharset) {
		this.outputStream = outputStream;
		if (logCharset == null) {
			this.logCharset = StandardCharsets.ISO_8859_1;
		} else {
			this.logCharset = logCharset;
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (outputStream != null) {
			outputStream.write(b);
		}
		logStream.write(b);
	}

	@Override
	public void close() throws IOException {
		if (outputStream != null) {
			outputStream.close();
		}
	}

	@Override
	public void flush() throws IOException {
		if (outputStream != null) {
			outputStream.flush();
		}
	}

	/**
	 * @return A copy of the bytes written this far.
	 */
	public byte[] getReadBytes() {
		return logStream.toByteArray();
	}

	@Override
	public String toString() {
		try {
			return logStream.toString(logCharset.name());
		} catch (UnsupportedEncodingException e) {
			return "UnsupportedEncodingException: " + e.getMessage();
		}
	}
}
