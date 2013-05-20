package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerTableServiceAsync {

	void getTables(AsyncCallback<List<TableEntryClient>> callback);

	void getTable(String tableId, AsyncCallback<TableEntryClient> callback);

	void createTable(String tableId, TableDefinitionClient definition,
			AsyncCallback<TableEntryClient> callback);

	void deleteTable(String tableId, AsyncCallback<Void> callback);

}
