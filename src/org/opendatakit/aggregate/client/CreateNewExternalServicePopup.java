package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class CreateNewExternalServicePopup extends PopupPanel {
  private ManageTabUI parent;
  
  private static final String TYPE_SPREAD_SHEET = "Google Spreadsheet";
  private static final String TYPE_FUSION_TABLE = "Google Fusion Table";
  
  public CreateNewExternalServicePopup(final String formId, final FormServiceAsync formSvc, final ManageTabUI parent) {
    super(false);
    this.parent = parent;
    FlexTable layout = new FlexTable();
    
    layout.setWidget(0, 0, new HTML("Form: " + formId + " "));
    
    final TextBox name = new TextBox();
    name.setText("Spreadsheet Name");
    name.setEnabled(false);
    name.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        name.setText("");
      }
    });
    
    final ListBox service = new ListBox();
    service.addItem(TYPE_FUSION_TABLE);
    service.addItem(TYPE_SPREAD_SHEET);
    service.addChangeHandler(new ChangeHandler () {
      @Override
      public void onChange(ChangeEvent event) {
        if (service.getItemText(service.getSelectedIndex()).equals(TYPE_FUSION_TABLE)) {
          name.setText("Spreadsheet Name");
          name.setEnabled(false);
        } else { // .equals(TYPE_SPREAD_SHEET)
          name.setEnabled(true);
        }
      }
    });
    
    layout.setWidget(0, 1, service);
    layout.setWidget(0, 2, name);
    
    final ListBox esOptions = new ListBox();
    for (ExternalServiceOption eso : ExternalServiceOption.values())
      esOptions.addItem(eso.toString());
    layout.setWidget(0, 3, esOptions);
    
    Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
    publishButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ExternalServiceOption serviceOp = null;
        String selectedOption = esOptions.getItemText(esOptions.getSelectedIndex());
        for (ExternalServiceOption selected : ExternalServiceOption.values()) {
          if (selected.toString().equals(selectedOption))
            serviceOp = selected;
        }
        String selectedService = service.getItemText(service.getSelectedIndex());
        if (selectedService.equals(TYPE_FUSION_TABLE)) {
          formSvc.createFusionTable(formId, serviceOp, new AsyncCallback<String>() {
  
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }
  
            @Override
            public void onSuccess(String result) {
              parent.parent.formSvc.generateOAuthUrl(result, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                  // TODO Auto-generated method stub
                }
                @Override
                public void onSuccess(String result) {
                  // TODO Auto-generated method stub
                  parent.hash.goTo(result);
                }
              });
            }
          });
        } else { // selectedService.equals(TYPE_SPREAD_SHEET)
          formSvc.createGoogleSpreadsheet(formId, name.getText(), serviceOp, new AsyncCallback<String> () {
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(String result) {
              // TODO Auto-generated method stub
              parent.parent.formSvc.generateOAuthUrl(result, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                  // TODO Auto-generated method stub
                }
                
                @Override
                public void onSuccess(String result) {
                  // TODO Auto-generated method stub
                  parent.hash.goTo(result);
                }
              });
            }
          });
        }
      }
    });
    layout.setWidget(0, 4, publishButton);
	
	Button closeButton = new Button("<img src=\"images/red_x.png\" />");
	closeButton.addStyleDependentName("close");
	closeButton.addStyleDependentName("negative");
	closeButton.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			hide();
		}
	});
	layout.setWidget(0, 5, closeButton);
    
    setWidget(layout);
  }
}
