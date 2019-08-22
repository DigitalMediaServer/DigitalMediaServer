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

import java.awt.Rectangle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconStage;

/**
 * This is a {@link RepaintableTreeCellRenderer} that implements
 * {@link AnimatedComponent} and thus the use of {@link AnimatedIcon}s.
 *
 * @author Nadahar
 */
public class AnimatedTreeCellRenderer extends RepaintableTreeCellRenderer implements AnimatedComponent {

	private static final long serialVersionUID = 1L;

	/**
	 * This isn't actually used but is needed for the {@link AnimatedComponent}
	 * implementation
	 */
	protected AnimatedIcon currentIcon = null;

	/**
	 * Creates a {@code AnimatedTreeCellRenderer}. Icons and text color are
	 * determined from the {@code UIManager}.
	 */
	public AnimatedTreeCellRenderer() {
	}

	@Override
	public void setNextIcon(AnimatedIconStage stage) {
		switch (stage.iconType) {
			case DISABLEDICON:
				setDisabledIcon(stage.icon);
				break;
			default:
				setIcon(stage.icon);
				break;
		}
	}

	@Override
	public AnimatedIcon getCurrentIcon() {
		return currentIcon;
	}

	@Override
	public void setCurrentIcon(AnimatedIcon icon) {
		currentIcon = icon;
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void validate() {
	}

	@Override
	public void revalidate() {
	}

	@Override
	public void repaint() {
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
	}

	@Override
	public void repaint(long tm) {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void repaint(Rectangle r) {
	}

	/**
	 * Repaints all instances of the specified {@link AnimatedIcon} in the
	 * specified {@link TreeNode} and child nodes recursively.
	 *
	 * @param node the start {@link TreeNode}.
	 * @param icon the {@link AnimatedIcon} to repaint.
	 */
	protected void repaintNode(@Nonnull TreeNode node, @Nonnull AnimatedIcon icon) {
		if (node instanceof AnimatedTreeNode) {
			AnimatedTreeNode animatedNode = (AnimatedTreeNode) node;
			if (animatedNode.getIcon() == icon) {
				TreeNode[] nodes = animatedNode.getPath();
				if (nodes != null && nodes.length > 0) {
					TreePath nodePath = new TreePath(nodes);
					Rectangle nodeRectangle = tree.getPathBounds(nodePath);
					if (nodeRectangle != null) {
						tree.repaint(nodeRectangle);
					}
				}
			}

		}
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			TreeNode childNode = node.getChildAt(i);
			if (childNode != null) {
				repaintNode(childNode, icon);
			}
		}
	}

	/**
	 * Traverses {@link RepaintableTreeCellRenderer#tree} and repaints all nodes
	 * using the specified {@link AnimatedIcon}.
	 *
	 * @param icon the {@link AnimatedIcon} to repaint across the {@link JTree}.
	 */
	public void repaintAffectedNodes(@Nullable AnimatedIcon icon) {
		if (icon == null || tree == null) {
			return;
		}

		if (tree.getModel() != null && tree.getModel().getRoot() instanceof TreeNode) {
			repaintNode((TreeNode) tree.getModel().getRoot(), icon);
		}
	}
}
