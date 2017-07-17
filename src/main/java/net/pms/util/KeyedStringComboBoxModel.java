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

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import net.pms.Messages;

/**
 * This class is a custom version of {@link KeyedComboBoxModel} that allows
 * adding custom key/value pairs at runtime by using an editable
 * {@link JComboBox}. Both keys and values are confined to {@link String}.
 *
 * @author Nadahar
 */

public class KeyedStringComboBoxModel extends KeyedComboBoxModel<String, String> {

	/**
	 * Creates a new keyed {@link ComboBoxModel}.
	 */
	public KeyedStringComboBoxModel() {
		super();
	}

	/**
	 * Creates a new keyed {@link ComboBoxModel} for the given keys and values. Keys
	 * and values must have the same number of items.
	 *
	 * @param keys   the keys
	 * @param values the values
	 */
	public KeyedStringComboBoxModel(final String[] keys, final String[] values) {
		super(keys, values);
	}

	/**
	 * Set the selected item. The implementation of this  method should notify
	 * all registered <code>ListDataListener</code>s that the contents have
	 * changed.
	 *
	 * @param anItem the list object to select or <code>null</code> to clear the
	 *               selection
	 *
	 * @deprecated Inherited method. Use {@link #setSelectedKey(String)} or
	 * {@link #setSelectedValue(String)} instead.
	 */
	@Deprecated
	@Override
	public void setSelectedItem(final Object anItem) {
		setSelectedValue((String) anItem);
	}

	/**
	 * Sets the selected value. If the {@link String} is not in the list of
	 * values, a new custom entry is generated.
	 *
	 * @param aValue the new selected item.
	 */

	@Override
	public void setSelectedValue(final String aValue) {
		if (aValue == null) {
			selectedItemIndex = -1;
			selectedItemKey = null;
			selectedItemValue = null;
		} else {
			int newSelectedIndex = findValueIndex(aValue);
			if (newSelectedIndex == -1) {
				newSelectedIndex = findKeyIndex(aValue);
				if (newSelectedIndex == -1) {
					add(aValue, generateCustomValue(aValue));
					selectedItemIndex = findKeyIndex(aValue);
					selectedItemKey = getKeyAt(selectedItemIndex);
					selectedItemValue = getValueAt(selectedItemIndex);
				} else {
					selectedItemIndex = newSelectedIndex;
					selectedItemKey = getKeyAt(selectedItemIndex);
					selectedItemValue = getValueAt(selectedItemIndex);
				}
			} else {
				selectedItemIndex = newSelectedIndex;
				selectedItemKey = getKeyAt(selectedItemIndex);
				selectedItemValue = getValueAt(selectedItemIndex);
			}
		}
		fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
	}

	/**
	 * Sets the selected key. If the {@link String} is not in the list of
	 * keys, a new custom entry is generated.
	 *
	 * @param aKey the new selected item.
	 */
	@Override
	public void setSelectedKey(final String aKey) {
		if (aKey == null) {
			selectedItemIndex = -1;
			selectedItemKey = null;
			selectedItemValue = null;
		} else {
			final int newSelectedItem = findKeyIndex(aKey);
			if (newSelectedItem == -1) {
				add(aKey, generateCustomValue(aKey));
				selectedItemIndex = findKeyIndex(aKey);
				selectedItemKey = getKeyAt(selectedItemIndex);
				selectedItemValue = getValueAt(selectedItemIndex);
			} else {
				selectedItemIndex = newSelectedItem;
				selectedItemKey = getKeyAt(selectedItemIndex);
				selectedItemValue = getValueAt(selectedItemIndex);
			}
		}
		fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
	}

	/**
	 * @deprecated Inherited method with no function.
	 */
	@Deprecated
	@Override
	public void setAllowOtherValue(final boolean allowOtherValue) {
		throw new IllegalArgumentException("AllowOtherValue is implicit in KeyedStringComboBoxModel");
	}

	private String generateCustomValue(String key) {
		return String.format(Messages.getString("KeyedComboBoxModel.1"), key);
	}
}
