package org.tepi.filtertable.paged;

public class PagedTableChangeEvent {
    final PagedFilterTable<?> table;

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
