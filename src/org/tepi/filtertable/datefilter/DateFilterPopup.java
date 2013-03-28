package org.tepi.filtertable.datefilter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private final Object propertyId;
    private String dateFormatPattern;

    public DateFilterPopup(FilterDecorator decorator, Object propertyId) {
        super(null);
        this.decorator = decorator;
        this.propertyId = propertyId;
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
        setFilterDecorator(decorator);
        setNullCaption();
    }

    @Override
    public void attach() {
        super.attach();
        if (decorator != null) {
            setFilterDecorator(decorator);
        } else {
            setDefaultDateFormat();
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
            value = buildValue(from, to);
            fromField.setValue(value.getFrom());
            toField.setValue(value.getTo());
            updateCaption();
        } else {
            value = null;
            fromField.setValue(null);
            toField.setValue(null);
            setNullCaption();
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
        /* Set DateField Locale */
        fromField.setLocale(getLocaleFailsafe());
        toField.setLocale(getLocaleFailsafe());
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
        int resolution = decorator.getDateFieldResolution(propertyId);
        fromField.setResolution(resolution);
        toField.setResolution(resolution);

        dateFormatPattern = decorator.getDateFormatPattern(propertyId);
        if (dateFormatPattern == null) {
            setDefaultDateFormat();
        }
        fromField.setDateFormat(dateFormatPattern);
        toField.setDateFormat(dateFormatPattern);

    }

    private void updateCaption() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
        setCaption((fromField.getValue() == null ? "" : sdf.format(fromField
                .getValue()))
                + " - "
                + (toField.getValue() == null ? "" : sdf.format(toField
                        .getValue())));
    }

    private void setNullCaption() {
        if (decorator != null && decorator.getAllItemsVisibleString() != null) {
            setCaption(decorator.getAllItemsVisibleString());
        } else {
            setCaption(null);
        }
    }

    private DateInterval buildValue(Date from, Date to) {
        /* Truncate the from and to dates */
        int res = decorator != null ? decorator
                .getDateFieldResolution(propertyId) : DateField.RESOLUTION_MIN;
        if (from != null) {
            from = truncateDate(from, res, true);
        }
        if (to != null) {
            to = truncateDate(to, res, false);
        }
        return new DateInterval(from, to);
    }

    private Date truncateDate(Date date, int resolution, boolean start) {
        Calendar cal = Calendar.getInstance(getLocaleFailsafe());
        cal.setTime(date);
        if (resolution > DateField.RESOLUTION_MSEC) {
            cal.set(Calendar.MILLISECOND, start ? 0 : 999);
        }
        if (resolution > DateField.RESOLUTION_SEC) {
            cal.set(Calendar.SECOND, start ? 0 : 59);
        }
        if (resolution > DateField.RESOLUTION_MIN) {
            cal.set(Calendar.MINUTE, start ? 0 : 59);
        }
        if (resolution > DateField.RESOLUTION_HOUR) {
            cal.set(Calendar.HOUR_OF_DAY, start ? 0 : 23);
        }
        if (resolution > DateField.RESOLUTION_DAY) {
            cal.set(Calendar.DAY_OF_MONTH,
                    start ? 1 : cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        if (resolution > DateField.RESOLUTION_MONTH) {
            cal.set(Calendar.MONTH,
                    start ? 0 : cal.getActualMaximum(Calendar.MONTH));
        }
        return cal.getTime();
    }

    private void setDefaultDateFormat() {
        dateFormatPattern = ((SimpleDateFormat) DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, getLocaleFailsafe()))
                .toPattern();
    }

    private Locale getLocaleFailsafe() {
        /* First try the locale provided by the decorator */
        if (decorator != null && decorator.getLocale() != null) {
            return decorator.getLocale();
        }
        /* Then try application locale */
        if (super.getLocale() != null) {
            return super.getLocale();
        }
        /* Finally revert to system default locale */
        return Locale.getDefault();
    }
}
