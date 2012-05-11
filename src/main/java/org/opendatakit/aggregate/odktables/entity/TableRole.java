package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import com.google.common.collect.Lists;

public enum TableRole {
  NONE("No permissions. Can not see that the table exists."),
  
  FILTERED_READER("Can read properties and read filtered data .", 
      TablePermission.READ_TABLE_ENTRY,
      TablePermission.FILTERED_ROWS, 
      TablePermission.READ_ROW, 
      TablePermission.READ_PROPERTIES),
      
  FILTERED_WRITER("Can read properties and read/write filtered data.",
      TablePermission.READ_TABLE_ENTRY, 
      TablePermission.FILTERED_ROWS, 
      TablePermission.READ_ROW,
      TablePermission.WRITE_ROW, 
      TablePermission.DELETE_ROW, 
      TablePermission.READ_PROPERTIES),
      
  READER("Can read properties and all data.", 
      TablePermission.READ_TABLE_ENTRY,
      TablePermission.READ_ROW, 
      TablePermission.READ_PROPERTIES),
      
  WRITER("Can read properties and read/write all data.", 
      TablePermission.READ_TABLE_ENTRY,
      TablePermission.READ_ROW, 
      TablePermission.WRITE_ROW, 
      TablePermission.DELETE_ROW,
      TablePermission.READ_PROPERTIES),
      
  OWNER("All permissions. Can read/write properties, read/write all data, and read/write access control lists.",
      TablePermission.values());

  private final String description;
  private final List<TablePermission> permissions;

  TableRole(String description, TablePermission... permissions) {
    this.description = description;
    this.permissions = Lists.newArrayList(permissions);
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
    return permissions;
  }

  public enum TablePermission {
    READ_TABLE_ENTRY,
    READ_ROW,
    WRITE_ROW,
    DELETE_ROW,
    FILTERED_ROWS,
    READ_PROPERTIES,
    WRITE_PROPERTIES,
    READ_ACL,
    WRITE_ACL,
    DELETE_ACL,
  }
}
