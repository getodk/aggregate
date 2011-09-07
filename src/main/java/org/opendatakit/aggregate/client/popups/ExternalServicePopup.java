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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UrlHash;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecutePublishButton;
import org.opendatakit.aggregate.client.widgets.ExternalServiceTypeListBox;
import org.opendatakit.aggregate.client.widgets.PublishOptionListBox;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class ExternalServicePopup extends PopupPanel {

  private String formId;
  private TextBox name;
  private ExternalServiceTypeListBox serviceType;
  private PublishOptionListBox esOptions;

  public ExternalServicePopup(String formId) {
    super(false);

    this.formId = formId;

    serviceType = new ExternalServiceTypeListBox(this);
    esOptions = new PublishOptionListBox();
    name = new TextBox();

    updateUIOptions();
    
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML("Form: " + formId + " "));
    layout.setWidget(0, 1, serviceType);
    layout.setWidget(0, 2, name);

    layout.setWidget(0, 3, esOptions);
    layout.setWidget(0, 4, new ExecutePublishButton(this));
    layout.setWidget(0, 5, new ClosePopupButton(this));

    setWidget(layout);
  }

  public void updateUIOptions() {
    ExternalServiceType type = serviceType.getExternalServiceType();
    
    if (type == null) {
      name.setEnabled(false);
      return;
    }

    switch (type) {
    case GOOGLE_SPREADSHEET:
      name.setText("");
      name.setEnabled(true);
      break;
    case GOOGLE_FUSIONTABLES:
    default: // unknown type
      name.setText("Spreadsheet Name");
      name.setEnabled(false);
      break;
    }
  }
  
  public void createExternalService() {

    ExternalServiceType type = serviceType.getExternalServiceType();
    ExternalServicePublicationOption serviceOp = esOptions.getEsPublishOption();

    switch (type) {
    case GOOGLE_SPREADSHEET:
      SecureGWT.getServicesAdminService().createGoogleSpreadsheet(formId, name.getText(),
          serviceOp, new OAuthCallback());
      break;
    case GOOGLE_FUSIONTABLES:
      SecureGWT.getServicesAdminService().createFusionTable(formId, serviceOp, new OAuthCallback());
      break;
    default: // unknown type
      break;
    }

  }

  private class OAuthCallback implements AsyncCallback<String> {

    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(String result) {
      SecureGWT.getServicesAdminService().generateOAuthUrl(result, new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(String result) {
          UrlHash.getHash().goTo(result);
        }
      });
    }
  }

}
