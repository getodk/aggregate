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
 * Binary content for a given field in a form is held in a set of tables
 * {@link BinaryContent}, {@link VersionedBinaryContent}, 
 * {@link VersionedBinaryContentRefBlob} and {@link RefBlob} for
 * each instance data field. The BinaryContent table enumerates the original
 * list of attachments (files) for a form and the version to be applied for that 
 * attachment.  The version may be null, in which case the original attachment
 * is no longer needed.  The ordinal, however, remains in use.  BinaryContent is 
 * extremely similar to the SelectChoice table, but always has a subordinate
 * {@link VersionedBinaryContent} table.  The BinaryContent table holds the 
 * filename for the attachment (if any), the ordinal number for that attachment
 * (to uniquely distinguish unnamed files), and the version in force for that 
 * attachment (or null if the attachment is no longer applicable).  
 * <p>
 * The VersionedBinaryContent table holds the information about a specific version
 * of an attachment (version, content type, content length, binary data).
 * <p>
 * A version is generated whenever the binary object is updated.
 * <p>
 * Versioning is unique to binary objects as it is likely that updates to 
 * the media associated with a form will occur, and that the xform 
 * definition itself my change to support revisions to the text, ordering
 * or additional language translations. 
 * <p>
 * The intent is that this is a write-once record with version history.
 * Version is recorded as a UUID (URI) in the {@link BinaryContent} and 
 * {@link VersionedBinaryContent} tables.  VersionedBinaryContent records
 * and BinaryContent records are never destroyed.  VersionedBinaryContent
 * records are never updated, but Binary Content records are.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public final class BinaryContent extends CommonFieldsBase {
	private static final DataField VERSION = new DataField("VERSION",
			DataField.DataType.URI, false);
	private static final DataField UNROOTED_FILE_PATH = new DataField(
			"UNROOTED_FILE_PATH", DataField.DataType.STRING, true, 4096L);

	public final DataField version;
	public final DataField unrootedFilePath;

	public BinaryContent(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC);
		fieldList.add(version = new DataField(VERSION));
		fieldList.add(unrootedFilePath = new DataField(UNROOTED_FILE_PATH));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)} This does not
	 * populate any fields related to the values of this row.
	 * 
	 * @param d
	 */
	public BinaryContent(BinaryContent ref) {
		super(ref);
		version = ref.version;
		unrootedFilePath = ref.unrootedFilePath;
	}

	public String getVersion() {
		return getStringField(version);
	}

	public void setVersion(String value) {
		if (!setStringField(version, value)) {
			throw new IllegalStateException("overflow on version");
		}
	}

	public String getUnrootedFilePath() {
		return getStringField(unrootedFilePath);
	}

	public void setUnrootedFilePath(String value) {
		// allow this to overflow
		if (!setStringField(unrootedFilePath, value)) {
			throw new IllegalStateException("overflow on unrootedFilePath");
		}
	}
}
