package org.tepi.filtertable;

import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;

/**
 * Interface for decorating the UI of the filter components contained in
 * FilterTable. Implement this interface to provide proper display names and
 * icons for enums and booleans shown in the filter components.
 * 
 * @author Teppo Kurki
 * 
 */
public interface FilterDecorator {

    /**
     * Returns the filter display name for the given enum value when filtering
     * the given property id.
     * 
     * @param propertyId
     *            ID of the property the filter is attached to.
     * @param value
     *            Value of the enum the display name is requested for.
     * @return UI Display name for the enum value.
     */
    public String getEnumFilterDisplayName(Object propertyId, Object value);

    /**
     * Returns the filter icon for the given enum value when filtering the given
     * property id.
     * 
     * @param propertyId
     *            ID of the property the filter is attached to.
     * @param value
     *            Value of the enum the icon is requested for.
     * @return Resource for the icon of the enum value.
     */
    public Resource getEnumFilterIcon(Object propertyId, Object value);

    /**
     * Returns the filter display name for the given boolean value when
     * filtering the given property id.
     * 
     * @param propertyId
     *            ID of the property the filter is attached to.
     * @param value
     *            Value of boolean the display name is requested for.
     * @return UI Display name for the given boolean value.
     */
    public String getBooleanFilterDisplayName(Object propertyId, boolean value);

    /**
     * Returns the filter icon for the given boolean value when filtering the
     * given property id.
     * 
     * @param propertyId
     *            ID of the property the filter is attached to.
     * @param value
     *            Value of boolean the icon is requested for.
     * @return Resource for the icon of the given boolean value.
     */
    public Resource getBooleanFilterIcon(Object propertyId, boolean value);

    /**
     * Returns whether the text filter should update as the user types. This
     * uses {@link TextChangeEventMode#LAZY}
     * 
     * @return true if the text field should use a TextChangeListener.
     */
    public boolean isTextFilterImmediate(Object propertyId);

    /**
     * The text change timeout dictates how often text change events are
     * communicated to the application, and thus how often are the filter values
     * updated.
     * 
     * @return the timeout in milliseconds
     */
    public int getTextChangeTimeout(Object propertyId);

    /**
     * Return the string that should be used as an "input prompt" when no
     * filtering is made on a filter component.
     * 
     * @return String to show for no filter defined
     */
    public String getAllItemsVisibleString();

    /**
     * Defines whether a popup-style numeric filter should be used for the
     * property with the given ID.
     * 
     * The types Integer, Long, Float and Double are considered to be 'numeric'
     * within this context.
     * 
     * @param propertyId
     *            ID of the property the popup will be applied to
     * @return true to use popup-style, false to use a TextField
     */
    public boolean usePopupForNumericProperty(Object propertyId);
}
