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
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.popups.ConfirmExportDeletePopup;

/**
 * Delete an exported datafile that may or may not be in the process of being created.
 */
public final class DeleteExportButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" /> Delete";
  private static final String TOOLTIP_TEXT = "Remove exported datafile";
  private static final String HELP_BALLOON_TXT = "This confirms that you want to delete the exported datafile.";

  private final ExportSummary export;

  public DeleteExportButton(ExportSummary export) {
    super(BUTTON_TXT, TOOLTIP_TEXT, HELP_BALLOON_TXT);
    this.export = export;
    addStyleDependentName("negative");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    ConfirmExportDeletePopup popup = new ConfirmExportDeletePopup(export);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }
}