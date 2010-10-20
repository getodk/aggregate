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
 * Binary objects may span multiple blobs; this class holds one 
 * blob in the sequence of blobs that comprise a binary object.
 * The class holds just the binary content.  
 * See {@link VersionedBinaryContentRefBlob} which defines the ordering
 * of Blobs within a versioned binary object.
 * <p>
 * The intent is that this is a write-once record with put/get
 * semantics.  Its functionality could be replaced with S3, or 
 * other document storage services. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class RefBlob extends CommonFieldsBase {

	private static final DataField VALUE = new DataField("VALUE",DataField.DataType.BINARY, false);
	public final DataField value;
	
	public RefBlob(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC_DOCUMENT);
		fieldList.add(value = new DataField(VALUE));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)}   
	 * This does not populate any fields related to the values of this row. 
	 *
	 * @param d
	 */
	public RefBlob(RefBlob ref) {
		super(ref);
		value = ref.value;
	}

	public byte[] getValue() {
		return getBlobField(value);
	}
	
	public void setValue(byte[] blob) {
		setBlobField(value, blob);
	}
}
