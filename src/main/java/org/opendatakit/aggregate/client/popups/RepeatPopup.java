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
import com.google.gwt.user.client.ui.ScrollPanel;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public class RepeatPopup extends AbstractPopupBase {

  private FlowPanel panel;

  public RepeatPopup(final String keyString) {
    super();
    panel = new FlowPanel();


    // Set up the callback object.
    AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(SubmissionUISummary summary) {
        panel.add(new SubmissionTable(summary, false)); //contains the data
      }
    };

    // obtain repeats
    SecureGWT.getSubmissionService().getRepeatSubmissions(keyString, callback);


    // populate the panel
    panel.add(new ClosePopupButton(this));

    ScrollPanel scroll = new ScrollPanel(panel);
    scroll.setPixelSize((Window.getClientWidth() * 3 / 4), (Window.getClientHeight() * 3 / 4));
    setWidget(scroll);
  }


}
