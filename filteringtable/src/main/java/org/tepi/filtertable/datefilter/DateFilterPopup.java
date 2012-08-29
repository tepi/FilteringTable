package org.tepi.filtertable.datefilter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.tepi.filtertable.FilterDecorator;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Extension of PopupButton used to implement filter UI for Date properties.
 * Users can select either start date, end date or both. The filter can also be
 * set or cleared via a button in the filter pop-up.
 * 
 * @author Teppo Kurki
 * 
 */
@SuppressWarnings("serial")
public class DateFilterPopup extends PopupButton {
    private DateField fromField;
    private DateField toField;
    private DateInterval value;
    private Object fromValue, toValue;
    private boolean cancelReset;
    private FilterDecorator decorator;
    private Label fromLabel;
    private Label toLabel;
    private Button set;
    private Button clear;

    public DateFilterPopup(FilterDecorator decorator, String caption) {
        super(caption);
        this.decorator = decorator;
        setImmediate(true);
        buildPopup();
        setStyleName("datefilterpopup");
        setReadThrough(true);
        addPopupVisibilityListener(new PopupVisibilityListener() {
            public void popupVisibilityChange(PopupVisibilityEvent event) {
                if (cancelReset || event.getPopupButton().isPopupVisible()) {
                    fromValue = fromField.getValue();
                    toValue = toField.getValue();
                    cancelReset = false;
                    return;
                }
                fromField.setValue(fromValue);
                toField.setValue(toValue);
                cancelReset = false;
            }
        });
    }

    @Override
    public void attach() {
        super.attach();
        if (decorator != null) {
            setFilterDecorator(decorator);
        }

    }

    private void buildPopup() {
        VerticalLayout content = new VerticalLayout();
        content.setStyleName("datefilterpopupcontent");
        content.setSpacing(true);
        content.setMargin(true);
        content.setWidth("300px");

        fromLabel = new Label("From");
        toLabel = new Label("To");
        fromLabel.setWidth("100px");
        toLabel.setWidth("100px");

        fromField = new DateField();
        toField = new DateField();

        fromField.setResolution(DateField.RESOLUTION_MIN);
        toField.setResolution(DateField.RESOLUTION_MIN);

        fromField.setWidth("100%");
        toField.setWidth("100%");

        fromField.setImmediate(true);
        toField.setImmediate(true);

        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setSizeUndefined();
        buttonBar.setSpacing(true);
        set = new Button("Set", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                cancelReset = true;
                setInternalValue((Date) fromField.getValue(),
                        (Date) toField.getValue());
            }
        });
        clear = new Button("Clear", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                setInternalValue(null, null);
            }
        });
        buttonBar.addComponent(set);
        buttonBar.addComponent(clear);

        HorizontalLayout row = new HorizontalLayout();
        row.setWidth("100%");
        row.addComponent(fromLabel);
        row.addComponent(fromField);
        row.setComponentAlignment(fromLabel, Alignment.MIDDLE_LEFT);
        row.setExpandRatio(fromField, 1);
        content.addComponent(row);
        row = new HorizontalLayout();
        row.setWidth("100%");
        row.addComponent(toLabel);
        row.addComponent(toField);
        row.setComponentAlignment(toLabel, Alignment.MIDDLE_LEFT);
        row.setExpandRatio(toField, 1);
        content.addComponent(row);
        content.addComponent(buttonBar);
        content.setComponentAlignment(buttonBar, Alignment.BOTTOM_RIGHT);
        content.setMargin(true);
        setComponent(content);
    }

    public void setInternalValue(Date from, Date to) {
        if (from != null || to != null) {
            value = new DateInterval(from, to);
            Locale locale = fromField.getLocale();
            if (decorator != null && decorator.getLocale() != null) {
                locale = decorator.getLocale();
            }
            DateFormat dateFormatter = null;
            if (locale == null) {
                dateFormatter = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT);
            } else {
                dateFormatter = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT, locale);
            }
            setCaption((from == null ? "" : dateFormatter.format(from)) + " - "
                    + (to == null ? "" : dateFormatter.format(to)));
        } else {
            value = null;
            setCaption(null);
        }
        setPopupVisible(false);
        valueChange(new ValueChangeEvent(DateFilterPopup.this));
    }

    public DateInterval getDateValue() {
        return value;
    }

    public void setFilterDecorator(FilterDecorator decorator) {
        this.decorator = decorator;
        if (decorator == null) {
            return;
        }
        /* Prepare DataFormatter with correct locale */
        Locale locale = getApplication().getLocale();
        if (decorator != null) {
            locale = decorator.getLocale() == null ? locale : decorator
                    .getLocale();
        }
        /* Set datefield locales */
        fromField.setLocale(locale);
        toField.setLocale(locale);
        /* Set captions */
        if (decorator.getFromCaption() != null) {
            fromLabel.setValue(decorator.getFromCaption());
        }
        if (decorator.getToCaption() != null) {
            toLabel.setValue(decorator.getToCaption());
        }
        if (decorator.getSetCaption() != null) {
            set.setCaption(decorator.getSetCaption());
        }
        if (decorator.getClearCaption() != null) {
            clear.setCaption(decorator.getClearCaption());
        }
    }
}
