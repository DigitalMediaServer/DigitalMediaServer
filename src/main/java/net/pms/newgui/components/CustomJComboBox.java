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

import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JToolTip;

/**
 * A subclass of {@link javax.swing.JComboBox} with a custom <code>ToolTip</code> handler
 */

public class CustomJComboBox<E> extends JComboBox<E> {

	private static final long serialVersionUID = -45894969088130959L;

	public CustomJComboBox() {
		super();
	}

	public CustomJComboBox(ComboBoxModel<E> aModel) {
		super(aModel);
	}

	public CustomJComboBox(E[] items) {
		super(items);
	}

	public CustomJComboBox(Vector<E> items) {
	    super(items);
	}

	@Override
	public JToolTip createToolTip() {
	    JToolTip tip = new HyperLinkToolTip();
	    tip.setComponent(this);
	    return tip;
	}
}
