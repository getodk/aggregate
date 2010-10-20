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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
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

import com.google.gdata.client.batch.BatchInterruptedException;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheet extends AbstractExternalService implements
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
	private GoogleSpreadsheetParameterTable objectEntity;

	private Datastore datastore;

	private User user;

	/**
	 * NOT PERSISTED
	 */
	private SpreadsheetService spreadsheetService;

	/**
	 * NOT PERSISTED - created for each object
	 */
	private HeaderFormatter headerFormatter;

	private List<String> headers;

	private GoogleSpreadsheet(FormDefinition formDefinition,
			FormServiceCursor fsc, Datastore datastore, User user)
			throws ODKEntityNotFoundException, ODKDatastoreException {
		super(formDefinition, new BasicElementFormatter(true, true, true));
		this.datastore = datastore;
		this.user = user;
		this.fsc = fsc;

		objectEntity = datastore.getEntity(GoogleSpreadsheetParameterTable
				.createRelation(datastore, user), fsc.getSubAuri(), user);

		spreadsheetService = new SpreadsheetService(
				ServletConsts.APPLICATION_NAME);
		spreadsheetService.setAuthSubToken(getAuthToken(), null);
		// TODO: REMOVE after bug is fixed
		// http://code.google.com/p/gdata-java-client/issues/detail?id=103
		spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V1);

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);
	}

	public GoogleSpreadsheet(FormDefinition formDefinition, String name,
			String spreadKey, String authToken, 
			ExternalServiceOption externalServiceOption,
			Datastore datastore, User user) throws ODKDatastoreException {
		super(formDefinition, new BasicElementFormatter(true, true, true));
		this.datastore = datastore;
		this.user = user;
		fsc = datastore.createEntityUsingRelation(FormServiceCursor
				.createRelation(datastore, user), null, user);
		objectEntity = datastore.createEntityUsingRelation(
				GoogleSpreadsheetParameterTable.createRelation(datastore, user),
				new EntityKey(fsc, fsc.getUri()), user);
		fsc.setSubAuri(objectEntity.getUri());
		fsc.setServiceClassname(GoogleSpreadsheet.class.getCanonicalName());
		fsc.setExternalServiceOption(externalServiceOption);
		fsc.setEstablishmentDateTime(new Date());
		fsc.setUploadCompleted(false);
		objectEntity.setAuthToken(authToken);
		objectEntity.setSpreadsheetName(name);
		objectEntity.setSpreadsheetKey(spreadKey);
		updateReadyValue();

		spreadsheetService = new SpreadsheetService(
				ServletConsts.APPLICATION_NAME);
		spreadsheetService.setAuthSubToken(getAuthToken(), null);
		// TODO: REMOVE after bug is fixed
		// http://code.google.com/p/gdata-java-client/issues/detail?id=103
		spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V1);

		headerFormatter = new BasicHeaderFormatter(true, true, true);
		headers = headerFormatter.generateHeaders(formDefinition,
				formDefinition.getTopLevelGroup(), null);

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

	public Boolean getReady() {
		return objectEntity.getReady();
	}

	public void updateReadyValue() {
		boolean ready = (getSpreadsheetName() != null)
				&& (getSpreadsheetKey() != null) && (getAuthToken() != null);
		objectEntity.setReady(ready);
	}

	public String getSpreadsheetName() {
		return objectEntity.getSpreadsheetName();
	}

	public String getSpreadsheetKey() {
		return objectEntity.getSpreadsheetKey();
	}

	public String getAuthToken() {
		return objectEntity.getAuthToken();
	}

	public void setAuthToken(String authToken) {
		objectEntity.setAuthToken(authToken);
	}

	public void generateWorksheet(WorksheetEntry worksheet) throws IOException,
			ServiceException, BatchInterruptedException, MalformedURLException,
			ODKIncompleteSubmissionData {

		// size worksheet correctly
		worksheet.setTitle(new PlainTextConstruct(formDefinition.getFormId()));
		worksheet.setRowCount(2);
		worksheet.setColCount(headers.size());
		worksheet.update();

		CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
		query.setMinimumRow(1);
		query.setMaximumRow(1);
		query.setMinimumCol(1);
		query.setMaximumCol(headers.size());
		query.setReturnEmpty(true);

		CellFeed cellFeed = spreadsheetService.query(query, CellFeed.class);
		CellFeed batchFeed = new CellFeed();

		List<CellEntry> cells = cellFeed.getEntries();
		int index = 0;
		for (CellEntry cell : cells) {
			cell.changeInputValueLocal(headers.get(index));
			BatchUtils.setBatchId(cell, Integer.toString(index));
			BatchUtils.setBatchOperationType(cell, BatchOperationType.UPDATE);
			batchFeed.getEntries().add(cell);
			index++;
		}

		// Submit the batch request.
		Link batchLink = cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		spreadsheetService.batch(new URL(batchLink.getHref()), batchFeed);
	}

	@Override
	public void insertData(Submission submission)
			throws ODKExternalServiceException {

		// TODO: Waylon -- not sure what this is doing w.r.t. FusionTable
		try {
			WorksheetEntry worksheet = getWorksheet(formDefinition.getFormId());
			ListEntry newEntry = new ListEntry();
			CustomElementCollection values = newEntry.getCustomElements();

			Row row = submission.getFormattedValuesAsRow(null, formatter);
			List<String> formattedValues = row.getFormattedValues();

			for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
				String headerString = headers.get(colIndex);
				String rowString = formattedValues.get(colIndex);
				values.setValueLocal(headerString,
						(rowString == null) ? BasicConsts.SPACE : rowString);
			}

			spreadsheetService.insert(worksheet.getListFeedUrl(), newEntry);
		} catch (Exception e) {
			throw new ODKExternalServiceException(e.getCause());
		}
	}

	public WorksheetEntry getWorksheet(String worksheetTitle)
			throws MalformedURLException, IOException, ServiceException {

		// get worksheet
		WorksheetEntry worksheet = null;
		URL worksheetFeedUrl = new URL(ServletConsts.WORKSHEETS_FEED_PREFIX
				+ getSpreadsheetKey() + ServletConsts.FEED_PERMISSIONS);
		WorksheetFeed worksheetFeed = spreadsheetService.getFeed(
				worksheetFeedUrl, WorksheetFeed.class);
		List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
		for (WorksheetEntry wksheet : worksheets) {
			if (wksheet.getTitle().getPlainText().equals(worksheetTitle)) {
				worksheet = wksheet;
			}
		}
		return worksheet;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GoogleSpreadsheet)) {
			return false;
		}
		GoogleSpreadsheet other = (GoogleSpreadsheet) obj;
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
