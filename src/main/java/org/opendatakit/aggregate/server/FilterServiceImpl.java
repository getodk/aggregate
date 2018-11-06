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

package org.opendatakit.aggregate.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

public class FilterServiceImpl extends RemoteServiceServlet implements FilterService {

  /**
   * identifier for serialization
   */
  private static final long serialVersionUID = 6350939191805868959L;

  @Override
  public FilterSet getFilterSet(String formId) throws FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      // verify form is still available
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }

      FilterSet filterSet = new FilterSet(formId);

      List<SubmissionFilterGroup> filterGroupList = SubmissionFilterGroup.getFilterGroupList(
          formId, cc);
      for (SubmissionFilterGroup group : filterGroupList) {
        if (group.isPublic()) {
          filterSet.addFilterGroup(group.transform());
        }
      }
      return filterSet;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @Override
  public String updateFilterGroup(FilterGroup group) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.persist(cc);
      return filterGrp.getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @Override
  public Boolean deleteFilterGroup(FilterGroup group) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.delete(cc);
      return Boolean.TRUE;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }
}
