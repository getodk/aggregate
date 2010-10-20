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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Defines the aggregate.opendatakit.org:FormInfo form which is used to hold all the 
 * form definitions in Aggregate.
 *  
 * @author mitchellsundt@gmail.com
 *
 */
public class FormInfo {
	private static final String DATAFIELD_SUBMISSION_ENABLED = "SUBMISSION_ENABLED";

	private static final String ELEMENT_NAME_SUBMISSION_ENABLED = "submissionEnabled";

	private static final String DATAFIELD_DOWNLOAD_ENABLED = "DOWNLOAD_ENABLED";

	private static final String ELEMENT_NAME_DOWNLOAD_ENABLED = "downloadEnabled";

	private static final String ELEMENT_NAME_DATA = "data"; // name of top level group

	private static final String ELEMENT_NAME_FORM_ID = "formId";

	private static final String ELEMENT_NAME_FORM_BINARY_CONTENT = "formBinaryContent";

	private static final String DATAFIELD_FORM_ID = "FORM_ID";

	private static final String FORM_INFO_TABLE_NAME = "_form_info";
	
	private static final String FORM_INFO_REF_TEXT = "_form_info_string_txt";

	private static final String FORM_INFO_LONG_STRING_REF_TEXT = "_form_info_string_ref";

	private static final String FORM_INFO_REF_BLOB = "_form_info_blb";

	private static final String FORM_INFO_VERSIONED_BINARY_CONTENT_REF_BLOB = "_form_info_ref";

	private static final String FORM_INFO_VERSIONED_BINARY_CONTENT = "_form_info_vbn";

	private static final String FORM_INFO_BINARY_CONTENT = "_form_info_bin";

	// special value for bootstrapping
	private static final String URI_FORM_ID_VALUE_FORM_INFO = "aggregate.opendatakit.org:FormInfo"; 

	private static FormDefinition formDefinition = null;
	public static InstanceDataBase reference = null;
	public static FormDataModel formId = null;
	public static FormDataModel downloadEnabled = null;
	public static FormDataModel submissionEnabled = null;
	public static FormDataModel formBinaryContent = null;

	/**
	 * Helper function to ensure that the form data model e
	 * @throws ODKDatastoreException 
	 */
	private static final void checkFormDataModel(Datastore datastore, User user) throws ODKDatastoreException {
		FormDefinition formDefinition = FormDefinition.getFormDefinition(
											FormDataModel.URI_FORM_ID_VALUE_FORM_DATA_MODEL, datastore, user);
		
		if ( formDefinition == null ) {
			final FormDataModel fdm = FormDefinition.getFormDataModel(datastore, user);
			
			List<FormDataModel> idDefn = FormDataModel.constructFormDataModel(datastore, fdm, fdm.getSchemaName(), user);
			
			datastore.putEntities(idDefn, user);
			
		    formDefinition = FormDefinition.getFormDefinition(
		    								FormDataModel.URI_FORM_ID_VALUE_FORM_DATA_MODEL, datastore, user);
			
			if ( formDefinition == null ) {
				throw new IllegalStateException("Unable to create form definition definition!");
			}
		}
	}
	
