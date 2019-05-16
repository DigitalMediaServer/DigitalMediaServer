/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------------
 * KeyedComboBoxModel.java
 * ------------------
 * (C) Copyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: KeyedComboBoxModel.java,v 1.6 2006/12/03 15:33:33 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 * 10-Dec-2015 : Implemented generics support
 *
 */
package net.pms.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * The KeyedComboBox model allows to define an internal key (the data element)
 * for every entry in the model.
 * <p/>
 * This class is useful in all cases, where the public text differs from the
 * internal view on the data. A separation between presentation data and
 * processing data is a prerequisite for localizing combobox entries. This model
 * does not allow selected elements, which are not in the list of valid
 * elements.
 *
 * @author Thomas Morgner
 * @author mail@tcox.org (Added generics)
 * @author Nadahar (Implemented generics)
 */
public class KeyedComboBoxModel<K, V> implements ComboBoxModel<V> {

	/**
	 * The internal data carrier to map keys to values and vice versa.
	 */
	public static class ComboBoxItemPair<K, V> {
		/**
		 * The key.
		 */
		private final K key;

		/**
		 * The value for the key.
		 */
		private final V value;

		/**
		 * Creates a new item pair for the given key and value. The value can be
		 * changed later, if needed.
		 *
		 * @param key   the key
		 * @param value the value
		 */
		public ComboBoxItemPair(final K key, final V value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Returns the key.
		 *
		 * @return the key.
		 */
		public K getKey() {
			return key;
		}

		/**
		 * Returns the value.
		 *
		 * @return the value for this key.
		 */
		public V getValue() {
			return value;
		}
	}

	/**
	 * The index of the selected item.
	 */
	protected int selectedItemIndex;

	protected K selectedItemKey;
	protected V selectedItemValue;

	/**
	 * The data (contains ComboBoxItemPairs).
	 */
	private ArrayList<ComboBoxItemPair<K, V>> data;

	/**
	 * The listeners.
	 */
	private ArrayList<ListDataListener> listdatalistener;

	/**
	 * The cached listeners as array.
	 */
	private transient ListDataListener[] tempListeners;

	private boolean allowOtherValue;

	/**
	 * Creates a new keyed {@link ComboBoxModel}.
	 */
	public KeyedComboBoxModel() {
		data = new ArrayList<>();
		listdatalistener = new ArrayList<>();
	}

	/**
	 * Creates a new keyed {@link ComboBoxModel} for the given keys and values. Keys
	 * and values must have the same number of items.
	 *
	 * @param keys   the keys
	 * @param values the values
	 */
	public KeyedComboBoxModel(final K[] keys, final V[] values) {
		this();
		setData(keys, values);
	}

	/**
	 * Replaces the data in this combobox model. The number of keys must be
	 * equals to the number of values.
	 *
	 * @param keys   the keys
	 * @param values the values
	 */
	public void setData(final K[] keys, final V[] values) {
		if (values.length != keys.length) {
			throw new IllegalArgumentException("Keys and values must have the same length.");
		}

		data.clear();
		data.ensureCapacity(keys.length);

		for (int i = 0; i < values.length; i++) {
			add(keys[i], values[i]);
		}

		selectedItemIndex = -1;
		final ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size() - 1);
		fireListDataEvent(evt);
	}

	/**
	 * Notifies all registered list data listener of the given event.
	 *
	 * @param event the event.
	 */
	protected synchronized void fireListDataEvent(final ListDataEvent event) {
		if (tempListeners == null) {
			tempListeners = listdatalistener.toArray(new ListDataListener[listdatalistener.size()]);
		}
		for (ListDataListener listener : tempListeners) {
			if (listener != null && event != null) {
				listener.contentsChanged(event);
			}
		}
	}

	/**
	 * Returns the selected item.
	 *
	 * @return The selected item or <code>null</code> if nothing is selected.
	 *
	 * @deprecated Inherited method. Use {@link #getSelectedKey()} or
	 * {@link #getSelectedValue()} instead.
	 */
	@Deprecated
	@Override
	public V getSelectedItem() {
		return selectedItemValue;
	}

	/**
	 * Returns the selected key.
	 *
	 * @return The selected key or <code>null</code> if nothing is selected.
	 */
	public K getSelectedKey() {
		return selectedItemKey;
	}

	/**
	 * Returns the selected value.
	 *
	 * @return The selected value or <code>null</code> if nothing is selected.
	 */
	public V getSelectedValue() {
		return selectedItemValue;
	}

