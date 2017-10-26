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

package org.opendatakit.common.ermodel;

import java.util.Date;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

public interface Entity {

  /**
   * @return the unique identifier of this Entity. You can later retrieve this
   *         Entity using {@link Relation#getEntity(String)}.
   */
  public abstract String getId();

  /**
   * @return the Date of the last time this Entity was saved to the datastore.
   */
  public abstract Date getLastUpdateDate();

  /**
   * @return the Date that this Entity was first saved to the datastore.
   */
  public abstract Date getCreationDate();

  /**
   * @return the user who created this entity, of the form
   *         "mailto:username@domain.com"
   */
  public abstract String getCreationUser();

  /**
   * @return the user who last updated this entity, of the form
   *         "mailto:username@domain.com"
   */
  public abstract String getLastUpdateUser();

  // accessors for data fields

  public abstract Boolean getBoolean(DataField field);

  public abstract Date getDate(DataField field);

  public abstract Double getDouble(DataField field);

  public abstract WrappedBigDecimal getNumeric(DataField field);

  public abstract Integer getInteger(DataField field);

  public abstract Long getLong(DataField field);

  public abstract String getString(DataField field);

  // accessors for strings

  public abstract Boolean getBoolean(String fieldName);

  public abstract Date getDate(String fieldName);

  public abstract Double getDouble(String fieldName);

  public abstract WrappedBigDecimal getNumeric(String fieldName);

  public abstract Integer getInteger(String fieldName);

  public abstract Long getLong(String fieldName);

  public abstract String getString(String fieldName);
  
  public abstract boolean isFromDatabase();

  public abstract void set(DataField field, Boolean value);

  public abstract void set(DataField field, Date value);

  public abstract void set(DataField field, Double value);

  public abstract void set(DataField field, WrappedBigDecimal value);

  public abstract void set(DataField field, Integer value);

  public abstract void set(DataField field, Long value);

  public abstract void set(DataField field, String value);

  // setters for names only

  public abstract void set(String fieldName, Boolean value);

  public abstract void set(String fieldName, Date value);

  public abstract void set(String fieldName, Double value);

  public abstract void set(String fieldName, WrappedBigDecimal value);

  public abstract void set(String fieldName, Integer value);

  public abstract void set(String fieldName, Long value);

  public abstract void set(String fieldName, String value);

  /**
   * Retrieves the value of the given attribute and returns the value as a
   * String.
   */
  public abstract String getAsString(String fieldName);

  /**
   * Attempts to parse 'value' to the correct type for the given attribute and
   * then set it on this entity.
   */
  public abstract void setAsString(String fieldName, String value);

  /**
   * Saves this Entity to the datastore.
   *
   * @throws ODKEntityPersistException
   *           if there was a problem saving the Entity.
   * @throws ODKOverQuotaException
   */
  public abstract void put(CallingContext cc) throws ODKEntityPersistException,
      ODKOverQuotaException;

  /**
   * Deletes this Entity from the datastore.
   *
   * @throws ODKDatastoreException
   *           if there was a problem deleting this Entity.
   */
  public abstract void delete(CallingContext cc) throws ODKDatastoreException;

}