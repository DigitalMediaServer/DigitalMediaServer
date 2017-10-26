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
package net.pms.newgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import net.pms.encoders.Player;
import net.pms.newgui.TranscodingTab.TranscodingTabListenerRegistrar;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.AnimatedTreeCellRenderer;


/**
 * A {@link javax.swing.tree.TreeCellRenderer} implementation customized to
 * render the transcoding engine {@link JTree} cells.
 *
 * @author Nadahar
 */
public class TranscodingEngineCellRenderer extends AnimatedTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final AnimatedIconFrame[] AMBER_FLASHING_FRAMES;
	private static final ImageIcon CATEGORY_ICON = LooksFrame.readImageIcon("icon-treemenu-category.png");
	private static final ImageIcon ENGINE_OFF_ICON = LooksFrame.readImageIcon("symbol-light-treemenu-green-off.png");
	private static final ImageIcon ENGINE_ON_ICON = LooksFrame.readImageIcon("symbol-light-treemenu-green-on.png");
	private static final ImageIcon ENGINE_OFF_WARNING_ICON;

	static {
		ArrayList<AnimatedIconFrame> tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-amber-F%d.png", 0, 7, true, 15, 15, 15))
		);
		ImageIcon amberOff = LooksFrame.readImageIcon("symbol-light-treemenu-amber-off.png");
		tempFrames.add(0, new AnimatedIconFrame(amberOff, 470, 530));
		tempFrames.set(8, new AnimatedIconFrame(tempFrames.get(8).getIcon(), 800));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		AMBER_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);
		ENGINE_OFF_WARNING_ICON = amberOff;
	}

	private final HashMap<EngineTreeNode, AnimatedIcon> warningIcons = new HashMap<>();
	@Nonnull
	private final TranscodingTabListenerRegistrar tabListenerRegistrar;

	/**
	 * Creates a new instance.
	 *
	 * @param tabListenerRegistrar the {@link TranscodingTabListenerRegistrar}.
	 */
	public TranscodingEngineCellRenderer(@Nonnull TranscodingTabListenerRegistrar tabListenerRegistrar) {
		if (tabListenerRegistrar == null) {
			throw new IllegalArgumentException("tabListenerRegistrar cannot be null");
		}
		this.tabListenerRegistrar = tabListenerRegistrar;
		setBackgroundSelectionColor(new Color(57, 114, 147));
		setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
	}

	/**
	 * Creates a new {@link AnimatedIcon} using {@link #AMBER_FLASHING_FRAMES}
	 * if necessary and sets that for the specified {@link EngineTreeNode}.
	 *
	 * @param engineNode the {@link EngineTreeNode} to set a warning icon for.
	 */
	protected void setWarningIcon(@Nonnull EngineTreeNode engineNode) {
		AnimatedIcon icon;
		if (warningIcons.containsKey(engineNode)) {
			icon = warningIcons.get(engineNode);
		} else {
			icon = new AnimatedIcon(
				this,
				true,
				AMBER_FLASHING_FRAMES
			);
			warningIcons.put(engineNode, icon);
			tabListenerRegistrar.register(icon);
		}
		setIcon(engineNode, icon);
	}

	/**
	 * Removes the specified {@link AnimatedIcon} from {@link #warningIcons} if
	 * it exists, and stops the animation if it is the last instance.
	 *
	 * @param engineNode the {@link EngineTreeNode} for which to remove the
	 *            association.
	 * @param icon the {@link AnimatedIcon} to remove.
	 */
	protected void removeWarningIcon(@Nonnull EngineTreeNode engineNode, @Nonnull AnimatedIcon icon) {
		warningIcons.remove(engineNode);
		if (!warningIcons.containsValue(icon)) {
			icon.stop();
			tabListenerRegistrar.unregister(icon);
		}
	}

	/**
	 * Sets the specified {@link Icon} for the specified {@link EngineTreeNode},
	 * handling starting and stopping of animations at the same time.
	 *
	 * @param engineNode the {@link EngineTreeNode} to set a warning icon for.
	 * @param icon the {@link Icon} to set.
	 */
	protected void setIcon(@Nonnull EngineTreeNode engineNode, @Nullable Icon icon) {
		Icon oldIcon = engineNode.getIcon();
		if (icon != oldIcon) {
			if (oldIcon instanceof AnimatedIcon) {
				removeWarningIcon(engineNode, (AnimatedIcon) oldIcon);
			}
			if (icon instanceof AnimatedIcon) {
				((AnimatedIcon) icon).startArm();
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
			setToolTipText(null);
			setFont(getFont().deriveFont(Font.PLAIN));
		}

		return this;
	}
}
