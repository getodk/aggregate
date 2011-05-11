package org.opendatakit.aggregate.client.form;

import org.opendatakit.aggregate.constants.common.ExternalServiceOption;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

  void getForms(AsyncCallback<FormSummary[]> callback);

  void getExternalServices(String formid, AsyncCallback<ExternServSummary[]> callback);

  void getExports(AsyncCallback<ExportSummary[]> callback);

  void createCsv(String formId, AsyncCallback<Boolean> callback);

  void getPossibleKmlSettings(String formId, AsyncCallback<KmlSettings> callback);

  void createKml(String formId, String geopointKey, String titleKey, String binaryKey,
      AsyncCallback<Boolean> callback);

  void setFormDownloadable(String formId, Boolean downloadable, AsyncCallback<Boolean> callback);

  void setFormAcceptSubmissions(String formId, Boolean acceptSubmissions, AsyncCallback<Boolean> callback);

  void createFusionTable(String formId, ExternalServiceOption esOption, AsyncCallback<String> callback);

  void generateOAuthUrl(String uri, AsyncCallback<String> callback);

  void createGoogleSpreadsheet(String formId, String name, ExternalServiceOption esOption,
      AsyncCallback<String> callback);

  void getGpsCoordnates(String formId, AsyncCallback<KmlSettings> callback);

}
