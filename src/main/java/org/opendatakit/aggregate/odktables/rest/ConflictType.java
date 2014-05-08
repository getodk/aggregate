package org.opendatakit.aggregate.odktables.rest;


/**
 * This class stores ints that should be stored in the
 * client-side (device) data table's {@link TableConstants#CONFLICT_TYPE}
 * column. This column represents the reason that the rows are labeled as conflict.
 * <p>
 * There are essentially three scenarios to consider. All of which will have
 * the rows' {@link TableConstants#SYNC_STATE} set to {@link SyncState.conflicting}:
 * <ol>
 * <li>Two users have modified the same row, one has synched, the other pull
 * those changes</li>
 * <ul>
 * <li> In this case neither row has been deleted. The local row will have
 * {@link TableConstants#CONFLICT_TYPE} equal to
 * {@link ConflictType#LOCAL_UPDATED_UPDATED_VALUES}, and the server
 * row will have {@link TableConstants#CONFLICT_TYPE} equal to
 * {@link ConflictType#SERVER_UPDATED_UPDATED_VALUES}.
 * </ul>
 * <li> The row has been deleted on the server, and the local user has edited
 * their local version</li>
 * <ul>
 * <li> In this case the server row is considered deleted. Its
 * {@link TableConstants#CONFLICT_TYPE} row will be set to
 * {@link ConflictType#SERVER_DELETED_OLD_VALUES}. The values in
 * that row will be the contents of that row on the server at the time of
 * deletion. The local row, which had been edited and been in
 * {@link SyncState.updating} before the sync (and thus why it was
 * not deleted outright), will have {@link TableConstants#CONFLICT_TYPE}
 * set to {@link ConflictType#LOCAL_UPDATED_UPDATED_VALUES}.
 * TODO: what happens if both versions are deleted, but the row versions were
 * different? Is this case handled appropriately? Unclear...
 * </ul>
 * <li> The local row has been deleted, but a newer version has
 * been updated on the server</li>
 * <ul>
 * <li> In this case the local row will have
 * {@link TableConstants#CONFLICT_TYPE} equal to
 * {@link ConflictType#LOCAL_DELETED_OLD_VALUES}. It will have the
 * values that were in the row at the time of deletion. These may differ
 * from the last synced version of the row--i.e. updates may have been
 * performed before deletion, meaning the sync state moved from
 * {@link SyncState.rest} to {@link SyncState.updating} to
 * {@link SyncState.deleting}. The server row meanwhile will have
 * {@link TableConstants#CONFLICT_TYPE} set to
 * {@link ConflictType#SERVER_UPDATED_UPDATED_VALUES}. The
 * contents of this row will be the latest version of the updated row on the
 * server.
 * </ul>
 * </ol>
 * @author sudar.sam@gmail.com
 *
 */
public class ConflictType {

  public static final int LOCAL_DELETED_OLD_VALUES = 0;
  public static final int LOCAL_UPDATED_UPDATED_VALUES = 1;
  public static final int SERVER_DELETED_OLD_VALUES = 2;
  public static final int SERVER_UPDATED_UPDATED_VALUES = 3;

  private ConflictType() {
  }
}