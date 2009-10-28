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

import java.io.IOException;
import java.util.List;

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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class InsightDataServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -4349962411968747884L;
  /**
   * URI from base
   */
  public static final String ADDR = "insightData";

  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // TODO: rename params so not spreadsheet

    // get parameter
    String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    String alreadyExists = getParameter(req, ServletConsts.ALREADY_EXISTS_PARAM);
    
    boolean insightRepoAlreadyExists = false;
    if(alreadyExists != null) {
      insightRepoAlreadyExists = Boolean.parseBoolean(alreadyExists);
    }
    
    if (spreadsheetName == null || odkFormKey == null || esTypeString == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    // get form
    EntityManager em = EMFactory.get().createEntityManager();
    Key formKey = KeyFactory.stringToKey(odkFormKey);
    Form form = em.getReference(Form.class, formKey);

    List<RhizaInsight> insightServers = form.getInsightExternalRepos();
    
    if(insightServers.isEmpty()) {
      return;
    }
    
    RhizaInsight insightServer = insightServers.get(0);
    
    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);
    
    FormProperties formProp = new FormProperties(form, em);

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
    
    if (!insightRepoAlreadyExists && esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
      form.removeInsightExternalRepos();
    }
    em.close();
  }
  
}
