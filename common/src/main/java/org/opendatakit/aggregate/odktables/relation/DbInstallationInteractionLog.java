/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Tracks the client installations and authenticated users that are 
 * modifying the server configuration. Also records the client's data
 * sync status for each table and information about the client (e.g., device info).
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbInstallationInteractionLog extends Relation {

  private DbInstallationInteractionLog(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final String RELATION_NAME = "INTERACTION_LOG";

  /**
   * The installation id from the header.
   */
  private static final DataField INSTALLATION_ID = new DataField("INSTALLATION_ID",
	      DataType.STRING, false);
  
  /**
   * The timestamp is the time of the authentication request.
   */
  private static final DataField TIMESTAMP = new DataField("TIMESTAMP",
      DataType.DATETIME, false);

  /**
   * The user participating in the interaction
   */
  private static final DataField AUTHORIZED_USER = new DataField("AUTHORIZED_USER",
		  DataType.STRING, false);

  public enum InteractionType {
    VerifyUser,
    ChangeConfiguration,
    DeviceInfo,
    SyncStatus
  };
  
  /**
   * The type of interaction -- enum name of InteractionType
   */
  private static final DataField INTERACTION_TYPE = new DataField("INTERACTION_TYPE",
        DataType.STRING, false);
  
  /**
   * The tableId being acted upon -- may be null
   */
  private static final DataField TABLE_ID = new DataField("TABLE_ID",
        DataType.STRING, true);
  
  /**
   * Additional details of the interaction
   */
  private static final DataField DETAIL;

  private static final List<DataField> dataFields;
  static {
    DataField detailField = new DataField("DETAIL", DataType.STRING, true);
    detailField.setMaxCharLen(4000L);
    DETAIL = detailField;

    dataFields = new ArrayList<DataField>();
    dataFields.add(INSTALLATION_ID);
    dataFields.add(TIMESTAMP);
    dataFields.add(AUTHORIZED_USER);
    dataFields.add(INTERACTION_TYPE);
    dataFields.add(TABLE_ID);
    dataFields.add(DETAIL);
  }

  public static class DbInstallationInteractionLogEntity {
    Entity e;

    public DbInstallationInteractionLogEntity(Entity e) {
      this.e = e;
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Primary Key -- the tableId
    public String getId() {
      return e.getId();
    }

    // Accessors

    public String getInstallationId() {
      return e.getString(INSTALLATION_ID);
    }

    public void setInstallationId(String value) {
      e.set(INSTALLATION_ID, value);
    }

    public Date getTimestamp() {
      return e.getDate(TIMESTAMP);
    }

    public void setTimestamp(Date value) {
      e.set(TIMESTAMP, value);
    }

    public String getAuthorizedUser() {
      return e.getString(AUTHORIZED_USER);
    }

    public void setAuthorizedUser(String value) {
      e.set(AUTHORIZED_USER, value);
    }

    public String getInteractionType() {
      String value = e.getString(INTERACTION_TYPE);
      return value;
    }

    public void setInteractionType(InteractionType value) {
      e.set(INTERACTION_TYPE, value.name());
    }

    public String getTableId() {
      return e.getString(TABLE_ID);
    }

    public void setTableId(String value) {
      e.set(TABLE_ID, value);
    }

    public String getDetail() {
      return e.getString(DETAIL);
    }

    public void setDetail(String value) {
      e.set(DETAIL, value);
    }
  }

  private static DbInstallationInteractionLog relation = null;

  public static synchronized final DbInstallationInteractionLog getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbInstallationInteractionLog(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    }
    return relation;
  }

  /**
   * Create a new row in this relation. The row is not yet persisted.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static DbInstallationInteractionLogEntity createNewEntity(CallingContext cc)
      throws ODKDatastoreException {
    return new DbInstallationInteractionLogEntity(getRelation(cc).newEntity(cc));
  }

  public static void recordVerificationEntry(String installationId, CallingContext cc)
      throws ODKDatastoreException {
    
    DbInstallationInteractionLogEntity vlog = DbInstallationInteractionLog.createNewEntity(cc);
    vlog.setInstallationId(installationId);
    vlog.setTimestamp(new Date());
    vlog.setAuthorizedUser(cc.getCurrentUser().getUriUser());
    vlog.setInteractionType(InteractionType.VerifyUser);
    vlog.put(cc);
  }

  public static void recordChangeConfigurationEntry(String installationId, String tableId, CallingContext cc)
      throws ODKDatastoreException {
    
    DbInstallationInteractionLogEntity vlog = DbInstallationInteractionLog.createNewEntity(cc);
    vlog.setInstallationId(installationId);
    vlog.setTimestamp(new Date());
    vlog.setAuthorizedUser(cc.getCurrentUser().getUriUser());
    vlog.setInteractionType(InteractionType.ChangeConfiguration);
    vlog.setTableId(tableId);
    vlog.put(cc);
  }

  public static void recordDeviceInfoEntry(String installationId, String deviceInfo, CallingContext cc)
      throws ODKDatastoreException {
    
    DbInstallationInteractionLogEntity vlog = DbInstallationInteractionLog.createNewEntity(cc);
    vlog.setInstallationId(installationId);
    vlog.setTimestamp(new Date());
    vlog.setAuthorizedUser(cc.getCurrentUser().getUriUser());
    vlog.setInteractionType(InteractionType.DeviceInfo);
    vlog.setDetail(deviceInfo);
    vlog.put(cc);
  }

  public static void recordSyncStatusEntry(String installationId, String tableId, String syncDetails, CallingContext cc)
      throws ODKDatastoreException {
    
    DbInstallationInteractionLogEntity vlog = DbInstallationInteractionLog.createNewEntity(cc);
    vlog.setInstallationId(installationId);
    vlog.setTimestamp(new Date());
    vlog.setAuthorizedUser(cc.getCurrentUser().getUriUser());
    vlog.setInteractionType(InteractionType.SyncStatus);
    vlog.setTableId(tableId);
    vlog.setDetail(syncDetails);
    vlog.put(cc);
  }

  public static List<DbInstallationInteractionLogEntity> queryForInstallationId(String installationId, CallingContext cc) 
      throws ODKDatastoreException {
    
    Query query = getRelation(cc).query("DbRoleVerificationLog.query", cc);
    
    query.addFilter(INSTALLATION_ID, FilterOperation.EQUAL, installationId);

    List<Entity> list = query.execute();
    List<DbInstallationInteractionLogEntity> results = new ArrayList<DbInstallationInteractionLogEntity>();
    for (Entity e : list) {
      results.add(new DbInstallationInteractionLogEntity(e));
    }
    return results;
  }
}
