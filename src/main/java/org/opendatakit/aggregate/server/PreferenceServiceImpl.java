/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.server;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.preferences.PreferenceSummary;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class PreferenceServiceImpl extends RemoteServiceServlet implements
org.opendatakit.aggregate.client.preferences.PreferenceService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -4892832848446000170L;

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

  @Override
  public PreferenceSummary getPreferences() throws AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    try {
      ServerPreferences pref = ServerPreferences.getServerPreferences(cc);
      if(pref != null) {
        return pref.getPreferenceSummary();
      }
    } catch (ODKEntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return null;
  }

  @Override
  public void setOdkTablesEnabled(Boolean enabled) throws AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    try {
      ServerPreferences pref = ServerPreferences.getServerPreferences(cc);
      if(pref == null) {
        pref = cc.getDatastore().createEntityUsingRelation(
            ServerPreferences.assertRelation(cc), cc.getCurrentUser());
      }
      pref.setOdkTablesEnabled(enabled);
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
