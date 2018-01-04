/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.Messages;
import net.pms.PMS;


/**
 * This interface represents a language with its English and localized name and
 * one or two codes. The localized name and the 2-letter code are optional.
 *
 * @author Nadahar
 */
public interface Language {

	/** Used to represent wildcard */
	Language ANY = new DefaultLangauge(
		"Any language",
		Messages.getString("Language.any"),
		LanguageType.WILDCARD,
		null,
		"*"
	);

	/** Used to represent that no language is wanted */
	Language OFF = new DefaultLangauge(
		"No language",
		Messages.getString("Language.none"),
		LanguageType.OFF,
		null,
		"off"
	);

	/**
	 * @return The 2-letter code if any.
	 */
	@Nullable
	public String get2LetterCode();

	/**
	 * @return The "main" code, normally a 3-letter code.
	 */
	@Nonnull
	public String getCode();

	/**
	 * @return The localized language name or the English language name if no
	 *         localized name is registered.
	 */
	@Nonnull
	public String getLocalizedNameFallback();

	/**
	 * @return The localized language name or {@code null}.
	 */
	@Nullable
	public String getLocalizedName();

	/**
	 * @return The English language name.
	 */
	@Nonnull
	public String getName();

	/**
	 * Gets the shortest possible code.
	 *
	 * @return The shortest code.
	 */
	@Nonnull
	public String getShortestCode();

	/**
	 * @return The {@link LanguageType}.
	 */
	@Nonnull
	public LanguageType getType();

	/**
	 * The default {@link Language} implementation.
	 */
	public static class DefaultLangauge implements Language {

		/** The {@link LanguageType} */
		@Nonnull
		protected final LanguageType type;

		/** The English language name */
		@Nonnull
		protected final String name;

		/** The localized language name */
		@Nullable
		protected final String localizedName;

		/** The 2-letter language code */
		@Nullable
		protected final String twoLetterCode;

		/** The main (preferably 3-letter) language code */
		@Nonnull
		protected final String code;

