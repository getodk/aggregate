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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class BinaryPopup extends AbstractPopupBase {

  public BinaryPopup(String url, boolean larger) {
    super();
    setTitle("Binary");

    int width = Window.getClientWidth() / 2;
    int height = Window.getClientHeight() / 2;

    if (larger) {
      width = Window.getClientWidth() * 4 / 5;
      height = Window.getClientHeight() * 4 / 5;
    }

    Frame frame = new Frame(url);
    frame.setPixelSize(width, height);

    FlowPanel panel = new FlowPanel();
    panel.setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    panel.setPixelSize(width + 6, height + 30);
    panel.add(new SimplePanel(new ClosePopupButton(this)));
    panel.add(frame);
    setWidget(panel);
  }
}
