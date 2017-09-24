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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.sun.jna.Platform;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.ProgramExecutableType;
import net.pms.configuration.WindowsProgramPaths;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerId;
import net.pms.newgui.components.DefaultTextField;
import net.pms.util.FileUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.KeyedComboBoxModel;
import net.pms.util.Version;


public class EngineSelection extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String SELECTION_COL_SPEC = "left:pref, $lcgap, pref, 0px, " + FormSpecs.BUTTON_COLSPEC + ", $lcgap, pref:grow";
	private static final String SELECTION_ROW_SPEC = "4*(pref, 3dlu), pref, 9dlu, pref, 9dlu:grow, pref";
	private static final String STATUS_COL_SPEC = "left:pref, $lcgap, pref, $lcgap, pref, $lcgap, pref:grow";
	private static final String STATUS_ROW_SPEC = "4*(pref, 3dlu), pref, 9dlu, pref, 9dlu:grow, pref";

	protected final Player player;
	protected ComponentOrientation orientation;
	protected DefaultTextField enginePath;
	protected JButton selectPath;

	public EngineSelection(@Nonnull Player player, @Nonnull ComponentOrientation orientation) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		if (orientation == null) {
			throw new IllegalArgumentException("orientation cannot be null");
		}
		this.player = player;
		this.orientation = orientation;
		initialize();
	}

	protected void initialize() {
		int dlu100x = LooksFrame.getDLU100x();
		int dlu100y = LooksFrame.getDLU100y();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(new EmptyBorder(0, 0, 0, 0));

		String colSpec = FormLayoutUtil.getColSpec(SELECTION_COL_SPEC, orientation);
		FormLayout selectionLayout = new FormLayout(colSpec, SELECTION_ROW_SPEC);

		CellConstraints cc = new CellConstraints();

		JPanel selection = new JPanel(selectionLayout);

		selection.setBorder(new CompoundBorder(
			new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(Messages.getString("Engine.Selection"), player.name())),
			new EmptyBorder(dlu100y / 10, dlu100x / 10, dlu100y / 10, dlu100x / 10) //TODO (Nad) DLU?
		));

		JLabel engineTypeLabel = new JLabel("Executable type:");
		selection.add(engineTypeLabel, cc.xy(1, 1));

		JComboBox<ProgramExecutableType> test = new JComboBox<ProgramExecutableType>(player.getProgramInfo().getExecutablesTypes().toArray(new ProgramExecutableType[player.getProgramInfo().getExecutablesTypes().size()]));
		test.setSelectedItem(PMS.getConfiguration().getConfiguredExecutableType(player));
		selection.add(test, cc.xy(3, 1));

		JLabel enginePathLabel = new JLabel("Engine executable path:");
		selection.add(enginePathLabel, cc.xy(1, 3));
		enginePath = new DefaultTextField(player.getExecutable(), "Enter path to executable", true);
		selection.add(enginePath, cc.xy(3, 3));

		selectPath = new JButton("...");
		selectPath.setRequestFocusEnabled(false);
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
				int result = chooser.showDialog((Component) e.getSource(), "Set executable");
				if (result == JFileChooser.APPROVE_OPTION) {
					enginePath.setText(chooser.getSelectedFile().getAbsolutePath());
					//PMS.getConfiguration().getFFmpegPaths()
					//TODO: (Nad) Apply?
				}
			}
		});
		selection.add(selectPath, cc.xy(5, 3));

		add(selection);

		colSpec = FormLayoutUtil.getColSpec(STATUS_COL_SPEC, orientation);
		FormLayout statusLayout = new FormLayout(colSpec, STATUS_ROW_SPEC);

		JPanel status = new JPanel(statusLayout);
		status.setBorder(new CompoundBorder(
			new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), String.format(Messages.getString("Engine.Status"), player.name())),
			new EmptyBorder(dlu100y / 10, dlu100x / 10, dlu100y / 10, dlu100x / 10)
		));

		status.add(new JLabel("Effective executable type:"), cc.xy(1, 1));
		status.add(new JLabel(player.getCurrentExecutableType().toString()), cc.xy(3, 1));

		Version version = player.getProgramInfo().getExecutableInfo(player.getCurrentExecutableType()).getVersion();
		if (version != null) {
			status.add(new JLabel("Version:"), cc.xy(5, 1));
			status.add(new JLabel(version.getVersionString()), cc.xy(7, 1));
		}

		add(status);
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

}
