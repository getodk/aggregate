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
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Delete the publishing of data to an external service.
 *
 */
public final class RestartButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<b><img src=\"images/green_right_arrow.png\" /> Restart Publisher";
  private static final String TOOLTIP_TEXT = "There were publishing failures - click to Restart the Publisher";
  private static final String HELP_BALLOON_TXT = "The external service was failing or the credentials were bad. Click to restart the publisher.";

  private final ExternServSummary publisher;

  public RestartButton(ExternServSummary publisher) {
    super(BUTTON_TXT, TOOLTIP_TEXT, HELP_BALLOON_TXT);
    this.publisher = publisher;
    addStyleDependentName("negative");
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

    ExternalServiceType type = publisher.getExternalServiceType();

    switch (type) {
    case GOOGLE_SPREADSHEET:
      SecureGWT.getServicesAdminService().restartPublisher(publisher.getUri(),
          new OAuth2Callback());
      break;
    case OHMAGE_JSON_SERVER:
      SecureGWT.getServicesAdminService().restartPublisher(publisher.getUri(),
          new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              // no-op
            }

            @Override
            public void onSuccess(Void result) {
              // no-op
            }
          });
      break;
    case GOOGLE_FUSIONTABLES:
      SecureGWT.getServicesAdminService().restartPublisher(publisher.getUri(),
          new OAuth2Callback());
      break;
    default: // unknown type
      break;
    }
  }
}