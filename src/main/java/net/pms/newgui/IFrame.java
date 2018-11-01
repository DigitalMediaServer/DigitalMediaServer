/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.newgui;

import net.pms.configuration.RendererConfiguration;
import net.pms.newgui.StatusTab.ConnectionState;

public interface IFrame {
	void append(String msg);
	void updateBuffer();
	void setReadValue(long v, String msg);
	void setConnectionState(ConnectionState connectionState);
	void addRenderer(RendererConfiguration renderer);
	void updateRenderer(RendererConfiguration renderer);
	void webInterfaceEnabled(boolean value);
	void setReloadable(boolean reload);
	void addEngines();
	void setStatusLine(String line);
	void serverReady();
	void setScanLibraryEnabled(boolean flag);
}
