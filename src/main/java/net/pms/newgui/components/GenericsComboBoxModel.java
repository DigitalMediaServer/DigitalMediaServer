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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;

/**
 * A {@link MutableComboBoxModel} implementation with the same functionality as
 * {@link DefaultComboBoxModel}, but with full generics support and backed by a
 * {@link List} instead of a {@link Vector}.
 *
 * @param <E> the type of the items of this model.
 *
 * @author Nadahar
 */
@NotThreadSafe
public class GenericsComboBoxModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>, Serializable {

	private static final long serialVersionUID = 1L;

	/** The {@link List} of items in this model */
	@Nonnull
	protected final List<E> items;

	/** The currently selected item or {@code null} */
	@Nullable
	protected E selectedItem;

	/**
	 * Creates a new empty instance.
	 */
	public GenericsComboBoxModel() {
		items = new ArrayList<E>();
	}

	/**
	 * Creates a new instance initialized with the specified array of items.
	 *
	 * @param items the array of items.
	 */
	public GenericsComboBoxModel(@Nonnull E[] items) {
		if (items == null) {
			throw new IllegalArgumentException("items cannot be null");
		}
		this.items = new ArrayList<E>(Arrays.asList(items));

		if (getSize() > 0) {
			selectedItem = getElementAt(0);
		}
	}

	/**
	 * Creates a new instance with the specified {@link List}.
	 *
	 * @param items the {@link List} of items.
	 * @param copy if {@code true}, a copy will be made of the specified
	 *            {@link List}. If {@code false} the same instance as the
	 *            specified {@link List} will be used, so that changes are
	 *            "write through".
	 */
	public GenericsComboBoxModel(@Nonnull List<E> items, boolean copy) {
		if (items == null) {
			throw new IllegalArgumentException("items cannot be null");
		}
		if (copy) {
			this.items = new ArrayList<E>(items);
		} else {
			this.items = items;
		}

		if (getSize() > 0) {
			selectedItem = getElementAt(0);
		}
	}

	/**
	 * Creates a new instance initialized with the specified {@link Collection}
	 * of items.
	 *
	 * @param items the {@link Collection} of items.
	 */
	public GenericsComboBoxModel(@Nonnull Collection<E> items) {
		if (items == null) {
			throw new IllegalArgumentException("items cannot be null");
		}
		this.items = new ArrayList<E>(items);
	}

	/**
	 * Returns the index-position of the specified item in the list.
	 *
	 * @param item the item whose index to return.
	 * @return The index position, where 0 is the first position.
	 */
	public int getIndexOf(@Nullable E item) {
		return items.indexOf(item);
	}

	/**
	 * @return A {@link List} containing the current items.
	 */
	public ArrayList<E> getItems() {
		return new ArrayList<E>(items);
	}

	/**
	 * Retains only the items that are contained in the specified
	 * {@link Collection}. In other words, removes all items that are not
	 * contained in the specified {@link Collection}.
	 *
	 * @param items the {@link Collection} of items to be retained.
	 * @return {@code true} if the list of items is changed as a result of the
	 *         call, {@code false} otherwise.
	 */
	public boolean retainItems(@Nullable Collection<E> items) {
		if (items == null) {
			return false;
		}
		return this.items.retainAll(items);
	}

	/**
	 * Appends all the items in the specified {@link Collection} after the
	 * current items, in the order that they are returned by the specified
	 * {@link Collection}'s {@link Iterator}. The behavior of this operation is
	 * undefined if the specified {@link Collection} is modified while the
	 * operation is in progress.
	 *
	 * @param items the {@link Collection} of items to be added.
	 * @return {@code true} if the list of items is changed as a result of the
	 *         call, {@code false} otherwise.
	 */
	public boolean addItems(@Nullable Collection<E> items) {
		if (items == null) {
			return false;
		}
		return this.items.addAll(items);
	}

