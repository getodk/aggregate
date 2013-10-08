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



import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class SubmissionFieldBase<T> implements SubmissionField<T>{

  /**
   * Submission property/element name
   */
  protected final FormElementModel element;

  public SubmissionFieldBase(FormElementModel element) {
    this.element = element;
  }

  @Override
  public final String getPropertyName() {
    return element.getElementName();
  }
  
  @Override
  public final FormElementModel getFormElementModel() {
    return element;
  }
  
  @Override
  public boolean depthFirstTraversal(SubmissionVisitor visitor) {
    return visitor.traverse(this);
  }
  
  /**
   * Get the value of submission field
   * 
   * @return
   *    value
   */
  public abstract T getValue();
  
  /**
   * Parse the value from string format and convert to proper type for
   * submission field
   * 
   * @param value string form of the value
   * @throws ODKConversionException
 * @throws ODKDatastoreException 
   */
  public abstract void setValueFromString(String value) throws ODKConversionException, ODKDatastoreException;
  
  
  /**
   * Get submission field value from database entity
   *
   * @param database - from which to retrieve value
   * @param user - requesting the value
 * @throws ODKDatastoreException 
   */
  public abstract void getValueFromEntity(CallingContext cc)
  					throws ODKDatastoreException;
  
  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */  
  public abstract void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) throws ODKDatastoreException;
  
  @Override
  public final boolean isBinary() {
	  return (element.getFormDataModel().getElementType() == ElementType.REF_BLOB);
  }
  
  /**
   * Convert value from byte array to data store blob type. Store blob in blob
   * storage and save the key of the blob storage into submission set. There can
   * only be one un-named file. If a value for the unrootedFilePath already exists,
   * and if it is different than the supplied byte array, the existing value will
   * not be changed unless overwiteOK is true.
   * 
   * @param byteArray
   *          byte form of the value
   * @param contentType
   *          type of binary data (NOTE: only used for binary data)
   * @param unrootedFilePath
   *          the filename for this byte array
   * @param overwriteOK
   *          true if overwriting an existing value is OK.
   * @param cc
   *          calling context
   * @return the outcome of the storage attempt. md5 hashes are used to
   *         determine file equivalence.
   * @throws ODKDatastoreException
   * 
   */
  @Override
  public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray,
      String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc)
      throws ODKDatastoreException {
    if(isBinary()) {
      throw new IllegalStateException("Should be overridden in derived class");
    } else {
      throw new IllegalStateException(ErrorConsts.BINARY_ERROR);
    }
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionFieldBase<?>)) {
      return false;
    }
    
    SubmissionFieldBase<?> other = (SubmissionFieldBase<?>) obj;
    return (element == null ? (other.element == null) :
    	(other.element != null && element.equals(other.element)));    
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if(element != null) hashCode += element.hashCode();
    return hashCode; 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (element != null ? element.getElementName() : BasicConsts.EMPTY_STRING); 
  }
}
