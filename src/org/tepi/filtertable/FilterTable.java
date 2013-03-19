package org.tepi.filtertable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.tepi.filtertable.FilterFieldGenerator.IFilterTable;

import com.ibm.icu.util.DateInterval;
import com.vaadin.data.Container;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.LegacyPaint;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.TextField;

/**
 * FilterTable is an extension of the Vaadin Table component that provides
 * automatically generated filter fields for each column.
 * 
 * @author Teppo Kurki
 * 
 */
public class FilterTable extends CustomTable implements IFilterTable {
    /* Maps property id's to column filter components */
    private Map<Object, Component> columnIdToFilterMap = new HashMap<Object, Component>();
    /* Internal list of currently collapsed column id:s */
    private Set<Object> collapsedColumnIds = new HashSet<Object>();
    /* Set to true to show the filter components */
    private boolean filtersVisible;
    /* Filter Generator and Decorator */
    private FilterGenerator filterGenerator;
    private FilterDecorator decorator;
    /* FilterFieldGenerator instance */
    private FilterFieldGenerator generator;
    /* Is initialization done */
    private boolean initDone;
    /* Force-render filter fields */
    private boolean reRenderFilterFields;

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
        target.addAttribute("forceRender", reRenderFilterFields);
        reRenderFilterFields = false;
        if (filtersVisible) {
            for (Object key : getColumnIdToFilterMap().keySet()) {
                /* Do not paint filters for collapsed columns */
                /* Do not paint filters for cols that do not exist in container */
                if (collapsedColumnIds.contains(key)
                        || !getContainerDataSource().getContainerPropertyIds()
                                .contains(key)) {
                    continue;
                }
                /* Paint the filter field */
                target.startTag("filtercomponent-" + columnIdMap.key(key));
                target.addAttribute("columnid", columnIdMap.key(key));
                Component c = getColumnIdToFilterMap().get(key);
                LegacyPaint.paint(c, target);
                target.endTag("filtercomponent-" + columnIdMap.key(key));
            }
        }
        target.endTag("filters");
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
                } else if (c instanceof AbstractField<?>) {
                    ((AbstractField<?>) c).setValue(null);
                }
            }
        } else {
            collapsedColumnIds.remove(propertyId);
        }
        markAsDirty();
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
            for (Component c : columnIdToFilterMap.values()) {
                c.setParent(null);
            }
            collapsedColumnIds.clear();
            columnIdToFilterMap.clear();
            generator.clearFilterData();
            generator.initializeFilterFields();
            reRenderFilterFields = true;
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
    public boolean setFilterFieldValue(Object propertyId, Object value)
            throws ConversionException {
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

    public Filterable getFilterable() {
        return getContainerDataSource() instanceof Filterable ? (Filterable) getContainerDataSource()
                : null;
    }

    public FilterGenerator getFilterGenerator() {
        return filterGenerator;
    }

    public FilterDecorator getFilterDecorator() {
        return decorator;
    }

    public Map<Object, Component> getColumnIdToFilterMap() {
        return columnIdToFilterMap;
    }

    public Component getAsComponent() {
        return this;
    }

    @Override
    public Iterator<Component> iterator() {
        Set<Component> children = new HashSet<Component>();
        if (visibleComponents != null) {
            children.addAll(visibleComponents);
        }
        if (initDone) {
            for (Object key : columnIdToFilterMap.keySet()) {
                children.add(columnIdToFilterMap.get(key));
            }
        }
        return children.iterator();
    }
}