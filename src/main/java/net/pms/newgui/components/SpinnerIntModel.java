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
package net.pms.newgui.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractSpinnerModel;
import javax.swing.SpinnerModel;
import net.pms.newgui.components.CustomJSpinner.IrregularEntry;

/**
 * This is an integer based spinner similar to
 * {@link javax.swing.SpinnerNumberModel}, but with different minimum and
 * maximum alignment in that minimum and maximum don't have to be a multiple of
 * step size. This also has more features, like mouse-wheel adjustment and
 * support for irregular values.
 * <p>
 * Irregular values are values that lay outside the "normal" range (between
 * minimum and maximum) which can be represented by a text instead of an integer
 * value. These can be used to represent "Automatic", "Default" or similar.
 *
 * @author Nadahar
 */
public class SpinnerIntModel extends AbstractSpinnerModel implements Serializable, SpinnerModel  {

	private static final long serialVersionUID = 2530845793726723137L;

	/** The current value */
	protected int value;

	/** The step size */
	protected int stepSize;

	/** The minimum regular value */
	protected int minimum;

	/** The maximum regular value */
	protected int maximum;

	/** The {@link Set} of {@link IrregularEntry} instances */
	@Nonnull
	protected final Set<IrregularEntry> irregularEntries = new HashSet<>();

	/**
	 * The (sorted) {@link List} of {@link IrregularEntry} instances with values
	 * below the "regular" value range.
	 */
	@Nonnull
	protected final List<Integer> subIrregularValues = new ArrayList<>();

	/**
	 * The (sorted) {@link List} of {@link IrregularEntry} instances with values
	 * above the "regular" value range.
	 */
	@Nonnull
	protected final List<Integer> superIrregularValues = new ArrayList<>();

