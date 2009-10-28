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


package org.odk.aggregate.form.remoteserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.odk.aggregate.form.Form;
import org.odk.aggregate.report.FormProperties;
import org.odk.aggregate.submission.Submission;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Entity
public class RhizaInsight implements RemoteServer {

  public static final int CONNECTION_TIMEOUT = 10000;

  /**
   * GAE datastore key that uniquely identifies the form element
   */
  @SuppressWarnings("unused")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  /**
   * URL of Insight Server
   */
  @Enumerated
  private Link serverUrl;

  public RhizaInsight(Link rhizaServerUrl) {
    serverUrl = rhizaServerUrl;
  }

  public Link getServerUrl() {
    return serverUrl;
  }

  public void sendSubmissionToRemoteServer(Form xform, String serverName, EntityManager em,
      String appName, Submission submission) {

    FormProperties formProp = new FormProperties(xform, em);
    sendSubmissionToRemoteServer(formProp, submission);
  }

  public void sendSubmissionToRemoteServer(FormProperties formProp, Submission submission) {

    System.out.println("Sending Rhiza Submission");
    
    Form xform = formProp.getForm();
    
    JsonPrimitive uuid = new JsonPrimitive(xform.getOdkId());
    JsonObject data = submission.generateJsonObject(formProp.getPropertyNames());
    JsonArray root = new JsonArray();

    root.add(uuid);
    root.add(data);
    System.out.println(root.toString());

    try {
      // TODO: understand what advantage link has
      URL url = new URL(serverUrl.getValue());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setConnectTimeout(CONNECTION_TIMEOUT);


      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
      writer.write(root.toString());
      writer.close();

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        return; // ok
      } else {
        // TODO: decide what to do - ask rhiza company
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
      // TODO: do something important with error
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: do something important with error
    }

  }


}
