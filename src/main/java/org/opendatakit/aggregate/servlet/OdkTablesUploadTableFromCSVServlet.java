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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.ImportFromCSVExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.impl.api.ServiceUtils;
import org.opendatakit.aggregate.odktables.importexport.CsvUtil;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet that handles the generation of a table from an uploaded CSV file.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesUploadTableFromCSVServlet extends ServletUtilBase {

  /**
   *
   */
  private static final long serialVersionUID = -890634967842304210L;

  private static final Log logger = LogFactory.getLog(OdkTablesUploadTableFromCSVServlet.class);

  private static final String ADDR = UIConsts.UPLOAD_TABLE_FROM_CSV_SERVLET_ADDR;

  /**
   * title for generated webpage
   */
  public static final String TITLE_INFO = "OdkTables Upload Table From CSV";

  private static final String UPLOAD_PAGE_BODY_START =

  "<div style=\"overflow: auto;\"><p id=\"subHeading\"><h2>Import a table from a CSV</h2></p>"
      + "<!--[if true]><p style=\"color: red;\">For a better user experience, use Chrome, Firefox or Safari</p>"
      + "<![endif] -->"
      + "<form id=\"ie_backward_compatible_form\""
      + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
      + " action=\""; // emit the ADDR

  private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">"
      + "     <table id=\"uploadTable\">"
      + "    <tr>"
      + "         <td><label for=\"table_name\">Name of New Table:</label></td>"
      + "       <td><input id=\"table_name\" type=\"text\" name=\"table_name\" /></td>"
      + "      </tr>\n"
      + "</td>"
      + "      <tr>"
      + "         <td><label for=\"table_file\">File:</label></td>"
      + "         <td><input id=\"table_file\" type=\"file\" size=\"80\" class=\"gwt-Button\""
      + "            name=\"table_file\" /></td>"
      + "      </tr>"
      + "<tr>"
      + "<td><input type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Create Table From CSv\" /></td>"
      + "<td />" + "</tr>" + " </table>\n" + "</form>";

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
    out.write(UPLOAD_PAGE_BODY_MIDDLE);
    finishBasicHtmlResponse(resp);
  }

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
   * Handler for HTTP Post request that takes a CSV file, uses that file to add
   * a new OdkTables table to the datastore.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServiceUtils.examineRequest(getServletContext(), req);
    // TODO here do I need to handle the log stuff?
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }

    try {
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      MultiPartFormItem csvFile = uploadedFormItems.getFormDataByFieldName("table_file");

      String tableName = uploadedFormItems.getSimpleFormField("table_name");

      addOpenDataKitHeaders(resp);
      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
      resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);

      CsvUtil csvUtil = new CsvUtil();
      byte[] bytes = csvFile.getStream().toByteArray();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      boolean success = csvUtil.importNewTable(br, tableName, cc);
      PrintWriter out = resp.getWriter();
      if (success) {
        out.write("The table was created successfully.");
      } else {
        // something went wrong while uploading.
        out.write("There was a problem uploading the table.");
      }
    } catch (FileUploadException e) {
      logger.error("error uploading csv: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    } catch (BadColumnNameExceptionClient e) {
      logger.error("bad column name: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
    } catch (ImportFromCSVExceptionClient e) {
      logger.error("problem importing from CSV: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM);
    } catch (ETagMismatchExceptionClient e) {
      logger.error("etag mismatch while importing: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
    } catch (PermissionDeniedExceptionClient e) {
      logger.error("permission denied while uploading: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
    } catch (EntityNotFoundExceptionClient e) {
      logger.error("entity not found while uploading: " + e.getMessage());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
    }
  }

}
