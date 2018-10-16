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


/**
 * Utility class for the macOS Cocoa framework.
 *
 * @author Nadahar
 */
public class Cocoa {

	/** The {@code NSApplication} runtime instance */
	public static final Pointer NS_APPLICATION = ObjCRuntime.INSTANCE.objc_lookUpClass("NSApplication");

	/** The {@code NSRunningApplication} runtime instance */
	public static final Pointer NS_RUNNING_APPLICATION = ObjCRuntime.INSTANCE.objc_lookUpClass("NSRunningApplication");

	/** The {@code sharedApplication} instance */
	public static final Pointer SHARED_APPLICATION = new Pointer(
		ObjCRuntime.INSTANCE.objc_msgSend(NS_APPLICATION, Selector.get("sharedApplication"))
	);

	/** The {@code currentApplication} instance */
	public static final Pointer CURRENT_APPLICATION = new Pointer(
		ObjCRuntime.INSTANCE.objc_msgSend(NS_RUNNING_APPLICATION, Selector.get("currentApplication"))
	);

	/**
	 * Not to be instantiated.
	 */
	private Cocoa() {
	}

	/**
	 * Tells macOS to hide the application GUI.
	 */
	public static void hide() {
		ObjCRuntime.INSTANCE.objc_msgSend(SHARED_APPLICATION, Selector.get("hide:"));
	}

	/**
	 * Tells macOS to unhide the application GUI.
	 */
	public static void unhide() {
		ObjCRuntime.INSTANCE.objc_msgSend(SHARED_APPLICATION, Selector.get("unhide:"));
	}

	/**
	 * Attempts to activate the application using the specified options.
	 *
	 * @param options The {@link NSApplicationActivationOptions} to use when
	 *            activating the application.
	 */
	public static void activate(NSApplicationActivationOptions... options) {
		Object[] optionsValues = new Integer[options.length];
		for (int i = 0; i < options.length; i++) {
			optionsValues[i] = options[i].getValue();
		}
		ObjCRuntime.INSTANCE.objc_msgSend(CURRENT_APPLICATION, Selector.get("activateWithOptions:"), optionsValues);
	}

	/**
	 * This class represents the macOS {@code SEL} type.
	 *
	 * @author Nadahar
	 */
	public static class Selector extends Pointer {

		/**
		 * Creates a new {@code SEL} wrapper using the specified memory address.
		 *
		 * @param address the memory address where the {@code SEL} instance is
		 *            located.
		 */
		protected Selector(long address) {
			super(address);
		}

		/**
		 * Creates a {@code SEL} instance for the given selector name and wraps
		 * it as a {@link Selector}.
		 *
		 * @param selectorName the selector name to use.
		 * @return The new {@link Selector} or {@code null};
		 */
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

	/**
	 * This represents the {@code AppKit} enumeration with the same name.
	 */
	public static enum NSApplicationActivationOptions {

		/**
		 * By default, activation brings only the main and key windows forward.
		 * If you specify {@code NSApplicationActivateAllWindows}, all of the
		 * application's windows are brought forward.
		 */
		NSApplicationActivateAllWindows(1),

		/**
		 * By default, activation deactivates the calling application (assuming
		 * it was active), and then the new application is activated only if
		 * there is no currently active application. This prevents the new
		 * application from stealing focus from the user, if the application is
		 * slow to activate and the user has switched to a different application
		 * in the interim. However, if you specify
		 * {@code NSApplicationActivateIgnoringOtherApps}, the application is
		 * activated regardless of the currently active application, potentially
		 * stealing focus from the user. You should rarely pass this flag
		 * because stealing key focus produces a very bad user experience.
		 */
		NSApplicationActivateIgnoringOtherApps(1 << 1);

		private Integer value;

		private NSApplicationActivationOptions(int value) {
			this.value = Integer.valueOf(value);
		}

		/**
		 * @return The integer value.
		 */
		@Nonnull
		public Integer getValue() {
			return value;
		}
	}
}
