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

import static org.opendatakit.aggregate.client.security.SecurityUtils.secureRequest;
import static org.opendatakit.aggregate.constants.common.ExportStatus.ABANDONED;
import static org.opendatakit.aggregate.constants.common.ExportStatus.AVAILABLE;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

/**
 * Popup asking for confirmation to delete an exported data file
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class ConfirmExportDeletePopup extends AbstractPopupBase {

  private static final String BUTTON_ICON = "<img src=\"images/green_right_arrow.png\"/>";
  private static final String TOOLTIP_TXT = "Remove this exported datafile";
  private static final String HELP_BALLOON_TXT = "This will remove this exported datafile. You will no longer be able to download this datafile.";

  public ConfirmExportDeletePopup(ExportSummary export) {
    super();

    String action = (export.getStatus() == AVAILABLE || export.getStatus() == ABANDONED)
        ? "remove"
        : "cancel generation and remove";

    String buttonTxt = BUTTON_ICON + action + " exported datafile";
    AggregateButton deleteButton = new AggregateButton(buttonTxt, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(event -> {
      secureRequest(
          SecureGWT.getFormService(),
          (rpc, sessionCookie, cb) -> rpc.deleteExport(export.getUri(), cb),
          this::onSuccess,
          this::onError
      );
      hide();
    });

    FlexTable layout = new FlexTable();

    HTML message = new HTML(new SafeHtmlBuilder()
        .appendEscaped("Delete this exported datafile?")
        .appendHtmlConstant("<br/>")
        .appendEscaped("Do you wish to " + action + " this exported datafile?")
        .toSafeHtml());
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private void onError(Throwable cause) {
    AggregateUI.getUI().reportError(cause);
  }

  private void onSuccess() {
    AggregateUI.getUI().clearError();
    AggregateUI.getUI().getTimer().refreshNow();
  }
}