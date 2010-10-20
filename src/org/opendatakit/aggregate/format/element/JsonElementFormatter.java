/*
 * Copyright (C) 2010 University of Washington
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

import com.google.appengine.repackaged.com.google.common.util.Base64;

public class JsonElementFormatter implements ElementFormatter {
  /**
   * separate the GPS coordinates of latitude and longitude into columns
   */
  private boolean separateCoordinates;

  /**
   * include GPS altitude data
   */
  private boolean includeAltitude;

  /**
   * include GPS accuracy data
   */
  private boolean includeAccuracy;

  /**
   * Datastore reference for retrieving needed data from the db
   */
  private final Datastore ds;

  private final User user;
  
  private final Realm realm;

  private FormDefinition formDefinition;
  
  private Submission submission;

  /**
   * Submission formatter that is using the element formatter. Needed for
   * recursive formatting of repeats
   */
  private SubmissionFormatter formatter;

  /**
   * Construct a JSON Element Formatter
   * 
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   * @param datastore
   *          Datastore reference for retrieving needed data from the db
   */
  public JsonElementFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
	      boolean includeGpsAccuracy, FormDefinition formDefinition, Datastore datastore, User user, Realm realm, SubmissionFormatter submissionFormatter) {
	this.formDefinition = formDefinition;
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
    ds = datastore;
    this.user = user;
    this.realm = realm;
  }

  public final Submission getSubmission() {
		return submission;
  }


  public final void setSubmission(Submission submission) {
		this.submission = submission;
  }
  

  @Override
  public void formatBinary(SubmissionKey key, String propertyName, Row row)
      throws ODKDatastoreException {
    if (key == null) {
      row.addFormattedValue(null);
      return;
    }

    byte[] imageBlob = null;
    List<SubmissionKeyPart> parts = SubmissionKeyPart.splitSubmissionKey(key);
	SubmissionValue v = submission.resolveSubmissionKey(parts);
	BlobSubmissionType b = (BlobSubmissionType) v;
	if ( b.getAttachmentCount() == 1 ) {
		String version = b.getCurrentVersion(1);
		imageBlob = b.getBlob(1, version);
	}
    if (imageBlob != null && imageBlob.length > 0) {
      addToJsonValueToRow(Base64.encode(imageBlob), propertyName, row);
    }

  }

  @Override
  public void formatBoolean(Boolean bool, String propertyName, Row row) {
    addToJsonValueToRow(bool, propertyName, row);

  }


	@Override

  public void formatChoices(List<String> choices, String propertyName, Row row) {
	StringBuilder b = new StringBuilder();

	boolean first = true;
	for ( String s : choices ) {
		if ( !first ) {
			b.append(" ");
		}
		first = false;
		b.append(s);
	}
	addToJsonValueToRow(b.toString(), propertyName, row);
  }

  @Override
  public void formatDate(Date date, String propertyName, Row row) {
    addToJsonValueToRow(date, propertyName, row);

  }

  @Override
  public void formatDecimal(BigDecimal dub, String propertyName, Row row) {
    addToJsonValueToRow(dub, propertyName, row);

  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, String propertyName, Row row) {
    if (separateCoordinates) {
      addToJsonValueToRow(coordinate.getLatitude(), propertyName + FormatConsts.HEADER_CONCAT
          + FormatConsts.LATITUDE, row);
      addToJsonValueToRow(coordinate.getLongitude(), propertyName + FormatConsts.HEADER_CONCAT
          + FormatConsts.LONGITUDE, row);

      if (includeAltitude) {
        addToJsonValueToRow(coordinate.getAltitude(), propertyName + FormatConsts.HEADER_CONCAT
            + FormatConsts.ALTITUDE, row);
      }

      if (includeAccuracy) {
        addToJsonValueToRow(coordinate.getAccuracy(), propertyName + FormatConsts.HEADER_CONCAT
            + FormatConsts.ACCURACY, row);
      }
    } else {
      if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
        String coordVal = coordinate.getLatitude().toString() + BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getLongitude().toString();
        if (includeAltitude) {
        	coordVal += BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getAltitude().toString();
        }
        if (includeAccuracy) {
        	coordVal += BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getAccuracy().toString();
        }
        row.addFormattedValue(coordVal);
      } else {
        row.addFormattedValue(null);
      }
    }

  }

  @Override
  public void formatLong(Long longInt, String propertyName, Row row) {
    addToJsonValueToRow(longInt, propertyName, row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, String propertyName, Row row) throws ODKDatastoreException {
    formatter.processRepeatedSubmssionSets(repeat.getElement(), repeat.getSubmissionSets());
  }

  @Override
  public void formatString(String string, String propertyName, Row row) {
    addToJsonValueToRow(string, propertyName, row);
  }

  private void addToJsonValueToRow(Object value, String propertyName, Row row) {
    StringBuilder jsonString = new StringBuilder();
    jsonString.append(BasicConsts.QUOTE);
    jsonString.append(propertyName);
    jsonString.append(BasicConsts.QUOTE + BasicConsts.COLON);

    if (value != null) {
      jsonString.append(BasicConsts.QUOTE);
      jsonString.append(value.toString());
      jsonString.append(BasicConsts.QUOTE);
    } else {
      jsonString.append(BasicConsts.EMPTY_STRING);
    }

    row.addFormattedValue(jsonString.toString());
  }
}
