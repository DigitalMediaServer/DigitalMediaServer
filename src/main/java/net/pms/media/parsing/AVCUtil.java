/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2016 Digital Media Server developers.
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
package net.pms.media.parsing;

import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.InputFile;
import net.pms.encoders.PlayerFactory;
import net.pms.encoders.StandardPlayerId;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.media.H264Level;
import net.pms.media.VideoLevel;


/**
 * This is a utility class for various AVC related operations.
 */
public class AVCUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(AVCUtil.class);

	/**
	 * Not to be instantiated.
	 */
	private AVCUtil() {
	}

	/**
	 * Checks if a video is within the PS3 reference frame count limit by either
	 * using parsed information from the specified {@link DLNAMediaInfo}
	 * instance or by parsing the specified {@link InputFile}.
	 * <p>
	 * <b>Note:</b> This method is not tested and has some questionable holes in
	 * its logic. It cannot handle all file formats, and if the profile level
	 * and reference frames is already provided in the {@link DLNAMediaInfo}
	 * instance, this information will be used even though it might have been
	 * parsed only from a file header instead of from an actual check of the
	 * H.264 stream.
	 * <p>
	 * The main purpose of this method is to preserve logic that has been
	 * removed from {@link DLNAMediaInfo} and which might be useful to build on
	 * in the future.
	 *
	 * @param media the {@link DLNAMediaInfo}.
	 * @param file the {@link InputFile} or {@code null}.
	 * @return {@code true} if the video is within the limits, {@code false} if
	 *         it's not or it can't be determined.
	 */
	public static boolean isWithinPS3Limits(@Nonnull DLNAMediaInfo media, @Nullable InputFile file) {
		VideoLevel level = H264Level.typeOf(media.getAvcAsInt());
		int referenceFrames = media.getReferenceFrameCount();

		if ((
				level == null ||
				referenceFrames < 1
			) && (
				FormatConfiguration.MKV.equals(media.getContainer()) ||
				FormatConfiguration.MOV.equals(media.getContainer()) ||
				FormatConfiguration.MP4.equals(media.getContainer())
			)
		) {
			byte[][] headers = getAnnexBFrameHeader(file);
			if (headers != null && headers[1].length > 0) {
				AVCHeader avcHeader = new AVCHeader(cleanAVCHeader(headers[1]));
				avcHeader.parse();
				if (level == null) {
					level = H264Level.typeOf(avcHeader.getLevel());
				}
				if (referenceFrames < 1) {
					referenceFrames = avcHeader.getRef_frames();
				}
			}
		}

		if (H264Level.L4.isGreaterThanOrEqualTo(level)) {
			return true;
		}

		if (level == null || referenceFrames < 1) {
			LOGGER.warn("Failed to determine PS3 compatibility because of missing media information");
			LOGGER.debug("Level: {}, Reference Frame Count: {}", level, referenceFrames);
			return false;
		}

		/*
		 * 2013-01-25: Confirmed maximum reference frames on PS3:
		 *    - 4 for 1920x1080
		 *    - 11 for 1280x720
		 */
		int maxref = (int) Math.floor(10252743 / (double) (media.getWidth() * media.getHeight()));
		if (referenceFrames > maxref) {
			if (file != null) {
				LOGGER.debug(
					"The file \"{}\" isn't compatible with this the PS3 because it can only handle {} " +
					"reference frames at this resolution while this file has {} reference frames",
					file.getFilename(),
					maxref,
					referenceFrames
				);
			} else {
				LOGGER.debug(
					"PS3 reference frames check failed because it can only handle {} " +
					"reference frames at this resolution while this media has {} reference frames",
					maxref,
					referenceFrames
				);
			}
			return false;
		}
		LOGGER.trace(
			"PS3 reference frames check passed with {} reference frames and a reference frame limit of {}",
			referenceFrames,
			maxref
		);
		return true;
	}

	/**
	 * This method seems to locate the "real" start of the AVC header. It's not
	 * clear why or under which circumstances the header isn't already correct,
	 * but it seems that this must be used on the result of the second array
	 * from {@link #getAnnexBFrameHeader(InputFile)} before calling
	 * {@link AVCHeader#AVCHeader(byte[])}.
	 *
	 * @param header the header to "clean".
	 * @return The "cleaned" header.
	 */
	@Nonnull
	public static byte[] cleanAVCHeader(@Nonnull byte[] header) {
		int skip = header[2] == 1 ? 4 : 5;
		byte[] result = new byte[header.length - skip];
		System.arraycopy(header, skip, result, 0, result.length);
		return result;
	}

	/**
	 * Extracts and returns the H.264 reference frames from some file formats.
	 *
	 * @param file the {@link InputFile} from which to extract the H.264
	 *            reference frames.
	 * @return The reference frames or {@code -1}.
	 */
	public static int getReferenceFrames(@Nonnull InputFile file) {
		byte[][] headers = getAnnexBFrameHeader(file);
		if (headers == null || headers[1].length == 0) {
			return -1;
		}

		AVCHeader avcHeader = new AVCHeader(cleanAVCHeader(headers[1]));
		avcHeader.parse();

		LOGGER.debug(
			"H.264 file \"{}\": Profile: {} / Level: {} / Ref Frames: {}",
			file.getFilename(),
			avcHeader.getProfile(),
			avcHeader.getLevel(),
			avcHeader.getRef_frames()
		);

		return avcHeader.getRef_frames();
	}

	/**
	 * This method uses FFmpeg to create one Annex B Frame from the source file,
	 * and then extracts and returns the frame header.
	 * <p>
	 * This isn't currently in use, but is kept in case it will be useful in the
	 * future. It's not entirely clear which file formats/containers are
	 * supported by this method, but MKV, MP4 and MOV seems to be been where is
	 * has been used in the past.
	 *
	 * @param file the {@link InputFile} to process.
	 * @return The frame header or {@code null}.
	 */
	@Nullable
	public static byte[][] getAnnexBFrameHeader(@Nonnull InputFile file) {
		String[] cmdArray = new String[14];
		cmdArray[0] = PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO);
		if (cmdArray[0] == null) {
			LOGGER.warn("Cannot process Annex B Frame Header if FFmpeg executable is undefined");
			return null;
		}
		cmdArray[1] = "-i";

		if (file.getPush() == null && file.getFilename() != null) {
			cmdArray[2] = file.getFilename();
		} else {
			cmdArray[2] = "-";
		}

		cmdArray[3] = "-vframes";
		cmdArray[4] = "1";
		cmdArray[5] = "-c:v";
		cmdArray[6] = "copy";
		cmdArray[7] = "-f";
		cmdArray[8] = "h264";
		cmdArray[9] = "-bsf";
		cmdArray[10] = "h264_mp4toannexb";
		cmdArray[11] = "-an";
		cmdArray[12] = "-y";
		cmdArray[13] = "pipe:";

		byte[][] returnData = new byte[2][];
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.maxBufferSize = 1;
		params.stdin = file.getPush();

		final ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, true, params);

		final Vector<Boolean> failure = new Vector<>(1);
		failure.add(Boolean.FALSE);

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					failure.set(0, Boolean.TRUE);
				} catch (InterruptedException e) { }
				pw.stopProcess();
			}
		};

		Thread failsafe = new Thread(r, "FFMpeg AnnexB Frame Header Failsafe");
		failsafe.start();
		pw.runInSameThread();

		if (failure.get(0).booleanValue()) {
			return null;
		}

		byte[] data = pw.getOutputByteArray().toByteArray();
		returnData[0] = data;
		int kf = 0;

		for (int i = 3; i < data.length; i++) {
			if (data[i - 3] == 1 && (data[i - 2] & 37) == 37 && (data[i - 1] & -120) == -120) {
				kf = i - 2;
				break;
			}
		}

		int st = 0;
		boolean found = false;

		if (kf > 0) {
			for (int i = kf; i >= 5; i--) {
				if (data[i - 5] == 0 && data[i - 4] == 0 && data[i - 3] == 0 && (data[i - 2] & 1) == 1 && (data[i - 1] & 39) == 39) {
					st = i - 5;
					found = true;
					break;
				}
			}
		}

		if (found) {
			byte[] header = new byte[kf - st];
			System.arraycopy(data, st, header, 0, kf - st);
			returnData[1] = header;
		}

		return returnData;
	}

}
