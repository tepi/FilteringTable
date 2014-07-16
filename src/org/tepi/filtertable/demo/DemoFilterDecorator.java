package org.tepi.filtertable.demo;

import java.io.Serializable;

import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.datefilter.DateFilterPopupConfig;
import org.tepi.filtertable.demo.FilterTableDemoUI.State;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;

@SuppressWarnings("serial")
class DemoFilterDecorator implements FilterDecorator, Serializable {

    @Override
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

    @Override
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

    @Override
    public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
        if ("validated".equals(propertyId)) {
            return value ? "Validated" : "Not validated";
        }
        // returning null will output default value
        return null;
    }

    @Override
    public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
        if ("validated".equals(propertyId)) {
            return value ? new ThemeResource("../runo/icons/16/ok.png")
                    : new ThemeResource("../runo/icons/16/cancel.png");
        }
        return null;
    }

    @Override
    public boolean isTextFilterImmediate(Object propertyId) {
        // use text change events for all the text fields
        return true;
    }

    @Override
    public int getTextChangeTimeout(Object propertyId) {
        // use the same timeout for all the text fields
        return 500;
    }

    @Override
    public String getAllItemsVisibleString() {
        return "Show all";
    }

    @Override
    public boolean usePopupForNumericProperty(Object propertyId) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public NumberFilterPopupConfig getNumberFilterPopupConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DateFilterPopupConfig getDateFilterPopupConfig() {
        DateFilterPopupConfig cfg = new DateFilterPopupConfig();
        cfg.setResolution(Resolution.DAY);
        cfg.setFromCaption("Start date:");
        cfg.setToCaption("End date:");
        return cfg;
    }
}
