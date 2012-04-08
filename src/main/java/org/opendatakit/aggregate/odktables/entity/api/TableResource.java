package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD, required = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Data
public class TableResource extends TableEntry {

  private String selfUri;

  private String columnsUri;

  private String dataUri;

  private String diffUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataEtag());
  }

}