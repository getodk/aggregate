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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilterServiceImpl extends RemoteServiceServlet implements FilterService {

  /**
   * identifier for serialization
   */
  private static final long serialVersionUID = 6350939191805868959L;
 

  @Override
  public FilterSet getFilterSet(String formId) throws FormNotAvailableException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    try {
      // verify form is still available
      Form.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e1) {
      throw new FormNotAvailableException(e1);
    }
    
    FilterSet filterSet = new FilterSet(formId);
    
    try {
      List<SubmissionFilterGroup> filterGroupList = SubmissionFilterGroup.getFilterGroupList(formId, cc);
      for(SubmissionFilterGroup group : filterGroupList) {
        filterSet.addFilterGroup(group.transform());
      }
    } catch (Exception e) {
      // TODO: send exception over service
      e.printStackTrace();
    }
    return filterSet;
  }
  
  @Override
  public String updateFilterGroup(FilterGroup group) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.persist(cc);      
      return filterGrp.getUri();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Boolean deleteFilterGroup(FilterGroup group) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.delete(cc);      
      return Boolean.TRUE;
    } catch (Exception e) {
      return Boolean.FALSE;
    }
  }
}
