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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Persistable definition of the XForm that defines how to store submissions to
 * the datastore. Includes form elements that specify how to properly convert
 * the data to/from the datastore.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface IForm {
  public static final Long MAX_FORM_ID_LENGTH = PersistConsts.GUARANTEED_SEARCHABLE_LEN;
  
  public void persist(CallingContext cc) throws ODKDatastoreException;

  /**
   * Deletes the Form including FormElements and Remote Services
   * 
   * @param ds
   *          Datastore
   * @throws ODKDatastoreException
   */
  public void deleteForm(CallingContext cc) throws ODKDatastoreException;
  
  /**
   * Get the datastore key that uniquely identifies the form entity
   * 
   * @return datastore key
   */
  public EntityKey getEntityKey();
  
  public SubmissionKey getSubmissionKey();

  public FormElementModel getTopLevelGroupElement();
  
  public XFormParameters getRootElementDefn();

  public String getMajorMinorVersionString();
  
  public String getOpenRosaVersionString();
  
  /**
   * Get the md5 hash of the Form Definition file.
   * 
   * @return
   */
  public String getXFormFileHash(CallingContext cc) throws ODKDatastoreException;
  
  public boolean hasValidFormDefinition();

  /**
   * Get the ODK identifier that identifies the form
   * 
   * @return odk identifier
   */
  public String getFormId();

  public boolean hasManifestFileset(CallingContext cc) throws ODKDatastoreException;

  public BinaryContentManipulator getManifestFileset();

  /**
   * Get the name that is viewable on ODK Aggregate
   * 
   * @return viewable name
   */
  public String getViewableName();
  
  /**
   * Revise the viewable name of a form.
   * 
   * @param title
   */
  public void setViewableName(String title);

  public String getViewableFormNameSuitableAsFileName();
  
  public String getDescription();
  
  public String getDescriptionUrl();

  /**
   * Get the date the form was created
   * 
   * @return creation date
   */
  public Date getCreationDate();

  /**
   * Get the last date the form was updated
   * 
   * @return last date form was updated
   */
  public Date getLastUpdateDate();

  /**
   * Get the user who uploaded/created the form
   * 
   * @return user name
   */
  public String getCreationUser();

  public BinaryContentManipulator getXformDefinition();

  /**
   * Get the file name to be used when generating the XML file describing from
   * 
   * @return xml file name
   */
  public String getFormFilename(CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the original XML that specified the form.
   * 
   * @return get XML definition of XForm
   */
  public String getFormXml(CallingContext cc) throws ODKDatastoreException;
  
  /**
   * Set the XML for this form.
   * Any revision to the value of the uiVersion attribute should already have
   * been performed.
   * 
   * @param formFilename
   * @param formXml
   * @param modelVersion
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobSubmissionOutcome setFormXml( String formFilename, String formXml, Long modelVersion, CallingContext cc ) throws ODKDatastoreException;
  
  /**
   * Return the md5 hash of this XML.
   * 
   * @param cc
   * @return md5 hash
   * @throws ODKDatastoreException
   */
  public String getMd5HashFormXml(CallingContext cc) throws ODKDatastoreException;
  
  /**
   * Gets whether the form is encrypted
   * 
   * @return true if form is encrypted, false otherwise
   */
  public Boolean isEncryptedForm();

  /**
   * Gets whether the form can be downloaded
   * 
   * @return true if form can be downloaded, false otherwise
   */
  public Boolean getDownloadEnabled();

  /**
   * Sets a boolean value of whether the form can be downloaded
   * 
   * @param downloadEnabled
   *          set to true if form can be downloaded, false otherwise
   * 
   */
  public void setDownloadEnabled(Boolean downloadEnabled);

  /**
   * Gets whether a new submission can be received
   * 
   * @return true if a new submission can be received, false otherwise
   */
  public Boolean getSubmissionEnabled();
  /**
   * Sets a boolean value of whether a new submission can be received
   * 
   * @param submissionEnabled
   *          set to true if a new submission can be received, false otherwise
   * 
   */
  public void setSubmissionEnabled(Boolean submissionEnabled);

  /**
   * Relies on getElementName() to determine the match of the FormElementModel.
   * Does a depth-first traversal of the list.
   * 
   * @param name
   * @return the found element or null if not found.
   */
  public FormElementModel findElementByName(String name);

  public FormElementModel getFormElementModel(List<SubmissionKeyPart> submissionKeyParts);


  public List<FormElementModel> getRepeatGroupsInModel();
  
  public Map<String, FormElementModel> getRepeatElementModels();
  
  public FormSummary generateFormSummary(CallingContext cc) throws ODKOverQuotaException, ODKDatastoreException;
  
  /**
   * Prints the data element definitions to the print stream specified
   * 
   * @param out
   *          Print stream to send the output to
   */
  public void printDataTree(PrintStream out);

  public void setIsComplete(Boolean value);

  public EntityKey getKey();

  /**
   * Media files are assumed to be in a directory one level deeper than the xml
   * definition. So the filename reported on the mime item has an extra leading
   * directory. Strip that off.
   * 
   * @param item
   * @param overwriteOK
   * @param cc
   * @return true if a file should be overwritten (updated); false if the file is completely new or unchanged.
   * @throws ODKDatastoreException
   */
  public boolean setXFormMediaFile(MultiPartFormItem item, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException;
  
  public String getUri();
}
