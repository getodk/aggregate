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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.externalservice.constants.FusionTableType;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.element.HeaderFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

public class FusionTable extends AbstractExternalService implements
		ExternalService {

	/**
	 * Datastore entity holding registration of an external service for a
	 * specific form and the cursor position within that form that was last
	 * processed by this service.
	 */
	private FormServiceCursor fsc;

	/**
	 * Datastore entity specific to this type of external service
	 */
	private FusionTableParameterTable objectEntity;

	private Datastore datastore;

	private User user;

	private static final String CREATE_FUSION_RESPONSE_HEADER = "tableid";

	private static final String FUSIONTABLES_SERVICE_NAME = "fusiontables";

	public static final String URL_STRING = "http://tables.googlelabs.com/api/query";

	public static final String ENCODE_SCHEME = "UTF-8";

	public static final Map<ElementType, FusionTableType> typeMap;
	
	static {
		typeMap = new HashMap<ElementType, FusionTableType>();
		typeMap.put(ElementType.STRING, FusionTableType.STRING);
		typeMap.put(ElementType.JRDATETIME, FusionTableType.DATE);
		typeMap.put(ElementType.JRDATE, FusionTableType.DATE);
		typeMap.put(ElementType.JRTIME, FusionTableType.DATE);
		typeMap.put(ElementType.INTEGER, FusionTableType.NUMBER);
		typeMap.put(ElementType.DECIMAL, FusionTableType.NUMBER);
		typeMap.put(ElementType.GEOPOINT, FusionTableType.GPS);
		
		typeMap.put(ElementType.BOOLEAN, FusionTableType.STRING);
		typeMap.put(ElementType.BINARY, FusionTableType.STRING);
		typeMap.put(ElementType.SELECT1, FusionTableType.STRING);
		typeMap.put(ElementType.SELECTN, FusionTableType.STRING);
		typeMap.put(ElementType.REPEAT, FusionTableType.STRING);
		typeMap.put(ElementType.GROUP, FusionTableType.STRING);
	}
	/**
	 * NOT PERSISTED
	 */
	private GoogleService fusionTableService;

	/**
	 * NOT PERSISTED - created for each object
	 */
	private HeaderFormatter headerFormatter;
	private List<String> headers;

	public FusionTable(FormDefinition formDefinition, FormServiceCursor fsc,
			Datastore datastore, User user)
			throws ODKEntityNotFoundException, ODKDatastoreException {
		super(formDefinition, new BasicElementFormatter(true, true, true));
		this.datastore = datastore;
		this.user = user;
		this.fsc = fsc;
		objectEntity = datastore.getEntity(FusionTableParameterTable
				.createRelation(datastore, user), fsc.getSubAuri(), user);

		fusionTableService = new GoogleService(FUSIONTABLES_SERVICE_NAME,
				AbstractExternalService.APP_NAME);
		fusionTableService.setAuthSubToken(getAuthToken());

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);
	}

	public FusionTable(FormDefinition formDefinition, String authToken,
			ExternalServiceOption externalServiceOption,
			Datastore datastore, User user) throws ODKDatastoreException,
			ODKExternalServiceException {
		super(formDefinition, new BasicElementFormatter(true, true, true));
		this.datastore = datastore;
		this.user = user;
		fsc = datastore.createEntityUsingRelation(FormServiceCursor
				.createRelation(datastore, user), null, user);
		objectEntity = datastore.createEntityUsingRelation(
				FusionTableParameterTable.createRelation(datastore, user),
				new EntityKey(fsc, fsc.getUri()), user);
		fsc.setSubAuri(objectEntity.getUri());
		fsc.setServiceClassname(FusionTable.class.getCanonicalName());
		fsc.setExternalServiceOption(externalServiceOption);
		fsc.setEstablishmentDateTime(new Date());
		fsc.setUploadCompleted(false);
		objectEntity.setAuthToken(authToken);

		fusionTableService = new GoogleService(FUSIONTABLES_SERVICE_NAME,
				AbstractExternalService.APP_NAME);
		fusionTableService.setAuthSubToken(authToken);

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);

		objectEntity.setFusionTableName(createFormStorageForRemoteService());
		datastore.putEntity(objectEntity, user);
		datastore.putEntity(fsc, user);
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

	public String getTableName() {
		return objectEntity.getFusionTableName();
	}

	public String getAuthToken() {
		return objectEntity.getAuthToken();
	}

	@Override
	protected void insertData(Submission submission)
			throws ODKExternalServiceException {
		// TODO: Waylon -- don't understand what you're doing here...
		try {
			Row row = submission.getFormattedValuesAsRow(null, formatter);

			String insertQuery = "INSERT INTO "
					+ getTableName()
					+ createCsvString(headers.iterator(), false)
					+ " VALUES "
					+ createCsvString(row.getFormattedValues().iterator(), true);
			executeInsert(fusionTableService, insertQuery);
		} catch (Exception e) {
			throw new ODKExternalServiceException(e.getCause());
		}
	}

	private String createFormStorageForRemoteService()
			throws ODKExternalServiceException {
		// types are in the same order as the headers...
		List<ElementType> types = headerFormatter.getHeaderTypes();
		StringBuilder createQuery = new StringBuilder();
		createQuery.append("CREATE TABLE ");
		createQuery.append(formDefinition.getFormId());
		createQuery.append(BasicConsts.LEFT_PARENTHESIS);

		boolean first = true;
		for ( int i = 0 ; i < headers.size() ; ++i ) {
			String name = headers.get(i);
			ElementType type = types.get(i);
			if ( !first ) {
				createQuery.append(BasicConsts.COMMA);
			}
			first = false;
			createQuery.append(BasicConsts.SINGLE_QUOTE);
			createQuery.append(name);
			createQuery.append(BasicConsts.SINGLE_QUOTE);
			createQuery.append(BasicConsts.COLON);
			createQuery.append(typeMap.get(type).getFusionTypeValue());
		}

		createQuery.append(BasicConsts.RIGHT_PARENTHESIS);

		String resultRequest;
		try {
			resultRequest = executeInsert(fusionTableService, createQuery.toString());
		} catch (Exception e) {
			throw new ODKExternalServiceException(e.getCause());
		}

		int index = resultRequest.lastIndexOf(CREATE_FUSION_RESPONSE_HEADER);
		if (index > 0) {
			String tableid = resultRequest.substring(index
					+ CREATE_FUSION_RESPONSE_HEADER.length());
			return tableid.trim();
		} else {
			throw new ODKExternalServiceException(
					ErrorConsts.ERROR_OBTAINING_FUSION_TABLE_ID);
		}
	}

	// TODO: make more of a utility function with CSV
	private String createCsvString(Iterator<String> itr, boolean header) {
		StringBuilder str = new StringBuilder();
		str.append(BasicConsts.SPACE + BasicConsts.LEFT_PARENTHESIS);
		while (itr.hasNext()) {
			str.append(BasicConsts.SINGLE_QUOTE);
			if (header) {
				str.append(itr.next());
			} else {
				str.append(itr.next());
			}
			str.append(BasicConsts.SINGLE_QUOTE);
			if (itr.hasNext()) {
				str.append(FormatConsts.CSV_DELIMITER);
			}
		}
		str.append(BasicConsts.RIGHT_PARENTHESIS + BasicConsts.SPACE);
		return str.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FusionTable)) {
			return false;
		}
		FusionTable other = (FusionTable) obj;
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

	private String executeInsert(GoogleService service, String insertStmt)
			throws MalformedURLException, IOException, ServiceException {
		GDataRequest request = service.getRequestFactory().getRequest(
				RequestType.INSERT, new URL(URL_STRING),
				new ContentType("application/x-www-form-urlencoded"));
		OutputStreamWriter writer = new OutputStreamWriter(request
				.getRequestStream());

		System.out.println(URLEncoder.encode(insertStmt, ENCODE_SCHEME));
		writer.append("sql=" + URLEncoder.encode(insertStmt, ENCODE_SCHEME));
		writer.flush();
		request.execute();

		BufferedReader line = new BufferedReader(new InputStreamReader(request
				.getResponseStream()));
		String tmpString = line.readLine();
		String response = null;
		while (tmpString != null) {
			response += tmpString + BasicConsts.NEW_LINE;
			tmpString = line.readLine();
		}

		return response;

	}

	private String executeQuery(GoogleService service, String queryStmt)
			throws IOException, ServiceException {
		URL url = new URL(URL_STRING + "?sql="
				+ URLEncoder.encode(queryStmt, "UTF-8"));
		GDataRequest request = service.getRequestFactory().getRequest(
				RequestType.QUERY, url, ContentType.TEXT_PLAIN);

		request.execute();
		BufferedReader line = new BufferedReader(new InputStreamReader(request
				.getResponseStream()));
		String tmpString = line.readLine();
		String response = null;
		while (tmpString != null) {
			response += tmpString + BasicConsts.NEW_LINE;
			tmpString = line.readLine();
		}

		return response;
	}

	@Override
	public void setUploadCompleted() throws ODKEntityPersistException {
		fsc.setUploadCompleted(true);
		datastore.putEntity(fsc, user);
	}

}