	/**
	 * Sets the selected key. If the object is not in the list of keys, no
	 * item gets selected.
	 *
	 * @param aKey the new selected item.
	 */
	public void setSelectedKey(final K aKey) {
		if (aKey == null) {
			selectedItemIndex = -1;
			selectedItemKey = null;
			selectedItemValue = null;
		} else {
			final int newSelectedItem = findKeyIndex(aKey);
			if (newSelectedItem == -1) {
				selectedItemIndex = -1;
				selectedItemKey = null;
				selectedItemValue = null;
			} else {
				selectedItemIndex = newSelectedItem;
				selectedItemKey = getKeyAt(selectedItemIndex);
				selectedItemValue = getValueAt(selectedItemIndex);
			}
		}
		fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
	}

	/**
	 * Set the selected item. The implementation of this  method should notify
	 * all registered <code>ListDataListener</code>s that the contents have
	 * changed.
	 *
	 * @param anItem the list object to select or <code>null</code> to clear the
	 *               selection
	 *
	 * @deprecated Inherited method. Use {@link #setSelectedKey(K)} or
	 * {@link #setSelectedValue(V)} instead.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItem(final Object anItem) {
		setSelectedValue((V) anItem);
	}

	/**
	 * Sets the selected value. If the object is not in the list of values, no
	 * item gets selected.
	 *
	 * @param aValue the new selected item.
	 */

	public void setSelectedValue(final V aValue) {
		if (aValue == null) {
			selectedItemIndex = -1;
			selectedItemKey = null;
			selectedItemValue = null;
		} else {
			int newSelectedIndex = findValueIndex(aValue);
			if (newSelectedIndex == -1) {
				if (isAllowOtherValue()) {
					selectedItemIndex = -1;
					selectedItemKey = null;
					selectedItemValue = aValue;
				} else {
					selectedItemIndex = -1;
					selectedItemKey = null;
					selectedItemValue = null;
				}
			} else {
				selectedItemIndex = newSelectedIndex;
				selectedItemKey = getKeyAt(selectedItemIndex);
				selectedItemValue = getValueAt(selectedItemIndex);
			}
		}
		fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
	}

	private boolean isAllowOtherValue() {
		return allowOtherValue;
	}

	public void setAllowOtherValue(final boolean allowOtherValue) {
		this.allowOtherValue = allowOtherValue;
	}

	/**
	 * Adds a listener to the list that's notified each time a change to the data
	 * model occurs.
	 *
	 * @param listener the <code>ListDataListener</code> to be added
	 */
	@Override
	public synchronized void addListDataListener(final ListDataListener listener) {
		listdatalistener.add(listener);
		tempListeners = null;
	}

	/**
	 * Returns the key at the specified index.
	 *
	 * @param index the requested index
	 * @return the key at <code>index</code>
	 *
	 * @deprecated Inherited method. Use {@link #getValueAt(int)} or
	 * {@link #getKeyAt(int)} instead.
	 */
	@Deprecated
	@Override
	public V getElementAt(final int index) {
		if (index >= data.size()) {
			return null;
		}

		final ComboBoxItemPair<K, V> datacon = data.get(index);
		if (datacon == null) {
			return null;
		}
		return datacon.getValue();
	}

	/**
	 * Returns the value at the specified index.
	 *
	 * @param index the requested index
	 * @return the value at <code>index</code>
	 */
	public V getValueAt(final int index) {
		if (index >= data.size()) {
			return null;
		}

		final ComboBoxItemPair<K, V> datacon = data.get(index);
		if (datacon == null) {
			return null;
		}
		return datacon.getValue();
	}

	/**
	 * Returns the key from the given index.
	 *
	 * @param index the index of the key.
	 * @return the the key at the specified index.
	 */
	public K getKeyAt(final int index) {
		if (index >= data.size()) {
			return null;
		}

		if (index < 0) {
			return null;
		}

		final ComboBoxItemPair<K, V> datacon = data.get(index);
		if (datacon == null) {
			return null;
		}
		return datacon.getKey();
	}

	/**
	 * Returns the {@link ComboBoxItemPair} representing the element at the
	 * given index.
	 *
	 * @param index the index of the element.
	 * @return The {@link ComboBoxItemPair}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *             (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	@Nonnull
	public ComboBoxItemPair<K, V> get(int index) {
		return data.get(index);
	}

	/**
	 * @return The number of elements.
	 */
	@Override
	public int getSize() {
		return data.size();
	}

