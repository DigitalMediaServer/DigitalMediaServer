/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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

package net.pms;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.sun.jna.Platform;
import com.sun.net.httpserver.HttpServer;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.jmdns.JmDNS;
import javax.swing.*;
import net.pms.configuration.DeviceConfiguration;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.database.TableManager;
import net.pms.database.Tables;
import net.pms.dlna.CodeEnter;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.GlobalIdRepo;
import net.pms.dlna.Playlist;
import net.pms.dlna.RootFolder;
import net.pms.dlna.virtual.MediaLibrary;
import net.pms.encoders.PlayerFactory;
import net.pms.exception.InitializationException;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.formats.FormatType;
import net.pms.io.*;
import net.pms.logging.CacheLogger;
import net.pms.logging.FrameAppender;
import net.pms.logging.LogLevel;
import net.pms.logging.LoggingConfig;
import net.pms.network.ChromecastMgr;
import net.pms.network.HTTPServer;
import net.pms.network.UPNPHelper;
import net.pms.newgui.*;
import net.pms.newgui.StatusTab.ConnectionState;
import net.pms.newgui.components.WindowProperties.WindowPropertiesConfiguration;
import net.pms.remote.RemoteWeb;
import net.pms.service.Services;
import net.pms.util.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class PMS {
	private static final String SCROLLBARS = "scrollbars";
	private static final String CONSOLE = "console";
	private static final String NOCONSOLE = "noconsole";
	public static final String CROWDIN_LINK = "http://crowdin.com/project/DigitalMediaServer";

	private boolean ready = false;

	private static FileWatcher fileWatcher;

	private GlobalIdRepo globalRepo;

	public static final String AVS_SEPARATOR = "\1";

	// (innot): The logger used for all logging.
	private static final Logger LOGGER = LoggerFactory.getLogger(PMS.class);

	// TODO(tcox):  This shouldn't be static
	private static PmsConfiguration configuration;

	/**
	 * Universally Unique Identifier used in the UPnP server.
	 */
	private String uuid;

	/**
	 * Relative location of a context sensitive help page in the documentation
	 * directory.
	 */
	private static String helpPage = "index.html";

	private JmDNS jmDNS;

	/**
	 * Returns a pointer to the DMS GUI's main window.
	 * @return {@link net.pms.newgui.IFrame} Main DMS window.
	 */
	public IFrame getFrame() {
		return frame;
	}

	/**
	 * Returns the root folder for a given renderer. There could be the case
	 * where a given media renderer needs a different root structure.
	 *
	 * @param renderer {@link net.pms.configuration.RendererConfiguration}
	 * is the renderer for which to get the RootFolder structure. If <code>null</code>,
	 * then the default renderer is used.
	 * @return {@link net.pms.dlna.RootFolder} The root folder structure for a given renderer
	 */
	public RootFolder getRootFolder(RendererConfiguration renderer) {
		// something to do here for multiple directories views for each renderer
		if (renderer == null) {
			renderer = RendererConfiguration.getDefaultConf();
		}

		return renderer.getRootFolder();
	}

	/**
	 * Pointer to a running DMS server.
	 */
	private static PMS instance = null;

	/**
	 * An array of {@link RendererConfiguration}s that have been found by DMS.
	 * <p>
	 * Important! If iteration is done on this list it's not thread safe unless
	 * the iteration loop is enclosed by a {@code synchronized} block on the <b>
	 * {@link List} itself</b>.
	 */
	private final List<RendererConfiguration> foundRenderers = Collections.synchronizedList(new ArrayList<RendererConfiguration>());

	/**
	 * The returned <code>List</code> itself is thread safe, but the objects
	 * it's holding is not. Any looping/iterating of this <code>List</code>
	 * MUST be enclosed in:
	 * S<pre><code>
	 * synchronized(getFoundRenderers()) {
	 *      ..code..
	 * }
	 * </code></pre>
	 * @return {@link #foundRenderers}
	 */
	public List<RendererConfiguration> getFoundRenderers() {
		return foundRenderers;
	}

	/**
	 * @deprecated Use {@link #setRendererFound(RendererConfiguration)} instead.
	 */
	@Deprecated
	public void setRendererfound(RendererConfiguration renderer) {
		setRendererFound(renderer);
	}

	/**
	 * Adds a {@link net.pms.configuration.RendererConfiguration} to the list of media renderers found.
	 * The list is being used, for example, to give the user a graphical representation of the found
	 * media renderers.
	 *
	 * @param renderer {@link net.pms.configuration.RendererConfiguration}
	 * @since 1.82.0
	 */
	public void setRendererFound(RendererConfiguration renderer) {
		synchronized (foundRenderers) {
			if (!foundRenderers.contains(renderer) && !renderer.isFDSSDP()) {
				LOGGER.debug("Adding status button for {}", renderer.getRendererName());
				foundRenderers.add(renderer);
				frame.addRenderer(renderer);
				frame.setConnectionState(ConnectionState.CONNECTED);
			}
		}
	}

	public void updateRenderer(RendererConfiguration renderer) {
		LOGGER.debug("Updating status button for {}", renderer.getRendererName());
		frame.updateRenderer(renderer);
	}

	/**
	 * HTTP server that serves the XML files needed by UPnP server and the media files.
	 */
	private HTTPServer server;

	/**
	 * User friendly name for the server.
	 */
	private String serverName;

	public ArrayList<Process> currentProcesses = new ArrayList<>();

	private PMS() {
	}

	/**
	 * {@link net.pms.newgui.IFrame} object that represents the DMS GUI.
	 */
	private IFrame frame;

	/**
	 * Main resource database that supports search capabilities. Also known as media cache.
	 * @see net.pms.dlna.DLNAMediaDatabase
	 */
	private DLNAMediaDatabase database;
	private Object databaseLock = new Object();

	/**
	 * Used to get the database. Needed in the case of the Xbox 360, that requires a database.
	 * for its queries.
	 * @return (DLNAMediaDatabase) a reference to the database.
	 */
	public DLNAMediaDatabase getDatabase() {
		synchronized (databaseLock) {
			if (database == null) {
				database = new DLNAMediaDatabase();
				database.init(false);
			}
			return database;
		}
	}

	private void displayBanner() throws IOException {
		LOGGER.debug("");
		LOGGER.info("Starting {} {}", getName(), getVersion());
		LOGGER.info("Based on PS3 Media Server by shagrath and Universal Media Server");
		LOGGER.info("http://www.digitalmediaserver.org");
		LOGGER.info("");

		String commitId = PropertiesUtil.getProjectProperties().get("git.commit.id");
		LOGGER.info(
			"Build: {} ({})",
			commitId.substring(0, 9),
			PropertiesUtil.getProjectProperties().get("git.commit.time")
		);

		// Log system properties
		logSystemInfo();

		String cwd = new File("").getAbsolutePath();
		LOGGER.info("Working directory: {}", cwd);

		LOGGER.info("Temporary directory: {}", configuration.getTempFolder());

		/**
		 * Verify the java.io.tmpdir is writable; JNA requires it.
		 * Note: the configured tempFolder has already been checked, but it
		 * may differ from the java.io.tmpdir so double check to be sure.
		 */
		File javaTmpdir = new File(System.getProperty("java.io.tmpdir"));

		if (!FileUtil.getFilePermissions(javaTmpdir).isWritable()) {
			LOGGER.error("The Java temp directory \"{}\" is not writable by DMS", javaTmpdir.getAbsolutePath());
			LOGGER.error("Please make sure the directory is writable for user \"{}\"", System.getProperty("user.name"));
			throw new IOException("Cannot write to Java temp directory: " + javaTmpdir.getAbsolutePath());
		}

		LOGGER.info("Logging configuration file: {}", LoggingConfig.getConfigFilePath());

		HashMap<String, String> lfps = LoggingConfig.getLogFilePaths();

		// Logfile name(s) and path(s)
		if (lfps != null && lfps.size() > 0) {
			if (lfps.size() == 1) {
				Entry<String, String> entry = lfps.entrySet().iterator().next();
				if (entry.getKey().toLowerCase().equals("default.log")) {
					LOGGER.info("Logfile: {}", entry.getValue());
				} else {
					LOGGER.info("{}: {}", entry.getKey(), entry.getValue());
				}
			} else {
				LOGGER.info("Logging to multiple files:");
				Iterator<Entry<String, String>> logsIterator = lfps.entrySet().iterator();
				Entry<String, String> entry;
				while (logsIterator.hasNext()) {
					entry = logsIterator.next();
					LOGGER.info("{}: {}", entry.getKey(), entry.getValue());
				}
			}
		}

		String profilePath = configuration.getProfilePath();
		String profileDirectoryPath = configuration.getProfileFolder();

		LOGGER.info("");
		LOGGER.info("Profile directory: {}", profileDirectoryPath);
		try {
			// Don't use the {} syntax here as the check needs to be performed on every log level
			LOGGER.info("Profile directory permissions: " + FileUtil.getFilePermissions(profileDirectoryPath));
		} catch (FileNotFoundException e) {
			LOGGER.warn("Profile directory not found: {}", e.getMessage());
		}
		LOGGER.info("Profile configuration file: {}", profilePath);
		try {
			// Don't use the {} syntax here as the check needs to be performed on every log level
			LOGGER.info("Profile configuration file permissions: " + FileUtil.getFilePermissions(profilePath));
		} catch (FileNotFoundException e) {
			LOGGER.warn("Profile configuration file not found: {}", e.getMessage());
		}
		LOGGER.info("Profile name: {}", configuration.getProfileName());
		LOGGER.info("");
		if (configuration.getExternalNetwork()) {
			File webConf = new File(configuration.getWebConfPath());
			if (webConf.exists()) {
				LOGGER.info("Web configuration file: {}", webConf.getAbsolutePath());
				try {
					FilePermissions webConfPermissions= FileUtil.getFilePermissions(webConf);
					LOGGER.info("Web configuration file permissions: {}", webConfPermissions);
				} catch (FileNotFoundException e) {
					// Should not happen
					LOGGER.info("Web configuration file not found: {}", e.getMessage());
				}
				LOGGER.info("");
			} else if (configuration.isWebConfPathSpecified()) {
				LOGGER.warn("Couldn't read the specified web configuration file \"{}\"", webConf);
				LOGGER.info("");
			}
		} else {
			LOGGER.info("Internet/external network access is denied");
			LOGGER.info("");
		}

		/**
		 * Ensure the data directory is created. On Windows this is
		 * usually done by the installer
		 */
		File dDir = new File(configuration.getDataDir());
		if (!dDir.exists() && !dDir.mkdirs()) {
			LOGGER.error("Failed to create profile folder \"{}\"", configuration.getDataDir());
		}

		dbgPack = new DbgPacker();
		tfm = new TempFileMgr();

		// This should be removed soon
		OpenSubtitle.convert();

		// Start this here to let the conversion work
		tfm.schedule();

	}

	/**
	 * Initialization procedure for DMS.
	 *
	 * @return {@code true} if the server has been initialized correctly,
	 *         {@code false} if initialization was aborted.
	 * @throws IOException If an I/O error occurs during initialization.
	 * @throws InitializationException If an error occurs during initialization.
	 */
	private boolean init(@Nullable Map<Option, Object> options) throws IOException, InitializationException {
		// Gather and log system information from a separate thread
		LogSystemInformationMode logSystemInfo = configuration.getLogSystemInformation();
		if (
			logSystemInfo == LogSystemInformationMode.ALWAYS ||
			logSystemInfo == LogSystemInformationMode.TRACE_ONLY &&
			LOGGER.isTraceEnabled()
		) {
			new SystemInformation().start();
		}

		// Show the language selection dialog before displayBanner();
		if (
			!isHeadless() &&
			(configuration.getLanguageRawString() == null ||
			!Languages.isValid(configuration.getLanguageRawString()))
		) {
			final boolean[] aborted = new boolean[1];
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						LanguageSelection languageDialog = new LanguageSelection(null, PMS.getLocale(), false);
						languageDialog.show();
						aborted[0] = languageDialog.isAborted();
					}
				});
			} catch (InterruptedException e) {
				LOGGER.info("LanguageSelection dialog was interrupted, aborting...");
				return false;
			} catch (InvocationTargetException e) {
				LOGGER.error("An error occurred during the LanguageSelection dialog: {}", e);
				return false;
			}
			if (aborted[0]) {
				return false;
			}
		}

		// Initialize splash screen
		final WindowPropertiesConfiguration[] windowConfiguration = new WindowPropertiesConfiguration[1];
		final Splash[] splash = new Splash[1];

		if (!isHeadless()) {
			final boolean initSplash =
				configuration.isShowSplashScreen() &&
				!configuration.isGUIStartHidden();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						windowConfiguration[0] = new WindowPropertiesConfiguration(
							Paths.get(configuration.getProfileFolder()).resolve("DMS.dat")
						);
						if (initSplash) {
							splash[0] = new Splash(configuration, windowConfiguration[0].getGraphicsConfiguration());
						}
					}
				});
			} catch (InterruptedException e) {
				LOGGER.info("Creation of WindowPropertiesConfiguration or Splash was interrupted, aborting...");
				return false;
			} catch (InvocationTargetException e) {
				LOGGER.error("An error occurred during creation of WindowPropertiesConfiguration or Splash: {}", e);
				return false;
			}
		}

		// Call this as early as possible
		displayBanner();

		// Initialize database
		try {
			if (!initializeDatabase(options, splash[0])) {
				if (splash[0] != null) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							splash[0].dispose();
							splash[0] = null;
						}
					});
				}
				return false;
			}
		} catch (InitializationException e) {
			if (splash[0] != null) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						splash[0].dispose();
						splash[0] = null;
					}
				});
			}
			throw e;
		}

		// Log registered ImageIO plugins
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("");
			LOGGER.trace("Registered ImageIO reader classes:");
			Iterator<ImageReaderSpi> readerIterator = IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, true);
			while (readerIterator.hasNext()) {
				ImageReaderSpi reader = readerIterator.next();
				LOGGER.trace("Reader class: {}", reader.getPluginClassName());
			}
			LOGGER.trace("");
			LOGGER.trace("Registered ImageIO writer classes:");
			Iterator<ImageWriterSpi> writerIterator = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
			while (writerIterator.hasNext()) {
				ImageWriterSpi writer = writerIterator.next();
				LOGGER.trace("Writer class: {}", writer.getPluginClassName());
			}
			LOGGER.trace("");
		}

		// Wizard
		if (configuration.isRunWizard() && !isHeadless()) {
			ConfigurationWizard.run(configuration, splash[0]);
		}

		fileWatcher = new FileWatcher();

		globalRepo = new GlobalIdRepo();

		if (!isHeadless()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						frame = new LooksFrame(configuration, windowConfiguration[0]);
					}
				});
			} catch (InterruptedException e) {
				LOGGER.info("Creation of the main GUI window was interrupted, aborting...");
				return false;
			} catch (InvocationTargetException e) {
				LOGGER.error("An error occurred during creation of the main GUI window: {}", e);
				return false;
			}
		} else {
			LOGGER.info("Graphics environment not available or headless mode is forced");
			LOGGER.info("Switching to console mode");
			frame = new DummyFrame();
		}

		// Close splash screen
		if (splash[0] != null) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					splash[0].dispose();
					splash[0] = null;
				}
			});
		}

		/*
		 * we're here:
		 *
		 *     main() -> createInstance() -> init()
		 *
		 * which means we haven't created the instance returned by get()
		 * yet, so the frame appender can't access the frame in the
		 * standard way i.e. PMS.get().getFrame(). we solve it by
		 * inverting control ("don't call us; we'll call you") i.e.
		 * we notify the appender when the frame is ready rather than
		 * e.g. making getFrame() static and requiring the frame
		 * appender to poll it.
		 *
		 * XXX an event bus (e.g. MBassador or Guava EventBus
		 * (if they fix the memory-leak issue)) notification
		 * would be cleaner and could support other lifecycle
		 * notifications (see above).
		 */
		FrameAppender.setFrame(frame);

		configuration.addConfigurationListener(new ConfigurationListener() {
			@Override
			public void configurationChanged(ConfigurationEvent event) {
				if ((!event.isBeforeUpdate()) && PmsConfiguration.NEED_RELOAD_FLAGS.contains(event.getPropertyName())) {
					frame.setReloadable(true);
				}
			}
		});

		// Web stuff
		if (configuration.useWebInterface()) {
			try {
				web = new RemoteWeb(configuration.getWebPort());
				frame.webInterfaceEnabled(true);
			} catch (FileNotFoundException e) {
				LOGGER.error("Web interface not available: {}", e.getMessage());
			} catch (BindException e) {
				LOGGER.error("FATAL ERROR: Unable to bind web interface on port {}: {}", configuration.getWebPort(), e.getMessage());
				LOGGER.info("Maybe another process is running or the hostname is wrong.");
				LOGGER.trace("", e);
			}
		}

		// init Credentials
		credMgr = new CredMgr(configuration.getCredFile());

		// init dbs
		keysDb = new DmsKeysDb();
		infoDb = new InfoDb();
		codes = new CodeDb();
		masterCode = null;

		RendererConfiguration.loadRendererConfigurations(configuration);
		// Now that renderer confs are all loaded, we can start searching for renderers
		UPNPHelper.getInstance().init();

		// launch ChromecastMgr
		jmDNS = null;
		launchJmDNSRenderers();

		// Initialize MPlayer and FFmpeg to let them generate fontconfig cache/s
		if (!configuration.isDisableSubtitles()) {
			LOGGER.info("Checking the fontconfig cache in the background, this can take two minutes or so.");

			//TODO: Rewrite fontconfig generation
			ThreadedProcessWrapper.runProcessNullOutput(5, TimeUnit.MINUTES, 2000, configuration.getMPlayerPath(), "dummy");

			/**
			 * Note: Different versions of fontconfig and bitness require
			 * different caches, which is why here we ask FFmpeg (64-bit
			 * if possible) to create a cache.
			 * This should result in all of the necessary caches being built.
			 */
			if (!Platform.isWindows() || Platform.is64Bit()) {
				ThreadedProcessWrapper.runProcessNullOutput(
					5,
					TimeUnit.MINUTES,
					2000,
					configuration.getFFmpegPaths().getDefaultPath().toString(),
					"-y",
					"-f",
					"lavfi",
					"-i",
					"nullsrc=s=720x480:d=1:r=1",
					"-vf",
					"ass=DummyInput.ass",
					"-target",
					"ntsc-dvd",
					"-"
				);
			}
		}

		frame.setConnectionState(ConnectionState.SEARCHING);

		// Check the existence of VSFilter / DirectVobSub
		if (BasicSystemUtils.INSTANCE.isAviSynthAvailable() && BasicSystemUtils.INSTANCE.getAvsPluginsDir() != null) {
			LOGGER.debug("AviSynth plugins directory: " + BasicSystemUtils.INSTANCE.getAvsPluginsDir().getAbsolutePath());
			File vsFilterDLL = new File(BasicSystemUtils.INSTANCE.getAvsPluginsDir(), "VSFilter.dll");
			if (vsFilterDLL.exists()) {
				LOGGER.debug("VSFilter / DirectVobSub was found in the AviSynth plugins directory.");
			} else {
				File vsFilterDLL2 = new File(BasicSystemUtils.INSTANCE.getKLiteFiltersDir(), "vsfilter.dll");
				if (vsFilterDLL2.exists()) {
					LOGGER.debug("VSFilter / DirectVobSub was found in the K-Lite Codec Pack filters directory.");
				} else {
					LOGGER.info("VSFilter / DirectVobSub was not found. This can cause problems when trying to play subtitled videos with AviSynth.");
				}
			}
		}

		// Check if Kerio is installed
		if (BasicSystemUtils.INSTANCE.isKerioFirewall()) {
			LOGGER.info("Detected Kerio firewall");
		}

		// Disable jaudiotagger logging
		LogManager.getLogManager().readConfiguration(
			new ByteArrayInputStream("org.jaudiotagger.level=OFF".getBytes(StandardCharsets.US_ASCII))
		);

		// Wrap System.err
		System.setErr(new PrintStream(new SystemErrWrapper(), true, StandardCharsets.UTF_8.name()));

		server = new HTTPServer(configuration.getServerPort());

		// Initialize a player factory to register all players
		PlayerFactory.initialize();

		// Any plugin-defined players are now registered, create the gui view.
		frame.addEngines();

		boolean binding = false;

		try {
			binding = server.start();
		} catch (BindException b) {
			LOGGER.error("FATAL ERROR: Unable to bind on port: " + configuration.getServerPort() + ", because: " + b.getMessage());
			LOGGER.info("Maybe another process is running or the hostname is wrong.");
		}

		new Thread("Connection Checker") {
			@Override
			public void run() {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
				}

				if (foundRenderers.isEmpty()) {
					frame.setConnectionState(ConnectionState.DISCONNECTED);
				} else {
					frame.setConnectionState(ConnectionState.CONNECTED);
				}
			}
		}.start();

		if (!binding) {
			return false;
		}

		if (web != null && web.getServer() != null) {
			LOGGER.info("WEB interface is available at: {}", web.getUrl());
		}

		// initialize the cache
		if (configuration.getUseCache()) {
			mediaLibrary = new MediaLibrary();
			LOGGER.info("A tiny cache admin interface is available at: http://{}:{}/console/home", server.getHost(), server.getPort());
		}

		// XXX: this must be called:
		//     a) *after* loading plugins i.e. plugins register root folders then RootFolder.discoverChildren adds them
		//     b) *after* mediaLibrary is initialized, if enabled (above)
		getRootFolder(RendererConfiguration.getDefaultConf());

		frame.serverReady();

		ready = true;

		// UPNPHelper.sendByeBye();
		Runtime.getRuntime().addShutdownHook(new Thread("DMS Shutdown") {
			@Override
			public void run() {
				try {
					UPNPHelper.shutDownListener();
					UPNPHelper.sendByeBye();

					LOGGER.debug("Shutting down the HTTP server");
					if (instance != null && instance.getServer() != null) {
						instance.getServer().stop();
						Thread.sleep(500);
					}

					LOGGER.debug("Shutting down all active processes");

					if (Services.processManager() != null) {
						Services.processManager().stop();
					}
					for (Process p : currentProcesses) {
						try {
							p.exitValue();
						} catch (IllegalThreadStateException ise) {
							LOGGER.trace("Forcing shutdown of process: " + p);
							ProcessUtil.destroy(p);
						}
					}
				} catch (InterruptedException e) {
					LOGGER.debug("Interrupted while shutting down..");
					LOGGER.trace("", e);
				}

				// Destroy services
				Services.destroy();

				LOGGER.info("Stopping {} {}", PMS.getName(), getVersion());
				/**
				 * Stopping logging gracefully (flushing logs)
				 * No logging is available after this point
				 */
				ILoggerFactory iLoggerContext = LoggerFactory.getILoggerFactory();
				if (iLoggerContext instanceof LoggerContext) {
					((LoggerContext) iLoggerContext).stop();
				} else {
					LOGGER.error("Unable to shut down logging gracefully");
					System.err.println("Unable to shut down logging gracefully");
				}

			}
		});

		configuration.setAutoSave();
		UPNPHelper.sendByeBye();
		try {
			LOGGER.trace("Waiting 250 milliseconds...");
			Thread.sleep(250);
			UPNPHelper.sendAlive();
			LOGGER.trace("Waiting 250 milliseconds...");
			Thread.sleep(250);
			UPNPHelper.listen();
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	private static boolean initializeDatabase(
		@Nullable Map<Option, Object> options,
		@Nullable final Splash splash
	) throws InitializationException {
		Services services = Services.get();
		if (Services.get() == null) {
			throw new InitializationException("Services don't exist during database initialization");
		}

		services.createTableManager();
		final TableManager tableManager = services.getTableManager();
		if (tableManager.isError()) {
			if (options != null && options.containsKey(Option.DB_BACKUP_DOWNGRADE) && tableManager.hasFutureTables()) {
				try {
					// Backup
					String backupName = Tables.copyDatabase(
						tableManager.getDatabaseFilepath(false),
						options.get(Option.DB_BACKUP_DOWNGRADE),
						false
					);
					LOGGER.info("Successfully backed up the database to \"{}\"", backupName);

					// Downgrade
					Tables.downgradeDatabase(tableManager);
					LOGGER.info("Successfully deleted incompatible database tables");
					tableManager.start();
					if (tableManager.isError()) {
						if (tableManager.getError() != null) {
							throw new InitializationException(
								"Failed to initialize the database after downgrade: " +
								tableManager.getError().getMessage(),
								tableManager.getError()
							);
						}
						// Should never get here
						throw new InitializationException("Failed to initialize the database after downgrade");
					}
					return true;
				} catch (IOException e) {
					LOGGER.error("Failed to backup the database, aborting: {}", e.getMessage());
					LOGGER.trace("", e);
					throw new InitializationException("Failed to backup database: " + e.getMessage(), e);
				} catch (SQLException e) {
					LOGGER.error("Failed to delete incompatible database tables, aborting: {}", e.getMessage());
					LOGGER.trace("", e);
					throw new InitializationException("Failed to downgrade database: " + e.getMessage(), e);
				}
			}
			if (!tableManager.isAlreadyOpenError() && options != null && options.containsKey(Option.DB_RENAME)) {
				try {
					String newName = Tables.copyDatabase(
						tableManager.getDatabaseFilepath(false),
						options.get(Option.DB_RENAME),
						true
					);
					LOGGER.info("Successfully renamed the database to \"{}\"", newName);
					tableManager.start();
					if (tableManager.isError()) {
						if (tableManager.getError() != null) {
							throw new InitializationException(
								"Failed to initialize the database after rename: " +
								tableManager.getError().getMessage(),
								tableManager.getError()
							);
						}
						// Should never get here
						throw new InitializationException("Failed to initialize the database after rename");
					}
					return true;
				} catch (IOException e) {
					LOGGER.error("Failed to rename the database, aborting: {}", e.getMessage());
					LOGGER.trace("", e);
					throw new InitializationException("Failed to rename database: " + e.getMessage(), e);
				}
			}
			if (options != null && options.containsKey(Option.DB_DOWNGRADE) && tableManager.hasFutureTables()) {
				try {
					Tables.downgradeDatabase(tableManager);
					LOGGER.info("Successfully deleted incompatible database tables");
					tableManager.start();
					if (tableManager.isError()) {
						if (tableManager.getError() != null) {
							throw new InitializationException(
								"Failed to initialize the database after downgrade: " +
								tableManager.getError().getMessage(),
								tableManager.getError()
							);
						}
						// Should never get here
						throw new InitializationException("Failed to initialize the database after downgrade");
					}
					return true;
				} catch (SQLException e) {
					LOGGER.error("Failed to delete incompatible database tables, aborting: {}", e.getMessage());
					LOGGER.trace("", e);
					throw new InitializationException("Failed to downgrade database: " + e.getMessage(), e);
				}
			}
			if (isHeadless()) {
				// Handle headless
				if (tableManager.isAlreadyOpenError()) {
					LOGGER.error("DMS can't start because the database located at");
					LOGGER.error("\"{}\"", tableManager.getDatabaseFilepath(true));
					LOGGER.error("is in use. This is normally because another instance of DMS is already running.");
				} else if (tableManager.isWrongVersionOrCorrupt()) {
					LOGGER.error("The database is either of a wrong version or corrupt. Attempting to rename the");
					LOGGER.error("database and create a new.");
					try {
						String source = tableManager.getDatabaseFilepath(false);
						String newName = Tables.copyDatabase(
							source,
							Tables.suggestNewDatabaseName(source, "bad"),
							true
						);
						LOGGER.info("Successfully renamed the database to \"{}\"", newName);
						tableManager.start();
						if (tableManager.isError()) {
							if (tableManager.getError() != null) {
								throw new InitializationException(
									"Failed to initialize the database after rename: " +
									tableManager.getError().getMessage(),
									tableManager.getError()
								);
							}
							// Should never get here
							throw new InitializationException("Failed to initialize the database after rename");
						}
						return true;
					} catch (IOException e) {
						LOGGER.error("Failed to rename the database, aborting: {}", e.getMessage());
						LOGGER.trace("", e);
						throw new InitializationException(
							"Database is wrong version or corrupt, automatic rename failed: " + e.getMessage(), e
						);
					}
				} else if (tableManager.hasFutureTables()) {
					boolean plural = tableManager.getFutureTables(true, false, true).size() > 1;
					LOGGER.error("The database has tables from a newer version of DMS that are incompatible");
					LOGGER.error("with this version. This can be resolved manually by deleting, renaming or");
					LOGGER.error("moving the database located at");
					LOGGER.error("\"{}\",", tableManager.getDatabaseFilepath(true));
					LOGGER.error("or automatically by starting DMS using either the \"-db rename\" or the ");
					LOGGER.error("\"-db downgrade\" command line argument.");
					LOGGER.error("");
					LOGGER.error("\"-rename\" will rename the current and create a new empty database,");
					LOGGER.error("\"-downgrade\" will only delete the incompatible tables and keep the remaining");
					LOGGER.error("data.");
					LOGGER.error("");
					LOGGER.error(
						"The incompatible table{} {}: {}",
						plural ? "s" : "",
						plural ? "are" : "is",
						tableManager.getFutureTablesString(true, true)
					);
				} else {
					LOGGER.error("A database error prevents DMS from starting. If you cannot resolve the error,");
					LOGGER.error("delete, rename or move the database located at");
					LOGGER.error("\"{}\".", tableManager.getDatabaseFilepath(true));
					LOGGER.error("Alternatively DMS can be started using the \"db -rename\" command line argument.");
					LOGGER.error("In either case, a new empty database will be created.");
				}
				return false;
			}

			// Handle with GUI
			final boolean[] aborted = new boolean[1];

			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						if (splash != null) {
							splash.setVisible(false);
						}
						DatabaseProblem databaseProblem = new DatabaseProblem(null, tableManager);
						databaseProblem.show();
						aborted[0] = databaseProblem.isAborted();
						if (!aborted[0] && splash != null) {
							splash.setVisible(true);
						}
					}
				});
			} catch (InterruptedException e) {
				LOGGER.info("DatabaseProblem dialog was interrupted, aborting...");
				return false;
			} catch (InvocationTargetException e) {
				LOGGER.error("An error occurred during the DatabaseProblem dialog: {}", e);
				return false;
			}
			if (aborted[0]) {
				return false;
			}
		}

		return true;
	}

	private MediaLibrary mediaLibrary;

	/**
	 * Returns the MediaLibrary used by DMS.
	 *
	 * @return The current {@link MediaLibrary} or {@code null} if none is in
	 *         use.
	 */
	public MediaLibrary getLibrary() {
		return mediaLibrary;
	}

	/**
	 * Restarts the server. The trigger is either a button on the main DMS window or via
	 * an action item.
	 */
	// Note: Don't try to optimize this by reusing the same server instance.
	// see the comment above HTTPServer.stop()
	public void reset() {
		TaskRunner.getInstance().submitNamed("restart", true, new Runnable() {
			@Override
			public void run() {
				try {
					LOGGER.trace("Waiting 1 second...");
					UPNPHelper.sendByeBye();
					if (server != null) {
						server.stop();
					}
					server = null;
					RendererConfiguration.loadRendererConfigurations(configuration);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.trace("Caught exception", e);
					}

					server = new HTTPServer(configuration.getServerPort());
					server.start();
					UPNPHelper.sendAlive();
					frame.setReloadable(false);
				} catch (IOException e) {
					LOGGER.error("error during restart :" +e.getMessage(), e);
				}
			}
		});
	}

	// Cannot remove these methods because of backwards compatibility;
	// none of the DMS code uses it, but some plugins still do.

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the debug stream, or {@link System#out} in case the
	 * debug stream has not been set up yet.
	 * @param msg {@link String} to be added to the debug stream.
	 */
	@Deprecated
	public static void debug(String msg) {
		LOGGER.trace(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the info stream.
	 * @param msg {@link String} to be added to the info stream.
	 */
	@Deprecated
	public static void info(String msg) {
		LOGGER.debug(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the minimal stream. This stream is also
	 * shown in the Trace tab.
	 * @param msg {@link String} to be added to the minimal stream.
	 */
	@Deprecated
	public static void minimal(String msg) {
		LOGGER.info(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the error stream. This is usually called by
	 * statements that are in a try/catch block.
	 * @param msg {@link String} to be added to the error stream
	 * @param t {@link Throwable} comes from an {@link Exception}
	 */
	@Deprecated
	public static void error(String msg, Throwable t) {
		LOGGER.error(msg, t);
	}

	/**
	 * Creates a new random {@link #uuid}. These are used to uniquely identify the server to renderers (i.e.
	 * renderers treat multiple servers with the same UUID as the same server).
	 * @return {@link String} with an Universally Unique Identifier.
	 */
	// XXX don't use the MAC address to seed the UUID as it breaks multiple profiles:
	// http://www.ps3mediaserver.org/forum/viewtopic.php?f=6&p=75542#p75542
	public synchronized String usn() {
		if (uuid == null) {
			// Retrieve UUID from configuration
			uuid = getConfiguration().getUuid();

			if (uuid == null) {
				uuid = UUID.randomUUID().toString();
				LOGGER.info("Generated new random UUID: {}", uuid);

				// save the newly-generated UUID
				getConfiguration().setUuid(uuid);

				try {
					getConfiguration().save();
				} catch (ConfigurationException e) {
					LOGGER.error("Failed to save configuration with new UUID", e);
				}
			} else {
				LOGGER.info("Using configured UUID: {}", uuid);
			}
		}

		return "uuid:" + uuid;
	}

	/**
	 * Returns the user friendly name of the UPnP server.
	 * @return {@link String} with the user friendly name.
	 */
	public String getServerName() {
		if (serverName == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.getProperty("os.name").replace(" ", "_"));
			sb.append('-');
			sb.append(System.getProperty("os.arch").replace(" ", "_"));
			sb.append('-');
			sb.append(System.getProperty("os.version").replace(" ", "_"));
			sb.append(", UPnP/1.0 DLNADOC/1.50, DMS/").append(getVersion());
			serverName = sb.toString();
		}

		return serverName;
	}

	/**
	 * Returns the PMS instance.
	 *
	 * @return {@link net.pms.PMS}
	 * @throws InitializationException If an error occurs during initialization.
	 */
	@Nonnull
	public static PMS get() {
		// When DMS is run as an application, the instance is initialized via the createInstance call in main().
		// However, plugin tests may need access to a DMS instance without going
		// to the trouble of launching the DMS application, so we provide a fallback
		// initialization here. Either way, createInstance() should only be called once (see below)
		if (instance == null) {
			try {
				createInstance(null);
			} catch (InitializationException e) {
				//XXX This is a bad solution which will lead to NPEs if initialization fails.
				// It is however the only solution without completely refactoring the use of
				// the "PMS" instance.
				LOGGER.error("An error occurred while instantiating the master instance: {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}

		return instance;
	}

	private synchronized static void createInstance(@Nullable Map<Option, Object> options) throws InitializationException {
		assert instance == null; // this should only be called once
		instance = new PMS();

		try {
			if (instance.init(options)) {
				LOGGER.info("{} is now available for renderers to find", getName());
			} else {
				LOGGER.info("{} initialization was aborted", getName());
			}
		} catch (IOException e) {
			throw new InitializationException("An I/O error occurred during initialization: " + e.getMessage(), e);
		}
	}

	/**
	 * @deprecated Use {@link net.pms.formats.FormatFactory#getAssociatedFormat(String)}
	 * instead.
	 *
	 * @param filename
	 * @return The format.
	 */
	@Deprecated
	public Format getAssociatedFormat(String filename) {
		return FormatFactory.getAssociatedFormat(filename);
	}

	@Nonnull
	private static Map<Option, Object> parseArgs(@Nullable String[] args) {
		HashMap<Option, Object> result = new HashMap<>();
		if (args == null || args.length == 0) {
			return result;
		}

		Pattern separator = Pattern.compile("=");
		ArrayList<String> arguments = new ArrayList<>();
		for (String argument : args) {
			String[] parts = separator.split(argument);
			if (parts.length > 1) {
				for (String part : parts) {
					if (isNotBlank(part)) {
						arguments.add(part.trim());
					}
				}
			} else {
				arguments.add(argument);
			}
		}

		for (int i = 0; i < arguments.size(); i++) {
			String argument = arguments.get(i).trim();
			if (!argument.startsWith("-")) {
				argument = argument.toLowerCase(Locale.ROOT);
			}
			switch (argument) {
				case "-h":
				case "--help":
				case "help":
					result.put(Option.HELP, null);
					break;
				case "-V":
				case "--version":
				case "version":
					result.put(Option.VERSION, null);
					break;
				case "-c":
				case "--console":
				case "--headless":
				case "headless":
				case CONSOLE:
					result.put(Option.HEADLESS, null);
					break;
				case "-s":
				case "--scrollbars":
				case SCROLLBARS:
					result.put(Option.SCROLLBARS, null);
					break;
				case "-C":
				case "--noconsole":
				case NOCONSOLE:
					result.put(Option.NOCONSOLE, null);
					break;
				case "-v":
				case "--verbose":
				case "-t":
				case "--trace":
				case "trace":
					result.put(Option.TRACE, null);
					break;
				case "-db":
				case "--database":
				case "db":
					if (i < arguments.size() - 1) {
						String value = arguments.get(++i);
						if (isNotBlank(value)) {
							value = value.trim().toLowerCase(Locale.ROOT);
							switch (value) {
								case "log":
								case "trace":
									result.put(Option.DB_LOG, null);
									break;
								case "backup":
									if (i < arguments.size() - 1) {
										String backupName = arguments.get(++i);
										if (isNotBlank(backupName)) {
											result.put(Option.DB_BACKUP_DOWNGRADE, backupName);
											break;
										}
									}
									result.put(Option.DB_BACKUP_DOWNGRADE, null);
									break;
								case "downgrade":
									result.put(Option.DB_DOWNGRADE, null);
									break;
								case "rename":
									if (i < arguments.size() - 1) {
										String newName = arguments.get(++i);
										if (isNotBlank(newName)) {
											result.put(Option.DB_RENAME, newName);
											break;
										}
									}
									result.put(Option.DB_RENAME, null);
									break;
								default:
									LOGGER.warn("Ignoring unknown {} argument \"{}\"", argument, value);
									break;
							}
							break;
						}
					}
					LOGGER.warn("Ignoring blank {} argument", argument);
					break;
				case "-P":
				case "--profiles":
				case "profiles":
					result.put(Option.SELECT_PROFILE, null);
					break;
				case "-p":
				case "--profile":
				case "profile":
					if (i < arguments.size() - 1) {
						String value = arguments.get(++i);
						if (isNotBlank(value)) {
							result.put(Option.PROFILE, value);
							break;
						}
					}
					LOGGER.warn("Ignoring blank profile");
					break;
				default:
					LOGGER.warn("Ignoring unknown argument \"{}\"", argument);
					break;
			}
		}
		return result;
	}

	private static void printHelp() {
		getVersion();
		PrintStream out = System.out;
		out.println("Options:");
		out.println("  -p, --profile=PROFILE_PATH      Use the configuration in PROFILE_PATH.");
		out.println("  -P, --profiles                  Show the profile selection dialog during");
		out.println("                                  startup, ignored if running headless or if a");
		out.println("                                  profile is specified.");
		out.println("  -v, --trace                     Force logging level to TRACE.");
		out.println("  -c, --headless                  Run without GUI.");
		out.println("  -C, --noconsole                 Fail if a GUI can't be created.");
		out.println("  -s, --scrollbars                Force horizontal and vertical GUI scroll bars.");
		out.println("  -db, --database");
		out.println("     log, trace                   Enable database logging.");
		out.println("     downgrade                    Delete and recreate any database tables of a");
		out.println("                                  newer version. The data in the incompatible");
		out.println("                                  tables will be lost.");
		out.println("     backup[=NAME]                Copy the database before downgrading it if any");
		out.println("                                  database tables are of a newer version. If a");
		out.println("                                  name for the backup isn't provided, one will");
		out.println("                                  be generated.");
		out.println("     rename[=NAME]                Rename the database and create a new, empty");
		out.println("                                  database if there is a problem with the");
		out.println("                                  current database. If a name isn't provided,");
		out.println("                                  one will be generated.");
		out.println("  -V, --version                   Display the version and exit.");
		out.println("  -h, --help                      Display this help and exit.");
	}

	private static void printVersion() {
		System.out.println(getName() + " " + getVersion());
		System.out.println();
	}

	public static void main(String args[]) {

		// This must be called before JNA is used
		configureJNA();

		// Start caching log messages until the logger is configured
		CacheLogger.startCaching();

		// Get options
		Map<Option, Object> options = parseArgs(args);
		if (options.containsKey(Option.HELP)) {
			printHelp();
			System.exit(0);
		}
		if (options.containsKey(Option.VERSION)) {
			printVersion();
			System.exit(0);
		}
		if (System.getProperty(CONSOLE, "").equalsIgnoreCase(Boolean.toString(true))) {
			options.put(Option.HEADLESS, null);
		}
		if (System.getProperty(NOCONSOLE, "").equalsIgnoreCase(Boolean.toString(true))) {
			options.put(Option.NOCONSOLE, null);
		}

		// Apply options
		if (options.containsKey(Option.HEADLESS)) {
			forceHeadless();
		}
		if (options.containsKey(Option.SCROLLBARS)) {
			System.setProperty(SCROLLBARS, Boolean.toString(true));
		}
		if (options.containsKey(Option.TRACE)) {
			traceMode = 2;
		}
		if (options.containsKey(Option.DB_LOG)) {
			logDB = true;
		}

		if (isHeadless()) {
			System.setProperty("java.awt.headless", Boolean.toString(true));
		}
		try {
			Toolkit.getDefaultToolkit();
		} catch (AWTError t) {
			LOGGER.error("Toolkit error, GUI is unavailable: {}: {}", t.getClass().getName(), t.getMessage());
			forceHeadless();
		}

		if (isHeadless() && options.containsKey(Option.NOCONSOLE)) {
			System.err.println(
				"Either a graphics environment isn't available or headless " +
				"mode is forced, but \"noconsole\" is specified. " + getName() +
				" can't start, exiting."
			);
			System.exit(1);
		} else if (!isHeadless()) {
			LooksFrame.initializeLookAndFeel();
		}

		if (options.containsKey(Option.PROFILE) && options.get(Option.PROFILE) instanceof String) {
			File profile = new File((String) options.get(Option.PROFILE));
			if (FileUtil.isValidFileName(profile)) {
				LOGGER.debug("Using specified profile: {}", profile.getAbsolutePath());
				System.setProperty("dms.profile.path", profile.getAbsolutePath());
			} else {
				LOGGER.error(
					"Invalid file or folder name \"{}\" in profile argument - using default profile",
					profile.getAbsolutePath()
				);
			}
		} else if (options.containsKey(Option.SELECT_PROFILE)) {
			if (isHeadless()) {
				LOGGER.error("The profile selection dialog isn't available in headless mode, using the default profile");
			} else {
				ProfileChooser.display();
			}
		}

		try {
			setConfiguration(new PmsConfiguration());
			assert getConfiguration() != null;

			/* Rename previous log file to .prev
			 * Log file location is unknown at this point, it's finally decided during loadFile() below
			 * but the file is also truncated at the same time, so we'll have to try a qualified guess
			 * for the file location.
			 */

			// Set root level from configuration here so that logging is available during renameOldLogFile();
			LoggingConfig.setRootLevel(getConfiguration().getRootLogLevel());
			renameOldLogFile();

			// Load the (optional) LogBack config file.
			// This has to be called after 'new PmsConfiguration'
			LoggingConfig.loadFile();

			// Check TRACE mode
			if (traceMode == 2) {
				LoggingConfig.setRootLevel(LogLevel.TRACE);
				LOGGER.debug("Forcing debug level to TRACE");
			} else {
				// Remember whether logging level was TRACE/ALL at startup
				traceMode = LoggingConfig.getRootLevel().toInt() <= Level.TRACE_INT ? 1 : 0;
			}

			// Configure syslog unless in forced trace mode
			if (traceMode != 2 && configuration.getLoggingUseSyslog()) {
				LoggingConfig.setSyslog();
			}
			// Configure log buffering
			if (traceMode != 2 && configuration.getLoggingBuffered()) {
				LoggingConfig.setBuffered(true);
			} else if (traceMode == 2) {
				// force unbuffered regardless of logback.xml if in forced trace mode
				LOGGER.debug("Forcing unbuffered verbose logging");
				LoggingConfig.setBuffered(false);
				LoggingConfig.forceVerboseFileEncoder();
			}

			// Write buffered messages to the log now that logger is configured
			CacheLogger.stopAndFlush();

			// Create services
			Services.create();

			LOGGER.debug(new Date().toString());

			try {
				getConfiguration().initCred();
			} catch (IOException e) {
				LOGGER.debug("Error initializing credentials: {}", e.getMessage());
				LOGGER.trace("", e);
			}

			// Create the PMS instance returned by get()
			createInstance(options); // Calls new() then init()
		} catch (ConfigurationException | InitializationException e) {
			final StringBuilder sb = new StringBuilder();
			String errorMessage;
			if (e instanceof ConfigurationException) {
				sb.append(Messages.getString("Application.ConfigurationError")).append(".");
				errorMessage = Messages.getRootString("Application.ConfigurationError") + ":";
			} else {
				sb.append(Messages.getString("Application.InitializationError")).append(".");
				errorMessage = Messages.getRootString("Application.InitializationError") + ":";
			}

			LOGGER.error("{} {}", errorMessage, e.getMessage());
			LOGGER.debug("", e);

			if (!isHeadless()) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							ErrorDialog errorDialog = new ErrorDialog(
								null,
								Messages.getString("PMS.42"),
								sb.toString(),
								null,
								e,
								LOGGER.isTraceEnabled()
							);
							errorDialog.show();
						}
					});
				} catch (InterruptedException e1) {
					LOGGER.error("Interrupted during the error dialog");
				} catch (InvocationTargetException e1) {
					LOGGER.error("An error occurred during the error dialog: {}", e1);
				}
			}
		}
	}

	public HTTPServer getServer() {
		return server;
	}

	public HttpServer getWebServer() {
		return web == null ? null : web.getServer();
	}

	/**
	 * Stores the file in the cache if it doesn't already exist.
	 *
	 * @param file the full path to the file.
	 * @param formatType the type constant defined in {@link Format}.
	 */
	public void storeFileInCache(File file, FormatType formatType) {
		if (
			getConfiguration().getUseCache() &&
			!getDatabase().isDataExists(file.getAbsolutePath(), file.lastModified())
		) {
			try {
				getDatabase().insertOrUpdateData(file.getAbsolutePath(), file.lastModified(), formatType, null);
			} catch (SQLException e) {
				LOGGER.error("Database error while trying to store \"{}\" in the cache: {}", file.getName(), e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}

	/**
	 * Retrieves the {@link net.pms.configuration.PmsConfiguration PmsConfiguration} object
	 * that contains all configured settings for DMS. The object provides getters for all
	 * configurable DMS settings.
	 *
	 * @return The configuration object
	 */
	public static PmsConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Retrieves the composite {@link net.pms.configuration.DeviceConfiguration DeviceConfiguration} object
	 * that applies to this device, which acts as its {@link net.pms.configuration.PmsConfiguration PmsConfiguration}.
	 *
	 * This function should be used to resolve the relevant PmsConfiguration wherever the renderer
	 * is known or can be determined.
	 *
	 * @param  renderer The renderer configuration.
	 * @return          The DeviceConfiguration object, if any, or the global PmsConfiguration.
	 */
	public static PmsConfiguration getConfiguration(RendererConfiguration renderer) {
		return (renderer != null && (renderer instanceof DeviceConfiguration)) ? (DeviceConfiguration) renderer : configuration;
	}

	public static PmsConfiguration getConfiguration(OutputParams params) {
		return getConfiguration(params != null ? params.mediaRenderer : null);
	}

	// Note: this should be used only when no RendererConfiguration or OutputParams is available
	public static PmsConfiguration getConfiguration(DLNAResource dlna) {
		return getConfiguration(dlna != null ? dlna.getDefaultRenderer() : null);
	}

	/**
	 * Sets the {@link net.pms.configuration.PmsConfiguration PmsConfiguration} object
	 * that contains all configured settings for DMS. The object provides getters for all
	 * configurable DMS settings.
	 *
	 * @param conf The configuration object.
	 */
	private static void setConfiguration(PmsConfiguration conf) {
		configuration = conf;
	}

	/**
	 * Sets up a standard test configuration that doesn't open any GUI dialogs
	 * during {@link #init()}.
	 * <p>
	 * XXX This is NOT thread-safe and can lead to random issues when tests are
	 * run in parallel. This isn't because of this method but because of the
	 * fundamental design of having an unprotected static
	 * {@link PmsConfiguration} instance. The problem only manifests during
	 * tests, as the configuration instance is only set once from the main
	 * thread during normal initialization.
	 *
	 * @throws ConfigurationException If a configuration error occurs.
	 */
	public static void setTestConfiguration() throws ConfigurationException {
		if (configuration == null) {
			PmsConfiguration testConfiguration = new PmsConfiguration(false);
			testConfiguration.setLanguage(Locale.ENGLISH);
			testConfiguration.setShowSplashScreen(false);
			testConfiguration.setRunWizard(false);
			configuration = testConfiguration;
		}
	}

	private static final Object PROPERTIES_LOCK = new Object();

	@GuardedBy("PROPERTIES_LOCK")
	private static volatile String name;

	/**
	 * @return The application name.
	 */
	@Nonnull
	public static String getName() {
		if (name == null) {
			synchronized (PROPERTIES_LOCK) {
				if (name == null) {
					name = PropertiesUtil.getProjectProperties().get("project.name");
				}
			}
		}
		return name;
	}

	@GuardedBy("PROPERTIES_LOCK")
	private static volatile String version;

	/**
	 * @return The application version.
	 */
	@Nonnull
	public static String getVersion() {
		if (version == null) {
			synchronized (PROPERTIES_LOCK) {
				if (version == null) {
					version = PropertiesUtil.getProjectProperties().get("project.version");
				}
			}
		}
		return version;
	}

	/**
	 * Returns whether the operating system is 64-bit or 32-bit.
	 *
	 * This will work with Windows and OS X but not necessarily with Linux
	 * because when the OS is not Windows we are using Java's os.arch which
	 * only detects the bitness of Java, not of the operating system.
	 *
	 * @return The bitness of the operating system.
	 *
	 * @deprecated Use {@link SystemInformation#getOSBitness()} instead.
	 */
	@Deprecated
	public static int getOSBitness() {
		return SystemInformation.getOSBitness();
	}

	/**
	 * Log system properties identifying Java, the OS and encoding and log
	 * warnings where appropriate.
	 */
	private static void logSystemInfo() {
		long jvmMemory = Runtime.getRuntime().maxMemory();

		LOGGER.info(
			"Java: {} {} ({}-bit) by {}",
			System.getProperty("java.vm.name"),
			System.getProperty("java.version"),
			System.getProperty("sun.arch.data.model"),
			System.getProperty("java.vendor")
		);
		LOGGER.info(
			"OS: {} {}-bit {}",
			System.getProperty("os.name"),
			SystemInformation.getOSBitness(),
			System.getProperty("os.version")
		);
		LOGGER.info(
			"Maximum JVM Memory: {}",
			jvmMemory == Long.MAX_VALUE ? "Unlimited" : ConversionUtil.formatBytes(jvmMemory, true)
		);
		LOGGER.info("Language: {}", WordUtils.capitalize(PMS.getLocale().getDisplayName(Locale.ENGLISH)));
		LOGGER.info("Encoding: {}", System.getProperty("file.encoding"));
		LOGGER.info("");

		if (Platform.isMac() && BasicSystemUtils.INSTANCE.getOSVersion().isLessThan(10, 6)) {
			// The binaries shipped with the Mac OS X version of DMS are being
			// compiled against specific OS versions, making them incompatible
			// with older versions. Warn the user about this when necessary.
			LOGGER.warn("-----------------------------------------------------------------");
			LOGGER.warn("WARNING!");
			LOGGER.warn("DMS ships with external binaries compiled for Mac OS X 10.6 or");
			LOGGER.warn("higher. You are running an older version of Mac OS X which means");
			LOGGER.warn("that these binaries used for example for transcoding may not work!");
			LOGGER.warn("To solve this, replace the binaries found int the \"osx\"");
			LOGGER.warn("subfolder with versions compiled for your version of OS X.");
			LOGGER.warn("-----------------------------------------------------------------");
			LOGGER.warn("");
		}
	}

	/**
	 * Try to rename old logfile to <filename>.prev
	 */
	private static void renameOldLogFile() {
		String fullLogFileName = configuration.getDefaultLogFilePath();
		String newLogFileName = fullLogFileName + ".prev";

		try {
			File logFile = new File(newLogFileName);
			if (logFile.exists()) {
				if (!logFile.delete()) {
					newLogFileName += ".prev";
				}
			}
			logFile = new File(fullLogFileName);
			if (logFile.exists()) {
				File newFile = new File(newLogFileName);
				if (!logFile.renameTo(newFile)) {
					LOGGER.warn("Could not rename \"{}\" to \"{}\"", fullLogFileName, newLogFileName);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Could not rename \"{}\" to \"{}\": {}", fullLogFileName, newLogFileName, e.getMessage());
			LOGGER.trace("", e);
		}
	}

	private DbgPacker dbgPack;

	public DbgPacker dbgPack() {
		return dbgPack;
	}

	private TempFileMgr tfm;

	public void addTempFile(File f) {
		tfm.add(f);
	}

	public void addTempFile(File f, int cleanTime) {
		tfm.add(f, cleanTime);
	}

	private static final Object HEADLESS_LOCK = new Object();

	@GuardedBy("HEADLESS_LOCK")
	private static Boolean headless;

	/**
	 * Checks if DMS is running in headless (console) mode, since some Linux
	 * distributions seem to not use java.awt.GraphicsEnvironment.isHeadless()
	 * properly.
	 */
	public static boolean isHeadless() {
		synchronized (HEADLESS_LOCK) {
			if (headless != null) {
				return headless.booleanValue();
			}

			try {
				JDialog d = new JDialog();
				d.dispose();
				headless = Boolean.FALSE;
			} catch (NoClassDefFoundError | HeadlessException | InternalError e) {
				headless = Boolean.TRUE;
			}
			return headless.booleanValue();

		}
	}

	/**
	 * Forces DMS to run in headless (console) mode whether a graphical
	 * environment is available or not.
	 */
	public static void forceHeadless() {
		synchronized (HEADLESS_LOCK) {
			headless = Boolean.TRUE;
		}
	}

	private static Locale locale = null;
	private static ReadWriteLock localeLock = new ReentrantReadWriteLock();

	/**
	 * Gets DMS' current {@link Locale} to be used in any {@link Locale}
	 * sensitive operations. If <code>null</code> the default {@link Locale}
	 * is returned.
	 */
	@Nonnull
	public static Locale getLocale() {
		localeLock.readLock().lock();
		try {
			if (locale != null) {
				return locale;
			}
			return Locale.getDefault();
		} finally {
			localeLock.readLock().unlock();
		}
	}

	/**
	 * Sets DMS' {@link Locale}.
	 *
	 * @param aLocale the {@link Locale} to set
	 */
	public static void setLocale(@Nullable Locale aLocale) {
		localeLock.writeLock().lock();
		try {
			locale = aLocale == null ? Locale.getDefault() : aLocale;
			Messages.setLocaleBundle(locale);
		} finally {
			localeLock.writeLock().unlock();
		}
	}

	/**
	 * Sets DMS' {@link Locale} with the same parameters as the
	 * {@link Locale} class constructor. <code>null</code> values are
	 * treated as empty strings.
	 *
	 * @param language An ISO 639 alpha-2 or alpha-3 language code, or a
	 * language subtag up to 8 characters in length. See the
	 * <code>Locale</code> class description about valid language values.
	 * @param country An ISO 3166 alpha-2 country code or a UN M.49
	 * numeric-3 area code. See the <code>Locale</code> class description
	 * about valid country values.
	 * @param variant Any arbitrary value used to indicate a variation of a
	 * <code>Locale</code>. See the <code>Locale</code> class description
	 * for the details.
	 */
	public static void setLocale(String language, String country, String variant) {
		if (country == null) {
			country = "";
		}
		if (variant == null) {
			variant = "";
		}
		localeLock.writeLock().lock();
		try {
			locale = new Locale(language, country, variant);
		} finally {
			localeLock.writeLock().unlock();
		}
	}

	/**
	 * Sets DMS' {@link Locale} with the same parameters as the
	 * {@link Locale} class constructor. <code>null</code> values are
	 * treated as empty strings.
	 *
	 * @param language An ISO 639 alpha-2 or alpha-3 language code, or a
	 * language subtag up to 8 characters in length. See the
	 * <code>Locale</code> class description about valid language values.
	 * @param country An ISO 3166 alpha-2 country code or a UN M.49
	 * numeric-3 area code. See the <code>Locale</code> class description
	 * about valid country values.
	 */
	public static void setLocale(String language, String country) {
		setLocale(language, country, "");
	}

	/**
	 * Sets DMS' {@link Locale} with the same parameters as the {@link Locale}
	 * class constructor. <code>null</code> values are
	 * treated as empty strings.
	 *
	 * @param language An ISO 639 alpha-2 or alpha-3 language code, or a
	 * language subtag up to 8 characters in length. See the
	 * <code>Locale</code> class description about valid language values.
	 */
	public static void setLocale(String language) {
		setLocale(language, "", "");
	}

	private RemoteWeb web;

	@Nullable
	public RemoteWeb getWebInterface() {
		return web;
	}

	/**
	 * Sets the relative URL of a context sensitive help page located in the
	 * documentation directory.
	 *
	 * @param page The help page.
	 */
	public static void setHelpPage(String page) {
		helpPage = page;
	}

	/**
	 * Returns the relative URL of a context sensitive help page in the
	 * documentation directory.
	 *
	 * @return The help page.
	 */
	public static String getHelpPage() {
		return helpPage;
	}

	public static boolean isReady() {
		return instance == null ? false : instance.ready;
	}

	@Nullable
	public static GlobalIdRepo getGlobalRepo() {
		return instance == null ? null : instance.globalRepo;
	}

	private InfoDb infoDb;
	private CodeDb codes;
	private CodeEnter masterCode;

	public void infoDbAdd(File f, String formattedName) {
		infoDb.backgroundAdd(f, formattedName);
	}

	public InfoDb infoDb() {
		return infoDb;
	}

	public CodeDb codeDb() {
		return codes;
	}

	public void setMasterCode(CodeEnter ce) {
		masterCode = ce;
	}

	public boolean masterCodeValid() {
		return (masterCode != null && masterCode.validCode(null));
	}

	public static FileWatcher getFileWatcher() {
		return fileWatcher;
	}

	public static class DynamicPlaylist extends Playlist {
		private long start;
		private String savePath;

		public DynamicPlaylist(String name, String dir, int mode) {
			super(name, null, 0, mode);
			savePath = dir;
			start = 0;
		}

		@Override
		public void clear() {
			super.clear();
			start = 0;
		}

		@Override
		public void save() {
			if (start == 0) {
				start = System.currentTimeMillis();
			}
			Date d = new Date(start);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm", Locale.US);
			list.save(new File(savePath, "dynamic_" + sdf.format(d) + ".ups"));
		}
	}

	private DynamicPlaylist dynamicPls;

	public Playlist getDynamicPls() {
		if (dynamicPls == null) {
			dynamicPls = new DynamicPlaylist(Messages.getString("PMS.146"),
				configuration.getDynamicPlsSavePath(),
				(configuration.isDynamicPlsAutoSave() ? Playlist.AUTOSAVE : 0) | Playlist.PERMANENT);
		}
		return dynamicPls;
	}

	private void launchJmDNSRenderers() {
		if (configuration.useChromecastExt()) {
			if (RendererConfiguration.getRendererConfigurationByName("Chromecast") != null) {
				try {
					startjmDNS();
					new ChromecastMgr(jmDNS);
				} catch (Exception e) {
					LOGGER.debug("Can't create chromecast mgr");
				}
			}
			else {
				LOGGER.info("No Chromecast renderer found. Please enable one and restart.");
			}
		}
	}

	private void startjmDNS() throws IOException{
		if (jmDNS == null) {
			jmDNS = JmDNS.create();
		}
	}

	private static int traceMode = 0;
	private static boolean logDB;

	/**
	 * Returns current trace mode state
	 *
	 * @return
	 *			0 = Not started in trace mode<br>
	 *			1 = Started in trace mode<br>
	 *			2 = Forced to trace mode
	 */
	public static int getTraceMode() {
		return traceMode;
	}

	/**
	 * Returns if the database logging is forced by command line arguments.
	 *
	 * @return {@code true} if database logging is forced, {@code false}
	 *         otherwise.
	 */
	public static boolean getLogDB() {
		return logDB;
	}

	private CredMgr credMgr;

	public static CredMgr.Cred getCred(String owner) {
		return instance.credMgr.getCred(owner);
	}

	public static CredMgr.Cred getCred(String owner, String tag) {
		return instance.credMgr.getCred(owner, tag);
	}

	public static String getCredTag(String owner, String username) {
		return instance.credMgr.getTag(owner, username);
	}

	public static boolean verifyCred(String owner, String tag, String user, String pwd) {
		return instance.credMgr.verify(owner, tag, user, pwd);
	}

	private DmsKeysDb keysDb;

	public static String getKey(String key) {
		 return instance.keysDb.get(key);
	}

	public static void setKey(String key, String val) {
		instance.keysDb.set(key, val);
	}

	/**
	 * Configures JNA according to the environment. This must be called before
	 * JNA is first initialized to have any effect.
	 */
	public static void configureJNA() {
		String osName = System.getProperty("os.name");
		if (osName == null) {
			osName = "";
		}
		boolean windows = osName.startsWith("Windows");
		double windowsVersion = Double.NaN;
		try {
			if (
				windows &&
				isNotBlank(System.getProperty("os.version"))
			) {
				windowsVersion = Double.parseDouble(System.getProperty("os.version"));
			}
		} catch (NumberFormatException e) {
			System.err.println(
				"Could not determine Windows version from " +
				System.getProperty("os.version") +
				". Not applying Windows XP hack: " + e.getMessage()
			);
		}
		boolean macos = osName.startsWith("Mac") || osName.startsWith("Darwin");

		// Set JNA "jnidispatch" resolution rules
		if (
			windows && !Double.isNaN(windowsVersion) && windowsVersion < 5.2
		) {
			String developmentPath = "src\\main\\external-resources\\lib\\winxp";
			if (new File(developmentPath).exists()) {
				System.setProperty("jna.boot.library.path", developmentPath);
			} else {
				System.setProperty("jna.boot.library.path", "win32\\winxp");
			}
		} else {
			System.setProperty("jna.nosys", "true");
		}

		// Set JNA library path
		ArrayList<String> libraryPaths = new ArrayList<>();
		if (windows) {
			Path path = Paths.get("target/bin/win32");
			if (Files.exists(path)) {
				libraryPaths.add(path.toAbsolutePath().toString());
			}
			libraryPaths.add("win32");
		} else if (macos) {
			Path path = Paths.get("target/bin/osx");
			if (Files.exists(path)) {
				libraryPaths.add(path.toAbsolutePath().toString());
			}
		}

		if (!libraryPaths.isEmpty()) {
			System.setProperty("jna.library.path", StringUtils.join(libraryPaths, File.pathSeparator));
		}
	}

	/**
	 * This {@code enum} represents the startup options parsed from the command
	 * line arguments, system variables or environment variables.
	 */
	public static enum Option {

		/** Backup and downgrade the database if incompatible */
		DB_BACKUP_DOWNGRADE,

		/** Downgrade the database if incompatible */
		DB_DOWNGRADE,

		/** Enable database logging */
		DB_LOG,

		/** Rename the database if incompatible */
		DB_RENAME,

		/** Force headless mode */
		HEADLESS,

		/** Display help and exit */
		HELP,

		/** Never use headless mode */
		NOCONSOLE,

		/** Specifies a profile to use */
		PROFILE,

		/** Always show horizontal and vertical scrollbars in the GUI */
		SCROLLBARS,

		/** Display the select profile dialog during startup */
		SELECT_PROFILE,

		/** Enable forced {@code TRACE} logging level */
		TRACE,

		/** Display the version and exit */
		VERSION
	}
}
