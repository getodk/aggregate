package org.opendatakit.aggregate.odktables.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Column {

  @Attribute(required = true)
  private String name;

  @Attribute(required = true)
  private ColumnType type;

  public enum ColumnType {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATETIME;
  }
}