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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.DynamicAssociationBase;
import org.opendatakit.aggregate.datamodel.DynamicBase;
import org.opendatakit.aggregate.datamodel.DynamicDocumentBase;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryResultsServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "view/queryResults";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "Query Results";

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
				BeanDefs.USER_BEAN);
		User user = userService.getCurrentUser();

		// get parameter
		String formId = getParameter(req, ServletConsts.FORM_ID);
		if (formId == null) {
			errorMissingKeyParam(resp);
			return;
		}

		String field = getParameter(req, ServletConsts.QUERY_FIELD_PARAM);
		String op = getParameter(req, ServletConsts.QUERY_OP_PARAM);
		String value = getParameter(req, ServletConsts.QUERY_VALUE_PARAM);

		if (field == null || op == null || value == null) {
			sendErrorNotEnoughParams(resp);
			return;
		}

		// get form
		Datastore ds = (Datastore) ContextFactory.get().getBean(
				BeanDefs.DATASTORE_BEAN);
		Form form = null;
		try {
			form = Form.retrieveForm(formId, ds, user);
		} catch ( ODKFormNotFoundException e) {
			odkIdNotFoundError(resp);
			return;
		}
		
		FormElementModel element = form.findElementByName(field);
		CommonFieldsBase tbl = element.getParent().getFormDataModel().getBackingObjectPrototype();

		if (element == null) {
			errorRetreivingData(resp);
			return;
		}

		Object compareValue = null;
		switch (element.getElementType()) {
		case BOOLEAN:
			compareValue = Boolean.parseBoolean(value);
			break;
		case INTEGER:
			compareValue = Long.valueOf(value);
			break;
		case DECIMAL:
			compareValue = new BigDecimal(value);
			break;
		case STRING:
			compareValue = value;
			break;
		default:
			throw new IllegalStateException("datatype not supported");			
		}
		Query query = ds.createQuery(tbl, user);
		query.addFilter(element.getFormDataModel().getBackingKey(), Query.FilterOperation
				.valueOf(op), compareValue);
		try {
			List<Submission> submissions = new ArrayList<Submission>();
			DataField foreignKey = null;
			if ( tbl instanceof TopLevelDynamicBase ) {
				foreignKey = ((TopLevelDynamicBase) tbl).primaryKey;
			} else if ( tbl instanceof DynamicAssociationBase ) {
				foreignKey = ((DynamicAssociationBase) tbl).topLevelAuri;
			} else if ( tbl instanceof DynamicDocumentBase ) {
				foreignKey = ((DynamicDocumentBase) tbl).topLevelAuri;
			} else if ( tbl instanceof DynamicBase ) {
				foreignKey = ((DynamicBase) tbl).topLevelAuri;
			} else {
				throw new IllegalStateException("unexpected persistence backing object type");
			}
			Set<EntityKey> keys = query
					.executeForeignKeyQuery(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(),
												foreignKey);
			for ( EntityKey k : keys ) {
				submissions.add( new Submission(k.getKey(), form, ds, user));
			}

			SubmissionFormatter formatter = new HtmlFormatter(form,
					getServerURL(req), resp.getWriter(), null, true);

			beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
			formatter.processSubmissions(submissions);
			finishBasicHtmlResponse(resp);
		} catch (ODKDatastoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorRetreivingData(resp);
		}
	}
}