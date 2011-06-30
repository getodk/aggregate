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

import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the actions requiring the ROLE_SERVICES_ADMIN privilege.  They
 * pertain to setting up and cancelling the publication of data to external
 * servers (e.g., Google Fusion Tables or Google Spreadsheets).
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@RemoteServiceRelativePath("servicesadminservice")
public interface ServicesAdminService extends RemoteService {

  ExternServSummary [] getExternalServices(String formid) throws AccessDeniedException;
  
  String generateOAuthUrl(String uri) throws AccessDeniedException;
  
  String createFusionTable(String formId, ExternalServicePublicationOption esOption) throws AccessDeniedException;
  
  String createGoogleSpreadsheet(String formId, String name, ExternalServicePublicationOption esOption) throws AccessDeniedException;
  
  Boolean deletePublisher(String uri) throws AccessDeniedException;
}
