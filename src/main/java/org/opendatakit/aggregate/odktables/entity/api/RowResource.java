package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.opendatakit.aggregate.odktables.entity.Row;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class RowResource extends Row {

  private String selfUri;

  private String tableUri;

  public RowResource(Row row) {
    super();
    setRowId(row.getRowId());
    setRowEtag(row.getRowEtag());
    setGroupOrUserId(row.getGroupOrUserId());
    setDeleted(row.isDeleted());
    setValues(row.getValues());
  }

}