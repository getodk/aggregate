package org.opendatakit.aggregate.client.form;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

	void getForms(AsyncCallback<FormSummary []> callback);

}
