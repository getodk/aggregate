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

import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.StaticAssociationBase;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class FormServiceCursor extends StaticAssociationBase {

  private static final String TABLE_NAME = "_form_service_cursor";
  /*
   * Property Names for datastore
   */
  private static final DataField EXT_SERVICE_TYPE_PROPERTY = new DataField("EXT_SERVICE_TYPE",
      DataField.DataType.STRING, false, 200L);
  private static final DataField EXTERNAL_SERVICE_OPTION = new DataField("EXTERNAL_SERVICE_OPTION",
      DataField.DataType.STRING, false, 80L);
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

  public final DataField externalServiceType;
  public final DataField externalServiceOption;
  public final DataField establishmentDateTime;
  public final DataField uploadCompleted;
  public final DataField lastUploadCursorDate;
  public final DataField lastUploadKey;
  public final DataField lastStreamingCursorDate;
  public final DataField lastStreamingKey;
  public final DataField formId;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
  private FormServiceCursor(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(externalServiceType = new DataField(EXT_SERVICE_TYPE_PROPERTY));
    fieldList.add(externalServiceOption = new DataField(EXTERNAL_SERVICE_OPTION));
    fieldList.add(establishmentDateTime = new DataField(ESTABLISHMENT_DATETIME));
    fieldList.add(uploadCompleted = new DataField(UPLOAD_COMPLETED_PROPERTY));
    fieldList.add(lastUploadCursorDate = new DataField(
        LAST_UPLOAD_CURSOR_DATE_PROPERTY));
    fieldList.add(lastUploadKey = new DataField(LAST_UPLOAD_KEY_PROPERTY));
    fieldList.add(lastStreamingCursorDate = new DataField(
        LAST_STREAMING_CURSOR_DATE_PROPERTY));
    fieldList.add(lastStreamingKey = new DataField(LAST_STREAMING_KEY_PROPERTY));
    fieldList.add(formId = new DataField(FORM_ID_PROPERTY));
  }

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
  private FormServiceCursor(FormServiceCursor ref, User user) {
    super(ref, user);
    externalServiceType = ref.externalServiceType;
    externalServiceOption = ref.externalServiceOption;
    establishmentDateTime = ref.establishmentDateTime;
    uploadCompleted = ref.uploadCompleted;
    lastUploadCursorDate = ref.lastUploadCursorDate;
    lastUploadKey = ref.lastUploadKey;
    lastStreamingCursorDate = ref.lastStreamingCursorDate;
    lastStreamingKey = ref.lastStreamingKey;
    formId = ref.formId;
  }

  // Only called from within the persistence layer.
  @Override
  public FormServiceCursor getEmptyRow(User user) {
	return new FormServiceCursor(this, user);
  }

  public ExternalServiceType getExternalServiceType() {
    String type = getStringField(externalServiceType);
    return ExternalServiceType.valueOf(type);
  }

  public void setServiceClassname(ExternalServiceType value) {
    if (!setStringField(externalServiceType, value.toString())) {
      throw new IllegalArgumentException("overflow externalServiceType");
    }
  }

  public ExternalServiceOption getExternalServiceOption() {
    return ExternalServiceOption.valueOf(getStringField(externalServiceOption));
  }

  public void setExternalServiceOption(ExternalServiceOption value) {
    if (!setStringField(externalServiceOption, value.toString())) {
      throw new IllegalArgumentException("overflow externalServiceOption");
    }
  }

  public Date getEstablishmentDateTime() {
    return getDateField(establishmentDateTime);
  }

  public void setEstablishmentDateTime(Date value) {
    setDateField(establishmentDateTime, value);
  }

  public Boolean getUploadCompleted() {
    return getBooleanField(uploadCompleted);
  }

  public void setUploadCompleted(Boolean value) {
    setBooleanField(uploadCompleted, value);
  }

  public Date getLastUploadCursorDate() {
    return getDateField(lastUploadCursorDate);
  }

  public void setLastUploadCursorDate(Date value) {
    setDateField(lastUploadCursorDate, value);
  }

  public String getLastUploadKey() {
    return getStringField(lastUploadKey);
  }

  public void setLastUploadKey(String value) {
    if (!setStringField(lastUploadKey, value)) {
      throw new IllegalArgumentException("overflow lastUploadKey");
    }
  }

  public Date getLastStreamingCursorDate() {
    return getDateField(lastStreamingCursorDate);
  }

  public void setLastStreamingCursorDate(Date value) {
    setDateField(lastStreamingCursorDate, value);
  }

  public String getLastStreamingKey() {
    return getStringField(lastStreamingKey);
  }

  public void setLastStreamingKey(String value) {
    if (!setStringField(lastStreamingKey, value)) {
      throw new IllegalArgumentException("overflow lastStreamingKey");
    }
  }

  public String getServiceAuri() {
    return getStringField(subAuri);
  }

  public void setServiceAuri(String value) {
    if (!setStringField(subAuri, value)) {
      throw new IllegalArgumentException("overflow serviceAuri");
    }
  }

  public String getFormUri() {
    return getStringField(domAuri);
  }

  public void setFormUri(String value) {
    if (!setStringField(domAuri, value)) {
      throw new IllegalArgumentException("overflow domAuri");
    }
  }

  public String getFormId() {
    return getStringField(formId);
  }

  public void setFormId(String value) {
    if (!setStringField(formId, value)) {
      throw new IllegalArgumentException("overflow formId");
    }
  }
  
  public ExternalService getExternalService(String baseWebServerUrl, Datastore ds, User user) throws ODKEntityNotFoundException {
    return getExternalServiceType().constructExternalService(this, baseWebServerUrl, ds, user);
  }
  
  private static FormServiceCursor relation = null;

  private static final FormServiceCursor createRelation(Datastore ds, User user)
      throws ODKDatastoreException {
    if (relation == null) {
      FormServiceCursor relationPrototype;
      relationPrototype = new FormServiceCursor(ds.getDefaultSchemaName());
      ds.createRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype;  // set static variable only upon success...
    }
    return relation;
  }

  public static final FormServiceCursor createFormServiceCursor(Form form,
      ExternalServiceType type, CommonFieldsBase service, Datastore ds, User user)
      throws ODKDatastoreException {
    FormServiceCursor relationPrototype = createRelation(ds, user);

    FormServiceCursor c = ds.createEntityUsingRelation(relationPrototype, null, user);

    c.setDomAuri(form.getEntityKey().getKey());
    c.setSubAuri(service.getUri());
    c.setFormId(form.getFormId());
    c.setServiceClassname(type);

    return c;
  }
  
  public static final List<ExternalService> getExternalServicesForForm(Form form,
      String baseWebServerUrl, Datastore ds, User user) throws ODKDatastoreException {
    FormServiceCursor relationPrototype = createRelation(ds, user);
    Query query = ds.createQuery(relationPrototype, user);
    // filter on the Form's Uri. We cannot filter on the FORM_ID since it is a
    // Text field in bigtable
    query.addFilter(relationPrototype.domAuri, FilterOperation.EQUAL, form.getEntityKey().getKey());
    List<ExternalService> esList = new ArrayList<ExternalService>();

    List<? extends CommonFieldsBase> fscList = query.executeQuery(0);
    for (CommonFieldsBase cb : fscList) {
      FormServiceCursor c = (FormServiceCursor) cb;
      ExternalService obj;

      obj = c.getExternalServiceType().constructExternalService(c, baseWebServerUrl, ds, user);
      esList.add(obj);

    }
    return esList;
  }

  public static final FormServiceCursor getFormServiceCursor(String uri, Datastore ds, User user) throws ODKEntityNotFoundException {
    try {
      FormServiceCursor relationPrototype = createRelation(ds, user);
      CommonFieldsBase entity = ds.getEntity(relationPrototype, uri, user);
      return (FormServiceCursor) entity;
    } catch (ODKDatastoreException e) {
      throw new ODKEntityNotFoundException(e);
    }
  }
  
   public static final List<FormServiceCursor> queryFormServiceCursorRelation(Date olderThanDate,
         Datastore ds, User user) throws ODKEntityNotFoundException {
      List<FormServiceCursor> fscList = new ArrayList<FormServiceCursor>();
      try {
         FormServiceCursor relationPrototype = createRelation(ds, user);
         Query query = ds.createQuery(relationPrototype, user);
         query.addFilter(relationPrototype.lastUpdateDate, FilterOperation.LESS_THAN_OR_EQUAL,
               olderThanDate);
         query.addSort(relationPrototype.lastUpdateDate, Direction.ASCENDING);
         List<? extends CommonFieldsBase> cfbList = query.executeQuery(0);
         for (CommonFieldsBase cfb : cfbList) {
            fscList.add((FormServiceCursor) cfb);
         }
      } catch (ODKDatastoreException e) {
         throw new ODKEntityNotFoundException(e);
      }
      return fscList;
   }
}
