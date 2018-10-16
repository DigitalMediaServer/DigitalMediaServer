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

import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nullable;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;


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
					int whetherToRunWizard = JOptionPane.showConfirmDialog(
						null,
						Messages.getString("Wizard.1"),
						Messages.getString("Dialog.Question"),
						JOptionPane.YES_NO_OPTION
					);
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
						int networkType = JOptionPane.showOptionDialog(
							null,
							Messages.getString("Wizard.7"),
							Messages.getString("Wizard.2") + " " + (currentQuestionNumber++) + " " +
							Messages.getString("Wizard.4") + " " + numberOfQuestions,
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							wizardOptions,
							wizardOptions[1]
						);
						switch (networkType) {
							case JOptionPane.YES_OPTION:
								// Wired (Gigabit)
								configuration.setMaximumBitrate("0");
								configuration.setMPEG2MainSettings("Automatic (Wired)");
								configuration.setx264ConstantRateFactor("Automatic (Wired)");
								save(configuration);
								break;
							case JOptionPane.NO_OPTION:
								// Wired (100 Megabit)
								configuration.setMaximumBitrate("90");
								configuration.setMPEG2MainSettings("Automatic (Wired)");
								configuration.setx264ConstantRateFactor("Automatic (Wired)");
								save(configuration);
								break;
							case JOptionPane.CANCEL_OPTION:
								// Wireless
								configuration.setMaximumBitrate("30");
								configuration.setMPEG2MainSettings("Automatic (Wireless)");
								configuration.setx264ConstantRateFactor("Automatic (Wireless)");
								save(configuration);
								break;
							default:
								break;
						}

						// Ask if they want to hide advanced options
						int showAdvancedOptions = JOptionPane.showConfirmDialog(
							null,
							Messages.getString("Wizard.AdvancedOptions"),
							Messages.getString("Wizard.2") + " " + (currentQuestionNumber++) + " " +
							Messages.getString("Wizard.4") + " " + numberOfQuestions,
							JOptionPane.YES_NO_OPTION
						);
						if (showAdvancedOptions == JOptionPane.YES_OPTION) {
							configuration.setHideAdvancedOptions(false);
							save(configuration);
						} else if (showAdvancedOptions == JOptionPane.NO_OPTION) {
							configuration.setHideAdvancedOptions(true);
							save(configuration);
						}

						JOptionPane.showMessageDialog(
							null,
							Messages.getString("Wizard.13"),
							Messages.getString("Wizard.12"),
							JOptionPane.INFORMATION_MESSAGE
						);

						configuration.setRunWizard(false);
						save(configuration);
					} else if (whetherToRunWizard == JOptionPane.NO_OPTION) {
						// The user has chosen to not run the wizard
						// Do not ask them again
						configuration.setRunWizard(false);
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
}
