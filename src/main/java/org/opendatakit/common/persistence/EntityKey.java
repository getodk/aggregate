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
 * The EntityKey class defines a key which corresponds to a specific Entity
 * in a specific relation.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class EntityKey {

	/**
	 * The relation to which the Entity belongs.
	 */
	private CommonFieldsBase relation;

	/**
	 * The key which corresponds to the Entity the EntityKey refers to.
	 */
	private String key;

	/**
	 * Construct an EntityKey using the given relation name and key.
	 * 
	 * @param relationName
	 *            the name of the relation to which the Entity belongs
	 * @param key
	 *            the key which corresponds to the Entity
	 */
	public EntityKey(CommonFieldsBase b, String key) {
		this.relation = b;
		this.key = key;
	}

	/**
	 * Returns the relation which this EntityKey refers to.
	 * 
	 * @return a String representing the relation name which this EntityKey
	 *         refers to
	 */
	public CommonFieldsBase getRelation() {
		return relation;
	}
	/**
	 * Returns the key which this EntityKey refers to.
	 * 
	 * @return a String representing the key which this EntityKey refers to
	 */
	public String getKey() {
		return key;
	}

	public int compareTo(EntityKey key2) {
		return key.compareTo(key2.key);
	}

}
