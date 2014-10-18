package org.opendatakit.aggregate.odktables.impl.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

@Path("")
public class OdkTablesImpl implements OdkTables {
  
  @Override
  public Response getAppName(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders) 
      throws ODKDatastoreException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    return Response.status(Status.OK).entity(preferencesAppId).build();
  }

  @Override
  public FileManifestServiceImpl getFileManifestService(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if ( !preferencesAppId.equals(appId) ) {
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

    if ( !preferencesAppId.equals(appId) ) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    return new FileServiceImpl(sc, req, httpHeaders, info, appId, cc);
  }

  @Override
  public Response /*TableResourceList*/ getTables(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if ( !preferencesAppId.equals(appId) ) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    TableServiceImpl ts = new TableServiceImpl(sc, req, httpHeaders, info, appId, cc);
    return ts.getTables(cursor, fetchLimit);
  }

  @Override
  public TableServiceImpl getTablesService(ServletContext sc, HttpServletRequest req,
      HttpHeaders httpHeaders, UriInfo info, String appId, String tableId) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    ServiceUtils.examineRequest(sc, req, httpHeaders);
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String preferencesAppId = ContextFactory.getOdkTablesAppId(cc);

    if ( !preferencesAppId.equals(appId) ) {
      throw new AppNameMismatchException("AppName (" + appId + ") differs");
    }

    return new TableServiceImpl(sc, req, httpHeaders, info, appId, tableId, cc);
  }

}
