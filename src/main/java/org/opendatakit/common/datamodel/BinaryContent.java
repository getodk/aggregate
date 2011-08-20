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
 * Binary content for a given field in a form is held in a set of tables
 * {@link BinaryContent}, 
 * {@link BinaryContentRefBlob} and {@link RefBlob} for
 * each instance data field. The BinaryContent table enumerates the original
 * list of attachments (files) for a form.  The table can hold multiple attachments
 * for a given form element through the use of the ordinal number, much like the
 * SelectChoice table.  In fact, the BinaryContent table is linked
 * back to the form in the same way the SelectChoice table is -- through the 
 * parent AURI and the top level AURI fields.  
 * <p>
 * The BinaryContent table holds the unrooted file path for the attachment, 
 * which may be null.  If this is just a placeholder
 * for an attachment, but the attachment has not yet been inserted into the 
 * database, the content type, length and hash will be null.  Otherwise, these
 * will have values describing the attachment.    
 * <p>
 * The intent is that this is a write-twice record.  Written once to create the
 * placeholder for the attachment, and written a second time to update the content
 * information of the attachment.  See {@link BinaryContentManipulator} for 
 * methods to manipulate and maintain this abstraction.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class BinaryContent extends DynamicBase {
	private static final DataField UNROOTED_FILE_PATH = new DataField(
			"UNROOTED_FILE_PATH", DataField.DataType.STRING, true, 4096L);
	private static final DataField CONTENT_TYPE = new DataField("CONTENT_TYPE",
			DataField.DataType.STRING, true, 80L);
	private static final DataField CONTENT_LENGTH = new DataField("CONTENT_LENGTH",
			DataField.DataType.INTEGER, true);
	private static final DataField CONTENT_HASH = new DataField("CONTENT_HASH", 
			DataField.DataType.STRING, true);

	public final DataField unrootedFilePath;
	public final DataField contentType;
	public final DataField contentLength;
	public final DataField contentHash;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	public BinaryContent(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
		fieldList.add(unrootedFilePath = new DataField(UNROOTED_FILE_PATH));
		fieldList.add(contentType = new DataField(CONTENT_TYPE));
		fieldList.add(contentLength = new DataField(CONTENT_LENGTH));
		fieldList.add(contentHash = new DataField(CONTENT_HASH));
	}

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private BinaryContent(BinaryContent ref, User user) {
		super(ref, user);
		unrootedFilePath = ref.unrootedFilePath;
		contentType = ref.contentType;
		contentLength = ref.contentLength;
		contentHash = ref.contentHash;
	}

	// Only called from within the persistence layer.
	@Override
	public BinaryContent getEmptyRow(User user) {
		return new BinaryContent(this, user);
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

	public String getContentHash() {
		return getStringField(contentHash);
	}

	public void setContentHash(String value) {
		if ( !setStringField(contentHash, value)) {
			throw new IllegalStateException("overflow on contentHash");
		}
	}
}
