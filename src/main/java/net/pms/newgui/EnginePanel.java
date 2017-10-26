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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.ProgramExecutableType;
import net.pms.configuration.WindowsProgramPaths;
import net.pms.encoders.Player;
import net.pms.newgui.TranscodingTab.TranscodingTabListenerRegistrar;
import net.pms.newgui.components.AnimatedButton;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconListenerRegistrar;
import net.pms.newgui.components.AnimatedIconListener;
import net.pms.newgui.components.AnimatedIconListenerAction;
import net.pms.newgui.components.DefaultTextField;
import net.pms.newgui.components.GenericsComboBoxModel;
import net.pms.util.FileUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.Version;

/**
 * Responsible for building and updating the engine panel (right side) of the
 * "Transcoding Tab".
 *
 * @author Nadahar
 */
public class EnginePanel extends JScrollPane {
	private static final long serialVersionUID = 1L;

	private static final String SELECTION_COL_SPEC = "left:pref, $lcgap, fill:50dlu:grow";
	private static final String SELECTION_ROW_SPEC = "pref, 3dlu, pref, 6dlu, pref";
	private static final String STATUS_COL_SPEC = "left:pref, $lcgap, 5*(pref, $lcgap), fill:10dlu:grow";
	private static final String STATUS_ROW_SPEC = "pref";
	private static final ImageIcon ENGINE_OK = LooksFrame.readImageIcon("symbol-light-green-on.png");
	private static final ImageIcon ENGINE_OK_DISABLED = LooksFrame.readImageIcon("symbol-light-green-off.png");
	private static final ImageIcon ENGINE_ERROR = LooksFrame.readImageIcon("symbol-light-red-on.png");
	private static final ImageIcon ENGINE_ERROR_DISABLED = LooksFrame.readImageIcon("symbol-light-red-off.png");
	private static final ImageIcon ENGINE_STATUS_OK = LooksFrame.readImageIcon("symbol-light-treemenu-green-on.png");
	private static final AnimatedIconFrame[] RED_FLASHING_FRAMES;
	private static final AnimatedIconFrame[] SMALL_RED_FLASHING_FRAMES;
	private static final AnimatedIconFrame[] SMALL_AMBER_FLASHING_FRAMES;

	/**
	 * Represents the width in pixels of 100 dialog units in the current
	 * environment
	 */
	protected static final int DLU_100X = LooksFrame.getDLU100x();

	/**
	 * Represents the height in pixels of 100 dialog units in the current
	 * environment
	 */
	protected static final int DLU_100Y = LooksFrame.getDLU100y();

	private final Player player;
	private ComponentOrientation orientation;
	private final JComponent engineSettings;
	private final JButton selectPath = new JButton("...");
	private final PmsConfiguration configuration;
	private final CardListenerRegistrar cardListenerRegistrar;
	private final GenericsComboBoxModel<ProgramExecutableType> executableTypeModel = new GenericsComboBoxModel<ProgramExecutableType>();
	private final DefaultTextField enginePath = new DefaultTextField("", true);
	private final AnimatedButton selectedLight = new AnimatedButton();
	private final AnimatedButton statusLight = new AnimatedButton();
	private final JTextField status = new JTextField();
	private final JTextField currentExecutableType = new JTextField();
	private final JLabel versionLabel = new JLabel();
	private final JTextField version = new JTextField();

	private final JTextArea selectionInfo = new JTextArea();
	private AnimatedIcon engineErrorActive;
	private AnimatedIcon engineStatusError;
	private AnimatedIcon engineStatusMissing;

	static {
		ArrayList<AnimatedIconFrame> tempFrames = new ArrayList<>();
		tempFrames.addAll(Arrays.asList(AnimatedIcon.buildAnimation("symbol-light-red-F%d.png", 0, 16, true, 2240, 300, 15)));
		tempFrames.remove(15);
		tempFrames.remove(13);
		tempFrames.remove(12);
		tempFrames.remove(10);
		tempFrames.remove(9);
		tempFrames.remove(7);
		tempFrames.remove(6);
		tempFrames.remove(4);
		tempFrames.remove(3);
		tempFrames.remove(1);
		RED_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);

		tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-red-F%d.png", 0, 7, true, 15, 800, 15
		)));
		tempFrames.add(0, new AnimatedIconFrame(LooksFrame.readImageIcon("symbol-light-treemenu-red-off.png"), 500));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		SMALL_RED_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);

		tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-amber-F%d.png", 0, 7, true, 15, 800, 15
		)));
		tempFrames.add(0, new AnimatedIconFrame(LooksFrame.readImageIcon("symbol-light-treemenu-amber-off.png"), 500));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		SMALL_AMBER_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);
	}

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param player the {@link Player} this panel is to represent.
	 * @param orientation the current {@link ComponentOrientation}.
	 * @param engineSettings the settings panel for this {@link Player} or
	 *            {@code null}.
	 * @param configuration the {@link PmsConfiguration}.
	 * @param tabListenerRegistrar the {@link TranscodingTabListenerRegistrar}.
	 */
	public EnginePanel(
		@Nonnull Player player,
		@Nonnull ComponentOrientation orientation,
		@Nullable JComponent engineSettings,
		@Nonnull PmsConfiguration configuration,
		@Nullable TranscodingTabListenerRegistrar tabListenerRegistrar
	) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		if (orientation == null) {
			throw new IllegalArgumentException("orientation cannot be null");
		}
		this.player = player;
		this.orientation = orientation;
		this.engineSettings = engineSettings;
		this.configuration = configuration;
		this.cardListenerRegistrar =
			tabListenerRegistrar == null ?
				null :
				new CardListenerRegistrar(tabListenerRegistrar, this);
		build();
	}

	/**
	 * Builds the panel.
	 */
	protected void build() {
		setBorder(new EmptyBorder(DLU_100Y / 40, DLU_100X / 40, DLU_100Y / 40, DLU_100X / 40));
		CellConstraints cc = new CellConstraints();

		JPanel mainPanel = new JPanel(new FormLayout("pref:grow", "3*(pref, 3dlu), 0:grow"));
		mainPanel.setBorder(new EmptyBorder(DLU_100Y / 20, DLU_100X / 20, DLU_100Y / 20, DLU_100X / 20));
		mainPanel.add(buildSelection(cc), cc.xy(1, 1));
		mainPanel.add(buildStatus(cc), cc.xy(1, 3));
		if (engineSettings != null) {
			JPanel engine = new JPanel(new BorderLayout());
			engine.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(
				Messages.getString("Engine.Settings"), player.name())),
				new EmptyBorder(DLU_100Y / 10, DLU_100X / 10, DLU_100Y / 10, DLU_100X / 10)
				));
			engine.add(engineSettings, BorderLayout.CENTER);
			mainPanel.add(engine, cc.xy(1, 5));
		}
		setViewportView(mainPanel);
	}

	/**
	 * Sets the selected engine light to the specified {@link IconState}.
	 *
	 * @param iconState the {@link IconState} to set.
	 */
	protected void setLight(IconState iconState) {
		if (
			iconState == null ||
			selectedLight.getIcon() != null &&
			(
				iconState == IconState.ERROR_ACTIVE && selectedLight.getIcon() == engineErrorActive ||
				iconState == IconState.ERROR && selectedLight.getIcon() == ENGINE_ERROR ||
				iconState == IconState.OK && selectedLight.getIcon() == ENGINE_OK ||
				iconState == IconState.OK_DISABLED && selectedLight.getIcon() == ENGINE_OK_DISABLED ||
				iconState == IconState.ERROR_DISABLED && selectedLight.getIcon() == ENGINE_ERROR_DISABLED
			)
		) {
			return;
		}
		if (selectedLight.getIcon() instanceof AnimatedIcon) {
			AnimatedIcon animatedIcon = (AnimatedIcon) selectedLight.getIcon();
			animatedIcon.stop();
			if (cardListenerRegistrar != null) {
				cardListenerRegistrar.unregister(animatedIcon);
			}
		}

		switch (iconState) {
			case ERROR_ACTIVE:
				if (engineErrorActive == null) {
					engineErrorActive = new AnimatedIcon(selectedLight, true, RED_FLASHING_FRAMES);
				}
				engineErrorActive.start();
				selectedLight.setIcon(engineErrorActive);
				if (cardListenerRegistrar != null) {
					cardListenerRegistrar.register(engineErrorActive);
				}
				break;
			case ERROR:
				selectedLight.setIcon(ENGINE_ERROR);
				break;
			case OK:
				selectedLight.setIcon(ENGINE_OK);
				break;
			case OK_DISABLED:
				selectedLight.setIcon(ENGINE_OK_DISABLED);
				break;
			case ERROR_DISABLED:
			default:
				selectedLight.setIcon(ENGINE_ERROR_DISABLED);
				break;
		}
	}

	/**
	 * Sets or updates the "selection part" of the panel.
	 */
	protected void updateSelection() {
		ProgramExecutableType selectedType = configuration.getPlayerExecutableType(player);
		executableTypeModel.syncWith(player.getProgramInfo().getExecutableTypes());
		executableTypeModel.setSelectedGItem(selectedType);

		Path path = player.getProgramInfo().getPath(selectedType);
		enginePath.setText(path == null ? null : path.toString());
		enginePath.setEditable(selectedType == ProgramExecutableType.CUSTOM);
		enginePath.setDefaultText(selectedType == ProgramExecutableType.CUSTOM ?
			Messages.getString("EnginePanel.ExecutablePathDefaultText") :
			""
		);
		selectPath.setVisible(selectedType == ProgramExecutableType.CUSTOM);
		if (selectedType == ProgramExecutableType.CUSTOM) {
			enginePath.setToolTipText(String.format(Messages.getString("EnginePanel.ExecutablePathCustomToolTip"), player.name()));
		} else {
			enginePath.setToolTipText(String.format(Messages.getString("EnginePanel.ExecutablePathToolTip"), player.name()));
		}

		if (player.isAvailable(selectedType)) {
			if (player.isEnabled()) {
				setLight(IconState.OK);
			} else {
				setLight(IconState.OK_DISABLED);
			}
		} else {
			if (player.isEnabled()) {
				if (player.getCurrentExecutableType() == selectedType) {
					setLight(IconState.ERROR_ACTIVE);
				} else {
					setLight(IconState.ERROR);
				}
			} else {
				setLight(IconState.ERROR_DISABLED);
			}
		}

		selectionInfo.setText(player.getStatusTextFull(selectedType));
	}

	/**
	 * Sets the engine status light to the specified {@link IconState}.
	 *
	 * @param iconState the {@link IconState} to set.
	 */
	protected void setStatusLight(@Nullable IconState iconState) {
		if (iconState == null ||
			statusLight.getIcon() != null &&
			(
				iconState == IconState.OK && statusLight.getIcon() == ENGINE_STATUS_OK ||
				iconState == IconState.ERROR && statusLight.getIcon() == engineStatusError ||
				iconState == IconState.MISSING && statusLight.getIcon() == engineStatusMissing
			)
		) {
			return;
		}
		if (statusLight.getIcon() instanceof AnimatedIcon) {
			AnimatedIcon animatedIcon = (AnimatedIcon) statusLight.getIcon();
			animatedIcon.stop();
			if (cardListenerRegistrar != null) {
				cardListenerRegistrar.unregister(animatedIcon);
			}
		}

		switch (iconState) {
			case ERROR:
			case ERROR_DISABLED:
				if (engineStatusError == null) {
					engineStatusError = new AnimatedIcon(statusLight, true, SMALL_AMBER_FLASHING_FRAMES);
				}
				engineStatusError.start();
				statusLight.setIcon(engineStatusError);
				if (cardListenerRegistrar != null) {
					cardListenerRegistrar.register(engineStatusError);
				}
				break;
			case OK:
			case OK_DISABLED:
				statusLight.setIcon(ENGINE_STATUS_OK);
				break;
			default:
				if (engineStatusMissing == null) {
					engineStatusMissing = new AnimatedIcon(statusLight, true, SMALL_RED_FLASHING_FRAMES);
				}
				engineStatusMissing.start();
				statusLight.setIcon(engineStatusMissing);
				if (cardListenerRegistrar != null) {
					cardListenerRegistrar.register(engineStatusMissing);
				}
				break;
		}
	}

	/**
	 * Sets or updates the "status part" of the panel.
	 */
	protected void updateStatus() {
		ProgramExecutableType current = player.getCurrentExecutableType();
		currentExecutableType.setText(current == null ? Messages.getString("Generic.None") : current.toString());
		String disabled = player.isEnabled() ? null : " (" + Messages.getString("Generic.Disabled").toLowerCase(PMS.getLocale()) + ")";

		if (current != null) {
			ExecutableInfo currentExecutableInfo = player.getProgramInfo().getExecutableInfo(current);
			boolean exists = currentExecutableInfo == null ? false : currentExecutableInfo.isPathExisting();
			versionLabel.setVisible(exists);
			version.setVisible(exists);
			if (exists) {
				if (player.isAvailable()) {
					setStatusLight(IconState.OK);
					if (disabled != null) {
						status.setText(Messages.getString("Generic.OK") + disabled);
					} else {
						status.setText(Messages.getString("Generic.OK"));
					}
				} else {
					setStatusLight(IconState.ERROR);
					if (disabled != null) {
						status.setText(Messages.getString("Generic.Error") + disabled);
					} else {
						status.setText(Messages.getString("Generic.Error"));
					}
				}
				@SuppressWarnings("null")
				Version executableVersion = currentExecutableInfo.getVersion();
				if (executableVersion != null) {
					version.setText(executableVersion.getVersionString());
				} else {
					version.setText(Messages.getString("Generic.Unknown"));
				}
			} else {
				setStatusLight(IconState.MISSING);
				if (currentExecutableInfo == null || currentExecutableInfo.getPath() == null) {
					if (disabled != null) {
						status.setText(Messages.getString("Generic.Undefined") + disabled);
					} else {
						status.setText(Messages.getString("Generic.Undefined"));
					}
				} else {
					if (disabled != null) {
						status.setText(Messages.getString("Generic.Missing") + disabled);
					} else {
						status.setText(Messages.getString("Generic.Missing"));
					}
				}
			}
		} else {
			setStatusLight(IconState.MISSING);
			if (disabled != null) {
				status.setText(Messages.getString("Generic.NA") + disabled);
			} else {
				status.setText(Messages.getString("Generic.NA"));
			}
			versionLabel.setVisible(false);
			version.setVisible(false);
		}
	}

	/**
	 * Sets or updated the whole panel.
	 */
	public void updatePanel() {
		updateSelection();
		updateStatus();
	}

	/**
	 * Builds the "selection part" of the panel.
	 *
	 * @param cc the {@link CellConstraints} to use.
	 * @return The built panel.
	 */
	protected JPanel buildSelection(@Nonnull CellConstraints cc) {
		FormLayout selectionLayout = new FormLayout(FormLayoutUtil.getColSpec(SELECTION_COL_SPEC, orientation), SELECTION_ROW_SPEC);

		JPanel selection = new JPanel(selectionLayout);

		selection.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(
			Messages.getString("Engine.Selection"), player.name())), new EmptyBorder(DLU_100Y / 10, DLU_100X / 10, DLU_100Y / 10,
			DLU_100X / 10)));

		JLabel engineTypeLabel = new JLabel(Messages.getString("EnginePanel.ExecutableType"));
		updateSelection();
		JComboBox<ProgramExecutableType> executableType = new JComboBox<ProgramExecutableType>(executableTypeModel);
		executableType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (configuration.setPlayerExecutableType(player, executableTypeModel.getSelectedGItem())) {
						updatePanel();
					}
				}
			}
		});
		if (executableType.getPreferredSize().width < DLU_100X / 2) {
			executableType.setPreferredSize(new Dimension(DLU_100X / 2, executableType.getPreferredSize().height));
		}
		engineTypeLabel.setLabelFor(executableType);
		executableType.setToolTipText(Messages.getString("EnginePanel.ExecutableTypeToolTip"));
		selection.add(engineTypeLabel, cc.xy(1, 1));
		selection.add(executableType, cc.xy(3, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

		JLabel enginePathLabel = new JLabel(Messages.getString("EnginePanel.ExecutablePath"));

		selectPath.setPreferredSize(new Dimension(DLU_100X / 3, selectPath.getPreferredSize().height));
		selectPath.setToolTipText(String.format(Messages.getString("EnginePanel.ExecutablePathBrowseToolTip"), player.name()));
		selectPath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				File currentFolder;
				if (isBlank(enginePath.getText())) {
					currentFolder = null;
				} else {
					currentFolder = new File(enginePath.getText());
					if (!currentFolder.isDirectory()) {
						currentFolder = currentFolder.getParentFile();
					}
				}
				chooser = new JFileChooser(currentFolder);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(new ExecutableFileFilter());
				int result = chooser.showDialog((Component) e.getSource(), "Select executable");
				if (result == JFileChooser.APPROVE_OPTION) {
					Path path = chooser.getSelectedFile().toPath();
					player.setCustomExecutablePath(path, true);
					enginePath.setText(path.toString());
					updatePanel();
				}
			}
		});
		enginePathLabel.setLabelFor(enginePath);
		selection.add(enginePathLabel, cc.xy(1, 3));
		enginePath.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				String pathString = enginePath.getText();
				Path path;
				try {
					path = isBlank(pathString) ? null : Paths.get(pathString);
				} catch (InvalidPathException e2) {
					path = null;
				}
				player.setCustomExecutablePath(path, true);
				if (path == null) {
					enginePath.setText(null);
				}
				updatePanel();
			}
		});
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.LINE_AXIS));
		pathPanel.add(enginePath);
		pathPanel.add(selectPath);
		selection.add(pathPanel, cc.xy(3, 3));

		selectedLight.setFocusable(false);
		selectedLight.setToolTipText(String.format(Messages.getString("EnginePanel.SelectedLight"), player.name()));
		selection.add(selectedLight, cc.xy(1, 5, CellConstraints.CENTER, CellConstraints.CENTER));

		selectionInfo.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), new EmptyBorder(DLU_100Y / 20, DLU_100X / 15,
			DLU_100Y / 15, DLU_100X / 15)));
		selectionInfo.setEditable(false);
		selectionInfo.setLineWrap(true);
		selectionInfo.setWrapStyleWord(true);

		selection.add(selectionInfo, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.FILL));

		return selection;
	}

	/**
	 * Builds the "status part" of the panel.
	 *
	 * @param cc the {@link CellConstraints} to use.
	 * @return The built panel.
	 */
	protected JPanel buildStatus(CellConstraints cc) {

		FormLayout statusLayout = new FormLayout(FormLayoutUtil.getColSpec(STATUS_COL_SPEC, orientation), STATUS_ROW_SPEC);

		JPanel statusPanel = new JPanel(statusLayout);
		statusPanel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(
			Messages.getString("Engine.Status"), player.name())), new EmptyBorder(DLU_100Y / 15, DLU_100X / 10, DLU_100Y / 15,
			DLU_100X / 10)));

		updateStatus();
		statusLight.setToolTipText(String.format(Messages.getString("EnginePanel.StatusLightToolTop"), player.name()));
		statusPanel.add(statusLight, cc.xy(1, 1));
		statusPanel.add(new JLabel(Messages.getString("EnginePanel.Status")), cc.xy(3, 1));
		status.setEditable(false);
		status.setToolTipText(String.format(Messages.getString("EnginePanel.StatusToolTip"), player.name()));
		statusPanel.add(status, cc.xy(5, 1));

		statusPanel.add(new JLabel(Messages.getString("EnginePanel.ActiveExecutable")), cc.xy(7, 1));

		currentExecutableType.setEditable(false);
		currentExecutableType.setToolTipText(String.format(Messages.getString("EnginePanel.ActiveExecutableToolTip"), player.name()));
		statusPanel.add(currentExecutableType, cc.xy(9, 1));

		versionLabel.setText(Messages.getString("EnginePanel.Version"));
		version.setEditable(false);
		version.setToolTipText(String.format(Messages.getString("EnginePanel.VersionToolTip"), player.name()));
		statusPanel.add(versionLabel, cc.xy(11, 1));
		statusPanel.add(version, cc.xy(13, 1));

		return statusPanel;
	}

	/**
	 * @return the {@link Player} this panel represents.
	 */
	@Nonnull
	public Player getPlayer() {
		return player;
	}

	/**
	 * This is a {@link FileFilter} implementation that filters out executable
	 * files only for the current platform.
	 *
	 * @author Nadahar
	 */
	protected static class ExecutableFileFilter extends FileFilter {

		private static final ArrayList<String> EXTENSIONS = new ArrayList<>();

		static {
			if (Platform.isWindows()) {
				for (String s : WindowsProgramPaths.getWindowsPathExtensions()) {
					if (isNotBlank(s)) {
						EXTENSIONS.add(s.toLowerCase(Locale.ROOT));
					}
				}
			} else if (Platform.isMac()) {
				EXTENSIONS.add(null);
				EXTENSIONS.addAll(Arrays.asList("action", "app", "bin", "command", "csh", "osx", "workflow"));
			} else {
				EXTENSIONS.add(null);
				EXTENSIONS.addAll(Arrays.asList("bin", "ksh", "out", "run", "sh"));
			}
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			String extenstion = FileUtil.getExtension(file);
			if (isBlank(extenstion)) {
				extenstion = null;
			} else {
				extenstion = extenstion.toLowerCase(Locale.ROOT);
			}
			return EXTENSIONS.contains(extenstion);
		}

		@Override
		public String getDescription() {
			return "Executable files";
		}

	}

	private static enum IconState {
		OK, OK_DISABLED, ERROR, ERROR_ACTIVE, ERROR_DISABLED, MISSING;
	}

	/**
	 * This is a hybrid between an {@link AnimatedIconListenerRegistrar} and an
	 * {@link AnimatedIconListener} , although it doesn't declare the latter as
	 * the implementation is simplified and doesn't involve
	 * {@link AnimatedIconListenerAction}.
	 * <p>
	 * This will handle everything related to suspension and unsuspension of an
	 * {@link AnimatedIcon} placed on one of the {@link EnginePanel} "cards".
	 * <p>
	 * It registers as a {@link ComponentListener} on the {@link EnginePanel}
	 * itself and suspends registered {@link AnimatedIcon} as the visibility of
	 * the panel changes. In addition, it registers with the specified
	 * {@link TranscodingTabListenerRegistrar} which handles tab changes and
	 * application minimize events.
	 *
	 * @author Nadahar
	 */
	private static class CardListenerRegistrar extends ComponentAdapter implements AnimatedIconListenerRegistrar {

		@Nonnull
		private final TranscodingTabListenerRegistrar tabListenerRegistrar;
		@Nonnull
		private final EnginePanel enginePanel;
		private final HashSet<AnimatedIcon> subscribers = new HashSet<>();
		private boolean isListening;

		/**
		 * Creates a new instance using the specified parameters.
		 *
		 * @param tabListenerRegistrar the
		 *            {@link TranscodingTabListenerRegistrar} to register with.
		 * @param enginePanel the {@link EnginePanel} to listen to.
		 */
		public CardListenerRegistrar(
			@Nonnull TranscodingTabListenerRegistrar tabListenerRegistrar,
			@Nonnull EnginePanel enginePanel
		) {
			if (tabListenerRegistrar == null) {
				throw new IllegalArgumentException("tabListenerRegistrar cannot be null");
			}
			this.tabListenerRegistrar = tabListenerRegistrar;
			this.enginePanel = enginePanel;
		}

		@Override
		public void componentShown(ComponentEvent e) {
			for (AnimatedIcon subscriber : subscribers) {
				subscriber.unsuspend();
			}
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			for (AnimatedIcon subscriber : subscribers) {
				subscriber.suspend();
			}
		}

		/**
		 * Registers the specified {@link AnimatedIcon} with both a
		 * {@link WindowIconifyListener}, a
		 * {@link LooksFrameTabModelChangeListener} and starts acting on
		 * visibility changes for the defined {@link EnginePanel}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to register.
		 */
		public void register(AnimatedIcon animatedIcon) {
			tabListenerRegistrar.register(animatedIcon);
			subscribers.add(animatedIcon);
			if (!enginePanel.isVisible()) {
				animatedIcon.suspend();
			}
			if (!isListening) {
				enginePanel.addComponentListener(this);
				isListening = true;
			}
		}

		/**
		 * Unregisters the specified {@link AnimatedIcon} with both a
		 * {@link WindowIconifyListener}, a
		 * {@link LooksFrameTabModelChangeListener} and stops acting on
		 * visibility changes for the defined {@link EnginePanel}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to unregister.
		 */
		public void unregister(AnimatedIcon animatedIcon) {
			tabListenerRegistrar.unregister(animatedIcon);
			subscribers.remove(animatedIcon);
			if (isListening && subscribers.size() == 0) {
				enginePanel.removeComponentListener(this);
				isListening = false;
			}
		}
	}
}
