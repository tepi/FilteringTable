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
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

class FilterFieldGenerator implements Serializable {
	private final IFilterTable owner;

	/* Mapping for property IDs, filters and components */
	private Map<Object, Filter> filters = new HashMap<Object, Container.Filter>();
	private Map<AbstractField, Object> customFields = new HashMap<AbstractField, Object>();
	private Map<NumberFilterPopup, Object> numbers = new HashMap<NumberFilterPopup, Object>();;
	private Map<TextField, Object> texts = new HashMap<TextField, Object>();
	private Map<ComboBox, Object> enums = new HashMap<ComboBox, Object>();
	private Map<ComboBox, Object> booleans = new HashMap<ComboBox, Object>();
	private Map<DateFilterPopup, Object> dates = new HashMap<DateFilterPopup, Object>();

	/* ValueChangeListener for filter components */
	private ValueChangeListener listener = initializeListener();

	FilterFieldGenerator(IFilterTable owner) {
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
					Component filter = createField(property, owner
							.getContainerDataSource().getType(property));
					addFilterColumn(property, filter);
				} else {
					addFilterColumn(property, createField(property, null));
				}
			}
		}
	}

	private Filter generateFilter(Property field, Object propertyId,
			Object value) {
		try {
			/* First try to get custom filter based on the field */
			if (owner.getFilterGenerator() != null && field instanceof Field) {
				Filter newFilter = owner.getFilterGenerator().generateFilter(
						propertyId, (Field) field);
				if (newFilter != null) {
					return newFilter;
				}
			}
			/* Use default filtering */
			if (field instanceof DateFilterPopup) {
				return generateDateFilter(field, propertyId, value);
			} else if (field instanceof NumberFilterPopup) {
				return generateNumberFilter(field, propertyId, value);
			} else if (value != null && !value.equals("")) {
				return generateGenericFilter(field, propertyId, value);
			}
			return null;
		} catch (Exception reason) {
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

	private Filter generateGenericFilter(Property field, Object propertyId,
			Object value) {
		/* Handle filtering for other data */
		if (owner.getFilterGenerator() != null) {
			Filter newFilter = owner.getFilterGenerator().generateFilter(
					propertyId, value);
			if (newFilter != null) {
				return newFilter;
			}
		}
		if (field instanceof ComboBox) {
			return new Equal(propertyId, value);
		} else {
			return new SimpleStringFilter(propertyId, String.valueOf(value),
					true, false);
		}
	}

	private Filter generateNumberFilter(Property field, Object propertyId,
			Object value) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		/* Handle number filtering */
		NumberInterval interval = ((NumberFilterPopup) field).getInterval();
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

		// We use reflection to get the valueOf method of the container
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

	private Filter generateDateFilter(Property field, Object propertyId,
			Object value) {
		/* Handle date filtering */
		DateInterval interval = ((DateFilterPopup) field).getDateValue();
		if (interval == null) {
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

	private Component createField(Object property, Class<?> type) {
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
		} else if (type == Integer.class || type == Long.class
				|| type == Float.class || type == Double.class
				|| type == Short.class || type == Byte.class
				|| type == int.class || type == long.class
				|| type == float.class || type == double.class
				|| type == short.class || type == byte.class) {
			if (owner.getFilterDecorator() != null
					&& owner.getFilterDecorator().usePopupForNumericProperty(
							property)) {
				NumberFilterPopup numField = createNumericField(type, property);
				numField.setWidth("100%");
				numField.setImmediate(true);
				numField.addListener(listener);
				return numField;
			} else {
				component = createTextField(property);
			}
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
				owner.getFilterDecorator(), propertyId);
		dates.put(dateFilterPopup, propertyId);
		return dateFilterPopup;
	}

	private NumberFilterPopup createNumericField(Class<?> type,
			Object propertyId) {
		NumberFilterPopup numberFilterPopup = new NumberFilterPopup(
				owner.getFilterDecorator(), null);
		numbers.put(numberFilterPopup, propertyId);
		return numberFilterPopup;
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
				} else if (numbers.containsKey(field)) {
					propertyId = numbers.get(field);
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
				/* If the owner is a PagedFilteringTable, move to the first page */
				if (owner instanceof PagedFilterTable<?>) {
					((PagedFilterTable<?>) owner).setCurrentPage(1);
				}
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

		public void requestRepaint();

		public FilterDecorator getFilterDecorator();

		public int getPageLength();

		public Component getAsComponent();

	}
}
