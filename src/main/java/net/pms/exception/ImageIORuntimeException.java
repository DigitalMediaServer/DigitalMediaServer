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
package net.pms.exception;

import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * An {@link IOException} that is a wrapper for {@link RuntimeException}s thrown
 * by {@link ImageIO}.
 * <p>
 * It is used to translate thrown {@link RuntimeException}s to
 * {@link IOException}s so they can be handled. This is needed because
 * {@link ImageIO} has the nasty habit of throwing {@link RuntimeException}s
 * when something goes wrong during an operation.
 *
 * @author Nadahar
 */
public class ImageIORuntimeException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with the specified code cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())}.
	 *
	 * @param cause the {@link RuntimeException} which caused this
	 *            {@link ImageIORuntimeException}.
	 */
	public ImageIORuntimeException(RuntimeException cause) {
		super(cause);
	}

	/**
	 * Creates a new exception with the specified detail message and cause.
	 * <p>
	 * <b>Note</b>: The detail message associated with {@code cause} is
	 * <b><i>not</i></b> automatically incorporated in this exception's detail
	 * message.
	 *
	 * @param message the detail message.
	 * @param cause the {@link RuntimeException} which caused this
	 *            {@link ImageIORuntimeException}.
	 */
	public ImageIORuntimeException(String message, RuntimeException cause) {
		super(message, cause);
	}
}
