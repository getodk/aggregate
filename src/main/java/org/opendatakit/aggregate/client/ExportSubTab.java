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

package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.table.ExportTable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ExportSubTab extends VerticalPanel implements SubTabInterface {

  private ExportTable exportTable;
  
  public ExportSubTab() {
    exportTable = new ExportTable();
    add(exportTable);
  }

  @Override
  public boolean canLeave() {
	  return true;
  }
  
  @Override
  public void update() {
    
    AsyncCallback<ExportSummary[]> callback = new AsyncCallback<ExportSummary[]>() {
      @Override
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(ExportSummary[] result) {
        exportTable.updateExportPanel(result);
      }
    };

    SecureGWT.getFormService().getExports(callback);
  }
  
}
