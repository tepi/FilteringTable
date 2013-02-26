package org.tepi.filtertable.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.demo.FiltertabledemoApplication.State;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.DateField;

class DemoFilterDecorator implements FilterDecorator {

    public String getEnumFilterDisplayName(Object propertyId, Object value) {
        if ("state".equals(propertyId)) {
            State state = (State) value;
            switch (state) {
            case CREATED:
                return "Order has been created";
            case PROCESSING:
                return "Order is being processed";
            case PROCESSED:
                return "Order has been processed";
            case FINISHED:
                return "Order is delivered";
            }
        }
        // returning null will output default value
        return null;
    }

    public Resource getEnumFilterIcon(Object propertyId, Object value) {
        if ("state".equals(propertyId)) {
            State state = (State) value;
            switch (state) {
            case CREATED:
                return new ThemeResource("../runo/icons/16/document.png");
            case PROCESSING:
                return new ThemeResource("../runo/icons/16/reload.png");
            case PROCESSED:
                return new ThemeResource("../runo/icons/16/ok.png");
            case FINISHED:
                return new ThemeResource("../runo/icons/16/globe.png");
            }
        }
        return null;
    }

    public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
        if ("validated".equals(propertyId)) {
            return value ? "Validated" : "Not validated";
        }
        // returning null will output default value
        return null;
    }

    public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
        if ("validated".equals(propertyId)) {
            return value ? new ThemeResource("../runo/icons/16/ok.png")
                    : new ThemeResource("../runo/icons/16/cancel.png");
        }
        return null;
    }

    public String getFromCaption() {
        return "Start date:";
    }

    public String getToCaption() {
        return "End date:";
    }

    public String getSetCaption() {
        // use default caption
        return null;
    }

    public String getClearCaption() {
        // use default caption
        return null;
    }

    public boolean isTextFilterImmediate(Object propertyId) {
        // use text change events for all the text fields
        return true;
    }

    public int getTextChangeTimeout(Object propertyId) {
        // use the same timeout for all the text fields
        return 500;
    }

    public String getAllItemsVisibleString() {
        return "Show all";
    }

    public int getDateFieldResolution(Object propertyId) {
        return DateField.RESOLUTION_DAY;
    }

    public String getDateFormatPattern(Object propertyId) {
        return ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
                new Locale("fi", "FI"))).toPattern();
    }

    public NumberFilterPopupConfig getNumberFilterPopupConfig() {
        // work with default config
        return null;
    }

    public boolean usePopupForNumericProperty(Object propertyId) {
        // TODO Auto-generated method stub
        return false;
    }
}
