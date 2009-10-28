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

package org.odk.aggregate.submission.type.jr;

import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.odk.aggregate.submission.type.DateSubmissionType;

/**
 * Data Storage Converter for Java Rosa Data Type
 *
 * @author wbrunette@gmail.com
 *
 */
public class JRDateType extends DateSubmissionType {

  /**
   * Constructor 
   * 
   * @param propertyName
   *    Name of submission element 
   */
  public JRDateType(String propertyName) {
    super(propertyName);
  }

  /**
   * Convert string value to date format
   */
  @Override
  public void setValueFromString(String value) {
    Date newDate = DateUtils.parseDate(value);
    setValue(newDate);
  }

}
