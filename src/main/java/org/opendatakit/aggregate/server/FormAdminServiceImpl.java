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

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
  public Boolean setFormAcceptSubmissions(String formId, Boolean acceptSubmissions) {
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

}
