/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.ermodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Base class for user-defined relations. The constructors assume that the name
 * of the table is UPPER_CASE only, as are the names of the DataFields in the
 * relation.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class Relation {

  /** regex for legal UPPER_CASE_COL_NAME column and table names */
  public static final String VALID_UPPER_CASE_NAME_REGEX = "[\\p{Upper}_][\\p{Upper}\\p{Digit}_]*";
  /** maximum length of a table or column name */
  public static final int MAX_PERSISTENCE_NAME_LENGTH = 63;

  private static final int MAX_DELETE_COUNT = 100;

  /** the table namespace of this relation */
  @SuppressWarnings("unused")
  private final TableNamespace namespace;
  /** name of the actual backing table in the persistence layer */
  private final String backingTableName;
  /** mapping from UPPER_CASE field names to the actual fields in database */
  private final Map<String, DataField> nameMap = new HashMap<String, DataField>();
  /** set of the actual DataFields in the database */
  private final Set<DataField> fieldSet = new HashSet<DataField>();

  RelationImpl prototype = null;

  /**
   * Standard constructor. Use for tables your application knows about and
   * manipulates directly.
   *
   * @param tableName
   *          must be UPPER_CASE beginning with an upper case letter. The actual
   *          table name in the datastore will have 3 leading underscores.
   * @param fields
   * @param cc
   * @throws ODKDatastoreException
   */
  protected Relation(String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    if (!tableName.matches(VALID_UPPER_CASE_NAME_REGEX) || tableName.contains("__")
        || tableName.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE table name beginning with an upper case letter.");
    }
    this.backingTableName = "___" + tableName;
    if (backingTableName.length() > MAX_PERSISTENCE_NAME_LENGTH) {
      throw new IllegalArgumentException("Backing table name is too long: " + backingTableName);
    }
    this.namespace = TableNamespace.EXTENSION;
    initialize(fields, cc);
  }

  /**
   * Use this constructor to place tableNames in a new namespace. This is useful
   * if you are dynamically creating tables. It allows those tables to be in a
   * different namespace from the tables your app uses to keep track of
   * everything. Aggregate, for example, ensures that submission tables start
   * with an alphabetic character, and that internal tracking tables start with
   * a leading underscore ('_').
   *
   * TableNames cannot collide if their namespaces are different. Namespaces
   * should be short 2-4 character prefixes. The overall length of the table
   * names in the database are limited to about 64 characters, so you want to
   * use short names.
   *
   * @param namespace
   *          must be UPPER_CASE beginning with an upper case letter.
   * @param tableName
   *          must be UPPER_CASE beginning with an upper case letter. The actual
   *          table name in the datastore will be composed of 2 leading
   *          underscores, the namespace string, 2 underscores, and this
   *          tableName string.
   * @param fields
   * @param cc
   * @throws ODKDatastoreException
   */
  public Relation(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    if (!namespace.matches(VALID_UPPER_CASE_NAME_REGEX) || namespace.contains("__")
        || namespace.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE namespace name beginning with an upper case letter.");
    }
    if (!tableName.matches(VALID_UPPER_CASE_NAME_REGEX) || tableName.contains("__")
        || tableName.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE table name beginning with an upper case letter.");
    }
    this.backingTableName = "__" + namespace + "__" + tableName;
    if (backingTableName.length() > MAX_PERSISTENCE_NAME_LENGTH) {
      throw new IllegalArgumentException("Backing table name is too long: " + backingTableName);
    }
    this.namespace = TableNamespace.EXTENSION;
    initialize(fields, cc);
  }

  /**
   * This is primarily for accessing the existing tables of form submissions or
   * the Aggregate internal data model. If you aren't accessing those, you
   * should not be using this constructor.
   *
   * @param type
   * @param tableName
   * @param fields
   * @param cc
   * @throws ODKDatastoreException
   */
  protected Relation(TableNamespace type, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (!tableName.matches(VALID_UPPER_CASE_NAME_REGEX)) {
      throw new IllegalArgumentException("Expected an UPPER_CASE table name.");
    }

    if (tableName.length() > MAX_PERSISTENCE_NAME_LENGTH) {
      throw new IllegalArgumentException("Backing table name is too long: " + tableName);
    }

    switch (type) {
    case SUBMISSIONS:
      // submissions tables never start with a leading underscore.
      if (tableName.charAt(0) == '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingTableName = tableName;
      namespace = TableNamespace.SUBMISSIONS;
      // don't proceed if the table doesn't exist
      if (!ds.hasRelation(ds.getDefaultSchemaName(), tableName, user)) {
        throw new IllegalArgumentException("Submissions table does not exist");
      }
      break;
    case INTERNALS:
      // internal tables to Aggregate start with an underscore
      // followed by an alphanumeric character.
      if (tableName.charAt(0) != '_' || tableName.charAt(1) == '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingTableName = tableName;
      namespace = TableNamespace.INTERNALS;
      // don't proceed if the table doesn't exist
      if (!ds.hasRelation(ds.getDefaultSchemaName(), tableName, user)) {
        throw new IllegalArgumentException("Submissions table does not exist");
      }
      break;
    case EXTENSION:
      // extensions start with at least two underscores...
      if (tableName.charAt(0) != '_' || tableName.charAt(1) != '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingTableName = tableName;
      namespace = TableNamespace.EXTENSION;
      break;
    default:
      throw new IllegalStateException("Unexpected TableNamespace value");
    }
    initialize(fields, cc);
  }

  /**
   * Create a new entity. This entity does not exist in the database until you
   * put() it there.
   *
   * @param cc
   * @return
   */
  public Entity newEntity(CallingContext cc) {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    return new EntityImpl(ds.createEntityUsingRelation(prototype, user));
  }

  /**
   * Create a new entity. This entity does not exist in the database until you
   * put() it there.
   *
   * @param uri
   *          the primary key for this new entity. The key must be a string less
   *          than 80 characters long. It should be in a URI-style format --
   *          meaning that it has a namespace identifier followed by a colon,
   *          followed by a string in that namespace. The default is a uri in
   *          the UUID namespace. You can construct one of these UUID uris using
   *          CommonFieldsBase.newUri().
   *
   *          Those are of the form: "uuid:371adf05-3cea-4e11-b56c-3b3a1ec25761"
   * @param cc
   * @return
   */
  public Entity newEntity(String uri, CallingContext cc) {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (uri == null) {
      throw new IllegalArgumentException("uri cannot be null");
    }

    EntityImpl ei = new EntityImpl(ds.createEntityUsingRelation(prototype, user));
    ei.backingObject.setStringField(ei.backingObject.primaryKey, uri);
    return ei;
  }

  /**
   * Fetch the entity with the given primary key (uri).
   *
   * @param uri
   * @param cc
   * @return
   * @throws ODKEntityNotFoundException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  public Entity getEntity(String uri, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    return new EntityImpl(ds.getEntity(prototype, uri, user));
  }

  /**
   * Creates an empty query which can be used to query this relation.
   *
   * @return an empty Query.
   */
  public Query query(String loggingContextTag, CallingContext cc) {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    org.opendatakit.common.persistence.Query emptyQuery = ds.createQuery(prototype,
        loggingContextTag, user);
    return new Query(this, emptyQuery);
  }

  /**
   * This deletes all records in your table and drops it from the datastore. The
   * deletion step is non-optimal for MySQL/Postgresql, but is required for
   * Google BigTables, as that has no concept of dropping a relation.
   *
   * @param cc
   * @throws ODKDatastoreException
   */
  public void dropRelation(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    org.opendatakit.common.persistence.Query q = ds.createQuery(prototype,
        "AbstractRelation.dropRelation", user);
    List<?> pkList = q.executeDistinctValueForDataField(prototype.primaryKey);
    List<EntityKey> keys = new ArrayList<EntityKey>();
    for (Object key : pkList) {
      // we don't ahve the individual records, just the PKs for them
      // construct the entity keys from the relation and those PKs
      keys.add(new EntityKey(prototype, (String) key));
      if ( keys.size() > MAX_DELETE_COUNT ) {
        ds.deleteEntities(keys, user);
        keys.clear();
      }
    }
    ds.deleteEntities(keys, user);
    ds.dropRelation(prototype, user);
    prototype = null;
  }

  /**
   * Retrieve the DataField that matches the given fieldName. Useful when
   * working with a dynamically-constructed table.
   *
   * @param fieldName
   * @return
   */
  public DataField getDataField(String fieldName) {
    DataField f = nameMap.get(fieldName);
    if (f == null) {
      if (this.prototype == null) {
        throw new IllegalArgumentException("Field name " + fieldName
            + " is not a valid field name for this relation");
      } else if (fieldName.equals(CommonFieldsBase.CREATION_DATE_COLUMN_NAME)) {
        f = this.prototype.creationDate;
      } else if (fieldName.equals(CommonFieldsBase.CREATOR_URI_USER_COLUMN_NAME)) {
        f = this.prototype.creatorUriUser;
      } else if (fieldName.equals(CommonFieldsBase.LAST_UPDATE_DATE_COLUMN_NAME)) {
        f = this.prototype.lastUpdateDate;
      } else if (fieldName.equals(CommonFieldsBase.LAST_UPDATE_URI_USER_COLUMN_NAME)) {
        f = this.prototype.lastUpdateUriUser;
      } else if (fieldName.equals(CommonFieldsBase.URI_COLUMN_NAME)) {
        f = this.prototype.primaryKey;
      } else {
        throw new IllegalArgumentException("Field name " + fieldName
            + " is not a valid field name for this relation");
      }
    }
    return f;
  }

  /**
   * @return an unmodifiable set of the fields in this relation
   */
  public Set<DataField> getDataFields() {
    return Collections.unmodifiableSet(fieldSet);
  }

  /**
   * The backing object for the Entity.
   *
   * @author mitchellsundt@gmail.com
   *
   */
  protected static class RelationImpl extends CommonFieldsBase {

    RelationImpl(String schemaName, String tableName, List<DataField> definedFields) {
      super(schemaName, tableName);
      fieldList.addAll(definedFields);
    }

    private RelationImpl(RelationImpl ref, User user) {
      super(ref, user);
    }

    @Override
    public CommonFieldsBase getEmptyRow(User user) {
      return new RelationImpl(this, user);
    }
  };

  /**
   * Implementation of the Entity interface.
   *
   * @author mitchellsundt@gmail.com
   *
   */
  protected class EntityImpl implements Entity {

    /** the actual persistence layer object holding the data values */
    private final RelationImpl backingObject;

    /**
     * Constructor used only be RelationManipulator
     *
     * @param backingObject
     */
    public EntityImpl(RelationImpl backingObject) {
      this.backingObject = backingObject;
    }

    @Override
    public String getId() {
      return backingObject.getUri();
    }

    @Override
    public String getCreationUser() {
      return backingObject.getCreatorUriUser();
    }

    @Override
    public Date getCreationDate() {
      return backingObject.getCreationDate();
    }

    @Override
    public Date getLastUpdateDate() {
      return backingObject.getLastUpdateDate();
    }

    @Override
    public String getLastUpdateUser() {
      return backingObject.getLastUpdateUriUser();
    }

    @Override
    public Boolean getBoolean(DataField field) {
      return backingObject.getBooleanField(verify(field));
    }

    @Override
    public Date getDate(DataField field) {
      return backingObject.getDateField(verify(field));
    }

    @Override
    public Double getDouble(DataField field) {
    	WrappedBigDecimal d = backingObject.getNumericField(verify(field));
      return (d == null) ? null : d.doubleValue();
    }

    @Override
    public WrappedBigDecimal getNumeric(DataField field) {
      return backingObject.getNumericField(verify(field));
    }

    @Override
    public Integer getInteger(DataField field) {
      Long l = backingObject.getLongField(verify(field));
      return (l == null) ? null : l.intValue();
    }

    @Override
    public Long getLong(DataField field) {
      return backingObject.getLongField(verify(field));
    }

    @Override
    public String getString(DataField field) {
      return backingObject.getStringField(verify(field));
    }

    @Override
    public Boolean getBoolean(String fieldName) {
      DataField field = getDataField(fieldName);
      return backingObject.getBooleanField(verify(field));
    }

    @Override
    public Date getDate(String fieldName) {
      DataField field = getDataField(fieldName);
      return backingObject.getDateField(verify(field));
    }

    @Override
    public Double getDouble(String fieldName) {
      DataField field = getDataField(fieldName);
      WrappedBigDecimal d = backingObject.getNumericField(verify(field));
      return (d == null) ? null : d.doubleValue();
    }

    @Override
    public WrappedBigDecimal getNumeric(String fieldName) {
      DataField field = getDataField(fieldName);
      return backingObject.getNumericField(verify(field));
    }

    @Override
    public Integer getInteger(String fieldName) {
      DataField field = getDataField(fieldName);
      Long l = backingObject.getLongField(verify(field));
      return (l == null) ? null : l.intValue();
    }

    @Override
    public Long getLong(String fieldName) {
      DataField field = getDataField(fieldName);
      return backingObject.getLongField(verify(field));
    }

    @Override
    public String getString(String fieldName) {
      DataField field = getDataField(fieldName);
      return backingObject.getStringField(verify(field));
    }

    @Override
    public void set(DataField field, Boolean value) {
      backingObject.setBooleanField(verify(field), value);
    }

    @Override
    public void set(DataField field, Date value) {
      backingObject.setDateField(verify(field), value);
    }

    @Override
    public void set(DataField field, Double value) {
      backingObject.setNumericField(verify(field),
          (value == null) ? null : WrappedBigDecimal.fromDouble(value));
    }

    @Override
    public void set(DataField field, WrappedBigDecimal value) {
      backingObject.setNumericField(verify(field), value);
    }

    @Override
    public void set(DataField field, Integer value) {
      backingObject.setLongField(verify(field), (value == null) ? null : Long.valueOf(value));
    }

    @Override
    public void set(DataField field, Long value) {
      backingObject.setLongField(verify(field), value);
    }

    @Override
    public void set(DataField field, String value) {
      if (!backingObject.setStringField(verify(field), value)) {
        throw new IllegalArgumentException("Value is too long (" + value.length() + ") for field "
            + field);
      }
    }

    @Override
    public void set(String fieldName, Boolean value) {
      DataField field = getDataField(fieldName);
      backingObject.setBooleanField(verify(field), value);
    }

    @Override
    public void set(String fieldName, Date value) {
      DataField field = getDataField(fieldName);
      backingObject.setDateField(verify(field), value);
    }

    @Override
    public void set(String fieldName, Double value) {
      DataField field = getDataField(fieldName);
      backingObject.setNumericField(verify(field),
          (value == null) ? null : WrappedBigDecimal.fromDouble(value));
    }

    @Override
    public void set(String fieldName, WrappedBigDecimal value) {
      DataField field = getDataField(fieldName);
      backingObject.setNumericField(verify(field), value);
    }

    @Override
    public void set(String fieldName, Integer value) {
      DataField field = getDataField(fieldName);
      backingObject.setLongField(verify(field), (value == null) ? null : Long.valueOf(value));
    }

    @Override
    public void set(String fieldName, Long value) {
      DataField field = getDataField(fieldName);
      backingObject.setLongField(verify(field), value);
    }

    @Override
    public void set(String fieldName, String value) {
      DataField field = getDataField(fieldName);
      if (!backingObject.setStringField(verify(field), value)) {
        throw new IllegalArgumentException("Value is too long (" + value.length() + ") for field "
            + field);
      }
    }

    @Override
    public String getAsString(String fieldName) {
      DataField f;
      if (fieldName.matches(VALID_UPPER_CASE_NAME_REGEX)) {
        f = Relation.this.getDataField(fieldName);
      } else {
        f = Relation.this.getDataField(WebUtils.unCamelCase(fieldName));
      }
      switch (f.getDataType()) {
      case INTEGER:
        Long l = backingObject.getLongField(f);
        if (l == null)
          return null;
        return l.toString();
      case DECIMAL:
        WrappedBigDecimal v = backingObject.getNumericField(f);
        if (v == null)
          return null;
        return v.toString();
      case BOOLEAN:
        Boolean b = backingObject.getBooleanField(f);
        if (b == null)
          return null;
        return b.toString();
      case STRING:
      case URI:
        return backingObject.getStringField(f);
      case DATETIME:
        Date d = backingObject.getDateField(f);
        return WebUtils.iso8601Date(d);
      default:
        throw new IllegalArgumentException("Invalid type for field " + f.getName());
      }
    }

    @Override
    public void setAsString(String fieldName, String value) {
      DataField f = getDataField(fieldName);
      if (f.getName().equals(CommonFieldsBase.CREATION_DATE_COLUMN_NAME)
          || f.getName().equals(CommonFieldsBase.CREATOR_URI_USER_COLUMN_NAME)
          || f.getName().equals(CommonFieldsBase.LAST_UPDATE_DATE_COLUMN_NAME)
          || f.getName().equals(CommonFieldsBase.LAST_UPDATE_URI_USER_COLUMN_NAME)
          || f.getName().equals(CommonFieldsBase.URI_COLUMN_NAME)) {
        throw new IllegalArgumentException("Cannot set the value of a metadata field: "
            + f.getName());
      }

      switch (f.getDataType()) {
      case INTEGER:
        try {
          backingObject.setLongField(f, (value == null || value.length() == 0) ? null : Long.valueOf(value));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unparsable integer value: " + value + " for field: "
              + f.getName());
        }
        break;
      case DECIMAL:
        try {
          backingObject.setNumericField(f, (value == null || value.length() == 0) ? null : new WrappedBigDecimal(value));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unparsable integer value: " + value + " for field: "
              + f.getName());
        }
        break;
      case BOOLEAN:
        Boolean b = WebUtils.parseBoolean(value);
        backingObject.setBooleanField(f, b);
        break;
      case STRING:
      case URI:
        if (!backingObject.setStringField(f, value)) {
          throw new IllegalArgumentException("Value is too long (" + value.length()
              + ") for field " + f.getName());
        }
        break;
      case DATETIME:
        Date d = WebUtils.parseDate(value);
        backingObject.setDateField(f, d);
        break;
      default:
        throw new IllegalArgumentException("Invalid type for field " + f.getName());
      }
    }
    
    @Override
    public boolean isFromDatabase() {
      return backingObject.isFromDatabase();
    }

    /**
     * Save this entity into the datastore.
     *
     * @param cc
     * @throws ODKEntityPersistException
     * @throws ODKOverQuotaException
     */
    @Override
    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();

      ds.putEntity(backingObject, user);
    }

    /**
     * Remove this entity from the datastore.
     *
     * @param cc
     * @throws ODKDatastoreException
     */
    @Override
    public void delete(CallingContext cc) throws ODKDatastoreException {
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();

      ds.deleteEntity(backingObject.getEntityKey(), user);
    }

    /**
     * Verify the DataField is one defined by this relation. This is purely for
     * debugging mismatched uses of DataFields. DataField equality is '=='
     * equivalence. You must use the same DataField as that used when creating
     * the relation.
     *
     * Use {@link Relation.getDataField(String fieldName)} to retrieve the
     * DataField for a given field name.
     *
     * @param fieldName
     * @return
     */
    private final DataField verify(DataField fieldName) {
      if (!backingObject.getFieldList().contains(fieldName)) {
        throw new IllegalArgumentException("FieldName: " + fieldName.getName()
            + " is not identical to the one specified in this relation " + fieldName.toString());
      }
      return fieldName;
    }

    private DataField getDataField(String fieldName) {
      DataField f;
      if (fieldName.matches(VALID_UPPER_CASE_NAME_REGEX)) {
        f = Relation.this.getDataField(fieldName);
      } else {
        f = Relation.this.getDataField(WebUtils.unCamelCase(fieldName));
      }
      return f;
    }
    

    boolean isCompatible(EntityImpl entityImpl) {
      return this.backingObject.getTableName().equals(entityImpl.backingObject.getTableName()) &&
          this.isFromDatabase() == entityImpl.isFromDatabase();
    }
  }

  /**
   * Complete the initialization of the relation with the UPPER_CASE fieldNames.
   * Note that the fields: _URI, _LAST_UPDATE_DATE, _LAST_UPDATE_URI_USER,
   * _CREATION_DATE, _CREATOR_URI_USER are always present and should not be
   * passed into the fields list.
   *
   * @param fields
   * @param cc
   * @throws ODKDatastoreException
   */
  private void initialize(List<DataField> fields, CallingContext cc) throws ODKDatastoreException {

    List<DataField> definedFields = new ArrayList<DataField>();
    for (DataField f : fields) {
      String name = f.getName();
      if (!name.matches(VALID_UPPER_CASE_NAME_REGEX)) {
        throw new IllegalArgumentException("Field name is not a valid UPPER_CASE name: " + name);
      }
      if (name.length() > MAX_PERSISTENCE_NAME_LENGTH) {
        throw new IllegalArgumentException("Field name is too long: " + name);
      }
      if (nameMap.containsKey(name)) {
        throw new IllegalArgumentException("Field name: " + name + " is already specified!");
      }
      nameMap.put(name, f);
      fieldSet.add(f);
      definedFields.add(f);
    }

    // the 5 reserved column names should not be in the DataField list.
    // If you need access the DataField for them, use the
    // Relation.getDataField() API to
    // obtain them, or just use the Entity.getCreationDate(), etc. APIs.
    if (nameMap.containsKey(CommonFieldsBase.CREATION_DATE_COLUMN_NAME)
        || nameMap.containsKey(CommonFieldsBase.CREATOR_URI_USER_COLUMN_NAME)
        || nameMap.containsKey(CommonFieldsBase.LAST_UPDATE_DATE_COLUMN_NAME)
        || nameMap.containsKey(CommonFieldsBase.LAST_UPDATE_URI_USER_COLUMN_NAME)
        || nameMap.containsKey(CommonFieldsBase.URI_COLUMN_NAME)) {
      throw new IllegalArgumentException("One of the 5 reserved DataField names is "
          + "errorneously supplied in the DataField list");
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    String schema = ds.getDefaultSchemaName();
    synchronized (Relation.class) {
      RelationImpl candidate = new RelationImpl(schema, backingTableName, definedFields);
      ds.assertRelation(candidate, user);
      prototype = candidate;
    }
  }

  /**
   * This is just a convenience method.
   *
   * @param e
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public static void putEntity(Entity e, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    EntityImpl ei = (EntityImpl) e;
    ds.putEntity(ei.backingObject, user);
  }

  /**
   * This is just a convenience method. It may fail midway through saving the
   * list of entities.
   *
   * @param eList
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public static void putEntities(List<Entity> eList, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    List<RelationImpl> backingObjects = new ArrayList<RelationImpl>();
    for (Entity e : eList) {
      EntityImpl ei = (EntityImpl) e;
      backingObjects.add(ei.backingObject);
    }
    ds.putEntities(backingObjects, user);
  }
  /**
   * Execute a set of commands sharing the same SQL but with different bind parameters.
   * This may either insert or update data (one or the other, across all alterations).
   * I.e., you cannot mix updates and inserts -- they are either all updates or all inserts.
   * 
   * @param bulkAlterEntities
   * @param cc
   * @throws ODKDatastoreException
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public void bulkAlterEntities(List<Entity> bulkAlterEntities, CallingContext cc)
      throws ODKDatastoreException, ODKEntityPersistException, ODKOverQuotaException {
 
    if (bulkAlterEntities == null) {
      throw new ODKDatastoreException("No bulk update list provided");
    }

    if (bulkAlterEntities.size() < 1) {
      throw new ODKDatastoreException("Bulk update list MUST contain at least one item");
    }

    ArrayList<CommonFieldsBase> changes = new ArrayList<CommonFieldsBase>();
    
    EntityImpl lastUpdate = (EntityImpl) bulkAlterEntities.get(0);
    for ( Entity entity : bulkAlterEntities ) {
      EntityImpl update = (EntityImpl) entity;
      if ( !lastUpdate.isCompatible(update) ) {
        throw new ODKDatastoreException(
            "INCOMPATIBLE BULK UPDATES were found inside an attempted bulk update");
      }
      changes.add(update.backingObject);
    }
    cc.getDatastore().batchAlterData(changes, cc.getCurrentUser());
  }

  /**
   * This is just a convenience function.
   *
   * @param e
   * @param cc
   * @throws ODKDatastoreException
   */
  public static void deleteEntity(Entity e, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    EntityImpl ei = (EntityImpl) e;
    ds.deleteEntity(ei.backingObject.getEntityKey(), user);
  }

  /**
   * This is just a convenience function. It can fail after having deleted only
   * some of the entities.
   *
   * @param eList
   * @param cc
   * @throws ODKDatastoreException
   */
  public static void deleteEntities(List<Entity> eList, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    List<EntityKey> keys = new ArrayList<EntityKey>();
    for (Entity e : eList) {
      EntityImpl ei = (EntityImpl) e;
      keys.add(ei.backingObject.getEntityKey());
    }
    ds.deleteEntities(keys, user);
  }

  /**
   * Helper function to verify that DataField values are not getting confounded.
   *
   * @param field
   * @return field
   */
  public final DataField verify(DataField field) {
    if (prototype != null && !prototype.getFieldList().contains(field)) {
      throw new IllegalArgumentException("FieldName: " + field.getName()
          + " is not identical to the one specified in this relation " + field.toString());
    }
    return field;
  }

}
