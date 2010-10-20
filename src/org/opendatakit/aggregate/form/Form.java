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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

/**
 * Persistable definition of the XForm that defines how to store submissions to
 * the datastore. Includes form elements that specify how to properly convert
 * the data to/from the datastore.
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class Form {
  /**
   * Submission entity
   */
  private Submission objectEntity;

  /**
   * Definition of what the Submission xform is.
   */
  private FormDefinition formDefinition;

  /**
   * Construct a form definition that can be persisted
   * 
   * @param formOdkId
   *          Form's unique ODK id
   * @param viewableName
   *          Name to be displayed
   * @param user
   *          User that created the form
   * @param formDefinition
   *          The xml definition or the form
   * @param fileName
   *          The name of xml used for outputting form
   * @param datastore
   *          datastore to access
   * @throws ODKEntityPersistException
   */
  public Form(InstanceDataBase formInfo, Datastore datastore, User user) throws ODKIncompleteSubmissionData, ODKDatastoreException {
    this(new Submission(formInfo, FormInfo.getFormDefinition(datastore), datastore, user), datastore, user);
    StringSubmissionType formId = (StringSubmissionType) objectEntity.getElementValue(FormInfo.formId);
    formDefinition = FormDefinition.getFormDefinition(formId.getValue(), datastore, user);
  }

  public Form(Submission submission, Datastore datastore, User user) throws ODKDatastoreException {
    objectEntity = submission;
    StringSubmissionType formId = (StringSubmissionType) objectEntity.getElementValue(FormInfo.formId);
    formDefinition = FormDefinition.getFormDefinition(formId.getValue(), datastore, user);
  }

  public void persist(Datastore datastore, User user) throws ODKDatastoreException {
    objectEntity.persist(datastore, user);
    
    // TODO: redo this further after mitch's list of key changes
    
    // Should remoteServers ever be persisted?
  }

  /**
   * Deletes the Form including FormElements and Remote Services
   * 
   * @param ds
   *          Datastore
   * @throws ODKDatastoreException
   */
  public void deleteForm(Datastore ds, User user) throws ODKDatastoreException {
	  // TODO: Waylon -- I would delegate the details to the external service
	  // for cleanup.  Not do it here.  Not sure about overhead to clean things up...
//    List<ExternalService> remoteServers = getRemoteServers();
//    for (ExternalService rs : remoteServers) {
//      EntityKey rsEntityKey = rs.getEntityKey();
//      ds.deleteEntity(rsEntityKey, uriUser);
//    }
	  
    List<EntityKey> eks = new ArrayList<EntityKey>();
    // delete everything in formInfo
    objectEntity.recursivelyAddEntityKeys(eks);
    // delete everything in the formDataModel
    for ( FormDataModel m : formDefinition.uriMap.values() ) {
		eks.add(new EntityKey(m.getBackingObjectPrototype(), m.getUri()));
    }
    ds.deleteEntities(eks, user);
  }

  /**
   * Get the datastore key that uniquely identifies the form entity
   * 
   * @return datastore key
   */
  public EntityKey getKey() {
    return objectEntity.getKey();
  }

  public EntityKey getEntityKey() {
	  return objectEntity.getKey();
  }
  
  public FormDataModel getTopLevelGroup() {
	  return formDefinition.getTopLevelGroup();
  }
  
  public FormDefinition getFormDefinition() {
	  return formDefinition;
  }
  
  /**
   * Get the ODK identifier that identifies the form
   * 
   * @return odk identifier
   */
  public String getFormId() {
    return formDefinition.getFormId();
  }

  /**
   * Get the name that is viewable on ODK Aggregate
   * 
   * @return viewable name
   */
  public String getViewableName() {
    return formDefinition.getFormName();
  }

  /**
   * Get the date the form was created
   * 
   * @return creation date
   */
  public Date getCreationDate() {
    return objectEntity.getCreationDate();
  }

  /**
   * Get the last date the form was updated
   * 
   * @return last date form was updated
   */
  public Date getUpdateDate() {
    return objectEntity.getLastUpdateDate();
  }

  /**
   * Get the user who uploaded/created the form
   * 
   * @return user name
   */
  public String getCreationUser() {
    return objectEntity.getCreatorUriUser();
  }

  /**
   * Get the file name to be used when generating the XML file describing from
   * 
   * @return xml file name
   */
  public String getFormFilename() throws ODKDatastoreException {
	  BlobSubmissionType bt = (BlobSubmissionType) objectEntity.getElementValue(FormInfo.formBinaryContent);
	  
	  int count = bt.getAttachmentCount();
	  // we use ordinal counting here: 1..count
	  for ( int i = 1 ; i <= count ; ++i ) {
		  String version = bt.getCurrentVersion(i);
		  if ( version == null ) continue;
		  String contentType = bt.getContentType(i, version);
		  String unrootedFileName = bt.getUnrootedFilename(i);
		  if ( contentType.equals("text/xml") && !unrootedFileName.contains("/")) {
			  return unrootedFileName;
		  }
	  }
	  throw new IllegalStateException("unable to locate the form definition");
  }

  /**
   * Get the original XML that specified the form
   * 
   * @return get XML definition of XForm
   */
  public String getFormXml() throws ODKDatastoreException {
	  BlobSubmissionType bt = (BlobSubmissionType) objectEntity.getElementValue(FormInfo.formBinaryContent);
	  
	  int count = bt.getAttachmentCount();
	  // we use ordinal counting here: 1..count
	  for ( int i = 1 ; i <= count ; ++i ) {
		  String version = bt.getCurrentVersion(i);
		  if ( version == null ) continue;
		  String contentType = bt.getContentType(i, version);
		  String unrootedFileName = bt.getUnrootedFilename(i);
		  if ( contentType.equals("text/xml") && !unrootedFileName.contains("/")) {
			  byte[] byteArray = bt.getBlob(i, version);
			  return new String(byteArray);
		  }
	  }
	  throw new IllegalStateException("unable to locate the form definition");
  }

  /**
   * Gets whether the form can be downloaded
   * 
   * @return true if form can be downloaded, false otherwise
   */
  public Boolean getDownloadEnabled() {
    return ((BooleanSubmissionType) objectEntity.getElementValue(FormInfo.downloadEnabled)).getValue();
  }

  /**
   * Sets a boolean value of whether the form can be downloaded
   * 
   * @param downloadEnabled
   *          set to true if form can be downloaded, false otherwise
   * 
   */
  public void setDownloadEnabled(Boolean downloadEnabled) {
    ((BooleanSubmissionType) objectEntity.getElementValue(FormInfo.downloadEnabled)).setBooleanValue(downloadEnabled);
  }

  /**
   * Gets whether a new submission can be received
   * 
   * @return true if a new submission can be received, false otherwise
   */
  public Boolean getSubmissionEnabled() {
    return ((BooleanSubmissionType) objectEntity.getElementValue(FormInfo.submissionEnabled)).getValue();
  }

  /**
   * Sets a boolean value of whether a new submission can be received
   * 
   * @param submissionEnabled
   *          set to true if a new submission can be received, false otherwise
   * 
   */
  public void setSubmissionEnabled(Boolean submissionEnabled) {
    ((BooleanSubmissionType) objectEntity.getElementValue(FormInfo.submissionEnabled)).setBooleanValue(submissionEnabled);
  }

  public FormDataModel getBeginningElement(String elementName) {
	  return formDefinition.getTopLevelGroup();
  }

  public FormDataModel getFormElement(String name) {
	return getFormElementHelper(formDefinition.getTopLevelGroup(), name);
  }
  
  private FormDataModel getFormElementHelper(FormDataModel node, String name) {
    if (node == null) {
      return null;
    }

    if (node.getElementName().equals(name)) {
      return node;
    }

    List<FormDataModel> children = node.getChildren();
    if (children != null) {
      for (FormDataModel child : children) {
        getFormElementHelper(child, name);
      }
    }
    return null;
  }
  
  /**
   * Prints the data element definitions to the print stream specified
   * 
   * @param out
   *          Print stream to send the output to
   */
  public void printDataTree(PrintStream out) {
    printTreeHelper(formDefinition.getTopLevelGroup(), out);
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
  private void printTreeHelper(FormDataModel node, PrintStream out) {
    if (node == null) {
      return;
    }
    out.println(node.toString());
    List<FormDataModel> children = node.getChildren();
    if (children == null) {
      return;
    }
    for (FormDataModel child : children) {
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
    return (objectEntity == null ? (other.objectEntity == null) : (objectEntity
            .equals(other.objectEntity)));
    // TODO: do we care about external services?
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if (objectEntity != null)
      hashCode += objectEntity.hashCode();
    // TODO: do we care about external services?
    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getViewableName();
  }

  /**
   * Static function to retrieve a form with the specified ODK id from the
   * datastore
   * 
   * @param submissionKey
   *          The ODK identifier that identifies the form
   * 
   * @return The ODK aggregate form definition/conversion object
   * 
   * @throws ODKFormNotFoundException
   *           Thrown when a form was not able to be found with the
   *           corresponding ODK ID
   */
  public static Form retrieveForm(String submissionKey, Datastore ds, User user, Realm realm) throws ODKFormNotFoundException {

    // TODO: consider using memcache to have form info in memory for
    // faster response times

    if (submissionKey == null) {
      return null;
    }
    String formId = FormDefinition.extractWellFormedFormId(submissionKey, realm);
    FormDefinition fd = FormDefinition.getFormDefinition(formId, ds, user);
    if ( fd == null ) {
    	throw new ODKFormNotFoundException("No data model for form");
    }
    
    try {
    	Query query = ds.createQuery(FormInfo.reference, user);
    	query.addFilter(FormInfo.formId.getBackingKey(), FilterOperation.EQUAL, fd.getFormId());
    	List<? extends CommonFieldsBase> eList = query.executeQuery(0);
    	if ( eList.size() == 1 ) {
    		InstanceDataBase formEntity = (InstanceDataBase) eList.get(0);
    		Form form = new Form(formEntity, ds, user);
    		return form;
    	} else if ( eList.size() > 1 ) {
    		throw new IllegalStateException("more than one FormInfo entry for the given form id: " + fd.getFormId());
    	}
    } catch (Exception e) {
      throw new ODKFormNotFoundException(e);
    }
    throw new ODKFormNotFoundException("No FormInfo record for the given form id: " + formId);
  }
}
