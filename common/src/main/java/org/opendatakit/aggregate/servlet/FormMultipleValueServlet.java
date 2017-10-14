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
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

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
	public static final String ADDR = "view/formMultipleValue";

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
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// verify parameters are present
		String keyString = getParameter(req, ServletConsts.FORM_ID);
		if (keyString == null) {
			sendErrorNotEnoughParams(resp);
			return;
		}
		SubmissionKey key = new SubmissionKey(keyString);

		List<SubmissionKeyPart> parts = key.splitSubmissionKey();
		Submission sub = null;
		try {
			IForm form = FormFactory.retrieveFormByFormId(parts.get(0).getElementName(), cc);
		    if ( !form.hasValidFormDefinition() ) {
				errorRetreivingData(resp);
				return; // ill-formed definition
		    }
			sub = Submission.fetchSubmission(parts, cc);

			if (sub != null) {
				SubmissionElement v = sub.resolveSubmissionKey(parts);
				RepeatSubmissionType b = (RepeatSubmissionType) v;

				// header info
				beginBasicHtmlResponse(TITLE_INFO + b.getPropertyName(), resp, cc);

				HtmlFormatter formatter = new HtmlFormatter(form, cc.getServerURL(), resp
						.getWriter(), null, false);
				formatter.processSubmissionSetPublic(b.getSubmissionSets(), b
						.getElement(), cc);

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
