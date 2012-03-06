package org.opendatakit.aggregate.odktables.api.entity;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.opendatakit.aggregate.odktables.entity.Row;

@XmlRootElement
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RowResource extends Row {

  private String selfUri;
  private String tableUri;
  
  public RowResource(Row row)
  {
    super();
    setRowId(row.getRowId());
    setRowEtag(row.getRowEtag());
    setGroupOrUserId(row.getGroupOrUserId());
    setDeleted(row.isDeleted());
    setValues(row.getValues());
  }

}
