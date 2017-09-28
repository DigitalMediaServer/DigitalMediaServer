package net.pms.dlna;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.util.ISO639;
import net.pms.util.Language;
import net.pms.util.OpenSubtitles;
import net.pms.util.OpenSubtitles.SubtitleItem;
import net.pms.util.UMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubSelFile extends VirtualFolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubSelFile.class);
	private final DLNAResource originalResource;

	public SubSelFile(DLNAResource resource) {
		super(resource.getDisplayNameBase(resource.configuration), null);
		originalResource = resource;
	}

	@Override
	public DLNAThumbnailInputStream getThumbnailInputStream() throws IOException {
		try {
			return originalResource.getThumbnailInputStream();
		} catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}

	@Override
	public void checkThumbnail() {
		originalResource.checkThumbnail();
	}

	@Override
	public void discoverChildren() {
		try {
			ArrayList<SubtitleItem> subtitleItems = OpenSubtitles.findSubtitles(originalResource, getDefaultRenderer());
			if (subtitleItems == null || subtitleItems.isEmpty()) {
				return;
			}
			Collections.sort(subtitleItems, new SubSort(getDefaultRenderer()));
			reduceSubtitles(subtitleItems, configuration.getLiveSubtitlesLimit());
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(
					"Discovery of OpenSubtitles subtitles for \"{}\" resulted in the following after sorting and reduction:\n{}",
					getName(),
					OpenSubtitles.toLogString(subtitleItems, 2)
				);
			}
			for (SubtitleItem subtitleItem : subtitleItems) {
				LOGGER.debug("Adding live subtitles child \"{}\" for {}", subtitleItem.getSubFileName(), originalResource);
				DLNAMediaOpenSubtitle subtitle = new DLNAMediaOpenSubtitle(subtitleItem);
				DLNAResource liveResource = originalResource.clone();
				if (liveResource.getMedia() != null) {
					liveResource.getMedia().getSubtitleTracksList().clear();
					liveResource.getMedia().getSubtitleTracksList().add(subtitle);
				}
				liveResource.setMediaSubtitle(subtitle);
				liveResource.resetSubtitlesStatus();
				addChild(liveResource);
			}
		} catch (Exception e) {
			LOGGER.error(
				"An unhandled error occurred during OpenSubtitles subtitles lookup for \"{}\": {}",
				getName(),
				e.getMessage()
			);
			LOGGER.trace("", e);
		}
	}

	private static int removeLanguage(ArrayList<SubtitleItem> subtitles, LinkedHashMap<ISO639, Integer> languages, ISO639 language, int count) {
		ListIterator<SubtitleItem> iterator = subtitles.listIterator(subtitles.size());
		int removed = 0;
		while (removed != count && iterator.hasPrevious()) {
			SubtitleItem subtitle = iterator.previous();
			if (
				(
					language == null &&
					subtitle.getLanguage() == null
				) ||
				(
					language != null &&
					language.equals(subtitle.getLanguage())
				)
			) {
				Integer langCount = languages.get(language);
				if (langCount != null) {
					if (langCount.intValue() == 1) {
						languages.remove(language);
					} else {
						languages.put(language, langCount.intValue() - 1);
					}
				}
				iterator.remove();
				removed++;
			}
		}
		return removed;
	}

	private static void reduceSubtitles(ArrayList<SubtitleItem> subtitles, int limit) { //TODO: (Nad) Keep hash results
		int remove = subtitles.size() - limit;
		if (remove <= 0) {
			return;
		}

		LinkedHashMap<ISO639, Integer> languages = new LinkedHashMap<>();
		for (SubtitleItem subtitle : subtitles) {
			ISO639 language = subtitle.getLanguage();
			if (!languages.containsKey(language)) {
				languages.put(language, 1);
			} else {
				languages.put(language, languages.get(language).intValue() + 1);
			}
		}

		// Remove those without a specified language first
		if (languages.containsKey(null)) {
			remove -= removeLanguage(subtitles, languages, null, remove);
		}

		if (remove > 0) {
			// Build a sorted map where each language gets 30 extra points per step in the priority
			ArrayList<LanguageRankedItem> languageRankedSubtitles = new ArrayList<>();
			int languagePreference = 0;
			ISO639 language = null;
			for (SubtitleItem subtitle : subtitles) {
				if (language == null) {
					language = subtitle.getLanguage();
					languagePreference = languages.size() * 30;
				} else if (!language.equals(subtitle.getLanguage())) {
					language = subtitle.getLanguage();
					languagePreference -= 30;
				}
				languageRankedSubtitles.add(new LanguageRankedItem(
					subtitle.getScore() + languagePreference,
					subtitle
				));
			}
			Collections.sort(languageRankedSubtitles);

			// Remove the entries with the lowest score
			for (LanguageRankedItem languageRankedItem : languageRankedSubtitles) {
				if (remove <= 0) {
					break;
				}
				if (subtitles.remove(languageRankedItem.subtitleItem)) {
					remove--;
				}
			}
		}
	}

	private static class SubSort implements Comparator<SubtitleItem>, Serializable {
		private static final long serialVersionUID = 1L;
		private final List<Language> configuredLanguages;

		SubSort(RendererConfiguration renderer) {
			configuredLanguages = Collections.unmodifiableList(UMSUtils.getLangList(renderer));
		}

		public List<Language> getConfiguredLanguages() {
			return configuredLanguages;
		}

		@Override
		public int compare(SubtitleItem o1, SubtitleItem o2) {
			if (o1 == null) {
				return o2 == null ? 0 : 1;
			}
			if (o2 == null) {
				return -1;
			}

			// Compare languages with configured priority
			if (o1.getLanguage() == null) {
				if (o2.getLanguage() != null) {
					return 1;
				}
			} else if (o2.getLanguage() == null) {
				return -1;
			} else {
				int index1 = configuredLanguages.indexOf(o1.getLanguage());
				int index2 = configuredLanguages.indexOf(o2.getLanguage());
				if (index1 < 0) {
					if (index2 >= 0) {
						return 1;
					}
				} else if (index2 < 0) {
					return -1;
				} else if (index1 != index2) {
					return index1 - index2;
				}
			}
			// Compare score
			if (Double.isNaN(o1.getScore()) || Double.compare(o1.getScore(), 0.0) < 0) {
				if (!Double.isNaN(o2.getScore()) && Double.compare(o2.getScore(), 0.0) >= 0) {
					return 1;
				}
			} else if (Double.isNaN(o2.getScore()) || Double.compare(o2.getScore(), 0.0) < 0) {
				return -1;
			} else {
				int scoreDiff = (int) (o2.getScore() - o1.getScore());
				if (Math.abs(scoreDiff) >= 1) {
					// Only use score if the difference is 1 or more
					return scoreDiff;
				}
			}
			// If scores are equal, use subtitle metadata
			if (o1.getSubBad() == null) {
				if (o2.getSubBad() != null) {
					return 1;
				}
			} else if (o2.getSubBad() == null) {
				return -1;
			} else if (o1.getSubBad().booleanValue()) {
				if (!o2.getSubBad().booleanValue()) {
					return 1;
				}
			} else if (o2.getSubBad().booleanValue()) {
				return -1;
			}
			if (o1.getSubFromTrusted() == null) {
				if (o2.getSubFromTrusted() != null) {
					return 1;
				}
			} else if (o2.getSubFromTrusted() == null) {
				return -1;
			} else if (o1.getSubFromTrusted().booleanValue()) {
				if (!o2.getSubFromTrusted().booleanValue()) {
					return -1;
				}
			} else if (o2.getSubFromTrusted().booleanValue()) {
				return 1;
			}
			// Compare subtitle rating
			double o1Value = Double.isNaN(o1.getSubRating()) ? 0.0 : o1.getSubRating();
			double o2Value = Double.isNaN(o2.getSubRating()) ? 0.0 : o2.getSubRating();
			int result = Double.compare(o1Value, o2Value);
			if (result != 0) {
				return result;
			}
			// Compare OpenSubtitles rating
			o1Value = Double.isNaN(o1.getOpenSubtitlesScore()) ? 0.0 : o1.getOpenSubtitlesScore();
			o2Value = Double.isNaN(o2.getOpenSubtitlesScore()) ? 0.0 : o2.getOpenSubtitlesScore();
			result = Double.compare(o1Value, o2Value);
			if (result != 0) {
				return result;
			}
			// This will probably never happen, but if everything else is equal use the fractional score value
			return (int) Math.signum(o2.getScore() - o1.getScore());
		}
	}

	@Override
	protected String getDisplayNameSuffix(RendererConfiguration renderer, PmsConfiguration configuration) {
		return "{" + Messages.getString("Subtitles.LiveSubtitles") + "}";
	}

	private static class LanguageRankedItem implements Comparable<LanguageRankedItem> {
		private final Double score;
		private final SubtitleItem subtitleItem;

		public LanguageRankedItem(double score, SubtitleItem subtitleItem) {
			this.score = Double.valueOf(score);
			this.subtitleItem = subtitleItem;
		}

		@Override
		public String toString() {
			return "[score=" + score + ", subtitleItem=" + subtitleItem + "]";
		}

		@Override
		public int compareTo(LanguageRankedItem o) {
			if (score == null || score.isNaN()) {
				if (o.score != null && !o.score.isNaN()) {
					return 1;
				}
			} else if (o.score == null || o.score.isNaN()) {
				return -1;
			} else {
				int result = score.compareTo(o.score);
				if (result != 0) {
					return result;
				}
			}
			if (subtitleItem == null) {
				if (o.subtitleItem != null) {
					return 1;
				}
			} else if (o.subtitleItem == null) {
				return -1;
			}
			return o.hashCode() - hashCode();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((score == null) ? 0 : score.hashCode());
			result = prime * result + ((subtitleItem == null) ? 0 : subtitleItem.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof LanguageRankedItem)) {
				return false;
			}
			LanguageRankedItem other = (LanguageRankedItem) obj;
			if (score == null) {
				if (other.score != null) {
					return false;
				}
			} else if (!score.equals(other.score)) {
				return false;
			}
			if (subtitleItem == null) {
				if (other.subtitleItem != null) {
					return false;
				}
			} else if (!subtitleItem.equals(other.subtitleItem)) {
				return false;
			}
			return true;
		}
	}
}
