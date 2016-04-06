/*
 * Copyright (C) 2013-2014 University of Washington
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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.web.CallingContext;

public class EnketoApiHandlerServlet extends ServletUtilBase {

  private static final long serialVersionUID = 5811797423869654357L;
  private static final Log logger = LogFactory.getLog(AggregateHtmlServlet.class);

  public static final String ADDR = UIConsts.ENKETO_API_HANDLER_ADDR;
  private final String USER_AGENT = "Mozilla/5.0";
  private static final String RESPONSE_ERROR = "Please verify the Enketo Webform Integration settings on the Preferences tab and try again.";

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    String enketo_api_url = null;
    HttpURLConnection con = null;
    BufferedReader in = null;
    String message = null;
    String enketoURL = null;
    try {
      enketo_api_url = req.getParameter("enketo_api_url");
      String enketo_api_token = req.getParameter("enketo_api_token");
      String form_id = req.getParameter("form_id");

      CallingContext cc = ContextFactory.getCallingContext(this, req);
      String aggregate_server_url = cc.getSecureServerURL();

      enketo_api_token += ":";
      byte[] encoded = Base64.encodeBase64(enketo_api_token.getBytes());

      String urlParameters = "server_url=" + aggregate_server_url + "&form_id=" + form_id;

      URL obj = new URL(enketo_api_url);
      con = (HttpURLConnection) obj.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Authorization", "Basic " + new String(encoded));
      con.setRequestProperty("User-Agent", USER_AGENT);
      con.setRequestProperty("Accept", "*/*");
      con.setRequestProperty("Accept-Charset", "UTF-8");
      con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

      // Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      logger.info("Enketo API response code : " + responseCode);

      try {
        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        logger.info("Getting the Enketo URL from InputStream");
      } catch (Exception io) {
        in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        logger.info("Getting the Error Message from ErrorStream");
      }

      String inputLine = null;
      StringBuffer response = new StringBuffer();

      String responseURL = null;
      logger.info("BufferReader Object : " + in);
      if (in != null) {
        logger.info("BufferReader Object : Not Null ");
        while ((inputLine = in.readLine()) != null) {
          if (inputLine.contains("message")) {
            message = inputLine;
            System.out.println(message);
          }
          if (inputLine.contains("https")) {
            responseURL = inputLine;
          } else if (inputLine.contains("http")) {
            logger.error("Enketo api token is compromised! Enketo URL should specify https");
            responseURL = inputLine;
          }
          
          response.append(inputLine);
        }
        in.close();
      }
      if (message != null) {
        String messageArry[] = message.split("\"");
        if (messageArry.length >= 3)
          message = messageArry[3];
        resp.setStatus(412);
        resp.setHeader("error", "There was an error obtaining the webform. (message:" + message
            + ")");
      } else if (responseURL != null) {
        String arryResponseURL[] = responseURL.toString().split(" ");
        for (String token : arryResponseURL) {
          if (!token.contains("http")) {
            continue;
          }
          token = token.replace("\\", "");
          token = token.replace("\"", "");
          token = token.replace(",", "");
          enketoURL = token;
        }
        resp.setStatus(responseCode);
        resp.setHeader("enketo_url", enketoURL);
        logger.info("Enketo API response URL :" + enketoURL);
      } else {
        resp.setStatus(responseCode);
      }

    } catch (java.net.SocketTimeoutException socketTimeoutException) {
      logger
          .info("Exception caught while calling enketo api :" + socketTimeoutException.toString());
      resp.setStatus(412);
      resp.setHeader("error", "API at " + enketo_api_url + " is not available");
    } catch (Exception exception) {
      logger.info("Exception caught while calling enketo api :" + exception.toString());
      resp.setStatus(412);
      if (message == null || message.equals("")) {
        message = RESPONSE_ERROR;
      }
      resp.setHeader("error", "There was an error obtaining the webform. (message:" + message + ")");
    } finally {
      if (con != null)
        con.disconnect();
    }

  }

}
