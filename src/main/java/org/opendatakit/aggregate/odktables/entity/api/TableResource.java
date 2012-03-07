package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.opendatakit.aggregate.odktables.entity.TableEntry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("tableResource")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class TableResource extends TableEntry {

  @XStreamAsAttribute
  private String selfUri;

  @XStreamAsAttribute
  private String columnsUri;

  @XStreamAsAttribute
  private String dataUri;

  @XStreamAsAttribute
  private String diffUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataEtag());
  }

}