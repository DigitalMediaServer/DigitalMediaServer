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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.ProgramExecutableType;
import net.pms.configuration.WindowsProgramPaths;
import net.pms.encoders.Player;
import net.pms.newgui.components.AnimatedButton;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.DefaultTextField;
import net.pms.newgui.components.GenericsComboBoxModel;
import net.pms.util.FileUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.Version;


public class EnginePanel extends JScrollPane {

	private static final long serialVersionUID = 1L;

	private static final String SELECTION_COL_SPEC = "left:pref, $lcgap, fill:50dlu:grow";
	private static final String SELECTION_ROW_SPEC = "pref, 3dlu, pref, 6dlu, pref";
	private static final String STATUS_COL_SPEC = "left:pref, $lcgap, 5*(pref, $lcgap), pref, 0:grow";
	private static final String STATUS_ROW_SPEC = "pref";
	private static final ImageIcon ENGINE_OK = LooksFrame.readImageIcon("symbol-light-green-on.png");
	private static final ImageIcon ENGINE_OK_DISABLED = LooksFrame.readImageIcon("symbol-light-green-off.png");
	private static final ImageIcon ENGINE_ERROR = LooksFrame.readImageIcon("symbol-light-red-on.png");
	private static final ImageIcon ENGINE_ERROR_DISABLED = LooksFrame.readImageIcon("symbol-light-red-off.png");
	private static final ImageIcon ENGINE_STATUS_OK = LooksFrame.readImageIcon("symbol-light-treemenu-green-on.png");
	private static final AnimatedIconFrame[] RED_FLASHING_FRAMES;
	private static final AnimatedIconFrame[] SMALL_RED_FLASHING_FRAMES;
	private static final AnimatedIconFrame[] SMALL_AMBER_FLASHING_FRAMES;

	protected final int dlu100x = LooksFrame.getDLU100x();
	protected final int dlu100y = LooksFrame.getDLU100y();

	protected final Player player; //TODO: (Nad) private/protected
	protected ComponentOrientation orientation;
	protected final JComponent engineSettings;
	protected final JButton selectPath = new JButton("...");
	protected final PmsConfiguration configuration;
	protected final GenericsComboBoxModel<ProgramExecutableType> executableTypeModel = new GenericsComboBoxModel<ProgramExecutableType>();
	protected final DefaultTextField enginePath = new DefaultTextField("Enter path to executable", true);;
	protected final AnimatedButton selectedLight = new AnimatedButton();
	protected final AnimatedButton statusLight = new AnimatedButton();
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
		tempFrames.add(new AnimatedIconFrame(LooksFrame.readImageIcon("symbol-light-red-off.png"), 2000));
		tempFrames.addAll(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-red-F%d.png", 0, 7, false, 8, 20, 8
		)));
		tempFrames.addAll(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-red-F%d.png", 6, 1, false, 10, 40, 20
		)));
		tempFrames.set(13, new AnimatedIconFrame(tempFrames.get(13).getIcon(), 35));
		tempFrames.set(12, new AnimatedIconFrame(tempFrames.get(12).getIcon(), 30));
		tempFrames.set(11, new AnimatedIconFrame(tempFrames.get(11).getIcon(), 25));
		RED_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);

		tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-red-F%d.png", 0, 7, true, 15, 800, 15))
		);
		tempFrames.add(0, new AnimatedIconFrame(
			LooksFrame.readImageIcon("symbol-light-treemenu-red-off.png"),
			500
		));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		SMALL_RED_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);

		tempFrames = new ArrayList<>(Arrays.asList(AnimatedIcon.buildAnimation(
			"symbol-light-treemenu-amber-F%d.png", 0, 7, true, 15, 800, 15))
		);
		tempFrames.add(0, new AnimatedIconFrame(
			LooksFrame.readImageIcon("symbol-light-treemenu-amber-off.png"),
			500
		));
		tempFrames.remove(7);
		tempFrames.remove(5);
		tempFrames.remove(3);
		tempFrames.remove(1);
		SMALL_AMBER_FLASHING_FRAMES = tempFrames.toArray(new AnimatedIconFrame[tempFrames.size()]);
	}

	public EnginePanel(
		@Nonnull Player player,
		@Nonnull ComponentOrientation orientation,
		@Nullable JComponent engineSettings,
		@Nonnull PmsConfiguration configuration
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
		initialize();
	}

	protected void initialize() {

		setBorder(new EmptyBorder(5, 5, 5, 5)); //TODO: (Nad) DLU
//		setLayout(new BorderLayout());
		CellConstraints cc = new CellConstraints();

		JPanel mainPanel = new JPanel(new FormLayout("pref:grow", "3*(pref, 3dlu), 0:grow"));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); //TODO: (Nad) DLU
		mainPanel.add(buildSelection(cc), cc.xy(1, 1));
		mainPanel.add(buildStatus(cc), cc.xy(1, 3));
		if (engineSettings != null) {
			JPanel engine = new JPanel(new BorderLayout());
			engine.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(Messages.getString("Engine.Settings"), player.name())),
				new EmptyBorder(15, 15, 15, 15) //TODO: (Nad) DLU
			));
			engine.add(engineSettings, BorderLayout.CENTER);
			mainPanel.add(engine, cc.xy(1, 5));
		}
