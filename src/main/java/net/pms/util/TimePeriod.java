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

import javax.annotation.concurrent.Immutable;


/**
 * A class representing either a fixed or variable period of time or duration.
 *
 * @author Nadahar
 */
@Immutable
public class TimePeriod {
	private final long fixedPeriod;
	private final long variation;

	/**
	 * Creates a new fixed length instance.
	 *
	 * @param fixedPeriod the period length in milliseconds.
	 */
	public TimePeriod(long fixedPeriod) {
		this.fixedPeriod = fixedPeriod;
		this.variation = 0;
	}

	/**
	 * Creates new variable length instance.
	 *
	 * @param fixedPeriod the fixed part of the period length in milliseconds.
	 * @param variation the variable part of the period length in milliseconds.
	 *            Please note that this isn't added to the fixed period, but is
	 *            split in two where half is subtracted from the fixed period
	 *            and the other half is added. This can thus never be greater
	 *            than {@code fixedPeriod} since it would result in a negative
	 *            length time period.
	 * @throws IllegalArgumentException If some variation of the time period
	 *             ends up having a negative length.
	 */
	public TimePeriod(long fixedPeriod, long variation) {
		if (
			fixedPeriod < 0 ||
			variation < 0 ||
			fixedPeriod + (long) (-0.5 * variation) < 0
		) {
			throw new IllegalArgumentException(
				"Negative time periods aren't supported since the concept is hard to grasp"
			);
		}
		this.fixedPeriod = fixedPeriod;
		this.variation = variation;
	}

	/**
	 * @return The fixed part of the time period in milliseconds.
	 */
	public long getFixedPeriod() {
		return fixedPeriod;
	}

	/**
	 * @return The variation in milliseconds.
	 */
	public long getVariation() {
		return variation;
	}

	/**
	 * @return {@code true} if this {@link TimePeriod} is variable,
	 *         {@code false} otherwise.
	 */
	public boolean isVariable() {
		return variation > 0;
	}

	/**
	 * @return {@code true} if this {@link TimePeriod} has a fixed length,
	 *         {@code false} otherwise.
	 */
	public boolean isFixed() {
		return variation == 0;
	}

	/**
	 * @return A valid duration for this {@link TimePeriod}. Please note that
	 *         unless this {@link TimePeriod} is of fixed length, the returned
	 *         value will vary.
	 */
	public long getDuration() {
		return variation == 0 ? fixedPeriod : fixedPeriod + (long) (variation * (Math.random() - 0.5));
	}

	/**
	 * @return A time in milliseconds since midnight, January 1, 1970 when this
	 *         {@link TimePeriod} will end if the time period starts now.
	 */
	public long getTime() {
		return System.currentTimeMillis() + (variation <= 0 ?
			fixedPeriod :
			fixedPeriod + (long) (variation * (Math.random() - 0.5)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("=");
		if (variation <= 0) {
			sb.append(StringUtil.formatDuration(fixedPeriod, false));
		} else {
			double delta = variation / 2.0;
			String shortest = StringUtil.formatDuration(Math.round(fixedPeriod - delta), false);
			String longest = StringUtil.formatDuration(Math.round(fixedPeriod + delta), false);
			int length = Math.min(shortest.length(), longest.length());
			int i = 0;
			for (; i < length; i++) {
				if (shortest.charAt(i) != longest.charAt(i)) {
					break;
				}
			}
			while (i > -1 && shortest.charAt(i) != ' ') {
				i--;
			}
			sb.append(shortest).append("-").append(i > 0 ? longest.substring(i + 1) : longest);
		}
		return sb.toString();
	}
}
