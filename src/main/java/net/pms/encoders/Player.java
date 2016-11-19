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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JComponent;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.ExternalProgramInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.ProgramExecutableType;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.util.FileUtil;
import net.pms.util.Iso639;
import net.pms.util.OpenSubtitle;
import net.pms.util.UMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/** The cached "enabled" text */
	protected final String enabledText = String.format(Messages.getString("Engine.Enabled"), name());

	/** The cached "enabled" text including version information */
	protected final String enabledVersionText = Messages.getString("Engine.EnabledVersion");

	/** The cached "disabled" text */
	protected final String disabledText = String.format(Messages.getString("Engine.Disabled"), name());

	public abstract int purpose();
	public abstract JComponent config();
	public abstract PlayerId id();
	public abstract String name();
	public abstract int type();
	/**
	 * Must be used to control all access to {@link #available}
	 */
	protected final ReentrantReadWriteLock availableLock = new ReentrantReadWriteLock();

	/**
	 * Initialized in {@link PlayerFactory#registerPlayer(Player)}.
	 */
	protected volatile ProgramExecutableType currentExecutableType;

	/**
	 * Used to store if this {@link Player} can be used, e.g if the binary is
	 * accessible for the given {@link ProgramExecutableType}. All access must
	 * be guarded with {@link #availableLock}.
	 */
	@GuardedBy("availableLock")
	private final Map<ProgramExecutableType, Boolean> available = new HashMap<>();

	/**
	 * Used to store a localized error text if the the given
	 * {@link ProgramExecutableType} for this {@link Player} is unavailable. All
	 * access must be guarded with {@link #availableLock}.
	 */
	@GuardedBy("availableLock")
	private final Map<ProgramExecutableType, String> errorText = new HashMap<>();

	/**
	 * Used to store the executable version if the given
	 * {@link ProgramExecutableType} for this {@link Player} is available and
	 * the information could be parsed. All access must be guarded with
	 * {@link #availableLock}.
	 */
	@GuardedBy("availableLock")
	protected final Map<ProgramExecutableType, String> versionText = new HashMap<>();

	/**
	 * Must be used to control all access to {@link #enabled}
	 */
	protected final ReentrantReadWriteLock enabledLock = new ReentrantReadWriteLock();

	/**
	 * Used to store if this {@link Player} is enabled in the configuration. All
	 * access must be guarded with {@link #availableLock}.
	 */
	@GuardedBy("enabledLock")
	protected boolean enabled = false;

	/**
	 * Gets the current {@link ProgramExecutableType} for this {@link Player}.
	 * This isn't necessarily the same as the configuration setting for this
	 * {@link PlayerId} which can be gotten using
	 * {@link PmsConfiguration#getExecutableType(PlayerId)}. The difference
	 * is that {@link PlayerFactory#registerPlayer(Player)} will choose the
	 * {@link ProgramExecutableType} based on configuration, default and test
	 * results without changing the configuration. //TODO: (Nad) make clear
	 *
	 * @return The current {@link ProgramExecutableType}
	 */
	public ProgramExecutableType getCurrentExecutableType() {
		return currentExecutableType;
	}

	/**
	 * Sets the current {@link ProgramExecutableType} for this {@link Player}
	 * and optionally also sets the same value in the configuration.
	 *
	 * @param executableType the new {@link ProgramExecutableType}.
	 * @param setConfiguration whether to set the configuration value.
	 */
	public void setCurrentExecutableType(ProgramExecutableType executableType, boolean setConfiguration) {
		currentExecutableType = executableType;
		if (setConfiguration) {
			_configuration.setExecutableType(id(), executableType);
		}
	}

	// FIXME this is an implementation detail (and not a very good one).
	// it's entirely up to engines how they construct their command lines.
	// need to get rid of this
	public abstract String[] args();

	public abstract String mimeType();
	public abstract ExternalProgramInfo executables();

	public String executable() {
		Path executable = executables().getPath(currentExecutableType);
		return executable == null ? null : executable.toString();
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
	 * Used to determine if this {@link Player} can be used, e.g if the binary is
	 * accessible for the given {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *                       text for.
	 */
	public boolean isAvailable(ProgramExecutableType executableType) {
		if (executableType == null) {
			return false;
		}
		availableLock.readLock().lock();
		try {
			Boolean result = available.get(executableType);
			return result == null ? false : result.booleanValue();
		} finally {
			availableLock.readLock().unlock();
		}
	}

	/**
	 * Used to determine if the player can be used, e.g if the binary is
	 * accessible for the current {@link ProgramExecutableType}.
	 */
	public boolean isAvailable() {
		return isAvailable(currentExecutableType);
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized text
	 * for the given {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get the status
	 *            text for.
	 * @return The localized status text.
	 */
	public String getStatusText(ProgramExecutableType executableType) {
		if (executableType == null) {
			return null;
		}
		availableLock.readLock().lock();
		try {
			if (isActive(executableType)) {
				if (isNotBlank(versionText.get(executableType))) {
					return String.format(enabledVersionText, name(), versionText.get(executableType));
				}
				return enabledText;
			} else if (isAvailable()) {
				return disabledText;
			} else {
				return errorText.get(executableType);
			}
		} finally {
			availableLock.readLock().unlock();
		}
	}

	/**
	 * Returns the current engine status (enabled, available) as a localized
	 * text for the current {@link ProgramExecutableType}.
	 *
	 * @return The localized status text.
	 */
	public String getStatusText() {
		return getStatusText(currentExecutableType);
	}

	/**
	 * Sets the engine available status and a related text. Note that
	 * {@code statusText} has a "dual function".
	 *
	 * @param available whether or not the {@link Player} is available
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *                       availability.
	 * @param statusText if {@code available} is {@code true}, the executable
	 *            version or {@code null} the version if unknown. If
	 *            {@code available} is {@code false}, a localized description of
	 *            the current error.
	 */
	public void setAvailable(boolean available, ProgramExecutableType executableType, String statusText) {
		if (executableType == null) {
			throw new IllegalArgumentException("executableType cannot be null or unknown");
		}
		availableLock.writeLock().lock();
		try {
			this.available.put(executableType, Boolean.valueOf(available));
			if (available) {
				versionText.put(executableType, statusText);
			} else {
				errorText.put(executableType, statusText);
			}
		} finally {
			availableLock.writeLock().unlock();
		}
	}

	/**
	 * Marks the engine as available for use.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *                       availability.
	 * @param versionText the parsed version string for the executable, or
	 *            {@code null} if the version is unknown.
	 */
	public void setAvailable(ProgramExecutableType executableType, String versionText) {
		setAvailable(true, executableType, versionText);
	}

	/**
	 * Marks the engine as unavailable for use.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to set
	 *                       availability.
	 * @param errorText the localized error description.
	 */
	public void setUnavailable(ProgramExecutableType executableType, String errorText) {
		setAvailable(false, executableType, errorText);
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
	 * enabled.
	 *
	 * @return {@code true} if this {@link Player} is both available and
	 *         enabled, {@code false} otherwise.
	 * for the given {@link ProgramExecutableType}.
	 *
	 * @param executableType the {@link ProgramExecutableType} for which to
	 *                       check availability.
	 */
	public boolean isActive(ProgramExecutableType executableType) {
		return isAvailable(executableType) && isEnabled();
	}

	/**
	 * Convenience method to check that a player is both available and enabled
	 * for the current {@link ProgramExecutableType}.
	 */
	public boolean isActive() {
		return isAvailable(currentExecutableType) && isEnabled();
	}

	/**
	 * Returns whether or not this {@link Player} supports GPU acceleration.
	 * <p>
	 * Each {@link Player} capable of video hardware acceleration must override
	 * this method and return {@code true}.
	 *
	 * @return {@code true} if GPU acceleration is supported, {@code false}
	 *         otherwise.
	 */
	public boolean isGPUAccelerationReady() {
		return false;
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
	 * Checks if {@code object} is a {@link Player} and has the same
	 * {@link #id()} as this.
	 *
	 * @return {@code true} if {@code object} is a {@link Player} and the IDs
	 *         match, {@code false} otherwise.
	 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id() == null) ? 0 : id().hashCode());
		return result;
	}
}
