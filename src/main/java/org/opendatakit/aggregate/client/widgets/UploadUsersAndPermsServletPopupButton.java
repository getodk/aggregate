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

import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.popups.ViewServletPopup;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

public final class UploadUsersAndPermsServletPopupButton extends AggregateButton {

  private static final String UPLOAD_USERS_AND_PERMS_CSV_TXT = "Upload Users and Capabilities .csv";
  private static final String UPLOAD_USERS_AND_PERMS_CSV_TOOLTIP_TXT = "Configure users and their capabilities using a .csv file";
  private static final String UPLOAD_USERS_AND_PERMS_CSV_BALLOON_TXT = "Additional information may be found under the Usage section of the pop-up.";
  private static final String UPLOAD_USERS_AND_PERMS_CSV_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> "
      + UPLOAD_USERS_AND_PERMS_CSV_TXT;

  public UploadUsersAndPermsServletPopupButton() {
    super(UPLOAD_USERS_AND_PERMS_CSV_BUTTON_TEXT, UPLOAD_USERS_AND_PERMS_CSV_TOOLTIP_TXT, UPLOAD_USERS_AND_PERMS_CSV_BALLOON_TXT);
  }
  
  public void onClick(final AggregateSubTabBase basePanel, ClickEvent event) {
    
    final ViewServletPopup servletPopup = new ViewServletPopup(UPLOAD_USERS_AND_PERMS_CSV_TXT, UIConsts.USERS_AND_PERMS_UPLOAD_SERVLET_ADDR);
    servletPopup.setPopupPositionAndShow(servletPopup.getPositionCallBack());
    servletPopup.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        servletPopup.hide();
        if(basePanel != null) {
          basePanel.update();
        }
      }

    });
  }  
}