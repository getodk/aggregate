/*
 * Copyright (C) 2011 University of Washington.
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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Servlet to generate the XML list of submission instanceIDs for a
 * given form. This is a full list of all submissions.  It is 
 * assumed that ODK will have the VM space to be able to emit this
 * list.
 * 
 * 10MB string space / 55 char per uuid =  181,818 records.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionDownloadListServlet extends ServletUtilBase {
  private static final String ID_LIST_TAG = "idList";

private static final String ID_TAG = "id";

/**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 13236849409070038L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/downloadSubmissionList";
  
  private static final String XML_TAG_NAMESPACE = "http://openrosa.org/briefcase/idList";

  /**
   * Handler for HTTP Get request that responds with an XML list of instanceIDs
   * on the system.
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    // get parameters
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    Date startDate = new Date(0);
    String startDateString = getParameter(req, ServletConsts.START_DATE);
    if ( startDateString != null ) {
    	startDate = WebUtils.parseDate(startDateString);
    }

    Date endDate = new Date();
    String endDateString = getParameter(req, ServletConsts.END_DATE);
    if ( endDateString == null ) {
    	errorMissingParam(resp);
    	return;
    } else {
    	endDate = WebUtils.parseDate(endDateString);
    }
    
    Form form;
    try {
      form = Form.retrieveForm(formId, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }
 
	addOpenRosaHeaders(resp);
    try {
        TopLevelDynamicBase tbl = (TopLevelDynamicBase) form.getFormDefinition().getTopLevelGroup().getBackingObjectPrototype();
        
        // Query by lastUpdateDate, filtering by isCompleted.
        // Submissions may be partially uploaded and are marked completed once they 
        // are fully uploaded.  We want the query to be aware of that and to not 
        // report anything that is not yet fully loaded.
        Query query = cc.getDatastore().createQuery(tbl, cc.getCurrentUser());
        query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
        query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.LESS_THAN, endDate);
        query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.GREATER_THAN, startDate);
        query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, Boolean.TRUE);

        List<?> uriList = query.executeDistinctValueForDataField(tbl.primaryKey);

        Document d = new Document();
    	d.setStandalone(true);
    	d.setEncoding(HtmlConsts.UTF8_ENCODE);
    	Element eList = d.createElement(XML_TAG_NAMESPACE, ID_LIST_TAG);
    	eList.setPrefix(null, XML_TAG_NAMESPACE);
    	d.addChild(0, Node.ELEMENT, eList);
    	int idx = 0;
    	for ( Object o : uriList ) {
    		String uri = (String) o;
    		Element e = eList.createElement(XML_TAG_NAMESPACE, ID_TAG);
    		e.setPrefix(null, XML_TAG_NAMESPACE);
    		e.addChild(0, Node.TEXT, uri);
    		eList.addChild(idx++, Node.ELEMENT, e);
    		eList.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
    	}

    	KXmlSerializer serializer = new KXmlSerializer();
		
	    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
	    resp.setContentType(HtmlConsts.RESP_TYPE_XML);
	    addOpenRosaHeaders(resp);
		
	    PrintWriter output = resp.getWriter();
        serializer.setOutput(output);
    	// setting the response content type emits the xml header.
    	// just write the body here...
    	d.writeChildren(serializer);
    	resp.setStatus(HttpServletResponse.SC_OK);
    } catch (ODKDatastoreException e) {
	  e.printStackTrace();
      errorRetreivingData(resp);
	}
  }

}
