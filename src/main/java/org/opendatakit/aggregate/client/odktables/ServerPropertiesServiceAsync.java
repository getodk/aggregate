package org.opendatakit.aggregate.client.odktables;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerPropertiesServiceAsync {

	void getProperties(String tableId, UriInfo info, AsyncCallback<PropertiesResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;

	// maybe should also throw etag mismatch and ODKTaskLockException?
	void setProperties(TablePropertiesClient properties, 
			String tableId, UriInfo info, AsyncCallback<PropertiesResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			ODKTaskLockException, EtagMismatchException, PermissionDeniedException;
	
	
}
