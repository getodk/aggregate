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
import com.google.gwt.user.client.ui.PopupPanel;

public final class ClosePopupButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
  private static final String TOOLTIP_TXT = "Close";
  private static final String HELP_BALLOON_TXT = "Close the window";

  private final PopupPanel popup;

  public ClosePopupButton(PopupPanel popup) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.popup = popup;
    addStyleDependentName("close");
    addStyleDependentName("negative");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    popup.hide();
  }
}