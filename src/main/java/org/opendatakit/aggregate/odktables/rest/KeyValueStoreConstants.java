/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables.rest;

/**
 * Constants used to access well-known values within the KVS.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class KeyValueStoreConstants {

  // special well-known partitions
  public static final String PARTITION_TABLE = "Table";
  public static final String PARTITION_COLUMN = "Column";
  // others should use their class names as the partition name

  // default aspect
  public static final String ASPECT_DEFAULT = "default";

  // Keys for:
  // TABLE_PARTITION, DEFAULT_ASPECT...

  // required for ODK Aggregate displays
  // json holding a string or a map of (locale -> string)
  public static final String TABLE_DISPLAY_NAME = "displayName";

  // These fields are useful for the spreadsheet view of the data
  // we may want to use them on ODK Aggregate.

  // json serialization of the list of elementKeys to display
  public static final String TABLE_COL_ORDER = "colOrder";
  // json serialization of the list of elementKeys to group by
  public static final String TABLE_GROUP_BY_COLS = "groupByCols";
  // elementKey of the column to sort by
  public static final String TABLE_SORT_COL = "sortCol";
  // true if sort order is ascending
  public static final String TABLE_SORT_ORDER = "sortOrder";
  // elementKey held fixed during left/right pan
  public static final String TABLE_INDEX_COL = "indexCol";

  // used by Survey when publishing via the legacy ODK 1.x pipeline:

  // the elementKey to use as the instanceName field for the submission
  public static final String XML_INSTANCE_NAME = "xmlInstanceName";
  // the root element to use for the XML submission
  public static final String XML_ROOT_ELEMENT_NAME = "xmlRootElementName";
  // the deviceId property to use in the metadata block
  public static final String XML_DEVICE_ID_PROPERTY_NAME = "xmlDeviceIdPropertyName";
  // the userId property to use in the metadata block
  public static final String XML_USER_ID_PROPERTY_NAME = "xmlUserIdPropertyName";
  // the public encryption key to use for encrypting the data
  public static final String XML_BASE64_RSA_PUBLIC_KEY = "xmlBase64RsaPublicKey";
  // the URL to use when submitting the data
  public static final String XML_SUBMISSION_URL = "xmlSubmissionUrl";

  // Keys for:
  // COLUMN_PARTITION, dbColName...

  // These may also be useful for the spreadsheet view of the data
  // we may want to use them on ODK Aggregate.

  // json holding a string or a map of (locale -> string)
  public static final String COLUMN_DISPLAY_NAME = "displayName";

  // boolean value (visible/hidden)
  public static final String COLUMN_DISPLAY_VISIBLE = "displayVisible";

  // json holding a list of maps with 'data_value' and 'display', a nested
  // map that holds all the display properties, including 'text' which can be
  // internationalized.
  public static final String COLUMN_DISPLAY_CHOICES_LIST = "displayChoicesList";

  // json holding a string or a map of (locale -> string)
  // this is used to render the data value (esp. for composite data values)
  public static final String COLUMN_DISPLAY_FORMAT = "displayFormat";

  // json -- format TBD
  public static final String COLUMN_JOINS = "joins";

}
