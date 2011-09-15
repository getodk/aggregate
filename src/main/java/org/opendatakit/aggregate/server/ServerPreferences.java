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

import java.util.List;

import org.opendatakit.aggregate.client.preferences.PreferenceSummary;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class ServerPreferences extends CommonFieldsBase {

  private static final String TABLE_NAME = "_server_preferences";

  private static final DataField GOOGLE_MAP_KEY = new DataField("GOOG_MAPS_API_KEY",
      DataField.DataType.STRING, true, 128L);

  private static final DataField ODK_TABLES_ENABLED = new DataField("ODK_TABLES_ENABLED",
      DataField.DataType.BOOLEAN, true);

  /**
   * Construct a relation prototype.
   * 
   * @param databaseSchema
   * @param tableName
   */
  private ServerPreferences(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(GOOGLE_MAP_KEY);
    fieldList.add(ODK_TABLES_ENABLED);
  }

  public PreferenceSummary getPreferenceSummary() {
    return new PreferenceSummary(getGoogleMapApiKey(), getOdkTablesEnabled());
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * @param ref
   * @param user
   */
  private ServerPreferences(ServerPreferences ref, User user) {
    super(ref, user);
  }

  @Override
  public ServerPreferences getEmptyRow(User user) {
    return new ServerPreferences(this, user);
  }

  public String getGoogleMapApiKey() {
    return getStringField(GOOGLE_MAP_KEY);
  }

  public void setGoogleMapApiKey(String googleMapsApiKey) {
    if (!setStringField(GOOGLE_MAP_KEY, googleMapsApiKey)) {
      throw new IllegalArgumentException("overflow filterGroup");
    }
  }

  public Boolean getOdkTablesEnabled() {
    Boolean value = getBooleanField(ODK_TABLES_ENABLED);

    // null value should be treated as false
    if (value == null) {
      return false;
    }
    return value;
  }

  public void setOdkTablesEnabled(Boolean enabled) {
    setBooleanField(ODK_TABLES_ENABLED, enabled);
  }

  public void persist(CallingContext cc) throws ODKEntityPersistException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(this, user);
  }

  private static ServerPreferences relation = null;

  public static synchronized final ServerPreferences assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      ServerPreferences relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new ServerPreferences(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  public static final ServerPreferences getServerPreferences(CallingContext cc)
      throws ODKEntityNotFoundException {
    try {
      ServerPreferences relation = assertRelation(cc);
      Query query = cc.getDatastore().createQuery(relation, cc.getCurrentUser());

      List<? extends CommonFieldsBase> results = query.executeQuery();
      if (!results.isEmpty()) {
        if (results.get(0) instanceof ServerPreferences) {
          ServerPreferences preferences = (ServerPreferences) results.get(0);
          return preferences;
        }
      }

      return  cc.getDatastore().createEntityUsingRelation(relation,
          cc.getCurrentUser());

    } catch (ODKDatastoreException e) {
      throw new ODKEntityNotFoundException(e);
    }
  }
}
