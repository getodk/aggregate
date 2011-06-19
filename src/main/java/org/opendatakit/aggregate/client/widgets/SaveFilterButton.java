package org.opendatakit.aggregate.client.widgets;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.popups.FilterPopup;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SaveFilterButton extends AButtonBase implements ClickHandler {

  private FilterPopup popup;

  public SaveFilterButton(FilterPopup popup) {
    super("<img src=\"images/green_check.png\" /> Save Filter");
    this.popup = popup;
    addStyleDependentName("positive");
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    Visibility kr = popup.getKeepRemove();
    RowOrCol rowcol = popup.getRowCol();
    FilterGroup group = popup.getGroup();

    if(group == null) {
      return;
    }
    
    Filter newFilter;
    if (rowcol.equals(RowOrCol.ROW)) {
      Column column = popup.getColumnForRowFilter();
      FilterOperation op = popup.getFilterOp();
      String value = popup.getFilterValue();
      newFilter = new RowFilter(kr, column, op, value, (long) group.getFilters().size()); 
    } else {
      List<ColumnFilterHeader> columnfilterheaders = popup.getColumnsForColumnFilter();
      newFilter = new ColumnFilter(kr, columnfilterheaders, (long) group.getFilters().size());
    }
    
    group.addFilter(newFilter);
    AggregateUI.getUI().getTimer().refreshNow();
    
    popup.hide();
  }

}
