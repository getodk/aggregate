/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.aggregate.odktables.api.perf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.internal.handlers.GzipHandler;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.ChangeSetList;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowList;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.aggregate.odktables.api.SyncETagsUtils;
import org.opendatakit.aggregate.odktables.api.SyncProgressState;
import org.opendatakit.aggregate.odktables.api.SyncRow;
import org.opendatakit.aggregate.odktables.api.SyncRowPending;
import org.opendatakit.aggregate.odktables.api.Synchronizer;
import org.opendatakit.aggregate.odktables.api.exceptions.InvalidAuthTokenException;

/**
 * Implementation of {@link Synchronizer} for ODK Aggregate.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */
public class AggregateSynchronizer implements Synchronizer {
  private static final Log logger = LogFactory.getLog(AggregateSynchronizer.class);

  private static final String LOGTAG = AggregateSynchronizer.class.getSimpleName();
  private static final String TOKEN_INFO = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";
  
  // parameters for queries that could return a lot of data...
  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";

  // parameter for file downloads -- if we want to have it come down as an attachment.
  public static final String PARAM_AS_ATTACHMENT = "as_attachment";

  int CONNECTION_TIMEOUT = 30000;
  
  // parameters for data/diff/query APIs.
  public static final String QUERY_DATA_ETAG = "data_etag";
  public static final String QUERY_SEQUENCE_VALUE = "sequence_value";
  public static final String QUERY_ACTIVE_ONLY = "active_only";
  // parameters for query API
  public static final String QUERY_START_TIME = "startTime";
  public static final String QUERY_END_TIME = "endTime";

  /** Timeout (in ms) we specify for each http request */
  public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
  /** Path to the file servlet on the Aggregate server. */

  private static final String FORWARD_SLASH = "/";

  static Map<String, String> mimeMapping;
  static {

    Map<String, String> m = new HashMap<String, String>();
    m.put("jpeg", "image/jpeg");
    m.put("jpg", "image/jpeg");
    m.put("png", "image/png");
    m.put("gif", "image/gif");
    m.put("pbm", "image/x-portable-bitmap");
    m.put("ico", "image/x-icon");
    m.put("bmp", "image/bmp");
    m.put("tiff", "image/tiff");

    m.put("mp2", "audio/mpeg");
    m.put("mp3", "audio/mpeg");
    m.put("wav", "audio/x-wav");

    m.put("asf", "video/x-ms-asf");
    m.put("avi", "video/x-msvideo");
    m.put("mov", "video/quicktime");
    m.put("mpa", "video/mpeg");
    m.put("mpeg", "video/mpeg");
    m.put("mpg", "video/mpeg");
    m.put("mp4", "video/mp4");
    m.put("qt", "video/quicktime");

    m.put("css", "text/css");
    m.put("htm", "text/html");
    m.put("html", "text/html");
    m.put("csv", "text/csv");
    m.put("txt", "text/plain");
    m.put("log", "text/plain");
    m.put("rtf", "application/rtf");
    m.put("pdf", "application/pdf");
    m.put("zip", "application/zip");
    m.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    m.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    m.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    m.put("xml", "application/xml");
    m.put("js", "application/x-javascript");
    m.put("json", "application/x-javascript");
    mimeMapping = m;

    // Android does not support Runtime delegation. Set it up manually...
    // force this once...
    org.apache.wink.common.internal.runtime.RuntimeDelegateImpl rd = new org.apache.wink.common.internal.runtime.RuntimeDelegateImpl();
    RuntimeDelegate.setInstance(rd);
  }

  private final String context;
  private final String appName;
  private final String odkClientApiVersion;
  private final String aggregateUri;
  private String accessToken;
  private final RestClient tokenRt;
  private final RestClient rt;
  private final Map<String, TableResource> resources;
  /** normalized aggregateUri */
  private final URI baseUri;
  // cookie manager 
  private final CookieManager cm;


  private final URI normalizeUri(String aggregateUri, String additionalPathPortion) {
    URI uriBase = URI.create(aggregateUri).normalize();
    String term = uriBase.getPath();
    if (term.endsWith(FORWARD_SLASH)) {
      if (additionalPathPortion.startsWith(FORWARD_SLASH)) {
        term = term.substring(0, term.length() - 1);
      }
    } else if (!additionalPathPortion.startsWith(FORWARD_SLASH)) {
      term = term + FORWARD_SLASH;
    }
    term = term + additionalPathPortion;
    URI uri = uriBase.resolve(term).normalize();
    logger.debug("normalizeUri: " + uri.toString());
    return uri;
  }

  private static final String escapeSegment(String segment) {
    return segment;
    // String encoding = CharEncoding.UTF_8;
    // String encodedSegment;
    // try {
    // encodedSegment = URLEncoder.encode(segment, encoding)
    // .replaceAll("\\+", "%20")
    // .replaceAll("\\%21", "!")
    // .replaceAll("\\%27", "'")
    // .replaceAll("\\%28", "(")
    // .replaceAll("\\%29", ")")
    // .replaceAll("\\%7E", "~");
    //
    // } catch (UnsupportedEncodingException e) {
    // e.printStackTrace();;
    // throw new IllegalStateException("Should be able to encode with " +
    // encoding);
    // }
    // return encodedSegment;
  }

