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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;


public class AuditCSVPopup extends AbstractPopupBase {
  public AuditCSVPopup(String keyString) {
    super();
    String[] parts = keyString.split("\\?");
    if (parts.length != 2)
      throw new RuntimeException("blobKey missing in keyString");
    String blobKey = parts[1].split("=")[1];

    setTitle("Audit CSV");
    int width = Window.getClientWidth() / 2;
    int height = Window.getClientHeight() / 2;

    final FlowPanel panel = new FlowPanel();
    panel.setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    panel.setPixelSize(width + 6, height + 30);
    panel.add(new SimplePanel(new ClosePopupButton(this)));
    final HTML loading = new HTML("<h1>Loading Audit CSV... Please wait</h1>");
    panel.add(loading);
    setWidget(panel);


    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(String csvContents) {
        AggregateUI.getUI().clearError();
        panel.remove(loading);
        panel.add(new HTML(csvContents));
        AggregateUI.resize();
      }
    };

    SecureGWT.getSubmissionService().getSubmissionAuditCSV(blobKey, callback);
  }
}
