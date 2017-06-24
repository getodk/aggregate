/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Row {

  /**
   * PK identifying this row of data.
   */
  @JacksonXmlProperty(localName = "id")
  @JsonProperty(value = "id", required = false)
  private String rowId;

  /**
   * identifies this revision of this row of data.
   * (needed to support updates to data rows)
   * (creation is a revision from 'undefined').
   */
  @JsonProperty(required = false)
  private String rowETag;

  /**
   * identifies the service-level 
   * interaction during which this 
   * revision was made. Useful for 
   * finding coincident changes 
   * and prior/next changes.
   */
  @JsonProperty(required = false)
  private String dataETagAtModification;

  /**
   * deletion is itself a revision.
   */
  @JsonProperty(required = false)
  private boolean deleted;

  /**
   * audit field returned for 
   * archive/recovery tools.
   */
  @JsonProperty(required = false)
  private String createUser;

  /**
   * audit field returned for 
   * archive/recovery tools
   */
  @JsonProperty(required = false)
  private String lastUpdateUser;

  /**
   * OdkTables metadata column.
   *
   * The ODK Survey form that 
   * was used when revising this
   * row.
   *
   * This can be useful for 
   * implementing workflows.
   * I.e., if savepointTyp is
   * COMPLETE with this formId,
   * then enable editing with
   * this other formId.
   */
  @JsonProperty(required = false)
  private String formId;

  /**
   * OdkTables metadata column.
   *
   * The locale of the device 
   * that last revised this row.
   */
  @JsonProperty(required = false)
  private String locale;

  /**
   * OdkTables metadata column.
   *
   * One of either COMPLETE
   * or INCOMPLETE. COMPLETE
   * indicates that the formId
   * used to fill out the row
   * has validated the entered 
   * values.
   */
  @JsonProperty(required = false)
  private String savepointType;

  /**
   * OdkTables metadata column.
   *
   * For Mezuri, the timestamp
   * of this data value.
   *
   * For ODK Survey, the last
   * save time of the survey.
   *
   * For sensor data,
   * the timestamp for the 
   * reading in this row.
   */
  @JsonProperty(required = false)
  private String savepointTimestamp;

  /**
   * OdkTables metadata column.
   *
   * For ODK Survey, the user
   * that filled out the survey.
   *
   * Unclear what this would be 
   * for sensors.
   *
   * For Mezuri, this would be
   * the task execution ID that 
   * created the row.
   */
 @JsonProperty(required = false)
  private String savepointCreator;

  /**
   * RowFilterScope is passed down to device.
   *
   * Implements DEFAULT, MODIFY, READ_ONLY, HIDDEN
   * with rowOwner being the "owner" of the row.
   * 
   * It is passed down to the 
   * device so that the 
   * device can do best-effort
   * enforcement of access control
   * (trusted executor)
   */
  @JacksonXmlProperty(localName = "filterScope")
  @JsonProperty(value = "filterScope", required = false)
  private RowFilterScope rowFilterScope;

  /**
   * Array of user-defined column name to
   * the string representation of its value.
   * Sorted by ascending column name.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(localName="orderedColumns")
  @JacksonXmlProperty(localName="value")
  private ArrayList<DataKeyValue> orderedColumns;

  /**
   * Construct a row for insertion. This is used by the remote client (ODK
   * Tables) to construct a REST request to insert the row.
   *
   * @param rowId
   * @param values
   */
  public static Row forInsert(String rowId, String formId, String locale, String savepointType,
      String savepointTimestamp, String savepointCreator, RowFilterScope filterScope,
      ArrayList<DataKeyValue> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.formId = formId;
    row.locale = locale;
    row.savepointType = savepointType;
    row.savepointTimestamp = savepointTimestamp;
    row.savepointCreator = savepointCreator;
    row.rowFilterScope = filterScope;
    if ( values == null ) {
      row.orderedColumns = new ArrayList<DataKeyValue>();
    } else {
      Collections.sort(values, new Comparator<DataKeyValue>(){

        @Override
        public int compare(DataKeyValue arg0, DataKeyValue arg1) {
          return arg0.column.compareTo(arg1.column);
        }});

      row.orderedColumns = values;
    }
    return row;
  }

  /**
   * Construct a row for updating. This is used by the remote client (ODK
   * Tables) to construct a REST request to modify the row.
   *
   * @param rowId
   * @param rowETag
   * @param values
   */
  public static Row forUpdate(String rowId, String rowETag, String formId, String locale,
      String savepointType, String savepointTimestamp, String savepointCreator, RowFilterScope filterScope,
      ArrayList<DataKeyValue> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.rowETag = rowETag;
    row.formId = formId;
    row.locale = locale;
    row.savepointType = savepointType;
    row.savepointTimestamp = savepointTimestamp;
    row.savepointCreator = savepointCreator;
    row.rowFilterScope = filterScope;
    if ( values == null ) {
      row.orderedColumns = new ArrayList<DataKeyValue>();
    } else {
      Collections.sort(values, new Comparator<DataKeyValue>(){

        @Override
        public int compare(DataKeyValue arg0, DataKeyValue arg1) {
          return arg0.column.compareTo(arg1.column);
        }});

      row.orderedColumns = values;
    }
    return row;
  }

  public static final ArrayList<DataKeyValue> convertFromMap(Map<String,String> cvalues) {
    ArrayList<DataKeyValue> svalues = new ArrayList<DataKeyValue>();
    for ( String key : cvalues.keySet() ) {
      svalues.add(new DataKeyValue(key, cvalues.get(key)));
    }
    Collections.sort(svalues, new Comparator<DataKeyValue>(){

      @Override
      public int compare(DataKeyValue arg0, DataKeyValue arg1) {
        return arg0.column.compareTo(arg1.column);
      }});
    return svalues;
  }

  public static final HashMap<String,String> convertToMap(ArrayList<DataKeyValue> svalues) {
    HashMap<String,String> cvalues = new HashMap<String,String>();
    for ( DataKeyValue kv : svalues ) {
      cvalues.put(kv.column, kv.value);
    }
    return cvalues;
  }

  public Row() {
    this.rowId = null;
    this.rowETag = null;
    this.dataETagAtModification = null;
    this.deleted = false;
    this.createUser = null;
    this.lastUpdateUser = null;
    // data coming up from client
    this.formId = null;
    this.locale = null;
    this.savepointType = null;
    this.savepointTimestamp = null;
    this.savepointCreator = null;
    this.rowFilterScope = null;
    this.orderedColumns = new ArrayList<DataKeyValue>();
  }

  protected Row(Row r) {
    this.rowId = r.rowId;
    this.rowETag = r.rowETag;
    this.dataETagAtModification = r.dataETagAtModification;
    this.deleted = r.deleted;
    this.createUser = r.createUser;
    this.lastUpdateUser = r.lastUpdateUser;
    // data coming up from client
    this.formId = r.formId;
    this.locale = r.locale;
    this.savepointType = r.savepointType;
    this.savepointTimestamp = r.savepointTimestamp;
    this.savepointCreator = r.savepointCreator;
    this.rowFilterScope = r.rowFilterScope;
    this.orderedColumns = r.orderedColumns;
  }

  public String getRowId() {
    return this.rowId;
  }

  public String getRowETag() {
    return this.rowETag;
  }

  public String getDataETagAtModification() {
    return this.dataETagAtModification;
  }

  public boolean isDeleted() {
    return this.deleted;
  }

  public String getCreateUser() {
    return createUser;
  }

  public String getLastUpdateUser() {
    return lastUpdateUser;
  }

  public RowFilterScope getRowFilterScope() {
    return rowFilterScope;
  }

  public String getSavepointCreator() {
    return this.savepointCreator;
  }

  public String getFormId() {
    return this.formId;
  }

  public String getLocale() {
    return this.locale;
  }

  public String getSavepointType() {
    return this.savepointType;
  }

  public String getSavepointTimestamp() {
    return this.savepointTimestamp;
  }

  @JsonIgnore
  public ArrayList<DataKeyValue> getValues() {
    return this.orderedColumns;
  }

  public void setRowId(final String rowId) {
    this.rowId = rowId;
  }

  public void setRowETag(final String rowETag) {
    this.rowETag = rowETag;
  }

  public void setDataETagAtModification(final String dataETagAtModification) {
    this.dataETagAtModification = dataETagAtModification;
  }

  public void setDeleted(final boolean deleted) {
    this.deleted = deleted;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public void setLastUpdateUser(String lastUpdateUser) {
    this.lastUpdateUser = lastUpdateUser;
  }

  public void setRowFilterScope(RowFilterScope filterScope) {
    this.rowFilterScope = filterScope;
  }

  public void setSavepointCreator(String savepointCreator) {
    this.savepointCreator = savepointCreator;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setSavepointType(String savepointType) {
    this.savepointType = savepointType;
  }

  @JsonIgnore
  public void setValues(final ArrayList<DataKeyValue> values) {
    if ( values == null ) {
      this.orderedColumns = null;
      return;
    }
    
    Collections.sort(values, new Comparator<DataKeyValue>(){

      @Override
      public int compare(DataKeyValue arg0, DataKeyValue arg1) {
        return arg0.column.compareTo(arg1.column);
      }});

    this.orderedColumns = values;
  }

  /**
   * Expects a string as generated by
   * {@link org.opendatakit.common.utils.WebUtils#iso8601Date(Date)}.
   *
   * @param timestamp
   */
  public void setSavepointTimestamp(String savepointTimestamp) {
    this.savepointTimestamp = savepointTimestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
    result = prime * result + ((rowETag == null) ? 0 : rowETag.hashCode());
    result = prime * result
        + ((dataETagAtModification == null) ? 0 : dataETagAtModification.hashCode());
    result = prime * result + ((deleted) ? 0 : 1);
    result = prime * result + ((createUser == null) ? 0 : createUser.hashCode());
    result = prime * result + ((lastUpdateUser == null) ? 0 : lastUpdateUser.hashCode());
    result = prime * result + ((rowFilterScope == null) ? 0 : rowFilterScope.hashCode());
    result = prime * result + ((savepointCreator == null) ? 0 : savepointCreator.hashCode());
    result = prime * result + ((formId == null) ? 0 : formId.hashCode());
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((savepointType == null) ? 0 : savepointType.hashCode());
    result = prime * result + ((savepointTimestamp == null) ? 0 : savepointTimestamp.hashCode());
    result = prime * result + ((orderedColumns == null) ? 0 : orderedColumns.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Row)) {
      return false;
    }
    Row other = (Row) obj;
    boolean simpleMatch = (rowId == null ? other.rowId == null : rowId.equals(other.rowId))
        && (rowETag == null ? other.rowETag == null : rowETag.equals(other.rowETag))
        && (dataETagAtModification == null ? other.dataETagAtModification == null
            : dataETagAtModification.equals(other.dataETagAtModification))
        && (deleted == other.deleted)
        && (createUser == null ? other.createUser == null : createUser.equals(other.createUser))
        && (lastUpdateUser == null ? other.lastUpdateUser == null : lastUpdateUser
            .equals(other.lastUpdateUser))
        && (rowFilterScope == null ? other.rowFilterScope == null : rowFilterScope.equals(other.rowFilterScope))
        && (savepointCreator == null ? other.savepointCreator == null : savepointCreator
            .equals(other.savepointCreator))
        && (formId == null ? other.formId == null : formId.equals(other.formId))
        && (locale == null ? other.locale == null : locale.equals(other.locale))
        && (savepointType == null ? other.savepointType == null : savepointType
            .equals(other.savepointType))
        && (savepointTimestamp == null ? other.savepointTimestamp == null : savepointTimestamp
            .equals(other.savepointTimestamp))
        && ( orderedColumns == null ? other.orderedColumns == null : 
            (other.orderedColumns != null && orderedColumns.size() == other.orderedColumns.size()));
    if ( !simpleMatch ) {
      return false;
    }
    if ( orderedColumns == null ) {
      return true;
    }

    // columns are ordered... compare one-to-one
    for ( int i = 0 ; i < orderedColumns.size() ; ++i ) {
      if ( !orderedColumns.get(i).equals(other.orderedColumns.get(i)) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * A reduced-function equals predicate to detect if the values significant to
   * the end user are matched across the two data records.
   * <p>
   * Ignore the following fields when making this comparison:
   * <ul>
   * <li>rowETag</li>
   * <li>dataETagAtModification</li>
   * <li>createUser</li>
   * <li>lastUpdateUser</li>
   * </ul>
   *
   * @param other
   * @return
   */
  public boolean hasMatchingSignificantFieldValues(Row other, Comparator<DataKeyValue> deepComparator) {
    if (other == null)
      return false;
    boolean simpleMatch = (rowId == null ? other.rowId == null : rowId.equals(other.rowId))
        && (deleted == other.deleted)
        && (rowFilterScope == null ? other.rowFilterScope == null : rowFilterScope.equals(other.rowFilterScope))
        && (formId == null ? other.formId == null : formId.equals(other.formId))
        && (locale == null ? other.locale == null : locale.equals(other.locale))
        && (savepointType == null ? other.savepointType == null : savepointType
            .equals(other.savepointType))
        && (savepointTimestamp == null ? other.savepointTimestamp == null : savepointTimestamp
            .equals(other.savepointTimestamp))
        && (savepointCreator == null ? other.savepointCreator == null : savepointCreator
            .equals(other.savepointCreator))
        && ( orderedColumns == null ? other.orderedColumns == null : 
            (other.orderedColumns != null && orderedColumns.size() == other.orderedColumns.size()));
    if ( !simpleMatch ) {
      return false;
    }
    if ( orderedColumns == null ) {
      return true;
    }
    
    // columns are ordered... compare one-to-one
    for ( int i = 0 ; i < orderedColumns.size() ; ++i ) {
      if ( deepComparator.compare(orderedColumns.get(i), other.orderedColumns.get(i)) != 0 ) {
        return false;
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Row [rowId=");
    builder.append(rowId);
    builder.append(", rowETag=");
    builder.append(rowETag);
    builder.append(", dataETagAtModification=");
    builder.append(dataETagAtModification);
    builder.append(", deleted=");
    builder.append(deleted);
    builder.append(", createUser=");
    builder.append(createUser);
    builder.append(", lastUpdateUser=");
    builder.append(lastUpdateUser);
    builder.append(", rowFilterScope=");
    builder.append(rowFilterScope);
    builder.append(", formId=");
    builder.append(formId);
    builder.append(", locale=");
    builder.append(locale);
    builder.append(", savepointType=");
    builder.append(savepointType);
    builder.append(", savepointTimestamp=");
    builder.append(savepointTimestamp);
    builder.append(", savepointCreator=");
    builder.append(savepointCreator);
    builder.append(", orderedValues=");
    builder.append(orderedColumns);
    builder.append("]");
    return builder.toString();
  }
}