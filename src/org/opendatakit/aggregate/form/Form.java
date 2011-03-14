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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

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

  /**
   * Submission entity
   */
  private final Submission objectEntity;

  /**
   * Definition of what the Submission xform is.
   */
  private final FormDefinition formDefinition;

  /**
   * NOT persisted
   */
  private Map<String, FormElementModel> repeatElementMap;
  
  // special values for bootstrapping
  public static final String URI_FORM_ID_VALUE_FORM_INFO = "aggregate.opendatakit.org:FormInfo";
 
  public Form(TopLevelDynamicBase formEntity, CallingContext cc) throws ODKEntityNotFoundException, ODKDatastoreException {
		this( new Submission(
				formEntity,
				FormInfo.getFormDefinition(cc), 
				cc),
				cc);
	  }

  /**
   * Retrieve a form definition from the database.
   * 
   * @param topLevelAuri
   * @param datastore
   * @param user
   * @throws ODKEntityNotFoundException
   * @throws ODKDatastoreException
   */
  public Form(String topLevelAuri, CallingContext cc) throws ODKEntityNotFoundException, ODKDatastoreException {
	this( new Submission(
			(FormInfoTable) cc.getDatastore().getEntity(FormInfo.getFormDefinition(cc).getTopLevelGroup().getBackingObjectPrototype(), topLevelAuri, cc.getCurrentUser()),
			FormInfo.getFormDefinition(cc), 
			cc),
			cc);
  }

  Form(Submission submission, CallingContext cc) throws ODKDatastoreException {
    objectEntity = submission;
    formDefinition = FormDefinition.getFormDefinition(getSubmissionXFormParameters(cc), cc);
  }

  public XFormParameters getSubmissionXFormParameters(CallingContext cc) {
		RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiSubmissionTable);
		List<SubmissionSet> submissions = r.getSubmissionSets();
		if ( submissions.size() != 1 ) {
			throw new IllegalStateException("Expecting only one submission record at this time!");
		}
		SubmissionSet submissionRecord = submissions.get(0);
		
		String submissionFormId = ((StringSubmissionType) submissionRecord.getElementValue(FormInfo.submissionFormId)).getValue();
		Long submissionModelVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionModelVersion)).getValue();
		Long submissionUiVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionUiVersion)).getValue();
		XFormParameters submissionDefn = new XFormParameters(submissionFormId, submissionModelVersion, submissionUiVersion);
		return submissionDefn;
  }
  
  public SubmissionAssociationTable getSubmissionAssociation(CallingContext cc) {

	XFormParameters submissionDefn = getSubmissionXFormParameters(cc);

	List<SubmissionAssociationTable> saList = SubmissionAssociationTable.findSubmissionAssociationsForXForm(submissionDefn,cc);
	if ( saList.size() == 0 ) return null;
	if ( saList.size() > 1 ) {
		throw new IllegalStateException("Logic is not yet in place for cross-form submission sharing");
	}
	SubmissionAssociationTable match = saList.get(0);
	return match;
  }
  
  public void persist(CallingContext cc) throws ODKDatastoreException {
    objectEntity.persist(cc);
    
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
  public void deleteForm(CallingContext cc) throws ODKDatastoreException {
	FormDataModel fdm = FormDataModel.assertRelation(cc);
    List<EntityKey> eksFormInfo = new ArrayList<EntityKey>();
    
    RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiSubmissionTable);
	List<SubmissionSet> submissions = r.getSubmissionSets();
	if ( submissions.size() != 1 ) {
		throw new IllegalStateException("Expecting only one submission record at this time!");
	}
	SubmissionSet submissionRecord = submissions.get(0);
	
	String submissionFormId = ((StringSubmissionType) submissionRecord.getElementValue(FormInfo.submissionFormId)).getValue();
	Long submissionModelVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionModelVersion)).getValue();
	Long submissionUiVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionUiVersion)).getValue();
	XFormParameters submissionDefn = new XFormParameters(submissionFormId, submissionModelVersion, submissionUiVersion);

    List<SubmissionAssociationTable> saList = 
    	SubmissionAssociationTable.findSubmissionAssociationsForXForm(submissionDefn, cc);
	if ( saList.size() > 1 ) {
		throw new IllegalStateException("Logic is not yet in place for cross-form submission sharing");
	}
    
    XFormParameters ref = null;
    
    if ( saList.size() == 1 ) {
    	SubmissionAssociationTable a = saList.get(0);
    	ref = a.getXFormParameters();
    	eksFormInfo.add(new EntityKey(a, a.getUri()));
    }
    
    // queue everything in formInfo for delete
    objectEntity.recursivelyAddEntityKeys(eksFormInfo, cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    
	if ( formDefinition != null ) {
		List<EntityKey> eks = new ArrayList<EntityKey>();

		// queue everything in the formDataModel for delete
	    for ( FormDataModel m : formDefinition.uriMap.values() ) {
	    	
			eks.add(new EntityKey(fdm, m.getUri()));
	    }
	    // delete everything out of FDM
	    ds.deleteEntities(eks, user);
	    // drop the tables...
	    for ( CommonFieldsBase b : formDefinition.getBackingTableSet()) {
	    	try {
	    		ds.dropRelation(b, user);
	    	} catch ( ODKDatastoreException e ) {
	    		e.printStackTrace();
	    	}
	    }
	}

	// tell FormDefinition to forget me...
    FormDefinition.forgetFormId(ref);

    // delete everything in formInfo
    ds.deleteEntities(eksFormInfo, user);
  }

  /**
   * Get the datastore key that uniquely identifies the form entity
   * 
   * @return datastore key
   */
  public EntityKey getEntityKey() {
	  return objectEntity.getKey();
  }
  
  public SubmissionKey getSubmissionKey() {
  	// TODO Auto-generated method stub
  	return objectEntity.constructSubmissionKey(null);
  }

  public FormElementModel getTopLevelGroupElement(){
		  return formDefinition.getTopLevelGroupElement();
  }
  
  public String getMajorMinorVersionString() {
	  RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
	  if ( r.getNumberRepeats() != 1 ) {
		  throw new IllegalStateException("Expecting exactly one fileset at this time");
	  }
	  SubmissionSet s = r.getSubmissionSets().get(0);
	  
	  Long modelVersion = ((LongSubmissionType) s.getElementValue(FormInfo.rootElementModelVersion)).getValue();
	  Long uiVersion = ((LongSubmissionType) s.getElementValue(FormInfo.rootElementUiVersion)).getValue();
	  StringBuilder b = new StringBuilder();
	  if ( modelVersion != null ) {
		  b.append(modelVersion.toString());
	  }
	  if ( uiVersion != null ) {
		  b.append(".");
		  b.append(uiVersion.toString());
	  }
	  return b.toString();
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
    StringSubmissionType formId = (StringSubmissionType) objectEntity.getElementValue(FormInfo.formId);
    return formId.getValue();
  }

  public boolean hasManifestFileset() {
	  // TODO: deal with version...
		RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
		for ( SubmissionSet filesetRecord : r.getSubmissionSets()) {
			BlobSubmissionType bt = (BlobSubmissionType) filesetRecord.getElementValue(FormInfo.manifestFileset);
			return ( bt.getAttachmentCount() != 0 );
		}
		return false;
  }

  public BlobSubmissionType getManifestFileset() {
	  // TODO: deal with version...
		RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
		for ( SubmissionSet filesetRecord : r.getSubmissionSets()) {
			BlobSubmissionType bt = (BlobSubmissionType) filesetRecord.getElementValue(FormInfo.manifestFileset);
			return bt;
		}
		return null;
  }
  
  private String getDescriptionTableFieldValue(String languageCode, FormElementModel field) {
		RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiDescriptionTable);
		String preferredViewableName = null;
		String defaultViewableName = null;
		for ( SubmissionSet descriptionRecord : r.getSubmissionSets()) {
			String value = ((StringSubmissionType) descriptionRecord.getElementValue(field)).getValue();
			String lc = ((StringSubmissionType) descriptionRecord.getElementValue(FormInfo.languageCode)).getValue();
			if ( lc == null ) {
				defaultViewableName = value;
			} else if ( lc.equals(languageCode) ) {
				preferredViewableName = value;
			}
		}
		return ( preferredViewableName == null ) ? defaultViewableName : preferredViewableName;
	  }
  
  /**
   * Get the name that is viewable on ODK Aggregate
   * 
   * @return viewable name
   */
  public String getViewableName(String languageCode) {
	  return getDescriptionTableFieldValue(languageCode, FormInfo.formName);
  }

  public String getViewableName() {
	  return getViewableName(null);
  }
  
  public String getViewableFormNameSuitableAsFileName() {
	String name = getViewableName();
	return name.replaceAll("[^\\p{L}0-9]","_"); // any non-alphanumeric is replaced with underscore
  }
  
  public String getDescription(String languageCode) {
	  return getDescriptionTableFieldValue(languageCode, FormInfo.description);
		}
  
  public String getDescription() {
	  return getDescription(null);
  }

  public String getDescriptionUrl(String languageCode) {
	  return getDescriptionTableFieldValue(languageCode, FormInfo.descriptionUrl);
  }
  
  public String getDescriptionUrl() {
	  return getDescriptionUrl(null);
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
	// assume for now that there is only one fileset...
	RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
	List<SubmissionSet> filesets = r.getSubmissionSets();
	if ( filesets.size() != 1 ) {
		throw new IllegalStateException("Expecting only one fileset record at this time!");
	}
	SubmissionSet filesetRecord = filesets.get(0);
	BlobSubmissionType bt = (BlobSubmissionType) filesetRecord.getElementValue(FormInfo.xformDefinition);
	  
	  int count = bt.getAttachmentCount();
	  // we use ordinal counting here: 1..count
	  for ( int i = 1 ; i <= count ; ++i ) {
		  String contentType = bt.getContentType(i);
		  if ( contentType == null ) continue; // incomplete form...
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
  public String getFormXml(CallingContext cc) throws ODKDatastoreException {
		// assume for now that there is only one fileset...
		RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
		List<SubmissionSet> filesets = r.getSubmissionSets();
		if ( filesets.size() != 1 ) {
			throw new IllegalStateException("Expecting only one fileset record at this time!");
		}
		SubmissionSet filesetRecord = filesets.get(0);
		BlobSubmissionType bt = (BlobSubmissionType) filesetRecord.getElementValue(FormInfo.xformDefinition);
	  
	  int count = bt.getAttachmentCount();
	  // we use ordinal counting here: 1..count
	  for ( int i = 1 ; i <= count ; ++i ) {
		  String contentType = bt.getContentType(i);
		  if ( contentType == null ) continue; // incomplete form...
		  String unrootedFileName = bt.getUnrootedFilename(i);
		  if ( contentType.equals("text/xml") && !unrootedFileName.contains("/")) {
			  byte[] byteArray = bt.getBlob(i, cc);
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
	// assume for now that there is only one fileset...
	RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
	List<SubmissionSet> filesets = r.getSubmissionSets();
	if ( filesets.size() != 1 ) {
		throw new IllegalStateException("Expecting only one fileset record at this time!");
	}
	SubmissionSet filesetRecord = filesets.get(0);
    return ((BooleanSubmissionType) filesetRecord.getElementValue(FormInfo.isDownloadAllowed)).getValue();
  }

  /**
   * Sets a boolean value of whether the form can be downloaded
   * 
   * @param downloadEnabled
   *          set to true if form can be downloaded, false otherwise
   * 
   */
  public void setDownloadEnabled(Boolean downloadEnabled) {
	// assume for now that there is only one fileset...
	RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiFilesetTable);
	List<SubmissionSet> filesets = r.getSubmissionSets();
	if ( filesets.size() != 1 ) {
		throw new IllegalStateException("Expecting only one fileset record at this time!");
	}
	SubmissionSet filesetRecord = filesets.get(0);
    ((BooleanSubmissionType) filesetRecord.getElementValue(FormInfo.isDownloadAllowed)).setBooleanValue(downloadEnabled);
  }

  /**
   * Gets whether a new submission can be received
   * 
   * @return true if a new submission can be received, false otherwise
   */
  public Boolean getSubmissionEnabled(CallingContext cc) {
	// assume for now that there is only one submission...
	RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiSubmissionTable);
	List<SubmissionSet> filesets = r.getSubmissionSets();
	if ( filesets.size() != 1 ) {
		throw new IllegalStateException("Expecting only one submission record at this time!");
	}
	SubmissionSet submissionRecord = filesets.get(0);
	SubmissionAssociationTable sa = findSubmission(submissionRecord, cc);
	return (sa == null) ? false : sa.getIsSubmissionAllowed();
  }

  /**
   * Sets a boolean value of whether a new submission can be received
   * 
   * @param submissionEnabled
   *          set to true if a new submission can be received, false otherwise
   * 
   */
  public void setSubmissionEnabled(Boolean submissionEnabled, CallingContext cc) {
	// assume for now that there is only one submission...
	RepeatSubmissionType r = (RepeatSubmissionType) objectEntity.getElementValue(FormInfo.fiSubmissionTable);
	List<SubmissionSet> filesets = r.getSubmissionSets();
	if ( filesets.size() != 1 ) {
		throw new IllegalStateException("Expecting only one submission record at this time!");
	}
	SubmissionSet submissionRecord = filesets.get(0);
	SubmissionAssociationTable sa = findSubmission(submissionRecord, cc);
	if ( sa != null ) sa.setIsSubmissionAllowed(submissionEnabled);
  }
 
  private SubmissionAssociationTable findSubmission(SubmissionSet submissionRecord, CallingContext cc) {
	  String submissionFormId = ((StringSubmissionType) submissionRecord.getElementValue(FormInfo.submissionFormId)).getValue();
	  Long submissionModelVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionModelVersion)).getValue();
	  Long submissionUiVersion = ((LongSubmissionType) submissionRecord.getElementValue(FormInfo.submissionUiVersion)).getValue();
	  XFormParameters p = new XFormParameters(submissionFormId, submissionModelVersion, submissionUiVersion);
	  
	  List<SubmissionAssociationTable> match = SubmissionAssociationTable.findSubmissionAssociationsForXForm(p, cc);
	  if ( !match.isEmpty() ) {
		  if ( match.size() != 1 ) {
			  throw new IllegalStateException("Logic is not yet in place for cross-form submission sharing");
		  }
		  return match.get(0);
	  }
	  return null;
  }
  
  private FormElementModel findElementByNameHelper(FormElementModel current, String name) {
	if ( current.getElementName().equals(name) ) return current;
	FormElementModel m = null;
	for ( FormElementModel c : current.getChildren() ) {
		m = findElementByNameHelper(c, name);
		if ( m != null ) break;
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
	  return findElementByNameHelper( getTopLevelGroupElement(), name );
  }

  public FormElementModel getFormElementModel(
			List<SubmissionKeyPart> submissionKeyParts) {
		FormElementModel m = null;
		boolean formIdElement = true;
		for (SubmissionKeyPart p : submissionKeyParts) {
			if ( formIdElement ) {
				if (!p.getElementName().equals(getFormId())) {
					return null;
				}
				formIdElement = false;
			} else if (m == null) {
				m = getTopLevelGroupElement();
				if ( !p.getElementName().equals(m.getElementName())) {
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
  
  private void getRepeatGroupsInModelHelper(FormElementModel current, List<FormElementModel> accumulation) {
	  for ( FormElementModel m : current.getChildren() ) {
		  if ( m.getElementType() == FormElementModel.ElementType.REPEAT ) {
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
    SubmissionAssociationTable sat = getSubmissionAssociation(cc);
    boolean submit = sat.getIsSubmissionAllowed();    
    boolean downloadable = getDownloadEnabled();
    return new FormSummary(getViewableName(), getFormId(), getCreationUser(), downloadable, submit);
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
    if ( objectEntity == null ) return (other.objectEntity == null);
    
    return (objectEntity.equals(other.objectEntity));
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
    return getViewableName(null);
  }
  
  /**
   * Returns the top level dynamic class for the FormInfo table.
   * @param datastore
   * @param userService
   * @return
   * @throws ODKDatastoreException
   */
  public static final FormInfoTable getFormInfoRelation(CallingContext cc) throws ODKDatastoreException {
	  return (FormInfoTable) FormInfo.getFormInfoForm(cc).getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();
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
		if ( firstSlash != -1 ) {
			// strip off the group path of the key
			formId = submissionKey.substring(0, firstSlash);
		}
		return formId;
	}

	/**
	 * Static function to retrieve a form with the specified ODK id from the
	 * datastore
	 * 
	 * @param submissionKey
	 *            The ODK identifier that identifies the form
	 * 
	 * @return The ODK aggregate form definition/conversion object
	 * 
	 * @throws ODKFormNotFoundException
	 *             Thrown when a form was not able to be found with the
	 *             corresponding ODK ID
	 */
	public static Form retrieveForm(String submissionKey, CallingContext cc) 
				throws ODKFormNotFoundException {

		// TODO: consider using memcache to have form info in memory for
		// faster response times.  Note that we already cache the 
		// FormDefinition...
		Form formInfoForm = null;
		try {
			// make sure the FormInfo table definition is loaded...
			formInfoForm = FormInfo.getFormInfoForm(cc);
		} catch (ODKDatastoreException e) {
			throw new ODKFormNotFoundException(e);
		}

		if (submissionKey == null) {
			return null;
		}
		String formIdValue = extractWellFormedFormId(
				submissionKey);
		if (formIdValue.equals(Form.URI_FORM_ID_VALUE_FORM_INFO)) {
			return formInfoForm;
		}

		try {
			String formUri = CommonFieldsBase.newMD5HashUri(formIdValue);
			Form form = new Form(formUri, cc);
			if ( !formIdValue.equals(form.getFormId()) ) {
				throw new IllegalStateException(
						"more than one FormInfo entry for the given form id: " + formIdValue );
			}
			return form;
		} catch (Exception e) {
			throw new ODKFormNotFoundException(e);
		}
	}
	
	/**
	 * Create or fetch the given formId.
	 * 
	 * @param formId
	 * @param ds
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final Submission createOrFetchFormId(String formId, CallingContext cc) throws ODKDatastoreException {

		// TODO: consider using memcache to have form info in memory for
		// faster response times.  Note that we already cache the 
		// FormDefinition...
		Form formInfoForm = null;
		// make sure the FormInfo table definition is loaded...
		formInfoForm = FormInfo.getFormInfoForm(cc);

		if (formId.equals(Form.URI_FORM_ID_VALUE_FORM_INFO)) {
			throw new IllegalStateException("Unexpectedly retrieving formInfo definition");
		}
		Submission formInfo = null;
		String formUri = CommonFieldsBase.newMD5HashUri(formId);
		
		try {
			Datastore ds = cc.getDatastore();
			User user = cc.getCurrentUser();
			FormInfoTable fi = (FormInfoTable) ds.getEntity(formInfoForm.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), formUri, user);
	    	formInfo = new Submission(fi, formInfoForm.getFormDefinition(), cc);
		} catch ( ODKEntityNotFoundException e ) {
			formInfo = new Submission(1L, 0L, formUri, formInfoForm.getFormDefinition(), cc);

	    	((StringSubmissionType) formInfo.getElementValue(FormInfo.formId)).setValueFromString(formId);
	    }
	    return formInfo;
	}
	
	public static final FormDefinition getFormInfoDefinition(CallingContext cc) throws ODKDatastoreException {
		return FormInfo.getFormDefinition(cc);
	}

	/**
	 * Static function to find form by FormId
	 * 
	 * @param formKey
	 *            The entity key for the FormInfo record defining the form
	 * 
	 * @return The ODK aggregate form definition/conversion object
	 * 
	 * @throws ODKFormNotFoundException
	 *             Thrown when a form was not able to be found with the
	 *             corresponding ODK ID
	 */
	public static Form retrieveFormByEntityKey(EntityKey formKey, CallingContext cc) throws ODKFormNotFoundException {

		// TODO: consider using memcache to have form info in memory for
		// faster response times.  Note that we already cache the 
		// FormDefinition...
		Form formInfoForm = null;
		try {
			// make sure the FormInfo table definition is loaded...
			formInfoForm = FormInfo.getFormInfoForm(cc);
		} catch (ODKDatastoreException e) {
			throw new ODKFormNotFoundException(e);
		}

		if (formKey == null) {
			return null;
		}
		
		if (formKey.getKey().equals(formInfoForm.getEntityKey().getKey())) {
			return formInfoForm;
		}
		
		try {
			Form form = new Form(formKey.getKey(), cc);
			return form;
		} catch (Exception e) {
			throw new ODKFormNotFoundException(e);
		}
	}
}
