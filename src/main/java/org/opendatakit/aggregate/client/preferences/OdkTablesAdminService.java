/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

@RemoteServiceRelativePath("odktablesadmin")
public interface OdkTablesAdminService extends RemoteService {
  OdkTablesAdmin[] listAdmin() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;

  Boolean deleteAdmin(String aggregateUid) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;

  Boolean updateAdmin(OdkTablesAdmin admin) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;

  Boolean setAdmins(ArrayList<UserSecurityInfo> admins) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
}

