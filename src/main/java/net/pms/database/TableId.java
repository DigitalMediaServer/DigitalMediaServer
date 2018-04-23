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
package net.pms.database;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.util.TitleCase;


/**
 * This {@code enum} contains all the {@link Table} IDs.
 *
 * @author Nadahar
 */
public enum TableId {

	/** The ID for {@link TableCoverArtArchive} */
	COVER_ART_ARCHIVE(TableCoverArtArchive.class, null),

	/** The ID for {@link TableMusicBrainzReleases} */
	MUSIC_BRAINZ_RELEASES(TableMusicBrainzReleases.class, null),

	/** The ID for {@link Tables} */
	TABLES(Tables.class, null);

	@Nonnull
	private final String name;

	@Nullable
	private final String messageIdentifier;

	@Nonnull
	private final Class<? extends Table> clazz;

	private TableId(@Nonnull Class<? extends Table> clazz, @Nullable String messageIdentifier) {
		this.name = name();
		this.clazz = clazz;
		this.messageIdentifier = messageIdentifier;
	}

	private TableId(@Nonnull String name, @Nonnull Class<? extends Table> clazz, @Nullable String messageIdentifier) {
		this.name = name;
		this.clazz = clazz;
		this.messageIdentifier = messageIdentifier;
	}

	/**
	 * @return The {@link Table} name as defined in the database.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Gets the display name for the {@link Table} represented by this
	 * {@link TableId}. This is either a "prettified" version of
	 * {@link #getName()} or a "root" ({@code messages.properties}) name
	 * depending on if the {@link TableId} in question has a translation
	 * identifier defined.
	 *
	 * @return The prettified or "root" ({@code messages.properties}) database
	 *         name.
	 */
	@Nonnull
	public String getRootDisplayName() {
		if (isBlank(messageIdentifier)) {
			return getTitleCasedName(Locale.ROOT);
		}
		return Messages.getRootString(messageIdentifier);
	}

	/**
	 * Gets the display name for the {@link Table} represented by this
	 * {@link TableId}. This is either a "prettified" version of
	 * {@link #getName()} or a localized name depending on if the
	 * {@link TableId} in question has a translation identifier defined. The
	 * {@link Locale} used for "prettifying" or translation lookup is the
	 * current {@link Locale} from {@link PMS#getLocale()}.
	 *
	 * @return The prettified or localized database name.
	 */
	@Nonnull
	public String getDisplayName() {
		return getDisplayName(null);
	}

	/**
	 * Gets the display name for the {@link Table} represented by this
	 * {@link TableId}. This is either a "prettified" version of
	 * {@link #getName()} or a localized name depending on if the
	 * {@link TableId} in question has a translation identifier defined.
	 *
	 * @param locale the {@link Locale} used both for "prettifying" and looking
	 *            up the correct translation.
	 * @return The prettified or localized database name.
	 */
	@Nonnull
	public String getDisplayName(@Nullable Locale locale) {
		if (isBlank(messageIdentifier)) {
			return getTitleCasedName(locale);
		}
		return Messages.getString(messageIdentifier, locale);
	}

	/**
	 * @return The {@link Class} of the {@link Table} represented by this
	 *         {@link TableId}.
	 */
	@Nonnull
	public Class<? extends Table> getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return getName();
	}

	private String getTitleCasedName(@Nullable Locale locale) {
		return TitleCase.convert(name.replaceAll("_", " ").trim(), locale == null ? PMS.getLocale() : locale);
	}

	@Nonnull
	Table newInstance(@Nonnull TableManager tableManager) {
		switch (this) {
			case COVER_ART_ARCHIVE:
				return new TableCoverArtArchive(tableManager);
			case MUSIC_BRAINZ_RELEASES:
				return new TableMusicBrainzReleases(tableManager);
			case TABLES:
			default:
				throw new AssertionError("TableId " + getName() + " not implemented");

		}
	}
}
