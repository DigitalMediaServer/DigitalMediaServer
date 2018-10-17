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
package net.pms.service;

import java.nio.charset.Charset;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.Platform;
import net.pms.configuration.PmsConfiguration;
import net.pms.database.TableManager;
import net.pms.io.WindowsSystemUtils;

/**
 * This class creates and destroys global services ands offers a static way of
 * referencing them.
 * <p>
 * It is <i>not</i> thread-safe: The services must be created before any threads
 * use the services and destroyed after other threads have terminated. This is
 * to avoid the cost of synchronization each time a service reference is needed.
 * <p>
 * <b>Note:</b> This class holds instance references that relies on
 * {@link PmsConfiguration} being initialized. This is therefore not suitable to
 * hold instance references that must exist when {@link PmsConfiguration} itself
 * is initialized.
 *
 * @author Nadahar
 */
@NotThreadSafe
public class Services {

	private static final Logger LOGGER = LoggerFactory.getLogger(Services.class);

	/**
	 * The default {@link Charset} for Windows consoles or {@code null} for
	 * other platforms
	 */
	@Nullable
	public static final Charset WINDOWS_CONSOLE;

	private static Services instance;

	static {
		if (Platform.isWindows()) {
			Charset windowsConsole = null;
			try {
				windowsConsole = Charset.forName("cp" + WindowsSystemUtils.getOEMCP());
			} catch (Exception e) {
				windowsConsole = Charset.defaultCharset();
			}
			WINDOWS_CONSOLE = windowsConsole;
		} else {
			WINDOWS_CONSOLE = null;
		}
	}

	private ProcessManager processManager;

	private SleepManager sleepManager;

	private TableManager tableManager;

	/**
	 * Returns current static {@link Services} instance. Isn't normally needed,
	 * use the static method for the individual service instead.
	 *
	 * @return The current static {@link Services} instance.
	 */
	@Nullable
	public static Services get() {
		return instance;
	}

	/**
	 * Creates a new static {@link Services} instance and starts the services.
	 *
	 * @throws IllegalStateException If the services have already been created.
	 */
	public static void create() {
		if (instance != null) {
			throw new IllegalStateException("Services have already been created");
		}
		instance = new Services();
	}

	/**
	 * Stops the services and nulls the static {@link Services} instance.
	 */
	public static void destroy() {
		if (instance != null) {
			instance.stop();
			instance = null;
		}
	}

	/**
	 * @return The {@link ProcessManager} instance.
	 */
	@Nullable
	public static ProcessManager processManager() {
		return instance == null ? null : instance.getProcessManager();
	}

	/**
	 * @return The {@link SleepManager} instance.
	 */
	@Nullable
	public static SleepManager sleepManager() {
		return instance == null ? null : instance.getSleepManager();
	}

	/**
	 * @return The {@link TableManager} instance.
	 */
	@Nullable
	public static TableManager tableManager() {
		return instance == null ? null : instance.getTableManager();
	}

	/**
	 * Sets the static {@link Services} instance. Isn't normally needed, use
	 * {@link Services#create()} instead.
	 *
	 * @param newInstance the {@link Services} instance to set.
	 */
	public static void set(Services newInstance) {
		instance = newInstance;
	}

	/**
	 * Creates a new instance and starts the services. Isn't normally needed,
	 * use {@link Services#create()} instead.
	 */
	public Services() {
		start();
	}

	/**
	 * Creates and starts the services. Isn't normally needed, use
	 * {@link Services#create()} instead.
	 *
	 * @throws IllegalStateException If the services have already been started.
	 */
	public void start() {
		if (processManager != null || sleepManager != null) {
			throw new IllegalStateException("Services have already been started");
		}
		LOGGER.debug("Starting services");

		processManager = new ProcessManager();
		sleepManager = new SleepManager();
	}

	/**
	 * Creates a {@link TableManager} instance. {@link TableManager} is
	 * <b>not</b> instantiated by {@link #start()} to allow recreation of the
	 * {@link Service}.
	 * <p>
	 * <b>This method is NOT thread-safe and must only be called during startup
	 * from a single thread before the database has been taken in use.</b>
	 *
	 * @throws IllegalStateException If the {@link TableManager} already exists.
	 */
	public void createTableManager() {
		if (tableManager != null) {
			throw new IllegalArgumentException("A TableManager service already exists");
		}
		tableManager = new TableManager(null);
	}

	/**
	 * Creates a {@link TableManager} instance. {@link TableManager} is
	 * <b>not</b> instantiated by {@link #start()} to allow recreation of the
	 * {@link Service} for different database names.
	 * <p>
	 * <b>This method is NOT thread-safe and must only be called during startup
	 * from a single thread before the database has been taken in use.</b>
	 *
	 * @param databaseName the database name to use or {@code null} to use the
	 *            default.
	 * @throws IllegalStateException If the {@link TableManager} already exists.
	 */
	public void createTableManager(@Nullable String databaseName) {
		if (tableManager != null) {
			throw new IllegalStateException("TableManager service already exists");
		}
		tableManager = new TableManager(databaseName);
	}

	/**
	 * Stops and destroys the {@link TableManager} instance.
	 * <p>
	 * <b>This method is NOT thread-safe and must only be called during startup
	 * from a single thread before the database has been taken in use.</b>
	 *
	 * @throws IllegalStateException If the {@link TableManager} doesn't exist.
	 */
	public void destroyTableManager() {
		if (tableManager == null) {
			throw new IllegalStateException("TableManager doesn't exist");
		}
		tableManager.stop();
		tableManager = null;
	}

	/**
	 * Stops and releases the current services.
	 */
	public void stop() {
		LOGGER.debug("Stopping services");

		if (processManager != null) {
			processManager.stop();
			processManager = null;
		}

		if (sleepManager != null) {
			sleepManager.stop();
			sleepManager = null;
		}

		if (tableManager != null) {
			tableManager.stop();
		}
	}

	/**
	 * Isn't normally needed, use {@link Services#processManager()} instead.
	 *
	 * @return The {@link ProcessManager} instance.
	 */
	@Nullable
	public ProcessManager getProcessManager() {
		return processManager;
	}

	/**
	 * Isn't normally needed, use {@link Services#sleepManager()} instead.
	 *
	 * @return The {@link SleepManager} instance.
	 */
	@Nullable
	public SleepManager getSleepManager() {
		return sleepManager;
	}

	/**
	 * Isn't normally needed, use {@link Services#tableManager()} instead.
	 *
	 * @return The {@link TableManager} instance.
	 */
	@Nullable
	public TableManager getTableManager() {
		return tableManager;
	}
}
