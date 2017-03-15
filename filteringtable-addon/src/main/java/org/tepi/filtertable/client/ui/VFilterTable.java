package org.tepi.filtertable.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.MeasuredSize;
import com.vaadin.client.ValueMap;
import com.vaadin.client.WidgetUtil;
import com.vaadin.v7.client.ui.VScrollTable;
import com.vaadin.v7.client.ui.VScrollTable.VScrollTableBody.VScrollTableRow;

/**
 * VFilterTable.
 * 
 * @author Teppo Kurki
 * @since 27.10.2014
 */
public class VFilterTable extends VScrollTable {

	private static final Logger LOG = Logger.getLogger(VFilterTable.class.getName());

	private static final String FILTER_PANEL_STYLE = "filters-panel";
	private static final String FILTER_WRAPPER_STYLE = "filterwrapper";
	private static final String FILTER_WRAPPER_FIRST_STYLE = "filterwrapper-first";
	private static final String FILTER_WRAPPER_LAST_STYLE = "filterwrapper-last";
	private static final String FILTER_PLACEHOLDER_STYLE = "filterplaceholder";
	private static final String FILTER_TABLE_HEADER_WRAP_STYLE = "v-filter-table-header-wrap";
	private static final String FILTER_TABLE_HEADER_STYLE = "v-filter-table-header";
	private static final String FILTER_TABLE_HEADER_CELL_STYLE = "v-table-header-cell";
	private static final String FILTER_TABLE_COLUMN_SELECTOR_STYLE = "v-filter-table-column-selector";

	private final Element tHeadTBodyElement;
	private final Element tHeadTableHeaderDiv;
	private final Element tHeadColumnSelectorDiv;
	private final Element filterTrElement;

	/* Set to true to render the filter bar */
	private boolean filtersVisible;
	/* Column filter components - mapped by column keys */
	public Map<String, Widget> filters = new HashMap<String, Widget>();

	private ValueMap columnHeaderStylenames;

	public VFilterTable() {
		super();

		Element tHeadElement = tHead.getElement();
		tHeadTBodyElement = findChildElement(tHeadElement, "tbody");
		if (tHeadTBodyElement == null) {
			assert tHeadTBodyElement != null;
			tHeadTableHeaderDiv = tHeadColumnSelectorDiv = filterTrElement = null;
			if (LOG.isLoggable(Level.WARNING))
				LOG.warning("Unable to find tBody element in table's header. Filter won't work!");
		} else {
			if (tHeadElement.getChildCount() > 1) {
				tHeadTableHeaderDiv = DOM.getChild(tHeadElement, 0);
				tHeadColumnSelectorDiv = DOM.getChild(tHeadElement, 1);
			} else {
				tHeadColumnSelectorDiv = tHeadTableHeaderDiv = null;
				if (LOG.isLoggable(Level.WARNING))
					LOG.warning("Unable to find table header div and column selector div in table's header.");
			}

			tHeadTBodyElement.appendChild(filterTrElement = DOM.createTR());
			filterTrElement.addClassName(FILTER_PANEL_STYLE);
			setFiltersVisible(true);
		}
	}

