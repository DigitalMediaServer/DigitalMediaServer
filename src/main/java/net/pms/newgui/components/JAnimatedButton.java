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

import java.net.URL;
import javax.swing.UIManager;
import net.pms.newgui.LooksFrame;


public class JAnimatedButton extends JImageButton {

	private static final long serialVersionUID = -8316312033513554308L;

	private AnimatedIcon currentIcon = null;

	/**
	 * Helps {@link AnimatedIcon} instances to stop other instances when the
	 * icon is changed. This is NOT thread safe.
	 *
	 * @return the previously painted {@link AnimatedIcon} or <code>null</code>
	 */
	public AnimatedIcon getCurrentIcon() {
		return currentIcon;
	}

	/**
	 * Sets the currently painted {@link AnimatedIcon}. This is NOT thread safe.
	 *
	 * @param icon the {@link AnimatedIcon} to set.
	 */
	public void setCurrentIcon(AnimatedIcon icon) {
		currentIcon = icon;
	}


	public JAnimatedButton(String text, AnimatedIcon icon) {
		super(text, icon);
	}

	public JAnimatedButton(AnimatedIcon icon) {
		super(icon);
	}

	public JAnimatedButton(String text, String iconName) {
		super(text, iconName);
	}

	public JAnimatedButton(String iconName) {
		super(iconName);
	}

	public JAnimatedButton() {
		super();
	}

	private AnimatedIcon readAnimatedIcon(String filename) {
		URL url = LooksFrame.class.getResource("/resources/images/" + filename);
		return url == null ? null : new AnimatedIcon(this, filename);
	}

	/**
	 * Set static icons from standard naming convention that is of type
	 * {@link AnimatedIcon}. While this can seem unnecessary it means
	 * that they can handle transitions to and from other (animated)
	 * {@link AnimatedIcon}s and thus be used on a {@link JAnimatedButton}.
	 *
	 * @param defaultIconName the base image resource name used when the
	 *                        button is in the normal state and which
	 *                        the other state names are derived from.
	 *
	 * @see JImageButton#setIcons(String)
	 */
	@Override
	protected void setIcons(String defaultIconName) {
		if (defaultIconName == null) {
			return;
		}

		AnimatedIcon icon = readAnimatedIcon(defaultIconName);
		if (icon == null) {
			setIcon(UIManager.getIcon("OptionPane.warningIcon"));
			return;
		}
		setIcon(icon);

		icon = readAnimatedIcon(appendToFileName(defaultIconName, "_pressed"));
		if (icon != null) {
			setPressedIcon(icon);
		}

		icon = readAnimatedIcon(appendToFileName(defaultIconName, "_disabled"));
		if (icon != null) {
			setDisabledIcon(icon);
		}

		icon = readAnimatedIcon(appendToFileName(defaultIconName, "_mouseover"));
		if (icon != null) {
			setRolloverIcon(icon);
		}
	}
}
