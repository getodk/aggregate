/**
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class FormServiceCursor extends CommonFieldsBase {

  private static final String TABLE_NAME = "_form_service_cursor_2";

  private static final DataField URI_MD5_FORM_ID_PROPERTY = new DataField("URI_MD5_FORM_ID",
	      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
  private static final DataField AURI_SERVICE_PROPERTY = new DataField("AURI_SERVICE",
	      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);

  private static final DataField EXT_SERVICE_TYPE_PROPERTY = new DataField("EXT_SERVICE_TYPE",
      DataField.DataType.STRING, false, 200L);
  private static final DataField EXTERNAL_SERVICE_OPTION = new DataField("EXTERNAL_SERVICE_OPTION",
      DataField.DataType.STRING, false, 80L);
  // some external services need to be prepared before they can receive data...
  private static final DataField IS_EXTERNAL_SERVICE_PREPARED = new DataField("IS_EXTERNAL_SERVICE_PREPARED",
		  DataField.DataType.BOOLEAN, true);
  private static final DataField OPERATIONAL_STATUS = new DataField("OPERATIONAL_STATUS",
		  DataField.DataType.STRING, true, 80L);
  private static final DataField RETRY_STATUS = new DataField("RETRY_STATUS",
      DataField.DataType.STRING, true, 80L);
  private static final DataField ESTABLISHMENT_DATETIME = new DataField("ESTABLISHMENT_DATETIME",
      DataField.DataType.DATETIME, false);
  private static final DataField UPLOAD_COMPLETED_PROPERTY = new DataField("UPLOAD_COMPLETED",
      DataField.DataType.BOOLEAN, true);
  private static final DataField LAST_UPLOAD_CURSOR_DATE_PROPERTY = new DataField(
      "LAST_UPLOAD_PERSISTENCE_CURSOR", DataField.DataType.DATETIME, true);
  private static final DataField LAST_UPLOAD_KEY_PROPERTY = new DataField("LAST_UPLOAD_KEY",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField LAST_STREAMING_CURSOR_DATE_PROPERTY = new DataField(
      "LAST_STREAMING_PERSISTENCE_CURSOR", DataField.DataType.DATETIME, true);
  private static final DataField LAST_STREAMING_KEY_PROPERTY = new DataField("LAST_STREAMING_KEY",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField FORM_ID_PROPERTY = new DataField("FORM_ID",
      DataField.DataType.STRING, true, 4096L);

	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(CallingContext)}
	 *
	 * @param databaseSchema
	 * @param tableName
	 */
  private FormServiceCursor(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(URI_MD5_FORM_ID_PROPERTY);
    fieldList.add(AURI_SERVICE_PROPERTY);
    fieldList.add(EXT_SERVICE_TYPE_PROPERTY);
    fieldList.add(EXTERNAL_SERVICE_OPTION);
    fieldList.add(IS_EXTERNAL_SERVICE_PREPARED);
    fieldList.add(OPERATIONAL_STATUS);
    fieldList.add(RETRY_STATUS);
    fieldList.add(ESTABLISHMENT_DATETIME);
    fieldList.add(UPLOAD_COMPLETED_PROPERTY);
    fieldList.add(LAST_UPLOAD_CURSOR_DATE_PROPERTY);
    fieldList.add(LAST_UPLOAD_KEY_PROPERTY);
    fieldList.add(LAST_STREAMING_CURSOR_DATE_PROPERTY);
    fieldList.add(LAST_STREAMING_KEY_PROPERTY);
    fieldList.add(FORM_ID_PROPERTY);
  }

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 *
	 * @param ref
	 * @param user
	 */
  private FormServiceCursor(FormServiceCursor ref, User user) {
    super(ref, user);
  }

  // Only called from within the persistence layer.
  @Override
  public FormServiceCursor getEmptyRow(User user) {
	return new FormServiceCursor(this, user);
  }

  public ExternalServiceType getExternalServiceType() {
    String type = getStringField(EXT_SERVICE_TYPE_PROPERTY);
    return ExternalServiceType.valueOf(type);
  }

  public void setServiceClassname(ExternalServiceType value) {
    if (!setStringField(EXT_SERVICE_TYPE_PROPERTY, value.name())) {
      throw new IllegalArgumentException("overflow externalServiceType");
    }
  }

  public ExternalServicePublicationOption getExternalServicePublicationOption() {
    return ExternalServicePublicationOption.valueOf(getStringField(EXTERNAL_SERVICE_OPTION));
  }

  public void setExternalServiceOption(ExternalServicePublicationOption value) {
    if (!setStringField(EXTERNAL_SERVICE_OPTION, value.name())) {
      throw new IllegalArgumentException("overflow externalServiceOption");
    }
  }

  public Boolean isExternalServicePrepared() {
	  return getBooleanField(IS_EXTERNAL_SERVICE_PREPARED);
  }

  public void setIsExternalServicePrepared(Boolean value) {
	  setBooleanField(IS_EXTERNAL_SERVICE_PREPARED, value);
  }

  public OperationalStatus getOperationalStatus() {
	  String value = getStringField(OPERATIONAL_STATUS);
	  if ( value == null ) return null;
	  return OperationalStatus.valueOf(value);
  }

  public void setOperationalStatus(OperationalStatus value) {
    if (!setStringField(OPERATIONAL_STATUS, value.name())) {
        throw new IllegalArgumentException("overflow operationalStatus");
    }
  }