	@Override
	protected void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
		super.setColWidth(colIndex, w, isDefinedWidth);
		setFilterWidth(colIndex);
	}

	@Override
	protected void reOrderColumn(String columnKey, int newIndex) {
		super.reOrderColumn(columnKey, newIndex);
		reRenderFilterComponents();
	}

	@Override
	public void onUnregister() {
		super.onUnregister();
		filters.clear();
	}

	/**
	 * Changes the visibility of the table filters.
	 *
	 * @param filtersVisible
	 *            {@code true} to display filters. Otherwise {@code false}
	 */
	public void setFiltersVisible(boolean filtersVisible) {
		if (this.filtersVisible != filtersVisible) {
			this.filtersVisible = filtersVisible;

			if (filterTrElement != null) {
				if (this.filtersVisible) {
					tHeadTBodyElement.appendChild(filterTrElement);
					tHead.addStyleName(FILTER_TABLE_HEADER_WRAP_STYLE);
					if (tHeadTableHeaderDiv != null) {
						tHeadTableHeaderDiv.addClassName(FILTER_TABLE_HEADER_STYLE);
						tHeadColumnSelectorDiv.addClassName(FILTER_TABLE_COLUMN_SELECTOR_STYLE);
					}
				} else {
					tHeadTBodyElement.removeChild(filterTrElement);
					tHead.removeStyleName(FILTER_TABLE_HEADER_WRAP_STYLE);
					if (tHeadTableHeaderDiv != null) {
						tHeadTableHeaderDiv.removeClassName(FILTER_TABLE_HEADER_STYLE);
						tHeadColumnSelectorDiv.addClassName(FILTER_TABLE_COLUMN_SELECTOR_STYLE);
					}
				}
			}
		}
	}

	/**
	 * Removes all filters from the filter row and re-adds them.
	 */
	public void reRenderFilterComponents() {
		filterTrElement.removeAllChildren();

		/* Remember height */
		MeasuredSize ms = new MeasuredSize();
		ms.measure(filterTrElement);
		int height = (int) ms.getInnerHeight();

		int visibleCellCount = tHead.getVisibleCellCount();
		for (int i = 0; i < visibleCellCount; i++) {
			String key = tHead.getHeaderCell(i).getColKey();
			if (key != null) {
				Widget widget = filters.get(key);

				SimplePanel wrapper = new SimplePanel();
				wrapper.addStyleName(FILTER_WRAPPER_STYLE);
				if (i == 0) {
					wrapper.addStyleName(FILTER_WRAPPER_FIRST_STYLE);
				} else if (i == visibleCellCount - 1) {
					wrapper.addStyleName(FILTER_WRAPPER_LAST_STYLE);
				}

				if (widget == null) {
					/*
					 * No filter defined -> Use a place holder of the correct
					 * width
					 */
					widget = new FlowPanel();
					widget.addStyleName(FILTER_PLACEHOLDER_STYLE);
					filters.put(key, widget);
				}

				wrapper.setWidget(widget);

				Element filterColumn = DOM.createTD();
				filterColumn.addClassName(FILTER_TABLE_HEADER_CELL_STYLE);
				filterTrElement.appendChild(filterColumn);

				wrapper.removeFromParent();
				filterColumn.appendChild(wrapper.getElement());
				adopt(wrapper);

				/* deal with wrapper height */
				MeasuredSize wrapperSize = new MeasuredSize();
				wrapperSize.measure(wrapper.getElement());
				int correction = wrapperSize.getMarginHeight() + wrapperSize.getBorderHeight()
						+ wrapperSize.getPaddingHeight();
				/* ensure no negative heights */
				int wrapperHeight = Math.max(height - correction, 0);
				wrapper.setHeight(wrapperHeight + "px");

				setFilterWidth(i);

				if (columnHeaderStylenames != null) {
					String styleName = columnHeaderStylenames.getString(key);
					if (styleName != null && !styleName.trim().isEmpty()) {
						tHead.getHeaderCell(i).addStyleName(columnHeaderStylenames.getString(key));
						wrapper.addStyleName(columnHeaderStylenames.getString(key));
					}
				}
			}
		}
	}

	/**
	 * Recalculates and re-sets the width of all table filters.
	 */
	public void resetFilterWidths() {
		for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
			setFilterWidth(i);
		}
	}

	private void setFilterWidth(int index) {
		if (headerChangedDuringUpdate) {
			return;
		}

		HeaderCell headerCell = tHead.getHeaderCell(index);
		if (headerCell != null) {
			Widget widget = filters.get(headerCell.getColKey());
			if (null != widget) {
				/*
				 * try to get width from first rendered row -> fixes 1px bug in
				 * GC
				 */
				final VScrollTableRow firstRow = scrollBody.getRowByRowIndex(scrollBody.getFirstRendered());
				int wrapperWidth = -1;
				if (firstRow != null) {
					final Element cell = DOM.getChild(firstRow.getElement(), index);
					wrapperWidth = WidgetUtil.getRequiredWidth(cell);
				}
				if (wrapperWidth <= 0) {
					wrapperWidth = WidgetUtil.getRequiredWidth(headerCell);
				}

				Widget wrapper = widget.getParent();
				MeasuredSize wrapperSize = new MeasuredSize();
				wrapperSize.measure(wrapper.getElement());
				int wrapperCorrections = wrapperSize.getMarginWidth() + wrapperSize.getBorderWidth()
						+ wrapperSize.getPaddingWidth();
				wrapperWidth = wrapperWidth - wrapperCorrections;
				wrapper.setWidth((wrapperWidth > 0 ? wrapperWidth : 0) + "px");

				if (0 < wrapperWidth) {
					int widgetWidth = wrapperWidth;
					MeasuredSize widgetSize = new MeasuredSize();
					widgetSize.measure(widget.getElement());
					widgetWidth -= widgetSize.getMarginWidth();
					widget.setWidth((widgetWidth > 0 ? widgetWidth : 0) + "px");
				}
			}
		}
	}

	public void setColumnHeaderStylenames(ValueMap valueMap) {
		this.columnHeaderStylenames = valueMap;
	}

	/**
	 * Helper method to find first instance of given child element {@code type}
	 * found by traversing DOM downwards from given {@code element}. If no
	 * matching child can be found {@code null} is returned.
	 *
	 * @param parent
	 *            the element where to start seeking of child element, not
	 *            {@code null}
	 * @param type
	 *            type of child element to seek for, not {@code null}
	 * @return first child of {@code type} or {@code null} if none was found
	 */
	public static Element findChildElement(final Element parent, final String type) {
		if (parent != null && type != null) {
			Element child = null;
			int count = DOM.getChildCount(parent);
			for (int i = 0; child == null && i < count; i++) {
				Element element = DOM.getChild(parent, i);
				String nodeName = element.getPropertyString("nodeName");
				child = type.equalsIgnoreCase(nodeName) ? element : findChildElement(element, type);
			}

			return child;
		}

		return null;
	}
}
