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


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;

/**
 * These are the actions requiring the ROLE_DATA_OWNER privilege.  They
 * pertain to setting up and canceling the publication of data to external
 * servers (e.g., Google Fusion Tables or Google Spreadsheets).
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 */
@RemoteServiceRelativePath("servicesadminservice")
public interface ServicesAdminService extends RemoteService {

  ExternServSummary[] getExternalServices(String formid) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  String createFusionTable(String formId, ExternalServicePublicationOption esOption, String ownerEmail) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  String createGoogleSpreadsheet(String formId, String name, ExternalServicePublicationOption esOption, String ownerEmail) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  String createSimpleJsonServer(String formId, String authKey, String url, ExternalServicePublicationOption es, String ownerEmail, BinaryOption binaryOption) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  String createOhmageJsonServer(String formId, String campaignUrn, String campaignTimestamp, String user, String hashedPassword, String url, ExternalServicePublicationOption es, String ownerEmail) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  String createRedCapServer(String formId, String apiKey, String url, ExternalServicePublicationOption esOption, String ownerEmail) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  Boolean deletePublisher(String uri) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  void restartPublisher(String uri) throws RequestFailureException, DatastoreFailureException;

  @XsrfProtect
  void updateApiKeyAndRestartPublisher(String uri, String apiKey) throws RequestFailureException, DatastoreFailureException;
}
