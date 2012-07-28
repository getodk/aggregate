package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

/**
 * This will be the DiffService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.DiffService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */
public interface ServerDiffService {

	
	List<RowResourceClient> getRowsSince(String dataEtag, String tableId, UriInfo info) throws AccessDeniedException, RequestFailureException, 
	DatastoreFailureException, PermissionDeniedException;


}
