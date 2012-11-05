package org.opendatakit.aggregate.client.popups;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

public class OdkTablesMediaFileListPopup extends AbstractPopupBase {

  private FlexTable fileList;
  
  public OdkTablesMediaFileListPopup(String tableId, String key) {
    
    SecureGWT.getServerDataService().getMedialFilesKey(tableId, key, 
        new MediaFileCallback());

    fileList = new FlexTable();
    fileList.setWidget(0, 0, new ClosePopupButton(this));
    fileList.getCellFormatter().getElement(0, 0).setAttribute("align", 
        "right");
    fileList.setText(1, 0, "Media Filename");
    fileList.setText(1, 1, "Content Type");
    fileList.setText(1, 2, "Length");
    fileList.getRowFormatter().addStyleName(1, "titleBar");
  
    setWidget(fileList);   
  }
  
  
  private class MediaFileCallback implements 
      AsyncCallback<List<FileSummaryClient>> {

    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(List<FileSummaryClient> result) {
      if(result == null) 
        return;
      
      int index = 2;
      for(FileSummaryClient file : result) {
        fileList.setText(index, 0, file.getFilename());
        fileList.setText(index, 1, file.getContentType());
        fileList.setText(index, 2, Long.toString(file.getContentLength()));
        index++;
      }
    }
    
  }
  
}