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

package org.opendatakit.aggregate.form;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Persistable definition of the XForm that defines how to store submissions to
 * the datastore. Includes form elements that specify how to properly convert
 * the data to/from the datastore.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class Form {

  private static final Log logger = LogFactory.getLog(Form.class);

  public static final Long MAX_FORM_ID_LENGTH = PersistConsts.GUARANTEED_SEARCHABLE_LEN;

  private static final class FormCache {
    final long timestamp;
    final Form form;

    FormCache(long timestamp, Form form) {
      this.timestamp = timestamp;
      this.form = form;
    }
  }

  private static final Map<String, FormCache> cache = new HashMap<String, FormCache>();

  /**
   * Common private static method through which all Form objects are obtained.
   * This provides a cache of the form data.  If known, the top-level object's
   * row object is passed in.  This is a database access optimization (minimize
   * GAE billing).
   *  
   * @param topLevelAuri
   * @param infoRow
   * @param cc
   * @return
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  private static synchronized Form getForm(String topLevelAuri, FormInfoTable infoRow, CallingContext cc)
      throws ODKOverQuotaException, ODKDatastoreException {
    FormCache c = cache.get(topLevelAuri);
    if (c != null && c.timestamp + PersistConsts.MAX_SETTLE_MILLISECONDS > System.currentTimeMillis()) {
      // TODO: This cache should reside in MemCache.  Right now, different running
      // servers might see different Form definitions for up to the settle time.
      //
      // Since the datastore is treated as having a settle time of MAX_SETTLE_MILLISECONDS,
      // we should rely on the cache for that time interval.  Without MemCache-style
      // support, this is somewhat problematic since different server instances might
      // see different versions of the same Form.
      // 
      logger.info("FormCache: using cached Form: " + topLevelAuri);
      return c.form;
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    FormInfoTable infoRelation = FormInfoTable.assertRelation(cc);

    if ( infoRow == null ) {
      infoRow = ds.getEntity(infoRelation, topLevelAuri, user);
    }

    logger.info("FormCache: inserting Form: " + topLevelAuri);
    Form f = new Form(infoRow, cc);
    FormCache fc = new FormCache(System.currentTimeMillis(), f);
    cache.put(topLevelAuri, fc);
    return f;
  }

  private static synchronized void clearForm(String topLevelAuri) {
    cache.remove(topLevelAuri);
  }

  /*
   * Following public fields are valid after the first successful call to
   * getFormDefinition()
   */

  private boolean newObject;

  private final FormInfoTable infoRow;

  private final FormInfoFilesetTable filesetRow;

  private final BinaryContentManipulator xform;

  private final BinaryContentManipulator manifest;

  /**
   * Definition of the database representation of the form submission.
   */
  private final FormDefinition formDefinition;

  /**
   * NOT persisted
   */
  private Map<String, FormElementModel> repeatElementMap;

  private Form(FormInfoTable infoRow, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    
    this.infoRow = infoRow;
    String topLevelAuri = infoRow.getUri();
    
    newObject = false;

    Query q;
    List<? extends CommonFieldsBase> rows;

    {
      // get fileset (for now, zero or one record)
      FormInfoFilesetTable filesetRelation = FormInfoFilesetTable.assertRelation(cc);
      q = ds.createQuery(filesetRelation, "Form.constructor", user);
      q.addFilter(filesetRelation.topLevelAuri, FilterOperation.EQUAL, topLevelAuri);

      rows = q.executeQuery();
      if (rows.size() == 0) {
        filesetRow = ds.createEntityUsingRelation(filesetRelation, user);
        filesetRow.setTopLevelAuri(topLevelAuri);
        filesetRow.setParentAuri(topLevelAuri);
        filesetRow.setOrdinalNumber(1L);
      } else if (rows.size() == 1) {
        filesetRow = (FormInfoFilesetTable) rows.get(0);
      } else {
        throw new IllegalStateException("more than one fileset!");
      }
    }

    this.xform = FormInfoFilesetTable.assertXformManipulator(topLevelAuri, filesetRow.getUri(), cc);
    xform.refreshFromDatabase(cc);

    this.manifest = FormInfoFilesetTable.assertManifestManipulator(topLevelAuri,
        filesetRow.getUri(), cc);
    manifest.refreshFromDatabase(cc);

    XFormParameters p = new XFormParameters(infoRow.getStringField(FormInfoTable.FORM_ID),
        filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION),
        filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION));

    formDefinition = FormDefinition.getFormDefinition(p, cc);
  }

  private Form(XFormParameters rootElementDefn, boolean isEncryptedForm, boolean isDownloadEnabled,
      byte[] xmlBytes, String title, CallingContext cc) throws ODKDatastoreException,
      ODKConversionException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    FormInfoTable infoRelation = FormInfoTable.assertRelation(cc);

    String formUri = CommonFieldsBase.newMD5HashUri(rootElementDefn.formId);

    Date now = new Date();
    infoRow = ds.createEntityUsingRelation(infoRelation, user);
    infoRow.setStringField(infoRow.primaryKey, formUri);
    infoRow.setSubmissionDate(now);
    infoRow.setMarkedAsCompleteDate(now);
    infoRow.setIsComplete(true);
    infoRow.setModelVersion(1L);
    infoRow.setUiVersion(0L);
    infoRow.setStringField(FormInfoTable.FORM_ID, rootElementDefn.formId);

    newObject = true;

    String topLevelAuri = infoRow.getUri();

    {
      // get fileset (for now, zero or one record)
      FormInfoFilesetTable filesetRelation = FormInfoFilesetTable.assertRelation(cc);
      filesetRow = ds.createEntityUsingRelation(filesetRelation, user);
      filesetRow.setTopLevelAuri(topLevelAuri);
      filesetRow.setParentAuri(topLevelAuri);
      filesetRow.setOrdinalNumber(1L);
      filesetRow.setLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION,
          rootElementDefn.modelVersion);
      filesetRow.setLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION,
          rootElementDefn.uiVersion);
      filesetRow.setBooleanField(FormInfoFilesetTable.IS_ENCRYPTED_FORM, isEncryptedForm);
      filesetRow.setBooleanField(FormInfoFilesetTable.IS_DOWNLOAD_ALLOWED, isDownloadEnabled);
      filesetRow.setStringField(FormInfoFilesetTable.FORM_NAME, title);
    }

    this.xform = FormInfoFilesetTable.assertXformManipulator(topLevelAuri, filesetRow.getUri(), cc);
    xform.setValueFromByteArray(xmlBytes, "text/xml", Long.valueOf(xmlBytes.length),
        title + ".xml", cc);

    this.manifest = FormInfoFilesetTable.assertManifestManipulator(topLevelAuri,
        filesetRow.getUri(), cc);

    formDefinition = FormDefinition.getFormDefinition(rootElementDefn, cc);
  }

  public boolean isNewlyCreated() {
    return newObject;
  }

  public void persist(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(infoRow, user);
    ds.putEntity(filesetRow, user);
    manifest.persist(cc);
    xform.persist(cc);

    if (formDefinition != null) {
      formDefinition.persistSubmissionAssociation(cc);
    }
  }

  /**
   * Deletes the Form including FormElements and Remote Services
   * 
   * @param ds
   *          Datastore
   * @throws ODKDatastoreException
   */
  public void deleteForm(CallingContext cc) throws ODKDatastoreException {
    clearForm(getUri());
    if (formDefinition != null) {
      // delete the data model normally
      formDefinition.deleteDataModel(cc);
    } else {

      XFormParameters p = new XFormParameters(infoRow.getStringField(FormInfoTable.FORM_ID),
          filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION),
          filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION));

      FormDefinition.deleteAbnormalModel(p, cc);
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    // delete everything in formInfo

    manifest.deleteAll(cc);
    xform.deleteAll(cc);
    ds.deleteEntity(filesetRow.getEntityKey(), user);
    ds.deleteEntity(infoRow.getEntityKey(), user);
  }

  /**
   * Get the datastore key that uniquely identifies the form entity
   * 
   * @return datastore key
   */
  public EntityKey getEntityKey() {
    return infoRow.getEntityKey();
  }

  public SubmissionKey getSubmissionKey() {
    return FormInfo.getSubmissionKey(infoRow.getUri());
  }

  public FormElementModel getTopLevelGroupElement() {
    return formDefinition.getTopLevelGroupElement();
  }

  public String getMajorMinorVersionString() {

    Long modelVersion = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION);
    Long uiVersion = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION);
    StringBuilder b = new StringBuilder();
    if (modelVersion != null) {
      b.append(modelVersion.toString());
    }
    if (uiVersion != null) {
      b.append(".");
      b.append(uiVersion.toString());
    }
    return b.toString();
  }

  public boolean hasValidFormDefinition() {
    return (formDefinition != null);
  }

  /**
   * Get the ODK identifier that identifies the form
   * 
   * @return odk identifier
   */
  public String getFormId() {
    return infoRow.getStringField(FormInfoTable.FORM_ID);
  }

  public boolean hasManifestFileset() {
    return manifest.getAttachmentCount() != 0;
  }

  public BinaryContentManipulator getManifestFileset() {
    return manifest;
  }

  /**
   * Get the name that is viewable on ODK Aggregate
   * 
   * @return viewable name
   */
  public String getViewableName() {
    return filesetRow.getStringField(FormInfoFilesetTable.FORM_NAME);
  }

  public String getViewableFormNameSuitableAsFileName() {
    String name = getViewableName();
    return name.replaceAll("[^\\p{L}0-9]", "_"); // any non-alphanumeric is
    // replaced with
    // underscore
  }

  public String getDescription() {
    return filesetRow.getStringField(FormInfoFilesetTable.DESCRIPTION);
  }

  public String getDescriptionUrl() {
    return filesetRow.getStringField(FormInfoFilesetTable.DESCRIPTION_URL);
  }

  /**
   * Get the date the form was created
   * 
   * @return creation date
   */
  public Date getCreationDate() {
    return infoRow.getCreationDate();
  }

  /**
   * Get the last date the form was updated
   * 
   * @return last date form was updated
   */
  public Date getUpdateDate() {
    return infoRow.getLastUpdateDate();
  }

  /**
   * Get the user who uploaded/created the form
   * 
   * @return user name
   */
  public String getCreationUser() {
    return infoRow.getCreatorUriUser();
  }

  public BinaryContentManipulator getXformDefinition() {
    return xform;
  }

  /**
   * Get the file name to be used when generating the XML file describing from
   * 
   * @return xml file name
   */
  public String getFormFilename() throws ODKDatastoreException {
    if (xform.getAttachmentCount() == 1) {
      return xform.getUnrootedFilename(1);
    } else if (xform.getAttachmentCount() > 1) {
      throw new IllegalStateException("Expecting only one fileset record at this time!");
    }
    return null;
  }

  /**
   * Get the original XML that specified the form
   * 
   * @return get XML definition of XForm
   */
  public String getFormXml(CallingContext cc) throws ODKDatastoreException {
    if (xform.getAttachmentCount() == 1) {
      if (xform.getContentHash(1) == null) {
        return null;
      }
      byte[] byteArray = xform.getBlob(1, cc);
      try {
        return new String(byteArray, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        throw new IllegalStateException("UTF-8 charset not supported!");
      }
    } else if (xform.getAttachmentCount() > 1) {
      throw new IllegalStateException("Expecting only one fileset record at this time!");
    }
    return null;
  }

  /**
   * Gets whether the form is encrypted
   * 
   * @return true if form is encrypted, false otherwise
   */
  public Boolean isEncryptedForm() {
    return filesetRow.getBooleanField(FormInfoFilesetTable.IS_ENCRYPTED_FORM);
  }

  /**
   * Gets whether the form can be downloaded
   * 
   * @return true if form can be downloaded, false otherwise
   */
  public Boolean getDownloadEnabled() {
    return filesetRow.getBooleanField(FormInfoFilesetTable.IS_DOWNLOAD_ALLOWED);
  }

  /**
   * Sets a boolean value of whether the form can be downloaded
   * 
   * @param downloadEnabled
   *          set to true if form can be downloaded, false otherwise
   * 
   */
  public void setDownloadEnabled(Boolean downloadEnabled) {
    filesetRow.setBooleanField(FormInfoFilesetTable.IS_DOWNLOAD_ALLOWED, downloadEnabled);
  }

  /**
   * Gets whether a new submission can be received
   * 
   * @return true if a new submission can be received, false otherwise
   */
  public Boolean getSubmissionEnabled() {
    // if the form definition doesn't exist, we can't accept submissions
    // this is a transient condition when in the midst of deleting a form or
    // uploading one
    // and another user attempts to list the available forms.
    if (formDefinition == null)
      return false;
    return formDefinition.getIsSubmissionAllowed();
  }

  /**
   * Sets a boolean value of whether a new submission can be received
   * 
   * @param submissionEnabled
   *          set to true if a new submission can be received, false otherwise
   * 
   */
  public void setSubmissionEnabled(Boolean submissionEnabled) {
    formDefinition.setIsSubmissionAllowed(submissionEnabled);
  }

  private FormElementModel findElementByNameHelper(FormElementModel current, String name) {
    if (current.getElementName().equals(name))
      return current;
    FormElementModel m = null;
    for (FormElementModel c : current.getChildren()) {
      m = findElementByNameHelper(c, name);
      if (m != null)
        break;
    }
    return m;
  }

  /**
   * Relies on getElementName() to determine the match of the FormElementModel.
   * Does a depth-first traversal of the list.
   * 
   * @param name
   * @return the found element or null if not found.
   */
  public FormElementModel findElementByName(String name) {
    return findElementByNameHelper(getTopLevelGroupElement(), name);
  }

  public FormElementModel getFormElementModel(List<SubmissionKeyPart> submissionKeyParts) {
    FormElementModel m = null;
    boolean formIdElement = true;
    for (SubmissionKeyPart p : submissionKeyParts) {
      if (formIdElement) {
        if (!p.getElementName().equals(getFormId())) {
          return null;
        }
        formIdElement = false;
      } else if (m == null) {
        m = getTopLevelGroupElement();
        if (!p.getElementName().equals(m.getElementName())) {
          return null;
        }
      } else {
        boolean found = false;
        for (FormElementModel c : m.getChildren()) {
          if (c.getElementName().equals(p.getElementName())) {
            m = c;
            found = true;
            break;
          }
        }
        if (!found) {
          return null;
        }
      }
    }
    return m;
  }

  private void getRepeatGroupsInModelHelper(FormElementModel current,
      List<FormElementModel> accumulation) {
    for (FormElementModel m : current.getChildren()) {
      if (m.getElementType() == FormElementModel.ElementType.REPEAT) {
        accumulation.add(m);
      }
      getRepeatGroupsInModelHelper(m, accumulation);
    }
  }

  public List<FormElementModel> getRepeatGroupsInModel() {
    List<FormElementModel> list = new ArrayList<FormElementModel>();

    getRepeatGroupsInModelHelper(getTopLevelGroupElement(), list);
    return list;
  }

  public Map<String, FormElementModel> getRepeatElementModels() {

    // check to see if repeatRootMap needs to be created
    // NOTE: this assumes the form does NOT get altered!!!
    if (repeatElementMap == null) {
      repeatElementMap = new HashMap<String, FormElementModel>();
      populateRepeatElementMap(formDefinition.getTopLevelGroupElement());
    }

    return repeatElementMap;
  }

  private void populateRepeatElementMap(FormElementModel node) {
    if (node == null) {
      return;
    }
    if (node.getElementType() == ElementType.REPEAT) {
      // TODO: this should be fully qualified element name or
      // you could get collisions.
      repeatElementMap.put(node.getElementName(), node);
    }
    List<FormElementModel> children = node.getChildren();
    if (children == null) {
      return;
    }
    for (FormElementModel child : children) {
      populateRepeatElementMap(child);
    }
  }

  public FormSummary generateFormSummary(CallingContext cc) {
    boolean submit = getSubmissionEnabled();
    boolean downloadable = getDownloadEnabled();
    Map<String, String> xmlProperties = new HashMap<String, String>();
    xmlProperties.put(ServletConsts.FORM_ID, getFormId());
    xmlProperties.put(ServletConsts.HUMAN_READABLE, BasicConsts.TRUE);

    String viewableURL = HtmlUtil.createHrefWithProperties(
        cc.getWebApplicationURL(FormXmlServlet.WWW_ADDR), xmlProperties, getViewableName());
    int mediaFileCount = getManifestFileset().getAttachmentCount();
    return new FormSummary(getViewableName(), getFormId(), getCreationDate(), getCreationUser(),
        downloadable, submit, viewableURL, mediaFileCount);
  }

  /**
   * Prints the data element definitions to the print stream specified
   * 
   * @param out
   *          Print stream to send the output to
   */
  public void printDataTree(PrintStream out) {
    printTreeHelper(formDefinition.getTopLevelGroupElement(), out);
  }

  /**
   * Recursive helper function that prints the data elements definitions to the
   * print stream specified
   * 
   * @param node
   *          node to be processed
   * @param out
   *          Print stream to send the output to
   */
  private void printTreeHelper(FormElementModel node, PrintStream out) {
    if (node == null) {
      return;
    }
    out.println(node.toString());
    List<FormElementModel> children = node.getChildren();
    if (children == null) {
      return;
    }
    for (FormElementModel child : children) {
      printTreeHelper(child, out);
    }
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Form)) {
      return false;
    }
    Form other = (Form) obj;
    if (infoRow == null)
      return (other.infoRow == null);

    return (infoRow.getUri().equals(other.infoRow.getUri()));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if (infoRow != null)
      hashCode += infoRow.getUri().hashCode();
    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getViewableName();
  }

  public static final List<Form> getForms(boolean checkAuthorization, CallingContext cc)
      throws ODKOverQuotaException, ODKDatastoreException {

    FormInfoTable relation = FormInfoTable.assertRelation(cc);

    // ensure that Form table exists...
    List<Form> forms = new ArrayList<Form>();

    Query formQuery = cc.getDatastore().createQuery(relation, "Form.getForms", cc.getCurrentUser());
    List<? extends CommonFieldsBase> infoRows = formQuery.executeQuery();

    for (CommonFieldsBase cb : infoRows) {
      FormInfoTable infoRow = (FormInfoTable) cb;
      Form form = getForm(cb.getUri(), infoRow, cc);
      // TODO: authorization check?
      forms.add(form);
    }
    return forms;
  }

  /**
   * Called during the startup action to load the Form table and eventually
   * handle migrations of forms from older table formats to newer ones.
   * 
   * @param cc
   * @throws ODKDatastoreException
   */
  public static final void initialize(CallingContext cc) throws ODKDatastoreException {
  }

  /**
   * Clean up the incoming string to extract just the formId from it.
   * 
   * @param submissionKey
   * @return
   */
  public static final String extractWellFormedFormId(String submissionKey) {
    int firstSlash = submissionKey.indexOf('/');
    String formId = submissionKey;
    if (firstSlash != -1) {
      // strip off the group path of the key
      formId = submissionKey.substring(0, firstSlash);
    }
    return formId;
  }

  /**
   * Static function to retrieve a form with the specified ODK id from the
   * datastore
   * 
   * @param formId
   *          The ODK identifier that identifies the form
   * 
   * @return The ODK aggregate form definition/conversion object
   * 
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   * @throws ODKFormNotFoundException
   *           Thrown when a form was not able to be found with the
   *           corresponding ODK ID
   */
  public static Form retrieveFormByFormId(String formId, CallingContext cc)
      throws ODKFormNotFoundException, ODKOverQuotaException, ODKDatastoreException {

    if (formId == null) {
      return null;
    }
    try {
      String formUri = CommonFieldsBase.newMD5HashUri(formId);
      Form form = getForm(formUri, null, cc);
      if (!formId.equals(form.getFormId())) {
        throw new IllegalStateException("more than one FormInfo entry for the given form id: "
            + formId);
      }
      return form;
    } catch (ODKOverQuotaException e) {
      throw e;
    } catch (ODKDatastoreException e) {
      throw e;
    } catch (Exception e) {
      throw new ODKFormNotFoundException(e);
    }
  }

  /**
   * Static function to retrieve a form with the specified ODK id from the
   * datastore
   * 
   * @param formId
   *          The ODK identifier that identifies the form
   * 
   * @return The ODK aggregate form definition/conversion object
   * 
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   * @throws ODKFormNotFoundException
   *           Thrown when a form was not able to be found with the
   *           corresponding ODK ID
   */
  public static Form retrieveForm(List<SubmissionKeyPart> parts, CallingContext cc)
      throws ODKOverQuotaException, ODKDatastoreException, ODKFormNotFoundException {

    if (!FormInfo.validFormKey(parts)) {
      return null;
    }

    try {
      String formUri = parts.get(1).getAuri();
      Form form = getForm(formUri, null, cc);
      return form;
    } catch ( ODKOverQuotaException e) {
      throw e;
    } catch ( ODKDatastoreException e) {
      throw e;
    } catch (Exception e) {
      throw new ODKFormNotFoundException(e);
    }
  }

  private BlobSubmissionOutcome isSameForm(XFormParameters rootElementDefn,
      boolean isEncryptedFlag, String title, byte[] xmlBytes, CallingContext cc)
      throws ODKDatastoreException, ODKFormAlreadyExistsException {
    String rootFormId = getFormId();
    Long rootModel = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION);
    Long rootUi = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION);
    Boolean isEncrypted = isEncryptedForm();
    String formName = getViewableName();

    boolean same = rootFormId.equals(rootElementDefn.formId)
        && sameVersion(rootModel, rootUi, rootElementDefn.modelVersion, rootElementDefn.uiVersion)
        && isEncryptedFlag == isEncrypted && title.equals(formName);

    if (!same)
      throw new ODKFormAlreadyExistsException();

    if (xform.getAttachmentCount() == 1) {
      String contentHash = xform.getContentHash(1);
      if (contentHash != null) {
        String md5Hash = CommonFieldsBase.newMD5HashUri(xmlBytes);
        if (!contentHash.equals(md5Hash)) {
          throw new ODKFormAlreadyExistsException();
        } else {
          return BlobSubmissionOutcome.FILE_UNCHANGED;
        }
      } else {
        return xform.setValueFromByteArray(xmlBytes, "text/xml", Long.valueOf(xmlBytes.length),
            title + ".xml", cc);
      }
    } else {
      return xform.setValueFromByteArray(xmlBytes, "text/xml", Long.valueOf(xmlBytes.length), title
          + ".xml", cc);
    }
  }

  /**
   * Create or fetch the given formId.
   * 
   * @param isEncryptedForm
   * @param rootElementDefn
   * 
   * @param formId
   * @param isDownloadEnabled
   * @param xmlBytes
   * @param submissionElementDefn
   * @param ds
   * @param user
   * @return
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   * @throws ODKConversionException
   *           if formId is too long...
   * @throws ODKFormAlreadyExistsException
   */
  public static final Form createOrFetchFormId(XFormParameters rootElementDefn,
      boolean isEncryptedForm, String title, byte[] xmlBytes, boolean isDownloadEnabled,
      CallingContext cc) throws ODKOverQuotaException, ODKDatastoreException, ODKConversionException,
      ODKFormAlreadyExistsException {

    Form thisForm = null;

    String formUri = CommonFieldsBase.newMD5HashUri(rootElementDefn.formId);

    try {
      thisForm = getForm(formUri, null, cc);

      if (thisForm.isSameForm(rootElementDefn, isEncryptedForm, title, xmlBytes, cc) != BlobSubmissionOutcome.NEW_FILE_VERSION) {
        return thisForm;
      }
      throw new ODKFormAlreadyExistsException();
    } catch (ODKEntityNotFoundException e) {
      thisForm = new Form(rootElementDefn, isEncryptedForm, isDownloadEnabled, xmlBytes, title, cc);
    }
    return thisForm;
  }

  public void setIsComplete(Boolean value) {
    infoRow.setIsComplete(value);
  }

  public EntityKey getKey() {
    return infoRow.getEntityKey();
  }

  private static final boolean sameVersion(Long rootModel, Long rootUi, Long rootModelVersion,
      Long rootUiVersion) {
    return ((rootModelVersion == null) ? (rootModel == null)
        : (rootModel != null && rootModelVersion.equals(rootModel)))
        && ((rootUiVersion == null) ? (rootUi == null) : (rootUi != null && rootUiVersion
            .equals(rootUi)));
  }

  /**
   * Media files are assumed to be in a directory one level deeper than the xml
   * definition. So the filename reported on the mime item has an extra leading
   * directory. Strip that off.
   * 
   * @param aFormDefinition
   * @param rootModelVersion
   * @param rootUiVersion
   * @param item
   * @param datastore
   * @param user
   * @return true if the files are completely new or are identical to the
   *         currently-stored ones.
   * @throws ODKDatastoreException
   */
  public boolean setXFormMediaFile(Long rootModelVersion, Long rootUiVersion,
      MultiPartFormItem item, CallingContext cc) throws ODKDatastoreException {
    Long rootModel = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_MODEL_VERSION);
    Long rootUi = filesetRow.getLongField(FormInfoFilesetTable.ROOT_ELEMENT_UI_VERSION);

    if (!sameVersion(rootModel, rootUi, rootModelVersion, rootUiVersion)) {
      throw new ODKDatastoreException("did not find matching FileSet for media file");
    }

    String filePath = item.getFilename();
    if (filePath.indexOf("/") != -1) {
      filePath = filePath.substring(filePath.indexOf("/") + 1);
    }
    boolean matchingFiles = (BlobSubmissionOutcome.NEW_FILE_VERSION != manifest
        .setValueFromByteArray(item.getStream().toByteArray(), item.getContentType(),
            item.getContentLength(), filePath, cc));
    return matchingFiles;
  }

  public String getUri() {
    return infoRow.getUri();
  }

}
