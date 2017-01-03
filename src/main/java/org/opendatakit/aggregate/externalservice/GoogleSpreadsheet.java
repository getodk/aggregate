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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.LinkElementFormatter;
import org.opendatakit.aggregate.format.header.GoogleSpreadsheetHeaderFormatter;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Get;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AddSheetResponse;
import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Response;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class GoogleSpreadsheet extends GoogleOauth2ExternalService implements ExternalService {
  private static final Log logger = LogFactory.getLog(GoogleSpreadsheet.class.getName());

  private static final String GOOGLE_DRIVE_FILES_API = "https://www.googleapis.com/drive/v2/files";

  private static final String GOOGLE_SPREADSHEET_OAUTH2_SCOPE = "https://www.googleapis.com/auth/drive " + SheetsScopes.SPREADSHEETS;

  private static ObjectMapper mapper = new ObjectMapper();

  /**
   * Datastore entity specific to this type of external service
   */
  private final GoogleSpreadsheet2ParameterTable objectEntity;

  /**
   * Datastore entity specific to this type of external service for the repeats
   */
  private final List<GoogleSpreadsheet2RepeatParameterTable> repeatElementEntities = new ArrayList<GoogleSpreadsheet2RepeatParameterTable>();

  private final Sheets spreadsheetService;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY =
      JacksonFactory.getDefaultInstance();

  /**
   * Common base constructor that initializes final values.
   * 
   * @param form
   * @param fpObject
   * @param cc
   * @throws ODKExternalServiceException
   * @throws IOException 
   * @throws GeneralSecurityException 
   */

  private GoogleSpreadsheet(IForm form, GoogleSpreadsheet2ParameterTable gsObject,
      FormServiceCursor formServiceCursor, CallingContext cc) throws ODKExternalServiceException {
    super(GOOGLE_SPREADSHEET_OAUTH2_SCOPE, form, formServiceCursor, new LinkElementFormatter(
        cc.getServerURL(), FormMultipleValueServlet.ADDR, true, true, true, true),
        new GoogleSpreadsheetHeaderFormatter(true, true, true), logger, cc);
    
    try {
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      spreadsheetService = new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
          .setApplicationName(ServletConsts.APPLICATION_NAME)
          .build();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      throw new ODKExternalServiceCredentialsException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }

    objectEntity = gsObject;
  }

  private GoogleSpreadsheet(IForm form, GoogleSpreadsheet2ParameterTable entity,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException, ODKOverQuotaException, ODKExternalServiceException {
    this(form, entity, createFormServiceCursor(form, entity, externalServiceOption,
        ExternalServiceType.GOOGLE_SPREADSHEET, cc), cc);
    objectEntity.setOwnerEmail(ownerEmail);
  }

  public GoogleSpreadsheet(FormServiceCursor fsc, IForm form, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, ODKOverQuotaException,
      ODKExternalServiceException, ODKFormNotFoundException {
    this(form, retrieveEntity(GoogleSpreadsheet2ParameterTable.assertRelation(cc), fsc, cc), fsc,
        cc);

    repeatElementEntities.addAll(GoogleSpreadsheet2RepeatParameterTable.getRepeatGroupAssociations(
        objectEntity.getUri(), cc));

  }

  public GoogleSpreadsheet(IForm form, String name,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException, ODKOverQuotaException, ODKExternalServiceException,
      ODKEntityPersistException {
    this(form, newEntity(GoogleSpreadsheet2ParameterTable.assertRelation(cc), cc),
        externalServiceOption, ownerEmail, cc);

    objectEntity.setSpreadsheetName(name);
    persist(cc);
  }
  
  protected String executeDriveStmt(String spreadsheetTitle, String spreadsheetDescription,
      CallingContext cc) throws
      IOException, ODKExternalServiceException, GeneralSecurityException {

    HashMap<String,String> requestBody = new HashMap<String,String>();
    requestBody.put("title", spreadsheetTitle);
    requestBody.put("description", spreadsheetDescription);
    requestBody.put("mimeType", "application/vnd.google-apps.spreadsheet");
    
    GenericUrl url = new GenericUrl(GOOGLE_DRIVE_FILES_API);

    String statement = mapper.writeValueAsString(requestBody);
    HttpContent entity = null;
    entity = new ByteArrayContent("application/json",
        statement.getBytes(HtmlConsts.UTF8_ENCODE));

    HttpRequest request = requestFactory.buildRequest("POST", url, entity);
    HttpResponse resp = request.execute();
    String response = WebUtils.readGoogleResponse(resp);

    int statusCode = resp.getStatusCode();
    if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
      throw new ODKExternalServiceCredentialsException(response.toString() + statement);
    } else if (statusCode != HttpServletResponse.SC_OK) {
      throw new ODKExternalServiceException(response.toString() + statement);
    }

    return response;
  }

  public void initiate(CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {

    boolean newlyCreated = false;
    if (objectEntity.getSpreadsheetKey() == null) {
      newlyCreated = true;

      // create spreadsheet
      String spreadsheetName = getSpreadsheetName();
      String spreadsheetDescription = spreadsheetName + " ODK Aggregate " + WebUtils.iso8601Date(new Date());
      // will hold doc id
      String spreadKey = null;
      try {
        String response = executeDriveStmt(spreadsheetName, spreadsheetDescription, cc);
        
        // convert response from json to Java
        TypeReference<HashMap<Object,Object>> ref = new TypeReference<HashMap<Object,Object>>() {};
        HashMap<Object,Object> map = mapper.readValue(response, ref);

        // get document ID (spreadsheet 'key')
        spreadKey = (String) map.get("id");
        
      } catch (IOException e) {
        e.printStackTrace();
        throw new ODKExternalServiceException(e);
      } catch (GeneralSecurityException e) {
        e.printStackTrace();
        throw new ODKExternalServiceCredentialsException(e);
      }   

      objectEntity.setSpreadsheetKey(spreadKey);
      objectEntity.setReady(false);
    }
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);

    if (newlyCreated || !getReady()) {
      try {
        // create worksheet
        WorksheetCreator ws = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(ExternalServiceConsts.EXT_SERV_ADDRESS, getSpreadsheetName());
        parameters.put(ServletConsts.EXTERNAL_SERVICE_TYPE, fsc
            .getExternalServicePublicationOption().name());

        MiscTasks m = new MiscTasks(TaskType.WORKSHEET_CREATE, form, parameters, cc);
        m.persist(cc);

        CallingContext ccDaemon = ContextFactory.duplicateContext(cc);
        ccDaemon.setAsDaemon(true);
        ws.createWorksheetTask(form, m, 1L, ccDaemon);
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      // upload data to external service
      postUploadTask(cc);
    }
  }

  @Override
  protected String getOwnership() {
    return objectEntity.getOwnerEmail().substring(EmailParser.K_MAILTO.length());
  }

  private void sharePublishedFiles(String ownerEmail, CallingContext cc)
      throws ODKExternalServiceException {
    executeDrivePermission(objectEntity.getSpreadsheetKey(), ownerEmail);
  }

  public Boolean getReady() {
    return objectEntity.getReady();
  }

  public void updateReadyValue() {
    boolean ready = (objectEntity.getSpreadsheetName() != null)
        && (objectEntity.getSpreadsheetKey() != null);
    objectEntity.setReady(ready);
  }

  public String getSpreadsheetName() {
    return objectEntity.getSpreadsheetName();
  }

  public void generateWorksheets(CallingContext cc) throws ODKDatastoreException, IOException,
      ODKExternalServiceException {

    // TODO: throw meaningful credentials-failure exception
    // TODO: throw meaningful credentials-failure exception
    
    
    List<Integer> sheetIds = new ArrayList<Integer>();

    // delete pre-existing worksheets and create the new sheets.
    // we first create the new sheets so that we are able to delete all of the old ones
    // (the spreadsheet is initially created with one worksheet which cannot be deleted).
    
    {    
      List<Request> requests = new ArrayList<Request>();
      Spreadsheet entry = spreadsheetService.spreadsheets().get(objectEntity.getSpreadsheetKey()).execute();
      List<Sheet> preExistingWorksheets = entry.getSheets();

      // create the sheets -- these are empty at first.
      {
        // create top level worksheet
        List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(),
            null);
        // set the sheet properties
        {
          SheetProperties sheetProperties = new SheetProperties();
          
          sheetProperties.setTitle(form.getFormId());
          GridProperties gridProperties = new GridProperties();
          gridProperties.setRowCount(2);
          gridProperties.setColumnCount(headers.size());
          sheetProperties.setGridProperties(gridProperties);
          
          AddSheetRequest asr = new AddSheetRequest();
          asr.setProperties(sheetProperties);
          requests.add(new Request().setAddSheet(asr));
        }
    
        // create worksheets for the repeat groups
        for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
          headers = headerFormatter.generateHeaders(form, repeatGroupElement, null);
          
          // set the sheet properties
          {
            SheetProperties sheetProperties = new SheetProperties();
            
            sheetProperties.setTitle(repeatGroupElement.getElementName());
            GridProperties gridProperties = new GridProperties();
            gridProperties.setRowCount(2);
            gridProperties.setColumnCount(headers.size());
            sheetProperties.setGridProperties(gridProperties);
            
            AddSheetRequest asr = new AddSheetRequest();
            asr.setProperties(sheetProperties);
            requests.add(new Request().setAddSheet(asr));
          }
        }
      }

      // now delete the old sheets
      for (Sheet worksheet : preExistingWorksheets) {
        DeleteSheetRequest dsr = new DeleteSheetRequest();
        dsr.setSheetId(worksheet.getProperties().getSheetId());
        Request req = new Request().setDeleteSheet(dsr);
        requests.add(req);
      }

      if ( !requests.isEmpty() ) {
        BatchUpdateSpreadsheetRequest req = new BatchUpdateSpreadsheetRequest();
        req.setRequests(requests);
        req.setIncludeSpreadsheetInResponse(false);
        BatchUpdateSpreadsheetResponse rsp = 
            spreadsheetService.spreadsheets().batchUpdate(objectEntity.getSpreadsheetKey(), req).execute();
        List<Response> responses = rsp.getReplies();
        
        // And now stitch everything back together
        if ( responses.size() != 1 + form.getRepeatGroupsInModel().size() + preExistingWorksheets.size() ) {
          throw new IllegalStateException("Mismatch in number of responses for number of requests in batch");
        }
        AddSheetResponse asrsp = responses.get(0).getAddSheet();
    
        Integer sheetId = asrsp.getProperties().getSheetId();
        sheetIds.add(sheetId);
        objectEntity.setTopLevelWorksheetId(sheetId.toString());
    
        // get relation prototype for creating repeat parameter table entries
        GoogleSpreadsheet2RepeatParameterTable repeatPrototype = GoogleSpreadsheet2RepeatParameterTable
            .assertRelation(cc);
    
        // create repeat worksheets
        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();
        int repeatGroupCount = -1;
        for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
          ++repeatGroupCount;
          // create the worksheet
          asrsp = responses.get(1+repeatGroupCount).getAddSheet();
          // add the worksheet id to the repeat element table -- NOTE: the added
          // entry is not actually persisted here
          GoogleSpreadsheet2RepeatParameterTable t = ds
              .createEntityUsingRelation(repeatPrototype, user);
          t.setUriGoogleSpreadsheet(objectEntity.getUri());
          t.setFormElementKey(repeatGroupElement.constructFormElementKey(form));
          sheetId = asrsp.getProperties().getSheetId();
          sheetIds.add(sheetId);
          t.setWorksheetId(sheetId.toString());
          repeatElementEntities.add(t);
        }
      }
    }
    
    List<Request> requests = new ArrayList<Request>();
    // Write the header cells in these sheets. 
    
    // create top level worksheet
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(),
        null);
    UpdateCellsRequest topLevelWorksheet = writeColumnHeadingsCells( 
        form.getFormId(), headers, sheetIds.get(0));
    
    requests.add(new Request().setUpdateCells(topLevelWorksheet));

    // create repeat worksheets
    int repeatGroupCount = -1;
    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      ++repeatGroupCount;
      // create the worksheet
      headers = headerFormatter.generateHeaders(form, repeatGroupElement, null);
      UpdateCellsRequest repeatWorksheet = writeColumnHeadingsCells(
          repeatGroupElement.getElementName(), headers, sheetIds.get(1+repeatGroupCount));

      requests.add(new Request().setUpdateCells(repeatWorksheet));
    }
    
    if ( !requests.isEmpty() ) {
      BatchUpdateSpreadsheetRequest req = new BatchUpdateSpreadsheetRequest();
      req.setRequests(requests);
      req.setIncludeSpreadsheetInResponse(false);
      BatchUpdateSpreadsheetResponse rsp = 
          spreadsheetService.spreadsheets().batchUpdate(objectEntity.getSpreadsheetKey(), req).execute();
    }
    
    persist(cc);

    // transfer ownership before marking service as prepared...
    sharePublishedFiles(objectEntity.getOwnerEmail(), cc);

    // persist the changes we have made to the repeat element table (changes
    // from calling executeCreateWorksheet)
    fsc.setIsExternalServicePrepared(true); // we have completed worksheet
    updateReadyValue();
    // creation...
    persist(cc);
  }

  private UpdateCellsRequest writeColumnHeadingsCells(String title, List<String> headers, Integer sheetId)
      throws IOException {

    // build the update request.
    UpdateCellsRequest req = new UpdateCellsRequest();
    GridRange gridRange = new GridRange();
    
    gridRange.setSheetId(sheetId);
    gridRange.setStartColumnIndex(0);
    gridRange.setEndColumnIndex(headers.size());
    gridRange.setStartRowIndex(0);
    gridRange.setEndRowIndex(1);
    
    req.setRange(gridRange);
    
    req.setFields("*");
    
    List<CellData> cells = new ArrayList<CellData>();
    int index = 0;
    for ( index = 0 ; index < headers.size(); ++index) {
      String header = headers.get(index);
      CellData cellData = new CellData();
      ExtendedValue ev = new ExtendedValue();
      ev.setStringValue(header);
      cellData.setUserEnteredValue(ev);
      cells.add(cellData);
    }
    
    RowData rowData = new RowData();
    rowData.setValues(cells);
    List<RowData> rows = new ArrayList<RowData>();
    rows.add(rowData);
    
    req.setRows(rows);

    return req;
  }

  private static class SheetInfo {
    Integer sheetId;
    List<String> headers;
    List<ElementType> headerTypes;
    Map<Integer,Integer> fieldMap = new HashMap<Integer,Integer>();
  }
  
  private Map<String, SheetInfo> sheetInfoMap = null;
  
  private void buildSheetInfoMap(CallingContext cc) throws IOException {
    if ( sheetInfoMap != null ) return;

    List<Sheet> sheets = spreadsheetService.spreadsheets().get(objectEntity.getSpreadsheetKey()).execute().getSheets();
    Map<Integer, Sheet> sheetMap = new HashMap<Integer, Sheet>();
    for ( Sheet sheet : sheets ) {
      sheetMap.put(sheet.getProperties().getSheetId(), sheet);
    }
    Sheet existingSheet;
    GridRange range;
    
    Map<String, SheetInfo> workingSheetInfoMap = new HashMap<String, SheetInfo>();

    Get req = spreadsheetService.spreadsheets().get(objectEntity.getSpreadsheetKey());
    SheetInfo sheetInfo;
    List<String> ranges = new ArrayList<String>();

    // build sheetInfo map
    sheetInfo = new SheetInfo();
    sheetInfo.sheetId = Integer.valueOf(objectEntity.getTopLevelWorksheetId());
    sheetInfo.headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(), null);
    sheetInfo.headerTypes = headerFormatter.getHeaderTypes();
    
    workingSheetInfoMap.put(objectEntity.getTopLevelWorksheetId(), sheetInfo);
    
    // build gridRange request for top-level sheet headers
    existingSheet = sheetMap.get(sheetInfo.sheetId);
    ranges.add(existingSheet.getProperties().getTitle() + "!R1C1:R1C" 
    + Integer.valueOf(existingSheet.getProperties().getGridProperties().getColumnCount()));
    
    // build gridRange request for repeat group sheet headers
    for (GoogleSpreadsheet2RepeatParameterTable tableId : repeatElementEntities) {

      FormElementKey elementKey = tableId.getFormElementKey();
      FormElementModel element = FormElementModel.retrieveFormElementModel(form, elementKey);

      // build sheetInfo map
      sheetInfo = new SheetInfo();
      sheetInfo.sheetId = Integer.valueOf(tableId.getWorksheetId());
      sheetInfo.headers =  headerFormatter.generateHeaders(form, element, null);
      sheetInfo.headerTypes = headerFormatter.getHeaderTypes();
      
      workingSheetInfoMap.put(tableId.getWorksheetId(), sheetInfo);
      
      existingSheet = sheetMap.get(sheetInfo.sheetId);
      ranges.add(existingSheet.getProperties().getTitle() + "!R1C1:R1C" 
          + Integer.valueOf(existingSheet.getProperties().getGridProperties().getColumnCount()));
    }
    req.setRanges(ranges);
    req.setIncludeGridData(true);
    Spreadsheet entry = req.execute();

    for ( Sheet sheet : entry.getSheets() ) {
      Integer id = sheet.getProperties().getSheetId();
      sheetInfo = workingSheetInfoMap.get(Integer.toString(id));
      
      for ( GridData data : sheet.getData() ) {
        // there may be extra entries to ignore
        if ( data == null ) {
          continue;
        }
        Integer startCol = 0;
        // there may be extra rows to ignore
        List<CellData> cells = data.getRowData().get(0).getValues();
        
        for ( CellData cell : cells ) {
          if ( cell == null ) {
            startCol++;
            continue;
          }
          String header = cell.getFormattedValue();
          if ( header == null ) {
            startCol++;
            continue;
          }
          for ( int i = 0 ; i < sheetInfo.headers.size() ; ++i ) {
            String hcol = sheetInfo.headers.get(i);
            if ( hcol.equals(header) ) {
              sheetInfo.fieldMap.put(i, startCol);
              break;
            }
          }
          startCol++;
        }
      }
    }
    sheetInfoMap = workingSheetInfoMap;
  }
  
  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    if (getReady()) {
      try {
        buildSheetInfoMap(cc);
        
        SheetInfo sheetInfo;
        List<Request> requests = new ArrayList<Request>();
        
        // upload base submission values
        sheetInfo = sheetInfoMap.get(objectEntity.getTopLevelWorksheetId());
        AppendCellsRequest acr = createAppendCellsRequest(submission, sheetInfo, cc);
        requests.add(new Request().setAppendCells(acr));

        // upload repeat values
        for (GoogleSpreadsheet2RepeatParameterTable tableId : repeatElementEntities) {
          FormElementKey elementKey = tableId.getFormElementKey();
          FormElementModel element = FormElementModel.retrieveFormElementModel(form, elementKey);
          sheetInfo = sheetInfoMap.get(tableId.getWorksheetId());
          
          List<SubmissionValue> values = submission.findElementValue(element);
          for (SubmissionValue value : values) {
            if (value instanceof RepeatSubmissionType) {
              RepeatSubmissionType repeat = (RepeatSubmissionType) value;
              if (repeat.getElement().equals(element)) {
                for (SubmissionSet set : repeat.getSubmissionSets()) {
                  acr = createAppendCellsRequest(set, sheetInfo, cc);
                  requests.add(new Request().setAppendCells(acr));
                }
              }
            } else {
              System.out
                  .println("ERROR: How did a non Repeat Submission Type get in the for loop?");
            }
          }
        }
        
        if ( !requests.isEmpty() ) {
          BatchUpdateSpreadsheetRequest req = new BatchUpdateSpreadsheetRequest();
          req.setRequests(requests);
          req.setIncludeSpreadsheetInResponse(false);
          BatchUpdateSpreadsheetResponse rsp = 
              spreadsheetService.spreadsheets().batchUpdate(objectEntity.getSpreadsheetKey(), req).execute();
        }

      } catch (Exception e) {
        e.printStackTrace();
        logger.error("Unable to insert data into spreadsheet " + objectEntity.getSpreadsheetName()
            + " exception: " + e.getMessage());
        throw new ODKExternalServiceException(e);
      }
    }
  }

  /**
   * Creates the request to append the data in the given submissionSet as a new entry (i.e. a new row)
   * in the given worksheet, including only the data specified by headers.
   * 
   * @param submissionSet
   *          the set of data from a single submission
   * @param sheetInfo
   *          encapsulates information about the Sheet (worksheet) and the 
   *          list of headers we are publishing into, and the mapping between the two.
   * @param cc
   *          the calling context
   * @throws ODKDatastoreException
   *           if there was a problem in the datastore
   * @throws IOException
   *           if there was a problem communicating over the internet with the
   *           Google Spreadsheet
   * @throws ServiceException
   *           if there was a problem with the GData service
   */
  private AppendCellsRequest createAppendCellsRequest(SubmissionSet submissionSet, SheetInfo sheetInfo,
      CallingContext cc) throws ODKDatastoreException, IOException {

    Row row = submissionSet.getFormattedValuesAsRow(null, formatter, true, cc);
    List<String> formattedValues = row.getFormattedValues();

    String rowString = null;
    Map<Integer, CellData> cellReorderMap = new HashMap<Integer, CellData>();

    // expect columns to be zero-based (0..n)
    // should typically correspond to the column order we define,
    // but allow the user to re-order the columns (during quiescent times)
    int minNewCol = 0;
    int maxNewCol = 0;
    for (int colIndex = 0; colIndex < sheetInfo.headers.size(); colIndex++) {
      Integer newCol = sheetInfo.fieldMap.get(colIndex);;
      minNewCol = (minNewCol < newCol) ? minNewCol : newCol;
      maxNewCol = (maxNewCol > newCol) ? maxNewCol : newCol;
      ElementType type = sheetInfo.headerTypes.get(colIndex);
      rowString = formattedValues.get(colIndex);
      CellData cellData = new CellData();
      if ( rowString == null ) {
        cellData.setUserEnteredValue(new ExtendedValue());
      } else {
        ExtendedValue ev;
        switch ( type ) {
          case BOOLEAN:
            ev = new ExtendedValue();
            ev.setBoolValue(Boolean.valueOf(rowString));
            cellData.setUserEnteredValue(ev);
            break;
          case INTEGER:
          case DECIMAL:
            ev = new ExtendedValue();
            ev.setNumberValue(Double.valueOf(rowString));
            cellData.setUserEnteredValue(ev);
            break;
          case JRDATE:
          case JRDATETIME:
          case JRTIME:
          default:
            ev = new ExtendedValue();
            ev.setStringValue(rowString);
            cellData.setUserEnteredValue(ev);
        }
      }
      cellReorderMap.put(newCol, cellData);
    }
    
    if ( minNewCol < 0 ) {
      throw new IllegalStateException("Expected columns in row to start at index 0");
    }
    
    AppendCellsRequest acr = new AppendCellsRequest();
    acr.setFields("*");
    acr.setSheetId(sheetInfo.sheetId);
    RowData rowData = new RowData();
    List<CellData> cells = new ArrayList<CellData>();
    for ( int col = 0 ; col <= maxNewCol; ++col ) {
      CellData cell = cellReorderMap.get(col);
      if ( cell == null ) {
        cell = new CellData();
      }
      cells.add(cell);
    }
    rowData.setValues(cells);
    List<RowData> rows = new ArrayList<RowData>();
    rows.add(rowData);
    acr.setRows(rows);
    return acr;
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
        : (other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (other.fsc != null && fsc.equals(other.fsc)));
  }

  @Override
  public String getDescriptiveTargetString() {
    Map<String, String> properties = new HashMap<String, String>();
    String id = objectEntity.getSpreadsheetKey();
    if (id == null || objectEntity.getReady() != true) {
      return "Not yet created";
    }
    properties.put("key", id);
    return HtmlUtil.createHrefWithProperties("https://docs.google.com/spreadsheet/ccc", properties,
        getSpreadsheetName(), true);
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return repeatElementEntities;
  }
}
