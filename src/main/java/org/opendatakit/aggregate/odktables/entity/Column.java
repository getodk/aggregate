package org.opendatakit.aggregate.odktables.entity;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Column {

  private String name;
  private ColumnType type;

  public enum ColumnType {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATETIME,
  }

}
