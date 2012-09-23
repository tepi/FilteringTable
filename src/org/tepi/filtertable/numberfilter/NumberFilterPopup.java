package org.tepi.filtertable.numberfilter;

import org.vaadin.hene.popupbutton.PopupButton;

import org.tepi.filtertable.FilterDecorator;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Produces the number filter popup for the table
 * @author Vimukthi
 *
 */
@SuppressWarnings({ "serial", "unchecked" })
public class NumberFilterPopup extends PopupButton implements PopupButton.PopupVisibilityListener {
	
	/**
	 * decorator for the table
	 */
	private FilterDecorator decorator;
	
	/**
	 * Object holding the filters
	 */
	private NumberInterval interval;
	
	/**
	 * the filter values
	 */
	private Object ltValue;
	private Object gtValue;
	private Object eqValue;
		
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
	private String filterSet = "Filter Set";
	
	/**
	 * buttons
	 */
	private Button ok;
	private Button reset;
	

	/**
	 * @param caption
	 */
	public NumberFilterPopup(FilterDecorator decorator, String caption) {
		super(caption);
		setImmediate(true);
		this.setDecorator(decorator);
		setStyleName("numberfilterpopup");
		initPopup();
		setReadThrough(true);
		addPopupVisibilityListener(this);
		setNullCaption();
	}
	
	public void initPopup(){
		final GridLayout content = new GridLayout(2, 4);
		content.setStyleName("numberfilterpopupcontent");
		content.setSpacing(true);
		content.setMargin(true);
		content.setWidth("300px");
		
		content.addComponent(new Label(gt), 0, 0);
		content.addComponent(new Label(lt), 0, 1);		
		content.addComponent(new Label(eq), 0, 2);
		
		// greater than input field
		gtInput = new TextField();
		gtInput.setInputPrompt(gtPrompt);
		content.addComponent(gtInput, 1, 0);
		
		// less than input field
		ltInput = new TextField();
		ltInput.setInputPrompt(ltPrompt);
		content.addComponent(ltInput, 1, 1);
		
		// equals input field
		eqInput = new TextField();
		eqInput.setInputPrompt(eqPrompt);
		content.addComponent(eqInput, 1, 2);
		// disable gt and lt fields when this activates
		eqInput.addListener(new FieldEvents.TextChangeListener() {
			
			public void textChange(TextChangeEvent event) {
				if (event.getText().equals("")){
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
				String ltNow;
				String gtNow;
				String eqNow;
				
				try {
					Double.valueOf((String) ltInput.getValue());
					ltNow = (String)ltInput.getValue();
				} catch (NumberFormatException e) {
					ltNow = null;
				}
				
				try {
					Double.valueOf((String) gtInput.getValue());
					gtNow = (String)gtInput.getValue();
				} catch (NumberFormatException e) {
					gtNow = null;
				}
				
				try {
					Double.valueOf((String) eqInput.getValue());
					eqNow = (String)eqInput.getValue();
				} catch (NumberFormatException e) {
					eqNow = null;
				}
				setInternalValue(ltNow, gtNow, eqNow);
			}
		});
		content.addComponent(ok, 0, 3);
		
		reset = new Button(resetCaption, new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setInternalValue(null, null, null);
				ltInput.setValue("");
				gtInput.setValue("");
				eqInput.setValue("");
				gtInput.setEnabled(true);
				ltInput.setEnabled(true);
			}
		});
		content.addComponent(reset, 1, 3);
		
