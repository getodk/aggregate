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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

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
	static FormElementModel isFilesetComplete = null;
	static FormElementModel isDownloadAllowed = null;
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
	static FormDefinition getFormDefinition(Datastore datastore) throws ODKDatastoreException {
		if ( formDefinition == null ) {
			// The FormDefn list of registered forms is itself a well-known
			// form within the Aggregate instance.  Load the form definition
			// for that well-known form id.
	        UserService userService = (UserService) ContextFactory.get().getBean(
	                BeanDefs.USER_BEAN);
            User user = userService.getDaemonAccountUser();
		    formDefinition = FormDefinition.getFormDefinition(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO, datastore, user);
		    if ( formDefinition == null ) {
				// doesn't have the form definition tables defined ...
				// need to insert definition records into the fdm table...
				final FormDataModel fdm = FormDataModel.createRelation(datastore, user);
				
				List<FormDataModel> idDefn = new ArrayList<FormDataModel>();
				EntityKey definitionKey = new EntityKey(fdm, CommonFieldsBase.newMD5HashUri(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO));
				FormInfoTable.createFormDataModel(idDefn, definitionKey, datastore, user);

				datastore.putEntities(idDefn, user);
				
			    formDefinition = FormDefinition.getFormDefinition(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO, datastore, user);
				
				if ( formDefinition == null ) {
					throw new IllegalStateException("Unable to create form definition definition!");
				}
				
			}
				
			// and update the FormInfo object so it has the canonical data fields...
			reference = (FormInfoTable) formDefinition.getTopLevelGroup().getBackingObjectPrototype();
			
			formId = findElement(formDefinition.getTopLevelGroupElement(), FormInfoTable.createRelation(datastore, user).formId);
			// FormInfoDescriptionTable element...
			{
				fiDescriptionTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoDescriptionTable.TABLE_NAME);
				FormInfoDescriptionTable f = FormInfoDescriptionTable.createRelation(datastore, user);
				languageCode = findElement(fiDescriptionTable, f.languageCode);
				formName = findElement(fiDescriptionTable, f.formName);
				description = findElement(fiDescriptionTable, f.description);
				descriptionUrl = findElement(fiDescriptionTable, f.descriptionUrl);
			}
			// FormInfoFilesetTable element...
			{
				fiFilesetTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoFilesetTable.TABLE_NAME);
				FormInfoFilesetTable f = FormInfoFilesetTable.createRelation(datastore, user);
				rootElementModelVersion = findElement(fiFilesetTable, f.rootElementModelVersion);
				rootElementUiVersion = findElement(fiFilesetTable, f.rootElementUiVersion);
				isFilesetComplete = findElement(fiFilesetTable, f.isFilesetComplete);
				isDownloadAllowed = findElement(fiFilesetTable, f.isDownloadAllowed);
				xformDefinition = fiFilesetTable.findElementByName(FormInfoFilesetTable.ELEMENT_NAME_XFORM_DEFINITION);
				manifestFileset = fiFilesetTable.findElementByName(FormInfoFilesetTable.ELEMENT_NAME_MANIFEST_FILESET);
			}
			// FormInfoSubmissionTable element...
			{
				fiSubmissionTable = formDefinition.getTopLevelGroupElement().findElementByName(FormInfoSubmissionTable.TABLE_NAME);
				FormInfoSubmissionTable f = FormInfoSubmissionTable.createRelation(datastore, user);
				submissionFormId = findElement(fiSubmissionTable, f.submissionFormId);
				submissionModelVersion = findElement(fiSubmissionTable, f.submissionModelVersion);
				submissionUiVersion = findElement(fiSubmissionTable, f.submissionUiVersion);
			}
			
			String formInfoUri = CommonFieldsBase.newMD5HashUri(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO);
			TopLevelDynamicBase fi = null;
			try {
				fi = datastore.getEntity(reference, formInfoUri, user);
			} catch ( ODKEntityNotFoundException e ) {
				// we must have failed before persisting a FormInfo record
				// or this must be our first time through...
				Submission formInfo = new Submission(1L, 0L, formInfoUri, formDefinition, datastore, user);
				((StringSubmissionType) formInfo.getElementValue(formId)).setValueFromString(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO);
				// default description...
				{
					RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(fiDescriptionTable);
					SubmissionSet sDescription = new SubmissionSet(formInfo, 1L, fiDescriptionTable, formDefinition, formInfo.getKey(), datastore, user);
					((StringSubmissionType) sDescription.getElementValue(formName)).setValueFromString("Form Information");
					((StringSubmissionType) sDescription.getElementValue(description)).setValueFromString("Form information table used by Aggregate to track all forms uploaded to this server.");
					r.addSubmissionSet(sDescription);
				}
				// fileset...
				{
					RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(fiFilesetTable);
					SubmissionSet sFileset = new SubmissionSet(formInfo, 1L, fiFilesetTable, formDefinition, formInfo.getKey(), datastore, user);
					((LongSubmissionType) sFileset.getElementValue(rootElementModelVersion)).setValueFromString("1");
					((LongSubmissionType) sFileset.getElementValue(rootElementUiVersion)).setValueFromString("0");
					((BooleanSubmissionType) sFileset.getElementValue(isFilesetComplete)).setValueFromString("yes");
					((BooleanSubmissionType) sFileset.getElementValue(isDownloadAllowed)).setValueFromString("yes");
					r.addSubmissionSet(sFileset);
				}
				// submission...
				{
					RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(fiSubmissionTable);
					SubmissionSet sSubmission = new SubmissionSet(formInfo, 1L, fiSubmissionTable, formDefinition, formInfo.getKey(), datastore, user);
					((StringSubmissionType) sSubmission.getElementValue(submissionFormId)).setValueFromString(FormDataModel.URI_FORM_ID_VALUE_FORM_INFO);
					((LongSubmissionType) sSubmission.getElementValue(submissionModelVersion)).setValueFromString("1");
					((LongSubmissionType) sSubmission.getElementValue(submissionUiVersion)).setValueFromString("0");
					r.addSubmissionSet(sSubmission);
				}
				formInfo.persist(datastore, user);
				fi = datastore.getEntity(reference, formInfoUri, user);
			}
			
			// and retrieve cleanly... 
		    Submission formInfo = new Submission(fi, formDefinition, datastore, user);
			formInfoForm = new Form(formInfo, datastore, user);
			
			// and now configure the SubmissionAssociation table.
			
		}
		return formDefinition;
	}
	
	private static final FormElementModel findElement(FormElementModel group, DataField backingKey) {
		for ( FormElementModel m : group.getChildren()) {
			if ( m.isMetadata() ) continue;
			if ( m.getFormDataModel().getBackingKey() == backingKey ) return m;
		}
		return null;
	}
	
	static final Form getFormInfoForm(Datastore datastore) throws ODKDatastoreException {
		getFormDefinition(datastore);
		return formInfoForm;
	}

	public static final void populateBackingTableMap(Map<String, DynamicCommonFieldsBase> backingTableMap) {
		try {
		    UserService userService = (UserService) ContextFactory.get().getBean(
		    		BeanDefs.USER_BEAN);
		    User user = userService.getDaemonAccountUser();
		    Datastore datastore = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);

		    DynamicCommonFieldsBase b;
			b = FormInfoTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoDescriptionTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoFilesetTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
			b = FormInfoSubmissionTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
		} catch (ODKDatastoreException e) {
			throw new IllegalStateException("the relations should already have been created");
		}
	}
	
	public static final void setFormDescription( Submission aFormDefinition, String languageCodeValue, String formNameValue, String descriptionValue, String descriptionUrlValue, Datastore datastore, User user ) throws ODKDatastoreException {
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
			SubmissionSet sDescription = new SubmissionSet(aFormDefinition, descriptions.size()+1L, fiDescriptionTable, formDefinition, aFormDefinition.getKey(), datastore, user);
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
	
	public static final void setXFormDefinition( Submission aFormDefinition, Long rootModelVersion, Long rootUiVersion, String title, byte[] definition, Datastore datastore, User user ) throws ODKDatastoreException, ODKConversionException {
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiFilesetTable);
	    List<SubmissionSet> filesets = r.getSubmissionSets();
	    SubmissionSet matchingSet = null;
	    for ( SubmissionSet f : filesets ) {
	    	Long rootModel = ((LongSubmissionType) f.getElementValue(rootElementModelVersion)).getValue();
	    	Long rootUi = ((LongSubmissionType) f.getElementValue(rootElementUiVersion)).getValue();
	    	if ((rootModelVersion == rootModel) && (rootUiVersion == rootUi)) {
	    		matchingSet = f;
	    	}
	    }
	    
	    if ( matchingSet == null ) {
	    	// create a matching set...
			SubmissionSet sFileset = new SubmissionSet(aFormDefinition, filesets.size()+1L, fiFilesetTable, formDefinition, aFormDefinition.getKey(), datastore, user);
			((LongSubmissionType) sFileset.getElementValue(rootElementModelVersion)).setValueFromString(rootModelVersion == null ? null : rootModelVersion.toString());
			((LongSubmissionType) sFileset.getElementValue(rootElementUiVersion)).setValueFromString(rootUiVersion == null ? null : rootUiVersion.toString());
			((BooleanSubmissionType) sFileset.getElementValue(isFilesetComplete)).setValueFromString("yes");
			((BooleanSubmissionType) sFileset.getElementValue(isDownloadAllowed)).setValueFromString("yes");
			r.addSubmissionSet(sFileset);
			matchingSet = sFileset;
	    }

	    BlobSubmissionType bt = (BlobSubmissionType) matchingSet.getElementValue(FormInfo.xformDefinition);
	    bt.setValueFromByteArray(definition, "text/xml", Long.valueOf(definition.length), title + ".xml",
	        datastore, user);
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
	 * @throws ODKDatastoreException
	 * @throws ODKConversionException
	 */
	public static final void setXFormMediaFile( Submission aFormDefinition, Long rootModelVersion, Long rootUiVersion, MultiPartFormItem item, Datastore datastore, User user ) throws ODKDatastoreException, ODKConversionException {
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiFilesetTable);
	    List<SubmissionSet> filesets = r.getSubmissionSets();
	    SubmissionSet matchingSet = null;
	    for ( SubmissionSet f : filesets ) {
	    	Long rootModel = ((LongSubmissionType) f.getElementValue(rootElementModelVersion)).getValue();
	    	Long rootUi = ((LongSubmissionType) f.getElementValue(rootElementUiVersion)).getValue();
	    	if ((rootModelVersion == rootModel) && (rootUiVersion == rootUi)) {
	    		matchingSet = f;
	    	}
	    }
	    
	    if ( matchingSet == null ) {
	    	// create a matching set...
			SubmissionSet sFileset = new SubmissionSet(aFormDefinition, filesets.size()+1L, fiFilesetTable, formDefinition, aFormDefinition.getKey(), datastore, user);
			((LongSubmissionType) sFileset.getElementValue(rootElementModelVersion)).setValueFromString(rootModelVersion == null ? null : rootModelVersion.toString());
			((LongSubmissionType) sFileset.getElementValue(rootElementUiVersion)).setValueFromString(rootUiVersion == null ? null : rootUiVersion.toString());
			((BooleanSubmissionType) sFileset.getElementValue(isFilesetComplete)).setValueFromString("yes");
			((BooleanSubmissionType) sFileset.getElementValue(isDownloadAllowed)).setValueFromString("yes");
			r.addSubmissionSet(sFileset);
			matchingSet = sFileset;
	    }

	    BlobSubmissionType bt = (BlobSubmissionType) matchingSet.getElementValue(FormInfo.manifestFileset);
	    try {
	    	String filePath = item.getFilename();
	    	if ( filePath.indexOf("/") != -1 ) {
	    		filePath = filePath.substring(filePath.indexOf("/")+1);
	    	}
			bt.setValueFromByteArray(item.getStream().toString(HtmlConsts.UTF8_ENCODE).getBytes(), item.getContentType(), item.getContentLength(), filePath,
			    datastore, user);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("encoding conversion error for UTF-8", e);
		}
	}

	public static void setFormSubmission(Submission aFormDefinition, String submissionFormIdValue,
			Long modelVersionValue, Long uiVersionValue, Datastore datastore, User user) throws ODKDatastoreException {
		if ( submissionFormIdValue == null ) {
			throw new IllegalArgumentException("submission form id cannot be null");
		}
	    RepeatSubmissionType r = (RepeatSubmissionType) aFormDefinition.getElementValue(FormInfo.fiSubmissionTable);
	    List<SubmissionSet> submissionDefns = r.getSubmissionSets();

	    for ( SubmissionSet f : submissionDefns ) {
	    	String formIdStr = ((StringSubmissionType) f.getElementValue(submissionFormId)).getValue();
	    	Long subModel = ((LongSubmissionType) f.getElementValue(submissionModelVersion)).getValue();
	    	Long subUi = ((LongSubmissionType) f.getElementValue(submissionUiVersion)).getValue();
	    	if (( modelVersionValue == subModel ) && ( uiVersionValue == subUi) &&
	    		( submissionFormIdValue.equals(formIdStr)) ) {
	    		return;
	    	}
	    }
	    
    	// create a matching set...
		SubmissionSet sDescription = new SubmissionSet(aFormDefinition, submissionDefns.size()+1L, fiSubmissionTable, formDefinition, aFormDefinition.getKey(), datastore, user);
		((StringSubmissionType) sDescription.getElementValue(submissionFormId)).setValueFromString(submissionFormIdValue);
		((LongSubmissionType) sDescription.getElementValue(submissionModelVersion)).setValueFromString((modelVersionValue == null) ? null : modelVersionValue.toString());
		((LongSubmissionType) sDescription.getElementValue(submissionUiVersion)).setValueFromString((uiVersionValue == null) ? null : uiVersionValue.toString());
		r.addSubmissionSet(sDescription);
	}
}
