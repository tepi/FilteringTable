package org.tepi.filtertable.paged;

public class PagedTableChangeEvent {
    final PagedFilteringTable<?> table;

    public PagedTableChangeEvent(PagedFilteringTable<?> table) {
        this.table = table;
    }

    public PagedFilteringTable<?> getTable() {
        return table;
    }

    public int getCurrentPage() {
        return table.getCurrentPage();
    }

    public int getTotalAmountOfPages() {
        return table.getTotalAmountOfPages();
    }
}