		setComponent(content);
	}
	
	

	public void setInternalValue(String ltValue, String gtValue, String eqValue) {
		if (ltValue != null || gtValue != null || eqValue != null){
			interval = new NumberInterval(ltValue, gtValue, eqValue);
			setCaption(filterSet);				
		} else {
			interval = null;
			setCaption(null);	
		}
		setPopupVisible(false);
		valueChange(new ValueChangeEvent(NumberFilterPopup.this));
	}
	
	@Override
	public void attach() {
		super.attach();
		if (decorator != null) {
			setFilterDecorator(decorator);
		}

	}
	
	public void setFilterDecorator(FilterDecorator decorator) {
		this.setDecorator(decorator);
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
		if (conf.getFilterSet() != null) {
			filterSet = conf.getFilterSet();
		}
		if (conf.getOkCaption() != null) {
			okCaption = conf.getOkCaption();
		}
		if (conf.getResetCaption() != null) {
			resetCaption = conf.getResetCaption();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener#popupVisibilityChange(org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent)
	 */
	public void popupVisibilityChange(PopupVisibilityEvent event) {
		// TODO Auto-generated method stub
		
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

	/**
	 * @return the ltValue
	 */
	public Object getLtValue() {
		return ltValue;
	}

	/**
	 * @param ltValue the ltValue to set
	 */
	public void setLtValue(Object ltValue) {
		this.ltValue = ltValue;
	}

	/**
	 * @return the gtValue
	 */
	public Object getGtValue() {
		return gtValue;
	}

	/**
	 * @param gtValue the gtValue to set
	 */
	public void setGtValue(Object gtValue) {
		this.gtValue = gtValue;
	}

	/**
	 * @return the eqValue
	 */
	public Object getEqValue() {
		return eqValue;
	}

	/**
	 * @param eqValue the eqValue to set
	 */
	public void setEqValue(Object eqValue) {
		this.eqValue = eqValue;
	}

	/**
	 * @return the decorator
	 */
	public FilterDecorator getDecorator() {
		return decorator;
	}

	/**
	 * @param decorator the decorator to set
	 */
	public void setDecorator(FilterDecorator decorator) {
		this.decorator = decorator;
	}

	/**
	 * @return the ltInput
	 */
	public TextField getLtInput() {
		return ltInput;
	}

	/**
	 * @param ltInput the ltInput to set
	 */
	public void setLtInput(TextField ltInput) {
		this.ltInput = ltInput;
	}

	/**
	 * @return the gtInput
	 */
	public TextField getGtInput() {
		return gtInput;
	}

	/**
	 * @param gtInput the gtInput to set
	 */
	public void setGtInput(TextField gtInput) {
		this.gtInput = gtInput;
	}

	/**
	 * @return the eqInput
	 */
	public TextField getEqInput() {
		return eqInput;
	}

	/**
	 * @param eqInput the eqInput to set
	 */
	public void setEqInput(TextField eqInput) {
		this.eqInput = eqInput;
	}

	/**
	 * @return the interval
	 */
	public NumberInterval getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(NumberInterval interval) {
		this.interval = interval;
	}

	/**
	 * @return the lt
	 */
	public String getLt() {
		return lt;
	}

	/**
	 * @return the gt
	 */
	public String getGt() {
		return gt;
	}

	/**
	 * @return the eq
	 */
	public String getEq() {
		return eq;
	}

	/**
	 * @return the ltPrompt
	 */
	public String getLtPrompt() {
		return ltPrompt;
	}

	/**
	 * @return the gtPrompt
	 */
	public String getGtPrompt() {
		return gtPrompt;
	}

	/**
	 * @return the eqPrompt
	 */
	public String getEqPrompt() {
		return eqPrompt;
	}

	/**
	 * @return the ok
	 */
	public Button getOk() {
		return ok;
	}

	/**
	 * @param ok the ok to set
	 */
	public void setOk(Button ok) {
		this.ok = ok;
	}

	/**
	 * @return the cancel
	 */
	public Button getReset() {
		return reset;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setReset(Button reset) {
		this.reset = reset;
	}

	/**
	 * @return the okcaption
	 */
	public String getOkcaption() {
		return okCaption;
	}

	/**
	 * @return the cancelcaption
	 */
	public String getResetcaption() {
		return resetCaption;
	}

	/**
	 * @return the filterset
	 */
	public String getFilterset() {
		return filterSet;
	}
	
	/**
	 * @return the okCaption
	 */
	public String getOkCaption() {
		return okCaption;
	}

	/**
	 * @param okCaption the okCaption to set
	 */
	public void setOkCaption(String okCaption) {
		this.okCaption = okCaption;
	}

	/**
	 * @return the resetCaption
	 */
	public String getResetCaption() {
		return resetCaption;
	}

	/**
	 * @param resetCaption the resetCaption to set
	 */
	public void setResetCaption(String resetCaption) {
		this.resetCaption = resetCaption;
	}

	/**
	 * @return the filterSet
	 */
	public String getFilterSet() {
		return filterSet;
	}

	/**
	 * @param filterSet the filterSet to set
	 */
	public void setFilterSet(String filterSet) {
		this.filterSet = filterSet;
	}

	/**
	 * @param ltPrompt the ltPrompt to set
	 */
	public void setLtPrompt(String ltPrompt) {
		this.ltPrompt = ltPrompt;
	}

	/**
	 * @param gtPrompt the gtPrompt to set
	 */
	public void setGtPrompt(String gtPrompt) {
		this.gtPrompt = gtPrompt;
	}

	/**
	 * @param eqPrompt the eqPrompt to set
	 */
	public void setEqPrompt(String eqPrompt) {
		this.eqPrompt = eqPrompt;
	}
}
