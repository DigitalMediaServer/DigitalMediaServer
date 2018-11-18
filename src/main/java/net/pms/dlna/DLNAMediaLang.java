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

import javax.annotation.Nullable;
import net.pms.util.ISO639;

/**
 * This class keeps track of the language information for subtitles or audio.
 */
public class DLNAMediaLang {

	/**
	 * A special ID value that indicates that the instance is just a placeholder
	 * that shouldn't be used.
	 */
	public static final int DUMMY_ID = Integer.MIN_VALUE;

	private int id;

	/** The {@link ISO639} */
	@Nullable
	protected ISO639 lang;

	/**
	 * Returns the unique ID for this language object.
	 *
	 * @return The ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets a unique ID for this language object.
	 *
	 * @param id the ID to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return The {@link ISO639} for this language object.
	 */
	@Nullable
	public ISO639 getLang() {
		return lang;
	}

	/**
	 * Sets the {@link ISO639} this language object.
	 *
	 * @param lang The {@link ISO639} to set.
	 */
	public void setLang(@Nullable ISO639 lang) {
		this.lang = lang;
	}
}
