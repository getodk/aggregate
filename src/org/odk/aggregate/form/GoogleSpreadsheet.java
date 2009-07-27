/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.form;

import com.google.appengine.api.datastore.Key;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GoogleSpreadsheet {
  /**
   * GAE datastore key that uniquely identifies the form element
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  private Key key;

  /**
   * Name of spreadsheet
   */
  @Enumerated
  private String spreadsheetName;

  /**
   * key of spreadsheet
   */
  @Enumerated
  private String spreadsheetKey;

  /**
   * Authorization Token for speadsheet
   */
  @Enumerated
  private String authToken;

 
  @Enumerated
  private Boolean ready;
  
  public GoogleSpreadsheet(String name, String key) {
    spreadsheetName = name;
    spreadsheetKey = key;
    ready = false;    
  }
  
  public Boolean getReady() {
    return ready;
  }

  public void updateReadyValue() {
    ready = (spreadsheetName != null) && (spreadsheetKey != null) && (authToken != null);   
  }

  public String getSpreadsheetName() {
    return spreadsheetName;
  }

  public String getSpreadsheetKey() {
    return spreadsheetKey;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }
  

}
