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
import org.opendatakit.aggregate.client.UIUtils;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.EnumListBox;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public final class PublishPopup extends AbstractPopupBase {

  private static final String EMPTY_STRING = "";
  private static final String HTTP_LOCALHOST = "http://localhost";
  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Publish";
  private static final String TOOLTIP_TXT = "Publish the data";
  private static final String HELP_BALLOON_TXT = "This will publish the data to Google Fusion Tables, "
      + " Google Spreadsheets, a REDCap server, or a server accepting JSON content.";

  private static final String ES_SERVICEOPTIONS_TOOLTIP = "Method data should be published";
  private static final String ES_SERVICEOPTIONS_BALLOON = "Choose whether you would like only old data, only new data, or all data to be published.";
  private static final String ES_TYPE_TOOLTIP = "Type of External Service Connection";
  private static final String ES_TYPE_BALLOON = "Select the application where you want your data to be published.";
 
  private static final String BO_TYPE_TOOLTIP = "Sets how the binary data from Media should be published";
  private static final String BO_TYPE_BALLOON = "Selects how the binary dat from Media should be published. Aggregate will provide links in the publish OR will embed the data in the publish";
  
  // this is the main flex table for the popup
  private final FlexTable layout;
  // this is the header
  private final FlexTable topBar;
  // to hold the options
  private final FlexTable optionsBar;

  private final AggregateButton publishButton;

  // to hold the google spreadsheet only options
  private final FlexTable gsBar;
  private final TextBox gsName;

  // to hold the jsonServer only options
  private final FlexTable jsBar;
  private final TextBox jsAuthKey;
  private final TextBox jsUrl;
  private final EnumListBox<BinaryOption> jsBinaryOptions;

  // to hold the jsonServer only options
  private final FlexTable ohmageBar;
  private final TextBox ohmageCampaignUrn;
  private final TextBox ohmageCampaignTimestamp;
  private final TextBox ohmageUsername;
  private final TextBox ohmageHashedPassword;
  private final TextBox ohmageUrl;

  // to hold the redcap only options
  private final FlexTable rcBar;
  private final TextBox rcApiKey;
  private final TextBox rcUrl;

  private final String formId;

  private final EnumListBox<ExternalServiceType> serviceType;
  private final EnumListBox<ExternalServicePublicationOption> esOptions;

  public PublishPopup(String formId) {
    super();

    this.formId = formId;
    this.publishButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    publishButton.addClickHandler(new CreateExernalServiceHandler());

    ExternalServiceType[] valuesToShow = { ExternalServiceType.GOOGLE_FUSIONTABLES,
        ExternalServiceType.GOOGLE_SPREADSHEET,
        ExternalServiceType.REDCAP_SERVER, ExternalServiceType.JSON_SERVER,
        ExternalServiceType.OHMAGE_JSON_SERVER };
    serviceType = new EnumListBox<ExternalServiceType>(valuesToShow, ES_TYPE_TOOLTIP,
        ES_TYPE_BALLOON);
    serviceType.addChangeHandler(new ExternalServiceTypeChangeHandler());

    esOptions = new EnumListBox<ExternalServicePublicationOption>(
        ExternalServicePublicationOption.values(), ES_SERVICEOPTIONS_TOOLTIP,
        ES_SERVICEOPTIONS_BALLOON);
    
    // Set up the tables in the popup
    layout = new FlexTable();

    topBar = new FlexTable();
    topBar.addStyleName("stretch_header");
    topBar.setWidget(0, 0, new HTML("<h2>Form: </h2>"));
    topBar.setWidget(0, 1, new HTML(formId));
    topBar.setWidget(0, 2, new HTML("<h2>Publish to: </h2>"));
    topBar.setWidget(0, 3, serviceType);
    topBar.setWidget(0, 4, publishButton);
    topBar.setWidget(0, 5, new ClosePopupButton(this));

    optionsBar = new FlexTable();
    optionsBar.addStyleName("flexTableBorderTopStretchWidth");
    optionsBar.setWidget(1, 0, new HTML("<h3>Data to Publish:</h3>"));
    optionsBar.setWidget(1, 1, esOptions);

    // this is only for google spreadsheets
    gsBar = new FlexTable();
    gsBar.addStyleName("stretch_header");
    gsBar.setWidget(1, 0, new HTML("<h3>Workbook Name:</h3>"));
    // make the name textbox an appropriate size
    gsName = new TextBox();
    gsName.setText(EMPTY_STRING);
    gsName.setVisibleLength(35);
    gsBar.setWidget(1, 1, gsName);

    // this is only for simple json server
    jsBar = new FlexTable();
    jsBar.addStyleName("stretch_header");
    // get the URL
    jsBar.setWidget(1, 0, new HTML("<h3>Url to publish to:</h3>"));
    jsUrl = new TextBox();
    jsUrl.setText(HTTP_LOCALHOST);
    jsUrl.setVisibleLength(60); 
    jsBar.setWidget(1, 1, jsUrl);
    // get token
    jsBar.setWidget(2, 0, new HTML("<h3>Authorization token:</h3>"));
    jsAuthKey = new TextBox();
    jsAuthKey.setText(EMPTY_STRING);
    jsAuthKey.setVisibleLength(45);
    jsBar.setWidget(2, 1, jsAuthKey);
    // make the options for how to handle the binary 
    jsBar.setWidget(3, 0, new HTML("<h3>Include Media as:</h3>"));
    jsBinaryOptions = new EnumListBox<BinaryOption>(
        BinaryOption.values(), BO_TYPE_TOOLTIP,
        BO_TYPE_BALLOON);
    jsBar.setWidget(3, 1, jsBinaryOptions);
    
    // this is only for ohmage server
    ohmageBar = new FlexTable();
    ohmageBar.addStyleName("stretch_header");
    ohmageBar.setWidget(1, 0, new HTML("<h3>Url to publish to:</h3>"));
    // make the name textbox an appropriate size
    ohmageUrl = new TextBox();
    ohmageUrl.setText(HTTP_LOCALHOST);
    ohmageUrl.setVisibleLength(60);
    ohmageBar.setWidget(1, 1, ohmageUrl);
    ohmageBar.setWidget(2, 0, new HTML("<h3>Campaign URN:</h3>"));
    // make the name textbox an appropriate size
    ohmageCampaignUrn = new TextBox();
    ohmageCampaignUrn.setText(EMPTY_STRING);
    ohmageCampaignUrn.setVisibleLength(60);
    ohmageBar.setWidget(2, 1, ohmageCampaignUrn);
    ohmageBar.setWidget(3, 0, new HTML("<h3>Campaign Creation Timestamp:</h3>"));
    // make the name textbox an appropriate size
    ohmageCampaignTimestamp = new TextBox();
    ohmageCampaignTimestamp.setText(EMPTY_STRING);
    ohmageCampaignTimestamp.setVisibleLength(45);
    ohmageBar.setWidget(3, 1, ohmageCampaignTimestamp);
    ohmageBar.setWidget(4, 0, new HTML("<h3>Ohmage Username:</h3>"));
    // make the name textbox an appropriate size
    ohmageUsername = new TextBox();
    ohmageUsername.setText(EMPTY_STRING);
    ohmageUsername.setVisibleLength(60);
    ohmageBar.setWidget(4, 1, ohmageUsername);
    ohmageBar.setWidget(5, 0, new HTML("<h3>Ohmage hashed Password:</h3>"));
    // make the name textbox an appropriate size
    ohmageHashedPassword = new TextBox();
    ohmageHashedPassword.setText(EMPTY_STRING);
    ohmageHashedPassword.setVisibleLength(60);
    ohmageBar.setWidget(5, 1, ohmageHashedPassword);

    // this is only for REDCap server
    rcBar = new FlexTable();
    rcBar.addStyleName("stretch_header");
    rcBar.setWidget(1, 0, new HTML("<h3>REDCap Url to publish to:</h3>"));
    // make the name textbox an appropriate size
    rcUrl = new TextBox();
    rcUrl.setText(HTTP_LOCALHOST);
    rcUrl.setVisibleLength(60);
    rcBar.setWidget(1, 1, rcUrl);
    rcBar.setWidget(2, 0, new HTML("<h3>REDCap API Key:</h3>"));
    // make the name textbox an appropriate size
    rcApiKey = new TextBox();
    rcApiKey.setText(EMPTY_STRING);
    rcApiKey.setVisibleLength(45);
    rcBar.setWidget(2, 1, rcApiKey);

    FlowPanel grouping = new FlowPanel();
    grouping.add(gsBar);
    grouping.add(jsBar);
    grouping.add(rcBar);
    grouping.add(ohmageBar);
    gsBar.setVisible(false);
    jsBar.setVisible(false);
    rcBar.setVisible(false);
    ohmageBar.setVisible(false);
    optionsBar.setWidget(2, 0, grouping);
    optionsBar.getFlexCellFormatter().setColSpan(2, 0, 2);

    layout.setWidget(0, 0, topBar);
    layout.setWidget(1, 0, optionsBar);
    // set the options to fill the table as well
    layout.getFlexCellFormatter().setColSpan(1, 0, 6);
    setWidget(layout);

    updateUIOptions();

  }

  public void updateUIOptions() {
    System.out.println("UPDATE UI OPTIONS CALLED");
    System.out.println("Type:" + serviceType.getSelectedValue());
    
    String externalServiceTypeString = serviceType.getSelectedValue();
    ExternalServiceType type = (externalServiceTypeString == null) ? null :
      ExternalServiceType.valueOf(externalServiceTypeString);

    if (type == null) {
      gsBar.setVisible(false);
      jsBar.setVisible(false);
      rcBar.setVisible(false);
      ohmageBar.setVisible(false);
      publishButton.setEnabled(false);
      return;
    }

    publishButton.setEnabled(true);

    switch (type) {
    case GOOGLE_SPREADSHEET:
      gsBar.setVisible(true);
      jsBar.setVisible(false);
      rcBar.setVisible(false);
      ohmageBar.setVisible(false);
      optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
      break;
    case JSON_SERVER:
      gsBar.setVisible(false);
      jsBar.setVisible(true);
      rcBar.setVisible(false);
      ohmageBar.setVisible(false);
      optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
      break;
    case OHMAGE_JSON_SERVER:
      gsBar.setVisible(false);
      jsBar.setVisible(false);
      rcBar.setVisible(false);
      ohmageBar.setVisible(true);
      optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
      break;
    case REDCAP_SERVER:
      gsBar.setVisible(false);
      jsBar.setVisible(false);
      rcBar.setVisible(true);
      ohmageBar.setVisible(false);
      optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
      break;
    case GOOGLE_FUSIONTABLES:
      gsBar.setVisible(false);
      jsBar.setVisible(false);
      rcBar.setVisible(false);
      ohmageBar.setVisible(false);
      optionsBar.getRowFormatter().setStyleName(2, "disabledTableRow");
      break;
    default: // unknown type
      gsBar.setVisible(false);
      jsBar.setVisible(false);
      rcBar.setVisible(false);
      ohmageBar.setVisible(false);
      optionsBar.getRowFormatter().setStyleName(2, "disabledTableRow");
      publishButton.setEnabled(false);
      break;
    }
  }

  private class CreateExernalServiceHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {

      String externalServiceTypeString = serviceType.getSelectedValue();
      ExternalServiceType type = (externalServiceTypeString == null) ? null :
        ExternalServiceType.valueOf(externalServiceTypeString);

      String serviceOpString = esOptions.getSelectedValue();
      ExternalServicePublicationOption serviceOp = (serviceOpString == null) ? null :
        ExternalServicePublicationOption.valueOf(serviceOpString);

      UserSecurityInfo info = AggregateUI.getUI().getUserInfo();
      String ownerEmail = info.getEmail();
      if (ownerEmail == null || ownerEmail.length() == 0) {
        try {
          ownerEmail = UIUtils.promptForEmailAddress();
        } catch (Exception e) {
          return; // user pressed cancel
        }
      }

      switch (type) {
      case GOOGLE_SPREADSHEET:
        SecureGWT.getServicesAdminService().createGoogleSpreadsheet(formId, gsName.getText(),
            serviceOp, ownerEmail, new ReportFailureCallback());
        break;
      case REDCAP_SERVER:
        SecureGWT.getServicesAdminService().createRedCapServer(formId, rcApiKey.getText(),
            rcUrl.getText(), serviceOp, ownerEmail, new ReportFailureCallback());
        break;
      case JSON_SERVER:
      {
        String jsBinaryOpString = jsBinaryOptions.getSelectedValue();
        BinaryOption jsBinaryOp = (jsBinaryOpString == null) ? null : BinaryOption.valueOf(jsBinaryOpString);
        SecureGWT.getServicesAdminService().createSimpleJsonServer(formId, jsAuthKey.getText(),
            jsUrl.getText(), serviceOp, ownerEmail, jsBinaryOp, new ReportFailureCallback());
      }
        break;
      case OHMAGE_JSON_SERVER:
        SecureGWT.getServicesAdminService().createOhmageJsonServer(formId,
            ohmageCampaignUrn.getText(), ohmageCampaignTimestamp.getText(),
            ohmageUsername.getText(), ohmageHashedPassword.getText(), ohmageUrl.getText(),
            serviceOp, ownerEmail, new ReportFailureCallback());
        break;
      case GOOGLE_FUSIONTABLES:
        SecureGWT.getServicesAdminService().createFusionTable(formId, serviceOp, ownerEmail,
            new ReportFailureCallback());
        break;
      default: // unknown type
        break;
      }

      hide();
    }
  }

  private class ReportFailureCallback implements AsyncCallback<String> {

    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(String result) {
    }
  }

  private class ExternalServiceTypeChangeHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      updateUIOptions();
    }
  }

}
