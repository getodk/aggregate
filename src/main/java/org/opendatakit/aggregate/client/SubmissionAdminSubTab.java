package org.opendatakit.aggregate.client;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.table.SubmissionAdminTable;
import org.opendatakit.aggregate.client.widgets.FormListBox;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

public class SubmissionAdminSubTab extends AggregateSubTabBase {

  private static final String SUBMISSION_TXT = "Manually Upload Submission Data";
  private static final String SUBMISSION_TOOLTIP_TXT = "Upload Submissions";
  private static final String SUBMISSION_BALLOON_TXT = "Upload submission data for a preload form.";
  private static final String SUBMISSION_BUTTON_TEXT = "<img src=\"images/blue_up_arrow.png\" /> "
      + SUBMISSION_TXT;
  
  // ui elements
  private FormListBox formsBox;
  private SimplePanel submissions;

  // state
  private FormSummary selectedForm;

  public SubmissionAdminSubTab() {

    formsBox = new FormListBox(new ChangeDropDownHandler());
    submissions = new SimplePanel();
    
    ServletPopupButton uploadSubmission = new ServletPopupButton(SUBMISSION_BUTTON_TEXT, SUBMISSION_TXT,
        UIConsts.SUBMISSION_SERVLET_ADDR, this, SUBMISSION_TOOLTIP_TXT, SUBMISSION_BALLOON_TXT);
    
    // create navigation buttons to servlet
    FlexTable navTable = new FlexTable();
    navTable.addStyleName("stretch_header");
    navTable.setWidget(0, 0, formsBox);
    navTable.setWidget(0, 1, uploadSubmission);
    navTable.getCellFormatter().getElement(0, 1).setAttribute("align", "right");

    // add to panel
    add(navTable);
    add(submissions);
  }


  @Override
  public boolean canLeave() {
     return true;
  }

  private class UpdateAction implements AsyncCallback<ArrayList<FormSummary>> {
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(ArrayList<FormSummary> formsFromService) {
      AggregateUI.getUI().clearError();
      
      // setup the display with the latest updates
      formsBox.updateFormDropDown(formsFromService);
      
      // update the class state with the currently displayed form
      selectedForm = formsBox.getSelectedForm();
      
      // Make the call to get the published services
      updateSubmissionsAdminTable();
    }
  };

  @Override
  public void update() {
    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_DATA_OWNER)) {
      formsBox.setVisible(true);
      submissions.setVisible(true);
       FormServiceAsync formSvc = SecureGWT.getFormService();
   
       // Make the call to the form service.
       formSvc.getForms(new UpdateAction());
    } else {
      formsBox.setVisible(false);
      submissions.setVisible(false);
    }
  }

  public synchronized void updateSubmissionsAdminTable() {
    AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(SubmissionUISummary incompleteSubmissions) {
        submissions.clear();
        submissions.add(new SubmissionAdminTable(incompleteSubmissions));
      }
    };

    if (selectedForm == null) {
      return;
    }
    
    // request the update if form is not the "none" form (ie id will equal null)
    if (selectedForm.getId() != null) {
      FilterGroup filterGroup = new FilterGroup(UIConsts.FILTER_NONE, selectedForm.getId(), null);
      SecureGWT.getFormAdminService().getIncompleteSubmissions(filterGroup, callback);
    }

  }
  
  /**
   * Handler to process the change in the form drop down
   * 
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      FormSummary form = formsBox.getSelectedForm();
      if(form != null) {
        selectedForm = form;
      }
      updateSubmissionsAdminTable();
    }
  }
  
  @Override
  public HelpSliderConsts[] getHelpSliderContent() {
    return null;
  }

}
