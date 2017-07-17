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

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


/**
 * This is a custom {@link HTMLEditorKit} that doesn't use the application
 * shared {@link StyleSheet}. If using the standard {@link HTMLEditorKit}
 * any changes to the {@link StyleSheet} will apply to all
 * {@link HTMLEditorKit}s in the application, practically rendering
 * {@link StyleSheet}s useless.
 *
 * @author Nadahar
 */
public class CustomHTMLEditorKit extends HTMLEditorKit{

	private static final long serialVersionUID = -4110333075630471497L;
	private StyleSheet customStyleSheet;

    @Override
    public void setStyleSheet(StyleSheet styleSheet) {
        customStyleSheet = styleSheet;
    }
    @Override
    public StyleSheet getStyleSheet() {
        if (customStyleSheet == null) {
            customStyleSheet = super.getStyleSheet();
        }
        return customStyleSheet;
    }
}
