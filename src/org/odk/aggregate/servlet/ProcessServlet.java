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
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.process.DeleteSubmissions;
import org.odk.aggregate.process.ProcessParams;
import org.odk.aggregate.process.ProcessType;

import com.google.appengine.api.datastore.Key;

/**
 * Processes request from web based interface based on users button
 * press specifying the type of processing they want
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class ProcessServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7328196170394698478L;

  /**
   * URI from base
   */
  public static final String ADDR = "process";

  /**
   * Handler for HTTP Post request
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {


    EntityManager em = EMFactory.get().createEntityManager();
    try {
      ProcessParams params = new ProcessParams(new MultiPartFormData(req));
      List<Key> keys = params.getKeys();
    
      if (params.getOdkId() == null || keys == null || params.getButtonText() == null) {
        sendErrorNotEnoughParams(resp);
        return;
      }
      
      if(params.getButtonText().equals(ProcessType.DELETE.getButtonText())) {
        DeleteSubmissions delete = new DeleteSubmissions(params.getOdkId(), keys, em);
        delete.deleteSubmissions();
        resp.sendRedirect(ServletConsts.WEB_ROOT); 
      } else {
        resp.getWriter().print("UNRECOGNIZED PROCESS TYPE!");
      }
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (FileUploadException e) {
      e.printStackTrace();
    }
    em.close();
  }

}
