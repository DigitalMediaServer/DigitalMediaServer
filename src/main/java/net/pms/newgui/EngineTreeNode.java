package net.pms.newgui;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedTreeNode;

/**
 * This is a {@link DefaultMutableTreeNode} extension that implements
 * {@link AnimatedTreeNode} and provides the specialized functionality needed to
 * represent a {@link Player} in the "Transcoding Engine Tree".
 *
 * @author Nadahar
 */
@NotThreadSafe
public class EngineTreeNode extends DefaultMutableTreeNode implements AnimatedTreeNode {
	private static final long serialVersionUID = 1L;

	/** The {@link Player} represented by this {@link TreeNode} */
	protected final Player player;

	/**
	 * A custom configuration panel if the linked {@link Player} is {@code null}
	 * or doesn't provide one
	 */
	protected final JComponent customConfigurationPanel;

	/** An {@link Icon} used by the {@link AnimatedTreeNode} implementation */
	protected Icon icon;

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param nodeName the name of the node to be shown in the {@link JTree}.
	 * @param player the {@link Player} this {@link TreeNode} should represent,
	 *            if any.
	 * @param customConfigurationPanel a custom configuration panel if
	 *            {@code player} is {@code null} or doesn't provide one.
	 */
	public EngineTreeNode(@Nullable String nodeName, @Nullable Player player, @Nullable JComponent customConfigurationPanel) {
		super(nodeName);
		this.player = player;
		this.customConfigurationPanel = customConfigurationPanel;
	}

	/**
	 * @return The ID of this {@link TreeNode} or {@code null}.
	 */
	@Nullable
	public String id() {
		if (player != null) {
			return player.id().toString();
		} else if (customConfigurationPanel != null) {
			return Integer.toString(customConfigurationPanel.hashCode());
		} else {
			return null;
		}
	}

	/**
	 * @return The linked {@link Player} for this {@link TreeNode} or
	 *         {@code null}.
	 */
	@Nullable
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return The configuration panel linked to this {@link TreeNode} or
	 *         {@code null}.
	 */
	@Nullable
	public JComponent getConfigurationPanel() {
		if (player != null) {
			return player.getConfigurationPanel();
		} else if (customConfigurationPanel != null) {
			return customConfigurationPanel;
		} else {
			return null;
		}
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
}
