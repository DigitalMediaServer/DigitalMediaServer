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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleType;
import net.pms.newgui.GuiUtil;
import net.pms.newgui.components.CustomJSpinner;
import net.pms.newgui.components.SpinnerIntModel;
import net.pms.util.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class handles the Windows-specific AviSynth/FFmpeg player combination.
 */
public class AviSynthFFmpeg extends FFMpegVideo {
	private static final Logger LOGGER = LoggerFactory.getLogger(AviSynthFFmpeg.class);
	public static final PlayerId ID = StandardPlayerId.AVI_SYNTH_FFMPEG;
	public static final String NAME = "AviSynth/FFmpeg";

	// Not to be instantiated by anything but PlayerFactory
	AviSynthFFmpeg() {
	}

	@Override
	public PlayerId id() {
		return ID;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean avisynth() {
		return true;
	}

	public static File getAVSScript(String filename, DLNAMediaSubtitle subTrack) throws IOException {
		return getAVSScript(filename, subTrack, -1, -1, null, null, _configuration);
	}

	/*
	 * Generate the AviSynth script based on the user's settings
	 */
	public static File getAVSScript(String filename, DLNAMediaSubtitle subTrack, int fromFrame, int toFrame, String frameRateRatio, String frameRateNumber, PmsConfiguration configuration) throws IOException {
		String onlyFileName = filename.substring(1 + filename.lastIndexOf('\\'));
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
			if (configuration.getFFmpegAviSynthConvertFps()) {
				convertfps = ", convertfps=true";
			}

			File f = new File(filename);
			if (f.exists()) {
				filename = ProcessUtil.getShortFileNameIfWideChars(filename);
			}

			String movieLine       = "DirectShowSource(\"" + filename + "\"" + directShowFPS + convertfps + ")" + assumeFPS;
			String mtLine1         = "";
			String mtLine2         = "";
			String interframeLines = null;
			String interframePath  = configuration.getInterFramePath();

			int cores = configuration.getFFmpegAviSynthEffectiveMaxThreads();
			if (cores > 1) {
				// Goes at the start of the file to initiate multithreading
				mtLine1 = "SetMemoryMax(512)\nSetMTMode(3," + cores + ")\n";

				// Goes after the input line to make multithreading more efficient
				mtLine2 = "SetMTMode(2)";
			}

			// True Motion
			if (configuration.getFFmpegAviSynthInterFrame()) {
				String GPU = "";
				movieLine += ".ConvertToYV12()";

				// Enable GPU to assist with CPU
				if (configuration.getFFmpegAviSynthInterFrameGPU()){
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
			String script = "<movie>\n<sub>\n";
			StringTokenizer st = new StringTokenizer(script, PMS.AVS_SEPARATOR);
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (line.contains("<movie") || line.contains("<sub")) {
					fullyManaged = true;
				}
				lines.add(line);
			}

			if (configuration.getFFmpegAviSynthInterFrame()) {
				lines.add(mtLine2);
				lines.add(interframeLines);
			}

			if (fullyManaged) {
				for (String s : lines) {
					if (s.contains("<moviefilename>")) {
						s = s.replace("<moviefilename>", filename);
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

	private JCheckBox interframe;
	private static JCheckBox interframegpu;
	private JCheckBox convertfps;

	@Override
	public JComponent getConfigurationPanel() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu");
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		JPanel cpuThreadsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		cpuThreadsPanel.add(new JLabel(Messages.getString("Generic.CPUThreads")));

		SpinnerIntModel cpuThreadsModel = new SpinnerIntModel(
			configuration.getFFmpegAviSynthMaxThreads(),
			1,
			Runtime.getRuntime().availableProcessors(),
			1
		);
		cpuThreadsModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setFFmpegAviSynthMaxThreads(((SpinnerIntModel) e.getSource()).getIntValue());
			}
		});
		CustomJSpinner cpuThreads = new CustomJSpinner(cpuThreadsModel, true);
		cpuThreads.setToolTipText(String.format(Messages.getString("Generic.CPUThreadsToolTip"), NAME));
		cpuThreadsPanel.add(cpuThreads);
		builder.add(cpuThreadsPanel).at(cc.xy(2, 3));

		interframe = new JCheckBox(Messages.getString("AviSynthMEncoder.13"), configuration.getFFmpegAviSynthInterFrame());
		interframe.setContentAreaFilled(false);
		interframe.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setFFmpegAviSynthInterFrame(interframe.isSelected());
				if (configuration.getFFmpegAviSynthInterFrame()) {
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

		interframegpu = new JCheckBox(Messages.getString("AviSynthMEncoder.15"), configuration.getFFmpegAviSynthInterFrameGPU());
		interframegpu.setContentAreaFilled(false);
		interframegpu.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegAviSynthInterFrameGPU((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(interframegpu)).at(cc.xy(2, 7));

		convertfps = new JCheckBox(Messages.getString("AviSynthMEncoder.3"), configuration.getFFmpegAviSynthConvertFps());
		convertfps.setContentAreaFilled(false);
		convertfps.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegAviSynthConvertFps((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(convertfps)).at(cc.xy(2, 9));

		return builder.getPanel();
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
		// uninitialized DLNAMediaSubtitle objects have a null language.
		if (subtitle != null && subtitle.getLang() != null) {
			// The resource needs a subtitle, but this engine implementation does not support subtitles yet
			return false;
		}

		try {
			String audioTrackName = resource.getMediaAudio().toString();
			String defaultAudioTrackName = resource.getMedia().getAudioTracksList().get(0).toString();

			if (!audioTrackName.equals(defaultAudioTrackName)) {
				// This engine implementation only supports playback of the default audio track at this time
				return false;
			}
		} catch (NullPointerException e) {
			LOGGER.trace("AviSynth/FFmpeg cannot determine compatibility based on audio track for " + resource.getSystemName());
		} catch (IndexOutOfBoundsException e) {
			LOGGER.trace("AviSynth/FFmpeg cannot determine compatibility based on default audio track for " + resource.getSystemName());
		}

		return resource.isVideo() && isContainerCompatible(resource);
	}
}
