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
package org.opendatakit.common.persistence;

/**
 * All instance data for an xform is stored in InstanceData tables
 * derived from this base class that are specific to that xform.
 * <p>
 * This common base class can be used by internal tables that should
 * be xform-like to share much of the processing of xform tables.
 * E.g., Xform tables can be viewed through the web interface.
 * <p>
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class InstanceDataBase extends CommonFieldsBase {
	
	public static final int WELL_KNOWN_COLUMN_COUNT = 7;
	
	public InstanceDataBase(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC);
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)}   
	 * This does not populate any fields related to the values of this row. 
	 *
	 * @param d
	 */
	protected InstanceDataBase(InstanceDataBase ref) {
		super(ref);
	}
}
