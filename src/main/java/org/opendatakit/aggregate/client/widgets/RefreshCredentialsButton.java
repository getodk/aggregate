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
import org.opendatakit.aggregate.client.UrlHash;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Delete the publishing of data to an external service.
 * 
 */
public final class RefreshCredentialsButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<b><img src=\"images/green_right_arrow.png\" /> Refresh Credentials";
  private static final String TOOLTIP_TEXT = "Credentials were rejected by service - click to Refresh Credentials";
  private static final String HELP_BALLOON_TXT = "Refresh the credentials used ot publish data to this service.";

  private final ExternServSummary publisher;

  public RefreshCredentialsButton(ExternServSummary publisher) {
    super(BUTTON_TXT, TOOLTIP_TEXT, HELP_BALLOON_TXT);
    this.publisher = publisher;
    addStyleDependentName("negative");
  }

  private class OAuthCallback implements AsyncCallback<String> {

    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(String result) {
      UrlHash.getHash().goTo(result);
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    ExternalServiceType type = publisher.getExternalServiceType();

    switch (type) {
    case GOOGLE_SPREADSHEET:
      SecureGWT.getServicesAdminService().refreshCredentials(publisher.getUri(),
          new OAuthCallback());
      break;
    case OHMAGE_JSON_SERVER:
      SecureGWT.getServicesAdminService().refreshCredentials(publisher.getUri(),
          new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // no-op
            }

            @Override
            public void onSuccess(String result) {
              // no-op
            }
          });
      break;
    case GOOGLE_FUSIONTABLES:
      SecureGWT.getServicesAdminService().refreshCredentials(publisher.getUri(),
          new OAuthCallback());
      break;
    default: // unknown type
      break;
    }
  }
}