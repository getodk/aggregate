package org.opendatakit.aggregate.client.form;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormAdminServiceAsync {

  void deleteForm(String formId, AsyncCallback<Boolean> callback);

  void purgePublishedData(String uriExternalService, Date earliest, AsyncCallback<Date> callback);

  void setFormAcceptSubmissions(String formId, Boolean acceptSubmissions,
      AsyncCallback<Boolean> callback);

  void setFormDownloadable(String formId, Boolean downloadable, AsyncCallback<Boolean> callback);

}
