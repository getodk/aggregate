package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("filterservice")
public interface FilterService extends RemoteService{

	FilterGroup addFilter(Filter filter);
	FilterGroup removeFilter(Filter filter);
	FilterTable createFilterGroup(FilterGroup group);
	FilterTable removeFilterGroup(FilterGroup group);
}
