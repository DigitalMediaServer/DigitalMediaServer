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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import net.pms.Messages;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
public class EngineTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineTreeNode.class);

	protected final Player player;
	protected final JComponent otherConfigPanel; //TODO: (Nad) Remove?
	protected JPanel warningPanel; //TODO: (Nad) Rename
	protected AnimatedIcon warningIcon;

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

	@Nullable
	public AnimatedIcon getWarningIcon() {
		return warningIcon;
	}

	public void setWarningIcon(AnimatedIcon icon) {
		warningIcon = icon;
	}

	@Nonnull
	public JComponent getConfigPanel() {
		if (player != null) {
			if (player.isAvailable()) {
				return player.config();
			}
			return getWarningPanel();
		} else if (otherConfigPanel != null) {
			return otherConfigPanel;
		} else {
			return new JPanel();
		}
	}

	@Nonnull
	private JPanel getWarningPanel() {
		if (warningPanel == null) {
			BufferedImage warningIcon = null;

			try {
				warningIcon = ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/icon-status-warning.png"));
			} catch (IOException e) {
				LOGGER.debug("Error reading icon-status-warning: ", e.getMessage());
				LOGGER.trace("", e);
			}

			ImagePanel iconPanel = new ImagePanel(warningIcon);

			FormLayout layout = new FormLayout(
				"10dlu, pref, 10dlu, pref:grow, 10dlu",
				"5dlu, pref, 3dlu, pref:grow, 5dlu"
			);

			FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.DIALOG).opaque(false);
			CellConstraints cc = new CellConstraints();

			builder.add(iconPanel).at(cc.xywh(2, 1, 1, 4, CellConstraints.CENTER, CellConstraints.TOP));

			JLabel warningLabel = new JLabel(Messages.getString("TreeNodeSettings.4"));
			builder.add(warningLabel).at(cc.xy(4, 2, CellConstraints.LEFT, CellConstraints.CENTER));
			warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD));

			if (isNotBlank(player.getStatusText())) {
				JTextArea stateText = new JTextArea(player.getStatusText());
				stateText.setPreferredSize(new Dimension());
				stateText.setEditable(false);
				stateText.setLineWrap(true);
				stateText.setWrapStyleWord(true);
				builder.add(stateText).at(cc.xy(4, 4, CellConstraints.FILL, CellConstraints.FILL));
			}
			warningPanel = builder.getPanel();
		}

		return warningPanel;
	}
}
