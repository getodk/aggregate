/**
 * Copyright (C) 2010 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.parser;

/**
 * Simple data structure used in {@link NamingSet} to resolve table and column names.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
final class Name {
  final String schema;
  final String prefix;
  final String qualifier;
  final String itemName;

  String mungedPrefix = "";
  String mungedQualifier = "";
  String mungedItemName = "";

  String resolvedName = null;

  Name(String schema, String prefix, String qualifier, String itemName) {
    this.schema = schema;
    this.prefix = Naming.toPersistenceNaming(prefix);
    this.qualifier = Naming.toPersistenceNaming(qualifier);
    this.itemName = Naming.toPersistenceNaming(itemName);
  }
}