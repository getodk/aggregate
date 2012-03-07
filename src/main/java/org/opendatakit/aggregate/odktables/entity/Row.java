package org.opendatakit.aggregate.odktables.entity;

import java.util.Map;

import org.opendatakit.aggregate.odktables.entity.serialization.MapConverter;

import lombok.Data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("row")
@Data
public class Row {

  @XStreamAlias("rowId")
  private String rowId;

  @XStreamAlias("rowEtag")
  private String rowEtag;

  @XStreamAlias("groupOrUserId")
  private String groupOrUserId;

  @XStreamAlias("deleted")
  private boolean deleted;

  @XStreamConverter(MapConverter.class)
  private Map<String, String> values;

  /**
   * Construct a row for insertion.
   * 
   * @param rowId
   * @param groupOrUserId
   * @param values
   */
  public static Row forInsert(String rowId, String groupOrUserId, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.groupOrUserId = groupOrUserId;
    row.values = values;
    return row;
  }

  /**
   * Construct a row for updating.
   * 
   * @param rowId
   * @param values
   */
  public static Row forUpdate(String rowId, String rowEtag, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.rowEtag = rowEtag;
    row.values = values;
    return row;
  }
}