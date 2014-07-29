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
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * This defines the columns in a given schemaETag storage representation of that
 * tableId. The table defines the abstract objects (e.g., geopoint) and
 * underlying database fields (e.g., latitude, longitude, etc.) for the table.
 * <p>
 * This table stores the immutable definitions of each column in the datastore.
 * It is based on the ColumnDefinitions table in ODK Tables; there should be a
 * mirrored architecture. It is based on the ODK Tables Schema Google doc.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */
public class DbColumnDefinitions extends Relation {

  private DbColumnDefinitions(String namespace, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final String RELATION_NAME = "COLUMN_DEFINITIONS3";

  // these are the column names in the COLUMN table
  private static final DataField TABLE_ID = new DataField("TABLE_ID", DataType.STRING, false)
      .setIndexable(IndexType.HASH);
  private static final DataField SCHEMA_ETAG = new DataField("SCHEMA_ETAG", DataType.STRING, false);
  private static final DataField ELEMENT_KEY = new DataField("ELEMENT_KEY", DataType.STRING, false);
  private static final DataField ELEMENT_NAME = new DataField("ELEMENT_NAME", DataType.STRING,
      false);
  private static final DataField ELEMENT_TYPE = new DataField("ELEMENT_TYPE", DataType.STRING, true);
  private static final DataField LIST_CHILD_ELEMENT_KEYS = new DataField("LIST_CHILD_ELEMENT_KEYS",
      DataType.STRING, true, 4096L);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(TABLE_ID);
    dataFields.add(SCHEMA_ETAG);
    dataFields.add(ELEMENT_KEY);
    dataFields.add(ELEMENT_NAME);
    dataFields.add(ELEMENT_TYPE);
    dataFields.add(LIST_CHILD_ELEMENT_KEYS);
  }

  public static class DbColumnDefinitionsEntity {

    Entity e;

    DbColumnDefinitionsEntity(Entity e) {
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

    public String getschemaETag() {
      return e.getString(SCHEMA_ETAG);
    }

    public void setschemaETag(String value) {
      e.set(SCHEMA_ETAG, value);
    }

    public String getElementKey() {
      return e.getString(ELEMENT_KEY);
    }

    public void setElementKey(String value) {
      e.set(ELEMENT_KEY, value);
    }

    public String getElementName() {
      return e.getString(ELEMENT_NAME);
    }

    public void setElementName(String value) {
      e.set(ELEMENT_NAME, value);
    }

    public String getElementType() {
      return e.getString(ELEMENT_TYPE);
    }

    public void setElementType(String value) {
      e.set(ELEMENT_TYPE, value);
    }

    public String getListChildElementKeys() {
      return e.getString(LIST_CHILD_ELEMENT_KEYS);
    }

    public void setListChildElementKeys(String value) {
      e.set(LIST_CHILD_ELEMENT_KEYS, value);
    }

    public boolean isUnitOfRetention() {
      String listChild = getListChildElementKeys();
      String type = getElementType();
      if ( "array".equals(type) ) {
        return true;
      }
      if ( listChild == null || listChild.length() == 0 || "[]".equals(listChild) ) {
        return true;
      }
      return false;
    }

    /**
     * Tests whether the semantically meaningful fields are equivalent.
     *
     * @param e
     * @return true if these are matching column definitions
     */
    public boolean matchingColumnDefinition(DbColumnDefinitionsEntity e) {
      if (!this.getElementKey().equals(e.getElementKey())) {
        return false;
      }
      if (!this.getElementName().equals(e.getElementName())) {
        return false;
      }
      if (!this.getElementType().equals(e.getElementType())) {
        return false;
      }
      if (this.getListChildElementKeys() == null) {
        if ( e.getListChildElementKeys() != null ) {
          return false;
        }
      } else if ( e.getListChildElementKeys() == null) {
        return false;
      } else if ( !this.getListChildElementKeys().equals(e.getListChildElementKeys())) {
        return false;
      }
      return true;
    }

  }

  private static DbColumnDefinitions relation = null;

  public static synchronized final DbColumnDefinitions getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbColumnDefinitions(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
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
  public static DbColumnDefinitionsEntity createNewEntity(CallingContext cc)
      throws ODKDatastoreException {
    return new DbColumnDefinitionsEntity(getRelation(cc).newEntity(cc));
  }

  /**
   * Gets all of the columns in the column definitions table. This will not
   * include metadata columns present in the data tables, like
   * _savepoint_timestamp or rowid.
   *
   * @param tableId
   * @param schemaETag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<DbColumnDefinitionsEntity> query(String tableId, String schemaETag,
      CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbColumnDefinitions.query", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(SCHEMA_ETAG, FilterOperation.EQUAL, schemaETag);

    List<Entity> list = query.execute();
    List<DbColumnDefinitionsEntity> results = new ArrayList<DbColumnDefinitionsEntity>();
    for (Entity e : list) {
      results.add(new DbColumnDefinitionsEntity(e));
    }
    return results;
  }

  /**
   * Return the actual database column names for the given table.
   * Sort them so that complex type elements are together
   * (they will be because of their common prefix).
   *
   * @param tableId
   * @param schemaETag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static ArrayList<String> queryForDbColumnNames(String tableId, String schemaETag,
      CallingContext cc) throws ODKDatastoreException {

    List<DbColumnDefinitionsEntity> columns = query(tableId, schemaETag, cc);

    ArrayList<String> list = new ArrayList<String>();
    for (DbColumnDefinitionsEntity e : columns) {
      if ( e.isUnitOfRetention() ) {
        list.add(e.getElementKey());
      }
    }
    Collections.sort(list);
    return list;
  }

}