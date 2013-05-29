package org.tepi.filtertable.demo;

import org.tepi.filtertable.FilterGenerator;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

class DemoFilterGenerator implements FilterGenerator {

	private final Window window;

	public DemoFilterGenerator(Window window) {
		this.window = window;
	}

	public Filter generateFilter(Object propertyId, Object value) {
		if ("id".equals(propertyId)) {
			/* Create an 'equals' filter for the ID field */
			if (value != null && value instanceof String) {
				try {
					return new Compare.Equal(propertyId,
							Integer.parseInt((String) value));
				} catch (NumberFormatException ignored) {
					// If no integer was entered, just generate default filter
				}
			}
		} else if ("checked".equals(propertyId)) {
			if (value != null && value instanceof Boolean) {
				if (Boolean.TRUE.equals(value)) {
					return new Compare.Equal(propertyId, value);
				} else {
					return new Or(new Compare.Equal(propertyId, true),
							new Compare.Equal(propertyId, false));
				}
			}
		}
		// For other properties, use the default filter
		return null;
	}

	public Filter generateFilter(Object propertyId, Field originatingField) {
		// Use the default filter
		return null;
	}

	public AbstractField getCustomFilterComponent(Object propertyId) {
		// removed custom filter component for id
		if ("checked".equals(propertyId)) {
			CheckBox box = new CheckBox();
			return box;
		}
		return null;
	}

	public void filterRemoved(Object propertyId) {
		Notification n = new Window.Notification("Filter removed from: "
				+ propertyId, Notification.TYPE_TRAY_NOTIFICATION);
		n.setDelayMsec(800);
		window.showNotification(n);
	}

	public void filterAdded(Object propertyId,
			Class<? extends Filter> filterType, Object value) {
		Notification n = new Window.Notification("Filter added to: "
				+ propertyId, Notification.TYPE_TRAY_NOTIFICATION);
		n.setDelayMsec(800);
		window.showNotification(n);
	}
}
