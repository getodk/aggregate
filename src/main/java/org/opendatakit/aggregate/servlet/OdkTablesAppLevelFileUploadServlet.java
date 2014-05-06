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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.impl.api.ServiceUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * This is the servlet that handles the uploading of files that are associted
 * with ODKTables tables. It is modeled off of FormUploadServlet.java.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesAppLevelFileUploadServlet extends ServletUtilBase {

  private static final long serialVersionUID = -1173762226947584151L;

  private static final Log logger = LogFactory.getLog(OdkTablesAppLevelFileUploadServlet.class);

  private static final String ADDR = UIConsts.APP_LEVEL_FILE_UPLOAD_SERVLET_ADDR;

  /**
   * Title for generated webpage.
   */
  public static final String TITLE_INFO = "OdkTables Application Level File Upload";

  private static final String UPLOAD_PAGE_BODY_START =

  "<div style=\"overflow: auto;\"><p id=\"subHeading\"><h2>Upload a file not associated with a specific tableId</h2></p>"
      + "<!--[if true]><p style=\"color: red;\">For a better user experience, use Chrome, Firefox or Safari</p>"
      + "<![endif] -->"
      + "<form id=\"ie_backward_compatible_form\""
      + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
      + " action=\"";// emit the ADDR

  private String UPLOAD_PAGE_BODY_MIDDLE_1 = "\">" + "	  <table id=\"uploadTable\">" + "		<tr>"
  /*
   * +
   * "			<td><label for=\"table_name\">Table Name (must be exactly as appears):</label></td>"
   * +
   * "			<td><input id=\"table_name\" type=\"text\" name=\"table_name\" /></td>"
   */+ "		</tr>\n" + "      <td><label for=\"table_name\">Select Table</label></td>" + "      <td>";
  private String UPLOAD_PAGE_BODY_MIDDLE_2 = "</td>"
      + "		<tr>"
      + "			<td><label for=\"file_key\">File Key (list, box, etc):</label></td>"
      + "			<td><input id=\"file_key\" type=\"text\" name=\"file_key\" /></td>"
      + "		</tr>\n"
      + "	  	<tr>"
      + "	  		<td><label for=\"table_file\">File:</label></td>"
      + "	  		<td><input id=\"table_file\" type=\"file\" size=\"80\" class=\"gwt-Button\""
      + "	  			name=\"table_file\" /></td>"
      + "	  	</tr>\n"
      + "	  	<tr>"
      + "	  		<td><label for=\"mediaFiles\">Optional Media file(s):</label></td>"
      + "	  		<td><input id=\"mediaFiles\" class=\"gwt-Button\" type=\"file\" size=\"80,20\" name=\"datafile\" multiple /><input id=\"clear_media_files\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles')\" /></td>"
      + "	  	</tr>"
      + "    <tr>"
      + "      <td><label for=\"path_prefix\">Path Prefix for Media Files</td>"
      + "      <td><input id=\"path_prefix\" type=\"text\" name=\"path_prefix\" /></td>"
      + "      </tr>\n"
      + "	  	<!--[if true]>"
      + "	      <tr>"
      + "	          <td><label for=\"mediaFiles2\">Media file #2:</label></td>"
      + "	          <td><input id=\"mediaFiles2\" class=\"gwt-Button\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files2\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles2')\" /></td>"
      + "	      </tr>"
      + "	      <tr>"
      + "	          <td><label for=\"mediaFiles3\">Media file #3:</label></td>"
      + "	          <td><input id=\"mediaFiles3\" class=\"gwt-Button\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files3\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles3')\" /></td>"
      + "	      </tr>"
      + "	      <tr>"
      + "	          <td><label for=\"mediaFiles4\">Media file #4:</label></td>"
      + "	          <td><input id=\"mediaFiles4\" class=\"gwt-Button\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files4\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles4')\" /></td>"
      + "	      </tr>"
      + "	      <tr>"
      + "	          <td><label for=\"mediaFiles5\">Media file #5:</label></td>"
      + "	          <td><input id=\"mediaFiles5\" class=\"gwt-Button\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files5\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles5')\" /></td>"
      + "	      </tr>"
      + "	      <tr>"
      + "	          <td><label for=\"mediaFiles6\">Media file #6:</label></td>"
      + "	          <td><input id=\"mediaFiles6\" class=\"gwt-Button\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files6\" type=\"button\" class=\"gwt-Button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles6')\" /></td>"
      + "	      </tr>"
      + "	      <![endif]-->\n"
      + "	  	<tr>"
      + "	  		<td><input type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Upload Form\" /></td>"
      + "	  		<td />" + "	  	</tr>" + "	  </table>\n" + "	  </form>"
      + "<p>Media files that are necessary for any of the uploaded files "
      + "(if any) should be in a single directory without subdirectories.</p>" + "<br><br>";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServiceUtils.examineRequest(getServletContext(), req);

    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // TODO deal with the javarosa stuff, as in FormUploadServlet's
    // corresponding method

    StringBuilder headerString = new StringBuilder();
    headerString.append("<script type=\"application/javascript\" src=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_SCRIPT_RESOURCE));
    headerString.append("\"></script>");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_BUTTON_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.AGGREGATE_STYLE));
    headerString.append("\" />");

    // header info
    beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
    addOpenDataKitHeaders(resp);
    PrintWriter out = resp.getWriter();
    out.write(UPLOAD_PAGE_BODY_START);
    out.write(cc.getWebApplicationURL(ADDR));
    // try {
    // String body_middle = UPLOAD_PAGE_BODY_MIDDLE_1 +
    // getSelectTableHtml(cc) + UPLOAD_PAGE_BODY_MIDDLE_2;
    // out.write(body_middle);
    // } catch (ODKDatastoreException e) {
    // //TODO fix this
    // e.printStackTrace();
    // }
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP head request. This is used to verify that channel security
   * and authentication have been properly established when uploading form
   * definitions via a program (e.g., Briefcase).
   */
  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServiceUtils.examineRequest(getServletContext(), req);
    @SuppressWarnings("unused")
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    logger.info("Inside doHead");
    addOpenDataKitHeaders(resp);
    resp.setStatus(204); // no content...
  }

  /**
   * Handler for HTTP Post request that takes a file and adds it to the
   * datastore.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServiceUtils.examineRequest(getServletContext(), req);
    // //TODO handle the logging stuff in the friendlytable_log table, that I
    // need to
    // // create. it also apparently matters which of the tables is written
    // first, so
    // // be sure to look how Dylan does it first.
    //
    // // will let the MultiPartFormData files, odk's custom parsing library,
    // // handle the parsing.
    //
    // CallingContext cc = ContextFactory.getCallingContext(this, req);
    //
    // // verify the requist is multipart, which you have set above in
    // // UPLOAD_PAGE_BODY_START
    // if (!ServletFileUpload.isMultipartContent(req)) {
    // resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
    // ErrorConsts.NO_MULTI_PART_CONTENT);
    // return;
    // }
    //
    // try {
    // // process the form
    // MultiPartFormData uploadedFormItems = new MultiPartFormData(req);
    //
    // // This will house all of the files being uploaded at this time.
    // Set<Map.Entry<String,MultiPartFormItem>> fileSet =
    // uploadedFormItems.getFileNameEntrySet();
    //
    //
    // // "table_file" is what the input was called in the table above. not sure
    // // if this is actually what i want it to be here.
    // MultiPartFormItem tableFile = uploadedFormItems
    // .getFormDataByFieldName("table_file");
    //
    // String tableName = uploadedFormItems
    // .getSimpleFormField("table_name");
    //
    // String fileKey = uploadedFormItems
    // .getSimpleFormField("file_key");
    //
    // String pathPrefix = uploadedFormItems
    // .getSimpleFormField("path_prefix");
    //
    // addOpenDataKitHeaders(resp);
    // resp.setStatus(HttpServletResponse.SC_CREATED);
    // resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
    // resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    //
    // // first we need to get the id of the table.
    // String tableId = getTableId(tableName, cc);
    //
    // // This will store all of the new entities we are adding. They are
    // // held in this list first so that we can construct them as they
    // // would be exist in the database and then check for conflicts.
    // // If there are no conflicts, you move through the entities and
    // // call put() to persist them. If you are uploading huge numbers
    // // if files, might this be a memory limitation?
    // List<Entity> newEntities = new ArrayList<Entity>();
    //
    // DbTableFiles fileBlobSet = new DbTableFiles(cc);
    // BlobEntitySet instance = fileBlobSet.newBlobEntitySet(cc);
    // // TODO: for now this is the filename, but eventually i will want to have
    // another
    // // input field that is "path prefix" that will tell you how to prefix
    // // the file additions.
    // instance.addBlob(tableFile.getStream().toByteArray(),
    // tableFile.getContentType(), tableFile.getFilename(), false, cc);
    //
    // // now we want to put the pertinent information into the user-friendly
    // table.
    // // why he didn't make this a util class I do not know.
    // EntityCreator ec = new EntityCreator();
    // Entity newRow = ec.newTableFileInfoEntity(tableId,
    // DbTableFileInfo.Type.FILE.name,
    // fileKey, instance.getUri(), false, cc);
    // newEntities.add(newRow);
    //
    // // now let's handle the media files.
    // // This will be the suffix for the media files.
    // int i = 1;
    // for (Map.Entry<String,MultiPartFormItem> itm : fileSet) {
    // if (itm.getKey().equals(tableFile.getFilename()))
    // continue; // ignore the file name, which we have already added
    // fileBlobSet = new DbTableFiles(cc);
    // instance = fileBlobSet.newBlobEntitySet(cc);
    // // TODO: for now this is the filename, but eventually i will want to have
    // another
    // // input field that is "path prefix" that will tell you how to prefix
    // // the file additions.
    // String tempFileName;
    // if (pathPrefix.equals("")) {
    // tempFileName = itm.getValue().getFilename();
    // } else {
    // tempFileName = pathPrefix + "/" + itm.getValue().getFilename();
    // }
    //
    // instance.addBlob(itm.getValue().getStream().toByteArray(),
    // itm.getValue().getContentType(), tempFileName, false, cc);
    //
    // // now we want to put the pertinent information into the user-friendly
    // table.
    // String tempFileKey = fileKey + "_" + String.valueOf(i);
    // // why he didn't make this a util class I do not know.
    // newRow = ec.newTableFileInfoEntity(tableId,
    // DbTableFileInfo.Type.FILE.name,
    // tempFileKey, instance.getUri(), true, cc);
    // newEntities.add(newRow);
    // i++;
    // }
    //
    // // now check for collisions.
    // List<Row> existingRows = getFileRows(req, tableId);
    // List<Row> newRows = EntityConverter.toRowsFromFileInfo(newEntities);
    // List<String> conflictingKeys = getDuplicateKeys(existingRows,
    // newRows);
    // List<String> conflictingFiles = getDuplicateFileNames(cc,
    // existingRows, newRows);
    // // first and foremost, you have to have a key.
    // fileKey = uploadedFormItems.getSimpleFormField("file_key");
    // if (tableFile.getFilename().equals("")) {
    // PrintWriter out = resp.getWriter();
    // out.write(HtmlConsts.HTML_OPEN);
    // out.write(HtmlConsts.BODY_OPEN);
    // out.write("<p>No files have been uploaded!</p>");
    // out.write("<p>You must select a file to upload.</p>");
    // out.write("<p>Click ");
    // out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here",
    // false));
    // out.write(" to return try again.</p>");
    // out.write(HtmlConsts.BODY_CLOSE);
    // out.write(HtmlConsts.HTML_CLOSE);
    // } else if (fileKey.equals("")) {
    // PrintWriter out = resp.getWriter();
    // out.write(HtmlConsts.HTML_OPEN);
    // out.write(HtmlConsts.BODY_OPEN);
    // out.write("<p>No files have been uploaded!</p>");
    // out.write("<p>You must enter a key for the file.</p>");
    // out.write("<p>Click ");
    // out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here",
    // false));
    // out.write(" to return try again.</p>");
    // out.write(HtmlConsts.BODY_CLOSE);
    // out.write(HtmlConsts.HTML_CLOSE);
    // } else if (conflictingKeys.size() == 0 && conflictingFiles.size() == 0) {
    // // persist all the entities
    // for (Entity entity : newEntities)
    // entity.put(cc);
    // PrintWriter out = resp.getWriter();
    // out.write(HtmlConsts.HTML_OPEN);
    // out.write(HtmlConsts.BODY_OPEN);
    // out.write("<p>Successful file upload.</p>");
    // out.write("<p>Click ");
    // out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here",
    // false));
    // out.write(" to return to add another file.</p>");
    // out.write(HtmlConsts.BODY_CLOSE);
    // out.write(HtmlConsts.HTML_CLOSE);
    // } else {
    // // we have to notify the client of conflicts
    // PrintWriter out = resp.getWriter();
    // out.write(HtmlConsts.HTML_OPEN);
    // out.write(HtmlConsts.BODY_OPEN);
    // out.write("<p>No files have been uploaded!</p>");
    // out.write("<p>Doing so would result in conflicts and the loss" +
    // " of information.</p>");
    // if (conflictingKeys.size() != 0) {
    // out.write("<p>The following keys already exist:</p>");
    // for (String key : conflictingKeys) {
    // out.write("<p>&nbsp;&nbsp;&nbsp;" + key + "</p>");
    // }
    // }
    // if (conflictingFiles.size() != 0) {
    // out.write("<p>The following files already exist:</p>");
    // for (String file : conflictingFiles) {
    // out.write("<p>&nbsp;&nbsp;&nbsp;" + file + "</p>");
    // }
    // }
    // out.write("<p>You must resolve these conflicts.</p>");
    // out.write("<p>Click ");
    // out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here",
    // false));
    // out.write(" to return try again.</p>");
    // out.write(HtmlConsts.BODY_CLOSE);
    // out.write(HtmlConsts.HTML_CLOSE);
    // }
    //
    //
    // } catch (ODKDatastoreException e) {
    // logger.error("File upload persistence error: " + e.getMessage());
    // e.printStackTrace();
    // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    // } catch (FileUploadException e) {
    // logger.error("File upload persistence error: " + e.getMessage());
    // e.printStackTrace(resp.getWriter());
    // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // ErrorConsts.UPLOAD_PROBLEM);
    // } catch (DatastoreFailureException e) {
    // logger.error("File upload persistence error: " + e.getMessage());
    // e.printStackTrace(resp.getWriter());
    // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    // } catch (RequestFailureException e) {
    // logger.error("Error uploading files due to error checking current" +
    // " contents of the datastores: " + e.getMessage());
    // e.printStackTrace(resp.getWriter());
    // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    // } catch (AccessDeniedException e) {
    // // I don't think this should happen, as by the time you've gotten
    // // to this servlet, you should have already had appropriate
    // // permissions to see this table and access things.
    // logger.error("Error uploading files, access denied when accessing" +
    // " the datastore: " + e.getMessage());
    // e.printStackTrace(resp.getWriter());
    // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    // }

  }

  // /**
  // * Get the rows in DbTableFileInfo for the corresponding table key.
  // */
  // private List<Row> getFileRows(HttpServletRequest req, String tableId)
  // throws AccessDeniedException, RequestFailureException,
  // DatastoreFailureException {
  // CallingContext cc = ContextFactory.getCallingContext(this, req);
  // try {
  // List<Row> rows = EntityConverter.toRowsFromFileInfo(
  // DbTableFileInfo.query(tableId, cc));
  // return rows;
  // } catch (ODKDatastoreException e) {
  // e.printStackTrace();
  // throw new DatastoreFailureException(e);
  // }
  // }
  //
  // /**
  // * This method returns the duplicate keys that are preventing the current
  // * upload.
  // * @param newRows the new rows you are adding
  // * @param oldRows the rows already in the database
  // * @return the conflicting keys
  // */
  // private List<String> getDuplicateKeys(List<Row> newRows, List<Row> oldRows)
  // {
  // List<String> duplicates = new ArrayList<String>();
  // // First we're going to convert the current rows to a set based on their
  // // keys.
  // Set<String> currentKeys = new HashSet<String>();
  // for (Row oldRow : oldRows) {
  // Map<String, String> values = oldRow.getValues();
  // String oldKey = values.get(DbTableFileInfo.KEY);
  // currentKeys.add(oldKey);
  // }
  // // Now check that the list of new keys doesn't have any collisions.
  // for (Row newRow : newRows) {
  // Map<String, String> values = newRow.getValues();
  // String newKey = values.get(DbTableFileInfo.KEY);
  // if (currentKeys.contains(newKey))
  // duplicates.add(newKey);
  // }
  // return duplicates;
  // }
  //
  // /**
  // * This method returns the duplicate filenames that would occur on the
  // * current upload.
  // * @param newRows the new rows you are adding
  // * @param oldRows the rows already in the database
  // * @return the conflicting keys
  // */
  // private List<String> getDuplicateFileNames(CallingContext cc,
  // List<Row> newRows, List<Row> oldRows)
  // throws ODKDatastoreException {
  // List<String> duplicates = new ArrayList<String>();
  // // First we're going to convert the current rows to a set based on their
  // // keys.
  // DbTableFiles blobSetRelation = new DbTableFiles(cc);
  // Set<String> currentFiles = new HashSet<String>();
  // for (Row oldRow : oldRows) {
  // Map<String, String> values = oldRow.getValues();
  // // we only want to look at the rows that represent files.
  // if (values.get(DbTableFileInfo.VALUE_TYPE)
  // .equals(DbTableFileInfo.Type.FILE.name)) {
  // String oldFile = blobSetRelation.getBlobEntitySet(
  // oldRow.getValues().get(DbTableFileInfo.VALUE), cc)
  // .getUnrootedFilename(1, cc);
  // currentFiles.add(oldFile);
  // }
  // }
  // // Now check that the list of new keys doesn't have any collisions.
  // for (Row newRow : newRows) {
  // Map<String, String> values = newRow.getValues();
  // // we only want to look at the rows that represent files.
  // if (values.get(DbTableFileInfo.VALUE_TYPE)
  // .equals(DbTableFileInfo.Type.FILE.name)) {
  // String newFile = blobSetRelation.getBlobEntitySet(
  // newRow.getValues().get(DbTableFileInfo.VALUE), cc)
  // .getUnrootedFilename(1, cc);
  // if (currentFiles.contains(newFile))
  // duplicates.add(newFile);
  // }
  // }
  // return duplicates;
  // }
  //
  // /**
  // * This generates the HTML for a selector box to allow the selection of
  // * a table name from the list of available tables.
  // * @param cc
  // * @return
  // */
  // private String getSelectTableHtml(CurrentUserPermissions userPermissions,
  // CallingContext cc)
  // throws ODKDatastoreException {
  // TableManager tm = new TableManager(userPermissions, cc);
  // List<TableEntry> entries = tm.getTables();
  // String html = "<select name=\"table_name\" id=\"table_name\">";
  // for (TableEntry table : entries) {
  // html = html + "<option value=\"" + table.getTableKey() + "\">"
  // + table.getTableKey() + "</option>";
  // }
  // html = html + "</select>";
  // return html;
  // }

}
