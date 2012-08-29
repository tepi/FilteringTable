package org.tepi.filtertable.demo;

import org.tepi.filtertable.FilterGenerator;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;

public class DemoFilterGenerator implements FilterGenerator {

    public Filter generateFilter(Object propertyId, Object value) {
        if ("id".equals(propertyId)) {
            /* Create an 'equals' filter for the ID field */
            if (value != null && value instanceof String) {
                try {
                    return new Compare.Equal(propertyId,
                            Integer.parseInt((String) value));
                } catch (NumberFormatException ignored) {
                    // If no integer was entered, just generate default filter
                }
            }
        }
        // For other properties, use the default filter
        return null;
    }

}
