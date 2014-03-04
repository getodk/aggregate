package org.opendatakit.aggregate.odktables.security;

import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;

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
   * @param appId
   * @param tableId
   * @param permission
   *          the permission to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user does not have the permission
   */
  public abstract void checkPermission(String appId, String tableId, TablePermission permission)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   * Check if the current user has the given permission on the table. An
   * exception-safe alternative to {@link #checkPermission(TablePermission)}
   *
   * @param appId
   * @param tableId
   * @param permission
   *          the permission to check
   * @return true if the user has the given permission, false otherwise
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public abstract boolean hasPermission(String appId, String tableId, TablePermission permission)
      throws ODKDatastoreException;

  /**
   * Check if the current user has the given filter scope on this table.
   *
   * @param appId
   * @param tableId
   * @param permission
   *          the permission that guards access to the row. Should be one of
   *          {@link TablePermission#READ_ROW},
   *          {@link TablePermission#WRITE_ROW}, or
   *          {@link TablePermission#DELETE_ROW}.
   * @param rowId
   *          the row to check
   * @param filterScope
   *          the filter scope bound to that row
   * @return
   * @throws ODKDatastoreException
   * @throws ODKEntityNotFoundException
   */
  public abstract boolean hasFilterScope(String appId, String tableId, TablePermission permission, String rowId, Scope filterScope) throws ODKEntityNotFoundException, ODKDatastoreException;

}