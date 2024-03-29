package net.pms.remote;

import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import javax.net.ssl.*;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.configuration.WebRender;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.DLNAThumbnailInputStream;
import net.pms.dlna.RealFile;
import net.pms.dlna.RootFolder;
import net.pms.image.ImageFormat;
import net.pms.io.BasicSystemUtils;
import net.pms.logging.LoggingConfig;
import net.pms.network.HTTPResource;
import net.pms.network.HTTPServer;
import net.pms.util.FullyPlayed;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class RemoteWeb {
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteWeb.class);
	private KeyStore keyStore;
	private KeyManagerFactory keyManagerFactory;
	private TrustManagerFactory trustManagerFactory;
	private HttpServer server;
	private SSLContext sslContext;
	private Map<String, RootFolder> roots;
	private RemoteUtil.ResourceManager resources;
	private static final PmsConfiguration configuration = PMS.getConfiguration();
	private static final int defaultPort = configuration.getWebPort();

	public RemoteWeb(int port) throws IOException {
		if (port <= 0) {
			port = defaultPort;
		}

		roots = new HashMap<>();
		// Add "classpaths" for resolving web resources
		resources = AccessController.doPrivileged(new PrivilegedAction<RemoteUtil.ResourceManager>() {

			@Override
			public RemoteUtil.ResourceManager run() {
				return new RemoteUtil.ResourceManager(
					"file:" + configuration.getProfileFolder() + "/web/",
					"jar:file:" + configuration.getProfileFolder() + "/web.zip!/",
					"file:" + configuration.getWebPath() + "/"
				);
			}
		});

		// Check that web interface resources are available
		try (InputStream is = resources.getInputStream("start.html")) {
			if (is == null) {
				throw new FileNotFoundException("Web interface resources not found");
			}
		}

		//readCred();

		// Setup the socket address
		InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port);

		// Initialize the HTTP(S) server
		if (configuration.getWebHttps()) {
			try {
				server = httpsServer(address);
			} catch (IOException e) {
				LOGGER.error("Failed to start WEB interface on HTTPS: {}", e.getMessage());
				LOGGER.trace("", e);
				if (e.getMessage().contains("DMS.jks")) {
					LOGGER.info(
						"To enable HTTPS please generate a self-signed keystore file " +
						"called \"DMS.jks\" with password \"dmsdms\" using the java " +
						"'keytool' commandline utility, and place it in the profile folder"
					);
				}
			} catch (GeneralSecurityException e) {
				LOGGER.error("Failed to start WEB interface on HTTPS due to a security error: {}", e.getMessage());
				LOGGER.trace("", e);
			}
		} else {
			server = HttpServer.create(address, 0);
		}

		if (server != null) {
			int threads = configuration.getWebThreads();

			// Add context handlers
			addCtx("/", new RemoteStartHandler(this));
			addCtx("/browse/", new RemoteBrowseHandler(this));
			RemotePlayHandler playHandler = new RemotePlayHandler(this);
			addCtx("/play", playHandler);
			addCtx("/playstatus", playHandler);
			addCtx("/playlist", playHandler);
			addCtx("/media", new RemoteMediaHandler(this));
			addCtx("/fmedia", new RemoteMediaHandler(this, true));
			addCtx("/thumb", new RemoteThumbHandler(this));
			addCtx("/raw", new RemoteRawHandler(this));
			addCtx("/files", new RemoteFileHandler(this));
			addCtx("/doc", new RemoteDocHandler(this));
			addCtx("/poll", new RemotePollHandler(this));
			server.setExecutor(Executors.newFixedThreadPool(threads));
			server.start();
		}
	}

	private HttpServer httpsServer(InetSocketAddress address) throws IOException, GeneralSecurityException {
		// Initialize the keystore
		char[] password = "dmsdms".toCharArray();
		keyStore = KeyStore.getInstance("JKS");
		try (
			InputStream is = Files.newInputStream(configuration.getProfileFolder().resolve("DMS.jks"))
		) {
			keyStore.load(is, password);
		}

		// Setup the key manager factory
		keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, password);

		// Setup the trust manager factory
		trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(keyStore);

		HttpsServer server = HttpsServer.create(address, 0);
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
			@Override
			public void configure(HttpsParameters params) {
				try {
					// initialise the SSL context
					SSLContext c = SSLContext.getDefault();
					SSLEngine engine = c.createSSLEngine();
					params.setNeedClientAuth(true);
					params.setCipherSuites(engine.getEnabledCipherSuites());
					params.setProtocols(engine.getEnabledProtocols());

					// get the default parameters
					SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
					params.setSSLParameters(defaultSSLParameters);
				} catch (Exception e) {
					LOGGER.error("HTTPS configuration error: {}", e.getMessage());
					LOGGER.trace("", e);
				}
			}
		});
		return server;
	}

	public String getTag(String user) {
		String tag = PMS.getCredTag("web", user);
		//tags.get(user);
		if (tag == null) {
			return user;
		}
		return tag;
	}

	public String getAddress() {
		return PMS.get().getServer().getHost() + ":" + server.getAddress().getPort();
	}

	public RootFolder getRoot(String user, HttpExchange t) throws InterruptedException {
		return getRoot(user, false, t);
	}

	public RootFolder getRoot(String user, boolean create, HttpExchange t) throws InterruptedException {
		String cookie = RemoteUtil.getCookie("DMS", t);
		RootFolder root;
		synchronized (roots) {
			root = roots.get(cookie);
			if (root == null) {
				// Double-check for cookie errors
				WebRender valid = RemoteUtil.matchRenderer(user, t);
				if (valid != null) {
					// A browser of the same type and user is already connected at
					// this ip but for some reason we didn't get a cookie match.
					RootFolder validRoot = valid.getRootFolder();
					// Do a reverse lookup to see if it's been registered
					for (Map.Entry<String, RootFolder> entry : roots.entrySet()) {
						if (entry.getValue() == validRoot) {
							// Found
							root = validRoot;
							cookie = entry.getKey();
							LOGGER.debug("Allowing browser connection without cookie match: {}: {}", valid.getRendererName(), t.getRemoteAddress().getAddress());
							break;
						}
					}
				}
			}

			if (!create || (root != null)) {
				t.getResponseHeaders().add("Set-Cookie", "DMS=" + cookie + ";Path=/");
				return root;
			}

			root = new RootFolder();
			try {
				WebRender render = new WebRender(user);
				root.setDefaultRenderer(render);
				render.setRootFolder(root);
				render.associateIP(t.getRemoteAddress().getAddress());
				render.associatePort(t.getRemoteAddress().getPort());
				if (configuration.useWebSubLang()) {
					render.setSubLang(StringUtils.join(RemoteUtil.getLangs(t), ","));
				}
//				render.setUA(t.getRequestHeaders().getFirst("User-agent"));
				render.setBrowserInfo(RemoteUtil.getCookie("DMSINFO", t), t.getRequestHeaders().getFirst("User-agent"));
				PMS.get().setRendererFound(render);
			} catch (ConfigurationException e) {
				root.setDefaultRenderer(RendererConfiguration.getDefaultConf());
			}
			//root.setDefaultRenderer(RendererConfiguration.getRendererConfigurationByName("web"));
			root.discoverChildren();
			cookie = UUID.randomUUID().toString();
			t.getResponseHeaders().add("Set-Cookie", "DMS=" + cookie + ";Path=/");
			roots.put(cookie, root);
		}
		return root;
	}

	public void associate(HttpExchange t, WebRender webRenderer) {
		webRenderer.associateIP(t.getRemoteAddress().getAddress());
		webRenderer.associatePort(t.getRemoteAddress().getPort());
	}

	private void addCtx(String path, HttpHandler h) {
		HttpContext ctx = server.createContext(path, h);
		if (configuration.isWebAuthenticate()) {
			ctx.setAuthenticator(new BasicAuthenticator("") {
				@Override
				public boolean checkCredentials(String user, String pwd) {
					LOGGER.debug("authenticate " + user);
					return PMS.verifyCred("web", PMS.getCredTag("web", user), user, pwd);
					//return pwd.equals(users.get(user));
					//return true;
				}
			});
		}
	}

	public HttpServer getServer() {
		return server;
	}

	static class RemoteThumbHandler implements HttpHandler {
		private RemoteWeb parent;

		public RemoteThumbHandler(RemoteWeb parent) {
			this.parent = parent;

		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				if (RemoteUtil.deny(t)) {
					throw new IOException("Access denied");
				}
				String id = RemoteUtil.getId("thumb/", t);
				LOGGER.trace("web thumb req " + id);
				if (id.contains("logo")) {
					RemoteUtil.sendLogo(t);
					return;
				}
				RootFolder root = parent.getRoot(RemoteUtil.userName(t), t);
				if (root == null) {
					LOGGER.debug("weird root in thumb req");
					throw new IOException("Unknown root");
				}
				final DLNAResource r = root.getDLNAResource(id, root.getDefaultRenderer());
				if (r == null) {
					// another error
					LOGGER.debug("media unknown");
					throw new IOException("Bad id");
				}
				DLNAThumbnailInputStream in;
				if (!configuration.isShowCodeThumbs() && !r.isCodeValid(r)) {
					// we shouldn't show the thumbs for coded objects
					// unless the code is entered
					in = r.getGenericThumbnailInputStream(null);
				} else {
					r.checkThumbnail();
					in = r.fetchThumbnailInputStream();
				}
				if (r instanceof RealFile && FullyPlayed.isFullyPlayedThumbnail(((RealFile) r).getFile())) {
					in = FullyPlayed.addFullyPlayedOverlay(in);
				}
				Headers hdr = t.getResponseHeaders();
				hdr.add("Content-Type", ImageFormat.PNG.equals(in.getFormat()) ? HTTPResource.PNG_TYPEMIME : HTTPResource.JPEG_TYPEMIME);
				hdr.add("Accept-Ranges", "bytes");
				hdr.add("Connection", "keep-alive");
				t.sendResponseHeaders(200, in.getSize());
				OutputStream os = t.getResponseBody();
				LOGGER.trace("Web thumbnail: Input is {} output is {}", in, os);
				RemoteUtil.dump(in, os);
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				// Nothing should get here, this is just to avoid crashing the thread
				LOGGER.error("Unexpected error in RemoteThumbHandler.handle(): {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}

	static class RemoteFileHandler implements HttpHandler {
		private RemoteWeb parent;

		public RemoteFileHandler(RemoteWeb parent) {
			this.parent = parent;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				LOGGER.debug("Handling web interface file request \"{}\"", t.getRequestURI());

				String path = t.getRequestURI().getPath();
				String response = null;
				String mime = null;
				int status = 200;

				if (path.contains("crossdomain.xml")) {
					response = "<?xml version=\"1.0\"?>" +
						"<!-- http://www.bitsontherun.com/crossdomain.xml -->" +
						"<cross-domain-policy>" +
						"<allow-access-from domain=\"*\" />" +
						"</cross-domain-policy>";
					mime = "text/xml";

				} else if (path.startsWith("/files/log/")) {
					String filename = path.substring(11);
					File file = parent.getResources().getFile(filename);
					if (file != null) {
						filename = file.getName();
						HashMap<String, Object> vars = new HashMap<>();
						vars.put("favicons", RemoteUtil.FAVICONS_HEADER);
						vars.put("title", filename);
						vars.put("brush", filename.endsWith("debug.log") || filename.endsWith("debug.log.prev") ? "debug_log" :
							filename.endsWith(".log") ? "log" : "conf");
						vars.put("log", StringEscapeUtils.escapeHtml4(RemoteUtil.read(file)));
						response = parent.getResources().getTemplate("util/log.html").execute(vars);
						mime = "text/html";
					} else {
						status = 404;
					}
					mime = "text/html";

				} else if (parent.getResources().write(path.substring(7), t)) {
					// The resource manager found and sent the file, all done.
					return;

				} else {
					status = 404;
				}

				if (status == 404 && response == null) {
					response = "<html><body>404 - File Not Found: " + path + "</body></html>";
					mime = "text/html";
				}

				RemoteUtil.respond(t, response, status, mime);
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				// Nothing should get here, this is just to avoid crashing the thread
				LOGGER.error("Unexpected error in RemoteFileHandler.handle(): {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}

	static class RemoteStartHandler implements HttpHandler {
		private static final Logger LOGGER = LoggerFactory.getLogger(RemoteStartHandler.class);
		@SuppressWarnings("unused")
		private final static String CRLF = "\r\n";
		private RemoteWeb parent;

		public RemoteStartHandler(RemoteWeb parent) {
			this.parent = parent;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				LOGGER.debug("root req " + t.getRequestURI());
				if (RemoteUtil.deny(t)) {
					throw new IOException("Access denied");
				}
				if (RemoteUtil.handleFavIcon(t, parent.getResources())) {
					return;
				}

				HashMap<String, Object> vars = new HashMap<>();
				vars.put("favicons", RemoteUtil.FAVICONS_HEADER);
				vars.put("serverName", configuration.getServerDisplayName());

				try {
					Template template = parent.getResources().getTemplate("start.html");
					if (template != null) {
						String response = template.execute(vars);
						RemoteUtil.respond(t, response, 200, "text/html");
					} else {
						throw new IOException("Web template \"start.html\" not found");
					}
				} catch (MustacheException e) {
					LOGGER.error("An error occurred while generating a HTTP response: {}", e.getMessage());
					LOGGER.trace("", e);
				}
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				// Nothing should get here, this is just to avoid crashing the thread
				LOGGER.error("Unexpected error in RemoteStartHandler.handle(): {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}

	static class RemoteDocHandler implements HttpHandler {
		private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDocHandler.class);
		@SuppressWarnings("unused")
		private final static String CRLF = "\r\n";

		private RemoteWeb parent;

		public RemoteDocHandler(RemoteWeb parent) {
			this.parent = parent;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			try {
				LOGGER.debug("root req " + exchange.getRequestURI());
				if (RemoteUtil.deny(exchange)) {
					throw new IOException("Access denied");
				}
				if (RemoteUtil.handleFavIcon(exchange, parent.getResources())) {
					return;
				}

				HashMap<String, Object> vars = new HashMap<>();
				vars.put("favicons", RemoteUtil.FAVICONS_HEADER);
				vars.put("logs", getLogs());
				if (configuration.getUseCache()) {
					HTTPServer server = PMS.get().getServer();
					vars.put("cache", "http://" + server.getHost() + ":" + server.getPort() + "/console/home");
				}

				String response = parent.getResources().getTemplate("doc.html").execute(vars);
				RemoteUtil.respond(exchange, response, 200, "text/html");
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				// Nothing should get here, this is just to avoid crashing the thread
				LOGGER.error("Unexpected error in RemoteDocHandler.handle(): {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}

		private ArrayList<HashMap<String, String>> getLogs() {
			Set<File> files = LoggingConfig.getDebugFiles(false);
			ArrayList<HashMap<String, String>> logs = new ArrayList<>();
			for (File file : files) {
				String id = String.valueOf(parent.getResources().add(file));
				HashMap<String, String> item = new HashMap<>();
				item.put("filename", file.getName());
				item.put("id", id);
				logs.add(item);
			}
			return logs;
		}
	}

	public RemoteUtil.ResourceManager getResources() {
		return resources;
	}

	public String getUrl() {
		if (server != null && server.getAddress() != null) {
			String host = server.getAddress().getAddress().isAnyLocalAddress() ?
				BasicSystemUtils.INSTANCE.getLocalHostname() :
				server.getAddress().getAddress().getHostAddress();

			return (server instanceof HttpsServer ? "https://" : "http://") +
				host + ":" + server.getAddress().getPort();
		}
		return null;
	}

	static class RemotePollHandler implements HttpHandler {
		private static final Logger LOGGER = LoggerFactory.getLogger(RemotePollHandler.class);
		@SuppressWarnings("unused")
		private final static String CRLF = "\r\n";

		private RemoteWeb parent;

		public RemotePollHandler(RemoteWeb parent) {
			this.parent = parent;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				//LOGGER.debug("poll req " + t.getRequestURI());
				if (RemoteUtil.deny(t)) {
					throw new IOException("Access denied");
				}
				RootFolder root = parent.getRoot(RemoteUtil.userName(t), t);
				WebRender renderer = (WebRender) root.getDefaultRenderer();
				String json = renderer.getPushData();
				RemoteUtil.respond(t, json, 200, "text");
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				// Nothing should get here, this is just to avoid crashing the thread
				LOGGER.error("Unexpected error in RemotePollHandler.handle(): {}", e.getMessage());
				LOGGER.trace("", e);
			}
		}
	}
}
