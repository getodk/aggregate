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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.JsonServerConsts;
import org.opendatakit.aggregate.constants.externalservice.OhmageJsonServerConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.OhmageJsonElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class OhmageJsonServer extends AbstractExternalService implements
		ExternalService {

	private static final Gson gson;
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(OhmageJsonTypes.Survey.class,
				new OhmageJsonTypes.Survey());
		builder.registerTypeAdapter(OhmageJsonTypes.RepeatableSet.class,
				new OhmageJsonTypes.RepeatableSet());
		builder.serializeNulls();
		builder.setPrettyPrinting();
		gson = builder.create();
	}

	/**
	 * Datastore entity specific to this type of external service
	 */
	private final OhmageJsonServerParameterTable objectEntity;

	private OhmageJsonServer(OhmageJsonServerParameterTable entity,
			FormServiceCursor formServiceCursor, IForm form, CallingContext cc) {
		super(form, formServiceCursor, new BasicElementFormatter(true, true,
				true, false), new BasicHeaderFormatter(true, true, true), cc);
		objectEntity = entity;
	}

	private OhmageJsonServer(OhmageJsonServerParameterTable entity, IForm form,
			ExternalServicePublicationOption externalServiceOption,
			CallingContext cc) throws ODKDatastoreException {
		this(entity, createFormServiceCursor(form, entity,
				externalServiceOption, ExternalServiceType.OHMAGE_JSON_SERVER,
				cc), form, cc);
	}

	public OhmageJsonServer(FormServiceCursor formServiceCursor, IForm form,
			CallingContext cc) throws ODKDatastoreException {
		this(retrieveEntity(OhmageJsonServerParameterTable.assertRelation(cc),
				formServiceCursor, cc), formServiceCursor, form, cc);
	}

	public OhmageJsonServer(IForm form, String serverURL,
			ExternalServicePublicationOption externalServiceOption,
			CallingContext cc) throws ODKDatastoreException {
		this(newEntity(OhmageJsonServerParameterTable.assertRelation(cc), cc),
				form, externalServiceOption, cc);

		// set stuff to ready for now
		// TODO: check for valid URL
		fsc.setIsExternalServicePrepared(true);
		fsc.setOperationalStatus(OperationalStatus.ACTIVE);

		objectEntity.setServerUrl(serverURL);
		persist(cc);
	}

	public String getServerUrl() {
		return objectEntity.getServerUrl();
	}

	@Override
	public void sendSubmission(Submission submission, CallingContext cc)
			throws ODKExternalServiceException {
		// TODO: think of more appropriate method
		List<Submission> list = new ArrayList<Submission>();
		list.add(submission);
		sendSubmissions(list, cc);
	}

	@Override
	public void sendSubmissions(List<Submission> submissions, CallingContext cc)
			throws ODKExternalServiceException {
		try {
			Map<UUID, byte[]> photos = new HashMap<UUID, byte[]>();
			List<OhmageJsonTypes.Survey> surveys = new ArrayList<OhmageJsonTypes.Survey>();

			for (Submission submission : submissions) {
				OhmageJsonTypes.Survey survey = new OhmageJsonTypes.Survey();
				// TODO: figure out these values
				survey.setDate(null);
				survey.setLocation(null);
				survey.setLocation_status(null);
				survey.setSurvey_id(null);
				survey.setSurvey_lauch_context(null);
				survey.setTime(System.currentTimeMillis());
				survey.setTimezone(null);

				OhmageJsonElementFormatter formatter = new OhmageJsonElementFormatter();
				// called purely for side effects
				submission.getFormattedValuesAsRow(null, formatter, false, cc);
				survey.setResponses(formatter.getResponses());

				photos.putAll(formatter.getPhotos());
				surveys.add(survey);
			}

			uploadSurveys(surveys, photos, cc);

		} catch (ODKExternalServiceCredentialsException e) {
		  fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
		  try {
		    persist(cc);
		  } catch ( Exception ex) {
		    ex.printStackTrace();
		    throw new ODKExternalServiceException(
		          "unable to persist bad credentials state", ex);
		  }
		  throw e;
		} catch (ODKExternalServiceException e) {
		  throw e;// don't wrap these
		} catch (Exception e) {
			throw new ODKExternalServiceException(e);
		}

	}

	/**
	 * Uploads a set of submissions to the ohmage system.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws ODKExternalServiceException
	 * @throws URISyntaxException
	 */
	public void uploadSurveys(List<OhmageJsonTypes.Survey> surveys,
			Map<UUID, byte[]> photos, CallingContext cc)
			throws ClientProtocolException, IOException,
			ODKExternalServiceException, URISyntaxException {

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				JsonServerConsts.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams,
				JsonServerConsts.CONNECTION_TIMEOUT);

		HttpClientFactory factory = (HttpClientFactory) cc
				.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
		HttpClient client = factory.createHttpClient(httpParams);
		HttpPost httppost = new HttpPost(getServerUrl()
				+ OhmageJsonServerConsts.OHMAGE_SURVEY_UPLOAD_PATH);

		// TODO: figure out campaign parameters
		StringBody campaignUrn = new StringBody("some campaign urn");
		StringBody campaignCreationTimestamp = new StringBody(
				"the creation timestamp");
		StringBody user = new StringBody("username");
		StringBody hashedPassword = new StringBody("the hashed password");
		StringBody clientParam = new StringBody("aggregate");
		StringBody surveyData = new StringBody(gson.toJson(surveys));

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("campaign_urn", campaignUrn);
		reqEntity.addPart("campaign_creation_timestamp",
				campaignCreationTimestamp);
		reqEntity.addPart("user", user);
		reqEntity.addPart("passowrd", hashedPassword);
		reqEntity.addPart("client", clientParam);
		reqEntity.addPart("survey", surveyData);
		for (Entry<UUID, byte[]> entry : photos.entrySet()) {
			InputStreamBody imageAttachment = new InputStreamBody(
					new ByteArrayInputStream(entry.getValue()), "image/jpeg",
					entry.getKey().toString());
			reqEntity.addPart(entry.getKey().toString(), imageAttachment);
		}

		httppost.setEntity(reqEntity);

		HttpResponse response = client.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		
		OhmageJsonTypes.server_response serverResp = null;
		if ( resEntity != null ) {
		  Reader resReader = new InputStreamReader(resEntity.getContent());
		  serverResp = gson.fromJson(resReader,
				OhmageJsonTypes.server_response.class);
		  // flush any remaining
		  try {
		    while (resReader.read() != -1);
		  } catch ( IOException e) {
		    // ignore...
		  }
		}
		
		int statusCode = response.getStatusLine().getStatusCode();
		
		if ( statusCode == HttpServletResponse.SC_UNAUTHORIZED ) {
        throw new ODKExternalServiceCredentialsException("failure from server: " + statusCode);
		} else if ( statusCode >= 300 || (serverResp != null &&
		            serverResp.getResult().equals("failure"))) {
        throw new ODKExternalServiceException("failure from server: " + statusCode);
  	   }
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OhmageJsonServer)) {
			return false;
		}
		OhmageJsonServer other = (OhmageJsonServer) obj;
		return (objectEntity == null ? (other.objectEntity == null)
				: (other.objectEntity != null && objectEntity
						.equals(other.objectEntity)))
				&& (fsc == null ? (other.fsc == null)
						: (other.fsc != null && fsc.equals(other.fsc)));
	}

	@Override
	protected void insertData(Submission submission, CallingContext cc)
			throws ODKExternalServiceException {
		sendSubmission(submission, cc);
	}

	@Override
	public String getDescriptiveTargetString() {
		return getServerUrl();
	}

	protected CommonFieldsBase retrieveObjectEntity() {
		return objectEntity;
	}

	@Override
	protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
		return null;
	}

}
