package net.pms.newgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconType;
import net.pms.newgui.components.AnimatedTreeCellRenderer;


public class TranscodingEngineCellRenderer extends AnimatedTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final AnimatedIconFrame[] AMBER_FLASHING_FRAMES;
	private static final AnimatedIconFrame[] AMBER_OFF_FRAME;
	private static final AnimatedIconFrame[] RED_ON_FRAME;
	private static final AnimatedIconFrame[] GREEN_ON_FRAME;

	static {
		ArrayList<AnimatedIconFrame> tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation("symbol-light-treemenu-amber-F%d.png", 0, 7, true, 15, 15, 15)));
		Icon amberOff = LooksFrame.readImageIcon("symbol-light-treemenu-amber-off.png");
		tempFrames.add(0, new AnimatedIconFrame(amberOff, 170, 530)); //TODO: (Nad) Adjust
		tempFrames.set(8, new AnimatedIconFrame(tempFrames.get(8).getIcon(), 800));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		AMBER_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);
		AMBER_OFF_FRAME = AnimatedIcon.buildAnimation(amberOff);
		RED_ON_FRAME = AnimatedIcon.buildAnimation("symbol-light-treemenu-red-on.png");
		GREEN_ON_FRAME = AnimatedIcon.buildAnimation("symbol-light-treemenu-green-on.png");
	}

	private final AnimatedIcon amberFlash;
	private final AnimatedIcon amberOff;
	private final AnimatedIcon redOn;
	private final AnimatedIcon greenOn;
	private AnimatedIcon testFlash;

	public TranscodingEngineCellRenderer(JTree tree) {
		if (tree == null) {
			throw new IllegalArgumentException("tree cannot be null");
		}
		setBackgroundSelectionColor(new Color(57, 114, 147));
		setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		amberFlash = new AnimatedIcon(tree, true, AMBER_FLASHING_FRAMES);
		amberOff = new AnimatedIcon(tree, false, AMBER_OFF_FRAME);
		redOn = new AnimatedIcon(tree, false, RED_ON_FRAME);
		greenOn = new AnimatedIcon(tree, false, GREEN_ON_FRAME);
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

//		if (leaf && value instanceof TreeNodeSettings) {
//			((ImageIcon) testFlash).setImageObserver(new NodeImageObserver(tree, value));
//		}


		if (leaf && value instanceof EngineTreeNode) {
			EngineTreeNode engineNode = (EngineTreeNode) value;
			Player player = engineNode.getPlayer();
			if (player == null) {
				setIcon(LooksFrame.readImageIcon("icon-treemenu-category.png"));
				setToolTipText(null);
			} else {
				if (player.isEnabled()) {
					if (player.isAvailable()) {
//						currentIcon = amberFlash;
//						amberFlash.start();
//						setIcon(amberFlash); //TODO: (Nad) Treemenu
//						setIcon(testFlash);
						AnimatedIcon warningIcon = engineNode.getWarningIcon();
						if (engineNode.getWarningIcon() == null) {
							warningIcon = new AnimatedIcon(this, true, AMBER_FLASHING_FRAMES);
							engineNode.setWarningIcon(warningIcon);
						}
						setIcon(warningIcon);
					} else {
//						currentIcon = amberFlash;
//						amberFlash.start();
//						setIcon(amberFlash);
					}
				} else {
//					if (currentIcon == amberFlash) {
//						amberFlash.stop();
//					}
					if (player.isAvailable()) {
//						currentIcon = amberOff;
						setIcon(amberOff);
					} else {
//						currentIcon = amberOff;
						setIcon(amberOff);
					}
				}
				setToolTipText(player.getStatusText());
			}

//			if (player != null && ((TreeNodeSettings) value).getParent().getIndex((TreeNodeSettings) value) == 0) { //TODO: (Nad) Temp
//				setFont(getFont().deriveFont(Font.BOLD));
//			} else {
//				setFont(getFont().deriveFont(Font.PLAIN));
//			}
		} else {
			setIcon(LooksFrame.readImageIcon("icon-treemenu-category.png"));
		}
//		setIcon(testFlash);

		return this;
	}
}
