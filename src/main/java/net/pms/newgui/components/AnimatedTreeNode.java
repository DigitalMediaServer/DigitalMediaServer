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
import javax.swing.Icon;
import javax.swing.tree.TreeNode;


/**
 * This interface is a {@link TreeNode} extension that provides support for
 * {@link AnimatedIcon} for {@link TreeNode}s by storing some extra information.
 *
 * @author Nadahar
 */
public interface AnimatedTreeNode extends TreeNode {

	/**
	 * @return The {@link Icon} for this {@link TreeNode} or {@code null}.
	 */
	@Nullable
	public Icon getIcon();

	/**
	 * Sets the current icon. This doesn't affect rendering of the
	 * {@link TreeNode}, but it used to keep track of which {@link Icon} is
	 * currently active.
	 *
	 * @param icon the current {@link Icon} or {@code null} for no current
	 *            {@link Icon}.
	 */
	public void setIcon(@Nullable Icon icon);

	/**
	 * Returns the path from the root, to get to this node. The last element in
	 * the path is this node.
	 *
	 * @return An array of {@link TreeNode} objects giving the path, where the
	 *         first element in the path is the root and the last element is
	 *         this node.
	 */
	@Nullable
	public TreeNode[] getPath();
}
