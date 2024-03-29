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
package net.pms.encoders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JComponent;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.ConfigurableProgramPaths;
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.ExternalProgramInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.ProgramExecutableType;
import net.pms.configuration.ProgramExecutableType.DefaultExecutableType;
import net.pms.configuration.RendererConfiguration;
import net.pms.configuration.ExecutableInfo.ExecutableInfoBuilder;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.FormatType;
import net.pms.io.BasicSystemUtils;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.util.FilePermissions;
import net.pms.util.FileUtil;
import net.pms.util.Iso639;
import net.pms.util.OpenSubtitle;
import net.pms.util.UMSUtils;
import net.pms.util.Version;
import net.pms.util.FilePermissions.FileFlag;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.Platform;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * The base class for all transcoding engines.
 */
@ThreadSafe
public abstract class Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

	public static final int VIDEO_SIMPLEFILE_PLAYER = 0;
	public static final int AUDIO_SIMPLEFILE_PLAYER = 1;
	public static final int VIDEO_WEBSTREAM_PLAYER = 2;
	public static final int AUDIO_WEBSTREAM_PLAYER = 3;
	public static final int MISC_PLAYER = 4;

	/** The final {@link ExternalProgramInfo} instance set in the constructor */
	@Nonnull
	protected final ExternalProgramInfo programInfo;

	public abstract int purpose();

	/**
	 * @return The configuration panel for this {@link Player} or {@code null}.
	 */
	@Nullable
	public abstract JComponent getConfigurationPanel();

	/**
	 * @return The {@link PlayerId} for this {@link Player}.
	 */
	@Nonnull
	public abstract PlayerId id();

	/**
	 * @return The {@link Configuration} key for this {@link Player}'s custom
	 *         executable path.
	 */
	public abstract String getConfigurablePathKey();
	public abstract String name();
	public abstract FormatType type();

	/**
	 * @return The {@link Configuration} key under which to store this
	 *         {@link Player}'s {@link ProgramExecutableType}.
	 */
	public abstract String getExecutableTypeKey();

	/**
	 * Must be used to control all access to {@link #specificErrors}
	 */
	protected final ReentrantReadWriteLock specificErrorsLock = new ReentrantReadWriteLock();

	/**
	 * The <i>current</i> {@link ProgramExecutableType} is the one that is
	 * actually being used at the time. It isn't necessarily the same as the
	 * <i>configured</i> {@link ProgramExecutableType} that can be read with
	 * {@link PmsConfiguration#getConfiguredExecutableType}. The
	 * <i>configured</i> {@link ProgramExecutableType} is controlled by the
	 * user. The {@code currentExecutableType} is initialized in
	 * {@link PlayerFactory#registerPlayer(Player)} and starts out being equal
	 * to the user configured setting (or the default is nothing is configured),
	 * but might be changed if the {@link Player} tests determine that the
	 * <i>configured</i> {@link ProgramExecutableType} is unavailable while one
	 * of the other {@link ProgramExecutableType}s are available.
	 * <p>
	 * DMS will seek to find a working executable for any {@link Player}; the
	 * result of which is kept in {@code currentExecutableType}.
	 */
	protected volatile ProgramExecutableType currentExecutableType;

	/**
	 * This is used to store executable test failures that only applies to this
	 * {@link Player}, i.e {@link ExecutableErrorType#SPECIFIC}. A general test
	 * failure {@link ExecutableErrorType#GENERAL} is for example if the
	 * executable isn't found, is corrupt or is incompatible with the system.
	 * {@link ExecutableErrorType#SPECIFIC} test failures are those that would
	 * still allow the executable to be used for other purposes, but which makes
	 * it unsuitable for a particular use. That could be things like failing
	 * version criteria or lacking support for specified codecs or formats.
	 * <p>
	 * The logic is that {@link ExecutableErrorType#GENERAL} failures are stored
	 * with the corresponding {@link ExternalProgramInfo} itself (as it applies
	 * to all usage of that executable), while
	 * {@link ExecutableErrorType#SPECIFIC} failures are stored in this
	 * {@link Player} local {@link Map}.
	 * <p>
	 * The stored failures are per {@link ProgramExecutableType}, and the error
	 * string is a localized error message.
	 * <p>
	 * All access must be guarded with {@link #specificErrorsLock}, and
	 * {@link #specificErrorsLock} must always be locked after
	 * {@link ExternalProgramInfo#getLock()} if both need locking in the same
	 * block.
	 */
	@GuardedBy("specificErrorsLock")
	protected final HashMap<ProgramExecutableType, String> specificErrors = new HashMap<>();

	/**
	 * Must be used to control all access to {@link #enabled}
	 */
	protected final ReentrantReadWriteLock enabledLock = new ReentrantReadWriteLock();

	/**
	 * Used to store if this {@link Player} is enabled in the configuration. All
	 * access must be guarded with {@link #enabledLock}.
	 */
	@GuardedBy("enabledLock")
	protected boolean enabled = false;


	/**
	 * Abstract constructor that sets the final {@code programInfo} variable.
	 */
	public Player() {
		programInfo = programInfo();
		if (programInfo == null) {
			throw new IllegalStateException(
				"Can't instantiate " + this.getClass().getSimpleName() + "because executables() returns null"
			);
		}
	}

	/**
	 * Gets the <i>current</i> {@link ProgramExecutableType} for this
	 * {@link Player}. For an explanation of the concept, see
	 * {@link #currentExecutableType}.
	 *
	 * @return The current {@link ProgramExecutableType}
	 */
	@Nullable
	public ProgramExecutableType getCurrentExecutableType() {
		return currentExecutableType;
	}

	/**
	 * Determines and sets the current {@link ProgramExecutableType} for this
	 * {@link Player}. The determination starts out with the configured
	 * {@link ProgramExecutableType}.
	 * <p>
	 * For an explanation of the concept, see {@link #currentExecutableType}.
	 */
	public void determineCurrentExecutableType() {
		determineCurrentExecutableType(configuration.getPlayerExecutableType(this));
	}

	/**
	 * Determines and sets the current {@link ProgramExecutableType} for this
	 * {@link Player}. The determination starts out with the specified
	 * {@link ProgramExecutableType}.
	 * <p>
	 * For an explanation of the concept, see {@link #currentExecutableType}.
	 *
	 * @param newExecutableType the preferred
	 *            {@link ProgramExecutableType}.
	 */
	public void determineCurrentExecutableType(@Nullable ProgramExecutableType newExecutableType) {
		// Find the best executable type to use, first try the configured type
		if (!isAvailable(newExecutableType)) {
			// Set the platform default if that's available
			ProgramExecutableType tmpExecutableType = programInfo.getDefault();
			if (isAvailable(tmpExecutableType)) {
				newExecutableType = tmpExecutableType;
			} else {
				// Set the first one that is available, if any
				for (ProgramExecutableType executableType : programInfo.getExecutableTypes()) {
					if (isAvailable(executableType)) {
						newExecutableType = executableType;
						break;
					}
				}
			}
			// Leave it to the configured type if no other is available
		}

		// If null, just pick one that exists if possible.
		if (newExecutableType == null) {
			if (currentExecutableType != null) {
				return;
			}
			for (ProgramExecutableType executableType : programInfo.getExecutableTypes()) {
				if (executableType != null) {
					newExecutableType = executableType;
					break;
				}
			}
		}
		if (currentExecutableType != newExecutableType) {
			currentExecutableType = newExecutableType;
			currentExecutableTypeUpdated();
		}
	}

	/**
	 * This should be called whenever {@code currentExecutableType} is changed,
	 * to alert implementations of the change. Implementations that want to be
	 * alerted should override this method.
	 *
	 * Implementations of this method should be light and defer to other threads
	 * for length operations.
	 */
	public void currentExecutableTypeUpdated() {
	}

	// FIXME this is an implementation detail (and not a very good one).
	// it's entirely up to engines how they construct their command lines.
	// need to get rid of this
	public abstract String[] args();

	public abstract String mimeType();

	/**
	 * Used to retrieve the {@link ExternalProgramInfo} for the {@link Player}
	 * during construction.
	 *
	 * @return The platform and configuration dependent {@link ExecutableInfo}
	 *         for this {@link Player}.
	 */
	@Nullable
	protected abstract ExternalProgramInfo programInfo();

	/**
	 * @return The {@link ExternalProgramInfo} instance.
	 */
	@Nonnull
	public ExternalProgramInfo getProgramInfo() {
		return programInfo;
	}

	/**
	 * @return The path to the currently configured
	 *         {@link ProgramExecutableType} for this {@link Player} or
	 *         {@code null} if undefined.
	 */
	@Nullable
	public String getExecutable() {
		Path executable = getProgramInfo().getPath(currentExecutableType);
		return executable == null ? null : executable.toString();
	}

	/**
	 * @return The {@link ExecutableInfo} for the currently configured
	 *         {@link ProgramExecutableType} for this {@link Player} or
	 *         {@code null}.
	 */
	@Nullable
	public ExecutableInfo getExecutableInfo() {
		return getProgramInfo().getExecutableInfo(currentExecutableType);
	}

	/**
	 * @return The {@link Version} for the currently configured
	 *         {@link ProgramExecutableType} for this {@link Player} or
	 *         {@code null}.
	 */
	@Nullable
	public Version getVersion() {
		ExecutableInfo executableInfo = getProgramInfo().getExecutableInfo(currentExecutableType);
		return executableInfo == null ? null : executableInfo.getVersion();
	}

	protected static final PmsConfiguration _configuration = PMS.getConfiguration();
	protected PmsConfiguration configuration = _configuration;

	public boolean avisynth() {
		return false;
	}

	public abstract boolean excludeFormat(Format extension);

	public abstract boolean isPlayerCompatible(RendererConfiguration renderer);

	public boolean isInternalSubtitlesSupported() {
		return true;
	}

	public boolean isExternalSubtitlesSupported() {
		return true;
	}

	public boolean isTimeSeekable() {
		return false;
	}

	/**
	 * Checks whether this {@link Player} can be used, e.g if the binary is
	 * accessible for the current {@link ProgramExecutableType}.
	 *
	 * @return {@code true} if this {@link Player} is available, {@code false}
	 *         otherwise.
	 */
	public boolean isAvailable() {
		return isAvailable(currentExecutableType);
	}

	/**
	 * Checks whether this {@link Player} can be used, e.g if the binary is
	 * accessible for the specified {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *            text for.
	 * @return {@code true} if this {@link Player} is available, {@code false}
	 *         otherwise.
	 */
	public boolean isAvailable(@Nullable ProgramExecutableType executableType) {
		if (executableType == null) {
			return false;
		}
		ExecutableInfo executableInfo = programInfo.getExecutableInfo(executableType);
		if (executableInfo == null) {
			return false;
		}
		Boolean result = programInfo.getExecutableInfo(executableType).getAvailable();
		if (result == null || !result.booleanValue()) {
			return false;
		}
		specificErrorsLock.readLock().lock();
		try {
			String specificError = specificErrors.get(executableType);
			return specificError == null;
		} finally {
			specificErrorsLock.readLock().unlock();
		}
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the given {@link ProgramExecutableType}. If there is an error, a
	 * generic text is returned.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *            text for.
	 * @return The localized status text.
	 */
	public String getStatusText(ProgramExecutableType executableType) {
		return getStatusText(executableType, false);
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the given {@link ProgramExecutableType}. If there is an error,
	 * the full error text is returned.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *            text for.
	 * @return The localized status text.
	 */
	public String getStatusTextFull(ProgramExecutableType executableType) {
		return getStatusText(executableType, true);
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the given {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *            text for.
	 * @param fullText if {@code true} the full error text is returned in case
	 *            of an error, if {@code false} only a generic text is returned
	 *            in case of an error.
	 * @return The localized status text.
	 */
	public String getStatusText(ProgramExecutableType executableType, boolean fullText) {
		if (executableType == null) {
			return null;
		}
		ExecutableInfo executableInfo = programInfo.getExecutableInfo(executableType);
		if (executableInfo == null) {
			return String.format(Messages.getString("Engine.Undefined"), name());
		}
		if (executableInfo.getAvailable() == null || executableInfo.getAvailable().booleanValue()) {
			// Generally available or unknown, check for Player specific failures
			specificErrorsLock.readLock().lock();
			try {
				String specificError = specificErrors.get(executableType);
				if (specificError != null) {
					return fullText ? specificError : String.format(Messages.getString("Engine.ErrorShort"), name());
				}
			} finally {
				specificErrorsLock.readLock().unlock();
			}
			if (executableInfo.getAvailable() == null) {
				return String.format(Messages.getString("Engine.UnknownStatus"), name());
			}
		}
		if (executableInfo.getAvailable().booleanValue()) {
			if (isEnabled()) {
				if (executableInfo.getVersion() != null) {
					return String.format(Messages.getString("Engine.EnabledVersion"), name(), executableInfo.getVersion());
				}
				return String.format(Messages.getString("Engine.Enabled"), name());
			}
			return String.format(Messages.getString("Engine.Disabled"), name());
		}
		if (executableInfo.getErrorText() == null) {
			return Messages.getString("General.3");
		}
		return fullText ? executableInfo.getErrorText() : String.format(Messages.getString("Engine.ErrorShort"), name());
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the current {@link ProgramExecutableType}. If there is an error,
	 * a generic text is returned.
	 *
	 * @return The localized status text.
	 */
	public String getStatusText() {
		return getStatusText(currentExecutableType, false);
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the current {@link ProgramExecutableType}. If there is an error,
	 * the full error text is returned.
	 *
	 * @return The localized status text.
	 */
	public String getStatusTextFull() {
		return getStatusText(currentExecutableType, true);
	}

	/**
	 * Marks the engine as available for use.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *            availability.
	 * @param version the {@link Version} string for the executable, or
	 *            {@code null} if the version is unknown.
	 */
	public void setAvailable(@Nonnull ProgramExecutableType executableType, @Nullable Version version) {
		setAvailable(true, executableType, version, null, null);
	}

	/**
	 * Marks the engine as unavailable.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *            availability.
	 * @param errorType the {@link ExecutableErrorType}.
	 * @param errorText the localized error description.
	 */
	public void setUnavailable(
		@Nonnull ProgramExecutableType executableType,
		@Nonnull ExecutableErrorType errorType,
		@Nullable String errorText
	) {
		setAvailable(false, executableType, null, errorType, errorText);
	}

	/**
	 * Marks the engine as unavailable.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *            availability.
	 * @param version the {@link Version} of the executable if known or
	 *            {@code null} if unknown.
	 * @param errorType the {@link ExecutableErrorType}.
	 * @param errorText the localized error description.
	 */
	public void setUnavailable(
		@Nonnull ProgramExecutableType executableType,
		@Nullable Version version,
		@Nonnull ExecutableErrorType errorType,
		@Nonnull String errorText
	) {
		setAvailable(false, executableType, version, errorType, errorText);
	}

	/**
	 * Sets the engine available status and a related error text.
	 *
	 * @param available whether or not the {@link Player} is available.
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *            availability.
	 * @param version the {@link Version} of the executable if known or
	 *            {@code null} if unknown.
	 * @param errorType the {@link ExecutableErrorType} if {@code available} is
	 *            {@code false}. Can be {@code null} if {@code available} is
	 *            {@code true}.
	 * @param errorText a localized description of the current error if
	 *            {@code available} is {@code false}, or {@code null} if the
	 *            executable is available.
	 */
	public void setAvailable(
		boolean available,
		@Nonnull ProgramExecutableType executableType,
		@Nullable Version version,
		@Nullable ExecutableErrorType errorType,
		@Nullable String errorText
	) {
		if (executableType == null) {
			throw new IllegalArgumentException("executableType cannot be null or unknown");
		}
		if (!available && (errorType == null || errorText == null)) {
			throw new IllegalArgumentException("errorType and errorText can only be null if available is true");
		}
		if (errorType == ExecutableErrorType.SPECIFIC) {
			/*
			 * Although most probably the case, we can't assume that a Player
			 * specific error means that the executable is generally available.
			 * Thus, only set the local specific error and not the global
			 * availability for this executable. If it's used by another player
			 * it will be tested again.
			 */
			specificErrorsLock.writeLock().lock();
			try {
				specificErrors.put(executableType, errorText);
			} finally {
				specificErrorsLock.writeLock().unlock();
			}
		} else {
			// Set the global general status
			ExecutableInfo executableInfo = programInfo.getExecutableInfo(executableType);
			if (executableInfo == null) {
				throw new IllegalStateException(
					"Cannot set availability for " + executableType + " " + name() + " because it is undefined"
				);
			}
			ExecutableInfoBuilder builder = executableInfo.modify();
			builder.available(Boolean.valueOf(available));
			if (version != null) {
				builder.version(version);
			}
			if (errorType != null || errorText != null) {
				builder.errorType(errorType).errorText(errorText);
			}
			programInfo.setExecutableInfo(executableType, builder.build());
		}
	}

	/**
	 * Sets the custom executable {@link Path} and the default
	 * {@link ProgramExecutableType} type, but won't run tests or perform other
	 * tasks normally needed after such a change.
	 * <p>
	 * <b>This should normally only be called from
	 * {@link PlayerFactory#registerPlayer(Player)}</b> to set the configured
	 * {@link Path} before other registration tasks are performed.
	 *
	 * @param customPath The custom executable {@link Path}.
	 */
	public void initCustomExecutablePath(@Nullable Path customPath) {
		customPath = ConfigurableProgramPaths.resolveCustomProgramPath(customPath);
		programInfo.setPath(ProgramExecutableType.CUSTOM, customPath);
		if (customPath == null) {
			programInfo.setOriginalDefault();
		} else {
			programInfo.setDefault(ProgramExecutableType.CUSTOM);
			LOGGER.debug("Custom executable path for {} was initialized to \"{}\"", programInfo.getName(), customPath);
		}
	}

	/**
	 * Sets or clears the {@link ProgramExecutableType#CUSTOM} executable
	 * {@link Path} for the underlying {@link ExternalProgramInfo}. This will
	 * impact all players sharing the same {@link ExternalProgramInfo}.
	 * <p>
	 * A changed {@link Path} will result in a rerun of tests and a reevaluation
	 * of the current {@link ExecutableInfo} for all affected {@link Player}s.
	 * As this is a costly operations, no changes will be made if the specified
	 * {@link Path} is equal to the existing {@link Path} or if both are
	 * {@code null}.
	 *
	 * @param customPath the new custom {@link Path} or {@code null} to clear.
	 * @param setConfiguration whether or not the {@link Path} should also be
	 *            stored in {@link PmsConfiguration}.
	 * @return {@code true} if any changes were made as a result of this call,
	 *         {@code false} otherwise.
	 */
	public boolean setCustomExecutablePath(@Nullable Path customPath, boolean setConfiguration) {
		boolean configurationChanged = false;
		if (setConfiguration) {
			try {
				configurationChanged = configuration.setPlayerCustomPath(this, customPath);
			} catch (IllegalStateException e) {
				configurationChanged = false;
				LOGGER.warn("Failed to set custom executable path for {}: {}", name(), e.getMessage());
				LOGGER.trace("", e);
			}
		}

		customPath = ConfigurableProgramPaths.resolveCustomProgramPath(customPath);
		boolean changed = programInfo.setPath(ProgramExecutableType.CUSTOM, customPath);
		if (changed) {
			DefaultExecutableType defaultType;
			if (customPath == null) {
				defaultType = DefaultExecutableType.ORIGINAL;
				if (setConfiguration && LOGGER.isDebugEnabled()) {
					LOGGER.debug("Custom executable path for {} was cleared", programInfo.getName());
				}
			} else {
				defaultType = DefaultExecutableType.CUSTOM;
				if (setConfiguration && LOGGER.isDebugEnabled()) {
					LOGGER.debug("Custom executable path for {} was set to \"{}\"", programInfo.getName(), customPath);
				}
			}
			PlayerFactory.reEvaluateExecutable(this, ProgramExecutableType.CUSTOM, defaultType);
		}
		return changed || configurationChanged;
	}

	/**
	 * Clears any registered {@link ExecutableErrorType#SPECIFIC} for the
	 * specified {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to
	 *            clear registered {@link ExecutableErrorType#SPECIFIC} errors.
	 */
	public void clearSpecificErrors(@Nullable ProgramExecutableType executableType) {
		if (executableType == null && !isSpecificTest()) {
			return;
		}
		specificErrorsLock.writeLock().lock();
		try {
			specificErrors.remove(executableType);
		} finally {
			specificErrorsLock.writeLock().unlock();
		}
	}

	/**
	 * Used to determine if this {@link Player} is enabled in the configuration.
	 *
	 * @return {@code true} if this is enabled, {@code false} otherwise.
	 */
	public boolean isEnabled() {
		enabledLock.readLock().lock();
		try {
			return enabled;
		} finally {
			enabledLock.readLock().unlock();
		}
	}

	/**
	 * Sets the enabled status for this {@link Player}.
	 *
	 * @param enabled {@code true} if this {@link Player} is enabled,
	 *            {@code false} otherwise.
	 * @param setConfiguration whether or not the enabled status should also be
	 *            stored in the current {@link PmsConfiguration}.
	 */
	public void setEnabled(boolean enabled, boolean setConfiguration) {
		enabledLock.writeLock().lock();
		try {
			this.enabled = enabled;
			if (setConfiguration) {
				_configuration.setEngineEnabled(id(), enabled);
			}
		} finally {
			enabledLock.writeLock().unlock();
		}
	}

	/**
	 * Toggles the enabled status for this {@link Player}.
	 *
	 * @param setConfiguration whether or not the enabled status should also be
	 *            stored in the current {@link PmsConfiguration}.
	 */
	public void toggleEnabled(boolean setConfiguration) {
		enabledLock.writeLock().lock();
		try {
			enabled = !enabled;
			if (setConfiguration) {
				_configuration.setEngineEnabled(id(), enabled);
			}
		} finally {
			enabledLock.writeLock().unlock();
		}
	}

	/**
	 * Convenience method to check if this {@link Player} is both available and
	 * enabled for the specified {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to
	 *            check availability.
	 * @return {@code true} if this {@link Player} is both available and
	 *         enabled, {@code false} otherwise.
	 *
	 */
	public boolean isActive(ProgramExecutableType executableType) {
		return isAvailable(executableType) && isEnabled();
	}

	/**
	 * Convenience method to check if this {@link Player} is both available and
	 * enabled for the current {@link ProgramExecutableType}.
	 *
	 * @return {@code true} if this {@link Player} is both available and
	 *         enabled, {@code false} otherwise.
	 */
	public boolean isActive() {
		return isAvailable(currentExecutableType) && isEnabled();
	}

	public abstract ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException;

	@Override
	public String toString() {
		return name();
	}

	/**
	 * This method populates the supplied {@link OutputParams} object with the
	 * correct audio track (aid) and subtitles (sid), based on the given
	 * filename, its MediaInfo metadata and DMS configuration settings.
	 *
	 * @param fileName The file name used to determine the availability of
	 *            subtitles.
	 * @param media The MediaInfo metadata for the file.
	 * @param params The parameters to populate.
	 */
	public static void setAudioAndSubs(String fileName, DLNAMediaInfo media, OutputParams params) {
		setAudioOutputParameters(media, params);
		setSubtitleOutputParameters(fileName, media, params);
	}

	/**
	 * This method populates the supplied {@link OutputParams} object with the
	 * correct audio track (aid) based on the MediaInfo metadata and DMS
	 * configuration settings.
	 *
	 * @param media The MediaInfo metadata for the file.
	 * @param params The parameters to populate.
	 */
	public static void setAudioOutputParameters(DLNAMediaInfo media, OutputParams params) {
		// Use device-specific DMS conf
		PmsConfiguration configuration = PMS.getConfiguration(params);
		if (params.aid == null && media != null && media.getFirstAudioTrack() != null) {
			// check for preferred audio
			DLNAMediaAudio dtsTrack = null;
			StringTokenizer st = new StringTokenizer(configuration.getAudioLanguages(), ",");
			while (st.hasMoreTokens()) {
				String lang = st.nextToken().trim();
				LOGGER.trace("Looking for an audio track with lang: " + lang);
				for (DLNAMediaAudio audio : media.getAudioTracksList()) {
					if (audio.matchCode(lang)) {
						params.aid = audio;
						LOGGER.trace("Matched audio track: " + audio);
						return;
					}

					if (dtsTrack == null && audio.isDTS()) {
						dtsTrack = audio;
					}
				}
			}

			// preferred audio not found, take a default audio track, dts first if available
			if (dtsTrack != null) {
				params.aid = dtsTrack;
				LOGGER.trace("Found priority audio track with DTS: " + dtsTrack);
			} else {
				params.aid = media.getAudioTracksList().get(0);
				LOGGER.trace("Chose a default audio track: " + params.aid);
			}
		}
	}

	/**
	 * This method populates the supplied {@link OutputParams} object with the
	 * correct subtitles (sid) based on the given filename, its MediaInfo
	 * metadata and DMS configuration settings.
	 *
	 * TODO: Rewrite this crazy method to be more concise and logical.
	 *
	 * @param fileName The file name used to determine the availability of
	 *            subtitles.
	 * @param media The MediaInfo metadata for the file.
	 * @param params The parameters to populate.
	 */
	public static void setSubtitleOutputParameters(String fileName, DLNAMediaInfo media, OutputParams params) {
		// Use device-specific DMS conf
		PmsConfiguration configuration = PMS.getConfiguration(params);
		String currentLang = null;
		DLNAMediaSubtitle matchedSub = null;

		if (params.aid != null) {
			currentLang = params.aid.getLang();
		}

		if (params.sid != null && params.sid.getId() == -1) {
			LOGGER.trace("Don't want subtitles!");
			params.sid = null;
			return;
		}

		/**
		 * Check for live subtitles
		 */
		if (params.sid != null && !StringUtils.isEmpty(params.sid.getLiveSubURL())) {
			LOGGER.debug("Live subtitles " + params.sid.getLiveSubURL());
			try {
				matchedSub = params.sid;
				String file = OpenSubtitle.fetchSubs(matchedSub.getLiveSubURL(), matchedSub.getLiveSubFile());
				if (!StringUtils.isEmpty(file)) {
					matchedSub.setExternalFile(new File(file), null);
					params.sid = matchedSub;
					return;
				}
			} catch (IOException e) {
			}
		}

		StringTokenizer st = new StringTokenizer(configuration.getAudioSubLanguages(), ";");

		/*
		 * Check for external and internal subtitles matching the user's
		 * language preferences
		 */
		boolean matchedInternalSubtitles = false;
		boolean matchedExternalSubtitles = false;
		while (st.hasMoreTokens()) {
			String pair = st.nextToken();
			if (pair.contains(",")) {
				String audio = pair.substring(0, pair.indexOf(','));
				String sub = pair.substring(pair.indexOf(',') + 1);
				audio = audio.trim();
				sub = sub.trim();
				LOGGER.trace("Searching for a match for: " + currentLang + " with " + audio + " and " + sub);

				if (Iso639.isCodesMatching(audio, currentLang) || (currentLang != null && audio.equals("*"))) {
					if (sub.equals("off")) {
						/*
						 * Ignore the "off" language for external subtitles if
						 * the user setting is enabled.
						 *
						 * TODO: Prioritize multiple external subtitles properly
						 * instead of just taking the first one we load
						 */
						if (configuration.isForceExternalSubtitles()) {
							for (DLNAMediaSubtitle subPresent : media.getSubtitleTracksList()) {
								if (subPresent.getExternalFile() != null) {
									matchedSub = subPresent;
									matchedExternalSubtitles = true;
									LOGGER.trace("Ignoring the \"off\" language because there are external subtitles");
									break;
								}
							}
						}
						if (!matchedExternalSubtitles) {
							matchedSub = new DLNAMediaSubtitle();
							matchedSub.setLang("off");
						}
					} else {
						for (DLNAMediaSubtitle subPresent : media.getSubtitleTracksList()) {
							if (subPresent.matchCode(sub) || sub.equals("*")) {
								if (subPresent.getExternalFile() != null) {
									if (configuration.isAutoloadExternalSubtitles()) {
										// Subtitle is external and we want external subtitles, look no further
										matchedSub = subPresent;
										LOGGER.trace("Matched external subtitles track: " + matchedSub);
										break;
									}
									// Subtitle is external but we do not want external subtitles, keep searching
									LOGGER.trace("External subtitles ignored because of user setting: " + subPresent);
								} else if (!matchedInternalSubtitles) {
									matchedSub = subPresent;
									LOGGER.trace("Matched internal subtitles track: " + matchedSub);
									if (configuration.isAutoloadExternalSubtitles()) {
										// Subtitle is internal and we will wait to see if an external one is available instead
										matchedInternalSubtitles = true;
									} else {
										// Subtitle is internal and we will use it
										break;
									}
								}
							}
						}
					}

					if (matchedSub != null && !matchedInternalSubtitles) {
						break;
					}
				}
			}
		}

		/*
		 * Check for external subtitles that were skipped in the above code
		 * block because they didn't match language preferences, if there wasn't
		 * already a match and the user settings specify it.
		 */
		if (matchedSub == null && configuration.isForceExternalSubtitles()) {
			for (DLNAMediaSubtitle subPresent : media.getSubtitleTracksList()) {
				if (subPresent.getExternalFile() != null) {
					matchedSub = subPresent;
					LOGGER.trace("Matched external subtitles track that did not match language preferences: " + matchedSub);
					break;
				}
			}
		}

		/*
		 * Disable chosen subtitles if the user has disabled all subtitles or if
		 * the language preferences have specified the "off" language.
		 *
		 * TODO: Can't we save a bunch of looping by checking for
		 * isDisableSubtitles just after the Live Subtitles check above?
		 */
		if (matchedSub != null && params.sid == null) {
			if (configuration.isDisableSubtitles() || (matchedSub.getLang() != null && matchedSub.getLang().equals("off"))) {
				LOGGER.trace("Disabled the subtitles: " + matchedSub);
			} else {
				params.sid = matchedSub;
			}
		}

		/*
		 * Check for forced subtitles.
		 */
		if (!configuration.isDisableSubtitles() && params.sid == null && media != null) {
			// Check for subtitles again
			File video = new File(fileName);
			FileUtil.isSubtitlesExists(video, media, false);

			if (configuration.isAutoloadExternalSubtitles()) {
				boolean forcedSubsFound = false;
				// Priority to external subtitles
				for (DLNAMediaSubtitle sub : media.getSubtitleTracksList()) {
					if (matchedSub != null && matchedSub.getLang() != null && matchedSub.getLang().equals("off")) {
						st = new StringTokenizer(configuration.getForcedSubtitleTags(), ",");

						while (sub.getSubtitlesTrackTitleFromMetadata() != null && st.hasMoreTokens()) {
							String forcedTags = st.nextToken();
							forcedTags = forcedTags.trim();

							if (
								sub.getSubtitlesTrackTitleFromMetadata().toLowerCase().contains(forcedTags) &&
								Iso639.isCodesMatching(sub.getLang(), configuration.getForcedSubtitleLanguage())
							) {
								LOGGER.trace(
									"Forcing preferred subtitles: {}/{}", sub.getLang(), sub.getSubtitlesTrackTitleFromMetadata()
								);
								LOGGER.trace("Forced subtitles track: " + sub);

								if (sub.getExternalFile() != null) {
									LOGGER.trace("Found external forced file: " + sub.getExternalFile().getAbsolutePath());
								}
								params.sid = sub;
								forcedSubsFound = true;
								break;
							}
						}
						if (forcedSubsFound) {
							break;
						}
					} else {
						LOGGER.trace("Found subtitles track: " + sub);

						if (sub.getExternalFile() != null) {
							LOGGER.trace("Found external file: " + sub.getExternalFile().getAbsolutePath());
							params.sid = sub;
							break;
						}
					}
				}
			}
			if (
				matchedSub != null &&
				matchedSub.getLang() != null &&
				matchedSub.getLang().equals("off")
			) {
				return;
			}

			if (params.sid == null) {
				st = new StringTokenizer(UMSUtils.getLangList(params.mediaRenderer), ",");
				while (st.hasMoreTokens()) {
					String lang = st.nextToken();
					lang = lang.trim();
					LOGGER.trace("Looking for a subtitle track with lang: " + lang);
					for (DLNAMediaSubtitle sub : media.getSubtitleTracksList()) {
						if (
							sub.matchCode(lang) &&
							!(
								!configuration.isAutoloadExternalSubtitles() &&
								sub.getExternalFile() != null
							)
						) {
							params.sid = sub;
							LOGGER.trace("Matched subtitles track: " + params.sid);
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * @see #convertToModX(int, int)
	 */
	@Deprecated
	public int convertToMod4(int number) {
		return convertToModX(number, 4);
	}

	/**
	 * Convert number to be divisible by mod.
	 *
	 * @param number the number to convert
	 * @param mod the number to divide by
	 * @return the number divisible by mod
	 */
	public static int convertToModX(int number, int mod) {
		if (number % mod != 0) {
			number -= (number % mod);
		}

		return number;
	}

	/**
	 * Returns whether or not this {@link Player} can handle a given
	 * {@link DLNAResource}. If {@code resource} is {@code null} {@code false}
	 * will be returned.
	 *
	 * @param resource the {@link DLNAResource} to be matched.
	 * @return {@code true} if {@code resource} can be handled, {@code false}
	 *         otherwise.
	 */
	public abstract boolean isCompatible(DLNAResource resource);

	/**
	 * Defines whether {@link #testExecutable} might set a
	 * {@link ExecutableErrorType#SPECIFIC} error. Implementations that return
	 * {@code true} will be tested even if the same executable has already been
	 * tested by another {@link Player} without errors. {@link Player}s that
	 * might have {@link ExecutableErrorType#SPECIFIC} errors set outside
	 * {@link #testExecutable} only should return {@code false}. If
	 * {@link #testPlayer} is overridden this might be irrelevant.
	 *
	 * @return {@code true} if this {@link Player}'s tests include specific
	 *         tests that might not apply to other {@link Player}s that use the
	 *         same executable, {@code false} otherwise.
	 */
	protected abstract boolean isSpecificTest();

	/**
	 * Does basic file tests of the specified executable, checking that it
	 * exists and has the required permissions.
	 *
	 * @param executableInfo the {@link ExecutableInfo} whose executable to
	 *            test.
	 * @return The resulting {@link ExecutableInfo} instance.
	 */
	@Nonnull
	protected ExecutableInfo testExecutableFile(@Nonnull ExecutableInfo executableInfo) {
		try {
			if (!new FilePermissions(executableInfo.getPath()).hasFlags(FileFlag.FILE, FileFlag.EXECUTE, FileFlag.READ)) {
				LOGGER.warn(
					"Insufficient permission to execute \"{}\" for transcoding engine {}",
					executableInfo.getPath(),
					this
				);
				executableInfo = executableInfo.modify()
					.available(Boolean.FALSE)
					.errorType(ExecutableErrorType.GENERAL)
					.errorText(
						String.format(Messages.getString("Engine.MissingExecutePermission"), executableInfo.getPath(), this)
					).build();
			}
		} catch (FileNotFoundException e) {
			LOGGER.warn(
				"Executable \"{}\" of transcoding engine {} not found: {}",
				executableInfo.getPath(),
				this,
				e.getMessage()
			);
			executableInfo = executableInfo.modify()
				.available(Boolean.FALSE)
				.errorType(ExecutableErrorType.GENERAL)
				.errorText(
					String.format(Messages.getString("Engine.ExecutableNotFound"), executableInfo.getPath(), this)
				).build();
		}
		return executableInfo;
	}

	/**
	 * Tests a specific executable and returns the results. If the executable
	 * has already has been tested, the previous results are used.
	 * <p>
	 * <b>This method must be implemented unless {@link #testPlayer} is
	 * overridden in such a way that this method is never called or no test can
	 * be performed on this executable</b> If the method isn't implemented,
	 * simply make it return {@code null}, which is interpreted by
	 * {@link #testPlayer} as if no test was performed.
	 *
	 * @param executableInfo the {@link ExecutableInfo} whose executable to
	 *            test.
	 * @return The resulting {@link ExecutableInfo} instance.
	 */
	@Nullable
	protected abstract ExecutableInfo testExecutable(@Nonnull ExecutableInfo executableInfo);

	/**
	 * Tests the executable(s) for this {@link Player} and stores the results.
	 * If the executable has already been tested by another {@link Player} or
	 * {@link ProgramExecutableType}, the previous results are used.
	 *
	 * @param executableType the {@link ProgramExecutableType} to test. Invalid
	 *            {@link ProgramExecutableType}s for this {@link Player} will
	 *            throw an {@link Exception}.
	 * @return {@code true} if a test was or previously has been performed,
	 *         {@code false} otherwise.
	 */
	public boolean testPlayer(@Nonnull ProgramExecutableType executableType) {
		if (executableType == null) {
			throw new IllegalArgumentException("executableType cannot be null");
		}
		ReentrantReadWriteLock programInfoLock = programInfo.getLock();
		programInfoLock.writeLock().lock();
		try {
			ExecutableInfo executableInfo = programInfo.getExecutableInfo(executableType);
			if (executableInfo == null || executableInfo.getPath() == null) {
				return false;
			}
			if (avisynth()) {
				if (!Platform.isWindows()) {
					LOGGER.debug(
						"Skipping transcoding engine {} ({}) as it's not compatible with this platform",
						this,
						executableType
					);
					setUnavailable(
						executableType,
						ExecutableErrorType.SPECIFIC,
						String.format(Messages.getString("Engine.ExecutablePlatformIncompatible"), this)
					);
					return true;
				}

				if (!BasicSystemUtils.INSTANCE.isAviSynthAvailable()) {
					LOGGER.debug(
						"Transcoding engine {} ({}) is unavailable since AviSynth couldn't be found",
						this,
						executableType
					);
					setUnavailable(
						executableType,
						ExecutableErrorType.SPECIFIC,
						String.format(Messages.getString("Engine.AviSynthNotFound"), this)
					);
					return true;
				}
			}

			if (
				executableInfo.getAvailable() != null &&
				(
					!executableInfo.getAvailable().booleanValue() ||
					!isSpecificTest()
				)
			) {
				// Executable has already been tested
				return true;
			}
			specificErrorsLock.writeLock().lock();
			try {
				if (specificErrors.get(executableType) != null) {
					// Executable Player specific failures has already been tested
					return true;
				}

				ExecutableInfo result = testExecutable(executableInfo);
				if (result == null) {
					// Executable test not implemented
					return false;
				}
				if (result.getAvailable() == null) {
					throw new AssertionError("Player test for " + name() + " failed to return availability");
				}
				if (!result.equals(executableInfo)) {
					// The test resulted in a change
					setAvailable(
						result.getAvailable(),
						executableType,
						result.getVersion(),
						result.getErrorType(),
						result.getErrorText()
					);
					programInfo.setExecutableInfo(executableType, result);
				}
				return true;
			} finally {
				specificErrorsLock.writeLock().unlock();
			}
		} finally {
			programInfoLock.writeLock().unlock();
		}
	}

	/**
	 * Checks if {@code object} is a {@link Player} and has the same
	 * {@link #id()} as this.
	 *
	 * @return {@code true} if {@code object} is a {@link Player} and the IDs
	 *         match, {@code false} otherwise.
	 */
	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || !(object instanceof Player)) {
			return false;
		}
		Player other = (Player) object;
		if (id() == null) {
			if (other.id() != null) {
				return false;
			}
		} else if (!id().equals(other.id())) {
			return false;
		}
		return true;
	}

	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id() == null) ? 0 : id().hashCode());
		return result;
	}
}
