package org.tepi.filtertable.datefilter;

import java.util.Date;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Simple Filter used to filter Date properties
 * 
 * @author Teppo Kurki
 * 
 */
public class DateFilter implements Filter {
    private static final long serialVersionUID = -1568791220144011057L;
    private final DateInterval dateInterval;
    private final Object propertyId;

    public DateFilter(DateInterval dateInterval, Object propertyId) {
        this.dateInterval = dateInterval;
        this.propertyId = propertyId;
    }

    public boolean passesFilter(Object itemId, Item item)
            throws UnsupportedOperationException {
        if (item == null) {
            return false;
        }
        Property property = item.getItemProperty(propertyId);
        if (property == null || property.getValue() == null
                || !(property.getValue() instanceof Date)) {
            return false;
        }
        return dateInterval.isBetween((Date) property.getValue());
    }

    public boolean appliesToProperty(Object propertyId) {
        if (propertyId == null || this.propertyId == null) {
            return false;
        }
        return propertyId.equals(this.propertyId);
    }
}