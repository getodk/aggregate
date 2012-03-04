package org.opendatakit.aggregate.odktables.entity;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement
@Data
public class Row {
  private String rowId;
  private String rowEtag;
  private String groupOrUserId;
  private boolean deleted;
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
    row.setRowId(rowId);
    row.setGroupOrUserId(groupOrUserId);
    row.setValues(values);
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
    row.setRowId(rowId);
    row.setRowEtag(rowEtag);
    row.setValues(values);
    return row;
  }
}