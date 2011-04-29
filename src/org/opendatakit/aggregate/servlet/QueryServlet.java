/*
 * Copyright (C) 2010 University of Washington
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryServlet extends ServletUtilBase {

    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = 3078038743780061473L;

    /**
     * URI from base
     */
    public static final String ADDR = "view/query";

    /**
     * Title for generated webpage
     */
    private static final String TITLE_INFO = "Query from: ";

    private List<FormElementModel> queryElement;

    /**
     * Handler for HTTP Get request that responds with an XML list of forms to
     * download
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    // get parameter
	String formId = getParameter(req, ServletConsts.FORM_ID);
	if (formId == null) {
	    errorMissingKeyParam(resp);
	    return;
	}

	// get form
	Form form = null;
	try {
		form = Form.retrieveForm(formId, cc);
	} catch ( ODKFormNotFoundException e ) {
	    odkIdNotFoundError(resp);
	    return;
	}

	queryElement = new ArrayList<FormElementModel>();

	processElementForColumnHead(form.getTopLevelGroupElement(), form.getTopLevelGroupElement(), "");

	beginBasicHtmlResponse(TITLE_INFO + form.getViewableName(), resp, true, cc); // header info

	PrintWriter out = resp.getWriter();
	out.write(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(QueryResultsServlet.ADDR), null,
		HtmlConsts.GET));

	out.write("Field to Filter:" + HtmlConsts.LINE_BREAK);
	List<String> queryField = createSelectOptionsFromFormElements(queryElement);
	out.write(HtmlUtil.createSelect(ServletConsts.QUERY_FIELD_PARAM,
		queryField));
	out.write("<p/>");
	out.write("Operation:" + HtmlConsts.LINE_BREAK);
	List<String> operationField = new ArrayList<String>();
	for (Query.FilterOperation op : Query.FilterOperation.values()) {
	    operationField.add(op.toString());
	}
	out.write(HtmlUtil.createSelect(ServletConsts.QUERY_OP_PARAM,
		operationField));
	out.write("<p/>");
	out.write("Value to Filter With:" + HtmlConsts.LINE_BREAK);
	out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT,
		ServletConsts.QUERY_VALUE_PARAM, null));
	out.write("<p/>");
	out.write(HtmlUtil.createInput("hidden", ServletConsts.FORM_ID, form.getFormId()));
	out.write(HtmlUtil.createInput("submit", null, null));
	out.write("</form>");

	finishBasicHtmlResponse(resp);
    }

    /**
     * Helper function to recursively go through the elements and create a
     * sortable list
     * 
     */
    private void processElementForColumnHead(FormElementModel node,
    		FormElementModel root, String parentName) {
	if (node == null)
	    return;

	// TODO: restructure
	switch ( node.getElementType() ) {
	case BOOLEAN:
	case INTEGER:
	case DECIMAL:
	case STRING:
	case SELECT1:
	case SELECTN:
		queryElement.add(node);
		break;
	}

	List<FormElementModel> childDataElements = node.getChildren();
	if (childDataElements == null) {
	    return;
	}
	for (FormElementModel child : childDataElements) {
	    processElementForColumnHead(child, root, parentName);
	}
    }

    private List<String> createSelectOptionsFromFormElements(List<FormElementModel> l) {
	List<String> options = new ArrayList<String>();
	for (FormElementModel fe : l) {
	    options.add(fe.getElementName());
	}
	return options;
    }

}
