/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission.type;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.odk.aggregate.exception.ODKConversionException;

import com.google.gson.JsonObject;

/**
 * Data Storage Converter for Data Type
 *
 * @author wbrunette@gmail.com
 *
 */
public class DateSubmissionType extends SubmissionSingleValueBase<Date> {
  /**
   * Constructor 
   * 
   * @param propertyName
   *    Name of submission element 
   */
  public DateSubmissionType(String propertyName) {
    super(propertyName, false);
  }

  /**
   * Parse the value from string format and convert to Date
   *
   * @param value string form of the value
   * @throws ODKConversionException
   */
  @Override
  public void setValueFromString(String value) throws ODKConversionException {
    // TODO: deal with locale information
    DateFormat date = DateFormat.getDateInstance();
    try {
      Date newDate = date.parse(value);

      setValue(newDate);
    } catch (ParseException e) {
      throw new ODKConversionException(e);
    }
  }

  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */  
  @Override
  public void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames) {
    if(!propertyNames.contains(propertyName)){
      return;
    }
    if(getValue() != null) {
      jsonObject.addProperty(propertyName, getValue().toString());
    }
  }
  
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DateSubmissionType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }

}
