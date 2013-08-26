/*
 * Copyright (C) 2013 University of Washington
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

import org.opendatakit.aggregate.constants.common.GmePhotoHostType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Parameter table for publisher into Google Map Engine service.
 *
 * @author wbrunette@gmail.com
 *
 */
public class GoogleMapsEngineParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_google_map_engine";

  private static final DataField GME_ASSET_ID_PROPERTY = new DataField("GME_ASSET_ID",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField GEO_POINT_ELEMENT_KEY_PROPERTY = new DataField("GEO_POINT_ELEMENT_KEY",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField PHOTO_HOST_TYPE_PROPERTY = new DataField("PHOTO_HOST_TYPE",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField GOOGLE_DRIVE_FOLDER_ID_PROPERTY = new DataField("DRIVE_FOLDER_ID",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField OWNER_EMAIL_PROPERTY = new DataField("OWNER_EMAIL",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField READY_PROPERTY = new DataField("READY",
      DataField.DataType.BOOLEAN, true);

  /**
   * Construct a relation prototype. Only called via
   * {@link #assertRelation(CallingContext)}
   *
   * @param databaseSchema
   * @param tableName
   */
  GoogleMapsEngineParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(GME_ASSET_ID_PROPERTY);
    fieldList.add(GEO_POINT_ELEMENT_KEY_PROPERTY);
    fieldList.add(PHOTO_HOST_TYPE_PROPERTY);
    fieldList.add(GOOGLE_DRIVE_FOLDER_ID_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
    fieldList.add(READY_PROPERTY);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   *
   * @param ref
   * @param user
   */
  private GoogleMapsEngineParameterTable(GoogleMapsEngineParameterTable ref, User user) {
    super(ref, user);
  }

  // Only called from within the persistence layer.
  @Override
  public GoogleMapsEngineParameterTable getEmptyRow(User user) {
    return new GoogleMapsEngineParameterTable(this, user);
  }

  public String getGmeAssetId() {
    return getStringField(GME_ASSET_ID_PROPERTY);
  }

  public void setGmeAssetId(String value) {
    if (!setStringField(GME_ASSET_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow gme asset id");
    }
  }

  public String getGoogleDriveFolderId() {
    return getStringField(GOOGLE_DRIVE_FOLDER_ID_PROPERTY);
  }

  public void setGoogleDriveFolderId(String value) {
    if (!setStringField(GOOGLE_DRIVE_FOLDER_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow google drive folder id");
    }
  }

  public String getGeoPointElementKey() {
    return getStringField(GEO_POINT_ELEMENT_KEY_PROPERTY);
  }

  public void setGeoPointElementKey(String value) {
    if (!setStringField(GEO_POINT_ELEMENT_KEY_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow geo point element key");
    }
  }

  public GmePhotoHostType getPhotoHostType() {
    String typeString = getStringField(PHOTO_HOST_TYPE_PROPERTY);
    return GmePhotoHostType.valueOf(typeString);
  }

  public void setPhotoHostType(GmePhotoHostType value) {
    if (!setStringField(PHOTO_HOST_TYPE_PROPERTY, value.name())) {
      throw new IllegalArgumentException("overflow gme host type");
    }
  }

  public String getOwnerEmail() {
    return getStringField(OWNER_EMAIL_PROPERTY);
  }

  public void setOwnerEmail(String value) {
    if (!setStringField(OWNER_EMAIL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow ownerEmail");
    }
  }

  public Boolean getReady() {
    return getBooleanField(READY_PROPERTY);
  }

  public void setReady(Boolean value) {
    setBooleanField(READY_PROPERTY, value);
  }

  private static GoogleMapsEngineParameterTable relation = null;

  public static synchronized final GoogleMapsEngineParameterTable assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      GoogleMapsEngineParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      relationPrototype = new GoogleMapsEngineParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

}
