/*
 * Copyright (C) 2012 University of Washington
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

package org.opendatakit.aggregate.client.popups;

import static org.opendatakit.aggregate.client.security.SecurityUtils.secureRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public final class ConfirmSubmissionDeletePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete Submission";
  private static final String TOOLTIP_TXT = "Delete Submission from database";
  private static final String HELP_BALLOON_TXT = "This will delete the submission from the database.";

  private static final String DELETE_SUBMISSION_WARNING = "Are you sure you want to Delete the submission? Once delete the submission will be permanently removed from Aggregate's database";

  private final String submissionKeyAsString;

  public ConfirmSubmissionDeletePopup(String submissionKeyAsString) {
    super();

    this.submissionKeyAsString = submissionKeyAsString;

    FlexTable layout = new FlexTable();

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new ExecuteDelete());

    HTML message = new HTML(DELETE_SUBMISSION_WARNING);
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private class ExecuteDelete implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      secureRequest(
          SecureGWT.getFormAdminService(),
          (rpc, sessionCookie, cb) -> rpc.deleteSubmission(submissionKeyAsString, cb),
          () -> {
            AggregateUI.getUI().clearError();
            AggregateUI.getUI().getTimer().refreshNow();
          },
          AggregateUI.getUI()::reportError
      );
      hide();
    }
  }
}
