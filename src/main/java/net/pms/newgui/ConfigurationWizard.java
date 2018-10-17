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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Vector;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.RootFolder;
import net.pms.util.SwingUtils;


/**
 * This utility class handles the configuration wizard.
 *
 * @author Nadahar
 */
public class ConfigurationWizard {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationWizard.class);

	/**
	 * Not to be instantiated.
	 */
	private ConfigurationWizard() {
	}

	/**
	 * Creates and runs the configuration wizard.
	 *
	 * @param configuration the {@link PmsConfiguration} to use.
	 * @param splash the {@link Splash} or {@code null}.
	 */
	public static void run(@Nullable final PmsConfiguration configuration, @Nullable final Splash splash) {
		if (configuration == null) {
			LOGGER.error("Can't run configuration wizard because the configuration is null");
			return;
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					// Hide splash screen
					if (splash != null) {
						splash.setVisible(false);
					}
					// Ask the user if they want to run the wizard
					int whetherToRunWizard = JOptionPane.YES_OPTION; //JOptionPane.showConfirmDialog( //TODO: (Nad) Temp
//						null,
//						Messages.getString("Wizard.1"),
//						Messages.getString("Dialog.Question"),
//						JOptionPane.YES_NO_OPTION
//					);
					if (whetherToRunWizard == JOptionPane.YES_OPTION) {
						// The user has chosen to run the wizard

						// Total number of questions
						int numberOfQuestions = 2;

						// The current question number
						int currentQuestionNumber = 1;

						// Ask if their network is wired, etc.
						Object[] wizardOptions = {
							Messages.getString("Wizard.8"),
							Messages.getString("Wizard.9"),
							Messages.getString("Wizard.10")
						};
//						int networkType = JOptionPane.showOptionDialog(
//							null,
//							Messages.getString("Wizard.7"),
//							Messages.getString("Wizard.2") + " " + (currentQuestionNumber++) + " " +
//							Messages.getString("Wizard.4") + " " + numberOfQuestions,
//							JOptionPane.YES_NO_CANCEL_OPTION,
//							JOptionPane.QUESTION_MESSAGE,
//							null,
//							wizardOptions,
//							wizardOptions[1]
//						);
//						switch (networkType) {
//							case JOptionPane.YES_OPTION:
//								// Wired (Gigabit)
//								configuration.setMaximumBitrate("0");
//								configuration.setMPEG2MainSettings("Automatic (Wired)");
//								configuration.setx264ConstantRateFactor("Automatic (Wired)");
//								save(configuration);
//								break;
//							case JOptionPane.NO_OPTION:
//								// Wired (100 Megabit)
//								configuration.setMaximumBitrate("90");
//								configuration.setMPEG2MainSettings("Automatic (Wired)");
//								configuration.setx264ConstantRateFactor("Automatic (Wired)");
//								save(configuration);
//								break;
//							case JOptionPane.CANCEL_OPTION:
//								// Wireless
//								configuration.setMaximumBitrate("30");
//								configuration.setMPEG2MainSettings("Automatic (Wireless)");
//								configuration.setx264ConstantRateFactor("Automatic (Wireless)");
//								save(configuration);
//								break;
//							default:
//								break;
//						}

						// Ask if they want to show advanced options
//						int showAdvancedOptions = JOptionPane.showConfirmDialog(
//							null,
//							Messages.getString("Wizard.AdvancedOptions"),
//							Messages.getString("Wizard.2") + " " + (currentQuestionNumber++) + " " +
//							Messages.getString("Wizard.4") + " " + numberOfQuestions,
//							JOptionPane.YES_NO_OPTION
//						);
//						if (showAdvancedOptions == JOptionPane.YES_OPTION) {
//							configuration.setHideAdvancedOptions(false);
//							save(configuration);
//						} else if (showAdvancedOptions == JOptionPane.NO_OPTION) {
//							configuration.setHideAdvancedOptions(true);
//							save(configuration);
//						}

						JOptionPane.showConfirmDialog(
							null,
							buildDefaultFoldersDialog(), // Messages.getString("Wizard.13"),
							Messages.getString("Wizard.12"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.INFORMATION_MESSAGE
						);

//						configuration.setRunWizard(false); //TODO: (Nad) Temp
						save(configuration);
					} else if (whetherToRunWizard == JOptionPane.NO_OPTION) {
						// The user has chosen to not run the wizard
						// Do not ask them again
//						configuration.setRunWizard(false); //TODO: (Nad) Temp
						save(configuration);
					}

					// Unhide splash screen
					if (splash != null) {
						splash.setVisible(true);
					}
				}
			});
		} catch (InterruptedException e) {
			LOGGER.info("The configuration wizard was interrupted, aborting...");
		} catch (InvocationTargetException e) {
			LOGGER.error("An error occurred during the configuration wizard: {}", e);
		}
	}

	private static JComponent buildDefaultFoldersDialog() {
		JPanel panel = new JPanel(new GridBagLayout());
		Color bgColor = new JLabel().getBackground();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.gridx = 1;
		constraints.gridy = 1;
		JTextArea text = new JTextArea("Text to measure");
		double avgCharWidth = SwingUtils.getComponentAverageCharacterWidth(text);
		int textWidth = (int) Math.round(avgCharWidth * 80);
		constraints.insets = new Insets((int) avgCharWidth, 0, (int) avgCharWidth, 0);
		text.setText("Digital Media Server defines some default folders that are likely to contain media files that you want to share. Using the default folders will make it easier to get started, but experienced users will probably want to configure the shared folders manually. The default folders are:");
		text.setPreferredSize(SwingUtils.getWordWrappedTextDimension(text, textWidth));
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setBackground(bgColor);
		text.setPreferredSize(text.getPreferredSize());
		panel.add(text, constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Vector<String> columns = new Vector<>();
		columns.add("Folder");
		Vector<Vector<?>> newDataVector = new Vector<>();
		for (Path folder : RootFolder.getDefaultFolders()) {
			Vector<String> rowVector = new Vector<>();
			rowVector.add(folder.toString());
			newDataVector.add(rowVector);
		}

		NonEditableTableModel model = new NonEditableTableModel(newDataVector, columns);

		JTable table = new JTable(model);
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize()));
		DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) table.getCellRenderer(0, 0);
		table.setRowHeight((int) (cellRenderer.getFontMetrics(cellRenderer.getFont()).getHeight() * 1.7));
		table.setIntercellSpacing(new Dimension(8, 2));
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		Dimension tablePreferredSize = table.getPreferredSize();
		int tableHeaderHeight = table.getTableHeader().getPreferredSize().height;
		scrollPane.setPreferredSize(new Dimension(
			tablePreferredSize.width,
			Math.min(tableHeaderHeight + tablePreferredSize.height + 4, tableHeaderHeight + table.getRowHeight() * 5)
		));
		panel.add(scrollPane, constraints);


