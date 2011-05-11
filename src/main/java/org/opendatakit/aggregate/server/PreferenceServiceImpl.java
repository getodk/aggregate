package org.opendatakit.aggregate.server;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class PreferenceServiceImpl extends RemoteServiceServlet implements
org.opendatakit.aggregate.client.preferences.PreferenceService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -4892832848446000170L;

  @Override
  public String getGoogleMapsKey() {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    try {
      ServerPreferences pref = ServerPreferences.getServerPreferences(cc);
      if(pref != null) {
        return pref.getGoogleMapApiKey();
      }
    } catch (ODKEntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return null;
  }

  @Override
  public void setGoogleMapsKey(String key) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    try {
      ServerPreferences pref = ServerPreferences.getServerPreferences(cc);
      if(pref == null) {
        pref = cc.getDatastore().createEntityUsingRelation(
            ServerPreferences.assertRelation(cc), cc.getCurrentUser());
      }
      pref.setGoogleMapApiKey(key);
      pref.persist(cc);
    } catch (ODKEntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

}
