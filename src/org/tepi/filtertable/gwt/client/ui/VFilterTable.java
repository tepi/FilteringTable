package org.tepi.filtertable.gwt.client.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VCustomScrollTable;

public class VFilterTable extends VCustomScrollTable {

	/* Custom FlowPanel for the column filters */
	private FilterPanel filters;

	private Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();

	private int previousWidth;

	public VFilterTable() {
		super();
		/* Create filter panel and insert between CustomTable Header and Content */
		filters = new FilterPanel();
		insert(filters, 1);
	}

	@Override
	protected void setContainerHeight() {
		/* First calculate the width with the normal method */
		super.setContainerHeight();
		/* Account for possibly visible table filter row */
		if (height != null && !"".equals(height) && filters.isVisible()) {
			containerHeight -= filters.getOffsetHeight();
			if (containerHeight < 0) {
				containerHeight = 0;
			}
			scrollBodyPanel.setHeight(containerHeight + "px");
		}
	}

	@Override
	protected void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
		super.setColWidth(colIndex, w, isDefinedWidth);
		columnWidths.put(colIndex, w);
		filters.setFilterWidth(colIndex, w);
	}

	@Override
	protected void setContentWidth(int pixels) {
		super.setContentWidth(pixels);
		filters.setWidth(pixels + "px");
	}

	@Override
	protected void reOrderColumn(String columnKey, int newIndex) {
		super.reOrderColumn(columnKey, newIndex);
		filters.reRenderFilterComponents();
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		final int newTotalRows = uidl.getIntAttribute("totalrows");
		if (newTotalRows == 0 && previousWidth >= 0) {
			scrollBody.getElement().getStyle().setWidth(previousWidth, Unit.PX);
			scrollBody.getElement().getStyle().setHeight(1, Unit.PX);
		} else {
			scrollBody.getElement().getStyle().clearWidth();
			scrollBody.getElement().getStyle().clearHeight();
			previousWidth = findTableInnerWidth();
		}
		filters.updateFromUIDL(uidl.getChildByTagName("filters"), client);
		/* Set possible new scroll position to filter bar */
		filters.setScrollLeft(scrollBodyPanel.getElement().getScrollLeft());
	}

	private int findTableInnerWidth() {
		if (scrollBody != null) {
			for (int i = 0; i < scrollBody.getElement().getChildCount(); i++) {
				Node child = scrollBody.getElement().getChild(i);
				if ("TABLE".equals(child.getNodeName())) {
					return ((TableElement) child).getOffsetWidth();
				}
			}
		}
		return -1;
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

	private class FilterPanel extends FlowPanel implements Container,
			ScrollHandler {
		private ApplicationConnection client;
		/* Column filter components - mapped by column keys */
		private Map<String, Widget> filters = new HashMap<String, Widget>();
		/* Cache UIDL:s last used to render the filter components */
		private Map<String, UIDL> uidls = new HashMap<String, UIDL>();
		/* Set to true to render the filter bar */
		private boolean filtersVisible;
		/* Wrapper that holds the filter component container */
		private FlowPanel wrap = new FlowPanel();
		/* Actual container for the filter components */
		private FlowPanel container = new FlowPanel();

		public FilterPanel() {
			container.setStyleName("filters-panel");
			DOM.setStyleAttribute(wrap.getElement(), "overflow", "hidden");
			setStyleName("filters-wrap");
			wrap.sinkEvents(Event.ONSCROLL);
			wrap.addDomHandler(this, ScrollEvent.getType());
			wrap.add(container);
			add(wrap);
		}

		public void setScrollLeft(int scrollLeft) {
			wrap.getElement().setScrollLeft(scrollLeft);
		}

		private void setFilterWidth(int index, int width) {
			Widget p = filters.get(getColKeyByIndex(index));
			if (p != null) {
				p.setWidth(Util.getRequiredWidth(tHead.getHeaderCell(index))
						+ "px");
			}
		}

		public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
			if (uidl == null) {
				return;
			}
			this.client = client;
			filtersVisible = uidl.hasAttribute("filtersvisible") ? uidl
					.getBooleanAttribute("filtersvisible") : false;
			boolean forceRender = uidl.getBooleanAttribute("forceRender");
			boolean unRegisterOldOnes = false;
			/* If filters are not set visible, clear and hide filter panel */
			setVisible(filtersVisible);
			setContainerHeight();
			Collection<Widget> oldFilters = filters.values();
			if (!filtersVisible) {
				container.clear();
				filters.clear();
				unRegisterOldOnes = true;
			} else {
				/* Just update, don't re-render */
				uidls.clear();
				for (final Iterator<Object> it = uidl.getChildIterator(); it
						.hasNext();) {
					final UIDL cc = (UIDL) it.next();
					if (cc.getTag().equals("filtercomponent")) {
						String cid = cc.getStringAttribute("columnid");
						uidls.put(cid, cc.getChildUIDL(0));
					}
				}
				if (forceRender) {
					/* Repaint filter components */
					reRenderFilterComponents();
					unRegisterOldOnes = true;
				} else {
					/* Just update from the UIDL */
					for (String filter : filters.keySet()) {
						Widget w = filters.get(filter);
						if (w != null && w instanceof Paintable) {
							((Paintable) w).updateFromUIDL(uidls.get(filter),
									client);
						}
					}
					resetFilterWidths();
				}
			}
			if (unRegisterOldOnes) {
				Collection<Widget> newFilters = filters.values();
				for (Widget filter : oldFilters) {
					if (!newFilters.contains(filter)) {
						client.unregisterPaintable((Paintable) filter);
					}
				}
			}
		}

		private void resetFilterWidths() {
			for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
				String key = getColKeyByIndex(i);
				if (key == null) {
					continue;
				}
				setFilterWidth(i, getColWidth(key));
			}
		}

		private void reRenderFilterComponents() {
			container.clear();
			filters.clear();
			for (int i = 0; i < tHead.getVisibleCellCount(); i++) {
				String key = getColKeyByIndex(i);
				if (key == null) {
					continue;
				}
				UIDL uidl = uidls.get(key);

				if (uidl == null) {
					/* No filter defined */
					/* Use a place holder of the correct width */
					Widget placeHolder = new FlowPanel();
					placeHolder.addStyleName("filterplaceholder");
					container.add(placeHolder);
					filters.put(key, placeHolder);
					setFilterWidth(i, getColWidth(key));
					continue;
				}
				Widget filter = (Widget) client.getPaintable(uidl);
				container.add(filter);
				((Paintable) filter).updateFromUIDL(uidl, client);
				filters.put(key, filter);
				setFilterWidth(i, getColWidth(key));
			}
		}

		public RenderSpace getAllocatedSpace(Widget child) {
			return new RenderSpace(child.getOffsetWidth(), 22);
		}

		public void replaceChildComponent(Widget oldComponent,
				Widget newComponent) {
		}

		public boolean hasChildComponent(Widget component) {
			return true;
		}

		public void updateCaption(Paintable component, UIDL uidl) {
		}

		public boolean requestLayout(Set<Paintable> children) {
			return true;
		}

		public void onScroll(ScrollEvent event) {
			if (!isAttached()) {
				return;
			}
			scrollLeft = wrap.getElement().getScrollLeft();
			scrollBodyPanel.getElement().setScrollLeft(scrollLeft);
			tHead.getElement().setScrollLeft(scrollLeft);
		}
	}
}