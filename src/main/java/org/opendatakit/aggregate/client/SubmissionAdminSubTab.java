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

package org.opendatakit.aggregate.client;

import static org.opendatakit.aggregate.client.LayoutUtils.buildVersionNote;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.table.SubmissionAdminTable;
import org.opendatakit.aggregate.client.widgets.FormListBox;
import org.opendatakit.aggregate.client.widgets.PurgeUpToDateButton;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

public class SubmissionAdminSubTab extends AggregateSubTabBase {

  private static final String SUBMISSION_TXT = "Manually Upload Submission Data";
  private static final String SUBMISSION_TOOLTIP_TXT = "Upload Submissions";
  private static final String SUBMISSION_BALLOON_TXT = "Upload submission data for a preload form.";
  private static final String SUBMISSION_BUTTON_TEXT = "<img src=\"images/blue_up_arrow.png\" /> "
      + SUBMISSION_TXT;

  // ui elements
  private FormListBox formsBox;
  private PurgeUpToDateButton purgeSubmission;
  private ScrollPanel submissions;

  // state
  private FormSummary selectedForm;

  public SubmissionAdminSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    formsBox = new FormListBox(new ChangeDropDownHandler());
    submissions = new ScrollPanel();
    submissions.getElement().setId("submission_admin_list");

    purgeSubmission = new PurgeUpToDateButton();
    purgeSubmission.setSelectedForm(selectedForm);

    ServletPopupButton uploadSubmission = new ServletPopupButton(SUBMISSION_BUTTON_TEXT, SUBMISSION_TXT,
        UIConsts.SUBMISSION_SERVLET_ADDR, this, SUBMISSION_TOOLTIP_TXT, SUBMISSION_BALLOON_TXT);

    // create navigation buttons to servlet
    FlexTable navTable = new FlexTable();
    navTable.setWidget(0, 0, new Label("Form: "));
    navTable.setWidget(0, 1, formsBox);
    navTable.setWidget(0, 2, purgeSubmission);

    // add to panel
    add(uploadSubmission);
    add(navTable);
    add(new Label("Incomplete Submissions:"));
    add(submissions);
    add(buildVersionNote(this));
  }


  @Override
  public boolean canLeave() {
    return true;
  }

  @Override
  public void update() {
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_DATA_OWNER)) {
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

  ;

  private void updateContent() {
    purgeSubmission.setSelectedForm(selectedForm);
    updateSubmissionsAdminTable();
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

    // request the update if form is not the "none" form (ie id will equal null or the empty string)
    if (selectedForm.getId() != null && selectedForm.getId().length() > 0) {
      FilterGroup filterGroup = new FilterGroup(UIConsts.FILTER_NONE, selectedForm.getId(), null);
      SecureGWT.getFormAdminService().getIncompleteSubmissions(filterGroup, callback);
    }

  }

  @Override
  public HelpSliderConsts[] getHelpSliderContent() {
    return null;
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
      updateContent();
    }
  }

  /**
   * Handler to process the change in the form drop down
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      FormSummary form = formsBox.getSelectedForm();
      if (form != null) {
        selectedForm = form;
      }
      updateContent();
    }
  }

}
