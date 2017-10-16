package net.pms.newgui.components;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconStage;

public class AnimatedTreeCellRenderer extends RepaintableTreeCellRenderer implements AnimatedComponent {

	private static final long serialVersionUID = 1L;
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

	/**
	 * @return {@code true} if painting and validation should work as normal,
	 *         {@code false} if it should be skipped.
	 */
	protected boolean isRefreshNeeded() {
		return true; //TODO: (Nad) Temp
//		if (isEnabled()) {
//			return getIcon() instanceof AnimatedIcon;
//		}
//		return getDisabledIcon() instanceof AnimatedIcon;
	}

	@Override
	public void invalidate() {
		if (isRefreshNeeded()) {
			super.invalidate();
		}
	}

	@Override
	public void validate() {
		if (isRefreshNeeded()) {
			super.validate();
		}
	}

	@Override
	public void revalidate() {
		if (isRefreshNeeded()) {
			super.revalidate();
		}
	}

	@Override
	public void repaint() {
		if (isRefreshNeeded()) {
			super.repaint();
		}
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		if (isRefreshNeeded()) {
			super.repaint(x, y, width, height);
		}
	}

	@Override
	public void repaint(long tm) {
		if (isRefreshNeeded()) {
			super.repaint(tm);
		}
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		if (isRefreshNeeded()) {
			super.repaint(tm, x, y, width, height);
		}
	}

	@Override
	public void repaint(Rectangle r) {
		if (isRefreshNeeded()) {
			super.repaint(r);
		}
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub //TODO: (Nad) Cleanup
		super.paint(g);
//		if (tree != null) {
//			System.err.println(getBounds());
//			tree.repaint(getBounds());
//		}
//		if (getIcon() instanceof AnimatedIcon && tree != null && tree.getModel() instanceof DefaultTreeModel) {
//			g.get
//			new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(node))
//
//		}

	}

	public JTree getTree() { //TODO: (Nad) Keep?
		return tree;
	}

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

	public void repaintAffectedNodes(@Nullable AnimatedIcon icon) {
		if (icon == null || tree == null) {
			return;
		}

		if (tree.getModel() != null && tree.getModel().getRoot() instanceof TreeNode) {
			repaintNode((TreeNode) tree.getModel().getRoot(), icon);
		}
	}
}
