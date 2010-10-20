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
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

public class QueryServlet extends ServletUtilBase {

    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = 3078038743780061473L;

    /**
     * URI from base
     */
    public static final String ADDR = "query";

    /**
     * Title for generated webpage
     */
    private static final String TITLE_INFO = "Query from: ";

    private List<FormDataModel> queryElement;

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
	// verify user is logged in
	if (!verifyCredentials(req, resp)) {
	    return;
	}

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();
	
	// get parameter
	String odkId = getParameter(req, ServletConsts.ODK_ID);
	if (odkId == null) {
	    errorMissingKeyParam(resp);
	    return;
	}

	// get form
	Datastore ds = (Datastore) ContextFactory.get().getBean(
		ServletConsts.DATASTORE_BEAN);
	FormDefinition fd = FormDefinition.getFormDefinition(odkId, ds, user);
	if ( fd == null ) {
	    odkIdNotFoundError(resp);
	    return;
	}

	queryElement = new ArrayList<FormDataModel>();

	processElementForColumnHead(fd.getTopLevelGroup(), fd.getTopLevelGroup(), "");

	beginBasicHtmlResponse(TITLE_INFO + fd.getFormName(), resp, req,
		true); // header info

	PrintWriter out = resp.getWriter();
	out.write(HtmlUtil.createFormBeginTag(QueryResultsServlet.ADDR, null,
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
	out.write(HtmlUtil.createInput("hidden", ServletConsts.ODK_ID, fd
		.getFormId()));
	out.write(HtmlUtil.createInput("submit", null, null));
	out.write("</form>");

	finishBasicHtmlResponse(resp);
    }

    /**
     * Helper function to recursively go through the elements and create a
     * sortable list
     * 
     */
    private void processElementForColumnHead(FormDataModel node,
    		FormDataModel root, String parentName) {
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

	List<FormDataModel> childDataElements = node.getChildren();
	if (childDataElements == null) {
	    return;
	}
	for (FormDataModel child : childDataElements) {
	    processElementForColumnHead(child, root, parentName);
	}
    }

    private List<String> createSelectOptionsFromFormElements(List<FormDataModel> l) {
	List<String> options = new ArrayList<String>();
	for (FormDataModel fe : l) {
	    options.add(fe.getElementName());
	}
	return options;
    }

}
