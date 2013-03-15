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
        this.lessThanValue = lessThanValue;
        this.greaterThanValue = greaterThanValue;
        this.equalsValue = equalsValue;
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