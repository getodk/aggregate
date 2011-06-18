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
