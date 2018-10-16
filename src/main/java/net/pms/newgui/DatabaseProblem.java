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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.database.TableManager;
import net.pms.database.Tables;
import net.pms.newgui.components.CustomJButton;
import net.pms.newgui.components.DefaultTextField;
import net.pms.util.FormLayoutUtil;
import net.pms.util.SwingUtils;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * This class creates and handles the database problem dialog.
 *
 * @author Nadahar
 */
public class DatabaseProblem {

	private JOptionPane pane;
	private JPanel rootPanel = new JPanel();
	private JPanel infoPanel = new JPanel();
	private JPanel pathPanel = new JPanel();
	private JButton renameButton = new JButton();
	private JButton backupDowngradeButton = new JButton();
	private JButton downgradeButton = new JButton();
	private JButton continueButton = new JButton();
	private JButton cancelCloseButton = new JButton();
	private JTextArea descriptionText = new JTextArea();
	private JTextArea warningText = new JTextArea();
	private JEditorPane errorText = new JEditorPane();
	private DefaultTextField databasePath = new DefaultTextField();
	private JLabel newDatabasePathLabel = new JLabel(Messages.getString("DatabaseProblem.NewDatabasePath"));
	private DefaultTextField newDatabasePath = new DefaultTextField();
	private CustomJButton newDatabasePathBrowse = new CustomJButton("    ...    ");
	private JFileChooser fileChooser;

	@Nonnull
	private final ComponentOrientation orientation;

	@Nullable
	private final Component parentComponent;

	@Nonnull
	private final TableManager tableManager;

	@Nonnull
	private final Locale locale;

	private int textWidth;
	private JDialog dialog;
	private boolean aborted = false;

	/**
	 * Creates a new instance.
	 *
	 * @param parentComponent the parent {@link Frame} or {@link Window} or
	 *            {@code null} to make it an orphan.
	 * @param tableManager the {@link TableManager} instance.
	 */
	public DatabaseProblem(@Nullable Component parentComponent, @Nonnull TableManager tableManager) {
		if (PMS.isHeadless()) {
			throw new IllegalStateException("DatabaseProblem dialog can't run in headless mode");
		}
		this.parentComponent = parentComponent;
		this.tableManager = tableManager;
		this.locale = PMS.getLocale();

		// Apply the orientation for the locale
		this.orientation = ComponentOrientation.getOrientation(locale);

		// Initialize the file chooser dialog
		try {
			fileChooser = new JFileChooser();
		} catch (Exception e) {
			fileChooser = new JFileChooser(new RestrictedFileSystemView());
		}
	}

	/**
	 * Creates and displays the "Database problem" dialog. Blocks until the
	 * dialog is closed, after which it is disposed of and the aborted status is
	 * set.
	 */
	public void show() {
		pane = new JOptionPane(
			buildComponent(),
			JOptionPane.ERROR_MESSAGE,
			JOptionPane.DEFAULT_OPTION,
			null,
			new JButton[]{renameButton, backupDowngradeButton, downgradeButton, cancelCloseButton, continueButton},
			cancelCloseButton
		);
		pane.setComponentOrientation(ComponentOrientation.getOrientation(locale));
		dialog = pane.createDialog(parentComponent, PMS.getName());
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setIconImage(LooksFrame.readImageIcon("icon-32.png").getImage());
		update();
		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setVisible(true);
		dialog.dispose();

		if (!"continue".equals(pane.getValue())) {
			aborted = true;
		}
	}

