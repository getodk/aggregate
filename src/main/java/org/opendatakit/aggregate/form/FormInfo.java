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
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.form.FormDefinition.OrdinalSequence;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Defines the aggregate.opendatakit.org:FormInfo form which is used to hold all the 
 * form definitions in Aggregate.
 *  
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class FormInfo {
	private static FormDefinition formDefinition = null;
	private static Form formInfoForm = null;
	
	private static FormInfoTable reference = null;
	
	public static final XFormParameters formInfoXFormParameters =
		new XFormParameters(Form.URI_FORM_ID_VALUE_FORM_INFO, 1L, 0L);
	/*
	 * Following public fields are valid after the first 
	 * successful call to getFormDefinition()
	 */
	
	// FormInfoTable element...
	static FormElementModel formId = null;
	// FormInfoDescriptionTable element...
	static FormElementModel fiDescriptionTable = null;
	static FormElementModel languageCode = null;
	static FormElementModel formName = null;
	static FormElementModel description = null;
	static FormElementModel descriptionUrl = null;
	// FormInfoFilesetTable element...
	static FormElementModel fiFilesetTable = null;
	static FormElementModel rootElementModelVersion = null;
	static FormElementModel rootElementUiVersion = null;
	static FormElementModel isDownloadAllowed = null;
	static FormElementModel isEncryptedForm = null;
	static FormElementModel xformDefinition = null;
	static FormElementModel manifestFileset = null;
	// FormInfoSubmissionTable element...
	static FormElementModel fiSubmissionTable = null;
	static FormElementModel submissionFormId = null;
	static FormElementModel submissionModelVersion = null;
	static FormElementModel submissionUiVersion = null;
	
	/**
	 * Return the form definition for the FormInfo submissions.  These are the 
	 * submissions that hold the form definitions.  As a side-efect, all the 
	 * public static variables of this class are initialized to non-null values.
	 * 
	 * @param datastore
	 * @return
	 * @throws ODKDatastoreException 
	 */
	static synchronized FormDefinition getFormDefinition(CallingContext cc) throws ODKDatastoreException {
		boolean asDaemon = cc.getAsDeamon();
		try {
			cc.setAsDaemon(true); // effectively a run-as daemon account w.r.t. database access
			if ( formDefinition == null ) {
				// The FormDefn list of registered forms is itself a well-known
				// form within the Aggregate instance.  Load the form definition
				// for that well-known form id.
			    formDefinition = FormDefinition.getFormDefinition(formInfoXFormParameters, cc);
	
			    String formInfoUri = CommonFieldsBase.newMD5HashUri(formInfoXFormParameters.formId);
			    
			    if ( formDefinition == null ) {
					// doesn't have the form definition tables defined ...
					// need to insert definition records into the sa and fdm tables...
			    	createForm(cc);
					
					if ( formDefinition == null ) {
						throw new IllegalStateException("Unable to create form definition definition!");
					}
					
				}
					
				// and update the FormInfo object so it has the canonical data fields...
				reference = (FormInfoTable) formDefinition.getTopLevelGroup().getBackingObjectPrototype();
				
				formId = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), FormInfoTable.assertRelation(cc).formId);
				// FormInfoDescriptionTable element...
				{
					fiDescriptionTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoDescriptionTable.TABLE_NAME);
					FormInfoDescriptionTable f = FormInfoDescriptionTable.assertRelation(cc);
					languageCode = FormDefinition.findElement(fiDescriptionTable, f.languageCode);
					formName = FormDefinition.findElement(fiDescriptionTable, f.formName);
					description = FormDefinition.findElement(fiDescriptionTable, f.description);
					descriptionUrl = FormDefinition.findElement(fiDescriptionTable, f.descriptionUrl);
				}
				// FormInfoFilesetTable element...
				{
					fiFilesetTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoFilesetTable.TABLE_NAME);
					FormInfoFilesetTable f = FormInfoFilesetTable.assertRelation(cc);
					rootElementModelVersion = FormDefinition.findElement(fiFilesetTable, f.rootElementModelVersion);
					rootElementUiVersion = FormDefinition.findElement(fiFilesetTable, f.rootElementUiVersion);
					isDownloadAllowed = FormDefinition.findElement(fiFilesetTable, f.isDownloadAllowed);
					isEncryptedForm = FormDefinition.findElement(fiFilesetTable, f.isEncryptedForm);
					xformDefinition = fiFilesetTable.findElementByName(FormInfoFilesetTable.ELEMENT_NAME_XFORM_DEFINITION);
					manifestFileset = fiFilesetTable.findElementByName(FormInfoFilesetTable.ELEMENT_NAME_MANIFEST_FILESET);
				}
				// FormInfoSubmissionTable element...
				{
					fiSubmissionTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoSubmissionTable.TABLE_NAME);
					FormInfoSubmissionTable f = FormInfoSubmissionTable.assertRelation(cc);
					submissionFormId = FormDefinition.findElement(fiSubmissionTable, f.submissionFormId);
					submissionModelVersion = FormDefinition.findElement(fiSubmissionTable, f.submissionModelVersion);
					submissionUiVersion = FormDefinition.findElement(fiSubmissionTable, f.submissionUiVersion);
				}
	
				Submission formInfo = FormDefinition.assertFormInfoRecord(reference, formDefinition, formInfoXFormParameters,
						"Form Information", 
						"Form information table used by Aggregate to track all forms uploaded to this server.", 
						formInfoUri, cc);
				
				formInfoForm = new Form(formInfo, cc);
			}
			return formDefinition;
		} finally {
			cc.setAsDaemon(asDaemon);
		}
	}
	
	static final void createForm(CallingContext cc) throws ODKDatastoreException {
		List<FormDataModel> model = new ArrayList<FormDataModel>();

		boolean asDaemon = cc.getAsDeamon();
		try {
			cc.setAsDaemon(true);
			Datastore ds = cc.getDatastore();
			User user = cc.getCurrentUser();
			FormInfoTable formInfoRelation = FormInfoTable.assertRelation(cc);
			
			String formInfoUri = CommonFieldsBase.newMD5HashUri(formInfoXFormParameters.formId);
		    SubmissionAssociationTable sa = SubmissionAssociationTable.assertSubmissionAssociation(
		    													formInfoUri, formInfoXFormParameters, cc);
			String definitionUri = sa.getUriSubmissionDataModel();

			FormInfoTable formInfoDefinition = ds.createEntityUsingRelation(formInfoRelation, user);
			formInfoDefinition.setStringField(formInfoRelation.primaryKey, definitionUri);
			
			OrdinalSequence os = new OrdinalSequence();
			
			String groupKey = FormDefinition.buildTableFormDataModel( model, 
					formInfoDefinition, 
					formInfoDefinition, // top level table
					formInfoDefinition.getUri(), // parent table uri...
					os,
					cc );
			
			Long ordinal = os.ordinal;
			FormInfoDescriptionTable.createFormDataModel(model, 
					formInfoDefinition, // top level table
					groupKey,
					os,
					cc );
			
			os.ordinal = ++ordinal;
			FormInfoFilesetTable.createFormDataModel(model, 
					formInfoDefinition, // top level table
					groupKey,
					os, 
					cc );
			
			os.ordinal = ++ordinal;
			FormInfoSubmissionTable.createFormDataModel(model, 
					formInfoDefinition, // top level table
					groupKey,
					os, 
					cc );
			
			os.ordinal = 2L;
			FormDefinition.buildLongStringFormDataModel(model, 
					FormInfoTable.FORM_INFO_LONG_STRING_REF_TEXT, 
					FormInfoTable.FORM_INFO_REF_TEXT, 
					formInfoDefinition, // top level and parent table
					os, 
					cc );
			
			FormDefinition.assertModel(formInfoXFormParameters, model, cc);
			
			// wait for GAE to settle before continuing...
			try {
				Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// and enable the data model for retrieval...
			sa.setIsPersistenceModelComplete(true);
			ds.putEntity(sa, user);
			
			// wait for GAE to settle before continuing...
			try {
				Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// get the data model for a form...
		    formDefinition = FormDefinition.getFormDefinition(formInfoXFormParameters, cc);
			
		    // stop here, as the caller will update everything else...
		} finally {
			cc.setAsDaemon(asDaemon);
		}
	}
	
	static final Form getFormInfoForm(CallingContext cc) throws ODKDatastoreException {
		getFormDefinition(cc);
		return formInfoForm;
	}

	public static final void populateBackingTableMap(Map<String, DynamicCommonFieldsBase> backingTableMap, CallingContext cc) {
		try {
		    DynamicCommonFieldsBase b;
			b = FormInfoTable.assertRelation(cc);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoDescriptionTable.assertRelation(cc);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoFilesetTable.assertRelation(cc);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoSubmissionTable.assertRelation(cc);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
		} catch (ODKDatastoreException e) {
			throw new IllegalStateException("the relations should already have been created");
		}
	}
	
	public static final void setFormDescription( Submission aFormDefinition, String languageCodeValue, String formNameValue, String descriptionValue, String descriptionUrlValue, CallingContext cc ) throws ODKDatastoreException {
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiDescriptionTable);
	    List<SubmissionSet> descriptions = r.getSubmissionSets();
	    SubmissionSet matchingSet = null;
	    for ( SubmissionSet f : descriptions ) {
	    	String languageCodeString = ((StringSubmissionType) f.getElementValue(languageCode)).getValue();
	    	if (languageCodeString == null ) {
	    		if ( languageCodeValue == null ) {
	    			matchingSet = f;
	    			break;
	    		}
	    	} else if ( languageCodeValue != null && languageCodeString.equals(languageCodeValue)) {
	    		matchingSet = f;
	    		break;
	    	}
	    }
	    
	    if ( matchingSet == null ) {
	    	// create a matching set...
			SubmissionSet sDescription = new SubmissionSet(aFormDefinition, descriptions.size()+1L, fiDescriptionTable, formDefinition, aFormDefinition.getKey(), cc);
			((StringSubmissionType) sDescription.getElementValue(languageCode)).setValueFromString(languageCodeValue);
			((StringSubmissionType) sDescription.getElementValue(formName)).setValueFromString(formNameValue);
			((StringSubmissionType) sDescription.getElementValue(description)).setValueFromString(descriptionValue);
			((StringSubmissionType) sDescription.getElementValue(descriptionUrl)).setValueFromString(descriptionUrlValue);
			r.addSubmissionSet(sDescription);
			matchingSet = sDescription;
	    } else {
	    	// update name, description, etc.
			((StringSubmissionType) matchingSet.getElementValue(formName)).setValueFromString(formNameValue);
			((StringSubmissionType) matchingSet.getElementValue(description)).setValueFromString(descriptionValue);
			((StringSubmissionType) matchingSet.getElementValue(descriptionUrl)).setValueFromString(descriptionUrlValue);
	    }
	}
	
	private static final boolean sameVersion( Long rootModel, Long rootUi, Long rootModelVersion, Long rootUiVersion) {
    	return  ((rootModelVersion == null) ? (rootModel == null) :
    				(rootModel != null && rootModelVersion.equals(rootModel))) &&
	    		((rootUiVersion == null) ? (rootUi == null) :
	    				(rootUi != null && rootUiVersion.equals(rootUi)));
	}
	
	/**
	 * Set the Xform definition.
	 * 
	 * @param aFormDefinition
	 * @param rootModelVersion
	 * @param rootUiVersion
	 * @param title
	 * @param definition
	 * @param datastore
	 * @param user
	 * @return true if the file is identical to the currently-stored one.
	 * @throws ODKDatastoreException
	 * @throws ODKFormAlreadyExistsException 
	 */
	public static final boolean setXFormDefinition( Submission aFormDefinition, 
			Long rootModelVersion, Long rootUiVersion, boolean isEncrypted,
			String title, byte[] definition, boolean isDownloadEnabled, CallingContext cc ) throws ODKDatastoreException, ODKFormAlreadyExistsException {

		RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiFilesetTable);
	    List<SubmissionSet> filesets = r.getSubmissionSets();
	    SubmissionSet matchingSet = null;
	    for ( SubmissionSet f : filesets ) {
	    	Long rootModel = ((LongSubmissionType) f.getElementValue(rootElementModelVersion)).getValue();
	    	Long rootUi = ((LongSubmissionType) f.getElementValue(rootElementUiVersion)).getValue();
	    	if (sameVersion(rootModel, rootUi, rootModelVersion, rootUiVersion)) {
	    		matchingSet = f;
	    	}
	    }

	    if ( matchingSet == null ) {	    	
	    	// we don't support multiple file versions yet...
	    	if ( filesets.size() != 0 ) throw new ODKFormAlreadyExistsException();
	    	
	    	// create a matching set...
			SubmissionSet sFileset = new SubmissionSet(aFormDefinition, filesets.size()+1L, fiFilesetTable, formDefinition, aFormDefinition.getKey(), cc);
			((LongSubmissionType) sFileset.getElementValue(rootElementModelVersion)).setValueFromString(rootModelVersion == null ? null : rootModelVersion.toString());
			((LongSubmissionType) sFileset.getElementValue(rootElementUiVersion)).setValueFromString(rootUiVersion == null ? null : rootUiVersion.toString());
			((BooleanSubmissionType) sFileset.getElementValue(isDownloadAllowed)).setValueFromString(isDownloadEnabled ? "yes" : "no");
			((BooleanSubmissionType) sFileset.getElementValue(isEncryptedForm)).setValueFromString(isEncrypted ? "yes" : "no");
			r.addSubmissionSet(sFileset);
			matchingSet = sFileset;
	    }
	    
	    BlobSubmissionType bt = (BlobSubmissionType) matchingSet.getElementValue(FormInfo.xformDefinition);
	    if ( bt.getAttachmentCount() == 0 ) {
	    	return bt.setValueFromByteArray(definition, "text/xml", Long.valueOf(definition.length), title + ".xml", cc
	    				) != BinaryContentManipulator.BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
	    } else {
	    	if ( bt.getAttachmentCount() > 1) throw new ODKFormAlreadyExistsException();
	    	String hash = bt.getContentHash(1);
	    	String thisHash = CommonFieldsBase.newMD5HashUri(definition);
	    	if (! hash.equals(thisHash) ) throw new ODKFormAlreadyExistsException();
	    	return true;
	    }
	}
	
	/**
	 * Media files are assumed to be in a directory one level deeper than the xml definition.
	 * So the filename reported on the mime item has an extra leading directory.  Strip that off.
	 * 
	 * @param aFormDefinition
	 * @param rootModelVersion
	 * @param rootUiVersion
	 * @param item
	 * @param datastore
	 * @param user
	 * @return true if the files are completely new or are identical to the currently-stored ones.
	 * @throws ODKDatastoreException
	 */
	public static final boolean setXFormMediaFile( Submission aFormDefinition, Long rootModelVersion, Long rootUiVersion, MultiPartFormItem item, CallingContext cc ) throws ODKDatastoreException {
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiFilesetTable);
	    List<SubmissionSet> filesets = r.getSubmissionSets();
	    SubmissionSet matchingSet = null;
	    for ( SubmissionSet f : filesets ) {
	    	Long rootModel = ((LongSubmissionType) f.getElementValue(rootElementModelVersion)).getValue();
	    	Long rootUi = ((LongSubmissionType) f.getElementValue(rootElementUiVersion)).getValue();
	    	if (sameVersion(rootModel, rootUi, rootModelVersion, rootUiVersion)) {
	    		matchingSet = f;
	    	}
	    }
	    
	    if ( matchingSet == null ) {
	    	throw new ODKDatastoreException("did not find matching FileSet for media file");
	    }

	    boolean matchingFiles = true;
	    BlobSubmissionType bt = (BlobSubmissionType) matchingSet.getElementValue(FormInfo.manifestFileset);
    	String filePath = item.getFilename();
    	if ( filePath.indexOf("/") != -1 ) {
    		filePath = filePath.substring(filePath.indexOf("/")+1);
    	}
		matchingFiles = matchingFiles &&
			(BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION != 
				bt.setValueFromByteArray(item.getStream().toByteArray(), item.getContentType(), item.getContentLength(), filePath, cc));
		return matchingFiles;
	}

	public static void setFormSubmission(Submission aFormDefinition, String submissionFormIdValue,
			Long modelVersionValue, Long uiVersionValue, CallingContext cc) throws ODKDatastoreException {
		if ( submissionFormIdValue == null ) {
			throw new IllegalArgumentException("submission form id cannot be null");
		}
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiSubmissionTable);
	    List<SubmissionSet> submissionDefns = r.getSubmissionSets();

	    for ( SubmissionSet f : submissionDefns ) {
	    	String formIdStr = ((StringSubmissionType) f.getElementValue(submissionFormId)).getValue();
	    	Long subModel = ((LongSubmissionType) f.getElementValue(submissionModelVersion)).getValue();
	    	Long subUi = ((LongSubmissionType) f.getElementValue(submissionUiVersion)).getValue();
	    	if ( sameVersion(subModel, subUi, modelVersionValue, uiVersionValue) &&
	    		 submissionFormIdValue.equals(formIdStr) ) {
	    		return;
	    	}
	    }
	    
    	// create a matching set...
		SubmissionSet sDescription = new SubmissionSet(aFormDefinition, submissionDefns.size()+1L, fiSubmissionTable, formDefinition, aFormDefinition.getKey(), cc);
		((StringSubmissionType) sDescription.getElementValue(submissionFormId)).setValueFromString(submissionFormIdValue);
		((LongSubmissionType) sDescription.getElementValue(submissionModelVersion)).setValueFromString((modelVersionValue == null) ? null : modelVersionValue.toString());
		((LongSubmissionType) sDescription.getElementValue(submissionUiVersion)).setValueFromString((uiVersionValue == null) ? null : uiVersionValue.toString());
		r.addSubmissionSet(sDescription);
	}
}
