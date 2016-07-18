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

package org.opendatakit.aggregate.odktables.entity;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.RowFilterScopeClient;
import org.opendatakit.aggregate.client.odktables.RowResourceClient;
import org.opendatakit.aggregate.client.odktables.ScopeClient;
import org.opendatakit.aggregate.client.odktables.TableAclClient;
import org.opendatakit.aggregate.client.odktables.TableAclResourceClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.client.odktables.TableRoleClient;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.common.utils.WebUtils;

/**
 * Various methods for transforming objects from client to server code.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class UtilTransforms {
  private static final Log log = LogFactory.getLog(UtilTransforms.class);

  /**
   * Transform the object into a server-side Column object.
   */
  public static Column transform(ColumnClient client) {
    Column transformedColumn = new Column(client.getElementKey(),
        client.getElementName(), client.getElementType(), client.getListChildElementKeys());
    return transformedColumn;
  }

  /**
   * Transform into the server-side Row.
   */
  public static Row transform(RowClient client) {
    Row serverRow = new Row();
    serverRow.setCreateUser(client.getCreateUser());
    serverRow.setDeleted(client.isDeleted());
    serverRow.setRowFilterScope(transform(client.getRowFilterScope()));
    serverRow.setLastUpdateUser(client.getLastUpdateUser());
    serverRow.setRowETag(client.getRowETag());
    serverRow.setRowId(client.getRowId());
    HashMap<String,String> cvalues = client.getValues();
    serverRow.setValues(Row.convertFromMap(cvalues));
    serverRow.setFormId(client.getFormId());
    serverRow.setLocale(client.getLocale());
    serverRow.setSavepointType(client.getSavepointType());
    String isoDateStr = client.getSavepointTimestampIso8601Date();
    Date isoDate = (isoDateStr == null) ? null : WebUtils.parseDate(isoDateStr);
    String nanoTime = (isoDate == null) ? null : TableConstants.nanoSecondsFromMillis(isoDate.getTime());
    // TODO: this truncates the nanosecond portion of a date!
    serverRow.setSavepointTimestamp(nanoTime);
    serverRow.setSavepointCreator(client.getSavepointCreator());
    serverRow.setDataETagAtModification(client.getDataETagAtModification());
    return serverRow;
  }

  /**
   * Transforms into the server-side RowFilterScope.
   */
  public static RowFilterScope transform(RowFilterScopeClient client) {
    RowFilterScope serverScope = null;
    if (client.getType() == null) {
      serverScope = RowFilterScope.EMPTY_ROW_FILTER;
      return serverScope;
    }
    switch (client.getType()) {
    case DEFAULT:
      serverScope = new RowFilterScope(RowFilterScope.Type.DEFAULT, client.getValue());
      break;
    case MODIFY:
      serverScope = new RowFilterScope(RowFilterScope.Type.MODIFY, client.getValue());
      break;
    case READ_ONLY:
      serverScope = new RowFilterScope(RowFilterScope.Type.READ_ONLY, client.getValue());
      break;
    case HIDDEN:
      serverScope = new RowFilterScope(RowFilterScope.Type.HIDDEN, client.getValue());
      break;
    default:
      serverScope = RowFilterScope.EMPTY_ROW_FILTER;

    }
    return serverScope;
  }

  /**
   * Transforms into the server-side Scope.
   */
  public static Scope transform(ScopeClient client) {
    Scope serverScope = null;
    if (client.getType() == null) {
      serverScope = Scope.EMPTY_SCOPE;
      return serverScope;
    }
    switch (client.getType()) {
    case DEFAULT:
      serverScope = new Scope(Scope.Type.DEFAULT, client.getValue());
      break;
    case USER:
      serverScope = new Scope(Scope.Type.USER, client.getValue());
      break;
    case GROUP:
      serverScope = new Scope(Scope.Type.GROUP, client.getValue());
      break;
    default:
      serverScope = Scope.EMPTY_SCOPE;

    }
    return serverScope;
  }

  /**
   * Transforms the object into a TableAcl object.
   */
  public static TableAcl transform(TableAclClient client) {
    TableAcl ta = new TableAcl();
    switch (client.getRole()) {
    case NONE:
      ta.setRole(TableRole.NONE);
      break;
    case FILTERED_WRITER:
      ta.setRole(TableRole.FILTERED_WRITER);
      break;
    case UNFILTERED_READER_FILTERED_WRITER:
      ta.setRole(TableRole.UNFILTERED_READER_FILTERED_WRITER);
      break;
    case READER:
      ta.setRole(TableRole.READER);
      break;
    case WRITER:
      ta.setRole(TableRole.WRITER);
      break;
    case OWNER:
      ta.setRole(TableRole.OWNER);
      break;
    default:
      throw new IllegalStateException("No assignable permissions in transforming table role.");
    }
    ta.setScope(transform(client.getScope()));
    return ta;
  }

  /**
   * Transforms the object into client-side TableEntryClient object.
   */
  public static TableEntryClient transform(TableEntry serverEntry) {
    TableEntryClient clientEntry = new TableEntryClient(serverEntry.getTableId(),
        serverEntry.getDataETag(), serverEntry.getSchemaETag());
    return clientEntry;
  }

  /**
   * This method transforms the TableResource into a client-side
   * TableResourceClient object.
   */
  public static TableResourceClient transform(TableResource serverResource) {
    TableResourceClient clientResource = new TableResourceClient(new TableEntryClient(
        serverResource.getTableId(), serverResource.getDataETag(),
        serverResource.getSchemaETag()));
    clientResource.setAclUri(serverResource.getAclUri());
    clientResource.setDataUri(serverResource.getDataUri());
    clientResource.setInstanceFilesUri(serverResource.getInstanceFilesUri());
    clientResource.setDiffUri(serverResource.getDiffUri());
    clientResource.setSelfUri(serverResource.getSelfUri());
    clientResource.setDefinitionUri(serverResource.getDefinitionUri());
    return clientResource;
  }

  /**
   * Transform the row into a client-side Row.
   */
  public static RowClient transform(Row serverRow) {
    RowClient row = new RowClient();
    row.setCreateUser(serverRow.getCreateUser());
    row.setDeleted(serverRow.isDeleted());
    row.setLastUpdateUser(serverRow.getLastUpdateUser());
    row.setDataETagAtModification(serverRow.getDataETagAtModification());
    row.setRowETag(serverRow.getRowETag());
    row.setRowId(serverRow.getRowId());
    if (serverRow.getRowFilterScope().getType() == null) {
      throw new IllegalStateException("rowFilterScope un-handled value!");
    } else {
      switch (serverRow.getRowFilterScope().getType()) {
      case DEFAULT:
        row.setRowFilterScope(new RowFilterScopeClient(RowFilterScopeClient.Type.DEFAULT, serverRow.getRowFilterScope()
            .getValue()));
        break;
      case MODIFY:
        row.setRowFilterScope(new RowFilterScopeClient(RowFilterScopeClient.Type.MODIFY, serverRow.getRowFilterScope()
            .getValue()));
        break;
      case READ_ONLY:
        row.setRowFilterScope(new RowFilterScopeClient(RowFilterScopeClient.Type.READ_ONLY, serverRow.getRowFilterScope()
            .getValue()));
        break;
      case HIDDEN:
        row.setRowFilterScope(new RowFilterScopeClient(RowFilterScopeClient.Type.HIDDEN, serverRow.getRowFilterScope()
            .getValue()));
        break;
      default:
        throw new IllegalStateException("rowFilterScope un-handled value!");
      }
    }

    // sync'd metadata
    row.setFormId(serverRow.getFormId());
    row.setLocale(serverRow.getLocale());
    row.setSavepointType(serverRow.getSavepointType());
    String savepointTimestamp = serverRow.getSavepointTimestamp();
    Long time = TableConstants.milliSecondsFromNanos(savepointTimestamp);
    row.setSavepointTimestampIso8601Date(time == null ? null : WebUtils.iso8601Date(new Date(time)));
    row.setSavepointCreator(serverRow.getSavepointCreator());

    // data
    row.setValues(Row.convertToMap(serverRow.getValues()));
    return row;
  }

  // adding this so you can also create the client version
  public static RowResourceClient transform(RowResource serverResource) {
    RowClient rowClient = new RowClient();
    rowClient.setCreateUser(serverResource.getCreateUser());
    rowClient.setDeleted(serverResource.isDeleted());
    rowClient.setRowFilterScope(UtilTransforms.transform(serverResource.getRowFilterScope()));
    rowClient.setLastUpdateUser(serverResource.getLastUpdateUser());
    rowClient.setRowETag(serverResource.getRowETag());
    rowClient.setRowId(serverResource.getRowId());

    // sync'd metadata
    rowClient.setFormId(serverResource.getFormId());
    rowClient.setLocale(serverResource.getLocale());
    rowClient.setSavepointType(serverResource.getSavepointType());
    String savepointTimestamp = serverResource.getSavepointTimestamp();
    Long time = TableConstants.milliSecondsFromNanos(savepointTimestamp);
    rowClient.setSavepointTimestampIso8601Date(time == null ? null : WebUtils.iso8601Date(new Date(
        time)));
    rowClient.setSavepointCreator(serverResource.getSavepointCreator());

    rowClient.setDataETagAtModification(serverResource.getDataETagAtModification());
    // data
    rowClient.setValues(Row.convertToMap(serverResource.getValues()));

    // manipulator URIs
    RowResourceClient resource = new RowResourceClient(rowClient);
    resource.setSelfUri(serverResource.getSelfUri());

    return resource;

  }

  public static ScopeClient transform(Scope serverScope) {
    // First get the type of this scope
    ScopeClient sc = null;
    switch (serverScope.getType()) {
    case DEFAULT:
      sc = new ScopeClient(ScopeClient.Type.DEFAULT, serverScope.getValue());
      break;
    case USER:
      sc = new ScopeClient(ScopeClient.Type.USER, serverScope.getValue());
      break;
    case GROUP:
      sc = new ScopeClient(ScopeClient.Type.GROUP, serverScope.getValue());
      break;
    }
    if (sc == null)
      sc = ScopeClient.EMPTY_SCOPE;
    return sc;
  }

  public static RowFilterScopeClient transform(RowFilterScope serverScope) {
    // First get the type of this scope
    RowFilterScopeClient sc = null;
    switch (serverScope.getType()) {
    case DEFAULT:
      sc = new RowFilterScopeClient(RowFilterScopeClient.Type.DEFAULT, serverScope.getValue());
      break;
    case MODIFY:
      sc = new RowFilterScopeClient(RowFilterScopeClient.Type.MODIFY, serverScope.getValue());
      break;
    case READ_ONLY:
      sc = new RowFilterScopeClient(RowFilterScopeClient.Type.READ_ONLY, serverScope.getValue());
      break;
    case HIDDEN:
      sc = new RowFilterScopeClient(RowFilterScopeClient.Type.HIDDEN, serverScope.getValue());
      break;
    }
    if (sc == null) {
      throw new IllegalStateException("rowFilterScope un-handled value!");
    }
    return sc;
  }

  /**
   * Transforms the TableAclObject into a TableAclClient object.
   */
  public static TableAclClient transform(TableAcl serverAcl) {
    TableAclClient tac = new TableAclClient();
    switch (serverAcl.getRole()) {
    case NONE:
      tac.setRole(TableRoleClient.NONE);
      break;
    case FILTERED_WRITER:
      tac.setRole(TableRoleClient.FILTERED_WRITER);
      break;
    case UNFILTERED_READER_FILTERED_WRITER:
      tac.setRole(TableRoleClient.UNFILTERED_READER_FILTERED_WRITER);
      break;
    case READER:
      tac.setRole(TableRoleClient.READER);
      break;
    case WRITER:
      tac.setRole(TableRoleClient.WRITER);
      break;
    case OWNER:
      tac.setRole(TableRoleClient.OWNER);
      break;
    default:
      throw new IllegalStateException("No assignable permissions in transforming table role.");
    }
    tac.setScope(UtilTransforms.transform(serverAcl.getScope()));
    return tac;
  }

  /**
   * Transform into the client-side TableAclResource.
   */
  public static TableAclResourceClient transform(TableAclResource serverResource) {
    TableAclClient tac = new TableAclClient();
    // now set the correct client side role
    switch (serverResource.getRole()) {
    case NONE:
      tac.setRole(TableRoleClient.NONE);
      break;
    case FILTERED_WRITER:
      tac.setRole(TableRoleClient.FILTERED_WRITER);
      break;
    case UNFILTERED_READER_FILTERED_WRITER:
      tac.setRole(TableRoleClient.UNFILTERED_READER_FILTERED_WRITER);
      break;
    case READER:
      tac.setRole(TableRoleClient.READER);
      break;
    case WRITER:
      tac.setRole(TableRoleClient.WRITER);
      break;
    case OWNER:
      tac.setRole(TableRoleClient.OWNER);
      break;
    default:
      throw new IllegalStateException("No assignable permissions in transforming table role.");
    }
    tac.setScope(UtilTransforms.transform(serverResource.getScope()));
    TableAclResourceClient tarc = new TableAclResourceClient(tac);
    tarc.setAclUri(serverResource.getAclUri());
    tarc.setSelfUri(serverResource.getSelfUri());
    tarc.setTableUri(serverResource.getTableUri());
    return tarc;
  }
}
