package net.pms.util;

import java.awt.Component;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.pms.Messages;
import net.pms.PMS;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CheckOSClock implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckOSClock.class);
	public static final String[] DEFAULT_NTP_SERVERS = {"0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org"}; //TODO: (Nad) Configuration
	public static final double SECOND_MS = 1000.0;
	public static final double MINUTE_MS = 60000.0;
	public static final double HOUR_MS = 3600000.0;
	public static final double DAY_MS = 86400000.0;

	private static String generateTimeOffsetString(long offsetMS, boolean localize) {
		String offset;
		boolean moreThan = false;
		//offsetMS = 2 * (long) DAY_MS + 4;
		if (Math.abs(offsetMS) > 2 * DAY_MS) { // 48 hours
			moreThan = true;
			offset = (long) (offsetMS / DAY_MS) + " " + Messages.getString("General.Days", localize);
		} else if (Math.abs(offsetMS) >= 2 * HOUR_MS) { // 2 hours
			long minutes = (long) (offsetMS / MINUTE_MS);
			moreThan = Math.abs(minutes % 60) < 3;
			offset = minutes / 60 + " " + Messages.getString("General.Hours", localize);
		} else if (Math.abs(offsetMS) > 2 * MINUTE_MS) { // 2 minutes
			long seconds = (long) (offsetMS / SECOND_MS);
			moreThan = Math.abs(seconds % 60) < 5;
			offset = seconds / 60 + " " + Messages.getString("General.Minutes", localize);
		} else {
			int seconds = (int) Math.round(offsetMS / SECOND_MS);
			offset = seconds + " " + Messages.getString(seconds == 1 ? "General.Second" : "General.Seconds", localize);
		}
		if (offsetMS > 0 && moreThan) {
			return String.format(localize ? Messages.getString("CheckOSClock.2") : "Computer clock is more than %s slow.", offset);
		} else if (offsetMS > 0) {
			return String.format(localize ? Messages.getString("CheckOSClock.1") : "Computer clock is %s slow.", offset);
		} else if (moreThan) {
			return String.format(localize ? Messages.getString("CheckOSClock.4") : "Computer clock is more than %s fast.", offset);
		}
		return String.format(localize ? Messages.getString("CheckOSClock.3") : "Computer clock is %s fast.", offset);
	}

	private static void handleTimeDiscrepancy(long offsetMS) {
		// The discrepancy threshold in milliseconds for warning the user
		if (Math.abs(offsetMS) > 500) {
			final String message = generateTimeOffsetString(offsetMS, true) + " " + Messages.getString("CheckOSClock.5");
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(generateTimeOffsetString(offsetMS, false));
			}
			if (!PMS.isHeadless()) {
				try {
					PMS.getGUIReadyLatch().await();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(
								SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
								message,
								Messages.getString("Dialog.Warning"),
								JOptionPane.WARNING_MESSAGE
							);
						}
					});
				} catch (InterruptedException e) {
					LOGGER.debug("CheckOSClock thread was interrupted before user warning could be issued");
					Thread.currentThread().interrupt();
				}
			}
		} else if (LOGGER.isInfoEnabled()) {
			if (Math.round(offsetMS/SECOND_MS) == 0.0) { //TODO: (Nad) Threshold
				LOGGER.info("Computer clock is correct");
			} else {
				LOGGER.warn(generateTimeOffsetString(offsetMS, false));
			}
		}
	}

	@Override
	public void run() {
		if (!PMS.getConfiguration().getExternalNetwork()) {
			LOGGER.debug("Not checking network time since external network is disabled");
			return;
		}
		if (DEFAULT_NTP_SERVERS.length < 1) {
			LOGGER.debug("No NTP servers to check, aborting..");
			return;
		}

		NTPUDPClient client = new NTPUDPClient();
		// Timeout in milliseconds
		client.setDefaultTimeout(10000);
		try {
			client.open();
			int i = 0;
			int j = (int) Math.round(Math.random() * 3);
			TimeInfo timeInfo = null;
			while (timeInfo == null && i < DEFAULT_NTP_SERVERS.length) {
				LOGGER.trace("Attempting to connect to \"{}\"", DEFAULT_NTP_SERVERS[j]);
				try {
					InetAddress hostAddr = InetAddress.getByName(DEFAULT_NTP_SERVERS[j]);
					LOGGER.trace("CheckOSClock: Resolved \"{}\" to {}", DEFAULT_NTP_SERVERS[j], hostAddr);
					timeInfo = client.getTime(hostAddr);
					LOGGER.trace("CheckOSClock: Got network time response from {}", hostAddr);
				} catch (IOException e) {
					LOGGER.warn("Error querying NTP server \"{}\": {}", DEFAULT_NTP_SERVERS[j], e.getMessage());
					LOGGER.trace("", e);
				}
				i++;
				if (j == DEFAULT_NTP_SERVERS.length - 1) {
					j = 0;
				} else {
					j++;
				}
			}
			if (timeInfo != null) {
				LOGGER.trace("CheckOSClock: Computing details");
				timeInfo.computeDetails();
				LOGGER.trace("CheckOSClock: Done computing details. The delay was {} milliseconds", timeInfo.getDelay());
				Long offsetMS = timeInfo.getOffset();
				if (offsetMS == null) {
					LOGGER.warn("Could not calculate OS clock offset, got an invalid result");
				} else {
					handleTimeDiscrepancy(offsetMS);
				}
			}else {
				LOGGER.debug("Could not query NTP server after {} attempts. Check network connectivity and DNS resolution", DEFAULT_NTP_SERVERS.length);
			}
		}catch (SocketException e1) {
			LOGGER.warn("Could not calculate OS clock offset: {}", e1.getMessage());
			LOGGER.trace("", e1);
		}finally {
			client.close();
		}
	}
}
