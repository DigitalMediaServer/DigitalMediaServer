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
package net.pms.newgui;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.sun.jna.Platform;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.io.BasicSystemUtils;
import net.pms.io.WindowsNamedPipe;
import net.pms.newgui.StatusTab.ConnectionState;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconListenerRegistrar;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconStage;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconType;
import net.pms.newgui.components.WindowProperties.WindowPropertiesConfiguration;
import net.pms.newgui.components.AnimatedButton;
import net.pms.newgui.components.AnimatedIconListener;
import net.pms.newgui.components.AnimatedIconListener.MinimizeListenerRegistrar;
import net.pms.newgui.components.AnimatedIconListener.WindowIconifyListener;
import net.pms.newgui.components.ImageButton;
import net.pms.newgui.components.AnimatedIconListenerAction;
import net.pms.newgui.components.WindowProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LooksFrame extends JFrame implements IFrame {
	private static final Logger LOGGER = LoggerFactory.getLogger(LooksFrame.class);

	private final PmsConfiguration configuration;
	private final WindowProperties windowProperties;
	private static final long serialVersionUID = 8723727186288427690L;
	protected static final Dimension STANDARD_SIZE = new Dimension(1000, 750);
	protected static final Dimension MINIMUM_SIZE = new Dimension(640, 480);

	/**
	 * List of context sensitive help pages URLs. These URLs should be
	 * relative to the documentation directory and in the same order as the
	 * tabs. The value <code>null</code> means "don't care", activating the
	 * tab will not change the help page.
	 */
	protected static final String[] HELP_PAGES = {
		"index.html",
		null,
		"general_configuration.html",
		null,
		"navigation_share.html",
		"transcoding.html",
		null,
		null
	};

	private NavigationShareTab nt;
	private StatusTab st;
	private TracesTab tt;
	private TranscodingTab tr;
	private GeneralTab gt;
	private HelpTab ht;
	private final JTabbedPane mainTabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final AnimatedButton reload = createAnimatedToolBarButton(Messages.getString("LooksFrame.12"), "button-restart.png");;
	private final AnimatedIcon restartRequredIcon = new AnimatedIcon(
		reload,
		true,
		AnimatedIcon.buildAnimation("button-restart-requiredF%d.png", 0, 24, true, 800, 300, 15)
	);
	private AnimatedIcon restartIcon;
	private ImageButton webinterface;
	private JLabel status;
	private final static Object lookAndFeelInitializedLock = new Object();
	private volatile static boolean lookAndFeelInitialized = false;
	private static int dlu100x = 167; // Default, will be set properly after LAF
	private static int dlu100y = 163; // Default, will be set properly after LAF
	private final MinimizeListenerRegistrar minimizeListenerRegistrar = new MinimizeListenerRegistrar(this);
	private final LooksFrameTabListenerRegistrar tabListenerRegistrar = new LooksFrameTabListenerRegistrar(mainTabbedPane);
	private ViewLevel viewLevel = ViewLevel.UNKNOWN;

	public ViewLevel getViewLevel() {
		return viewLevel;
	}

	public void setViewLevel(ViewLevel viewLevel) {
		if (viewLevel != ViewLevel.UNKNOWN){
			this.viewLevel = viewLevel;
			tt.applyViewLevel();
		}
	}

	public TracesTab getTt() {
		return tt;
	}

	public NavigationShareTab getNt() {
		return nt;
	}

	public TranscodingTab getTr() {
		return tr;
	}

	public GeneralTab getGt() {
		return gt;
	}

	public static void initializeLookAndFeel() {

		if (lookAndFeelInitialized) {
			return;
		}

		synchronized (lookAndFeelInitializedLock) {
			if (lookAndFeelInitialized) {
				return;
			}

			LookAndFeel selectedLaf = null;
			if (Platform.isWindows()) {
				selectedLaf = new WindowsLookAndFeel();
			} else if (System.getProperty("nativelook") == null && !Platform.isMac()) {
				selectedLaf = new PlasticLookAndFeel();
			} else {
				try {
					String systemClassName = UIManager.getSystemLookAndFeelClassName();
					// Workaround for Gnome
					try {
						String gtkLAF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
						Class.forName(gtkLAF);

						if (systemClassName.equals("javax.swing.plaf.metal.MetalLookAndFeel")) {
							systemClassName = gtkLAF;
						}
					} catch (ClassNotFoundException ce) {
						LOGGER.error("Error loading GTK look and feel: ", ce);
					}

					LOGGER.trace("Choosing Java look and feel: " + systemClassName);
					UIManager.setLookAndFeel(systemClassName);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					selectedLaf = new PlasticLookAndFeel();
					LOGGER.error("Error while setting native look and feel: ", e1);
				}
			}

			if (selectedLaf instanceof PlasticLookAndFeel) {
				PlasticLookAndFeel.setPlasticTheme(PlasticLookAndFeel.createMyDefaultTheme());
				PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
				PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);
			} else if (selectedLaf != null && selectedLaf.getClass() == MetalLookAndFeel.class) {
				MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
			}

			// Work around caching in MetalRadioButtonUI
			JRadioButton radio = new JRadioButton();
			radio.getUI().uninstallUI(radio);
			JCheckBox checkBox = new JCheckBox();
			checkBox.getUI().uninstallUI(checkBox);

			if (selectedLaf != null) {
				try {
					UIManager.setLookAndFeel(selectedLaf);
					// Workaround for JDK-8179014: JFileChooser with Windows look and feel crashes on win 10
					// https://bugs.openjdk.java.net/browse/JDK-8179014
					if (selectedLaf instanceof WindowsLookAndFeel) {
						UIManager.put("FileChooser.useSystemExtensionHiding", false);
					}
				} catch (UnsupportedLookAndFeelException e) {
					LOGGER.warn("Can't change look and feel", e);
				}
			}

			JLabel tempLabel = new JLabel();
			dlu100x = Sizes.getUnitConverter().dialogUnitXAsPixel(100, tempLabel);
			dlu100y = Sizes.getUnitConverter().dialogUnitYAsPixel(100, tempLabel);

			lookAndFeelInitialized = true;
		}
	}

	/**
	 * Returns the the number of pixels represented by 100 horizontal dialog
	 * units for the default {@link JLabel} font using the current
	 * {@code LookAndFeel}.
	 *
	 * @return The size in pixels of 100 horizontal DLU units.
	 */
	public static int getDLU100x() {
		return dlu100x;
	}

	/**
	 * Returns the the number of pixels represented by 100 vertical dialog units
	 * for the default {@link JLabel} font using the current {@code LookAndFeel}.
	 *
	 * @return The size in pixels of 100 vertical DLU units.
	 */
	public static int getDLU100y() {
		return dlu100y;
	}

	/**
	 * Constructs a <code>DemoFrame</code>, configures the UI,
	 * and builds the content.
	 */
	public LooksFrame(@Nonnull PmsConfiguration configuration, @Nonnull WindowPropertiesConfiguration windowConfiguration) {
		super(windowConfiguration.getGraphicsConfiguration());
		if (configuration == null) {
			throw new IllegalArgumentException("configuration can't be null");
		}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(true);
		windowProperties = new WindowProperties(this, STANDARD_SIZE, MINIMUM_SIZE, windowConfiguration);
		this.configuration = configuration;
		minimizeListenerRegistrar.register(restartRequredIcon);
		Options.setDefaultIconSize(new Dimension(18, 18));
		Options.setUseNarrowButtons(true);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (SystemTray.isSupported() && PMS.getConfiguration().isGUIHideOnClose()) {
					e.getWindow().setVisible(false);
				} else {
					quit();
				}
			}
		});

		// Set view level, can be omitted if ViewLevel is implemented in configuration
		// by setting the view level as variable initialization
		if (configuration.isHideAdvancedOptions()) {
			viewLevel = ViewLevel.NORMAL;
		} else {
			viewLevel = ViewLevel.ADVANCED;
		}

		// Global options
		Options.setTabIconsEnabled(true);
		UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY, null);

		// Swing Settings
		initializeLookAndFeel();

		// http://propedit.sourceforge.jp/propertieseditor.jnlp
		Font sf = null;

		// Set an unicode font for testing exotic languages (Japanese)
		final String language = configuration.getLanguageTag();

		if (language != null && (language.equals("ja") || language.startsWith("zh") || language.equals("ko"))) {
			sf = new Font("SansSerif", Font.PLAIN, 12);
		}

		if (sf != null) {
			UIManager.put("Button.font", sf);
			UIManager.put("ToggleButton.font", sf);
			UIManager.put("RadioButton.font", sf);
			UIManager.put("CheckBox.font", sf);
			UIManager.put("ColorChooser.font", sf);
			UIManager.put("ToggleButton.font", sf);
			UIManager.put("ComboBox.font", sf);
			UIManager.put("ComboBoxItem.font", sf);
			UIManager.put("InternalFrame.titleFont", sf);
			UIManager.put("Label.font", sf);
			UIManager.put("List.font", sf);
			UIManager.put("MenuBar.font", sf);
			UIManager.put("Menu.font", sf);
			UIManager.put("MenuItem.font", sf);
			UIManager.put("RadioButtonMenuItem.font", sf);
			UIManager.put("CheckBoxMenuItem.font", sf);
			UIManager.put("PopupMenu.font", sf);
			UIManager.put("OptionPane.font", sf);
			UIManager.put("Panel.font", sf);
			UIManager.put("ProgressBar.font", sf);
			UIManager.put("ScrollPane.font", sf);
			UIManager.put("Viewport", sf);
			UIManager.put("TabbedPane.font", sf);
			UIManager.put("TableHeader.font", sf);
			UIManager.put("TextField.font", sf);
			UIManager.put("PasswordFiled.font", sf);
			UIManager.put("TextArea.font", sf);
			UIManager.put("TextPane.font", sf);
			UIManager.put("EditorPane.font", sf);
			UIManager.put("TitledBorder.font", sf);
			UIManager.put("ToolBar.font", sf);
			UIManager.put("ToolTip.font", sf);
			UIManager.put("Tree.font", sf);
			UIManager.put("Spinner.font", sf);
		}

		setTitle("Test");
		setIconImage(readImageIcon("icon-32.png").getImage());

		JComponent jp = buildContent();
		String showScrollbars = System.getProperty("scrollbars", "").toLowerCase();

		/**
		 * Handle scrollbars:
		 *
		 * 1) forced scrollbars (-Dscrollbars=true): always display them
		 * 2) optional scrollbars (-Dscrollbars=optional): display them as needed
		 * 3) otherwise (default): don't display them
		 */
		switch (showScrollbars) {
			case "true":
				setContentPane(
					new JScrollPane(
						jp,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
					)
				);
				break;
			case "optional":
				setContentPane(
					new JScrollPane(
						jp,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
					)
				);
				break;
			default:
				setContentPane(jp);
				break;
		}

		String projectName = PMS.getName();
		String projectVersion = PMS.getVersion();
		String title = projectName + " " + projectVersion;

		// If the version contains a "-" (e.g. "1.50.1-SNAPSHOT" or "1.50.1-beta1"), add a warning message
		if (projectVersion.indexOf('-') > -1) {
			title = title + " - " + Messages.getString("LooksFrame.26");
		}

		if (PMS.getTraceMode() == 2) {
			// Forced trace mode
			title = title + "  [" + Messages.getString("TracesTab.10").toUpperCase() + "]";
		}

		setTitle(title);

		// Display tooltips immediately and for a long time
		ToolTipManager.sharedInstance().setInitialDelay(400);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		ToolTipManager.sharedInstance().setReshowDelay(400);

		if (!configuration.isGUIStartHidden() || !SystemTray.isSupported()) {
			setVisible(true);
		}
		BasicSystemUtils.INSTANCE.addSystemTray(this);
	}

	public static ImageIcon readImageIcon(String filename) {
		URL url = LooksFrame.class.getResource("/resources/images/" + filename);
		return url == null ? null : new ImageIcon(url);
	}

	public JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		toolBar.add(new JPanel());

		webinterface = createToolBarButton(Messages.getString("LooksFrame.29"), "button-wif.png");
		webinterface.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String error = null;
				if (PMS.get().getWebInterface() != null && isNotBlank(PMS.get().getWebInterface().getUrl())) {
					try {
						URI uri = new URI(PMS.get().getWebInterface().getUrl());
						try {
							Desktop.getDesktop().browse(uri);
						} catch (RuntimeException | IOException be) {
							LOGGER.error("Couldn't open the default web browser: {}", be.getMessage());
							LOGGER.trace("", be);
							error = Messages.getString("LooksFrame.BrowserError") + "\n" + be.getMessage();
						}
					} catch (URISyntaxException se) {
						LOGGER.error(
							"Could not form a valid web interface URI from \"{}\": {}",
							PMS.get().getWebInterface().getUrl(),
							se.getMessage()
						);
						LOGGER.trace("", se);
						error = Messages.getString("LooksFrame.URIError");
					}
				}
				else {
					error = Messages.getString("LooksFrame.URIError");
				}
				if (error != null) {
					JOptionPane.showMessageDialog(null, error, Messages.getString("Dialog.Error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		webinterface.setToolTipText(Messages.getString("LooksFrame.30"));
		webinterface.setEnabled(false);
		toolBar.add(webinterface);
		toolBar.addSeparator(new Dimension(20, 1));

		restartIcon = (AnimatedIcon) reload.getIcon();
		restartRequredIcon.startArm();
		setReloadable(false);
		reload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reload.setEnabled(false);
				PMS.get().reset();
			}
		});
		reload.setToolTipText(Messages.getString("LooksFrame.28"));
		toolBar.add(reload);

		toolBar.addSeparator(new Dimension(20, 1));
		AbstractButton quit = createToolBarButton(Messages.getString("LooksFrame.5"), "button-quit.png");
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		toolBar.add(quit);
		toolBar.add(new JPanel());

		// Apply the orientation to the toolbar and all components in it
		ComponentOrientation orientation = ComponentOrientation.getOrientation(PMS.getLocale());
		toolBar.applyComponentOrientation(orientation);
		toolBar.setBorder(new EmptyBorder(new Insets(8,0,0,0)));

		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(buildMain(), BorderLayout.CENTER);
		status = new JLabel("");
		status.setBorder(BorderFactory.createEmptyBorder());
		status.setComponentOrientation(orientation);

		// Calling applyComponentOrientation() here would be ideal.
		// Alas it horribly mutilates the layout of several tabs.
		//panel.applyComponentOrientation(orientation);
		panel.add(status, BorderLayout.SOUTH);

		return panel;
	}

	public JComponent buildMain() {

		mainTabbedPane.setUI(new CustomTabbedPaneUI());

		st = new StatusTab(configuration, this);
		tt = new TracesTab(configuration, this);
		gt = new GeneralTab(configuration, this);
		nt = new NavigationShareTab(configuration, this);
		tr = new TranscodingTab(configuration, this);
		ht = new HelpTab();

		mainTabbedPane.addTab(Messages.getString("LooksFrame.18"), st.build());
		mainTabbedPane.addTab(Messages.getString("LooksFrame.19"), tt.build());
		mainTabbedPane.addTab(Messages.getString("LooksFrame.20"), gt.build());
		mainTabbedPane.addTab(Messages.getString("LooksFrame.22"), nt.build());
		if (!configuration.isDisableTranscoding()) {
			mainTabbedPane.addTab(Messages.getString("LooksFrame.21"), tr.build());
		} else {
			tr.build();
		}
		mainTabbedPane.addTab(Messages.getString("LooksFrame.24"), new HelpTab().build());
		mainTabbedPane.addTab(Messages.getString("LooksFrame.25"), new AboutTab().build());

		mainTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int selectedIndex = mainTabbedPane.getSelectedIndex();

				if (HELP_PAGES[selectedIndex] != null) {
					PMS.setHelpPage(HELP_PAGES[selectedIndex]);

					// Update the contents of the help tab itself
					ht.updateContents();
				}
			}
		});

		mainTabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		/*
		 * Set the orientation of the tabbedPane.
		 * Note: Do not use applyComponentOrientation() here because it
		 * messes with the layout of several tabs.
		 */
		ComponentOrientation orientation = ComponentOrientation.getOrientation(PMS.getLocale());
		mainTabbedPane.setComponentOrientation(orientation);

		return mainTabbedPane;
	}

	protected ImageButton createToolBarButton(String text, String iconName) {
		ImageButton button = new ImageButton(text, iconName);
		button.setFocusable(false);
		return button;
	}

	protected AnimatedButton createAnimatedToolBarButton(String text, String iconName) {
		AnimatedButton button = new AnimatedButton(text, iconName);
		button.setFocusable(false);
		return button;
	}

	protected ImageButton createToolBarButton(String text, String iconName, String toolTipText) {
		ImageButton button = new ImageButton(text, iconName);
		button.setToolTipText(toolTipText);
		button.setFocusable(false);
		button.setBorderPainted(false);
		return button;
	}

	public void quit() {
		WindowsNamedPipe.setLoop(false);
		windowProperties.dispose();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted during shutdown: {}", e);
		}

		System.exit(0);
	}

	@Override
	public void dispose() {
		windowProperties.dispose();
		super.dispose();
	}

	@Override
	public void append(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tt.append(msg);
			}
		});
	}

	@Override
	public void setReadValue(long v, String msg) {
		st.setReadValue(v, msg);
	}

	@Override
	public void setConnectionState(final ConnectionState connectionState) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				st.setConnectionState(connectionState);
			}
		});
	}

	@Override
	public void updateBuffer() {
		st.updateCurrentBitrate();
	}

	/**
	 * Sets the enabled status of the web interface button.
	 *
	 * @param value {@code true} if the button should be enabled, {@code false}
	 *            otherwise.
	 */
	@Override
	public void webInterfaceEnabled(final boolean value) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				webinterface.setEnabled(value);
			}
		});
	}

	/**
	 * This method is being called when a configuration change requiring
	 * a restart of the HTTP server has been done by the user. It should notify the user
	 * to restart the server.<br>
	 * Currently the icon as well as the tool tip text of the restart button is being
	 * changed.<br>
	 * The actions requiring a server restart are defined by {@link PmsConfiguration#NEED_RELOAD_FLAGS}
	 *
	 * @param required true if the server has to be restarted, false otherwise
	 */
	@Override
	public void setReloadable(final boolean required) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (required) {
					if (reload.getIcon() == restartIcon) {
						restartIcon.setNextStage(new AnimatedIconStage(AnimatedIconType.DEFAULTICON, restartRequredIcon, false));
						reload.setToolTipText(Messages.getString("LooksFrame.13"));
					}
				} else {
					reload.setEnabled(true);
					if (restartRequredIcon == reload.getIcon()) {
						reload.setToolTipText(Messages.getString("LooksFrame.28"));
						reload.setNextIcon(new AnimatedIconStage(AnimatedIconType.DEFAULTICON, restartIcon, false));
						restartRequredIcon.restartArm();
					}
				}
			}
		});
	}

	@Override
	public void addEngines() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tr.addEngines();
			}
		});
	}

	@Override
	public void setStatusLine(final String line) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (isBlank(line)) {
					status.setBorder(BorderFactory.createEmptyBorder());
					status.setText("");
				} else {
					status.setBorder(BorderFactory.createEmptyBorder(0, 9, 8, 0));
					status.setText(line);
				}
			}
		});
	}

	@Override
	public void addRenderer(RendererConfiguration renderer) {
		st.addRenderer(renderer);
	}

	@Override
	public void updateRenderer(RendererConfiguration renderer) {
		StatusTab.updateRenderer(renderer);
	}

	@Override
	public void serverReady() {
		st.updateMemoryUsage();
		gt.addRenderers();
	}

	@Override
	public void setScanLibraryEnabled(final boolean flag) {
		final NavigationShareTab navigationShareTab = getNt();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				navigationShareTab.setScanLibraryEnabled(flag);
			}
		});
	}

	@Override
	public String getLog() {
		return getTt().getList().getText();
	}

	/**
	 * @return The {@link MinimizeListenerRegistrar} instance created for this
	 *         {@link LooksFrame} instance.
	 */
	@Nonnull
	public MinimizeListenerRegistrar getMinimizeListenerRegistrar() {
		return minimizeListenerRegistrar;
	}

	/**
	 * @return The {@link LooksFrameTabListenerRegistrar} instance created for
	 *         this {@link LooksFrame} instance.
	 */
	@Nonnull
	public LooksFrameTabListenerRegistrar getTabListenerRegistrar() {
		return tabListenerRegistrar;
	}

	public static JComponent createStyledSeparator(String text, int style, ComponentOrientation orientation) {
		JLabel separatorLabel = new JLabel(text);
		separatorLabel.setFont(separatorLabel.getFont().deriveFont(style));
		separatorLabel.setHorizontalAlignment(orientation.isLeftToRight() ? SwingConstants.LEFT : SwingConstants.RIGHT);
		return DefaultComponentFactory.getInstance().createSeparator(separatorLabel);
	}

	/**
	 * This {@code enum} represents the different "main tabs" of DMS' GUI.
	 *
	 * @author Nadahar
	 */
	public static enum LooksFrameTab {

		/** The {@code Status} tab */
		STATUS_TAB,

		/** The {@code Logs} tab */
		TRACES_TAB,

		/** The {@code General Configuration} tab */
		GENERAL_TAB,

		/** The {@code Navigation/Share Settings} tab */
		NAVIGATION_TAB,

		/** The {@code Transcoding Settings} tab */
		TRANSCODING_TAB,

		/** The {@code Help} tab */
		HELP_TAB,

		/** The {@code About} tab */
		ABOUT_TAB,

		/** An unknown or invalid tab */
		UNKNOWN;

		/**
		 * Converts a tab index value to a {@link LooksFrameTab} instance.
		 *
		 * @param value the tab index value.
		 * @return The corresponding {@link LooksFrameTab} instance.
		 */
		@Nonnull
		public static LooksFrameTab typeOf(int value) {
			switch (value) {
				case 0:
					return STATUS_TAB;
				case 1:
					return TRACES_TAB;
				case 2:
					return GENERAL_TAB;
				case 3:
					return NAVIGATION_TAB;
				case 4:
					return TRANSCODING_TAB;
				case 5:
					return HELP_TAB;
				case 6:
					return ABOUT_TAB;
				default:
					return UNKNOWN;
			}
		}
	}

	/**
	 * This class is an {@link AnimatedIconListenerRegistrar} implementation
	 * that registers {@link AnimatedIcon}s and suspends or unsuspends them on
	 * {@link LooksFrame} tab change events.
	 *
	 * @author Nadahar
	 */
	public static class LooksFrameTabListenerRegistrar implements AnimatedIconListenerRegistrar {

		private final LooksFrameTabModelChangeListener listener;
		private final HashMap<AnimatedIcon, SubscriberInfo> subscribers = new HashMap<>();
		private boolean isListening;
		private final AnimatedIconListenerAction<LooksFrameTab> listenerAction = new AnimatedIconListenerAction<LooksFrameTab>() {

			@Override
			public void executeAction(@Nullable LooksFrameTab value) {
				if (value == null) {
					return;
				}
				for (Entry<AnimatedIcon, SubscriberInfo> entry : subscribers.entrySet()) {
					if (value == entry.getValue().tab) {
						unsuspendIcon(entry.getKey(), entry.getValue());
					} else {
						suspendIcon(entry.getKey(), entry.getValue());
					}
				}
			}
		};

		/**
		 * Unsuspends the specified {@link AnimatedIcon} if it is suspended and
		 * updates the specified {@link SubscriberInfo}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to unsuspend.
		 * @param subscriberInfo the {@link SubscriberInfo} belonging to
		 *            {@code animatedIcon}.
		 */
		protected void unsuspendIcon(@Nonnull AnimatedIcon animatedIcon, @Nonnull SubscriberInfo subscriberInfo) {
			if (subscriberInfo.suspended) {
				animatedIcon.unsuspend();
				subscriberInfo.suspended = false;
			}
		}

		/**
		 * Suspends the specified {@link AnimatedIcon} if it isn't already
		 * suspended and updates the specified {@link SubscriberInfo}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to suspend.
		 * @param subscriberInfo the {@link SubscriberInfo} belonging to
		 *            {@code animatedIcon}.
		 */
		protected void suspendIcon(@Nonnull AnimatedIcon animatedIcon, @Nonnull SubscriberInfo subscriberInfo) {
			if (!subscriberInfo.suspended) {
				animatedIcon.suspend();
				subscriberInfo.suspended = true;
			}
		}

		/**
		 * Creates a new instance that listens to the specified
		 * {@link JTabbedPane}.
		 *
		 * @param mainTabbedPane the {@link JTabbedPane} instance to listen to.
		 */
		protected LooksFrameTabListenerRegistrar(@Nonnull JTabbedPane mainTabbedPane) {
			if (mainTabbedPane == null) {
				throw new IllegalArgumentException("mainTabbedPane cannot be null");
			}
			listener = new LooksFrameTabModelChangeListener(mainTabbedPane);
		}

		/**
		 * Registers the specified {@link AnimatedIcon} with a
		 * {@link LooksFrameTabModelChangeListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to register.
		 * @param visibleTab the {@link LooksFrameTab} for which
		 *            {@code animatedIcon} should be visible.
		 */
		public void register(@Nullable AnimatedIcon animatedIcon, @Nullable LooksFrameTab visibleTab) {
			if (animatedIcon == null || visibleTab == null) {
				return;
			}
			SubscriberInfo subscriberInfo = subscribers.get(animatedIcon);
			if (subscriberInfo == null || subscriberInfo.tab != visibleTab) {
				if (subscriberInfo != null) {
					LOGGER.debug(
						"TabListenerRegistrar Warning: Changing tab on already registered icon {} from {} to {}",
						animatedIcon,
						subscriberInfo.tab,
						visibleTab
					);
					if (subscriberInfo.suspended) {
						unsuspendIcon(animatedIcon, subscriberInfo);
					}
				}
				subscriberInfo = new SubscriberInfo(visibleTab);
				if (listener.getCurrentTab() != visibleTab) {
					suspendIcon(animatedIcon, subscriberInfo);
					subscriberInfo.suspended = true;
				}
				subscribers.put(animatedIcon, subscriberInfo);
				if (!isListening) {
					listener.registerAction(listenerAction);
					isListening = true;
				}
			}
		}

		/**
		 * Unregisters the specified {@link AnimatedIcon} with a
		 * {@link LooksFrameTabModelChangeListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to unregister.
		 */
		public void unregister(@Nullable AnimatedIcon animatedIcon) {
			if (animatedIcon != null && subscribers.containsKey(animatedIcon)) {
				if (subscribers.get(animatedIcon).suspended) {
					animatedIcon.unsuspend();
				}
				subscribers.remove(animatedIcon);
				if (isListening && subscribers.size() == 0) {
					listener.unregisterAction(listenerAction);
					isListening = false;
				}
			}
		}

		/**
		 * An internal "struct" to hold information about subscribing
		 * {@link AnimatedIcon}s.
		 */
		protected static class SubscriberInfo {

			private final LooksFrameTab tab;
			private boolean suspended;

			/**
			 * Creates a new instance for the specified {@link LooksFrameTab}.
			 *
			 * @param visibleTab the {@link LooksFrameTab} for which the
			 *            associated {@link AnimatedIcon} should be visible.
			 */
			public SubscriberInfo(LooksFrameTab visibleTab) {
				this.tab = visibleTab;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + " [suspended=" + suspended + ", tab=" + tab + "]";
			}
		}
	}

	/**
	 * This is an {@link AnimatedIconListener} implementation that listens to
	 * {@link LooksFrame} tab change events and executes registered
	 * {@link AnimatedIconListenerAction}s in response to events.
	 *
	 * @author Nadahar
	 */
	public static class LooksFrameTabModelChangeListener implements AnimatedIconListener<LooksFrameTab>, ChangeListener {

		@Nonnull private final JTabbedPane mainTabbedPane;
		@Nonnull private final HashSet<AnimatedIconListenerAction<LooksFrameTab>> actions = new HashSet<>();
		private boolean isListening;
		@Nonnull private LooksFrameTab currentTab;


		/**
		 * Creates a new instance using the specified {@link JTabbedPane}
		 * instance.
		 *
		 * @param mainTabbedPane the {@link JTabbedPane} instance to listen to.
		 * @param actions (optional) one or more
		 *            {@link AnimatedIconListenerAction}s to add.
		 */
		@SafeVarargs
		public LooksFrameTabModelChangeListener(
			@Nonnull JTabbedPane mainTabbedPane,
			@Nullable AnimatedIconListenerAction<LooksFrameTab>... actions
		) {
			if (mainTabbedPane == null) {
				throw new IllegalArgumentException("mainTabbedPane cannot be null");
			}
			this.mainTabbedPane = mainTabbedPane;
			this.currentTab = LooksFrameTab.typeOf(mainTabbedPane.getSelectedIndex());
			if (actions != null && actions.length > 0) {
				this.actions.addAll(Arrays.asList(actions));
			}
		}

		/**
		 * @return The currently active {@link LooksFrameTab}.
		 */
		public LooksFrameTab getCurrentTab() {
			return currentTab;
		}

		@Override
		public boolean registerAction(@Nullable AnimatedIconListenerAction<LooksFrameTab> action) {
			if (action == null) {
				return false;
			}
			boolean add = actions.add(action);
			if (add && !isListening) {
				mainTabbedPane.getModel().addChangeListener(this);
				isListening = true;
			}
			return add;
		}

		@Override
		public boolean unregisterAction(@Nullable AnimatedIconListenerAction<LooksFrameTab> action) {
			if (action == null) {
				return false;
			}
			boolean remove = actions.remove(action);
			if (remove && isListening && actions.size() == 0) {
				mainTabbedPane.getModel().removeChangeListener(this);
				isListening = false;
			}
			return remove;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e != null && e.getSource() instanceof SingleSelectionModel) {
				LooksFrameTab newTab = LooksFrameTab.typeOf(((SingleSelectionModel) e.getSource()).getSelectedIndex());
				if (newTab != currentTab) {
					currentTab = newTab;
					for (AnimatedIconListenerAction<LooksFrameTab> action : actions) {
						action.executeAction(newTab);
					}
				}
			}
		}
	}

	/**
	 * Creates a new {@link AnimatedIconListenerRegistrar} that registers tab
	 * change and application minimize events. Suitable for {@link AnimatedIcon}
	 * s that's visible whenever a given tab is visible.
	 *
	 * @author Nadahar
	 */
	public abstract static class AbstractTabListenerRegistrar implements AnimatedIconListenerRegistrar {

		private final MinimizeListenerRegistrar minimizeListenerRegistrar;
		private final LooksFrameTabListenerRegistrar tabListenerRegistrar;

		/**
		 * Creates a new instance using the specified {@link LooksFrame}
		 * instance.
		 *
		 * @param looksFrame the {@link LooksFrame} instance to listen to.
		 */
		protected AbstractTabListenerRegistrar(@Nonnull LooksFrame looksFrame) {
			if (looksFrame == null) {
				throw new IllegalArgumentException("looksFrame cannot be null");
			}
			minimizeListenerRegistrar = looksFrame.getMinimizeListenerRegistrar();
			tabListenerRegistrar = looksFrame.getTabListenerRegistrar();
		}

		/**
		 * @return The tab for which the {@link AnimatedIcon} is visible
		 */
		protected abstract LooksFrameTab getVisibleTab();

		/**
		 * Registers the specified {@link AnimatedIcon} with both a
		 * {@link WindowIconifyListener} and a
		 * {@link LooksFrameTabModelChangeListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to register.
		 */
		public void register(@Nullable AnimatedIcon animatedIcon) {
			if (animatedIcon != null) {
				minimizeListenerRegistrar.register(animatedIcon);
				tabListenerRegistrar.register(animatedIcon, getVisibleTab());
			}
		}

		/**
		 * Unregisters the specified {@link AnimatedIcon} with both a
		 * {@link WindowIconifyListener} and a
		 * {@link LooksFrameTabModelChangeListener}.
		 *
		 * @param animatedIcon the {@link AnimatedIcon} to unregister.
		 */
		public void unregister(AnimatedIcon animatedIcon) {
			if (animatedIcon != null) {
				minimizeListenerRegistrar.unregister(animatedIcon);
				tabListenerRegistrar.unregister(animatedIcon);
			}
		}
	}
}
