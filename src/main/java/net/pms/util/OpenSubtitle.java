/*
 * Universal Media Server, for streaming any media to DLNA
 * compatible renderers based on the http://www.ps3mediaserver.org.
 * Copyright (C) 2012 UMS developers.
 *
 * This program is a free software; you can redistribute it and/or
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

package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSubtitle {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubtitle.class);
	private static final String SUB_DIR = "subs";
	private static final String UA = "Digital Media Server v1";
	private static final long TOKEN_AGE_TIME = 10 * 60 * 1000; // 10 mins
	//private static final long SUB_FILE_AGE = 14 * 24 * 60 * 60 * 1000; // two weeks

	/**
	 * Size of the chunks that will be hashed in bytes (64 KB)
	 */
	private static final int HASH_CHUNK_SIZE = 64 * 1024;

	private static final String OPENSUBS_URL = "http://api.opensubtitles.org/xml-rpc";
	private static final ReentrantReadWriteLock tokenLock = new ReentrantReadWriteLock();
	private static String token = null;
	private static long tokenAge;

	//TODO: (Nad) Remove
	public static String getHash(File file) throws IOException {
		return getHash(file.toPath());
	}

	/**
	 * Gets the <a href=
	 * "http://trac.opensubtitles.org/projects/opensubtitles/wiki/HashSourceCodes"
	 * >OpenSubtitles hash</a> for the specified {@link Path} by first trying to
	 * extract it from the filename and if that doesn't work calculate it with
	 * {@link #computeHash(Path)}.
	 *
	 * @param file the {@link Path} for which to get the hash.
	 * @return The OpenSubtitles hash or {@code null}.
	 * @throws IOException If an I/O error occurs during the operation.
	 */
	public static String getHash(Path file) throws IOException {
		String hash = ImdbUtil.extractOSHash(file);
		if (isBlank(hash)) {
			hash = computeHash(file);
		}
		LOGGER.debug("OpenSubtitles hash for \"{}\" is {}", file.getFileName(), hash);
		return hash;
	}

	/**
	 * Calculates the <a href=
	 * "http://trac.opensubtitles.org/projects/opensubtitles/wiki/HashSourceCodes"
	 * >OpenSubtitles hash</a> for the specified {@link Path}.
	 *
	 * @param file the {@link Path} for which to calculate the hash.
	 * @return The calculated OpenSubtitles hash or {@code null}.
	 * @throws IOException If an I/O error occurs during the operation.
	 */
	public static String computeHash(Path file) throws IOException {
		if (!Files.isRegularFile(file)) {
			return null;
		}

		long size = Files.size(file);
		long chunkSizeForFile = Math.min(HASH_CHUNK_SIZE, size);

		try (FileChannel fileChannel = FileChannel.open(file)) {
			long head = computeHashForChunk(fileChannel.map(MapMode.READ_ONLY, 0, chunkSizeForFile));
			long tail = computeHashForChunk(fileChannel.map(MapMode.READ_ONLY, Math.max(size - HASH_CHUNK_SIZE, 0), chunkSizeForFile));

			return String.format("%016x", size + head + tail);
		}
	}
	private static long computeHashForChunk(ByteBuffer buffer) {
		LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
		long hash = 0;

		while (longBuffer.hasRemaining()) {
			hash += longBuffer.get();
		}

		return hash;
	}

	public static String postPage(URLConnection connection, String query) throws IOException {
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Content-Type", "text/xml");
		connection.setRequestProperty("Content-Length", "" + query.length());
		((HttpURLConnection) connection).setRequestMethod("POST");
		//LOGGER.debug("opensub query "+query);
		// open up the output stream of the connection
		if (!StringUtils.isEmpty(query)) {
			try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
				output.writeBytes(query);
				output.flush();
			}
		}

		StringBuilder page;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			page = new StringBuilder();
			String str;
			while ((str = in.readLine()) != null) {
				page.append(str.trim());
				page.append("\n");
			}
		}
		//LOGGER.debug("opensubs result page "+page.toString());
		return page.toString();
	}

	/*
	 * This MUST be called with a lock on tokenLock
	 */
	private static boolean tokenIsYoung() {
		long now = System.currentTimeMillis();
		return ((now - tokenAge) < TOKEN_AGE_TIME);
	}

	private static boolean login() throws IOException {
		tokenLock.writeLock().lock();
		try {
			if (token != null && tokenIsYoung()) {
				return true;
			}
			URL url = new URL(OPENSUBS_URL);
			CredMgr.Cred credential = PMS.getCred("opensubtitles");
			String password = "";
			String user = "";
			if(credential != null) {
				// if we got credentials use them
				if (!StringUtils.isEmpty(credential.password)) {
					password = DigestUtils.md5Hex(credential.password);
				}
				user = credential.username;
			}
			String request =
				"<methodCall>\n" +
					"<methodName>LogIn</methodName>\n" +
					"<params>\n"+
						"<param>\n" +
							"<value><string>" + user +"</string></value>\n" +
						"</param>\n" +
						"<param>\n" +
							"<value><string>" + password + "</string></value>\n" +
						"</param>\n" +
						"<param>\n" +
							"<value><string/></value>\n" +
						"</param>\n" +
						"<param>\n" +
							"<value><string>" + UA + "</string></value>\n" +
						"</param>\n" +
					"</params>\n" +
				"</methodCall>\n";
			Pattern pattern = Pattern.compile("token.*?<string>([^<]+)</string>", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(postPage(url.openConnection(), request));
			if (matcher.find()) {
				token = matcher.group(1);
				tokenAge = System.currentTimeMillis();
			}
			return token != null;
		} finally {
			tokenLock.writeLock().unlock();
		}
	}

	public static String fetchImdbId(File f) throws IOException {
		return fetchImdbId(getHash(f));
	}

	public static String fetchImdbId(String hash) throws IOException {
		LOGGER.trace("Fetching IMDB ID form OpenSubtitles for hash \"{}\"", hash);
		Pattern pattern = Pattern.compile("MovieImdbID.*?<string>([^<]+)</string>", Pattern.DOTALL);
		String reply = checkMovieHash(hash);
		LOGGER.trace("Reply from OpenSubtitles is:\n{}", reply);
		Matcher matcher = pattern.matcher(reply);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private static String checkMovieHash(String hash) throws IOException {
		if (!login()) {
			return "";
		}
		URL url = new URL(OPENSUBS_URL);
		tokenLock.readLock().lock();
		String request = null;
		try {
		request =
			"<methodCall>\n" +
				"<methodName>CheckMovieHash</methodName>\n" +
				"<params>\n" +
					"<param>\n" +
						"<value><string>" + token + "</string></value>\n" +
					"</param>\n" +
					"<param>\n<value>\n" +
						"<array>\n" +
							"<data>\n" +
								"<value><string>" + hash + "</string></value>\n" +
							"</data>\n" +
						"</array>\n</value>\n" +
					"</param>" +
				"</params>\n" +
			"</methodCall>\n";
		} finally {
			tokenLock.readLock().unlock();
		}
		LOGGER.trace("Sending request to \"{}\":\n{}", url, request);
		return postPage(url.openConnection(), request);
	}

	public static String getMovieInfo(File f) throws IOException {
		String info = checkMovieHash(getHash(f));
		if (StringUtils.isEmpty(info)) {
			return "";
		}
		@SuppressWarnings("unused")
		Pattern re = Pattern.compile("MovieImdbID.*?<string>([^<]+)</string>", Pattern.DOTALL);
		LOGGER.debug("info is " + info);
		return info;
	}

	public static Map<String, Object> findSubs(File file) throws IOException {
		return findSubs(file, null);
	}

	public static Map<String, Object> findSubs(File file, RendererConfiguration renderer) throws IOException {
		Map<String, Object> result = findSubs(getHash(file), file.length(), null, null, renderer);
		if (result.isEmpty()) { // no good on hash! try imdb
			String imdb = ImdbUtil.extractImdbId(file.toPath(), false);
			if (StringUtils.isEmpty(imdb)) {
				imdb = fetchImdbId(file);
			}
			result = findSubs(null, 0, imdb, null, renderer);
		}
		if (result.isEmpty()) { // final try, use the name
			result = querySubs(file.getName(), renderer);
		}
		return result;
	}

	public static Map<String, Object> findSubs(String fileHash, long size) throws IOException {
		return findSubs(fileHash, size, null, null, null);
	}

	public static Map<String, Object> findSubs(String imdb) throws IOException {
		return findSubs(null, 0, imdb, null, null);
	}

	public static Map<String, Object> querySubs(String query) throws IOException {
		return querySubs(query, null);
	}

	public static Map<String, Object> querySubs(String query, RendererConfiguration r) throws IOException {
		return findSubs(null, 0, null, query, r);
	}

	public static Map<String, Object> findSubs(
		String fileHash,
		long size,
		String imdb,
		String query,
		RendererConfiguration renderer
	) throws IOException {
		TreeMap<String, Object> result = new TreeMap<>();
		if (!login()) {
			return result;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Language language : UMSUtils.getLangList(renderer)) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(language.getCode());
		}
		String languages = sb.toString();
		URL url = new URL(OPENSUBS_URL);
		String hashRequest = "";
		String imdbIdRequest = "";
		String queryRequest = "";
		if (!StringUtils.isEmpty(fileHash)) {
			hashRequest =
				"<member>" +
					"<name>moviehash</name>" +
					"<value><string>" + fileHash + "</string></value>" +
				"</member>\n" +
				"<member>" +
					"<name>moviebytesize</name>" +
					"<value><double>" + size + "</double></value>" +
				"</member>\n";
		} else if (!StringUtils.isEmpty(imdb)) {
			imdbIdRequest =
				"<member>" +
					"<name>imdbid</name>" +
					"<value><string>" + imdb + "</string></value>" +
				"</member>\n";
		} else if (!StringUtils.isEmpty(query)) {
			queryRequest =
				"<member>" +
					"<name>query</name>" +
					"<value><string>" + query + "</string></value>" +
				"</member>\n";
		} else {
			return result;
		}
		String request = null;
		tokenLock.readLock().lock();
		try {
			request =
				"<methodCall>\n" +
					"<methodName>SearchSubtitles</methodName>\n" +
					"<params>\n" +
						"<param>\n<value><string>" + token + "</string></value>\n</param>\n" +
						"<param>\n<value>\n" +
							"<array>\n" +
								"<data>\n<value>" +
									"<struct>" +
										"<member>" +
											"<name>sublanguageid</name>" +
												"<value><string>" + languages + "</string></value>" +
										"</member>" +
										hashRequest +
										imdbIdRequest +
										queryRequest + "\n" +
									"</struct></value>" +
								"</data>\n" +
							"</array>\n</value>\n" +
						"</param>" +
					"</params>\n" +
				"</methodCall>\n";
		} finally {
			tokenLock.readLock().unlock();
		}
		Pattern pattern = Pattern.compile(
			"SubFileName</name>.*?<string>([^<]+)</string>.*?SubLanguageID</name>.*?<string>" +
			"([^<]+)</string>.*?SubDownloadLink</name>.*?<string>([^<]+)</string>",
			Pattern.DOTALL
		);
		String page = postPage(url.openConnection(), request);
		Matcher matcher = pattern.matcher(page);
		while (matcher.find()) {
			LOGGER.debug("Found subtitle {} named \"{}\" zip {}", matcher.group(2), matcher.group(1), matcher.group(3));
			result.put(matcher.group(2) + ":" + matcher.group(1), matcher.group(3));
			if (result.size() > PMS.getConfiguration().liveSubtitlesLimit()) {
				// limit the number of hits somewhat
				break;
			}
		}
		return result;
	}

	/**
	 * Feeds the correct parameters to getInfo below.
	 *
	 * @see #getInfo(String, long, String, String, RendererConfiguration)
	 *
	 * @param file the file to lookup
	 * @param formattedName the name to use in the name search
	 *
	 * @return
	 * @throws IOException
	 */
	public static String[] getInfo(File file, String formattedName) throws IOException {
		return getInfo(file, formattedName, null);
	}

	public static String[] getInfo(File file, String formattedName, RendererConfiguration renderer) throws IOException {
		String[] res = getInfo(getHash(file), file.length(), null, null, renderer);
		if (res == null || res.length == 0) { // no good on hash! try imdb
			String imdb = ImdbUtil.extractImdbId(file.toPath(), false);
			if (isNotBlank(imdb)) {
				res = getInfo(null, 0, imdb, null, renderer);
			}
		}
		if (res == null || res.length == 0) { // final try, use the name
			if (StringUtils.isNotEmpty(formattedName)) {
				res = getInfo(null, 0, null, formattedName, renderer);
			} else {
				res = getInfo(null, 0, null, file.getName(), renderer);
			}
		}
		return res;
	}

	/**
	 * Attempt to return information from OpenSubtitles about the file based
	 * on information from the filename; either the hash, the IMDb ID or the
	 * filename itself.
	 *
	 * @param hash  the video hash
	 * @param size  the bytesize to be used with the hash
	 * @param imdb  the IMDb ID
	 * @param query the string to search OpenSubtitles for
	 * @param renderer the renderer to get subtitle languages from
	 *
	 * @return a string array including the IMDb ID, episode title, season
	 *         number, episode number relative to the season, and the show
	 *         name, or {@code null} if we couldn't find it on OpenSubtitles.
	 *
	 * @throws IOException
	 */
	private static String[] getInfo(
		String hash,
		long size,
		String imdb,
		String query,
		RendererConfiguration renderer
	) throws IOException {
		if (!login()) {
			return null;
		}
		URL url = new URL(OPENSUBS_URL);
		String hashStr = "";
		String imdbStr = "";
		String qStr = "";
		if (!StringUtils.isEmpty(hash)) {
			hashStr = "<member><name>moviehash</name><value><string>" + hash + "</string></value></member>\n" +
					"<member><name>moviebytesize</name><value><double>" + size + "</double></value></member>\n";
		} else if (!StringUtils.isEmpty(imdb)) {
			imdbStr = "<member><name>imdbid</name><value><string>" + imdb + "</string></value></member>\n";
		} else if (!StringUtils.isEmpty(query)) {
			qStr = "<member><name>query</name><value><string>" + query + "</string></value></member>\n";
		} else {
			return null;
		}
		String req = null;
		tokenLock.readLock().lock();
		try {
			req =
				"<methodCall>\n" +
					"<methodName>SearchSubtitles</methodName>\n" +
					"<params>\n" +
						"<param>\n" +
							"<value><string>" + token + "</string></value>\n" +
						"</param>\n" +
						"<param>\n<value>\n" +
							"<array>\n" +
								"<data>\n" +
									"<value><struct>" +
										hashStr +
										imdbStr +
										qStr + "\n" +
									"</struct></value>" +
								"</data>\n" +
							"</array>\n</value>\n" +
						"</param>" +
					"</params>\n" +
				"</methodCall>\n";
		} finally {
			tokenLock.readLock().unlock();
		}
		Pattern re = Pattern.compile(
				".*IDMovieImdb</name>.*?<string>([^<]+)</string>.*?" + "" +
				"MovieName</name>.*?<string>([^<]+)</string>.*?" +
				"SeriesSeason</name>.*?<string>([^<]+)</string>.*?" +
				"SeriesEpisode</name>.*?<string>([^<]+)</string>.*?" +
				"MovieYear</name>.*?<string>([^<]+)</string>.*?",
				Pattern.DOTALL
		);
		String page = postPage(url.openConnection(), req);

		// LOGGER.trace("opensubs page: " + page);

		Matcher m = re.matcher(page);
		if (m.find()) {
			LOGGER.debug("Matched OpenSubtitles entry: " + m.group(1) + "," + m.group(2) + "," + m.group(3) + "," + m.group(4) + "," + m.group(5));
			Pattern re1 = Pattern.compile("&#34;([^&]+)&#34;(.*)");
			String name = m.group(2);
			Matcher m1 = re1.matcher(name);
			String episodeName = "";
			if (m1.find()) {
				episodeName = m1.group(2).trim();
				name = m1.group(1).trim();
			}

			/**
			 * Sometimes if OpenSubtitles doesn't have an episode title they call it
			 * something like "Episode #1.4", so discard that.
			 */
			episodeName = StringEscapeUtils.unescapeHtml4(episodeName);
			if (episodeName.startsWith("Episode #")) {
				episodeName = "";
			}

			return new String[]{
				ImdbUtil.ensureTT(m.group(1).trim()),
				episodeName,
				StringEscapeUtils.unescapeHtml4(name),
				m.group(3).trim(), // Season number
				m.group(4).trim(), // Episode number
				m.group(5).trim()  // Year
			};
		}
		return null;
	}

	public static String subFile(String name) {
		String dir = PMS.getConfiguration().getDataFile(SUB_DIR);
		File path = new File(dir);
		if (!path.exists()) {
			if (!path.mkdirs()) {
				LOGGER.error("Failed to create subtitles folder {}", path.getAbsolutePath());
			}
		}
		return path.getAbsolutePath() + File.separator + name + ".srt";
	}

	public static String fetchSubs(String url) throws FileNotFoundException, IOException {
		return fetchSubs(url, subFile(String.valueOf(System.currentTimeMillis())));
	}

	public static String fetchSubs(String url, String outName) throws FileNotFoundException, IOException {
		if (!login()) {
			return "";
		}
		if (StringUtils.isEmpty(outName)) {
			outName = subFile(String.valueOf(System.currentTimeMillis()));
		}
		File f = new File(outName);
		URL u = new URL(url);
		URLConnection connection = u.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		InputStream in = connection.getInputStream();
		OutputStream out;
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(in)) {
			out = new FileOutputStream(f);
			byte[] buf = new byte[4096];
			int len;
			while ((len = gzipInputStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		}
		out.close();
		if (!PMS.getConfiguration().isLiveSubtitlesKeep()) {
			int tmo = PMS.getConfiguration().getLiveSubtitlesTimeout();
			if (tmo <= 0) {
				PMS.get().addTempFile(f);
			}
			else {
				PMS.get().addTempFile(f, tmo);
			}
		}
		return f.getAbsolutePath();
	}

	public static String getLang(String str) {
		String[] tmp = str.split(":", 2);
		if (tmp.length > 1) {
			return tmp[0];
		}
		return "";
	}

	public static String getName(String str) {
		String[] tmp = str.split(":", 2);
		if (tmp.length > 1) {
			return tmp[1];
		}
		return str;
	}

	public static void convert() {
		if (PMS.getConfiguration().isLiveSubtitlesKeep()) {
			return;
		}
		File path = new File(PMS.getConfiguration().getDataFile(SUB_DIR));
		if (!path.exists()) {
			// no path nothing to do
			return;
		}
		File[] files = path.listFiles();
		if (files != null) {
			for (File file : files) {
				PMS.get().addTempFile(file);
			}
		}
	}
}
