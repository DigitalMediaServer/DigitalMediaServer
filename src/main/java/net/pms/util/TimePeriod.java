package net.pms.util;

import javax.annotation.concurrent.Immutable;


@Immutable
public class TimePeriod {
	private final long fixedPeriod;
	private final long variation;

	public TimePeriod(long fixedPeriod) {
		this.fixedPeriod = fixedPeriod;
		this.variation = 0;
	}

	public TimePeriod(long fixedPeriod, long variation) {
		this.fixedPeriod = fixedPeriod;
		this.variation = variation;
	}

	public long getFixedPeriod() {
		return fixedPeriod;
	}

	public long getVariation() {
		return variation;
	}

	public long getDuration() {
		return variation <= 0 ? fixedPeriod : fixedPeriod + (long) (variation * (Math.random() - 0.5));
	}

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
