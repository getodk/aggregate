/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.ermodel;


/**
 * There are 3 types of table namespaces in Aggregate. These are managed by
 * defining each such that they have unique prefixes that cannot collide.
 * 
 * <ul>
 * <li>Submissions - the form submissions uploaded from ODK Collect. These all
 * start with an alphabetic character.</li>
 * <li>Internals - the tables used by Aggregate to maintain its state. These all
 * start with a single leading underscore.</li>
 * <li>Extension - tables added by others to extend aggregate. These all start
 * with two leading underscores. Three if they are 'static' extension tables.
 * See {@link Relation}.</li>
 * </ul>
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public enum TableNamespace {
  SUBMISSIONS, INTERNALS, EXTENSION;
}
