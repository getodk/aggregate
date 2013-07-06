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

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.common.ermodel.simple.Entity;
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
  private CallingContext cc;
  private EntityConverter converter;
  private EntityCreator creator;
  private String tableId;

  /**
   * Construct a new TableAclManager.
   *
   * @param tableId
   *          the unique identifier of the table
   * @param cc
   *          the calling context
   * @throws ODKEntityNotFoundException
   *           if no table with the given id exists
   * @throws ODKDatastoreException
   *           if there is an internal error in the datastore
   */
  public TableAclManager(String tableId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(cc);

    this.cc = cc;
    this.converter = new EntityConverter();
    this.creator = new EntityCreator();
    this.tableId = tableId;
    // check table exists
    DbTableEntry.getRelation(cc).getEntity(tableId, cc);
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
   */
  public List<TableAcl> getAcls() throws ODKDatastoreException {
    List<Entity> acls = DbTableAcl.query(tableId, cc);
    return converter.toTableAcls(acls);
  }

  /**
   * Get all acls for the table and given scope type.
   *
   * @param type
   *          the type of acls to retrieve
   * @return a list of TableAcl
   * @throws ODKDatastoreException
   */
  public List<TableAcl> getAcls(Scope.Type type) throws ODKDatastoreException {
    Validate.notNull(type);

    List<Entity> acls = DbTableAcl.query(tableId, type.name(), cc);
    return converter.toTableAcls(acls);
  }

  /**
   * Get the access control list for the given scope.
   *
   * @param scope
   *          the scope of the list
   * @return the acl, or null if none exists
   * @throws ODKDatastoreException
   */
  public TableAcl getAcl(Scope scope) throws ODKDatastoreException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());

    Entity entity = DbTableAcl.getAcl(tableId, scope.getType().name(), scope.getValue(), cc);
    TableAcl acl = null;
    if (entity != null) {
      acl = converter.toTableAcl(entity);
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
   */
  public TableAcl setAcl(Scope scope, TableRole role) throws ODKDatastoreException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());
    Validate.notNull(role);

    Entity acl = DbTableAcl.getAcl(tableId, scope.getType().name(), scope.getValue(), cc);
    if (acl == null) {
      acl = creator.newTableAclEntity(tableId, scope, role, cc);
    } else {
      acl.set(DbTableAcl.ROLE, role.name());
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
   */
  public void deleteAcl(Scope scope) throws ODKDatastoreException {
    Validate.notNull(scope);
    Validate.notNull(scope.getType());

    Entity acl = DbTableAcl.getAcl(tableId, scope.getType().name(), scope.getValue(), cc);
    if (acl != null) {
      acl.delete(cc);
    }
  }
}
