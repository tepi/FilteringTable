package org.tepi.filtertable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.tepi.filtertable.FilterFieldGenerator.IFilterTable;
import org.tepi.filtertable.client.ui.FilterTableConnector;
import org.tepi.filtertable.datefilter.DateInterval;

import com.vaadin.server.KeyMapper;
import com.vaadin.server.LegacyPaint;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;

/**
 * FilterTable is an extension of the Vaadin Table component that provides
 * automatically generated filter fields for each column.
 * 
 * @author Teppo Kurki
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
public class FilterTable extends Table implements IFilterTable {
	/* Maps property id's to column filter components */
	private final Map<Object, Component> columnIdToFilterMap = new HashMap<Object, Component>();
	/* Internal list of currently collapsed column id:s */
	private final Set<Object> collapsedColumnIds = new HashSet<Object>();
	/* Set to true to show the filter components */
	private boolean filtersVisible;
	/* Filter Generator and Decorator */
	private FilterGenerator filterGenerator;
	private FilterDecorator decorator;
	/* FilterFieldGenerator instance */
	private final FilterFieldGenerator generator;
	/* Is initialization done */
	private final boolean initDone;
	/* Force-render filter fields */
	private boolean reRenderFilterFields;
	/* Are filters run immediately, or only on demand? */
	private boolean filtersRunOnDemand = false;
	/* Custom column header style names */
	private final HashMap<Object, String> columnHeaderStylenames = new HashMap<Object, String>();
	/* Fields from Table accessed via reflection */
	private KeyMapper<Object> _columnIdMap;
	private HashSet<Component> _visibleComponents;
	/* Timezone for date filters */
	private TimeZone timeZone;

	/**
	 * Creates a new empty FilterTable
	 */
	public FilterTable() {
		this(null);
	}

	/**
	 * Creates a new empty FilterTable with the given caption
	 * 
	 * @param caption
	 *            Caption to set for the FilterTable
	 */
	@SuppressWarnings("unchecked")
	public FilterTable(String caption) {
		super(caption);
		try {
			java.lang.reflect.Field field = com.vaadin.v7.ui.Table.class.getDeclaredField("columnIdMap");
			field.setAccessible(true);
			_columnIdMap = (KeyMapper<Object>) field.get(this);
		} catch (Exception exception) {
			throw new IllegalArgumentException("Unable to get columnIdMap or visibleComponents", exception);
		}
		generator = new FilterFieldGenerator(this);
		initDone = true;
	}

	@Override
	protected void refreshRenderedCells() {
		super.refreshRenderedCells();

		// NOTE: 'visibleComponents' HashSet is (re)created by method  getVisibleCellsNoCache(...)
		//       But only when method  refreshRenderedCells()  calls it.
		try {
			java.lang.reflect.Field field = com.vaadin.v7.ui.Table.class.getDeclaredField("visibleComponents");
			field.setAccessible(true);
			_visibleComponents = (HashSet<Component>) field.get(this);
		} catch (Exception exception) {
			throw new IllegalArgumentException("Unable to get visibleComponents", exception);
		}
	}
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		/* Add filter components to UIDL */
		target.startTag(FilterTableConnector.TAG_FILTERS);
		target.addAttribute(FilterTableConnector.ATTRIBUTE_FILTERS_VISIBLE, filtersVisible);
		target.addAttribute(FilterTableConnector.ATTRIBUTE_FORCE_RENDER, reRenderFilterFields);
		reRenderFilterFields = false;
		for (Object key : getColumnIdToFilterMap().keySet()) {
			/* Make sure parent is set properly */
			if (columnIdToFilterMap.get(key) != null && columnIdToFilterMap.get(key).getParent() == null) {
				continue;
			}
			/* Paint the filter field */
			target.startTag(FilterTableConnector.TAG_FILTER_COMPONENT + _columnIdMap.key(key));
			target.addAttribute(FilterTableConnector.ATTRIBUTE_COLUMN_ID, _columnIdMap.key(key));
			Component c = getColumnIdToFilterMap().get(key);
			LegacyPaint.paint(c, target);
			target.endTag(FilterTableConnector.TAG_FILTER_COMPONENT + _columnIdMap.key(key));
		}
		Map<String, String> headerStylenames = getColumnHeaderStylenamesForPaint();
		if (headerStylenames != null) {
			target.addAttribute(FilterTableConnector.ATTRIBUTE_COLUMN_HEADER_STYLE_NAMES, headerStylenames);
		}
		target.endTag(FilterTableConnector.TAG_FILTERS);
	}

	@Override
	public void setColumnCollapsed(Object propertyId, boolean collapsed) throws IllegalStateException {
		super.setColumnCollapsed(propertyId, collapsed);
		Component c = getColumnIdToFilterMap().get(propertyId);
		if (collapsed) {
			collapsedColumnIds.add(propertyId);
			if (c != null) {
				c.setParent(null);
				if (c instanceof TextField) {
					((TextField) c).setValue("");
				} else if (c instanceof AbstractField<?>) {
					((AbstractField<?>) c).setValue(null);
				}
			}
		} else {
			if (c != null) {
				c.setParent(this);
			}
			collapsedColumnIds.remove(propertyId);
		}
		reRenderFilterFields = true;
		markAsDirty();
	}

	@Override
	public void setContainerDataSource(Container newDataSource) {
		super.setContainerDataSource(newDataSource);
	}

	@Override
	public void setContainerDataSource(Container newDataSource, Collection<?> visibleIds) {
		super.setContainerDataSource(newDataSource, visibleIds);
		resetFilters();
	}

	/**
	 * Resets all filters.
	 * 
	 * Note: Recreates the filter fields also!
	 */
	public void resetFilters() {
		if (initDone) {
			disableContentRefreshing();
			for (Component c : columnIdToFilterMap.values()) {
				c.setParent(null);
			}
			collapsedColumnIds.clear();
			columnIdToFilterMap.clear();
			generator.destroyFilterComponents();
			generator.initializeFilterFields();
			reRenderFilterFields = true;
			enableContentRefreshing(true);
		}
	}

	/**
	 * Clears all filters without recreating the filter fields.
	 */
	public void clearFilters() {
		if (initDone) {
			generator.clearFilterData();
		}
	}

	/**
	 * Sets the FilterDecorator for this FilterTable. FilterDecorator may be
	 * used to provide proper translated display names and icons for the enum,
	 * boolean and date values used in the filters.
	 * 
	 * Note: Recreates the filter fields also!
	 * 
	 * @param decorator
	 *            An implementation of FilterDecorator to use with this
	 *            FilterTable. Remove by giving null as this parameter.
	 */
	public void setFilterDecorator(FilterDecorator decorator) {
		this.decorator = decorator;
		resetFilters();
	}

	/**
	 * Sets the FilterGenerator to use for providing custom Filters to the
	 * container for one or more properties.
	 * 
	 * @param generator
	 *            FilterGenerator to use with this FilterTable. Remove by giving
	 *            null as this parameter.
	 */
	public void setFilterGenerator(FilterGenerator generator) {
		filterGenerator = generator;
	}

	/**
	 * Sets the Filter bar visible or hidden.
	 * 
	 * @param filtersVisible
	 *            true to set the Filter bar visible.
	 */
	public void setFilterBarVisible(boolean filtersVisible) {
		this.filtersVisible = filtersVisible;
		for (Object key : columnIdToFilterMap.keySet()) {
			columnIdToFilterMap.get(key).setParent(filtersVisible ? this : null);
		}
		reRenderFilterFields = true;
		markAsDirty();
	}

	/**
	 * Returns the current visibility state of the filter bar.
	 * 
	 * @return true if the filter bar is visible
	 */
	public boolean isFilterBarVisible() {
		return filtersVisible;
	}

	/**
	 * Toggles the visibility of the filter field defined for the give column
	 * ID.
	 * 
	 * @param columnId
	 *            Column/Property ID of the filter to toggle
	 * @param visible
	 *            true to set visible, false to set hidden
	 */
	public void setFilterFieldVisible(Object columnId, boolean visible) {
		Component component = columnIdToFilterMap.get(columnId);
		if (component != null) {
			component.setVisible(visible);
			reRenderFilterFields = true;
			markAsDirty();
		}
	}

	/**
	 * Returns visibility state of the filter field for the given column ID
	 * 
	 * @param columnId
	 *            Column/Property ID of the filter field to query
	 * @return true if filter is visible, false if it's hidden
	 */
	public boolean isFilterFieldVisible(Object columnId) {
		Component component = columnIdToFilterMap.get(columnId);
		if (component != null) {
			return component.isVisible();
		}
		return false;
	}

	/**
	 * Set a value of a filter field. Note that for Date filters you need to
	 * provide a value of {@link DateInterval} type.
	 * 
	 * @param propertyId
	 *            Property id for which to set the value
	 * @param value
	 *            New value
	 * @return true if setting succeeded, false if field was not found
	 * @throws ConversionException
	 *             exception from the underlying field
	 */
	public boolean setFilterFieldValue(Object propertyId, Object value) throws ConversionException {
		Component field = getColumnIdToFilterMap().get(propertyId);
		boolean retVal = field != null;
		if (field != null) {
			((AbstractField<?>) field).setConvertedValue(value);
		}
		return retVal;
	}

	/**
	 * Get the current value of a filter field
	 * 
	 * @param propertyId
	 *            Property id from which to get the value
	 * @return Current value
	 */
	public Object getFilterFieldValue(Object propertyId) {
		Component field = getColumnIdToFilterMap().get(propertyId);
		if (field != null) {
			return ((AbstractField<?>) field).getValue();
		} else {
			return null;
		}
	}

	/**
	 * Returns the filter component instance associated with the given property
	 * ID.
	 * 
	 * @param propertyId
	 *            Property id for which to find the filter component.
	 * @return Related component instance or null if not found.
	 */
	public Component getFilterField(Object propertyId) {
		return getColumnIdToFilterMap().get(propertyId);
	}

	@Override
	public Filterable getFilterable() {
		return getContainerDataSource() instanceof Filterable ? (Filterable) getContainerDataSource() : null;
	}

	@Override
	public FilterGenerator getFilterGenerator() {
		return filterGenerator;
	}

	@Override
	public FilterDecorator getFilterDecorator() {
		return decorator;
	}

	@Override
	public Map<Object, Component> getColumnIdToFilterMap() {
		return columnIdToFilterMap;
	}

	@Override
	public HasComponents getAsComponent() {
		return this;
	}

	@Override
	public Iterator<Component> iterator() {
		Set<Component> children = new HashSet<Component>();
		if (_visibleComponents != null) {
			children.addAll(_visibleComponents);
		}
		if (initDone && filtersVisible) {
			for (Object key : columnIdToFilterMap.keySet()) {
				Component filter = columnIdToFilterMap.get(key);
				if (equals(filter.getParent()) && filter.isVisible()) {
					children.add(filter);
				}
			}
		}
		return children.iterator();
	}

	@Override
	public void setVisibleColumns(Object... visibleColumns) {
		reRenderFilterFields = true;
		if (visibleColumns != null && columnIdToFilterMap != null) {
			/* First clear all parent references */
			for (Object key : columnIdToFilterMap.keySet()) {
				columnIdToFilterMap.get(key).setParent(null);
			}
			/* Set this as parent to visible columns */
			for (Object key : visibleColumns) {
				Component filter = columnIdToFilterMap.get(key);
				if (filter != null && isFilterBarVisible()) {
					filter.setParent(this);
				}
			}
		}
		super.setVisibleColumns(visibleColumns);
	}

	@Override
	public void setRefreshingEnabled(boolean enabled) {
		if (enabled) {
			enableContentRefreshing(true);
		} else {
			disableContentRefreshing();
		}
	}

	public void setFilterOnDemand(boolean filterOnDemand) {
		if (filtersRunOnDemand == filterOnDemand) {
			return;
		} else {
			filtersRunOnDemand = filterOnDemand;
			reRenderFilterFields = true;
			generator.setFilterOnDemandMode(filtersRunOnDemand);
		}

	}

	public boolean isFilterOnDemand() {
		return filtersRunOnDemand;
	}

	public void runFilters() {
		if (!filtersRunOnDemand) {
			throw new IllegalStateException("Can't run filters on demand when filtersRunOnDemand is set to false");
		}
		generator.runFiltersNow();
	}

	private Map<String, String> getColumnHeaderStylenamesForPaint() {
		String[] allStyleNames = getColumnHeaderStylenames();
		if (allStyleNames == null) {
			return null;
		}
		Map<String, String> stylenamesForPaint = new HashMap<>();
		Object[] visibleColumns = getVisibleColumns();

		for (int i = 0; i < allStyleNames.length; i++) {
			Object colId = visibleColumns[i];
			String stylename = allStyleNames[i];
			// don't add collapsed columns
			if (!collapsedColumnIds.contains(colId) && stylename != null) {
				stylenamesForPaint.put(_columnIdMap.key(colId), stylename);
			}
		}

		return stylenamesForPaint;

	}

	/**
	 * Gets the column filter wrapper style names of the columns.
	 * 
	 * @return an array of the column filter wrapper style names or null if there aren't
	 *         style names set.
	 */
	public String[] getColumnHeaderStylenames() {
		if (columnHeaderStylenames.size() == 0) {
			return null;
		}

		Object[] visibleColumns = getVisibleColumns();
		final String[] headerStylenames = new String[visibleColumns.length];

		for (int i = 0; i < visibleColumns.length; i++) {
			headerStylenames[i] = columnHeaderStylenames.get(visibleColumns[i]);
		}
		return headerStylenames;
	}

	/**
	 * Sets the column filter wrapper style names of columns.
	 * 
	 * @param headerStylenames
	 *            an array of the column filter wrapper style names that match the
	 *            {@link #getVisibleColumns()} method
	 */
	public void setColumnHeaderStylenames(String... headerStylenames) {
		Object[] visibleColumns = getVisibleColumns();

		if (headerStylenames.length != visibleColumns.length) {
			throw new IllegalArgumentException(
					"The length of the header style names array must match the number of visible columns");
		}

		columnHeaderStylenames.clear();
		for (int i = 0; i < visibleColumns.length; i++) {
			columnHeaderStylenames.put(visibleColumns[i], headerStylenames[i]);
		}

		markAsDirty();
	}

	/**
	 * Sets the column filter wrapper style name for the specified column.
	 * 
	 * @param propertyId
	 *            the propertyId identifying the column
	 * @param headerStylename
	 *            the column filter wrapper style name to set
	 */
	public void setColumnHeaderStylename(Object propertyId, String headerStylename) {

		if (headerStylename == null) {
			columnHeaderStylenames.remove(propertyId);
		} else {
			columnHeaderStylenames.put(propertyId, headerStylename);
		}

		markAsDirty();
	}

	/**
	 * Gets the column filter wrapper style name for the specified column.
	 * 
	 * @param propertyId
	 *            the propertyId identifying the column.
	 * @return the column filter wrapper style name for the specified column if it has one.
	 */
	public String getColumnHeaderStylename(Object propertyId) {
		if (getColumnHeaderMode() == ColumnHeaderMode.HIDDEN) {
			return null;
		}
		return columnHeaderStylenames.get(propertyId);
	}
	
	/**
	 * Get TimeZone, used for Date filters
	 * @return TimeZone of FilterTable
	 */
	public TimeZone getTimeZone() {
	        return timeZone;
	}

	/**
	 * Set TimeZone, used for Date filters
	 * @param timeZone TimeZone of FilterTable
	 */
	public void setTimeZone(TimeZone timeZone) {
	        this.timeZone = timeZone;
	        generator.setTimeZone(timeZone);
	}
}