/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.datamodel;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;

/**
 * Long strings that exceed the maximum string length of the column 
 * allocated in the underlying persistence layer are tracked in a 
 * single xform-wide table represented by the LongStringText class.
 * This table tracks all strings in the form instance and splits them 
 * into multiple text storage objects tracked by the {@link RefText} class.
 * <p>
 * The handling of parts supports massive strings that are too big
 * to store as a single text.  Unlike blobs, long strings are not 
 * versioned.  Note that the xform definition is stored as a text/html
 * blob, not as text, so it can be versioned.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class LongStringRefText extends CommonFieldsBase {

	private static final DataField URI_FORM_DATA_MODEL = new DataField("URI_FORM_DATA_MODEL",DataField.DataType.URI, false);
	private static final DataField PART = new DataField("PART",DataField.DataType.INTEGER, false);
	public final DataField part;
	public final DataField uriFormDataModel;
	
	public LongStringRefText(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC_ASSOCIATION);
		fieldList.add(part = new DataField(PART));
		fieldList.add(uriFormDataModel = new DataField(URI_FORM_DATA_MODEL));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)}   
	 * This does not populate any fields related to the values of this row. 
	 *
	 * @param d
	 */
	public LongStringRefText(LongStringRefText ref) {
		super(ref);
		part = ref.part;
		uriFormDataModel = ref.uriFormDataModel;
	}

	public Long getPart() {
		return getLongField(part);
	}

	public void setPart(Long value) {
		setLongField(part, value);
	}

	public String getUriFormDataModel() {
		return getStringField(uriFormDataModel);
	}

	public void setUriFormDataModel(String uri) {
		if ( !setStringField(uriFormDataModel, uri) ) {
			throw new IllegalArgumentException("overflow uriFormDataModel");
		}
	}
}
