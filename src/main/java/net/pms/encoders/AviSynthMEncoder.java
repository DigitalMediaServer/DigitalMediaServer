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

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.MediaType;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleType;
import net.pms.newgui.GuiUtil;
import net.pms.newgui.components.CustomJSpinner;
import net.pms.newgui.components.SpinnerIntModel;
import net.pms.util.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AviSynthMEncoder extends MEncoderVideo {
	private static final Logger LOGGER = LoggerFactory.getLogger(AviSynthMEncoder.class);
	public static final PlayerId ID = StandardPlayerId.AVI_SYNTH_MENCODER;
	public static final String NAME = "AviSynth/MEncoder";

	// Not to be instantiated by anything but PlayerFactory
	AviSynthMEncoder() {
	}

	private JTextArea textArea;
	private JCheckBox convertfps;
	private JCheckBox interframe;
	private static JCheckBox interframegpu;

	@Override
	public JComponent getConfigurationPanel() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 12dlu, p, 3dlu, 0:grow"
		);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		JPanel cpuThreadsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		cpuThreadsPanel.add(new JLabel(Messages.getString("Generic.CPUThreads")));

		SpinnerIntModel cpuThreadsModel = new SpinnerIntModel(
			configuration.getMEncoderAviSynthMaxThreads(),
			1,
			Runtime.getRuntime().availableProcessors(),
			1
		);
		cpuThreadsModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setMEncoderAviSynthMaxThreads(((SpinnerIntModel) e.getSource()).getIntValue());
			}
		});
		CustomJSpinner cpuThreads = new CustomJSpinner(cpuThreadsModel, true);
		cpuThreads.setToolTipText(String.format(Messages.getString("Generic.CPUThreadsToolTip"), NAME));
		cpuThreadsPanel.add(cpuThreads);
		builder.add(cpuThreadsPanel).at(cc.xy(2, 3));

		interframe = new JCheckBox(Messages.getString("AviSynthMEncoder.13"), configuration.getAvisynthInterFrame());
		interframe.setContentAreaFilled(false);
		interframe.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setAvisynthInterFrame(interframe.isSelected());
				if (configuration.getAvisynthInterFrame()) {
					JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
						Messages.getString("AviSynthMEncoder.16"),
						Messages.getString("Dialog.Information"),
						JOptionPane.INFORMATION_MESSAGE
					);
				}
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(interframe)).at(cc.xy(2, 5));

		interframegpu = new JCheckBox(Messages.getString("AviSynthMEncoder.15"), configuration.isMEncoderAviSynthInterFrameGPU());
		interframegpu.setContentAreaFilled(false);
		interframegpu.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setMEncoderAviSynthInterFrameGPU((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(interframegpu)).at(cc.xy(2, 7));

		convertfps = new JCheckBox(Messages.getString("AviSynthMEncoder.3"), configuration.getAvisynthConvertFps());
		convertfps.setContentAreaFilled(false);
		convertfps.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAvisynthConvertFps((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(convertfps)).at(cc.xy(2, 9));

		String aviSynthScriptInstructions = Messages.getString("AviSynthMEncoder.4") +
			Messages.getString("AviSynthMEncoder.5") +
			Messages.getString("AviSynthMEncoder.6") +
			Messages.getString("AviSynthMEncoder.7") +
			Messages.getString("AviSynthMEncoder.8");
		JTextArea aviSynthScriptInstructionsContainer = new JTextArea(aviSynthScriptInstructions);
		aviSynthScriptInstructionsContainer.setEditable(false);
		aviSynthScriptInstructionsContainer.setBorder(BorderFactory.createEtchedBorder());
		aviSynthScriptInstructionsContainer.setBackground(new Color(255, 255, 192));
		aviSynthScriptInstructionsContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(130, 135, 144)), BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		builder.add(aviSynthScriptInstructionsContainer).at(cc.xy(2, 11));

		String clip = configuration.getAvisynthScript();
		if (clip == null) {
			clip = "";
		}
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(clip, PMS.AVS_SEPARATOR);
		int i = 0;
		while (st.hasMoreTokens()) {
			if (i > 0) {
				sb.append("\n");
			}
			sb.append(st.nextToken());
			i++;
		}
		textArea = new JTextArea(sb.toString());
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				StringBuilder sb = new StringBuilder();
				StringTokenizer st = new StringTokenizer(textArea.getText(), "\n");
				int i = 0;
				while (st.hasMoreTokens()) {
					if (i > 0) {
						sb.append(PMS.AVS_SEPARATOR);
					}
					sb.append(st.nextToken());
					i++;
				}
				configuration.setAvisynthScript(sb.toString());
			}
		});

		JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension(500, 350));
		builder.add(pane).at(cc.xy(2, 13));

		return builder.getPanel();
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
	}

	@Override
	public PlayerId id() {
		return ID;
	}

	@Override
	public boolean avisynth() {
		return true;
	}

	@Override
	public String name() {
		return NAME;
	}

	/*
	 * Generate the AviSynth script based on the user's settings
	 */
	public static File getAVSScript(String fileName, DLNAMediaSubtitle subTrack, int fromFrame, int toFrame, String frameRateRatio, String frameRateNumber, PmsConfiguration configuration) throws IOException {
		String onlyFileName = fileName.substring(1 + fileName.lastIndexOf('\\'));
		File file = new File(configuration.getTempFolder(), "dms-avs-" + onlyFileName + ".avs");
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
			String numerator;
			String denominator;

			if (frameRateRatio != null && frameRateNumber != null) {
				if (frameRateRatio.equals(frameRateNumber)) {
					// No ratio was available
					numerator = frameRateRatio;
					denominator = "1";
				} else {
					String[] frameRateNumDen = frameRateRatio.split("/");
					numerator = frameRateNumDen[0];
					denominator = "1001";
				}
			} else {
				// No framerate was given so we should try the most common one
				numerator = "24000";
				denominator = "1001";
				frameRateNumber = "23.976";
			}

			String assumeFPS = ".AssumeFPS(" + numerator + "," + denominator + ")";

			String directShowFPS = "";
			if (!"0".equals(frameRateNumber)) {
				directShowFPS = ", fps=" + frameRateNumber;
			}

			String convertfps = "";
			if (configuration.getAvisynthConvertFps()) {
				convertfps = ", convertfps=true";
			}

			File f = new File(fileName);
			if (f.exists()) {
				fileName = ProcessUtil.getShortFileNameIfWideChars(fileName);
			}

			String movieLine       = "DirectShowSource(\"" + fileName + "\"" + directShowFPS + convertfps + ")" + assumeFPS;
			String mtLine1         = "";
			String mtLine2         = "";
			String mtLine3         = "";
			String interframeLines = null;
			String interframePath  = configuration.getInterFramePath();

			int cores = configuration.getMEncoderAviSynthEffectiveMaxThreads();
			if (cores > 1) {
				// Goes at the start of the file to initiate multithreading
				mtLine1 = "SetMemoryMax(512)\nSetMTMode(3," + cores + ")\n";

				// Goes after the input line to make multithreading more efficient
				mtLine2 = "SetMTMode(2)";

				// Goes at the end of the file to allow the multithreading to work with MEncoder
				mtLine3 = "SetMTMode(1)\nGetMTMode(false) > 0 ? distributor() : last";
			}

			// True Motion
			if (configuration.getAvisynthInterFrame()) {
				String GPU = "";
				movieLine += ".ConvertToYV12()";

				// Enable GPU to assist with CPU
				if (configuration.isMEncoderAviSynthInterFrameGPU()){
					GPU = ", GPU=true";
				}

				interframeLines = "\n" +
					"PluginPath = \"" + interframePath + "\"\n" +
					"LoadPlugin(PluginPath+\"svpflow1.dll\")\n" +
					"LoadPlugin(PluginPath+\"svpflow2.dll\")\n" +
					"Import(PluginPath+\"InterFrame2.avsi\")\n" +
					"InterFrame(Cores=" + cores + GPU + ", Preset=\"Faster\")\n";
			}

			String subLine = null;
			if (subTrack != null && configuration.isAutoloadExternalSubtitles() && !configuration.isDisableSubtitles()) {
				if (subTrack.getExternalFile() != null) {
					LOGGER.info("AviSynth script: Using subtitle track: " + subTrack);
					String function = "TextSub";
					if (subTrack.getType() == SubtitleType.VOBSUB) {
						function = "VobSub";
					}
					subLine = function + "(\"" + ProcessUtil.getShortFileNameIfWideChars(subTrack.getExternalFile().getAbsolutePath()) + "\")";
				}
			}

			ArrayList<String> lines = new ArrayList<>();

			lines.add(mtLine1);

			boolean fullyManaged = false;
			String script = configuration.getAvisynthScript();
			StringTokenizer st = new StringTokenizer(script, PMS.AVS_SEPARATOR);
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (line.contains("<movie") || line.contains("<sub")) {
					fullyManaged = true;
				}
				lines.add(line);
			}

			lines.add(mtLine2);

			if (configuration.getAvisynthInterFrame()) {
				lines.add(interframeLines);
			}

			lines.add(mtLine3);

			if (fullyManaged) {
				for (String s : lines) {
					if (s.contains("<moviefilename>")) {
						s = s.replace("<moviefilename>", fileName);
					}

					s = s.replace("<movie>", movieLine);
					s = s.replace("<sub>", subLine != null ? subLine : "#");
					pw.println(s);
				}
			} else {
				pw.println(movieLine);
				if (subLine != null) {
					pw.println(subLine);
				}
				pw.println("clip");

			}
		}
		file.deleteOnExit();
		return file;
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		Format format = resource.getFormat();

		if (format != null) {
			if (format.getIdentifier() == Format.Identifier.WEB) {
				return false;
			}
		}

		DLNAMediaSubtitle subtitle = resource.getMediaSubtitle();

		// Check whether the subtitle actually has a language defined,
		// Uninitialized DLNAMediaSubtitle objects have a null language.
		if (subtitle != null && subtitle.getLang() != null) {
			// This engine only supports external subtitles
			if (subtitle.getExternalFile() != null) {
				return true;
			}

			return false;
		}

		try {
			String audioTrackName = resource.getMediaAudio().toString();
			String defaultAudioTrackName = resource.getMedia().getAudioTracksList().get(0).toString();

			if (!audioTrackName.equals(defaultAudioTrackName)) {
				// This engine only supports playback of the default audio track
				return false;
			}
		} catch (NullPointerException e) {
			LOGGER.trace("AviSynth/MEncoder cannot determine compatibility based on audio track for " + resource.getSystemName());
		} catch (IndexOutOfBoundsException e) {
			LOGGER.trace("AviSynth/MEncoder cannot determine compatibility based on default audio track for " + resource.getSystemName());
		}

		return resource.matches(
			MediaType.VIDEO,
			FormatConfiguration.MKV,
			FormatConfiguration.MP4,
			FormatConfiguration.MPEG1,
			FormatConfiguration.MPEG2,
			FormatConfiguration.MPEG4ASP,
			FormatConfiguration.MPEG4SP,
			FormatConfiguration.MPEGPS,
			FormatConfiguration.MPEGTS,
			FormatConfiguration.OGG
		);
	}
}