  /**
   * Format a file path to be pushed up to aggregate. Essentially escapes the
   * string as for an html url, but leaves forward slashes. The path must begin
   * with a forward slash, as if starting at the root directory.
   * 
   * @return a properly escaped url, with forward slashes remaining.
   */
  private String uriEncodeSegments(String path) {
    String[] parts = path.split("/");
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < parts.length; ++i) {
      if (i != 0) {
        b.append("/");
      }
      b.append(escapeSegment(parts[i]));
    }
    String escaped = b.toString();
    return escaped;
  }

  private String getTablesUriFragment() {
    /**
     * Path to the tables servlet (the one that manages table definitions) on
     * the Aggregate server.
     */
    return "/odktables/" + escapeSegment(appName) + "/tables/";
  }

  private String getManifestUriFragment() {
    /**
     * Path to the tables servlet (the one that manages table definitions) on
     * the Aggregate server.
     */
    return "/odktables/" + escapeSegment(appName) + "/manifest/"
        + escapeSegment(odkClientApiVersion) + "/";
  }

  /**
   * Get the URI for the file servlet on the Aggregate server located at
   * aggregateUri.
   *
   * @param aggregateUri
   * @return
   */
  private String getFilePathURI() {
    return "/odktables/" + escapeSegment(appName) + "/files/" + escapeSegment(odkClientApiVersion)
        + "/";
  }
  /**
   * Simple Resource for all server interactions.
   * 
   * @param uri
   * @return
   * @throws InvalidAuthTokenException 
   */
  private Resource buildBasicResource(URI uri) throws InvalidAuthTokenException {

    Resource rsc = rt.resource(uri);

    // report our locale... (not currently used by server)
    rsc.acceptLanguage(Locale.getDefault());

    // set the access token...
    rsc.header(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
    rsc.header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
    rsc.header(HttpHeaders.USER_AGENT, "Sync it (gzip)");
    GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    Date now = new Date();
    g.setTime(now);
    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz", Locale.US);
    formatter.setCalendar(g);
    rsc.header(ApiConstants.DATE_HEADER, formatter.format(now));

    if (accessToken != null && baseUri != null) {
      if (uri.getHost().equals(baseUri.getHost()) && uri.getPort() == baseUri.getPort()) {
        rsc.header("Authorization", "Bearer " + accessToken);
      }
    }
    
    return rsc;
  }

  private Resource buildResource(URI uri, MediaType contentType) throws InvalidAuthTokenException {

    Resource rsc = buildBasicResource(uri);

    rsc.contentType(contentType);

    // set our preferred response media type to json using quality parameters
    Map<String, String> mediaTypeParams;
    // we really like JSON
    mediaTypeParams = new HashMap<String, String>();
    mediaTypeParams.put("q", "1.0");
    MediaType json = new MediaType(MediaType.APPLICATION_JSON_TYPE.getType(),
        MediaType.APPLICATION_JSON_TYPE.getSubtype(), mediaTypeParams);
    // don't really want plaintext...
    mediaTypeParams = new HashMap<String, String>();
    mediaTypeParams.put("charset", CharEncoding.UTF_8.toLowerCase(Locale.ENGLISH));
    mediaTypeParams.put("q", "0.4");
    MediaType tplainUtf8 = new MediaType(MediaType.TEXT_PLAIN_TYPE.getType(),
        MediaType.TEXT_PLAIN_TYPE.getSubtype(), mediaTypeParams);

    // accept either json or plain text (no XML to device)
    rsc.accept(json, tplainUtf8);

    // set the response entity character set to CharEncoding.UTF_8
    rsc.header("Accept-Charset", CharEncoding.UTF_8);
    
    return rsc;
  }

  /**
   * Simple Resource for file download.
   * 
   * @param uri
   * @return
   * @throws InvalidAuthTokenException 
   */
  private Resource buildFileDownloadResource(URI uri) throws InvalidAuthTokenException {

    Resource rsc = buildBasicResource(uri);
    
    return rsc;
  }

  private Resource buildResource(URI uri) throws InvalidAuthTokenException {

    // select our preferred protocol...
    MediaType protocolType = MediaType.APPLICATION_JSON_TYPE;
    
    Resource rsc = buildResource(uri, protocolType);
    
    return rsc;
  }

  public AggregateSynchronizer(String ctxt, String appName, String odkApiVersion,
      String aggregateUri, String accessToken) throws InvalidAuthTokenException {
    this.context = ctxt;
    this.appName = appName;
    this.odkClientApiVersion = odkApiVersion;
    this.aggregateUri = aggregateUri;
    logger.debug("AggregateUri:" + aggregateUri);
    this.baseUri = normalizeUri(aggregateUri, "/");
    logger.error("baseUri:" + baseUri);

    // This is technically not correct, as we should really have a global
    // that we manage for this... If there are two or more service threads
    // running, we could forget other session cookies. But, by creating a 
    // new cookie manager here, we ensure that we don't have any stale 
    // session cookies at the start of each sync.
    
    cm = new CookieManager();
    CookieHandler.setDefault(cm);
    
    // now everything should work...
    ClientConfig cc;

    cc = new ClientConfig();
    cc.setLoadWinkApplications(false);
    cc.applications(new ODKClientApplication());
    cc.handlers(new GzipHandler(), new ReAuthSecurityHandler(this));
    cc.connectTimeout(CONNECTION_TIMEOUT);
    cc.readTimeout(2 * CONNECTION_TIMEOUT);
    cc.followRedirects(true);

    this.rt = new RestClient(cc);

    cc = new ClientConfig();
    cc.setLoadWinkApplications(false);
    cc.applications(new ODKClientApplication());
    cc.connectTimeout(CONNECTION_TIMEOUT);
    cc.readTimeout(2 * CONNECTION_TIMEOUT);
    cc.followRedirects(true);

    this.tokenRt = new RestClient(cc);

    this.resources = new HashMap<String, TableResource>();

    checkAccessToken(accessToken);
    this.accessToken = accessToken;

  }

  private static final String ACCOUNT_TYPE_G = "com.google";
  private final static String authString = "oauth2:https://www.googleapis.com/auth/userinfo.email";

  public String updateAccessToken() throws InvalidAuthTokenException {
    // no-op
    return accessToken;
  }
  
  private void checkAccessToken(String accessToken) throws InvalidAuthTokenException {
    try {
      @SuppressWarnings("unused")
      Object responseEntity = tokenRt.resource(
          TOKEN_INFO + URLEncoder.encode(accessToken, ApiConstants.UTF8_ENCODE)).get(Object.class);
    } catch (ClientWebException e) {
      logger.error("HttpClientErrorException in checkAccessToken");
      Object o = null;
      try {
        String entityBody = e.getResponse().getEntity(String.class);
        o = ODKFileUtils.mapper.readValue(entityBody, Object.class);
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new InvalidAuthTokenException(
            "Unable to parse response from auth token verification (" + e.toString() + ")", e);
      }
      if (o != null && o instanceof Map) {
        @SuppressWarnings("rawtypes")
        Map m = (Map) o;
        if (m.containsKey("error")) {
          throw new InvalidAuthTokenException("Invalid auth token (" + m.get("error").toString()
              + "): " + accessToken, e);
        } else {
          throw new InvalidAuthTokenException("Unknown response from auth token verification ("
              + e.toString() + ")", e);
        }
      }
    } catch (Exception e) {
      logger.error("HttpClientErrorException in checkAccessToken");
      e.printStackTrace();;
      throw new InvalidAuthTokenException("Invalid auth token (): " + accessToken, e);
    }
  }

  @Override
  public TableResourceList getTables(String webSafeResumeCursor) throws ClientWebException, InvalidAuthTokenException {

    TableResourceList tableResources;
    try {
      String tableFrag = getTablesUriFragment();
      tableFrag = tableFrag.substring(0, tableFrag.length() - 1);
      URI uri = normalizeUri(aggregateUri, tableFrag);
      if ( webSafeResumeCursor == null ) {
        tableResources = buildResource(uri).get(TableResourceList.class);
      } else {
        tableResources = buildResource(uri).queryParam(CURSOR_PARAMETER, webSafeResumeCursor)
            .get(TableResourceList.class);
      }
    } catch (ClientWebException e) {
      logger.error("Exception while requesting list of tables from server: " + e.toString());
      throw e;
    }

    return tableResources;
  }

  @Override
  public TableDefinitionResource getTableDefinition(String tableDefinitionUri)
      throws ClientWebException, InvalidAuthTokenException {
    URI uri = URI.create(tableDefinitionUri).normalize();
    TableDefinitionResource definitionRes = buildResource(uri).get(TableDefinitionResource.class);
    return definitionRes;
  }

  @Override
  public URI constructTableInstanceFileUri(String tableId, String schemaETag) {
    // e.g., https://msundt-test.appspot.com:443/odktables/tables/tables/Milk_bank/ref/uuid:bb26cdaf-9ccf-4a4f-8e28-c114fe30358a/attachments
    URI instanceFileUri = normalizeUri(aggregateUri, getTablesUriFragment() + tableId + "/ref/" + schemaETag + "/attachments");
    return instanceFileUri;
  }
  
  @Override
  public TableResource createTable(String tableId, String schemaETag, ArrayList<Column> columns)
      throws ClientWebException, InvalidAuthTokenException {

    // build request
    URI uri = normalizeUri(aggregateUri, getTablesUriFragment() + tableId);
    TableDefinition definition = new TableDefinition(tableId, schemaETag, columns);
    // create table
    TableResource resource;
    try {
      // TODO: we also need to put up the key value store/properties.
      resource = buildResource(uri).put(new EntityType<TableResource>() {
      }, definition);
    } catch (ClientWebException e) {
      logger.error(
          "ResourceAccessException in createTable: " + tableId + " exception: " + e.toString());
      throw e;
    }

    // save resource
    this.resources.put(resource.getTableId(), resource);
    return resource;
  }

  @Override
  public void deleteTable(TableResource table) throws ClientWebException, InvalidAuthTokenException {
    URI uri = URI.create(table.getDefinitionUri()).normalize();
    buildResource(uri).delete();
    this.resources.remove(table.getTableId());
  }
  
  @Override
  public ChangeSetList getChangeSets(TableResource table, String dataETag) throws ClientWebException, InvalidAuthTokenException {

    String tableId = table.getTableId();
    URI uri;
    Resource resource;
    uri = normalizeUri(table.getDiffUri(), "/changeSets");
    resource = buildResource(uri);
    if ((table.getDataETag() != null) && dataETag != null) {
      resource = buildResource(uri).queryParam(QUERY_DATA_ETAG, dataETag);
    }
    
    ChangeSetList changeSets;
    try {
      changeSets = resource.get(ChangeSetList.class);
      return changeSets;
    } catch (ClientWebException e) {
      logger.error("Exception while requesting list of changeSets from server: " + tableId
          + " exception: " + e.toString());
      throw e;
    }
  }
  
  @Override
  public RowResourceList getChangeSet(TableResource table, String dataETag, boolean activeOnly, String websafeResumeCursor)
      throws ClientWebException, InvalidAuthTokenException {

    String tableId = table.getTableId();
    URI uri;
    Resource resource;
    if ((table.getDataETag() == null) || dataETag == null) {
      throw new IllegalArgumentException("dataETag cannot be null!");
    }
    
    uri = normalizeUri(table.getDiffUri(), "/changeSets/" + dataETag);
    resource = buildResource(uri);
    
    if ( activeOnly ) {
      resource = resource.queryParam(QUERY_ACTIVE_ONLY, "true");
    }

    // and apply the cursor...
    if ( websafeResumeCursor != null ) {
      resource = resource.queryParam(CURSOR_PARAMETER, websafeResumeCursor);
    }
    
    RowResourceList rows;
    try {
      rows = resource.get(RowResourceList.class);
      return rows;
    } catch (ClientWebException e) {
      logger.error("Exception while requesting changeSet rows from server: " + tableId
          + " exception: " + e.toString());
      throw e;
    }
  }


  @Override
  public RowResourceList getUpdates(TableResource table, String dataETag, String websafeResumeCursor)
      throws ClientWebException, InvalidAuthTokenException {

    String tableId = table.getTableId();
    URI uri;
    Resource resource;
    if ((table.getDataETag() == null) || dataETag == null) {
      uri = URI.create(table.getDataUri());
      resource = buildResource(uri);
    } else {
      uri = URI.create(table.getDiffUri());
      resource = buildResource(uri).queryParam(QUERY_DATA_ETAG, dataETag);
    }
    // and apply the cursor...
    if ( websafeResumeCursor != null ) {
      resource = resource.queryParam(CURSOR_PARAMETER, websafeResumeCursor);
    }
    RowResourceList rows;
    try {
      rows = resource.get(RowResourceList.class);
      return rows;
    } catch (ClientWebException e) {
      logger.error("Exception while requesting list of rows from server: " + tableId
          + " exception: " + e.toString());
      throw e;
    }
  }

  @Override
  public RowOutcomeList alterRows(TableResource resource,
      List<SyncRow> rowsToInsertUpdateOrDelete) throws ClientWebException, InvalidAuthTokenException {

    ArrayList<Row> rows = new ArrayList<Row>();
    for (SyncRow rowToAlter : rowsToInsertUpdateOrDelete) {
      Row row = Row.forUpdate(rowToAlter.getRowId(), rowToAlter.getRowETag(),
          rowToAlter.getFormId(), rowToAlter.getLocale(),
          rowToAlter.getSavepointType(), rowToAlter.getSavepointTimestamp(),
          rowToAlter.getSavepointCreator(), rowToAlter.getFilterScope(),
          rowToAlter.getValues());
      row.setDeleted(rowToAlter.isDeleted());
      rows.add(row);
    }
    RowList rlist = new RowList(rows, resource.getDataETag());

    URI uri = URI.create(resource.getDataUri());
    RowOutcomeList outcomes;
    try {
      outcomes = buildResource(uri).put(new EntityType<RowOutcomeList>() {
      }, rlist);
    } catch (ClientWebException e) {
      logger.error(
          "Exception while updating rows on server: " + resource.getTableId() + " exception: " + e.toString());
      throw e;
    }
    return outcomes;
  }

  private static List<String> filterOutTableIdAssetFiles(List<String> relativePaths) {
    List<String> newList = new ArrayList<String>();
    for (String relativePath : relativePaths) {
      if (relativePath.startsWith("assets/csv/")) {
        // by convention, the files here begin with their identifying tableId
        continue;
      } else {
        newList.add(relativePath);
      }
    }
    return newList;
  }

  /**
   * Remove all assets/*.init files (e.g., tables.init) that only alter the data
   * tables of the application. These are not needed on the server because their
   * actions have already caused changes in the data tables that will be shared
   * across all devices. I.e., they only need to be executed on the one starter
   * device, and then everything gets replicated everywhere else.
   * 
   * @param relativePaths
   * @return
   */
  private static List<String> filterOutAssetInitFiles(List<String> relativePaths) {
    List<String> newList = new ArrayList<String>();
    for (String relativePath : relativePaths) {
      if (relativePath.equals("assets/tables.init")) {
        continue;
      } else {
        newList.add(relativePath);
      }
    }
    return newList;
  }

  private static List<String> filterInTableIdFiles(List<String> relativePaths, String tableId) {
    List<String> newList = new ArrayList<String>();
    for (String relativePath : relativePaths) {
      if (relativePath.startsWith("assets/csv/")) {
        // by convention, the files here begin with their identifying tableId
        String[] parts = relativePath.split("/");
        if (parts.length >= 3) {
          String[] nameElements = parts[2].split("\\.");
          if (nameElements[0].equals(tableId)) {
            newList.add(relativePath);
          }
        }
      }
    }
    return newList;
  }

  /**
   * Get all the files under the given folder, excluding those directories that
   * are the concatenation of folder and a member of excluding. If the member of
   * excluding is a directory, none of its children will be synched either.
   * <p>
   * If the folder doesn't exist it returns an empty list.
   * <p>
   * If the file exists but is not a directory, logs an error and returns an
   * empty list.
   * 
   * @param folder
   * @param excluding
   *          can be null--nothing will be excluded. Should be relative to the
   *          given folder.
   * @param relativeTo
   *          the path to which the returned paths will be relative. A null
   *          value makes them relative to the folder parameter. If it is non
   *          null, folder must start with relativeTo, or else the files in
   *          folder could not possibly be relative to relativeTo. In this case
   *          will throw an IllegalArgumentException.
   * @return the relative paths of the files under the folder--i.e. the paths
   *         after the folder parameter, not including the first separator
   * @throws IllegalArgumentException
   *           if relativeTo is not a substring of folder.
   */
  private List<String> getAllFilesUnderFolder(File baseFolder,
      final Set<String> excludingNamedItemsUnderFolder) {
    String appName = ODKFileUtils.extractAppNameFromPath(baseFolder);

    // Return an empty list of the folder doesn't exist or is not a directory
    if (!baseFolder.exists()) {
      return new ArrayList<String>();
    } else if (!baseFolder.isDirectory()) {
      logger.error("[getAllFilesUnderFolder] folder is not a directory: " + baseFolder.getAbsolutePath());
      return new ArrayList<String>();
    }

    // construct the set of starting directories and files to process
    File[] partials = baseFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if (excludingNamedItemsUnderFolder == null) {
          return true;
        } else {
          return !excludingNamedItemsUnderFolder.contains(pathname.getName());
        }
      }
    });

    if (partials == null) {
      return Collections.emptyList();
    }

    LinkedList<File> unexploredDirs = new LinkedList<File>();
    List<File> nondirFiles = new ArrayList<File>();

    // copy the starting set into a queue of unexploredDirs
    // and a list of files to be sync'd
    for (int i = 0; i < partials.length; ++i) {
      if (partials[i].isDirectory()) {
        unexploredDirs.add(partials[i]);
      } else {
        nondirFiles.add(partials[i]);
      }
    }

    while (!unexploredDirs.isEmpty()) {
      File exploring = unexploredDirs.removeFirst();
      File[] files = exploring.listFiles();
      for (File f : files) {
        if (f.isDirectory()) {
          // we'll need to explore it
          unexploredDirs.add(f);
        } else {
          // we'll add it to our list of files.
          nondirFiles.add(f);
        }
      }
    }

    List<String> relativePaths = new ArrayList<String>();
    // we want the relative path, so drop the necessary bets.
    for (File f : nondirFiles) {
      // +1 to exclude the separator.
      relativePaths.add(ODKFileUtils.asRelativePath(appName, f));
    }
    return relativePaths;
  }

  @Override
  public boolean syncAppLevelFiles(boolean pushLocalFiles, String serverReportedAppLevelETag, SynchronizerStatus syncStatus)
      throws ClientWebException, InvalidAuthTokenException {
    // Get the app-level files on the server.
    syncStatus.updateNotification(SyncProgressState.APP_FILES, RString.getting_app_level_manifest,
        null, 1.0, false);
    List<OdkTablesFileManifestEntry> manifest = getAppLevelFileManifest(pushLocalFiles, serverReportedAppLevelETag);

    if (manifest == null) {
      logger.info("no change in app-leve manifest -- skipping!");
      // short-circuited -- no change in manifest
      syncStatus.updateNotification(SyncProgressState.APP_FILES,
          RString.getting_app_level_manifest, null, 100.0, false);
      return true;
    }

    // Get the app-level files on our device.
    Set<String> dirsToExclude = ODKFileUtils.getDirectoriesToExcludeFromSync(true);
    File appFolder = new File(ODKFileUtils.getAppFolder(appName));
    List<String> relativePathsOnDevice = getAllFilesUnderFolder(appFolder, dirsToExclude);
    relativePathsOnDevice = filterOutTableIdAssetFiles(relativePathsOnDevice);
    relativePathsOnDevice = filterOutAssetInitFiles(relativePathsOnDevice);

    boolean success = true;
    double stepSize = 100.0 / (1 + relativePathsOnDevice.size() + manifest.size());
    int stepCount = 1;

    if (pushLocalFiles) {
      // if we are pushing, we want to push the local files that are different
      // up to the server, then remove the files on the server that are not
      // in the local set.
      List<String> serverFilesToDelete = new ArrayList<String>();

      for (OdkTablesFileManifestEntry entry : manifest) {
        File localFile = ODKFileUtils.asAppFile(appName, entry.filename);
        if (!localFile.exists() || !localFile.isFile()) {
          // we need to delete this file from the server.
          serverFilesToDelete.add(entry.filename);
        } else if (ODKFileUtils.getMd5Hash(appName, localFile).equals(entry.md5hash)) {
          // we are ok -- no need to upload or delete
          relativePathsOnDevice.remove(entry.filename);
        }
      }

      // this is the actual step size when we are pushing...
      stepSize = 100.0 / (1 + relativePathsOnDevice.size() + serverFilesToDelete.size());

      for (String relativePath : relativePathsOnDevice) {

        syncStatus.updateNotification(SyncProgressState.APP_FILES, RString.uploading_local_file,
            new Object[] { relativePath }, stepCount * stepSize, false);

        File localFile = ODKFileUtils.asAppFile(appName, relativePath);
        String wholePathToFile = localFile.getAbsolutePath();
        if (!uploadFile(wholePathToFile, relativePath)) {
          success = false;
          logger.error("Unable to upload file to server: " + relativePath);
        }

        ++stepCount;
      }

      for (String relativePath : serverFilesToDelete) {

        syncStatus.updateNotification(SyncProgressState.APP_FILES,
            RString.deleting_file_on_server, new Object[] { relativePath }, stepCount * stepSize,
            false);

        if (!deleteFile(relativePath)) {
          success = false;
          logger.error("Unable to delete file on server: " + relativePath);
        }

        ++stepCount;
      }
    } else {
      // if we are pulling, we want to pull the server files that are different
      // down from the server, then remove the local files that are not present
      // on the server.

      for (OdkTablesFileManifestEntry entry : manifest) {
        syncStatus.updateNotification(SyncProgressState.APP_FILES, RString.verifying_local_file,
            new Object[] { entry.filename }, stepCount * stepSize, false);

        // make sure our copy is current
        compareAndDownloadFile(null, entry);
        // remove it from the set of app-level files we found before the sync
        relativePathsOnDevice.remove(entry.filename);

        // this is the corrected step size based upon matching files
        stepSize = 100.0 / (1 + relativePathsOnDevice.size() + manifest.size());

        ++stepCount;
      }

      for (String relativePath : relativePathsOnDevice) {

        syncStatus.updateNotification(SyncProgressState.APP_FILES, RString.deleting_local_file,
            new Object[] { relativePath }, stepCount * stepSize, false);

        // and remove any remaining files, as these do not match anything on
        // the server.
        File f = ODKFileUtils.asAppFile(appName, relativePath);
        if (!f.delete()) {
          success = false;
          logger.error("Unable to delete " + f.getAbsolutePath());
        }

        ++stepCount;
      }
    }

    return success;
  }

  private String determineContentType(String fileName) {
    int ext = fileName.lastIndexOf('.');
    if (ext == -1) {
      return "application/octet-stream";
    }
    String type = fileName.substring(ext + 1);
    String mimeType = mimeMapping.get(type);
    if (mimeType == null) {
      return "application/octet-stream";
    }
    return mimeType;
  }

  @Override
  public void syncTableLevelFiles(String tableId, String serverReportedTableLevelETag, OnTablePropertiesChanged onChange,
      boolean pushLocalFiles, SynchronizerStatus syncStatus) throws ClientWebException, InvalidAuthTokenException {

    syncStatus.updateNotification(SyncProgressState.TABLE_FILES, RString.getting_table_manifest,
        new Object[] { tableId }, 1.0, false);

    // get the table files on the server
    List<OdkTablesFileManifestEntry> manifest = getTableLevelFileManifest(tableId, serverReportedTableLevelETag, pushLocalFiles);

    if (manifest == null) {
      logger.info("no change in table manifest -- skipping!");
      // short-circuit because our files should match those on the server
      syncStatus.updateNotification(SyncProgressState.TABLE_FILES, RString.getting_table_manifest,
          new Object[] { tableId }, 100.0, false);

      return;
    }

    String tableIdPropertiesFile = "tables" + File.separator + tableId + File.separator
        + "properties.csv";

    boolean tablePropertiesChanged = false;

    // Get any assets/csv files that begin with tableId
    Set<String> dirsToExclude = new HashSet<String>();
    File assetsCsvFolder = new File(ODKFileUtils.getAssetsCsvFolder(appName));
    List<String> relativePathsToTableIdAssetsCsvOnDevice = getAllFilesUnderFolder(assetsCsvFolder,
        dirsToExclude);
    relativePathsToTableIdAssetsCsvOnDevice = filterInTableIdFiles(
        relativePathsToTableIdAssetsCsvOnDevice, tableId);

    // We don't want to sync anything in the instances directory, because this
    // contains things like media attachments.
    File tableFolder = new File(ODKFileUtils.getTablesFolder(appName, tableId));
    dirsToExclude.add(ODKFileUtils.INSTANCES_FOLDER_NAME);
    List<String> relativePathsOnDevice = getAllFilesUnderFolder(tableFolder, dirsToExclude);

    // mix in the assets files for this tableId, if any...
    relativePathsOnDevice.addAll(relativePathsToTableIdAssetsCsvOnDevice);

    double stepSize = 100.0 / (1 + relativePathsOnDevice.size() + manifest.size());
    int stepCount = 1;

    if (pushLocalFiles) {
      // if we are pushing, we want to push the local files that are different
      // up to the server, then remove the files on the server that are not
      // in the local set.
      List<String> serverFilesToDelete = new ArrayList<String>();

      for (OdkTablesFileManifestEntry entry : manifest) {
        File localFile = ODKFileUtils.asAppFile(appName, entry.filename);
        if (!localFile.exists() || !localFile.isFile()) {
          // we need to delete this file from the server.
          serverFilesToDelete.add(entry.filename);
        } else if (ODKFileUtils.getMd5Hash(appName, localFile).equals(entry.md5hash)) {
          // we are ok -- no need to upload or delete
          relativePathsOnDevice.remove(entry.filename);
        }
      }

      // this is the actual step size when we are pushing...
      stepSize = 100.0 / (1 + relativePathsOnDevice.size() + serverFilesToDelete.size());

      boolean success = true;
      for (String relativePath : relativePathsOnDevice) {

        syncStatus.updateNotification(SyncProgressState.TABLE_FILES, RString.uploading_local_file,
            new Object[] { relativePath }, stepCount * stepSize, false);

        File localFile = ODKFileUtils.asAppFile(appName, relativePath);
        String wholePathToFile = localFile.getAbsolutePath();
        if (!uploadFile(wholePathToFile, relativePath)) {
          success = false;
          logger.error("Unable to upload file to server: " + relativePath);
        }

        ++stepCount;
      }

      for (String relativePath : serverFilesToDelete) {

        syncStatus.updateNotification(SyncProgressState.TABLE_FILES,
            RString.deleting_file_on_server, new Object[] { relativePath }, stepCount * stepSize,
            false);

        if (!deleteFile(relativePath)) {
          success = false;
          logger.error("Unable to delete file on server: " + relativePath);
        }

        ++stepCount;
      }

      if (!success) {
        logger.info("unable to delete one or more files!");
      }

    } else {
      // if we are pulling, we want to pull the server files that are different
      // down from the server, then remove the local files that are not present
      // on the server.

      for (OdkTablesFileManifestEntry entry : manifest) {

        syncStatus.updateNotification(SyncProgressState.TABLE_FILES, RString.verifying_local_file,
            new Object[] { entry.filename }, stepCount * stepSize, false);

        // make sure our copy is current
        boolean outcome = compareAndDownloadFile(tableId, entry);
        // and if it was the table properties file, remember whether it changed.
        if (entry.filename.equals(tableIdPropertiesFile)) {
          tablePropertiesChanged = outcome;
        }
        // remove it from the set of app-level files we found before the sync
        relativePathsOnDevice.remove(entry.filename);

        // this is the corrected step size based upon matching files
        stepSize = 100.0 / (1 + relativePathsOnDevice.size() + manifest.size());

        ++stepCount;
      }

      boolean success = true;
      for (String relativePath : relativePathsOnDevice) {

        syncStatus.updateNotification(SyncProgressState.TABLE_FILES, RString.deleting_local_file,
            new Object[] { relativePath }, stepCount * stepSize, false);

        // and remove any remaining files, as these do not match anything on
        // the server.
        File f = ODKFileUtils.asAppFile(appName, relativePath);
        if (!f.delete()) {
          success = false;
          logger.error("Unable to delete " + f.getAbsolutePath());
        }

        ++stepCount;
      }

      if (tablePropertiesChanged && (onChange != null)) {
        // update this table's KVS values...
        onChange.onTablePropertiesChanged(tableId);
      }

      if (!success) {
        logger.info("unable to delete one or more files!");
      }

      // should we return our status?
    }
  }

  public List<OdkTablesFileManifestEntry> getAppLevelFileManifest(boolean pushLocalFiles, String serverReportedAppLevelETag)
      throws ClientWebException, InvalidAuthTokenException {
    SyncETagsUtils seu = new SyncETagsUtils();
    URI fileManifestUri = normalizeUri(aggregateUri, getManifestUriFragment());
    String eTag = seu.getManifestSyncETag(context, appName, fileManifestUri, null);
    Resource rsc = buildResource(fileManifestUri);
    // don't short-circuit manifest if we are pushing local files,
    // as we need to know exactly what is on the server to minimize
    // transmissions of files being pushed up to the server.
    if (!pushLocalFiles && eTag != null) {
      rsc.header(HttpHeaders.IF_NONE_MATCH, eTag);
      if ( serverReportedAppLevelETag != null && serverReportedAppLevelETag.equals(eTag) ) {
        // no change -- we can skip the request to the server
        return null;
      }
    }
    ClientResponse rsp = rsc.get();
    if (rsp.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      // signal this by returning null;
      return null;
    }
    if (rsp.getStatusCode() < 200 || rsp.getStatusCode() >= 300) {
      throw new ClientWebException(null, rsp);
    }
    if (!rsp.getHeaders().containsKey(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER) ) {
      throw new ClientWebException(null, rsp);
    }

    // retrieve the manifest...
    OdkTablesFileManifest manifest;
    manifest = rsp.getEntity(OdkTablesFileManifest.class);
    List<OdkTablesFileManifestEntry> theList = null;
    if (manifest != null) {
      theList = manifest.getFiles();
    }
    if (theList == null) {
      theList = Collections.emptyList();
    }
    // update the manifest ETag record...
    eTag = rsp.getHeaders().getFirst(HttpHeaders.ETAG);
    seu.updateManifestSyncETag(context, appName, fileManifestUri, null, eTag);
    // and return the list of values...
    return theList;
  }

  public List<OdkTablesFileManifestEntry> getTableLevelFileManifest(String tableId, String serverReportedTableLevelETag,
      boolean pushLocalFiles) throws ClientWebException, InvalidAuthTokenException {
    SyncETagsUtils seu = new SyncETagsUtils();
    URI fileManifestUri = normalizeUri(aggregateUri, getManifestUriFragment() + tableId);
    String eTag = seu.getManifestSyncETag(context, appName, fileManifestUri, tableId);
    Resource rsc = buildResource(fileManifestUri);
    // don't short-circuit manifest if we are pushing local files,
    // as we need to know exactly what is on the server to minimize
    // transmissions of files being pushed up to the server.
    if (!pushLocalFiles && eTag != null) {
      rsc.header(HttpHeaders.IF_NONE_MATCH, eTag);
      if ( serverReportedTableLevelETag != null && serverReportedTableLevelETag.equals(eTag) ) {
        // no change -- we can skip the request to the server
        return null;
      }
    }
    ClientResponse rsp = rsc.get();
    if (rsp.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      // signal this by returning null;
      return null;
    }
    if (rsp.getStatusCode() < 200 || rsp.getStatusCode() >= 300) {
      throw new ClientWebException(null, rsp);
    }
    if (!rsp.getHeaders().containsKey(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER) ) {
      throw new ClientWebException(null, rsp);
    }

    // retrieve the manifest...
    OdkTablesFileManifest manifest;
    manifest = rsp.getEntity(OdkTablesFileManifest.class);
    List<OdkTablesFileManifestEntry> theList = null;
    if (manifest != null) {
      theList = manifest.getFiles();
    }
    if (theList == null) {
      theList = Collections.emptyList();
    }
    // update the manifest ETag record...
    eTag = rsp.getHeaders().getFirst(HttpHeaders.ETAG);
    seu.updateManifestSyncETag(context, appName, fileManifestUri, tableId, eTag);
    // and return the list of values...
    return theList;
  }

  private boolean deleteFile(String pathRelativeToAppFolder) throws ClientWebException, InvalidAuthTokenException {
    String escapedPath = uriEncodeSegments(pathRelativeToAppFolder);
    URI filesUri = normalizeUri(aggregateUri, getFilePathURI() + escapedPath);
    logger.info("[deleteFile] fileDeleteUri: " + filesUri.toString());
    buildResource(filesUri).delete();
    // TODO: verify whether or not this worked.
    return true;
  }

  private boolean uploadFile(String wholePathToFile, String pathRelativeToAppFolder) throws InvalidAuthTokenException {
    File file = new File(wholePathToFile);
    String escapedPath = uriEncodeSegments(pathRelativeToAppFolder);
    URI filesUri = normalizeUri(aggregateUri, getFilePathURI() + escapedPath);
    logger.info("[uploadFile] filePostUri: " + filesUri.toString());
    String ct = determineContentType(file.getName());
    MediaType contentType = MediaType.valueOf(ct);
    ClientResponse response = buildResource(filesUri, contentType).post(file);
    if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
      return false;
    }
    if ( !response.getHeaders().containsKey(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER)) {
      return false;
    }
    return true;
  }

  private boolean uploadInstanceFile(File file, URI instanceFileUri) throws InvalidAuthTokenException {
    logger.info("[uploadFile] filePostUri: " + instanceFileUri.toString());
    String ct = determineContentType(file.getName());
    MediaType contentType = MediaType.valueOf(ct);
    ClientResponse response = buildResource(instanceFileUri, contentType).post(file);
    if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
      return false;
    }
    if ( !response.getHeaders().containsKey(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER)) {
      return false;
    }
    return true;
  }

  /**
   * Get the URI to which to post in order to upload the file.
   *
   * @param pathRelativeToAppFolder
   * @return
   */
  public URI getFilePostUri(String appName, String pathRelativeToAppFolder) {
    String escapedPath = uriEncodeSegments(pathRelativeToAppFolder);
    URI filesUri = normalizeUri(aggregateUri, getFilePathURI() + escapedPath);
    return filesUri;
  }

  /**
   *
   * @param entry
   * @return
   */
  private boolean compareAndDownloadFile(String tableId, OdkTablesFileManifestEntry entry) {
    String basePath = ODKFileUtils.getAppFolder(appName);

    SyncETagsUtils seu = new SyncETagsUtils();

    // if the file is a placeholder on the server, then don't do anything...
    if (entry.contentLength == 0) {
      return false;
    }
    // now we need to look through the manifest and see where the files are
    // supposed to be stored. Make sure you don't return a bad string.
    if (entry.filename == null || entry.filename.equals("")) {
      logger.info("returned a null or empty filename");
      return false;
    } else {

      URI uri = null;
      URL urlFile = null;
      try {
        logger.info("[downloadFile] downloading at url: " + entry.downloadUrl);
        urlFile = new URL(entry.downloadUrl);
        uri = urlFile.toURI();
      } catch (MalformedURLException e) {
        logger.error(e.toString());
        e.printStackTrace();;
        return false;
      } catch (URISyntaxException e) {
        logger.error(e.toString());
        e.printStackTrace();;
        return false;
      }

      // filename is the unrooted path of the file, so prepend the basepath.
      String path = basePath + File.separator + entry.filename;
      // Before we try dl'ing the file, we have to make the folder,
      // b/c otherwise if the folders down to the path have too many non-
      // existent folders, we'll get a FileNotFoundException when we open
      // the FileOutputStream.
      File newFile = new File(path);
      String folderPath = newFile.getParent();
      ODKFileUtils.createFolder(folderPath);
      if (!newFile.exists()) {
        // the file doesn't exist on the system
        // filesToDL.add(newFile);
        try {
          int statusCode = downloadFile(newFile, uri);
          if (statusCode == HttpStatus.SC_OK) {
            seu.updateFileSyncETag(context, appName, uri, tableId, newFile.lastModified(),
                entry.md5hash);
            return true;
          } else {
            return false;
          }
        } catch (Exception e) {
          e.printStackTrace();;
          logger.error("trouble downloading file for first time");
          return false;
        }
      } else {
        boolean hasUpToDateEntry = true;
        String md5hash = seu
            .getFileSyncETag(context, appName, uri, tableId, newFile.lastModified());
        if (md5hash == null) {
          // file exists, but no record of what is on the server
          // compute local value
          hasUpToDateEntry = false;
          md5hash = ODKFileUtils.getMd5Hash(appName, newFile);
        }
        // so as it comes down from the manifest, the md5 hash includes a
        // "md5:" prefix. Add that and then check.
        if (!md5hash.equals(entry.md5hash)) {
          hasUpToDateEntry = false;
          // it's not up to date, we need to download it.
          try {
            int statusCode = downloadFile(newFile, uri);
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_MODIFIED) {
              seu.updateFileSyncETag(context, appName, uri, tableId, newFile.lastModified(),
                  md5hash);
              return true;
            } else {
              return false;
            }
          } catch (Exception e) {
            e.printStackTrace();;
            // TODO throw correct exception
            logger.error("trouble downloading new version of existing file");
            return false;
          }
        } else {
          if (!hasUpToDateEntry) {
            seu.updateFileSyncETag(context, appName, uri, tableId, newFile.lastModified(), md5hash);
          }
          // no change
          return false;
        }
      }
    }
  }

  /**
   *
   * @param destFile
   * @param downloadUrl
   * @return true if the download was successful
   * @throws Exception
   */
  private int downloadFile(File destFile, URI downloadUrl) throws Exception {

    // WiFi network connections can be renegotiated during a large form download
    // sequence.
    // This will cause intermittent download failures. Silently retry once after
    // each
    // failure. Only if there are two consecutive failures, do we abort.
    boolean success = false;
    int attemptCount = 0;
    while (!success && attemptCount++ <= 2) {

      Resource resource = buildFileDownloadResource(downloadUrl);
      if ( destFile.exists() ) {
        String md5Hash = ODKFileUtils.getMd5Hash(appName, destFile);
        resource.header(HttpHeaders.IF_NONE_MATCH, md5Hash);
      }
      
      ClientResponse response = null;
      try {
        response = resource.get();
        int statusCode = response.getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
          response.consumeContent();
          if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            // clear the cookies -- should not be necessary?
            // ss: might just be a collect thing?
          }
          logger.warn("downloading " + downloadUrl.toString() + " returns " + statusCode);
          return statusCode;
        }
        
        if (!response.getHeaders().containsKey(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER)) {
          response.consumeContent();
          logger.warn("downloading " + downloadUrl.toString() + " appears to have been redirected.");
          return 302;
        }
        
        File tmp = new File(destFile.getParentFile(), destFile.getName() + ".tmp");
        int totalLen = 0;
        InputStream is = null;
        BufferedOutputStream os = null;
        try {
          // open the InputStream of the (uncompressed) entity body...
          is = response.getEntity(InputStream.class);
          os = new BufferedOutputStream(new FileOutputStream(tmp));
          
          // write connection to temporary file
          byte buf[] = new byte[8192];
          int len;
          while ((len = is.read(buf, 0, buf.length)) >= 0) {
            if (len != 0) {
              totalLen += len;
              os.write(buf, 0, len);
            }
          }
          is.close();
          is = null;
          
          os.flush();
          os.close();
          os = null;

          success = tmp.renameTo(destFile);
        } catch (Exception e) {
          // most likely a socket timeout
          e.printStackTrace();
          logger.error( "downloading " + downloadUrl.toString() + " failed after " + totalLen + " bytes: " + e.toString());
          try {
            // signal to the framework that this socket is hosed.
            // with the various nested streams, this may not work...
            is.reset();
          } catch ( Exception ex ) {
            // ignore
          }
          throw e;
        } finally {
          if (os != null) {
            try {
              os.close();
            } catch (Exception e) {
              // no-op
            }
          }
          if (is != null) {
            try {
              // ensure stream is consumed...
              byte buf[] = new byte[8192];
              while (is.read(buf) >= 0)
                ;
            } catch (Exception e) {
              // no-op
            }
            try {
              is.close();
            } catch (Exception e) {
              // no-op
            }
          }
          if (tmp.exists()) {
            tmp.delete();
          }
          response.consumeContent();
        }
      } catch (ClientWebException e) {
        e.printStackTrace();;
        if ( response != null ) {
          response.consumeContent();
        }
        if (attemptCount != 1) {
          throw e;
        }
      }
    }
    return HttpStatus.SC_OK;
  }

  private static final class CommonFileAttachmentTerms {
    File localFile;
    URI instanceFileDownloadUri;
  }

  private CommonFileAttachmentTerms computeCommonFileAttachmentTerms(String instanceFileUri, String instanceId, File instanceFolder, String relativePath) {
    // clean up the value...
    if ( relativePath.startsWith("/") ) {
      relativePath = relativePath.substring(1);
    }
    
    File localFile = ODKFileUtils.getAsFile(appName, relativePath);
    String baseInstanceFolder = instanceFolder.getAbsolutePath();
    String baseLocalAttachment = localFile.getAbsolutePath();
    if ( !baseLocalAttachment.startsWith(baseInstanceFolder) ) {
      throw new IllegalStateException("instance data file is not within the instances tree!");
    }
    String partialValue = baseLocalAttachment.substring(baseInstanceFolder.length());
    if (partialValue.startsWith("/") ) {
      partialValue = partialValue.substring(1);
    }
    
    URI instanceFileDownloadUri = normalizeUri(instanceFileUri, instanceId + "/file/"
        + partialValue);

  
    CommonFileAttachmentTerms cat = new CommonFileAttachmentTerms();
    cat.localFile = localFile;
    cat.instanceFileDownloadUri = instanceFileDownloadUri;
    
    return cat;
  }
  
  @Override
  public boolean getFileAttachments(String instanceFileUri, String tableId, SyncRowPending serverRow,
      boolean deferInstanceAttachments) throws ClientWebException {

    if (serverRow.getUriFragments().isEmpty()) {
      throw new IllegalStateException("should never get here!");
    }
    
    SyncETagsUtils seu = new SyncETagsUtils();

    /**********************************************
     * 
     * 
     * 
     * 
     * With the new mechanism, we can directly fetch any file from the server
     * in the fileAttachmentColumns. And we can include any md5Hash we might
     * have of the local file. This would enable the server to say
     * "yes, it is identical".
     */
    boolean success = true;
    try {
      // 1) Get this row's instanceId (rowId)
      String instanceId = serverRow.getRowId();
      
      // 2) Get the folder holding the instance attachments
      File instanceFolder = new File(ODKFileUtils.getInstanceFolder(appName, tableId, instanceId));

      // 3) get all the files in that folder...
      List<String> relativePathsToAppFolderOnDevice = getAllFilesUnderFolder(
          instanceFolder, null);

      // 4) Iterate over all non-null file attachments in the data row
      for (String relativePath : serverRow.getUriFragments()) {
        // clean up the value...
        if ( relativePath.startsWith("/") ) {
          relativePath = relativePath.substring(1);
        }
        
        // remove it from the local files list
        relativePathsToAppFolderOnDevice.remove(relativePath);
        
        CommonFileAttachmentTerms cat = computeCommonFileAttachmentTerms(instanceFileUri, instanceId, instanceFolder, relativePath);

        if (!cat.localFile.exists()) {

          if (deferInstanceAttachments) {
            return false;
          }

          int statusCode = downloadFile(cat.localFile, cat.instanceFileDownloadUri);
          if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_MODIFIED) { 
            String md5Hash = ODKFileUtils.getMd5Hash(appName, cat.localFile);
            seu.updateFileSyncETag(context, appName, cat.instanceFileDownloadUri, tableId,
                cat.localFile.lastModified(), md5Hash);
          } else {
            success = false;
          }
        } else {
          // assume that if the local file exists, it matches exactly the
          // content on the server.
          // this could be inaccurate if there are buggy programs on the
          // device!
          String md5hash = seu.getFileSyncETag(context, appName, cat.instanceFileDownloadUri,
              tableId, cat.localFile.lastModified());
          if (md5hash == null) {
            md5hash = ODKFileUtils.getMd5Hash(appName, cat.localFile);
            seu.updateFileSyncETag(context, appName, cat.instanceFileDownloadUri, tableId,
                cat.localFile.lastModified(), md5hash);
          }
        }
      }

      // we usually do this, but, when we have a conflict row, we pull the
      // server files down, and leave the local files. Upon the next sync,
      // we will resolve what to do and clean up.
      if (serverRow.shouldDeleteExtraneousLocalFiles()) {
        for (String relativePath : relativePathsToAppFolderOnDevice) {
          // remove local files that are not on server...
          File localFile = ODKFileUtils.asAppFile(appName, relativePath);
          if (!localFile.delete()) {
            success = false;
          }
        }
      }
      return success;
    } catch (ClientWebException e) {
      logger.error("Exception while getting attachment: " + e.toString());
      throw e;
    } catch (Exception e) {
      e.printStackTrace();;
      return false;
    }
    /******************************************************
     * End of the new file attachment mechanism...
     *
     * 
     * 
     * 
     * 
     */
  }

  @Override
  public boolean putFileAttachments(String instanceFileUri, String tableId, SyncRowPending localRow,
      boolean deferInstanceAttachments) throws ClientWebException {

    if (localRow.getUriFragments().isEmpty()) {
      throw new IllegalStateException("should never get here!");
    }

    /**********************************************
     * 
     * 
     * 
     * 
     * With the new mechanism, we can directly fetch any file from the server
     * in the fileAttachmentColumns. For PUT, we don't know if the local file
     * exists on the server, so we need to PUT every attachment. This can be 
     * done by retrieving the manifest, and comparing that against the local 
     * directory, or we can issue an if-none-match GET request for each file 
     * we need. If we do not get a NOT_MODIFIED return, then we upload it. 
     */
    boolean success = true;
    try {
      // 1) Get this row's instanceId (rowId)
      String instanceId = localRow.getRowId();
      
      // 2) Get the folder holding the instance attachments
      File instanceFolder = new File(ODKFileUtils.getInstanceFolder(appName, tableId, instanceId));

      // 3) Iterate over all non-null file attachments in the data row
      for (String relativePath : localRow.getUriFragments()) {
        // clean up the value...
        if (relativePath.startsWith("/")) {
          relativePath = relativePath.substring(1);
        }
        
        CommonFileAttachmentTerms cat = computeCommonFileAttachmentTerms(instanceFileUri, instanceId, instanceFolder, relativePath);

        if (cat.localFile.exists()) {

          // issue a GET. If the return is NOT_MODIFIED, then we don't need to
          // POST it.
          int statusCode = downloadFile(cat.localFile, cat.instanceFileDownloadUri);
          if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
            // no-op... what is on server matches local.
          } else if (statusCode == HttpStatus.SC_OK) {
            // The test for ODK header ensures we detect wifi login overlays
            // this should not happen -- indicates something is corrupted.
            logger.error(
                "Unexpectedly overwriting attachment: " + cat.instanceFileDownloadUri.toString());
          } else if (statusCode == HttpStatus.SC_NOT_FOUND
              || statusCode == HttpStatus.SC_NO_CONTENT) {

            if (deferInstanceAttachments) {
              return false;
            }

            // upload it...
            boolean outcome = uploadInstanceFile(cat.localFile, cat.instanceFileDownloadUri);
            if (!outcome) {
              success = false;
            }
          } else {
            success = false;
          }
        } else {
          // we will pull these files later during a getFileAttachments() call,
          // if needed...
        }
      }
      return success;
    } catch (ClientWebException e) {
      logger.error("Exception while putting attachment: " + e.toString());
      throw e;
    } catch (Exception e) {
      logger.error("Exception during sync: " + e.toString());
      return false;
    }
    /******************************************************
     * End of the new file attachment mechanism...
     *
     * 
     * 
     * 
     * 
     */
  }
}
