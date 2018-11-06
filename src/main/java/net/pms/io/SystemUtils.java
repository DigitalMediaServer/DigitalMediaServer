package net.pms.io;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.newgui.LooksFrame;
import net.pms.util.Version;

public interface SystemUtils {

	public abstract File getAvsPluginsDir();

	public abstract File getKLiteFiltersDir();

	public abstract String getShortPathNameW(String longPathName);

	public abstract String getWindowsDirectory();

	public abstract String getDiskLabel(File f);

	public abstract boolean isKerioFirewall();

	public abstract Path getVlcPath();

	public abstract Version getVlcVersion();

	public abstract boolean isAviSynthAvailable();

	/**
	 * Open HTTP URLs in the default browser.
	 * @param uri URI string to open externally.
	 */
	public void browseURI(String uri);

	public boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException;

	public void addSystemTray(final LooksFrame frame);

	/**
	 * Sets the enabled status of the system tray web interface menu item.
	 *
	 * @param value the enabled status.
	 */
	public void setWebInterfaceSystemTrayEnabled(boolean value);

	/**
	 * Fetch the hardware address for a network interface.
	 *
	 * @param ni Interface to fetch the mac address for
	 * @return the mac address as bytes, or null if it couldn't be fetched.
	 * @throws SocketException
	 *             This won't happen on Mac OS, since the NetworkInterface is
	 *             only used to get a name.
	 */
	public byte[] getHardwareAddress(NetworkInterface ni) throws SocketException;

	/**
	 * Return the platform specific ping command for the given host address,
	 * ping count and packet size.
	 *
	 * @param hostAddress The host address.
	 * @param count The ping count.
	 * @param packetSize The packet size.
	 * @return The ping command.
	 */
	String[] getPingCommand(String hostAddress, int count, int packetSize);

	String parsePingLine(String line);

	/**
	 * This is't an actual but an estimated value assuming default MTU size.
	 *
	 * @param packetSize the size of the packet in bytes.
	 * @return The estimated number of fragments.
	 */
	int getPingPacketFragments(int packetSize);

	/**
	 * Returns the operating system version. This might not be the same as the
	 * "marketing version" or "distro version".
	 * <p>
	 * On Windows, the version returned is called the "internal" version, where
	 * for example Windows 7 (marketing) returns 6.1. On Linux, the kernel
	 * version will normally be returned, not the version number assigned by the
	 * "distro".
	 *
	 * @return The OS {@link Version}.
	 */
	@Nonnull
	public Version getOSVersion();

	/**
	 * @return The locally resolvable name of the local computer or
	 *         {@code "localhost"} if no resolvable name could be found.
	 */
	@Nonnull
	public String getLocalHostname();

	/**
	 * @return The name of the local computer or {@code null} if it
	 *         couldn't be resolved.
	 */
	@Nullable
	public String getComputerName();
}
