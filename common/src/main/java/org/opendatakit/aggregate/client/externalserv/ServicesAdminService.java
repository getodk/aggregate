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


import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the actions requiring the ROLE_DATA_OWNER privilege.  They
 * pertain to setting up and canceling the publication of data to external
 * servers (e.g., Google Fusion Tables or Google Spreadsheets).
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
@RemoteServiceRelativePath("servicesadminservice")
public interface ServicesAdminService extends RemoteService {

  ExternServSummary [] getExternalServices(String formid) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  String createFusionTable(String formId, ExternalServicePublicationOption esOption, String ownerEmail) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  String createGoogleSpreadsheet(String formId, String name, ExternalServicePublicationOption esOption, String ownerEmail) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  String createSimpleJsonServer(String formId, String authKey, String url, ExternalServicePublicationOption es, String ownerEmail, BinaryOption binaryOption) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  String createOhmageJsonServer(String formId, String campaignUrn, String campaignTimestamp, String user, String hashedPassword, String url, ExternalServicePublicationOption es, String ownerEmail) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  String createRedCapServer(String formId, String apiKey, String url, ExternalServicePublicationOption esOption, String ownerEmail) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  Boolean deletePublisher(String uri) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  void restartPublisher(String uri) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  void updateApiKeyAndRestartPublisher(String uri, String apiKey) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
}
