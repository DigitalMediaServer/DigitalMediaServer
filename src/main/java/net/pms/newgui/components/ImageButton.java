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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.UIManager;
import net.pms.newgui.LooksFrame;
import net.pms.util.FileUtil;


/**
 * This class is a {@link JButton} subclass that supports {@link Icon}
 * decoration of the button using a standardized naming convention.
 * <p>
 * When an icon name is specified, it is assumed to be the name of an image
 * resource, and additional button states are automatically set by looking for
 * images resources with the same name with a suffix added. The convention
 * states that:
 * <ul>
 * <li> {@code "_pressed"} is added to the "base" resource name for the pressed
 * button state.</li>
 * <li> {@code "_disabled"} is added to the "base" resource name for the disabled
 * button state.</li>
 * <li> {@code "_mouseover"} is added to the "base" resource name for the button
 * state when the mouse is over the button.</li>
 * </ul>
 *
 * @author Nadahar
 */
public class ImageButton extends JButton {

	private static final long serialVersionUID = 8120596501408171329L;

	/**
	 * Creates a new instance with the specified text and icon(s).
	 *
	 * @param text the button text.
	 * @param defaultIconName the base image resource name used when the button
	 *            is in the normal state and from which the other state names
	 *            are derived from.
	 */
	public ImageButton(String text, String defaultIconName) {
		super(text, null);
		setProperites();
		setIcons(defaultIconName);
	}

	/**
	 * Creates a new instance with the specified icon(s).
	 *
	 * @param defaultIconName the base image resource name used when the button
	 *            is in the normal state and from which the other state names
	 *            are derived from.
	 */
	public ImageButton(String defaultIconName) {
		this(null, defaultIconName);
	}

	/**
	 * Creates a new instance with no icons or text set.
	 */
	public ImageButton() {
		this(null, (String) null);
	}

	/**
	 * Creates a new instance with the specified {@link Icon} and text.
	 *
	 * @param text the text to use.
	 * @param icon the {@link Icon} to use.
	 */
	public ImageButton(String text, Icon icon) {
		super(text, icon);
		setProperites();
	}

	/**
	 * Creates a new instance with the specified {@link Icon}.
	 *
	 * @param icon the {@link Icon} to use.
	 */
	public ImageButton(Icon icon) {
		super(icon);
		setProperites();
	}

	/**
	 * Sets standard properties, used by constructors.
	 */
	protected void setProperites() {
		setRequestFocusEnabled(false);
		setBorderPainted(false);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setContentAreaFilled(false);
		setOpaque(false);
	}

	/**
	 * Set icons from standard naming convention based on a base image name.
	 *
	 * @param defaultIconName the base image resource name used when the button
	 *            is in the normal state and from which the other state names
	 *            are derived from.
	 */
	public void setIcons(String defaultIconName) {
		if (defaultIconName == null) {
			return;
		}

		ImageIcon icon = LooksFrame.readImageIcon(defaultIconName);
		if (icon == null) {
			setIcon(UIManager.getIcon("OptionPane.warningIcon"));
			return;
		}
		setIcon(icon);

		icon = LooksFrame.readImageIcon(FileUtil.appendToFileName(defaultIconName, "_pressed"));
		if (icon != null) {
			setPressedIcon(icon);
		}

		icon = LooksFrame.readImageIcon(FileUtil.appendToFileName(defaultIconName, "_disabled"));
		if (icon != null) {
			setDisabledIcon(icon);
		}

		icon = LooksFrame.readImageIcon(FileUtil.appendToFileName(defaultIconName, "_mouseover"));
		if (icon != null) {
			setRolloverIcon(icon);
		}
	}
}
