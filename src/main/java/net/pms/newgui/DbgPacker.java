package net.pms.newgui;

import com.sun.jna.Platform;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalIconFactory;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.io.BasicSystemUtils;
import net.pms.logging.LoggingConfig;
import net.pms.newgui.components.CustomJButton;
import net.pms.util.FilePermissions;
import net.pms.util.FilePermissions.FileFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbgPacker implements ActionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(DbgPacker.class);
	public static final String ZIP_FILE_NAME = "dms_debug.zip";

	private LinkedHashMap<File, JCheckBox> items;
	private Path zippedDebugFile;
	private CustomJButton openZip;

	public JComponent config() {
		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 5, 0, 5);
		c.ipadx = 5;
		c.gridx = 0;
		c.gridy = 0;
		items = new LinkedHashMap<>();
		JCheckBox checkBox;
		boolean exists;
		for (File file : LoggingConfig.getDebugFiles(false)) {
			exists = file.exists();
			checkBox = new JCheckBox(file.getName(), exists);
			checkBox.setEnabled(exists);
			c.weightx = 1.0;
			top.add(checkBox, c);
			CustomJButton open = exists ? new CustomJButton(MetalIconFactory.getTreeLeafIcon()) : new CustomJButton("+");
			open.setActionCommand(file.getAbsolutePath());
			open.setToolTipText((exists ? "" : Messages.getString("DbgPacker.1") + " ") + file.getAbsolutePath());
			open.addActionListener(this);
			c.gridx++;
			c.weightx = 0.0;
			top.add(open, c);
			c.gridx--;
			c.gridy++;
			if (exists) {
				try {
					file = file.getCanonicalFile();
				} catch (IOException e) {
					LOGGER.error("Failed to resolve canonical file for \"{}\"", file);
				}
			}
			items.put(file, checkBox);
		}
		c.weightx = 2.0;
		CustomJButton debugPack = new CustomJButton(Messages.getString("DbgPacker.2"));
		debugPack.setActionCommand("pack");
		debugPack.addActionListener(this);
		top.add(debugPack, c);
		openZip = new CustomJButton(MetalIconFactory.getTreeFolderIcon());
		openZip.setActionCommand("showzip");
		openZip.setToolTipText(Messages.getString("DbgPacker.3"));
		openZip.setEnabled(false);
		openZip.addActionListener(this);
		c.gridx++;
		c.weightx = 0.0;
		top.add(openZip, c);
		return top;
	}

	private static void writeToZip(ZipOutputStream out, File f) throws Exception {
		byte[] buf = new byte[1024];
		int len;
		if (!f.exists()) {
			LOGGER.debug("DbgPack file {} does not exist - ignoring",f.getAbsolutePath());
			return;
		}
		try (FileInputStream in = new FileInputStream(f)) {
			out.putNextEntry(new ZipEntry(f.getName()));
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
		}
	}

	private boolean saveDialog() {
		JFileChooser fc = new JFileChooser() {
			private static final long serialVersionUID = -7279491708128801610L;

			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if (!f.isDirectory()) {
					if (f.exists() && JOptionPane.showConfirmDialog(null, Messages.getString("DbgPacker.4"), "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return;
					}
					super.approveSelection();
				}
			}
		};
		fc.setFileFilter(
			new FileFilter() {
				@Override
				public boolean accept(File f) {
					String s = f.getName();
					return f.isDirectory() || (s.endsWith(".zip") || s.endsWith(".ZIP"));
				}

				@Override
				public String getDescription() {
					return "*.zip";
				}
			});
		if (zippedDebugFile == null) {
			resolveTargetPath();
		}

		if (zippedDebugFile != null) {
			fc.setSelectedFile(zippedDebugFile.toFile());
		}
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				zippedDebugFile = file.toPath();
			}
			return true;
		}
		return false;
	}

	private boolean validFolder(@Nullable Path folder) {
		try {
			return folder != null && new FilePermissions(folder).hasFlags(FileFlag.FOLDER, FileFlag.BROWSE, FileFlag.WRITE);
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	private void resolveTargetPath() {
		Path path = BasicSystemUtils.INSTANCE.getDesktopFolder();
		if (!validFolder(path)) {
			Map<String, String> logFilePaths = LoggingConfig.getLogFilePaths();
			if (logFilePaths != null && !logFilePaths.isEmpty()) {
				String s = logFilePaths.get("default.log");
				if (isNotBlank(s)) {
					try {
						path = Paths.get(s);
						if (path != null) {
							path = path.getParent();
							if (!validFolder(path)) {
								path = null;
							}
						}
					} catch (InvalidPathException e) {
						path = null;
					}
				}
			}
		}
		if (path == null) {
			LOGGER.warn("Could not resolve suggested destination folder for packed debug files");
		} else {
			zippedDebugFile = path.resolve(ZIP_FILE_NAME);
		}
	}

	private void packDbg() {
		if (!saveDialog()) {
			return;
		}
		try {
			try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zippedDebugFile))) {
				for (Map.Entry<File, JCheckBox> item : items.entrySet()) {
					if (item.getValue().isSelected()) {
						File file = item.getKey();
						LOGGER.debug("Packing {}", file.getAbsolutePath());
						writeToZip(zos, file);
					}
				}
			}
			openZip.setEnabled(true);
		} catch (Exception e) {
			LOGGER.error("Error writing debug ZIP file: {}", e.getLocalizedMessage());
			LOGGER.trace("", e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("pack")) {
			packDbg();
		} else {
			// Open: "showzip" - zipped file folder
			//   not "showzip" - one of the listed files
			File file = str.equals("showzip") ? zippedDebugFile == null ? new File("") : zippedDebugFile.toFile().getParentFile() : new File(str);
			if (file.exists()) {
				try {
					java.awt.Desktop.getDesktop().open(file);
				} catch (IOException e2) {
					LOGGER.warn("Failed to open default desktop application: {}", e2);
					if (Platform.isWindows()) {
						JOptionPane.showMessageDialog(null, Messages.getString("DbgPacker.5") + e2, Messages.getString("TracesTab.6"),JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, Messages.getString("DbgPacker.6") + e2, Messages.getString("TracesTab.6"), JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(
					null, String.format(Messages.getString("DbgPacker.7"), file.getAbsolutePath()), null, JOptionPane.INFORMATION_MESSAGE);
				reload((JComponent) e.getSource());
			}
		}
	}

	private void reload(JComponent c) {
		// Rebuild and restart
		LOGGER.debug("Reloading...");
		((Window) c.getTopLevelAncestor()).dispose();
		JOptionPane.showOptionDialog(
			SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
			config(),
			Messages.getString("Dialog.Options"),
			JOptionPane.CLOSED_OPTION,
			JOptionPane.PLAIN_MESSAGE,
			null,
			null,
			null
		);
	}
}
