package org.tepi.filtertable.demo;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Locale;

import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.demo.FilterTableDemoUI.State;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;

class DemoFilterDecorator implements FilterDecorator, Serializable {

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

    public Resolution getDateFieldResolution(Object propertyId) {
        return Resolution.DAY;
    }

    public DateFormat getDateFormat(Object propertyId) {
        return DateFormat.getDateInstance(DateFormat.SHORT, new Locale("fi",
                "FI"));
    }

    public boolean usePopupForNumericProperty(Object propertyId) {
        // TODO Auto-generated method stub
        return false;
    }
}
