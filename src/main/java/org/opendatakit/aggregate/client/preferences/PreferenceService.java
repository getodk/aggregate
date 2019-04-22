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

package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.opendatakit.aggregate.client.exception.RequestFailureException;

/**
 * These actions require the ROLE_USER privilege, which is the least capable
 * privilege (granted to all authorized users of the system).
 *
 * @author wbrunette@gmail.com
 */
@RemoteServiceRelativePath("preferenceservice")
public interface PreferenceService extends RemoteService {
  PreferenceSummary getPreferences() throws RequestFailureException;

  @XsrfProtect
  void setSkipMalformedSubmissions(Boolean skipMalformedSubmissions) throws RequestFailureException;

  String getVersioNote();
}
