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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UIUtils;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Delete the publishing of data to an external service.
 *
 */
public final class SharePublishedDocumentsButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<b><img src=\"images/green_right_arrow.png\" /> Share Documents";
  private static final String TOOLTIP_TEXT = "Click to share these documents with another Google account";
  private static final String HELP_BALLOON_TXT = "Share these published documents with another Google account.";

  private final ExternServSummary publisher;

  public SharePublishedDocumentsButton(ExternServSummary publisher) {
    super(BUTTON_TXT, TOOLTIP_TEXT, HELP_BALLOON_TXT);
    this.publisher = publisher;

    ExternalServiceType type = publisher.getExternalServiceType();
    if (( type != ExternalServiceType.GOOGLE_FUSIONTABLES &&
          type != ExternalServiceType.GOOGLE_SPREADSHEET ) ||
        ( publisher.getStatus() != OperationalStatus.ACTIVE &&
          publisher.getStatus() != OperationalStatus.PAUSED &&
          publisher.getStatus() != OperationalStatus.COMPLETED )) {
      addStyleDependentName("negative");
    }
  }

  private class OAuth2Callback implements AsyncCallback<Void> {

    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(Void result) {
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    // prompt for name
    String newEmail;
    try {
      newEmail = UIUtils.promptForEmailAddress();
    } catch (Exception e) {
      return; // user pressed cancel
    }

    SecureGWT.getServicesAdminService().sharePublishedFiles(publisher.getUri(), newEmail,
          new OAuth2Callback());
  }
}