/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com. Copyright
 * (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.drew.lang.annotations.Nullable;

/**
 * This is a utility class for converting {@link String}s to title case.
 */
public class TitleCase {

	/**
	 * Not to be instantiated.
	 */
	private TitleCase() {
	}

	/** The standard list of English words to keep in lower-case */
	public static final List<String> ENGLISH_LOWER = Collections.unmodifiableList(Arrays.asList(
		"a", "an", "and", "as", "at", "but", "by", "en", "for", "if", "in",
		"nor", "of", "on", "or", "per", "pro", "so", "the", "to", "v", "v.",
		"via", "vs", "vs.", "yet"
	));

	/**
	 * A {@link Pattern} that matches a word with "irregular" case, that is: the
	 * word is mixed-case with upper-case letters following lower-case letters
	 */
	public static final Pattern IRREGULAR_CASE = Pattern.compile("[\\p{Lower}]+[\\p{Upper}]+", Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * A {@link Pattern} that matches
	 * "{@code !"#$%&'()*+,-./:;<=>?@[\]^_`|}~}" and their Unicode equivalents.
	 */
	public static final Pattern PUNCT = Pattern.compile("[\\p{Punct}]+", Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * Converts the specified {@link String} to title-case using
	 * {@link #ENGLISH_LOWER}.
	 *
	 * @param s the {@link String} to convert.
	 * @param locale the {@link Locale} to use. If {@code null},
	 *            {@link Locale#getDefault()} is used, which is prone to give
	 *            unintended results.
	 * @return The title-case converted {@link String}.
	 */
	@Nullable
	public static String convert(@Nullable String s, @Nullable Locale locale) {
		return convert(s, false, locale, null);
	}

	/**
	 * Converts the specified {@link String} to title-case.
	 *
	 * @param s the {@link String} to convert.
	 * @param locale the {@link Locale} to use. If {@code null},
	 *            {@link Locale#getDefault()} is used, which is prone to give
	 *            unintended results.
	 * @param keepLower a {@link Collections} of words to keep in lower-case. If
	 *            {@code null}, {@link #ENGLISH_LOWER} will be used.
	 * @return The title-case converted {@link String}.
	 */
	@Nullable
	public static String convert(
		@Nullable String s,
		@Nullable Locale locale,
		@Nullable Collection<? extends CharSequence> keepLower
	) {
		return convert(s, false, locale, keepLower);
	}

	/**
	 * Converts the specified {@link String} to title-case.
	 *
	 * @param s the {@link String} to convert.
	 * @param stripNewLines if {@code true} newlines are replaced with spaces.
	 * @param locale the {@link Locale} to use. If {@code null},
	 *            {@link Locale#getDefault()} is used, which is prone to give
	 *            unintended results.
	 * @param keepLower a {@link Collections} of words to keep in lower-case. If
	 *            {@code null}, {@link #ENGLISH_LOWER} will be used.
	 * @return The title-case converted {@link String}.
	 */
	@Nullable
	public static String convert(
		@Nullable String s,
		boolean stripNewLines,
		@Nullable Locale locale,
		@Nullable Collection<? extends CharSequence> keepLower
	) {
		if (isBlank(s)) {
			return s;
		}
		if (keepLower == null) {
			keepLower = ENGLISH_LOWER;
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}

		ArrayList<String> result = new ArrayList<String>();

		String[] lines;
		if (stripNewLines) {
			lines = new String[] {StringUtil.NEWLINE.matcher(s).replaceAll(" ")};
		} else {
			lines = StringUtil.NEWLINE.split(s);
		}

		for (String line : lines) {
			ArrayList<String> resultLine = new ArrayList<String>();
			String[] words = StringUtil.WHITESPACE.split(line);
			for (String word : words) {
				if (word == null || word.isEmpty()) {
					continue;
				}
				if (word.contains("-")) {
					resultLine.add(titleCaseBy(word, "-", locale, keepLower));
				} else if (word.contains("//") || '/' == word.charAt(0)) {
					resultLine.add(word);
				} else if (word.contains("/")) {
					resultLine.add(titleCaseBy(word, "/", locale, keepLower));
				} else {
					resultLine.add(titleCaseWord(word, false, locale, keepLower));
				}
			}

			if (resultLine.size() > 0 && Character.isLowerCase(resultLine.get(0).codePointAt(0))) {
				resultLine.set(0, titleCaseWord(resultLine.get(0), true, locale, keepLower));
			}
			if (resultLine.size() > 1 && Character.isLowerCase(resultLine.get(resultLine.size() - 1).codePointAt(0))) {
				resultLine.set(resultLine.size() - 1, titleCaseWord(resultLine.get(resultLine.size() - 1), true, locale, keepLower));
			}
			result.add(StringUtils.join(resultLine, " "));
		}
		return StringUtils.join(result, "\n");
	}

	private static String titleCaseBy(String s, String delimiter, Locale locale, Collection<? extends CharSequence> keepLower) {
		if (s == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(s.length());
		String[] parts = s.split(delimiter);
		boolean first = true;
		for (String part : parts) {
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(titleCaseWord(part, false, locale, keepLower));
		}
		return sb.toString();
	}

	private static String titleCaseWord(String word, boolean forceUpper, Locale locale, Collection<? extends CharSequence> keepLower) {
		if (word == null || word.isEmpty()) {
			return word;
		}
		if (word.contains("_") || word.contains("@")) {
			return word;
		}

		if (IRREGULAR_CASE.matcher(word).find()) {
			return word;
		}

		if (PUNCT.matcher(String.valueOf(word.charAt(0))).matches()) {
			if (word.length() > 1) {
				return String.valueOf(word.charAt(0)) + titleCaseWord(word.substring(1), true, locale, keepLower);
			}
			return word;
		}

		if (StringUtil.isUpperCase(word) && word.contains(".")) {
			return word;
		}
		word = word.toLowerCase(locale);

		if (word.length() > 3) {
			char second = word.charAt(1);
			if (second == '`' || second == '\'' || second == '’') {
				return new StringBuilder(word.length())
					.append(String.valueOf(word.charAt(0)).toUpperCase(locale))
					.append(word.charAt(1))
					.append(String.valueOf(word.charAt(2)).toUpperCase(locale))
					.append(word.substring(3))
					.toString();
			}
		}

		if (!forceUpper && keepLower.contains(word)) {
			return word;
		}

		word = String.valueOf(word.charAt(0)).toUpperCase(locale) + word.substring(1);

		return word;
	}
}
