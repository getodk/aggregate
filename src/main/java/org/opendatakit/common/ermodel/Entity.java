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

import java.math.BigDecimal;
import java.util.Date;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * API for the manipulation of an entity within a {@link Relation}.  See
 * {@link AbstractRelation} for how to create a new entity, fetch an 
 * existing one, etc.  In general, a {@link Relation} should only store
 * fairly short strings.  The total number of bytes that an Entity can 
 * hold is generally limited by the persistence layer to less than 65000
 * bytes.  If you need to store a file, use the BinaryContentManipulator
 * class to do that. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public interface Entity {
	// primary key 
	public String getUri();

	// metadata field
	public Date getLastUpdateDate();

	// metadata field; of the form: "mailto:user@uw.edu"
	public String getLastUpdateUriUser();

	// metadata field
	public Date getCreationDate();

	// metadata field; of the form: "mailto:user@uw.edu"
	public String getCreatorUriUser();
	
	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setBoolean(DataField fieldName, Boolean value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public Boolean getBoolean(DataField fieldName);
	
	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setDate(DataField fieldName, Date value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public Date getDate(DataField fieldName);
	
	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setDouble(DataField fieldName, Double value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public Double getDouble(DataField fieldName);
	
	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setNumeric(DataField fieldName, BigDecimal value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public BigDecimal getNumeric(DataField fieldName);

	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setInteger(DataField fieldName, Integer value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public Integer getInteger(DataField fieldName);

	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setLong(DataField fieldName, Long value);
	
	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public Long getLong(DataField fieldName);
	
	/**
	 * Type-safe setter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setString(DataField fieldName, String value );

	/**
	 * Type-safe getter.  Will throw exception if field is of the wrong type.
	 * 
	 * @param fieldName
	 */
	public String getString(DataField fieldName);
	
	/**
	 * Given a field name, and a string representation of the value, 
	 * this interprets an UPPER_CASE name as the true field name, and
	 * otherwise attempts to convert a camelCase name to CAMEL_CASE.
	 * It then retrieves the DataField for that name and, based upon
	 * the datatype of that field, parses the value to obtain the 
	 * appropriately-typed value to store into this field.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void setField( String fieldName, String value );
	
	/**
	 * Given a field name, this interprets an UPPER_CASE name as the
	 * true field name, and otherwise attempts to convert a camelCase
	 * name to CAMEL_CASE. It then retrieves the value of the 
	 * DataField for that name and converts it to a string.
	 * 
	 * @param fieldName
	 * @return
	 */
	public String getField( String fieldName );
	
	/**
	 * Save this entity into the datastore.
	 * 
	 * @param cc
	 * @throws ODKEntityPersistException
	 * @throws ODKOverQuotaException 
	 */
	public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;
	
	/**
	 * Remove this entity from the datastore.
	 * 
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public void remove(CallingContext cc) throws ODKDatastoreException;
}
