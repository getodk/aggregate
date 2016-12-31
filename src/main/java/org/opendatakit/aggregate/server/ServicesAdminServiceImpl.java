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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.AbstractExternalService;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.JsonServer;
import org.opendatakit.aggregate.externalservice.OhmageJsonServer;
import org.opendatakit.aggregate.externalservice.REDCapServer;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServicesAdminServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.externalserv.ServicesAdminService {

  /**
	 *
	 */
  private static final long serialVersionUID = 51251316598366231L;

  @Override
  public ExternServSummary[] getExternalServices(String formId) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (formId.equals(BasicConsts.EMPTY_STRING))
      return null;

    try {
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      List<ExternalService> esList = FormServiceCursor.getExternalServicesForForm(form, cc);

      ExternServSummary[] externServices;
      if (esList.size() > 0) {
        externServices = new ExternServSummary[esList.size()];

        for (int i = 0; i < esList.size(); i++) {
          externServices[i] = esList.get(i).transform();
        }

        return externServices;

      } else {
        return null;
      }

    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @Override
  public String createFusionTable(String formId, ExternalServicePublicationOption esOption,
      String ownerEmail) throws AccessDeniedException, FormNotAvailableException,
      RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for fusion table aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      if (ownerEmail == null || ownerEmail.length() == 0) {
        throw new RequestFailureException("Owner email must be supplied.");
      }
      FusionTable fusion = new FusionTable(form, esOption, ownerEmail, cc);
      fusion.initiate(cc);
      return fusion.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createGoogleSpreadsheet(String formId, String name,
      ExternalServicePublicationOption esOption, String ownerEmail) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for fusion table aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      if (ownerEmail == null || ownerEmail.length() == 0) {
        throw new RequestFailureException("Owner email must be supplied.");
      }
      if (name == null || name.length() == 0) {
        throw new RequestFailureException("Spreadsheet name must be supplied.");
      }
      GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet(form, name, esOption, ownerEmail, cc);
      spreadsheet.initiate(cc);
      return spreadsheet.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createRedCapServer(String formId, String apiKey, String url,
      ExternalServicePublicationOption esOption, String ownerEmail) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // TODO: better error reporting -- form is being deleted. Disallow
      // creation of publishers.
      if (deletionTimestamp != null)
        return null;
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      REDCapServer redcap = new REDCapServer(form, apiKey, url, esOption, ownerEmail, cc);
      redcap.initiate(cc);
      return redcap.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createSimpleJsonServer(String formId, String authKey, String url,
      ExternalServicePublicationOption esOption, String ownerEmail, BinaryOption binaryOption)
      throws AccessDeniedException, FormNotAvailableException, RequestFailureException,
      DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for Simple JSON server aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      AbstractExternalService server = new JsonServer(form, authKey, url, esOption, ownerEmail,
          binaryOption, cc);
      server.initiate(cc);
      return server.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createOhmageJsonServer(String formId, String campaignUrn, String campaignTimestamp,
      String user, String hashedPassword, String url, ExternalServicePublicationOption esOption,
      String ownerEmail) throws AccessDeniedException, FormNotAvailableException,
      RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for Ohmage JSON server aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      OhmageJsonServer server = new OhmageJsonServer(form, campaignUrn, campaignTimestamp, user,
          hashedPassword, url, esOption, ownerEmail, cc);
      server.initiate(cc);
      return server.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public Boolean deletePublisher(String uri) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    FormServiceCursor fsc = null;
    ExternalService es = null;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      if (fsc != null) {
        es = fsc.getExternalService(cc);
      }
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException("Publisher not found");
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }

    if (es != null) {
      try {
        boolean deleted = FormServiceCursor.deleteExternalServiceTask(es, cc);
        // and insert a sleep to let this settle before returning
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
        }
        // success!
        return deleted;
      } catch (ODKOverQuotaException e) {
        e.printStackTrace();
        throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        throw new DatastoreFailureException(e);
      }
    }
    return false;
  }

  @Override
  public void restartPublisher(String uri) throws AccessDeniedException, FormNotAvailableException,
      RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    FormServiceCursor fsc = null;
    ExternalService es = null;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      if (fsc != null) {
        es = fsc.getExternalService(cc);
      }
      if (es == null) {
        throw new RequestFailureException("Service description not found for this publisher");
      }
      OperationalStatus status = fsc.getOperationalStatus();
      if ( status != OperationalStatus.BAD_CREDENTIALS && 
           status != OperationalStatus.ABANDONED &&
           status != OperationalStatus.PAUSED ) {
        throw new RequestFailureException(
            "Rejecting change request -- publisher is not in a failure state");
      }
      es.initiate(cc);
    } catch (RequestFailureException e) {
      throw e;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException("Publisher not found");
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException("Internal error");
    }

  }

  @Override
  public void updateApiKeyAndRestartPublisher(String uri, String apiKey)
      throws AccessDeniedException, FormNotAvailableException, RequestFailureException,
      DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    FormServiceCursor fsc = null;
    ExternalService es = null;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      if (fsc != null) {
        es = fsc.getExternalService(cc);
      }
      if (es == null) {
        throw new RequestFailureException("Service description not found for this publisher");
      }
      if (fsc.getOperationalStatus() != OperationalStatus.BAD_CREDENTIALS) {
        throw new RequestFailureException(
            "Credentials have not failed for this publisher -- rejecting change request");
      }
      if (fsc.getExternalServiceType() != ExternalServiceType.REDCAP_SERVER) {
        throw new RequestFailureException("This publisher is not a REDCap publisher");
      }
      REDCapServer rc = (REDCapServer) es;
      rc.setApiKey(apiKey);
      rc.persist(cc);
      es.initiate(cc);
    } catch (RequestFailureException e) {
      throw e;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException("Publisher not found");
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException("Internal error");
    }

  }

}
