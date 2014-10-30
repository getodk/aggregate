/*
 * Copyright (C) 2012-2013 University of Washington
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
import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Query.WebsafeQueryResult;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Unlike the other tables, this is not updated atomically through the use of
 * the propertiesETag.
 *
 * Instead, all changes are immediate and apply to all propertiesETag and
 * dataETag versions of the table and its data.
 *
 * If we eventually do bulk updates from the UI, e.g., setAcls( List&lt;Acl&gt;
 * ) then we will need to craft the updates to first add privileges then to
 * remove the old privileges so that we never make an object inaccessible.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbTableAcl extends Relation {

  private DbTableAcl(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final DataField TABLE_ID = new DataField("TABLE_ID", DataType.STRING, false)
      .setIndexable(IndexType.ORDERED);
  private static final DataField SCOPE_TYPE = new DataField("SCOPE_TYPE", DataType.STRING, false);
  private static final DataField SCOPE_VALUE = new DataField("SCOPE_VALUE", DataType.STRING, true);
  private static final DataField ROLE = new DataField("ROLE", DataType.STRING, false);

  private static final String RELATION_NAME = "TABLE_ACL";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(TABLE_ID);
    dataFields.add(SCOPE_TYPE);
    dataFields.add(SCOPE_VALUE);
    dataFields.add(ROLE);
  }

  public static class DbTableAclEntity {
    Entity e;

    public DbTableAclEntity(Entity e) {
      this.e = e;
    }

    // Primary Key
    public String getId() {
      return e.getId();
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Accessors

    public String getTableId() {
      return e.getString(TABLE_ID);
    }

    public void setTableId(String value) {
      e.set(TABLE_ID, value);
    }

    public String getScopeType() {
      return e.getString(SCOPE_TYPE);
    }

    public void setScopeType(String value) {
      e.set(SCOPE_TYPE, value);
    }

    public String getScopeValue() {
      return e.getString(SCOPE_VALUE);
    }

    public void setScopeValue(String value) {
      e.set(SCOPE_VALUE, value);
    }

    public String getRole() {
      return e.getString(ROLE);
    }

    public void setRole(String value) {
      e.set(ROLE, value);
    }
  }

  private static DbTableAcl theRelation = null;

  private static synchronized DbTableAcl getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (theRelation == null) {
      DbTableAcl relation = new DbTableAcl(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
      theRelation = relation;
    }
    return theRelation;
  }

  /**
   * Create a new row in this relation. The row is not yet persisted.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static DbTableAclEntity createNewEntity(CallingContext cc) throws ODKDatastoreException {
    return new DbTableAclEntity(getRelation(cc).newEntity(cc));
  }

  public static WebsafeQueryResult queryTableIdAcls(String tableId, QueryResumePoint startCursor, int fetchLimit, CallingContext cc)
      throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableAcl.query()", cc);
    query.equal(DbTableAcl.TABLE_ID, tableId);
    if ( startCursor == null || startCursor.isForwardCursor() ) {
      query.addSort(DbTableAcl.ROLE, Direction.ASCENDING);
      query.addSort(DbTableAcl.SCOPE_TYPE, Direction.ASCENDING);
      query.addSort(DbTableAcl.SCOPE_VALUE, Direction.ASCENDING);
    } else {
      query.addSort(DbTableAcl.ROLE,  Direction.DESCENDING);
      query.addSort(DbTableAcl.SCOPE_TYPE, Direction.DESCENDING);
      query.addSort(DbTableAcl.SCOPE_VALUE, Direction.DESCENDING);
    }

    WebsafeQueryResult result = query.execute(startCursor, fetchLimit);
    return result;
  }

  public static WebsafeQueryResult queryTableIdScopeTypeAcls(String tableId, String scopeType,
      QueryResumePoint startCursor, int fetchLimit, CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableAcl.query()", cc);
    query.equal(DbTableAcl.TABLE_ID, tableId);
    query.equal(DbTableAcl.SCOPE_TYPE, scopeType);
    if ( startCursor == null || startCursor.isForwardCursor() ) {
      query.addSort(DbTableAcl.ROLE, Direction.ASCENDING);
      query.addSort(DbTableAcl.SCOPE_TYPE, Direction.ASCENDING);
      query.addSort(DbTableAcl.SCOPE_VALUE, Direction.ASCENDING);
    } else {
      query.addSort(DbTableAcl.ROLE,  Direction.DESCENDING);
      query.addSort(DbTableAcl.SCOPE_TYPE, Direction.DESCENDING);
      query.addSort(DbTableAcl.SCOPE_VALUE, Direction.DESCENDING);
    }

    WebsafeQueryResult result = query.execute(startCursor, fetchLimit);
    return result;
  }

  /**
   * Retrieves the acl entity for a given table and scope.
   *
   * @param tableId
   * @param scopeType
   * @param scopeValue
   * @param cc
   * @return the acl entity, or null if none exists
   * @throws ODKDatastoreException
   */
  public static DbTableAclEntity queryTableIdScopeTypeValueAcl(String tableId, String scopeType,
      String scopeValue, CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableAcl.getAcl()", cc);
    query.equal(DbTableAcl.TABLE_ID, tableId);
    query.equal(DbTableAcl.SCOPE_TYPE, scopeType);
    query.equal(DbTableAcl.SCOPE_VALUE, scopeValue);
    Entity acl = query.get();

    return (acl == null) ? null : new DbTableAclEntity(acl);
  }
}
