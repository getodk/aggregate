/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

/**
 * Data Storage Converter for String Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class StringSubmissionType extends SubmissionFieldBase<String> {

  /**
   * Backing object holding the value of the submission field
   */
  protected final DynamicCommonFieldsBase backingObject;

  private boolean isChanged = false;
  private boolean isLongString = false;
  private boolean wasLongString = false;
  private String fullValue = null;
  private final FormDefinition formDefinition;
  private final EntityKey topLevelTableKey;

  public String getValue() {
    return fullValue;
  }

  /**
   * Constructor
   * 
   * @param propertyName
   *          Name of submission element
   */
  public StringSubmissionType(DynamicCommonFieldsBase backingObject, FormElementModel m,
      FormDefinition formDefinition, EntityKey topLevelTableKey) {
    super(m);
    this.backingObject = backingObject;
    this.formDefinition = formDefinition;
    this.topLevelTableKey = topLevelTableKey;
  }

  /**
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   */
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue,
      CallingContext cc) throws ODKDatastoreException {
    elemFormatter.formatString(getValue(), element, ordinalValue, row);
  }

  /**
   * Set the string value
   * 
   * @param value
   *          string form of the value
   * @throws ODKEntityPersistException
   */
  @Override
  public void setValueFromString(String value) throws ODKEntityPersistException {
     isChanged = true;
     fullValue = value;
     // update field in the backing object
     wasLongString = isLongString;
     isLongString = !backingObject.setStringField(element.getFormDataModel().getBackingKey(), value);
     // we'll persist the fullValue in the persist() method...
  }

  @Override
  public void getValueFromEntity(CallingContext cc)
        throws ODKDatastoreException {
     
	 FormDataModel model = element.getFormDataModel();
	 DataField f = model.getBackingKey();
     String value = backingObject.getStringField(f);
	 if ( value != null ) {
		int outcome = f.getMaxCharLen().compareTo(Long.valueOf(value.length()));
		if ( outcome == 0 ) {
			// this may be extended...
	        String longValue = formDefinition.getLongString(backingObject.getUri(), model.getUri(), topLevelTableKey, cc);
	        if ( longValue != null && longValue.length() != 0 ) {
	           value = longValue;
	           isLongString = true;
	        }
		} else if ( outcome < 0 ) {
			throw new IllegalStateException("Unexpected -- stored value is longer than max char len! " +
					model.getPersistAsSchema() + " " + model.getPersistAsTable() + " " + f.getName());
		}
	}
     this.fullValue = value;
     isChanged = false;
  }
  

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
     if (!(obj instanceof StringSubmissionType)) {
        return false;
     }
     
     StringSubmissionType t = (StringSubmissionType) obj;
     return ( super.equals(t) && 
           (t.isChanged == isChanged ) &&
           (t.isLongString = isLongString ) &&
           ((t.fullValue == null) ? (fullValue == null) : (t.fullValue.equals(fullValue))) &&
           (t.backingObject.getUri().equals(backingObject.getUri())) );
  }

  @Override
  public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc) throws ODKDatastoreException {
     if ( isLongString || wasLongString ) {
        formDefinition.recursivelyAddLongStringTextEntityKeys(keyList, backingObject.getUri(), element.getFormDataModel().getUri(), topLevelTableKey, cc);
     }
  }

  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException {
     if ( isChanged && (isLongString || wasLongString) ) {
    	 List<EntityKey> keyList = new ArrayList<EntityKey>();
    	 try {
			recursivelyAddEntityKeys( keyList, cc);
			cc.getDatastore().deleteEntities(keyList, cc.getCurrentUser());
			wasLongString = false;
		} catch (ODKDatastoreException e) {
			throw new ODKEntityPersistException(e);
		}
        formDefinition.setLongString(fullValue, backingObject.getUri(), element.getFormDataModel().getUri(), topLevelTableKey, cc);
     }
     isChanged = false;
  }
}
