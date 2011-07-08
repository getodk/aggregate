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
        // TODO Auto-generated method stub
      }

      @Override
      public void onSuccess(ExportSummary[] result) {
        exportTable.updateExportPanel(result);
      }
    };

    SecureGWT.getFormService().getExports(callback);
  }
  
}
