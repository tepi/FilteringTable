package org.tepi.filtertable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.datefilter.DateInterval;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.TextField;

class FilterFieldGenerator {
    private final FilterTable owner;

    /* Mapping for property IDs, filters and components */
    private Map<Object, Filter> filters = new HashMap<Object, Container.Filter>();
    private Map<AbstractField, Object> customFields = new HashMap<AbstractField, Object>();
    private Map<TextField, Object> texts = new HashMap<TextField, Object>();
    private Map<ComboBox, Object> enums = new HashMap<ComboBox, Object>();
    private Map<ComboBox, Object> booleans = new HashMap<ComboBox, Object>();
    private Map<DateFilterPopup, Object> dates = new HashMap<DateFilterPopup, Object>();

    /* ValueChangeListener for filter components */
    private ValueChangeListener listener = initializeListener();

    FilterFieldGenerator(FilterTable owner) {
        this.owner = owner;
    }

    void clearFilterData() {
        /* Remove all filters from container */
        for (Object propertyId : filters.keySet()) {
            owner.getFilterable()
                    .removeContainerFilter(filters.get(propertyId));
            if (owner.getFilterGenerator() != null) {
                owner.getFilterGenerator().filterRemoved(propertyId);
            }
        }
        /* Clear the data related to filters */
        customFields.clear();
        filters.clear();
        texts.clear();
        enums.clear();
        booleans.clear();
        dates.clear();
    }

    void initializeFilterFields() {
        /* Create new filters only if Filterable */
        if (owner.getFilterable() != null) {
            for (Object property : owner.getVisibleColumns()) {
                if (owner.getContainerPropertyIds().contains(property)) {
                    AbstractField filter = createField(property, owner
                            .getContainerDataSource().getType(property));
                    addFilterColumn(property, filter);
                } else {
                    addFilterColumn(property, createField(null, null));
                }
            }
        }
    }

