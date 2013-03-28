package org.tepi.filtertable.datefilter;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple date interval providing isBetween comparison for other Date objects.
 * 
 * @author Teppo Kurki
 * 
 */
public class DateInterval implements Serializable {
    private final Date from;
    private final Date to;

    public DateInterval(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public boolean isNull() {
        return from == null && to == null;
    }
}
