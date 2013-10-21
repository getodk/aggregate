package org.opendatakit.aggregate.odktables.rest;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains various things that are constant in tables and must be known and
 * retained by Aggregate.
 * @author sudar.sam@gmail.com
 *
 */
public class TableConstants {

  //TODO: should probably have an Aggregate Column object instead that will
  // allow you to specify type here.

  /*
   * These are the names of the shared columns. Included here so that they can
   * be accessed directly by aggregate.
   */

  // tablename is chosen by user...
  public static final String ID = "_id";
  public static final String URI_ACCESS_CONTROL = "_uri_access_control";
  public static final String SYNC_TAG = "_sync_tag";
  public static final String SYNC_STATE = "_sync_state";
  public static final String CONFLICT_TYPE = "_conflict_type";

  /**
   * (savepoint_timestamp, savepoint_type, form_id, locale) are the tuple written
   * and managed by ODK Survey when a record is updated. ODK Tables needs to update
   * these appropriately when a cell is directly edited based upon whether or not
   * the table is 'form-managed' or not. If form-managed, and direct cell editing
   * is allowed, it should set 'savepoint_type' to 'INCOMPLETE' and should leave
   * form_id unchanged. Otherwise, it can set 'savepoint_type' to 'COMPLETE' and
   * set form_id to null.
   *
   * Note that the value of 'savepoint_type' must be 'COMPLETE' in order for
   * ODK Tables to sync the values up to the server, otherwise there is a
   * conflict resolution step that is enforced on the device.  I.e., this is a
   * requirement before a data row can move off of a device. Hence, we do
   * not need to record the value of 'savepoint_type' on the server, because it will
   * always be 'COMPLETE'.
   *
   * In contrast, the row security management, savepoint, form, locale, sync state,
   * and conflict resolution fields are metadata and are not directly exposed to
   * the user.
   */
  public static final String SAVEPOINT_TIMESTAMP = "_savepoint_timestamp";
  public static final String FORM_ID = "_form_id";
  public static final String LOCALE = "_locale";

  /*
   * savepoint_type is never sent to the server since it should always be 'COMPLETE'
   */
  public static final String SAVEPOINT_TYPE = "_savepoint_type";

  /**
   * This set contains the names of the  metadata columns that are present in
   * all ODKTables data tables. The data in these columns needs to be synched
   * to the server.
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
    SHARED_COLUMN_NAMES.add(URI_ACCESS_CONTROL);
    SHARED_COLUMN_NAMES.add(SAVEPOINT_TIMESTAMP);
    SHARED_COLUMN_NAMES.add(FORM_ID);
    SHARED_COLUMN_NAMES.add(LOCALE);
    CLIENT_ONLY_COLUMN_NAMES.add(ID);
    CLIENT_ONLY_COLUMN_NAMES.add(SAVEPOINT_TYPE);
    CLIENT_ONLY_COLUMN_NAMES.add(SYNC_STATE);
    CLIENT_ONLY_COLUMN_NAMES.add(SYNC_TAG);
    CLIENT_ONLY_COLUMN_NAMES.add(CONFLICT_TYPE);
    }

}
