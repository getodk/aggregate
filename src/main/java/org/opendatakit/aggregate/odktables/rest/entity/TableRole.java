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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum TableRole {
  NONE("No permissions. Can not see that the table exists."),

  FILTERED_READER("Can read properties and read filtered data .", TablePermission.READ_TABLE_ENTRY,
      TablePermission.READ_ROW, TablePermission.READ_PROPERTIES),

  FILTERED_WRITER("Can read properties and read/write/delete filtered data.",
      TablePermission.READ_TABLE_ENTRY, TablePermission.READ_ROW, TablePermission.WRITE_ROW,
      TablePermission.DELETE_ROW, TablePermission.READ_PROPERTIES),

  UNFILTERED_READER_FILTERED_WRITER(
      "Can read properties, read all data, and write/delete filtered data.",
      TablePermission.READ_TABLE_ENTRY, TablePermission.READ_ROW, TablePermission.WRITE_ROW,
      TablePermission.DELETE_ROW, TablePermission.UNFILTERED_READ, TablePermission.READ_PROPERTIES),

  READER("Can read properties and all data.", TablePermission.READ_TABLE_ENTRY,
      TablePermission.READ_ROW, TablePermission.UNFILTERED_READ, TablePermission.READ_PROPERTIES),

  WRITER("Can read properties and read/write/delete all data.", TablePermission.READ_TABLE_ENTRY,
      TablePermission.READ_ROW, TablePermission.WRITE_ROW, TablePermission.DELETE_ROW,
      TablePermission.UNFILTERED_READ, TablePermission.UNFILTERED_WRITE,
      TablePermission.UNFILTERED_DELETE, TablePermission.READ_PROPERTIES),

  OWNER(
      "All permissions. Can delete table, read/write properties, read/write/delete all data, and read/write/delete access control lists.",
      TablePermission.values());

  private final String description;
  private final List<TablePermission> permissions;

  TableRole(String description, TablePermission... permissions) {
    this.description = description;
    if (permissions == null) {
      throw new NullPointerException("empty permissions list");
    }
    ArrayList<TablePermission> list = new ArrayList<TablePermission>(permissions.length);
    Collections.addAll(list, permissions);
    this.permissions = list;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the permissions
   */
  public List<TablePermission> getPermissions() {
    return Collections.unmodifiableList(permissions);
  }

  /**
   *
   * @param permission
   * @return true if this role has the given permission
   */
  public boolean hasPermission(TablePermission permission) {
    return permissions.contains(permission);
  }

  public enum TablePermission {
    READ_TABLE_ENTRY, DELETE_TABLE, READ_ROW, WRITE_ROW, DELETE_ROW, UNFILTERED_READ, UNFILTERED_WRITE, UNFILTERED_DELETE, READ_PROPERTIES, WRITE_PROPERTIES, READ_ACL, WRITE_ACL, DELETE_ACL,
  }

}
