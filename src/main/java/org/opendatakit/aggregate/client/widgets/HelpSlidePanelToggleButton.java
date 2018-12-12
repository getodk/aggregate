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
import com.google.gwt.user.client.ui.Image;
import org.opendatakit.aggregate.client.AggregateUI;

public final class HelpSlidePanelToggleButton extends AggregateImageToggleButton {

  private static final Image HELP_PANEL_ICON_ON = new Image("images/help_icon_on.png");
  private static final Image HELP_PANEL_ICON_OFF = new Image("images/help_icon_off.png");
  private static final String TOOLTIP_TXT = "Help Panel";
  private static final String HELP_BALLOON_TXT = "This displays a help panel below with comprehensive information.";

  public HelpSlidePanelToggleButton() {
    super(HELP_PANEL_ICON_OFF, HELP_PANEL_ICON_ON, TOOLTIP_TXT, HELP_BALLOON_TXT);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    if (getValue()) {
      AggregateUI.getUI().displayHelpPanel();
    } else {
      AggregateUI.getUI().hideHelpPanel();
    }

  }

}
