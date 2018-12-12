/*
 * Copyright (C) 2013-2014 University of Washington
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

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public final class EnketoRedirectErrorPopup extends AbstractPopupBase {

  private final String ENKETO_WEBFORM_ERROR;

  public EnketoRedirectErrorPopup(String submissionKeyAsString) {
    super();

    this.ENKETO_WEBFORM_ERROR = submissionKeyAsString;

    FlexTable layout = new FlexTable();

    HTML message = new HTML(ENKETO_WEBFORM_ERROR);
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

}
