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
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.image.thumbnail.CoverSupplier;
import net.pms.io.BasicSystemUtils;
import net.pms.newgui.LooksFrame.AbstractTabListenerRegistrar;
import net.pms.newgui.LooksFrame.LooksFrameTab;
import net.pms.newgui.components.AnimatedIcon;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconFrame;
import net.pms.newgui.components.AnimatedIcon.AnimatedIconListenerRegistrar;
import net.pms.newgui.components.CustomJButton;
import net.pms.newgui.components.AnimatedButton;
import net.pms.newgui.components.ImageButton;
import net.pms.util.FileUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.FullyPlayedAction;
import net.pms.util.KeyedComboBoxModel;
import net.pms.util.UMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationShareTab {
	private static final Logger LOGGER = LoggerFactory.getLogger(NavigationShareTab.class);
	private static final Vector<String> FOLDERS_COLUMN_NAMES = new Vector<>(
		Arrays.asList(new String[] {Messages.getString("Generic.Folder"), Messages.getString("FoldTab.65")})
	);

	private JPanel sharedPanel;
	private JTable sharedFolders;
	private SharedFoldersTableModel folderTableModel;
	private JCheckBox hidevideosettings;
	private JCheckBox hidetranscode;
	private JCheckBox hidemedialibraryfolder;
	private JCheckBox hideextensions;
	private JCheckBox hideemptyfolders;
	private JCheckBox hideengines;
	private JTextField seekpos;
	private JCheckBox thumbgenCheckBox;
	private JCheckBox mplayer_thumb;
	private JCheckBox dvdiso_thumb;
	private JCheckBox image_thumb;
	private JCheckBox cacheenable;
	private JCheckBox archive;
	private JComboBox<String> sortmethod;
	private JComboBox<String> audiothumbnail;
	private JTextField defaultThumbFolder;
	private JCheckBox iphoto;
	private JCheckBox aperture;
	public static JCheckBox itunes;
	private CustomJButton select;
	private CustomJButton cachereset;
	private JCheckBox ignorethewordthe;
	private JTextField atzLimit;
	private JCheckBox liveSubtitles;
	private JCheckBox prettifyfilenames;
	private JCheckBox episodeTitles;
	private JCheckBox newmediafolder;
	private JCheckBox recentlyplayedfolder;
	private JCheckBox resume;
	private JComboBox<String> fullyPlayedAction;
	private JTextField fullyPlayedOutputDirectory;
	private CustomJButton selectFullyPlayedOutputDirectory;
	private final ImageButton customizeButton = new ImageButton();
	private final ImageButton addButton = new ImageButton("button-add-folder.png");
	private final ImageButton removeButton = new ImageButton("button-remove-folder.png");
	private final ImageButton arrowDownButton = new ImageButton("button-arrow-down.png");
	private final ImageButton arrowUpButton = new ImageButton("button-arrow-up.png");
	private final AnimatedButton scanButton = new AnimatedButton("button-scan.png");
	private final AnimatedIcon scanNormalIcon = (AnimatedIcon) scanButton.getIcon();
	private final AnimatedIcon scanRolloverIcon = (AnimatedIcon) scanButton.getRolloverIcon();
	private final AnimatedIcon scanPressedIcon = (AnimatedIcon) scanButton.getPressedIcon();
	private final AnimatedIcon scanDisabledIcon = (AnimatedIcon) scanButton.getDisabledIcon();
	private final AnimatedIcon scanBusyIcon;
	private final AnimatedIcon scanBusyRolloverIcon;
	private final AnimatedIcon scanBusyPressedIcon;
	private final AnimatedIcon scanBusyDisabledIcon;
	private final NavigationTabListenerRegistrar tabListenerRegistrar;

	private final PmsConfiguration configuration;
	private final LooksFrame looksFrame;

	NavigationShareTab(PmsConfiguration configuration, LooksFrame looksFrame) {
		this.configuration = configuration;
		this.looksFrame = looksFrame;
		tabListenerRegistrar = new NavigationTabListenerRegistrar(looksFrame);
		scanBusyIcon = new AnimatedIcon(
			scanButton,
			true,
			AnimatedIcon.buildAnimation("button-scan-busyF%d.png", 0, 14, false, 35, 35, 35)
		);
		scanBusyRolloverIcon = new AnimatedIcon(
			scanButton,
			false,
			new AnimatedIconFrame(LooksFrame.readImageIcon("button-cancel.png"), 0)
		);
		scanBusyPressedIcon = new AnimatedIcon(
			scanButton,
			false,
			new AnimatedIconFrame(LooksFrame.readImageIcon("button-cancel_pressed.png"), 0)
		);
		scanBusyDisabledIcon = new AnimatedIcon(
			scanButton,
			true,
			AnimatedIcon.buildAnimation("button-scan-busyF%d_disabled.png", 0, 14, false, 35, 35, 35)
		);
	}

	private static final String PANEL_COL_SPEC = "left:pref,          50dlu,                pref, 150dlu,                       pref, 25dlu,               pref, 9dlu, pref, default:grow, pref, 25dlu";
	private static final String PANEL_ROW_SPEC =
		//                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		"p,"                              // Thumbnails
		+ "3dlu,"                         //
		+ "p,"                            //                      Generate thumbnails         Thumbnail seeking position:         [seeking position]               Image thumbnails
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "9dlu,"                         //
		+ "p,"                            // File sorting
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "9dlu,"                         //
		+ "p,"                            // Virtual folders
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "3dlu,"                         //
		+ "p,"                            //
		+ "9dlu,"                         //
		+ "fill:default:grow";            // Shared folders
	private static final String SHARED_FOLDER_COL_SPEC = "left:pref, left:pref, pref, pref, pref, pref, 0:grow";
	private static final String SHARED_FOLDER_ROW_SPEC = "2*(p, 3dlu), 55dlu:grow";

	public JComponent build() {
		// Apply the orientation for the locale
		ComponentOrientation orientation = ComponentOrientation.getOrientation(PMS.getLocale());
		String colSpec = FormLayoutUtil.getColSpec(PANEL_COL_SPEC, orientation);

		// Set basic layout
		FormLayout layout = new FormLayout(colSpec, PANEL_ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.DLU4);
		builder.opaque(true);

		CellConstraints cc = new CellConstraints();

		// Init all gui components
		initSimpleComponents(cc);
		sharedPanel = initSharedFoldersGuiComponents(cc).build();

		// Build gui with initialized components
		if (!configuration.isHideAdvancedOptions()) {
			JComponent cmp = builder.addSeparator(Messages.getString("FoldTab.13"), FormLayoutUtil.flip(cc.xyw(1, 1, 12), colSpec, orientation));
			cmp = (JComponent) cmp.getComponent(0);
			cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

			builder.add(GuiUtil.getPreferredSizeComponent(thumbgenCheckBox),        FormLayoutUtil.flip(cc.xyw(1, 3, 3), colSpec, orientation));
			builder.addLabel(Messages.getString("NetworkTab.16"),                   FormLayoutUtil.flip(cc.xyw(4, 3, 2), colSpec, orientation));
			builder.add(seekpos,                                                    FormLayoutUtil.flip(cc.xy(6, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(image_thumb),             FormLayoutUtil.flip(cc.xyw(9, 3, 4), colSpec, orientation));

			builder.addLabel(Messages.getString("FoldTab.26"),                      FormLayoutUtil.flip(cc.xyw(1, 5, 3), colSpec, orientation));
			builder.add(audiothumbnail,                                             FormLayoutUtil.flip(cc.xyw(4, 5, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(mplayer_thumb),           FormLayoutUtil.flip(cc.xyw(9, 5, 4), colSpec, orientation));

			builder.addLabel(Messages.getString("FoldTab.27"),                      FormLayoutUtil.flip(cc.xy(1, 7), colSpec, orientation));
			builder.add(defaultThumbFolder,                                         FormLayoutUtil.flip(cc.xyw(4, 7, 2), colSpec, orientation));
			builder.add(select,                                                     FormLayoutUtil.flip(cc.xy(6, 7), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(dvdiso_thumb),            FormLayoutUtil.flip(cc.xyw(9, 7, 4), colSpec, orientation));

			cmp = builder.addSeparator(Messages.getString("NetworkTab.59"),         FormLayoutUtil.flip(cc.xyw(1, 9, 12), colSpec, orientation));
			cmp = (JComponent) cmp.getComponent(0);
			cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

			builder.addLabel(Messages.getString("FoldTab.18"),                      FormLayoutUtil.flip(cc.xyw(1, 11, 3), colSpec, orientation));
			builder.add(sortmethod,                                                 FormLayoutUtil.flip(cc.xyw(4, 11, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(ignorethewordthe),        FormLayoutUtil.flip(cc.xyw(9, 11, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(prettifyfilenames),       FormLayoutUtil.flip(cc.xyw(1, 13, 5), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(episodeTitles),           FormLayoutUtil.flip(cc.xyw(9, 13, 4), colSpec, orientation));

			cmp = builder.addSeparator(Messages.getString("NetworkTab.60"),         FormLayoutUtil.flip(cc.xyw(1, 15, 12), colSpec, orientation));
			cmp = (JComponent) cmp.getComponent(0);
			cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

			builder.add(GuiUtil.getPreferredSizeComponent(hideextensions),          FormLayoutUtil.flip(cc.xyw(1, 17, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(hideengines),             FormLayoutUtil.flip(cc.xyw(4, 17, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(hideemptyfolders),        FormLayoutUtil.flip(cc.xyw(9, 17, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(itunes),                  FormLayoutUtil.flip(cc.xy(1, 19), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(iphoto),                  FormLayoutUtil.flip(cc.xyw(4, 19, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(aperture),                FormLayoutUtil.flip(cc.xyw(9, 19, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(cacheenable),             FormLayoutUtil.flip(cc.xy(1, 21), colSpec, orientation));
			builder.add(cachereset,                                                 FormLayoutUtil.flip(cc.xyw(4, 21, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(hidemedialibraryfolder),  FormLayoutUtil.flip(cc.xyw(9, 21, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(archive),                 FormLayoutUtil.flip(cc.xyw(1, 23, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(hidevideosettings),       FormLayoutUtil.flip(cc.xyw(4, 23, 3), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(hidetranscode),           FormLayoutUtil.flip(cc.xyw(9, 23, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(liveSubtitles),           FormLayoutUtil.flip(cc.xyw(1, 25, 3), colSpec, orientation));
			builder.addLabel(Messages.getString("FoldTab.37"),                      FormLayoutUtil.flip(cc.xyw(4, 25, 2), colSpec, orientation));
			builder.add(atzLimit,                                                   FormLayoutUtil.flip(cc.xy(6, 25), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(newmediafolder),          FormLayoutUtil.flip(cc.xyw(9, 25, 4), colSpec, orientation));

			builder.add(GuiUtil.getPreferredSizeComponent(resume),                  FormLayoutUtil.flip(cc.xy(1, 27), colSpec, orientation));
			builder.add(GuiUtil.getPreferredSizeComponent(recentlyplayedfolder),    FormLayoutUtil.flip(cc.xyw(4, 27, 3), colSpec, orientation));

			builder.addLabel(Messages.getString("FoldTab.72"),                      FormLayoutUtil.flip(cc.xy (1,  29   ), colSpec, orientation));
			builder.add(fullyPlayedAction,                                          FormLayoutUtil.flip(cc.xyw(4,  29, 3), colSpec, orientation));
			builder.add(fullyPlayedOutputDirectory,                                 FormLayoutUtil.flip(cc.xyw(9,  29, 2), colSpec, orientation));
			builder.add(selectFullyPlayedOutputDirectory,                           FormLayoutUtil.flip(cc.xyw(11, 29, 2), colSpec, orientation));

			builder.add(sharedPanel,                                                FormLayoutUtil.flip(cc.xyw(1, 31, 12), colSpec, orientation));
		} else {
			builder.add(sharedPanel, FormLayoutUtil.flip(cc.xyw(1, 1, 12), colSpec, orientation));
		}

		builder.add(sharedPanel, FormLayoutUtil.flip(cc.xyw(1, 31, 12), colSpec, orientation));

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		JScrollPane scrollPane = new JScrollPane(
			panel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);

		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	private void initSimpleComponents(CellConstraints cc) {
		// Thumbnail seeking position
		seekpos = new JTextField("" + configuration.getThumbnailSeekPos());
		seekpos.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(seekpos.getText());
					configuration.setThumbnailSeekPos(ab);
					if (configuration.getUseCache()) {
						PMS.get().getDatabase().deleteThumbnails();
					}
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse thumbnail seek position from \"" + seekpos.getText() + "\"");
				}

			}
		});
		if (configuration.isThumbnailGenerationEnabled()) {
			seekpos.setEnabled(true);
		} else {
			seekpos.setEnabled(false);
		}

		// Generate thumbnails
		thumbgenCheckBox = new JCheckBox(Messages.getString("NetworkTab.2"), configuration.isThumbnailGenerationEnabled());
		thumbgenCheckBox.setContentAreaFilled(false);
		thumbgenCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setThumbnailGenerationEnabled((e.getStateChange() == ItemEvent.SELECTED));
				seekpos.setEnabled(configuration.isThumbnailGenerationEnabled());
				mplayer_thumb.setEnabled(configuration.isThumbnailGenerationEnabled());
			}
		});

		// Use MPlayer for video thumbnails
		mplayer_thumb = new JCheckBox(Messages.getString("FoldTab.14"), configuration.isUseMplayerForVideoThumbs());
		mplayer_thumb.setToolTipText(Messages.getString("FoldTab.61"));
		mplayer_thumb.setContentAreaFilled(false);
		mplayer_thumb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setUseMplayerForVideoThumbs((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		if (configuration.isThumbnailGenerationEnabled()) {
			mplayer_thumb.setEnabled(true);
		} else {
			mplayer_thumb.setEnabled(false);
		}

		// DVD ISO thumbnails
		dvdiso_thumb = new JCheckBox(Messages.getString("FoldTab.19"), configuration.isDvdIsoThumbnails());
		dvdiso_thumb.setContentAreaFilled(false);
		dvdiso_thumb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setDvdIsoThumbnails((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Image thumbnails
		image_thumb = new JCheckBox(Messages.getString("FoldTab.21"), configuration.getImageThumbnailsEnabled());
		image_thumb.setContentAreaFilled(false);
		image_thumb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setImageThumbnailsEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Audio thumbnails import
		final KeyedComboBoxModel<CoverSupplier, String> thumbKCBM = new KeyedComboBoxModel<>(
			new CoverSupplier[]{CoverSupplier.NONE, CoverSupplier.COVER_ART_ARCHIVE},
			new String[]{Messages.getString("FoldTab.35"), Messages.getString("FoldTab.73")}
		);
		audiothumbnail = new JComboBox<>(thumbKCBM);
		audiothumbnail.setEditable(false);

		thumbKCBM.setSelectedKey(configuration.getAudioThumbnailMethod());

		audiothumbnail.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setAudioThumbnailMethod(thumbKCBM.getSelectedKey());
					LOGGER.info("Setting {} {}", Messages.getRootString("FoldTab.26"), thumbKCBM.getSelectedValue());
				}
			}
		});

		// Alternate video cover art folder
		defaultThumbFolder = new JTextField(configuration.getAlternateThumbFolder());
		defaultThumbFolder.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAlternateThumbFolder(defaultThumbFolder.getText());
			}
		});

		// Alternate video cover art folder button
		select = new CustomJButton("...");
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					defaultThumbFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setAlternateThumbFolder(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		// Hide Server Settings folder
		hidevideosettings = new JCheckBox(Messages.getString("FoldTab.38"), configuration.getHideVideoSettings());
		hidevideosettings.setContentAreaFilled(false);
		hidevideosettings.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideVideoSettings((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Hide #--TRANSCODE--# folder
		hidetranscode = new JCheckBox(Messages.getString("FoldTab.33"), configuration.getHideTranscodeEnabled());
		hidetranscode.setContentAreaFilled(false);
		hidetranscode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideTranscodeEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Hide cache folder
		hidemedialibraryfolder = new JCheckBox(Messages.getString("FoldTab.32"), configuration.isHideMediaLibraryFolder());
		hidemedialibraryfolder.setContentAreaFilled(false);
		hidemedialibraryfolder.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideMediaLibraryFolder((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Browse compressed archives
		archive = new JCheckBox(Messages.getString("NetworkTab.1"), configuration.isArchiveBrowsing());
		archive.setContentAreaFilled(false);
		archive.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setArchiveBrowsing(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		// Enable the cache
		cacheenable = new JCheckBox(Messages.getString("NetworkTab.17"), configuration.getUseCache());
		cacheenable.setToolTipText(Messages.getString("FoldTab.48"));
		cacheenable.setContentAreaFilled(false);
		cacheenable.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setUseCache((e.getStateChange() == ItemEvent.SELECTED));
				cachereset.setEnabled(configuration.getUseCache());
				setScanLibraryEnabled(configuration.getUseCache());
			}
		});

		// Reset cache
		cachereset = new CustomJButton(Messages.getString("NetworkTab.18"));
		cachereset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(
					looksFrame,
					Messages.getString("NetworkTab.13") + Messages.getString("NetworkTab.19"),
					Messages.getString("Dialog.Question"),
					JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					PMS.get().getDatabase().init(true);
				}

			}
		});
		cachereset.setEnabled(configuration.getUseCache());

		// Hide file extensions
		hideextensions = new JCheckBox(Messages.getString("FoldTab.5"), configuration.isHideExtensions());
		hideextensions.setContentAreaFilled(false);
		if (configuration.isPrettifyFilenames()) {
			hideextensions.setEnabled(false);
		}
		hideextensions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideExtensions((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Hide transcoding engine names
		hideengines = new JCheckBox(Messages.getString("FoldTab.8"), configuration.isHideEngineNames());
		hideengines.setToolTipText(Messages.getString("FoldTab.46"));
		hideengines.setContentAreaFilled(false);
		hideengines.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideEngineNames((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Hide empty folders
		hideemptyfolders = new JCheckBox(Messages.getString("FoldTab.31"), configuration.isHideEmptyFolders());
		hideemptyfolders.setToolTipText(Messages.getString("FoldTab.59"));
		hideemptyfolders.setContentAreaFilled(false);
		hideemptyfolders.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideEmptyFolders((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Show iTunes library
		itunes = new JCheckBox(Messages.getString("FoldTab.30"), configuration.isShowItunesLibrary());
		itunes.setToolTipText(Messages.getString("FoldTab.47"));
		itunes.setContentAreaFilled(false);
		if (!(Platform.isMac() || Platform.isWindows())) {
			itunes.setEnabled(false);
		}
		itunes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setShowItunesLibrary((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Show iPhoto library
		iphoto = new JCheckBox(Messages.getString("FoldTab.29"), configuration.isShowIphotoLibrary());
		iphoto.setContentAreaFilled(false);
		if (!Platform.isMac()) {
			iphoto.setEnabled(false);
		}
		iphoto.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setShowIphotoLibrary((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Show aperture library
		aperture = new JCheckBox(Messages.getString("FoldTab.34"), configuration.isShowApertureLibrary());
		aperture.setContentAreaFilled(false);
		if (!Platform.isMac()) {
			aperture.setEnabled(false);
		}
		aperture.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setShowApertureLibrary((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// File order
		final KeyedComboBoxModel<Integer, String> kcbm = new KeyedComboBoxModel<>(
			new Integer[]{
				UMSUtils.SORT_LOC_SENS,  // alphabetical
				UMSUtils.SORT_LOC_NAT,   // natural sort
				UMSUtils.SORT_INS_ASCII, // ASCIIbetical
				UMSUtils.SORT_MOD_NEW,   // newest first
				UMSUtils.SORT_MOD_OLD,   // oldest first
				UMSUtils.SORT_RANDOM,    // random
				UMSUtils.SORT_NO_SORT    // no sorting
			},
			new String[]{
				Messages.getString("FoldTab.15"),
				Messages.getString("FoldTab.22"),
				Messages.getString("FoldTab.20"),
				Messages.getString("FoldTab.16"),
				Messages.getString("FoldTab.17"),
				Messages.getString("FoldTab.58"),
				Messages.getString("FoldTab.62")
			}
		);
		sortmethod = new JComboBox<>(kcbm);
		sortmethod.setEditable(false);
		kcbm.setSelectedKey(configuration.getSortMethod(null));

		sortmethod.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setSortMethod(kcbm.getSelectedKey());
					LOGGER.info("Setting {} {}", Messages.getRootString("FoldTab.18"), kcbm.getSelectedValue());
				}
			}
		});

		// Ignore the word "the" while sorting
		ignorethewordthe = new JCheckBox(Messages.getString("FoldTab.39"), configuration.isIgnoreTheWordAandThe());
		ignorethewordthe.setToolTipText(Messages.getString("FoldTab.44"));
		ignorethewordthe.setContentAreaFilled(false);
		ignorethewordthe.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setIgnoreTheWordAandThe((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		atzLimit = new JTextField("" + configuration.getATZLimit());
		atzLimit.setToolTipText(Messages.getString("FoldTab.49"));
		atzLimit.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(atzLimit.getText());
					configuration.setATZLimit(ab);
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse ATZ limit from \"" + atzLimit.getText() + "\"");
					LOGGER.debug("The full error was: " + nfe);
				}
			}
		});

		liveSubtitles = new JCheckBox(Messages.getString("FoldTab.42"), configuration.isHideLiveSubtitlesFolder());
		liveSubtitles.setContentAreaFilled(false);
		liveSubtitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideLiveSubtitlesFolder((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		prettifyfilenames = new JCheckBox(Messages.getString("FoldTab.43"), configuration.isPrettifyFilenames());
		prettifyfilenames.setToolTipText(Messages.getString("FoldTab.45"));
		prettifyfilenames.setContentAreaFilled(false);
		prettifyfilenames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setPrettifyFilenames((e.getStateChange() == ItemEvent.SELECTED));
				hideextensions.setEnabled((e.getStateChange() != ItemEvent.SELECTED));
				episodeTitles.setEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		episodeTitles = new JCheckBox(Messages.getString("FoldTab.74"), configuration.isUseInfoFromIMDb());
		episodeTitles.setToolTipText(Messages.getString("FoldTab.64"));
		episodeTitles.setContentAreaFilled(false);
		if (!configuration.isPrettifyFilenames()) {
			episodeTitles.setEnabled(false);
		}
		episodeTitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setUseInfoFromIMDb((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		newmediafolder = new JCheckBox(Messages.getString("FoldTab.54"), configuration.isHideNewMediaFolder());
		newmediafolder.setToolTipText(Messages.getString("FoldTab.66"));
		newmediafolder.setContentAreaFilled(false);
		newmediafolder.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideNewMediaFolder((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		resume = new JCheckBox(Messages.getString("NetworkTab.68"), configuration.isResumeEnabled());
		resume.setToolTipText(Messages.getString("NetworkTab.69"));
		resume.setContentAreaFilled(false);
		resume.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setResume((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		recentlyplayedfolder = new JCheckBox(Messages.getString("FoldTab.55"), configuration.isHideRecentlyPlayedFolder());
		recentlyplayedfolder.setContentAreaFilled(false);
		recentlyplayedfolder.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideRecentlyPlayedFolder((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		// Fully played action
		final KeyedComboBoxModel<FullyPlayedAction, String> fullyPlayedActionModel = new KeyedComboBoxModel<>(
			new FullyPlayedAction[]{
				FullyPlayedAction.NO_ACTION,
				FullyPlayedAction.MARK,
				FullyPlayedAction.HIDE_VIDEO,
				FullyPlayedAction.MOVE_FOLDER,
				FullyPlayedAction.MOVE_TRASH
			},
			new String[]{
				Messages.getString("FoldTab.67"),
				Messages.getString("FoldTab.68"),
				Messages.getString("FoldTab.69"),
				Messages.getString("FoldTab.70"),
				Messages.getString("FoldTab.71")
			}
		);
		fullyPlayedAction = new JComboBox<>(fullyPlayedActionModel);
		fullyPlayedAction.setEditable(false);
		fullyPlayedActionModel.setSelectedKey(configuration.getFullyPlayedAction());
		fullyPlayedAction.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setFullyPlayedAction(fullyPlayedActionModel.getSelectedKey());
					fullyPlayedOutputDirectory.setEnabled(fullyPlayedActionModel.getSelectedKey() == FullyPlayedAction.MOVE_FOLDER);
					selectFullyPlayedOutputDirectory.setEnabled(fullyPlayedActionModel.getSelectedKey() == FullyPlayedAction.MOVE_FOLDER);
				}
			}
		});

		// Watched video output directory
		fullyPlayedOutputDirectory = new JTextField(configuration.getFullyPlayedOutputDirectory());
		fullyPlayedOutputDirectory.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setFullyPlayedOutputDirectory(fullyPlayedOutputDirectory.getText());
			}
		});
		fullyPlayedOutputDirectory.setEnabled(configuration.getFullyPlayedAction() == FullyPlayedAction.MOVE_FOLDER);

		// Watched video output directory selection button
		selectFullyPlayedOutputDirectory = new CustomJButton("...");
		selectFullyPlayedOutputDirectory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fullyPlayedOutputDirectory.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setFullyPlayedOutputDirectory(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		selectFullyPlayedOutputDirectory.setEnabled(configuration.getFullyPlayedAction() == FullyPlayedAction.MOVE_FOLDER);
	}

	private PanelBuilder initSharedFoldersGuiComponents(CellConstraints cc) {
		// Apply the orientation for the locale
		ComponentOrientation orientation = ComponentOrientation.getOrientation(PMS.getLocale());
		String colSpec = FormLayoutUtil.getColSpec(SHARED_FOLDER_COL_SPEC, orientation);

		FormLayout layoutFolders = new FormLayout(colSpec, SHARED_FOLDER_ROW_SPEC);
		PanelBuilder builderFolder = new PanelBuilder(layoutFolders);
		builderFolder.opaque(true);

		JComponent cmp = builderFolder.addSeparator(Messages.getString("FoldTab.7"), FormLayoutUtil.flip(cc.xyw(1, 1, 7), colSpec, orientation));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		boolean defaultSharedFolders = configuration.isDefaultSharedFolders();
		folderTableModel = new SharedFoldersTableModel();
		sharedFolders = new JTable(folderTableModel);

		/* An attempt to set the correct row height adjusted for font scaling.
		 * It sets all rows based on the font size of cell (0, 0). The + 4 is
		 * to allow 2 pixels above and below the text. */
		DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) sharedFolders.getCellRenderer(0,0);
		FontMetrics metrics = cellRenderer.getFontMetrics(cellRenderer.getFont());
		sharedFolders.setRowHeight(metrics.getLeading() + metrics.getMaxAscent() + metrics.getMaxDescent() + 4);
		sharedFolders.setIntercellSpacing(new Dimension(8, 2));
		sharedFolders.setEnabled(!defaultSharedFolders);

		updateCustomizeButton(!defaultSharedFolders);
		final JPanel tmpsharedPanel = sharedPanel;
		customizeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				toggleCustomizeFolders(tmpsharedPanel);
			}
		});
		builderFolder.add(customizeButton, FormLayoutUtil.flip(cc.xy(1, 3), colSpec, orientation));

		addButton.setToolTipText(Messages.getString("FoldTab.9"));
		addButton.setEnabled(!defaultSharedFolders);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog((Component) e.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					int firstSelectedRow = sharedFolders.getSelectedRow();
					if (firstSelectedRow >= 0) {
						((SharedFoldersTableModel) sharedFolders.getModel()).insertRow(
							firstSelectedRow,
							new Object[]{chooser.getSelectedFile().getAbsolutePath(), true}
						);
					} else {
						((SharedFoldersTableModel) sharedFolders.getModel()).addRow(
							new Object[]{chooser.getSelectedFile().getAbsolutePath(), true}
						);
					}
				}
			}
		});
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (configuration.isDefaultSharedFolders() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					if (
						JOptionPane.showConfirmDialog(
							tmpsharedPanel,
							Messages.getString("SharedFolders.EnableModify"),
							null,
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE
						) == JOptionPane.YES_OPTION
					) {
						toggleCustomizeFolders(tmpsharedPanel);
					}
				}
			}
		});
		builderFolder.add(addButton, FormLayoutUtil.flip(cc.xy(2, 3), colSpec, orientation));

		removeButton.setToolTipText(Messages.getString("FoldTab.36"));
		removeButton.setEnabled(!defaultSharedFolders);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = sharedFolders.getSelectedRows();
				if (rows.length > 0) {
					if (rows.length > 1) {
						if (
							JOptionPane.showConfirmDialog(
								tmpsharedPanel,
								String.format(
									PMS.getLocale(),
									Messages.getString("SharedFolders.ConfirmRemove"), rows.length
								),
								Messages.getString("Dialog.Confirm"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE
							) != JOptionPane.YES_OPTION
						) {
							return;
						}
					}
					for (int i = rows.length - 1; i >= 0; i--) {
						((SharedFoldersTableModel) sharedFolders.getModel()).removeRow(rows[i]);
					}
				}
			}
		});
		removeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (configuration.isDefaultSharedFolders() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					if (
						JOptionPane.showConfirmDialog(
							tmpsharedPanel,
							Messages.getString("SharedFolders.EnableModify"),
							null,
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE
						) == JOptionPane.YES_OPTION
					) {
						toggleCustomizeFolders(tmpsharedPanel);
					}
				}
			}
		});
		builderFolder.add(removeButton, FormLayoutUtil.flip(cc.xy(3, 3), colSpec, orientation));

		arrowDownButton.setToolTipText(Messages.getString("SharedFolders.ArrowDown"));
		arrowDownButton.setEnabled(!defaultSharedFolders);
		arrowDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < sharedFolders.getRowCount() - 1; i++) {
					if (sharedFolders.isRowSelected(i)) {
						Object  value1 = sharedFolders.getValueAt(i, 0);
						boolean value2 = (boolean) sharedFolders.getValueAt(i, 1);

						sharedFolders.setValueAt(sharedFolders.getValueAt(i + 1, 0), i    , 0);
						sharedFolders.setValueAt(value1                    , i + 1, 0);
						sharedFolders.setValueAt(sharedFolders.getValueAt(i + 1, 1), i    , 1);
						sharedFolders.setValueAt(value2                    , i + 1, 1);
						sharedFolders.changeSelection(i + 1, 1, false, false);

						break;
					}
				}
			}
		});
		builderFolder.add(arrowDownButton, FormLayoutUtil.flip(cc.xy(4, 3), colSpec, orientation));

		arrowUpButton.setToolTipText(Messages.getString("SharedFolders.ArrowUp"));
		arrowUpButton.setEnabled(!defaultSharedFolders);
		arrowUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i < sharedFolders.getRowCount(); i++) {
					if (sharedFolders.isRowSelected(i)) {
						Object  value1 = sharedFolders.getValueAt(i, 0);
						boolean value2 = (boolean) sharedFolders.getValueAt(i, 1);

						sharedFolders.setValueAt(sharedFolders.getValueAt(i - 1, 0), i    , 0);
						sharedFolders.setValueAt(value1                    , i - 1, 0);
						sharedFolders.setValueAt(sharedFolders.getValueAt(i - 1, 1), i    , 1);
						sharedFolders.setValueAt(value2                    , i - 1, 1);
						sharedFolders.changeSelection(i - 1, 1, false, false);

						break;

					}
				}
			}
		});
		builderFolder.add(arrowUpButton, FormLayoutUtil.flip(cc.xy(5, 3), colSpec, orientation));

		scanButton.setToolTipText(Messages.getString("FoldTab.2"));
		scanButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configuration.getUseCache()) {
					DLNAMediaDatabase database = PMS.get().getDatabase();

					if (database != null) {
						if (!database.isScanLibraryRunning()) {
							int option = JOptionPane.showConfirmDialog(
								looksFrame,
								Messages.getString("FoldTab.3") + Messages.getString("FoldTab.4"),
								Messages.getString("Dialog.Question"),
								JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.YES_OPTION) {
								database.scanLibrary();
								scanBusyIcon.startArm();
								scanBusyDisabledIcon.startArm();
								tabListenerRegistrar.register(scanBusyIcon);
								tabListenerRegistrar.register(scanBusyDisabledIcon);
								scanButton.setIcon(scanBusyIcon);
								scanButton.setRolloverIcon(scanBusyRolloverIcon);
								scanButton.setPressedIcon(scanBusyPressedIcon);
								scanButton.setDisabledIcon(scanBusyDisabledIcon);
								scanButton.setToolTipText(Messages.getString("FoldTab.40"));
							}
						} else {
							int option = JOptionPane.showConfirmDialog(
								looksFrame,
								Messages.getString("FoldTab.10"),
								Messages.getString("Dialog.Question"),
								JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.YES_OPTION) {
								database.stopScanLibrary();
								looksFrame.setStatusLine(Messages.getString("FoldTab.41"));
								scanButton.setEnabled(false);
								scanButton.setToolTipText(Messages.getString("FoldTab.41"));
							}
						}
					}
				}
			}
		});

		builderFolder.add(scanButton, FormLayoutUtil.flip(cc.xy(6, 3), colSpec, orientation));
		scanButton.setEnabled(configuration.getUseCache());

		updateSharedFolders();

		JScrollPane pane = new JScrollPane(sharedFolders);
		Dimension d = sharedFolders.getPreferredSize();
		pane.setPreferredSize(new Dimension(d.width, sharedFolders.getRowHeight() * 2));
		builderFolder.add(pane, FormLayoutUtil.flip(
			cc.xyw(1, 5, 7, CellConstraints.DEFAULT, CellConstraints.FILL),
			colSpec,
			orientation
		));

		return builderFolder;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateSharedFolders() {
		List<Path> folders = configuration.getSharedFolders();
		Vector<Vector<?>> newDataVector = new Vector<>();
		if (!folders.isEmpty()) {
			List<Path> foldersMonitored = configuration.getMonitoredFolders();
			for (Path folder : folders) {
				Vector rowVector = new Vector();
				rowVector.add(folder.toString());
				rowVector.add(Boolean.valueOf(foldersMonitored.contains(folder)));
				newDataVector.add(rowVector);
			}
		}
		folderTableModel.setDataVector(newDataVector, FOLDERS_COLUMN_NAMES);
		TableColumn column = sharedFolders.getColumnModel().getColumn(0);
		column.setMinWidth(600);
	}

	@Nullable
	private static Icon getIcon(@Nullable String baseName, @Nullable String suffix) {
		if (isBlank(baseName)) {
			return null;
		}
		ImageIcon icon = isBlank(suffix) ?
			LooksFrame.readImageIcon(baseName) :
			LooksFrame.readImageIcon(FileUtil.appendToFileName(baseName, suffix));

		return icon == null ? UIManager.getIcon("OptionPane.warningIcon") : icon;
	}

	private void toggleCustomizeFolders(Component parentComponent) {
		boolean defaultFolders = !configuration.isDefaultSharedFolders();
		if (
			!defaultFolders &&
			configuration.isSharedFoldersEmpty() &&
			!BasicSystemUtils.INSTANCE.getDefaultFolders().isEmpty()
		) {
			if (
				JOptionPane.showConfirmDialog(
					parentComponent,
					Messages.getString("SharedFolders.CopyDefault"),
					null,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE
				) == JOptionPane.YES_OPTION
			) {
				configuration.setSharedFoldersToDefault();
			}
		}
		configuration.setDefaultSharedFolders(defaultFolders);
		updateCustomizeButton(!defaultFolders);
		addButton.setEnabled(!defaultFolders);
		removeButton.setEnabled(!defaultFolders);
		arrowDownButton.setEnabled(!defaultFolders);
		arrowUpButton.setEnabled(!defaultFolders);
		sharedFolders.setEnabled(!defaultFolders);
		updateSharedFolders();
	}

	private void updateCustomizeButton(boolean customize) {
		String baseName = customize ? "button-default.png" : "button-modify.png";
		customizeButton.setIcon(getIcon(baseName, null));
		customizeButton.setPressedIcon(getIcon(baseName, "_pressed"));
		customizeButton.setDisabledIcon(getIcon(baseName, "_disabled"));
		customizeButton.setRolloverIcon(getIcon(baseName, "_mouseover"));
		customizeButton.setToolTipText(customize ?
			Messages.getString("SharedFolders.DefaultFoldersToolTip") :
			Messages.getString("SharedFolders.ModifyFoldersToolTip")
		);
	}

	public void setScanLibraryEnabled(boolean enabled) {
		scanBusyIcon.stop();
		scanBusyDisabledIcon.stop();
		tabListenerRegistrar.unregister(scanBusyIcon);
		tabListenerRegistrar.unregister(scanBusyDisabledIcon);
		while (scanBusyIcon.isSuspended()) {
			scanBusyIcon.unsuspend();
		}
		while (scanBusyDisabledIcon.isSuspended()) {
			scanBusyDisabledIcon.unsuspend();
		}
		scanButton.setEnabled(enabled);
		scanButton.setIcon(scanNormalIcon);
		scanButton.setRolloverIcon(scanRolloverIcon);
		scanButton.setPressedIcon(scanPressedIcon);
		scanButton.setDisabledIcon(scanDisabledIcon);
		scanButton.setToolTipText(Messages.getString("FoldTab.2"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public class SharedFoldersTableModel extends DefaultTableModel {
		private static final long serialVersionUID = -4247839506937958655L;

		public SharedFoldersTableModel() {
			super(FOLDERS_COLUMN_NAMES, 0);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == 1 ? Boolean.class : String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 1;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			Vector rowVector = (Vector) dataVector.elementAt(row);
			if (aValue instanceof Boolean && column == 1) {
				rowVector.setElementAt(aValue, 1);
			} else {
				rowVector.setElementAt(aValue, column);
			}
			fireTableCellUpdated(row, column);
			configuration.setSharedFolders(folderTableModel.getDataVector());
		}

		@Override
		public void insertRow(int row, Vector rowData) {
			super.insertRow(row, rowData);
			configuration.setSharedFolders(folderTableModel.getDataVector());
		}

		@Override
		public void removeRow(int row) {
			super.removeRow(row);
			configuration.setSharedFolders(folderTableModel.getDataVector());
		}
	}

	/**
	 * Creates a new {@link AnimatedIconListenerRegistrar} that registers tab
	 * change to and from {@link LooksFrameTab#NAVIGATION_TAB} and application
	 * minimize events. Suitable for {@link AnimatedIcon}s that's visible
	 * whenever this tab is visible.
	 *
	 * @author Nadahar
	 */
	public static class NavigationTabListenerRegistrar extends AbstractTabListenerRegistrar {

		private NavigationTabListenerRegistrar(@Nonnull LooksFrame looksFrame) {
			super(looksFrame);
		}

		@Override
		protected LooksFrameTab getVisibleTab() {
			return LooksFrameTab.NAVIGATION_TAB;
		}
	}
}
