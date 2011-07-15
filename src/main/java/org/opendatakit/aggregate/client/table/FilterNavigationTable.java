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

package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.widgets.ExportButton;
import org.opendatakit.aggregate.client.widgets.PublishButton;
import org.opendatakit.aggregate.client.widgets.VisualizationButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;


public class FilterNavigationTable extends FlexTable{

  private FilterSubTab filterSubTab;
  private FormNFilterSelectionTable formNFilterSelection;
  
  public FilterNavigationTable(FilterSubTab filterSubTab) {
    this.filterSubTab = filterSubTab;
    formNFilterSelection = new FormNFilterSelectionTable(filterSubTab);
    setWidget(0, 0, formNFilterSelection);
    setHTML(0, 1, "<h2 id=\"form_name\"></h2>");

    getElement()
     .getFirstChildElement().getNextSiblingElement()
     .getFirstChildElement()
     .getFirstChildElement().getNextSiblingElement().setId("form_title_cell");
   
  }
  

  
  public void updateNavTable(FormSummary form) {
    setHTML(0, 1, "<h2 id=\"form_name\">" + form.getTitle() + "</h2>");
    
    FlexTable actionTable = new FlexTable();
    
    // end goals vis, export, publish
    VisualizationButton visualizeButton = new VisualizationButton(filterSubTab);
    actionTable.setWidget(0, 0, visualizeButton);

    ExportButton exportButton = new ExportButton(form.getId());
    actionTable.setWidget(0, 1, exportButton);
    
    PublishButton publishButton = new PublishButton(form.getId());
    actionTable.setWidget(0, 2, publishButton);
    
    setWidget(0, 2, actionTable);

    getElement().getFirstChildElement().getNextSiblingElement().getFirstChildElement()
    .getFirstChildElement().getNextSiblingElement().getNextSiblingElement().setAttribute("align", "right");

  }
  
  public void update() {
    formNFilterSelection.update();
  }
  
  public ListBox getCurrentFilterList() {
    return formNFilterSelection.getFiltersBox();
  }
  
}
