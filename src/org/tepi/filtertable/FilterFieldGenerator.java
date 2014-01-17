package org.tepi.filtertable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.numberfilter.NumberFilterPopup;
import org.tepi.filtertable.numberfilter.NumberInterval;
import org.tepi.filtertable.paged.PagedFilterTable;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
class FilterFieldGenerator implements Serializable {
    private final IFilterTable owner;

    /* Mapping for property IDs, filters and components */
    private final Map<Object, Filter> filters = new HashMap<Object, Container.Filter>();
    private final Map<AbstractField<?>, Object> customFields = new HashMap<AbstractField<?>, Object>();
    private final Map<TextField, Object> texts = new HashMap<TextField, Object>();
    private final Map<ComboBox, Object> enums = new HashMap<ComboBox, Object>();
    private final Map<ComboBox, Object> booleans = new HashMap<ComboBox, Object>();
    private final Map<DateFilterPopup, Object> dates = new HashMap<DateFilterPopup, Object>();
    private final Map<NumberFilterPopup, Object> numbers = new HashMap<NumberFilterPopup, Object>();

    /* ValueChangeListener for filter components */
    private final ValueChangeListener listener = initializeListener();

    FilterFieldGenerator(IFilterTable owner) {
        this.owner = owner;
    }

    void clearFilterData() {
        owner.setRefreshingEnabled(false);
        /* Remove all filters from container */
        for (Object propertyId : filters.keySet()) {
            owner.getFilterable()
                    .removeContainerFilter(filters.get(propertyId));
            if (owner.getFilterGenerator() != null) {
                owner.getFilterGenerator().filterRemoved(propertyId);
            }
        }
        /* Remove listeners */
        for (AbstractField<?> af : customFields.keySet()) {
            af.removeValueChangeListener(listener);
        }
        for (TextField tf : texts.keySet()) {
            tf.removeValueChangeListener(listener);
        }
        for (ComboBox cb : enums.keySet()) {
            cb.removeValueChangeListener(listener);
        }
        for (ComboBox cb : booleans.keySet()) {
            cb.removeValueChangeListener(listener);
        }
        for (DateFilterPopup dfp : dates.keySet()) {
            dfp.removeValueChangeListener(listener);
        }
        for (NumberFilterPopup nfp : numbers.keySet()) {
            nfp.removeValueChangeListener(listener);
        }
        /* Clear the data related to filters */
        customFields.clear();
        filters.clear();
        texts.clear();
        enums.clear();
        booleans.clear();
        dates.clear();
        numbers.clear();

        owner.setRefreshingEnabled(true);
    }

    void initializeFilterFields() {
        /* Create new filters only if Filterable */
        if (owner.getFilterable() != null) {
            for (Object property : owner.getVisibleColumns()) {
                if (owner.getContainerPropertyIds().contains(property)) {
                    Component filter = createField(property, owner
                            .getContainerDataSource().getType(property));
                    addFilterColumn(property, filter);
                } else {
                    addFilterColumn(property, createField(property, null));
                }
            }
        }
    }

    private Filter generateFilter(Property<?> field, Object propertyId,
            Object value) {
        try {
            /* First try to get custom filter based on the field */
            if (owner.getFilterGenerator() != null && field instanceof Field) {
                Filter newFilter = owner.getFilterGenerator().generateFilter(
                        propertyId, (Field<?>) field);
                if (newFilter != null) {
                    return newFilter;
                }
            }
            if (field instanceof NumberFilterPopup) {
                return generateNumberFilter(field, propertyId, value);
            } else if (field instanceof DateFilterPopup) {
                return generateDateFilter(field, propertyId, value);
            } else if (value != null && !value.equals("")) {
                return generateGenericFilter(field, propertyId, value);
            }
            return null;
        } catch (Exception reason) {
            /* Clear the field on failure during generating filter */
            field.setValue(null);
            /* Notify FilterGenerator, or re-throw if not available */
            if (owner.getFilterGenerator() != null) {
                return owner.getFilterGenerator().filterGeneratorFailed(reason,
                        propertyId, value);
            } else {
                throw new RuntimeException("Creating a filter for property '"
                        + propertyId + "' with value '" + value
                        + "'has failed.", reason);
            }
        }
    }

