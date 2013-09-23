package org.tepi.filtertable.paged;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PagedTableChangeEvent implements Serializable {
    private final PagedFilterTable<?> table;

    public PagedTableChangeEvent(PagedFilterTable<?> table) {
        this.table = table;
    }

    public PagedFilterTable<?> getTable() {
        return table;
    }

    public int getCurrentPage() {
        return table.getCurrentPage();
    }

    public int getTotalAmountOfPages() {
        return table.getTotalAmountOfPages();
    }
}
