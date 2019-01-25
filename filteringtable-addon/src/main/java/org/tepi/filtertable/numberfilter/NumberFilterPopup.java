package org.tepi.filtertable.numberfilter;

import java.math.BigInteger;

import org.tepi.filtertable.FilterDecorator;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Produces the number filter popup for the table
 * 
 * @author Vimukthi
 * @author Teppo Kurki [adapted for V8]
 */
@SuppressWarnings({ "serial", "deprecation" })
public class NumberFilterPopup extends CustomField<NumberInterval> {

	private PopupButton content;
	private FilterDecorator decorator;
	private boolean settingValue;
	private String valueMarker;
	private NumberInterval interval;
	private boolean allowDecimalPlaces;

	/* Input fields */
	private TextField ltInput;
	private TextField gtInput;
	private TextField eqInput;

	/* Default labels */
	private static final String LT = "<";
	private static final String GT = ">";
	private static final String EQ = "=";
	private static final String DEFAULT_LT_PROMPT = "Less than";
	private static final String DEFAULT_GT_PROMPT = "Greater than";
	private static final String DEFAULT_EQ_PROMPT = "Equal to";
	private static final String DEFAULT_OK_CAPTION = "Set";
	private static final String DEFAULT_RESET_CAPTION = "Clear";
	private static final String DEFAULT_VALUE_MARKER = "[x]";

	/* Buttons */
	private Button ok;
	private Button reset;

	public NumberFilterPopup(FilterDecorator decorator) {
		this.decorator = decorator;
		/*
		 * This call is needed for the value setting to function before attach
		 */
		getContent();
	}

	private void initPopup() {
		final GridLayout content = new GridLayout(2, 4);
		content.setStyleName("numberfilterpopupcontent");
		content.setSpacing(true);
		content.setMargin(true);
		content.setSizeUndefined();

		Label label = new Label(GT);
		content.addComponent(label, 0, 0);
		content.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
		label = new Label(LT);
		content.addComponent(label, 0, 1);
		content.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
		label = new Label(EQ);
		content.addComponent(label, 0, 2);
		content.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);

		// greater than input field
		gtInput = new TextField();
		content.addComponent(gtInput, 1, 0);

		// less than input field
		ltInput = new TextField();
		content.addComponent(ltInput, 1, 1);

		// equals input field
		eqInput = new TextField();
		content.addComponent(eqInput, 1, 2);

		// disable gt and lt fields when this activates
		eqInput.addValueChangeListener(e -> {
			if (e.getProperty().getValue().equals("")) {
				gtInput.setEnabled(true);
				ltInput.setEnabled(true);
			} else {
				gtInput.setEnabled(false);
				ltInput.setEnabled(false);
			}
		});
		eqInput.setTextChangeEventMode(TextChangeEventMode.EAGER);

		ok = new Button(DEFAULT_OK_CAPTION, (ClickListener) event -> {
			String errorMsg = "Please enter a valid number";
			if (decorator != null && decorator.getNumberValidationErrorMessage() != null) {
				errorMsg = decorator.getNumberValidationErrorMessage();
			}

			String ltNow = "";
			String gtNow = "";
			String eqNow = "";
			boolean valid = true;
			try {
				ltNow = parseInput(ltInput);
			} catch (NumberFormatException e1) {
				ltInput.setComponentError(new UserError(errorMsg));
				valid = false;
			}
			try {
				gtNow = parseInput(gtInput);
			} catch (NumberFormatException e2) {
				gtInput.setComponentError(new UserError(errorMsg));
				valid = false;
			}
			try {
				eqNow = parseInput(eqInput);
			} catch (NumberFormatException e3) {
				eqInput.setComponentError(new UserError(errorMsg));
				valid = false;
			}
			if (!valid) {
				return;
			}
			setValue(new NumberInterval(ltNow, gtNow, eqNow));
			NumberFilterPopup.this.content.setPopupVisible(false);
		});

		reset = new Button(DEFAULT_RESET_CAPTION, (ClickListener) event -> {
			setValue(null);
			NumberFilterPopup.this.content.setPopupVisible(false);
		});

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setWidth("100%");
		buttons.setSpacing(true);
		buttons.addComponent(ok);
		buttons.addComponent(reset);
		buttons.setExpandRatio(ok, 1);
		buttons.setComponentAlignment(ok, Alignment.MIDDLE_RIGHT);
		content.addComponent(buttons, 0, 3, 1, 3);