	private void update() {
		dialog.setLocale(locale);
		dialog.applyComponentOrientation(ComponentOrientation.getOrientation(locale));

		databasePath.setText(tableManager.getDatabaseFilepath(false));

		renameButton.setActionCommand("rename");
		backupDowngradeButton.setActionCommand("backupDowngrade");
		downgradeButton.setActionCommand("downgrade");
		cancelCloseButton.setActionCommand("cancel");

		if (tableManager.hasFutureTables()) {
			int tables = tableManager.getFutureTables(false, false, false).size();
			StringBuilder description = new StringBuilder(String.format(
				locale,
				Messages.getString(tables == 1 ? "DatabaseProblem.FutureTable" : "DatabaseProblem.FutureTables"),
				tableManager.getFutureTablesString(false, false)
			));
			description.append("\n\n").append(Messages.getString("DatabaseProblem.Resolve"));
			descriptionText.setText(description.toString());

			renameButton.setEnabled(true);
			backupDowngradeButton.setEnabled(true);
			backupDowngradeButton.setVisible(true);
			downgradeButton.setEnabled(true);
			downgradeButton.setVisible(true);
			cancelCloseButton.setText(Messages.getString("Dialog.Cancel"));
			continueButton.setEnabled(false);
			StringBuilder sb = new StringBuilder();
			String tablesString;
			if (tableManager.hasFutureTablesRelated()) {
				sb.append(Messages.getString(tables == 1 ?
					"DatabaseProblem.AffectRelated" :
					"DatabaseProblem.AffectRelatedPlural")
				).append(" ");
				tables++;
				tablesString = tableManager.getFutureTablesString(true, false);
			} else {
				tablesString = tableManager.getFutureTablesString(false, false);
			}
			sb.append(Messages.getString(tables == 1 ?
				"DatabaseProblem.DowngradeWarning" :
				"DatabaseProblem.DowngradeWarningPlural")
			);
			warningText.setText(String.format(locale, sb.toString(), tablesString));
			warningText.setVisible(true);
		} else if (tableManager.isError()) {
			if (tableManager.isAlreadyOpenError()) {
				descriptionText.setText(Messages.getString("DatabaseProblem.AlreadyOpen"));
				renameButton.setText(Messages.getString("Dialog.Retry"));
				renameButton.setActionCommand("retry");
			} else if (tableManager.isWrongVersionOrCorrupt()) {
				descriptionText.setText(Messages.getString("DatabaseProblem.Corrupt"));
			} else {
				descriptionText.setText(Messages.getString("DatabaseProblem.Error"));
			}
			renameButton.setEnabled(true);
			downgradeButton.setVisible(false);
			backupDowngradeButton.setVisible(false);
			cancelCloseButton.setText(Messages.getString("Dialog.Close"));
			continueButton.setEnabled(false);
			warningText.setVisible(false);
		} else {
			pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			descriptionText.setText(Messages.getString("DatabaseProblem.NoProblem"));
			warningText.setVisible(false);
			errorText.setVisible(false);
			renameButton.setEnabled(false);
			backupDowngradeButton.setEnabled(false);
			downgradeButton.setEnabled(false);
			continueButton.setEnabled(true);
			JRootPane rootPane = SwingUtilities.getRootPane(continueButton);
			rootPane.setDefaultButton(continueButton);
			continueButton.requestFocusInWindow();
			warningText.setVisible(false);
		}

		// Set the width of the text panels by font size to accommodate font scaling
		double avgCharWidth = SwingUtils.getComponentAverageCharacterWidth(descriptionText);
		textWidth = (int) Math.round(avgCharWidth * 100);
		Insets buttonInsets = SwingUtils.createButtonInsets(avgCharWidth);
		renameButton.setMargin(buttonInsets);
		backupDowngradeButton.setMargin(buttonInsets);
		downgradeButton.setMargin(buttonInsets);
		continueButton.setMargin(buttonInsets);
		cancelCloseButton.setMargin(buttonInsets);

		descriptionText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(descriptionText, textWidth));
		descriptionText.setVisible(true);
		if (warningText.isVisible()) {
			warningText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(warningText, textWidth));
		}

		if (tableManager.getError() != null && !tableManager.isAlreadyOpenError()) {
			errorText.setText(tableManager.getError().getMessage());
			errorText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(errorText, textWidth, errorText.getText()));
			errorText.setVisible(true);
		} else {
			errorText.setVisible(false);
		}
	}

	private JComponent buildComponent() {
		// UIManager manages to get the background color wrong for text
		// components on OS X, so we apply the color manually
		Color backgroundColor = UIManager.getColor("Panel.background");
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));

		infoPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
					EtchedBorder.LOWERED),
					Messages.getString("DatabaseProblem.Caption")
				),
				BorderFactory.createEmptyBorder(10, 5, 10, 5)
			)
		));
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
		descriptionText.setEditable(false);
		descriptionText.setBackground(backgroundColor);
		descriptionText.setFocusable(false);
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
		infoPanel.add(descriptionText);

		warningText.setEditable(false);
		warningText.setFocusable(false);
		warningText.setBackground(backgroundColor);
		warningText.setFont(warningText.getFont().deriveFont(Font.BOLD));
		warningText.setLineWrap(true);
		warningText.setWrapStyleWord(true);
		warningText.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 15));
		infoPanel.add(warningText);

		String colSpec = FormLayoutUtil.getColSpec("left:pref, 3dlu, pref:grow, 3dlu, right:pref", orientation);
		FormLayout layout = new FormLayout(colSpec, "pref, 3dlu, pref");
		FormBuilder builder = FormBuilder.create().layout(layout).border(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
					EtchedBorder.LOWERED),
					Messages.getString("DatabaseProblem.DatabasePath")
				),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
			)
		));

		CellConstraints cc = new CellConstraints();

		builder
			.addLabel(Messages.getString("DatabaseProblem.CurrentDatabasePath"))
			.at(FormLayoutUtil.flip(cc.xy(1, 1), colSpec, orientation));
		builder.add(databasePath).at(FormLayoutUtil.flip(cc.xy(3, 1), colSpec, orientation));
		databasePath.setEditable(false);
		builder.add(newDatabasePathLabel).at(FormLayoutUtil.flip(cc.xy(1, 3), colSpec, orientation));
		builder.add(newDatabasePath).at(FormLayoutUtil.flip(cc.xy(3, 3), colSpec, orientation));
		newDatabasePathBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setDialogTitle(Messages.getString("DatabaseProblem.SelectFolder"));
				File folder = null;
				if (isNotBlank(newDatabasePath.getText())) {
					folder = new File(newDatabasePath.getText());
					while (folder != null && !folder.isDirectory()) {
						folder = folder.getParentFile();
					}
				}
				fileChooser.setCurrentDirectory(folder);
				int returnValue = fileChooser.showOpenDialog((Component) e.getSource());
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					newDatabasePath.setText(Tables.suggestNewDatabaseName(
						new File(fileChooser.getSelectedFile(), tableManager.getDatabaseName()).getAbsolutePath(),
						backupDowngradeButton.isEnabled() ? "backup" : null
					));
				}
			}
		});
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		newDatabasePathBrowse.setDefaultCapable(false);
		builder.add(newDatabasePathBrowse).at(FormLayoutUtil.flip(cc.xy(5, 3), colSpec, orientation));

		newDatabasePathLabel.setVisible(false);
		newDatabasePath.setVisible(false);
		newDatabasePathBrowse.setVisible(false);

		pathPanel = builder.build();

		errorText.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
					EtchedBorder.LOWERED),
					Messages.getString("Generic.ErrorMessage")
				),
				BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(10, 10, 10, 10),
					BorderFactory.createCompoundBorder(
						BorderFactory.createLoweredSoftBevelBorder(),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)
					)
				)
			)
		));
		errorText.setEditable(false);
		errorText.setFocusable(true);
		errorText.setBackground(backgroundColor);
		errorText.setFont(new Font("monospaced", Font.PLAIN, 12));

		rootPanel.add(infoPanel);
		rootPanel.add(pathPanel);
		rootPanel.add(errorText);

		renameButton.setText(Messages.getString("DatabaseProblem.Rename"));
		renameButton.addActionListener(new OperationsActionListener());
		renameButton.setDefaultCapable(false);

		backupDowngradeButton.setText(Messages.getString("DatabaseProblem.BackupDowngrade"));
		backupDowngradeButton.addActionListener(new OperationsActionListener());
		backupDowngradeButton.setDefaultCapable(false);

		downgradeButton.setText(Messages.getString("DatabaseProblem.Downgrade"));
		downgradeButton.addActionListener(new OperationsActionListener());
		downgradeButton.setDefaultCapable(false);

		cancelCloseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ("cancel".equals(e.getActionCommand())) {
					pane.setValue("cancel");
				} else if ("cancelRename".equals(e.getActionCommand())) {
					newDatabasePathLabel.setVisible(false);
					newDatabasePath.setVisible(false);
					newDatabasePathBrowse.setVisible(false);
					update();
					dialog.pack();
					dialog.setLocationRelativeTo(parentComponent);
					dialog.repaint();
				}
			}
		});

		continueButton.setText(Messages.getString("Dialog.Continue"));
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("continue")) {
					pane.setValue(e.getActionCommand());
				}
			}
		});
		continueButton.setActionCommand("continue");

		return rootPanel;

	}

	/**
	 * @return {@code true} if the "Database problem" dialog was aborted by
	 *         close or cancel, {@code false} otherwise.
	 */
	public boolean isAborted() {
		return aborted;
	}

	private class OperationsActionListener implements ActionListener {

		private void prepareRename(@Nullable String tag) {
			cancelCloseButton.setActionCommand("cancelRename");
			errorText.setVisible(false);
			downgradeButton.setEnabled(false);
			newDatabasePathLabel.setVisible(true);
			newDatabasePath.setVisible(true);
			newDatabasePath.setText(Tables.suggestNewDatabaseName(tableManager.getDatabaseFilepath(false), tag));
			newDatabasePathBrowse.setVisible(true);
			dialog.pack();
			dialog.setLocationRelativeTo(parentComponent);
			dialog.repaint();
		}

		private void unprepareRename() {
			cancelCloseButton.setActionCommand("cancel");
			newDatabasePathLabel.setVisible(false);
			newDatabasePath.setVisible(false);
			newDatabasePathBrowse.setVisible(false);
			update();
			dialog.pack();
			dialog.setLocationRelativeTo(parentComponent);
			dialog.repaint();
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if ("rename".equals(event.getActionCommand())) {
				backupDowngradeButton.setEnabled(false);
				renameButton.setActionCommand("doRename");
				prepareRename(tableManager.isWrongVersionOrCorrupt() ? "bad" : null);
			} else if ("backupDowngrade".equals(event.getActionCommand())) {
				renameButton.setEnabled(false);
				backupDowngradeButton.setActionCommand("doBackupDowngrade");
				prepareRename("backup");
			} else if ("doRename".equals(event.getActionCommand())) {
				try {
					Tables.copyDatabase(tableManager.getDatabaseFilepath(false), newDatabasePath.getText(), true);
					tableManager.start();
					renameButton.setActionCommand("rename");
					unprepareRename();
				} catch (IOException e) {
					descriptionText.setVisible(false);
					warningText.setText(String.format(
						locale,
						Messages.getString("DatabaseProblem.RenameError"),
						newDatabasePath.getText()
					));
					warningText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(warningText, textWidth));
					errorText.setText(e.toString());
					errorText.setVisible(true);
					dialog.pack();
					dialog.setLocationRelativeTo(parentComponent);
					dialog.repaint();
				}
			} else if ("doBackupDowngrade".equals(event.getActionCommand())) {
				boolean copied = false;
				try {
					Tables.copyDatabase(tableManager.getDatabaseFilepath(false), newDatabasePath.getText(), false);
					copied = true;
					Tables.downgradeDatabase(tableManager);
					tableManager.start();
					renameButton.setActionCommand("rename");
					unprepareRename();
					backupDowngradeButton.setActionCommand("backupDowngrade");
				} catch (IOException | SQLException e) {
					descriptionText.setVisible(false);
					if (!copied) {
						warningText.setText(String.format(
							locale,
							Messages.getString("DatabaseProblem.RenameError"),
							newDatabasePath.getText()
						));
					} else {
						warningText.setText(Messages.getString("DatabaseProblem.DowngradeError"));
						cancelCloseButton.setActionCommand("cancel");
						backupDowngradeButton.setEnabled(false);
					}
					warningText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(warningText, textWidth));
					errorText.setText(e.toString());
					errorText.setVisible(true);
					dialog.pack();
					dialog.setLocationRelativeTo(parentComponent);
					dialog.repaint();
				}
			} else if ("downgrade".equals(event.getActionCommand())) {
				StringBuilder message = new StringBuilder(Messages.getString("DatabaseProblem.ConfirmDowngrade"));
				message.append("\n\n");
				if (tableManager.getFutureTables(true, false, false).size() > 1) {
					message.append(String.format(
						locale,
						Messages.getString("DatabaseProblem.DowngradeWarningPlural"),
						tableManager.getFutureTablesString(true, false)
					));
				} else {
					message.append(String.format(
						locale,
						Messages.getString("DatabaseProblem.DowngradeWarning"),
						tableManager.getFutureTablesString(true, false)
					));
				}
				int confirm = JOptionPane.showConfirmDialog(
					dialog,
					message.toString(),
					Messages.getString("Dialog.Confirm"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (confirm == JOptionPane.YES_OPTION) {
					try {
						Tables.downgradeDatabase(tableManager);
						tableManager.start();
						update();
						dialog.pack();
						dialog.setLocationRelativeTo(parentComponent);
						dialog.repaint();
					} catch (SQLException e) {
						descriptionText.setVisible(false);
						warningText.setText(Messages.getString("DatabaseProblem.DowngradeError"));
						warningText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(warningText, textWidth));
						errorText.setText(e.toString());
						errorText.setVisible(true);
						renameButton.setEnabled(false);
						backupDowngradeButton.setEnabled(false);
						downgradeButton.setEnabled(false);
						dialog.pack();
						dialog.setLocationRelativeTo(parentComponent);
						dialog.repaint();
					}
				}
			} else if ("retry".equals(event.getActionCommand())) {
				tableManager.start();
				if (tableManager.isError()) {
					renameButton.setText(Messages.getString("DatabaseProblem.Rename"));
					update();
					dialog.pack();
					dialog.setLocationRelativeTo(parentComponent);
					dialog.repaint();
				} else {
					pane.setValue("continue");
				}
			}
		}
	}
}
