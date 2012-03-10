package org.opendatakit.aggregate.odktables.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD, required = false)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TableEntry {
  
  private String tableId;
  private String dataEtag;

}