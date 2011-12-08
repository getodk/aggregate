package org.opendatakit.aggregate.client.preferences;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("odktablesadmin")
public interface OdkTablesAdminService extends RemoteService {
  OdkTablesAdmin [] listAdmin() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
  
  Boolean deleteAdmin(String aggregateUid) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
  
  Boolean addAdmin(OdkTablesAdmin admin) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;

  Boolean updateAdmin(OdkTablesAdmin admin) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
}

