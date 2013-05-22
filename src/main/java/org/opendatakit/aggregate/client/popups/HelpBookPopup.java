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

import org.opendatakit.aggregate.client.table.BinaryPopupClickHandler;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.BookHelpConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class HelpBookPopup extends PopupPanel {

  private FlowPanel panel;

  public HelpBookPopup() {
    super(false);
    panel = new FlowPanel(); // vertical
    panel.setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    // populate the panel
    panel.add(new ClosePopupButton(this));

    BookHelpConsts[] consts = BookHelpConsts.values();

    FlowPanel content = new FlowPanel(); // vertical
    content.setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    for (int i = 0; i < consts.length; i++) {
      content.add(new HTML("<h2 id=\"form_name\">" + consts[i].getTitle() + "</h2>"));

      if (consts[i].getVideoUrl() != null) {
        String buttonTxt = consts[i].getTitle() + " Video Assistance";
        String toolTipTxt = "Play Video descibing how to " + consts[i].getTitle();
        AggregateButton vidButton = new AggregateButton(buttonTxt, toolTipTxt);
        vidButton.addClickHandler(new BinaryPopupClickHandler(consts[i].getVideoUrl(), true));
        content.add(vidButton);
      }

      content.add(new HTML(consts[i].getConcept() + "<br><br>"));
      content.add(new HTML(consts[i].getProcedures() + "<br><br>"));
    }

    ScrollPanel scroll = new ScrollPanel(content);
    scroll.setPixelSize((Window.getClientWidth() * 3 / 4), (Window.getClientHeight() * 3 / 4));

    panel.add(scroll);

    setWidget(panel);
    center();
  }
}
