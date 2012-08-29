package org.tepi.filtertable;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.tepi.filtertable.datefilter.DateFilter;
import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.gwt.client.ui.VFilterTable;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * FilterTable is an extension of the Vaadin Table component that provides
 * automatically generated filter fields for each column.
 * 
 * @author Teppo Kurki
 * 
 */
@SuppressWarnings("serial")
@ClientWidget(VFilterTable.class)
public class FilterTable extends CustomTable {

    /* Maps property id's to column filter components */
    private Map<String, Component> columnIdToFilterMap = new HashMap<String, Component>();
    /* Internal list of currently collapsed column id:s */
    private Set<Object> collapsedColumnIds = new HashSet<Object>();
    /* Set to true to show the filter components */
    private boolean filtersVisible;

    /* Mapping for property IDs, filters and components */
    private Map<Object, Filter> filters = new HashMap<Object, Filter>();
    private Map<TextField, Object> texts = new HashMap<TextField, Object>();
    private Map<ComboBox, Object> enums = new HashMap<ComboBox, Object>();
    private Map<ComboBox, Object> booleans = new HashMap<ComboBox, Object>();
    private Map<DateFilterPopup, Object> dates = new HashMap<DateFilterPopup, Object>();
    private FilterGenerator filterGenerator;

    private FilterDecorator decorator;

    private Object filterToFocus;

    public FilterTable() {
        super();
    }

