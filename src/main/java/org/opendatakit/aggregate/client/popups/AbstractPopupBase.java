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
import com.google.gwt.user.client.ui.PopupPanel;

public class AbstractPopupBase extends PopupPanel {

  public AbstractPopupBase() {
    super(false);
    setModal(true); // things not in the popup are inactive

    // Set glass behind the popup so that the things behind it are grayed out.
    this.setGlassEnabled(true);
    this.setGlassStyleName("gwt-PopupPanelGlassAggregate");

  }

  public PopupPanel.PositionCallback getPositionCallBack() {
    return new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = Window.getScrollLeft() + ((Window.getClientWidth() - offsetWidth) / 2);
        int top = Window.getScrollTop() + ((Window.getClientHeight() - offsetHeight) / 2);
        setPopupPosition(left, top);
      }
    };
  }

}
