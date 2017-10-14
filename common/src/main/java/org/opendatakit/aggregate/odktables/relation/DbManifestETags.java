/*
 * Copyright (C) 2014 University of Washington
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
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Tracks the ETags of the manifests associated with a given TableId. Appl-level manifests
 * have a TableId of APP_LEVEL ("APP LEVEL"); the space ensures it could never collide with
 * a valid tableId.
 * 
 * TableId is the PK of this table.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbManifestETags extends Relation {

  public static final String APP_LEVEL = "APP LEVEL";
  
  private DbManifestETags(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final String RELATION_NAME = "MANIFEST_ETAGS";

  /**
   * eTag of the manifest for this tableId
   */
  private static final DataField MANIFEST_ETAG = new DataField("MANIFEST_ETAG",
      DataType.STRING, true);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(MANIFEST_ETAG);
  }

  public static class DbManifestETagEntity {
    Entity e;

    public DbManifestETagEntity(Entity e) {
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

    public String getManifestETag() {
      return e.getString(MANIFEST_ETAG);
    }

    public void setManifestETag(String value) {
      e.set(MANIFEST_ETAG, value);
    }
  }

  private static DbManifestETags relation = null;

  public static synchronized final DbManifestETags getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbManifestETags(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
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
  public static DbManifestETagEntity createNewEntity(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    return new DbManifestETagEntity(getRelation(cc).newEntity(tableId, cc));
  }

  public static DbManifestETagEntity getTableIdEntry(String tableId, CallingContext cc)
      throws ODKOverQuotaException, ODKEntityNotFoundException, ODKDatastoreException {

    return new DbManifestETagEntity(getRelation(cc).getEntity(tableId, cc));
  }

}
