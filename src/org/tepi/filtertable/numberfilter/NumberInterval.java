package org.tepi.filtertable.numberfilter;

import java.io.Serializable;

/**
 * @author Vimukthi
 * 
 */
public class NumberInterval implements Serializable {

    private final String lessThanValue;
    private final String greaterThanValue;
    private final String equalsValue;

    public NumberInterval(String lessThanValue, String greaterThanValue,
            String equalsValue) {
        super();
        if (lessThanValue != null && !lessThanValue.trim().isEmpty()) {
            this.lessThanValue = lessThanValue;
        } else {
            this.lessThanValue = null;
        }

        if (greaterThanValue != null && !greaterThanValue.trim().isEmpty()) {
            this.greaterThanValue = greaterThanValue;
        } else {
            this.greaterThanValue = null;
        }

        if (equalsValue != null && !equalsValue.trim().isEmpty()) {
            this.equalsValue = equalsValue;
        } else {
            this.equalsValue = null;
        }
    }

    public String getLessThanValue() {
        return lessThanValue;
    }

    public String getGreaterThanValue() {
        return greaterThanValue;
    }

    public String getEqualsValue() {
        return equalsValue;
    }
}
