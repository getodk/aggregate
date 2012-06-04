package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.TableAclManager;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.TableAclResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class TableAclServiceImpl implements TableAclService {

  private TableAclManager am;
  private UriInfo info;
  private AuthFilter af;

  public TableAclServiceImpl(String tableId, UriInfo info, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.am = new TableAclManager(tableId, cc);
    this.info = info;
    this.af = new AuthFilter(tableId, cc);
  }

  @Override
  public List<TableAclResource> getAcls() throws ODKDatastoreException, PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    List<TableAcl> acls = am.getAcls();
    return getResources(acls);
  }

  @Override
  public List<TableAclResource> getUserAcls() throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    List<TableAcl> acls = am.getAcls(Scope.Type.USER);
    return getResources(acls);
  }

  @Override
  public List<TableAclResource> getGroupAcls() throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    List<TableAcl> acls = am.getAcls(Scope.Type.GROUP);
    return getResources(acls);
  }

  @Override
  public TableAclResource getUserAcl(String userId) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    if (userId.equals("null"))
      userId = null;
    TableAcl acl = am.getAcl(new Scope(Scope.Type.USER, userId));
    return getResource(acl);
  }

  @Override
  public TableAclResource getGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    TableAcl acl = am.getAcl(new Scope(Scope.Type.GROUP, groupId));
    return getResource(acl);
  }

  @Override
  public TableAclResource getDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ACL);
    TableAcl acl = am.getAcl(new Scope(Scope.Type.DEFAULT, null));
    return getResource(acl);
  }

  @Override
  public TableAclResource setUserAcl(String userId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.WRITE_ACL);
    if (userId.equals("null"))
      userId = null;
    acl = am.setAcl(new Scope(Scope.Type.USER, userId), acl.getRole());
    return getResource(acl);
  }

  @Override
  public TableAclResource setGroupAcl(String groupId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.WRITE_ACL);
    acl = am.setAcl(new Scope(Scope.Type.GROUP, groupId), acl.getRole());
    return getResource(acl);
  }

  @Override
  public TableAclResource setDefaultAcl(TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.WRITE_ACL);
    acl = am.setAcl(new Scope(Scope.Type.DEFAULT, null), acl.getRole());
    return getResource(acl);
  }

  @Override
  public void deleteDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {
    af.checkPermission(TablePermission.DELETE_ACL);
    am.deleteAcl(new Scope(Scope.Type.DEFAULT, null));
  }

  @Override
  public void deleteUserAcl(String userId) throws ODKDatastoreException, PermissionDeniedException {
    af.checkPermission(TablePermission.DELETE_ACL);
    am.deleteAcl(new Scope(Scope.Type.USER, userId));
  }

  @Override
  public void deleteGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.DELETE_ACL);
    am.deleteAcl(new Scope(Scope.Type.USER, groupId));
  }

  private TableAclResource getResource(TableAcl acl) {
    String tableId = am.getTableId();
    Scope.Type type = acl.getScope().getType();
    String value = acl.getScope().getValue();
    if (value == null)
      value = "null";

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    UriBuilder selfBuilder = ub.clone().path(TableService.class, "getAcl");
    URI self;
    switch (type) {
    case USER:
      self = selfBuilder.path(TableAclService.class, "getUserAcl").build(tableId, value);
      break;
    case GROUP:
      self = selfBuilder.path(TableAclService.class, "getGroupAcl").build(tableId, value);
      break;
    case DEFAULT:
    default:
      self = selfBuilder.path(TableAclService.class, "getDefaultAcl").build(tableId);
      break;
    }
    URI acls = ub.clone().path(TableService.class, "getAcl").build(tableId);
    URI table = ub.clone().path(TableService.class, "getTable").build(tableId);

    TableAclResource resource = new TableAclResource(acl);
    resource.setSelfUri(self.toASCIIString());
    resource.setAclUri(acls.toASCIIString());
    resource.setTableUri(table.toASCIIString());
    return resource;
  }

  private List<TableAclResource> getResources(List<TableAcl> acls) {
    List<TableAclResource> resources = new ArrayList<TableAclResource>();
    for (TableAcl acl : acls) {
      resources.add(getResource(acl));
    }
    return resources;
  }

}
