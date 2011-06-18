package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UrlHash;
import org.opendatakit.aggregate.client.permissions.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.client.popups.ExternalServicePopup;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class ExecutePublishButton  extends AButtonBase implements ClickHandler {
 
  private ExternalServicePopup popup;
  
  public ExecutePublishButton(ExternalServicePopup popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Publish");
    this.popup = popup;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    String formId = popup.getFormId(); 
    TextBox name = popup.getName();
    ListBox service = popup.getService();
    ListBox esOptions = popup.getEsOptions();
    
    ExternalServicePublicationOption serviceOp = null;
    String selectedOption = esOptions.getItemText(esOptions.getSelectedIndex());
    for (ExternalServicePublicationOption selected : ExternalServicePublicationOption.values()) {
      if (selected.toString().equals(selectedOption))
        serviceOp = selected;
    }
    String selectedService = service.getItemText(service.getSelectedIndex());
    ServicesAdminServiceAsync servicesAdminSvc = SecureGWT.get().createServicesAdminService();
    if (selectedService.equals(ExternalServicePopup.TYPE_FUSION_TABLE)) {
      servicesAdminSvc.createFusionTable(formId, serviceOp, new CreateFusionTablesCallback());
    } else { // selectedService.equals(TYPE_SPREAD_SHEET)
      servicesAdminSvc.createGoogleSpreadsheet(formId, name.getText(), serviceOp,
          new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(String result) {
              // TODO Auto-generated method stub
              ServicesAdminServiceAsync servicesAdminSvc = SecureGWT.get()
                  .createServicesAdminService();
              servicesAdminSvc.generateOAuthUrl(result, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                  // TODO Auto-generated method stub
                }

                @Override
                public void onSuccess(String result) {
                  // TODO Auto-generated method stub
                  UrlHash.getHash().goTo(result);
                }
              });
            }
          });
    }
    
    AggregateUI.getUI().getTimer().restartTimer();
    popup.hide();
  }

  private class CreateFusionTablesCallback implements AsyncCallback<String> {
        
    public void onFailure(Throwable caught) {
      // TODO Auto-generated method stub
    }


    public void onSuccess(String result) {
      ServicesAdminServiceAsync servicesAdminSvc = SecureGWT.get()
          .createServicesAdminService();
      servicesAdminSvc.generateOAuthUrl(result, new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
        }

        @Override
        public void onSuccess(String result) {
          // TODO Auto-generated method stub
          UrlHash.getHash().goTo(result);
        }
      });
    }
  }
}
   
  
