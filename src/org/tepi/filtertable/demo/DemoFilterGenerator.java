package org.tepi.filtertable.demo;

import org.tepi.filtertable.FilterGenerator;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;

class DemoFilterGenerator implements FilterGenerator {

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

    public AbstractField getCustomFilterComponent(Object propertyId) {
        if ("id".equals(propertyId)) {
            ComboBox cb = new ComboBox();
            cb.addItem("1");
            cb.addItem("2");
            return cb;
        } else if ("checked".equals(propertyId)) {
            CheckBox box = new CheckBox();
            return box;
        } else if ("name".equals(propertyId)) {
            TextField tf = new TextField();
            tf.setVisible(false);
            return tf;
        }
        return null;
    }

    public void filterRemoved(Object propertyId) {
        // TODO Auto-generated method stub
    }

    public void filterAdded(Object propertyId,
            Class<? extends Filter> filterType, Object value) {
        // TODO Auto-generated method stub
    }
}
