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

import javax.annotation.Nullable;


/**
 * An interface for animated components supporting {@link AnimatedIcon}.
 *
 * @author Nadahar
 */
public interface AnimatedComponent extends AnimatedIconCallback {

	/**
	 * Helps {@link AnimatedIcon} instances to stop other instances when the
	 * active {@link AnimatedIcon} is changed.
	 *
	 * @return the previously painted {@link AnimatedIcon} or {@code null}.
	 */
	@Nullable
	public AnimatedIcon getCurrentIcon();

	/**
	 * Sets the currently painted {@link AnimatedIcon}.
	 *
	 * @param icon the {@link AnimatedIcon} to set.
	 */
	public void setCurrentIcon(AnimatedIcon icon);
}
