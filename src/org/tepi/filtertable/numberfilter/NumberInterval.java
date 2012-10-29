package org.tepi.filtertable.numberfilter;

/**
 * @author Vimukthi
 * 
 */
public class NumberInterval {

    private final String ltValue;
    private final String gtValue;
    private final String eqValue;

    /**
     * @param ltValue
     * @param gtValue
     * @param eqValue
     */
    NumberInterval(String ltValue, String gtValue, String eqValue) {
        super();
        this.ltValue = ltValue;
        this.gtValue = gtValue;
        this.eqValue = eqValue;
    }

    /**
     * @return the ltValue
     */
    public String getLtValue() {
        return ltValue;
    }

    /**
     * @return the gtValue
     */
    public String getGtValue() {
        return gtValue;
    }

    /**
     * @return the eqValue
     */
    public String getEqValue() {
        return eqValue;
    }
}
