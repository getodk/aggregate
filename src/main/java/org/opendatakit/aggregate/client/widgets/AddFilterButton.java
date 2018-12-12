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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.popups.FilterPopup;

public final class AddFilterButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/yellow_plus.png\" /> Add Filter";
  private static final String TOOLTIP_TXT = "Add a new filter";
  private static final String HELP_BALLOON_TXT = "The button displays a popup window that allows a new filter to be added to the filter group";

  private final FilterSubTab basePanel;

  public AddFilterButton(FilterSubTab panel) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    basePanel = panel;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    FilterGroup currentFilterGroup = basePanel.getDisplayedFilterGroup();
    // check if we should even pop up a filter if there is no form
    if (currentFilterGroup.getFormId() == null) {
      return;
    }

    FilterPopup filterPopup = new FilterPopup(basePanel.getSubmissionTable(), currentFilterGroup);
    filterPopup.setPopupPositionAndShow(filterPopup.getPositionCallBack());
    filterPopup.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        basePanel.update();
      }

    });
  }

}