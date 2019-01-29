package org.tepi.filtertable.datefilter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.tepi.filtertable.FilterDecorator;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.shared.ui.datefield.Resolution;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.InlineDateField;

/**
 * Extension of PopupButton used to implement filter UI for Date properties.
 * Users can select either start date, end date or both. The filter can also be
 * set or cleared via a button in the filter pop-up.
 * 
 * @author Teppo Kurki
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
public class DateFilterPopup extends CustomField<DateInterval> {
	protected PopupButton content;

	protected DateField fromField, toField;
	private Date fromValue, toValue;
	private boolean cancelReset;
	private FilterDecorator decorator;
	protected Button set, clear;
	private final Object propertyId;
	private String dateFormatPattern;
	private TimeZone timeZone;

	private static final String DEFAULT_FROM_CAPTION = "From";
	private static final String DEFAULT_TO_CAPTION = "To";
	private static final String DEFAULT_SET_CAPTION = "Set";
	private static final String DEFAULT_CLEAR_CAPTION = "Clear";
	private static final Resolution DEFAULT_RESOLUTION = Resolution.DAY;

	public DateFilterPopup(FilterDecorator decorator, Object propertyId) {
		this.decorator = decorator;
		this.propertyId = propertyId;
		/*
		 * This call is needed for the value setting to function before attach
		 */
		getContent();
	}

	@Override
	public void attach() {
		super.attach();
		setFilterDecorator(decorator);
	}

	@Override
	public void setValue(DateInterval newFieldValue)
			throws com.vaadin.v7.data.Property.ReadOnlyException, ConversionException {
		if (newFieldValue == null) {
			newFieldValue = new DateInterval(null, null);
		}
		fromField.setValue(newFieldValue.getFrom());
		toField.setValue(newFieldValue.getTo());
		super.setValue(newFieldValue);
		updateCaption(newFieldValue.isNull());
	}

	protected void buildPopup() {
		VerticalLayout content = new VerticalLayout();
		content.setStyleName("datefilterpopupcontent");
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeUndefined();

		fromField = new InlineDateField();
		toField = new InlineDateField();
		fromField.setImmediate(true);
		toField.setImmediate(true);
		fromField.addValueChangeListener(e -> {
			if (e.getProperty().getValue() != null) {
				toField.setRangeStart((Date) e.getProperty().getValue());
			} else {
				toField.setRangeStart(null);
			}
		});
		toField.addValueChangeListener(e -> {
			if (e.getProperty().getValue() != null) {
				fromField.setRangeEnd((Date) e.getProperty().getValue());
			} else {
				fromField.setRangeEnd(null);
			}
		});

		set = new Button();
		clear = new Button();
		ClickListener buttonClickHandler = event -> updateValue(clear.equals(event.getButton()));
		set.addClickListener(buttonClickHandler);
		clear.addClickListener(buttonClickHandler);

		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSizeUndefined();
		buttonBar.setSpacing(true);
		buttonBar.addComponent(set);
		buttonBar.addComponent(clear);

		HorizontalLayout row = new HorizontalLayout();
		row.setSizeUndefined();
		row.setSpacing(true);
		row.addComponent(fromField);
		row.addComponent(toField);

		content.addComponent(row);
		content.addComponent(buttonBar);
		content.setComponentAlignment(buttonBar, Alignment.BOTTOM_RIGHT);

		this.content.setContent(content);
	}

	public void setFilterDecorator(FilterDecorator decorator) {
		this.decorator = decorator;

		/* Set DateField Locale */
		fromField.setLocale(getLocaleFailsafe());
		toField.setLocale(getLocaleFailsafe());

		String fromCaption = DEFAULT_FROM_CAPTION;
		String toCaption = DEFAULT_TO_CAPTION;
		String setCaption = DEFAULT_SET_CAPTION;
		String clearCaption = DEFAULT_CLEAR_CAPTION;
		Resolution resolution = DEFAULT_RESOLUTION;
		dateFormatPattern = ((SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
				getLocaleFailsafe())).toPattern();

		if (decorator != null) {
			if (decorator.getFromCaption() != null) {
				fromCaption = decorator.getFromCaption();
			}
			if (decorator.getToCaption() != null) {
				toCaption = decorator.getToCaption();
			}
			if (decorator.getSetCaption() != null) {
				setCaption = decorator.getSetCaption();
			}
			if (decorator.getClearCaption() != null) {
				clearCaption = decorator.getClearCaption();
			}
			if (decorator.getDateFieldResolution(propertyId) != null) {
				resolution = decorator.getDateFieldResolution(propertyId);
			}
			String dateFormatPattern = decorator.getDateFormatPattern(propertyId);
			if (dateFormatPattern != null) {
				this.dateFormatPattern = dateFormatPattern;
			}
		}
		/* Set captions */
		fromField.setCaption(fromCaption);
		toField.setCaption(toCaption);
		set.setCaption(setCaption);
		clear.setCaption(clearCaption);
		/* Set resolutions and date formats */
		fromField.setResolution(resolution);
		toField.setResolution(resolution);
		fromField.setDateFormat(dateFormatPattern);
		toField.setDateFormat(dateFormatPattern);
	}

	private void updateCaption(boolean nullTheCaption) {
		if (nullTheCaption) {
			if (decorator != null && decorator.getAllItemsVisibleString() != null) {
				content.setCaption(decorator.getAllItemsVisibleString());
			} else {
				content.setCaption(null);
			}
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
			if (timeZone != null) {
				sdf.setTimeZone(timeZone);
			}
			content.setCaption((fromField.getValue() == null ? "" : sdf.format(fromField.getValue())) + " - "
					+ (toField.getValue() == null ? "" : sdf.format(toField.getValue())));
		}
	}

	private void updateValue(boolean nullTheValue) {
		if (nullTheValue) {
			fromField.setValue(null);
			toField.setValue(null);
		} else {
			cancelReset = true;
		}
		/* Truncate the from and to dates */
		Resolution res = decorator != null ? decorator.getDateFieldResolution(propertyId) : DEFAULT_RESOLUTION;
		if (res == null) {
			res = DEFAULT_RESOLUTION;
		}
		fromValue = truncateDate(fromField.getValue(), res, true);
		toValue = truncateDate(toField.getValue(), res, false);
		setValue(new DateInterval(fromValue, toValue));
		DateFilterPopup.this.content.setPopupVisible(false);
	}

	private Date truncateDate(Date date, Resolution resolution, boolean start) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance(getLocaleFailsafe());
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, start ? 0 : 999);
		for (Resolution res : Resolution.getResolutionsLowerThan(resolution)) {
			if (res == Resolution.SECOND) {
				cal.set(Calendar.SECOND, start ? 0 : 59);
			} else if (res == Resolution.MINUTE) {
				cal.set(Calendar.MINUTE, start ? 0 : 59);
			} else if (res == Resolution.HOUR) {
				cal.set(Calendar.HOUR_OF_DAY, start ? 0 : 23);
			} else if (res == Resolution.DAY) {
				cal.set(Calendar.DAY_OF_MONTH, start ? 1 : cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else if (res == Resolution.MONTH) {
				cal.set(Calendar.MONTH, start ? 0 : cal.getActualMaximum(Calendar.MONTH));
			}
		}
		return cal.getTime();
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

	@Override
	protected Component initContent() {
		if (content == null) {
			content = new PopupButton(null);
			content.setWidth(100, Unit.PERCENTAGE);
			setImmediate(true);
			buildPopup();
			setStyleName("datefilterpopup");
			setFilterDecorator(decorator);
			updateCaption(true);
			content.addPopupVisibilityListener(event -> {
				if (cancelReset || event.getPopupButton().isPopupVisible()) {
					fromValue = fromField.getValue();
					toValue = toField.getValue();
					cancelReset = false;
					return;
				}
				fromField.setValue(fromValue);
				toField.setValue(toValue);
				cancelReset = false;
			});
		}
		return content;
	}

	@Override
	public Class<? extends DateInterval> getType() {
		return DateInterval.class;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		set.setEnabled(!readOnly);
		clear.setEnabled(!readOnly);
		fromField.setEnabled(!readOnly);
		toField.setEnabled(!readOnly);
	}
	
	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		if (timeZone != null) {
			fromField.setTimeZone(timeZone);
			toField.setTimeZone(timeZone);
		}
	}
}