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

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.popups.VisualizationPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class VisualizationButton extends AbstractButtonBase implements ClickHandler {
  
  private static final String TOOLTIP_TEXT = "Visualize the data";
  
  private FilterSubTab filterSubTab;
  
  public VisualizationButton(FilterSubTab filterSubTab) {
    super("<img src=\"images/bar_chart.png\" /> Visualize", TOOLTIP_TEXT);
    this.filterSubTab = filterSubTab;
  }

  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    final PopupPanel vizPopup = new VisualizationPopup(filterSubTab);
    vizPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
          int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
          int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
          vizPopup.setPopupPosition(left, top);
      }
    });
  }
}
