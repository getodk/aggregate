package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

// TODO: refactor so that shares the same code base with SubmissionTabUI

public class RepeatPopup extends PopupPanel {

  private FlowPanel panel;
  private FlexTable dataTable;
  private SubmissionServiceAsync submissionSvc;
  private AggregateUI parent;
  
  public RepeatPopup(final String keyString, final SubmissionServiceAsync submissionSvcInput, final AggregateUI parentInput) {
    super(false);
    panel = new FlowPanel();
    dataTable = new FlexTable(); //contains the data
    submissionSvc = submissionSvcInput;
    parent = parentInput;
    
    // Initialize the service proxy.
    if (submissionSvc == null) {
       submissionSvc = SecureGWT.get().createSubmissionService();
    }

    // Set up the callback object.
    AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
       public void onFailure(Throwable caught) {
          // TODO: deal with error
       }

       public void onSuccess(SubmissionUISummary summary) {
         parent.getSubmissionNav().updateSubmissionTable(dataTable, summary);
       }
    };
    
    // obtain repeats
    submissionSvc.getRepeatSubmissions(keyString, callback);
    
    // create close button
    Button closeButton = new Button("<img src=\"images/red_x.png\" />");
    closeButton.addStyleDependentName("close");
    closeButton.addStyleDependentName("negative");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    
    // populate the panel
    panel.add(closeButton);
    panel.add(dataTable);
    
    ScrollPanel scroll = new ScrollPanel(panel);
    scroll.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2));
    setWidget(scroll);
  }
}
