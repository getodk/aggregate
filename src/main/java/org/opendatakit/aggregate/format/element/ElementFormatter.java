/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format.element;

import java.util.Date;
import java.util.List;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.aggregate.submission.type.jr.JRTemporal;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public interface ElementFormatter {
  void formatUid(String uri, String propertyName, Row row);

  void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException;

  void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row);

  void formatChoices(List<String> choices, FormElementModel element, String ordinalValue, Row row);

  void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row);

  void formatDecimal(WrappedBigDecimal dub, FormElementModel element, String ordinalValue, Row row);

  void formatJRDate(JRTemporal value, FormElementModel element, String ordinalValue, Row row);

  void formatJRTime(JRTemporal value, FormElementModel element, String ordinalValue, Row row);

  void formatJRDateTime(JRTemporal value, FormElementModel element, String ordinalValue, Row row);

  void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row);

  void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row);

  void formatString(String string, FormElementModel element, String ordinalValue, Row row);

  void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException;

}
