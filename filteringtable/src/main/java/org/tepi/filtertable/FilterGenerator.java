package org.tepi.filtertable;

import com.vaadin.data.Container.Filter;

/**
 * Interface for generating custom Filters from values entered to the filtering
 * fields by the user.
 * 
 * @author tepi
 * 
 */
public interface FilterGenerator {

    /**
     * Generates a new Filter for the property with the given ID, using the
     * Value object as basis for the filtering.
     * 
     * @param propertyId
     *            ID of the filtered property.
     * @param value
     *            Value entered by the user. Type of the value will depend on
     *            the type this property has in the underlying container. Date,
     *            Boolean and enum types will be provided as themselves. All
     *            other types of properties will result in a String-typed
     *            filtering value.
     * @return A generated Filter object, or NULL if you want to allow
     *         FilterTable to generate the default Filter for this property.
     */
    public Filter generateFilter(Object propertyId, Object value);

}
