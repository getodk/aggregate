package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.widgets.DeleteFilterButton;
import org.opendatakit.aggregate.client.widgets.AddFilterButton;
import org.opendatakit.aggregate.client.widgets.SaveFilterGroupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class FiltersDataPanel extends FlowPanel {

  private Tree filtersTree;
  private TreeItem root;

  private FilterSubTab parentSubTab;

  public FiltersDataPanel(FilterSubTab panel) {
    this.parentSubTab = panel;
    getElement().setId("filters_container");
    
    // create tree
    filtersTree = new Tree();
    add(filtersTree);
    
    // create the root as the new filter button
    root = new TreeItem(new AddFilterButton(panel));   
    filtersTree.addItem(root);
  }

  public void update(FilterGroup group) {
    // clear the current filters being displayed so we don't get duplicates
    root.removeItems();
  
    // create filter header
    FlexTable filterHeader = new FlexTable();
    String filterName = group.getName();
    if(filterName.equals(UIConsts.FILTER_NONE)) {
      filterName = ""; // avoid displaying none word, better as empty string
    }
    
    filterHeader.setWidget(0, 0, new Label(filterName));
    filterHeader.setWidget(0, 1, new SaveFilterGroupButton(parentSubTab));

    TreeItem currentFilterGroup = new TreeItem(filterHeader);
    
    // recreate filter list
    FlexTable filters = new FlexTable();
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

      DeleteFilterButton removeFilter = new DeleteFilterButton(filter, parentSubTab);
      filters.setWidget(row, 1, removeFilter);
      row++;
    }
    currentFilterGroup.addItem(filters);
    currentFilterGroup.setState(true);
    
    // set system auto drops the information under root
    root.addItem(currentFilterGroup);
    root.setState(true);
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
