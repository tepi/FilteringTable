package org.tepi.filtertable;

import java.util.Locale;

import com.vaadin.terminal.Resource;

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
     * Returns the preferred Locale for the DateFilter component
     * 
     * @return Preferred locale
     */
    public Locale getLocale();

    /**
     * Return display caption for the From field
     * 
     * @return caption for From field
     */
    public String getFromCaption();

    /**
     * Return display caption for the To field
     * 
     * @return caption for To field
     */
    public String getToCaption();

    /**
     * Return display caption for the Set button
     * 
     * @return caption for Set button
     */
    public String getSetCaption();

    /**
     * Return display caption for the Clear button
     * 
     * @return caption for Clear button
     */
    public String getClearCaption();
}