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

package org.odk.aggregate.form.remoteserver;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.table.SubmissionSpreadsheetTable;

import com.google.appengine.api.datastore.Key;

@Entity
public class GoogleSpreadsheetOAuth implements RemoteServer{
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
   * id of worksheet
   */
  @Enumerated
  private String worksheetId;

  /**
   * Authorization Token for speadsheet
   */
  @Enumerated
  private String authToken;
  
  /**
   * Authorization Secret for speadsheet
   */
  @Enumerated
  private String tokenSecret;

 
  @Enumerated
  private Boolean ready;
  
  public GoogleSpreadsheetOAuth(String name, String key) {
    spreadsheetName = name;
    spreadsheetKey = key;
    ready = false;    
  }
  
  public Boolean getReady() {
    return ready;
  }

  public void updateReadyValue() {
    ready = (spreadsheetName != null) && (spreadsheetKey != null) && (authToken != null) && (tokenSecret != null);   
  }

  public String getSpreadsheetName() {
    return spreadsheetName;
  }

  public String getSpreadsheetKey() {
    return spreadsheetKey;
  }

  public OAuthToken getAuthToken() {
    return new OAuthToken(authToken, tokenSecret);
  }

  public void setAuthToken(OAuthToken authToken) {
    this.authToken = authToken.getToken();
    this.tokenSecret = authToken.getTokenSecret();
  }
  
  public String getWorksheetId()
  {
    return this.worksheetId;
  }
  
  public void setWorksheetId(String worksheetId)
  {
    this.worksheetId = worksheetId;
  }
  
  public void sendSubmissionToRemoteServer(Form xform, String serverName, EntityManager em, String appName, Submission submission) {
    SubmissionSpreadsheetTable subResults =
      new SubmissionSpreadsheetTable(xform, serverName, em, appName);

  subResults.insertNewDataInSpreadsheet(submission, this);
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GoogleSpreadsheet)) {
      return false;
    }
    GoogleSpreadsheetOAuth other = (GoogleSpreadsheetOAuth) obj;
    return (key == null ? (other.key == null) : (key.equals(other.key)))
        && (spreadsheetName == null ? (other.spreadsheetName == null) : (spreadsheetName.equals(other.spreadsheetName)))
        && (spreadsheetKey == null ? (other.spreadsheetKey == null) : (spreadsheetKey.equals(other.spreadsheetKey)))
        && (authToken == null ? (other.authToken == null) : (authToken.equals(other.authToken)))
        && (tokenSecret == null ? (other.tokenSecret == null) : (tokenSecret.equals(other.tokenSecret)))
        && (ready == null ? (other.ready == null) : (ready.equals(other.ready)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if(key != null) hashCode += key.hashCode();
    if(spreadsheetName != null) hashCode += spreadsheetName.hashCode();
    if(spreadsheetKey != null) hashCode += spreadsheetKey.hashCode();
    if(authToken != null) hashCode += authToken.hashCode();
    if(tokenSecret != null) hashCode += tokenSecret.hashCode();
    if(ready != null) hashCode += ready.hashCode();
    return hashCode;
  }
}

