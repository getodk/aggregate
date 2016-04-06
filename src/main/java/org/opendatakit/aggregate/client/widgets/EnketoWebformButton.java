/*
 * Copyright (C) 2013-2014 University of Washington
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

import org.opendatakit.aggregate.client.popups.EnketoRedirectErrorPopup;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public class EnketoWebformButton extends AggregateButton implements ClickHandler {

  private static final String ENKETO_ERROR_400 = "Malformed request, maybe parameters are missing";
  private static final String ENKETO_ERROR_401 = "Authentication failed, incorrect or expired API token used or none at all";
  private static final String ENKETO_ERROR_403 = "Authentication succeeded, but account is not active or quota is filled up";
  private static final String ENKETO_ERROR_404 = "Resource was not found in database";
  private static final String ENKETO_ERROR_405 = "Request not allowed. You may not have API access on your plan";
  private static final String ENKETO_ERROR_410 = "This API endpoint is deprecated in this version";
  private static final String ENKETO_ERROR_411 = "Form doest not exist in the specified aggregate server";
  private static final String ENKETO_IMAGE = "<img src=\"images/enketo.ico\" />";
  private static final String BUTTON_TXT = "<img src=\"images/enketo.ico\" /> Enketo Webform";
  private static final String TOOLTIP_TXT = "Open in Enketo Webform";
  private static final String HELP_BALLOON_TXT = "This will open the xlsform as Enketo Webform";
  private static final String ENKETO_API_URL = "enketo_api_url=";
  private static final String ENKETO_API_TOKEN = "&enketo_api_token=";
  private static final String ENKETO_API_FORM_ID = "&form_id=";
  private static final String ENKETO_SURVEY_ID = "/survey";

  private String selectedForm;
  private String selectedInstanceId;

  public EnketoWebformButton(String selectedForm) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.selectedForm = selectedForm;
  }

  public EnketoWebformButton(String selectedInstanceId, String selectedForm, String buttonText) {
    super(ENKETO_IMAGE + buttonText, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.selectedInstanceId = selectedInstanceId;
    this.selectedForm = selectedForm;
  }

  public void onClick(ClickEvent event) {
    super.onClick(event);

    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, UIConsts.ENKETO_API_HANDLER_ADDR);
    builder.setHeader("Content-type", "application/x-www-form-urlencoded");

    StringBuffer requestdata = new StringBuffer();
    if (selectedInstanceId == null) {
      String enketoURL = ENKETO_API_URL + Preferences.getEnketoApiUrl() + ENKETO_SURVEY_ID;
      requestdata.append(enketoURL);
      requestdata.append(ENKETO_API_FORM_ID + selectedForm);
      requestdata.append(ENKETO_API_TOKEN + Preferences.getEnketoApiToken());
    }
    try {
      builder.sendRequest(requestdata.toString(), new RequestCallback() {
        public void onError(Request request, Throwable e) {
          Window.alert(e.getMessage());
        }

        public void onResponseReceived(Request request, Response response) {
          int statusCode = response.getStatusCode();
          EnketoRedirectErrorPopup popup;
          switch (statusCode) {
          case 200:
            Window.open(response.getHeader("enketo_url"), "_self", "");
            break;
          case 201:
            Window.open(response.getHeader("enketo_url"), "_self", "");
            break;
          case 400:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_400);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 401:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_401);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 403:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_403);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 404:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_404);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 405:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_405);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 410:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_410);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          case 411:
            popup = new EnketoRedirectErrorPopup(ENKETO_ERROR_411);
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          default:
            popup = new EnketoRedirectErrorPopup(response.getHeader("error"));
            popup.setPopupPositionAndShow(popup.getPositionCallBack());
            break;
          }
        }

      });
    } catch (RequestException e) {
      // Couldn't connect to server
      EnketoRedirectErrorPopup popup = new EnketoRedirectErrorPopup(e.getMessage());
      popup.setPopupPositionAndShow(popup.getPositionCallBack());
      // }

    }
  }

}
