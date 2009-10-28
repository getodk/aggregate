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

package org.odk.aggregate.table;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.persistence.EntityManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.GoogleSpreadsheet;
import org.odk.aggregate.report.FormProperties;
import org.odk.aggregate.submission.Submission;

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


public class SubmissionSpreadsheetTable extends SubmissionCsvTable {

  private String applicationName;

  public SubmissionSpreadsheetTable(Form xform, String serverName, EntityManager entityManager,
      String appName) {
    super(xform, serverName, entityManager);
    applicationName = appName;
  }

  public void generateWorksheet(SpreadsheetService service, WorksheetEntry worksheet)
      throws IOException, ServiceException, BatchInterruptedException, MalformedURLException,
      ODKIncompleteSubmissionData {

    // get data
    // TODO replaced the old with form properties need to make sure it will be
    // old - ResultTable results = generateResultTable(TableConsts.EPOCH, false);
    FormProperties formProp = new FormProperties(form, em);
    List<String> headers = formProp.getHeaders();
    
    // size worksheet correctly
    worksheet.setTitle(new PlainTextConstruct(super.getOdkId()));
    worksheet.setRowCount(2);
    worksheet.setColCount(headers.size());
    worksheet.update();



    CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
    query.setMinimumRow(1);
    query.setMaximumRow(1);
    query.setMinimumCol(1);
    query.setMaximumCol(headers.size());
    query.setReturnEmpty(true);

    CellFeed cellFeed = service.query(query, CellFeed.class);
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
    service.batch(new URL(batchLink.getHref()), batchFeed);
  }

  public void uploadSubmissionDataToSpreadsheet(SpreadsheetService service, WorksheetEntry worksheet)
      throws ODKIncompleteSubmissionData, IOException, ServiceException {
    ResultTable results = generateResultTable(TableConsts.EPOCH, false);
    // TODO: migrate to batch uploads
    List<List<String>> resultRows = results.getRows();

    for (List<String> row : resultRows) {
      insertNewData(service, worksheet, headers, row);
    }
  }

  public void insertNewDataInSpreadsheet(Submission submission, GoogleSpreadsheet spreadsheet) {
	if (!spreadsheet.getReady()) {
		return;
	}
	  
	ResultTable result = generateSingleEntryResultTable(submission);

    SpreadsheetService service = new SpreadsheetService(applicationName);
    service.setAuthSubToken(spreadsheet.getAuthToken(), null);

    WorksheetEntry worksheet;
    try {
      worksheet = getWorksheet(service, spreadsheet.getSpreadsheetKey(), super.getOdkId());
      insertNewData(service, worksheet, result.getHeader(), result.getRows().get(0));
    } catch (MalformedURLException e) {
      // TODO: determine better error handling
      e.printStackTrace();
    } catch (IOException e) {
      // TODO: determine better error handling
      e.printStackTrace();
    } catch (ServiceException e) {
      // TODO: determine better error handling
      e.printStackTrace();
    }

  }

  private void insertNewData(SpreadsheetService service, WorksheetEntry worksheet,
      List<String> headers, List<String> row) throws IOException, ServiceException {
    ListEntry newEntry = new ListEntry();
    CustomElementCollection values = newEntry.getCustomElements();
    for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
      String headerString = headers.get(colIndex);
      String rowString = row.get(colIndex);
      values.setValueLocal(headerString, (rowString == null) ? BasicConsts.SPACE : rowString);
    }
    service.insert(worksheet.getListFeedUrl(), newEntry);
  }


  public WorksheetEntry getWorksheet(SpreadsheetService service, String spreadsheetKey,
      String worksheetTitle) throws MalformedURLException, IOException, ServiceException {

    // get worksheet
    WorksheetEntry worksheet = null;
    URL worksheetFeedUrl =
        new URL(ServletConsts.WORKSHEETS_FEED_PREFIX + spreadsheetKey
            + ServletConsts.FEED_PERMISSIONS);
    WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl, WorksheetFeed.class);
    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
    for (WorksheetEntry wksheet : worksheets) {
      if (wksheet.getTitle().getPlainText().equals(worksheetTitle)) {
        worksheet = wksheet;
      }
    }
    return worksheet;
  }
}
