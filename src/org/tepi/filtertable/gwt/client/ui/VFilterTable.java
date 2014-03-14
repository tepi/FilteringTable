package org.tepi.filtertable.gwt.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.Focusable;
import com.vaadin.client.MeasuredSize;
import com.vaadin.client.Util;
import com.vaadin.client.ui.VCustomScrollTable;
import com.vaadin.client.ui.VCustomScrollTable.VScrollTableBody.VScrollTableRow;

public class VFilterTable extends VCustomScrollTable {

    /* Custom FlowPanel for the column filters */
    FilterPanel filters;

    public VFilterTable() {
        super();

        /* Create filter panel and insert between CustomTable Header and Content */
        filters = new FilterPanel();
        insert(filters, 1);

        /*
         * Do not display the filter bar initially.
         * 
         * This is a tentative fix for a weird issue where the width of the
         * FilteringTable is not calculated correctly when it is contained
         * within a Window.
         */
        filters.getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void setContainerHeight() {
        if (!isDynamicHeight()) {

            /*
             * Android 2.3 cannot measure the height of the inline-block
             * properly, and will return the wrong offset height. So for android
             * 2.3 we set the element to a block element while measuring and
             * then restore it which yields the correct result. #11331
             */
            if (BrowserInfo.get().isAndroid23()) {
                getElement().getStyle().setDisplay(Display.BLOCK);
            }

            containerHeight = getOffsetHeight();
            containerHeight -= showColHeaders ? tHead.getOffsetHeight() : 0;
            containerHeight -= tFoot.getOffsetHeight();
            containerHeight -= getContentAreaBorderHeight();
            /* Account for possibly visible table filter row */
            if (filters.isVisible()) {
                containerHeight -= filters.getOffsetHeight();
            }
            if (containerHeight < 0) {
                containerHeight = 0;
            }

            scrollBodyPanel.setHeight(containerHeight + "px");

            if (BrowserInfo.get().isAndroid23()) {
                getElement().getStyle().clearDisplay();
            }
        }
    }

    @Override
    protected void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
        super.setColWidth(colIndex, w, isDefinedWidth);
        filters.setFilterWidth(colIndex);
    }

    @Override
    protected void setContentWidth(int pixels) {
        filters.setWidth(pixels + "px");
        super.setContentWidth(pixels);
    }

    @Override
    protected void reOrderColumn(String columnKey, int newIndex) {
        super.reOrderColumn(columnKey, newIndex);
        filters.reRenderFilterComponents();
    }

    @Override
    public void onScroll(ScrollEvent event) {
        super.onScroll(event);
        filters.setScrollLeft(scrollLeft);
    }

    protected String getColKeyByIndex(int index) {
        HeaderCell hc = tHead.getHeaderCell(index);
        return hc == null ? null : hc.getColKey();
    }

    @Override
    public void onFocus(FocusEvent event) {
        super.onFocus(event);
    }

    class FilterPanel extends FlowPanel implements ScrollHandler {
        /* Column filter components - mapped by column keys */
        Map<String, Widget> filters = new HashMap<String, Widget>();
        /* Set to true to render the filter bar */
        boolean filtersVisible;
        /* Wrapper that holds the filter component container */
        private final FlowPanel wrap = new FlowPanel();
        /* Actual container for the filter components */
        FlowPanel container = new FlowPanel();

        public FilterPanel() {
            container.setStyleName("filters-panel");
            DOM.setStyleAttribute(wrap.getElement(), "overflow", "hidden");
            setStyleName("filters-wrap");
            wrap.sinkEvents(Event.ONSCROLL);
            wrap.addDomHandler(this, ScrollEvent.getType());
            wrap.add(container);
            add(wrap);
        }

        void reRenderFilterComponents() {
            container.clear();
            for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
                String key = getColKeyByIndex(i);
                if (key == null) {
                    continue;
                }
                Widget widget = filters.get(key);

                if (widget == null) {
                    /* No filter defined */
                    /* Use a place holder of the correct width */
                    Widget placeHolder = new FlowPanel();
                    placeHolder.addStyleName("filterplaceholder");
                    container.add(placeHolder);
                    filters.put(key, placeHolder);
                    setFilterWidth(i);
                } else {
                    container.add(widget);
                    setFilterWidth(i);
                }
            }
        }

        public void setScrollLeft(int scrollLeft) {
            wrap.getElement().setScrollLeft(scrollLeft);
        }

        private void setFilterWidth(int index) {
            Widget p = filters.get(getColKeyByIndex(index));
            if (p != null) {
                /* Try to get width from header cell */
                int w = Util.getRequiredWidth(tHead.getHeaderCell(index));
                if (w <= 0) {
                    /* Header not available, try first rendered row */
                    VScrollTableRow firstRow = scrollBody
                            .getRowByRowIndex(scrollBody.getFirstRendered());
                    final Element cell = DOM.getChild(firstRow.getElement(),
                            index);
                    w = Util.getRequiredWidth(cell);
                }
                MeasuredSize measuredSize = new MeasuredSize();
                measuredSize.measure(p.getElement());
                int margins = measuredSize.getMarginLeft()
                        + measuredSize.getMarginRight();
                w -= margins;
                /* Ensure no negative widths are set */
                w = w > 0 ? w : 0;
                p.setWidth(w + "px");
            }
        }

        @Override
        public void onScroll(ScrollEvent event) {
            if (!isAttached()) {
                return;
            }
            scrollLeft = wrap.getElement().getScrollLeft();
            scrollBodyPanel.getElement().setScrollLeft(scrollLeft);
            tHead.getElement().setScrollLeft(scrollLeft);
        }

        public void focusWidget(Widget filterToFocus) {
            if (filterToFocus == null) {
                return;
            } else if (filterToFocus instanceof FocusWidget) {
                ((FocusWidget) filterToFocus).setFocus(true);
            } else if (filterToFocus instanceof Focusable) {
                ((Focusable) filterToFocus).focus();
            }
        }

        public void resetFilterWidths() {
            for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
                setFilterWidth(i);
            }
        }
    }
}
