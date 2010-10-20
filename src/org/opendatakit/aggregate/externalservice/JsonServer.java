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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.externalservice.constants.JsonServerType;
import org.opendatakit.aggregate.format.element.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.element.HeaderFormatter;
import org.opendatakit.aggregate.format.structure.JsonFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonServer implements ExternalService {

	private final FormDefinition formDefinition;

	/**
	 * Datastore entity holding registration of an external service for a
	 * specific form and the cursor position within that form that was last
	 * processed by this service.
	 */
	private final FormServiceCursor fsc;

	/**
	 * Datastore entity specific to this type of external service
	 */
	private final JsonServerParameterTable objectEntity;

	private final Datastore datastore;

	private final User user;
	
	private final Realm realm;

	/**
	 * NOT PERSISTED - created for each object
	 */
	private HeaderFormatter headerFormatter;
	private List<String> headers;

	public static final int CONNECTION_TIMEOUT = 10000;

	public static final Map<ElementType, JsonServerType> typeMap;
	
	static {
		typeMap = new HashMap<ElementType, JsonServerType>();
		typeMap.put(ElementType.STRING, JsonServerType.STRING);
		typeMap.put(ElementType.JRDATETIME, JsonServerType.DATE);
		typeMap.put(ElementType.JRDATE, JsonServerType.DATE);
		typeMap.put(ElementType.JRTIME, JsonServerType.DATE);
		typeMap.put(ElementType.INTEGER, JsonServerType.NUMBER);
		typeMap.put(ElementType.DECIMAL, JsonServerType.NUMBER);
		typeMap.put(ElementType.GEOPOINT, JsonServerType.GPS);
		
		typeMap.put(ElementType.BOOLEAN, JsonServerType.STRING);
		typeMap.put(ElementType.BINARY, JsonServerType.CONTENT_TYPE);
		typeMap.put(ElementType.SELECT1, JsonServerType.STRING);
		typeMap.put(ElementType.SELECTN, JsonServerType.STRING);
		typeMap.put(ElementType.REPEAT, JsonServerType.STRING);
		typeMap.put(ElementType.GROUP, JsonServerType.STRING);
	}

	public JsonServer(FormDefinition formDefinition, FormServiceCursor fsc,
			Datastore datastore, User user, Realm realm)
			throws ODKEntityNotFoundException, ODKDatastoreException,
			ODKExternalServiceException {
		this.datastore = datastore;
		this.user = user;
		this.realm = realm;
		this.formDefinition = formDefinition;
		this.fsc = fsc;

		objectEntity = datastore.getEntity(JsonServerParameterTable
				.createRelation(datastore, user), fsc.getSubAuri(), user);

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);

		createForm();
	}

	public JsonServer(FormDefinition formDefinition, String serverURL,
			ExternalServiceOption externalServiceOption,
			Datastore datastore, User user, Realm realm) throws ODKDatastoreException,
			ODKExternalServiceException {
		this.datastore = datastore;
		this.user = user;
		this.realm = realm;
		this.formDefinition = formDefinition;

		fsc = datastore.createEntityUsingRelation(FormServiceCursor
				.createRelation(datastore, user), null, user);
		objectEntity = datastore.createEntityUsingRelation(
				JsonServerParameterTable.createRelation(datastore, user),
				new EntityKey(fsc, fsc.getUri()), user);
		fsc.setSubAuri(objectEntity.getUri());
		fsc.setServiceClassname(JsonServer.class.getCanonicalName());
		fsc.setExternalServiceOption(externalServiceOption);
		fsc.setEstablishmentDateTime(new Date());
		fsc.setUploadCompleted(false);
		objectEntity.setServerUrl(serverURL);
		datastore.putEntity(fsc, user);
		datastore.putEntity(objectEntity, user);

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);

		createForm();
	}

	public void persist() throws ODKEntityPersistException {
		datastore.putEntity(objectEntity, user);
		datastore.putEntity(fsc, user);
	}

	public void delete() throws ODKDatastoreException {
		datastore.deleteEntity(new EntityKey(objectEntity, objectEntity
				.getUri()), user);
		datastore.deleteEntity(new EntityKey(fsc, fsc.getUri()), user);
	}

	public String getServerUrl() {
		return objectEntity.getServerUrl();
	}

	private void createForm() throws ODKExternalServiceException {
		System.out.println("BEGINNING INSERTION");

		JsonArray def = new JsonArray();

		JsonObject jsonFormBase = new JsonObject();
		jsonFormBase.addProperty("ODKID", formDefinition.getFormId());
		def.add(jsonFormBase);

		// TODO: Waylon -- I need to understand this to recommend an alternative
		// mechanism...

		List<ElementType> types = headerFormatter.getHeaderTypes();
		for ( int i = 0 ; i < headers.size() ; ++i ) {
			String name = headers.get(i);
			ElementType type = types.get(i);
			JsonObject element = new JsonObject();
			JsonServerType jt = typeMap.get(type);
			if ( jt == JsonServerType.CONTENT_TYPE ) {
				// TODO: need to handle the case where element is
				// a binary -- we don't know whether it is really 
				// a picture or not, but should we claim it to be?
				element.addProperty("TYPE", "picture");
			} else {
				element.addProperty("TYPE", jt.getJsonServerTypeValue());
			}
			element.addProperty("LABEL", name);
			def.add(element);
		}

		System.out.println(def.toString());

		try {
			// TODO: change so not hard coded
			URL url = new URL(
					"http://floresta.rhizalabs.com/cbi/upload/createDataset");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(CONNECTION_TIMEOUT);

			OutputStreamWriter writer = new OutputStreamWriter(connection
					.getOutputStream(), "UTF-8");
			writer.write(def.toString());

			writer.close();

			System.out.println(connection.getResponseMessage());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStreamReader is = new InputStreamReader(connection
						.getInputStream());
				BufferedReader reader = new BufferedReader(is);

				String responseLine = reader.readLine();
				while (responseLine != null) {
					System.out.print(responseLine);
					responseLine = reader.readLine();
				}
				is.close();
			} else {
				System.out.println("FAILURE OF RHIZA DATA COLLECTION CREATION");
			}
		} catch (Exception e) {
			throw new ODKExternalServiceException(e.getCause());
		}
	}

	@Override
	public void sendSubmission(Submission submission)
			throws ODKExternalServiceException {
		// TODO: think of more appropriate method
		List<Submission> list = new ArrayList<Submission>();
		list.add(submission);
		sendSubmissions(list);
	}

	@Override
	public void sendSubmissions(List<Submission> submissions)
			throws ODKExternalServiceException {
		try {

			URL url = new URL(getServerUrl());
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(CONNECTION_TIMEOUT);

			System.out.println("Sending JSON Submissions");

			OutputStreamWriter writer = new OutputStreamWriter(connection
					.getOutputStream(), "UTF-8");
			PrintWriter pWriter = new PrintWriter(writer);

			JsonFormatter formatter = new JsonFormatter(pWriter, null,
					formDefinition, datastore, user, realm);
			formatter.processSubmissions(submissions);

			writer.flush();

			// TODO: PROBLEM - NOT good for only one response code check at the
			// end

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return; // ok
			} else {
				// TODO: decide what to do
			}

		} catch (Exception e) {
			throw new ODKExternalServiceException(e.getCause());
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
				: (objectEntity.equals(other.objectEntity)))
				&& (fsc == null ? (other.fsc == null) : (fsc.equals(other.fsc)));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 13;
		if (objectEntity != null)
			hashCode += objectEntity.hashCode();
		if (fsc != null)
			hashCode += fsc.hashCode();
		return hashCode;
	}
	

	@Override
	public void setUploadCompleted() throws ODKEntityPersistException {
		fsc.setUploadCompleted(true);
		datastore.putEntity(fsc, user);
	}
}