	/**
	 * Creates a new instance with no limits and with a step size of {@code 1}.
	 * <p>
	 * <b>Note:</b> This constructor can not be used with irregular values as
	 * the initial value will be corrected to lay within minimum and maximum and
	 * as such discard any irregular value.
	 *
	 * @param value the initial value of the model.
	 */
	public SpinnerIntModel(int value) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	}

	/**
	 * Creates a new instance with the specified limits, an initial value of
	 * {@code 0} and a step size of {@code 1}.
	 * <p>
	 * <b>Note:</b> This constructor can be used with irregular values as long
	 * as the initial value is set with {@link #setIntValue(int)} <i>after</i>
	 * the irregular values have been set with
	 * {@link #setIrregularEntries(Collection)}.
	 *
	 * @param minimum the first value in the range.
	 * @param maximum the last value in the range.
	 */
	public SpinnerIntModel(int minimum, int maximum) {
		this(0, minimum, maximum, 1);
	}

	/**
	 * Creates a new instance with the specified initial value, the specified
	 * limits and a step size of {@code 1}.
	 * <p>
	 * <b>Note:</b> This constructor can not be used with irregular values as
	 * the initial value will be corrected to lay within minimum and maximum and
	 * as such discard any irregular value.
	 *
	 * @param value the initial value of the model.
	 * @param minimum the first value in the range.
	 * @param maximum the last value in the range.
	 */
	public SpinnerIntModel(int value, int minimum, int maximum) {
		this(value, minimum, maximum, 1);
	}

	/**
	 * Creates a new instance with the specified initial value, limits, step
	 * size and optionally irregular values.
	 *
	 * @param value the initial value of the model.
	 * @param minimum the first value in the range.
	 * @param maximum the last value in the range.
	 * @param stepSize the step size.
	 * @param irregularEntries zero or more {@link IrregularEntry} instances
	 *            that defines irregular entries for the model.
	 *
	 * @throws IllegalArgumentException if {@code minimum} is greater than
	 *             {@code maximum}, {@code stepSize} is less than {@code 1} or
	 *             one of the irregular values lies within {@code minimum} and
	 *             {@code maximum} (inclusive).
	 */
	public SpinnerIntModel(int value, int minimum, int maximum, int stepSize, IrregularEntry... irregularEntries) {
		if (stepSize < 1) {
			throw new IllegalArgumentException("stepSize must be 1 or greater");
		}
		if (minimum > maximum) {
			throw new IllegalArgumentException("minimum must be less than or equal to maximum");
		}
		this.minimum = minimum;
		this.maximum = maximum;
		this.stepSize = stepSize;
		if (irregularEntries != null && irregularEntries.length > 0) {
			addIrregularEntries(Arrays.asList(irregularEntries));
		}
		this.value = validValue(value);
	}

	/**
	 * @return A copy of the current irregular entries.
	 */
	@Nonnull
	public Set<IrregularEntry> getIrregularEntries() {
		return new HashSet<>(irregularEntries);
	}

	/**
	 * Sets the irregular entries of this {@link SpinnerIntModel} to the
	 * specified {@link Collection} of {@link IrregularEntry} instances.
	 * <p>
	 * <b>Note:</b> If this {@link SpinnerModel} is in use by one or more
	 * {@link CustomJSpinner}(s),
	 * {@link CustomJSpinner#updateIrregularEntries()} must be invoked after
	 * this method to effectuate the changes.
	 *
	 * @param irregularEntries the {@link Collection} of {@link IrregularEntry}
	 *            instances to set.
	 */
	public void setIrregularEntries(@Nullable Collection<IrregularEntry> irregularEntries) {
		this.irregularEntries.clear();
		addIrregularEntries(irregularEntries);
	}

	/**
	 * Adds the specified {@link Collection} of {@link IrregularEntry} instances
	 * to this {@link SpinnerIntModel}.
	 * <p>
	 * <b>Note:</b> If this {@link SpinnerModel} is in use by one or more
	 * {@link CustomJSpinner}(s),
	 * {@link CustomJSpinner#updateIrregularEntries()} must be invoked after
	 * this method to effectuate the changes.
	 *
	 * @param irregularEntries the {@link Collection} of {@link IrregularEntry}
	 *            instances to add.
	 */
	public void addIrregularEntries(@Nullable Collection<IrregularEntry> irregularEntries) {
		if (irregularEntries != null) {
			this.irregularEntries.addAll(irregularEntries);
		}
		subIrregularValues.clear();
		superIrregularValues.clear();
		int tmpValue;
		for (IrregularEntry entry : this.irregularEntries) {
			tmpValue = entry.getValue();
			if (tmpValue < minimum) {
				subIrregularValues.add(Integer.valueOf(tmpValue));
			} else if (tmpValue > maximum) {
				superIrregularValues.add(Integer.valueOf(tmpValue));
			} else {
				throw new IllegalArgumentException("Irregular value " + tmpValue + " isn't outside the normal range");
			}
		}
		Collections.sort(subIrregularValues);
		Collections.sort(superIrregularValues);
	}

	/**
	 * @return The minimum regular value.
	 */
	public int getMinimum() {
		return minimum;
	}

	/**
	 * Sets the minimum regular value.
	 *
	 * @param minimum the minimum regular value.
	 */
	public void setMinimum(int minimum) {
		this.minimum = minimum;
		fireStateChanged();
	}

	/**
	 * @return The maximum regular value.
	 */
	public int getMaximum() {
		return maximum;
	}

	/**
	 * Sets the maximum regular value.
	 *
	 * @param maximum the maximum regular value.
	 */
	public void setMaximum(int maximum) {
		this.maximum = maximum;
		fireStateChanged();
	}

	/**
	 * @return The step size.
	 */
	public int getStepSize() {
		return stepSize;
	}

	/**
	 * Sets the step size.
	 *
	 * @param stepSize the step size to set.
	 */
	public void setStepSize(int stepSize) {
		if (stepSize != this.stepSize) {
			this.stepSize = stepSize;
			fireStateChanged();
		}
	}

	@Nullable
	private Integer changeValue(boolean increase) {
		if (!superIrregularValues.isEmpty()) {
			Integer irregularValue;
			for (int i = 0; i < superIrregularValues.size(); i++) {
				irregularValue = superIrregularValues.get(i);
				if (value == irregularValue.intValue()) {
					if (increase) {
						return i < superIrregularValues.size() - 1 ? superIrregularValues.get(i + 1) : null;
					}
					return i > 0 ? superIrregularValues.get(i - 1) : Integer.valueOf(maximum);
				}
			}
		}
		if (!subIrregularValues.isEmpty()) {
			Integer irregularValue;
			for (int i = 0; i < subIrregularValues.size(); i++) {
				irregularValue = subIrregularValues.get(i);
				if (value == irregularValue.intValue()) {
					if (increase) {
						return i < subIrregularValues.size() - 1 ? subIrregularValues.get(i + 1) : Integer.valueOf(minimum);
					}
					return i > 0 ? subIrregularValues.get(i - 1) : null;
				}
			}
		}
		int newValue = value + stepSize * (increase ? 1 : -1);
		if (increase && newValue == minimum + stepSize && minimum % stepSize != 0) {
			newValue = ((minimum / stepSize) * stepSize) + stepSize;
		} else if (!increase && newValue == maximum - stepSize && maximum % stepSize != 0) {
			newValue = (maximum / stepSize) * stepSize;
		}
		newValue = Math.min(Math.max(newValue, minimum), maximum);
		if (newValue == value) {
			if (increase && !superIrregularValues.isEmpty()) {
				return superIrregularValues.get(0);
			}
			if (!increase && !subIrregularValues.isEmpty()) {
				return subIrregularValues.get(subIrregularValues.size() - 1);
			}
			return null;
		}
		return Integer.valueOf(newValue);
	}

	@Override
	public Integer getNextValue() {
		return changeValue(true);
	}

	@Override
	public Integer getPreviousValue() {
		return changeValue(false);
	}

	@Override
	public Integer getValue() {
		return Integer.valueOf(value);
	}

	/**
	 * @return The current spinner value.
	 */
	public int getIntValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException("value must be an Integer");
		}
		if (((Integer) value).intValue() != this.value) {
			this.value = validValue(((Integer) value).intValue());
			fireStateChanged();
		}
	}

	/**
	 * Sets the current value of the model. If the value is outside the
	 * "regular" range and doesn't match an existing irregular value, it will be
	 * set to either {@code minimum} or {@code maximum}, whatever is closer.
	 *
	 * @param value the value to set.
	 */
	public void setIntValue(int value) {
		if (value != this.value) {
			this.value = validValue(value);
			fireStateChanged();
		}
	}

	/**
	 * Determines if the specified value is valid and modifies it so that it is
	 * if not. A valid value is one that is greater than or equal to
	 * {@code minimum} <i>and</i> less than or equal to {@code maximum}, or one
	 * that matches a defined irregular value.
	 *
	 * @param value the value to validate.
	 * @return The value if valid, or the closes valid value within the
	 *         "regular" range if not.
	 */
	protected int validValue(int value) {
		for (IrregularEntry entry : this.irregularEntries) {
			if (entry.getValue() == value) {
				return value;
			}
		}
		return Math.min(Math.max(value, minimum), maximum);
	}
}
