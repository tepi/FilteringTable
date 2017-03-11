package org.tepi.filtertable.paged;

import java.util.ArrayList;
import java.util.List;

public class PagedFilterControlConfig {

    private String itemsPerPage = "Items per page:";
    private String page = "Page:";

    private String first = "<<";
    private String last = ">>";

    private String previous = "<";
    private String next = ">";

    private List<Integer> pageLengths;

    public String getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(String itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<Integer> getPageLengths() {
        return pageLengths;
    }

    public void setPageLengthsAndCaptions(List<Integer> pageLengths) {
        this.pageLengths = pageLengths;
    }

    public PagedFilterControlConfig() {
        pageLengths = new ArrayList<Integer>();
        pageLengths.add(5);
        pageLengths.add(10);
        pageLengths.add(25);
        pageLengths.add(50);
        pageLengths.add(100);
    }
}
