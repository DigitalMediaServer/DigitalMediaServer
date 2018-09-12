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
package net.pms.platform.macos;

import javax.annotation.Nonnull;
import com.drew.lang.annotations.Nullable;
import com.sun.jna.Pointer;


public class Cocoa {

	/** The {@code NSApplication} runtime instance */
	public static final Pointer NS_APPLICATION = ObjCRuntime.INSTANCE.objc_lookUpClass("NSApplication");

	/** The {@code sharedApplication} instance */
	public static final Pointer SHARED_APPLICATION = new Pointer(
		ObjCRuntime.INSTANCE.objc_msgSend(NS_APPLICATION, Selector.get("sharedApplication"))
	);

	/**
	 * Not to be instantiated.
	 */
	private Cocoa() {
	}

	public static void hide() {
		ObjCRuntime.INSTANCE.objc_msgSend(SHARED_APPLICATION, Selector.get("hide:"));
	}

	public static Pointer getSelector(String selectorName) {
		return ObjCRuntime.INSTANCE.sel_getUid(selectorName);
	}

	public static class Selector extends Pointer {

		protected Selector(long address) {
			super(address);
		}

		@Nullable
		public static Selector get(@Nonnull String selectorName) {
			Pointer selector = ObjCRuntime.INSTANCE.sel_getUid(selectorName);
			return selector == null ? null : new Selector(Pointer.nativeValue(selector));
		}

		@Override
		public String toString() {
			return ObjCRuntime.INSTANCE.sel_getName(this);
		}
	}
}
