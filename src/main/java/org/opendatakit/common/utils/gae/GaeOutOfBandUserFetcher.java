/*
 * Copyright (C) 2012 University of Washington.
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
package org.opendatakit.common.utils.gae;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import org.opendatakit.common.utils.OutOfBandUserFetcher;

/**
 * Attempt to obtain a user e-mail string from the Google AppEngine
 * OAuthService (Oauth 1.0).
 *
 * @author mitchellsundt@gmail.com
 */
public class GaeOutOfBandUserFetcher implements OutOfBandUserFetcher {

  @Override
  public String getEmail() {

    try {
      OAuthService authService = OAuthServiceFactory.getOAuthService();

      User user = authService.getCurrentUser();

      if (user != null) {
        String email = user.getEmail();
        if (email != null && email.length() != 0) {
          return "mailto:" + email;
        }
      }
    } catch (OAuthRequestException e) {
      // ignore this -- just means it isn't an OAuth-mediated request.
    }
    return null;
  }

}
