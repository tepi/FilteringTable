package org.tepi.filtertable;

import java.io.Serializable;

import com.vaadin.data.Container.Filter;
import com.vaadin.ui.AbstractField;

/**
 * Interface for generating custom Filters from values entered to the filtering
 * fields by the user.
 * 
 * @author tepi
 * 
 */
public interface FilterGenerator extends Serializable {

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

    /**
     * Allows you to provide a custom filtering field for the properties as
     * needed.
     * 
     * @param propertyId
     *            ID of the property for for which the field is asked for
     * @return a custom filtering field OR null if you want to use the generated
     *         default field.
     */
    public AbstractField getCustomFilterComponent(Object propertyId);

    /**
     * This method is called when a filter has been removed from the container
     * by the FilterTable.
     * 
     * @param propertyId
     *            ID of the property from which the filter was removed
     */
    public void filterRemoved(Object propertyId);

    /**
     * This method is called when a filter has been added to the container by
     * the FilterTable
     * 
     * @param propertyId
     *            ID of the property to which the filter was added
     * @param filterType
     *            Type of the filter (class)
     * @param value
     *            Value the filter was based on
     */
    public void filterAdded(Object propertyId,
            Class<? extends Filter> filterType, Object value);

}
