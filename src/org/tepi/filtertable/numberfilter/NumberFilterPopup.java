package org.tepi.filtertable.numberfilter;

import org.tepi.filtertable.FilterDecorator;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Produces the number filter popup for the table
 * 
 * @author Vimukthi
 * 
 */
@SuppressWarnings({ "serial", "unchecked" })
public class NumberFilterPopup extends PopupButton implements
        PopupButton.PopupVisibilityListener {

    /**
     * decorator for the table
     */
    private FilterDecorator decorator;

    /**
     * Object holding the filters
     */
    private NumberInterval interval;

    /**
     * input text fields
     */
    private TextField ltInput;
    private TextField gtInput;
    private TextField eqInput;

    /**
     * label strings
     */
    private static final String lt = "<";
    private static final String gt = ">";
    private static final String eq = "=";
    private String ltPrompt = "Less than";
    private String gtPrompt = "Greater than";
    private String eqPrompt = "Equal to";
    private String okCaption = "Set";
    private String resetCaption = "Clear";
    private String valueMarker = "[x]";

    /**
     * buttons
     */
    private Button ok;
    private Button reset;

    private boolean settingValue;

    /**
     * @param caption
     */
    public NumberFilterPopup(FilterDecorator decorator, String caption) {
        super(caption);
        setImmediate(true);
        this.decorator = decorator;
        setStyleName("numberfilterpopup");
        initPopup();
        setReadThrough(true);
        addPopupVisibilityListener(this);
        setNullCaption();
    }

    private void initPopup() {
        final GridLayout content = new GridLayout(2, 4);
        content.setStyleName("numberfilterpopupcontent");
        content.setSpacing(true);
        content.setMargin(true);
        content.setSizeUndefined();

        content.addComponent(new Label(gt), 0, 0);
        content.addComponent(new Label(lt), 0, 1);
        content.addComponent(new Label(eq), 0, 2);

        // greater than input field
        gtInput = new TextField();
        gtInput.setNullRepresentation("");
        content.addComponent(gtInput, 1, 0);

        // less than input field
        ltInput = new TextField();
        ltInput.setNullRepresentation("");
        content.addComponent(ltInput, 1, 1);

        // equals input field
        eqInput = new TextField();
        eqInput.setNullRepresentation("");
        content.addComponent(eqInput, 1, 2);

        // disable gt and lt fields when this activates
        eqInput.addListener(new FieldEvents.TextChangeListener() {

            public void textChange(TextChangeEvent event) {
                if (event.getText().equals("")) {
                    gtInput.setEnabled(true);
                    ltInput.setEnabled(true);
                } else {
                    gtInput.setEnabled(false);
                    ltInput.setEnabled(false);
                }
            }
        });

        ok = new Button(okCaption, new ClickListener() {
            public void buttonClick(ClickEvent event) {
                // users inputs
                String ltNow = (String) ltInput.getValue();
                String gtNow = (String) gtInput.getValue();
                String eqNow = (String) eqInput.getValue();
                setInternalValue(ltNow, gtNow, eqNow);
            }
        });

        reset = new Button(resetCaption, new ClickListener() {
            public void buttonClick(ClickEvent event) {
                reset();
            }
        });
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidth("100%");
        buttons.setSpacing(true);
        buttons.addComponent(ok);
        buttons.addComponent(reset);
        buttons.setExpandRatio(ok, 1);
        buttons.setComponentAlignment(ok, Alignment.MIDDLE_RIGHT);
        content.addComponent(buttons, 0, 3, 1, 3);

        setLabels();
        setComponent(content);
    }

    public void setInternalValue(String ltValue, String gtValue, String eqValue) {
        settingValue = true;
        interval = ltValue != null || gtValue != null || eqValue != null ? new NumberInterval(
                ltValue, gtValue, eqValue) : null;
        setCaption(buildDisplayLabel());
        setPopupVisible(false);
        valueChange(new ValueChangeEvent(NumberFilterPopup.this));
        settingValue = false;
    }

    @Override
    public void attach() {
        super.attach();
        if (decorator != null) {
            setFilterDecorator(decorator);
        }
    }

    public void setFilterDecorator(FilterDecorator decorator) {
        this.decorator = decorator;
        if (decorator == null || decorator.getNumberFilterPopupConfig() == null) {
            return;
        }

        NumberFilterPopupConfig conf = decorator.getNumberFilterPopupConfig();

        /* Set captions */
        if (conf.getEqPrompt() != null) {
            eqPrompt = conf.getEqPrompt();
        }
        if (conf.getLtPrompt() != null) {
            ltPrompt = conf.getLtPrompt();
        }
        if (conf.getGtPrompt() != null) {
            gtPrompt = conf.getGtPrompt();
        }
        if (conf.getValueMarker() != null) {
            valueMarker = conf.getValueMarker();
        }
        if (conf.getOkCaption() != null) {
            okCaption = conf.getOkCaption();
        }
        if (conf.getResetCaption() != null) {
            resetCaption = conf.getResetCaption();
        }

        setLabels();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener#
     * popupVisibilityChange
     * (org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent)
     */
    public void popupVisibilityChange(PopupVisibilityEvent event) {
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
    }

    /**
     * @return the interval
     */
    public NumberInterval getInterval() {
        return interval;
    }

    public void reset() {
        setInternalValue(null, null, null);
        ltInput.setValue("");
        gtInput.setValue("");
        eqInput.setValue("");
        gtInput.setEnabled(true);
        ltInput.setEnabled(true);
        setNullCaption();
    }

    /**
     * set the initial caption for the filter
     */
    private void setNullCaption() {
        if (decorator != null && decorator.getAllItemsVisibleString() != null) {
            setCaption(decorator.getAllItemsVisibleString());
        } else {
            setCaption(null);
        }
    }

    private void setLabels() {
        gtInput.setInputPrompt(gtPrompt);
        ltInput.setInputPrompt(ltPrompt);
        eqInput.setInputPrompt(eqPrompt);
        ok.setCaption(okCaption);
        reset.setCaption(resetCaption);
        buildDisplayLabel();
    }

    private String buildDisplayLabel() {
        if (interval != null) {
            if (interval.getEqualsValue() != null) {
                return valueMarker + " = " + interval.getEqualsValue();
            } else if (interval.getGreaterThanValue() != null
                    && interval.getLessThanValue() != null) {
                return interval.getGreaterThanValue() + " < " + valueMarker
                        + " < " + interval.getLessThanValue();
            } else if (interval.getGreaterThanValue() != null) {
                return valueMarker + " > " + interval.getGreaterThanValue();
            } else if (interval.getLessThanValue() != null) {
                return valueMarker + " < " + interval.getLessThanValue();
            }
        }
        return null;
    }
}
