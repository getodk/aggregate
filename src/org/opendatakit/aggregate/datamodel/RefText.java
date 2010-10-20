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
 * Long strings may span multiple texts; this class holds one 
 * text in the sequence of texts that comprise a long string.
 * The class holds just the textual content.  
 * See {@link LongStringRefText} which defines the ordering
 * of Texts within a long string.
 * <p>
 * The intent is that this is a write-once record with put/get
 * semantics.  Its functionality could be replaced with S3, or 
 * other document storage services. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class RefText extends CommonFieldsBase {

	private static final DataField VALUE = new DataField("VALUE",DataField.DataType.LONG_STRING, false);
	public final DataField value;
	
	public RefText(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC_DOCUMENT);
		fieldList.add(value = new DataField(VALUE));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)}   
	 * This does not populate any fields related to the values of this row. 
	 *
	 * @param d
	 */
	public RefText(RefText ref) {
		super(ref);
		value = ref.value;
	}

	public String getValue() {
		return getStringField(value);
	}
	
	public void setValue(String str) {
		setStringField(value, str);
	}
}
