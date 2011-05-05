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
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.task.PurgeOlderSubmissions;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.web.CallingContext;

/**
 * Servlet handling the 'purge sent records' confirmation and task 
 * initiation for Aggregate acting as a reliable message queue.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class ExternalServicesPurgeServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1759717296626410301L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "admin/purgePublishedSubmissions";

	public static final String CONFIRM = "yes";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "Confirm Purge of Published Records";

	/**
	 * Handler for HTTP Get request that asks to confirm purge of published records.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		String uriFsc = req.getParameter(ExternalServiceConsts.FSC_URI_PARAM);
		String formId = req.getParameter(ServletConsts.FORM_ID);
		String confirm = req.getParameter(CONFIRM);
		String dateString = req.getParameter(PurgeOlderSubmissions.PURGE_DATE);
		
		if ( uriFsc == null || uriFsc.length() == 0 ) {
			errorMissingParam(resp);
			return;
		} else {
			// decode the date string if present...
			Date earliest = null;
			if ( dateString != null && dateString.length() != 0 ) {
				try {
					dateString = URLDecoder.decode(dateString, HtmlConsts.UTF8_ENCODE);
					earliest = PurgeOlderSubmissions.PURGE_DATE_FORMAT.parse(dateString);
				} catch ( Exception e ) {
					errorBadParam(resp);
					return;
				}
			}
			
			try {
				// need to decode the parameter...
				uriFsc = URLDecoder.decode(uriFsc, HtmlConsts.UTF8_ENCODE);
				formId = URLDecoder.decode(formId, HtmlConsts.UTF8_ENCODE);
				FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uriFsc, cc);
				if ( fsc != null ) {
					ExternalService es = fsc.getExternalService(cc);
					if ( es != null ) {
						if ( earliest == null || confirm == null || confirm.length() == 0 ) {
							// generate 'confirm?' html
							beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
							PrintWriter out = resp.getWriter();
							if ( earliest == null ) {
								switch ( fsc.getExternalServiceOption() ) {
								case UPLOAD_ONLY:
									if ( fsc.getUploadCompleted() ) {
										earliest = fsc.getEstablishmentDateTime();
									} else {
										earliest = fsc.getLastUploadCursorDate();
									}
									break;
								case UPLOAD_N_STREAM:
									if ( fsc.getUploadCompleted() ) {
										earliest = fsc.getLastStreamingCursorDate();
										if ( earliest == null ) {
											earliest = fsc.getEstablishmentDateTime();
										}
									} else {
										earliest = fsc.getLastUploadCursorDate();
									}
									break;
								case STREAM_ONLY:
									earliest = fsc.getLastStreamingCursorDate();
									if ( earliest == null ) {
										earliest = fsc.getEstablishmentDateTime();
									}
									break;
								}
							}
							if ( fsc.getExternalServiceOption() != ExternalServiceOption.UPLOAD_ONLY) {
								out.write("<p><b>Note:</b> Even though the chosen publishing action involves an ongoing streaming" +
										" of data to the external service, this purge action is a one-time event and is " +
										"not automatically ongoing.  You will need to periodically repeat this process.</p>");
							}
							out.write("Click to confirm purge of <b>" + fsc.getFormId() + "</b> submissions older than " + 
									earliest.toString());
							Map<String,String> properties = new HashMap<String,String>();
							properties.put(ExternalServiceConsts.FSC_URI_PARAM, fsc.getUri());
							properties.put(ServletConsts.FORM_ID, formId);
							properties.put(CONFIRM, "yes");
							properties.put(	PurgeOlderSubmissions.PURGE_DATE, 
											PurgeOlderSubmissions.PURGE_DATE_FORMAT.format(earliest));
							out.write(HtmlUtil.createHtmlButtonToGetServlet(
									cc.getWebApplicationURL(ADDR), "Purge submissions", properties));
							finishBasicHtmlResponse(resp);
							return;
						} else {
							SubmissionKey formKey = new SubmissionKey(formId);
				            List<SubmissionKeyPart> parts = formKey.splitSubmissionKey();
				            if (parts.size() != 2) {
				            	errorBadParam(resp);
				            	return;
				            }

				            Form form = Form.retrieveForm(parts.get(0).getElementName(), cc);

				            if (form == null) {
				            	errorRetreivingData(resp);
				            	return;
				            }

				            CommonFieldsBase rel = cc.getDatastore().getEntity(form.getTopLevelGroupElement().getFormDataModel()
				                .getBackingObjectPrototype(), parts.get(1).getAuri(), cc.getCurrentUser());
				            // If the FormInfo table is the target, log an error!
				            if (rel != null) {
								form = new Form((TopLevelDynamicBase) rel, cc);
								if (form.getFormId().equals(Form.URI_FORM_ID_VALUE_FORM_INFO)) {
									errorBadParam(resp);
									return;
								}

								// any confirm parameter value means OK -- purge it!
								PurgeOlderSubmissions pos = 
									(PurgeOlderSubmissions) cc.getBean(BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN);
								
								if ( pos == null ) {
									resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, 
											BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN + " bean is not defined.");
									return;
								}
								// set up the purge request here...
								Map<String,String> parameters = new HashMap<String,String>();
							  
								parameters.put( PurgeOlderSubmissions.PURGE_DATE, 
												PurgeOlderSubmissions.PURGE_DATE_FORMAT.format(earliest));
							
								MiscTasks m = new MiscTasks(TaskType.PURGE_OLDER_SUBMISSIONS, form, parameters, cc);
								m.persist(cc);
				            	CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
				            	ccDaemon.setAsDaemon(true);
				            	pos.createPurgeOlderSubmissionsTask(form, m.getSubmissionKey(), 1L, ccDaemon);
								resp.sendRedirect(cc.getWebApplicationURL(ExternalServicesListServlet.ADDR));
				            } else {
				            	errorRetreivingData(resp);
				            	return;
				            }
						}
					} else {
						errorRetreivingData(resp);
						return;
					}
				} else {
					errorRetreivingData(resp);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorRetreivingData(resp);
			}
		}
	}
}
