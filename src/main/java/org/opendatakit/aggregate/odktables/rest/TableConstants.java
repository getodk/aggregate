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
  public static final String ID = "id";
  public static final String ROW_ID = "id";
  public static final String URI_ACCESS_CONTROL = "uri_access_control";
  public static final String SYNC_TAG = "sync_tag";
  public static final String SYNC_STATE = "sync_state";
  public static final String TRANSACTIONING = "transactioning";

  /**
   * (timestamp, saved, form_id) are the tuple written and managed by ODK Survey
   * when a record is updated. ODK Tables needs to update these appropriately
   * when a cell is directly edited based upon whether or not the table is
   * 'form-managed' or not.
   *
   * timestamp and last_mod_time are the same field. last_mod_time is simply
   * a well-formatted text representation of the timestamp value.
   */
  public static final String TIMESTAMP = "timestamp";
  public static final String SAVED = "saved";
  public static final String FORM_ID = "form_id";
  /*
   * For ODKTables generated rows (as opposed to ODK Collect), the thought is
   * that this instance name would just be the iso86 pretty print date of
   * creation.
   */
  public static final String INSTANCE_NAME = "instance_name";
  public static final String LOCALE = "locale";


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
    SHARED_COLUMN_NAMES.add(TIMESTAMP);
    SHARED_COLUMN_NAMES.add(FORM_ID);
    SHARED_COLUMN_NAMES.add(INSTANCE_NAME);
    SHARED_COLUMN_NAMES.add(LOCALE);
    CLIENT_ONLY_COLUMN_NAMES.add(ID);
    CLIENT_ONLY_COLUMN_NAMES.add(ROW_ID);
    CLIENT_ONLY_COLUMN_NAMES.add(SAVED);
    CLIENT_ONLY_COLUMN_NAMES.add(SYNC_STATE);
    CLIENT_ONLY_COLUMN_NAMES.add(SYNC_TAG);
    CLIENT_ONLY_COLUMN_NAMES.add(TRANSACTIONING);
    }

}
