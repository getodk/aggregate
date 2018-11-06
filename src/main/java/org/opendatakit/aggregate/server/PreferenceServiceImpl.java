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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.preferences.PreferenceSummary;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.preferences.PreferenceService {

  private static final Logger log = LoggerFactory.getLogger(PreferenceServiceImpl.class);
  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -489283284844600170L;

  @Override
  public PreferenceSummary getPreferences() throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      return ServerPreferencesProperties.getPreferenceSummary(cc);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    }
  }

  @Override
  public void setFasterBackgroundActionsDisabled(Boolean disabled) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      ServerPreferencesProperties.setFasterBackgroundActionsDisabled(cc, disabled);

      log.info("setFasterBackgroundActionsDisabled as: " + Boolean.toString(disabled));
      Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
      // NOTE: this will fire off a watchdog worker
      // if we are re-enabling faster publishing
      //
      // No-op if not changed.
      wd.setFasterWatchdogCycleEnabled(!disabled);

    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    }

  }

  @Override
  public void setSkipMalformedSubmissions(Boolean skipMalformedSubmissions)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      ServerPreferencesProperties.setSkipMalformedSubmissions(cc, skipMalformedSubmissions);

      log.info("setSkipMalformedSubmissions as: " + Boolean.toString(skipMalformedSubmissions));

    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    }

  }
}