	/**
	 * Return the form definition for the FormInfo submissions.  These are the 
	 * submissions that hold the form definitions.  As a side-efect, all the 
	 * public static variables of this class are initialized to non-null values.
	 * 
	 * @param datastore
	 * @return
	 * @throws ODKDatastoreException 
	 */
	public static FormDefinition getFormDefinition(Datastore datastore) throws ODKDatastoreException {
		if ( formDefinition == null ) {
			// The FormDefn list of registered forms is itself a well-known
			// form within the Aggregate instance.  Load the form definition
			// for that well-known form id.
	        UserService userService = (UserService) ContextFactory.get().getBean(
	                ServletConsts.USER_BEAN);
            User user = userService.getDaemonAccountUser();
		    formDefinition = FormDefinition.getFormDefinition(FormInfo.URI_FORM_ID_VALUE_FORM_INFO, datastore, user);
			if ( formDefinition == null ) {
				// if we don't have FormInfo, we likely don't have FormDataModel...
				checkFormDataModel(datastore, user);
				// doesn't have the form definition tables defined ...
				// need to insert definition records into the fdm table...
				final FormDataModel fdm = FormDefinition.getFormDataModel(datastore, user);
				
				List<FormDataModel> idDefn = FormInfo.constructFormDataModel(datastore, fdm, fdm.getSchemaName(), user);
		
				datastore.putEntities(idDefn, user);
				
			    formDefinition = FormDefinition.getFormDefinition(FormInfo.URI_FORM_ID_VALUE_FORM_INFO, datastore, user);
				
				if ( formDefinition == null ) {
					throw new IllegalStateException("Unable to create form definition definition!");
				}
			}
				
			// and update the FormInfo object so it has the canonical data fields...
			reference = (InstanceDataBase) formDefinition.getTopLevelGroup().getBackingObjectPrototype();
			for ( FormDataModel m : formDefinition.getTopLevelGroup().getChildren() ) {
				if ( m.getElementName().equals(ELEMENT_NAME_FORM_ID)) {
					formId = m;
				} else if ( m.getElementName().equals(ELEMENT_NAME_DOWNLOAD_ENABLED)) {
					downloadEnabled = m;
				} else if ( m.getElementName().equals(ELEMENT_NAME_SUBMISSION_ENABLED)) {
					submissionEnabled = m;
				} else if ( m.getElementName().equals(ELEMENT_NAME_FORM_BINARY_CONTENT)) {
					formBinaryContent = m;
				}
			}
		}
		return formDefinition;
	}
	
	/**
	 * Construct the form data model for the formInfo construct (so it is itself a Submission entity).
	 * 
	 * @param ds
	 * @param fdm
	 * @param schemaName
	 * @param uriUser
	 * @return
	 */
	private static final List<FormDataModel> constructFormDataModel(Datastore ds, FormDataModel fdm, String schemaName, User user) {

		List<FormDataModel> idDefn = new ArrayList<FormDataModel>();
		
		FormDataModel d;
		
		// data record...
		d = ds.createEntityUsingRelation(fdm, null, user);
		idDefn.add(d);
		final String topLevelURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, null);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, "Form Information");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.FORM_NAME.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, null);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		final EntityKey k = new EntityKey(d, d.getUri());

		// data record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String groupURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, topLevelURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_DATA);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.GROUP.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_TABLE_NAME);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// formId record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_FORM_ID);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, DATAFIELD_FORM_ID);
		d.setStringField(fdm.persistAsTable, FORM_INFO_TABLE_NAME);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// downloadEnabled record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 2L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_DOWNLOAD_ENABLED);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.BOOLEAN.toString());
		d.setStringField(fdm.persistAsColumn, DATAFIELD_DOWNLOAD_ENABLED);
		d.setStringField(fdm.persistAsTable, FORM_INFO_TABLE_NAME);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		
		// submissionEnabled record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 3L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_SUBMISSION_ENABLED);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.BOOLEAN.toString());
		d.setStringField(fdm.persistAsColumn, DATAFIELD_SUBMISSION_ENABLED);
		d.setStringField(fdm.persistAsTable, FORM_INFO_TABLE_NAME);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for form binary content...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String bcURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 4L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_FORM_BINARY_CONTENT);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.BINARY.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_BINARY_CONTENT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for form versioned binary content...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String vbcURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, bcURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_FORM_BINARY_CONTENT);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_VERSIONED_BINARY_CONTENT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for form binary content ref blob..
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String bcbURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, vbcURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_FORM_BINARY_CONTENT);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY_CONTENT_REF_BLOB.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_VERSIONED_BINARY_CONTENT_REF_BLOB);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for form ref blob...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, bcbURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, ELEMENT_NAME_FORM_BINARY_CONTENT);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_BLOB.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_REF_BLOB);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for long string ref text...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String lst = d.getUri();
		d.setLongField(fdm.ordinalNumber, 2L);
		d.setStringField(fdm.parentAuri, topLevelURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.LONG_STRING_REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_LONG_STRING_REF_TEXT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for ref text...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, lst);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_INFO);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_INFO_REF_TEXT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		return idDefn;
	}
}
