package org.tepi.filtertable.datefilter;

import java.util.Locale;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.DateField;

public class DateFilterPopupConfig {

    private String fromCaption;
    private String toCaption;
    private String setCaption;
    private String clearCaption;

    private Resolution resolution;

    private String dateFormatPattern;

    private Locale locale;

    /**
     * Return display caption for the From field
     * 
     * @return caption for From field
     */
    public String getFromCaption() {
        return fromCaption;
    }

    public void setFromCaption(String fromCaption) {
        this.fromCaption = fromCaption;
    }

    /**
     * Return display caption for the To field
     * 
     * @return caption for To field
     */
    public String getToCaption() {
        return toCaption;
    }

    public void setToCaption(String toCaption) {
        this.toCaption = toCaption;
    }

    /**
     * Return display caption for the Set button
     * 
     * @return caption for Set button
     */
    public String getSetCaption() {
        return setCaption;
    }

    public void setSetCaption(String setCaption) {
        this.setCaption = setCaption;
    }

    /**
     * Return display caption for the Clear button
     * 
     * @return caption for Clear button
     */
    public String getClearCaption() {
        return clearCaption;
    }

    public void setClearCaption(String clearCaption) {
        this.clearCaption = clearCaption;
    }

    /**
     * Return DateField resolution for the Date filtering. This will only be
     * called for Date -typed properties. Filtering values output by the
     * FilteringTable will also be truncated to this resolution.
     * 
     * @return A resolution defined in {@link DateField}
     */
    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * Returns a date format pattern to be used for formatting the date/time
     * values shown in the filtering field. Note that this is completely
     * independent from the resolution set for the property, and is used for
     * display purposes only.
     * 
     * See SimpleDateFormat for the pattern definition
     * 
     * @return A date format pattern or null to use the default formatting
     */
    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public void setDateFormatPattern(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the locale to be used with Date filters. If none is provided,
     * reverts to default locale of the system.
     * 
     * @return Desired locale for the dates
     */
    public Locale getLocale() {
        return locale;
    }
}
