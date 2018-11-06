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

package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

@RemoteServiceRelativePath("filterservice")
public interface FilterService extends RemoteService {

  FilterSet getFilterSet(String formId) throws FormNotAvailableException, RequestFailureException, DatastoreFailureException;

  /**
   * Saves the supplied filter group in the database
   *
   * @param group the filter group to be saved in the DB
   * @return uid of the filter group on success, null on failure
   * @throws AccessDeniedException
   */
  String updateFilterGroup(FilterGroup group) throws RequestFailureException, DatastoreFailureException;

  Boolean deleteFilterGroup(FilterGroup group) throws RequestFailureException, DatastoreFailureException;
}
