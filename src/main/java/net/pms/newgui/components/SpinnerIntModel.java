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
import javax.swing.AbstractSpinnerModel;
import javax.swing.SpinnerModel;

/**
 * An integer based spinner similar to {@link javax.swing.SpinnerNumberModel}, but
 * with different minimum and maximum alignment in that minimum and maximum don't
 * have to be a multiple of step size.
 *
 * @author Nadahar
 */
public class SpinnerIntModel extends AbstractSpinnerModel implements Serializable, SpinnerModel  {

	private static final long serialVersionUID = 2530845793726723137L;
    private int stepSize, value, minimum, maximum;

	/**
	 * Constructs an integer based <code>SpinnerModel</code> where value
	 * is confined by <code>minimum</code> and <code>maximum</code>. Any
	 * change of value will be in <code>stepSize</code>.
	 * @param value the current integer value of the model
	 * @param minimum the first integer in the range
	 * @param maximum the last integer in the range
	 * @param stepSize the difference between values
	 */
	public SpinnerIntModel(int value, int minimum, int maximum, int stepSize) {
		if (!(minimum <= value && maximum >= value)) {
			throw new IllegalArgumentException("(minimum <= value <= maximum) is false");
		}
		this.value = value;
		this.minimum = minimum;
		this.maximum = maximum;
		this.stepSize = stepSize;
	}

	/**
	 * Constructs an integer based <code>SpinnerModel</code> where value
	 * is confined by <code>minimum</code> and <code>maximum</code>. Any
	 * change of value will be of size 1.
	 * @param value the current integer value of the model
	 * @param minimum the first integer in the range
	 * @param maximum the last integer in the range
	 */
	public SpinnerIntModel(int value, int minimum, int maximum) {
		this(value, minimum, maximum, 1);
	}

	/**
	 * Constructs an integer based <code>SpinnerModel</code>. Any
	 * change of value will be in <code>stepSize</code>.
	 * @param value the current integer value of the model
	 * @param stepSize the difference between values
	 */
	public SpinnerIntModel(int value, int stepSize) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, stepSize);
	}

	/**
	 * Constructs an integer based <code>SpinnerModel</code>. Any
	 * change of value will of size 1.
	 * @param value the current integer value of the model
	 */
	public SpinnerIntModel(int value) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
		fireStateChanged();
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMaximum(int maximum) {
    	this.maximum = maximum;
    	fireStateChanged();
    }

    public int getMaximum() {
    	return maximum;
    }

    public void setStepSize(int stepSize) {
        if (stepSize != this.stepSize) {
            this.stepSize = stepSize;
            fireStateChanged();
        }
    }

    public int getStepSize() {
    	return stepSize;
    }

    private int incrValue(int dir) {
    	int newValue = value + stepSize * dir;
        if (dir > 0 && newValue == minimum + stepSize && minimum % stepSize != 0) {
        	newValue = ((minimum / stepSize) * stepSize) + stepSize;
        } else if (dir < 0 && newValue == maximum - stepSize && maximum % stepSize != 0) {
        	newValue = (maximum / stepSize) * stepSize;
        }
		return Math.min(Math.max(newValue, minimum),maximum);
    }

	@Override
	public Object getNextValue() {
        return incrValue(+1);
	}

	@Override
	public Object getPreviousValue() {
        return incrValue(-1);
	}

	@Override
	public Object getValue() {
		return value;
	}

	public int getIntValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException("value must be integer");
		}
		if ((Integer) value != this.value) {
			this.value = (Integer) value;
			fireStateChanged();
		}
	}

	public void setIntValue(int value) {
		if (value != this.value) {
			this.value = value;
			fireStateChanged();
		}
	}
}
