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

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.web.CallingContext;

/**
 * Data Storage Converter for String Type
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class StringSubmissionType extends SubmissionSingleValueBase<String> {
  public StringSubmissionType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
    super(backingObject, element);
  }

  @Override
  public void setValueFromString(String value) {
    setValue(value);
  }

  @Override
  public void getValueFromEntity(CallingContext cc) {
    String value = backingObject.getStringField(element.getFormDataModel().getBackingKey());
    setValue(value);
  }

  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) {
    elemFormatter.formatString(getValue(), element, ordinalValue, row);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StringSubmissionType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }

  @Override
  public String getValue() {
    return backingObject.getStringField(element.getFormDataModel().getBackingKey());
  }

  protected void setValue(String value) {
    DataField f = element.getFormDataModel().getBackingKey();
    if (value != null && value.length() > f.getMaxCharLen().intValue()) {
      value = value.substring(0, f.getMaxCharLen().intValue());
    }
    if (!backingObject.setStringField(f, value)) {
      throw new IllegalStateException("this should already be truncated");
    }
  }

}
