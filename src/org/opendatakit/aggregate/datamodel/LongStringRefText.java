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

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.security.User;

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
 * @author wbrunette@gmail.com
 * 
 */
public final class LongStringRefText extends DynamicAssociationBase {

	private static final DataField URI_FORM_DATA_MODEL = new DataField("URI_FORM_DATA_MODEL",DataField.DataType.URI, false);
	private static final DataField PART = new DataField("PART",DataField.DataType.INTEGER, false);
	public final DataField part;
	public final DataField uriFormDataModel;
	
	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	public LongStringRefText(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
		fieldList.add(part = new DataField(PART));
		fieldList.add(uriFormDataModel = new DataField(URI_FORM_DATA_MODEL));
	}

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private LongStringRefText(LongStringRefText ref, User user) {
		super(ref, user);
		part = ref.part;
		uriFormDataModel = ref.uriFormDataModel;
	}

	// Only called from within the persistence layer.
	@Override
	public LongStringRefText getEmptyRow(User user) {
		return new LongStringRefText(this, user);
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