// TODO: implement failure after N retries (TBD)
// RetryStatus is added to support that determination.
//
//  public String getRetryStatus() {
//     String value = getStringField(RETRY_STATUS);
//     if ( value == null ) return null;
//     return value;
//  }
//
//  public void setRetryStatus(String value) {
//    if (!setStringField(RETRY_STATUS, value)) {
//      throw new IllegalArgumentException("overflow retryStatus");
//    }
//  }

  public Date getEstablishmentDateTime() {
    return getDateField(ESTABLISHMENT_DATETIME);
  }

  public void setEstablishmentDateTime(Date value) {
    setDateField(ESTABLISHMENT_DATETIME, value);
  }

  public Boolean getUploadCompleted() {
    return getBooleanField(UPLOAD_COMPLETED_PROPERTY);
  }

  public void setUploadCompleted(Boolean value) {
    setBooleanField(UPLOAD_COMPLETED_PROPERTY, value);
  }

  public Date getLastUploadCursorDate() {
    return getDateField(LAST_UPLOAD_CURSOR_DATE_PROPERTY);
  }

  public void setLastUploadCursorDate(Date value) {
    setDateField(LAST_UPLOAD_CURSOR_DATE_PROPERTY, value);
  }

  public String getLastUploadKey() {
    return getStringField(LAST_UPLOAD_KEY_PROPERTY);
  }

  public void setLastUploadKey(String value) {
    if (!setStringField(LAST_UPLOAD_KEY_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow lastUploadKey");
    }
  }

  public Date getLastStreamingCursorDate() {
    return getDateField(LAST_STREAMING_CURSOR_DATE_PROPERTY);
  }

  public void setLastStreamingCursorDate(Date value) {
    setDateField(LAST_STREAMING_CURSOR_DATE_PROPERTY, value);
  }

  public String getLastStreamingKey() {
    return getStringField(LAST_STREAMING_KEY_PROPERTY);
  }

  public void setLastStreamingKey(String value) {
    if (!setStringField(LAST_STREAMING_KEY_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow lastStreamingKey");
    }
  }

  public String getAuriService() {
    return getStringField(AURI_SERVICE_PROPERTY);
  }

  public void setAuriService(String value) {
    if (!setStringField(AURI_SERVICE_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow auriService");
    }
  }

  public String getUriMd5FormId() {
    return getStringField(URI_MD5_FORM_ID_PROPERTY);
  }

  public void setUriMd5FormId(String value) {
    if (!setStringField(URI_MD5_FORM_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow uriMd5FormId");
    }
  }

  public String getFormId() {
    return getStringField(FORM_ID_PROPERTY);
  }

  public void setFormId(String value) {
    if (!setStringField(FORM_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow formId");
    }
  }

  public ExternalService getExternalService(CallingContext cc) throws ODKEntityNotFoundException, ODKFormNotFoundException, ODKOverQuotaException, ODKDatastoreException {
    IForm form = FormFactory.retrieveFormByFormId(getFormId(), cc);
    return constructExternalService(this, form, cc);
  }

  private static FormServiceCursor relation = null;

  private static synchronized final FormServiceCursor assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      FormServiceCursor relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new FormServiceCursor(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype;  // set static variable only upon success...
    }
    return relation;
  }

  public static final FormServiceCursor createFormServiceCursor(IForm form,
      ExternalServiceType type, CommonFieldsBase service, CallingContext cc)
      throws ODKDatastoreException {
    FormServiceCursor relation = assertRelation(cc);

    FormServiceCursor c = cc.getDatastore().createEntityUsingRelation(relation, cc.getCurrentUser());

    c.setUriMd5FormId(form.getEntityKey().getKey());
    c.setAuriService(service.getUri());
    c.setFormId(form.getFormId());
    c.setServiceClassname(type);

    return c;
  }

  /**
   * Implement the generic deletion of an ExternalService task here.
   * This needs to be within a TaskLock to prevent a concurrent update of the
   * ExternalServiceCursor from leaving the ExternalServiceCursor object in the
   * database while the underlying Publisher records
   * (e.g., FusionTable2ParameterTable, GoogleSpreadsheet2ParameterTable)
   * are deleted.
   *
   * @param service
   * @param cc
   * @return true if the deletion was successful
   * @throws ODKDatastoreException
   */
  public static final boolean deleteExternalServiceTask(ExternalService service, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    String uriExternalService = service.getFormServiceCursor().getUri();
    TaskLock taskLock = ds.createTaskLock(user);
    String pLockId = UUID.randomUUID().toString();
    boolean deleted = false;
    try {
      if (taskLock.obtainLock(pLockId, uriExternalService, TaskLockType.UPLOAD_SUBMISSION)) {
        taskLock = null;
        service.delete(cc);
        deleted = true;
      }
    } catch (ODKTaskLockException e1) {
      e1.printStackTrace();
    } finally {
      if (!deleted) {
        Log logger =LogFactory.getLog(FormServiceCursor.class);
        logger.error("Unable to delete FormServiceCursor: " + service.getFormServiceCursor().getUri());
      }
    }
    taskLock = ds.createTaskLock(user);
    try {
      for (int i = 0; i < 10; i++) {
        if (taskLock.releaseLock(pLockId, uriExternalService, TaskLockType.UPLOAD_SUBMISSION))
          break;
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
          // just move on, this retry mechanism is to only
          // make things
          // nice
        }
      }
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
    }
    return deleted;
  }

  public static final List<ExternalService> getExternalServicesForForm(IForm form,
      CallingContext cc) throws ODKDatastoreException {
    FormServiceCursor relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, "FormServiceCursor.getExternalServicesForForm[" + form.getFormId() + "]", cc.getCurrentUser());
    // filter on the Form's Uri. We cannot filter on the FORM_ID since it is a
    // Text field in bigtable
    query.addFilter(URI_MD5_FORM_ID_PROPERTY, FilterOperation.EQUAL, form.getEntityKey().getKey());
    query.addSort(URI_MD5_FORM_ID_PROPERTY, Direction.ASCENDING); // gae optimization
    List<ExternalService> esList = new ArrayList<ExternalService>();

    List<? extends CommonFieldsBase> fscList = query.executeQuery();
    for (CommonFieldsBase cb : fscList) {
      FormServiceCursor c = (FormServiceCursor) cb;

      ExternalService obj = constructExternalService(c, form, cc);
      if ( obj != null ) {
    	  esList.add(obj);
      }

    }
    return esList;
  }

  public static final FormServiceCursor getFormServiceCursor(String uri, CallingContext cc) throws ODKEntityNotFoundException, ODKOverQuotaException, ODKDatastoreException {
    try {
      FormServiceCursor relation = assertRelation(cc);
      CommonFieldsBase entity = cc.getDatastore().getEntity(relation, uri, cc.getCurrentUser());
      return (FormServiceCursor) entity;
    } catch (ODKOverQuotaException e) {
      throw e;
    } catch (ODKEntityNotFoundException e) {
      throw e;
    } catch (ODKDatastoreException e) {
      throw e;
    }
  }

  /**
   * Retrieve the list of FormServiceCursor objects that have not been updated
   * more recently than the olderThanDate.  I.e., old ones that might need to
   * be kicked into action.
   *
   * @param olderThanDate
   * @param cc
   * @return
   * @throws ODKEntityNotFoundException
   * @throws ODKOverQuotaException
   */
  public static final List<FormServiceCursor> queryFormServiceCursorRelation(Date olderThanDate,
         CallingContext cc) throws ODKEntityNotFoundException, ODKOverQuotaException {
      List<FormServiceCursor> fscList = new ArrayList<FormServiceCursor>();
      try {
         FormServiceCursor relation = assertRelation(cc);
         Query query = cc.getDatastore().createQuery(relation, "FormServiceCursor.queryFormServiceCursorRelation", cc.getCurrentUser());
         query.addFilter(relation.lastUpdateDate, FilterOperation.LESS_THAN_OR_EQUAL,
               olderThanDate);
         query.addSort(relation.lastUpdateDate, Direction.ASCENDING);
         List<? extends CommonFieldsBase> cfbList = query.executeQuery();
         for (CommonFieldsBase cfb : cfbList) {
            fscList.add((FormServiceCursor) cfb);
         }
      } catch (ODKOverQuotaException e) {
        throw e;
      } catch (ODKDatastoreException e) {
         throw new ODKEntityNotFoundException(e);
      }
      return fscList;
   }

   public static final ExternalService constructExternalService(FormServiceCursor fsc, IForm form,
       CallingContext cc) throws ODKEntityNotFoundException, ODKOverQuotaException {
     try {
       switch (fsc.getExternalServiceType()) {
       case GOOGLE_FUSIONTABLES:
         return new FusionTable(fsc, form, cc);
       case GOOGLE_SPREADSHEET:
         return new GoogleSpreadsheet(fsc, form, cc);
       case JSON_SERVER:
         return new JsonServer(fsc, form, cc);
       case OHMAGE_JSON_SERVER:
         return new OhmageJsonServer(fsc, form, cc);
       case REDCAP_SERVER:
         return new REDCapServer(fsc, form, cc);
       case GOOGLE_MAPS_ENGINE:
       {
    	   // obsolete -- remove the fsc. The parameter table is left as cruft.
    	    Datastore ds = cc.getDatastore();
    	    User user = cc.getCurrentUser();

    	    ds.deleteEntity(fsc.getEntityKey(), user);
       }
    	 return null;
       default:
         return null;
       }
     } catch (ODKOverQuotaException e) {
       throw e;
     } catch (Exception e) {
       throw new ODKEntityNotFoundException("Somehow DB entities for publisher got into problem state", e);
     }
   }
}