//		JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		add(scrollPane);

		setViewportView(mainPanel);
	}

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
			((AnimatedIcon) selectedLight.getIcon()).stop(); //TODO: Unregister?
		}

		switch (iconState) {
			case ERROR_ACTIVE:
				if (engineErrorActive == null) {
					engineErrorActive = new AnimatedIcon(selectedLight, true, null, RED_FLASHING_FRAMES); //TODO: (Nad) Registrar
				}
				engineErrorActive.start();
				selectedLight.setIcon(engineErrorActive);
				break;
			case ERROR:
				selectedLight.setIcon(ENGINE_ERROR);
				break;
			case OK:
				selectedLight.setIcon(ENGINE_OK);
				break;
			case OK_DISABLED:
				statusLight.setIcon(ENGINE_OK_DISABLED);
				break;
			case ERROR_DISABLED:
			default:
				selectedLight.setIcon(ENGINE_ERROR_DISABLED);
				break;
		}
	}

	protected void updateSelection() {
		ProgramExecutableType selectedType = configuration.getPlayerExecutableType(player);
		executableTypeModel.syncWith(player.getProgramInfo().getExecutableTypes());
		executableTypeModel.setSelectedGItem(selectedType);

		Path path = player.getProgramInfo().getPath(selectedType);
		enginePath.setText(path == null ? null : path.toString());
		enginePath.setEditable(selectedType == ProgramExecutableType.CUSTOM);
		selectPath.setVisible(selectedType == ProgramExecutableType.CUSTOM);

		if (player.isAvailable(selectedType)) {
			if (player.isEnabled()) {
				selectedLight.setIcon(ENGINE_OK);
			} else {
				selectedLight.setIcon(ENGINE_OK_DISABLED);
			}
		} else {
			if (player.isEnabled()) {
				if (player.getCurrentExecutableType() == selectedType) {
					setLight(IconState.ERROR_ACTIVE);
				} else {
					selectedLight.setIcon(ENGINE_ERROR);
				}
			} else {
				selectedLight.setIcon(ENGINE_ERROR_DISABLED);
			}
		}

		selectionInfo.setText(player.getStatusText(selectedType));
	}

	protected void setStatusLight(@Nullable IconState iconState) {
		if (
			iconState == null ||
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
			((AnimatedIcon) statusLight.getIcon()).stop(); //TODO: Unregister?
		}

		switch (iconState) {
			case ERROR:
			case ERROR_DISABLED:
				if (engineStatusError == null) {
					engineStatusError = new AnimatedIcon(statusLight, true, null, SMALL_AMBER_FLASHING_FRAMES); //TODO: (Nad) Registrar
				}
				engineStatusError.start();
				statusLight.setIcon(engineStatusError);
				break;
			case OK:
			case OK_DISABLED:
				statusLight.setIcon(ENGINE_STATUS_OK);
				break;
			default:
				if (engineStatusMissing == null) {
					engineStatusMissing = new AnimatedIcon(statusLight, true, null, SMALL_RED_FLASHING_FRAMES); //TODO: (Nad) Registrar
				}
				engineStatusMissing.start();
				statusLight.setIcon(engineStatusMissing);
				break;
		}
	}

	protected void updateStatus() {
		ProgramExecutableType current = player.getCurrentExecutableType();
		currentExecutableType.setText(current == null ? Messages.getString("Generic.None") : current.toString());

		if (current != null) {
			ExecutableInfo currentExecutableInfo = player.getProgramInfo().getExecutableInfo(current);
			boolean exists = currentExecutableInfo == null ? false : currentExecutableInfo.isPathExisting();
			versionLabel.setVisible(exists);
			version.setVisible(exists);
			if (exists) {
				if (player.isAvailable()) {
					setStatusLight(IconState.OK);
					status.setText(Messages.getString("Generic.OK"));
				} else {
					setStatusLight(IconState.ERROR);
					status.setText(Messages.getString("Generic.Error"));
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
					status.setText(Messages.getString("Generic.Undefined"));
				} else {
					status.setText(Messages.getString("Generic.Missing"));
				}
			}
		} else {
			setStatusLight(IconState.MISSING);
			status.setText(Messages.getString("Generic.NA"));
			versionLabel.setVisible(false);
			version.setVisible(false);
		}
	}

	public void updatePanel() {
		updateSelection();
		updateStatus();
	}

	protected JPanel buildSelection(@Nonnull CellConstraints cc) {
		FormLayout selectionLayout = new FormLayout(
			FormLayoutUtil.getColSpec(SELECTION_COL_SPEC, orientation),
			SELECTION_ROW_SPEC
		);

		JPanel selection = new JPanel(selectionLayout);

		selection.setBorder(new CompoundBorder(
			new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(Messages.getString("Engine.Selection"), player.name())),
			new EmptyBorder(dlu100y / 10, dlu100x / 10, dlu100y / 10, dlu100x / 10)
		));

		JLabel engineTypeLabel = new JLabel("Executable type:"); //TODO: (Nad) Translations
		updateSelection();
		JComboBox<ProgramExecutableType> executableType = new JComboBox<ProgramExecutableType>(executableTypeModel);
		executableType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setPlayerExecutableType(player, executableTypeModel.getSelectedGItem());
					updatePanel();
				}
			}
		});
		if (executableType.getPreferredSize().width < dlu100x / 2) {
			executableType.setPreferredSize(new Dimension(dlu100x / 2, executableType.getPreferredSize().height));
		}
		engineTypeLabel.setLabelFor(executableType);
		selection.add(engineTypeLabel, cc.xy(1, 1));
		selection.add(executableType, cc.xy(3, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

		JLabel enginePathLabel = new JLabel("Path to executable:");

		selectPath.setPreferredSize(new Dimension(dlu100x / 3, selectPath.getPreferredSize().height));
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
		selection.add(selectedLight, cc.xy(1, 5, CellConstraints.CENTER, CellConstraints.CENTER));

		selectionInfo.setBorder(new CompoundBorder(
			new EtchedBorder(EtchedBorder.LOWERED),
			new EmptyBorder(dlu100y / 20, dlu100x / 15, dlu100y / 15, dlu100x / 15)
		));
		selectionInfo.setEditable(false);
		selectionInfo.setLineWrap(true);
		selectionInfo.setWrapStyleWord(true);

		selection.add(selectionInfo, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.FILL));

		return selection;
	}

	protected JPanel buildStatus(CellConstraints cc) {

		FormLayout statusLayout = new FormLayout(
			FormLayoutUtil.getColSpec(STATUS_COL_SPEC, orientation),
			STATUS_ROW_SPEC
		);

		JPanel status = new JPanel(statusLayout);
		status.setBorder(new CompoundBorder(
			new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(Messages.getString("Engine.Status"), player.name())),
			new EmptyBorder(dlu100y / 15, dlu100x / 10, dlu100y / 15, dlu100x / 10)
		));

		updateStatus();
		status.add(statusLight, cc.xy(1, 1));
		status.add(new JLabel("Status:"), cc.xy(3, 1));
		this.status.setEditable(false);
		status.add(this.status, cc.xy(5, 1));

		status.add(new JLabel("Effective type:"), cc.xy(7, 1));

		currentExecutableType.setEditable(false);
		status.add(currentExecutableType, cc.xy(9, 1));

		versionLabel.setText("Version:");
		version.setEditable(false);
		status.add(versionLabel, cc.xy(11, 1));
		status.add(version, cc.xy(13, 1));

		return status;
	}

	@Nonnull
	public Player getPlayer() {
		return player;
	}

	protected static class ExecutableFileFilter extends FileFilter {

		protected final static ArrayList<String> extensions = new ArrayList<>();

		static {
			if (Platform.isWindows()) {
				for (String s : WindowsProgramPaths.getWindowsPathExtensions()) {
					if (isNotBlank(s)) {
						extensions.add(s.toLowerCase(Locale.ROOT));
					}
				}
			} else if (Platform.isMac()) {
				extensions.add(null);
				extensions.addAll(Arrays.asList(
					"action", "app", "bin", "command", "csh", "osx", "workflow"
				));
			} else {
				extensions.add(null);
				extensions.addAll(Arrays.asList(
					"bin", "ksh", "out", "run", "sh"
				));
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
			return extensions.contains(extenstion);
		}

		@Override
		public String getDescription() {
			return "Executable files";
		}

	}

	private static enum IconState {
		OK,
		OK_DISABLED,
		ERROR,
		ERROR_ACTIVE,
		ERROR_DISABLED,
		MISSING;
	}
}
