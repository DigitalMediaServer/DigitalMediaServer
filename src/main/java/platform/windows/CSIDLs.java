package platform.windows;


/**
 * This is an utility class containing Windows {@code CSIDL} constants.
 *
 * @author Nadahar
 */
public class CSIDLs {

	/** Shared Music ({@link KnownFolders#FOLDERID_PublicMusic}) */
	public static final int CSIDL_COMMON_MUSIC = 0x0035;

	/** Shared Pictures ({@link KnownFolders#FOLDERID_PublicPictures}) */
	public static final int CSIDL_COMMON_PICTURES = 0x0036;

	/** Shared Video ({@link KnownFolders#FOLDERID_PublicVideos}) */
	public static final int CSIDL_COMMON_VIDEO = 0x0037;

	/** Desktop ({@link KnownFolders#FOLDERID_Desktop}) */
	public static final int CSIDL_DESKTOP = 0x0000;

	/** My Music ({@link KnownFolders#FOLDERID_Music}) */
	public static final int CSIDL_MYMUSIC = 0x000d;

	/** My Pictures ({@link KnownFolders#FOLDERID_Pictures}) */
	public static final int CSIDL_MYPICTURES = 0x0027;

	/** My Videos ({@link KnownFolders#FOLDERID_Videos}) */
	public static final int CSIDL_MYVIDEO = 0x000e;

	/**
	 * Not to be instantiated.
	 */
	private CSIDLs() {
	}

}
