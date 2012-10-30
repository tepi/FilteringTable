package org.tepi.filtertable.numberfilter;

/**
 * @author Vimukthi
 * 
 */
public class NumberInterval {

    private final String lessThanValue;
    private final String greaterThanValue;
    private final String equalsValue;

    NumberInterval(String lessThanValue, String greaterThanValue,
            String equalsValue) {
        super();
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
