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

package org.opendatakit.aggregate.databaseRepair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateButton;

public class DatabaseRepairSubTab extends AggregateSubTabBase {

  private final FlexTable reportsTable;
  final FlowPanel reviewPanel;

  public DatabaseRepairSubTab() {
    reportsTable = new FlexTable();
    reportsTable.addStyleName("dataTable");
    add(new HTML("<h3>Form corruption report</h3>"));
    add(reportsTable);

    reviewPanel = new FlowPanel("div");
    add(new HTML("<h3>Review panel</h3>"));
    add(reviewPanel);

    update();
  }


  @Override
  public boolean canLeave() {
    return true;
  }

  @Override
  public void update() {
    reviewPanel.clear();
    SecureGWT.getDatabaseRepairService().formsMissingFileset(new AsyncCallback<List<FormCorruptionReport>>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(List<FormCorruptionReport> reports) {
        AggregateUI.getUI().clearError();

        reportsTable.removeAllRows();
        setReportsTableHeader();
        for (int i = 0; i < reports.size(); i++) {
          FormCorruptionReport form = reports.get(i);
          reportsTable.setText(i + 1, 0, form.getFormId());
          reportsTable.setWidget(i + 1, 1, buildFilesetCell(form.getFileset()));
          if (i % 2 == 0)
            reportsTable.getRowFormatter().addStyleName(i, "evenTableRow");
        }
      }
    });
  }

  private void setReportsTableHeader() {
    reportsTable.setText(0, 0, "Form ID");
    reportsTable.setText(0, 1, "# Filesets");
    reportsTable.getRowFormatter().addStyleName(0, "titleBar");
  }

  private Widget buildFilesetCell(final FilesetReport fileset) {
    if (fileset.isOk())
      return new HTML("ok");
    FlowPanel aDiv = new FlowPanel("div");
    aDiv.add(new InlineHTML(fileset.getCorruptionCause()));
    if (fileset.canBeFixed()) {
      Button button = new AggregateButton("Review", "Review");
      button.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          new FilesetReviewPanel(fileset, DatabaseRepairSubTab.this);
        }
      });
      aDiv.add(button);
    }
    return aDiv;
  }
}
