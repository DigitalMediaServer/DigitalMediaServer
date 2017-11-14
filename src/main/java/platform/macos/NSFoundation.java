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
package platform.macos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.pms.util.jna.JnaIntEnum;
import net.pms.util.jna.JnaIntEnumConverter;
import net.pms.util.jna.JnaLongEnum;
import net.pms.util.jna.JnaLongEnumConverter;
import net.pms.util.jna.macos.corefoundation.CoreFoundation.CFTypeRef;
import net.pms.util.jna.macos.kernreturn.KernReturnT;
import net.pms.util.jna.macos.kernreturn.KernReturnTConverter;

/**
 * Partial mapping of Apple's NS (NextStep)/Cocoa Foundation framework.
 *
 * Most mappings are from
 * <ul>
 * <li>NSObject.h</li>
 * <li>CFBase.h</li> //TODO: (Nad) Cleanup
 * <li>CFData.h</li>
 * <li>CFDictionary.h</li>
 * <li>CFNumber.h</li>
 * <li>CFString.h</li>
 * </ul>
 *
 * None of the above are fully mapped.
 *
 * @author Nadahar
 */
@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
@SuppressWarnings({
	"checkstyle:ConstantName",
	"checkstyle:MethodName",
	"checkstyle:ParameterName",
	"checkstyle:LineLength"
})
public interface NSFoundation extends Library {

	/**
	 * A {@link Map} of library options for use with {@link Native#loadLibrary}.
	 */
	Map<String, Object> options = Collections.unmodifiableMap(new HashMap<String, Object>() {
		private static final long serialVersionUID = 1L;
		{
			put(Library.OPTION_TYPE_MAPPER, new DefaultTypeMapper() {
				{
					addTypeConverter(JnaIntEnum.class, new JnaIntEnumConverter()); //TODO: (Nad) Remove not needed
					addTypeConverter(JnaLongEnum.class, new JnaLongEnumConverter());
					addTypeConverter(KernReturnT.class, new KernReturnTConverter());
				}
			});
		}
	});

	/**
	 * The static {@link NSFoundation} instance.
	 */
	NSFoundation INSTANCE = (NSFoundation) Native.loadLibrary("/System/Library/Frameworks/Foundation.framework/Resources/BridgeSupport/Foundation.dylib", NSFoundation.class, options);
//	NSFoundation INSTANCE = (NSFoundation) Native.loadLibrary("Foundation.framework/Resources/BridgeSupport/Foundation.dylib", NSFoundation.class, options);
//	NSFoundation INSTANCE = (NSFoundation) Native.loadLibrary("Foundation", NSFoundation.class, options);

	// After using a CFBridgingRetain on an NSObject, the caller must take responsibility for calling CFRelease at an appropriate time.
	@Nullable
	CFTypeRef CFBridgingRetain(@Nullable Pointer x);

	@Nullable
	Pointer CFBridgingRelease(@Nullable CFTypeRef x);
	
	Pointer NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory directory, long domainMask, boolean expandTild);
}
