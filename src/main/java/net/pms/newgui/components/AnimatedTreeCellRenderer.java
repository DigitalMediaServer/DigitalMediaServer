package net.pms.newgui.components;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.swing.JTree;
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

	public static class NodeImageObserver implements ImageObserver {

		JTree tree;

		TreeModel model;

		TreeNode node;

		NodeImageObserver(JTree tree, TreeNode node) {
			this.tree = tree;
			this.model = tree.getModel();
			this.node = node;
		}

		@Override
		public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
			if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
				TreePath path = new TreePath(((DefaultTreeModel) model).getPathToRoot(node));
				Rectangle rect = tree.getPathBounds(path);
				if (rect != null) {
					tree.repaint(rect);
				}
			}
			return (flags & (ALLBITS | ABORT)) == 0;
		}
	}

}
