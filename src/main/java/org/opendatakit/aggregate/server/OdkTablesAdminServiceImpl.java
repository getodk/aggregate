package org.opendatakit.aggregate.server;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdminService;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class OdkTablesAdminServiceImpl extends RemoteServiceServlet implements OdkTablesAdminService {

  /**
   * 
   */
  private static final long serialVersionUID = -2602832816355702415L;

  @Override
  public OdkTablesAdmin[] listAdmin() throws AccessDeniedException {
    
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean deleteAdmin(String aggregateUid) throws AccessDeniedException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Boolean addAdmin(OdkTablesAdmin admin) throws AccessDeniedException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Boolean updateAdmin(OdkTablesAdmin admin) throws AccessDeniedException {
    // TODO Auto-generated method stub
    return false;
  }

}
