package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opendatakit.aggregate.odktables.entity.Row;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rowResource")
@EqualsAndHashCode(callSuper = true)
@Data
public class RowResource extends Row {

  @XStreamAlias("self")
  private String selfUri;

  @XStreamAlias("table")
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