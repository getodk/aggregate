/*
 * Copyright (C) 2009 University of Washington
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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.process.DeleteSubmissions;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;

public class FormDeleteTaskServlet extends ServletUtilBase {
 
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8219849865201422548L;

  /**
   * URI from base
   */
  public static final String ADDR = "formDeleteTask";
  
  /**
   * Handler for HTTP Get request that shows the list of forms
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    
    EntityManager em = EMFactory.get().createEntityManager();
    // retrieve submissions
    Query surveyQuery = new Query(odkId);
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.DESCENDING);
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(20));
    
    if(submissionEntities.size() > 0) {
      List<Key> keys = new ArrayList<Key>();
      for(Entity en: submissionEntities) {
        keys.add(en.getKey());
      }
      DeleteSubmissions delete;
      try {
        delete = new DeleteSubmissions(odkId, keys, em);
      } catch (ODKFormNotFoundException e1) {
        e1.printStackTrace();
        return;
      }
      delete.deleteSubmissions();
      TaskOptions task = TaskOptions.Builder.url("/" + FormDeleteTaskServlet.ADDR);
      task.method(TaskOptions.Method.GET);
      task.countdownMillis(1);
      task.param(ServletConsts.ODK_FORM_KEY, odkId);
      Queue queue = QueueFactory.getDefaultQueue();
      try {
        queue.add(task);
      } catch (Exception e) {
        resp.getWriter().print(ErrorConsts.TASK_PROBLEM);
        e.printStackTrace();
      }

    } else {
      Form form;
      try {
        form = Form.retrieveForm(em, odkId);
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
        return;
      }
      em.remove(form);
    }
  }
}
