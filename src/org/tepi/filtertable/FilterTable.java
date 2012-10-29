package org.tepi.filtertable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.gwt.client.ui.VFilterTable;
import org.tepi.filtertable.numberfilter.NumberFilterPopup;
import org.tepi.filtertable.numberfilter.NumberInterval;

import com.vaadin.data.Container;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * FilterTable is an extension of the Vaadin Table component that provides
 * automatically generated filter fields for each column.
 * 
 * @author Teppo Kurki
 * 
 */
@ClientWidget(VFilterTable.class)
public class FilterTable extends CustomTable {
    /* Maps property id's to column filter components */
    private Map<Object, Component> columnIdToFilterMap = new HashMap<Object, Component>();
    /* Internal list of currently collapsed column id:s */
    private Set<Object> collapsedColumnIds = new HashSet<Object>();
    /* Set to true to show the filter components */
    private boolean filtersVisible;
    /* Column IDs of hidden filter components */
    private Set<Object> columnIdsOfHiddenFilters = new HashSet<Object>();
    /* Filter Generator and Decorator */
    private FilterGenerator filterGenerator;
    private FilterDecorator decorator;
    /* Temporary reference to to-be-focused filter field */
    private Object filterToFocus;
    /* FilterFieldGenerator instance */
    private FilterFieldGenerator generator;
    /* Is initialization done */
    private boolean initDone;

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
    public FilterTable(String caption) {
        super(caption);
        generator = new FilterFieldGenerator(this);
        initDone = true;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        /* Add filter components to UIDL */
        target.startTag("filters");
        target.addAttribute("filtersvisible", filtersVisible);
        if (filtersVisible) {
            for (Object key : getColumnIdToFilterMap().keySet()) {
                /* Do not paint filters for collapsed columns */
                if (collapsedColumnIds.contains(key)) {
                    continue;
                }
                target.startTag("filtercomponent");
                target.addAttribute("columnid", columnIdMap.key(key));
                Component c = getColumnIdToFilterMap().get(key);
                // Paint labels instead of fields for generated columns and
                // hidden filters
                if (!getContainerDataSource().getContainerPropertyIds()
                        .contains(key)
                        || columnIdsOfHiddenFilters.contains(key)) {
                    c = new Label();
                    c.setSizeUndefined();
                }
                c.paint(target);
                target.endTag("filtercomponent");
            }
        }
        target.endTag("filters");
        /* Focus the previously focused filter component */
        if (filterToFocus != null) {
            Component filter = getColumnIdToFilterMap().get(filterToFocus);
            if (filter instanceof Focusable) {
                focusFilterComponent((Focusable) filter);
            }
            filterToFocus = null;
        }
    }

    @Override
    public void setColumnCollapsed(Object propertyId, boolean collapsed)
            throws IllegalStateException {
        super.setColumnCollapsed(propertyId, collapsed);
        Component c = getColumnIdToFilterMap().get(propertyId);
        if (collapsed) {
            collapsedColumnIds.add(propertyId);
            if (c != null) {
                if (c instanceof TextField) {
                    ((TextField) c).setValue("");
                } else if (c instanceof DateFilterPopup) {
                    ((DateFilterPopup) c).setInternalValue(null, null);
                } else if (c instanceof NumberFilterPopup) {
                    ((NumberFilterPopup) c).setInternalValue(null, null, null);
                } else if (c instanceof Field) {
                    ((Field) c).setValue(null);
                }
            }
        } else {
            collapsedColumnIds.remove(propertyId);
        }
        requestRepaint();
    }

    @Override
    public void detach() {
        for (Component c : getColumnIdToFilterMap().values()) {
            c.detach();
        }
        super.detach();
    }

    @Override
    public void attach() {
        for (Component c : getColumnIdToFilterMap().values()) {
            c.attach();
        }
        super.attach();
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        super.setContainerDataSource(newDataSource);
        resetFilters();
    }

    /**
     * Resets all filters.
     * 
     * Note: Recreates the filter fields also!
     */
    public void resetFilters() {
        if (initDone) {
            collapsedColumnIds.clear();
            columnIdToFilterMap.clear();
            columnIdsOfHiddenFilters.clear();
            generator.clearFilterData();
            generator.initializeFilterFields();
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
        requestRepaint();
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
        int previousSize = columnIdsOfHiddenFilters.size();
        if (visible) {
            columnIdsOfHiddenFilters.remove(columnId);
        } else {
            columnIdsOfHiddenFilters.add(columnId);
        }
        if (columnIdsOfHiddenFilters.size() != previousSize) {
            requestRepaint();
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
        /* Return false for generated columns */
        if (!getContainerDataSource().getContainerPropertyIds().contains(
                columnId)) {
            return false;
        }
        return !columnIdsOfHiddenFilters.contains(columnId);
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
    public boolean setFilterFieldValue(String propertyId, Object value)
            throws ConversionException {
        AbstractField field = (AbstractField) getColumnIdToFilterMap().get(
                propertyId);
        boolean retVal = field != null;
        if (field != null) {
            if (field instanceof DateFilterPopup
                    && value instanceof DateInterval) {
                ((DateFilterPopup) field).setInternalValue(
                        ((DateInterval) value).getFrom(),
                        ((DateInterval) value).getTo());
            } else if (field instanceof NumberFilterPopup
                    && value instanceof NumberInterval) {
                ((NumberFilterPopup) field).setInternalValue(
                        ((NumberInterval) value).getLtValue(),
                        ((NumberInterval) value).getGtValue(),
                        ((NumberInterval) value).getEqValue());
            } else {
                field.setValue(value);
            }
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
    public Object getFilterFieldValue(String propertyId) {
        AbstractField field = (AbstractField) getColumnIdToFilterMap().get(
                propertyId);
        if (field != null) {
            if (field instanceof DateFilterPopup) {
                return ((DateFilterPopup) field).getDateValue();
            } else if (field instanceof NumberFilterPopup) {
                return ((NumberFilterPopup) field).getInterval();
            } else {
                return field.getValue();
            }
        } else {
            return null;
        }
    }

    Filterable getFilterable() {
        return getContainerDataSource() instanceof Filterable ? (Filterable) getContainerDataSource()
                : null;
    }

    FilterGenerator getFilterGenerator() {
        return filterGenerator;
    }

    FilterDecorator getFilterDecorator() {
        return decorator;
    }

    void focusFilter(Focusable toFocus) {
        super.focusFilterComponent(toFocus);
    }

    void setFilterToFocus(Object propertyId) {
        filterToFocus = propertyId;
    }

    Map<Object, Component> getColumnIdToFilterMap() {
        return columnIdToFilterMap;
    }
}
