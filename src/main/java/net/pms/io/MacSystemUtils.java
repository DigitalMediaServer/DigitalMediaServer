package net.pms.io;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.drew.lang.annotations.Nullable;
import net.pms.newgui.LooksFrame;
import net.pms.platform.macos.Cocoa;
import net.pms.platform.macos.Cocoa.NSApplicationActivationOptions;
import net.pms.platform.macos.NSFoundation;
import net.pms.platform.macos.NSFoundation.NSSearchPathDirectory;
import net.pms.platform.macos.NSFoundation.NSSearchPathDomainMask;
import net.pms.platform.macos.SystemConfiguration;
import net.pms.util.Version;
import net.pms.util.jna.macos.corefoundation.CoreFoundation;
import net.pms.util.jna.macos.corefoundation.CoreFoundation.CFStringRef;
import net.pms.util.jna.macos.iokit.IOKitUtils;

public class MacSystemUtils extends BasicSystemUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(MacSystemUtils.class);

	/** Only to be instantiated by {@link BasicSystemUtils#createInstance()}. */
	protected MacSystemUtils() {
	}

	@Override
	public void browseURI(String uri) {
		try {
			// On OS X, open the given URI with the "open" command.
			// This will open HTTP URLs in the default browser.
			Runtime.getRuntime().exec(new String[] {"open", uri });

		} catch (IOException e) {
			LOGGER.trace("Unable to open the given URI: {}", uri);
		}
	}

	@Override
	public boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException {
		return false;
	}

	@Override
	protected ActionListener buildShowItemActionListener(LooksFrame frame) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Cocoa.unhide();
				Cocoa.activate(NSApplicationActivationOptions.NSApplicationActivateIgnoringOtherApps);
			}
		};
	}

	/**
	 * Fetch the hardware address for a network interface.
	 *
	 * @param ni Interface to fetch the MAC address for
	 * @return the MAC address as bytes, or null if it couldn't be fetched.
	 * @throws SocketException
	 *         This won't happen on OS X, since the NetworkInterface is
	 *         only used to get a name.
	 */
	@Override
	public byte[] getHardwareAddress(NetworkInterface ni) throws SocketException {
		// On Mac OS X, fetch the hardware address from the command line tool "ifconfig".
		byte[] aHardwareAddress = null;

		try {
			Process process = Runtime.getRuntime().exec(new String[] {"ifconfig", ni.getName(), "ether" });
			List<String> lines = null;
			try (InputStream inputStream = process.getInputStream()) {
				lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
			}
			String aMacStr = null;
			Pattern aMacPattern = Pattern.compile("\\s*ether\\s*([a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2})");

			if (lines != null) {
				for (String line : lines) {
					Matcher aMacMatcher = aMacPattern.matcher(line);

					if (aMacMatcher.find()) {
						aMacStr = aMacMatcher.group(1);
						break;
					}
				}
			}

			if (aMacStr != null) {
				String[] aComps = aMacStr.split(":");
				aHardwareAddress = new byte[aComps.length];

				for (int i = 0; i < aComps.length; i++) {
					String aComp = aComps[i];
					aHardwareAddress[i] = (byte) Short.valueOf(aComp, 16).shortValue();
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to execute ifconfig", e);
		}

		return aHardwareAddress;
	}

	/**
	 * Return the platform specific ping command for the given host address,
	 * ping count and packet size. macOS has a maximum UDP packet size of ~8400
	 * bytes, so this method will divide packets of larger sizes into multiple
	 * packets to "simulate" the effect.
	 *
	 * @param hostAddress the host address.
	 * @param count the ping count.
	 * @param packetSize the packet size.
	 * @return The ping command.
	 */
	@Override
	public String[] getPingCommand(String hostAddress, int count, int packetSize) {
		if (packetSize > 8000) {
			int divisor = getPingPacketDivisor(packetSize);
			packetSize /= divisor;
			count *= divisor;
		}
		return new String[] {
			"ping", /* count */ "-c", Integer.toString(count),
			/* delay */ "-i", "0.1",
			/* size */ "-s", Integer.toString(packetSize),
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
		// Avoid returning the macOS ping statistics
		return timeString != null && timeString.contains("/") ? null : timeString;
	}

	@Override
	public int getPingPacketFragments(int packetSize) {
		if (packetSize > 8000) {
			packetSize /= getPingPacketDivisor(packetSize);
		}
		return ((packetSize + 8) / 1500) + 1;
	}

	/**
	 * macOS has a default maximum UDP packet size of ~8400 bytes. This method
	 * will divide packets of larger size into multiple packets to "simulate"
	 * the effect.
	 *
	 * @param packetSize the packet size.
	 * @return The divisor with which to device the packet size and multiply the
	 *         count.
	 */
	private static int getPingPacketDivisor(int packetSize) {
		return (int) Math.ceil(packetSize / 8000.0);
	}

	@Override
	@Nonnull
	protected String getTrayIconName() {
		return "icon-18.png";
	}

	@Override
	@Nonnull
	protected Version getOSVersionInternal() {
		int[] elements = IOKitUtils.getMacosversion();
		return new Version(elements[0], elements[1], elements[2], 0);
	}

	@Override
	@Nullable
	public String getComputerName() {
		try {
		CFStringRef cfResult = SystemConfiguration.INSTANCE.SCDynamicStoreCopyComputerName(null, null);
		if (cfResult != null) {
			try {
				String result = cfResult.toString();
				if (isNotBlank(result)) {
					return result;
				}
			} finally {
				CoreFoundation.INSTANCE.CFRelease(cfResult);
			}
		}
		} catch (Exception e) {
			LOGGER.error("The call to SCDynamicStoreCopyComputerName failed with: {}", e.getMessage());
			LOGGER.trace("", e);
		}
		// Fallback
		return super.getComputerName();
	}

	@Override
	@Nullable
	protected Path enumerateDefaultFolders(List<Path> folders) {
		Path desktop = null;
		List<Path> desktops = NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSDesktopDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		);
		if (!desktops.isEmpty()) {
			folders.addAll(desktops);
			desktop = folders.get(0);
		}
		folders.addAll(NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSDownloadsDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		));
		folders.addAll(NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSMoviesDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		));
		folders.addAll(NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSMusicDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		));
		folders.addAll(NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSPicturesDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		));
		folders.addAll(NSFoundation.nsSearchPathForDirectoriesInDomains(
			NSSearchPathDirectory.NSSharedPublicDirectory,
			NSSearchPathDomainMask.NSAllDomainsMask,
			true
		));
		return desktop;
	}
}
