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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The XML document that represents a column. This is the XML representation of
 * a column definition as stored in the (keep this fully qualified!)
 * {@link org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions}
 * table.
 *
 * Removed all JAXB annotations -- these cause issues on Android 4.2 and earlier.
 *
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
public class Column implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = -6624997293167731653L;

  /**
   * The tableId containing this elementKey
   */
  /**
   * The fully qualified key for this element. This is the element's database 
   * column name. For composite types whose elements are individually retained
   * (e.g., geopoint), this would be the elementName of the geopoint (e.g., 
   * 'myLocation' concatenated with '_' and this elementName (e.g., 
   * 'myLocation_latitude').
   *
   * Never longer than 58 characters.
   * Never a SQL or SQLite reserved word
   * Satisfies this regex: '^\\p{L}\\p{M}*(\\p{L}\\p{M}*|\\p{Nd}|_)*$'
   */
  private String elementKey;

  /**
   * The name by which this element is referred. For composite types whose
   * elements are individually retained (e.g., geopoint), this would be simply
   * 'latitude'
   *
   * Never longer than 58 characters.
   * Never a SQL or SQLite reserved word
   * Satisfies this regex: '^\\p{L}\\p{M}*(\\p{L}\\p{M}*|\\p{Nd}|_)*$'
   */
  @JsonProperty(required = false)
  private String elementName;

  /**
   * This is the ColumnType of the field. It is either:
   *    boolean
   *    integer
   *    number
   *    configpath
   *    rowpath
   *    array
   *    array(len)
   *    string
   *    string(len)
   *    typename
   *    typename(len)
   *    
   *    or
   *    
   *    typename:datatype
   *    typename:datatype(len)
   *    
   *    where datatype can be one of boolean, integer, number, array, object
   *
   *    Where:
   *
   *    'typename' is any other alpha-numeric name (user-definable data type).
   *
   *    The (len) attribute, if present, identifies the VARCHAR storage
   *    requirements for the field when the field is a unit of retention.
   *    Ignored if not a unit of retention.
   *
   *    The server stores:
   *
   *      integer as a 32-bit integer.
   *
   *      number as a double-precision floating point value.
   *
   *      configpath indicates that it is a relative path to a file under the 'config'
   *             directory in the 'new' directory structure. i.e., the relative path is
   *             rooted from:
   *                 /sdcard/opendatakit/{appId}/config/
   *
   *      rowpath indicates that it is a relative path to a file under the row's attachment
   *             directory in the 'new' directory structure. i.e., the relative path is
   *             rooted from:
   *                 /sdcard/opendatakit/{appId}/data/attachments/{tableId}/{rowId}/
   *
   *      array is a JSON serialization expecting one child element key
   *            that defines the data type in the array.  Array fields
   *            MUST be a unit of retention (or be nested within one).
   *
   *      string is a string value
   *
   *      anything else, if it has no child element key, it is a string
   *            (simple user-defined data type). Unless a datatype is specified.
   *
   *      anything else, if it has one or more child element keys, is a
   *            JSON serialization of an object containing those keys
   *            (complex user-defined data type).
   *
   */
  private String elementType;

  /**
   * JSON serialization of an array of strings. Each value in the
   * array identifies an elementKey of a nested field within this
   * elementKey. If there are one or more nested fields, then the
   * value stored in this elementKey is a JSON serialization of
   * either an array or an object. Otherwise, it is either an
   * integer, number or string field.
   *
   * If the elementType is 'array', the serialization is an
   * array and the nested field is retrieved via a subscript.
   *
   * Otherwise, the serialization is an object and the nested
   * field is retrieved via the elementName of that field.
   */
  @JsonProperty(required = false)
  private String listChildElementKeys;
  
  /**
   * If true, then this elementKey is a column in the backing
   * database table. If false, then either the elementKey is a
   * component of an enclosing object that is a column in the
   * backing database table, or, each of its child element keys
   * or their descendants are columns in the backing database
   * table.
   */
  @SuppressWarnings("unused")
  private Column() {
  }

  /**
   * Create a column. NB: It needs to be decided if backing name and display
   * name are different in the datastore on the server in the same way they are
   * on the phone, and if they should both be stored in the COLUMN table as
   * adjacent columns, or what exactly. Either way, its implementation should be
   * brought into alignment with ColumnClient, which has both display and
   * backing names when the answer to the above questions is decided.
   * 
   * @param tableId
   * @param elementKey
   * @param elementName
   * @param elementType
   * @param listChildElementKeys - a JSON serialization of a list of child
   *                 elementKey values. These must be in the (alphabetical) order 
   *                 produced by Javascript sort().
   */
  public Column(final String elementKey, final String elementName,
      final String elementType, final String listChildElementKeys) {
    this.elementKey = elementKey;
    this.elementName = elementName;
    this.elementType = elementType;
    this.listChildElementKeys = listChildElementKeys;
  }

  public String getElementKey() {
    return this.elementKey;
  }

  public String getElementName() {
    return this.elementName;
  }

  /**
   * Returns the string name of an ODKTables column type.
   */
  public String getElementType() {
    return this.elementType;
  }

  public String getListChildElementKeys() {
    return this.listChildElementKeys;
  }
  
  @Override
  public String toString() {
    return "Column(elementKey=" + this.getElementKey()
        + ", elementName=" + this.getElementName() + ", elementType= " + this.getElementType()
        + ", listChildElementKeys=" + this.getListChildElementKeys() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((elementKey == null) ? 0 : elementKey.hashCode());
    result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
    result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
    result = prime * result
        + ((listChildElementKeys == null) ? 0 : listChildElementKeys.hashCode());
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
    if (!(obj instanceof Column)) {
      return false;
    }
    Column other = (Column) obj;
    return (elementKey == null ? other.elementKey == null : elementKey.equals(other.elementKey))
        && (elementName == null ? other.elementName == null : elementName.equals(other.elementName))
        && (elementType == null ? other.elementType == null : elementType.equals(other.elementType))
        && (listChildElementKeys == null ? other.listChildElementKeys == null
            : listChildElementKeys.equals(other.listChildElementKeys));
  }
}
