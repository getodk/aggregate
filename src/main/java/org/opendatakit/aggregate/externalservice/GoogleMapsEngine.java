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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.GmePhotoHostType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.aggregate.submission.type.GeoPointSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.utils.HtmlUtil;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.gdata.util.ServiceException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Implementation of publisher into Google Map Engine service.
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class GoogleMapsEngine extends GoogleOauth2ExternalService implements ExternalService {


  private static final Log logger = LogFactory.getLog(GoogleMapsEngine.class.getName());

  private static final String GME_ASSET_ID = "gme_table_id";
  private static final String GME_OAUTH2_SCOPE = "https://www.googleapis.com/auth/mapsengine https://www.googleapis.com/auth/drive";

  // Error Strings
  private static final String FOLDER_ID_ERROR = "Somehow we did not get a folderID to put the images in";
  private static final String OWNER_EMAIL_ERROR = "Somehow we did not get an owner email for the images";
  private static final String BODY_SUPPLIED_ERROR = "Body was supplied for GET or DELETE request";
  private static final String NO_BODY_ERROR = "No body supplied for POST, PATCH or PUT request";
  private static final String NO_GME_ASSET_ID_ERROR = "NO Google Map Engine Asset Id Specified";
  private static final String NO_GEOPOINT_ERROR = "Somehow missing a geopoint element definition";
  private static final String BAD_CRED_UPDATE_ERROR = "Unable to set OperationalStatus to Bad credentials: ";

  /**
   * Datastore entity specific to this type of external service
   */
  private final GoogleMapsEngineParameterTable objectEntity;

  /**
   * The geoPoint field in submission used to locate the submission on google
   * maps
   */
  private final FormElementModel geoPointField;

  /**
   * Common base initialization of a Google Map Engine (both new and existing).
   * 
   * @param entity
   * @param formServiceCursor
   * @param form
   * @param cc
   */
  private GoogleMapsEngine(GoogleMapsEngineParameterTable entity,
      FormServiceCursor formServiceCursor, IForm form, CallingContext cc)
      throws ODKExternalServiceException {
    super(GME_OAUTH2_SCOPE, form, formServiceCursor, new BasicElementFormatter(false, false, false,
        false), null, logger, cc);
    objectEntity = entity;

    String geopointKey = objectEntity.getGeoPointElementKey();
    if (geopointKey == null) {
      throw new ODKExternalServiceException(NO_GEOPOINT_ERROR);
    }

    FormElementKey geoPointFEMKey = new FormElementKey(geopointKey);
    geoPointField = FormElementModel.retrieveFormElementModel(form, geoPointFEMKey);

  }

  /**
   * Continuation of the creation of a brand new Google Map Engine. Needed
   * because entity must be passed into two objects in the constructor.
   * 
   * @param entity
   * @param form
   * @param externalServiceOption
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   * @throws ODKExternalServiceException
   */
  private GoogleMapsEngine(GoogleMapsEngineParameterTable entity, IForm form,
      ExternalServicePublicationOption externalServiceOption, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException,
      ODKExternalServiceException {
    this(entity, createFormServiceCursor(form, entity, externalServiceOption,
        ExternalServiceType.GOOGLE_MAPS_ENGINE, cc), form, cc);
    persist(cc);
  }

  /**
   * Reconstruct a Google Map Engine definition from its persisted
   * representation in the datastore.
   * 
   * @param formServiceCursor
   * @param form
   * @param cc
   * @throws ODKDatastoreException
   * @throws ODKExternalServiceException
   */
  public GoogleMapsEngine(FormServiceCursor formServiceCursor, IForm form, CallingContext cc)
      throws ODKDatastoreException, ODKExternalServiceException {

    this(retrieveEntity(GoogleMapsEngineParameterTable.assertRelation(cc), formServiceCursor, cc),
        formServiceCursor, form, cc);
  }

  /**
   * Create a brand new Google Map Engine
   * 
   * @param form
   * @param externalServiceOption
   * @param ownerUserEmail
   *          -- user that should be granted ownership of the fusionTable
   *          artifact(s)
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   * @throws ODKExternalServiceException
   */
  public GoogleMapsEngine(IForm form, ExternalServicePublicationOption externalServiceOption,
      String gmeAssetId, String geoPointField, GmePhotoHostType gmePhotoHostType,
      String ownerEmail, CallingContext cc) throws ODKEntityPersistException,
      ODKOverQuotaException, ODKDatastoreException, ODKExternalServiceException {
    this(newGmeEntity(gmeAssetId, geoPointField, gmePhotoHostType, ownerEmail, cc), form,
        externalServiceOption, cc);
    persist(cc);
  }

  /**
   * Helper function to create a Google Map Engine parameter table (missing the
   * not-yet-created album identifier and gme identifier).
   * 
   * @param ownerEmail
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  private static final GoogleMapsEngineParameterTable newGmeEntity(String gmeAssetId,
      String geoPointField, GmePhotoHostType gmePhotoHostType, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    GoogleMapsEngineParameterTable t = ds.createEntityUsingRelation(
        GoogleMapsEngineParameterTable.assertRelation(cc), user);
    t.setGmeAssetId(gmeAssetId);
    t.setGeoPointElementKey(geoPointField);
    t.setPhotoHostType(gmePhotoHostType);
    t.setOwnerEmail(ownerEmail);
    return t;
  }

  @Override
  public void initiate(CallingContext cc) throws ODKExternalServiceException,
      ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException {

    if (fsc.isExternalServicePrepared() == null || !fsc.isExternalServicePrepared()) {

      // don't need to create GME because this is done in a separate step
      // (because GME has no programmatic access yet)
      // TODO: when GME creates programmatic access to create the storage,
      // should implement here
      if (objectEntity.getGmeAssetId() == null) {
        throw new ODKExternalServiceException(NO_GME_ASSET_ID_ERROR);
      }

      if (objectEntity.getPhotoHostType() == GmePhotoHostType.GOOGLE_DRIVE) {
        Drive drive = getGoogleDrive();

        // create google drive folder
        String parentFolderId = createGoogleDriverFolder(drive, "images-" + fsc.getFormId());

        // save folder id and transfer ownership to user
        objectEntity.setGoogleDriveFolderId(parentFolderId);
        executeDrivePermission(drive, parentFolderId, objectEntity.getOwnerEmail());
      }

      fsc.setIsExternalServicePrepared(true);
    }
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);

    // upload data to external service
    postUploadTask(cc);

  }

  @Override
  public String getDescriptiveTargetString() {
    return HtmlUtil.createHrefWithProperties("http://mapsengine.google.com", null,
        "Google Map Engine", true);
  }

  @Override
  protected String getOwnership() {
    return objectEntity.getOwnerEmail().substring(EmailParser.K_MAILTO.length());
  }

  @Override
  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return null;
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {

    boolean hasGeoPoint = false;

    GmePhotoHostType photoHost = objectEntity.getPhotoHostType();
    List<SubmissionValue> valuesValues = submission.getSubmissionValues();

    JsonObject feature = new JsonObject();
    feature.addProperty("type", "Feature");

    // find the geopoint
    // if non specified system should skip this submission
    String qualifiedGeoPointName = geoPointField.getGroupQualifiedElementName();
    for (SubmissionValue value : valuesValues) {
      FormElementModel fem = value.getFormElementModel();
      if (qualifiedGeoPointName.equals(fem.getGroupQualifiedElementName())) {
        if (value instanceof GeoPointSubmissionType) {
          GeoPointSubmissionType geoSub = (GeoPointSubmissionType) value;
          GeoPoint geoPoint = geoSub.getValue();
          if(geoPoint.getLatitude() == null || geoPoint.getLatitude() == null) {
            continue;
          }
          JsonArray coord = new JsonArray();
          coord.add(new JsonPrimitive(geoPoint.getLatitude()));
          coord.add(new JsonPrimitive(geoPoint.getLongitude()));
          JsonObject geo = new JsonObject();
          geo.addProperty("type", "Point");
          geo.add("coordinates", coord);
          feature.add("geometry", geo);
          hasGeoPoint = true;
        }
      }
    }

    // check to see if we found a geo point if we did finish construction and
    // start transfer
    // returning should skip the submission (as exception cause retrys)
    if (!hasGeoPoint) {
      return;
    }

    JsonObject properties = new JsonObject();
    properties.addProperty("gx_id", submission.getKey().getKey());
    for (SubmissionValue value : valuesValues) {
      FormElementModel fem = value.getFormElementModel();
      // check to see if it's the geopoint we already used
      if (qualifiedGeoPointName.equals(fem.getGroupQualifiedElementName())) {
        continue;
      }

      FormDataModel fdm = fem.getFormDataModel();
      // check to see if it's unwanted meta data
      if (fdm == null)
        continue;
      String xpath = fdm.getGroupQualifiedXpathElementName();
      if (value instanceof RepeatSubmissionType) {
        // supposed to ignore repeats completely
        continue;
      } else if (value instanceof BlobSubmissionType) {
        try {

          BlobSubmissionType blob = (BlobSubmissionType) value;
          if (photoHost == GmePhotoHostType.GOOGLE_DRIVE) {
            File image = insertPhotoIntoGoogleDrive(blob, cc);
            if (image == null) {
              logger.error("Unable to insert data into google drive because got null back ");
            } else {
              properties.addProperty(xpath, generateGoogleDriveUrl(image));
            }
          } else if (photoHost == GmePhotoHostType.AGGREGATE) {
            String url = generateAggregateImgUrl(blob, cc);
            properties.addProperty(xpath, url);
          } else {
            throw new ODKExternalServiceException("Somehow did not get a valid gme photo host type");
          }

        } catch (ODKDatastoreException e) {
          logger.error("Unable to insert data into google drive because of Database error "
              + form.getFormId() + " exception: " + e.getMessage());
        }
      } else {
        String valueStr = getValueAsString(value, submission, cc);
        properties.addProperty(xpath, valueStr);
      }

    }

    // finish formatting
    feature.add("properties", properties);

    JsonArray featureSet = new JsonArray();
    featureSet.add(feature);

    JsonObject json = new JsonObject();
    json.add("features", featureSet);

    String statement = json.toString();
    System.out.println(statement);
    try {
      executeGMEStmt(POST, statement, cc);
    } catch (ODKExternalServiceCredentialsException e) {
      fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
      try {
        persist(cc);
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new ODKExternalServiceException(BAD_CRED_UPDATE_ERROR + e1);
      }
      throw e;
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }

  private String getValueAsString(SubmissionValue value, Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    // TODO: this needs to change if you only want the single value this is WAY
    // TOO complex
    Row row = new Row(submission.constructSubmissionKey(submission.getFormElementModel()));
    try {
      value.formatValue(formatter, row, null, cc);
      List<String> rowValues = row.getFormattedValues();
      if (rowValues.size() == 1) {
        return rowValues.get(0);
      } else {
        throw new ODKExternalServiceException("Somehow got multiple values in the row");
      }
    } catch (ODKDatastoreException e) {
      logger.error("Unable to properly format the submission value");
      throw new ODKExternalServiceException(e);
    }
  }

  private File insertPhotoIntoGoogleDrive(BlobSubmissionType blob, CallingContext cc)
      throws ODKExternalServiceException, ODKDatastoreException {
    // these conditions must be met for there to be a image
    if (blob == null || (blob.getAttachmentCount(cc) == 0) || (blob.getContentHash(1, cc) == null)) {
      return null;
    }

    byte[] imageBlob = null;
    String contentType = blob.getContentType(1, cc);
    String filename = blob.getUnrootedFilename(1, cc);

    // verify the binary content is an image, if not return no image uploaded
    if (!(HtmlConsts.RESP_TYPE_IMAGE_JPEG.equals(contentType) || HtmlConsts.RESP_TYPE_IMAGE_PNG
        .equals(contentType))) {
      return null;
    }

    if (blob.getAttachmentCount(cc) == 1) {
      imageBlob = blob.getBlob(1, cc);
    }

    if (imageBlob != null && imageBlob.length > 0) {
      String ownerEmail = objectEntity.getOwnerEmail();
      if (ownerEmail == null) {
        throw new ODKExternalServiceException(OWNER_EMAIL_ERROR);
      }

      String folderId = objectEntity.getGoogleDriveFolderId();
      if (folderId == null) {
        throw new ODKExternalServiceException(FOLDER_ID_ERROR);
      }

      ParentReference folder = new ParentReference();
      folder.setId(folderId);

      List<ParentReference> parents = new ArrayList<ParentReference>();
      parents.add(folder);

      File picture = new File();
      picture.setTitle(filename);
      picture.setOriginalFilename(filename);
      picture.setMimeType(contentType);
      picture.setParents(parents);

      Drive drive = getGoogleDrive();
      InputStreamContent photoSource = new InputStreamContent(contentType,
          new ByteArrayInputStream(imageBlob));

      try {
        picture = drive.files().insert(picture, photoSource).execute();
        drive.permissions().insert(picture.getId(), generatePublicPermission()).execute();

      } catch (IOException e) {
        e.printStackTrace();
        throw new ODKExternalServiceException(e);
      }

      return picture;
    }
    return null;
  }

  public static String parseGmeAssetId(IForm form, CallingContext cc) throws ODKDatastoreException,
      UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
    String gmeAssetId = null;

    String formXml = form.getFormXml(cc);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringComments(true);
    factory.setCoalescing(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder
        .parse(new ByteArrayInputStream(formXml.getBytes(HtmlConsts.UTF8_ENCODE)));

    Element rootXml = doc.getDocumentElement();
    NodeList instance = rootXml.getElementsByTagName("instance");

    for (int i = 0; i < instance.getLength(); ++i) {
      Node possibleInstanceNode = instance.item(i);
      NodeList possibleFormRootValues = possibleInstanceNode.getChildNodes();
      for (int j = 0; j < possibleFormRootValues.getLength(); ++j) {
        Node node = possibleFormRootValues.item(j);
        if (node instanceof Element) {
          Element possibleFormRoot = (Element) node;
          // extract form id
          String formId = possibleFormRoot.getAttribute("id");
          // if odk id is not present use namespace
          if (formId.equalsIgnoreCase(BasicConsts.EMPTY_STRING)) {
            String schema = possibleFormRoot.getAttribute("xmlns");
            if (!formId.equalsIgnoreCase(BasicConsts.EMPTY_STRING)) {
              formId = schema;
            }

          }

          // if form id is present correct node, therefore extract gme asset id
          if (form.getFormId().equals(formId)) {
            gmeAssetId = possibleFormRoot.getAttribute(GME_ASSET_ID);
          }
        }
      }

    }
    return gmeAssetId;
  }

  private String executeGMEStmt(String method, String statement, CallingContext cc)
      throws ServiceException, IOException, ODKExternalServiceException, GeneralSecurityException {

    if (statement == null && (POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new ODKExternalServiceException(NO_BODY_ERROR);
    } else if (statement != null
        && !(POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new ODKExternalServiceException(BODY_SUPPLIED_ERROR);
    }

    HttpContent entity = null;
    if (statement != null) {

      // the alternative -- using ContentType.create(,) throws an exception???
      // entity = new StringEntity(statement, "application/json", UTF_8);

      // NOTE: by using HtmlConsts versus "application/json" we now include the
      // UTF-8 in the type
      // could cause problems so place to look for error
      entity = new ByteArrayContent(HtmlConsts.RESP_TYPE_JSON,
          statement.getBytes(HtmlConsts.UTF8_ENCODE));

    }

    HttpRequest request = requestFactory.buildRequest(method, generateGmeUrl(), entity);
    request.setThrowExceptionOnExecuteError(false);

    HttpResponse resp = request.execute();
    String response = WebUtils.readGoogleResponse(resp);
    int statusCode = resp.getStatusCode();

    switch (statusCode) {
    case HttpServletResponse.SC_CONFLICT:
      logger.warn("GME CONFLICT DETECTED" + response + statement);
    case HttpServletResponse.SC_OK:
    case HttpServletResponse.SC_NO_CONTENT:
      return response;
    case HttpServletResponse.SC_FORBIDDEN:
    case HttpServletResponse.SC_UNAUTHORIZED:
      throw new ODKExternalServiceCredentialsException(response + statement);
    default:
      throw new ODKExternalServiceException(response + statement);
    }

  }

  /*
   * ********************
   * 
   * HELPER FUNCTIONS
   * 
   * ********************
   */

  private GenericUrl generateGmeUrl() {
    GenericUrl url = new GenericUrl("https://www.googleapis.com/mapsengine/v1/tables/"
        + objectEntity.getGmeAssetId() + "/features/batchInsert");
    return url;
  }

  private Permission generatePublicPermission() {
    Permission publicPermission = new Permission();
    publicPermission.setKind("drive#permission");
    publicPermission.setRole("reader");
    publicPermission.setType("anyone");
    publicPermission.setValue("");
    return publicPermission;
  }

  private String generateAggregateImgUrl(BlobSubmissionType blob, CallingContext cc) {
    SubmissionKey key = blob.getValue();
    Map<String, String> linkProps = new HashMap<String, String>();
    linkProps.put(ServletConsts.BLOB_KEY, key.toString());
    return HtmlUtil.createLinkWithProperties(cc.getServerURL() + BasicConsts.FORWARDSLASH
        + BinaryDataServlet.ADDR, linkProps);
  }

  private String generateGoogleDriveUrl(File file) {
    return "https://docs.google.com/file/d/" + file.getId();
  }

  private String createGoogleDriverFolder(Drive drive, String folderName)
      throws ODKExternalServiceException {
    ParentReference root = new ParentReference();
    root.setId("root");

    List<ParentReference> parents = new ArrayList<ParentReference>();
    parents.add(root);

    File folder = new File();
    folder.setTitle(folderName);
    folder.setParents(parents);
    folder.setMimeType("application/vnd.google-apps.folder");

    try {
      folder = drive.files().insert(folder).execute();
      return folder.getId();
    } catch (IOException e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }

}
