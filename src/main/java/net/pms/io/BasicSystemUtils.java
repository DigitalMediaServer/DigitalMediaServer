/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2011 G.Zsombor
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
package net.pms.io;

import static org.apache.commons.lang3.StringUtils.isBlank;
import com.drew.lang.annotations.Nullable;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.Nonnull;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.newgui.LooksFrame;
import net.pms.platform.posix.NixCLibrary;
import net.pms.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for the SystemUtils class for the generic cases.
 * @author zsombor
 *
 */
public class BasicSystemUtils implements SystemUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(BasicSystemUtils.class);

	/** The singleton platform dependent {@link SystemUtils} instance */
	public static SystemUtils INSTANCE = BasicSystemUtils.createInstance();

	@Nonnull
	protected final Version osVersion;
	protected Path vlcPath;
	protected Version vlcVersion;
	protected boolean aviSynth;

	protected MenuItem webInterfaceItem;

	protected static BasicSystemUtils createInstance() {
		if (Platform.isWindows()) {
			return new WindowsSystemUtils();
		}
		if (Platform.isMac()) {
			return new MacSystemUtils();
		}
		if (Platform.isSolaris()) {
			return new SolarisUtils();
		}
		return new BasicSystemUtils();
	}

	/** Only to be instantiated by {@link BasicSystemUtils#createInstance()}. */
	protected BasicSystemUtils() {
		osVersion = getOSVersionInternal();
	}

	@Override
	public File getAvsPluginsDir() {
		return null;
	}

	@Override
	public File getKLiteFiltersDir() {
		return null;
	}

	@Override
	public String getShortPathNameW(String longPathName) {
		return longPathName;
	}

	@Override
	public String getWindowsDirectory() {
		return null;
	}

	@Override
	public String getDiskLabel(File f) {
		return null;
	}

	@Override
	public boolean isKerioFirewall() {
		return false;
	}

	@Override
	public Path getVlcPath() {
		return vlcPath;
	}

	@Override
	public Version getVlcVersion() {
		return vlcVersion;
	}

	@Override
	public boolean isAviSynthAvailable() {
		return aviSynth;
	}

	@Override
	public void browseURI(String uri) {
		try {
			Desktop.getDesktop().browse(new URI(uri));
		} catch (IOException | URISyntaxException e) {
			LOGGER.trace("Unable to open the given URI: " + uri + ".");
		}
	}

	@Override
	public boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException {
		return ni.isLoopback();
	}

	@Override
	public void setWebInterfaceSystemTrayEnabled(boolean value) {
		if (webInterfaceItem != null) {
			webInterfaceItem.setEnabled(value);
		}
	}

	@Override
	public void addSystemTray(final LooksFrame frame) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image trayIconImage = resolveTrayIcon();

			PopupMenu popup = new PopupMenu();
			MenuItem quitItem = new MenuItem(Messages.getString("LooksFrame.5"));
			MenuItem showItem = new MenuItem(Messages.getString("LooksFrame.6"));

			showItem.addActionListener(buildShowItemActionListener(frame));
			popup.add(showItem);

			if (PMS.getConfiguration().useWebInterface()) {
				popup.addSeparator();
				webInterfaceItem = new MenuItem(Messages.getString("LooksFrame.29"));
				webInterfaceItem.setEnabled(false);
				webInterfaceItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						browseURI(PMS.get().getWebInterface().getUrl());
					}
				});
				popup.add(webInterfaceItem);
			}

			popup.addSeparator();
			quitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.quit();
				}
			});
			popup.add(quitItem);

			final TrayIcon trayIcon = new TrayIcon(trayIconImage, PMS.getName(), popup);

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(true);
					frame.setFocusable(true);
				}
			});
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				LOGGER.error("Couldn't add system tray icon: {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}

	protected ActionListener buildShowItemActionListener(final LooksFrame frame) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);
			}
		};
	}

	/**
	 * Fetch the hardware address for a network interface.
	 *
	 * @param ni Interface to fetch the mac address for
	 * @return the mac address as bytes, or null if it couldn't be fetched.
	 * @throws SocketException
	 *             This won't happen on Mac OS, since the NetworkInterface is
	 *             only used to get a name.
	 */
	@Override
	public byte[] getHardwareAddress(NetworkInterface ni) throws SocketException {
		return ni.getHardwareAddress();
	}

	/**
	 * Return the platform specific ping command for the given host address,
	 * ping count and packet size.
	 *
	 * @param hostAddress The host address.
	 * @param count The ping count.
	 * @param packetSize The packet size.
	 * @return The ping command.
	 */
	@Override
	public String[] getPingCommand(String hostAddress, int count, int packetSize) {
		return new String[] {
			"ping",
			"-c", // count
			Integer.toString(count),
			"-s", // size
			Integer.toString(packetSize),
			hostAddress
		};
	}

	@Override
	public String parsePingLine(String line) {
		int msPos = line.indexOf("ms");
		String timeString = null;

		if (msPos > -1) {
			if (line.lastIndexOf('<', msPos) > -1) {
				timeString = "0.5";
			} else {
				timeString = line.substring(line.lastIndexOf('=', msPos) + 1, msPos).trim();
			}
		}
		return timeString;
	}

	@Override
	public int getPingPacketFragments(int packetSize) {
		return ((packetSize + 8) / 1500) + 1;
	}

	/**
	 * @return The system tray icon {@link Image} for the current platform.
	 */
	protected Image resolveTrayIcon() {
		return Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/images/" + getTrayIconName()));
	}

	/**
	 * @return The name of the system tray icon for the current platform.
	 */
	@Nonnull
	protected String getTrayIconName() {
		return "icon-24.png";
	}

	@Override
	@Nonnull
	public Version getOSVersion() {
		return osVersion;
	}

	/**
	 * @return The non-cached OS {@link Version}.
	 */
	@Nonnull
	protected Version getOSVersionInternal() {
		return new Version(System.getProperty("os.version"));
	}

	@Override
	@Nonnull
	public String getLocalHostname() {
		String hostname = getComputerName();
		if (isBlank(hostname)) {
			return "localhost";
		}
		try {
			List<InetAddress> resolvedAddresses = Arrays.asList(InetAddress.getAllByName(getComputerName()));
			if (!resolvedAddresses.isEmpty()) {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				if (interfaces != null) {
					while (interfaces.hasMoreElements()) {
						NetworkInterface networkInterface = interfaces.nextElement();
						for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
							if (address.getAddress() != null && resolvedAddresses.contains(address.getAddress())) {
								return hostname;
							}
						}
					}
				}
			}
		} catch (UnknownHostException | SocketException e) {
			LOGGER.trace(
				"Failed to resolve \"{}\" to an IP address, returning \"localhost\" as the local hostname: {}",
				hostname,
				e.getMessage()
			);
		}

		return "localhost";
	}

	@Override
	@Nullable
	public String getComputerName() {
		byte[] hostname = new byte[256];
		return NixCLibrary.INSTANCE.gethostname(hostname, hostname.length) == 0 ? Native.toString(hostname) : null;
	}
}
