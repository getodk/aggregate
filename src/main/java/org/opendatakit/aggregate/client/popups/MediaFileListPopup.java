package org.opendatakit.aggregate.client.popups;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

public class MediaFileListPopup extends AbstractPopupBase {

  private FlexTable fileList;
  
  public MediaFileListPopup(String formId) {

    SecureGWT.getFormAdminService().getFormMediaFileList(formId, new MediaFileCallback());

    fileList = new FlexTable();
    fileList.setWidget(0, 0, new ClosePopupButton(this));
    fileList.getCellFormatter().getElement(0, 0).setAttribute("align", "right");
    fileList.setText(1, 0, "Media Filename");
    fileList.getRowFormatter().addStyleName(1, "titleBar");
  
    setWidget(fileList);   
  }
  
  
  private class MediaFileCallback implements AsyncCallback<ArrayList<String>> {

    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(ArrayList<String> result) {
      if(result == null) 
        return;
      
      int index = 2;
      for(String file : result) {
        fileList.setText(index++, 0, file);
      }
    }
    
  }
  
}
