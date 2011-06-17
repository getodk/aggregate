package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.widgets.NewFilterButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class FiltersDataPanel extends FlowPanel {

  private Tree activeFilters;
  private TreeItem title;

  private FilterGroup currentGroup;
  private List<FilterGroup> viewingFilters;

  private FilterSubTab parentPanel;

  public FiltersDataPanel(FilterSubTab panel) {
    parentPanel = panel;

    viewingFilters = new ArrayList<FilterGroup>();
    activeFilters = new Tree();

    // add new filter button
    NewFilterButton newFilter = new NewFilterButton(panel);
    activeFilters.add(newFilter);

    add(activeFilters);
    getElement().setId("filters_container");
  }

  public void updateFilters(FilterGroup group) {
    title = new TreeItem(new Label("Active Filters"));
    activeFilters.addItem(title);

    TreeItem itemGroup = loadFilterGroup(group);
    title.addItem(itemGroup);
    title.setState(true);
  }

  public TreeItem loadFilterGroup(final FilterGroup group) {

    Label groupName = new Label(group.getName());

    final FlexTable filters = new FlexTable();
    final FlexTable filterBox = new FlexTable();

    final Button saveFilterGroup = new Button("Save");
    saveFilterGroup.getElement().setPropertyObject("group", group);
    saveFilterGroup.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (filters.getRowCount() == 0) {
          Window.alert("You need at least one filter to save a group.");
        } else {
          boolean match = false;
          String newFilter;

          newFilter = Window.prompt("Please enter a name for this group", "FilterGroup");

          while (true) {
            ListBox filtersBox = parentPanel.getCurrentFilterList();
            if (newFilter != null) {
              for (int i = 0; i < filtersBox.getItemCount(); i++) {
                if ((filtersBox.getValue(i)).compareTo(newFilter) == 0
                    && newFilter.compareTo(currentGroup.getName()) != 0) {
                  match = true;
                }
              }
            }
            if (!match) {
              break;
            } else {
              match = false;
              newFilter = Window.prompt("That group already exists. Please enter a new name",
                  "FilterGroup");
            }

          }
          // Save the new filter
          addFilterGroup(newFilter, currentGroup);
          AggregateUI.getUI().getTimer().restartTimer();
        }
      }
    });
    filterBox.setWidget(0, 0, groupName);
    filterBox.setWidget(0, 1, saveFilterGroup);

    TreeItem filterGroup = new TreeItem(filterBox);

    int row = 0;
    for (Filter filter : group.getFilters()) {
      if (filter instanceof RowFilter) {
        RowFilter rowFilter = (RowFilter) filter;
        filters.setWidget(row, 0,
            new Label(rowFilter.getVisibility() + rowFilter.getColumn().getDisplayHeader()
                + "where columns are " + rowFilter.getOperation() + rowFilter.getInput()));
      } else if (filter instanceof ColumnFilter) {
        ColumnFilter columnFilter = (ColumnFilter) filter;
        List<ColumnFilterHeader> columns = columnFilter.getColumnFilterHeaders();
        String columnNames = "";
        for (ColumnFilterHeader column : columns) {
          columnNames += " " + column.getColumn().getDisplayHeader();
        }
        filters.setWidget(row, 0, new Label(columnFilter.getVisibility() + columnNames));
      }

      final Button removeFilter = new Button("-");
      filters.setWidget(row, 1, removeFilter);
      removeFilter.getElement().setPropertyObject("filter", filter);

      removeFilter.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

          Filter remove = (Filter) removeFilter.getElement().getPropertyObject("filter");
          currentGroup.removeFilter(remove);
          AggregateUI.getUI().getTimer().restartTimer();
        }
      });
      row++;
    }
    filterGroup.addItem(filters);
    filterGroup.setState(true);
    return filterGroup;
  }

  private void addFilterGroup(final String id, FilterGroup group) {
    FilterServiceAsync filterSvc = SecureGWT.get().createFilterService();

    // Set up the callback object.
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {

      }

      @Override
      public void onSuccess(Boolean result) {
        parentPanel.update();
      }
    };
    List<Filter> filters = new ArrayList<Filter>();
    filters.addAll(currentGroup.getFilters());
    FilterGroup newGroup = new FilterGroup(id, group.getFormId(), filters);
    currentGroup = enterEditMode(newGroup);

    // Make the call to the form service.
    filterSvc.updateFilterGroup(newGroup, callback);
  }

  // we can only edit one filter group at a time
  // so we are making a temporary filter group
  // that will take all of the user changes
  // these won't be reflected permanently unless a save is done
  // we don't need to do this with the default group however
  public FilterGroup enterEditMode(FilterGroup group) {
    viewingFilters.clear();
    ArrayList<Filter> filters = new ArrayList<Filter>();
    filters.addAll(group.getFilters());
    FilterGroup tempGroup = new FilterGroup(group.getName(), group.getFormId(), filters);
    viewingFilters.add(tempGroup);
    return tempGroup;
  }

  private void removeFilterGroup(FilterGroup group) {
    FilterServiceAsync filterSvc = SecureGWT.get().createFilterService();

    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        AggregateUI.getUI().clearError();
      }
    };

    filterSvc.deleteFilterGroup(group, callback);
  }
}
