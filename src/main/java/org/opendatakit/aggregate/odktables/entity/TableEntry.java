package org.opendatakit.aggregate.odktables.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("tableEntry")
@AllArgsConstructor
@Data
public class TableEntry {
  
  @XStreamAlias("tableId")
  private String tableId;

  @XStreamAlias("dataEtag")
  private String dataEtag;

}