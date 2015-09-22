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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.Focusable;
import com.vaadin.client.MeasuredSize;
import com.vaadin.client.Util;
import com.vaadin.client.ui.VCustomScrollTable;
import com.vaadin.client.ui.VCustomScrollTable.VScrollTableBody.VScrollTableRow;
import com.vaadin.client.ui.dd.VLazyInitItemIdentifiers;
import com.vaadin.shared.ui.dd.AcceptCriterion;
import com.vaadin.ui.CustomTable;

public class VFilterTable extends VCustomScrollTable {

    @AcceptCriterion(CustomTable.TableDropCriterion.class)
    final public static class VTableLazyInitItemIdentifiers extends
            VLazyInitItemIdentifiers {
        // all logic in superclass
    }

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

        /* Wrap filters with additional div for styling? */
        boolean wrapFilters = false;

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
            if (!wrapFilters) {
                reRenderNotWrappedFilterComponents();
            } else {
                reRenderWrappedFilterComponents();
            }
        }

        private void reRenderNotWrappedFilterComponents() {
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

        private void reRenderWrappedFilterComponents() {
            // Remember height
            MeasuredSize ms = new MeasuredSize();
            ms.measure(container.getElement());
            int height = (int) ms.getInnerHeight();

            int visibleCellCount = tHead.getVisibleCellCount();
            for (int i = 0; i < visibleCellCount; i++) {
                String key = getColKeyByIndex(i);
                if (key == null) {
                    continue;
                }
                Widget widget = filters.get(key);
                SimplePanel wrapper = new SimplePanel();
                wrapper.addStyleName("filterwrapper");

                if (0 == i) {
                    wrapper.addStyleName("filterwrapper-first");
                }
                if (visibleCellCount - 1 == i) {
                    wrapper.addStyleName("filterwrapper-last");
                }

                if (widget == null) {
                    /* No filter defined */
                    /* Use a place holder of the correct width */
                    Widget placeHolder = new FlowPanel();
                    placeHolder.addStyleName("filterplaceholder");
                    wrapper.setWidget(placeHolder);
                    container.add(wrapper);
                    filters.put(key, placeHolder);
                    setWrappedFilterWidth(i);
                } else {
                    wrapper.setWidget(widget);
                    container.add(wrapper);
                    setWrappedFilterWidth(i);
                }

                // deal with wrapper height
                MeasuredSize wrapperSize = new MeasuredSize();
                wrapperSize.measure(wrapper.getElement());
                int correction = wrapperSize.getMarginHeight()
                        + wrapperSize.getBorderHeight()
                        + wrapperSize.getPaddingHeight();
                int wrapperHeight = height - correction;
                // ensure no negative heights
                wrapperHeight = wrapperHeight > 0 ? wrapperHeight : 0;
                wrapper.setHeight(wrapperHeight + "px");
            }
        }

        public void setScrollLeft(int scrollLeft) {
            wrap.getElement().setScrollLeft(scrollLeft);
        }

        private void setNotWrappedFilterWidth(int index) {
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
                w -= measuredSize.getMarginWidth();
                /* Ensure no negative widths are set */
                w = w > 0 ? w : 0;
                p.setWidth(w + "px");
            }
        }

        private void setWrappedFilterWidth(int index) {
            Widget widget = filters.get(getColKeyByIndex(index));
            if (null != widget) {
                Widget wrapper = widget.getParent();
                int wrapperWidth = Util.getRequiredWidth(tHead
                        .getHeaderCell(index));
                if (wrapperWidth <= 0) {
                    VScrollTableRow firstRow = scrollBody
                            .getRowByRowIndex(scrollBody.getFirstRendered());
                    final Element cell = DOM.getChild(firstRow.getElement(),
                            index);
                    wrapperWidth = Util.getRequiredWidth(cell);
                }
                MeasuredSize wrapperSize = new MeasuredSize();
                wrapperSize.measure(wrapper.getElement());
                int wrapperCorrections = wrapperSize.getMarginWidth()
                        + wrapperSize.getBorderWidth()
                        + wrapperSize.getPaddingWidth();
                wrapperWidth = wrapperWidth - wrapperCorrections;
                wrapperWidth = wrapperWidth > 0 ? wrapperWidth : 0;
                wrapper.setWidth(wrapperWidth + "px");

                if (0 < wrapperWidth) {
                    int widgetWidth = wrapperWidth;
                    MeasuredSize widgetSize = new MeasuredSize();
                    widgetSize.measure(widget.getElement());
                    widgetWidth -= widgetSize.getMarginWidth();
                    widgetWidth = widgetWidth > 0 ? widgetWidth : 0;
                    widget.setWidth(widgetWidth + "px");
                }
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

        private void setFilterWidth(int index) {
            if (headerChangedDuringUpdate) {
                return;
            }
            if (!wrapFilters) {
                setNotWrappedFilterWidth(index);
            } else {
                setWrappedFilterWidth(index);
            }
        }
    }

    public void setColumnHeaderStylenames(String[] columnHeaderStylenames) {

        for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
            HeaderCell cell = tHead.getHeaderCell(i);
            String stylename = columnHeaderStylenames[i];
            if (!stylename.isEmpty()) {
                cell.addStyleName(columnHeaderStylenames[i]);
            }
        }

    }
}
