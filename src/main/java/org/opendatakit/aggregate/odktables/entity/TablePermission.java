package org.opendatakit.aggregate.odktables.entity;

public enum TablePermission {
  READ_ROW, WRITE_ROW, DELETE_ROW,
  FILTERED_ROWS,
  READ_PROPERTIES, WRITE_PROPERTIES,
  READ_ACL, WRITE_ACL;
}
