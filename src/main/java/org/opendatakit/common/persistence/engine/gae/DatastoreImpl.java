/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.gae;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Text;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class DatastoreImpl implements Datastore {

  /**
   * Maximum size limit 1MB (1024*1024-1) Now down to 1,000,000
   */
  private static final int BLOB_MAX_SIZE = 1000000 - 1;

  private static final int MAX_IDENTIFIER_LEN = 64;

  private static final Long GAE_MAX_STRING_LEN = 255L;
  // these aren't actually used for filtering...
  public static final Integer DEFAULT_DBL_NUMERIC_SCALE = 10;
  public static final Integer DEFAULT_DBL_NUMERIC_PRECISION = 38;
  public static final Integer DEFAULT_INT_NUMERIC_PRECISION = 10;

  private final String schemaName;

  private DatastoreService ds;

  private StringFieldLengthMapping stringFieldLengthMap = new StringFieldLengthMapping();
  
  private DatastoreAccessMetrics dam = new DatastoreAccessMetrics();

  public DatastoreImpl() throws Exception {
    ds = DatastoreServiceFactory.getDatastoreService();
    schemaName = "opendatakit";
    
    LogFactory.getLog(DatastoreImpl.class).info("Running on " + 
          ds.getDatastoreAttributes().getDatastoreType().toString() + " datastore");
  }

  DatastoreService getDatastoreService() {
    return ds;
  }
  
  public DatastoreAccessMetrics getDam() {
    return dam;
  }

  void recordQueryUsage(CommonFieldsBase relation, int recCount) {
    dam.recordQueryUsage(relation, recCount);
  }

  void recordQueryUsage(String specialTableName, int recCount) {
    dam.recordQueryUsage(specialTableName, recCount);
  }

  @Override
  public String getDefaultSchemaName() {
    return schemaName;
  }

  @Override
  public int getMaxLenColumnName() {
    return MAX_IDENTIFIER_LEN;
  }

  @Override
  public int getMaxLenTableName() {
    return MAX_IDENTIFIER_LEN;
  }

  @Override
  public void assertRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException {
    int nColumns = 0;
    long nBytes = 0L;
    
    // update the relation so that all STRING and URI field lengths are defined (non-null)
    stringFieldLengthMap.assertStringFieldLengths(constructGaeKind(relation), relation, dam, this, user);
    
    for (DataField d : relation.getFieldList()) {
      switch (d.getDataType()) {
      case LONG_STRING:
      case BINARY:
        d.setMaxCharLen(Long.valueOf(BLOB_MAX_SIZE));
        break;
      case STRING:
      case URI:
        nBytes += d.getMaxCharLen();
        ++nColumns;
        break;
      case DATETIME:
        nBytes += 24;
        ++nColumns;
        break;
      case INTEGER:
        d.setNumericPrecision(DEFAULT_INT_NUMERIC_PRECISION);
        nBytes += 8;
        ++nColumns;
        break;
      case DECIMAL:
        d.setNumericPrecision(DEFAULT_DBL_NUMERIC_PRECISION);
        d.setNumericScale(DEFAULT_DBL_NUMERIC_SCALE);
        nBytes += 8;
        ++nColumns;
        break;
      }
    }
    // limits for GAE are 5000 columns and 1Mb for a batch put request.
    // Don't know how much overhead there is in the construction of the
    // request, but figure 30% overhead (which is absurd).
    //
    // Insist the upper layers partition the data tables when they have
    // more than potentially 4000 columns or 700,000 bytes of data per row.
    if ((nColumns > 4000) || (nBytes > 700000)) {
      throw new ODKDatastoreException("table is overly large");
    }
  }

  /**
   * Determine whether this relation already exists.
   * 
   * @return false because we don't care about naming collisions.
   * @throws ODKDatastoreException 
   */
  @Override
  public boolean hasRelation(String schema, String tableName, User user) throws ODKDatastoreException {
    List<com.google.appengine.api.datastore.Entity> gaeKeys = null;
    try {
      com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query(
          schema + "." + tableName);
      query.setKeysOnly();
      PreparedQuery preparedQuery = ds.prepare(query);
      gaeKeys = preparedQuery.asList(FetchOptions.Builder.withLimit(2));
    } catch (OverQuotaException e) {
      throw new ODKOverQuotaException(e);
    } catch (Exception ex) {
      // No-op
    }
    if (gaeKeys == null || gaeKeys.size() == 0)
      return false;
    return true;
  }

  @Override
  public void dropRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException {
    // remove string lengths...
    stringFieldLengthMap.removeStringFieldLengths(constructGaeKind(relation), dam, this, user);
    // TODO: delete all entities in the relation.
    // as long as it is a form, the form delete will delete all
    // submissions before calling this, so we are OK for the common
    // case.
  }

  private String constructGaeKind(CommonFieldsBase entity) {
    return entity.getSchemaName() + "." + entity.getTableName();
  }

  private Key constructGaeKey(CommonFieldsBase entity, String uri) {
    return KeyFactory.createKey(constructGaeKind(entity), uri);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends CommonFieldsBase> T createEntityUsingRelation(T relation, User user) {
    CommonFieldsBase row;
    try {
      row = (T) relation.getEmptyRow(user);
    } catch (Exception e) {
      throw new IllegalStateException("failed to create empty row", e);
    }
    return (T) row;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends CommonFieldsBase> T getEntity(T relation, String uri, User user)
      throws ODKDatastoreException {
    Key selfKey = constructGaeKey(relation, uri);
    com.google.appengine.api.datastore.Entity gaeEntity = null;
    dam.recordGetUsage(relation);
    try {
      gaeEntity = ds.get(selfKey);
    } catch (EntityNotFoundException e) {
      throw new ODKEntityNotFoundException(e);
    } catch (OverQuotaException e) {
      throw new ODKOverQuotaException(e);
    } catch (Exception e) {
      throw new ODKDatastoreException(e);
    }

    CommonFieldsBase row;
    try {
      row = relation.getEmptyRow(user);
    } catch (Exception e) {
      throw new IllegalStateException("failed to create empty row", e);
    }
    updateRowFromGae(row, gaeEntity);
    return (T) row;
  }

  @Override
  public Query createQuery(CommonFieldsBase table, String loggingContextTag, User user) {
    Query query = new QueryImpl(table, loggingContextTag, this, user);
    return query;
  }

  public void updateRowFromGae(CommonFieldsBase row,
      com.google.appengine.api.datastore.Entity gaeEntity) {
    row.setOpaquePersistenceData(gaeEntity);
    row.setFromDatabase(true);
    for (DataField d : row.getFieldList()) {
      Object o = gaeEntity.getProperty(d.getName());
      if (o != null) {
        switch (d.getDataType()) {
        case BINARY:
          Blob bin = (Blob) o;
          byte[] array = bin.getBytes();
          if (array != null && array.length != 0) {
            row.setBlobField(d, array);
          }
          break;
        case LONG_STRING:
          Text txt = (Text) o;
          row.setStringField(d, txt.getValue());
          break;
        case BOOLEAN:
          Boolean bool = (Boolean) o;
          row.setBooleanField(d, bool);
          break;
        case DATETIME:
          Date date = (Date) o;
          row.setDateField(d, date);
          break;
        case DECIMAL:
          BigDecimal bd = new BigDecimal((Double) o);
          row.setNumericField(d, bd);
          break;
        case INTEGER:
          Long l = (Long) o;
          row.setLongField(d, l);
          break;
        case STRING:
        case URI:
          String s = (String) o;
          o = gaeEntity.getProperty("__" + d.getName());
          if (o != null) {
            Text t = (Text) o;
            row.setStringField(d, t.getValue());
          } else {
            row.setStringField(d, s);
          }
          break;
        default:
          throw new IllegalStateException("Unrecognized datatype");
        }
      }
    }
  }

  private com.google.appengine.api.datastore.Entity prepareGaeFromRow(CommonFieldsBase entity,
      User user) {

    if (entity.isFromDatabase()) {
      entity.setDateField(entity.lastUpdateDate, new Date());
      entity.setStringField(entity.lastUpdateUriUser, user.getUriUser());
    } else {
      // we need to create the backing object...
      //
      com.google.appengine.api.datastore.Entity gaeEntity;
      // because we sometimes access the nested records without first
      // accessing the top level record, it seems we can't leverage
      // the Google BigTable parent-key feature for colocation unless
      // we want to take a hit on the getEntity call and turn that
      // into a query.
      gaeEntity = new com.google.appengine.api.datastore.Entity(constructGaeKind(entity),
          entity.getUri());

      entity.setOpaquePersistenceData(gaeEntity);
    }

    // get the google backing object...
    com.google.appengine.api.datastore.Entity e;
    if (entity.getOpaquePersistenceData() == null) {
      throw new IllegalStateException("Entity should have opaque persistence data!");
    } else {
      e = (com.google.appengine.api.datastore.Entity) entity.getOpaquePersistenceData();
      for (DataField d : entity.getFieldList()) {
        if (entity.isNull(d)) {
          e.removeProperty(d.getName());
        } else
          switch (d.getDataType()) {
          case BINARY:
            byte[] array = entity.getBlobField(d);
            if (array == null || array.length == 0) {
              e.removeProperty(d.getName());
            } else {
              Blob bin = new Blob(array);
              e.setProperty(d.getName(), bin);
            }
            break;
          case LONG_STRING:
            Text txt = new Text(entity.getStringField(d));
            e.setProperty(d.getName(), txt);
            break;
          case BOOLEAN:
            e.setProperty(d.getName(), entity.getBooleanField(d));
            break;
          case DATETIME:
            e.setProperty(d.getName(), entity.getDateField(d));
            break;
          case DECIMAL:
            BigDecimal bd = entity.getNumericField(d);
            e.setProperty(d.getName(), bd.doubleValue());
            break;
          case INTEGER:
            e.setProperty(d.getName(), entity.getLongField(d));
            break;
          case STRING:
          case URI:
            String s = entity.getStringField(d);
            if (s.length() > GAE_MAX_STRING_LEN.intValue()) {
              Text t = new Text(s);
              e.setProperty("__" + d.getName(), t);
              e.setProperty(d.getName(), s.substring(0, GAE_MAX_STRING_LEN.intValue()));
            } else {
              e.removeProperty("__" + d.getName());
              e.setProperty(d.getName(), s);
            }
            break;
          default:
            throw new IllegalStateException("Unrecognized datatype");
          }
      }
    }
    return e;
  }

  @Override
  public void putEntity(CommonFieldsBase entity, User user) throws ODKEntityPersistException, ODKOverQuotaException {
    com.google.appengine.api.datastore.Entity e = prepareGaeFromRow(entity, user);
    dam.recordPutUsage(entity);
    try {
      ds.put(e);
    } catch (OverQuotaException ex) {
      throw new ODKOverQuotaException(ex);
    } catch (Exception ex) {
      throw new ODKEntityPersistException(ex);
    }
  }

  @Override
  public void putEntities(Collection<? extends CommonFieldsBase> entities, User user)
      throws ODKEntityPersistException, ODKOverQuotaException {
    List<com.google.appengine.api.datastore.Entity> gaeEntities = new ArrayList<com.google.appengine.api.datastore.Entity>();
    for (CommonFieldsBase entity : entities) {
      dam.recordPutUsage(entity);
      gaeEntities.add(prepareGaeFromRow(entity, user));
    }
    try {
      if (gaeEntities.size() != 0) {
        ds.put(gaeEntities);
      }
    } catch (OverQuotaException ex) {
      throw new ODKOverQuotaException(ex);
    } catch (Exception ex) {
      throw new ODKEntityPersistException(ex);
    }
  }

  @Override
  public void deleteEntity(EntityKey key, User user) throws ODKDatastoreException {
    Key dsKey = constructGaeKey(key.getRelation(), key.getKey());
    dam.recordDeleteUsage(key);
    try {
      LogFactory.getLog(DatastoreImpl.class).info(
          "Executing delete " + constructGaeKind(key.getRelation()) + " with key " + key.getKey()
              + " by user " + user.getUriUser());
      ds.delete(dsKey);
    } catch (OverQuotaException ex) {
      throw new ODKOverQuotaException(ex);
    } catch (Exception ex) {
      throw new ODKDatastoreException(ex);
    }
  }

  @Override
  public void deleteEntities(Collection<EntityKey> keys, User user) throws ODKDatastoreException {

    if (keys.isEmpty()) {
      return;
    }

    List<Key> datastoreKeys = new ArrayList<Key>();
    for (EntityKey entityKey : keys) {
      dam.recordDeleteUsage(entityKey);
      datastoreKeys.add(constructGaeKey(entityKey.getRelation(), entityKey.getKey()));
      LogFactory.getLog(DatastoreImpl.class).info(
          "Executing delete " + constructGaeKind(entityKey.getRelation()) + " with key "
              + entityKey.getKey() + " by user " + user.getUriUser());
    }
    try {
      ds.delete(datastoreKeys);
    } catch (OverQuotaException ex) {
      throw new ODKOverQuotaException(ex);
    } catch (Exception ex) {
      throw new ODKDatastoreException(ex);
    }

  }

  @Override
  public TaskLock createTaskLock(User user) {
    return new TaskLockImpl(dam);
  }
}