    private Filter generateGenericFilter(Property<?> field, Object propertyId,
            Object value) {
        /* Handle filtering for other data */
        if (owner.getFilterGenerator() != null) {
            Filter newFilter = owner.getFilterGenerator().generateFilter(
                    propertyId, value);
            if (newFilter != null) {
                return newFilter;
            }
        }
        /* Special handling for ComboBox (= enum properties) */
        if (field instanceof ComboBox) {
            return new Equal(propertyId, value);
        } else {
            return new SimpleStringFilter(propertyId, String.valueOf(value),
                    true, false);
        }
    }

    private Filter generateNumberFilter(Property<?> field, Object propertyId,
            Object value) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        /* Handle number filtering */
        NumberInterval interval = ((NumberFilterPopup) field).getValue();
        if (interval == null) {
            /* Number interval is empty -> no filter */
            return null;
        }
        if (owner.getFilterGenerator() != null) {
            Filter newFilter = owner.getFilterGenerator().generateFilter(
                    propertyId, interval);
            if (newFilter != null) {
                return newFilter;
            }
        }
        String ltValue = interval.getLessThanValue();
        String gtValue = interval.getGreaterThanValue();
        String eqValue = interval.getEqualsValue();
        Class<?> clazz = getProperNumericClass(propertyId);

        Method valueOf;

