package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.table.ExportTable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ExportSubTab extends VerticalPanel implements SubTabInterface {

  private ExportTable exportTable;
  
  public ExportSubTab() {
    exportTable = new ExportTable();
    add(exportTable);
  }
  
  public void update() {
    
    FormServiceAsync  formSvc = SecureGWT.get().createFormService();

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

    formSvc.getExports(callback);
  }
  
}
