package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This will be the DiffService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.DiffService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */

@RemoteServiceRelativePath("serverdiffservice")
public interface ServerDiffService extends RemoteService {

	
	List<RowClient> getRowsSince(String dataEtag, String tableId) throws AccessDeniedException, 
	RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;


}
