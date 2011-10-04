package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.widgets.CursorAdvancementButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

public class SubmissionPaginationNavBar extends SimplePanel {


  private final FlexTable controls;
  
  public SubmissionPaginationNavBar() {

    getElement().setId("filter_submission_pagination");
    
    controls = new FlexTable();
    controls.addStyleName("form_title_bar");
    controls.getElement().setAttribute("align", "center");
    
    add(controls);
  }
  
  
  public void update(FilterGroup group, SubmissionUISummary summary) {
    // create previous button
    controls.setWidget(0, 0, new CursorAdvancementButton(summary, group, false));
    
    controls.setHTML(0, 1, "<h2 id=\"form_name\">" + summary.getFormTitle() + "</h2>");
    
    // create next button
    controls.setWidget(0, 2, new CursorAdvancementButton(summary, group, true));
  }
}
