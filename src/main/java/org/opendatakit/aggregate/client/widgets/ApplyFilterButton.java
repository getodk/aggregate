/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;

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
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class ApplyFilterButton extends AbstractButtonBase implements ClickHandler {

  private static final String TOOLTIP_TEXT = UIConsts.EMPTY_STRING;
  
  private FilterPopup popup;

  public ApplyFilterButton(FilterPopup popup) {
    super("<img src=\"images/green_check.png\" /> Apply Filter", TOOLTIP_TEXT);
    this.popup = popup;
    addStyleDependentName("positive");
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
      ArrayList<ColumnFilterHeader> columnfilterheaders = popup.getColumnsForColumnFilter();
      newFilter = new ColumnFilter(kr, columnfilterheaders, (long) group.getFilters().size());
    }
    
    group.addFilter(newFilter);
    AggregateUI.getUI().getTimer().refreshNow();
    
    popup.hide();
  }
}