        // We use reflection to get the vaueOf method of the container
        // datatype
        valueOf = clazz.getMethod("valueOf", String.class);
        if (eqValue != null) {
            return new Compare.Equal(propertyId, valueOf.invoke(clazz, eqValue));
        } else if (ltValue != null && gtValue != null) {
            return new And(new Compare.Less(propertyId, valueOf.invoke(clazz,
                    ltValue)), new Compare.Greater(propertyId, valueOf.invoke(
                    clazz, gtValue)));
        } else if (ltValue != null) {
            return new Compare.Less(propertyId, valueOf.invoke(clazz, ltValue));
        } else if (gtValue != null) {
            return new Compare.Greater(propertyId, valueOf.invoke(clazz,
                    gtValue));
        }
        return null;
    }

    private Filter generateDateFilter(Property<?> field, Object propertyId,
            Object value) {
        /* Handle date filtering */
        DateInterval interval = ((DateFilterPopup) field).getValue();
        if (interval == null || interval.isNull()) {
            /* Date interval is empty -> no filter */
            return null;
        }
        /* Try to get a custom filter from a provided filter generator */
        if (owner.getFilterGenerator() != null) {
            Filter newFilter = owner.getFilterGenerator().generateFilter(
                    propertyId, interval);
            if (newFilter != null) {
                return newFilter;
            }
        }
        /* On failure we generate the default filter */
        Comparable<?> actualFrom = interval.getFrom(), actualTo = interval
                .getTo();
        Class<?> type = owner.getType(propertyId);
        if (java.sql.Date.class.equals(type)) {
            actualFrom = actualFrom == null ? null : new java.sql.Date(interval
                    .getFrom().getTime());
            actualTo = actualTo == null ? null : new java.sql.Date(interval
                    .getTo().getTime());
        } else if (Timestamp.class.equals(type)) {
            actualFrom = actualFrom == null ? null : new Timestamp(interval
                    .getFrom().getTime());
            actualTo = actualTo == null ? null : new Timestamp(interval.getTo()
                    .getTime());
        }
        if (actualFrom != null && actualTo != null) {
            return new Between(propertyId, actualFrom, actualTo);
        } else if (actualFrom != null) {
            return new Compare.GreaterOrEqual(propertyId, actualFrom);
        } else {
            return new Compare.LessOrEqual(propertyId, actualTo);
        }
    }

    private Class<?> getProperNumericClass(Object propertyId) {
        Class<?> clazz = owner.getContainerDataSource().getType(propertyId);
        if (clazz.equals(int.class)) {
            return Integer.class;
        }
        if (clazz.equals(long.class)) {
            return Long.class;
        }
        if (clazz.equals(float.class)) {
            return Float.class;
        }
        if (clazz.equals(double.class)) {
            return Double.class;
        }
        return clazz;
    }

    private void addFilterColumn(Object propertyId, Component filter) {
        owner.getColumnIdToFilterMap().put(propertyId, filter);
        filter.setParent(owner.getAsComponent());
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

    private Component createField(Object property, Class<?> type) {
        AbstractField<?> component = null;
        if (owner.getFilterGenerator() != null) {
            component = owner.getFilterGenerator().getCustomFilterComponent(
                    property);
        }
        if (component != null) {
            customFields.put(component, property);
        } else if (type == null) {
            component = new TextField();
            component.setWidth(100, Unit.PERCENTAGE);
            return component;
        } else if (type == boolean.class || type == Boolean.class) {
            component = createBooleanField(property);
        } else if (type.isEnum()) {
            component = createEnumField(type, property);
        } else if (type == Date.class || type == Timestamp.class
                || type == java.sql.Date.class) {
            DateFilterPopup dfp = createDateField(property);
            dfp.setWidth(100, Unit.PERCENTAGE);
            dfp.setImmediate(true);
            dfp.addValueChangeListener(listener);
            return dfp;
        } else if ((type == Integer.class || type == Long.class
                || type == Float.class || type == Double.class
                || type == Short.class || type == Byte.class
                || type == int.class || type == long.class
                || type == float.class || type == double.class
                || type == short.class || type == byte.class)
                && owner.getFilterDecorator() != null
                && owner.getFilterDecorator().usePopupForNumericProperty(
                        property)) {
            NumberFilterPopup nfp = createNumericField(type, property);
            nfp.setWidth(100, Unit.PERCENTAGE);
            nfp.setImmediate(true);
            nfp.addValueChangeListener(listener);
            return nfp;
        } else {
            component = createTextField(property);
        }
        component.setWidth(null);
        component.setImmediate(true);
        component.addValueChangeListener(listener);
        return component;
    }

    private AbstractField<?> createTextField(Object propertyId) {
        final TextField textField = new TextField();
        if (owner.getFilterDecorator() != null) {
            if (owner.getFilterDecorator().isTextFilterImmediate(propertyId)) {
                textField.addTextChangeListener(new TextChangeListener() {

                    @Override
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

    private AbstractField<?> createBooleanField(Object propertyId) {
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

    private DateFilterPopup createDateField(Object propertyId) {
        DateFilterPopup dateFilterPopup = new DateFilterPopup(
                owner.getFilterDecorator(), propertyId);
        dates.put(dateFilterPopup, propertyId);
        return dateFilterPopup;
    }

    private NumberFilterPopup createNumericField(Class<?> type,
            Object propertyId) {
        NumberFilterPopup numberFilterPopup = new NumberFilterPopup(
                owner.getFilterDecorator());
        numbers.put(numberFilterPopup, propertyId);
        return numberFilterPopup;
    }

    private ValueChangeListener initializeListener() {
        return new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (owner.getFilterable() == null) {
                    return;
                }
                Property<?> field = event.getProperty();
                Object value = field.getValue();
                Object propertyId = null;
                if (customFields.containsKey(field)) {
                    propertyId = customFields.get(field);
                } else if (texts.containsKey(field)) {
                    propertyId = texts.get(field);
                } else if (dates.containsKey(field)) {
                    propertyId = dates.get(field);
                } else if (numbers.containsKey(field)) {
                    propertyId = numbers.get(field);
                } else if (enums.containsKey(field)) {
                    propertyId = enums.get(field);
                } else if (booleans.containsKey(field)) {
                    propertyId = booleans.get(field);
                }

                owner.setRefreshingEnabled(false);

                // Generate a new filter
                Filter newFilter = generateFilter(field, propertyId, value);

                // Check if the filter is already set
                Filter possiblyExistingFilter = filters.get(propertyId);
                if (possiblyExistingFilter != null && newFilter != null
                        && possiblyExistingFilter.equals(newFilter)) {
                    return;
                }

                /* Remove the old filter and set the new filter */
                removeFilter(propertyId);
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
                 * If the owner is a PagedFilteringTable, move to the first page
                 */
                if (owner instanceof PagedFilterTable<?>) {
                    ((PagedFilterTable<?>) owner).setCurrentPage(1);
                }

                owner.setRefreshingEnabled(true);
            }
        };
    }

    interface IFilterTable {

        public Filterable getFilterable();

        public FilterGenerator getFilterGenerator();

        public Object[] getVisibleColumns();

        public Collection<?> getContainerPropertyIds();

        public Container getContainerDataSource();

        public Class<?> getType(Object propertyId);

        public Map<Object, Component> getColumnIdToFilterMap();

        public FilterDecorator getFilterDecorator();

        public int getPageLength();

        public HasComponents getAsComponent();

        public void setRefreshingEnabled(boolean enabled);

    }
}