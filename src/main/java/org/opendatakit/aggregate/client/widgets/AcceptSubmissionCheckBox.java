package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.form.FormAdminServiceAsync;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AcceptSubmissionCheckBox extends ACheckBoxBase implements ValueChangeHandler<Boolean> {
  
  private String formId;

  public AcceptSubmissionCheckBox(String formId, Boolean accept) {
    super();
    this.formId = formId;
    setValue(accept);
    addValueChangeHandler(this);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);
    FormAdminServiceAsync formAdminSvc = SecureGWT.get().createFormAdminService();
    formAdminSvc.setFormAcceptSubmissions(formId, event.getValue(), new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        AggregateUI.getUI().clearError();
      }
    });
  }

}
