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
package net.pms.configuration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import com.sun.jna.Platform;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.CodeEnter;
import net.pms.dlna.MediaType;
import net.pms.encoders.FFMpegVideo;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;
import net.pms.encoders.PlayerId;
import net.pms.encoders.StandardPlayerId;
import net.pms.exception.InvalidArgumentException;
import net.pms.image.thumbnail.CoverSupplier;
import net.pms.io.BasicSystemUtils;
import net.pms.io.WindowsSystemUtils;
import net.pms.logging.LogLevel;
import net.pms.newgui.NavigationShareTab.SharedFoldersTableModel;
import net.pms.platform.windows.CSIDL;
import net.pms.platform.windows.KnownFolders;
import net.pms.service.PreventSleepMode;
import net.pms.service.Services;
import net.pms.util.ConversionUtil;
import net.pms.util.FilePermissions;
import net.pms.util.FileUtil;
import net.pms.util.ConversionUtil.UnitPrefix;
import net.pms.util.FullyPlayedAction;
import net.pms.util.Languages;
import net.pms.util.LogSystemInformationMode;
import net.pms.util.Pair;
import net.pms.util.PropertiesUtil;
import net.pms.util.StringUtil;
import net.pms.util.SubtitleColor;
import net.pms.util.UMSUtils;
import net.pms.util.UniqueList;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for all configurable DMS settings. Settings are typically defined by three things:
 * a unique key for use in the configuration file "DMS.conf", a getter (and setter) method and
 * a default value. When a key cannot be found in the current configuration, the getter will
 * return a default value. Setters only store a value, they do not permanently save it to
 * file.
 */
