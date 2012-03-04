package org.opendatakit.common.ermodel.simple;

import java.math.BigDecimal;
import java.util.Date;

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

  public abstract Boolean getBoolean(String fieldName);

  public abstract Date getDate(String fieldName);

  public abstract Double getDouble(String fieldName);

  public abstract BigDecimal getNumeric(String fieldName);

  public abstract Integer getInteger(String fieldName);

  public abstract Long getLong(String fieldName);

  public abstract String getString(String fieldName);

  public abstract void set(String fieldName, Boolean value);

  public abstract void set(String fieldName, Date value);

  public abstract void set(String fieldName, Double value);

  public abstract void set(String fieldName, BigDecimal value);

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