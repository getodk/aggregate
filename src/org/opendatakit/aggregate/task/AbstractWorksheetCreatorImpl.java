/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.task;

import java.util.List;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

public abstract class AbstractWorksheetCreatorImpl implements WorksheetCreator {

	private final GoogleSpreadsheet getGoogleSpreadsheetWithName(Form form,
			String spreadsheetName, Datastore datastore, User user) throws ODKDatastoreException {
		List<ExternalService> remoteServers = FormServiceCursor.getExternalServicesForForm(form.getKey(), 
				form.getFormDefinition(), datastore, user);
		
		if (remoteServers == null) {
			return null;
		}

		// find spreadsheet with name
		for (ExternalService rs : remoteServers) {
			if (rs instanceof GoogleSpreadsheet) {
				GoogleSpreadsheet sheet = (GoogleSpreadsheet) rs;

				if (sheet.getSpreadsheetName().equals(spreadsheetName)) {
					return sheet;
				}
			}
		}

		return null;
	}

	public final void worksheetCreator(String appName, String serverURL,
			String spreadsheetName, ExternalServiceOption esType, Form form,
			Datastore ds, User user) throws ODKExternalServiceException, ODKDatastoreException {

		GoogleSpreadsheet spreadsheet = getGoogleSpreadsheetWithName(form, spreadsheetName, ds, user);

		// verify form has a spreadsheet element
		if (spreadsheet == null) {
			throw new ODKExternalServiceException("unable to find spreadsheet");
		}

		try {

			// TODO: make more robust (currently assuming nothing has touched
			// the sheet)
			// get worksheet
			WorksheetEntry worksheet = spreadsheet.getWorksheet("Sheet 1");

			// verify worksheet was found
			if (worksheet == null) {
				throw new ODKExternalServiceException(
						"COULD NOT FIND WORKSHEET");
			}

			spreadsheet.generateWorksheet(worksheet);

			if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
				QueryByDate query = new QueryByDate(form.getFormDefinition(),
						BasicConsts.EPOCH, false, ServletConsts.FETCH_LIMIT,
						ds, user);
				List<Submission> submissions = query.getResultSubmissions();
				spreadsheet.sendSubmissions(submissions);
			}

		} catch (Exception e1) {
			throw new ODKExternalServiceException(e1);
		}
		try {
			if (!esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
				spreadsheet.updateReadyValue();
			} else {
				// TODO: should set upload completed flag in the case
				// where the spreadsheet is upload and streaming
				// and the upload has completed.
				spreadsheet.setUploadCompleted();
				spreadsheet.persist();
				// remove spreadsheet permission as no longer needed
				AuthSubUtil.revokeToken(spreadsheet.getAuthToken(), null);
			}
		} catch (Exception e) {
			throw new ODKExternalServiceException(e);

		}
	}

}
