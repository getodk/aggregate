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

import java.util.Date;

import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmPurgeButton extends AbstractButtonBase implements ClickHandler {
  
  private static final String TOOLTIP_TEXT = UIConsts.EMPTY_STRING;
  
  private String uri;
  private PopupPanel popup;
  private Date earliest;

  public ConfirmPurgeButton(String uri, Date earliest, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Purge Data", TOOLTIP_TEXT);
    this.uri = uri;
    this.popup = popup;
    this.earliest = earliest;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    // OK -- we are to proceed.
    SecureGWT.getFormAdminService().purgePublishedData(uri, earliest, new AsyncCallback<Date>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed purge of published data: " + caught.getMessage());

      }

      @Override
      public void onSuccess(Date result) {
        Window.alert("Successful commencement of the purge of " + "\nall data published as of "
            + result.toString());
      }
    });
    popup.hide();
  }
}