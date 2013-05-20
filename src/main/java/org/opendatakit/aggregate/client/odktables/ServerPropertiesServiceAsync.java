package org.opendatakit.aggregate.client.odktables;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerPropertiesServiceAsync {

	void getProperties(String tableId, AsyncCallback<TablePropertiesClient> callback);

	void setProperties(TablePropertiesClient properties,
			String tableId, AsyncCallback<TablePropertiesClient> callback);


}