	/**
	 * Makes sure that all the items in the specified {@link Collection} exists
	 * in the list of items by adding those missing.
	 *
	 * @param items the {@link Collection} of items to make sure exists.
	 * @return The number of additions made to the list of items as a result of
	 *         the call.
	 */
	public int setItems(@Nullable Collection<E> items) {
		if (items == null) {
			return 0;
		}
		int count = 0;
		for (E item : items) {
			if (!this.items.contains(item)) {
				this.items.add(item);
				count++;
			}
		}
		return count;
	}

	/**
	 * Makes sure that the list of items contains the same items as the
	 * specified {@link Collection} in any order. This is a one-way
	 * synchronization where all changes are made to the list of items and the
	 * specified {@link Collection} is unchanged. Items will be removed and
	 * added as needed.
	 *
	 * @param items the {@link Collection} of items to synchronize with.
	 * @return The sum of additions (positive) and removals (negative) to the
	 *         list of items. A zero return value doesn't necessarily mean that
	 *         nothing was changed, but can also mean that the same number of
	 *         items were added and removed.
	 */
	public int syncWith(@Nullable Collection<E> items) {
		if (items == null) {
			return 0;
		}
		int count = this.items.size();
		if (retainItems(items)) {
			count = this.items.size() - count;
		}
		return count + setItems(items);
	}

	/**
	 * Removes all items.
	 */
	public void removeAllElements() {
		if (items.size() > 0) {
			int firstIndex = 0;
			int lastIndex = items.size() - 1;
			items.clear();
			selectedItem = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedItem = null;
		}
	}

	/**
	 * Sets or clears the selected item using generics.
	 *
	 * @param item the new selected item or {@code null} for no selection.
	 */
	public void setSelectedGItem(@Nullable E item) {
		if ((
				selectedItem != null &&
				!selectedItem.equals(item)
			) || (
				selectedItem == null &&
				item != null
			)
		) {
			selectedItem = item;
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @return The selected item or {@code null} if no selection exists.
	 */
	@Nullable
	public E getSelectedGItem() {
		return selectedItem;
	}

	/**
	 * Removes an item from the model if it exists.
	 *
	 * @param item the item to be removed.
	 * @return {@code true} it the item was removed, {@code false} if it wasn't
	 *         found.
	 */
	public boolean removeItem(@Nullable E item) {
		int index = items.indexOf(item);
		if (index != -1) {
			removeElementAt(index);
			return true;
		}
		return false;
	}

	// ComboBoxModel implementation

	/**
	 * Sets or clears the selected item.
	 *
	 * @param item the new selected item or {@code null} for no selection.
	 *
	 * @deprecated Inherited method without generics, use
	 *             {@link #setSelectedItemGenerics} instead.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@Override
	public void setSelectedItem(Object item) {
		if ((
				selectedItem != null &&
				!selectedItem.equals(item)
			) || (
				selectedItem == null &&
				item != null
			)
		) {
			selectedItem = (E) item;
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @return The selected item or {@code null} if no selection exists.
	 *
	 * @deprecated Inherited method without generics, use
	 *             {@link #getSelectedGItem()} instead.
	 */
	@Deprecated
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	// ListModel implementation

	@Override
	public int getSize() {
		return items.size();
	}

	@Override
	@Nullable
	public E getElementAt(int index) {
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return null;
	}

	// MutableComboBoxModel implementation

	@Override
	public void addElement(@Nullable E item) {
		items.add(item);
		fireIntervalAdded(this, items.size() - 1, items.size() - 1);
		if (items.size() == 1 && selectedItem == null && item != null) {
			setSelectedGItem(item);
		}
	}

	@Override
	public void insertElementAt(@Nullable E item, int index) {
		items.add(index, item);
		fireIntervalAdded(this, index, index);
	}

	@Override
	public void removeElementAt(int index) {
		if (getElementAt(index) == selectedItem) {
			if (index == 0) {
				setSelectedGItem(getSize() == 1 ? null : getElementAt(index + 1));
			} else {
				setSelectedGItem(getElementAt(index - 1));
			}
		}
		items.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public void removeElement(Object item) {
		int index = items.indexOf(item);
		if (index != -1) {
			removeElementAt(index);
		}
	}
}