		/**
		 * Creates a new instance with the specified values.
		 *
		 * @param name the English language name.
		 * @param localizedName the localized ({@link PMS#getLocale()}
		 *            dependent) language name.
		 * @param type the {@link LanguageType}.
		 * @param twoLetterCode the 2-letter code if any.
		 * @param code the primary (preferably 3-letter) code.
		 */
		public DefaultLangauge(
			@Nonnull String name,
			@Nullable String localizedName,
			@Nonnull LanguageType type,
			@Nullable String twoLetterCode,
			@Nonnull String code
		) {
			this.type = type;
			this.name = name;
			this.localizedName = localizedName;
			this.twoLetterCode = twoLetterCode;
			this.code = code;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getLocalizedName() {
			return localizedName;
		}

		@Override
		public String getLocalizedNameFallback() {
			return isNotBlank(localizedName) ? localizedName : name;
		}

		@Override
		public String get2LetterCode() {
			return twoLetterCode;
		}

		@Override
		public String getCode() {
			return code;
		}

		@Override
		public String getShortestCode() {
			if (isBlank(twoLetterCode)) {
				return code;
			}

			return twoLetterCode.length() < code.length() ? twoLetterCode : code;
		}

		@Override
		public LanguageType getType() {
			return type;
		}

		@Override
		public String toString() {
			return name;
		}

		/**
		 * Parses the specified language name or code to a {@link Language} or
		 * {@code null} if the parsing fails. The value can be a two or tree
		 * letter {@link ISO639} code, an English language name or one of the
		 * special values specified below.
		 * <p>
		 * The parsing will only consider the special values
		 * {@link Language#ANY}, {@link Language#OFF} and any {@link ISO639}
		 * entry as possible matches. Other {@link Language} implementations are
		 * not evaluated.
		 *
		 * @param code the language code to parse.
		 * @return The resulting {@link Language} or {@code null}.
		 */
		@Nullable
		public static Language parseLanguage(@Nullable String code) {
			if (code == null) {
				return null;
			}
			if ("*".equals(code)) {
				return Language.ANY;
			}
			if ("off".equals(code)) {
				return Language.OFF;
			}
			return ISO639.get(code);
		}

		/**
		 * Parses the specified comma separated {@link String} of language names
		 * or codes to an {@link ArrayList} of {@link Language}s. Unparsable
		 * elements are silently dropped from the result. The values can be a
		 * two or tree letter {@link ISO639} codes, English or localized
		 * languages name or the special values specified below.
		 * <p>
		 * The parsing will only consider the special values
		 * {@link Language#ANY}, {@link Language#OFF} and any {@link ISO639}
		 * entry as possible matches. Other {@link Language} implementations are
		 * not evaluated.
		 *
		 * @param codes the comma separated {@link String} of language codes to
		 *            parse.
		 * @return The resulting {@link ArrayList} of {@link Language}s. The
		 *         {@link ArrayList} might be empty.
		 */
		@Nonnull
		public static ArrayList<Language> parseLanguages(@Nullable String codes) {
			ArrayList<Language> result = new ArrayList<>();
			if (isBlank(codes)) {
				return result;
			}
			String[] languages = StringUtil.COMMA.split(codes.trim().toLowerCase(Locale.ROOT));
			for (String languageString : languages) {
				Language language = parseLanguage(languageString);
				if (language != null) {
					result.add(language);
				}
			}
			return result;
		}

		/**
		 * Returns an alphabetically sorted array of {@link ISO639} values
		 * potentially together with the special values {@link Language#ANY} and
		 * {@link Language#OFF}. Use the filter to control which entries are
		 * included.
		 *
		 * @param filter the {@link EnumSet} of {@link LanguageType}s to
		 *            include.
		 * @param sortLocalized if {@code true} the entries are sorted by the
		 *            localized name, if {@code false} they are sorted by their
		 *            first (English) name.
		 * @return The array of values.
		 */
		@Nonnull
		public static Language[] getAll(EnumSet<LanguageType> filter, final boolean sortLocalized) {
			ArrayList<Language> result = new ArrayList<>(500);
			if (filter == null || filter.isEmpty()) {
				filter = EnumSet.allOf(LanguageType.class);
			}

			for (ISO639 iso639 : ISO639.values()) {
				if (filter.contains(iso639.getType())) {
					result.add(iso639);
				}
			}

			Collections.sort(result, new Comparator<Language>() {
				@Override
				public int compare(Language o1, Language o2) {
					if (o1 == null && o2 == null) {
						return 0;
					} else if (o1 == null) {
						return 1;
					} else if (o2 == null) {
						return -1;
					}
					if (sortLocalized) {
						return o1.getLocalizedNameFallback().compareTo(o2.getLocalizedNameFallback());
					}
					return o1.getName().compareTo(o2.getName());
				}
			});


			if (filter.contains(LanguageType.OFF)) {
				result.add(0, Language.OFF);
			}

			if (filter.contains(LanguageType.WILDCARD)) {
				result.add(0, Language.ANY);
			}

			return result.toArray(new Language[result.size()]);
		}

		/**
		 * Returns an alphabetically sorted {@link KeyedComboBoxModel} of
		 * {@link ISO639} and localized language name pairs potentially together
		 * with the special values {@link Language#ANY} and
		 * {@link Language#OFF}. Use the filter to control which entries are
		 * included.
		 *
		 * @param filter the {@link EnumSet} of {@link LanguageType}s to
		 *            include.
		 * @return The populated {@link KeyedComboBoxModel}.
		 */
		@Nonnull
		public static KeyedComboBoxModel<Language, String> getKeyedComboBoxModel(
			EnumSet<LanguageType> filter
		) {
			Language[] languages = getAll(filter, true);
			String[] names = new String[languages.length];
			for (int i = 0; i < languages.length; i++) {
				names[i] = languages[i].getLocalizedNameFallback();
			}
			KeyedComboBoxModel<Language, String> result = new KeyedComboBoxModel<>(languages, names);
			return result;
		}
	}

	/**
	 * This {@code enum} is used to categorize {@link Language} instances.
	 */
	public static enum LanguageType {

		/** Language group entries */
		GROUP,

		/** Historical language entries */
		HISTORICAL,

		/** Non-language entries */
		NON_LANGUAGE,

		/** Normal entries */
		NORMAL,

		/** The special "null" language entry {@link Language#OFF} */
		OFF,

		/** The "undefined" language entry {@link ISO639#UND} */
		UNDEFINED,

		/** Wildcard entries */
		WILDCARD;
	}
}
