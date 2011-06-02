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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.task.PurgeOlderSubmissions;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormAdminServiceImpl extends RemoteServiceServlet implements
		org.opendatakit.aggregate.client.form.admin.FormAdminService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2513124088714784947L;

	@Override
	public Boolean setFormDownloadable(String formId, Boolean downloadable) {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		try {
			Form form = Form.retrieveForm(formId, cc);
			form.setDownloadEnabled(downloadable);
			form.persist(cc);
			return true;
		} catch (ODKFormNotFoundException e1) {
			return false;
		} catch (ODKDatastoreException e) {
			return false;
		}
	}

	@Override
	public Boolean setFormAcceptSubmissions(String formId,
			Boolean acceptSubmissions) {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		try {
			Form form = Form.retrieveForm(formId, cc);
			form.setSubmissionEnabled(acceptSubmissions);
			form.persist(cc);
			return true;
		} catch (ODKFormNotFoundException e1) {
			return false;
		} catch (ODKDatastoreException e) {
			return false;
		}
	}

	public Date purgePublishedData(String uriExternalService, Date earliest)
			throws AccessDeniedException, DatastoreFailureException, RequestFailureException {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);
		
		FormServiceCursor fsc;
		try {
			fsc = FormServiceCursor.getFormServiceCursor(uriExternalService, cc);
		} catch (ODKEntityNotFoundException e) {
			e.printStackTrace();
			throw new RequestFailureException("Unable to retrieve Publishing configuration");
		}

		// any confirm parameter value means OK -- purge it!
		PurgeOlderSubmissions pos = 
			(PurgeOlderSubmissions) cc.getBean(BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN);
		
		if ( pos == null ) {
			// TODO: throw generic server failure exception...
			return null;
		}
		// set up the purge request here...
		Map<String,String> parameters = new HashMap<String,String>();
	  
		parameters.put( PurgeOlderSubmissions.PURGE_DATE, 
						PurgeOlderSubmissions.PURGE_DATE_FORMAT.format(earliest));
		Form form;
		try {
			form = Form.retrieveForm(fsc.getFormId(), cc);
		} catch (ODKFormNotFoundException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to retrieve form " + fsc.getFormId());
		}

		MiscTasks m;
		try {
			m = new MiscTasks(TaskType.PURGE_OLDER_SUBMISSIONS, form, parameters, cc);
			m.persist(cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		}
    	CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
    	ccDaemon.setAsDaemon(true);
    	try {
			pos.createPurgeOlderSubmissionsTask(form, m.getSubmissionKey(), 1L, ccDaemon);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		} catch (ODKFormNotFoundException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		}
    	return earliest;
	}

}
