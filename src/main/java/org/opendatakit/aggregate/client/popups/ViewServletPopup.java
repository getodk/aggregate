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

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;

public class ViewServletPopup extends AbstractPopupBase {

  public ViewServletPopup(String title, String url) {
    super();
    setTitle(title);
    
    // so we can play with the dimensions...
    int innerWidth = Window.getClientWidth()*2 / 3;
    int innerHeight = Window.getClientHeight()*2 /3;
    
    Frame frame = new Frame(url);
    frame.addStyleName("uploadFrame");
    frame.setPixelSize(innerWidth - 30,innerHeight);

    FlowPanel panel = new FlowPanel();
    panel.setPixelSize(innerWidth+6,innerHeight+30);
    
    ClosePopupButton closeButton = new ClosePopupButton(this);
    closeButton.addStyleName("uploadCloseButton");
    panel.add(closeButton);
    panel.add(frame);
    setWidget(panel);
  }
}
