package org.opendatakit.aggregate.client.filter;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FilterServiceAsync {

	void updateFilterGroup(FilterGroup group,
			AsyncCallback<Boolean> callback);

	void deleteFilterGroup(FilterGroup group,
			AsyncCallback<Boolean> callback);

    void getFilterSet(String formId, AsyncCallback<FilterSet> callback);

}
