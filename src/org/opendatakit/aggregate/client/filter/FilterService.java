package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("filterservice")
public interface FilterService extends RemoteService{

	FilterGroup addFilter(Filter filter);
	FilterGroup removeFilter(Filter filter);
	FilterSet createFilterGroup(FilterGroup group);
	FilterSet removeFilterGroup(FilterGroup group);
	FilterGroup maskAddFilter(Filter filter);
	FilterGroup maskRemoveFilter(Filter filter);
	FilterSet maskAddFilterGroup(FilterGroup group);
	FilterSet maskRemoveFilterGroup(FilterGroup group);
}