	/**
	 * @return An copy of the elements.
	 */
	@Nonnull
	public List<ComboBoxItemPair<K, V>> getElements() {
		return new ArrayList<>(data);
	}

	/**
	 * Removes a listener from the list that's notified each time a change to
	 * the data model occurs.
	 *
	 * @param l the <code>ListDataListener</code> to be removed
	 */
	@Override
	public synchronized void removeListDataListener(final ListDataListener l) {
		listdatalistener.remove(l);
		tempListeners = null;
	}

	/**
	 * Tries to find the index of element with the given key.
	 *
	 * @param key the key for the element to be searched.
	 * @return the index of the key, or -1 if not found.
	 */
	public int findKeyIndex(@Nullable K key) {
		if (key == null) {
			return -1;
		}

		for (int i = 0; i < data.size(); i++) {
			final ComboBoxItemPair<K, V> datacon = data.get(i);
			if (key.equals(datacon.getKey())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Tries to find the index of element with the given value.
	 *
	 * @param value the value for the element to be searched.
	 * @return the index of the value, or -1 if not found.
	 */
	public int findValueIndex(@Nonnull V value) {
		if (value == null) {
			return -1;
		}

		for (int i = 0; i < data.size(); i++) {
			final ComboBoxItemPair<K, V> datacon = data.get(i);
			if (value.equals(datacon.getValue())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Removes an entry from the model.
	 *
	 * @param key the key
	 * @return The removed value or {@code null}.
	 */
	@Nullable
	public V removeKey(@Nullable K key) {
		final int idx = findKeyIndex(key);
		if (idx == -1) {
			return null;
		}

		ComboBoxItemPair<K, V> result = remove(idx);
		return result == null ? null : result.value;
	}

	/**
	 * Removes an entry from the model.
	 *
	 * @param value the key
	 * @return The removed value or {@code null}.
	 */
	@Nullable
	public K removeValue(@Nullable V value) {
		final int idx = findValueIndex(value);
		if (idx == -1) {
			return null;
		}

		ComboBoxItemPair<K, V> result = remove(idx);
		return result == null ? null : result.key;
	}

	/**
	 * Removes an entry from the model.
	 *
	 * @param idx the entry index.
	 * @return The removed {@link ComboBoxItemPair} or {@code null} if it didn't
	 *         exist.
	 */
	@Nullable
	public ComboBoxItemPair<K, V> remove(int idx) {
		if (idx < 0 || idx >= data.size()) {
			return null;
		}

		ComboBoxItemPair<K, V> result = data.remove(idx);
		if (selectedItemIndex == idx) {
			selectedItemIndex = -1;
			selectedItemKey = null;
			selectedItemValue = null;
		} else if (selectedItemIndex > idx) {
			selectedItemIndex--;
			ComboBoxItemPair<K, V> pair = data.get(selectedItemIndex);
			selectedItemKey = pair.key;
			selectedItemValue = pair.value;
		}
		final ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, idx, idx);
		fireListDataEvent(evt);
		return result;
	}

	/**
	 * Adds a new entry to the model.
	 *
	 * @param key    the key
	 * @param value the display value.
	 */
	public void add(final K key, final V value) {
		final ComboBoxItemPair<K, V> con = new ComboBoxItemPair<>(key, value);
		data.add(con);
		final ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, data.size() - 1, data.size() - 1);
		fireListDataEvent(evt);
	}

	/**
	 * Adds a new entry to the model at the specified position.
	 *
	 * @param index the index at which the specified element is to be inserted.
	 * @param key the key.
	 * @param value the display value.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *             (<tt>index &lt; 0 || index &gt; size()</tt>).
	 */
	public void add(int index, final K key, final V value) {
		final ComboBoxItemPair<K, V> con = new ComboBoxItemPair<>(key, value);
		data.add(index, con);
		if (selectedItemIndex >= index) {
			selectedItemIndex++;
			ComboBoxItemPair<K, V> pair = data.get(selectedItemIndex);
			selectedItemKey = pair.key;
			selectedItemValue = pair.value;
		}
		final ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
		fireListDataEvent(evt);
	}

	/**
	 * Removes all entries from the model.
	 */
	public void clear() {
		final int size = getSize();
		data.clear();
		selectedItemIndex = -1;
		selectedItemKey = null;
		selectedItemValue = null;
		final ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size - 1);
		fireListDataEvent(evt);
	}
}
