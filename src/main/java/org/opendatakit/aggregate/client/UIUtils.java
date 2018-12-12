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

import com.google.gwt.user.client.Window;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.web.constants.BasicConsts;

public class UIUtils {

  public static final String CONFIRM_OWNER_EMAIL_TXT = "Please confirm that this e-mail address: ";
  public static final String CONFIRM_OWNER_EMAIL_TXT2 = " is accurate and contains no mispellings. " +
      "This account will become the owner of the published tables.";

  public static String promptForFilterName(ArrayList<FilterGroup> currentFilters) throws Exception {
    boolean match = false;
    String newFilterName = Window.prompt(UIConsts.PROMPT_FOR_NAME_TXT, BasicConsts.EMPTY_STRING);

    while (true) {
      if (newFilterName != null) {
        for (FilterGroup filter : currentFilters) {
          if (filter.getName().equals(newFilterName)) {
            match = true;
          }
        }
      }
      if (newFilterName == null) { // cancel was pressed
        throw new Exception("User Cancelled"); // exit
      } else if (match) {
        match = false;
        newFilterName = Window.prompt(UIConsts.REPROMPT_FOR_NAME_TXT, BasicConsts.EMPTY_STRING);
      } else if (newFilterName.equals(BasicConsts.EMPTY_STRING)) {
        newFilterName = Window.prompt(UIConsts.ERROR_NO_NAME, BasicConsts.EMPTY_STRING);
      } else {
        break;
      }
    }
    return newFilterName;
  }

  public static String promptForREDCapApiKey() throws Exception {
    String newApiKey = Window.prompt(UIConsts.PROMPT_FOR_REDCAP_APIKEY_TXT, BasicConsts.EMPTY_STRING);

    while (true) {
      if (newApiKey == null) { // cancel was pressed
        throw new Exception("User Cancelled"); // exit
      } else if (newApiKey.equals(BasicConsts.EMPTY_STRING)) {
        newApiKey = Window.prompt(UIConsts.REPROMPT_FOR_REDCAP_APIKEY_TXT, BasicConsts.EMPTY_STRING);
      } else {
        return newApiKey;
      }
    }
  }

  public static String promptForEmailAddress() throws Exception {
    String newEmailName = Window.prompt(UIConsts.PROMPT_FOR_EMAIL_TXT, BasicConsts.EMPTY_STRING);

    while (true) {
      if (newEmailName == null) { // cancel was pressed
        throw new Exception("User Cancelled"); // exit
      } else if (newEmailName.equals(BasicConsts.EMPTY_STRING)) {
        newEmailName = Window.prompt(UIConsts.REPROMPT_FOR_EMAIL_TXT, BasicConsts.EMPTY_STRING);
      } else {
        String email = EmailParser.parseEmail(newEmailName);
        if (email != null) {
          if (Window.confirm(CONFIRM_OWNER_EMAIL_TXT + email.substring(EmailParser.K_MAILTO.length()) + CONFIRM_OWNER_EMAIL_TXT2)) {
            return email;
          }
        }
        newEmailName = Window.prompt(UIConsts.REPROMPT_FOR_EMAIL_TXT, BasicConsts.EMPTY_STRING);
      }
    }
  }

}
