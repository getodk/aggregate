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
package org.opendatakit.aggregate.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExportStatus;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PersistentResults {
  public static final long RETRY_INTERVAL_MILLISECONDS = (11 * 60) * 1000; // 11 minutes
  // public static final long RETRY_INTERVAL_MILLISECONDS = 10000; // for debugging...
  public static final long MAX_RETRY_ATTEMPTS = 3;

  public static final String FORM_ID_PERSISTENT_RESULT = "aggregate.opendatakit.org:PersistentResults";

  private final PersistentResultsTable row;

  private final BinaryContentManipulator bcm;

  private PersistentResults(PersistentResultsTable row, CallingContext cc)
      throws ODKDatastoreException {
    this.row = row;
    this.bcm = PersistentResultsTable.assertManipulator(row.getUri(), cc);
  }

  /**
   * Constructor when retrieving a PersistentResults entry from the datastore.
   * 
   * @param persistentResult
   *          -- submission key of the persistent result to retrieve.
   * @param cc
   * @throws ODKDatastoreException
   */
  public PersistentResults(SubmissionKey persistentResult, CallingContext cc)
      throws ODKDatastoreException {
    List<SubmissionKeyPart> parts = persistentResult.splitSubmissionKey();
    if (parts == null || parts.size() == 0) {
      throw new IllegalArgumentException("submission key is empty");
    }
    if (!parts.get(0).getElementName().equals(FORM_ID_PERSISTENT_RESULT)) {
      throw new IllegalArgumentException("unrecognized form id");
    }
    if (parts.size() < 2) {
      throw new IllegalArgumentException("submission key does not have a top level group");
    }
    SubmissionKeyPart tlg = parts.get(1);
    if (!tlg.getElementName().equals(PersistentResultsTable.TABLE_NAME)) {
      throw new IllegalArgumentException("top level group name: " + tlg.getElementName()
          + " is not as expected: " + PersistentResultsTable.TABLE_NAME);
    }
    if (tlg.getAuri() == null) {
      throw new IllegalArgumentException("submission key does not have top level auri");
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    row = ds.getEntity(relation, tlg.getAuri(), user);
    this.bcm = PersistentResultsTable.assertManipulator(row.getUri(), cc);
  }

  /**
   * Constructor helper for the common case. Note that the form (objectEntity)
   * is not yet persisted. To persist it, you must call
   * objectEntity.persist(datastore, user)
   * 
   * @param requestingUser
   * @param requestDate
   * @param status
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public PersistentResults(ExportType type, IForm form, SubmissionFilterGroup filterGroup, Map<String, String> parameters,
      CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    row = ds.createEntityUsingRelation(relation, user);
    setRequestingUser(user.getUriUser());
    Date now = new Date();
    setRequestDate(now);
    setRequestParameters(parameters);
    setLastRetryDate(now);
    setAttemptCount(1L);
    setStatus(ExportStatus.GENERATION_IN_PROGRESS);
    setResultType(type);
    setFormId(form.getFormId());
    if(filterGroup != null) {
      setFilterGroupUri(filterGroup.getUri());
    }
    this.bcm = PersistentResultsTable.assertManipulator(row.getUri(), cc);

    // NOTE: the entity is not yet persisted!
  }

  public String getRequestingUser() {
    return row.getStringField(PersistentResultsTable.REQUESTING_USER);
  }

  public void setRequestingUser(String value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.REQUESTING_USER, value)) {
      throw new IllegalStateException("overflow requestingUser");
    }
  }

  public Date getRequestDate() {
    return row.getDateField(PersistentResultsTable.REQUEST_DATE);
  }

  public void setRequestDate(Date value) {
    row.setDateField(PersistentResultsTable.REQUEST_DATE, value);
  }

  public Map<String, String> getRequestParameters() throws ODKDatastoreException {
    String parameterDocument = row.getStringField(PersistentResultsTable.REQUEST_PARAMETERS);
    try {
      return PropertyMapSerializer.deserializeRequestParameters(parameterDocument);
    } catch (Exception e) {
      throw new ODKDatastoreException("bad parameter list in database", e);
    }
  }

  public void setRequestParameters(Map<String, String> value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.REQUEST_PARAMETERS,
        PropertyMapSerializer.serializeRequestParameters(value))) {
      throw new IllegalStateException("overflow requestParameters");
    }
  }

  public Date getLastRetryDate() {
    return row.getDateField(PersistentResultsTable.LAST_RETRY_DATE);
  }

  public void setLastRetryDate(Date value) {
    row.setDateField(PersistentResultsTable.LAST_RETRY_DATE, value);
  }

  public Long getAttemptCount() {
    return row.getLongField(PersistentResultsTable.ATTEMPT_COUNT);
  }

  public void setAttemptCount(Long value) {
    row.setLongField(PersistentResultsTable.ATTEMPT_COUNT, value);
  }

  public ExportStatus getStatus() {
    return ExportStatus.valueOf(row.getStringField(PersistentResultsTable.STATUS));
  }

  public void setStatus(ExportStatus value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.STATUS, value.name())) {
      throw new IllegalStateException("overflow status");
    }
  }

  public ExportType getResultType() {
    return ExportType.valueOf(row.getStringField(PersistentResultsTable.RESULT_TYPE));
  }

  public void setResultType(ExportType value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.RESULT_TYPE, value.name())) {
      throw new IllegalStateException("overflow resultType");
    }
  }

  public Date getCompletionDate() {
    return row.getDateField(PersistentResultsTable.COMPLETION_DATE);
  }

  public void setCompletionDate(Date value) {
    row.setDateField(PersistentResultsTable.COMPLETION_DATE, value);
  }

  public class ResultFileInfo {
    public final String unrootedFilename;
    public final String downloadUrl;
    public final String contentType;
    public final Long contentLength;

    ResultFileInfo(String unrootedFilename, String downloadUrl, String contentType,
        Long contentLength) {
      this.unrootedFilename = unrootedFilename;
      this.downloadUrl = downloadUrl;
      this.contentType = contentType;
      this.contentLength = contentLength;
    }
  }

  public ResultFileInfo getResultFileInfo(CallingContext cc) throws ODKDatastoreException {

    if (bcm.getAttachmentCount(cc) == 0)
      return null;
    if (bcm.getAttachmentCount(cc) > 1) {
      throw new IllegalStateException("Too many results attached!");
    }
    String unrootedFilename = bcm.getUnrootedFilename(1, cc);
    SubmissionKey key = getSubmissionKey();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, key.toString());
    properties.put(ServletConsts.AS_ATTACHMENT, "yes");
    String addr = cc.getServerURL() + BasicConsts.FORWARDSLASH + BinaryDataServlet.ADDR;
    String url = HtmlUtil.createLinkWithProperties(addr, properties);
    return new ResultFileInfo(unrootedFilename, url, bcm.getContentType(1, cc), bcm.getContentLength(1, cc));
  }

  public byte[] getResultFileContents(CallingContext cc) throws ODKDatastoreException {
    if (bcm.getAttachmentCount(cc) == 0)
      return null;
    if (bcm.getAttachmentCount(cc) > 1) {
      throw new IllegalStateException("Too many results attached!");
    }
    return bcm.getBlob(1, cc);
  }

  public void setResultFile(byte[] byteArray, String contentType,
      String unrootedFilePath, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException {
    if (bcm.getAttachmentCount(cc) > 0) {
      throw new IllegalStateException("Results are already attached!");
    }
    bcm.setValueFromByteArray(byteArray, contentType, unrootedFilePath, overwriteOK, cc);
  }

  public String getUri() {
    return row.getUri();
  }
  
  public String getFormId() {
    return row.getStringField(PersistentResultsTable.FORM_ID);
  }

  public void setFormId(String value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.FORM_ID, value)) {
      throw new IllegalStateException("overflow formId");
    }
  }

  public String getFilterGroupUri() {
    return row.getStringField(PersistentResultsTable.URI_FILTER_GROUP_PROPERTY);
  }

  public void setFilterGroupUri(String value) throws ODKEntityPersistException {
    if (!row.setStringField(PersistentResultsTable.URI_FILTER_GROUP_PROPERTY, value)) {
      throw new IllegalStateException("overflow Uri of the filter group");
    }
  }
  
  public void deleteResultFile(CallingContext cc) throws ODKDatastoreException {
    bcm.deleteAll(cc);
  }

  public void deleteFilterGroup(CallingContext cc) throws ODKDatastoreException {
    if(getFilterGroupUri() != null) {
      try {
   	    SubmissionFilterGroup subFilterGroup = 
   		  SubmissionFilterGroup.getFilterGroup(getFilterGroupUri(), cc);
        subFilterGroup.delete(cc);
      } catch ( ODKEntityNotFoundException e ) {
    	  // this should be fine to ignore 
    	  // -- it likely means the filter group was already deleted.
    	  e.printStackTrace();
      }
    }
  }
  
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    bcm.persist(cc);
    ds.putEntity(row, user);
  }

  public void delete(CallingContext cc) throws ODKDatastoreException {
    deleteResultFile(cc);
    deleteFilterGroup(cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.deleteEntity(row.getEntityKey(), user);
  }

  public SubmissionKey getSubmissionKey() {
    return new SubmissionKey(FORM_ID_PERSISTENT_RESULT + "[@version=null and @uiVersion=null]/"
        + PersistentResultsTable.TABLE_NAME + "[@key=" + row.getUri() + "]");
  }

  public static final PersistentResults getPersistentResult(String uri, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    PersistentResultsTable p = ds.getEntity(relation, uri, user);
    PersistentResults export = new PersistentResults(p, cc);
    return export;
  }

  public static final List<PersistentResults> getAvailablePersistentResults(CallingContext cc)
      throws ODKFormNotFoundException, ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    Query query = ds.createQuery(relation, "PersistentResults.getAvailablePersistentResults", user);

    Date lastDate = new Date();
    query.addSort(relation.lastUpdateDate, Query.Direction.DESCENDING);
    query.addFilter(relation.lastUpdateDate, Query.FilterOperation.LESS_THAN, lastDate);
    // query.addFilter(PersistentResults.getRequestingUserKey(),
    // FilterOperation.EQUAL, cc.getCurrentUser().getUriUser());

    List<? extends CommonFieldsBase> l = query.executeQuery();

    List<PersistentResults> results = new ArrayList<PersistentResults>();
    for (CommonFieldsBase cb : l) {
      PersistentResultsTable p = (PersistentResultsTable) cb;
      PersistentResults export = new PersistentResults(p, cc);
      results.add(export);
    }

    return results;
  }

  public static final List<PersistentResults> getStalledRequests(CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    Query q = ds.createQuery(relation, "PersistentResults.getStalledRequests", user);
    Date now = new Date();

    Date limit = new Date(now.getTime() - RETRY_INTERVAL_MILLISECONDS);
    q.addFilter(PersistentResultsTable.LAST_RETRY_DATE, FilterOperation.LESS_THAN, limit);
    List<? extends CommonFieldsBase> l = q.executeQuery();
    /*
     * The list of objects consists only of those that were last fired at a
     * lastRetryDate older than the retry interval, which should be longer than
     * the allowed Task lifetime.
     */
    List<PersistentResults> r = new ArrayList<PersistentResults>();
    for (CommonFieldsBase b : l) {
      PersistentResultsTable p = (PersistentResultsTable) b;
      PersistentResults result = new PersistentResults(p, cc);
      if (result.getStatus() == ExportStatus.AVAILABLE)
        continue;
      if (result.getStatus() == ExportStatus.ABANDONED)
        continue;
      if (result.getAttemptCount().compareTo(MAX_RETRY_ATTEMPTS) >= 0) {
        // the task is stale, and should be marked abandoned,
        // but the worker thread must have failed. Attempt
        // it here...
        result.setAttemptCount(result.getAttemptCount() + 1L);
        result.setStatus(ExportStatus.ABANDONED);
        result.setCompletionDate(now);
        result.persist(cc);
        continue;
      }
      // OK. If we are here, a task was last fired for this request
      // more than the retry interval ago and the task is eligible
      // to be restarted.
      r.add(result);
    }
    return r;
  }

  public static final List<PersistentResults> getAllTasksForForm(IForm theForm, CallingContext cc)
      throws ODKDatastoreException {
    User user = cc.getCurrentUser();
    Datastore ds = cc.getDatastore();
    PersistentResultsTable relation = PersistentResultsTable.assertRelation(cc);
    Query q = ds.createQuery(relation, "PersistentResults.getAllTasksForForm", user);
    q.addFilter(PersistentResultsTable.FORM_ID, FilterOperation.EQUAL, theForm.getFormId());
    List<? extends CommonFieldsBase> l = q.executeQuery();

    List<PersistentResults> r = new ArrayList<PersistentResults>();
    for (CommonFieldsBase b : l) {
      PersistentResultsTable p = (PersistentResultsTable) b;
      PersistentResults result = new PersistentResults(p, cc);
      r.add(result);
    }
    return r;
  }

  /**
   * Underlying top-level persistent object for the PerisistentResults form.
   * 
   * @author mitchellsundt@gmail.com
   * 
   */
  private static final class PersistentResultsTable extends CommonFieldsBase {

    static final String TABLE_NAME = "_persistent_results";
    private static final String PERSISTENT_RESULT_FILE_REF_BLOB = "_persistent_result_file_blb";
    private static final String PERSISTENT_RESULT_FILE_BINARY_CONTENT_REF_BLOB = "_persistent_result_file_ref";
    private static final String PERSISTENT_RESULT_FILE_BINARY_CONTENT = "_persistent_result_file_bin";

    private static final DataField REQUESTING_USER = new DataField("REQUESTING_USER",
        DataField.DataType.STRING, true);

    private static final DataField REQUEST_DATE = new DataField("REQUEST_DATE",
        DataField.DataType.DATETIME, true);

    private static final DataField REQUEST_PARAMETERS = new DataField("REQUEST_PARAMETERS",
        DataField.DataType.STRING, true, 8192L);

    private static final DataField LAST_RETRY_DATE = new DataField("LAST_RETRY_DATE",
        DataField.DataType.DATETIME, true);

    private static final DataField ATTEMPT_COUNT = new DataField("ATTEMPT_COUNT",
        DataField.DataType.INTEGER, true);

    private static final DataField STATUS = new DataField("STATUS", DataField.DataType.STRING, true);

    private static final DataField RESULT_TYPE = new DataField("RESULT_TYPE",
        DataField.DataType.STRING, true);

    private static final DataField COMPLETION_DATE = new DataField("COMPLETION_DATE",
        DataField.DataType.DATETIME, true);

    private static final DataField FORM_ID = new DataField("FORM_ID_KEY",
        DataField.DataType.STRING, true, IForm.MAX_FORM_ID_LENGTH);

    private static final DataField URI_FILTER_GROUP_PROPERTY = new DataField("URI_FILTER_GROUP",
        DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);

    /**
     * Construct a relation prototype.
     * 
     * @param databaseSchema
     */
    private PersistentResultsTable(String databaseSchema) {
      super(databaseSchema, TABLE_NAME);
      fieldList.add(REQUESTING_USER);
      fieldList.add(REQUEST_DATE);
      fieldList.add(REQUEST_PARAMETERS);
      fieldList.add(LAST_RETRY_DATE);
      fieldList.add(ATTEMPT_COUNT);
      fieldList.add(STATUS);
      fieldList.add(RESULT_TYPE);
      fieldList.add(COMPLETION_DATE);
      fieldList.add(FORM_ID);
      fieldList.add(URI_FILTER_GROUP_PROPERTY);
    }

    /**
     * Construct an empty entity.
     * 
     * @param ref
     * @param user
     */
    private PersistentResultsTable(PersistentResultsTable ref, User user) {
      super(ref, user);
    }

    @Override
    public PersistentResultsTable getEmptyRow(User user) {
      return new PersistentResultsTable(this, user);
    }

    private static PersistentResultsTable relation = null;
    private static BinaryContent binaryRelation = null;
    private static BinaryContentRefBlob binaryRefBlobRelation = null;
    private static RefBlob refBlobRelation = null;

    static synchronized final PersistentResultsTable assertRelation(CallingContext cc)
        throws ODKDatastoreException {
      if (relation == null) {
        PersistentResultsTable relationPrototype;
        Datastore ds = cc.getDatastore();
        User user = cc.getUserService().getDaemonAccountUser();
        relationPrototype = new PersistentResultsTable(ds.getDefaultSchemaName());
        ds.assertRelation(relationPrototype, user); // may throw exception...
        // at this point, the prototype has become fully populated
        BinaryContent bc = new BinaryContent(ds.getDefaultSchemaName(),
            PERSISTENT_RESULT_FILE_BINARY_CONTENT);
        ds.assertRelation(bc, user);
        BinaryContentRefBlob bref = new BinaryContentRefBlob(ds.getDefaultSchemaName(),
            PERSISTENT_RESULT_FILE_BINARY_CONTENT_REF_BLOB);
        ds.assertRelation(bref, user);
        RefBlob ref = new RefBlob(ds.getDefaultSchemaName(), PERSISTENT_RESULT_FILE_REF_BLOB);
        ds.assertRelation(ref, user);
        // everything is now fully populated
        binaryRelation = bc;
        binaryRefBlobRelation = bref;
        refBlobRelation = ref;
        relation = relationPrototype; // set static variable only upon
                                      // success...
      }
      return relation;
    }

    static final BinaryContentManipulator assertManipulator(String uri, CallingContext cc)
        throws ODKDatastoreException {
      // make sure the relations are defined...
      assertRelation(cc);
      return new BinaryContentManipulator(uri, uri, binaryRelation, binaryRefBlobRelation,
          refBlobRelation);
    }
  }
}
