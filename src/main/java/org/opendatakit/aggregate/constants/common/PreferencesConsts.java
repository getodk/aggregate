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

package org.opendatakit.aggregate.constants.common;


public enum PreferencesConsts implements HelpSliderConsts {
  GOOGLE("Google API Credentials", "These credentials are used when publishing into Google services."),
  ENKETO("Enketo Credentials", "These credentials are used for Enketo webforms integration."),
  FEATURES("Aggregate Features",
      "These settings affect the operations of the server.<br>" +
          "1. Disable faster background actions - check this to reduce AppEngine quota usage.<br>" +
          "2. ODK Tables Synchronization Functionality - check this to enable ODK Tables functionality.");

  private String title;
  private String content;

  private PreferencesConsts(String titleString, String contentString) {
    title = titleString;
    content = contentString;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }
}