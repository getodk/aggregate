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

import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the APIs available to users with the ROLE_ANALYST privilege.
 * Adding forms, deleting forms, and other forms management should be 
 * handled in the FormAdminService (which requires ROLE_FORM_ADMIN privilege).
 * 
 * @author wbrunette@gmail.com
 *
 */
@RemoteServiceRelativePath("formservice")
public interface FormService extends RemoteService {

  FormSummary [] getForms() throws AccessDeniedException;
  
  ExportSummary [] getExports() throws AccessDeniedException;
 
  Boolean createCsv(String formId) throws AccessDeniedException;
  
  KmlSettings getPossibleKmlSettings(String formId) throws AccessDeniedException;
  
  Boolean createKml(String formId, String geopointKey, String titleKey, String binaryKey) throws AccessDeniedException;
  
  KmlSettings getGpsCoordnates(String formId) throws AccessDeniedException;
  
}
