package org.tepi.filtertable.numberfilter;

/**
 * 
 * Provides way to set decorative configurations for the {@link NumberFilterPopup)
 * @author Vimukthi
 * 
 */
public class NumberFilterPopupConfig {
    private String ltPrompt;
    private String gtPrompt;
    private String eqPrompt;
    private String okCaption;
    private String resetCaption;
    private String valueMarker;

    /**
     * @return the ltPrompt
     */
    public String getLtPrompt() {
        return ltPrompt;
    }

    /**
     * @param ltPrompt
     *            the ltPrompt to set
     */
    public void setLtPrompt(String ltPrompt) {
        this.ltPrompt = ltPrompt;
    }

    /**
     * @return the gtPrompt
     */
    public String getGtPrompt() {
        return gtPrompt;
    }

    /**
     * @param gtPrompt
     *            the gtPrompt to set
     */
    public void setGtPrompt(String gtPrompt) {
        this.gtPrompt = gtPrompt;
    }

    /**
     * @return the eqPrompt
     */
    public String getEqPrompt() {
        return eqPrompt;
    }

    /**
     * @param eqPrompt
     *            the eqPrompt to set
     */
    public void setEqPrompt(String eqPrompt) {
        this.eqPrompt = eqPrompt;
    }

    /**
     * @return the okCaption
     */
    public String getOkCaption() {
        return okCaption;
    }

    /**
     * @param okCaption
     *            the okCaption to set
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
     * @param resetCaption
     *            the resetCaption to set
     */
    public void setResetCaption(String resetCaption) {
        this.resetCaption = resetCaption;
    }

    public String getValueMarker() {
        return valueMarker;
    }

    public void setValueMarker(String valueMarker) {
        this.valueMarker = valueMarker;
    }
}
