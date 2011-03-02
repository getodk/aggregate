package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FilterServiceAsync {

	void addFilter(Filter filter, 
			AsyncCallback<FilterGroup> callback);
	
	void removeFilter(Filter filter, 
			AsyncCallback<FilterGroup> callback);

	void createFilterGroup(FilterGroup group,
			AsyncCallback<FilterSet> callback);

	void removeFilterGroup(FilterGroup group,
			AsyncCallback<FilterSet> callback);

}
