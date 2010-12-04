/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet generates a webpage with a list of submissions from a repeat node of
 * a form
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormMultipleValueServlet extends ServletUtilBase {
	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = -5870882843863177371L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "formMultipleValue";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "Submissions Results: ";

	/**
	 * Handler for HTTP Get request that responds with list of values from a
	 * repeat
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

		// verify parameters are present
		String keyString = getParameter(req, ServletConsts.FORM_ID);
		if (keyString == null) {
			sendErrorNotEnoughParams(resp);
			return;
		}
		SubmissionKey key = new SubmissionKey(keyString);

		Datastore ds = (Datastore) ContextFactory.get().getBean(
				BeanDefs.DATASTORE_BEAN);
		List<SubmissionKeyPart> parts = SubmissionKeyPart
				.splitSubmissionKey(key);
		Submission sub = null;
		try {
			Form form = Form.retrieveForm(parts.get(0).getElementName(), ds, user);
			sub = Submission.fetchSubmission(parts, ds, user);

			if (sub != null) {
				SubmissionElement v = sub.resolveSubmissionKey(parts);
				RepeatSubmissionType b = (RepeatSubmissionType) v;

				HtmlFormatter formatter = new HtmlFormatter(form, getServerURL(req), resp
						.getWriter(), null, false);

				// header info
				beginBasicHtmlResponse(TITLE_INFO + b.getPropertyName(), resp,
						req, true);

				formatter.processSubmissionSetPublic(b.getSubmissionSets(), b
						.getElement());

				// footer info
				finishBasicHtmlResponse(resp);
			}
		} catch (ODKFormNotFoundException e1) {
			odkIdNotFoundError(resp);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			errorRetreivingData(resp);
		}
	}
}
