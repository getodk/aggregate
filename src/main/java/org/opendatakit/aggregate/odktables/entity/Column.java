package org.opendatakit.aggregate.odktables.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("column")
@AllArgsConstructor
@Data
public class Column {

  @XStreamAlias("name")
  private String name;

  @XStreamAlias("type")
  private ColumnType type;

  public enum ColumnType {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATETIME;
  }
}