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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import java.util.Date;


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

    final HTMLPanel panel = new HTMLPanel("");
    panel.add(new SimplePanel(new ClosePopupButton(this)));
    panel.add(new HTML("<h2>Audit CSV contents</h2>"));
    panel.setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    panel.getElement().getStyle().setProperty("overflow", "scroll");
    panel.setPixelSize(width + 6, height + 30);
    setWidget(panel);

    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(String csvContents) {
        String[] allLines = csvContents.split("\n");

        StringBuilder html = new StringBuilder("<table class=\"dataTable\">");

        html.append("<tr class=\"titleBar\">")
            .append("<td>Event</td><td>Node</td><td>Start</td><td>End</td>")
            .append("</tr>");
        for (int i = 1, max = allLines.length; i < max; i++) {
          html.append(Row.from(allLines[i]).asTr());
        }
        html.append("</table>");

        AggregateUI.getUI().clearError();
        panel.add(new HTML(html.toString()));
        AggregateUI.resize();
      }
    };

    SecureGWT.getSubmissionService().getSubmissionAuditCSV(blobKey, callback);
  }

  static private class Row {
    private final String event;
    private final String node;
    private final Date start;
    private final Date end;

    private Row(String event, String node, Date start, Date end) {
      this.event = event;
      this.node = node;
      this.start = start;
      this.end = end;
    }

    static Row from(String csvLine) {
      String[] values = csvLine.split(",");
      return new Row(
          values[0],
          values[1],
          new Date(Long.parseLong(values[2])),
          values.length == 4 ? new Date(Long.parseLong(values[3])) : null
      );
    }

    String asTr() {
      return "<tr>" +
          "<td>" + this.event + "</td>" +
          "<td>" + this.node + "</td>" +
          "<td>" + this.start.toString() + "</td>" +
          "<td>" + this.getEnd() + "</td>" +
          "</tr>";
    }

    String getEnd() {
      return this.end != null ? this.end.toString() : "N/A";
    }

  }
}
