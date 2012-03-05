package org.opendatakit.aggregate.odktables.api.entity;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.opendatakit.aggregate.odktables.entity.TableEntry;

@XmlRootElement
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class TableResource extends TableEntry {

  private String selfUri;
  private String columnsUri;
  private String dataUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataEtag());
  }
}