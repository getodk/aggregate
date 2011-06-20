package org.opendatakit.aggregate.client.externalserv;

import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServicesAdminServiceAsync {

  void createFusionTable(String formId, ExternalServicePublicationOption esOption,
      AsyncCallback<String> callback);

  void createGoogleSpreadsheet(String formId, String name,
      ExternalServicePublicationOption esOption, AsyncCallback<String> callback);

  void generateOAuthUrl(String uri, AsyncCallback<String> callback);

  void getExternalServices(String formid, AsyncCallback<ExternServSummary[]> callback);

}
