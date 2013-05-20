package org.opendatakit.aggregate.client.odktables;

import org.opendatakit.aggregate.client.exception.EtagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This will be the PropertiesService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.PropertiesService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */

@RemoteServiceRelativePath("serverpropertiesservice")
public interface ServerPropertiesService extends RemoteService {

	
	TablePropertiesClient getProperties(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;

	TablePropertiesClient setProperties(TablePropertiesClient properties, String tableId) throws 
		AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, EtagMismatchExceptionClient,
		PermissionDeniedExceptionClient;
	

}
