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

import java.util.List;

import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;


/**
 * Base class for type conversion
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 * @param <T>
 *  a GAE datastore type
 * 
 */
public abstract class SubmissionSingleValueBase<T> extends SubmissionFieldBase<T> {

  /**
   * Backing object holding the value of the submission field
   */
  protected final DynamicCommonFieldsBase backingObject;

  /**
   * Constructor
   */
  public SubmissionSingleValueBase(DynamicCommonFieldsBase backingObject, FormElementModel element) {
    super(element);
    this.backingObject = backingObject;
  }
    
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionSingleValueBase<?>)) {
      return false;
    }
    
    SubmissionSingleValueBase<?> other = (SubmissionSingleValueBase<?>) obj;
    
    return super.equals(obj) && (backingObject == other.backingObject);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + backingObject.hashCode(); 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
	T value = getValue();
    return super.toString() + FormatConsts.TO_STRING_DELIMITER 
      + (value != null ? value.toString() : BasicConsts.EMPTY_STRING);
  }

  @Override
  public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc) {
  }
  
  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException {
  }
}
