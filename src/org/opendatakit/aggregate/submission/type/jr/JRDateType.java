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

package org.opendatakit.aggregate.submission.type.jr;

import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.opendatakit.aggregate.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.submission.type.DateSubmissionType;

/**
 * Data Storage Converter for Java Rosa Data Type
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class JRDateType extends DateSubmissionType {

  /**
   * Constructor 
   * 
   * @param propertyName
   *    Name of submission element 
   */
  public JRDateType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
    super(backingObject, element);
  }

  /**
   * Convert string value to date format
   */
  @Override
  public void setValueFromString(String value) {
	  if ( value == null ) {
		  setValue(null);
	  } else {
		  Date newDate = DateUtils.parseDate(value);
		  setValue(newDate);
	  }
  }

}
