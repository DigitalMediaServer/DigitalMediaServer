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
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;


/**
 * Performs conversion between {@link JnaLongEnum} and native enums.
 *
 * @author Nadahar
 */
public class JnaLongEnumConverter implements TypeConverter {

	@Override
	public Object fromNative(Object input, FromNativeContext context) {
		if (!JnaLongEnum.class.isAssignableFrom(context.getTargetType())) {
			throw new IllegalStateException("JnaLongEnumConverter can only convert objects implementing JnaLongEnum");
		}
		@SuppressWarnings("rawtypes")
		Class targetClass = context.getTargetType();
		Object[] enumValues = targetClass.getEnumConstants();
		return ((JnaLongEnum<?>) enumValues[0]).typeForValue((long) input);
	}

	@Override
	public Class<Long> nativeType() {
		return Long.class;
	}

	@Override
	public Long toNative(Object input, ToNativeContext context) {
		return ((JnaLongEnum<?>) input).getValue();
	}
}
