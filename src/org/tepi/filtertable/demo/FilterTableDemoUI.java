package org.tepi.filtertable.demo;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.paged.PagedFilterTable;

import com.vaadin.annotations.Title;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Title("FilterTable Demo Application")
public class FilterTableDemoUI extends UI {

    /**
     * Example enum for enum filtering feature
     */
    enum State {
        CREATED, PROCESSING, PROCESSED, FINISHED;
    }

    @Override
    protected void init(VaadinRequest request) {
        setLocale(new Locale("fi", "FI"));
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, false, false, false));
        mainLayout.setSizeFull();

        final TabSheet ts = new TabSheet();
        ts.setStyleName(Reindeer.TABSHEET_MINIMAL);
        ts.setSizeFull();
        mainLayout.addComponent(ts);
        mainLayout.setExpandRatio(ts, 1);

        ts.addTab(buildNormalTableTab(), "Normal FilterTable");
        ts.addTab(buildPagedTableTab(), "Paged FilterTable");

        setContent(mainLayout);
    }

    private Component buildNormalTableTab() {
        /* Create FilterTable */
        FilterTable normalFilterTable = buildFilterTable();
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(normalFilterTable);
        mainLayout.setExpandRatio(normalFilterTable, 1);
        mainLayout.addComponent(buildButtons(normalFilterTable));

        Panel p = new Panel();
        p.setStyleName(Reindeer.PANEL_LIGHT);
        p.setSizeFull();
        p.setContent(mainLayout);

        return p;
    }

    private FilterTable buildFilterTable() {
        FilterTable filterTable = new FilterTable();
        filterTable.setSizeFull();

        filterTable.setFilterDecorator(new DemoFilterDecorator());
        filterTable.setFilterGenerator(new DemoFilterGenerator());

        filterTable.setFilterBarVisible(true);

        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);

        filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

        filterTable.setColumnCollapsingAllowed(true);

        filterTable.setColumnCollapsed("state", true);

        filterTable.setColumnReorderingAllowed(true);

        filterTable.setContainerDataSource(buildContainer());

        filterTable.setVisibleColumns(new String[] { "name", "id", "state",
                "date", "validated", "checked" });

        return filterTable;
    }

    private Component buildPagedTableTab() {
        /* Create FilterTable */
        PagedFilterTable<IndexedContainer> pagedFilterTable = buildPagedFilterTable();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(pagedFilterTable);
        mainLayout.addComponent(pagedFilterTable.createControls());
        mainLayout.addComponent(buildButtons(pagedFilterTable));
        return mainLayout;
    }

    private PagedFilterTable<IndexedContainer> buildPagedFilterTable() {
        PagedFilterTable<IndexedContainer> filterTable = new PagedFilterTable<IndexedContainer>();
        filterTable.setWidth("100%");

        filterTable.setFilterDecorator(new DemoFilterDecorator());
        filterTable.setFilterGenerator(new DemoFilterGenerator());

        filterTable.setFilterBarVisible(true);

        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);

        filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

        filterTable.setColumnCollapsingAllowed(true);

        filterTable.setColumnCollapsed("state", true);

        filterTable.setColumnReorderingAllowed(true);

        filterTable.setContainerDataSource(buildContainer());

        filterTable.setVisibleColumns(new String[] { "name", "id", "state",
                "date", "validated", "checked" });

        return filterTable;
    }

    private Component buildButtons(final FilterTable relatedFilterTable) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setHeight(null);
        buttonLayout.setWidth("100%");
        buttonLayout.setSpacing(true);

        Label hideFilters = new Label("Show Filters:");
        hideFilters.setSizeUndefined();
        buttonLayout.addComponent(hideFilters);
        buttonLayout.setComponentAlignment(hideFilters, Alignment.MIDDLE_LEFT);

        for (Object propId : relatedFilterTable.getContainerPropertyIds()) {
            Component t = createToggle(relatedFilterTable, propId);
            buttonLayout.addComponent(t);
            buttonLayout.setComponentAlignment(t, Alignment.MIDDLE_LEFT);
        }

        CheckBox showFilters = new CheckBox("Toggle Filter Bar visibility");
        showFilters.setValue(relatedFilterTable.isFilterBarVisible());
        showFilters.setImmediate(true);
        showFilters.addValueChangeListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                relatedFilterTable.setFilterBarVisible((Boolean) event
                        .getProperty().getValue());

            }
        });
        buttonLayout.addComponent(showFilters);
        buttonLayout.setComponentAlignment(showFilters, Alignment.MIDDLE_RIGHT);
        buttonLayout.setExpandRatio(showFilters, 1);

        Button setVal = new Button("Set the State filter to 'Processed'");
        setVal.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                relatedFilterTable
                        .setFilterFieldValue("state", State.PROCESSED);
            }
        });
        buttonLayout.addComponent(setVal);

        Button reset = new Button("Reset");
        reset.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                relatedFilterTable.resetFilters();
            }
        });
        buttonLayout.addComponent(reset);

        return buttonLayout;
    }

    @SuppressWarnings("unchecked")
    private Container buildContainer() {
        IndexedContainer cont = new IndexedContainer();
        Calendar c = Calendar.getInstance();

        cont.addContainerProperty("name", String.class, null);
        cont.addContainerProperty("id", Integer.class, null);
        cont.addContainerProperty("state", State.class, null);
        cont.addContainerProperty("date", Timestamp.class, null);
        cont.addContainerProperty("validated", Boolean.class, null);
        cont.addContainerProperty("checked", Boolean.class, null);

        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            cont.addItem(i);
            /* Set name and id properties */
            cont.getContainerProperty(i, "name").setValue("Order " + i);
            cont.getContainerProperty(i, "id").setValue(i);
            /* Set state property */
            int rndInt = random.nextInt(4);
            State stateToSet = State.CREATED;
            if (rndInt == 0) {
                stateToSet = State.PROCESSING;
            } else if (rndInt == 1) {
                stateToSet = State.PROCESSED;
            } else if (rndInt == 2) {
                stateToSet = State.FINISHED;
            }
            cont.getContainerProperty(i, "state").setValue(stateToSet);
            /* Set date property */
            cont.getContainerProperty(i, "date").setValue(
                    new Timestamp(c.getTimeInMillis()));
            c.add(Calendar.DAY_OF_MONTH, 1);
            /* Set validated property */
            cont.getContainerProperty(i, "validated").setValue(
                    random.nextBoolean());
            /* Set checked property */
            cont.getContainerProperty(i, "checked").setValue(
                    random.nextBoolean());
        }
        return cont;
    }

    private Component createToggle(final FilterTable relatedFilterTable,
            final Object propId) {
        CheckBox toggle = new CheckBox(propId.toString());
        toggle.setValue(relatedFilterTable.isFilterFieldVisible(propId));
        toggle.setImmediate(true);
        toggle.addValueChangeListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                relatedFilterTable.setFilterFieldVisible(propId,
                        !relatedFilterTable.isFilterFieldVisible(propId));
            }
        });
        return toggle;
    }
}