    public FilterTable(String caption, Container datasource) {
        super(caption, datasource);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        /* Add filter components to UIDL */
        target.startTag("filters");
        target.addAttribute("filtersvisible", filtersVisible);
        if (filtersVisible) {
            for (String key : columnIdToFilterMap.keySet()) {
                /* Do not paint filters for collapsed columns */
                if (collapsedColumnIds.contains(key)) {
                    continue;
                }
                target.startTag("filtercomponent");
                target.addAttribute("columnid", columnIdMap.key(key));
                columnIdToFilterMap.get(key).paint(target);
                target.endTag("filtercomponent");
            }
        }
        target.endTag("filters");
        /* Focus the previously focused filter component */
        if (filterToFocus != null) {
            Component filter = columnIdToFilterMap.get(filterToFocus);
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
        Component c = columnIdToFilterMap.get(propertyId);
        if (collapsed) {
            collapsedColumnIds.add(propertyId);
            if (c != null) {
                if (c instanceof TextField) {
                    ((TextField) c).setValue("");
                } else if (c instanceof DateFilterPopup) {
                    ((DateFilterPopup) c).setInternalValue(null, null);
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
        for (Component c : columnIdToFilterMap.values()) {
            c.detach();
        }
        super.detach();
    }

    @Override
    public void attach() {
        for (Component c : columnIdToFilterMap.values()) {
            c.attach();
        }
        super.attach();
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        clearFilterData();
        super.setContainerDataSource(newDataSource);
        initializeFilterFields(getContainerDataSource());
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
        clearFilterData();
        initializeFilterFields(getContainerDataSource());
    }

    /**
     * Sets the Filter bar visible or hidden.
     * 
     * @param filtersVisible
     *            true to set the Filter bar visible.
     */
    public void setFiltersVisible(boolean filtersVisible) {
        this.filtersVisible = filtersVisible;
        requestRepaint();
    }

    /**
     * Returns the current visibility state of the filter bar.
     * 
     * @return true if the filter bar is visible
     */
    public boolean isFiltersVisible() {
        return filtersVisible;
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
     * Resets all filters.
     * 
     * Note: Recreates the filter fields also!
     */
    public void resetFilters() {
        clearFilterData();
        initializeFilterFields(getContainerDataSource());
    }

    /* ValueChangeListener for filter components */
    private ValueChangeListener listener = new Property.ValueChangeListener() {
        public void valueChange(Property.ValueChangeEvent event) {
            if (!(getContainerDataSource() instanceof Filterable)) {
                return;
            }
            Property field = event.getProperty();
            Object value = field.getValue();
            Object propertyId = null;
            if (texts.containsKey(field)) {
                propertyId = texts.get(field);
            } else if (dates.containsKey(field)) {
                propertyId = dates.get(field);
            } else if (enums.containsKey(field)) {
                propertyId = enums.get(field);
            } else if (booleans.containsKey(field)) {
                propertyId = booleans.get(field);
            }
            removeFilter(propertyId);
            /* Generate and set a new filter */
            Filter newFilter = generateFilter(field, propertyId, value);
            if (newFilter != null) {
                setFilter(newFilter, propertyId);
            }
            /*
             * Handle focusing. Note: The size comparison is an ugly hack due to
             * some focusing behavior within the VScrollTable which I could not
             * understand :).
             */
            if (getPageLength() <= getContainerDataSource().size()) {
                filterToFocus = propertyId;
            } else {
                Component filter = columnIdToFilterMap.get(propertyId);
                if (filter instanceof Focusable) {
                    focusFilterComponent((Focusable) filter);
                }
            }
        }
    };

    private Filter generateFilter(Property field, Object propertyId,
            Object value) {
        if (field instanceof DateFilterPopup) {
            /* Handle date filtering */
            DateInterval interval = ((DateFilterPopup) field).getDateValue();
            if (interval == null) {
                /* Date interval is empty -> no filter */
                return null;
            }
            if (filterGenerator != null) {
                Filter newFilter = filterGenerator.generateFilter(propertyId,
                        value);
                if (newFilter != null) {
                    return newFilter;
                }
            }
            return new DateFilter(interval, propertyId);
        } else if (value != null && !value.equals("")) {
            /* Handle filtering for other data */
            if (filterGenerator != null) {
                Filter newFilter = filterGenerator.generateFilter(propertyId,
                        value);
                if (newFilter != null) {
                    return newFilter;
                }
            }
            return new SimpleStringFilter(propertyId, String.valueOf(value),
                    true, false);
        }
        /* Value is null or empty -> no filter */
        return null;
    }

    private void addFilterColumn(Object propertyId, Component filter) {
        columnIdToFilterMap.put(String.valueOf(propertyId), filter);
        filter.setParent(this);
        requestRepaint();
    }

    private void clearFilterData() {
        if (filters != null) {
            /* Remove all filters from container */
            for (Object propertyId : filters.keySet()) {
                ((Filterable) getContainerDataSource())
                        .removeContainerFilter(filters.get(propertyId));
            }
            /* Clear the data related to filters */
            columnIdToFilterMap.clear();
            collapsedColumnIds.clear();
            // filtersVisible = false;
            filters.clear();
            texts.clear();
            enums.clear();
            booleans.clear();
            dates.clear();
        }
    }

    private void initializeFilterFields(Container newDataSource) {
        if (newDataSource instanceof Container.Filterable) {
            /* Create new filters only if Filterable */
            for (Object property : getVisibleColumns()) {
                if (getContainerPropertyIds().contains(property)) {
                    AbstractField filter = createField(property,
                            newDataSource.getType(property));
                    addFilterColumn(property, filter);
                } else {
                    addFilterColumn(property, createField(null, null));
                }
            }
        }
    }

    private void removeFilter(Object propertyId) {
        ((Filterable) getContainerDataSource()).removeContainerFilter(filters
                .get(propertyId));
        filters.remove(propertyId);
    }

    private void setFilter(Filter filter, Object propertyId) {
        ((Filterable) getContainerDataSource()).addContainerFilter(filter);
        filters.put(propertyId, filter);
    }

    private AbstractField createField(Object property, Class<?> type) {
        AbstractField component = null;
        if (type == boolean.class || type == Boolean.class) {
            component = createBooleanField(property);
        } else if (type.isEnum()) {
            component = createEnumField(type, property);
        } else if (type == Date.class || type == Timestamp.class
                || type == java.sql.Date.class) {
            component = createDateField(property);
        } else {
            component = type == null ? new TextField()
                    : createTextField(property);
        }
        component.setWidth("100%");
        if (type != null) {
            component.setImmediate(true);
            component.addListener(listener);
        }
        return component;
    }

    private AbstractField createTextField(Object propertyId) {
        final TextField textField = new TextField();
        if (decorator != null && decorator.isTextFilterImmediate(propertyId)) {
            textField.addListener(new TextChangeListener() {

                public void textChange(TextChangeEvent event) {
                    textField.setValue(event.getText());
                }
            });
            textField.setTextChangeTimeout(decorator
                    .getTextChangeTimeout(propertyId));
        }
        texts.put(textField, propertyId);
        return textField;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private AbstractField createEnumField(Class<?> type, Object propertyId) {
        ComboBox enumSelect = new ComboBox();
        for (Object o : EnumSet.allOf((Class<Enum>) type)) {
            enumSelect.addItem(o);
            if (decorator != null) {
                String caption = decorator.getEnumFilterDisplayName(propertyId,
                        o);
                enumSelect.setItemCaption(o, caption == null ? o.toString()
                        : caption);
                Resource icon = decorator.getEnumFilterIcon(propertyId, o);
                if (icon != null) {
                    enumSelect.setItemIcon(o, icon);
                }
            } else {
                enumSelect.setItemCaption(o, o.toString());
            }
        }
        enums.put(enumSelect, propertyId);
        return enumSelect;
    }

    private AbstractField createBooleanField(Object propertyId) {
        ComboBox booleanSelect = new ComboBox();
        booleanSelect.addItem(true);
        booleanSelect.addItem(false);
        if (decorator != null) {
            String caption = decorator.getBooleanFilterDisplayName(propertyId,
                    true);
            booleanSelect.setItemCaption(true, caption == null ? "true"
                    : caption);
            Resource icon = decorator.getBooleanFilterIcon(propertyId, true);
            if (icon != null) {
                booleanSelect.setItemIcon(true, icon);
            }
            caption = decorator.getBooleanFilterDisplayName(propertyId, false);
            booleanSelect.setItemCaption(false, caption == null ? "false"
                    : caption);
            icon = decorator.getBooleanFilterIcon(propertyId, false);
            if (icon != null) {
                booleanSelect.setItemIcon(false, icon);
            }
        } else {
            booleanSelect.setItemCaption(true, "true");
            booleanSelect.setItemCaption(false, "false");
        }
        booleans.put(booleanSelect, propertyId);
        return booleanSelect;
    }

    private AbstractField createDateField(Object propertyId) {
        DateFilterPopup dateFilterPopup = new DateFilterPopup(decorator, null);
        dates.put(dateFilterPopup, propertyId);
        return dateFilterPopup;
    }

	public Map<String, Component> getColumnIdToFilterMapCopy() {
		return Collections.unmodifiableMap(columnIdToFilterMap);
	}
    
    
}