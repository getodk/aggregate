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

package org.opendatakit.aggregate.client.popups;

import static com.google.gwt.user.client.Window.alert;
import static org.opendatakit.aggregate.client.security.SecurityUtils.secureRequest;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.OperationalStatus;

/**
 * Popup asking for confirmation to delete an external service publisher
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class ConfirmPublishDeletePopup extends AbstractPopupBase {

  private static final String BUTTON_ICON = "<img src=\"images/green_right_arrow.png\"/>";
  private static final String TOOLTIP_TXT = "Remove this publisher";
  private static final String HELP_BALLOON_TXT = "This will remove the publisher. You will no longer be able to upload or stream data.";


  public ConfirmPublishDeletePopup(ExternServSummary publisher) {
    super();

    String action = (publisher.getStatus() == OperationalStatus.COMPLETED || publisher.getStatus() == OperationalStatus.ABANDONED)
        ? "remove"
        : "stop publishing and remove";

    String buttonTxt = BUTTON_ICON + action + " Publisher";
    AggregateButton deleteButton = new AggregateButton(buttonTxt, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(event -> {
      secureRequest(
          SecureGWT.getServicesAdminService(),
          (rpc, sessionCookie, cb) -> rpc.deletePublisher(publisher.getUri(), cb),
          (Boolean result) -> onSuccess(action, result),
          this::onError
      );
      hide();
    });

    FlexTable layout = new FlexTable();

    HTML message = new HTML(new SafeHtmlBuilder()
        .appendEscaped("Delete this publisher?")
        .appendHtmlConstant("<br/>")
        .appendEscaped("Do you wish to " + action + " this location?")
        .toSafeHtml());
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private void onError(Throwable cause) {
    AggregateUI.getUI().reportError(cause);
  }

  private void onSuccess(String action, Boolean result) {
    AggregateUI.getUI().clearError();
    if (!result)
      alert("Error: Unable to " + action + " this publisher");
    AggregateUI.getUI().getTimer().refreshNow();
  }
}