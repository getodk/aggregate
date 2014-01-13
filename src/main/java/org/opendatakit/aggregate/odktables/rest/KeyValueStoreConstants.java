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
  public static final String TABLE_TYPE = "tableType";
  public static final String TABLE_ACCESS_CONTROL_TABLE_ID = "accessControlTableId";
  public static final String TABLE_DISPLAY_NAME = "displayName";
  public static final String TABLE_COL_ORDER = "colOrder";
  public static final String TABLE_PRIME_COLS = "primeCols";
  public static final String TABLE_SORT_COL = "sortCol";
  public static final String TABLE_INDEX_COL = "indexCol";
  public static final String TABLE_CURRENT_VIEW_TYPE = "currentViewType";
  public static final String TABLE_SUMMARY_DISPLAY_FORMAT = "summaryDisplayFormat";
  public static final String TABLE_CURRENT_QUERY = "currentQuery";

  // Keys for:
  // COLUMN_PARTITION, dbColName...
  public static final String COLUMN_DISPLAY_NAME = "displayName";
  public static final String COLUMN_DISPLAY_VISIBLE = "displayVisible";
  public static final String COLUMN_DISPLAY_CHOICES_LIST = "displayChoicesList";
  public static final String COLUMN_DISPLAY_FORMAT = "displayFormat";
  public static final String COLUMN_SMS_IN = "smsIn";
  public static final String COLUMN_SMS_OUT = "smsOut";
  public static final String COLUMN_SMS_LABEL = "smsLabel";
  public static final String COLUMN_FOOTER_MODE = "footerMode";
  public static final String COLUMN_JOINS = "joins";

}