//		DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) sharedFolders.getCellRenderer(0,0);
//		FontMetrics metrics = cellRenderer.getFontMetrics(cellRenderer.getFont());
//		sharedFolders.setRowHeight(metrics.getLeading() + metrics.getMaxAscent() + metrics.getMaxDescent() + 4);
//		sharedFolders.setIntercellSpacing(new Dimension(8, 2));
//		sharedFolders.setEnabled(!defaultSharedFolders);
//
//		Vector<Vector<?>> newDataVector = new Vector<>();
//		if (!folders.isEmpty()) {
//			List<Path> foldersMonitored = configuration.getMonitoredFolders();
//			for (Path folder : folders) {
//				Vector rowVector = new Vector();
//				rowVector.add(folder.toString());
//				rowVector.add(Boolean.valueOf(foldersMonitored.contains(folder)));
//				newDataVector.add(rowVector);
//			}
//		}
//		folderTableModel.setDataVector(newDataVector, FOLDERS_COLUMN_NAMES);
//		TableColumn column = sharedFolders.getColumnModel().getColumn(0);
//		column.setMinWidth(600);
//
//
//		List<Path> folders = RootFolder.getDefaultFolders();
//		JList<Path> list = new JList<Path>(folders.toArray(new Path[folders.size()]));
////		list.setEnabled(false);
//		list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, list.getFont().getSize()));
//		list.setBorder(new BevelBorder(BevelBorder.LOWERED));
//		list.setBackground(panel.getBackground());
//		panel.add(list, constraints);

		constraints.gridy++;
		JTextArea confirmText = new JTextArea("Please make sure that there are no sensitive files in any of the folders before sharing them. Do you want to share these folders?");
		confirmText.setEditable(false);
		confirmText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(confirmText, textWidth));
		confirmText.setLineWrap(true);
		confirmText.setWrapStyleWord(true);
		confirmText.setBackground(bgColor);
		panel.add(confirmText, constraints);

		return panel;
	}

	/**
	 * Force saves the specified {@link PmsConfiguration}.
	 *
	 * @param configuration the {@link PmsConfiguration} to save.
	 */
	protected static void save(PmsConfiguration configuration) {
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			LOGGER.error("Failed to save the configuration: {}", e);
		}
	}

	public static class NonEditableTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public NonEditableTableModel(Vector<?> data, Vector<String> columnNames) {
			super(data, columnNames);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
