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

package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.admin.FormAdminService;
import org.opendatakit.aggregate.client.form.admin.FormAdminServiceAsync;
import org.opendatakit.aggregate.client.preferences.PreferenceService;
import org.opendatakit.aggregate.client.preferences.PreferenceServiceAsync;
import org.opendatakit.aggregate.client.services.admin.ServicesAdminService;
import org.opendatakit.aggregate.client.services.admin.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * This class wraps GWT.create() so that a ODK-specific header can be set on all
 * GWT requests to identify those requests as gwt requests.  This allows ODK's 
 * GWTAccessDeniedHandlerImpl to detect requests failing the declarative security
 * model in WEB-INF/applicationContext-security.xml and throw an 
 * AccessDeniedException back up through the GWT RPC mechanism.  Without the header,
 * the failed requests would be redirected to an access-denied.html static page.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class SecureGWT {
	public enum ServiceType {
		FILTER,
		FORM,
		FORM_ADMIN,
		PREFERENCE,
		SERVICES_ADMIN,
		SUBMISSION
	}
	
	private static SecureGWT singleton = null;
	
	private static final RpcRequestBuilder reqBuilder = new RpcRequestBuilder() {

		@Override
		protected RequestBuilder doCreate(String serviceEntryPoint) {
		    RequestBuilder rb = super.doCreate(serviceEntryPoint);
		    rb.setHeader("X-opendatakit-gwt", "yes");
		    return rb;
		}
		
	};
	
	private SecureGWT()
	{};
	
	public static synchronized final SecureGWT get() {
		if ( singleton == null ) {
			singleton = new SecureGWT();
		}
		return singleton;
	}
	
	private Object create(ServiceType type) {
		Object obj = null;
		switch (type) {
		case FILTER:
			obj = GWT.create(FilterService.class);
			break;
		case FORM:
			obj = GWT.create(FormService.class);
			break;
		case FORM_ADMIN:
			obj = GWT.create(FormAdminService.class);
			break;
		case PREFERENCE:
			obj = GWT.create(PreferenceService.class);
			break;
		case SERVICES_ADMIN:
			obj = GWT.create(ServicesAdminService.class);
			break;
		case SUBMISSION:
			obj = GWT.create(SubmissionService.class);
			break;
		default:
			throw new IllegalStateException("Unrecognized type " + type.toString());
		}
		ServiceDefTarget sd = (ServiceDefTarget) obj;
		sd.setRpcRequestBuilder(reqBuilder);
		return obj;
	}
	
	public FilterServiceAsync createFilterService() {
		return (FilterServiceAsync) create(ServiceType.FILTER);
	}

	public FormServiceAsync createFormService() {
		return (FormServiceAsync) create(ServiceType.FORM);
	}

	public FormAdminServiceAsync createFormAdminService() {
		return (FormAdminServiceAsync) create(ServiceType.FORM_ADMIN);
	}

	public PreferenceServiceAsync createPreferenceService() {
		return (PreferenceServiceAsync) create(ServiceType.PREFERENCE);
	}

	public ServicesAdminServiceAsync createServicesAdminService() {
		return (ServicesAdminServiceAsync) create(ServiceType.SERVICES_ADMIN);
	}

	public SubmissionServiceAsync createSubmissionService() {
		return (SubmissionServiceAsync) create(ServiceType.SUBMISSION);
	}
}
