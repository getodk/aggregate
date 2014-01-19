package org.opendatakit.aggregate.odktables.security;

import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * A class that holds all the information about a given ODK Tables user
 * and their permissions.  This is typically constructed at the beginning
 * of a REST request.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public interface TablesUserPermissions {

  public abstract String getOdkTablesUserId();

  public abstract String getPhoneNumber();

  public abstract String getXBearerCode();

  /**
   * Checks that the current user has the given permission on the table.
   *
   * @param tableId
   * @param permission
   *          the permission to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user does not have the permission
   */
  public abstract void checkPermission(String tableId, TablePermission permission)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   * Check if the current user has the given permission on the table. An
   * exception-safe alternative to {@link #checkPermission(TablePermission)}
   *
   * @param tableId
   * @param permission
   *          the permission to check
   * @return true if the user has the given permission, false otherwise
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public abstract boolean hasPermission(String tableId, TablePermission permission)
      throws ODKDatastoreException;

  public abstract boolean hasFilterScope(String tableId, Scope filterScope);

  /**
   * Check that the user either has the given permission or is within the scope
   * of the filter on the given row.
   *
   * In other words, if the user has the given permission, then they pass the
   * check and the method returns. However, if the user does not have the given
   * permission, then they must fall within the scope of the filter on the given
   * row.
   *
   * @param permission
   *          the permission that guards access to the row. Should be one of
   *          {@link TablePermission#UNFILTERED_READ},
   *          {@link TablePermission#UNFILTERED_WRITE}, or
   *          {@link TablePermission#UNFILTERED_DELETE}.
   * @param row
   *          the row to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user does not have the given permission and is not
   *           within the scope of the filter on the row
   */
  public abstract void checkFilter(String tableId, TablePermission permission, String rowId,
      Scope filter) throws ODKDatastoreException, PermissionDeniedException;
}