		this.content.setContent(content);
	}

	private String parseInput(TextField input) throws NumberFormatException {
		String value = input.getValue();
		if (value == null || value.trim().isEmpty()) {
			return "";
		} else {
			if (allowDecimalPlaces) {
				Double.valueOf(value);
			} else {
				new BigInteger(value);
			}
			return value;
		}
	}

	@Override
	public void setValue(NumberInterval newFieldValue)
			throws com.vaadin.data.Property.ReadOnlyException, ConversionException {
		settingValue = true;
		boolean nullValue = false;
		if (newFieldValue == null || (newFieldValue.getEqualsValue() == null
				&& newFieldValue.getGreaterThanValue() == null && newFieldValue.getLessThanValue() == null)) {
			nullValue = true;
			newFieldValue = null;
			gtInput.setEnabled(true);
			ltInput.setEnabled(true);
		}
		interval = newFieldValue;
		ltInput.setValue(nullValue ? "" : newFieldValue.getLessThanValue());
		gtInput.setValue(nullValue ? "" : newFieldValue.getGreaterThanValue());
		eqInput.setValue(nullValue ? "" : newFieldValue.getEqualsValue());
		ltInput.setComponentError(null);
		gtInput.setComponentError(null);
		eqInput.setComponentError(null);
		super.setValue(newFieldValue);
		updateCaption();
		settingValue = false;
	}

	private void updateCaption() {
		if (interval == null) {
			content.setCaption(decorator != null && decorator.getAllItemsVisibleString() != null
					? decorator.getAllItemsVisibleString()
					: "");
		} else {
			if (!interval.getEqualsValue().isEmpty()) {
				content.setCaption(valueMarker + " = " + interval.getEqualsValue());
			} else if (!interval.getGreaterThanValue().isEmpty() && !interval.getLessThanValue().isEmpty()) {
				content.setCaption(
						interval.getGreaterThanValue() + " < " + valueMarker + " < " + interval.getLessThanValue());
			} else if (!interval.getGreaterThanValue().isEmpty()) {
				content.setCaption(valueMarker + " > " + interval.getGreaterThanValue());
			} else if (!interval.getLessThanValue().isEmpty()) {
				content.setCaption(valueMarker + " < " + interval.getLessThanValue());
			}
		}
	}

	@Override
	public void attach() {
		super.attach();
		setFilterDecorator(decorator);
	}

	public void setFilterDecorator(FilterDecorator decorator) {
		this.decorator = decorator;

		String eqP = DEFAULT_EQ_PROMPT;
		String ltP = DEFAULT_LT_PROMPT;
		String gtP = DEFAULT_GT_PROMPT;
		valueMarker = DEFAULT_VALUE_MARKER;
		String ok = DEFAULT_OK_CAPTION;
		String reset = DEFAULT_RESET_CAPTION;

		if (decorator != null && decorator.getNumberFilterPopupConfig() != null) {
			NumberFilterPopupConfig conf = decorator.getNumberFilterPopupConfig();
			if (conf.getEqPrompt() != null) {
				eqP = conf.getEqPrompt();
			}
			if (conf.getLtPrompt() != null) {
				ltP = conf.getLtPrompt();
			}
			if (conf.getGtPrompt() != null) {
				gtP = conf.getGtPrompt();
			}
			if (conf.getValueMarker() != null) {
				valueMarker = conf.getValueMarker();
			}
			if (conf.getOkCaption() != null) {
				ok = conf.getOkCaption();
			}
			if (conf.getResetCaption() != null) {
				reset = conf.getResetCaption();
			}
		}

		gtInput.setInputPrompt(gtP);
		ltInput.setInputPrompt(ltP);
		eqInput.setInputPrompt(eqP);
		this.ok.setCaption(ok);
		this.reset.setCaption(reset);
	}

	@Override
	protected Component initContent() {
		if (content == null) {
			content = new PopupButton();
			content.setWidth(100, Unit.PERCENTAGE);
			setImmediate(true);
			setStyleName("numberfilterpopup");
			initPopup();
			setFilterDecorator(decorator);
			content.addPopupVisibilityListener(event -> {
				if (settingValue) {
					settingValue = false;
				} else if (interval == null) {
					ltInput.setValue("");
					gtInput.setValue("");
					eqInput.setValue("");
				} else {
					ltInput.setValue(interval.getLessThanValue());
					gtInput.setValue(interval.getGreaterThanValue());
					eqInput.setValue(interval.getEqualsValue());
				}
			});
			updateCaption();
		}
		return content;
	}

	@Override
	public Class<? extends NumberInterval> getType() {
		return NumberInterval.class;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		ok.setEnabled(!readOnly);
		reset.setEnabled(!readOnly);
		ltInput.setEnabled(!readOnly);
		gtInput.setEnabled(!readOnly);
		eqInput.setEnabled(!readOnly);
	}

	public void setDecimalPlacesAllowed(boolean allowDecimalPlaces) {
		this.allowDecimalPlaces = allowDecimalPlaces;
	}
}
