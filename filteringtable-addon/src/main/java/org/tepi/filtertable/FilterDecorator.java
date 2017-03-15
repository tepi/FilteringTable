package org.tepi.filtertable;

import java.io.Serializable;
import java.util.Locale;

import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import com.vaadin.server.Resource;
import com.vaadin.v7.shared.ui.datefield.Resolution;
import com.vaadin.v7.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.v7.ui.DateField;

/**
 * Interface for decorating the UI of the filter components contained in
 * FilterTable. Implement this interface to provide proper display names and
 * icons for enums and booleans shown in the filter components.
 * 
 * @author Teppo Kurki
 * 
 */
@SuppressWarnings({ "deprecation" })
public interface FilterDecorator extends Serializable {

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

	/**
	 * Return DateField resolution for the Date filtering of the property ID.
	 * This will only be called for Date -typed properties. Filtering values
	 * output by the FilteringTable will also be truncated to this resolution.
	 * 
	 * @param propertyId
	 *            ID of the property the resolution will be applied to
	 * @return A resolution defined in {@link DateField}
	 */
	public Resolution getDateFieldResolution(Object propertyId);

	/**
	 * Returns a date format pattern to be used for formatting the date/time
	 * values shown in the filtering field of the given property ID. Note that
	 * this is completely independent from the resolution set for the property,
	 * and is used for display purposes only.
	 * 
	 * See SimpleDateFormat for the pattern definition
	 * 
	 * @param propertyId
	 *            ID of the property the format will be applied to
	 * @return A date format pattern or null to use the default formatting
	 */
	public String getDateFormatPattern(Object propertyId);

	/**
	 * Returns the locale to be used with Date filters. If none is provided,
	 * reverts to default locale of the system.
	 * 
	 * @return Desired locale for the dates
	 */
	public Locale getLocale();

	/**
	 * Return the string that should be used as an "input prompt" when no
	 * filtering is made on a filter component.
	 * 
	 * @return String to show for no filter defined
	 */
	public String getAllItemsVisibleString();

	/**
	 * Return configuration for the numeric filter field popup
	 * 
	 * @return Configuration for numeric filter
	 */
	public NumberFilterPopupConfig getNumberFilterPopupConfig();

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

	/**
	 * Returns the error message that is shown when an invalid number is entered
	 * into any of the fields in a number filter popup.
	 * 
	 * @return Error message
	 */
	public String getNumberValidationErrorMessage();
}