public class PmsConfiguration extends RendererConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(PmsConfiguration.class);
	protected static final int DEFAULT_PROXY_SERVER_PORT = -1;
	public static final int DEFAULT_SERVER_PORT = 5252;
	public static final int DEFAULT_WEBINTERFACE_PORT = 6363;
	// 90000 lines is approximately 10 MiB depending on locale and message length
	public static final int LOGGING_LOGS_TAB_LINEBUFFER_MAX = 90000;
	public static final int LOGGING_LOGS_TAB_LINEBUFFER_MIN = 100;
	public static final int LOGGING_LOGS_TAB_LINEBUFFER_STEP = 500;

	private static volatile boolean enabledEnginesBuilt = false;
	private static final ReentrantReadWriteLock enabledEnginesLock = new ReentrantReadWriteLock();
	private static UniqueList<PlayerId> enabledEngines;

	private static volatile boolean enginesPriorityBuilt = false;
	private static final ReentrantReadWriteLock enginesPriorityLock = new ReentrantReadWriteLock();
	private static UniqueList<PlayerId> enginesPriority;

	protected static final String KEY_3D_SUBTITLES_DEPTH = "3d_subtitles_depth";
	protected static final String KEY_ALIVE_DELAY = "ALIVE_delay";
	protected static final String KEY_ALTERNATE_SUBTITLES_FOLDER = "alternate_subtitles_folder";
	protected static final String KEY_ALTERNATE_THUMB_FOLDER = "alternate_thumb_folder";
	protected static final String KEY_APPEND_PROFILE_NAME = "append_profile_name";
	protected static final String KEY_ATZ_LIMIT = "atz_limit";
	protected static final String KEY_AUTOMATIC_DISCOVER = "automatic_discover";
	protected static final String KEY_AUTOMATIC_MAXIMUM_BITRATE = "automatic_maximum_bitrate";
	protected static final String KEY_AUDIO_BITRATE = "audio_bitrate";
	protected static final String KEY_AUDIO_CHANNEL_COUNT = "audio_channels";
	protected static final String KEY_AUDIO_EMBED_DTS_IN_PCM = "audio_embed_dts_in_pcm";
	protected static final String KEY_AUDIO_LANGUAGES = "audio_languages";
	protected static final String KEY_AUDIO_REMUX_AC3 = "audio_remux_ac3";
	protected static final String KEY_AUDIO_RESAMPLE = "audio_resample";
	protected static final String KEY_AUDIO_SUB_LANGS = "audio_subtitles_languages";
	protected static final String KEY_AUDIO_THUMBNAILS_METHOD = "audio_thumbnails_method";
	protected static final String KEY_AUDIO_USE_PCM = "audio_use_pcm";
	protected static final String KEY_AUTOLOAD_SUBTITLES = "autoload_external_subtitles";
	protected static final String KEY_AVISYNTH_CONVERT_FPS = "avisynth_convert_fps";
	protected static final String KEY_AVISYNTH_INTERFRAME = "avisynth_interframe";
	protected static final String KEY_AVISYNTH_SCRIPT = "avisynth_script";
	protected static final String KEY_ASS_MARGIN = "subtitles_ass_margin";
	protected static final String KEY_ASS_OUTLINE = "subtitles_ass_outline";
	protected static final String KEY_ASS_SCALE = "subtitles_ass_scale";
	protected static final String KEY_ASS_SHADOW = "subtitles_ass_shadow";
	protected static final String KEY_BUFFER_MAX = "buffer_max";
	protected static final String KEY_BUMP_ADDRESS = "bump";
	protected static final String KEY_BUMP_IPS = "allowed_bump_ips";
	protected static final String KEY_BUMP_JS = "bump.js";
	protected static final String KEY_BUMP_SKIN_DIR = "bump.skin";
	protected static final String KEY_CHAPTER_INTERVAL = "chapter_interval";
	protected static final String KEY_CHAPTER_SUPPORT = "chapter_support";
	protected static final String KEY_CHROMECAST_DBG = "chromecast_debug";
	protected static final String KEY_CHROMECAST_EXT = "chromecast_extension";
	protected static final String KEY_CODE_CHARS = "code_charset";
	protected static final String KEY_CODE_THUMBS = "code_show_thumbs_no_code";
	protected static final String KEY_CODE_TMO = "code_valid_timeout";
	protected static final String KEY_CODE_USE = "code_enable";
	protected static final String KEY_DATABASE_CACHE_SIZE = "db_cache_size";
	protected static final String KEY_DISABLE_FAKESIZE = "disable_fakesize";
	public    static final String KEY_DISABLE_SUBTITLES = "disable_subtitles";
	protected static final String KEY_DISABLE_TRANSCODE_FOR_EXTENSIONS = "disable_transcode_for_extensions";
	protected static final String KEY_DISABLE_TRANSCODING = "disable_transcoding";
	protected static final String KEY_DVDISO_THUMBNAILS = "dvd_isos_thumbnails";
	protected static final String KEY_DYNAMIC_PLS = "dynamic_playlist";
	protected static final String KEY_DYNAMIC_PLS_AUTO_SAVE = "dynamic_playlist_auto_save";
	protected static final String KEY_DYNAMIC_PLS_HIDE = "dynamic_playlist_hide_folder";
	protected static final String KEY_DYNAMIC_PLS_SAVE_PATH = "dynamic_playlist_save_path";
	protected static final String KEY_ENCODED_AUDIO_PASSTHROUGH = "encoded_audio_passthrough";
	protected static final String KEY_ENGINES = "engines";
	protected static final String KEY_ENGINES_PRIORITY = "engines_priority";
	protected static final String KEY_FFMPEG_AVISYNTH_CONVERT_FPS = "ffmpeg_avisynth_convertfps";
	protected static final String KEY_FFMPEG_AVISYNTH_INTERFRAME = "ffmpeg_avisynth_interframe";
	protected static final String KEY_FFMPEG_AVISYNTH_INTERFRAME_GPU = "ffmpeg_avisynth_interframegpu";
	protected static final String KEY_FFMPEG_AVISYNTH_MAX_THREADS = "ffmpeg_avisynth_max_threads";
	protected static final String KEY_FFMPEG_FONTCONFIG = "ffmpeg_fontconfig";
	protected static final String KEY_FFMPEG_DECODING_HARDWARE_ACCELERATION = "ffmpeg_decoding_hardware_acceleration";
	protected static final String KEY_FFMPEG_MAX_THREADS = "ffmpeg_max_threads";
	protected static final String KEY_FFMPEG_MENCODER_PROBLEMATIC_SUBTITLES = "ffmpeg_mencoder_problematic_subtitles";
	protected static final String KEY_FFMPEG_MUX_TSMUXER_COMPATIBLE = "ffmpeg_mux_tsmuxer_compatible";
	protected static final String KEY_FIX_25FPS_AV_MISMATCH = "fix_25fps_av_mismatch";
	protected static final String KEY_FOLDER_LIMIT = "folder_limit";
	protected static final String KEY_FOLDERS = "folders";
	protected static final String KEY_FOLDERS_IGNORED = "folders_ignored";
	protected static final String KEY_FOLDERS_MONITORED = "folders_monitored";
	protected static final String KEY_FONT = "subtitles_font";
	protected static final String KEY_FORCE_EXTERNAL_SUBTITLES = "force_external_subtitles";
	protected static final String KEY_FORCE_TRANSCODE_FOR_EXTENSIONS = "force_transcode_for_extensions";
	protected static final String KEY_FORCED_SUBTITLE_LANGUAGE = "forced_subtitle_language";
	protected static final String KEY_FORCED_SUBTITLE_TAGS = "forced_subtitle_tags";
	protected static final String KEY_GUI_CLOSE_ACTION = "gui_close_action";
	protected static final String KEY_GUI_LOG_SEARCH_CASE_SENSITIVE = "gui_log_search_case_sensitive";
	protected static final String KEY_GUI_LOG_SEARCH_MULTILINE = "gui_log_search_multiline";
	protected static final String KEY_GUI_LOG_SEARCH_USE_REGEX = "gui_log_search_use_regex";
	protected static final String KEY_HIDE_ADVANCED_OPTIONS = "hide_advanced_options";
	protected static final String KEY_HIDE_EMPTY_FOLDERS = "hide_empty_folders";
	protected static final String KEY_HIDE_ENGINENAMES = "hide_enginenames";
	protected static final String KEY_HIDE_EXTENSIONS = "hide_extensions";
	protected static final String KEY_HIDE_LIVE_SUBTITLES_FOLDER = "hide_live_subtitles_folder";
	protected static final String KEY_HIDE_MEDIA_LIBRARY_FOLDER = "hide_media_library_folder";
	protected static final String KEY_HIDE_NEW_MEDIA_FOLDER = "hide_new_media_folder";
	protected static final String KEY_HIDE_RECENTLY_PLAYED_FOLDER = "hide_recently_played_folder";
	protected static final String KEY_HIDE_SUBS_INFO = "hide_subs_info";
	protected static final String KEY_HIDE_TRANSCODE_FOLDER = "hide_transcode_folder";
	protected static final String KEY_HIDE_VIDEO_SETTINGS = "hide_video_settings";
	protected static final String KEY_HTTP_ENGINE_V2 = "http_engine_v2";
	protected static final String KEY_IGNORE_THE_WORD_A_AND_THE = "ignore_the_word_a_and_the";
	protected static final String KEY_IMAGE_THUMBNAILS_ENABLED = "image_thumbnails";
	protected static final String KEY_INFO_DB_RETRY = "infodb_retry";
	protected static final String KEY_IP_FILTER = "ip_filter";
	protected static final String KEY_ITUNES_LIBRARY_PATH = "itunes_library_path";
	protected static final String KEY_LANGUAGE = "language";
	protected static final String KEY_LIVE_SUBTITLES_KEEP = "live_subtitles_keep";
	protected static final String KEY_LIVE_SUBTITLES_LIMIT = "live_subtitles_limit";
	protected static final String KEY_LIVE_SUBTITLES_TMO = "live_subtitles_timeout";
	protected static final String KEY_LOG_SYSTEM_INFO = "log_system_info";
	protected static final String KEY_LOGGING_LOGFILE_NAME = "logging_logfile_name";
	protected static final String KEY_LOGGING_BUFFERED = "logging_buffered";
	protected static final String KEY_LOGGING_FILTER_CONSOLE = "logging_filter_console";
	protected static final String KEY_LOGGING_FILTER_LOGS_TAB = "logging_filter_logs_tab";
	protected static final String KEY_LOGGING_LOGS_TAB_LINEBUFFER = "logging_logs_tab_linebuffer";
	protected static final String KEY_LOGGING_SYSLOG_FACILITY = "logging_syslog_facility";
	protected static final String KEY_LOGGING_SYSLOG_HOST = "logging_syslog_host";
	protected static final String KEY_LOGGING_SYSLOG_PORT = "logging_syslog_port";
	protected static final String KEY_LOGGING_USE_SYSLOG = "logging_use_syslog";
	protected static final String KEY_LOG_DATABASE = "log_database";
	protected static final String KEY_MAX_AUDIO_BUFFER = "maximum_audio_buffer_size";
	protected static final String KEY_MAX_BITRATE = "maximum_bitrate";
	protected static final String KEY_MAX_MEMORY_BUFFER_SIZE = "maximum_video_buffer_size";
	protected static final String KEY_MEDIA_LIB_SORT = "media_lib_sort";
	protected static final String KEY_MENCODER_ASS = "mencoder_ass";
	protected static final String KEY_MENCODER_AC3_FIXED = "mencoder_ac3_fixed";
	protected static final String KEY_MENCODER_AVISYNTH_INTERFRAME_GPU = "mencoder_avisynth_interframegpu";
	protected static final String KEY_MENCODER_AVISYNTH_MAX_THREADS = "mencoder_avisynth_max_threads";
	protected static final String KEY_MENCODER_CODEC_SPECIFIC_SCRIPT = "mencoder_codec_specific_script";
	protected static final String KEY_MENCODER_CUSTOM_OPTIONS = "mencoder_custom_options";
	protected static final String KEY_MENCODER_FONT_CONFIG = "mencoder_fontconfig";
	protected static final String KEY_MENCODER_FORCE_FPS = "mencoder_forcefps";
	protected static final String KEY_MENCODER_INTELLIGENT_SYNC = "mencoder_intelligent_sync";
	protected static final String KEY_MENCODER_MAX_THREADS = "mencoder_max_threads";
	protected static final String KEY_MENCODER_MUX_COMPATIBLE = "mencoder_mux_compatible";
	protected static final String KEY_MENCODER_SPEED_TRUMPS_COMPATIBILITY = "mencoder_speed_trumps_compatibility";
	protected static final String KEY_MENCODER_NO_OUT_OF_SYNC = "mencoder_nooutofsync";
	protected static final String KEY_MENCODER_NOASS_BLUR = "mencoder_noass_blur";
	protected static final String KEY_MENCODER_NOASS_OUTLINE = "mencoder_noass_outline";
	protected static final String KEY_MENCODER_NOASS_SCALE = "mencoder_noass_scale";
	protected static final String KEY_MENCODER_NOASS_SUBPOS = "mencoder_noass_subpos";
	protected static final String KEY_MENCODER_NORMALIZE_VOLUME = "mencoder_normalize_volume";
	protected static final String KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT = "mencoder_overscan_compensation_height";
	protected static final String KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH = "mencoder_overscan_compensation_width";
	protected static final String KEY_MENCODER_REMUX_MPEG2 = "mencoder_remux_mpeg2";
	protected static final String KEY_MENCODER_SCALER = "mencoder_scaler";
	protected static final String KEY_MENCODER_SCALEX = "mencoder_scalex";
	protected static final String KEY_MENCODER_SCALEY = "mencoder_scaley";
	protected static final String KEY_MENCODER_SUB_FRIBIDI = "mencoder_subfribidi";
	protected static final String KEY_MENCODER_USE_PCM_FOR_HQ_AUDIO_ONLY = "mencoder_usepcm_for_hq_audio_only";
	protected static final String KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY = "mencoder_vobsub_subtitle_quality";
	protected static final String KEY_MENCODER_YADIF = "mencoder_yadif";
	protected static final String KEY_MIN_MEMORY_BUFFER_SIZE = "minimum_video_buffer_size";
	protected static final String KEY_MIN_PLAY_TIME = "minimum_watched_play_time";
	protected static final String KEY_MIN_PLAY_TIME_FILE = "min_playtime_file";
	protected static final String KEY_MIN_PLAY_TIME_WEB = "min_playtime_web";
	protected static final String KEY_MIN_STREAM_BUFFER = "minimum_web_buffer_size";
	protected static final String KEY_GUI_START_HIDDEN = "gui_start_hidden";
	protected static final String KEY_MPEG2_MAIN_SETTINGS = "mpeg2_main_settings";
	protected static final String KEY_MUX_ALLAUDIOTRACKS = "tsmuxer_mux_all_audiotracks";
	protected static final String KEY_NETWORK_INTERFACE = "network_interface";
	protected static final String KEY_OPEN_ARCHIVES = "enable_archive_browsing";
	protected static final String KEY_OVERSCAN = "mencoder_overscan";
	protected static final String KEY_PLAYLIST_AUTO_ADD_ALL= "playlist_auto_add_all";
	protected static final String KEY_PLAYLIST_AUTO_CONT = "playlist_auto_continue";
	protected static final String KEY_PLAYLIST_AUTO_PLAY= "playlist_auto_play";
	protected static final String KEY_PLUGIN_FOLDER = "plugins";
	protected static final String KEY_PLUGIN_PURGE_ACTION = "plugin_purge";
	protected static final String KEY_PRETTIFY_FILENAMES = "prettify_filenames";
	/**
	 * This key was used in older versions, only supports {@code true} or
	 * {@code false}. Kept for backwards-compatibility for now.
	 *
	 * @deprecated Use {@link #KEY_PREVENT_SLEEP} instead.
	 */
	@Deprecated
	protected static final String KEY_PREVENTS_SLEEP = "prevents_sleep_mode";
	protected static final String KEY_PREVENT_SLEEP = "prevent_sleep";
	protected static final String KEY_PROFILE_NAME = "name";
	protected static final String KEY_PROXY_SERVER_PORT = "proxy";
	protected static final String KEY_RENDERER_DEFAULT = "renderer_default";
	protected static final String KEY_RENDERER_FORCE_DEFAULT = "renderer_force_default";
	protected static final String KEY_RESUME = "resume";
	protected static final String KEY_RESUME_BACK = "resume_back";
	protected static final String KEY_RESUME_KEEP_TIME = "resume_keep_time";
	protected static final String KEY_RESUME_REWIND = "resume_rewind";
	protected static final String KEY_ROOT_LOG_LEVEL = "log_level";
	protected static final String KEY_RUN_WIZARD = "run_wizard";
	protected static final String KEY_SCRIPT_DIR = "script_dir";
	protected static final String KEY_SEARCH_FOLDER = "search_folder";
	protected static final String KEY_SEARCH_IN_FOLDER = "search_in_folder";
	protected static final String KEY_SEARCH_RECURSE = "search_recurse"; // legacy option
	protected static final String KEY_SEARCH_RECURSE_DEPTH = "search_recurse_depth";
	protected static final String KEY_SELECTED_RENDERERS = "selected_renderers";
	protected static final String KEY_SERVER_HOSTNAME = "hostname";
	protected static final String KEY_SERVER_NAME = "server_name";
	protected static final String KEY_SERVER_PORT = "port";
	protected static final String KEY_SHARES = "shares";
	protected static final String KEY_SHOW_APERTURE_LIBRARY = "show_aperture_library";
	protected static final String KEY_SHOW_IPHOTO_LIBRARY = "show_iphoto_library";
	protected static final String KEY_SHOW_ITUNES_LIBRARY = "show_itunes_library";
	protected static final String KEY_SHOW_SPLASH_SCREEN = "show_splash_screen";
	protected static final String KEY_SKIP_LOOP_FILTER_ENABLED = "mencoder_skip_loop_filter";
	protected static final String KEY_SKIP_NETWORK_INTERFACES = "skip_network_interfaces";
	protected static final String KEY_SORT_METHOD = "sort_method";
	protected static final String KEY_SORT_PATHS = "sort_paths";
	protected static final String KEY_SPEED_DBG = "speed_debug";
	protected static final String KEY_SUBS_COLOR = "subtitles_color";
	protected static final String KEY_SUBTITLES_CODEPAGE = "subtitles_codepage";
	protected static final String KEY_SUBTITLES_LANGUAGES = "subtitles_languages";
	protected static final String KEY_TEMP_FOLDER_PATH = "temp_directory";
	protected static final String KEY_THUMBNAIL_GENERATION_ENABLED = "generate_thumbnails";
	protected static final String KEY_THUMBNAIL_SEEK_POS = "thumbnail_seek_position";
	protected static final String KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS = "transcode_block_multiple_connections";
	protected static final String KEY_TRANSCODE_FOLDER_NAME = "transcode_folder_name";
	protected static final String KEY_TRANSCODE_KEEP_FIRST_CONNECTION = "transcode_keep_first_connection";
	protected static final String KEY_TSMUXER_FORCEFPS = "tsmuxer_forcefps";
	protected static final String KEY_UPNP_ENABLED = "upnp_enable";
	protected static final String KEY_UPNP_PORT = "upnp_port";
	protected static final String KEY_USE_CACHE = "use_cache";
	protected static final String KEY_USE_DEFAULT_FOLDERS = "use_default_folders";
	protected static final String KEY_USE_EMBEDDED_SUBTITLES_STYLE = "use_embedded_subtitles_style";
	protected static final String KEY_USE_IMDB_INFO = "use_imdb_info";
	protected static final String KEY_USE_MPLAYER_FOR_THUMBS = "use_mplayer_for_video_thumbs";
	protected static final String KEY_UUID = "uuid";
	protected static final String KEY_VIDEOTRANSCODE_START_DELAY = "videotranscode_start_delay";
	protected static final String KEY_VIRTUAL_FOLDERS = "virtual_folders";
	protected static final String KEY_VIRTUAL_FOLDERS_FILE = "virtual_folders_file";
	protected static final String KEY_VLC_AUDIO_SYNC_ENABLED = "vlc_audio_sync_enabled";
	protected static final String KEY_VLC_MAX_THREADS = "vlc_max_threads";
	protected static final String KEY_VLC_SAMPLE_RATE = "vlc_sample_rate";
	protected static final String KEY_VLC_SAMPLE_RATE_OVERRIDE = "vlc_sample_rate_override";
	protected static final String KEY_VLC_SCALE = "vlc_scale";
	protected static final String KEY_VLC_SUBTITLE_ENABLED = "vlc_subtitle_enabled";
	protected static final String KEY_VLC_USE_EXPERIMENTAL_CODECS = "vlc_use_experimental_codecs";
	protected static final String KEY_VLC_HARDWARE_ACCELERATION = "vlc_hardware_acceleration";
	protected static final String KEY_FULLY_PLAYED_ACTION = "fully_played_action";
	protected static final String KEY_FULLY_PLAYED_OUTPUT_DIRECTORY = "fully_played_output_directory";
	protected static final String KEY_WEB_AUTHENTICATE = "web_authenticate";
	protected static final String KEY_WEB_BROWSE_LANG = "web_use_browser_lang";
	protected static final String KEY_WEB_BROWSE_SUB_LANG = "web_use_browser_sub_lang";
	protected static final String KEY_WEB_CHROME_TRICK = "web_chrome_mkv_as_webm_spoof";
	protected static final String KEY_WEB_CONF_PATH = "web_conf";
	protected static final String KEY_WEB_CONT_AUDIO = "web_continue_audio";
	protected static final String KEY_WEB_CONT_IMAGE = "web_continue_image";
	protected static final String KEY_WEB_CONT_VIDEO = "web_continue_video";
	protected static final String KEY_WEB_CONTROL = "web_control";
	protected static final String KEY_WEB_ENABLE = "web_enable";
	protected static final String KEY_WEB_FIREFOX_LINUX_MP4 = "web_firefox_linux_mp4";
	protected static final String KEY_WEB_FLASH = "web_flash";
	protected static final String KEY_WEB_HEIGHT = "web_height";
	protected static final String KEY_WEB_IMAGE_SLIDE = "web_image_show_delay";
	protected static final String KEY_WEB_LOOP_AUDIO = "web_loop_audio";
	protected static final String KEY_WEB_LOOP_IMAGE = "web_loop_image";
	protected static final String KEY_WEB_LOOP_VIDEO = "web_loop_video";
	protected static final String KEY_WEB_LOW_SPEED = "web_low_speed";
	protected static final String KEY_WEB_MP4_TRANS = "web_mp4_trans";
	protected static final String KEY_WEB_PATH = "web_path";
	protected static final String KEY_WEB_SIZE = "web_size";
	protected static final String KEY_WEB_SUBS_TRANS = "web_subtitles_transcoded";
	protected static final String KEY_WEB_THREADS = "web_threads";
	protected static final String KEY_WEB_TRANSCODE = "web_transcode";
	protected static final String KEY_WEB_WIDTH = "web_width";
	protected static final String KEY_X264_CONSTANT_RATE_FACTOR = "x264_constant_rate_factor";

	// The name of the subdirectory under which DMS config files are stored for this build (default: DMS).
	// See Build for more details
	protected static final String PROFILE_FOLDER_NAME = Build.getProfileFolderName();

	// The default profile name displayed on the renderer
	protected static final String COMPUTER_NAME = BasicSystemUtils.INSTANCE.getComputerName();

	protected static String DEFAULT_AVI_SYNTH_SCRIPT;
	protected static final int MAX_MAX_MEMORY_DEFAULT_SIZE = 400;
	protected static final int BUFFER_MEMORY_FACTOR = 368;
	protected static int MAX_MAX_MEMORY_BUFFER_SIZE = MAX_MAX_MEMORY_DEFAULT_SIZE;
	protected static final char LIST_SEPARATOR = ',';
	public final String ALL_RENDERERS = "All renderers";

	// Path to default logfile directory
	protected String defaultLogFileFolder = null;

	public TempFolder tempFolder;
	@Nonnull
	protected final PlatformProgramPaths programPaths;
	public IpFilter filter;

	/**
	 * The set of keys defining when the HTTP server has to restarted due to a configuration change
	 */
	public static final Set<String> NEED_RELOAD_FLAGS = new HashSet<>(
		Arrays.asList(
			KEY_ALTERNATE_THUMB_FOLDER,
			KEY_ATZ_LIMIT,
			KEY_AUDIO_THUMBNAILS_METHOD,
			KEY_CHAPTER_SUPPORT,
			KEY_DISABLE_TRANSCODE_FOR_EXTENSIONS,
			KEY_DISABLE_TRANSCODING,
			KEY_FOLDERS,
			KEY_FOLDERS_MONITORED,
			KEY_FORCE_TRANSCODE_FOR_EXTENSIONS,
			KEY_HIDE_EMPTY_FOLDERS,
			KEY_HIDE_ENGINENAMES,
			KEY_HIDE_EXTENSIONS,
			KEY_HIDE_LIVE_SUBTITLES_FOLDER,
			KEY_HIDE_MEDIA_LIBRARY_FOLDER,
			KEY_HIDE_SUBS_INFO,
			KEY_HIDE_TRANSCODE_FOLDER,
			KEY_HIDE_VIDEO_SETTINGS,
			KEY_IGNORE_THE_WORD_A_AND_THE,
			KEY_IP_FILTER,
			KEY_NETWORK_INTERFACE,
			KEY_OPEN_ARCHIVES,
			KEY_PRETTIFY_FILENAMES,
			KEY_SERVER_HOSTNAME,
			KEY_SERVER_NAME,
			KEY_SERVER_PORT,
			KEY_SHOW_APERTURE_LIBRARY,
			KEY_SHOW_IPHOTO_LIBRARY,
			KEY_SHOW_ITUNES_LIBRARY,
			KEY_SORT_METHOD,
			KEY_USE_CACHE,
			KEY_USE_DEFAULT_FOLDERS
		)
	);

	/*
		The following code enables a single setting - DMS_PROFILE - to be used to
		initialize PROFILE_PATH i.e. the path to the current session's profile (AKA DMS.conf).
		It also initializes PROFILE_DIRECTORY - i.e. the directory the profile is located in -
		which is needed to detect the default WEB.conf location (anything else?).

		While this convention - and therefore PROFILE_DIRECTORY - will remain,
		adding more configurables - e.g. web_conf = ... - is on the TODO list.

		DMS_PROFILE is read (in this order) from the property dms.profile.path or the
		environment variable DMS_PROFILE. If DMS is launched with the command-line option
		"profiles" (e.g. from a shortcut), it displays a file chooser dialog that
		allows the dms.profile.path property to be set. This makes it easy to run DMS
		under multiple profiles without fiddling with environment variables, properties or
		command-line arguments.

		1) if DMS_PROFILE is not set, DMS.conf is located in:

			Windows:             %ALLUSERSPROFILE%\$build
			Mac OS X:            $HOME/Library/Application Support/$build
			Everything else:     $HOME/.config/$build

		- where $build is a subdirectory that ensures incompatible DMS builds don't target/clobber
		the same configuration files. The default value for $build is "DMS". Other builds might use e.g.
		"DMS Rendr Edition" or "dms-mlx".

		2) if a relative or absolute *directory path* is supplied (the directory must exist),
		it is used as the profile directory and the profile is located there under the default profile name (DMS.conf):

			DMS_PROFILE = /absolute/path/to/dir
			DMS_PROFILE = relative/path/to/dir # relative to the working directory

		Amongst other things, this can be used to restore the legacy behaviour of locating DMS.conf in the current
		working directory e.g.:

			DMS_PROFILE=. ./DMS.sh

		3) if a relative or absolute *file path* is supplied (the file doesn't have to exist),
		it is taken to be the profile, and its parent dir is taken to be the profile (i.e. config file) dir:

			DMS_PROFILE = DMS.conf            # profile dir = .
			DMS_PROFILE = folder/dev.conf     # profile dir = folder
			DMS_PROFILE = /path/to/some.file  # profile dir = /path/to/
	 */
	protected static final String DEFAULT_CONFIGURATION_FILENAME = "DMS.conf";
	protected static final String ENV_PROFILE_PATH = "DMS_PROFILE";
	protected static final String DEFAULT_WEB_CONF_FILENAME = "WEB.conf";
	protected static final String DEFAULT_CREDENTIALS_FILENAME = "DMS.cred";

	// Path to directory containing DMS config files
	protected static final Path PROFILE_FOLDER;

	// Absolute path to configuration file e.g. /path/to/DMS.conf
	protected static final Path CONFIGURATION_FILE;

	// Absolute path to WEB.conf file e.g. /path/to/WEB.conf
	protected static String WEB_CONF_PATH;

	// Absolute path to skel (default) profile file e.g. /etc/skel/.config/digitalmediaserver/DMS.conf
	// "project.skelprofile.dir" project property
	protected static final String SKEL_PROFILE_PATH;

	protected static final String PROPERTY_PROFILE_PATH = "dms.profile.path";
	protected static final String SYSTEM_PROFILE_FOLDER;

	static {
		// first of all, set up the path to the default system profile directory
		if (Platform.isWindows()) {
			String programData = System.getenv("ALLUSERSPROFILE");

			if (programData != null) {
				SYSTEM_PROFILE_FOLDER = String.format("%s\\%s", programData, PROFILE_FOLDER_NAME);
			} else {
				SYSTEM_PROFILE_FOLDER = ""; // i.e. current (working) directory
			}
		} else if (Platform.isMac()) {
			SYSTEM_PROFILE_FOLDER = String.format(
				"%s/%s/%s",
				System.getProperty("user.home"),
				"/Library/Application Support",
				PROFILE_FOLDER_NAME
			);
		} else {
			String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

			if (xdgConfigHome == null) {
				SYSTEM_PROFILE_FOLDER = String.format("%s/.config/%s", System.getProperty("user.home"), PROFILE_FOLDER_NAME);
			} else {
				SYSTEM_PROFILE_FOLDER = String.format("%s/%s", xdgConfigHome, PROFILE_FOLDER_NAME);
			}
		}

		// now set the profile path. first: check for a custom setting.
		// try the system property, typically set via the profile chooser
		String customProfilePath = System.getProperty(PROPERTY_PROFILE_PATH);

		// failing that, try the environment variable
		if (isBlank(customProfilePath)) {
			customProfilePath = System.getenv(ENV_PROFILE_PATH);
		}

		// if customProfilePath is still blank, the default profile dir/filename is used
		CONFIGURATION_FILE = FileUtil.resolvePathWithDefaults(
			customProfilePath,
			SYSTEM_PROFILE_FOLDER,
			DEFAULT_CONFIGURATION_FILENAME
		);
		PROFILE_FOLDER = CONFIGURATION_FILE.getParent();

		// Set SKEL_PROFILE_PATH for Linux systems
		String skelDir = PropertiesUtil.getProjectProperties().get("project.skelprofile.dir");
		if (Platform.isLinux() && StringUtils.isNotBlank(skelDir)) {
			SKEL_PROFILE_PATH = FilenameUtils.normalize(
				new File(
					new File(
						skelDir,
						PROFILE_FOLDER_NAME
					).getAbsolutePath(),
					DEFAULT_CONFIGURATION_FILENAME
				).getAbsolutePath()
			);
		} else {
			SKEL_PROFILE_PATH = null;
		}
	}

	/**
	 * Default constructor that will attempt to load the DMS configuration file
	 * from the profile path.
	 *
	 * @throws org.apache.commons.configuration.ConfigurationException
	 */
	public PmsConfiguration() throws ConfigurationException {
		this(true);
	}

	/**
	 * Constructor that will initialize the DMS configuration.
	 *
	 * @param loadFile Set to true to attempt to load the DMS configuration
	 *                 file from the profile path. Set to false to skip
	 *                 loading.
	 */
	public PmsConfiguration(boolean loadFile) throws ConfigurationException {
		super(0);

		if (loadFile) {
			try {
				((PropertiesConfiguration) configuration).load(CONFIGURATION_FILE.toFile());
			} catch (ConfigurationException e) {
				if (Platform.isLinux() && SKEL_PROFILE_PATH != null) {
					LOGGER.debug("Failed to load {} ({}) - attempting to load skel profile", CONFIGURATION_FILE, e.getMessage());
					File skelConfigFile = new File(SKEL_PROFILE_PATH);

					try {
						// Load defaults from skel profile, save them later to PROFILE_PATH
						((PropertiesConfiguration)configuration).load(skelConfigFile);
						LOGGER.info("Default configuration loaded from {}", SKEL_PROFILE_PATH);
					} catch (ConfigurationException ce) {
						LOGGER.warn("Can't load neither {}: {} nor {}: {}", CONFIGURATION_FILE, e.getMessage(), SKEL_PROFILE_PATH, ce.getMessage());
					}
				} else {
					LOGGER.warn("Can't load {}: {}", CONFIGURATION_FILE, e.getMessage());
				}
			}
		}

		((PropertiesConfiguration)configuration).setPath(CONFIGURATION_FILE.toString());

		tempFolder = new TempFolder(getString(KEY_TEMP_FOLDER_PATH, null));
		programPaths = new ConfigurableProgramPaths(configuration);
		filter = new IpFilter();
		PMS.setLocale(getLanguageLocale(true));
		//TODO: The line below should be removed once all calls to Locale.getDefault() is replaced with PMS.getLocale()
		Locale.setDefault(getLanguageLocale());

		// Set DEFAULT_AVI_SYNTH_SCRIPT according to language
		DEFAULT_AVI_SYNTH_SCRIPT = "<movie>\n<sub>\n";

		long usableMemory = (Runtime.getRuntime().maxMemory() / 1048576) - BUFFER_MEMORY_FACTOR;
		if (usableMemory > MAX_MAX_MEMORY_DEFAULT_SIZE) {
			MAX_MAX_MEMORY_BUFFER_SIZE = (int) usableMemory;
		}
	}

	/**
	 * The following 2 constructors are for minimal instantiation in the context of subclasses
	 * (i.e. DeviceConfiguration) that use our getters and setters on another Configuration object.
	 * Here our main purpose is to initialize RendererConfiguration as required.
	 */
	protected PmsConfiguration(int ignored) {
		// Just instantiate
		super(0);
		tempFolder = null;
		programPaths = new ConfigurableProgramPaths(configuration);
		filter = null;
	}

	protected PmsConfiguration(File f, String uuid) throws ConfigurationException {
		// Just initialize super
		super(f, uuid);
		tempFolder = null;
		programPaths = new ConfigurableProgramPaths(configuration);
		filter = null;
	}

	@Override
	public void reset() {
		// This is just to prevent super.reset() from being invoked. Actual resetting would
		// require rebooting here, since all of the application settings are implicated.
	}

	private static String verifyLogFolder(File folder, String fallbackTo) {
		try {
			FilePermissions permissions = FileUtil.getFilePermissions(folder);
			if (LOGGER.isTraceEnabled()) {
				if (!permissions.isFolder()) {
					LOGGER.trace("getDefaultLogFileFolder: \"{}\" is not a folder, falling back to {} for logging", folder.getAbsolutePath(), fallbackTo);
				} else if (!permissions.isBrowsable()) {
					LOGGER.trace("getDefaultLogFileFolder: \"{}\" is not browsable, falling back to {} for logging", folder.getAbsolutePath(), fallbackTo);
				} else if (!permissions.isWritable()) {
					LOGGER.trace("getDefaultLogFileFolder: \"{}\" is not writable, falling back to {} for logging", folder.getAbsolutePath(), fallbackTo);
				}
			}
			if (permissions.isFolder() && permissions.isBrowsable() && permissions.isWritable()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Default logfile folder set to: {}", folder.getAbsolutePath());
				}
				return folder.getAbsolutePath();
			}
		} catch (FileNotFoundException e) {
			LOGGER.trace("getDefaultLogFileFolder: \"{}\" not found, falling back to {} for logging: {}", folder.getAbsolutePath(), fallbackTo, e.getMessage());
		}
		return null;
	}

	/**
	 * @return first writable folder in the following order:
	 * <p>
	 *     1. (On Linux only) path to {@code /var/log/dms/%USERNAME%/}.
	 * </p>
	 * <p>
	 *     2. Path to profile folder ({@code ~/.config/DigitalMediaServer/} on Linux, {@code %ALLUSERSPROFILE%\DigitalMediaServer} on Windows and
	 *     {@code ~/Library/Application Support/DigitalMediaServer/} on Mac).
	 * </p>
	 * <p>
	 *     3. Path to user-defined temporary folder specified by {@code temp_directory} parameter in DMS.conf.
	 * </p>
	 * <p>
	 *     4. Path to system temporary folder.
	 * </p>
	 * <p>
	 *     5. Path to current working directory.
	 * </p>
	 */
	public synchronized String getDefaultLogFileFolder() {
		if (defaultLogFileFolder == null) {
			if (Platform.isLinux()) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("getDefaultLogFileFolder: System is Linux, trying \"/var/log/DMS/{}/\"", System.getProperty("user.name"));
				}
				final File logDirectory = new File("/var/log/DMS/" + System.getProperty("user.name") + "/");
				if (!logDirectory.exists()) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("getDefaultLogFileFolder: Trying to create: \"{}\"", logDirectory.getAbsolutePath());
					}
					try {
						FileUtils.forceMkdir(logDirectory);
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("getDefaultLogFileFolder: \"{}\" created", logDirectory.getAbsolutePath());
						}
					} catch (IOException e) {
						LOGGER.debug("Could not create \"{}\": {}", logDirectory.getAbsolutePath(), e.getMessage());
					}
				}
				defaultLogFileFolder = verifyLogFolder(logDirectory, "profile folder");
			}

			if (defaultLogFileFolder == null) {
				// Log to profile directory if it is writable.
				defaultLogFileFolder = verifyLogFolder(PROFILE_FOLDER.toFile(), "temporary folder");
			}

			if (defaultLogFileFolder == null) {
				// Try user-defined temporary folder or fall back to system temporary folder.
				try {
					defaultLogFileFolder = verifyLogFolder(getTempFolder(), "working folder");
				} catch (IOException e) {
					LOGGER.error("Could not determine default logfile folder, falling back to working directory: {}", e.getMessage());
					defaultLogFileFolder = "";
				}
			}
		}

		return defaultLogFileFolder;
	}

	public String getDefaultLogFileName() {
		String s = getString(KEY_LOGGING_LOGFILE_NAME, "debug.log");
		if (FileUtil.isValidFileName(s)) {
			return s;
		}
		return "debug.log";
	}

	public String getDefaultLogFilePath() {
		return FileUtil.appendPathSeparator(getDefaultLogFileFolder()) + getDefaultLogFileName();
	}

	public File getTempFolder() throws IOException {
		return tempFolder.getTempFolder();
	}

	public LogSystemInformationMode getLogSystemInformation() {
		LogSystemInformationMode defaultValue = LogSystemInformationMode.TRACE_ONLY;
		String value = getString(KEY_LOG_SYSTEM_INFO, defaultValue.toString());
		LogSystemInformationMode result = LogSystemInformationMode.typeOf(value);
		return result != null ? result : defaultValue;
	}

	/**
	 * @return {@code true} if custom program paths are supported, {@code false}
	 *         otherwise.
	 */
	public boolean isCustomProgramPathsSupported() {
		return programPaths instanceof ConfigurableProgramPaths;
	}

	/**
	 * Returns the configured {@link ProgramExecutableType} for the specified
	 * {@link Player}. Note that this can be different from the
	 * {@link Player#currentExecutableType} for the same {@link Player}.
	 *
	 * @param player the {@link Player} for which to get the configured
	 *            {@link ProgramExecutableType}.
	 * @return The configured {@link ProgramExecutableType}, the default
	 *         {@link ProgramExecutableType} if none is configured or
	 *         {@code null} if there is no default.
	 *
	 * @see Player#getCurrentExecutableType()
	 */
	@Nullable
	public ProgramExecutableType getPlayerExecutableType(@Nonnull Player player) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		ProgramExecutableType executableType = ProgramExecutableType.toProgramExecutableType(
			getString(player.getExecutableTypeKey(), null),
			player.getProgramInfo().getDefault()
		);

		// The default might also be null, in which case the current should be used.
		return executableType == null ? player.getCurrentExecutableType() : executableType;
	}

	/**
	 * Sets the configured {@link ProgramExecutableType} for the specified
	 * {@link Player}.
	 *
	 * @param player the {@link Player} for which to set the configured
	 *            {@link ProgramExecutableType}.
	 * @param executableType the {@link ProgramExecutableType} to set.
	 * @return {@code true} if a change was made, {@code false} otherwise.
	 */
	public boolean setPlayerExecutableType(@Nonnull Player player, @Nonnull ProgramExecutableType executableType) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		if (executableType == null) {
			throw new IllegalArgumentException("executableType cannot be null");
		}
		String key = player.getExecutableTypeKey();
		if (key != null) {
			String currentValue = configuration.getString(key);
			String newValue = executableType.toRootString();
			if (newValue.equals(currentValue)) {
				return false;
			}
			configuration.setProperty(key, newValue);
			player.determineCurrentExecutableType(executableType);
			return true;
		}
		return false;
	}

	/**
	 * Gets the configured {@link Path} for the specified {@link PlayerId}. The
	 * {@link Player} must be registered. No check for existence or search in
	 * the OS path is performed.
	 *
	 * @param playerId the {@link PlayerId} for the registered {@link Player}
	 *            whose configured {@link Path} to get.
	 * @return The configured {@link Path} or {@code null} if missing, blank or
	 *         invalid.
	 */
	@Nullable
	public Path getPlayerCustomPath(@Nullable PlayerId playerId) {
		if (playerId == null) {
			return null;
		}
		return getPlayerCustomPath(PlayerFactory.getPlayer(playerId, false, false));
	}

	/**
	 * Gets the configured {@link Path} for the specified {@link Player}. No
	 * check for existence or search in the OS path is performed.
	 *
	 * @param player the {@link Player} whose configured {@link Path} to get.
	 * @return The configured {@link Path} or {@code null} if missing, blank or
	 *         invalid.
	 */
	@Nullable
	public Path getPlayerCustomPath(@Nullable Player player) {
		if (
			player == null ||
			isBlank(player.getConfigurablePathKey()) ||
			!(programPaths instanceof ConfigurableProgramPaths)
		) {
			return null;
		}

		try {
			return ((ConfigurableProgramPaths) programPaths).getCustomProgramPath(player.getConfigurablePathKey());
		} catch (ConfigurationException e) {
			LOGGER.warn(
				"An invalid executable path is configured for transcoding engine {}. The path is being ignored: {}",
				player,
				e.getMessage()
			);
			LOGGER.trace("", e);
			return null;
		}
	}

	/**
	 * Sets the custom executable {@link Path} for the specified {@link Player}
	 * in the configuration.
	 * <p>
	 * <b>Note:</b> This isn't normally what you'd want. To change the
	 * {@link Path} <b>for the {@link Player} instance</b> in the same
	 * operation, use {@link Player#setCustomExecutablePath} instead.
	 *
	 * @param player the {@link Player} whose custom executable {@link Path} to
	 *            set.
	 * @param path the {@link Path} to set or {@code null} to clear.
	 * @return {@code true} if a change was made to the configuration,
	 *         {@code false} otherwise.
	 * @throws IllegalStateException If {@code player} has no configurable path
	 *             key or custom program paths aren't supported.
	 */
	public boolean setPlayerCustomPath(@Nonnull Player player, @Nullable Path path) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		if (isBlank(player.getConfigurablePathKey())) {
			throw new IllegalStateException(
				"Can't set custom executable path for player " + player + "because it has no configurable path key"
			);
		}
		if (!isCustomProgramPathsSupported()) {
			throw new IllegalStateException("The program paths aren't configurable");
		}
		return ((ConfigurableProgramPaths) programPaths).setCustomProgramPathConfiguration(
			path,
			player.getConfigurablePathKey()
		);
	}

	/**
	 * @return The {@link ExternalProgramInfo} for VLC.
	 */
	@Nullable
	public ExternalProgramInfo getVLCPaths() {
		return programPaths.getVLC();
	}

	/**
	 * @return The {@link ExternalProgramInfo} for MEncoder.
	 */
	@Nullable
	public ExternalProgramInfo getMEncoderPaths() {
		return programPaths.getMEncoder();
	}

	/**
	 * @return The {@link ExternalProgramInfo} for DCRaw.
	 */
	@Nullable
	public ExternalProgramInfo getDCRawPaths() {
		return programPaths.getDCRaw();
	}

	/**
	 * @return The {@link ExternalProgramInfo} for FFmpeg.
	 */
	@Nullable
	public ExternalProgramInfo getFFmpegPaths() {
		return programPaths.getFFmpeg();
	}

	/**
	 * @return The {@link ExternalProgramInfo} for MPlayer.
	 */
	@Nullable
	public ExternalProgramInfo getMPlayerPaths() {
		return programPaths.getMPlayer();
	}

	/**
	 * @return The configured path to the MPlayer executable. If none is
	 *         configured, the default is used.
	 */
	@Nullable
	public String getMPlayerPath() {
		ProgramExecutableType executableType = ProgramExecutableType.toProgramExecutableType(
			ConfigurableProgramPaths.KEY_MPLAYER_EXECUTABLE_TYPE,
			getMPlayerPaths().getDefault()
		);
		Path executable = null;
		if (executableType != null) {
			executable = getMPlayerPaths().getPath(executableType);
		}
		if (executable == null) {
			executable = getMPlayerPaths().getDefaultPath();
		}
		return executable == null ? null : executable.toString();
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for MPlayer
	 * both in {@link PmsConfiguration} and the {@link ExternalProgramInfo}.
	 *
	 * @param customPath the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomMPlayerPath(@Nullable Path customPath) {
		if (!isCustomProgramPathsSupported()) {
			throw new IllegalStateException("The program paths aren't configurable");
		}
		((ConfigurableProgramPaths) programPaths).setCustomMPlayerPath(customPath);
	}

	/**
	 * @return The {@link ExternalProgramInfo} for tsMuxeR.
	 */
	@Nullable
	public ExternalProgramInfo getTsMuxeRPaths() {
		return programPaths.getTsMuxeR();
	}

	/**
	 * @return The {@link ExternalProgramInfo} for FLAC.
	 */
	@Nullable
	public ExternalProgramInfo getFLACPaths() {
		return programPaths.getFLAC();
	}

	/**
	 * @return The configured path to the FLAC executable. If none is
	 *         configured, the default is used.
	 */
	@Nullable
	public String getFLACPath() {
		ProgramExecutableType executableType = ProgramExecutableType.toProgramExecutableType(
			ConfigurableProgramPaths.KEY_FLAC_EXECUTABLE_TYPE,
			getFLACPaths().getDefault()
		);
		Path executable = null;
		if (executableType != null) {
			executable = getFLACPaths().getPath(executableType);
		}
		if (executable == null) {
			executable = getFLACPaths().getDefaultPath();
		}
		return executable == null ? null : executable.toString();
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for FLAC
	 * both in {@link PmsConfiguration} and the {@link ExternalProgramInfo}.
	 *
	 * @param customPath the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomFlacPath(@Nullable Path customPath) {
		if (!isCustomProgramPathsSupported()) {
			throw new IllegalStateException("The program paths aren't configurable");
		}
		((ConfigurableProgramPaths) programPaths).setCustomFlacPath(customPath);
	}

	/**
	 * @return The {@link ExternalProgramInfo} for Interframe.
	 */
	@Nullable
	public ExternalProgramInfo getInterFramePaths() {
		return programPaths.getInterFrame();
	}

	/**
	 * @return The configured path to the Interframe folder. If none is
	 *         configured, the default is used.
	 */
	@Nullable
	public String getInterFramePath() {
		ProgramExecutableType executableType = ProgramExecutableType.toProgramExecutableType(
			ConfigurableProgramPaths.KEY_INTERFRAME_EXECUTABLE_TYPE,
			getInterFramePaths().getDefault()
		);
		Path executable = null;
		if (executableType != null) {
			executable = getInterFramePaths().getPath(executableType);
		}
		if (executable == null) {
			executable = getInterFramePaths().getDefaultPath();
		}
		return executable == null ? null : executable.toString();
	}

	/**
	 * Sets a new {@link ProgramExecutableType#CUSTOM} {@link Path} for
	 * Interframe both in {@link PmsConfiguration} and the
	 * {@link ExternalProgramInfo}.
	 *
	 * @param customPath the new {@link Path} or {@code null} to clear it.
	 */
	public void setCustomInterFramePath(@Nullable Path customPath) {
		if (!isCustomProgramPathsSupported()) {
			throw new IllegalStateException("The program paths aren't configurable");
		}
		((ConfigurableProgramPaths) programPaths).setCustomInterFramePath(customPath);
	}

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg. Default value is true.
	 * @return True if tsMuxeR should parse fps from FFmpeg.
	 */
	public boolean isTsmuxerForceFps() {
		return getBoolean(KEY_TSMUXER_FORCEFPS, true);
	}

	/**
	 * The AC-3 audio bitrate determines the quality of digital audio sound. An AV-receiver
	 * or amplifier has to be capable of playing this quality. Default value is 640.
	 * @return The AC-3 audio bitrate.
	 */
	public int getAudioBitrate() {
		return getInt(KEY_AUDIO_BITRATE, 640);
	}

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg.
	 * @param value Set to true if tsMuxeR should parse fps from FFmpeg.
	 */
	public void setTsmuxerForceFps(boolean value) {
		configuration.setProperty(KEY_TSMUXER_FORCEFPS, value);
	}

	/**
	 * The server port where DMS listens for TCP/IP traffic. Default value is 5252.
	 * @return The port number.
	 */
	public int getServerPort() {
		return getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
	}

	/**
	 * Set the server port where DMS must listen for TCP/IP traffic.
	 * @param value The TCP/IP port number.
	 */
	public void setServerPort(int value) {
		configuration.setProperty(KEY_SERVER_PORT, value);
	}

	/**
	 * The hostname of the server.
	 * @return The hostname if it is defined, otherwise <code>null</code>.
	 */
	public String getServerHostname() {
		return getString(KEY_SERVER_HOSTNAME, null);
	}

	/**
	 * Set the hostname of the server.
	 * @param value The hostname.
	 */
	public void setHostname(String value) {
		configuration.setProperty(KEY_SERVER_HOSTNAME, value);
	}

	public String getServerDisplayName() {
		String profileName = isAppendProfileName() ? getProfileName() : null;
		if (profileName != null) {
			return String.format("%s [%s]", getString(KEY_SERVER_NAME, PMS.getName()), profileName);
		}
		return getString(KEY_SERVER_NAME, PMS.getName());
	}
	/**
	 * The name of the server.
	 *
	 * @return The name of the server.
	 */
	public String getServerName() {
		return getString(KEY_SERVER_NAME, PMS.getName());
	}

	/**
	 * Set the name of the server.
	 *
	 * @param value The name.
	 */
	public void setServerName(String value) {
		configuration.setProperty(KEY_SERVER_NAME, value);
	}

	/**
	 * The TCP/IP port number for a proxy server. Default value is -1.
	 *
	 * @return The proxy port number.
	 */
	// no longer used
	@Deprecated
	public int getProxyServerPort() {
		return getInt(KEY_PROXY_SERVER_PORT, DEFAULT_PROXY_SERVER_PORT);
	}

	/**
	 * Gets the language {@link String} as stored in the {@link PmsConfiguration}.
	 * May return <code>null</code>.
	 * @return The language {@link String}
	 */
	public String getLanguageRawString() {
		return configuration.getString(KEY_LANGUAGE);
	}

	/**
	 * Gets the {@link java.util.Locale} of the preferred language for the DMS
	 * user interface. The default is based on the default (OS) locale.
	 * @param log determines if any issues should be logged.
	 * @return The {@link java.util.Locale}.
	 */
	public Locale getLanguageLocale(boolean log) {
		String languageCode = configuration.getString(KEY_LANGUAGE);
		Locale locale = null;
		if (languageCode != null && !languageCode.isEmpty()) {
			locale = Languages.toLocale(Locale.forLanguageTag(languageCode));
			if (log && locale == null) {
				LOGGER.error("Invalid or unsupported language tag \"{}\", defaulting to OS language.", languageCode);
			}
		} else if (log) {
			LOGGER.info("Language not specified, defaulting to OS language.");
		}

		if (locale == null) {
			locale = Languages.toLocale(Locale.getDefault());
			if (log && locale == null) {
				LOGGER.error("Unsupported language tag \"{}\", defaulting to US English.", Locale.getDefault().toLanguageTag());
			}
		}

		if (locale == null) {
			locale = Locale.forLanguageTag("en-US"); // Default
		}
		return locale;
	}

	/**
	 * Gets the {@link java.util.Locale} of the preferred language for the DMS
	 * user interface. The default is based on the default (OS) locale. Doesn't
	 * log potential issues.
	 * @return The {@link java.util.Locale}.
	 */
	public Locale getLanguageLocale() {
		return getLanguageLocale(false);
	}

	/**
	 * Gets the {@link java.util.Locale} compatible tag of the preferred
	 * language for the DMS user interface. The default is based on the default (OS) locale.
	 * @return The <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IEFT BCP 47</a> language tag.
	 */
	public String getLanguageTag() {
		return getLanguageLocale().toLanguageTag();
	}

	/**
	 * @deprecated Use {@link #getLanguageTag} or {@link #getLanguageLocale} instead
	 * @since 5.2.3
	 */
	@Deprecated
	public String getLanguage() {
		return getLanguageTag();
	}

	/**
	 * Sets the preferred language for the DMS user interface.
	 * @param locale The {@link java.net.Locale}.
	 */
	public void setLanguage(Locale locale) {
		if (locale != null) {
			if (Languages.isValid(locale)) {
				configuration.setProperty(KEY_LANGUAGE, Languages.toLanguageTag(locale));
				PMS.setLocale(Languages.toLocale(locale));
				//TODO: The line below should be removed once all calls to Locale.getDefault() is replaced with PMS.getLocale()
				Locale.setDefault(Languages.toLocale(locale));
			} else {
				LOGGER.error("setLanguage() aborted because of unsupported language tag \"{}\"", locale.toLanguageTag());
			}
		} else {
			configuration.setProperty(KEY_LANGUAGE, "");
		}
	}

	/**
	 * Sets the preferred language for the DMS user interface.
	 * @param value The <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IEFT BCP 47</a> language tag.
	 */
	public void setLanguage(String value) {
		if (value != null && !value.isEmpty()) {
			setLanguage(Locale.forLanguageTag(value));
		} else {
			LOGGER.error("setLanguage() aborted because language tag is empty");
		}
	}

	/**
	 * Returns the preferred minimum size for the transcoding memory buffer in megabytes.
	 * Default value is 12.
	 * @return The minimum memory buffer size.
	 */
	public int getMinMemoryBufferSize() {
		return getInt(KEY_MIN_MEMORY_BUFFER_SIZE, 12);
	}

	/**
	 * Returns the preferred maximum size for the transcoding memory buffer in megabytes.
	 * The value returned has a top limit of {@link #MAX_MAX_MEMORY_BUFFER_SIZE}. Default
	 * value is 200.
	 *
	 * @return The maximum memory buffer size.
	 */
	public int getMaxMemoryBufferSize() {
		return Math.max(0, Math.min(MAX_MAX_MEMORY_BUFFER_SIZE, getInt(KEY_MAX_MEMORY_BUFFER_SIZE, 200)));
	}

	/**
	 * Set the preferred maximum for the transcoding memory buffer in megabytes. The top
	 * limit for the value is {@link #MAX_MAX_MEMORY_BUFFER_SIZE}.
	 *
	 * @param value The maximum buffer size.
	 */
	public void setMaxMemoryBufferSize(int value) {
		configuration.setProperty(KEY_MAX_MEMORY_BUFFER_SIZE, Math.max(0, Math.min(MAX_MAX_MEMORY_BUFFER_SIZE, value)));
	}

	/**
	 * Returns the font scale used for ASS subtitling. Default value is 1.4.
	 * @return The ASS font scale.
	 */
	public String getAssScale() {
		return getString(KEY_ASS_SCALE, "1.4");
	}

	/**
	 * Some versions of MEncoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec. Returns true if "ac3_fixed" should be used.
	 * Default is false.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @return True if "ac3_fixed" should be used.
	 */
	public boolean isMencoderAc3Fixed() {
		return getBoolean(KEY_MENCODER_AC3_FIXED, false);
	}

	/**
	 * Returns the margin used for ASS subtitling. Default value is 10.
	 * @return The ASS margin.
	 */
	public String getAssMargin() {
		return getString(KEY_ASS_MARGIN, "10");
	}

	/**
	 * Returns the outline parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS outline parameter.
	 */
	public String getAssOutline() {
		return getString(KEY_ASS_OUTLINE, "1");
	}

	/**
	 * Returns the shadow parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS shadow parameter.
	 */
	public String getAssShadow() {
		return getString(KEY_ASS_SHADOW, "1");
	}

	/**
	 * Returns the subfont text scale parameter used for subtitling without ASS.
	 * Default value is 3.
	 * @return The subfont text scale parameter.
	 */
	public String getMencoderNoAssScale() {
		return getString(KEY_MENCODER_NOASS_SCALE, "3");
	}

	/**
	 * Returns the subpos parameter used for subtitling without ASS.
	 * Default value is 2.
	 * @return The subpos parameter.
	 */
	public String getMencoderNoAssSubPos() {
		return getString(KEY_MENCODER_NOASS_SUBPOS, "2");
	}

	/**
	 * Returns the subfont blur parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont blur parameter.
	 */
	public String getMencoderNoAssBlur() {
		return getString(KEY_MENCODER_NOASS_BLUR, "1");
	}

	/**
	 * Returns the subfont outline parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont outline parameter.
	 */
	public String getMencoderNoAssOutline() {
		return getString(KEY_MENCODER_NOASS_OUTLINE, "1");
	}

	/**
	 * Set the subfont outline parameter used for subtitling without ASS.
	 * @param value The subfont outline parameter value to set.
	 */
	public void setMencoderNoAssOutline(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_OUTLINE, value);
	}

	/**
	 * Some versions of MEncoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @param value Set to true if "ac3_fixed" should be used.
	 */
	public void setMencoderAc3Fixed(boolean value) {
		configuration.setProperty(KEY_MENCODER_AC3_FIXED, value);
	}

	/**
	 * Set the margin used for ASS subtitling.
	 * @param value The ASS margin value to set.
	 */
	public void setAssMargin(String value) {
		configuration.setProperty(KEY_ASS_MARGIN, value);
	}

	/**
	 * Set the outline parameter used for ASS subtitling.
	 * @param value The ASS outline parameter value to set.
	 */
	public void setAssOutline(String value) {
		configuration.setProperty(KEY_ASS_OUTLINE, value);
	}

	/**
	 * Set the shadow parameter used for ASS subtitling.
	 * @param value The ASS shadow parameter value to set.
	 */
	public void setAssShadow(String value) {
		configuration.setProperty(KEY_ASS_SHADOW, value);
	}

	/**
	 * Set the font scale used for ASS subtitling.
	 * @param value The ASS font scale value to set.
	 */
	public void setAssScale(String value) {
		configuration.setProperty(KEY_ASS_SCALE, value);
	}

	/**
	 * Set the subfont text scale parameter used for subtitling without ASS.
	 * @param value The subfont text scale parameter value to set.
	 */
	public void setMencoderNoAssScale(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SCALE, value);
	}

	/**
	 * Set the subfont blur parameter used for subtitling without ASS.
	 * @param value The subfont blur parameter value to set.
	 */
	public void setMencoderNoAssBlur(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_BLUR, value);
	}

	/**
	 * Set the subpos parameter used for subtitling without ASS.
	 * @param value The subpos parameter value to set.
	 */
	public void setMencoderNoAssSubPos(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SUBPOS, value);
	}

	/**
	 * Returns the number of seconds from the start of a video file (the seek
	 * position) where the thumbnail image for the movie should be extracted
	 * from. Default is 4 seconds.
	 *
	 * @return The seek position in seconds.
	 */
	public int getThumbnailSeekPos() {
		return getInt(KEY_THUMBNAIL_SEEK_POS, 4);
	}

	/**
	 * Sets the number of seconds from the start of a video file (the seek
	 * position) where the thumbnail image for the movie should be extracted
	 * from.
	 *
	 * @param value The seek position in seconds.
	 */
	public void setThumbnailSeekPos(int value) {
		configuration.setProperty(KEY_THUMBNAIL_SEEK_POS, value);
	}

	/**
	 * Returns whether the user wants ASS/SSA subtitle support. Default is
	 * true.
	 *
	 * @return True if MEncoder should use ASS/SSA support.
	 */
	public boolean isMencoderAss() {
		return getBoolean(KEY_MENCODER_ASS, true);
	}

	/**
	 * Returns whether or not subtitles should be disabled for all
	 * transcoding engines. Default is false, meaning subtitles should not
	 * be disabled.
	 *
	 * @return True if subtitles should be disabled, false otherwise.
	 */
	public boolean isDisableSubtitles() {
		return getBoolean(KEY_DISABLE_SUBTITLES, false);
	}

	/**
	 * Set whether or not subtitles should be disabled for
	 * all transcoding engines.
	 *
	 * @param value Set to true if subtitles should be disabled.
	 */
	public void setDisableSubtitles(boolean value) {
		configuration.setProperty(KEY_DISABLE_SUBTITLES, value);
	}

	/**
	 * Returns whether or not the Pulse Code Modulation audio format should be
	 * forced. The default is false.
	 * @return True if PCM should be forced, false otherwise.
	 */
	public boolean isAudioUsePCM() {
		return getBoolean(KEY_AUDIO_USE_PCM, false);
	}

	/**
	 * Returns whether or not the Pulse Code Modulation audio format should be
	 * used only for HQ audio codecs. The default is false.
	 * @return True if PCM should be used only for HQ audio codecs, false otherwise.
	 */
	public boolean isMencoderUsePcmForHQAudioOnly() {
		return getBoolean(KEY_MENCODER_USE_PCM_FOR_HQ_AUDIO_ONLY, false);
	}

	/**
	 * Returns the name of a TrueType font to use for subtitles.
	 * Default is <code>""</code>.
	 * @return The font name.
	 */
	public String getFont() {
		return getString(KEY_FONT, "");
	}

	/**
	 * Returns the audio language priority as a comma separated
	 * string. For example: <code>"eng,fre,jpn,ger,und"</code>, where "und"
	 * stands for "undefined".
	 * Can be a blank string.
	 * Default value is "loc,eng,fre,jpn,ger,und".
	 *
	 * @return The audio language priority string.
	 */
	public String getAudioLanguages() {
		String result = getString(KEY_AUDIO_LANGUAGES, null);
		return (isBlank(result)) ? Messages.getString("MEncoderVideo.126") : result;
	}

	/**
	 * Returns the subtitle language priority as a comma-separated
	 * string. For example: <code>"eng,fre,jpn,ger,und"</code>, where "und"
	 * stands for "undefined".
	 * Can be a blank string.
	 * Default value is a localized list (e.g. "eng,fre,jpn,ger,und").
	 *
	 * @return The subtitle language priority string.
	 */
	public String getSubtitlesLanguages() {
		String result = getString(KEY_SUBTITLES_LANGUAGES, null);
		return (isBlank(result)) ? Messages.getString("MEncoderVideo.127") : result;
	}

	/**
	 * Returns the ISO 639 language code for the subtitle language that should
	 * be forced.
	 * Can be a blank string.
	 * @return The subtitle language code.
	 */
	public String getForcedSubtitleLanguage() {
		return configurationReader.getPossiblyBlankConfigurationString(
				KEY_FORCED_SUBTITLE_LANGUAGE,
				PMS.getLocale().getLanguage()
		);
	}

	/**
	 * Returns the tag string that identifies the subtitle language that
	 * should be forced.
	 * @return The tag string.
	 */
	public String getForcedSubtitleTags() {
		return getString(KEY_FORCED_SUBTITLE_TAGS, "forced");
	}

	/**
	 * Returns a string of audio language and subtitle language pairs
	 * ordered by priority to try to match. Audio language
	 * and subtitle language should be comma-separated as a pair,
	 * individual pairs should be semicolon separated. "*" can be used to
	 * match any language. Subtitle language can be defined as "off".
	 * Default value is <code>"*,*"</code>.
	 *
	 * @return The audio and subtitle languages priority string.
	 */
	public String getAudioSubLanguages() {
		String result = getString(KEY_AUDIO_SUB_LANGS, null);
		return (isBlank(result)) ? Messages.getString("MEncoderVideo.128") : result;
	}

	/**
	 * Sets a string of audio language and subtitle language pairs
	 * ordered by priority to try to match. Audio language
	 * and subtitle language should be comma-separated as a pair,
	 * individual pairs should be semicolon separated. "*" can be used to
	 * match any language. Subtitle language can be defined as "off".
	 *
	 * Example: <code>"en,off;jpn,eng;*,eng;*;*"</code>.
	 *
	 * @param value The audio and subtitle languages priority string.
	 */
	public void setAudioSubLanguages(String value) {
		configuration.setProperty(KEY_AUDIO_SUB_LANGS, value);
	}

	/**
	 * Returns whether or not MEncoder should use FriBiDi mode, which
	 * is needed to display subtitles in languages that read from right to
	 * left, like Arabic, Farsi, Hebrew, Urdu, etc. Default value is false.
	 *
	 * @return True if FriBiDi mode should be used, false otherwise.
	 */
	public boolean isMencoderSubFribidi() {
		return getBoolean(KEY_MENCODER_SUB_FRIBIDI, false);
	}

	/**
	 * Returns the character encoding (or code page) that should used
	 * for displaying non-Unicode external subtitles. Default is empty string
	 * (do not force encoding with -subcp key).
	 *
	 * @return The character encoding.
	 */
	public String getSubtitlesCodepage() {
		return getString(KEY_SUBTITLES_CODEPAGE, "");
	}

	/**
	 * Whether MEncoder should use fontconfig for displaying subtitles.
	 *
	 * @return True if fontconfig should be used, false otherwise.
	 */
	public boolean isMencoderFontConfig() {
		return getBoolean(KEY_MENCODER_FONT_CONFIG, true);
	}

	/**
	 * Set to true if MEncoder should be forced to use the framerate that is
	 * parsed by FFmpeg.
	 *
	 * @param value Set to true if the framerate should be forced, false
	 *              otherwise.
	 */
	public void setMencoderForceFps(boolean value) {
		configuration.setProperty(KEY_MENCODER_FORCE_FPS, value);
	}

	/**
	 * Whether MEncoder should be forced to use the framerate that is
	 * parsed by FFmpeg.
	 *
	 * @return True if the framerate should be forced, false otherwise.
	 */
	public boolean isMencoderForceFps() {
		return getBoolean(KEY_MENCODER_FORCE_FPS, false);
	}

	/**
	 * Sets the audio language priority as a comma separated
	 * string. For example: <code>"eng,fre,jpn,ger,und"</code>, where "und"
	 * stands for "undefined".
	 * @param value The audio language priority string.
	 */
	public void setAudioLanguages(String value) {
		configuration.setProperty(KEY_AUDIO_LANGUAGES, value);
	}

	/**
	 * Sets the subtitle language priority as a comma-separated string.
	 *
	 * Example: <code>"eng,fre,jpn,ger,und"</code>, where "und" stands for
	 * "undefined".
	 *
	 * @param value The subtitle language priority string.
	 */
	public void setSubtitlesLanguages(String value) {
		configuration.setProperty(KEY_SUBTITLES_LANGUAGES, value);
	}

	/**
	 * Sets the ISO 639 language code for the subtitle language that should
	 * be forced.
	 *
	 * @param value The subtitle language code.
	 */
	public void setForcedSubtitleLanguage(String value) {
		configuration.setProperty(KEY_FORCED_SUBTITLE_LANGUAGE, value);
	}

	/**
	 * Sets the tag string that identifies the subtitle language that
	 * should be forced.
	 *
	 * @param value The tag string.
	 */
	public void setForcedSubtitleTags(String value) {
		configuration.setProperty(KEY_FORCED_SUBTITLE_TAGS, value);
	}

	/**
	 * Returns custom commandline options to pass on to MEncoder.
	 *
	 * @return The custom options string.
	 */
	public String getMencoderCustomOptions() {
		return getString(KEY_MENCODER_CUSTOM_OPTIONS, "");
	}

	/**
	 * Sets custom commandline options to pass on to MEncoder.
	 *
	 * @param value The custom options string.
	 */
	public void setMencoderCustomOptions(String value) {
		configuration.setProperty(KEY_MENCODER_CUSTOM_OPTIONS, value);
	}

	/**
	 * Sets the character encoding (or code page) that should be used
	 * for displaying non-Unicode external subtitles. Default is empty (autodetect).
	 *
	 * @param value The character encoding.
	 */
	public void setSubtitlesCodepage(String value) {
		configuration.setProperty(KEY_SUBTITLES_CODEPAGE, value);
	}

	/**
	 * Sets whether or not MEncoder should use FriBiDi mode, which
	 * is needed to display subtitles in languages that read from right to
	 * left, like Arabic, Farsi, Hebrew, Urdu, etc. Default value is false.
	 *
	 * @param value Set to true if FriBiDi mode should be used.
	 */
	public void setMencoderSubFribidi(boolean value) {
		configuration.setProperty(KEY_MENCODER_SUB_FRIBIDI, value);
	}

	/**
	 * Sets the name of a TrueType font to use for subtitles.
	 *
	 * @param value The font name.
	 */
	public void setFont(String value) {
		configuration.setProperty(KEY_FONT, value);
	}

	/**
	 * Older versions of MEncoder do not support ASS/SSA subtitles on all
	 * platforms. Set to true if MEncoder supports them. Default should be
	 * true on Windows and OS X, false otherwise.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1097
	 *
	 * @param value Set to true if MEncoder supports ASS/SSA subtitles.
	 */
	public void setMencoderAss(boolean value) {
		configuration.setProperty(KEY_MENCODER_ASS, value);
	}

	/**
	 * Sets whether or not MEncoder should use fontconfig for displaying
	 * subtitles.
	 *
	 * @param value Set to true if fontconfig should be used.
	 */
	public void setMencoderFontConfig(boolean value) {
		configuration.setProperty(KEY_MENCODER_FONT_CONFIG, value);
	}

	/**
	 * Sets whether or not the Pulse Code Modulation audio format should be
	 * forced.
	 *
	 * @param value Set to true if PCM should be forced.
	 */
	public void setAudioUsePCM(boolean value) {
		configuration.setProperty(KEY_AUDIO_USE_PCM, value);
	}

	/**
	 * Sets whether or not the Pulse Code Modulation audio format should be
	 * used only for HQ audio codecs.
	 *
	 * @param value Set to true if PCM should be used only for HQ audio.
	 */
	public void setMencoderUsePcmForHQAudioOnly(boolean value) {
		configuration.setProperty(KEY_MENCODER_USE_PCM_FOR_HQ_AUDIO_ONLY, value);
	}

	/**
	 * Whether archives (e.g. .zip or .rar) should be browsable.
	 *
	 * @return True if archives should be browsable.
	 */
	public boolean isArchiveBrowsing() {
		return getBoolean(KEY_OPEN_ARCHIVES, false);
	}

	/**
	 * Sets whether archives (e.g. .zip or .rar) should be browsable.
	 *
	 * @param value Set to true if archives should be browsable.
	 */
	public void setArchiveBrowsing(boolean value) {
		configuration.setProperty(KEY_OPEN_ARCHIVES, value);
	}

	/**
	 * Returns true if MEncoder should use the deinterlace filter, false
	 * otherwise.
	 *
	 * @return True if the deinterlace filter should be used.
	 */
	public boolean isMencoderYadif() {
		return getBoolean(KEY_MENCODER_YADIF, false);
	}

	/**
	 * Set to true if MEncoder should use the deinterlace filter, false
	 * otherwise.
	 *
	 * @param value Set ot true if the deinterlace filter should be used.
	 */
	public void setMencoderYadif(boolean value) {
		configuration.setProperty(KEY_MENCODER_YADIF, value);
	}

	/**
	 * Whether MEncoder should be used to upscale the video to an
	 * optimal resolution. Default value is false, meaning the renderer will
	 * upscale the video itself.
	 *
	 * @return True if MEncoder should be used, false otherwise.
	 * @see #getMencoderScaleX()
	 * @see #getMencoderScaleY()
	 */
	public boolean isMencoderScaler() {
		return getBoolean(KEY_MENCODER_SCALER, false);
	}

	/**
	 * Set to true if MEncoder should be used to upscale the video to an
	 * optimal resolution. Set to false to leave upscaling to the renderer.
	 *
	 * @param value Set to true if MEncoder should be used to upscale.
	 * @see #setMencoderScaleX(int)
	 * @see #setMencoderScaleY(int)
	 */
	public void setMencoderScaler(boolean value) {
		configuration.setProperty(KEY_MENCODER_SCALER, value);
	}

	/**
	 * Returns the width in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @return The width in pixels.
	 */
	public int getMencoderScaleX() {
		return getInt(KEY_MENCODER_SCALEX, 0);
	}

	/**
	 * Sets the width in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @param value The width in pixels.
	 */
	public void setMencoderScaleX(int value) {
		configuration.setProperty(KEY_MENCODER_SCALEX, value);
	}

	/**
	 * Returns the height in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @return The height in pixels.
	 */
	public int getMencoderScaleY() {
		return getInt(KEY_MENCODER_SCALEY, 0);
	}

	/**
	 * Sets the height in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @param value The height in pixels.
	 */
	public void setMencoderScaleY(int value) {
		configuration.setProperty(KEY_MENCODER_SCALEY, value);
	}

	/**
	 * Returns the number of audio channels that should be used for
	 * transcoding. Default value is 6 (for 5.1 audio).
	 *
	 * @return The number of audio channels.
	 */
	public int getAudioChannelCount() {
		int valueFromUserConfig = getInt(KEY_AUDIO_CHANNEL_COUNT, 6);

		if (valueFromUserConfig != 6 && valueFromUserConfig != 2) {
			return 6;
		}

		return valueFromUserConfig;
	}

	/**
	 * Sets the number of audio channels that MEncoder should use for
	 * transcoding.
	 *
	 * @param value The number of audio channels.
	 */
	public void setAudioChannelCount(int value) {
		if (value != 6 && value != 2) {
			value = 6;
		}
		configuration.setProperty(KEY_AUDIO_CHANNEL_COUNT, value);
	}

	/**
	 * Sets the AC3 audio bitrate, which determines the quality of digital
	 * audio sound. An AV-receiver or amplifier has to be capable of playing
	 * this quality.
	 *
	 * @param value The AC3 audio bitrate.
	 */
	public void setAudioBitrate(int value) {
		configuration.setProperty(KEY_AUDIO_BITRATE, value);
	}

	/**
	 * Returns the maximum video bitrate to be used by MEncoder and FFmpeg.
	 *
	 * @return The maximum video bitrate.
	 */
	public String getMaximumBitrate() {
		String maximumBitrate = getMaximumBitrateDisplay();
		if ("0".equals(maximumBitrate)) {
			maximumBitrate = "1000";
		}
		return maximumBitrate;
	}

	/**
	 * The same as getMaximumBitrate() but this value is displayed to the user
	 * because for our own uses we turn the value "0" into the value "1000" but
	 * that can be confusing for the user.
	 *
	 * @return The maximum video bitrate to display in the GUI.
	 */
	public String getMaximumBitrateDisplay() {
		return getString(KEY_MAX_BITRATE, "90");
	}

	/**
	 * Sets the maximum video bitrate to be used by MEncoder.
	 *
	 * @param value The maximum video bitrate.
	 */
	public void setMaximumBitrate(String value) {
		configuration.setProperty(KEY_MAX_BITRATE, value);
	}

	/**
	 * @return The selected renderers as a list.
	 */
	public List<String> getSelectedRenderers() {
		return getStringList(KEY_SELECTED_RENDERERS, ALL_RENDERERS);
	}

	/**
	 * @param value The comma-separated list of selected renderers.
	 * @return {@code true} if this call changed the {@link Configuration},
	 *         {@code false} otherwise.
	 */
	public boolean setSelectedRenderers(String value) {
		if (value.isEmpty()) {
			value = "None";
		}
		if (!value.equals(configuration.getString(KEY_SELECTED_RENDERERS, null))) {
			configuration.setProperty(KEY_SELECTED_RENDERERS, value);
			return true;
		}
		return false;
	}

	/**
	 * @param value a string list of renderers.
	 * @return {@code true} if this call changed the {@link Configuration},
	 *         {@code false} otherwise.
	 */
	public boolean setSelectedRenderers(List<String> value) {
		if (value == null) {
			return setSelectedRenderers("");
		}
		List<String> currentValue = getStringList(KEY_SELECTED_RENDERERS, null);
		if (currentValue == null || value.size() != currentValue.size() || !value.containsAll(currentValue)) {
			setStringList(KEY_SELECTED_RENDERERS, value);
			return true;
		}
		return false;
	}

	/**
	 * Returns true if thumbnail generation is enabled, false otherwise.
	 *
	 * @return boolean indicating whether thumbnail generation is enabled.
	 */
	public boolean isThumbnailGenerationEnabled() {
		return getBoolean(KEY_THUMBNAIL_GENERATION_ENABLED, true);
	}

	/**
	 * Sets the thumbnail generation option.
	 */
	public void setThumbnailGenerationEnabled(boolean value) {
		configuration.setProperty(KEY_THUMBNAIL_GENERATION_ENABLED, value);
	}

	/**
	 * Returns true if DMS should generate thumbnails for images. Default value
	 * is true.
	 *
	 * @return True if image thumbnails should be generated.
	 */
	public boolean getImageThumbnailsEnabled() {
		return getBoolean(KEY_IMAGE_THUMBNAILS_ENABLED, true);
	}

	/**
	 * Set to true if DMS should generate thumbnails for images.
	 *
	 * @param value True if image thumbnails should be generated.
	 */
	public void setImageThumbnailsEnabled(boolean value) {
		configuration.setProperty(KEY_IMAGE_THUMBNAILS_ENABLED, value);
	}

	/**
	 * Returns {@code true} if DMS should start hidden, i.e. without the GUI
	 * visible.
	 *
	 * @return {@code true} if DMS should start hidden, {@code false} otherwise.
	 */
	public boolean isGUIStartHidden() {
		return getBoolean(KEY_GUI_START_HIDDEN, false);
	}

	/**
	 * Sets whether DMS should start with the GUI hidden or not.
	 *
	 * @param value {@code true} if DMS should start hidden, {@code false}
	 *            otherwise.
	 */
	public void setGUIStartHidden(boolean value) {
		configuration.setProperty(KEY_GUI_START_HIDDEN, value);
	}

	/**
	 * Returns {@code true}e if DMS should start on user login and {@code false}
	 * if not.
	 *
	 * @return {@code true} if DMS should start on user login, {@code false}
	 *         otherwise.
	 */
	public boolean isAutoStart() {
		if (Platform.isWindows()) {
			Path startupLink = ((WindowsSystemUtils) BasicSystemUtils.INSTANCE).getCurrentUserKnownFolderPath(
				KnownFolders.FOLDERID_Startup,
				CSIDL.CSIDL_STARTUP
			);
			if (startupLink == null) {
				return false;
			}
			startupLink = startupLink.resolve("Digital Media Server.lnk");
			return Files.exists(startupLink);
		}

		return false;
	}

	/**
	 * Set to true if DMS should start on user login.
	 *
	 * @param value {@code true} if DMS should start on user login,
	 *            {@code false} otherwise.
	 * @throws IOException If an error occurs during the operation.
	 */
	public void setAutoStart(boolean value) throws IOException {
		Path startupLink = ((WindowsSystemUtils) BasicSystemUtils.INSTANCE).getCurrentUserKnownFolderPath(
			KnownFolders.FOLDERID_Startup,
			CSIDL.CSIDL_STARTUP
		);

		if (startupLink == null) {
			throw new IOException("Couldn't resolve the startup folder for the current user");
		}
		startupLink = startupLink.resolve("Digital Media Server.lnk");

		if (value) {
			// Enable
			if (Files.exists(startupLink)) {
				LOGGER.debug("Start on user login is already active");
				return;
			}

			Path source = ((WindowsSystemUtils) BasicSystemUtils.INSTANCE).getCurrentUserKnownFolderPath(
				KnownFolders.FOLDERID_CommonPrograms,
				CSIDL.CSIDL_COMMON_PROGRAMS
			);
			if (source != null) {
				source = source.resolve("Digital Media Server\\Digital Media Server.lnk");
			}

			if (source == null || !Files.exists(source)) {
				throw new IOException(
					"Can't make Digital Media Server start on user login " +
					"because the Digital Media Server shortcut can't be found"
				);
			}

			Files.copy(source, startupLink);
			if (Files.exists(startupLink)) {
				LOGGER.info("Digital Media Server will start on user login");
			} else {
				throw new IOException("An error occurred while trying to make Digital Media Server start on user login");
			}
		} else {
			// Disable
			if (!Files.exists(startupLink)) {
				LOGGER.debug("Start on user login is already inactive");
				return;
			}

			Files.delete(startupLink);
			if (!Files.exists(startupLink)) {
				LOGGER.info("Digital Media Server will no longer start on user login");
			} else {
				throw new IOException(
					"An error occurred while trying to make Digital Media Server not start on user login"
				);
			}
		}
	}

	/**
	 * Whether we should check for external subtitle files with the same
	 * name as the media (*.srt, *.sub, *.ass, etc.).
	 *
	 * Note: This will return true if either the autoload external subtitles
	 * setting is enabled or the force external subtitles setting is enabled
	 *
	 * @return Whether we should check for external subtitle files.
	 */
	public boolean isAutoloadExternalSubtitles() {
		return getBoolean(KEY_AUTOLOAD_SUBTITLES, true) || isForceExternalSubtitles();
	}

	/**
	 * Whether we should check for external subtitle files with the same
	 * name as the media (*.srt, *.sub, *.ass etc.).
	 *
	 * @param value Whether we should check for external subtitle files.
	 */
	public void setAutoloadExternalSubtitles(boolean value) {
		configuration.setProperty(KEY_AUTOLOAD_SUBTITLES, value);
	}

	/**
	 * Whether we should force external subtitles with the same name as the
	 * media (*.srt, *.sub, *.ass, etc.) to display, regardless of whether
	 * language preferences disable them.
	 *
	 * @return Whether we should force external subtitle files.
	 */
	public boolean isForceExternalSubtitles() {
		return getBoolean(KEY_FORCE_EXTERNAL_SUBTITLES, true);
	}

	/**
	 * Whether we should force external subtitles with the same name as the
	 * media (*.srt, *.sub, *.ass, etc.) to display, regardless of whether
	 * language preferences disable them.
	 *
	 * @param value Whether we should force external subtitle files.
	 */
	public void setForceExternalSubtitles(boolean value) {
		configuration.setProperty(KEY_FORCE_EXTERNAL_SUBTITLES, value);
	}

	/**
	 * Returns true if DMS should hide the "# Videosettings #" folder on the
	 * DLNA device. The default value is false: DMS will display the folder.
	 *
	 * @return True if DMS should hide the folder, false othewise.
	 */
	public boolean getHideVideoSettings() {
		return getBoolean(KEY_HIDE_VIDEO_SETTINGS, true);
	}

	/**
	 * Set to true if DMS should hide the "# Videosettings #" folder on the
	 * DLNA device, or set to false to make DMS display the folder.
	 *
	 * @param value True if DMS should hide the folder.
	 */
	public void setHideVideoSettings(boolean value) {
		configuration.setProperty(KEY_HIDE_VIDEO_SETTINGS, value);
	}

	/**
	 * Gets the {@link FullyPlayedAction}.
	 *
	 * @return What to do with a file after it has been fully played
	 */
	public FullyPlayedAction getFullyPlayedAction() {
		return FullyPlayedAction.toFullyPlayedAction(getInt(KEY_FULLY_PLAYED_ACTION, 1));
	}

	/**
	 * Sets the {@link FullyPlayedAction}.
	 *
	 * @param action what to do with a file after it has been fully played
	 */
	public void setFullyPlayedAction(FullyPlayedAction action) {
		configuration.setProperty(KEY_FULLY_PLAYED_ACTION, action.toInt());
	}

	/**
	 * Returns the folder to move fully played files to.
	 *
	 * @see #getFullyPlayedAction()
	 * @return The folder to move fully played files to
	 */
	public String getFullyPlayedOutputDirectory() {
		return getString(KEY_FULLY_PLAYED_OUTPUT_DIRECTORY, "");
	}

	/**
	 * Sets the folder to move fully played files to.
	 *
	 * @see #getFullyPlayedAction()
	 * @param value the folder to move fully played files to
	 */
	public void setFullyPlayedOutputDirectory(String value) {
		configuration.setProperty(KEY_FULLY_PLAYED_OUTPUT_DIRECTORY, value);
	}

	/**
	 * Returns true if DMS should cache scanned media in its internal database,
	 * speeding up later retrieval. When false is returned, DMS will not use
	 * cache and media will have to be rescanned.
	 *
	 * @return True if DMS should cache media.
	 */
	public boolean getUseCache() {
		return getBoolean(KEY_USE_CACHE, true);
	}

	/**
	 * Set to true if DMS should cache scanned media in its internal database,
	 * speeding up later retrieval.
	 *
	 * @param value True if DMS should cache media.
	 */
	public void setUseCache(boolean value) {
		configuration.setProperty(KEY_USE_CACHE, value);
	}

	/**
	 * Whether we should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @param value True if we should pass the flag.
	 */
	public void setAvisynthConvertFps(boolean value) {
		configuration.setProperty(KEY_AVISYNTH_CONVERT_FPS, value);
	}

	/**
	 * Returns true if we should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @return True if we should pass the flag.
	 */
	public boolean getAvisynthConvertFps() {
		return getBoolean(KEY_AVISYNTH_CONVERT_FPS, true);
	}

	public void setAvisynthInterFrame(boolean value) {
		configuration.setProperty(KEY_AVISYNTH_INTERFRAME, value);
	}

	public boolean getAvisynthInterFrame() {
		return getBoolean(KEY_AVISYNTH_INTERFRAME, false);
	}

	public void setMEncoderAviSynthInterFrameGPU(boolean value) {
		configuration.setProperty(KEY_MENCODER_AVISYNTH_INTERFRAME_GPU, value);
	}

	public boolean isMEncoderAviSynthInterFrameGPU() {
		return getBoolean(KEY_MENCODER_AVISYNTH_INTERFRAME_GPU, false);
	}

	public int getMEncoderAviSynthMaxThreads() {
		return getInt(KEY_MENCODER_AVISYNTH_MAX_THREADS, 1);
	}

	public void setMEncoderAviSynthMaxThreads(int value) {
		configuration.setProperty(KEY_MENCODER_AVISYNTH_MAX_THREADS, value);
	}

	public int getMEncoderAviSynthEffectiveMaxThreads() {
		return Math.max(Math.min(getMEncoderAviSynthMaxThreads(), Runtime.getRuntime().availableProcessors()), 1);
	}

	/**
	 * Returns the template for the AviSynth script. The script string can
	 * contain the character "\u0001", which should be treated as the newline
	 * separator character.
	 *
	 * @return The AviSynth script template.
	 */
	public String getAvisynthScript() {
		return getString(KEY_AVISYNTH_SCRIPT, DEFAULT_AVI_SYNTH_SCRIPT);
	}

	/**
	 * Sets the template for the AviSynth script. The script string may contain
	 * the character "\u0001", which will be treated as newline character.
	 *
	 * @param value The AviSynth script template.
	 */
	public void setAvisynthScript(String value) {
		configuration.setProperty(KEY_AVISYNTH_SCRIPT, value);
	}

	/**
	 * Returns additional codec specific configuration options for MEncoder.
	 *
	 * @return The configuration options.
	 */
	public String getMencoderCodecSpecificConfig() {
		return getString(KEY_MENCODER_CODEC_SPECIFIC_SCRIPT, "");
	}

	/**
	 * Sets additional codec specific configuration options for MEncoder.
	 *
	 * @param value The additional configuration options.
	 */
	public void setMencoderCodecSpecificConfig(String value) {
		configuration.setProperty(KEY_MENCODER_CODEC_SPECIFIC_SCRIPT, value);
	}

	/**
	 * Returns the maximum size (in MB) that DMS should use for buffering
	 * audio.
	 *
	 * @return The maximum buffer size.
	 */
	public int getMaxAudioBuffer() {
		return getInt(KEY_MAX_AUDIO_BUFFER, 100);
	}

	/**
	 * Returns the minimum size (in MB) that DMS should use for the buffer used
	 * for streaming media.
	 *
	 * @return The minimum buffer size.
	 */
	public int getMinStreamBuffer() {
		return getInt(KEY_MIN_STREAM_BUFFER, 1);
	}

	/**
	 * Converts the getMPEG2MainSettings() from MEncoder's format to FFmpeg's.
	 *
	 * @return MPEG-2 settings formatted for FFmpeg.
	 */
	public String getMPEG2MainSettingsFFmpeg() {
		String mpegSettings = getMPEG2MainSettings();
		if (StringUtils.isBlank(mpegSettings) || mpegSettings.contains("Automatic")) {
			return mpegSettings;
		}

		return convertMencoderSettingToFFmpegFormat(mpegSettings);
	}

	public int getFFmpegMaxThreads() {
		return getInt(KEY_FFMPEG_MAX_THREADS, 0);
	}

	/**
	 * Sets the maximum number of concurrent FFmpeg threads.
	 *
	 * @param value The maximum number of concurrent threads.
	 */
	public void setFFmpegMaxThreads(int value) {
		configuration.setProperty(KEY_FFMPEG_MAX_THREADS, value);
	}

	public int getFFmpegDecodingThreads() {
		if (getFFmpegVideoHardwareAccelerationMethod() != FFMpegVideo.HARDWARE_ACCELERATION_NONE) {
			return 1;
		}
		return getFFmpegVideoHardwareAccelerationMethod() == FFMpegVideo.HARDWARE_ACCELERATION_NONE ?
			Math.max(Math.min(getFFmpegMaxThreads(), Runtime.getRuntime().availableProcessors()), 0) :
			1;
	}

	public int getFFmpegEncodingThreads() {
		return Math.max(Math.min(getFFmpegMaxThreads(), Runtime.getRuntime().availableProcessors()), 0);
	}

	public String getFFmpegHardwareDecodingAccelerationMethod() {
		return getString(KEY_FFMPEG_DECODING_HARDWARE_ACCELERATION, FFMpegVideo.HARDWARE_ACCELERATION_NONE).trim().toLowerCase(Locale.ROOT);
	}

	public void setFFmpegDecodingHardwareAccelerationMethod(String value) {
		configuration.setProperty(KEY_FFMPEG_DECODING_HARDWARE_ACCELERATION, value);
	}

	/**
	 * Validates and return the hardware acceleration method for FFmpegVideo.
	 * The validation is made against the hardware acceleration methods
	 * collected from the currently chosen executable. Invalid method names are
	 * replaced with {@link FFMpegVideo#HARDWARE_ACCELERATION_AUTO}, unless no
	 * information has been collected, in which case the specified string is
	 * used as-is. Blank values returns
	 * {@link FFMpegVideo#HARDWARE_ACCELERATION_AUTO}.
	 *
	 * @return The "sanitized" {@link FFMpegVideo} acceleration method.
	 */
	@Nonnull
	public String getFFmpegVideoHardwareAccelerationMethod() {
		Player player = PlayerFactory.getPlayer(FFMpegVideo.ID, false, false);
		FFmpegExecutableInfo info = null;
		if (player != null) {
			ExecutableInfo executableInfo = player.getExecutableInfo();
			if (executableInfo instanceof FFmpegExecutableInfo) {
				info = (FFmpegExecutableInfo) executableInfo;
			}
		}
		String result = getFFmpegHardwareDecodingAccelerationMethod();
		if (info == null) {
			LOGGER.warn("Could not validate FFmpeg hardware acceleration method \"{}\", transcoding might fail", result);
			return isBlank(result) ? FFMpegVideo.HARDWARE_ACCELERATION_AUTO : result;
		}
		Set<String> methods = info.getHardwareAccelerationMethods();
		if (
			result.equals(FFMpegVideo.HARDWARE_ACCELERATION_NONE) ||
			result.equals(FFMpegVideo.HARDWARE_ACCELERATION_AUTO) ||
			methods.contains(result)
		) {
			return result;
		}
		LOGGER.warn(
			"Invalid or unsupported FFmpeg hardware acceleration method \"{}\" " +
			"configured, falling back to automatic detection",
			result
		);
		return FFMpegVideo.HARDWARE_ACCELERATION_AUTO;
	}

	public int getFFmpegAviSynthMaxThreads() {
		return getInt(KEY_FFMPEG_AVISYNTH_MAX_THREADS, Runtime.getRuntime().availableProcessors());
	}

	public void setFFmpegAviSynthMaxThreads(int value) {
		configuration.setProperty(KEY_FFMPEG_AVISYNTH_MAX_THREADS, value);
	}

	public int getFFmpegAviSynthEffectiveMaxThreads() {
		return Math.max(Math.min(
			getFFmpegAviSynthMaxThreads(),
			Runtime.getRuntime().availableProcessors()
		), 1);
	}

	/**
	 * Whether we should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @param value True if we should pass the flag.
	 */
	public void setFFmpegAviSynthConvertFps(boolean value) {
		configuration.setProperty(KEY_AVISYNTH_CONVERT_FPS, value);
	}

	/**
	 * Returns true if we should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @return True if we should pass the flag.
	 */
	public boolean getFFmpegAviSynthConvertFps() {
		return getBoolean(KEY_FFMPEG_AVISYNTH_CONVERT_FPS, true);
	}

	public void setFFmpegAviSynthInterFrame(boolean value) {
		configuration.setProperty(KEY_FFMPEG_AVISYNTH_INTERFRAME, value);
	}

	public boolean getFFmpegAviSynthInterFrame() {
		return getBoolean(KEY_FFMPEG_AVISYNTH_INTERFRAME, false);
	}

	public void setFFmpegAviSynthInterFrameGPU(boolean value) {
		configuration.setProperty(KEY_FFMPEG_AVISYNTH_INTERFRAME_GPU, value);
	}

	public boolean getFFmpegAviSynthInterFrameGPU() {
		return getBoolean(KEY_FFMPEG_AVISYNTH_INTERFRAME_GPU, false);
	}

	public boolean isMencoderNoOutOfSync() {
		return getBoolean(KEY_MENCODER_NO_OUT_OF_SYNC, true);
	}

	public void setMencoderNoOutOfSync(boolean value) {
		configuration.setProperty(KEY_MENCODER_NO_OUT_OF_SYNC, value);
	}

	public boolean getTrancodeBlocksMultipleConnections() {
		return getBoolean(KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS, false);
	}

	public void setTranscodeBlocksMultipleConnections(boolean value) {
		configuration.setProperty(KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS, value);
	}

	public boolean getTrancodeKeepFirstConnections() {
		return getBoolean(KEY_TRANSCODE_KEEP_FIRST_CONNECTION, true);
	}

	public void setTrancodeKeepFirstConnections(boolean value) {
		configuration.setProperty(KEY_TRANSCODE_KEEP_FIRST_CONNECTION, value);
	}

	public boolean isMencoderIntelligentSync() {
		return getBoolean(KEY_MENCODER_INTELLIGENT_SYNC, true);
	}

	public void setMencoderIntelligentSync(boolean value) {
		configuration.setProperty(KEY_MENCODER_INTELLIGENT_SYNC, value);
	}

	public boolean getSkipLoopFilterEnabled() {
		return getBoolean(KEY_SKIP_LOOP_FILTER_ENABLED, false);
	}

	/**
	 * The list of network interfaces that should be skipped when checking
	 * for an available network interface. Entries should be comma separated
	 * and typically exclude the number at the end of the interface name.
	 * <p>
	 * Default is to skip the interfaces created by Virtualbox, OpenVPN and
	 * Parallels: "tap,vmnet,vnic,virtualbox".
	 * @return The string of network interface names to skip.
	 */
	public List<String> getSkipNetworkInterfaces() {
		return getStringList(KEY_SKIP_NETWORK_INTERFACES, "tap,vmnet,vnic,virtualbox");
	}

	public void setSkipLoopFilterEnabled(boolean value) {
		configuration.setProperty(KEY_SKIP_LOOP_FILTER_ENABLED, value);
	}

	public String getMPEG2MainSettings() {
		return getString(KEY_MPEG2_MAIN_SETTINGS, "Automatic (Wired)");
	}

	public void setMPEG2MainSettings(String value) {
		configuration.setProperty(KEY_MPEG2_MAIN_SETTINGS, value);
	}

	public String getx264ConstantRateFactor() {
		return getString(KEY_X264_CONSTANT_RATE_FACTOR, "Automatic (Wired)");
	}

	public void setx264ConstantRateFactor(String value) {
		configuration.setProperty(KEY_X264_CONSTANT_RATE_FACTOR, value);
	}

	public String getMencoderVobsubSubtitleQuality() {
		return getString(KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY, "3");
	}

	public void setMencoderVobsubSubtitleQuality(String value) {
		configuration.setProperty(KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY, value);
	}

	public String getMencoderOverscanCompensationWidth() {
		return getString(KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH, "0");
	}

	public void setMencoderOverscanCompensationWidth(String value) {
		if (value.trim().length() == 0) {
			value = "0";
		}
		configuration.setProperty(KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH, value);
	}

	public String getMencoderOverscanCompensationHeight() {
		return getString(KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT, "0");
	}

	public void setMencoderOverscanCompensationHeight(String value) {
		if (value.trim().length() == 0) {
			value = "0";
		}
		configuration.setProperty(KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT, value);
	}

	/**
	 * Lazy implementation, call before accessing {@link #enabledEngines}.
	 */
	private void buildEnabledEngines() {
		if (enabledEnginesBuilt) {
			return;
		}
		enabledEnginesLock.writeLock().lock();
		try {
			// Not a bug, using double checked locking
			if (enabledEnginesBuilt) {
				return;
			}
			String engines = configuration.getString(KEY_ENGINES);
			enabledEngines = stringToPlayerIdSet(engines);
			if (isBlank(engines)) {
				configuration.setProperty(KEY_ENGINES, collectionToString(enabledEngines));
			}

			enabledEnginesBuilt = true;
		} finally {
			enabledEnginesLock.writeLock().unlock();
		}
	}

	/**
	 * Gets a {@link UniqueList} of the {@link PlayerId}s in no particular
	 * order. Returns a new instance, any modifications won't affect original
	 * list.
	 *
	 * @return A copy of the {@link List} of {@link PlayerId}s.
	 */
	public List<PlayerId> getEnabledEngines() {
		buildEnabledEngines();
		enabledEnginesLock.readLock().lock();
		try {
			return new ArrayList<>(enabledEngines);
		} finally {
			enabledEnginesLock.readLock().unlock();
		}
	}

	/**
	 * Gets the enabled status of the specified {@link PlayerId}.
	 *
	 * @param id the {@link PlayerId} to check.
	 * @return {@code true} if the {@link Player} with {@code id} is enabled,
	 *         {@code false} otherwise.
	 */
	public boolean isEngineEnabled(PlayerId id) {
		if (id == null) {
			throw new NullPointerException("id cannot be null");
		}
		buildEnabledEngines();
		enabledEnginesLock.readLock().lock();
		try {
			return enabledEngines.contains(id);
		} finally {
			enabledEnginesLock.readLock().unlock();
		}
	}

	/**
	 * Gets the enabled status of the specified {@link Player}.
	 *
	 * @param player the {@link Player} to check.
	 * @return {@code true} if {@code player} is enabled, {@code false}
	 *         otherwise.
	 */
	public boolean isEngineEnabled(Player player) {
		if (player == null) {
			throw new NullPointerException("player cannot be null");
		}

		return isEngineEnabled(player.id());
	}

	/**
	 * Sets the enabled status of the specified {@link PlayerId}.
	 *
	 * @param id the {@link PlayerId} whose enabled status to set.
	 * @param enabled the enabled status to set.
	 */
	public void setEngineEnabled(PlayerId id, boolean enabled) {
		if (id == null) {
			throw new IllegalArgumentException("Unrecognized id");
		}

		enabledEnginesLock.writeLock().lock();
		try {
			buildEnabledEngines();
			if (enabledEngines.contains(id)) {
				if (!enabled) {
					enabledEngines.remove(id);
				}
			} else {
				if (enabled) {
					enabledEngines.add(id);
				}
			}
			configuration.setProperty(KEY_ENGINES, collectionToString(enabledEngines));
		} finally {
			enabledEnginesLock.writeLock().unlock();
		}
	}

	/**
	 * Sets the enabled status of the specified {@link Player}.
	 *
	 * @param player the {@link Player} whose enabled status to set.
	 * @param enabled the enabled status to set.
	 */
	public void setEngineEnabled(Player player, boolean enabled) {
		setEngineEnabled(player.id(), enabled);
	}

	/**
	 * This is to make sure that any incorrect capitalization in the
	 * configuration file is corrected. This should only need to be called from
	 * {@link PlayerFactory#registerPlayer(Player)}.
	 *
	 * @param player the {@link Player} for which to assure correct
	 *            capitalization.
	 */
	public void capitalizeEngineId(Player player) {
		if (player == null) {
			throw new NullPointerException("player cannot be null");
		}

		String engines = configuration.getString(KEY_ENGINES);
		if (StringUtils.isNotBlank(engines)) {
			String capitalizedEngines = StringUtil.caseReplace(engines.trim(), player.id().toString());
			if (!engines.equals(capitalizedEngines)) {
				configuration.setProperty(KEY_ENGINES, capitalizedEngines);
			}
		}

		engines = configuration.getString(KEY_ENGINES_PRIORITY);
		if (StringUtils.isNotBlank(engines)) {
			String capitalizedEngines = StringUtil.caseReplace(engines.trim(), player.id().toString());
			if (!engines.equals(capitalizedEngines)) {
				configuration.setProperty(KEY_ENGINES_PRIORITY, capitalizedEngines);
			}
		}
	}

	/**
	 * Lazy implementation, call before accessing {@link #enginesPriority}.
	 */
	private void buildEnginesPriority() {
		if (enginesPriorityBuilt) {
			return;
		}
		enginesPriorityLock.writeLock().lock();
		try {
			// Not a bug, using double checked locking
			if (enginesPriorityBuilt) {
				return;
			}
			String enginesPriorityString = configuration.getString(KEY_ENGINES_PRIORITY);
			enginesPriority = stringToPlayerIdSet(enginesPriorityString);
			if (isBlank(enginesPriorityString)) {
				configuration.setProperty(KEY_ENGINES_PRIORITY, collectionToString(enginesPriority));
			}
			enginesPriorityBuilt = true;
		} finally {
			enginesPriorityLock.writeLock().unlock();
		}
	}

	/**
	 * Gets a {@link UniqueList} of the {@link PlayerId}s ordered by priority.
	 * Returns a new instance, any modifications won't affect priority list.
	 *
	 * @return A copy of the priority list.
	 */
	public UniqueList<PlayerId> getEnginesPriority() {
		buildEnginesPriority();
		enginesPriorityLock.readLock().lock();
		try {
			return new UniqueList<>(enginesPriority);
		} finally {
			enginesPriorityLock.readLock().unlock();
		}
	}

	/**
	 * Returns the priority index according to the rules of {@link List#indexOf}.
	 *
	 * @param id the {@link PlayerId} whose position to return.
	 * @return The priority index of {@code id}, or {@code -1} if the priority
	 *         list doesn't contain {@code id}.
	 */
	public int getEnginePriority(PlayerId id) {
		if (id == null) {
			throw new NullPointerException("id cannot be null");
		}

		buildEnginesPriority();
		enginesPriorityLock.readLock().lock();
		try {
			int index = enginesPriority.indexOf(id);
			if (index >= 0) {
				return index;
			}
		} finally {
			enginesPriorityLock.readLock().unlock();
		}

		// The engine isn't listed, add it last
		enginesPriorityLock.writeLock().lock();
		try {
			enginesPriority.add(id);
			return enginesPriority.indexOf(id);
		} finally {
			enginesPriorityLock.writeLock().unlock();
		}
	}

	/**
	 * Returns the priority index according to the rules of {@link List#indexOf}.
	 *
	 * @param player the {@link Player} whose position to return.
	 * @return the priority index of {@code player}, or -1 if this the priority
	 *         list doesn't contain {@code player}.
	 */
	public int getEnginePriority(Player player) {
		if (player == null) {
			throw new NullPointerException("player cannot be null");
		}
		return getEnginePriority(player.id());
	}

	/**
	 * Moves or inserts a {@link Player} directly above another {@link Player}
	 * in the priority list. If {code abovePlayer} is {@code null},
	 * {@code player} will be placed first in the list. If {@code abovePlayer}
	 * isn't found, {@code player} will be placed last in the list.
	 *
	 * @param player the {@link Player} to move or insert in the priority list.
	 * @param abovePlayer the {@link Player} to place {@code player} relative
	 *            to.
	 */
	public void setEnginePriorityAbove(@Nonnull Player player, @Nullable Player abovePlayer) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		setEnginePriorityAbove(player.id(), abovePlayer == null ? null : abovePlayer.id());
	}

	/**
	 * Moves or inserts a {@link PlayerId} directly above another
	 * {@link PlayerId} in the priority list. If {code aboveId} is {@code null},
	 * {@code id} will be placed first in the list. If {@code aboveId} isn't
	 * found, {@code id} will be placed last in the list.
	 *
	 * @param id the {@link PlayerId} to move or insert in the priority list.
	 * @param aboveId the {@link PlayerId} to place {@code id} relative to.
	 */
	public void setEnginePriorityAbove(PlayerId id, PlayerId aboveId) {
		if (id == null) {
			throw new IllegalArgumentException("Unrecognized id");
		}

		enginesPriorityLock.writeLock().lock();
		try {
			buildEnginesPriority();

			if (enginesPriority.indexOf(id) > -1) {
				enginesPriority.remove(id);
			}

			int newPosition;
			if (aboveId == null) {
				newPosition = 0;
			} else {
				newPosition = enginesPriority.indexOf(aboveId);
				if (newPosition < 0) {
					newPosition = enginesPriority.size();
				}
			}
			enginesPriority.add(newPosition, id);
			configuration.setProperty(KEY_ENGINES_PRIORITY, collectionToString(enginesPriority));
		} finally {
			enginesPriorityLock.writeLock().unlock();
		}
		PlayerFactory.sortPlayers();
	}

	/**
	 * Moves or inserts a {@link Player} directly below another {@link Player}
	 * in the priority list. If {code belowPlayer} is {@code null} or isn't
	 * found, {@code player} will be placed last in the list.
	 *
	 * @param player the {@link Player} to move or insert in the priority list.
	 * @param belowPlayer the {@link Player} to place {@code player} relative
	 *            to.
	 */
	public void setEnginePriorityBelow(Player player, Player belowPlayer) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		setEnginePriorityBelow(player.id(), belowPlayer == null ? null : belowPlayer.id());
	}

	/**
	 * Moves or inserts a {@link PlayerId} directly below another
	 * {@link PlayerId} in the priority list. If {code belowId} is {@code null}
	 * or isn't found, {@code id} will be placed last in the list.
	 *
	 * @param id the {@link PlayerId} to move or insert in the priority list.
	 * @param belowId the {@link PlayerId} to place {@code id} relative to.
	 */
	public void setEnginePriorityBelow(PlayerId id, PlayerId belowId) {
		if (id == null) {
			throw new IllegalArgumentException("Unrecognized id");
		}

		enginesPriorityLock.writeLock().lock();
		try {
			buildEnginesPriority();

			if (enginesPriority.indexOf(id) > -1) {
				enginesPriority.remove(id);
			}

			int newPosition;
			if (belowId == null) {
				newPosition = enginesPriority.size();
			} else {
				newPosition = enginesPriority.indexOf(belowId) + 1;
				if (newPosition < 0) {
					newPosition = enginesPriority.size();
				}
			}
			enginesPriority.add(newPosition, id);
			configuration.setProperty(KEY_ENGINES_PRIORITY, collectionToString(enginesPriority));
		} finally {
			enginesPriorityLock.writeLock().unlock();
		}
		PlayerFactory.sortPlayers();
	}

	private static String collectionToString(Collection<?> list) {
		return StringUtils.join(list, LIST_SEPARATOR);
	}

	@SuppressWarnings("unused")
	private static List<String> stringToStringList(String input) {
		List<String> output = new ArrayList<>();
		Collections.addAll(output, StringUtils.split(input, LIST_SEPARATOR));
		return output;
	}

	private static UniqueList<PlayerId> stringToPlayerIdSet(String input) {
		UniqueList<PlayerId> output = new UniqueList<>();
		if (isBlank(input)) {
			output.addAll(StandardPlayerId.ALL);
			return output;
		}
		input = input.trim().toLowerCase(Locale.ROOT);
		if ("none".equals(input)) {
			return output;
		}
		for (String s : StringUtils.split(input, LIST_SEPARATOR)) {
			PlayerId playerId = StandardPlayerId.toPlayerID(s);
			if (playerId != null) {
				output.add(playerId);
			} else {
				LOGGER.warn("Unknown transcoding engine \"{}\"", s);
			}
		}
		return output;
	}

	public void save() throws ConfigurationException {
		((PropertiesConfiguration) configuration).save();
		LOGGER.info("Configuration saved to \"{}\"", CONFIGURATION_FILE);
	}

	private final Object sharedFoldersLock = new Object();

	@GuardedBy("sharedFoldersLock")
	private boolean sharedFoldersRead;

	@GuardedBy("sharedFoldersLock")
	private ArrayList<Path> sharedFolders;

	@GuardedBy("sharedFoldersLock")
	private boolean monitoredFoldersRead;

	@GuardedBy("sharedFoldersLock")
	private ArrayList<Path> monitoredFolders;

	@GuardedBy("sharedFoldersLock")
	private boolean ignoredFoldersRead;

	@GuardedBy("sharedFoldersLock")
	private ArrayList<Path> ignoredFolders;

	@GuardedBy("sharedFoldersLock")
	private boolean defaultSharedFolders;

	@GuardedBy("sharedFoldersLock")
	private boolean defaultSharedFoldersRead;

	private void readSharedFolders() {
		synchronized (sharedFoldersLock) {
			if (!sharedFoldersRead) {
				sharedFolders = getFolders(KEY_FOLDERS);
				sharedFoldersRead = true;
			}
		}
	}

	private void readDefaultSharedFolders() {
		synchronized (sharedFoldersLock) {
			if (!defaultSharedFoldersRead) {
				Boolean useDefault;
				try {
					useDefault = configuration.getBoolean(KEY_USE_DEFAULT_FOLDERS, null);
				} catch (ConversionException e) {
					useDefault = null;
					String useDefaultString = configuration.getString(KEY_USE_DEFAULT_FOLDERS, null);
					if (!isBlank(useDefaultString)) {
						LOGGER.warn(
							"Invalid configured value for {} \"{}\", using default",
							KEY_USE_DEFAULT_FOLDERS,
							useDefaultString
						);
					}
				}
				if (useDefault != null) {
					defaultSharedFolders = useDefault.booleanValue();
				} else {
					defaultSharedFolders = isSharedFoldersEmpty();
				}
				defaultSharedFoldersRead = true;
			}
		}
	}

	/**
	 * @return {@code true} if the default shared folders are used,
	 *         {@code false} otherwise.
	 */
	public boolean isDefaultSharedFolders() {
		synchronized (sharedFoldersLock) {
			readDefaultSharedFolders();
			return defaultSharedFolders;
		}
	}

	/**
	 * Sets whether to use the default shared folders.
	 *
	 * @param useDefaultFolders {@code true} to use the default shared folders,
	 *            {@code false} otherwise.
	 */
	public void setDefaultSharedFolders(boolean useDefaultFolders) {
		synchronized (sharedFoldersLock) {
			readDefaultSharedFolders();
			if (useDefaultFolders != defaultSharedFolders) {
				configuration.setProperty(KEY_USE_DEFAULT_FOLDERS, useDefaultFolders);
				defaultSharedFolders = useDefaultFolders;
			}
		}
	}

	/**
	 * @return {@code true} if the configured shared folders are empty,
	 *         {@code false} otherwise.
	 */
	public boolean isSharedFoldersEmpty() {
		synchronized (sharedFoldersLock) {
			readSharedFolders();
			return sharedFolders.isEmpty();
		}
	}

	/**
	 * @return The {@link List} of {@link Path}s of shared folders.
	 */
	@Nonnull
	public List<Path> getSharedFolders() {
		synchronized (sharedFoldersLock) {
			if (isDefaultSharedFolders()) {
				return BasicSystemUtils.INSTANCE.getDefaultFolders();
			}
			readSharedFolders();
			return new ArrayList<>(sharedFolders);
		}
	}

	/**
	 * @return The {@link List} of {@link Path}s of monitored folders.
	 */
	@Nonnull
	public List<Path> getMonitoredFolders() {
		synchronized (sharedFoldersLock) {
			if (isDefaultSharedFolders()) {
				return BasicSystemUtils.INSTANCE.getDefaultFolders();
			}
			if (!monitoredFoldersRead) {
				monitoredFolders = getFolders(KEY_FOLDERS_MONITORED);
				monitoredFoldersRead = true;
			}
			return new ArrayList<>(monitoredFolders);
		}
	}

	/**
	 * @return The {@link List} of {@link Path}s of ignored folders.
	 */
	@Nonnull
	public List<Path> getIgnoredFolders() {
		synchronized (sharedFoldersLock) {
			if (!ignoredFoldersRead) {
				ignoredFolders = getFolders(KEY_FOLDERS_IGNORED);
				ignoredFoldersRead = true;
			}
			return ignoredFolders;
		}
	}

	/**
	 * Transforms a comma-separated list of directory entries into an
	 * {@link ArrayList} of {@link Path}s. Verifies that the folder exists and
	 * is valid.
	 *
	 * @param key the {@link Configuration} key to read.
	 * @return The {@link List} of folders or {@code null}.
	 */
	@Nonnull
	protected ArrayList<Path> getFolders(String key) {
		String foldersString = configuration.getString(key, null);

		ArrayList<Path> folders = new ArrayList<>();
		if (foldersString == null || foldersString.length() == 0) {
			return folders;
		}
		String[] foldersArray = foldersString.trim().split("\\s*,\\s*");

		for (String folder : foldersArray) {
			/*
			 * Unescape embedded commas. Note: Backslashing isn't safe as it
			 * conflicts with the Windows path separator.
			 */
			folder = folder.replaceAll("&comma;", ",");

			if (KEY_FOLDERS.equals(key)) {
				LOGGER.info("Checking shared folder: \"{}\"", folder);
			}

			Path path = Paths.get(folder);
			if (Files.exists(path)) {
				if (!Files.isDirectory(path)) {
					if (KEY_FOLDERS.equals(key)) {
						LOGGER.warn(
							"The \"{}\" is not a folder! Please remove it from your shared folders " +
							"list on the \"{}\" tab or in the configuration file.",
							folder,
							Messages.getString("LooksFrame.22")
						);
					} else {
						LOGGER.debug("The \"{}\" is not a folder - check the configuration for key \"{}\"", folder, key);
					}
				}
			} else if (KEY_FOLDERS.equals(key)) {
				LOGGER.warn(
					"\"{}\" does not exist. Please remove it from your shared folders " +
					"list on the \"{}\" tab or in the configuration file.",
					folder,
					Messages.getString("LooksFrame.22")
				);
			} else {
				LOGGER.debug("\"{}\" does not exist - check the configuration for key \"{}\"", folder, key);
			}

			// add the path even if there are problems so that the user can update the shared folders as required.
			folders.add(path);
		}

		return folders;
	}

	/**
	 * Stores the shared folders in the configuration from the specified
	 * {@link SharedFoldersTableModel#getDataVector()} value. This is expected
	 * to be a {@link Vector} of rows containing a {@link Vector} of column
	 * values where the first column is a {@link String} and the seconds is a
	 * {@link Boolean}.
	 *
	 * @param tableVector the {@link SharedFoldersTableModel#getDataVector()}
	 *            value to use.
	 */
	@SuppressWarnings("rawtypes")
	public void setSharedFolders(Vector<Vector<?>> tableVector) {
		if (tableVector == null || tableVector.isEmpty()) {
			synchronized (sharedFoldersLock) {
				if (!sharedFoldersRead || !sharedFolders.isEmpty()) {
					configuration.setProperty(KEY_FOLDERS, "");
					sharedFolders.clear();
					sharedFoldersRead = true;
				}
				if (!monitoredFoldersRead || !monitoredFolders.isEmpty()) {
					configuration.setProperty(KEY_FOLDERS_MONITORED, "");
					monitoredFolders.clear();
					monitoredFoldersRead = true;
				}
			}
			return;
		}
		String listSeparator = String.valueOf(LIST_SEPARATOR);
		ArrayList<Path> tmpSharedfolders = new ArrayList<>();
		ArrayList<Path> tmpMonitoredFolders = new ArrayList<>();
		for (Vector rowVector : tableVector) {
			if (rowVector != null && rowVector.size() == 2 && rowVector.get(0) instanceof String) {
				String folderPath = (String) rowVector.get(0);
				/*
				 * Escape embedded commas. Note: Backslashing isn't safe as it
				 * conflicts with the Windows path separator.
				 */
				if (folderPath.contains(listSeparator)) {
					folderPath = folderPath.replace(listSeparator, "&comma;");
				}
				Path folder = Paths.get(folderPath);
				tmpSharedfolders.add(folder);
				if ((boolean) rowVector.get(1)) {
					tmpMonitoredFolders.add(folder);
				}
			} else {
				LOGGER.error("Unexpected vector content in setSharedFolders(), saving of shared folders failed");
				return;
			}
		}
		synchronized (sharedFoldersLock) {
			if (!sharedFoldersRead || !sharedFolders.equals(tmpSharedfolders)) {
				configuration.setProperty(KEY_FOLDERS, StringUtils.join(tmpSharedfolders, LIST_SEPARATOR));
				sharedFolders = tmpSharedfolders;
				sharedFoldersRead = true;
			}
			if (!monitoredFoldersRead || !monitoredFolders.equals(tmpMonitoredFolders)) {
				configuration.setProperty(KEY_FOLDERS_MONITORED, StringUtils.join(tmpMonitoredFolders, LIST_SEPARATOR));
				monitoredFolders = tmpMonitoredFolders;
				monitoredFoldersRead = true;
			}
		}
	}

	/**
	 * Sets the shared folders and the monitor folders to the platform default
	 * folders.
	 */
	public void setSharedFoldersToDefault() {
		synchronized (sharedFoldersLock) {
			sharedFolders = new ArrayList<>(BasicSystemUtils.INSTANCE.getDefaultFolders());
			configuration.setProperty(KEY_FOLDERS, StringUtils.join(sharedFolders, LIST_SEPARATOR));
			sharedFoldersRead = true;
			monitoredFolders = new ArrayList<>(BasicSystemUtils.INSTANCE.getDefaultFolders());
			configuration.setProperty(KEY_FOLDERS_MONITORED, StringUtils.join(monitoredFolders, LIST_SEPARATOR));
			monitoredFoldersRead = true;
		}
	}

	public String getNetworkInterface() {
		return getString(KEY_NETWORK_INTERFACE, "");
	}

	public void setNetworkInterface(String value) {
		configuration.setProperty(KEY_NETWORK_INTERFACE, value);
	}

	public boolean isHideEngineNames() {
		return getBoolean(KEY_HIDE_ENGINENAMES, true);
	}

	public void setHideEngineNames(boolean value) {
		configuration.setProperty(KEY_HIDE_ENGINENAMES, value);
	}

	public boolean isHideExtensions() {
		return getBoolean(KEY_HIDE_EXTENSIONS, true);
	}

	public void setHideExtensions(boolean value) {
		configuration.setProperty(KEY_HIDE_EXTENSIONS, value);
	}

	public String getShares() {
		return getString(KEY_SHARES, "");
	}

	public void setShares(String value) {
		configuration.setProperty(KEY_SHARES, value);
	}

	public String getDisableTranscodeForExtensions() {
		return getString(KEY_DISABLE_TRANSCODE_FOR_EXTENSIONS, "");
	}

	public void setDisableTranscodeForExtensions(String value) {
		configuration.setProperty(KEY_DISABLE_TRANSCODE_FOR_EXTENSIONS, value);
	}

	public boolean isDisableTranscoding() {
		return getBoolean(KEY_DISABLE_TRANSCODING, false);
	}

	public String getForceTranscodeForExtensions() {
		return getString(KEY_FORCE_TRANSCODE_FOR_EXTENSIONS, "");
	}

	public void setForceTranscodeForExtensions(String value) {
		configuration.setProperty(KEY_FORCE_TRANSCODE_FOR_EXTENSIONS, value);
	}

	public int getMEncoderMaxThreads() {
		return getInt(KEY_MENCODER_MAX_THREADS, 0);
	}

	/**
	 * Sets the maximum number of concurrent MEncoder threads.
	 *
	 * @param value The maximum number of concurrent threads.
	 */
	public void setMEncoderMaxThreads(int value) {
		configuration.setProperty(KEY_MENCODER_MAX_THREADS, value);
	}

	public int getMEncoderEffectiveMaxThreads() {
		return Math.max(Math.min(getMEncoderMaxThreads(), Runtime.getRuntime().availableProcessors()), 0);
	}

	public boolean isMEncoderSacrificeCompatibilityForSpeed() {
		return getBoolean(KEY_MENCODER_SPEED_TRUMPS_COMPATIBILITY, false);
	}

	public void setMEncoderSacrificeCompatibilityForSpeed(boolean value) {
		configuration.setProperty(KEY_MENCODER_SPEED_TRUMPS_COMPATIBILITY, value);
	}

	public void setAudioRemuxAC3(boolean value) {
		configuration.setProperty(KEY_AUDIO_REMUX_AC3, value);
	}

	public boolean isAudioRemuxAC3() {
		return getBoolean(KEY_AUDIO_REMUX_AC3, true);
	}

	public void setMencoderRemuxMPEG2(boolean value) {
		configuration.setProperty(KEY_MENCODER_REMUX_MPEG2, value);
	}

	public boolean isMencoderRemuxMPEG2() {
		return getBoolean(KEY_MENCODER_REMUX_MPEG2, true);
	}

	public void setDisableFakeSize(boolean value) {
		configuration.setProperty(KEY_DISABLE_FAKESIZE, value);
	}

	public boolean isDisableFakeSize() {
		return getBoolean(KEY_DISABLE_FAKESIZE, false);
	}

	/**
	 * Whether the style rules defined by styled subtitles (ASS/SSA) should
	 * be followed (true) or overridden by our style rules (false).
	 *
	 * @param value whether to use the embedded styles or ours
	 */
	public void setUseEmbeddedSubtitlesStyle(boolean value) {
		configuration.setProperty(KEY_USE_EMBEDDED_SUBTITLES_STYLE, value);
	}

	/**
	 * Whether the style rules defined by styled subtitles (ASS/SSA) should
	 * be followed (true) or overridden by our style rules (false).
	 *
	 * @return whether to use the embedded styles or ours
	 */
	public boolean isUseEmbeddedSubtitlesStyle() {
		return getBoolean(KEY_USE_EMBEDDED_SUBTITLES_STYLE, true);
	}

	public int getMEncoderOverscan() {
		return getInt(KEY_OVERSCAN, 0);
	}

	public void setMEncoderOverscan(int value) {
		configuration.setProperty(KEY_OVERSCAN, value);
	}

	/**
	 * Returns sort method to use for ordering lists of files. One of the
	 * following values is returned:
	 * <ul>
	 * <li>0: Locale-sensitive A-Z</li>
	 * <li>1: Sort by modified date, newest first</li>
	 * <li>2: Sort by modified date, oldest first</li>
	 * <li>3: Case-insensitive ASCIIbetical sort</li>
	 * <li>4: Locale-sensitive natural sort</li>
	 * <li>5: Random</li>
	 * </ul>
	 * Default value is 4.
	 * @return The sort method
	 */
	private static int findPathSort(String[] paths, String path) throws NumberFormatException{
		for (String path1 : paths) {
			String[] kv = path1.split(",");
			if (kv.length < 2) {
				continue;
			}
			if (kv[0].equals(path)) {
				return Integer.parseInt(kv[1]);
			}
		}
		return -1;
	}

	public int getSortMethod(File path) {
		int cnt = 0;
		String raw = getString(KEY_SORT_PATHS, null);
		if (StringUtils.isEmpty(raw)) {
			return getInt(KEY_SORT_METHOD, UMSUtils.SORT_LOC_NAT);
		}
		if (Platform.isWindows()) {
			// windows is crap
			raw = raw.toLowerCase();
		}
		String[] paths = raw.split(" ");

		while (path != null && (cnt++ < 100)) {
			String key = path.getAbsolutePath();
			if (Platform.isWindows()) {
				key = key.toLowerCase();
			}
			try {
				int ret = findPathSort(paths, key);
				if (ret != -1) {
					return ret;
				}
			} catch (NumberFormatException e) {
				// just ignore
			}
			path = path.getParentFile();
		}
		return getInt(KEY_SORT_METHOD, UMSUtils.SORT_LOC_NAT);
	}

	/**
	 * Set the sort method to use for ordering lists of files. The following
	 * values are recognized:
	 * <ul>
	 * <li>0: Locale-sensitive A-Z</li>
	 * <li>1: Sort by modified date, newest first</li>
	 * <li>2: Sort by modified date, oldest first</li>
	 * <li>3: Case-insensitive ASCIIbetical sort</li>
	 * <li>4: Locale-sensitive natural sort</li>
	 * <li>5: Random</li>
	 * </ul>
	 * @param value The sort method to use
	 */
	public void setSortMethod(int value) {
		configuration.setProperty(KEY_SORT_METHOD, value);
	}

	@Nonnull
	public CoverSupplier getAudioThumbnailMethod() {
		return CoverSupplier.typeOf(getInt(KEY_AUDIO_THUMBNAILS_METHOD, -1), CoverSupplier.COVER_ART_ARCHIVE);
	}

	public void setAudioThumbnailMethod(@Nullable CoverSupplier value) {
		configuration.setProperty(KEY_AUDIO_THUMBNAILS_METHOD, value == null ? "" : value.ordinal());
	}

	public String getAlternateThumbFolder() {
		return getString(KEY_ALTERNATE_THUMB_FOLDER, "");
	}

	public void setAlternateThumbFolder(String value) {
		configuration.setProperty(KEY_ALTERNATE_THUMB_FOLDER, value);
	}

	public String getAlternateSubtitlesFolder() {
		return getString(KEY_ALTERNATE_SUBTITLES_FOLDER, "");
	}

	public void setAlternateSubtitlesFolder(String value) {
		configuration.setProperty(KEY_ALTERNATE_SUBTITLES_FOLDER, value);
	}

	public void setAudioEmbedDtsInPcm(boolean value) {
		configuration.setProperty(KEY_AUDIO_EMBED_DTS_IN_PCM, value);
	}

	public boolean isAudioEmbedDtsInPcm() {
		return getBoolean(KEY_AUDIO_EMBED_DTS_IN_PCM, false);
	}

	public void setEncodedAudioPassthrough(boolean value) {
		configuration.setProperty(KEY_ENCODED_AUDIO_PASSTHROUGH, value);
	}

	public boolean isEncodedAudioPassthrough() {
		return getBoolean(KEY_ENCODED_AUDIO_PASSTHROUGH, false);
	}

	public void setMencoderMuxWhenCompatible(boolean value) {
		configuration.setProperty(KEY_MENCODER_MUX_COMPATIBLE, value);
	}

	public boolean isMencoderMuxWhenCompatible() {
		return getBoolean(KEY_MENCODER_MUX_COMPATIBLE, false);
	}

	public void setMEncoderNormalizeVolume(boolean value) {
		configuration.setProperty(KEY_MENCODER_NORMALIZE_VOLUME, value);
	}

	public boolean isMEncoderNormalizeVolume() {
		return getBoolean(KEY_MENCODER_NORMALIZE_VOLUME, false);
	}

	public void setFFmpegMuxWithTsMuxerWhenCompatible(boolean value) {
		configuration.setProperty(KEY_FFMPEG_MUX_TSMUXER_COMPATIBLE, value);
	}

	public boolean isFFmpegMuxWithTsMuxerWhenCompatible() {
		return getBoolean(KEY_FFMPEG_MUX_TSMUXER_COMPATIBLE, false);
	}

	/**
	 * @see #setFFmpegDeferToMEncoderForEmbeddedSubtitles(boolean)
	 * @deprecated
	 */
	@Deprecated
	public void setFFmpegDeferToMEncoderForSubtitles(boolean value) {
		setFFmpegDeferToMEncoderForProblematicSubtitles(value);
	}

	/**
	 * @see #isFFmpegDeferToMEncoderForEmbeddedSubtitles()
	 * @deprecated
	 */
	@Deprecated
	public boolean isFFmpegDeferToMEncoderForSubtitles() {
		return isFFmpegDeferToMEncoderForProblematicSubtitles();
	}

	/**
	 * Whether FFmpegVideo should defer to MEncoderVideo when there are
	 * subtitles that need to be transcoded which FFmpeg will need to
	 * initially parse, which can cause timeouts.
	 *
	 * @param value
	 */
	public void setFFmpegDeferToMEncoderForProblematicSubtitles(boolean value) {
		configuration.setProperty(KEY_FFMPEG_MENCODER_PROBLEMATIC_SUBTITLES, value);
	}

	/**
	 * Whether FFmpegVideo should defer to MEncoderVideo when there are
	 * subtitles that need to be transcoded which FFmpeg will need to
	 * initially parse, which can cause timeouts.
	 *
	 * @return
	 */
	public boolean isFFmpegDeferToMEncoderForProblematicSubtitles() {
		return getBoolean(KEY_FFMPEG_MENCODER_PROBLEMATIC_SUBTITLES, true);
	}

	public void setFFmpegFontConfig(boolean value) {
		configuration.setProperty(KEY_FFMPEG_FONTCONFIG, value);
	}

	public boolean isFFmpegFontConfig() {
		return getBoolean(KEY_FFMPEG_FONTCONFIG, false);
	}

	public void setMuxAllAudioTracks(boolean value) {
		configuration.setProperty(KEY_MUX_ALLAUDIOTRACKS, value);
	}

	public boolean isMuxAllAudioTracks() {
		return getBoolean(KEY_MUX_ALLAUDIOTRACKS, false);
	}

	public void setUseMplayerForVideoThumbs(boolean value) {
		configuration.setProperty(KEY_USE_MPLAYER_FOR_THUMBS, value);
	}

	public boolean isUseMplayerForVideoThumbs() {
		return getBoolean(KEY_USE_MPLAYER_FOR_THUMBS, false);
	}

	public String getIpFilter() {
		return getString(KEY_IP_FILTER, "");
	}

	public synchronized IpFilter getIpFiltering() {
	    filter.setRawFilter(getIpFilter());
	    return filter;
	}

	public void setIpFilter(String value) {
		configuration.setProperty(KEY_IP_FILTER, value);
	}

	public void setPreventSleep(PreventSleepMode value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		configuration.setProperty(KEY_PREVENT_SLEEP, value.getValue());
		Services.sleepManager().setMode(value);
	}

	public PreventSleepMode getPreventSleep() {
		PreventSleepMode sleepMode = null;
		String value = getString(KEY_PREVENT_SLEEP, null);
		if (value == null && configuration.containsKey(KEY_PREVENTS_SLEEP)) {
			// Backwards compatibility
			sleepMode = getBoolean(KEY_PREVENTS_SLEEP, true) ? PreventSleepMode.PLAYBACK : PreventSleepMode.NEVER;
			configuration.clearProperty(KEY_PREVENTS_SLEEP);
			configuration.setProperty(KEY_PREVENT_SLEEP, sleepMode.getValue());
		} else if (value != null) {
			sleepMode = PreventSleepMode.typeOf(value);
		}
		return sleepMode != null ? sleepMode : PreventSleepMode.PLAYBACK; // Default
	}

	public void setHTTPEngineV2(boolean value) {
		configuration.setProperty(KEY_HTTP_ENGINE_V2, value);
	}

	public boolean isHTTPEngineV2() {
		return getBoolean(KEY_HTTP_ENGINE_V2, true);
	}

	public boolean isShowIphotoLibrary() {
		return getBoolean(KEY_SHOW_IPHOTO_LIBRARY, false);
	}

	public void setShowIphotoLibrary(boolean value) {
		configuration.setProperty(KEY_SHOW_IPHOTO_LIBRARY, value);
	}

	public boolean isShowApertureLibrary() {
		return getBoolean(KEY_SHOW_APERTURE_LIBRARY, false);
	}

	public void setShowApertureLibrary(boolean value) {
		configuration.setProperty(KEY_SHOW_APERTURE_LIBRARY, value);
	}

	public boolean isShowItunesLibrary() {
		return getBoolean(KEY_SHOW_ITUNES_LIBRARY, false);
	}

	public String getItunesLibraryPath() {
		return getString(KEY_ITUNES_LIBRARY_PATH, "");
	}

	public void setShowItunesLibrary(boolean value) {
		configuration.setProperty(KEY_SHOW_ITUNES_LIBRARY, value);
	}

	public boolean isHideAdvancedOptions() {
		return getBoolean(PmsConfiguration.KEY_HIDE_ADVANCED_OPTIONS, true);
	}

	public void setHideAdvancedOptions(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_ADVANCED_OPTIONS, value);
	}

	public boolean isHideEmptyFolders() {
		return getBoolean(PmsConfiguration.KEY_HIDE_EMPTY_FOLDERS, false);
	}

	public void setHideEmptyFolders(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_EMPTY_FOLDERS, value);
	}

	public boolean isHideMediaLibraryFolder() {
		return getBoolean(PmsConfiguration.KEY_HIDE_MEDIA_LIBRARY_FOLDER, true);
	}

	public void setHideMediaLibraryFolder(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_MEDIA_LIBRARY_FOLDER, value);
	}

	// TODO (breaking change): rename to e.g. isTranscodeFolderEnabled
	// (and return true by default)
	public boolean getHideTranscodeEnabled() {
		return getBoolean(KEY_HIDE_TRANSCODE_FOLDER, false);
	}

	// TODO (breaking change): rename to e.g. setTranscodeFolderEnabled
	// (and negate the value in the caller)
	public void setHideTranscodeEnabled(boolean value) {
		configuration.setProperty(KEY_HIDE_TRANSCODE_FOLDER, value);
	}

	public boolean isDvdIsoThumbnails() {
		return getBoolean(KEY_DVDISO_THUMBNAILS, true);
	}

	public void setDvdIsoThumbnails(boolean value) {
		configuration.setProperty(KEY_DVDISO_THUMBNAILS, value);
	}

	public Object getCustomProperty(String property) {
		return configurationReader.getCustomProperty(property);
	}

	public void setCustomProperty(String property, Object value) {
		configuration.setProperty(property, value);
	}

	public boolean isChapterSupport() {
		return getBoolean(KEY_CHAPTER_SUPPORT, false);
	}

	public void setChapterSupport(boolean value) {
		configuration.setProperty(KEY_CHAPTER_SUPPORT, value);
	}

	public int getChapterInterval() {
		return getInt(KEY_CHAPTER_INTERVAL, 5);
	}

	public void setChapterInterval(int value) {
		configuration.setProperty(KEY_CHAPTER_INTERVAL, value);
	}

	public SubtitleColor getSubsColor() {
		String colorString = getString(KEY_SUBS_COLOR, null);
		if (StringUtils.isNotBlank(colorString)) {
			try {
				return new SubtitleColor(colorString);
			} catch (InvalidArgumentException e) {
				LOGGER.error("Using default subtitle color: {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
		return new SubtitleColor(0xFF, 0xFF, 0xFF);
	}

	public void setSubsColor(Color color) {
		setSubsColor(new SubtitleColor(color));
	}

	public void setSubsColor(SubtitleColor color) {
		if (color.getAlpha() != 0xFF) {
			configuration.setProperty(KEY_SUBS_COLOR, color.get0xRRGGBBAA());
		} else {
			configuration.setProperty(KEY_SUBS_COLOR, color.get0xRRGGBB());
		}
	}

	public boolean isFix25FPSAvMismatch() {
		return getBoolean(KEY_FIX_25FPS_AV_MISMATCH, false);
	}

	public void setFix25FPSAvMismatch(boolean value) {
		configuration.setProperty(KEY_FIX_25FPS_AV_MISMATCH, value);
	}

	public int getVideoTranscodeStartDelay() {
		return getInt(KEY_VIDEOTRANSCODE_START_DELAY, 6);
	}

	public void setVideoTranscodeStartDelay(int value) {
		configuration.setProperty(KEY_VIDEOTRANSCODE_START_DELAY, value);
	}

	public boolean isAudioResample() {
		return getBoolean(KEY_AUDIO_RESAMPLE, true);
	}

	public void setAudioResample(boolean value) {
		configuration.setProperty(KEY_AUDIO_RESAMPLE, value);
	}

	public boolean isIgnoreTheWordAandThe() {
		return getBoolean(KEY_IGNORE_THE_WORD_A_AND_THE, true);
	}

	public void setIgnoreTheWordAandThe(boolean value) {
		configuration.setProperty(KEY_IGNORE_THE_WORD_A_AND_THE, value);
	}

	public boolean isPrettifyFilenames() {
		return getBoolean(KEY_PRETTIFY_FILENAMES, false);
	}

	public void setPrettifyFilenames(boolean value) {
		configuration.setProperty(KEY_PRETTIFY_FILENAMES, value);
	}

	public boolean isUseInfoFromIMDb() {
		return getBoolean(KEY_USE_IMDB_INFO, false) && isPrettifyFilenames();
	}

	public void setUseInfoFromIMDb(boolean value) {
		configuration.setProperty(KEY_USE_IMDB_INFO, value);
	}

	public boolean isRunWizard() {
		return getBoolean(KEY_RUN_WIZARD, true);
	}

	public void setRunWizard(boolean value) {
		configuration.setProperty(KEY_RUN_WIZARD, value);
	}

	public boolean isHideNewMediaFolder() {
		return getBoolean(KEY_HIDE_NEW_MEDIA_FOLDER, false);
	}

	public void setHideNewMediaFolder(final boolean value) {
		this.configuration.setProperty(KEY_HIDE_NEW_MEDIA_FOLDER, value);
	}

	public boolean isHideRecentlyPlayedFolder() {
		return getBoolean(PmsConfiguration.KEY_HIDE_RECENTLY_PLAYED_FOLDER, false);
	}

	public void setHideRecentlyPlayedFolder(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_RECENTLY_PLAYED_FOLDER, value);
	}

	/**
	 * Returns the name of the renderer to fall back on when header matching
	 * fails. DMS will recognize the configured renderer instead of "Unknown
	 * renderer". Default value is "", which means DMS will return the unknown
	 * renderer when no match can be made.
	 *
	 * @return The name of the renderer DMS should fall back on when header
	 *         matching fails.
	 * @see #isRendererForceDefault()
	 */
	public String getRendererDefault() {
		return getString(KEY_RENDERER_DEFAULT, "");
	}

	/**
	 * Sets the name of the renderer to fall back on when header matching
	 * fails. DMS will recognize the configured renderer instead of "Unknown
	 * renderer". Set to "" to make DMS return the unknown renderer when no
	 * match can be made.
	 *
	 * @param value The name of the renderer to fall back on. This has to be
	 *              <code>""</code> or a case insensitive match with the name
	 *              used in any render configuration file.
	 * @see #setRendererForceDefault(boolean)
	 */
	public void setRendererDefault(String value) {
		configuration.setProperty(KEY_RENDERER_DEFAULT, value);
	}

	/**
	 * Gets whether or not the default {@link RendererConfiguration} should be
	 * enforced. That means that no recognition is attempted, and a single
	 * {@link RendererConfiguration} is applied to all detected renderers.
	 *
	 * @return {@code true} if the default {@link RendererConfiguration} should
	 *         be enforced, {@code false} otherwise.
	 */
	public boolean isRendererForceDefault() {
		return getBoolean(KEY_RENDERER_FORCE_DEFAULT, false);
	}

	/**
	 * Set to true when DMS should not try to guess connecting renderers
	 * and instead force picking the defined fallback renderer. Set to false
	 * to make DMS attempt to recognize connecting renderers by their headers.
	 *
	 * @param value True when the fallback renderer should always be picked.
	 * @see #setRendererDefault(String)
	 */
	public void setRendererForceDefault(boolean value) {
		configuration.setProperty(KEY_RENDERER_FORCE_DEFAULT, value);
	}

	public String getVirtualFolders() {
		return getString(KEY_VIRTUAL_FOLDERS, "");
	}

	public String getVirtualFoldersFile() {
		return getString(KEY_VIRTUAL_FOLDERS_FILE, "");
	}

	public Path getConfigurationFile() {
		return CONFIGURATION_FILE;
	}

	public Path getProfileFolder() {
		return PROFILE_FOLDER;
	}

	public boolean isWebConfPathSpecified() {
		return isNotBlank(configuration.getString(KEY_WEB_CONF_PATH));
	}

	/**
	 * Returns the absolute path to the WEB.conf file. By default
	 * this is <pre>PROFILE_DIRECTORY + File.pathSeparator + WEB.conf</pre>,
	 * but it can be overridden via the <pre>web_conf</pre> profile option.
	 * The existence of the file is not checked.
	 *
	 * @return the path to the WEB.conf file.
	 */
	public String getWebConfPath() {
		// Initialise this here rather than in the constructor
		// or statically so that custom settings are logged
		// to the logfile/Logs tab.
		if (WEB_CONF_PATH == null) {
			WEB_CONF_PATH = FileUtil.resolvePathWithDefaults(
				configurationReader.getNonBlankConfigurationString(KEY_WEB_CONF_PATH, null, false),
				PROFILE_FOLDER,
				DEFAULT_WEB_CONF_FILENAME
			).toString();
		}

		return getString(KEY_WEB_CONF_PATH, WEB_CONF_PATH);
	}

	public String getPluginFolder() {
		return getString(KEY_PLUGIN_FOLDER, "plugins");
	}

	public void setPluginFolder(String value) {
		configuration.setProperty(KEY_PLUGIN_FOLDER, value);
	}

	public String getProfileName() {
		return getString(KEY_PROFILE_NAME, COMPUTER_NAME);
	}

	public int getUpnpPort() {
		return getInt(KEY_UPNP_PORT, 1900);
	}

	public String getUuid() {
		return getString(KEY_UUID, null);
	}

	public void setUuid(String value){
		configuration.setProperty(KEY_UUID, value);
	}

	public void addConfigurationListener(ConfigurationListener l) {
		((PropertiesConfiguration)configuration).addConfigurationListener(l);
	}

	public void removeConfigurationListener(ConfigurationListener l) {
		((PropertiesConfiguration)configuration).removeConfigurationListener(l);
	}

	public boolean getFolderLimit() {
		return getBoolean(KEY_FOLDER_LIMIT, false);
	}

	public String getScriptDir() {
		return getString(KEY_SCRIPT_DIR, null);
	}

	public String getPluginPurgeAction() {
		return getString(KEY_PLUGIN_PURGE_ACTION, "delete");
	}

	public boolean getSearchFolder() {
		return getBoolean(KEY_SEARCH_FOLDER, false);
	}

	public boolean getSearchInFolder() {
		return getBoolean(KEY_SEARCH_IN_FOLDER, false) && getSearchFolder();
	}

	public int getSearchDepth() {
		int ret = (getBoolean(KEY_SEARCH_RECURSE, true) ? 100 : 2);
	   	return getInt(KEY_SEARCH_RECURSE_DEPTH, ret);
	}

	public void reload() {
		try {
			((PropertiesConfiguration)configuration).refresh();
		} catch (ConfigurationException e) {
			LOGGER.error(null, e);
		}
	}

	/**
	 * Retrieve the name of the folder used to select subtitles, audio channels, chapters, engines &amp;c.
	 * Defaults to the localized version of <pre>#--TRANSCODE--#</pre>.
	 * @return The folder name.
	 */
	public String getTranscodeFolderName() {
		return getString(KEY_TRANSCODE_FOLDER_NAME, Messages.getString("TranscodeVirtualFolder.0"));
	}

	/**
	 * Set a custom name for the <pre>#--TRANSCODE--#</pre> folder.
	 * @param name The folder name.
	 */
	public void setTranscodeFolderName(String name) {
		configuration.setProperty(KEY_TRANSCODE_FOLDER_NAME, name);
	}

	/**
	 * @return the {@link GUICloseAction} to use.
	 */
	public GUICloseAction getGUICloseAction() {
		return GUICloseAction.typeOf(getString(KEY_GUI_CLOSE_ACTION, GUICloseAction.ASK.getValue()));
	}

	/**
	 * Sets what action should be taken when the GUI window is closed.
	 *
	 * @param value the {@link GUICloseAction} to use.
	 */
	public void setGUICloseAction(GUICloseAction value) {
		if (value == null) {
			configuration.setProperty(KEY_GUI_CLOSE_ACTION, "");
		} else {
			configuration.setProperty(KEY_GUI_CLOSE_ACTION, value.getValue());
		}
	}

	/**
	 * Get the state of the GUI log tab "Case sensitive" check box
	 * @return true if enabled, false if disabled
	 */
	public boolean getGUILogSearchCaseSensitive() {
		return getBoolean(KEY_GUI_LOG_SEARCH_CASE_SENSITIVE, false);
	}

	/**
	 * Set the state of the GUI log tab "Case sensitive" check box
	 * @param value true if enabled, false if disabled
	 */
	public void setGUILogSearchCaseSensitive(boolean value) {
		configuration.setProperty(KEY_GUI_LOG_SEARCH_CASE_SENSITIVE, value);
	}

	/**
	 * Get the state of the GUI log tab "Multiline" check box
	 * @return true if enabled, false if disabled
	 */
	public boolean getGUILogSearchMultiLine() {
		return getBoolean(KEY_GUI_LOG_SEARCH_MULTILINE, false);
	}

	/**
	 * Set the state of the GUI log tab "Multiline" check box
	 * @param value true if enabled, false if disabled
	 */
	public void setGUILogSearchMultiLine(boolean value) {
		configuration.setProperty(KEY_GUI_LOG_SEARCH_MULTILINE, value);
	}

	/**
	 * Get the state of the GUI log tab "RegEx" check box
	 * @return true if enabled, false if disabled
	 */
	public boolean getGUILogSearchRegEx() {
		return getBoolean(KEY_GUI_LOG_SEARCH_USE_REGEX, false);
	}

	/**
	 * Set the state of the GUI log tab "RegEx" check box
	 * @param value true if enabled, false if disabled
	 */
	public void setGUILogSearchRegEx(boolean value) {
		configuration.setProperty(KEY_GUI_LOG_SEARCH_USE_REGEX, value);
	}

	/* Start without external netowrk (increase startup speed) */
	public static final String KEY_EXTERNAL_NETWORK = "external_network";

	public boolean getExternalNetwork() {
		return getBoolean(KEY_EXTERNAL_NETWORK, true);
	}

	public void setExternalNetwork(boolean b) {
		configuration.setProperty(KEY_EXTERNAL_NETWORK, b);
	}

	/* Credential path handling */
	public static final String KEY_CRED_PATH = "cred.path";

	public void initCred() throws IOException {
		File credFile = getCredFile();

		if (!credFile.exists()) {
			// Create an empty file and save the path if needed
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(credFile), StandardCharsets.UTF_8))) {
				writer.write("# Add credentials to the file");
				writer.newLine();
				writer.write("# on the format tag=user,password");
				writer.newLine();
				writer.write("# For example:");
				writer.newLine();
				writer.write("# channels.xxx=name,secret");
				writer.newLine();
			}
			// Save the path if we got here
			configuration.setProperty(KEY_CRED_PATH, credFile.getAbsolutePath());
			try {
				((PropertiesConfiguration)configuration).save();
			} catch (ConfigurationException e) {
				LOGGER.warn("An error occurred while saving configuration: {}", e.getMessage());
			}
		}
	}

	/**
	 * @deprecated Use {@link #getCredFile()} instead.
	 */
	@Deprecated
	public String getCredPath() {
		return getCredFile().getAbsolutePath();
	}

	public File getCredFile() {
		String path = getString(KEY_CRED_PATH, "");
		if (path != null && !path.trim().isEmpty()) {
			return new File(path);
		}
		return new File(getProfileFolder().toFile(), DEFAULT_CREDENTIALS_FILENAME);
	}

	public int getATZLimit() {
		int tmp = getInt(KEY_ATZ_LIMIT, 10000);
		if (tmp <= 2) {
			// this is silly, ignore
			tmp = 10000;
		}
		return tmp;
	}

	public void setATZLimit(int val) {
		if (val <= 2) {
			// clear prop
			configuration.clearProperty(KEY_ATZ_LIMIT);
			return;
		}
		configuration.setProperty(KEY_ATZ_LIMIT, val);
	}

	public void setATZLimit(String str) {
		try {
			setATZLimit(Integer.parseInt(str));
		} catch (Exception e) {
			setATZLimit(0);
		}
	}

	public String getDataDir() {
		return getProfileFolder() + File.separator + "data";
	}

	public String getDataFile(String str) {
		return getDataDir() + File.separator + str;
	}

	private String KEY_URL_RES_ORDER = "url_resolve_order";

	public String[] getURLResolveOrder() {
		return getString(KEY_URL_RES_ORDER, "").split(",");
	}

	public boolean isHideLiveSubtitlesFolder() {
		return getBoolean(KEY_HIDE_LIVE_SUBTITLES_FOLDER, true);
	}

	public void setHideLiveSubtitlesFolder(boolean value) {
		configuration.setProperty(KEY_HIDE_LIVE_SUBTITLES_FOLDER, value);
	}

	public int liveSubtitlesLimit() {
		return getInt(KEY_LIVE_SUBTITLES_LIMIT, 20);
	}

	public boolean isLiveSubtitlesKeep() {
		return getBoolean(KEY_LIVE_SUBTITLES_KEEP, false);
	}

	public int getLiveSubtitlesTimeout() {
		return getInt(KEY_LIVE_SUBTITLES_TMO, 0) * 24 * 3600 * 1000;
	}

	public void setLiveSubtitlesTimeout(int t) {
		configuration.setProperty(KEY_LIVE_SUBTITLES_TMO, t);
	}

	public boolean getLoggingBuffered() {
		return getBoolean(KEY_LOGGING_BUFFERED, false);
	}

	public void setLoggingBuffered(boolean value) {
		configuration.setProperty(KEY_LOGGING_BUFFERED, value);
	}

	public LogLevel getLoggingFilterConsole() {
		return LogLevel.typeOf(getString(KEY_LOGGING_FILTER_CONSOLE, "Trace"), LogLevel.TRACE);
	}

	public void setLoggingFilterConsole(LogLevel value) {
		configuration.setProperty(KEY_LOGGING_FILTER_CONSOLE, value == null ? "" : value.toString(false));
	}

	public LogLevel getLoggingFilterLogsTab() {
		return LogLevel.typeOf(getString(KEY_LOGGING_FILTER_LOGS_TAB, "Info"), LogLevel.INFO);
	}

	public void setLoggingFilterLogsTab(LogLevel value) {
		configuration.setProperty(KEY_LOGGING_FILTER_LOGS_TAB, value == null ? "" : value.toString(false));
	}

	public int getLoggingLogsTabLinebuffer() {
		return Math.min(Math.max(getInt(KEY_LOGGING_LOGS_TAB_LINEBUFFER, 1000), LOGGING_LOGS_TAB_LINEBUFFER_MIN),LOGGING_LOGS_TAB_LINEBUFFER_MAX);
	}

	public void setLoggingLogsTabLinebuffer(int value) {
		value = Math.min(Math.max(value, LOGGING_LOGS_TAB_LINEBUFFER_MIN),LOGGING_LOGS_TAB_LINEBUFFER_MAX);
		configuration.setProperty(KEY_LOGGING_LOGS_TAB_LINEBUFFER, value);
	}

	public String getLoggingSyslogFacility() {
		return getString(KEY_LOGGING_SYSLOG_FACILITY, "USER");
	}

	public void setLoggingSyslogFacility(String value) {
		configuration.setProperty(KEY_LOGGING_SYSLOG_FACILITY, value);
	}

	public void setLoggingSyslogFacilityDefault() {
		setLoggingSyslogFacility("USER");
	}

	public String getLoggingSyslogHost() {
		return getString(KEY_LOGGING_SYSLOG_HOST, "");
	}

	public void setLoggingSyslogHost(String value) {
		configuration.setProperty(KEY_LOGGING_SYSLOG_HOST, value);
	}

	public int getLoggingSyslogPort() {
		int i = getInt(KEY_LOGGING_SYSLOG_PORT, 514);
		if (i < 1 || i > 65535) {
			return 514;
		}
		return i;
	}

	public void setLoggingSyslogPort(int value) {
		if (value < 1 || value > 65535) {
			setLoggingSyslogPortDefault();
		} else {
			configuration.setProperty(KEY_LOGGING_SYSLOG_PORT, value);
		}
	}

	public void setLoggingSyslogPortDefault() {
		setLoggingSyslogPort(514);
	}

	public boolean getLoggingUseSyslog() {
		return getBoolean(KEY_LOGGING_USE_SYSLOG, false);
	}

	public void setLoggingUseSyslog(boolean value) {
		configuration.setProperty(KEY_LOGGING_USE_SYSLOG, value);
	}

	/**
	 * Gets the database cache size in KiB.
	 *
	 * @return The database cache size in KiB.
	 */
	public int getDatabaseCacheSize() {
		long jvmMemory = Runtime.getRuntime().maxMemory();
		Object cacheSizeObject = configuration.getProperty(KEY_DATABASE_CACHE_SIZE);
		String cacheSizeString = cacheSizeObject == null ? null :
			cacheSizeObject instanceof String ?
				(String) cacheSizeObject :
				cacheSizeObject.toString();
		long size;
		String unit;
		if (isBlank(cacheSizeString)) {
			size = Long.MIN_VALUE;
			unit = null;
		} else {
			Pair<Number, String> cacheSize = ConversionUtil.parseNumberWithUnit(
				cacheSizeString,
				null,
				0,
				RoundingMode.HALF_EVEN,
				null,
				UnitPrefix.MEBI,
				"B",
				true,
				false,
				false
			);
			if (cacheSize == null || cacheSize.getFirst() == null) {
				LOGGER.warn("Ignoring invalid database cache size of \"{}\"", cacheSizeString);
				size = Long.MIN_VALUE;
				unit = null;
			} else if (cacheSize.getFirst().longValue() == 0) {
				// Cache disabled, use int to ignore fractional values
				return 0;
			} else {
				size = cacheSize.getFirst().longValue();
				unit = cacheSize.getSecond().toUpperCase(Locale.ROOT);
			}
			if (size != Long.MIN_VALUE && size < 0) {
				LOGGER.warn("Ignoring negative database cache size \"{}\"", cacheSizeString);
				size = Long.MIN_VALUE;
			}
			if (isNotBlank(unit) && !"B".equals(unit) && !"b".equals(unit) && !"%".equals(unit)) {
				LOGGER.warn("Ignoring database cache size with invalid unit \"{}\"", unit);
				size = Long.MIN_VALUE;
			}
		}

		if (size == Long.MIN_VALUE || "%".equals(unit)) {
			// Percent or default
			if (jvmMemory == Long.MAX_VALUE) {
				if (size == Long.MIN_VALUE) {
					LOGGER.warn(
						"Unable to apply default database cache size since no JVM heap limit is defined, disabling cache"
					);
				} else {
					LOGGER.warn(
						"Unable to apply database cache size in percent since no JVM heap limit is defined, disabling cache"
					);
				}
				return 0;
			}
			if (size > 50L) {
				LOGGER.warn(
					"Reducing the database cache size from {}% to the maximum allowed size of 50%",
					size
				);
				size = 50L;
			}
			if (size == Long.MIN_VALUE) {
				// Set default size, 10% if heap is less than 1 GiB, 20% otherwise.
				size = jvmMemory < 1073741824 ? 10 : 20;
				LOGGER.debug("Using default database cache size of {}%", size);
			}

			return (int) ((jvmMemory * size) / 102400); // Percent to KiB
		}
		// Bytes
		if (jvmMemory != Long.MAX_VALUE && size > jvmMemory / 2) {
			LOGGER.warn(
				"Reducing database cache size from {} to {} to stay within 50% of available JVM heap size",
				ConversionUtil.formatBytes(size, true),
				ConversionUtil.formatBytes(jvmMemory / 2, true)
			);
			size = jvmMemory / 2;
		}
		return (int) (size / 1024); // Bytes to KiB
	}

	/**
	 * Returns whether database logging is enabled. The returned value is
	 * {@code true} if either the value is {@code true} or a command line
	 * argument has forced it to {@code true}.
	 *
	 * @return {@code true} if database logging is enabled, {@code false}
	 *         otherwise.
	 */
	public boolean getDatabaseLogging() {
		boolean dbLog = getBoolean(KEY_LOG_DATABASE, false);
		return dbLog || PMS.getLogDB();
	}

	public boolean isVLCHardwareAcceleration() {
		return getBoolean(KEY_VLC_HARDWARE_ACCELERATION, true);
	}

	public void setVLCHardwareAcceleration(boolean value) {
		configuration.setProperty(KEY_VLC_HARDWARE_ACCELERATION, value);
	}

	public boolean isVlcExperimentalCodecs() {
		return getBoolean(KEY_VLC_USE_EXPERIMENTAL_CODECS, false);
	}

	public void setVlcExperimentalCodecs(boolean value) {
		configuration.setProperty(KEY_VLC_USE_EXPERIMENTAL_CODECS, value);
	}

	public boolean isVlcAudioSyncEnabled() {
		return getBoolean(KEY_VLC_AUDIO_SYNC_ENABLED, false);
	}

	public void setVlcAudioSyncEnabled(boolean value) {
		configuration.setProperty(KEY_VLC_AUDIO_SYNC_ENABLED, value);
	}

	public int getVlcMaxThreads() {
		return getInt(KEY_VLC_MAX_THREADS, Runtime.getRuntime().availableProcessors());
	}

	public void setVlcMaxThreads(int value) {
		configuration.setProperty(KEY_VLC_MAX_THREADS, value);
	}

	public int getVlcEffectiveMaxThreads() {
		return Math.max(Math.min(
			getVlcMaxThreads(),
			Runtime.getRuntime().availableProcessors()
		), 1);
	}

	public boolean isVlcSubtitleEnabled() {
		return getBoolean(KEY_VLC_SUBTITLE_ENABLED, true);
	}

	public void setVlcSubtitleEnabled(boolean value) {
		configuration.setProperty(KEY_VLC_SUBTITLE_ENABLED, value);
	}

	public String getVlcScale() {
		return getString(KEY_VLC_SCALE, "1.0");
	}

	public void setVlcScale(String value) {
		configuration.setProperty(KEY_VLC_SCALE, value);
	}

	public boolean getVlcSampleRateOverride() {
		return getBoolean(KEY_VLC_SAMPLE_RATE_OVERRIDE, false);
	}

	public void setVlcSampleRateOverride(boolean value) {
		configuration.setProperty(KEY_VLC_SAMPLE_RATE_OVERRIDE, value);
	}

	public String getVlcSampleRate() {
		return getString(KEY_VLC_SAMPLE_RATE, "48000");
	}

	public void setVlcSampleRate(String value) {
		configuration.setProperty(KEY_VLC_SAMPLE_RATE, value);
	}

	public boolean isResumeEnabled()  {
		return getBoolean(KEY_RESUME, true);
	}

	public void setResume(boolean value) {
		configuration.setProperty(KEY_RESUME, value);
	}

	@Deprecated
	public int getMinPlayTime() {
		return getMinimumWatchedPlayTime();
	}

	public int getMinimumWatchedPlayTime() {
		return getInt(KEY_MIN_PLAY_TIME, 30000);
	}

	public int getMinimumWatchedPlayTimeSeconds() {
		return getMinimumWatchedPlayTime() / 1000;
	}

	public int getMinPlayTimeWeb() {
		return getInt(KEY_MIN_PLAY_TIME_WEB, getMinimumWatchedPlayTime());
	}

	public int getMinPlayTimeFile() {
		return getInt(KEY_MIN_PLAY_TIME_FILE, getMinimumWatchedPlayTime());
	}

	public int getResumeRewind() {
		return getInt(KEY_RESUME_REWIND, 17000);
	}

	public double getResumeBackFactor() {
		int percent = getInt(KEY_RESUME_BACK, 92);
		if (percent > 97) {
			percent = 97;
		}
		if (percent < 10) {
			percent = 10;
		}
		return (percent / 100.0);
	}

	public int getResumeKeepTime() {
		return getInt(KEY_RESUME_KEEP_TIME, 0);
	}

	public boolean hideSubsInfo() {
		return getBoolean(KEY_HIDE_SUBS_INFO, false);
	}

	/**
	 * Whether the profile name should be appended to the server name when
	 * displayed on the renderer
	 *
	 * @return True if the profile name should be appended.
	 */
	public boolean isAppendProfileName() {
		return getBoolean(KEY_APPEND_PROFILE_NAME, true);
	}

	/**
	 * Set whether the profile name should be appended to the server name
	 * when displayed on the renderer
	 *
	 * @param value Set to true if the profile name should be appended.
	 */
	public void setAppendProfileName(boolean value) {
		configuration.setProperty(KEY_APPEND_PROFILE_NAME, value);
	}

	public int getDepth3D() {
		return getInt(KEY_3D_SUBTITLES_DEPTH, 0);
	}

	public void setDepth3D(int value) {
		configuration.setProperty(KEY_3D_SUBTITLES_DEPTH, value);
	}

	/**
	 * Web stuff
	 */
	protected static final String KEY_NO_FOLDERS = "no_shared";
	protected static final String KEY_WEB_HTTPS = "web_https";
	protected static final String KEY_WEB_PORT = "web_port";
	protected static final int WEB_MAX_THREADS = 100;

	public boolean getNoFolders(String tag) {
		if (tag == null) {
			return getBoolean(KEY_NO_FOLDERS, false);
		}
		String x = (tag.toLowerCase() + ".no_shared").replaceAll(" ", "_");
		return getBoolean(x, false);
	}

	public boolean getWebHttps() {
		return getBoolean(KEY_WEB_HTTPS, false);
	}

	@Nonnull
	public File getWebPath() {
		File path = new File(getString(KEY_WEB_PATH, "web"));
		if (!path.exists() || !new File(path, "start.html").exists()) {
			// Make it work while debugging
			File debugPath = new File("src/main/external-resources/" + getString(KEY_WEB_PATH, "web"));
			if (debugPath.exists()) {
				return debugPath;
			}
		}
		return path;
	}

	public boolean isWebAuthenticate() {
		return getBoolean(KEY_WEB_AUTHENTICATE, false);
	}

	public int getWebThreads() {
		int x = getInt(KEY_WEB_THREADS, 30);
		return (x > WEB_MAX_THREADS ? WEB_MAX_THREADS : x);
	}

	public boolean isWebMp4Trans() {
		return getBoolean(KEY_WEB_MP4_TRANS, false);
	}

	public String getBumpAddress() {
		return getString(KEY_BUMP_ADDRESS, "");
	}

	public void setBumpAddress(String value) {
		configuration.setProperty(KEY_BUMP_ADDRESS, value);
	}

	public String getBumpJS(String fallback) {
		return getString(KEY_BUMP_JS, fallback);
	}

	public String getBumpSkinDir(String fallback) {
		return getString(KEY_BUMP_SKIN_DIR, fallback);
	}

	/**
	 * Default port for the WEB interface.
	 */
	public int getWebPort() {
		return getInt(KEY_WEB_PORT, DEFAULT_WEBINTERFACE_PORT);
	}

	public boolean useWebInterface() {
		return getBoolean(KEY_WEB_ENABLE, true);
	}

	public boolean isAutomaticMaximumBitrate() {
		return getBoolean(KEY_AUTOMATIC_MAXIMUM_BITRATE, false);
	}

	public void setAutomaticMaximumBitrate(boolean b) {
		if (!isAutomaticMaximumBitrate() && b) {
			// get all bitrates from renderers
			RendererConfiguration.calculateAllSpeeds();
		}
		configuration.setProperty(KEY_AUTOMATIC_MAXIMUM_BITRATE, b);
	}

	public boolean isSpeedDbg() {
		return getBoolean(KEY_SPEED_DBG, false);
	}

	public boolean getAutoDiscover() {
		return getBoolean(KEY_AUTOMATIC_DISCOVER, false);
	}

	public int mediaLibrarySort() {
		return getInt(KEY_MEDIA_LIB_SORT, UMSUtils.SORT_NO_SORT);
	}

	public boolean getWebAutoCont(MediaType mediaType) {
		String key = KEY_WEB_CONT_VIDEO;
		boolean def = false;
		if (mediaType == MediaType.AUDIO) {
			key = KEY_WEB_CONT_AUDIO;
			def = true;
		} else if (mediaType == MediaType.IMAGE) {
			key = KEY_WEB_CONT_IMAGE;
			def = false;
		}
		return getBoolean(key, def);
	}

	public boolean getWebAutoLoop(MediaType mediaType) {
		String key = KEY_WEB_LOOP_VIDEO;
		if (mediaType == MediaType.AUDIO) {
			key = KEY_WEB_LOOP_AUDIO;
		}
		if (mediaType == MediaType.IMAGE) {
			key = KEY_WEB_LOOP_IMAGE;
		}
		return getBoolean(key, false);
	}

	public int getWebImgSlideDelay() {
		return getInt(KEY_WEB_IMAGE_SLIDE, 0);
	}

	public String getWebSize() {
		return getString(KEY_WEB_SIZE, "");
	}

	public int getWebHeight() {
		return getInt(KEY_WEB_HEIGHT, 0);
	}

	public int getWebWidth() {
		return getInt(KEY_WEB_WIDTH, 0);
	}

	public boolean getWebFlash() {
		return getBoolean(KEY_WEB_FLASH, false);
	}

	public boolean getWebChrome() {
		return getBoolean(KEY_WEB_CHROME_TRICK, false);
	}

	public boolean getWebFirefoxLinuxMp4() {
		return getBoolean(KEY_WEB_FIREFOX_LINUX_MP4, false);
	}

	public boolean getWebSubs() {
		return getBoolean(KEY_WEB_SUBS_TRANS, false);
	}

	public String getBumpAllowedIps() {
		return getString(KEY_BUMP_IPS, "");
	}

	public String getWebTranscode() {
		return getString(KEY_WEB_TRANSCODE, null);
	}

	public int getWebLowSpeed() {
		return getInt(KEY_WEB_LOW_SPEED, 0);
	}

	public boolean useWebLang() {
		return getBoolean(KEY_WEB_BROWSE_LANG, false);
	}

	public boolean useWebSubLang() {
		return getBoolean(KEY_WEB_BROWSE_SUB_LANG, false);
	}

	public boolean useWebControl() {
		return getBoolean(KEY_WEB_CONTROL, true);
	}

	public boolean useCode() {
		return getBoolean(KEY_CODE_USE, true);
	}

	public int getCodeValidTmo() {
		return (getInt(KEY_CODE_TMO, 4 * 60) * 60 * 1000);
	}

	public boolean isShowCodeThumbs() {
		return getBoolean(KEY_CODE_THUMBS, true);
	}

	public int getCodeCharSet() {
		int cs = getInt(KEY_CODE_CHARS, CodeEnter.DIGITS);
		if (cs < CodeEnter.DIGITS || cs > CodeEnter.BOTH) {
			// ensure we go a legal value
			cs = CodeEnter.DIGITS;
		}
		return cs;
	}

	public boolean isDynamicPls() {
		return getBoolean(KEY_DYNAMIC_PLS, false);
	}

	public boolean isDynamicPlsAutoSave() {
	   	return getBoolean(KEY_DYNAMIC_PLS_AUTO_SAVE, false);
	}

	public String getDynamicPlsSavePath() {
		String path = getString(KEY_DYNAMIC_PLS_SAVE_PATH, "");
		if (StringUtils.isEmpty(path)) {
			path = getDataFile("dynpls");
			// ensure that this path exists
			new File(path).mkdirs();
		}
		return path;
	}

	public String getDynamicPlsSaveFile(String str) {
		return getDynamicPlsSavePath() + File.separator + str;
	}

	public boolean isHideSavedPlaylistFolder() {
		return getBoolean(KEY_DYNAMIC_PLS_HIDE, false);
	}

	public boolean isAutoContinue() {
		return getBoolean(KEY_PLAYLIST_AUTO_CONT, false);
	}

	public boolean isAutoAddAll() {
		return getBoolean(KEY_PLAYLIST_AUTO_ADD_ALL, false);
	}

	public String getAutoPlay() {
		return getString(KEY_PLAYLIST_AUTO_PLAY, null);
	}

	public boolean useChromecastExt() {
		return getBoolean(KEY_CHROMECAST_EXT, false);
	}

	public boolean isChromecastDbg() {
		return getBoolean(KEY_CHROMECAST_DBG, false);
	}

	/**
	 * Enable the automatically saving of modified properties to the disk.
	 */
	public void setAutoSave() {
		((PropertiesConfiguration) configuration).setAutoSave(true);
	}

	public boolean isUpnpEnabled() {
		return getBoolean(KEY_UPNP_ENABLED, true);
	}

	public LogLevel getRootLogLevel() {
		return LogLevel.typeOf(getString(KEY_ROOT_LOG_LEVEL, "Info"), LogLevel.INFO);
	}

	public void setRootLogLevel(LogLevel level) {
		configuration.setProperty(KEY_ROOT_LOG_LEVEL, level.toString(false));
	}

	public boolean isShowSplashScreen() {
		return getBoolean(KEY_SHOW_SPLASH_SCREEN, true);
	}

	public void setShowSplashScreen(boolean value) {
		configuration.setProperty(KEY_SHOW_SPLASH_SCREEN, value);
	}

	public boolean isInfoDbRetry() {
		return getBoolean(KEY_INFO_DB_RETRY, false);
	}

	public int getAliveDelay() {
		return getInt(KEY_ALIVE_DELAY, 0);
	}

	public enum GUICloseAction {

		/** Ask the user what to do */
		ASK,

		/** Hide the GUI */
		HIDE,

		/** Quit */
		QUIT;

		public String getValue() {
			return name().toLowerCase(Locale.ROOT);
		}
		@Override
		public String toString() {
			switch (this) {
				case ASK:
					return Messages.getString("Generic.Ask");
				case HIDE:
					return Messages.getString("GeneralTab.HideWindowOption");
				case QUIT:
					return Messages.getString("LooksFrame.5");
				default:
					throw new IllegalStateException("Unimplemented enum value: " + name());
			}
		}

		public static GUICloseAction typeOf(String value) {
			if (value == null) {
				return ASK;
			}
			value = value.trim().toLowerCase(Locale.ROOT);
			switch (value) {
				case "hide":
				case "minimize":
					return HIDE;
				case "exit":
				case "quit":
				case "stop":
					return QUIT;
				default:
					return ASK;
			}
		}
	}
}
