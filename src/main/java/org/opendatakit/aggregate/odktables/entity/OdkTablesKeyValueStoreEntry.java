/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This is a simple struct-like object that will hold the rows from the key
 * value store. It is meant to be parsed into JSON objects to passed to the
 * phone. So this can be thought of as information ODKTables on the phone needs
 * to know about an entry in the key value store.
 * <p>
 * For a more in-depth explanation of all these fields, see the
 * KeyValueStoreManager.java class in ODK Tables.
 *
 * @author sudar.sam@gmail.com
 *
 */
@Root(strict = false)
public class OdkTablesKeyValueStoreEntry {

  /**
   * The table id of the table to which this entry belongs.
   */
  @Element(required = true)
  public String tableId;

  /**
   * The partition in the key value store to which the entry belongs. For an in
   * depth example see KeyValueStoreManager.java in the ODK Tables project.
   * Otherwise, just know that it is essentially the identifier of the class
   * that is responsible for managing the entry. ListView.java would have (by
   * convention) a partition name ListView. TableProperties and ColumnProperties
   * are the exception, belonging simply to the partitions "Table" and "Column".
   */
  @Element(required = false)
  public String partition;

  /**
   * The aspect is essentially the scope, or the instance of the partition, to
   * which this key/entry belongs. For instance, a table-wide property would
   * have the aspect "default". A column's aspect would be its element key (ie
   * its unique column identifier for the table). A particular saved graph view
   * might have the display name of that graph.
   */
  @Element(required = false)
  public String aspect;

  /**
   * The key of this entry. This is important so that ODKTables knows what to do
   * with this entry. Eg a key of "list" might mean that this entry is important
   * to the list view of the table.
   */
  @Element(required = false)
  public String key;

  /**
   * The type of this entry. This is important to taht ODKTables knows how to
   * interpret the value of this entry. Eg type String means that the value
   * holds a string. Type file means that the value is a JSON object holding a
   * FileManifestEntry object with information relating to the version of the
   * file and how to get it.
   */
  @Element(required = false)
  public String type;

  /**
   * The actual value of this entry. If the type is String, this is a string. If
   * it is a File, it is a FileManifestEntry JSON object.
   */
  @Element(required = false)
  public String value;

  @Override
  public String toString() {
    return "[tableId=" + tableId + ", partition=" + partition + ", aspect=" + aspect + ", key="
        + key + ", type=" + type + ", value=" + value + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((partition == null) ? 0 : partition.hashCode());
    result = prime * result + ((aspect == null) ? 0 : aspect.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if (!(obj instanceof OdkTablesKeyValueStoreEntry)) {
      return false;
    }
    OdkTablesKeyValueStoreEntry other = (OdkTablesKeyValueStoreEntry) obj;
    return (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (partition == null ? other.partition == null : partition.equals(other.partition))
        && (aspect == null ? other.aspect == null : aspect.equals(other.aspect))
        && (key == null ? other.key == null : key.equals(other.key))
        && (type == null ? other.type == null : type.equals(other.type))
        && (value == null ? other.value == null : value.equals(other.value));
  }

}
