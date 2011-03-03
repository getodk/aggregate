package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FilterServiceAsync {

	void updateFilterGroup(FilterGroup group,
			AsyncCallback<Boolean> callback);

	void deleteFilterGroup(FilterGroup group,
			AsyncCallback<Boolean> callback);

	void maskAddFilter(Filter filter, 
			AsyncCallback<FilterGroup> callback);

	void maskRemoveFilter(Filter filter, 
			AsyncCallback<FilterGroup> callback);

	void maskAddFilterGroup(FilterGroup group, 
			AsyncCallback<FilterSet> callback);

	void maskRemoveFilterGroup(FilterGroup group,
			AsyncCallback<FilterSet> callback);

  void getFilterSet(String formId, AsyncCallback<FilterSet> callback);

}
