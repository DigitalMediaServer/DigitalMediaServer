package net.pms.newgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconType;
import net.pms.newgui.components.AnimatedTreeCellRenderer;


public class TranscodingEngineCellRenderer extends AnimatedTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final AnimatedIconFrame[] AMBER_FLASHING_FRAMES;
	private static final ImageIcon CATEGORY_ICON = LooksFrame.readImageIcon("icon-treemenu-category.png");
	private static final ImageIcon ENGINE_OFF_ICON = LooksFrame.readImageIcon("symbol-light-treemenu-red-on.png");
	private static final ImageIcon ENGINE_ON_ICON = LooksFrame.readImageIcon("symbol-light-treemenu-green-on.png");
	private static final ImageIcon ENGINE_OFF_WARNING_ICON;

	static {
		ArrayList<AnimatedIconFrame> tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-amber-F%d.png", 0, 7, true, 15, 15, 15))
		);
		Icon amberOff = LooksFrame.readImageIcon("symbol-light-treemenu-amber-off.png");
		tempFrames.add(0, new AnimatedIconFrame(amberOff, 470, 530)); //TODO: (Nad) Adjust
		tempFrames.set(8, new AnimatedIconFrame(tempFrames.get(8).getIcon(), 800));
		ENGINE_OFF_WARNING_ICON = (ImageIcon) tempFrames.get(2).getIcon();
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		AMBER_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);
	}

	private final AnimatedIcon amberFlash;
	private AnimatedIcon testFlash;
	private final HashMap<EngineTreeNode, AnimatedIcon> warningIcons = new HashMap<>();

	public TranscodingEngineCellRenderer(JTree tree) {
		if (tree == null) {
			throw new IllegalArgumentException("tree cannot be null");
		}
		setBackgroundSelectionColor(new Color(57, 114, 147));
		setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		amberFlash = new AnimatedIcon(this, true, AMBER_FLASHING_FRAMES);
	}

	protected void setWarningIcon(@Nonnull EngineTreeNode engineNode) {
		AnimatedIcon icon;
		if (warningIcons.containsKey(engineNode)) {
			icon = warningIcons.get(engineNode);
		} else {
			icon = new AnimatedIcon(this, true, AMBER_FLASHING_FRAMES);
			warningIcons.put(engineNode, icon);
		}
		setIcon(engineNode, icon);
	}

	protected void setIcon(@Nonnull EngineTreeNode engineNode, @Nullable Icon icon) {
		Icon oldIcon = engineNode.getIcon();
		if (icon != oldIcon) {
			if (oldIcon instanceof AnimatedIcon) {
				((AnimatedIcon) oldIcon).stop();
				warningIcons.remove(engineNode);
			}
			if (icon instanceof AnimatedIcon) {
				((AnimatedIcon) icon).start();
			}
		}
		setIcon(icon);
		engineNode.setIcon(icon);
	}

	@Override
	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean sel,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus
	) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (testFlash == null) {
			testFlash = new AnimatedIcon(tree, true, AMBER_FLASHING_FRAMES);
			testFlash.start();
		}

		if (leaf && value instanceof EngineTreeNode) {
			EngineTreeNode engineNode = (EngineTreeNode) value;
			Player player = engineNode.getPlayer();
			if (player == null) {
				setIcon(engineNode, CATEGORY_ICON);
				setToolTipText(null);
			} else {
				if (player.isEnabled()) {
					if (player.isAvailable()) {
						setIcon(engineNode, ENGINE_ON_ICON);
					} else {
						setWarningIcon(engineNode);
					}
				} else {
					if (player.isAvailable()) {
						setIcon(engineNode, ENGINE_OFF_ICON);
					} else {
						setIcon(engineNode, ENGINE_OFF_WARNING_ICON);
					}
				}
				setToolTipText(player.getStatusText());
			}

			if (player != null && ((EngineTreeNode) value).getParent().getIndex((EngineTreeNode) value) == 0) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}
		} else {
			setIcon(CATEGORY_ICON);
		}
//		setIcon(testFlash);

		return this;
	}
}
