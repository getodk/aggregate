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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class HelpBalloon extends PopupPanel {

  private static final int SPACE = 1;

  private int offsetX;
  private int offsetY;
  private Widget widget;

  public HelpBalloon(Widget sender, final String text) {
    this(sender, 0, 0, text);
  }

  public HelpBalloon(Widget sender, int offsetX, int offsetY, final String text) {
    super(true);
    this.widget = sender;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    add(new HTML(text));
  }

  public void display() {
    // determine position
    int x = widget.getAbsoluteLeft();
    int y = widget.getAbsoluteTop();

    int halfScreenSizeX = Window.getClientWidth() / 2;
    int halfScreenSizeY = Window.getClientHeight() / 2;

    // decide where the popup should go based on the quadrant of the screen
    // the popup should always appear towards the center
    if (x >= halfScreenSizeX && y >= halfScreenSizeY) { // 4
      // put ballon in the upper left corner
      x -= (this.getOffsetWidth() * 3 / 4 + SPACE);
      y -= (this.getOffsetHeight() + SPACE);
    } else if (x < halfScreenSizeX && y >= halfScreenSizeY) { // 3
      // put balloon in upper right corner
      x += (widget.getOffsetWidth() * 3 / 4 + SPACE);
      y -= (this.getOffsetHeight() + SPACE);
    } else if (x >= halfScreenSizeX && y < halfScreenSizeY) { // 2
      // put balloon in bottom left corner
      x -= (this.getOffsetWidth() * 3 / 4 + SPACE);
      y += (widget.getOffsetHeight() + SPACE);
    } else {
      // put balloon in bottom right corner
      x += (widget.getOffsetWidth() * 3 / 4 + SPACE);
      y += (widget.getOffsetHeight() + SPACE);
    }

    setPopupPosition(x + offsetX, y + offsetY);
    show();
  }

}
