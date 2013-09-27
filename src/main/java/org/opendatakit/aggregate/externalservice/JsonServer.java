/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.externalservice;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.structure.JsonFormatterWithFilters;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class JsonServer extends AbstractExternalService implements ExternalService {

  /**
   * Datastore entity specific to this type of external service
   */
  private final JsonServer3ParameterTable objectEntity;

  private JsonServer(JsonServer3ParameterTable entity, FormServiceCursor formServiceCursor,
      IForm form, CallingContext cc) {
    super(form, formServiceCursor, new BasicElementFormatter(true, true, true, false),
        new BasicHeaderFormatter(true, true, true), cc);
    objectEntity = entity;
  }

  private JsonServer(JsonServer3ParameterTable entity, IForm form,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException {
    this(entity, createFormServiceCursor(form, entity, externalServiceOption,
        ExternalServiceType.JSON_SERVER, cc), form, cc);
    objectEntity.setOwnerEmail(ownerEmail);
  }

  public JsonServer(FormServiceCursor formServiceCursor, IForm form, CallingContext cc)
      throws ODKDatastoreException {
    this(retrieveEntity(JsonServer3ParameterTable.assertRelation(cc), formServiceCursor, cc),
        formServiceCursor, form, cc);
  }

  public JsonServer(IForm form, String authKey, String serverURL,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, BinaryOption binaryOption, CallingContext cc)
      throws ODKDatastoreException {
    this(newEntity(JsonServer3ParameterTable.assertRelation(cc), cc), form, externalServiceOption,
        ownerEmail, cc);

    objectEntity.setServerUrl(serverURL);
    objectEntity.setAuthKey(authKey);
    objectEntity.setBinaryOption(binaryOption);
    persist(cc);
  }

  @Override
  public void initiate(CallingContext cc) throws ODKExternalServiceException,
      ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException {
    // set stuff to ready for now
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);

    // upload data to external service
    postUploadTask(cc);
  }

  @Override
  protected String getOwnership() {
    return objectEntity.getOwnerEmail().substring(EmailParser.K_MAILTO.length());
  }

  public String getServerUrl() {
    return objectEntity.getServerUrl();
  }

  public String getAuthKey() {
    return objectEntity.getAuthKey();
  }

  private void sendRequest(String url, HttpEntity postBody, CallingContext cc)
      throws ODKExternalServiceException {
    try {

      HttpResponse resp = super.sendHttpRequest(POST, url, postBody, null, cc);
      WebUtils.readResponse(resp);

      // get response
      int statusCode = resp.getStatusLine().getStatusCode();
      String reason = resp.getStatusLine().getReasonPhrase();
      if(reason == null) {
        reason = BasicConsts.EMPTY_STRING;
      }
      if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
        throw new ODKExternalServiceCredentialsException(reason + " (" + statusCode + ")");
      } else if (statusCode != HttpServletResponse.SC_OK) {
        throw new ODKExternalServiceException(reason + " (" + statusCode + ")");
      }
    } catch (ODKExternalServiceException e) {
      throw e; // don't wrap these...
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);// wrap...
    }
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JsonServer)) {
      return false;
    }
    JsonServer other = (JsonServer) obj;
    return (objectEntity == null ? (other.objectEntity == null)
        : (other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (other.fsc != null && fsc.equals(other.fsc)));
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    try {
      BinaryOption option = objectEntity.getBinaryOption();

      ByteArrayOutputStream baStream = new ByteArrayOutputStream();
      PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(baStream, HtmlConsts.UTF8_ENCODE));

      System.out.println("Sending one JSON Submission");

      // format submission
      JsonFormatterWithFilters formatter = new JsonFormatterWithFilters(pWriter, form, null, option,
          true, cc.getServerURL());
      formatter.processSubmissions(Collections.singletonList(submission), cc);
      pWriter.flush();

      JsonParser parser = new JsonParser();
      JsonElement submissionJsonObj = parser.parse(baStream.toString(HtmlConsts.UTF8_ENCODE));

      // create json object
      JsonObject entity = new JsonObject();
      entity.addProperty("token", getAuthKey());
      entity.addProperty("content", "record");
      entity.addProperty("formId", form.getFormId());
      entity.addProperty("formVersion", form.getMajorMinorVersionString());
      entity.add("data", submissionJsonObj);

      StringEntity postentity = new StringEntity(entity.toString());
      postentity.setContentType("application/json");

      this.sendRequest(getServerUrl(), postentity, cc);
    } catch (ODKExternalServiceCredentialsException e) {
      fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
      try {
        persist(cc);
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new ODKExternalServiceException("unable to persist bad credentials status", e1);
      }
      throw e; // don't wrap
    } catch (ODKExternalServiceException e) {
      throw e; // don't wrap
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }
  }

  @Override
  public String getDescriptiveTargetString() {
    // the token, if supplied, is a secret.
    // Show only the first 4 characters, or,
    // if the string is less than 8 characters long, show less.
    String auth = getAuthKey();
    if (auth != null && auth.length() != 0) {
      auth = " token: " + auth.substring(0, Math.min(4, auth.length() / 3)) + "...";
    }
    return getServerUrl() + auth;
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return null;
  }

}
