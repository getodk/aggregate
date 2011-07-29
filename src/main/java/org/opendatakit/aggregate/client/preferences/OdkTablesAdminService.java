package org.opendatakit.aggregate.client.preferences;

import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("odktablesadmin")
public interface OdkTablesAdminService extends RemoteService {
  OdkTablesAdmin [] listAdmin() throws AccessDeniedException;
  
  Boolean deleteAdmin(String aggregateUid) throws AccessDeniedException;
  
  Boolean addAdmin(OdkTablesAdmin admin) throws AccessDeniedException;

  Boolean updateAdmin(OdkTablesAdmin admin) throws AccessDeniedException;
}

