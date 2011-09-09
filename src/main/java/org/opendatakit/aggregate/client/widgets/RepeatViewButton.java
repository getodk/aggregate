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

import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.client.popups.RepeatPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class RepeatViewButton extends AbstractButtonBase implements ClickHandler {

  private static final String TOOLTIP_TEXT = "View the items";

private static final String HELP_BALLOON_TXT = "This will open up a smaller popup table with the items" +
		"you are requesting to view.";

  private String url;

  public RepeatViewButton(String url) {
    super("View", TOOLTIP_TEXT);
    this.url = url;
    helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    RepeatPopup popup = new RepeatPopup(url);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }
}