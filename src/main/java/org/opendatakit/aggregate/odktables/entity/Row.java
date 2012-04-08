package org.opendatakit.aggregate.odktables.entity;

import java.util.Map;

import lombok.Data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root
@Data
public class Row {

  @Element(name = "id", required = false)
  private String rowId;

  @Element(name = "etag", required = false)
  private String rowEtag;

  @Element(required = false)
  private String groupOrUserId;

  @Element(required = false)
  private boolean deleted;

  @ElementMap(entry = "entry", key = "column", attribute = true, inline = true)
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