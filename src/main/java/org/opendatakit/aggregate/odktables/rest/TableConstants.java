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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.SimpleTimeZone;

/**
 * Contains various things that are constant in tables and must be known and
 * retained by Aggregate.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableConstants {

  /**
   * Columns in the User data table.
   */

  /**
   * ID is the primary key, as experienced by the user. There may be
   * multiple rows with this ID due to conflicts and checkpoint states.
   */
  public static final String ID = "_id";

  /**
   * ROW_ETAG defines the the server entity tag (version) of the row.
   * This is controlled by the server and changes by the client are always
   * ignored.
   */
  public static final String ROW_ETAG = "_row_etag";

  /************************************************
   * These columns are only on the client device.
   ************************************************/

  /**
   * Client side only. One of the {@link SyncState} values.
   */
  public static final String SYNC_STATE = "_sync_state";

  /**
   * Client side only. One of the {@ConflictType} constants.
   */
  public static final String CONFLICT_TYPE = "_conflict_type";

  /************************************************
   * These columns are common across server and client
   ************************************************/

  /**
   * Contains the type attribute of a Scope. The Scope describes the access
   * controls on this row. Changes to this value on the device may or may not
   * be applied during a sync action (TBD).
   */
  public static final String FILTER_TYPE = "_filter_type";
  public static final String DEFAULT_ACCESS = "_default_access";
  /**
   * Contains the value attribute of a Scope. The Scope describes the access
   * controls on this row. Changes to this value on the device may or may not
   * be applied during a sync action (TBD).
   */
  public static final String FILTER_VALUE = "_filter_value";
  public static final String ROW_OWNER = "_row_owner";
  
  /**
   * Contains the groupType attribute of a Scope. The Scope describes the access
   * controls on this row. Changes to this value on the device may or may not
   * be applied during a sync action (TBD).
   */
  public static final String GROUP_READ_ONLY = "_group_read_only";
  /**
   * Contains the groupsList attribute of a Scope. The Scope describes the access
   * controls on this row. Changes to this value on the device may or may not
   * be applied during a sync action (TBD).
   */
  public static final String GROUP_MODIFY = "_group_modify";
  
  /**
   * Contains the ext attribute of a Scope. The Scope describes the access
   * controls on this row. Changes to this value on the device may or may not
   * be applied during a sync action (TBD).
   */
  public static final String GROUP_PRIVILEGED = "_group_privileged";

  /**
   * (form_id, locale, savepoint_type, savepoint_timestamp, savepoint_creator)
   * are the tuple written and managed by ODK Survey when a record is updated.
   *
   * ODK Tables needs to update these appropriately when a cell is directly
   * edited based upon whether or not the table is 'form-managed' or not. If
   * form-managed, and direct cell editing is allowed, it should set
   * 'savepoint_type' to 'INCOMPLETE' and should leave form_id unchanged.
   * Otherwise, it can set 'savepoint_type' to 'COMPLETE' and set form_id to null.
   *
   * The value of 'savepoint_creator' is the user that is making the change. This
   * may be a remote SMS user.
   *
   * In a table being sync'd, the value of 'savepoint_type' for each row must
   * either be INCOMPLETE or COMPLETE.  There is a clean-up step before a sync
   * during which the user is prompted to select whether to mark a checkpoint
   * (null) savepoint-type as INCOMPLETE or to remove it. Only once that is done
   * can the sync proceed.
   *
   * In contrast, the row security management, savepoint, form, locale, sync
   * state, and conflict resolution fields are metadata and are not directly
   * exposed to the user.
   */
  public static final String SAVEPOINT_TYPE = "_savepoint_type";
  public static final String SAVEPOINT_TIMESTAMP = "_savepoint_timestamp";
  public static final String SAVEPOINT_CREATOR = "_savepoint_creator";
  public static final String FORM_ID = "_form_id";
  public static final String LOCALE = "_locale";

  /**
   * This set contains the names of the metadata columns that are present in all
   * ODKTables data tables. The data in these columns needs to be synched to the
   * server.
   */
  public static final Set<String> SHARED_COLUMN_NAMES;

  /**
   * This set contains the names of all the metadata columns that are specific
   * to each phone and whose data should NOT be synched onto the server and
   * between phones.
   */
  public static final Set<String> CLIENT_ONLY_COLUMN_NAMES;

  static {
    SHARED_COLUMN_NAMES = new HashSet<String>();
    CLIENT_ONLY_COLUMN_NAMES = new HashSet<String>();
    SHARED_COLUMN_NAMES.add(DEFAULT_ACCESS);
    SHARED_COLUMN_NAMES.add(ROW_OWNER);
    SHARED_COLUMN_NAMES.add(GROUP_READ_ONLY);
    SHARED_COLUMN_NAMES.add(GROUP_MODIFY);
    SHARED_COLUMN_NAMES.add(GROUP_PRIVILEGED);
    SHARED_COLUMN_NAMES.add(SAVEPOINT_TYPE);
    SHARED_COLUMN_NAMES.add(SAVEPOINT_TIMESTAMP);
    SHARED_COLUMN_NAMES.add(SAVEPOINT_CREATOR);
    SHARED_COLUMN_NAMES.add(FORM_ID);
    SHARED_COLUMN_NAMES.add(LOCALE);
    CLIENT_ONLY_COLUMN_NAMES.add(ID); // somewhat of a misnomer -- this is transmitted and serves as a PK to the record.
    CLIENT_ONLY_COLUMN_NAMES.add(ROW_ETAG); // somewhat of a misnomer -- this is transmitted, but never overwrites server.
    CLIENT_ONLY_COLUMN_NAMES.add(SYNC_STATE);
    CLIENT_ONLY_COLUMN_NAMES.add(CONFLICT_TYPE);
  }

  // nanosecond-extended iso8601-style UTC date yyyy-mm-ddTHH:MM:SS.sssssssss
  private static final String MILLI_TO_NANO_TIMESTAMP_EXTENSION = "000000";

  public static String nanoSecondsFromMillis(Long timeMillis ) {
    if ( timeMillis == null ) return null;
    // convert to a nanosecond-extended iso8601-style UTC date yyyy-mm-ddTHH:MM:SS.sssssssss
    Calendar c = GregorianCalendar.getInstance(new SimpleTimeZone(0,"UT"));
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    sf.setCalendar(c);
    Date d = new Date(timeMillis);
    String v = sf.format(d) + MILLI_TO_NANO_TIMESTAMP_EXTENSION;
    return v;
  }

  public static Long milliSecondsFromNanos(String timeNanos ) {
    if ( timeNanos == null ) return null;
    // convert from a nanosecond-extended iso8601-style UTC date yyyy-mm-ddTHH:MM:SS.sssssssss
    Calendar c = GregorianCalendar.getInstance(new SimpleTimeZone(0,"UT"));
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    sf.setCalendar(c);
    String truncated = timeNanos.substring(0, timeNanos.length()-MILLI_TO_NANO_TIMESTAMP_EXTENSION.length());
    Date d;
    try {
      d = sf.parse(truncated);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Unrecognized time format: " + timeNanos);
    }
    Long v = d.getTime();
    return v;
  }
}
