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
import org.opendatakit.aggregate.client.widgets.ExecuteDeleteFormButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmFormDeletePopup extends PopupPanel {

  public ConfirmFormDeletePopup(String formId) {
    super(false);

    FlexTable layout = new FlexTable();
   
    HTML message = new HTML(
        "Delete all data and the form definition for <b>"
        + formId
        + "</b>?<br/>Do you wish to delete all uploaded data and the form definition for this form?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, new ExecuteDeleteFormButton(formId, this));
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }
}