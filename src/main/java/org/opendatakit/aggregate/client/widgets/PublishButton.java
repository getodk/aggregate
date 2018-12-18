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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.PublishPopup;
import org.opendatakit.common.security.common.GrantedAuthorityName;

public final class PublishButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Publish";
  private static final String TOOLTIP_TXT = "Publish the data";
  private static final String HELP_BALLOON_TXT = "This will publish the data to Google Spreadsheets.";

  private final String formId;

  public PublishButton(String formId) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.formId = formId;
    boolean enabled = AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_DATA_OWNER);
    setEnabled(enabled);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    PublishPopup popup = new PublishPopup(formId);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }
}
