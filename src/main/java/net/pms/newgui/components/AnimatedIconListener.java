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
package net.pms.newgui.components;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconListenerRegistrar;

/**
 * An {@link AnimatedIconListener} is an implementation that can register
 * {@link AnimatedIconListenerAction}s and execute them in response to events.
 *
 * @param <T> the parameter type for the {@link AnimatedIconListenerAction}.
 *
 * @author Nadahar
 */
public interface AnimatedIconListener<T> {

	/**
	 * Registers an {@link AnimatedIconListenerAction} with this
	 * {@link AnimatedIconListener}.
	 *
	 * @param action the {@link AnimatedIconListenerAction} to register.
	 * @return {@code true} if the action was registered, {@code false}
	 *         otherwise.
	 */
	public boolean registerAction(@Nullable AnimatedIconListenerAction<T> action);

	/**
	 * Unregisters an {@link AnimatedIconListenerAction} with this
	 * {@link AnimatedIconListener}.
	 *
	 * @param action the {@link AnimatedIconListenerAction} to unregister.
	 * @return {@code true} if the action was unregistered, {@code false}
	 *         otherwise.
	 */
	public boolean unregisterAction(@Nullable AnimatedIconListenerAction<T> action);

	/**
	 * This class is an {@link AnimatedIconListenerRegistrar} implementation that registers
	 * {@link AnimatedIcon}s and suspends or unsuspends them on application
	 * minimize/restore events.
	 *
	 * @author Nadahar
	 */
	public static class MinimizeListenerRegistrar implements AnimatedIconListenerRegistrar {

		private final WindowIconifyListener listener;
		private final HashSet<AnimatedIcon> subscribers = new HashSet<>();
		private boolean isListening;
		private final AnimatedIconListenerAction<WindowMinimized> listenerAction = new AnimatedIconListenerAction<WindowMinimized>() {

			@Override
			public void executeAction(WindowMinimized value) {
				if (value == WindowMinimized.MINIMIZED) {
					for (AnimatedIcon subscriber : subscribers) {
						subscriber.suspend();
					}
				} else if (value == WindowMinimized.RESTORED) {
					for (AnimatedIcon subscriber : subscribers) {
						subscriber.unsuspend();
					}
				}
			}

		};

		/**
		 * Creates a new instance that listens to the specified {@link Window}.
		 *
		 * @param window the {@link Window} instance to listen to.
		 */
		public MinimizeListenerRegistrar(@Nonnull Window window) {
			if (window == null) {
				throw new IllegalArgumentException("window cannot be null");
			}
			listener = new WindowIconifyListener(window);
		}

		/**
		 * Registers the specified {@link AnimatedIcon} with a
		 * {@link WindowIconifyListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to register.
		 */
		public void register(@Nullable AnimatedIcon animatedIcon) {
			if (animatedIcon == null || subscribers.contains(animatedIcon)) {
				return;
			}
			subscribers.add(animatedIcon);
			if (!isListening) {
				listener.registerAction(listenerAction);
				isListening = true;
			}
		}

		/**
		 * Unregisters the specified {@link AnimatedIcon} with a
		 * {@link WindowIconifyListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to unregister.
		 */
		public void unregister(@Nullable AnimatedIcon animatedIcon) {
			if (animatedIcon != null && subscribers.remove(animatedIcon)) {
				if (isListening && subscribers.size() == 0) {
					listener.unregisterAction(listenerAction);
					isListening = false;
				}
			}
		}
	}

	/**
	 * This is an {@link AnimatedIconListener} implementation that listens to
	 * minimize/restore events and executes registered
	 * {@link AnimatedIconListenerAction}s in response to events.
	 *
	 * @author Nadahar
	 */
	public class WindowIconifyListener extends WindowAdapter implements AnimatedIconListener<WindowMinimized> {

		@Nonnull private final Window window;
		@Nonnull private final HashSet<AnimatedIconListenerAction<WindowMinimized>> actions = new HashSet<>();
		private boolean isListening;


		/**
		 * Creates a new instance using the specified {@link LooksFrame}
		 * instance.
		 *
		 * @param window the {@link Window} instance to listen to.
		 * @param actions (optional) one or more
		 *            {@link AnimatedIconListenerAction}s to add.
		 */
		@SafeVarargs
		public WindowIconifyListener(
			@Nonnull Window window,
			@Nullable AnimatedIconListenerAction<WindowMinimized>... actions
		) {
			if (window == null) {
				throw new IllegalArgumentException("window cannot be null");
			}
			this.window = window;
			if (actions != null && actions.length > 0) {
				this.actions.addAll(Arrays.asList(actions));
			}
		}

		@Override
		public boolean registerAction(@Nullable AnimatedIconListenerAction<WindowMinimized> action) {
			if (action == null) {
				return false;
			}
			boolean add = actions.add(action);
			if (add && !isListening) {
				window.addWindowListener(this);
				isListening = true;
			}
			return add;
		}

		@Override
		public boolean unregisterAction(@Nullable AnimatedIconListenerAction<WindowMinimized> action) {
			if (action == null) {
				return false;
			}
			boolean remove = actions.remove(action);
			if (remove && isListening && actions.size() == 0) {
				window.removeWindowListener(this);
				isListening = false;
			}
			return remove;
		}

		private void executeAction(WindowMinimized newState) {
			for (AnimatedIconListenerAction<WindowMinimized> action : actions) {
				action.executeAction(newState);
			}
		}

		@Override
		public void windowIconified(WindowEvent e) {
			executeAction(WindowMinimized.MINIMIZED);
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			executeAction(WindowMinimized.RESTORED);
		}
	}

	/**
	 * This {@code enum} represents a window "minimize value".
	 */
	public static enum WindowMinimized {

		/** The window was minimized */
		MINIMIZED,

		/** The window was restored */
		RESTORED
	}
}
