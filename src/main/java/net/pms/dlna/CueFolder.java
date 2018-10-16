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
package net.pms.dlna;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;
import net.pms.formats.Format;
import net.pms.formats.FormatType;
import net.pms.image.ImageFormat;
import net.pms.image.ImagesUtil.ScaleType;
import net.pms.image.thumbnail.AudioTagInfo;
import net.pms.image.thumbnail.CoverUtil;
import net.pms.util.ConversionUtil;
import net.pms.util.FileUtil;
import org.digitalmediaserver.cuelib.CueParser;
import org.digitalmediaserver.cuelib.CueSheet;
import org.digitalmediaserver.cuelib.FileData;
import org.digitalmediaserver.cuelib.Position;
import org.digitalmediaserver.cuelib.TrackData;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CueFolder extends DLNAResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(CueFolder.class);
	private final File playlistfile;
	private final CueSheet embeddedCueSheet;
	private DLNAThumbnail thumbnail;
	private boolean thumbnailParsed;
	private String album;
	private String artist;
	private int numTracks;

	public File getPlaylistfile() {
		return playlistfile;
	}

	public CueFolder(File file) {
		playlistfile = file;
		embeddedCueSheet = null;
		setLastModified(playlistfile.lastModified());
	}

	public CueFolder(@Nonnull File file, @Nonnull CueSheet embeddedCueSheet) {
		this.playlistfile = file;
		this.embeddedCueSheet = embeddedCueSheet;
		setLastModified(this.playlistfile.lastModified());
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return playlistfile.getName();
	}

	@Override
	public String getSystemName() {
		return playlistfile.getName();
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public synchronized void checkThumbnail() {
		if (thumbnailParsed) {
			return;
		}

		if (FileUtil.isExtension(playlistfile, false, "flac")) {
			AudioFile audioFile;
			try {
				audioFile = AudioFileIO.readAs(playlistfile, "flac");
				Tag tag = audioFile.getTag();
				if (tag != null && !tag.getArtworkList().isEmpty()) {
					thumbnail = DLNABinaryThumbnail.toThumbnail(
						tag.getArtworkList().get(0).getBinaryData(),
						640,
						480,
						ScaleType.MAX,
						ImageFormat.SOURCE,
						false
					);
				}
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
				LOGGER.debug("An error occurred when trying to read metadata from \"{}\": {}", playlistfile, e.getMessage());
				LOGGER.trace("", e);
			}
		}
		if (thumbnail == null) {
			CoverUtil coverUtil = CoverUtil.get();
			if (coverUtil == null) {
				return;
			}
			AudioTagInfo tagInfo = coverUtil.createAudioTagInfo(album, artist, null, -1, -1, numTracks);
			if (tagInfo.hasUsefulInfo()) {
				thumbnail = coverUtil.getThumbnail(tagInfo);
			}
		}
		thumbnailParsed = true;
	}

	@Override
	protected synchronized DLNAThumbnailInputStream getThumbnailInputStream() throws IOException {
		if (thumbnail != null) {
			DLNAThumbnailInputStream inputStream = DLNAThumbnailInputStream.toThumbnailInputStream(thumbnail);
			if (inputStream != null) {
				return inputStream;
			}
		}

		return getGenericThumbnailInputStream(null);
	}

	synchronized DLNAThumbnail getThumbnail() {
		if (!thumbnailParsed) {
			checkThumbnail();
		}
		return thumbnail;
	}

	@Override
	protected synchronized void resolveOnce() {
		long playlistLength = playlistfile.length();
		if (embeddedCueSheet != null || playlistLength < 10000000) {
			LOGGER.trace("Parsing cue sheet \"{}\"", playlistfile);
			CueSheet cueSheet = embeddedCueSheet;
			if (cueSheet == null) {
				try {
					//XXX Hardcoding UTF-8 is less than ideal, but the only other option is to somehow probe the file
					cueSheet = CueParser.parse(playlistfile, StandardCharsets.UTF_8);
				} catch (IOException e) {
					LOGGER.warn("Error parsing cue file \"{}\": {}", playlistfile, e.getMessage());
					LOGGER.trace("", e);
					return;
				}
			}

			if (cueSheet != null) {
				this.album = cueSheet.getTitle();
				this.artist = cueSheet.getPerformer();

				ArrayList<CueSheetEntry> sheetResources = new ArrayList<>();
				for (FileData fileData : cueSheet.getFileData()) {
					ArrayList<CueSheetEntry> fileResources = new ArrayList<>();
					List<TrackData> tracks = fileData.getTrackData();
					Format originalFormat = null;
					DLNAMediaInfo originalMedia = null;
					for (int i = 0; i < tracks.size(); i++) {
						TrackData track = tracks.get(i);
						int trackNo = sheetResources.size() + i + 1;
						Position start = track.getStartIndex().getPosition();
						double startTime = start == null ? 0.0 : getTime(start);
						Position end = (i + 1) < tracks.size() ? tracks.get(i + 1).getFirstIndex().getPosition() : null;
						double endTime = end == null ? Double.POSITIVE_INFINITY : getTime(end);
						File file = embeddedCueSheet == null ?
							new File(playlistfile.getParentFile(), fileData.getFile()) :
							playlistfile;
						String trackTitle = isNotBlank(track.getTitle()) ?
							track.getTitle() :
							Messages.getString("Generic.MusicAlbumTrack") + " #" + trackNo;
						CueSheetEntry cueSheetEntry = new CueSheetEntry(
							file,
							trackTitle,
							startTime,
							endTime
						);
						if (LOGGER.isTraceEnabled()) {
							StringBuilder sb = new StringBuilder("Track #").append(trackNo);
							if (isNotBlank(track.getTitle())) {
								sb.append('"').append(track.getTitle()).append('"');
							}
							if (cueSheetEntry.isPartialSource()) {
								LOGGER.trace(
									"{} split range: {} - {}",
									sb,
									cueSheetEntry.getClipStart(),
									cueSheetEntry.getClipEnd()
								);
							} else {
								LOGGER.trace("{} will be used in its entirety", sb);
							}
						}
						cueSheetEntry.setParent(this);
						fileResources.add(cueSheetEntry);

						if (originalFormat == null) {
							// Parse the format
							cueSheetEntry.resolveFormat();
							originalFormat = cueSheetEntry.getFormat();
							if (originalFormat == null) {
								LOGGER.warn(
									"Couldn't resolve format for track #{} ({}) in cue sheet \"{}\"",
									trackNo,
									cueSheetEntry.getName(),
									playlistfile.getAbsolutePath()
								);
							}
						}

						Format trackFormat;
						if (originalFormat == null) {
							trackFormat = cueSheetEntry.getFormat();
						} else {
							trackFormat = originalFormat;
							cueSheetEntry.setFormat(trackFormat);
						}

						if (originalMedia == null) {
							// Parse the DLNAMediaInfo
							cueSheetEntry.run();
							originalMedia = cueSheetEntry.getMedia();
							if (originalMedia == null) {
								LOGGER.warn(
									"Couldn't resolve media information for track #{} ({}) in cue sheet \"{}\"",
									trackNo,
									cueSheetEntry.getName(),
									playlistfile.getAbsolutePath()
								);
							}
						}

						DLNAMediaInfo trackMedia;
						if (originalMedia == null) {
							trackMedia = cueSheetEntry.getMedia();
						} else {
							try {
								trackMedia = originalMedia.clone();
								cueSheetEntry.setMedia(trackMedia);
							} catch (CloneNotSupportedException e) {
								LOGGER.info(
									"Error cloning DLNAMediaInfo: {} for track #{} in cue sheet \"{}\": {}",
									originalMedia,
									trackNo,
									playlistfile.getAbsolutePath(),
									e.getMessage()
								);
								LOGGER.trace("", e);
								trackMedia = cueSheetEntry.getMedia();
							}
						}

						cueSheetEntry.setSplitTrack(i + 1);

						if (trackMedia != null && trackMedia.getFirstAudioTrack() != null) {
							DLNAMediaAudio audio = trackMedia.getFirstAudioTrack();
							if (trackMedia.isAudio() && audio != null) {
								audio.setSongname(trackTitle);
								audio.setTrack(trackNo);
								if (isNotBlank(cueSheet.getTitle())) {
									audio.setAlbum(cueSheet.getTitle());
								}
								if (isNotBlank(track.getPerformer())) {
									audio.setArtist(track.getPerformer());
								} else if (isNotBlank(cueSheet.getPerformer())) {
									audio.setArtist(cueSheet.getPerformer());
								} else if (isNotBlank(track.getSongwriter())) {
									audio.setArtist(track.getSongwriter());
								} else if (isNotBlank(cueSheet.getSongwriter())) {
									audio.setArtist(cueSheet.getSongwriter());
								}
								if (isNotBlank(cueSheet.getGenre())) {
									audio.setGenre(cueSheet.getGenre());
								}
								if (cueSheet.getYear() > 0) {
									audio.setYear(cueSheet.getYear());
								}
							}
							cueSheetEntry.getMedia().setSize(-1);
						}
					}

					sheetResources.addAll(fileResources);
				}

				this.numTracks = sheetResources.size();

				Player player = null;
				File lastFile = null;
				for (CueSheetEntry cueSheetEntry : sheetResources) {
					if (lastFile == null || !lastFile.equals(cueSheetEntry.getFile())) {
						lastFile = cueSheetEntry.getFile();
						player = null;
					}

					// Force transcoding if splitting is used
					if (cueSheetEntry.getPlayer() == null && cueSheetEntry.isPartialSource()) {
						if (player == null) {
							player = PlayerFactory.getPlayer(cueSheetEntry);
						}
						cueSheetEntry.setPlayer(player);
					}

					addChild(cueSheetEntry);
				}
				PMS.get().storeFileInCache(playlistfile, FormatType.PLAYLIST);
			}
		} else {
			LOGGER.warn(
				"Skipping cue sheet \"{}\" because it's too large ({})",
				playlistfile,
				ConversionUtil.formatBytes(playlistLength, true)
			);
		}
	}

	private static double getTime(Position p) {
		return p.getMinutes() * 60 + p.getSeconds() + ((double) p.getFrames() / 100);
	}
}
