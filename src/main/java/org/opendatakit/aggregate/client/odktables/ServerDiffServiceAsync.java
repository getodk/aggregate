package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerDiffServiceAsync {

	void getRowsSince(String dataEtag, String tableId, AsyncCallback<List<RowClient>> callback);


}
