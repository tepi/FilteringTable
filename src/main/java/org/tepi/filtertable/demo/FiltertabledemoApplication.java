package org.tepi.filtertable.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.tepi.filtertable.FilterTable;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.CustomTable.ColumnGenerator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FiltertabledemoApplication extends Application {

    /**
     * Example enum for enum filtering feature
     */
    enum State {
        CREATED, PROCESSING, PROCESSED, FINISHED;
    }

    private FilterTable filterTable;

    @Override
    public void init() {
        setLocale(new Locale("fi", "FI"));

        Window mainWindow = new Window("FilterTable Demo Application");
        setMainWindow(mainWindow);

        /* Create FilterTable */
        filterTable = buildFilterTable();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(filterTable);
        mainLayout.setExpandRatio(filterTable, 1);
        mainLayout.addComponent(buildButtons());

        mainWindow.setContent(mainLayout);
    }

    private FilterTable buildFilterTable() {
        FilterTable filterTable = new FilterTable("FilterTable Demo");

        filterTable.setSizeFull();

        filterTable.setFilterDecorator(new DemoFilterDecorator());
        filterTable
                .setFilterGenerator(new DemoFilterGenerator(getMainWindow()));

        filterTable.setFilterBarVisible(true);

        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(true);

        filterTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);

        filterTable.setColumnCollapsingAllowed(true);

        filterTable.setColumnCollapsed("state", true);

        filterTable.setColumnReorderingAllowed(true);

        filterTable.setContainerDataSource(buildContainer());

        filterTable.addGeneratedColumn("foo", new ColumnGenerator() {

            public Object generateCell(CustomTable source, Object itemId,
                    Object columnId) {
                return "testing";
            }
        });

        filterTable.setVisibleColumns(new String[] { "name", "id", "foo",
                "state", "date", "validated", "checked" });

        return filterTable;
    }

    private Component buildButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setHeight(null);
        buttonLayout.setWidth("100%");
        buttonLayout.setSpacing(true);

        Label hideFilters = new Label("Show Filters:");
        hideFilters.setSizeUndefined();
        buttonLayout.addComponent(hideFilters);
        buttonLayout.setComponentAlignment(hideFilters, Alignment.MIDDLE_LEFT);

        for (Object propId : filterTable.getContainerPropertyIds()) {
            Component t = createToggle(propId);
            buttonLayout.addComponent(t);
            buttonLayout.setComponentAlignment(t, Alignment.MIDDLE_LEFT);
        }

        CheckBox showFilters = new CheckBox("Toggle Filter Bar visibility");
        showFilters.setValue(filterTable.isFilterBarVisible());
        showFilters.setImmediate(true);
        showFilters.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                filterTable.setFilterBarVisible((Boolean) event.getProperty()
                        .getValue());

            }
        });
        buttonLayout.addComponent(showFilters);
        buttonLayout.setComponentAlignment(showFilters, Alignment.MIDDLE_RIGHT);
        buttonLayout.setExpandRatio(showFilters, 1);

        Button setVal = new Button("Set the State filter to 'Processed'");
        setVal.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                filterTable.setFilterFieldValue("state", State.PROCESSED);
            }
        });
        buttonLayout.addComponent(setVal);

        Button reset = new Button("Reset");
        reset.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                filterTable.resetFilters();
            }
        });
        buttonLayout.addComponent(reset);

        return buttonLayout;
    }

    private Container buildContainer() {
        IndexedContainer cont = new IndexedContainer();
        Calendar c = Calendar.getInstance();

        cont.addContainerProperty("name", String.class, null);
        cont.addContainerProperty("id", Integer.class, null);
        cont.addContainerProperty("state", State.class, null);
        cont.addContainerProperty("date", Date.class, null);
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
            cont.getContainerProperty(i, "date").setValue(c.getTime());
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

    private Component createToggle(final Object propId) {
        CheckBox toggle = new CheckBox(propId.toString());
        toggle.setValue(filterTable.isFilterFieldVisible(propId));
        toggle.setImmediate(true);
        toggle.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                filterTable.setFilterFieldVisible(propId,
                        !filterTable.isFilterFieldVisible(propId));
            }
        });
        return toggle;
    }
}
