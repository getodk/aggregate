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

package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query.WebsafeQueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * Manages retrieving and setting access control lists on a table.
 *
 * @author the.dylan.price@gmail.com
 *
 */
public class TableAclManager {
  
  public static class WebsafeAcls {
    public final List<TableAcl> acls;

    public final String websafeRefetchCursor;
    public final String websafeBackwardCursor;
    public final String websafeResumeCursor;
    public final boolean hasMore;
    public final boolean hasPrior;

    public WebsafeAcls(List<TableAcl> acls,
        String websafeRefetchCursor, String websafeBackwardCursor, String websafeResumeCursor,
        boolean hasMore, boolean hasPrior) {
      this.acls = acls;
      this.websafeRefetchCursor = websafeRefetchCursor;
      this.websafeBackwardCursor = websafeBackwardCursor;
      this.websafeResumeCursor = websafeResumeCursor;
      this.hasMore = hasMore;
      this.hasPrior = hasPrior;
    }
  }
  
  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private EntityConverter converter;
  private EntityCreator creator;
  private String appId;
  private String tableId;

  /**
   * Construct a new TableAclManager.
   *
   * @param appId
   *          the unique application id
   * @param tableId
   *          the unique identifier of the table
   * @param cc
   *          the calling context
   * @throws ODKEntityNotFoundException
   *           if no table with the given id exists
   * @throws ODKDatastoreException
   *           if there is an internal error in the datastore
   */
  public TableAclManager(String appId, String tableId, TablesUserPermissions userPermissions, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(appId);
    Validate.notEmpty(tableId);
    Validate.notNull(cc);

    this.cc = cc;
    this.appId = appId;
    this.tableId = tableId;
    this.userPermissions = userPermissions;
    this.converter = new EntityConverter();
    this.creator = new EntityCreator();
    // check table exists
    DbTableEntryEntity e = DbTableEntry.getTableIdEntry(tableId, cc);
    if (e == null) {
      throw new IllegalArgumentException("tableId does not exist!");
    }
    // tableId may be in the process of being deleted.
    // we allow everything to work up until the pending-deleted table is removed.
  }

  /**
   * @return the appId of the application this TableAclManager was constructed with
   */
  public String getAppId() {
    return appId;
  }

  /**
   * @return the tableId of the table this TableAclManager was constructed with
   */
  public String getTableId() {
    return tableId;
  }

  /**
   * Get all acls for the table.
   *
   * @return a list of TableAcl
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public WebsafeAcls getAcls(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException, PermissionDeniedException {
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ACL);
    
    WebsafeQueryResult result = DbTableAcl.queryTableIdAcls(tableId, startCursor, fetchLimit, cc);
    

    List<DbTableAclEntity> results = new ArrayList<DbTableAclEntity>();
    for (Entity e : result.entities) {
      results.add(new DbTableAclEntity(e));
    }

    return new WebsafeAcls( converter.toTableAcls(results),
            result.websafeRefetchCursor,
            result.websafeBackwardCursor,
            result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }

  /**
   * Get all acls for the table and given scope type.
   *
   * @param type
   *          the type of acls to retrieve
   * @return a list of TableAcl
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public WebsafeAcls getAcls(Scope.Type type, QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(type);
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ACL);

    WebsafeQueryResult result = DbTableAcl.queryTableIdScopeTypeAcls(tableId, type.name(), startCursor, fetchLimit, cc);

    List<DbTableAclEntity> results = new ArrayList<DbTableAclEntity>();
    for (Entity e : result.entities) {
      results.add(new DbTableAclEntity(e));
    }
    
    return new WebsafeAcls( converter.toTableAcls(results),
        result.websafeRefetchCursor,
        result.websafeBackwardCursor,
        result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }

  /**
   * Get the access control list for the given scope.
   *
   * @param scope
   *          the scope of the list
   * @return the acl, or null if none exists
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public TableAcl getAcl(Scope scope) throws ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ACL);

    DbTableAclEntity aclEntity = DbTableAcl.queryTableIdScopeTypeValueAcl(tableId, scope.getType()
        .name(), scope.getValue(), cc);
    TableAcl acl = null;
    if (aclEntity != null) {
      acl = converter.toTableAcl(aclEntity);
    }
    return acl;
  }

  /**
   * This is a private API for the TablesUserPermissions object. Not to be called by anyone else!
   *
   * @param scope
   * @return
   * @throws ODKDatastoreException
   */
  public TableAcl getAclForTablesUserPermissions(Scope scope) throws ODKDatastoreException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());

    DbTableAclEntity aclEntity = DbTableAcl.queryTableIdScopeTypeValueAcl(tableId, scope.getType()
        .name(), scope.getValue(), cc);
    TableAcl acl = null;
    if (aclEntity != null) {
      acl = converter.toTableAcl(aclEntity);
    }
    return acl;
  }

  /**
   * Set the permissions of an access control list.
   *
   * @param scope
   *          the scope of the acl
   * @param role
   *          the role of the acl
   * @return the modified acl (created if none already exists)
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public TableAcl setAcl(Scope scope, TableRole role) throws ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());
    Validate.notNull(role);
    userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ACL);

    DbTableAclEntity acl = DbTableAcl.queryTableIdScopeTypeValueAcl(tableId,
        scope.getType().name(), scope.getValue(), cc);
    if (acl == null) {
      acl = creator.newTableAclEntity(tableId, scope, role, cc);
    } else {
      acl.setRole(role.name());
    }
    acl.put(cc);

    return converter.toTableAcl(acl);
  }

  /**
   * Delete the acl with the given scope.
   *
   * @param scope
   *          the scope of the acl
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public void deleteAcl(Scope scope) throws ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());
    userPermissions.checkPermission(appId, tableId, TablePermission.DELETE_ACL);

    DbTableAclEntity acl = DbTableAcl.queryTableIdScopeTypeValueAcl(tableId,
        scope.getType().name(), scope.getValue(), cc);
    if (acl != null) {
      acl.delete(cc);
    }
  }
}
