package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.widgets.AcceptSubmissionCheckBox;
import org.opendatakit.aggregate.client.widgets.DeleteFormButton;
import org.opendatakit.aggregate.client.widgets.DownloadableCheckBox;
import org.opendatakit.aggregate.client.widgets.ExportButton;
import org.opendatakit.aggregate.client.widgets.PublishButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class FormTable extends FlexTable {

  private static final String K_MAILTO = "mailto:";
   
  public FormTable() {
    
    // create table headers
    setText(0, 0, "Title");
    setText(0, 1, "Form Id");
    setText(0, 2, "User");
    setText(0, 3, "Downloadable");
    setText(0, 4, "Accept Submissions");
    setText(0, 5, "Publish");
    setText(0, 6, "Export");
    setText(0, 7, "Delete");
    
    // add styling
    getRowFormatter().addStyleName(0, "titleBar");
    addStyleName("dataTable");
    getElement().setId("form_management_table");
  }
  
  /**
   * Update the list of forms
   * 
   * @param formSummary
   */
  public void updateFormTable(FormSummary[] forms) {
    for (int j = 0; j < forms.length; j++) {
      int i = j + 1;
      FormSummary form = forms[j];
      setWidget(i, 0, new HTML(form.getViewableURL()));
      setWidget(i, 1, new HTML(form.getId()));
      String user = form.getCreatedUser();
      String displayName;
      if (user.startsWith(K_MAILTO)) {
        displayName = user.substring(K_MAILTO.length());
      } else if (user.startsWith("uid:")) {
        displayName = user.substring("uid:".length(), user.indexOf("|"));
      } else {
        displayName = user;
      }
      setText(i, 2, displayName);
      setWidget(i, 3, 
          new DownloadableCheckBox(form.getId(), form.isDownloadable()));
      setWidget(i, 4,
          new AcceptSubmissionCheckBox(form.getId(), form.receiveSubmissions()));
      setWidget(i, 5, new PublishButton(form.getId()));
      setWidget(i, 6, new ExportButton(form.getId()));
      setWidget(i, 7, new DeleteFormButton(form.getId()));

      if (i % 2 == 0)
        getRowFormatter().addStyleName(i, "evenTableRow");
    }
  }
  
}
