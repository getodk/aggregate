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

import com.google.appengine.api.datastore.Key;
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
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.GoogleSpreadsheet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.persistence.EntityManager;


public class SubmissionSpreadsheetTable extends SubmissionCsvTable {

  private String applicationName;
  
  public SubmissionSpreadsheetTable(Form xform, String serverName,
      EntityManager entityManager, String appName) {
    super(xform, serverName, entityManager);
    applicationName = appName;
  }

  public void generateWorksheet(SpreadsheetService service,
      WorksheetEntry worksheet) throws IOException, ServiceException,
      BatchInterruptedException, MalformedURLException, ODKIncompleteSubmissionData {

    // get data
    ResultTable results = generateResultTable(TableConsts.EPOCH, false);

    // size worksheet correctly
    worksheet.setTitle(new PlainTextConstruct(super.getOdkId()));
    worksheet.setRowCount(2);
    worksheet.setColCount(results.getNumColumns());
    worksheet.update();

    List<String> headers = results.getHeader();

    CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
    query.setMinimumRow(1);
    query.setMaximumRow(1);
    query.setMinimumCol(1);
    query.setMaximumCol(results.getNumColumns());
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

    // TODO: migrate to batch uploads
    List<List<String>> resultRows = results.getRows();

    for (List<String> row : resultRows) {
      insertNewData(service, worksheet, headers, row);
    }
  }

  public void insertNewDataInSpreadsheet(Key submissionKey, List<GoogleSpreadsheet> spreadsheets) throws ODKIncompleteSubmissionData  {
    ResultTable result = generateSingleEntryResultTable(submissionKey);
    
    // TODO: make threaded (even better task API)
    
    for(GoogleSpreadsheet repo : spreadsheets) {
      if(!repo.getReady()) {
        continue;
      }
      SpreadsheetService service = new SpreadsheetService(applicationName);
      service.setAuthSubToken(repo.getAuthToken(), null);
      
      WorksheetEntry worksheet;
      try {
        worksheet = getWorksheet(service, repo.getSpreadsheetKey(), super.getOdkId());
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
  }

  private void insertNewData(SpreadsheetService service, WorksheetEntry worksheet, List<String> headers, List<String> row) throws IOException,
      ServiceException {
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

    SpreadsheetFeed feed =
        service.getFeed(new URL(ServletConsts.SPREADSHEET_FEED), SpreadsheetFeed.class);
    List<SpreadsheetEntry> spreadsheets = feed.getEntries();

    SpreadsheetEntry sheet = null;
    for (SpreadsheetEntry entry : spreadsheets) {
      if (spreadsheetKey.equals(entry.getKey())) {
        sheet = entry;
      }
    }

    // verify spreadsheet was found
    if (sheet == null) {
      return null;
    }

    // get worksheet
    WorksheetEntry worksheet = null;
    List<WorksheetEntry> worksheets = sheet.getWorksheets();
    for (WorksheetEntry wksheet : worksheets) {
      if (wksheet.getTitle().getPlainText().equals(worksheetTitle)) {
        worksheet = wksheet;
      }
    }
    return worksheet;
  }  
}
