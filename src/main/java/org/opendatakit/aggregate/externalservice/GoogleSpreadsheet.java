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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.googleapi.GoogleCredential;
import org.opendatakit.aggregate.externalservice.googleapi.SafeApacheHttpTransport;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.LinkElementFormatter;
import org.opendatakit.aggregate.format.header.GoogleSpreadsheetHeaderFormatter;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.CredentialStoreRefreshListener;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class GoogleSpreadsheet extends OAuth2ExternalService implements ExternalService, CredentialStore {
  private static final String UTF_8_ENCODING = "UTF-8";

  private static final Log logger = LogFactory.getLog(GoogleSpreadsheet.class.getName());

  private static final String GOOGLE_SPREADSHEET_OAUTH2_SCOPE =
      "https://www.googleapis.com/auth/drive https://docs.google.com/feeds/ https://docs.googleusercontent.com/ https://spreadsheets.google.com/feeds/";

  private static final JsonFactory jsonFactory = new JacksonFactory();

  private static SafeApacheHttpTransport safeApacheHttpTransport = null;
  /**
   * Datastore entity specific to this type of external service
   */
  private final GoogleSpreadsheet2ParameterTable objectEntity;

  /**
   * Datastore entity specific to this type of external service for the repeats
   */
  private final List<GoogleSpreadsheet2RepeatParameterTable> repeatElementEntities = new ArrayList<GoogleSpreadsheet2RepeatParameterTable>();

  private final SpreadsheetService spreadsheetService;

  // ccSaved Needed by CredentialStore interface -- ccSaved is only valid for a limited timeframe.
  // DO NOT use this as a long-term value. It is present only to work around a bug in
  // GoogleCredential
  private CallingContext ccSaved;

  /**
   * Access token is used for granting access rights to the spreadsheet through
   * the Google Drive API.
   */
  private String accessToken = null;

  /**
   * Common base constructor that initializes final values.
   *
   * @param form
   * @param fpObject
   * @param cc
   * @throws ODKExternalServiceException
   */
  private GoogleSpreadsheet(IForm form, GoogleSpreadsheet2ParameterTable gsObject,
      FormServiceCursor formServiceCursor, CallingContext cc) throws ODKExternalServiceException {
    super(form, formServiceCursor,
        new LinkElementFormatter(cc.getServerURL(), FormMultipleValueServlet.ADDR, true, true, true, true),
        new GoogleSpreadsheetHeaderFormatter(true, true, true),
        cc);
    spreadsheetService = new SpreadsheetService(ServletConsts.APPLICATION_NAME);
    objectEntity = gsObject;
    try {
      // TODO: REMOVE after bug is fixed
      // http://code.google.com/p/gdata-java-client/issues/detail?id=103
      spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V1);
      spreadsheetService.setConnectTimeout(SpreadsheetConsts.SERVER_TIMEOUT);
      spreadsheetService.setOAuth2Credentials(getOAuth2Credential(cc));
    } catch (ODKExternalServiceCredentialsException e) {
      if ( fsc.getOperationalStatus().equals(OperationalStatus.ACTIVE) ) {
        fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
        try {
          persist(cc);
        } catch (Exception e1) {
          logger.error("Unable to persist bad credentials status" + e1.toString());
          throw new ODKExternalServiceException("unable to persist bad credentials status", e1);
        }
      }
      throw e;
    }
  }

  private GoogleSpreadsheet(IForm form, GoogleSpreadsheet2ParameterTable entity,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc) throws ODKDatastoreException, ODKOverQuotaException, ODKExternalServiceException {
    this(form, entity, createFormServiceCursor(form, entity, externalServiceOption, ExternalServiceType.GOOGLE_SPREADSHEET, cc), cc);
    objectEntity.setOwnerEmail(ownerEmail);
  }

  public GoogleSpreadsheet(FormServiceCursor fsc, IForm form, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, ODKOverQuotaException, ODKExternalServiceException, ODKFormNotFoundException {
    this(form, retrieveEntity(GoogleSpreadsheet2ParameterTable.assertRelation(cc), fsc, cc), fsc, cc);

    repeatElementEntities.addAll(GoogleSpreadsheet2RepeatParameterTable.getRepeatGroupAssociations(
        objectEntity.getUri(), cc));

  }

  public GoogleSpreadsheet(IForm form, String name,
      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException, ODKOverQuotaException, ODKExternalServiceException, ODKEntityPersistException {
    this(form, newEntity(GoogleSpreadsheet2ParameterTable.assertRelation(cc), cc), externalServiceOption, ownerEmail, cc);

    objectEntity.setSpreadsheetName(name);
    persist(cc);
  }

  private synchronized GoogleCredential getOAuth2Credential(CallingContext cc) throws ODKExternalServiceException {

    try {
      String serviceAccountId = ServerPreferencesProperties.getServerPreferencesProperty(cc,  ServerPreferencesProperties.GOOGLE_API_CLIENT_ID);
      String serviceAccountUser = ServerPreferencesProperties.getServerPreferencesProperty(cc,  ServerPreferencesProperties.GOOGLE_API_SERVICE_ACCOUNT_EMAIL);
      String privateKeyString = ServerPreferencesProperties.getServerPreferencesProperty(cc, ServerPreferencesProperties.PRIVATE_KEY_FILE_CONTENTS);

      this.ccSaved = cc;

      if ( privateKeyString == null || serviceAccountId == null || serviceAccountUser == null ||
           privateKeyString.trim().length() == 0 ||
           serviceAccountId.trim().length() == 0 ||
           serviceAccountUser.trim().length() == 0 ) {
        throw new ODKExternalServiceCredentialsException("Google API credentials have not be configured");
      }

      byte[] privateKeyBytes = Base64.decodeBase64(privateKeyString);

     KeyStore ks = null;
     try {
        ks = KeyStore.getInstance("PKCS12");
     } catch (KeyStoreException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
     }
     try {
       ks.load(new ByteArrayInputStream(privateKeyBytes), "notasecret".toCharArray());
     } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected NoSuchAlgorithmException " + e.toString());
     } catch (CertificateException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected CertificateException " + e.toString());
     } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected FileNotFoundException " + e.toString());
     } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected IOException " + e.toString());
     }

     Enumeration<String> aliasEnum = null;
     try {
        aliasEnum = ks.aliases();
     } catch (KeyStoreException e) {
        e.printStackTrace();
        throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
     }

     Key key = null;

     while (aliasEnum.hasMoreElements()) {
        String keyName = (String) aliasEnum.nextElement();
        try {
           key = ks.getKey(keyName, "notasecret".toCharArray());
        } catch (UnrecoverableKeyException e) {
           e.printStackTrace();
           throw new IllegalArgumentException("Unexpected UnrecoverableKeyException " + e.toString());
        } catch (KeyStoreException e) {
           e.printStackTrace();
           throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
        } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
           throw new IllegalArgumentException("Unexpected NoSuchAlgorithmException " + e.toString());
        }
        break;
     }
     if ( safeApacheHttpTransport == null ) {
       SafeApacheHttpTransport.setHttpClientFactory(cc);
       safeApacheHttpTransport = new SafeApacheHttpTransport();
     }
     List<CredentialRefreshListener> l = new ArrayList<CredentialRefreshListener>();
     l.add(new CredentialStoreRefreshListener("ignored",this));

      GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountId)
        .setServiceAccountUser(serviceAccountUser)
        .setServiceAccountPrivateKey((PrivateKey) key)
        .setServiceAccountScopes(GOOGLE_SPREADSHEET_OAUTH2_SCOPE)
        .setJsonFactory(jsonFactory)
        .setTransport(safeApacheHttpTransport)
        .setTokenServerUrl(new GenericUrl("https://accounts.google.com/o/oauth2/token"))
        .setRefreshListeners(l)
        .build();
      credential.setHttpClientFactory((HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY));
      // and get any OAuth2 token we might already have
      load("unused", credential);
      return credential;
    } catch (ODKEntityNotFoundException e) {
      String str = "Unable to set Oauth2 credentials";
      logger.error(str + "\nReason: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceCredentialsException(str, e);
    } catch (ODKOverQuotaException e) {
      String str = "Unable to set Oauth2 credentials";
      logger.error(str + "\nReason: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceCredentialsException(str, e);
    }
  }

  public void initiate(CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {
    // authenticate2AndCreate(GOOGLE_SPREADSHEET_OAUTH2_SCOPE, cc);
    // setup service
    DocsService service = new DocsService(ServletConsts.APPLICATION_NAME);
    service.setConnectTimeout(SpreadsheetConsts.SERVER_TIMEOUT);
    service.setOAuth2Credentials(getOAuth2Credential(cc));

    boolean newlyCreated = false;
    if ( fsc.getOperationalStatus() != OperationalStatus.BAD_CREDENTIALS ) {
      newlyCreated = true;

      // create spreadsheet
      com.google.gdata.data.docs.SpreadsheetEntry createdEntry = new SpreadsheetEntry();
      createdEntry.setTitle(new PlainTextConstruct(getSpreadsheetName()));

      com.google.gdata.data.docs.SpreadsheetEntry updatedEntry;
      try {
        updatedEntry = service.insert(new URL(SpreadsheetConsts.DOC_FEED), createdEntry);
      } catch (IOException e) {
        // try one more time
        try {
          updatedEntry = service.insert(new URL(SpreadsheetConsts.DOC_FEED), createdEntry);
        } catch (AuthenticationException e1) {
          e1.printStackTrace();
          throw new ODKExternalServiceCredentialsException(e1);
        } catch (Exception e1) {
           e1.printStackTrace();
          throw new ODKExternalServiceException(e1);
        }
      } catch (AuthenticationException e) {
        e.printStackTrace();
        throw new ODKExternalServiceCredentialsException(e);
      } catch (Exception e) {
        e.printStackTrace();
        throw new ODKExternalServiceException(e);
      }

      // get key
      String spreadKey = updatedEntry.getDocId();

      objectEntity.setSpreadsheetKey(spreadKey);
    }
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    updateReadyValue();
    persist(cc);

    if ( newlyCreated ) {
      executeDrivePermission(objectEntity.getSpreadsheetKey(), objectEntity.getOwnerEmail(), logger, "google spreadsheet", cc);

      try {
        // create worksheet
        WorksheetCreator ws = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(ExternalServiceConsts.EXT_SERV_ADDRESS, getSpreadsheetName());
        parameters.put(ServletConsts.EXTERNAL_SERVICE_TYPE, fsc.getExternalServicePublicationOption().name());

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
      if (!fsc.getExternalServicePublicationOption().equals(ExternalServicePublicationOption.STREAM_ONLY)) {

        UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
        CallingContext ccDaemon = ContextFactory.duplicateContext(cc);
        ccDaemon.setAsDaemon(true);
        uploadTask.createFormUploadTask(fsc, ccDaemon);
      }

    }
  }

  protected String getAccessToken(boolean forceRefresh, CallingContext cc) throws ODKExternalServiceCredentialsException {
    try {
      if (accessToken == null && !forceRefresh) {
        accessToken = ServerPreferencesProperties.getServerPreferencesProperty(cc,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_ACCESS_TOKEN);
      }

      if (accessToken == null || forceRefresh) {
        accessToken = getOAuth2AccessToken(GOOGLE_SPREADSHEET_OAUTH2_SCOPE, cc);
        ServerPreferencesProperties.setServerPreferencesProperty(cc,
            ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_ACCESS_TOKEN, accessToken);
      }
      return accessToken;
    } catch (Exception e) {
      throw new ODKExternalServiceCredentialsException("Unable to obtain OAuth2 access token: "
          + e.toString());
    }
  }

  public void sharePublishedFiles(String ownerEmail, CallingContext cc) throws ODKExternalServiceException {
    if (fsc.isExternalServicePrepared()) {
      executeDrivePermission(objectEntity.getSpreadsheetKey(), ownerEmail, logger, "google spreadsheet", cc);
    }
  }

  public Boolean getReady() {
    return objectEntity.getReady();
  }

  public void updateReadyValue() {
    boolean ready = (objectEntity.getSpreadsheetName()!= null)
        && (objectEntity.getSpreadsheetKey() != null);
    objectEntity.setReady(ready);
  }

  public String getSpreadsheetName() {
    return objectEntity.getSpreadsheetName();
  }

  public void generateWorksheets(CallingContext cc) throws ODKDatastoreException, IOException,
      ServiceException {

    // retrieve pre-existing worksheets
    URL url = new URL(SpreadsheetConsts.SPREADSHEETS_FEED
        + URLEncoder.encode(objectEntity.getSpreadsheetKey(), UTF_8_ENCODING));
    com.google.gdata.data.spreadsheet.SpreadsheetEntry entry = spreadsheetService.getEntry(url,
        com.google.gdata.data.spreadsheet.SpreadsheetEntry.class);
    List<WorksheetEntry> preExistingWorksheets = entry.getWorksheets();

    // create top level worksheet
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(), null);
    WorksheetEntry topLevelWorksheet = executeCreateWorksheet(entry, form.getFormId(), headers);
    objectEntity.setTopLevelWorksheetId(extractWorksheetId(topLevelWorksheet));

    // delete pre-existing worksheets
    for (WorksheetEntry worksheet : preExistingWorksheets) {
      worksheet.getSelf().delete();
    }

    // get relation prototype for creating repeat parameter table entries
    GoogleSpreadsheet2RepeatParameterTable repeatPrototype = GoogleSpreadsheet2RepeatParameterTable
        .assertRelation(cc);

    // create repeat worksheets
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      // create the worksheet
      headers = headerFormatter.generateHeaders(form, repeatGroupElement, null);
      WorksheetEntry repeatWorksheet = executeCreateWorksheet(entry,
          repeatGroupElement.getElementName(), headers);

      // add the worksheet id to the repeat element table -- NOTE: the added
      // entry is not actually persisted here
      GoogleSpreadsheet2RepeatParameterTable t = ds.createEntityUsingRelation(repeatPrototype, user);
      t.setUriGoogleSpreadsheet(objectEntity.getUri());
      t.setFormElementKey(repeatGroupElement.constructFormElementKey(form));
      t.setWorksheetId(extractWorksheetId(repeatWorksheet));
      repeatElementEntities.add(t);
    }

    // persist the changes we have made to the repeat element table (changes
    // from calling executeCreateWorksheet)
    fsc.setIsExternalServicePrepared(true); // we have completed worksheet
    // creation...
    persist(cc);
  }

  private String extractWorksheetId(WorksheetEntry entry) throws IOException, ServiceException {
    String[] urlElements = entry.getSelf().getId().split("/");
    String worksheetId = urlElements[urlElements.length - 1];
    return worksheetId;
  }

  private WorksheetEntry executeCreateWorksheet(
      com.google.gdata.data.spreadsheet.SpreadsheetEntry entry, String title, List<String> headers)
      throws IOException, ServiceException {

    // create the worksheet
    WorksheetEntry uncreatedWorksheet = new WorksheetEntry();
    uncreatedWorksheet.setTitle(new PlainTextConstruct(title));
    uncreatedWorksheet.setRowCount(2);
    uncreatedWorksheet.setColCount(headers.size());
    URL worksheetFeedUrl = entry.getWorksheetFeedUrl();
    logger.info("WorksheetFeedUrl: " + worksheetFeedUrl.toString() );
    WorksheetEntry createdWorksheet = spreadsheetService.insert(worksheetFeedUrl,
        uncreatedWorksheet);
    logger.info("CellFeedUrl: " + createdWorksheet.getCellFeedUrl().toString() );

    // update the cells of the worksheet with the proper headers
    // first query the worksheet for the cells we need to change
    CellQuery query = new CellQuery(createdWorksheet.getCellFeedUrl());
    query.setMinimumRow(1);
    query.setMaximumRow(1);
    query.setMinimumCol(1);
    query.setMaximumCol(headers.size());
    query.setReturnEmpty(true);
    CellFeed existingCellFeed = spreadsheetService.query(query, CellFeed.class);
    // create the new cell feed based on our headers
    CellFeed batchRequest = new CellFeed();
    List<CellEntry> cells = existingCellFeed.getEntries();
    int index = 0;
    for (CellEntry cell : cells) {
      String header = headers.get(index);
      cell.changeInputValueLocal(header);
      BatchUtils.setBatchId(cell, Integer.toString(index));
      BatchUtils.setBatchOperationType(cell, BatchOperationType.UPDATE);
      batchRequest.getEntries().add(cell);
      index++;
    }
    // submit the cell feed update as a batch operation
    Link batchLink = existingCellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
    URL batchLinkUrl = new URL(batchLink.getHref());
    logger.info("BatchLinkUrl: " + batchLinkUrl.toString());
    CellFeed batchResponse = spreadsheetService.batch(batchLinkUrl, batchRequest);

    // Check the results
    for (CellEntry cellEntry : batchResponse.getEntries()) {
      String batchId = BatchUtils.getBatchId(cellEntry);
      if (!BatchUtils.isSuccess(cellEntry)) {
        BatchStatus status = BatchUtils.getBatchStatus(cellEntry);
        System.out.printf("%s failed (%s) %s", batchId, status.getReason(), status.getContent());
        // TODO: throw exception?
      }
    }

    return createdWorksheet;
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    if (getReady()) {
      try {
        // upload base submission values
        List<String> headers = headerFormatter.generateHeaders(form,
            form.getTopLevelGroupElement(), null);
        WorksheetEntry topLevelWorksheet = getWorksheet(objectEntity.getTopLevelWorksheetId());
        executeInsertData(submission, headers, topLevelWorksheet, cc);

        // upload repeat values
        for (GoogleSpreadsheet2RepeatParameterTable tableId : repeatElementEntities) {
          FormElementKey elementKey = tableId.getFormElementKey();
          FormElementModel element = FormElementModel.retrieveFormElementModel(form, elementKey);
          headers = headerFormatter.generateHeaders(form, element, null);

          List<SubmissionValue> values = submission.findElementValue(element);
          for (SubmissionValue value : values) {
            if (value instanceof RepeatSubmissionType) {
              RepeatSubmissionType repeat = (RepeatSubmissionType) value;
              if (repeat.getElement().equals(element)) {
                for (SubmissionSet set : repeat.getSubmissionSets()) {
                  WorksheetEntry repeatWorksheet = getWorksheet(tableId.getWorksheetId());
                  executeInsertData(set, headers, repeatWorksheet, cc);
                }
              }
            } else {
              System.out
                  .println("ERROR: How did a non Repeat Submission Type get in the for loop?");
            }
          }
        }
      } catch ( AuthenticationException e) {
        logger.error("Unable to insert data into spreadsheet " + objectEntity.getSpreadsheetName() + " exception: " + e.getMessage());
        e.printStackTrace();
        fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
        try {
          persist(cc);
        } catch (Exception e1) {
          e1.printStackTrace();
          throw new ODKExternalServiceException("Unable to set OperationalStatus to Bad Credentials: " + e.toString(), e1);
        }
        e.printStackTrace();
        throw new ODKExternalServiceCredentialsException(e);
      } catch (Exception e) {
    	  e.printStackTrace();
    	  logger.error("Unable to insert data into spreadsheet " + objectEntity.getSpreadsheetName() + " exception: " + e.getMessage());
        throw new ODKExternalServiceException(e);
      }
    }
  }

  /**
   * Inserts the data in the given submissionSet as a new entry (i.e. a new row)
   * in the given worksheet, including only the data specified by headers.
   *
   * @param submissionSet
   *          the set of data from a single submission
   * @param headers
   *          a list of headers corresponding to the headers in worksheet. Only
   *          the data in submissionSet corresponding to these headers will be
   *          submitted.
   * @param worksheet
   *          the worksheet representing the worksheet in a Google Spreadsheet.
   * @throws ODKDatastoreException
   *           if there was a problem in the datastore
   * @throws IOException
   *           if there was a problem communicating over the internet with the
   *           Google Spreadsheet
   * @throws ServiceException
   *           if there was a problem with the GData service
   */
  private void executeInsertData(SubmissionSet submissionSet, List<String> headers,
      WorksheetEntry worksheet, CallingContext cc) throws ODKDatastoreException, IOException,
      ServiceException {
    ListEntry newEntry = new ListEntry();
    CustomElementCollection values = newEntry.getCustomElements();

    Row row = submissionSet.getFormattedValuesAsRow(null, formatter, true, cc);
    List<String> formattedValues = row.getFormattedValues();

    String rowString = null;
    String headerString = null;
    for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
      headerString = headers.get(colIndex);
      rowString = formattedValues.get(colIndex);
      values.setValueLocal(headerString, (rowString == null) ? BasicConsts.SPACE : rowString);
    }

    URL listFeedUrl = worksheet.getListFeedUrl();
    logger.info("listFeedUrl: " + listFeedUrl.toString());
    spreadsheetService.insert(listFeedUrl, newEntry);
  }

  public WorksheetEntry getWorksheet(String worksheetId) throws IOException, ServiceException {
    URL url = new URL(SpreadsheetConsts.WORKSHEETS_FEED
        + URLEncoder.encode(objectEntity.getSpreadsheetKey(), UTF_8_ENCODING) + SpreadsheetConsts.FEED_PERMISSIONS
        + URLEncoder.encode(worksheetId, UTF_8_ENCODING));
    WorksheetEntry worksheetEntry = spreadsheetService.getEntry(url, WorksheetEntry.class);
    return worksheetEntry;
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
    return getSpreadsheetName();
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return repeatElementEntities;
  }

  @Override
  public boolean load(String userId, Credential credential) {
    try {
      String tokenString = ServerPreferencesProperties.getServerPreferencesProperty(ccSaved,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_ACCESS_TOKEN);
      if ( tokenString != null ) {
        credential.setAccessToken(tokenString);
        String refreshString = ServerPreferencesProperties.getServerPreferencesProperty(ccSaved,
            ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_REFRESH_TOKEN);
        credential.setRefreshToken(refreshString);
        String expirationTimestamp = ServerPreferencesProperties.getServerPreferencesProperty(ccSaved,
            ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_EXPIRATION_TIME);
        credential.setExpirationTimeMilliseconds(expirationTimestamp == null ? null : Long.valueOf(expirationTimestamp));
        return true;
      }
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void store(String userId, Credential credential) throws IOException {
    try {
      accessToken = credential.getAccessToken();
      ServerPreferencesProperties.setServerPreferencesProperty(ccSaved,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_ACCESS_TOKEN,
          credential.getAccessToken());
      ServerPreferencesProperties.setServerPreferencesProperty(ccSaved,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_REFRESH_TOKEN,
          credential.getRefreshToken());
      Long expirationTime = credential.getExpirationTimeMilliseconds();
      ServerPreferencesProperties.setServerPreferencesProperty(ccSaved,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_EXPIRATION_TIME,
          (expirationTime == null) ? null : Long.toString(expirationTime));
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void delete(String userId, Credential credential) throws IOException {
    try {
      accessToken = null;
      ServerPreferencesProperties.setServerPreferencesProperty(ccSaved,
          ServerPreferencesProperties.GOOGLE_SPREADSHEETS_OAUTH2_ACCESS_TOKEN,
          null);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
    }
  }

}
