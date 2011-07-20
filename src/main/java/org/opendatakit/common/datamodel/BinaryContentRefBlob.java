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
 * {@link BinaryContent}, {@link BinaryContentRefBlob} and {@link RefBlob} for
 * each instance data field. The BinaryContentRefBlob table links
 * a particular BinaryContent with the specific sequence of the
 * blobs needed to reconstruct that binary content.
 * <p>
 * The handling of parts supports massive binary objects that are too big
 * to store as a single blob.  The blob parts aren't stored in this table
 * so that the full index of blob parts can be retrieved at once, without 
 * also requiring that all the blob parts be in memory at the same time.
 * This supports the future streaming of attachments.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class BinaryContentRefBlob extends DynamicAssociationBase {

	private static final DataField PART = new DataField("PART",DataField.DataType.INTEGER, false);
	
	public final DataField part;
	
	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	public BinaryContentRefBlob(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
		fieldList.add(part = new DataField(PART));
	}

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private BinaryContentRefBlob(BinaryContentRefBlob ref, User user) {
		super(ref, user);
		part = ref.part;
	}

	// Only called from within the persistence layer.
	@Override
	public BinaryContentRefBlob getEmptyRow(User user) {
		return new BinaryContentRefBlob(this, user);
	}

	public Long getPart() {
		return getLongField(part);
	}
	
	public void setPart(Long value) {
		setLongField(part,value);
	}
}
