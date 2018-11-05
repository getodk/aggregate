/*
 * Copyright (C) 2014 University of Washington
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
import org.opendatakit.aggregate.client.AggregateSubTabBase;

public class OdkTablesDeleteAppLevelFileButton extends AggregateButton {


  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
  private static final String TOOLTIP_TXT = "Delete File";
  private static final String HELP_BALLOON_TXT = "Completely delete this file.";

  private final String odkClientApiVersion;
  private final String filePath;

  private AggregateSubTabBase basePanel;

  public OdkTablesDeleteAppLevelFileButton(AggregateSubTabBase basePanel,
                                           String odkClientApiVersion, String filePath) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.basePanel = basePanel;
    this.odkClientApiVersion = odkClientApiVersion;
    this.filePath = filePath;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    OdkTablesConfirmDeleteAppLevelFilePopup popup =
        new OdkTablesConfirmDeleteAppLevelFilePopup(basePanel, odkClientApiVersion, filePath);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }

}
