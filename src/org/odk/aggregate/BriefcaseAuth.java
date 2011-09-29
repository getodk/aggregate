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

package org.odk.aggregate;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * Simple management class for handling Briefcase Application interactions with
 * Aggregate 0.9.x. The approach is to have an authenticated-access page within
 * Aggregate present an 8-letter code that is pasted into the Briefcase
 * application's credentials page. The 8-letter code can be enabled and reset
 * from Aggregate 0.9.x at a frequency of the user's choosing.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public final class BriefcaseAuth {

  public final static class BriefcaseAuthToken {
    Entity e;
    String token;
    boolean isEnabled;

    public BriefcaseAuthToken(Entity e, String token, boolean enabled) {
      this.e = e;
      this.token = token;
      this.isEnabled = enabled;
    }

    public final String getToken() {
      return token;
    }

    public final boolean isEnabled() {
      return isEnabled;
    }
  }

  public static void enableBriefcaseAuthToken(boolean enabled) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    BriefcaseAuthToken t = getBriefcaseAuthToken(ds);
    t.e.setProperty("ENABLED", enabled);
    ds.put(t.e);
  }

  public static BriefcaseAuthToken regenerateBriefcaseAuthToken() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    BriefcaseAuthToken t = getBriefcaseAuthToken(ds);

    ds.delete(t.e.getKey());
    try {
      // wait for settle...
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }
    t = getBriefcaseAuthToken(ds);
    t.isEnabled = true;
    t.e.setProperty("ENABLED", true);
    ds.put(t.e);

    return t;
  }

  public static boolean verifyBriefcaseAuthToken(String token) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    BriefcaseAuthToken t = getBriefcaseAuthToken(ds);

    return t.isEnabled && t.token.equals(token);
  }

  public static BriefcaseAuthToken getBriefcaseAuthToken() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    BriefcaseAuthToken t = getBriefcaseAuthToken(ds);

    return t;
  }

  private static BriefcaseAuthToken getBriefcaseAuthToken(DatastoreService ds) {

    Entity keeperEntity = null;
    String token = null;
    boolean enabled = false;
    Query query = new Query("_BRIEFCASE_APP_AUTH_");
    PreparedQuery pq = ds.prepare(query);
    List<Entity> eList = pq.asList(FetchOptions.Builder.withDefaults());
    if (eList.isEmpty()) {
      Entity e = new Entity("_BRIEFCASE_APP_AUTH_", "T"
          + String.format("%1$08x", System.currentTimeMillis()));
      SecureRandom r = new SecureRandom();
      int i = r.nextInt();
      token = String.format("%1$08x", i);
      enabled = false;
      e.setProperty("TOKEN", token); // 10 seconds...
      e.setProperty("ENABLED", enabled);
      e.setProperty("LAST_UPDATE_DATE", new Date());
      ds.put(e);
      keeperEntity = e;
    } else {
      Date lastDate = null;
      for (Entity e : eList) {
        Object o = e.getProperty("LAST_UPDATE_DATE");
        if (o != null && o instanceof Date) {
          Date d = (Date) o;
          if (lastDate == null || d.compareTo(lastDate) > 0) {
            lastDate = d;
            token = (String) e.getProperty("TOKEN");
            enabled = (Boolean) e.getProperty("ENABLED");
            keeperEntity = e;
          }
        }
      }
      for (Entity e : eList) {
        if (e != keeperEntity) {
          ds.delete(e.getKey());
        }
      }
    }

    BriefcaseAuthToken t = new BriefcaseAuthToken(keeperEntity, token, enabled);
    return t;
  }

}
