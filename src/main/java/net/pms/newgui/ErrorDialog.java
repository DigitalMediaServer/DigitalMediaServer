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
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.util.SwingUtils;


/**
 * This class creates and handles the generic error dialog.
 *
 * @author Nadahar
 */
public class ErrorDialog {

	private JOptionPane pane;
	private JPanel rootPanel = new JPanel();
	private JPanel infoPanel = new JPanel();
	private JTextArea descriptionText = new JTextArea();
	private JTextArea warningText = new JTextArea();
	private JEditorPane errorText = new JEditorPane();
	private String caption;

	@Nonnull
	private final ComponentOrientation orientation;

	@Nullable
	private final Component parentComponent;

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
	 * @param caption the information text caption.
	 * @param infoText the information text to show.
	 * @param warningText the warning text to show in bold.
	 * @param throwable the {@link Throwable} whose message to show as an error.
	 * @param trace if {@code true}, the stack-trace from {@code throwable} is
	 *            included in the error message.
	 */
	public ErrorDialog(
		@Nullable Component parentComponent,
		@Nullable String caption,
		@Nullable String infoText,
		@Nullable String warningText,
		@Nullable Throwable throwable,
		boolean trace
	) {
		if (PMS.isHeadless()) {
			throw new IllegalStateException("ErrorDialog dialog can't run in headless mode");
		}
		this.parentComponent = parentComponent;
		this.locale = PMS.getLocale();
		this.caption = isBlank(caption) ? Messages.getString("Generic.Error") : caption;
		this.descriptionText.setText(infoText);
		this.warningText.setText(warningText);
		if (throwable != null) {
			StringWriter sw = new StringWriter();
			if (trace) {
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
			} else {
				sw.append(throwable.toString());
			}
			this.errorText.setText(sw.toString());
		}

		// Apply the orientation for the locale
		this.orientation = ComponentOrientation.getOrientation(locale);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param parentComponent the parent {@link Frame} or {@link Window} or
	 *            {@code null} to make it an orphan.
	 * @param caption the information text caption.
	 * @param infoText the information text to show.
	 * @param warningText the warning text to show in bold.
	 * @param errorText the error message to show.
	 */
	public ErrorDialog(
		@Nullable Component parentComponent,
		@Nullable String caption,
		@Nullable String infoText,
		@Nullable String warningText,
		@Nullable String errorText
	) {
		if (PMS.isHeadless()) {
			throw new IllegalStateException("ErrorDialog dialog can't run in headless mode");
		}
		this.parentComponent = parentComponent;
		this.locale = PMS.getLocale();
		this.caption = isBlank(caption) ? Messages.getString("Generic.Error") : caption;
		this.descriptionText.setText(infoText);
		this.warningText.setText(warningText);
		this.errorText.setText(errorText);

		// Apply the orientation for the locale
		this.orientation = ComponentOrientation.getOrientation(locale);
	}

	/**
	 * Creates and displays the error dialog. Blocks until the dialog is closed,
	 * after which it is disposed of and the aborted status is set. The dialog
	 * uses {@link JOptionPane#ERROR_MESSAGE} and
	 * {@link JOptionPane#DEFAULT_OPTION}.
	 */
	public void show() {
		show(JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * Creates and displays the error dialog. Blocks until the dialog is closed,
	 * after which it is disposed of and the aborted status is set.
	 *
	 * @param messageType the type of message to be displayed:
	 *            {@link JOptionPane#ERROR_MESSAGE},
	 *            {@link JOptionPane#INFORMATION_MESSAGE},
	 *            {@link JOptionPane#WARNING_MESSAGE},
	 *            {@link JOptionPane#QUESTION_MESSAGE} or
	 *            {@link JOptionPane#PLAIN_MESSAGE}.
	 * @param optionType the options to display in the pane:
	 *            {@link JOptionPane#DEFAULT_OPTION},
	 *            {@link JOptionPane#YES_NO_OPTION},
	 *            {@link JOptionPane#YES_NO_CANCEL_OPTION} or
	 *            {@link JOptionPane#OK_CANCEL_OPTION}
	 */
	public void show(int messageType, int optionType) {
		pane = new JOptionPane(
			buildComponent(),
			messageType,
			optionType
		);
		pane.setComponentOrientation(ComponentOrientation.getOrientation(locale));
		dialog = pane.createDialog(parentComponent, PMS.getName());
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setIconImage(LooksFrame.readImageIcon("icon-32.png").getImage());
		dialog.setLocale(locale);
		dialog.applyComponentOrientation(ComponentOrientation.getOrientation(locale));
		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setVisible(true);
		dialog.dispose();

		if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
			aborted = true;
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
					EtchedBorder.LOWERED), caption
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
		descriptionText.setVisible(isNotBlank(descriptionText.getText()));
		warningText.setVisible(isNotBlank(warningText.getText()));

		errorText.setEditable(false);
		errorText.setFocusable(true);
		errorText.setBackground(backgroundColor);
		errorText.setFont(new Font("monospaced", Font.PLAIN, 12));

		rootPanel.add(infoPanel);

		Component adjustTo;
		if (isNotBlank(descriptionText.getText())) {
			adjustTo = descriptionText;
		} else if (isNotBlank(warningText.getText())) {
			adjustTo = warningText;
		} else if (isNotBlank(errorText.getText())) {
			adjustTo = errorText;
		} else {
			adjustTo = descriptionText;
		}
		// Set the width of the text panels by font size to accommodate font scaling
		double avgCharWidth = SwingUtils.getComponentAverageCharacterWidth(adjustTo);
		textWidth = (int) Math.round(avgCharWidth * 100);

		if (isNotBlank(descriptionText.getText())) {
			descriptionText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(descriptionText, textWidth));
		}
		if (isNotBlank(warningText.getText())) {
			warningText.setPreferredSize(SwingUtils.getWordWrappedTextDimension(warningText, textWidth));
		}

		if (isNotBlank(errorText.getText())) {
			Dimension errorSize = SwingUtils.getWordWrappedTextDimension(errorText, textWidth);

			JScrollPane scrollPane = new JScrollPane(errorText);
			scrollPane.setBorder(BorderFactory.createCompoundBorder(
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
			if (errorSize.height > textWidth / 2) {
				scrollPane.setPreferredSize(new Dimension(textWidth, textWidth / 2));
			}
			rootPanel.add(scrollPane);
		}
		rootPanel.applyComponentOrientation(orientation);

		return rootPanel;
	}

	/**
	 * @return {@code true} if the the dialog was aborted by close or cancel,
	 *         {@code false} otherwise.
	 */
	public boolean isAborted() {
		return aborted;
	}
}
