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

package org.opendatakit.aggregate.client.form;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the APIs available to users with the ROLE_DATA_VIEWER privilege.
 * Adding forms, deleting forms, and other forms management should be 
 * handled in the FormAdminService (which requires ROLE_DATA_OWNER privilege).
 * 
 * @author wbrunette@gmail.com
 *
 */
@RemoteServiceRelativePath("formservice")
public interface FormService extends RemoteService {

  ArrayList<FormSummary> getForms() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
  
  ArrayList<ExportSummary> getExports() throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  Boolean createCsvFromFilter(FilterGroup group) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  Boolean createJsonFileFromFilter(FilterGroup group) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  KmlOptionsSummary getPossibleKmlSettings(String formId) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  Boolean createKmlFromFilter(FilterGroup group, ArrayList<KmlSelection> kmlElementsToInclude) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  GeopointElementList getGpsCoordnates(String formId) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
  
  void deleteExport(String uri) throws AccessDeniedException, FormNotAvailableException, RequestFailureException, DatastoreFailureException;
}
