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
 * each instance data field. The VersionedBinaryContent table captures
 * the particular characteristics for a particular version of a binary
 * content file (as identified by the filename in the BinaryContent table).
 * These characteristics are: content type, content length, and the binary
 * data itself (identified through the associated {@link VersionedBinaryContentRefBlob}
 * table.
 * <p>
 * Different versions may exist if, for example, we support uploads of 
 * lower-resolution files from the field, followed by uploads of higher-
 * resolution files once the device is back in the clinic.  Versions may
 * exist for binary content of form definitions, where we might update the
 * instructional video in a form or add translations to a form.
 * <p>
 * A version is generated whenever the binary object is updated. The uri of
 * the blobs in which this data is stored is held in the associated
 * {@link VersionedBinaryContentRefBlob} table.
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
public final class VersionedBinaryContent extends CommonFieldsBase {
	private static final DataField VERSION = new DataField("VERSION",
			DataField.DataType.URI, false);
	private static final DataField CONTENT_TYPE = new DataField("CONTENT_TYPE",
			DataField.DataType.STRING, false, 80L);
	private static final DataField CONTENT_LENGTH = new DataField("CONTENT_LENGTH",
			DataField.DataType.INTEGER, false);

	public final DataField version;
	public final DataField contentType;
	public final DataField contentLength;

	public VersionedBinaryContent(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC);
		fieldList.add(version = new DataField(VERSION));
		fieldList.add(contentType = new DataField(CONTENT_TYPE));
		fieldList.add(contentLength = new DataField(CONTENT_LENGTH));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)} This does not
	 * populate any fields related to the values of this row.
	 * 
	 * @param d
	 */
	public VersionedBinaryContent(VersionedBinaryContent ref) {
		super(ref);
		version = ref.version;
		contentType = ref.contentType;
		contentLength = ref.contentLength;
	}

	public String getVersion() {
		return getStringField(version);
	}

	public void setVersion(String value) {
		if (!setStringField(version, value)) {
			throw new IllegalStateException("overflow on version");
		}
	}

	public String getContentType() {
		return getStringField(contentType);
	}

	public void setContentType(String value) {
		if (!setStringField(contentType, value)) {
			throw new IllegalStateException("overflow on contentType");
		}
	}

	public Long getContentLength() {
		return getLongField(contentLength);
	}

	public void setContentLength(Long value) {
		setLongField(contentLength, value);
	}
}
