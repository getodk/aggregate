package org.opendatakit.aggregate.odktables.impl.api;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
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
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.AppNameList;
import org.opendatakit.aggregate.odktables.rest.entity.ClientVersionList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

@Path("")
public class OdkTablesImpl implements OdkTables {

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
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
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
