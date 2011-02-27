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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Servlet to list all external services actions for a particular form
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ExternalServicesListServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = -4405695604321586000L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "extern/externalServices";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "External Services Status";

	/**
	 * Handler for HTTP Get request that shows the list of forms
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// generate html
		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		try {
			QueryFormList formsList = new QueryFormList(false, cc);

			List<Form> forms = formsList.getForms();

			for (Form form : forms) {

				List<ExternalService> esList = FormServiceCursor
						.getExternalServicesForForm(form, cc);
				if ( esList.size() == 0 ) continue;
				
				resp.getWriter().print(
						HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, form
								.getFormId()));

				List<Row> formattedFormValues = new ArrayList<Row>();
				List<String> headers = new ArrayList<String>();
				headers.add("CreatedBy User");
				headers.add("Operational Status");
				headers.add("Establishment Datetime");
				headers.add("External Services Action");
				headers.add("External Service Type");
				headers.add("Name");

				for (ExternalService es : esList) {
					FormServiceCursor fsc = es.getFormServiceCursor();
					Row row = new Row(new SubmissionKey(fsc.getTableName()
							+ "[@key=" + fsc.getUri() + "]"));
					row.addFormattedValue(fsc.getCreatorUriUser());
					row.addFormattedValue(fsc.getOperationalStatus()
									.toString());
					row.addFormattedValue(fsc.getEstablishmentDateTime()
							.toString());
					row.addFormattedValue(fsc.getExternalServiceOption()
							.getDescriptionOfOption());
					row.addFormattedValue(fsc.getExternalServiceType().getServiceName());
					row.addFormattedValue(es.getDescriptiveTargetString());
					formattedFormValues.add(row);
				}

				resp.getWriter().print(
						HtmlUtil.wrapResultTableWithHtmlTags(false, null, headers,
								formattedFormValues));
			}
			finishBasicHtmlResponse(resp);

		} catch (ODKDatastoreException e) {
			errorRetreivingData(resp);
		} catch (ODKIncompleteSubmissionData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorRetreivingData(resp);
		}
	}
}
