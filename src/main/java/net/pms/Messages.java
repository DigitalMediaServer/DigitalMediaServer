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
package net.pms;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class provides a mechanism to localize the text messages found in UMS.
 * It is based on {@link ResourceBundle}.
 */
public class Messages {
	private static final String BUNDLE_NAME = "resources.i18n.messages";

	private static ReadWriteLock resourceBundleLock = new ReentrantReadWriteLock();
	private static ResourceBundle resourceBundle;
	private static final ResourceBundle ROOT_RESOURCE_BUNDLE;

	static {
		/*
		 * This is called when the first call to any of the static class methods
		 * are done. PMS.setLocale() will call setLocaleBundle() to access the
		 * correct resource bundle, but we need something in the mean time if
		 * this class invoked before PM.setLocale() has been called. This can
		 * happen if any code calls getString() before configuration has been
		 * loaded, in which case the default locale will be used.
		 */
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
		ROOT_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT, new ResourceBundle.Control() {
	        @Override
	        public List<Locale> getCandidateLocales(String name,
	                                                Locale locale) {
	            return Collections.singletonList(Locale.ROOT);
	        }
		});

	}

	private Messages() {
	}

	/**
	 * Creates a resource bundle based on the given {@link Locale} and keeps
	 * this for use by any calls to {@link #getString(String)}. If no matching
	 * {@link ResourceBundle} can be found, one is chosen from a number of
	 * candidates according to <a href=
	 * "https://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html#default_behavior"
	 * > ResourceBundle default behavior</a>.
	 *
	 * @param locale the {@link Locale} from which the {@link ResourceBundle} is
	 *            selected.
	 */
	public static void setLocaleBundle(Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("locale cannot be null");
		}
		resourceBundleLock.writeLock().lock();
		try {
			if (isRootEnglish(locale)) {
				resourceBundle = ROOT_RESOURCE_BUNDLE;
			} else {
				resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
			}
		} finally {
			resourceBundleLock.writeLock().unlock();
		}
	}

	/**
	 * Returns the string associated with {@code key} either localized or not.
	 *
	 * @param key the keys in UMS follow the format "group.x". group states
	 *            where this key is likely to be used. For example, StatusTab
	 *            refers to the status tab in the UMS GUI. "x" can be anything.
	 * @param localized whether the returned {@link String} should be localized
	 *            or not.
	 * @return The {@link String} if {@code key} is found, otherwise a copy of
	 *         {@code key}.
	 */
	public static String getString(String key, boolean localized) {
		return localized ? getString(key) : getRootString(key);
	}

	/**
	 * Returns the locale-specific string associated with {@code key}.
	 *
	 * @param key the keys in UMS follow the format "group.x". group states
	 *            where this key is likely to be used. For example, StatusTab
	 *            refers to the status tab in the UMS GUI. "x" can be anything.
	 * @return The localized {@link String} if {@code key} is found, otherwise a
	 *         copy of {@code key}.
	 */
	public static String getString(String key) {
		resourceBundleLock.readLock().lock();
		try {
			return getString(key, resourceBundle);
		} finally {
			resourceBundleLock.readLock().unlock();
		}
	}

	/**
	 * Returns the string associated with {@code key} from the language file
	 * representing {@code locale}. If an exact match for the {@code locale}
	 * can't be found, a "similar" {@link Locale} will be used. If no "similar"
	 * language can't be found, the default {@link Locale} will be used. The
	 * root {@link Locale} is only chosen as a last resort.
	 * <p>
	 * See <a href=
	 * "https://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html#default_behavior"
	 * > ResourceBundle default behavior</a> for more information about the
	 * selection process.
	 *
	 * @param key the keys in UMS follow the format "group.x". group states
	 *            where this key is likely to be used. For example, StatusTab
	 *            refers to the status tab in the UMS GUI. "x" can be anything.
	 * @param locale the {@link Locale} to use.
	 * @return The localized {@link String} if {@code key} is found, otherwise a
	 *         copy of {@code key}.
	 */
	public static String getString(String key, Locale locale) {
		if (locale == null) {
			return getString(key);
		}

		// Selecting base bundle (en-US) for all English variants but British
		if (isRootEnglish(locale)) {
			return getRootString(key);
		}
		ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_NAME, locale);
		if (rb == null) {
			rb = ROOT_RESOURCE_BUNDLE;
		}
		return getString(key, rb);
	}


	/**
	 * Returns the string associated with {@code key} from the root language
	 * file {@code "messages.properties"}.
	 *
	 * @param key the keys in UMS follow the format "group.x". group states
	 *            where this key is likely to be used. For example, StatusTab
	 *            refers to the status tab in the UMS GUI. "x" can be anything.
	 * @return The non-localized {@link String} if {@code key} is found,
	 *         otherwise a copy of {@code key}.
	 */
	public static String getRootString(String key) {
		return getString(key, ROOT_RESOURCE_BUNDLE);
	}

	private static String getString(String key, ResourceBundle rb) {
		try {
			return rb.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Checks if the given {@link Locale} should use the root language file
	 * {@code "messages.properties"} which is {@code en-US}. It currently
	 * represents all variants of English but British English.
	 *
	 * @param locale the {@link Locale} to check.
	 * @return The result.
	 */
	private static boolean isRootEnglish(Locale locale) {
		return locale.getLanguage().toLowerCase(Locale.ENGLISH).equals("en") && !locale.getCountry().equals("GB");
	}
}
