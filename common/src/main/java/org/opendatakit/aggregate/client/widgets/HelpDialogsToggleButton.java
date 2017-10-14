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

import com.google.gwt.user.client.ui.Image;

public final class HelpDialogsToggleButton extends AggregateImageToggleButton {

  private static final Image HELP_DIALOG_ICON_ON = new Image("images/help_dialog_on.jpg");
  private static final Image HELP_DIALOG_ICON_OFF = new Image("images/help_dialog_off.jpg");
  private static final String TOOLTIP_TXT = "Help Balloons";
  private static final String HELP_BALLOON_TXT = "This will display a more detailed help balloon when"
      + "you hover over an icon.";

  public HelpDialogsToggleButton() {
    super(HELP_DIALOG_ICON_OFF, HELP_DIALOG_ICON_ON, TOOLTIP_TXT, HELP_BALLOON_TXT);
  }

}
