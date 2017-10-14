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
import java.util.Locale;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Tracks the per-row attachment manifest ETags of a row.
 *
 * @author dylan price
 * @author sudar.sam@gmail.com
 *
 */
public class DbTableInstanceManifestETags extends Relation {

  private DbTableInstanceManifestETags(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  /**
   * NOTE: the PK of this table is the PK of the DbTable relation.
   * i.e., the ROW_ID of a DbLogTable entry. Instance manifests are 
   * always comprehensive.
   */

  public static final DataField MANIFEST_ETAG = new DataField("_MANIFEST_ETAG", DataType.STRING, false);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();

    dataFields.add(MANIFEST_ETAG);
  }

  public static class DbTableInstanceManifestETagEntity {
    Entity e;

    public DbTableInstanceManifestETagEntity(Entity e) {
      this.e = e;
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Primary Key -- the rowId
    public String getId() {
      return e.getId();
    }

    // Accessors

    public String getManifestETag() {
      return e.getString(MANIFEST_ETAG);
    }

    public void setManifestETag(String value) {
      e.set(MANIFEST_ETAG, value);
    }
  }

  public static synchronized DbTableInstanceManifestETags getRelation(String tableId, CallingContext cc) throws ODKDatastoreException {
    DbTableInstanceManifestETags relation = new DbTableInstanceManifestETags(RUtil.NAMESPACE, 
        tableId.toUpperCase(Locale.ENGLISH) + "_MFE", dataFields, cc);
    return relation;
  }

  /**
   * Create a new row in this relation. The row is not yet persisted.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static DbTableInstanceManifestETagEntity createNewEntity(String tableId, String rowId, CallingContext cc)
      throws ODKDatastoreException {
    return new DbTableInstanceManifestETagEntity(getRelation(tableId, cc).newEntity(rowId, cc));
  }

  public static DbTableInstanceManifestETagEntity getRowIdEntry(String tableId, String rowId, CallingContext cc)
      throws ODKOverQuotaException, ODKEntityNotFoundException, ODKDatastoreException {

    return new DbTableInstanceManifestETagEntity(getRelation(tableId, cc).getEntity(rowId, cc));
  }

}