/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.submission.UIGeoPoint;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.UiElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmissionServiceImpl extends RemoteServiceServlet implements
org.opendatakit.aggregate.client.submission.SubmissionService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -7997978505247614945L;

  @Override
  public SubmissionUISummary getSubmissions(FilterGroup filterGroup) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    SubmissionUISummary summary = new SubmissionUISummary();
    try {
      String formId = filterGroup.getFormId();
      Form form = Form.retrieveForm(formId, cc);
      QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup, 1000, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);

      getSubmissions(filterGroup, cc, summary, form, submissions);
      
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    
    
    return summary;
  }

  private void getSubmissions(FilterGroup filterGroup, CallingContext cc,
      SubmissionUISummary summary, Form form, List<Submission> submissions)
      throws ODKDatastoreException {
    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
    ElementFormatter elemFormatter = new UiElementFormatter(cc.getServerURL(), headerGenerator.getGeopointIncludes());

    
    // format row elements
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(filteredElements, elemFormatter, false, cc);
      try {
        summary.addSubmission(new SubmissionUI(row.getFormattedValues()));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  public SubmissionUISummary getSubmissions(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    SubmissionUISummary summary = new SubmissionUISummary();
    try {
      Form form = Form.retrieveForm(formId, cc);
      QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
          ServletConsts.FETCH_LIMIT, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);

      getSubmissions(null, cc, summary, form, submissions);
      
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    
    
    return summary;
  }

  @Override
  public UIGeoPoint[] getGeoPoints(String formId, String geopointKey) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    try {
      Form form = Form.retrieveForm(formId, cc);
      QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
          ServletConsts.FETCH_LIMIT, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);
      
      UIGeoPoint[] points = new UIGeoPoint[submissions.size()];
      FormElementModel geopointField = null;
      if (geopointKey != null) {
        FormElementKey geopointFEMKey = new FormElementKey(geopointKey);
        geopointField = FormElementModel.retrieveFormElementModel(form, geopointFEMKey);
      }
      List<FormElementModel> filteredElements = new ArrayList<FormElementModel>();
      filteredElements.add(geopointField);
      ElementFormatter elemFormatter = new BasicElementFormatter(true, false, false);
      
      // format row elements
      int i = 0;
      for (SubmissionSet sub : submissions) {
        Row row = sub.getFormattedValuesAsRow(filteredElements, elemFormatter, false, cc);
      
        try {
          List<String> formatted = row.getFormattedValues();
          if(formatted.size() == 2) {
            UIGeoPoint gpsPoint = new UIGeoPoint(formatted.get(0), formatted.get(1));
            points[i] = gpsPoint;
          } else {
            System.out.println("TOO MANY VALUES TO GENERATE A GEOPOINT");
          }
          
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        i++;
      }
      return points;
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();     
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return null;
  }

  
  
}
