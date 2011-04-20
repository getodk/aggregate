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

import java.util.Date;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Data Storage Converter for Data Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class DateSubmissionType extends SubmissionSingleValueBase<Date> {
	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            Name of submission element
	 */
	protected DateSubmissionType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
		super(backingObject, element);
	}

	/**
	 * Parse the value from string format and convert to Date
	 * 
	 * @param value
	 *            string form of the value
	 * @throws ODKConversionException
	 */
	@Override
	public void setValueFromString(String value) throws ODKConversionException {
		Date d;
		try {
			d = WebUtils.parseDate(value);
		} catch( IllegalArgumentException e ) {
			throw new ODKConversionException(e);
		}
		setValue(d);
	}
	
	/**
	 * Helper for updating dates in predefined forms...
	 * 
	 * @param date
	 */
	public void setValueFromDate(Date date) {
		setValue(date);
	}
	
	@Override
	public void getValueFromEntity(CallingContext cc) {
		Date value = backingObject.getDateField(element.getFormDataModel().getBackingKey());
		setValue(value);
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

	@Override
	public Date getValue() {
		return backingObject.getDateField(element.getFormDataModel().getBackingKey());
	}

	/**
	 * Set the value of submission field
	 * 
	 * @param value
	 *            value to set
	 */
	protected void setValue(Date value) {
		backingObject.setDateField(element.getFormDataModel().getBackingKey(), (Date) value);
	}

}
