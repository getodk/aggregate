package org.opendatakit.aggregate.odktables.entity.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.opendatakit.aggregate.odktables.entity.Row;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rowResource")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class RowResource extends Row {

  @XStreamAsAttribute
  private String selfUri;

  @XStreamAsAttribute
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