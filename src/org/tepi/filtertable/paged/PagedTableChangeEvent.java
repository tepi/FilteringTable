package org.tepi.filtertable.paged;

class PagedTableChangeEvent {
    private final PagedFilterTable<?> table;

    PagedTableChangeEvent(PagedFilterTable<?> table) {
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
