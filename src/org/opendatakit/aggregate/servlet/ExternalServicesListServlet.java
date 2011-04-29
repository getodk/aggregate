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
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

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

	public static final String CONFIRM = "confirm";
	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "External Services Status";

	private static final String CONFIRM_TITLE_INFO = "Confirm Removal of External Service";

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
				headers.add("");
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
					// set up buttons available for this publishing action...
					Map<String,String> properties = new HashMap<String,String>();
					properties.put(ExternalServiceConsts.FSC_URI_PARAM, fsc.getUri());
					properties.put(ServletConsts.FORM_ID, form.getSubmissionKey().toString());
					// if we are in-progress, we are cancelling the action; 
					// otherwise we're just removing the entry tracking it.
					String action = ((fsc.getOperationalStatus() == OperationalStatus.COMPLETED) ||
									 (fsc.getOperationalStatus() == OperationalStatus.ABANDONED)) ?
											 "Remove" : "Cancel (and remove)";
					// offer to purge all published submissions if the publishing
					// mechanism is upload or upload-and-stream.  Don't offer it 
					// if it is stream-only, as that leaves a gap in the data timespan.
					String purgeButton = "";
					if ( fsc.getExternalServiceOption() != ExternalServiceOption.STREAM_ONLY ) {
						purgeButton = HtmlUtil.createHtmlButtonToGetServlet(
								cc.getWebApplicationURL(ExternalServicesPurgeServlet.ADDR),
								"Purge published submissions", properties);
					}
					row.addFormattedValue(HtmlUtil.createHtmlButtonToPostServlet(
							cc.getWebApplicationURL(ADDR), action, properties)
							+ purgeButton);
					// and the rest of the description of the action...
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		String uriFsc = req.getParameter(ExternalServiceConsts.FSC_URI_PARAM);
		String formId = req.getParameter(ServletConsts.FORM_ID);
		
		if ( uriFsc == null || uriFsc.length() == 0 ) {
			errorMissingParam(resp);
			return;
		} else {
			String confirm = req.getParameter(CONFIRM);
			try {
				// need to decode the parameters...
				uriFsc = URLDecoder.decode(uriFsc, HtmlConsts.UTF8_ENCODE);
				formId = URLDecoder.decode(formId, HtmlConsts.UTF8_ENCODE);
				FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uriFsc, cc);
				if ( fsc != null ) {
					ExternalService es = fsc.getExternalService(cc);
					if ( es != null ) {
						if ( confirm == null || confirm.length() == 0 ) {
							// generate 'confirm?' html
							beginBasicHtmlResponse(CONFIRM_TITLE_INFO, resp, true, cc); // header info
							PrintWriter out = resp.getWriter();
							out.write("<h3>" + fsc.getFormId() + "</h3>");

							List<Row> formattedFormValues = new ArrayList<Row>();
							List<String> headers = new ArrayList<String>();
							headers.add("CreatedBy User");
							headers.add("Operational Status");
							headers.add("Establishment Datetime");
							headers.add("External Services Action");
							headers.add("External Service Type");
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
							formattedFormValues.add(row);
							resp.getWriter().print(
									HtmlUtil.wrapResultTableWithHtmlTags(false, null, headers,
											formattedFormValues));

							out.write("<br><h3>Confirm deletion of this external service configuration?</h3>");
							Map<String,String> properties = new HashMap<String,String>();
							properties.put(ExternalServiceConsts.FSC_URI_PARAM, fsc.getUri());
							properties.put(ServletConsts.FORM_ID, formId);
							properties.put(CONFIRM, "yes");
							out.write(HtmlUtil.createHtmlButtonToPostServlet(
									cc.getWebApplicationURL(ADDR), "Delete", properties));
							finishBasicHtmlResponse(resp);
							return;
						} else {
							// any confirm parameter value means OK -- delete it!
							es.delete(cc);
						}
					} else {
						errorRetreivingData(resp);
						return;
					}
				}
				resp.sendRedirect(cc.getWebApplicationURL(ADDR));
			} catch (ODKEntityNotFoundException e) {
				e.printStackTrace();
				errorRetreivingData(resp);
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Error during deletion");
			}
		}
	}
}
