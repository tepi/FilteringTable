package org.tepi.filtertable.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.tepi.filtertable.FilterTable;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class FiltertabledemoApplication extends Application {

    /**
     * Example enum for enum filtering feature
     */
    public enum State {
        CREATED, PROCESSING, PROCESSED, FINISHED;
    }

    private FilterTable filterTable;

    @Override
    public void init() {
        setTheme("runo");
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
        FilterTable filterTable = new FilterTable();
        filterTable.setSizeFull();
        filterTable.setFilterDecorator(new DemoFilterDecorator());
        filterTable.setFilterGenerator(new DemoFilterGenerator());
        filterTable.setContainerDataSource(buildContainer());
        filterTable.setFiltersVisible(true);
        return filterTable;
    }

    private Component buildButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSizeUndefined();
        buttonLayout.setSpacing(true);
        Button showFilters = new Button("Show filter bar");
        showFilters.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                filterTable.setFiltersVisible(true);
            }
        });
        buttonLayout.addComponent(showFilters);
        Button hideFilters = new Button("Hide filter bar");
        hideFilters.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                filterTable.setFiltersVisible(false);
            }
        });
        buttonLayout.addComponent(hideFilters);
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
        }
        return cont;
    }
}
