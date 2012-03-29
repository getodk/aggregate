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

import java.math.BigDecimal;
import java.util.List;

import org.opendatakit.aggregate.constants.common.GeoPointConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Geopoints appear as a single complex-valued field to their callers.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class GeoPointSubmissionType extends SubmissionSingleValueBase<GeoPoint> {

	private GeoPoint coordinates;

	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            Name of submission element
	 */
	public GeoPointSubmissionType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
		super(backingObject, element);
	}

	@Override
	public void setValueFromString(String value) throws ODKConversionException {
		if ( value == null ) {
			coordinates = new GeoPoint();
		} else {
			String[] values = value.split("\\s+");
			if (values.length == 2) {
				coordinates = new GeoPoint( new BigDecimal(values[0]), 
											new BigDecimal(values[1]));
			} else if (values.length == 3) {
				coordinates = new GeoPoint( new BigDecimal(values[0]), 
											new BigDecimal(values[1]),
											new BigDecimal(values[2]));
			} else if (values.length == 4) {
				coordinates = new GeoPoint( new BigDecimal(values[0]), 
											new BigDecimal(values[1]),
											new BigDecimal(values[2]),
											new BigDecimal(values[3]));
			} else {
				throw new ODKConversionException(
						"Problem with GPS Coordinates being parsed from XML");
			}
		}
		for ( FormDataModel m : element.getFormDataModel().getChildren()) {
			switch ( m.getOrdinalNumber().intValue() ) {
			case GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER:
				backingObject.setNumericField(m.getBackingKey(), coordinates.getLatitude());
				break;
			case GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER:
				backingObject.setNumericField(m.getBackingKey(), coordinates.getLongitude());
				break;
			case GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER:
				backingObject.setNumericField(m.getBackingKey(), coordinates.getAltitude());
				break;
			case GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER:
				backingObject.setNumericField(m.getBackingKey(), coordinates.getAccuracy());
				break;
			}
		}
	}

	@Override
	public GeoPoint getValue() {
		return coordinates;
	}

	/**
	 * Format value for output
	 * 
	 * @param elemFormatter
	 *            the element formatter that will convert the value to the
	 *            proper format for output
	 */
	@Override
	public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc)
			throws ODKDatastoreException {
		elemFormatter.formatGeoPoint(coordinates, element, ordinalValue, row);
	}

	@Override
	public void getValueFromEntity(CallingContext cc) {
		BigDecimal latCoor = null;
		BigDecimal longCoor = null;
		BigDecimal altitude = null;
		BigDecimal accuracy = null;
		for ( FormDataModel m : element.getFormDataModel().getChildren()) {
			switch ( m.getOrdinalNumber().intValue() ) {
			case GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER:
				latCoor = backingObject.getNumericField(m.getBackingKey());
				break;
			case GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER:
				longCoor = backingObject.getNumericField(m.getBackingKey());
				break;
			case GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER:
				altitude = backingObject.getNumericField(m.getBackingKey());
				break;
			case GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER:
				accuracy = backingObject.getNumericField(m.getBackingKey());
				break;
			}
		}
		coordinates = new GeoPoint(latCoor, longCoor, altitude, accuracy);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GeoPointSubmissionType)) {
			return false;
		}
		// super will compare value
		if (!super.equals(obj)) {
			return false;
		}

		GeoPointSubmissionType other = (GeoPointSubmissionType) obj;
		return (coordinates == null ? (other.coordinates == null)
				: (other.coordinates != null && coordinates.equals(other.coordinates)));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode()
				+ (coordinates == null ? 0 : coordinates.hashCode());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + FormatConsts.TO_STRING_DELIMITER
				+ (getValue() != null ? getValue() : BasicConsts.EMPTY_STRING);
	}

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc) {
		// geopoint storage is handled by SubmissionSet
	}

	@Override
	public void persist(CallingContext cc) {
		// geopoint persistence is handled by SubmissionSet
	}

}
