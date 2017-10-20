package net.pms.newgui;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import net.pms.Messages;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
public class EngineTreeNode extends DefaultMutableTreeNode implements AnimatedTreeNode {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineTreeNode.class);

	protected final Player player;
	protected final JComponent otherConfigPanel; //TODO: (Nad) Remove?
	protected JPanel warningPanel; //TODO: (Nad) Rename
	protected Icon icon;

	public EngineTreeNode(@Nullable String nodeName, @Nullable Player player, @Nullable JComponent otherConfigPanel) {
		super(nodeName);
		this.player = player;
		this.otherConfigPanel = otherConfigPanel;

	}

	@Nullable
	public String id() { //TODO: (Nad) PlayerID?
		if (player != null) {
			return player.id().toString();
		} else if (otherConfigPanel != null) {
			return "" + otherConfigPanel.hashCode();
		} else {
			return null;
		}
	}

	@Nullable
	public Player getPlayer() {
		return player;
	}

	@Nonnull
	public JComponent getConfigPanel() {
		if (player != null) {
			return player.config();
		} else if (otherConfigPanel != null) {
			return otherConfigPanel;
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
