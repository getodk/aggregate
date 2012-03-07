package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opendatakit.aggregate.odktables.entity.TableEntry;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("tableResource")
@EqualsAndHashCode(callSuper = true)
@Data
public class TableResource extends TableEntry {

  @XStreamAlias("self")
  private String selfUri;

  @XStreamAlias("columns")
  private String columnsUri;

  @XStreamAlias("data")
  private String dataUri;

  @XStreamAlias("diff")
  private String diffUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataEtag());
  }

}