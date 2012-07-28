package org.opendatakit.aggregate.client.odktables;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

/**
 * This will be the PropertiesService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.PropertiesService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */
public interface ServerPropertiesService {

	
	TablePropertiesClient getProperties(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;

	TablePropertiesClient setProperties(TablePropertiesClient properties, String tableId) throws 
		AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, ODKTaskLockException, EtagMismatchException,
		PermissionDeniedException;
	

}
