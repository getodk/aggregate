package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("filterservice")
public interface FilterService extends RemoteService {

  FilterSet getFilterSet(String formId);
  
  Boolean updateFilterGroup(FilterGroup group);
  Boolean deleteFilterGroup(FilterGroup group);
}
