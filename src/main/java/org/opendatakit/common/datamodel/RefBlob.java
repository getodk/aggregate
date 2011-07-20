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
package org.opendatakit.common.datamodel;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.security.User;

/**
 * Binary objects may span multiple blobs; this class holds one 
 * blob in the sequence of blobs that comprise a binary object.
 * The class holds just the binary content.  
 * See {@link BinaryContentRefBlob} which defines the ordering
 * of Blobs within a binary object.
 * <p>
 * The intent is that this is a write-once record with put/get
 * semantics.  Its functionality could be replaced with S3, or 
 * other document storage services. 
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class RefBlob extends DynamicDocumentBase {

	private static final DataField VALUE = new DataField("VALUE",DataField.DataType.BINARY, false);
	public final DataField value;
	
	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	public RefBlob(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
		fieldList.add(value = new DataField(VALUE));
	}

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private RefBlob(RefBlob ref, User user) {
		super(ref, user);
		value = ref.value;
	}

	// Only called from within the persistence layer.
	@Override
	public RefBlob getEmptyRow(User user) {
		return new RefBlob(this, user);
	}

	public byte[] getValue() {
		return getBlobField(value);
	}
	
	public void setValue(byte[] blob) {
		setBlobField(value, blob);
	}
}
