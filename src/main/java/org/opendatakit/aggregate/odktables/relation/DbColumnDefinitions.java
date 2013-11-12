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
 * Comments by sudar.sam@gmail.com, so there may be discrepancies between the
 * comments and what was actually intended.
 * <p>
 * This table stores the immutable definitions of each column in the datastore.
 * It is based on the eponymous table in ODK Tables, so that there should be a
 * mirrored architecture. It is based on the ODK Tables Schema Google doc.
 * <p>
 * This is the table (i.e. "relation") that appears in the datastore as the
 * "_ODKTABLES_COLUMN" table. This is the table that stores the information
 * about the "columns" that are in the different odktables tables. It has the
 * type and name information. Therefore this is the datastore table that you
 * query to get all the column names for a certain table. Each entity (row in
 * this "relation") has the table id that the column belongs to, along with the
 * column type and name.
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

  private static final String RELATION_NAME = "COLUMN_DEFINITIONS2";

  // these are the column names in the COLUMN table
  private static final DataField TABLE_ID = new DataField("TABLE_ID", DataType.STRING, false)
      .setIndexable(IndexType.HASH);
  private static final DataField PROPERTIES_ETAG = new DataField("PROPERTIES_ETAG",
      DataType.STRING, false);
  private static final DataField ELEMENT_KEY = new DataField("ELEMENT_KEY", DataType.STRING, false);
  private static final DataField ELEMENT_NAME = new DataField("ELEMENT_NAME", DataType.STRING,
      false);
  private static final DataField ELEMENT_TYPE = new DataField("ELEMENT_TYPE", DataType.STRING, true);
  private static final DataField LIST_CHILD_ELEMENT_KEYS = new DataField("LIST_CHILD_ELEMENT_KEYS",
      DataType.STRING, true, 4096L);
  private static final DataField IS_UNIT_OF_RETENTION = new DataField("IS_UNIT_OF_RETENTION", DataType.BOOLEAN,
      false);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(TABLE_ID);
    dataFields.add(PROPERTIES_ETAG);
    dataFields.add(ELEMENT_KEY);
    dataFields.add(ELEMENT_NAME);
    dataFields.add(ELEMENT_TYPE);
    dataFields.add(LIST_CHILD_ELEMENT_KEYS);
    dataFields.add(IS_UNIT_OF_RETENTION);
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

    public String getPropertiesETag() {
      return e.getString(PROPERTIES_ETAG);
    }

    public void setPropertiesETag(String value) {
      e.set(PROPERTIES_ETAG, value);
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

    public Boolean getIsUnitOfRetention() {
      return e.getBoolean(IS_UNIT_OF_RETENTION);
    }

    public void setIsUnitOfRetention(Boolean value) {
      e.set(IS_UNIT_OF_RETENTION, value);
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
   * include metadata columns present in the data tables, like last_mod_time or
   * rowid.
   *
   * @param tableId
   * @param propertiesEtag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<DbColumnDefinitionsEntity> query(String tableId, String propertiesEtag,
      CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbColumnDefinitions.query", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(PROPERTIES_ETAG, FilterOperation.EQUAL, propertiesEtag);

    List<Entity> list = query.execute();
    List<DbColumnDefinitionsEntity> results = new ArrayList<DbColumnDefinitionsEntity>();
    for (Entity e : list) {
      results.add(new DbColumnDefinitionsEntity(e));
    }
    return results;
  }

  /**
   * Return the ELEMENT_NAMEs for the given table. Currently returns all, even
   * the non-unit-of-retention ones.
   *
   * @param tableId
   * @param propertiesEtag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<String> queryForColumnNames(String tableId, String propertiesEtag,
      CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbColumnDefinitions.queryForColumnNames", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(PROPERTIES_ETAG, FilterOperation.EQUAL, propertiesEtag);

    List<?> results = query.getDistinct(ELEMENT_NAME);
    List<String> list = new ArrayList<String>();
    for (Object o : results) {
      list.add((String) o);
    }
    return list;
  }

}