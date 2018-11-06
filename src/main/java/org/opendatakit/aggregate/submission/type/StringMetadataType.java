/*
 * Copyright (C) 2011 University of Washington.
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

public class StringMetadataType extends MetadataBaseType<String> {

  public StringMetadataType(DynamicCommonFieldsBase backingObject,
                            FormElementModel metadataType, DataField field) {
    super(backingObject, metadataType, field);
  }

  @Override
  public String getValue() {
    return backingObject.getStringField(field);
  }

  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row,
                          String ordinalValue, CallingContext cc) {
    elemFormatter.formatString(getValue(), metadataType, ordinalValue, row);
  }

}