    private Filter generateFilter(Property field, Object propertyId,
            Object value) {
        if (field instanceof DateFilterPopup) {
            /* Handle date filtering */
            DateInterval interval = ((DateFilterPopup) field).getDateValue();
            if (interval == null) {
                /* Date interval is empty -> no filter */
                return null;
            }
            if (owner.getFilterGenerator() != null) {
                Filter newFilter = owner.getFilterGenerator().generateFilter(
                        propertyId, interval);
                if (newFilter != null) {
                    return newFilter;
                }
            }
            Date from = interval.getFrom();
            Date to = interval.getTo();
            if (from != null && to != null) {
                return new Between(propertyId, from, to);
            } else if (from != null) {
                return new Compare.GreaterOrEqual(propertyId, from);
            } else {
                return new Compare.LessOrEqual(propertyId, to);
            }
        } else if (value != null && !value.equals("")) {
            /* Handle filtering for other data */
            if (owner.getFilterGenerator() != null) {
                Filter newFilter = owner.getFilterGenerator().generateFilter(
                        propertyId, value);
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
        owner.getColumnIdToFilterMap().put(propertyId, filter);
        filter.setParent(owner);
        owner.requestRepaint();
    }

    private void removeFilter(Object propertyId) {
        if (filters.get(propertyId) != null) {
            owner.getFilterable()
                    .removeContainerFilter(filters.get(propertyId));
            filters.remove(propertyId);
        }
    }

    private void setFilter(Filter filter, Object propertyId) {
        owner.getFilterable().addContainerFilter(filter);
        filters.put(propertyId, filter);
    }

    private AbstractField createField(Object property, Class<?> type) {
        AbstractField component = null;
        if (owner.getFilterGenerator() != null) {
            component = owner.getFilterGenerator().getCustomFilterComponent(
                    property);
        }
        if (component != null) {
            customFields.put(component, property);
        } else if (type == null) {
            component = new TextField();
            component.setWidth("100%");
            return component;
        } else if (type == boolean.class || type == Boolean.class) {
            component = createBooleanField(property);
        } else if (type.isEnum()) {
            component = createEnumField(type, property);
        } else if (type == Date.class || type == Timestamp.class
                || type == java.sql.Date.class) {
            component = createDateField(property);
        } else {
            component = createTextField(property);
        }
        component.setWidth("100%");
        component.setImmediate(true);
        component.addListener(listener);
        return component;
    }

    private AbstractField createTextField(Object propertyId) {
        final TextField textField = new TextField();
        if (owner.getFilterDecorator() != null) {
            if (owner.getFilterDecorator().isTextFilterImmediate(propertyId)) {
                textField.addListener(new TextChangeListener() {

                    public void textChange(TextChangeEvent event) {
                        textField.setValue(event.getText());
                    }
                });
                textField.setTextChangeTimeout(owner.getFilterDecorator()
                        .getTextChangeTimeout(propertyId));
            }
            if (owner.getFilterDecorator().getAllItemsVisibleString() != null) {
                textField.setInputPrompt(owner.getFilterDecorator()
                        .getAllItemsVisibleString());
            }
        }
        texts.put(textField, propertyId);
        return textField;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private AbstractField createEnumField(Class<?> type, Object propertyId) {
        ComboBox enumSelect = new ComboBox();
        /* Add possible 'view all' item */
        if (owner.getFilterDecorator() != null
                && owner.getFilterDecorator().getAllItemsVisibleString() != null) {
            Object nullItem = enumSelect.addItem();
            enumSelect.setNullSelectionItemId(nullItem);
            enumSelect.setItemCaption(nullItem, owner.getFilterDecorator()
                    .getAllItemsVisibleString());
        }
        /* Add items from enumeration */
        for (Object o : EnumSet.allOf((Class<Enum>) type)) {
            enumSelect.addItem(o);
            if (owner.getFilterDecorator() != null) {
                String caption = owner.getFilterDecorator()
                        .getEnumFilterDisplayName(propertyId, o);
                enumSelect.setItemCaption(o, caption == null ? o.toString()
                        : caption);
                Resource icon = owner.getFilterDecorator().getEnumFilterIcon(
                        propertyId, o);
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
        if (owner.getFilterDecorator() != null) {
            /* Add possible 'view all' item */
            if (owner.getFilterDecorator().getAllItemsVisibleString() != null) {
                Object nullItem = booleanSelect.addItem();
                booleanSelect.setNullSelectionItemId(nullItem);
                booleanSelect.setItemCaption(nullItem, owner
                        .getFilterDecorator().getAllItemsVisibleString());
            }
            String caption = owner.getFilterDecorator()
                    .getBooleanFilterDisplayName(propertyId, true);
            booleanSelect.setItemCaption(true, caption == null ? "true"
                    : caption);
            Resource icon = owner.getFilterDecorator().getBooleanFilterIcon(
                    propertyId, true);
            if (icon != null) {
                booleanSelect.setItemIcon(true, icon);
            }
            caption = owner.getFilterDecorator().getBooleanFilterDisplayName(
                    propertyId, false);
            booleanSelect.setItemCaption(false, caption == null ? "false"
                    : caption);
            icon = owner.getFilterDecorator().getBooleanFilterIcon(propertyId,
                    false);
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
        DateFilterPopup dateFilterPopup = new DateFilterPopup(
                owner.getFilterDecorator(), null);
        dates.put(dateFilterPopup, propertyId);
        return dateFilterPopup;
    }

    private ValueChangeListener initializeListener() {
        return new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (owner.getFilterable() == null) {
                    return;
                }
                Property field = event.getProperty();
                Object value = field.getValue();
                Object propertyId = null;
                if (customFields.containsKey(field)) {
                    propertyId = customFields.get(field);
                } else if (texts.containsKey(field)) {
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
                    if (owner.getFilterGenerator() != null) {
                        owner.getFilterGenerator().filterAdded(propertyId,
                                newFilter.getClass(), value);
                    }
                } else {
                    if (owner.getFilterGenerator() != null) {
                        owner.getFilterGenerator().filterRemoved(propertyId);
                    }
                }
                /*
                 * Handle focusing. Note: The size comparison is an ugly hack
                 * due to some focusing behavior within the VScrollTable which I
                 * could not understand :).
                 */
                if (owner.getPageLength() <= owner.getContainerDataSource()
                        .size()) {
                    owner.setFilterToFocus(propertyId);
                } else {
                    Component filter = owner.getColumnIdToFilterMap().get(
                            propertyId);
                    if (filter instanceof Focusable) {
                        owner.focusFilter((Focusable) filter);
                    }
                }
            }
        };
    }
}
