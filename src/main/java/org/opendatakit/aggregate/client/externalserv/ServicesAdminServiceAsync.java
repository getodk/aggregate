/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.externalserv;

import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServicesAdminServiceAsync {

  void getExternalServices(String formid, AsyncCallback<ExternServSummary[]> callback);

  void createFusionTable(String formId, ExternalServicePublicationOption esOption,
      String ownerEmail, AsyncCallback<String> callback);

  void createGoogleSpreadsheet(String formId, String name,
      ExternalServicePublicationOption esOption, String ownerEmail, AsyncCallback<String> callback);

  void createSimpleJsonServer(String formId, String authKey, String url,
      ExternalServicePublicationOption es, String ownerEmail, BinaryOption binaryOption,
      AsyncCallback<String> callback);

  void createOhmageJsonServer(String formId, String campaignUrn, String campaignTimestamp,
      String user, String hashedPassword, String url, ExternalServicePublicationOption es,
      String ownerEmail, AsyncCallback<String> callback);

  void createRedCapServer(String formId, String apiKey, String url,
      ExternalServicePublicationOption esOption, String ownerEmail, AsyncCallback<String> callback);

  void deletePublisher(String uri, AsyncCallback<Boolean> callback);

  void restartPublisher(String uri, AsyncCallback<Void> callback);

  void updateApiKeyAndRestartPublisher(String uri, String apiKey, AsyncCallback<Void> callback);

}
