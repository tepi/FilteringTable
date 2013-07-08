package org.tepi.filtertable.paged;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.UnsupportedFilterException;

public class PagedFilterTableContainer<T extends Container.Indexed & Container.Filterable & Container.ItemSetChangeNotifier>
		implements Container, Container.Indexed, Container.Sortable,
		Container.Filterable, Container.ItemSetChangeNotifier {
	private static final long serialVersionUID = -2134233618583099046L;

	private final T container;
	private int pageLength = 25;
	private int startIndex = 0;

	public PagedFilterTableContainer(T container) {
		this.container = container;
	}

	public T getContainer() {
		return container;
	}

	public int getPageLength() {
		return pageLength;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/*
	 * Overridden methods from the real container from here forward
	 */

	public int size() {
		int rowsLeft = container.size() - startIndex;
		if (rowsLeft > pageLength) {
			return pageLength;
		} else {
			return rowsLeft;
		}
	}

	public int getRealSize() {
		return container.size();
	}

	public Object getIdByIndex(int index) {
		return container.getIdByIndex(index + startIndex);
	}

	/*
	 * Delegate methods to real container from here on
	 */

	public Item getItem(Object itemId) {
		return container.getItem(itemId);
	}

	public Collection<?> getContainerPropertyIds() {
		return container.getContainerPropertyIds();
	}

	public Collection<?> getItemIds() {
		return container.getItemIds();
	}

	@Override
	public List<?> getItemIds(int startIndex, int numberOfItems) {
		return container
				.getItemIds(this.startIndex + startIndex, numberOfItems);
	}

	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		return container.getContainerProperty(itemId, propertyId);
	}

	public Class<?> getType(Object propertyId) {
		return container.getType(propertyId);
	}

	public boolean containsId(Object itemId) {
		return container.containsId(itemId);
	}

	public Item addItem(Object itemId) throws UnsupportedOperationException {
		return container.addItem(itemId);
	}

	public Object addItem() throws UnsupportedOperationException {
		return container.addItem();
	}

	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		return container.removeItem(itemId);
	}

	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		return container.addContainerProperty(propertyId, type, defaultValue);
	}

	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		return container.removeContainerProperty(propertyId);
	}

	public boolean removeAllItems() throws UnsupportedOperationException {
		return container.removeAllItems();
	}

	public Object nextItemId(Object itemId) {
		return container.nextItemId(itemId);
	}

	public Object prevItemId(Object itemId) {
		return container.prevItemId(itemId);
	}

	public Object firstItemId() {
		return container.firstItemId();
	}

	public Object lastItemId() {
		return container.lastItemId();
	}

	public boolean isFirstId(Object itemId) {
		return container.isFirstId(itemId);
	}

	public boolean isLastId(Object itemId) {
		return container.isLastId(itemId);
	}

	public Object addItemAfter(Object previousItemId)
			throws UnsupportedOperationException {
		return container.addItemAfter(previousItemId);
	}

	public Item addItemAfter(Object previousItemId, Object newItemId)
			throws UnsupportedOperationException {
		return container.addItemAfter(previousItemId, newItemId);
	}

	public int indexOfId(Object itemId) {
		return container.indexOfId(itemId);
	}

	public Object addItemAt(int index) throws UnsupportedOperationException {
		return container.addItemAt(index);
	}

	public Item addItemAt(int index, Object newItemId)
			throws UnsupportedOperationException {
		return container.addItemAt(index, newItemId);
	}

	/*
	 * Sorting interface from here on
	 */

	public void sort(Object[] propertyId, boolean[] ascending) {
		if (container instanceof Container.Sortable) {
			((Container.Sortable) container).sort(propertyId, ascending);
		}
	}

	public Collection<?> getSortableContainerPropertyIds() {
		if (container instanceof Container.Sortable) {
			return ((Container.Sortable) container)
					.getSortableContainerPropertyIds();
		}
		return Collections.EMPTY_LIST;
	}

	public void addContainerFilter(Filter filter)
			throws UnsupportedFilterException {
		container.addContainerFilter(filter);
	}

	@Override
	public Collection<Filter> getContainerFilters() {
		return container.getContainerFilters();
	}

	public void removeContainerFilter(Filter filter) {
		container.removeContainerFilter(filter);
	}

	public void removeAllContainerFilters() {
		container.removeAllContainerFilters();
	}

	@Override
	public void addItemSetChangeListener(ItemSetChangeListener listener) {
		((Container.ItemSetChangeNotifier) container)
				.addItemSetChangeListener(listener);
	}

	@Override
	public void removeItemSetChangeListener(ItemSetChangeListener listener) {
		((Container.ItemSetChangeNotifier) container)
				.removeItemSetChangeListener(listener);
	}

	@Override
	public void addListener(ItemSetChangeListener listener) {
		addItemSetChangeListener(listener);
	}

	@Override
	public void removeListener(ItemSetChangeListener listener) {
		removeItemSetChangeListener(listener);
	}
}