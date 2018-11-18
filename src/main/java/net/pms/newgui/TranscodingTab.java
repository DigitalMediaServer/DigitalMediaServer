/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.newgui;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;
import net.pms.newgui.LooksFrame.AbstractTabListenerRegistrar;
import net.pms.newgui.LooksFrame.LooksFrameTab;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconListenerRegistrar;
import net.pms.newgui.components.CustomJButton;
import net.pms.newgui.components.CustomJSpinner;
import net.pms.newgui.components.DefaultTextField;
import net.pms.newgui.components.ImageButton;
import net.pms.newgui.components.SpinnerIntModel;
import net.pms.util.FormLayoutUtil;
import net.pms.util.KeyedComboBoxModel;
import net.pms.util.KeyedStringComboBoxModel;
import net.pms.util.Language;
import net.pms.util.Language.DefaultLangauge;
import net.pms.util.Language.LanguageType;
import net.pms.util.Pair;
import net.pms.util.SubtitleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscodingTab {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranscodingTab.class);
	private static final String COMMON_COL_SPEC = "left:pref, 3dlu, pref:grow";
	private static final String COMMON_ROW_SPEC = "4*(pref, 3dlu), pref, 9dlu, pref, 9dlu:grow, pref";
	private static final String LEFT_COL_SPEC = "left:pref, pref, pref, pref, 0:grow";
	private static final String LEFT_ROW_SPEC = "fill:10:grow, 3dlu, p, 3dlu, p, 3dlu, p";
	private static final String MAIN_COL_SPEC = "left:pref, pref, 7dlu, pref, pref, fill:10:grow";
	private static final String MAIN_ROW_SPEC = "fill:10:grow";
	private static final String EMPTY_PANEL = "empty_panel";

	private final PmsConfiguration configuration;
	private ComponentOrientation orientation;
	private LooksFrame looksFrame;
	private final TranscodingTabListenerRegistrar tabListenerRegistrar;

	TranscodingTab(PmsConfiguration configuration, LooksFrame looksFrame) {
		this.configuration = configuration;
		this.looksFrame = looksFrame;
		tabListenerRegistrar = new TranscodingTabListenerRegistrar(looksFrame);
		// Apply the orientation for the locale
		orientation = ComponentOrientation.getOrientation(PMS.getLocale());
	}

	private JCheckBox disableSubs;
	private JTextField forcetranscode;
	private JTextField notranscode;
	private JTextField maxbuffer;
	private JComboBox<Integer> nbcores;
	private DefaultMutableTreeNode parent[];
	private JPanel cardsPanel;
	private CardLayout cardLayout;
	private JTextField abitrate;
	private JTree tree;
	private JCheckBox forcePCM;
	private JCheckBox encodedAudioPassthrough;
	private static JCheckBox forceDTSinPCM;
	private JComboBox<String> channels;
	private JComboBox<String> vq;
	private JComboBox<String> x264Quality;
	private JCheckBox ac3remux;
	private JCheckBox mpeg2remux;
	private JCheckBox chapter_support;
	private JTextField chapter_interval;
	private JCheckBox videoHWacceleration;
	private DefaultTextField langs;
	private DefaultTextField defaultsubs;
	private JComboBox<String> forcedsub;
	private JTextField forcedtags;
	private JTextField alternateSubFolder;
	private JButton folderSelectButton;
	private JCheckBox autoloadExternalSubtitles;
	private JCheckBox deleteDownloadedSubtitles;
	private CustomJSpinner liveSubtitlesLimit;
	private DefaultTextField defaultaudiosubs;
	private JComboBox<String> subtitleCodePage;
	private JTextField defaultfont;
	private JButton fontselect;
	private JCheckBox fribidi;
	private JTextField assScale;
	private CustomJSpinner assOutline;
	private CustomJSpinner assShadow;
	private CustomJSpinner assMargin;
	private JButton subColor;
	private JCheckBox forceExternalSubtitles;
	private JCheckBox useEmbeddedSubtitlesStyle;
	private CustomJSpinner depth3D;
	private HashMap<EngineTreeNode, JComponent> engineSelectionPanels = new HashMap<>();

	/*
	 * 16 cores is the maximum allowed by MEncoder as of MPlayer r34863.
	 * Revisions before that allowed only 8.
	 */
	private static final int MAX_CORES = 16;
	private ImageButton arrowDownButton;
	private ImageButton arrowUpButton;
	private ImageButton toggleButton;
	private static enum ToggleButtonState {
		On ("button-toggle-on.png"),
		Off ("button-toggle-off.png");

		private final String iconName;
		private ToggleButtonState(String name) {
			iconName = name;
		}

		public String getIconName() {
			return iconName;
		}
	}

	public JComponent build() {
		String colSpec = FormLayoutUtil.getColSpec(MAIN_COL_SPEC, orientation);
		FormLayout mainlayout = new FormLayout(colSpec, MAIN_ROW_SPEC);
		FormBuilder builder = FormBuilder.create().layout(mainlayout).border(Paddings.DLU4).opaque(true);

		CellConstraints cc = new CellConstraints();

		if (!configuration.isHideAdvancedOptions()) {
			builder.add(buildRightTabbedPanel()).at(FormLayoutUtil.flip(cc.xyw(4, 1, 3), colSpec, orientation));
			builder.add(buildLeft()).at(FormLayoutUtil.flip(cc.xy(2, 1), colSpec, orientation));
		} else {
			builder.add(buildRightTabbedPanel()).at(FormLayoutUtil.flip(cc.xyw(2, 1, 5), colSpec, orientation));
			builder.add(buildLeft()).at(FormLayoutUtil.flip(cc.xy(2, 1), colSpec, orientation));
		}

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	private JComponent buildRightTabbedPanel() {
		cardLayout = new CardLayout();
		cardsPanel = new JPanel(cardLayout);
		return cardsPanel;
	}

	private void setButtonsState() {
		TreePath path = null;
		if (tree != null) {
			path = tree.getSelectionModel().getSelectionPath();
		}
		if (
			path == null ||
			!(path.getLastPathComponent() instanceof EngineTreeNode) ||
			((EngineTreeNode) path.getLastPathComponent()).getPlayer() == null)
		{
			arrowDownButton.setEnabled(false);
			arrowUpButton.setEnabled(false);
			toggleButton.setIcons(ToggleButtonState.On.getIconName());
			toggleButton.setEnabled(false);
		} else {
			EngineTreeNode node = (EngineTreeNode) path.getLastPathComponent();
			DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
			int index = treeModel.getIndexOfChild(node.getParent(), node);
			if (index == 0) {
				arrowUpButton.setEnabled(false);
			} else {
				arrowUpButton.setEnabled(true);
			}
			if (index == node.getParent().getChildCount() - 1) {
				arrowDownButton.setEnabled(false);
			} else {
				arrowDownButton.setEnabled(true);
			}
			Player player = node.getPlayer();
			if (player.isEnabled()) {
				toggleButton.setIcons(ToggleButtonState.On.getIconName());
				toggleButton.setEnabled(true);
			} else {
				toggleButton.setIcons(ToggleButtonState.Off.getIconName());
				toggleButton.setEnabled(true);
			}
		}
	}

	public JComponent buildLeft() {
		String colSpec = FormLayoutUtil.getColSpec(LEFT_COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, LEFT_ROW_SPEC);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		arrowDownButton = new ImageButton("button-arrow-down.png");
		arrowDownButton.setToolTipText(Messages.getString("TrTab2.6"));
		arrowDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path != null && path.getLastPathComponent() instanceof EngineTreeNode) {
					EngineTreeNode node = (EngineTreeNode) path.getLastPathComponent();
					if (node.getPlayer() != null) {
						DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();   // get the tree model
						//now get the index of the selected node in the DefaultTreeModel
						int index = treeModel.getIndexOfChild(node.getParent(), node);
						// if selected node is last, return (can't move down)
						if (index < node.getParent().getChildCount() - 1) {
							treeModel.insertNodeInto(node, (DefaultMutableTreeNode) node.getParent(), index + 1);   // move the node
							treeModel.reload();
							for (int i = 0; i < tree.getRowCount(); i++) {
								tree.expandRow(i);
							}
							tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
							configuration.setEnginePriorityBelow(
								node.getPlayer(),
								((EngineTreeNode) treeModel.getChild(node.getParent(), index)).getPlayer()
							);
						}
					}
				}
			}
		});
		builder.add(arrowDownButton).at(FormLayoutUtil.flip(cc.xy(2, 3), colSpec, orientation));

		arrowUpButton = new ImageButton("button-arrow-up.png");
		arrowUpButton.setToolTipText(Messages.getString("TrTab2.6"));
		arrowUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path != null && path.getLastPathComponent() instanceof EngineTreeNode) {
					EngineTreeNode node = (EngineTreeNode) path.getLastPathComponent();
					if (node.getPlayer() != null) {
						DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();   // get the tree model
						//now get the index of the selected node in the DefaultTreeModel
						int index = treeModel.getIndexOfChild(node.getParent(), node);
						// if selected node is first, return (can't move up)
						if (index != 0) {
							treeModel.insertNodeInto(node, (DefaultMutableTreeNode) node.getParent(), index - 1);   // move the node
							treeModel.reload();
							for (int i = 0; i < tree.getRowCount(); i++) {
								tree.expandRow(i);
							}
							tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
							configuration.setEnginePriorityAbove(node.getPlayer(), ((EngineTreeNode) treeModel.getChild(node.getParent(), index)).getPlayer());
						}
					}
				}
			}
		});
		builder.add(arrowUpButton).at(FormLayoutUtil.flip(cc.xy(3, 3), colSpec, orientation));

		toggleButton = new ImageButton();
		toggleButton.setToolTipText(Messages.getString("TrTab2.0"));
		setButtonsState();
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				EngineTreeNode node = null;
				if (path.getLastPathComponent() instanceof EngineTreeNode) {
					node = (EngineTreeNode) path.getLastPathComponent();
				}
				if (node != null && node.getPlayer() != null) {
					node.getPlayer().toggleEnabled(true);
					tree.updateUI();
					setButtonsState();
					JComponent component = engineSelectionPanels.get(node);
					if (component instanceof EnginePanel) {
						((EnginePanel) component).updatePanel();
					}
				}
			}
		});
		builder.add(toggleButton).at(FormLayoutUtil.flip(cc.xy(4, 3), colSpec, orientation));

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(Messages.getString("TrTab2.11"));
		EngineTreeNode commonEnc = new EngineTreeNode(Messages.getString("TrTab2.5"), null, buildCommon());
		cardsPanel.add(commonEnc.getConfigurationPanel(), commonEnc.id());
		root.add(commonEnc);

		parent = new DefaultMutableTreeNode[5];
		parent[0] = new DefaultMutableTreeNode(Messages.getString("TrTab2.14"));
		parent[1] = new DefaultMutableTreeNode(Messages.getString("TrTab2.15"));
		parent[2] = new DefaultMutableTreeNode(Messages.getString("TrTab2.16"));
		parent[3] = new DefaultMutableTreeNode(Messages.getString("TrTab2.17"));
		parent[4] = new DefaultMutableTreeNode(Messages.getString("TrTab2.18"));
		root.add(parent[0]);
		root.add(parent[1]);
		root.add(parent[2]);
		root.add(parent[3]);
		root.add(parent[4]);

		cardsPanel.add(EMPTY_PANEL, new JPanel());

		tree = new JTree(new DefaultTreeModel(root));
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				setButtonsState();
				if (e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() instanceof EngineTreeNode) {
					EngineTreeNode engine = (EngineTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
					cardLayout.show(cardsPanel, engine.id());
				} else {
					cardLayout.show(cardsPanel, EMPTY_PANEL);
				}
			}
		});

		tree.setRequestFocusEnabled(false);
		tree.setCellRenderer(new TranscodingEngineCellRenderer(tabListenerRegistrar));
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						int doubleCount = e.getClickCount() / 2;
						if (doubleCount % 2 != 0) {
							// Ignore even number of double clicks
							Object node = tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
							if (node instanceof EngineTreeNode && ((EngineTreeNode)node).getPlayer() != null) {
								((EngineTreeNode)node).getPlayer().toggleEnabled(true);
								tree.updateUI();
								setButtonsState();
								JComponent component = engineSelectionPanels.get(node);
								if (component instanceof EnginePanel) {
									((EnginePanel) component).updatePanel();
								}
							}
						}
					}
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (' ' == e.getKeyChar()) {
					TreePath path = tree.getSelectionModel().getSelectionPath();
					EngineTreeNode node = null;
					if (path.getLastPathComponent() instanceof EngineTreeNode) {
						node = (EngineTreeNode) path.getLastPathComponent();
					}
					if (node != null && node.getPlayer() != null) {
						node.getPlayer().toggleEnabled(true);
						tree.updateUI();
						setButtonsState();
						JComponent component = engineSelectionPanels.get(node);
						if (component instanceof EnginePanel) {
							((EnginePanel) component).updatePanel();
						}
					}
				}
			}
		});
		JScrollPane pane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		builder.add(pane).at(FormLayoutUtil.flip(cc.xyw(2, 1, 4), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.19")).at(FormLayoutUtil.flip(cc.xyw(2, 5, 4), colSpec, orientation));
		builder.addLabel(Messages.getString("TrTab2.20")).at(FormLayoutUtil.flip(cc.xyw(2, 7, 4), colSpec, orientation));

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	public void addEngines() {
		for (Player player : PlayerFactory.getPlayers(false, true)) {
			if (player.isGPUAccelerationReady()) {
				videoHWacceleration.setEnabled(true);
				videoHWacceleration.setSelected(configuration.isGPUAcceleration());
				break;
			}
		}

		for (Player player : PlayerFactory.getAllPlayers()) {
			EngineTreeNode engine = new EngineTreeNode(player.name(), player, null);

			JComponent engineSettings = engine.getConfigurationPanel();
			JComponent enginePanel = new EnginePanel(player, orientation, engineSettings, configuration, tabListenerRegistrar);
			engineSelectionPanels.put(engine, enginePanel);
			cardsPanel.add(enginePanel, engine.id());
			parent[player.purpose()].add(engine);
		}

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		tree.setSelectionRow(0);
		tree.updateUI();
	}

	public JComponent buildCommon() {
		String colSpec = FormLayoutUtil.getColSpec(COMMON_COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, COMMON_ROW_SPEC);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		builder
			.add(LooksFrame.createStyledSeparator(Messages.getString("NetworkTab.5"), Font.BOLD, orientation))
			.at(FormLayoutUtil.flip(cc.xyw(1, 1, 3), colSpec, orientation));

		disableSubs = new JCheckBox(Messages.getString("TrTab2.51"), configuration.isDisableSubtitles());
		disableSubs.setContentAreaFilled(false);
 		disableSubs.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setDisableSubtitles((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		if (!configuration.isHideAdvancedOptions()) {
			builder.addLabel(Messages.getString("TrTab2.23")).at(FormLayoutUtil.flip(cc.xy(1, 3), colSpec, orientation));
			maxbuffer = new JTextField("" + configuration.getMaxMemoryBufferSize());
			maxbuffer.setToolTipText(Messages.getString("TrTab2.73"));
			maxbuffer.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					try {
						int ab = Integer.parseInt(maxbuffer.getText());
						configuration.setMaxMemoryBufferSize(ab);
					} catch (NumberFormatException nfe) {
						LOGGER.debug("Could not parse max memory buffer size from \"" + maxbuffer.getText() + "\"");
					}
				}
			});
			builder.add(maxbuffer).at(FormLayoutUtil.flip(cc.xy(3, 3), colSpec, orientation));

			String nCpusLabel = String.format(Messages.getString("TrTab2.24"), Runtime.getRuntime().availableProcessors());
			builder.addLabel(nCpusLabel).at(FormLayoutUtil.flip(cc.xy(1, 5), colSpec, orientation));

			Integer[] guiCores = new Integer[MAX_CORES];
			for (int i = 0; i < MAX_CORES; i++) {
				guiCores[i] = i + 1;
			}
			nbcores = new JComboBox<>(guiCores);
			nbcores.setEditable(false);
			int nbConfCores = configuration.getNumberOfCpuCores();
			if (nbConfCores > 0 && nbConfCores <= MAX_CORES) {
				nbcores.setSelectedItem(nbConfCores);
			} else {
				nbcores.setSelectedIndex(0);
			}

			nbcores.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					configuration.setNumberOfCpuCores((int) e.getItem());
				}
			});
			builder.add(nbcores).at(FormLayoutUtil.flip(cc.xy(3, 5), colSpec, orientation));

			chapter_support = new JCheckBox(Messages.getString("TrTab2.52"), configuration.isChapterSupport());
			chapter_support.setContentAreaFilled(false);
			chapter_support.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					configuration.setChapterSupport((e.getStateChange() == ItemEvent.SELECTED));
					chapter_interval.setEnabled(configuration.isChapterSupport());
				}
			});
			builder.add(GuiUtil.getPreferredSizeComponent(chapter_support)).at(FormLayoutUtil.flip(cc.xy(1, 7), colSpec, orientation));

			chapter_interval = new JTextField("" + configuration.getChapterInterval());
			chapter_interval.setEnabled(configuration.isChapterSupport());
			chapter_interval.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					try {
						int ab = Integer.parseInt(chapter_interval.getText());
						configuration.setChapterInterval(ab);
					} catch (NumberFormatException nfe) {
						LOGGER.debug("Could not parse chapter interval from \"" + chapter_interval.getText() + "\"");
					}
				}
			});
			builder.add(chapter_interval).at(FormLayoutUtil.flip(cc.xy(3, 7), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(disableSubs)).at(FormLayoutUtil.flip(cc.xy(1, 9), colSpec, orientation));
		} else {
			builder.add(GuiUtil.getPreferredSizeComponent(disableSubs)).at(FormLayoutUtil.flip(cc.xy(1, 3), colSpec, orientation));
		}

		JTabbedPane setupTabbedPanel = new JTabbedPane();
		setupTabbedPanel.setUI(new CustomTabbedPaneUI());

		setupTabbedPanel.addTab(Messages.getString("TrTab2.67"), buildVideoSetupPanel());
		setupTabbedPanel.addTab(Messages.getString("TrTab2.68"), buildAudioSetupPanel());
		setupTabbedPanel.addTab(Messages.getString("MEncoderVideo.8"), buildSubtitlesSetupPanel());

		if (!configuration.isHideAdvancedOptions()) {
			builder.add(setupTabbedPanel).at(FormLayoutUtil.flip(cc.xywh(1, 11, 3, 3), colSpec, orientation));
		}

		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	private JComponent buildVideoSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("left:pref, 3dlu, pref:grow", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, 2*(pref, 3dlu), 10dlu, 10dlu, 4*(pref, 3dlu), pref");
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.DLU4);
		CellConstraints cc = new CellConstraints();

		videoHWacceleration = new JCheckBox(Messages.getString("TrTab2.70"), configuration.isGPUAcceleration());
		videoHWacceleration.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setGPUAcceleration((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(videoHWacceleration)).at(FormLayoutUtil.flip(cc.xy(1, 2), colSpec, orientation));
		videoHWacceleration.setEnabled(false);

		mpeg2remux = new JCheckBox(Messages.getString("MEncoderVideo.39"), configuration.isMencoderRemuxMPEG2());
		mpeg2remux.setToolTipText(Messages.getString("TrTab2.82") + (Platform.isWindows() ? " " + Messages.getString("TrTab2.21") : "") + "</html>");
		mpeg2remux.setContentAreaFilled(false);
		mpeg2remux.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderRemuxMPEG2((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(mpeg2remux)).at(FormLayoutUtil.flip(cc.xy(1, 6), colSpec, orientation));

		builder
			.add(LooksFrame.createStyledSeparator(Messages.getString("TrTab2.7"), Font.BOLD, orientation))
			.at(FormLayoutUtil.flip(cc.xyw(1, 8, 3), colSpec, orientation));

		builder.add(new JLabel(Messages.getString("TrTab2.32"))).at(FormLayoutUtil.flip(cc.xy(1, 10), colSpec, orientation));
		String[] keys = new String[] {
			"Automatic (Wired)",
			"Automatic (Wireless)",
			"keyint=5:vqscale=1:vqmin=1",
			"keyint=5:vqscale=1:vqmin=2",
			"keyint=5:vqscale=2:vqmin=3",
			"keyint=25:vqmax=5:vqmin=2",
			"keyint=25:vqmax=7:vqmin=2",
			"keyint=25:vqmax=8:vqmin=3"
		};
		//TODO Set ViewLevel.EXPERT when ViewLevel is fully implemented
		String[] values = new String[] {
			Messages.getString("TrTab2.92"), // Automatic (Wired)
			Messages.getString("TrTab2.93"), // Automatic (Wireless)
			String.format(
				Messages.getString("TrTab2.61")+"%s", // Lossless
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=5:vqscale=1:vqmin=1)" : ""
			),
			String.format(
				Messages.getString("TrTab2.60")+"%s", // Great
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=5:vqscale=1:vqmin=2)" : ""
			),
			String.format(
				Messages.getString("TrTab2.62")+"%s", // Good (wired)
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=5:vqscale=2:vqmin=3)" : ""
			),
			String.format(
				Messages.getString("TrTab2.63")+"%s", // Good (wireless)
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=25:vqmax=5:vqmin=2)" : ""
			),
			String.format(
				Messages.getString("TrTab2.64")+"%s", // Medium (wireless)
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=25:vqmax=7:vqmin=2)" : ""
			),
			String.format(
				Messages.getString("TrTab2.65")+"%s", // Low
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (keyint=25:vqmax=8:vqmin=3)" : ""
			)
		};
		final KeyedStringComboBoxModel mPEG2MainModel = new KeyedStringComboBoxModel(keys, values);

		vq = new JComboBox<>(mPEG2MainModel);
		vq.setPreferredSize(getPreferredHeight(vq));
		vq.setToolTipText(Messages.getString("TrTab2.74"));
		mPEG2MainModel.setSelectedKey(configuration.getMPEG2MainSettings());
		vq.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setMPEG2MainSettings(mPEG2MainModel.getSelectedKey());
				}
			}
		});
		vq.setEditable(true);
		builder.add(vq).at(FormLayoutUtil.flip(cc.xy(3, 10), colSpec, orientation));

		builder.add(new JLabel(Messages.getString("TrTab2.79"))).at(FormLayoutUtil.flip(cc.xy(1, 12), colSpec, orientation));
		keys = new String[] {
			"Automatic (Wired)",
			"Automatic (Wireless)",
			"16"
		};
		//TODO Set ViewLevel.EXPERT when ViewLevel is fully implemented
		values = new String[] {
			Messages.getString("TrTab2.92"),
			Messages.getString("TrTab2.93"),
			String.format(
				Messages.getString("TrTab2.61")+"%s", // Lossless
				looksFrame.getViewLevel().isGreaterOrEqual(ViewLevel.ADVANCED) ? " (16)" : ""
			)
		};
		final KeyedStringComboBoxModel x264QualityModel = new KeyedStringComboBoxModel(keys, values);

		x264Quality = new JComboBox<>(x264QualityModel);
		x264Quality.setPreferredSize(getPreferredHeight(x264Quality));
		x264Quality.setToolTipText(Messages.getString("TrTab2.81"));
		x264QualityModel.setSelectedKey(configuration.getx264ConstantRateFactor());
		x264Quality.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setx264ConstantRateFactor(x264QualityModel.getSelectedKey());
				}
			}
		});
		x264Quality.setEditable(true);
		builder.add(x264Quality).at(FormLayoutUtil.flip(cc.xy(3, 12), colSpec, orientation));

		builder.add(new JLabel(Messages.getString("TrTab2.8"))).at(FormLayoutUtil.flip(cc.xy(1, 14), colSpec, orientation));
		notranscode = new JTextField(configuration.getDisableTranscodeForExtensions());
		notranscode.setToolTipText(Messages.getString("TrTab2.96"));
		notranscode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setDisableTranscodeForExtensions(notranscode.getText());
			}
		});
		builder.add(notranscode).at(FormLayoutUtil.flip(cc.xy(3, 14), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.9")).at(FormLayoutUtil.flip(cc.xy(1, 16), colSpec, orientation));
		forcetranscode = new JTextField(configuration.getForceTranscodeForExtensions());
		forcetranscode.setToolTipText(Messages.getString("TrTab2.96"));
		forcetranscode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setForceTranscodeForExtensions(forcetranscode.getText());
			}
		});
		builder.add(forcetranscode).at(FormLayoutUtil.flip(cc.xy(3, 16), colSpec, orientation));

		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	private JComponent buildAudioSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("left:pref, 3dlu, pref:grow", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, pref, 3dlu, 5*(pref, 3dlu), pref, 12dlu, 3*(pref, 3dlu), pref:grow");
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.DLU4);
		CellConstraints cc = new CellConstraints();

		builder.addLabel(Messages.getString("TrTab2.50")).at(FormLayoutUtil.flip(cc.xy(1, 2), colSpec, orientation));

		Integer[] keys = new Integer[] {2, 6};
		String[] values = new String[] {
			Messages.getString("TrTab2.55"),
			Messages.getString("TrTab2.56"), // 7.1 not supported by Mplayer
		};

		final KeyedComboBoxModel<Integer, String> audioChannelsModel = new KeyedComboBoxModel<>(keys, values);
		channels = new JComboBox<>(audioChannelsModel);
		channels.setEditable(false);
		audioChannelsModel.setSelectedKey(configuration.getAudioChannelCount());
		channels.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioChannelCount(audioChannelsModel.getSelectedKey());
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(channels)).at(FormLayoutUtil.flip(cc.xy(3, 2), colSpec, orientation));

		forcePCM = new JCheckBox(Messages.getString("TrTab2.27"), configuration.isAudioUsePCM());
		forcePCM.setToolTipText(Messages.getString("TrTab2.83"));
		forcePCM.setContentAreaFilled(false);
		forcePCM.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioUsePCM(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(forcePCM)).at(FormLayoutUtil.flip(cc.xy(1, 4), colSpec, orientation));

		ac3remux = new JCheckBox(Messages.getString("TrTab2.26"), configuration.isAudioRemuxAC3());
		ac3remux.setToolTipText(Messages.getString("TrTab2.84") + (Platform.isWindows() ? " " + Messages.getString("TrTab2.21") : "") + "</html>");
		ac3remux.setEnabled(!configuration.isEncodedAudioPassthrough());
		ac3remux.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioRemuxAC3((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(ac3remux)).at(FormLayoutUtil.flip(cc.xy(1, 6), colSpec, orientation));

		forceDTSinPCM = new JCheckBox(Messages.getString("TrTab2.28"), configuration.isAudioEmbedDtsInPcm());
		forceDTSinPCM.setToolTipText(Messages.getString("TrTab2.85") + (Platform.isWindows() ? " " + Messages.getString("TrTab2.21") : "") + "</html>");
		forceDTSinPCM.setEnabled(!configuration.isEncodedAudioPassthrough());
		forceDTSinPCM.setContentAreaFilled(false);
		forceDTSinPCM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setAudioEmbedDtsInPcm(forceDTSinPCM.isSelected());
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(forceDTSinPCM)).at(FormLayoutUtil.flip(cc.xy(1, 8), colSpec, orientation));

		encodedAudioPassthrough = new JCheckBox(Messages.getString("TrTab2.53"), configuration.isEncodedAudioPassthrough());
		encodedAudioPassthrough.setToolTipText(Messages.getString("TrTab2.86") + (Platform.isWindows() ? " " + Messages.getString("TrTab2.21") : "") + "</html>");
		encodedAudioPassthrough.setContentAreaFilled(false);
		encodedAudioPassthrough.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setEncodedAudioPassthrough((e.getStateChange() == ItemEvent.SELECTED));
				ac3remux.setEnabled((e.getStateChange() != ItemEvent.SELECTED));
				forceDTSinPCM.setEnabled((e.getStateChange() != ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(encodedAudioPassthrough)).at(cc.xyw(1, 10, 3));

		builder.addLabel(Messages.getString("TrTab2.29")).at(FormLayoutUtil.flip(cc.xy(1, 12), colSpec, orientation));
		abitrate = new JTextField("" + configuration.getAudioBitrate());
		abitrate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(abitrate.getText());
					configuration.setAudioBitrate(ab);
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse audio bitrate from \"" + abitrate.getText() + "\"");
				}
			}
		});
		builder.add(abitrate).at(FormLayoutUtil.flip(cc.xy(3, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.7")).at(FormLayoutUtil.flip(cc.xy(1, 14), colSpec, orientation));
		langs = new DefaultTextField(20, Messages.getString("MEncoderVideo.126"), true);
		langs.setText(languagesToString(configuration.getAudioLanguages()));
		langs.setToolTipText(Messages.getString("TrTab2.75"));
		langs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setAudioLanguages(langs.getText());
				langs.setText(languagesToString(configuration.getAudioLanguages()));
			}
		});
		builder.add(langs).at(FormLayoutUtil.flip(cc.xy(3, 14), colSpec, orientation));

		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	@Nullable
	private static String languagesToString(@Nullable Collection<? extends Language> languages) {
		if (languages == null || languages.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Language language : languages) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(language.getCode());
		}
		return sb.toString();
	}

	@Nullable
	private static String languagePairsToString(@Nullable Collection<Pair<Language, Language>> pairs) {
		if (pairs == null || pairs.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Pair<Language, Language> pair : pairs) {
			if (pair.getFirst() != null && pair.getSecond() != null) {
				if (first) {
					first = false;
				} else {
					sb.append(";");
				}
				sb.append(pair.getFirst().getCode()).append(",").append(pair.getSecond().getCode());
			}
		}
		return sb.toString();
	}

	private JComponent buildSubtitlesSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, pref", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, 11*(pref, 3dlu), pref");
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.DLU4);
		CellConstraints cc = new CellConstraints();

		builder.addLabel(Messages.getString("MEncoderVideo.9")).at(FormLayoutUtil.flip(cc.xy(1, 2), colSpec, orientation));
		defaultsubs = new DefaultTextField(20, Messages.getString("MEncoderVideo.127"), true);
		defaultsubs.setText(languagesToString(configuration.getSubtitlesLanguages()));
		defaultsubs.setToolTipText(Messages.getString("TrTab2.76"));
		defaultsubs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setSubtitlesLanguages(defaultsubs.getText());
				defaultsubs.setText(languagesToString(configuration.getSubtitlesLanguages()));
			}
		});
		builder.add(defaultsubs).at(FormLayoutUtil.flip(cc.xyw(3, 2, 13), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.94")).at(FormLayoutUtil.flip(cc.xy(1, 4), colSpec, orientation));

		final KeyedComboBoxModel<Language, String> forcedSubModel = DefaultLangauge.getKeyedComboBoxModel(
			EnumSet.of(LanguageType.NORMAL, LanguageType.WILDCARD)
		);
		forcedsub = new JComboBox<>(forcedSubModel);
		forcedSubModel.setSelectedKey(configuration.getForcedSubtitleLanguage());
		forcedsub.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setForcedSubtitleLanguage(forcedSubModel.getSelectedKey());
				}
			}
		});
		builder.add(forcedsub).at(FormLayoutUtil.flip(cc.xyw(3, 4, 3), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.95"))
			.at(FormLayoutUtil.flip(cc.xyw(7, 4, 5, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		forcedtags = new JTextField(configuration.getForcedSubtitleTags());
		forcedtags.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setForcedSubtitleTags(forcedtags.getText());
			}
		});
		builder.add(forcedtags).at(FormLayoutUtil.flip(cc.xyw(13, 4, 3), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.10")).at(FormLayoutUtil.flip(cc.xy(1, 6), colSpec, orientation));
		defaultaudiosubs = new DefaultTextField(20, Messages.getString("MEncoderVideo.128"), true);
		defaultaudiosubs.setText(languagePairsToString(configuration.getAudioSubLanguages()));
		defaultaudiosubs.setToolTipText(Messages.getString("TrTab2.77"));
		defaultaudiosubs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setAudioSubLanguages(defaultaudiosubs.getText());
				defaultaudiosubs.setText(languagePairsToString(configuration.getAudioSubLanguages()));
			}
		});
		builder.add(defaultaudiosubs).at(FormLayoutUtil.flip(cc.xyw(3, 6, 13), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.37")).at(FormLayoutUtil.flip(cc.xy(1, 8), colSpec, orientation));
		alternateSubFolder = new JTextField(configuration.getAlternateSubtitlesFolder());
		alternateSubFolder.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAlternateSubtitlesFolder(alternateSubFolder.getText());
			}
		});
		builder.add(alternateSubFolder).at(FormLayoutUtil.flip(cc.xyw(3, 8, 12), colSpec, orientation));

		folderSelectButton = new JButton("...");
		folderSelectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					alternateSubFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setAlternateSubtitlesFolder(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(folderSelectButton).at(FormLayoutUtil.flip(cc.xy(15, 8), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.95")).at(FormLayoutUtil.flip(cc.xy(1, 10), colSpec, orientation));
		String[] keys = new String[]{
			"", "cp874", "cp932", "cp936", "cp949", "cp950", "cp1250",
			"cp1251", "cp1252", "cp1253", "cp1254", "cp1255", "cp1256",
			"cp1257", "cp1258", "ISO-2022-CN", "ISO-2022-JP", "ISO-2022-KR",
			"ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4",
			"ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
			"ISO-8859-9", "ISO-8859-10", "ISO-8859-11", "ISO-8859-13",
			"ISO-8859-14", "ISO-8859-15", "ISO-8859-16", "Big5", "EUC-JP",
			"EUC-KR", "GB18030", "IBM420", "IBM424", "KOI8-R", "Shift_JIS", "TIS-620"
		};
		String[] values = new String[]{
			Messages.getString("Generic.AutoDetect"),
			Messages.getString("CharacterSet.874"),
			Messages.getString("CharacterSet.932"),
			Messages.getString("CharacterSet.936"),
			Messages.getString("CharacterSet.949"),
			Messages.getString("CharacterSet.950"),
			Messages.getString("CharacterSet.1250"),
			Messages.getString("CharacterSet.1251"),
			Messages.getString("CharacterSet.1252"),
			Messages.getString("CharacterSet.1253"),
			Messages.getString("CharacterSet.1254"),
			Messages.getString("CharacterSet.1255"),
			Messages.getString("CharacterSet.1256"),
			Messages.getString("CharacterSet.1257"),
			Messages.getString("CharacterSet.1258"),
			Messages.getString("CharacterSet.2022-CN"),
			Messages.getString("CharacterSet.2022-JP"),
			Messages.getString("CharacterSet.2022-KR"),
			Messages.getString("CharacterSet.8859-1"),
			Messages.getString("CharacterSet.8859-2"),
			Messages.getString("CharacterSet.8859-3"),
			Messages.getString("CharacterSet.8859-4"),
			Messages.getString("CharacterSet.8859-5"),
			Messages.getString("CharacterSet.8859-6"),
			Messages.getString("CharacterSet.8859-7"),
			Messages.getString("CharacterSet.8859-8"),
			Messages.getString("CharacterSet.8859-9"),
			Messages.getString("CharacterSet.8859-10"),
			Messages.getString("CharacterSet.8859-11"),
			Messages.getString("CharacterSet.8859-13"),
			Messages.getString("CharacterSet.8859-14"),
			Messages.getString("CharacterSet.8859-15"),
			Messages.getString("CharacterSet.8859-16"),
			Messages.getString("CharacterSet.Big5"),
			Messages.getString("CharacterSet.EUC-JP"),
			Messages.getString("CharacterSet.EUC-KR"),
			Messages.getString("CharacterSet.GB18030"),
			Messages.getString("CharacterSet.IBM420"),
			Messages.getString("CharacterSet.IBM424"),
			Messages.getString("CharacterSet.KOI8-R"),
			Messages.getString("CharacterSet.ShiftJIS"),
			Messages.getString("CharacterSet.TIS-620")
		};

		final KeyedComboBoxModel<String, String> subtitleCodePageModel = new KeyedComboBoxModel<>(keys, values);
		subtitleCodePage = new JComboBox<>(subtitleCodePageModel);
		subtitleCodePage.setPreferredSize(getPreferredHeight(subtitleCodePage));
		subtitleCodePage.setToolTipText(Messages.getString("TrTab2.94"));
		subtitleCodePageModel.setSelectedKey(configuration.getSubtitlesCodepage());
		subtitleCodePage.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setSubtitlesCodepage(subtitleCodePageModel.getSelectedKey());
				}
			}
		});

		subtitleCodePage.setEditable(false);
		builder.add(subtitleCodePage).at(FormLayoutUtil.flip(cc.xyw(3, 10, 7), colSpec, orientation));

		fribidi = new JCheckBox(Messages.getString("MEncoderVideo.23"), configuration.isMencoderSubFribidi());
		fribidi.setContentAreaFilled(false);
		fribidi.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderSubFribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(fribidi).at(FormLayoutUtil.flip(cc.xyw(11, 10, 5, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.24")).at(FormLayoutUtil.flip(cc.xy(1, 12), colSpec, orientation));
		defaultfont = new JTextField(configuration.getFont());
		defaultfont.setToolTipText(Messages.getString("TrTab2.97"));
		defaultfont.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setFont(defaultfont.getText());
			}
		});
		builder.add(defaultfont).at(FormLayoutUtil.flip(cc.xyw(3, 12, 12), colSpec, orientation));

		fontselect = new CustomJButton("...");
		fontselect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FontFileFilter());
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("MEncoderVideo.25"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					defaultfont.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setFont(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		builder.add(fontselect).at(FormLayoutUtil.flip(cc.xy(15, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.12")).at(FormLayoutUtil.flip(cc.xy(1, 14), colSpec, orientation));

		JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		flowPanel.setComponentOrientation(orientation);
		builder.addLabel(Messages.getString("MEncoderVideo.133")).at(FormLayoutUtil.flip(cc.xy(1, 14, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		assScale = new JTextField(configuration.getAssScale());
		assScale.setHorizontalAlignment(SwingConstants.RIGHT);
		assScale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAssScale(assScale.getText());
			}
		});
		flowPanel.add(assScale);

		flowPanel.add(new JLabel(Messages.getString("MEncoderVideo.13")));

		int assOutlineValue;
		try {
			assOutlineValue = Integer.parseInt(configuration.getAssOutline());
		} catch (NumberFormatException e) {
			assOutlineValue = 1;
		}
		final SpinnerIntModel assOutlineModel = new SpinnerIntModel(assOutlineValue, 0, 99, 1);
		assOutline = new CustomJSpinner(assOutlineModel, true);
		assOutline.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setAssOutline(assOutlineModel.getValue().toString());
			}
		});
		flowPanel.add(assOutline);

		flowPanel.add(new JLabel(Messages.getString("MEncoderVideo.14")));

		int assShadowValue;
		try {
			assShadowValue = Integer.parseInt(configuration.getAssShadow());
		} catch (NumberFormatException e) {
			assShadowValue = 1;
		}
		final SpinnerIntModel assShadowModel = new SpinnerIntModel(assShadowValue, 0, 99, 1);
		assShadow = new CustomJSpinner(assShadowModel, true);
		assShadow.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setAssShadow(assShadowModel.getValue().toString());
			}
		});
		flowPanel.add(assShadow);

		flowPanel.add(new JLabel(Messages.getString("MEncoderVideo.15")));

		int assMarginValue;
		try {
			assMarginValue = Integer.parseInt(configuration.getAssMargin());
		} catch (NumberFormatException e) {
			assMarginValue = 10;
		}
		final SpinnerIntModel assMarginModel = new SpinnerIntModel(assMarginValue, 0, 999, 5);
		assMargin = new CustomJSpinner(assMarginModel, true);
		assMargin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setAssMargin(assMarginModel.getValue().toString());
			}
		});

		flowPanel.add(assMargin);
		builder.add(flowPanel).at(FormLayoutUtil.flip(cc.xyw(3, 14, 13), colSpec, orientation));

		autoloadExternalSubtitles = new JCheckBox(Messages.getString("MEncoderVideo.22"), configuration.isAutoloadExternalSubtitles());
		autoloadExternalSubtitles.setToolTipText(Messages.getString("TrTab2.78"));
		autoloadExternalSubtitles.setContentAreaFilled(false);
		autoloadExternalSubtitles.setEnabled(!configuration.isForceExternalSubtitles());
		autoloadExternalSubtitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAutoloadExternalSubtitles((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(autoloadExternalSubtitles).at(FormLayoutUtil.flip(cc.xyw(1, 16, 10), colSpec, orientation));

		subColor = new JButton();
		subColor.setText(Messages.getString("MEncoderVideo.31"));
		subColor.setBackground(configuration.getSubsColor());
		subColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JColorChooser jColorChooser = new JColorChooser(subColor.getBackground());
				Locale locale = PMS.getLocale();
				jColorChooser.setLocale(locale);
				jColorChooser.setComponentOrientation(ComponentOrientation.getOrientation(locale));
				JDialog dialog = JColorChooser.createDialog(looksFrame, Messages.getString("MEncoderVideo.125"), true, jColorChooser, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						Color newColor = jColorChooser.getColor();
						if (newColor != null) {
							subColor.setBackground(newColor);
							configuration.setSubsColor(newColor);
							// Subtitle color has been changed so all temporary subtitles must be deleted
							SubtitleUtils.deleteSubs();
						}
					}
				}, null);
				dialog.setVisible(true);
				dialog.dispose();
			}
		});
		builder.add(subColor).at(FormLayoutUtil.flip(cc.xyw(11, 16, 5), colSpec, orientation));

		forceExternalSubtitles = new JCheckBox(Messages.getString("TrTab2.87"), configuration.isForceExternalSubtitles());
		forceExternalSubtitles.setToolTipText(Messages.getString("TrTab2.88"));
		forceExternalSubtitles.setContentAreaFilled(false);
		forceExternalSubtitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setForceExternalSubtitles((e.getStateChange() == ItemEvent.SELECTED));
				if (configuration.isForceExternalSubtitles()) {
					autoloadExternalSubtitles.setSelected(true);
				}
				autoloadExternalSubtitles.setEnabled(!configuration.isForceExternalSubtitles());
			}
		});
		builder.add(forceExternalSubtitles).at(FormLayoutUtil.flip(cc.xyw(1, 18, 6), colSpec, orientation));

		deleteDownloadedSubtitles = new JCheckBox(Messages.getString("TrTab2.DeleteLiveSubtitles"), !configuration.isLiveSubtitlesKeep());
		deleteDownloadedSubtitles.setToolTipText(Messages.getString("TrTab2.DeleteLiveSubtitlesTooltip"));
		deleteDownloadedSubtitles.setContentAreaFilled(false);
		deleteDownloadedSubtitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setLiveSubtitlesKeep((e.getStateChange() != ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(deleteDownloadedSubtitles))
			.at(FormLayoutUtil.flip(cc.xyw(7, 18, 9, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));

		useEmbeddedSubtitlesStyle = new JCheckBox(Messages.getString("MEncoderVideo.36"), configuration.isUseEmbeddedSubtitlesStyle());
		useEmbeddedSubtitlesStyle.setToolTipText(Messages.getString("TrTab2.89"));
		useEmbeddedSubtitlesStyle.setContentAreaFilled(false);
		useEmbeddedSubtitlesStyle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setUseEmbeddedSubtitlesStyle(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(useEmbeddedSubtitlesStyle).at(FormLayoutUtil.flip(cc.xyw(1, 20, 5), colSpec, orientation));


		final SpinnerIntModel liveSubtitlesLimitModel = new SpinnerIntModel(configuration.getLiveSubtitlesLimit(), 1, 999, 1);
		liveSubtitlesLimit = new CustomJSpinner(liveSubtitlesLimitModel, true);
		liveSubtitlesLimit.setToolTipText(Messages.getString("TrTab2.LiveSubtitlesLimitTooltip"));
		liveSubtitlesLimit.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setLiveSubtitlesLimit(liveSubtitlesLimitModel.getIntValue());
			}
		});
		JLabel liveSubtitlesLimitLabel = new JLabel(Messages.getString("TrTab2.LiveSubtitlesLimit"));
		liveSubtitlesLimitLabel.setLabelFor(liveSubtitlesLimit);
		builder.add(liveSubtitlesLimitLabel).at(FormLayoutUtil.flip(cc.xyw(7, 20, 7, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		builder.add(liveSubtitlesLimit).at(FormLayoutUtil.flip(cc.xy(15, 20), colSpec, orientation));

		final SpinnerIntModel depth3DModel = new SpinnerIntModel(configuration.getDepth3D(), -5, 5, 1);
		depth3D = new CustomJSpinner(depth3DModel, true);
		depth3D.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setDepth3D(depth3DModel.getIntValue());
			}
		});
		JLabel depth3DLabel = new JLabel(Messages.getString("TrTab2.90"));
		depth3DLabel.setLabelFor(depth3D);
		builder.add(depth3DLabel).at(FormLayoutUtil.flip(cc.xyw(7, 22, 7, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		builder.add(depth3D).at(FormLayoutUtil.flip(cc.xy(15, 22), colSpec, orientation));

		final JPanel panel = builder.getPanel();
		GuiUtil.enableContainer(panel, !configuration.isDisableSubtitles());
		disableSubs.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// If "Disable Subtitles" is not selected, subtitles are enabled
				GuiUtil.enableContainer(panel, e.getStateChange() != ItemEvent.SELECTED);
			}
		});

		panel.applyComponentOrientation(orientation);
		return panel;
	}

	// This is kind of a hack to give combo boxes a small preferred size
	private static Dimension getPreferredHeight(JComponent component) {
		return new Dimension(20, component.getPreferredSize().height);
	}

	/**
	 * Creates a new {@link AnimatedIconListenerRegistrar} that registers tab
	 * change to and from {@link LooksFrameTab#TRANSCODING_TAB} and application
	 * minimize events. Suitable for {@link AnimatedIcon}s that's visible
	 * whenever this tab is visible.
	 *
	 * @author Nadahar
	 */
	public static class TranscodingTabListenerRegistrar extends AbstractTabListenerRegistrar {

		private TranscodingTabListenerRegistrar(@Nonnull LooksFrame looksFrame) {
			super(looksFrame);
		}

		@Override
		protected LooksFrameTab getVisibleTab() {
			return LooksFrameTab.TRANSCODING_TAB;
		}
	}
}
