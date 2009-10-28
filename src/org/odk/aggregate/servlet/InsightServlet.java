/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ExternalServiceOption;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.RhizaInsight;
import org.odk.aggregate.report.FormProperties;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionFieldType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Query;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InsightServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 456146061385437109L;
  /**
   * URI from base
   */
  public static final String ADDR = "insight";
  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // TODO: rename params so not spreadsheet

    // get parameter
    String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

    if (spreadsheetName == null || odkFormKey == null || esTypeString == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    // get form
    EntityManager em = EMFactory.get().createEntityManager();
    Key formKey = KeyFactory.stringToKey(odkFormKey);
    Form form = em.getReference(Form.class, formKey);

    RhizaInsight insightServer = new RhizaInsight(new Link(spreadsheetName));

    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);


    FormProperties formProp = new FormProperties(form, em);
    createForm(formProp);

    
    if (!esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
      form.addInsightExternalRepos(insightServer);
    }

    if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Query surveyQuery = new Query(form.getOdkId());
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.ASCENDING);
      List<Entity> submissionEntities =
          ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(1000));

      for (Entity entity : submissionEntities) {
        try {
          Submission sub = new Submission(entity, form);
          insightServer.sendSubmissionToRemoteServer(formProp, sub);
        } catch (ODKIncompleteSubmissionData e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    em.close();

    resp.sendRedirect(ServletConsts.WEB_ROOT);
  }

  private void createForm(FormProperties formProp) {
    System.out.println("BEGINNING INSERTION");
    
    JsonArray def = new JsonArray(); 
    
    JsonObject form = new JsonObject();
    form.addProperty("ODKID", formProp.getOdkId());
    def.add(form);
    
    Map<String, SubmissionFieldType> types = formProp.getPropertyTypes();
    for(Map.Entry<String, SubmissionFieldType> entry : types.entrySet()) {
      JsonObject element = new JsonObject();
      element.addProperty("TYPE", entry.getValue().getRhizaInsightType().getRhizaInsightValue());
      element.addProperty("LABEL", entry.getKey());
      def.add(element);
    }
   
    System.out.println(def.toString());
    
    try {
      URL url = new URL("http://floresta.rhizalabs.com/cbi/upload/createDataset");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setConnectTimeout(RhizaInsight.CONNECTION_TIMEOUT);
      
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
     writer.write(def.toString());
     
     
     writer.close();       

      System.out.println(connection.getResponseMessage());
      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        InputStreamReader is = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(is);
        
        String responseLine = reader.readLine();
        while(responseLine != null) {
          System.out.print(responseLine);
          responseLine = reader.readLine();
        }
        is.close();
      } else {
        System.out.println("FAILURE OF RHIZA DATA COLLECTION CREATION");
      }
  } catch (MalformedURLException e) {
    e.printStackTrace();
  } catch (IOException e) {
    e.printStackTrace();
  }
    
  }
  
}
