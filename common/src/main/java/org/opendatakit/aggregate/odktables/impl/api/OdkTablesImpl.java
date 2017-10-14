package org.opendatakit.aggregate.odktables.impl.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbInstallationInteractionLog;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.AppNameList;
import org.opendatakit.aggregate.odktables.rest.entity.ClientVersionList;
import org.opendatakit.aggregate.odktables.rest.entity.PrivilegesInfo;
import org.opendatakit.aggregate.odktables.rest.entity.UserInfo;
import org.opendatakit.aggregate.odktables.rest.entity.UserInfoList;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("")
public class OdkTablesImpl implements OdkTables {
  
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Response getAppNames(@Context ServletContext sc, @Context HttpServletRequest req,
      @Context HttpHeaders httpHeaders) throws ODKDatastoreException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    AppNameList appNames = new AppNameList(Collections.singletonList(preferencesAppId));
    return Response.ok(appNames)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getOdkClientVersions(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    List<String> distinctOdkClientVersions = null;
    String eTagOdkClientVersions = null;

    // retrieve the incoming if-none-match eTag...
    List<String> eTags = httpHeaders.getRequestHeader(HttpHeaders.IF_NONE_MATCH);
    String eTag = (eTags == null || eTags.isEmpty()) ? null : eTags.get(0);
    try {
      distinctOdkClientVersions = DbTableFileInfo.queryForAllOdkClientVersions(cc);
      eTagOdkClientVersions = Integer.toHexString(
          (distinctOdkClientVersions == null) ? -1 : distinctOdkClientVersions.hashCode());

      if (eTag != null && distinctOdkClientVersions != null && eTag.equals(eTagOdkClientVersions)) {
        return Response.status(Status.NOT_MODIFIED).header(HttpHeaders.ETAG, eTag)
            .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Credentials", "true").build();
      }
    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(OdkTablesImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }

    if (distinctOdkClientVersions == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Unable to retrieve odkClientVersions.")
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else {
      UriBuilder ub = info.getBaseUriBuilder();
      ub.path(OdkTables.class, "getOdkClientVersions");

      ClientVersionList clientVersions = new ClientVersionList(distinctOdkClientVersions);
      return Response.ok(clientVersions).header(HttpHeaders.ETAG, eTagOdkClientVersions)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    }
  }

  @Override
  public Response getPrivilegesInfo(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException
  {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }
    
    try {
      PrivilegesInfo privilegesInfo = SecurityServiceUtil.getRolesAndDefaultGroup(cc);

      // if the request includes an installation header, 
      // log that the user that has been verified on that installation.
      String installationId = req.getHeader(ApiConstants.OPEN_DATA_KIT_INSTALLATION_HEADER);
      try {
        if ( installationId != null ) {
          DbInstallationInteractionLog.recordVerificationEntry(installationId, cc);
        }
      } catch ( Exception e ) {
        LogFactory.getLog(OdkTablesImpl.class).warn("Unable to write verification log entry for " +
                    installationId + " user " + cc.getCurrentUser().getUriUser(), e);
      }

      return Response.ok(privilegesInfo)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } catch ( Exception e ) {
      Log log = LogFactory.getLog(OdkTablesImpl.class);
      log.error("Exception retrieving user privileges");
      e.printStackTrace();

      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Exception retrieving user privileges.")
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    }
  }
  
  @Override
  public Response /*UserInfoList*/ getUsersInfo(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException
  {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }
    
    PrivilegesInfo currentUserPrivileges = SecurityServiceUtil.getRolesAndDefaultGroup(cc);
      
    boolean returnFullList = false;
    for ( String grant : currentUserPrivileges.getRoles() ) {
      if (grant.equals(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN.name()) ||
          grant.equals(GrantedAuthorityName.ROLE_ADMINISTER_TABLES.name()) ||
          grant.equals(GrantedAuthorityName.ROLE_SUPER_USER_TABLES.name())) {
        returnFullList = true;
        break;
      }
    }
      
    // returned object (will be JSON serialized).
    ArrayList<UserInfo> listOfUsers = new ArrayList<UserInfo>();

    if ( !returnFullList ) {
      // only return ourself -- we don't have privileges to see everyone
      UserInfo userInfo = new UserInfo(currentUserPrivileges.getUser_id(),
          currentUserPrivileges.getFull_name(), currentUserPrivileges.getRoles());
      listOfUsers.add(userInfo);
    } else {
        // we have privileges to see all users -- return the full mapping
          ArrayList<UserSecurityInfo> allUsers;
          try {
            allUsers = SecurityServiceUtil.getAllUsers(true, cc);
          } catch (AccessDeniedException e) {
            Log log = LogFactory.getLog(OdkTablesImpl.class);
            log.error("AccessDeniedException retrieving user list");
            e.printStackTrace();

            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("AccessDeniedException retrieving user list.")
                .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true").build();
          } catch (DatastoreFailureException e) {
            Log log = LogFactory.getLog(OdkTablesImpl.class);
            log.error("DatastoreFailureException retrieving user list");
            e.printStackTrace();

            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("DatastoreFailureException retrieving user list.")
                .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true").build();
          }
          
          for (UserSecurityInfo i : allUsers ) {
            if ( i.getType() == UserType.ANONYMOUS ) {
                continue;
            }
            UserInfo userInfo = SecurityServiceUtil.createUserInfo(cc, i);
            listOfUsers.add(userInfo);
          }
      }
      
      UserInfoList userInfoList = new UserInfoList(listOfUsers);
      
      return Response.ok(userInfoList)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response postInstallationInfo(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId, Object body) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    {
      // if the request includes an installation header, 
      // log that the user that has been changing the configuration from that installation.
      String installationId = req.getHeader(ApiConstants.OPEN_DATA_KIT_INSTALLATION_HEADER);
      try {
        if ( installationId != null ) {
          DbInstallationInteractionLog.recordDeviceInfoEntry(installationId, 
              mapper.writeValueAsString(body), cc);
        }
      } catch ( Exception e ) {
        LogFactory.getLog(OdkTablesImpl.class).error("(ignored) Unable to recordChangeConfigurationEntry", e);
      }
    }
    
    // no body in response
    return Response.ok()
            .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public FileManifestServiceImpl getFileManifestService(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    return new FileManifestServiceImpl(info, appId, cc);
  }

  @Override
  public FileServiceImpl getFilesService(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    return new FileServiceImpl(sc, req, httpHeaders, info, appId, cc);
  }

  @Override
  public Response /* TableResourceList */ getTables(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId,
      @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit)
      throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException,
      ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    TableServiceImpl ts = new TableServiceImpl(sc, req, httpHeaders, info, appId, cc);
    return ts.getTables(cursor, fetchLimit);
  }

  @Override
  public TableServiceImpl getTablesService(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId, String tableId)
      throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException,
      ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if (!preferencesAppId.equals(appId)) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    return new TableServiceImpl(sc, req, httpHeaders, info, appId, tableId, cc);
  }

}
