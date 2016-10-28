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
package org.opendatakit.common.persistence;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.security.User;

/**
 * Base class defining the audit fields for a table.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public abstract class CommonFieldsBase {

  public static final int AUDIT_COLUMN_COUNT = PersistConsts.AUDIT_COLUMN_COUNT;

  public static final String URI_COLUMN_NAME = PersistConsts.URI_COLUMN_NAME;
  public static final String LAST_UPDATE_DATE_COLUMN_NAME = PersistConsts.LAST_UPDATE_DATE_COLUMN_NAME;
  public static final String LAST_UPDATE_URI_USER_COLUMN_NAME = PersistConsts.LAST_UPDATE_URI_USER_COLUMN_NAME;
  public static final String CREATION_DATE_COLUMN_NAME = PersistConsts.CREATION_DATE_COLUMN_NAME;
  public static final String CREATOR_URI_USER_COLUMN_NAME = PersistConsts.CREATOR_URI_USER_COLUMN_NAME;

  /** standard audit fields */

  /** creator */
  private static final DataField CREATOR_URI_USER = new DataField(CREATOR_URI_USER_COLUMN_NAME,
      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN);
  /** creation date */
  private static final DataField CREATION_DATE = new DataField(CREATION_DATE_COLUMN_NAME,
      DataField.DataType.DATETIME, false);
  /** last user to update record */
  private static final DataField LAST_UPDATE_URI_USER = new DataField(
      LAST_UPDATE_URI_USER_COLUMN_NAME, DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);
  /** last update date */
  private static final DataField LAST_UPDATE_DATE = new DataField(LAST_UPDATE_DATE_COLUMN_NAME,
      DataField.DataType.DATETIME, false).setIndexable(IndexType.ORDERED);

  /** primary key for all tables */
  private static final DataField URI = new DataField(URI_COLUMN_NAME, DataField.DataType.URI,
      false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);

  /** member variables */
  protected final String schemaName;
  protected final String tableName;
  private boolean fromDatabase = false;
  private Object opaquePersistenceData = null;
  protected final List<DataField> fieldList = new ArrayList<DataField>();
  protected final Map<DataField, Object> fieldValueMap = new HashMap<DataField, Object>();

  public final DataField primaryKey;
  public final DataField creatorUriUser;
  public final DataField creationDate;
  public final DataField lastUpdateUriUser;
  public final DataField lastUpdateDate;

  /**
   * Construct a relation prototype.
   *
   * @param schemaName
   * @param tableName
   * @param tableType
   */
  protected CommonFieldsBase(String schemaName, String tableName) {
    this.schemaName = schemaName;
    this.tableName = tableName;
    // always primary key with the same name...
    fieldList.add(primaryKey = new DataField(URI));

    // and add audit fields everywhere...
    fieldList.add(creatorUriUser = new DataField(CREATOR_URI_USER));
    fieldList.add(creationDate = new DataField(CREATION_DATE));
    fieldList.add(lastUpdateUriUser = new DataField(LAST_UPDATE_URI_USER));
    fieldList.add(lastUpdateDate = new DataField(LAST_UPDATE_DATE));
  }

  /**
   * Construct an empty entity.
   *
   * @param ref
   * @param user
   */
  protected CommonFieldsBase(CommonFieldsBase ref, User user) {
    schemaName = ref.schemaName;
    tableName = ref.tableName;

    primaryKey = ref.primaryKey;
    creatorUriUser = ref.creatorUriUser;
    creationDate = ref.creationDate;
    lastUpdateUriUser = ref.lastUpdateUriUser;
    lastUpdateDate = ref.lastUpdateDate;

    fieldList.addAll(ref.fieldList);

    // populate the audit fields...
    Date now = new Date();
    fieldValueMap.put(creationDate, now);
    fieldValueMap.put(lastUpdateDate, now);
    fieldValueMap.put(creatorUriUser, user.getUriUser());
    fieldValueMap.put(primaryKey, CommonFieldsBase.newUri());
  }

  public final EntityKey getEntityKey() {
    return new EntityKey(this, getUri());
  }

  public final String getSchemaName() {
    return schemaName;
  }

  public final String getTableName() {
    return tableName;
  }

  /**
   * @return the primary key value for this row
   */
  public final String getUri() {
    return getStringField(primaryKey);
  }

  public final String getCreatorUriUser() {
    return getStringField(creatorUriUser);
  }

  public final Date getCreationDate() {
    return getDateField(creationDate);
  }

  public final String getLastUpdateUriUser() {
    return getStringField(lastUpdateUriUser);
  }

  public final Date getLastUpdateDate() {
    return getDateField(lastUpdateDate);
  }

  public final List<DataField> getFieldList() {
    return Collections.unmodifiableList(fieldList);
  }

  public final String getStringField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (String) o;
  }

  /**
   * Set the given field to the given value. If the value is too long, the
   * prefix is stored and false is returned.
   *
   * @param f
   *          field to set
   * @param value
   *          string value for field
   * @return false if the value had to be truncated.
   */
  public final boolean setStringField(DataField f, String value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (!((f.getDataType() == DataType.STRING) || (f.getDataType() == DataType.LONG_STRING) || (f
        .getDataType() == DataType.URI))) {
      throw new IllegalArgumentException("Attempting to set non-string field " + f.getName()
          + " with a String in " + schemaName + "." + tableName);
    }
    boolean noOverflow = true;
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return true;
    } else if (f.getMaxCharLen().compareTo(Long.valueOf(value.length())) < 0) {
      if (f.getDataType() == DataType.LONG_STRING) {
        throw new IllegalArgumentException("overflowing field " + f.getName()
            + " as a LONG_STRING!! in " + schemaName + "." + tableName);
      } else if (f.getDataType() == DataType.URI) {
        throw new IllegalArgumentException("overflowing field " + f.getName() + " as a URI!! in "
            + schemaName + "." + tableName);
      }
      noOverflow = false;
      value = value.substring(0, f.getMaxCharLen().intValue());
    }
    fieldValueMap.put(f, value);
    return noOverflow;
  }

  public final Long getLongField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (Long) o;
  }

  public final void setLongField(DataField f, Long value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (f.getDataType() != DataType.INTEGER) {
      throw new IllegalArgumentException("Attempting to set non-integer field " + f.getName()
          + " with a Long in " + schemaName + "." + tableName);
    }
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return;
    }
    fieldValueMap.put(f, value);
  }

  public final WrappedBigDecimal getNumericField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (WrappedBigDecimal) o;
  }

  public final void setNumericField(DataField f, WrappedBigDecimal value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (f.getDataType() != DataType.DECIMAL) {
      throw new IllegalArgumentException("Attempting to set non-decimal field " + f.getName()
          + " with a BigDecimal in " + schemaName + "." + tableName);
    }
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return;
    }
    if ( !f.isDoublePrecision()  && !value.isSpecialValue() ) {
      // enforce scaling here...
      fieldValueMap.put(f, value.setScale(f.getNumericScale(), BigDecimal.ROUND_HALF_UP));
    } else {
      fieldValueMap.put(f, value);
    }
  }

  public final Date getDateField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (Date) o;
  }

  public final void setDateField(DataField f, Date value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (f.getDataType() != DataType.DATETIME) {
      throw new IllegalArgumentException("Attempting to set non-datetime field " + f.getName()
          + " with a Date in " + schemaName + "." + tableName);
    }
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return;
    }
    fieldValueMap.put(f, value);
  }

  public final Boolean getBooleanField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (Boolean) o;
  }

  public final void setBooleanField(DataField f, Boolean value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (f.getDataType() != DataType.BOOLEAN) {
      throw new IllegalArgumentException("Attempting to set non-boolean field " + f.getName()
          + " with a Boolean in " + schemaName + "." + tableName);
    }
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return;
    }
    fieldValueMap.put(f, value);
  }

  public final byte[] getBlobField(DataField f) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to get a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    Object o = fieldValueMap.get(f);
    if (o == null)
      return null;
    return (byte[]) o;
  }

  public final void setBlobField(DataField f, byte[] value) {
    if (f == null) {
      throw new IllegalArgumentException("Field value is null!");
    }
    if (!fieldList.contains(f)) {
      throw new IllegalArgumentException("Attempting to set a field " + f.getName()
          + " not belonging to " + schemaName + "." + tableName);
    }
    if (f.getDataType() != DataType.BINARY) {
      throw new IllegalArgumentException("Attempting to set non-blob field " + f.getName()
          + " with byte-array in " + schemaName + "." + tableName);
    }
    if (value == null) {
      if (!f.getNullable()) {
        throw new IllegalStateException("Attempting to set null value in non-null field "
            + f.getName() + " in " + schemaName + "." + tableName);
      }
      fieldValueMap.remove(f);
      return;
    }
    fieldValueMap.put(f, value);
  }

  public final static String newUri() {
    String s = "uuid:" + UUID.randomUUID().toString().toLowerCase();
    return s;
  }

  public final static String newMD5HashUri(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] asBytes;
      try {
        asBytes = value.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        throw new IllegalStateException("unexpected", e);
      }
      md.update(asBytes);

      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      return "md5:" + md5;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unexpected problem computing md5 hash", e);
    }
  }

  public final static String newMD5HashUri(byte[] asBytes) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(asBytes);

      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      return "md5:" + md5;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unexpected problem computing md5 hash", e);
    }
  }

  /**********************************************************************************
   **********************************************************************************
   **********************************************************************************
   * APIs that should only be used by the persistence layer
   **********************************************************************************
   **********************************************************************************
   **********************************************************************************/

  /**
   * Method implemented in the most derived class to clone the (concrete)
   * relation instance to produce an empty entity. Only called via
   * {@link org.opendatakit.common.persistence.Datastore#createEntityUsingRelation(CommonFieldsBase, User)}
   *
   * @param user
   * @return empty entity
   */
  public abstract CommonFieldsBase getEmptyRow(User user);

  /**
   * @return true if the row contains data that originated from the persistent
   *         store.
   */
  public final boolean isFromDatabase() {
    return fromDatabase;
  }

  /**
   * Set whether or not the row contains data that originated from the
   * persistent store. This should only be called from within the persistence
   * layer implementation. Used to determine whether to INSERT or UPDATE a
   * record in the persistent store.
   *
   * @param fromDatabase
   */
  public final void setFromDatabase(boolean fromDatabase) {
    this.fromDatabase = fromDatabase;
  }

  /**
   * @return the opaque object linked to this row by the persistence layer.
   */
  public Object getOpaquePersistenceData() {
    return opaquePersistenceData;
  }

  /**
   * Associate an opaque object with this row. This should only be called from
   * within the persistence layer implementation. Used by some persistence
   * layers to associated private information to a retrieved object that will be
   * needed if updates to the row are requested.
   *
   * @param opaquePersistenceData
   */
  public void setOpaquePersistenceData(Object opaquePersistenceData) {
    this.opaquePersistenceData = opaquePersistenceData;
  }

  public final boolean isNull(DataField f) {
    return (fieldValueMap.get(f) == null);
  }

  public boolean sameTable(CommonFieldsBase ref) {
    return getSchemaName().equals(ref.getSchemaName()) && getTableName().equals(ref.getTableName());
  }
}
