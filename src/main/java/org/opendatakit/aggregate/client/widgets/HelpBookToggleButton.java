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
import org.opendatakit.aggregate.client.popups.HelpBookPopup;

public final class HelpBookToggleButton extends AggregateImageToggleButton {

  private static final Image HELP_BOOK_ICON = new Image("images/help_book_icon.png");
  private static final String TOOLTIP_TEXT = "Show Detailed Help";
  private static final String HELP_BALLOON_TXT = "This will display a more detailed help popup.";

  public HelpBookToggleButton() {
    super(HELP_BOOK_ICON, TOOLTIP_TEXT, HELP_BALLOON_TXT);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    HelpBookPopup helpPopup = new HelpBookPopup();
    helpPopup.show();
  }
}
