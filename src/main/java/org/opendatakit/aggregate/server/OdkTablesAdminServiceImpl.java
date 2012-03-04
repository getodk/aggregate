package org.opendatakit.aggregate.server;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdminService;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class OdkTablesAdminServiceImpl extends RemoteServiceServlet implements
    OdkTablesAdminService {

  private static final long serialVersionUID = -2602832816355702415L;

  @Override
  public OdkTablesAdmin[] listAdmin() throws AccessDeniedException {
    return new OdkTablesAdmin[0];
  }

  @Override
  public Boolean deleteAdmin(String aggregateUid) throws AccessDeniedException {
    return true;
  }

  @Override
  public Boolean addAdmin(OdkTablesAdmin admin) throws AccessDeniedException {
    return true;
  }

  @Override
  public Boolean updateAdmin(OdkTablesAdmin admin) throws AccessDeniedException {
    return true;
  }

  private CallingContext getCC() {
    HttpServletRequest req = super.getThreadLocalRequest();
    return ContextFactory.getCallingContext(this, req);
  }

}
