/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington.
 * Copyright (C) 2018 Nafundi
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
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.type.SubmissionSingleValueBase;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.web.CallingContext;

public class JRDateTimeType extends SubmissionSingleValueBase<Date> {
  public JRDateTimeType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
    super(backingObject, element);
  }

  @Override
  public void setValueFromString(String value) {
    if (value == null) {
      setValue(null);
    } else {
      Date newDate = DateUtils.parseDateTime(value);
      setValue(newDate);
    }
  }

  @Override
  public void getValueFromEntity(CallingContext cc) {
    Date value = backingObject.getDateField(element.getFormDataModel().getBackingKey());
    setValue(value);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JRDateTimeType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }

  @Override
  public Date getValue() {
    return backingObject.getDateField(element.getFormDataModel().getBackingKey());
  }

  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) {
    elemFormatter.formatDateTime(getValue(), element, ordinalValue, row);
  }

  private void setValue(Date value) {
    backingObject.setDateField(element.getFormDataModel().getBackingKey(), (Date) value);
  }

}
