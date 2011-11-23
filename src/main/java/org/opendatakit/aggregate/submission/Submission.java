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

package org.opendatakit.aggregate.submission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Defines a form submission that can be converted into a datastore entity.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class Submission extends SubmissionSet {

	/**
	 * Construct an empty submission for the given form definition.
	 * 
	 * @param modelVersion
	 * @param uiVersion
	 * @param uriTopLevelGroup -- override the primary key for the top level table. 
	 * @param formDefinition
	 * @param submissionDate
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public Submission(Long modelVersion, Long uiVersion, String uriTopLevelGroup,
			IForm form, Date submissionDate, CallingContext cc) throws ODKDatastoreException {
		super( modelVersion, uiVersion, uriTopLevelGroup, form, cc);
		((TopLevelDynamicBase) getGroupBackingObject()).setSubmissionDate(submissionDate);
	}

	/**
	 * Construct a submission from an entity from the data store
	 * 
	 * @param submission - top level entity of the submission to restore
	 * @param formDefinition - the definition of the form
	 * @param cc - the CallingContext for this request
	 * @throws ODKDatastoreException
	 */
	public Submission(TopLevelDynamicBase submission,
			IForm form, CallingContext cc)
			throws ODKDatastoreException {
		super(null, submission, form.getTopLevelGroupElement(),
				form, cc);
	}
	
	public Submission(String uri, IForm form, CallingContext cc) throws ODKEntityNotFoundException, ODKDatastoreException {
		super(null, (TopLevelDynamicBase) cc.getDatastore().getEntity(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), uri, cc.getCurrentUser()),
				form.getTopLevelGroupElement(), form, cc);
	}

	/**
	 * Get the time that the submission was created/received
	 * 
	 * @return date of submission
	 */
	public Date getSubmissionDate() {
		return ((TopLevelDynamicBase) getGroupBackingObject()).getSubmissionDate();
	}

	public Long getModelVersion() {
		return ((TopLevelDynamicBase) getGroupBackingObject()).getModelVersion();
	}

	public Long getUiVersion() {
		return ((TopLevelDynamicBase) getGroupBackingObject()).getUiVersion();
	}
	
	public void setIsComplete(Boolean value) {
		((TopLevelDynamicBase) getGroupBackingObject()).setIsComplete(value); 
	}
	
	public Boolean isComplete() {
		return ((TopLevelDynamicBase) getGroupBackingObject()).getIsComplete();
	}

	public void setMarkedAsCompleteDate(Date value) {
		((TopLevelDynamicBase) getGroupBackingObject()).setMarkedAsCompleteDate(value);
	}

	public Date getMarkedAsCompleteDate() {
		return ((TopLevelDynamicBase) getGroupBackingObject()).getMarkedAsCompleteDate();
	}


	/**
	 * This has 2 modes of operation.
	 * (1) If propertyNames is null, then the types list of FormElementNamespace values is 
	 * used to render the output.
	 * (2) Otherwise, this works as a two-stage filter. The types list of FormElementNamespace
	 * values is a filter against the list of propertyNames specified.  So if you have an 
	 * arbitrary list of elements and want only the metadata elements to be reported, you
	 * would pass [ METADATA ] in the types list.  The resulting subset is then rendered
	 * (and the resulting row might have no columns).
	 *  
	 * @param types -- list of e.g., (METADATA, VALUES) to be rendered.
	 * @param propertyNames -- joint subset of property names to be rendered.
	 * @param elemFormatter 
	 * @param includeParentUid
	 * @param cc
	 * @return rendered Row object
	 * @throws ODKDatastoreException
	 */
	public Row getFormattedValuesAsRow(List<FormElementNamespace> types, List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter, boolean includeParentUid, CallingContext cc) throws ODKDatastoreException {

		if ( propertyNames == null ) {
			Row row = new Row(constructSubmissionKey(null));
			getFormattedNamespaceValuesForRow(row, types, elemFormatter, includeParentUid, cc);
			return row;
		}
		
		// otherwise, apply filtering...
		boolean hasMeta = false;
		boolean hasValues = false;
		for ( FormElementNamespace type : types ) {
			if ( type == FormElementNamespace.METADATA ) {
				hasMeta = true;
			} else if ( type == FormElementNamespace.VALUES ) {
				hasValues = true;
			} else {
				throw new IllegalStateException("unexpected FormElementNamespace value " + type.toString());
			}
		}
		
		List<FormElementModel> reducedProperties = new ArrayList<FormElementModel>();
		for ( FormElementModel m : propertyNames ) {
			if ( m.isMetadata() && hasMeta ) {
				reducedProperties.add(m);
			} else if ( !m.isMetadata() && hasValues ) {
				reducedProperties.add(m);
			}
		}
		
		return getFormattedValuesAsRow(reducedProperties,	elemFormatter, includeParentUid, cc);
	}
	
	/**
	 * Given the list of FormElementNamespaces to render, this renders the namespaces in the order given.
	 * 
	 * @param row
	 * @param types
	 * @param elemFormatter
	 * @param includeParentUid
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public void getFormattedNamespaceValuesForRow(Row row, List<FormElementNamespace> types,
			ElementFormatter elemFormatter, boolean includeParentUid, CallingContext cc) throws ODKDatastoreException {
		List<FormElementModel> elementList = new ArrayList<FormElementModel>();
		// get the in-order list of all flattened elements within this submission set...
		List<FormElementModel> allElements = getFormElements();
		// and now place them in the proper ordering according to the sequence of types in the types list.
		for ( FormElementNamespace type : types ) {
			if ( type == FormElementNamespace.METADATA ) {
				for ( FormElementModel m : allElements ) {
					if ( m.isMetadata() ) {
						elementList.add(m);
					}
				}
			} else if ( type == FormElementNamespace.VALUES ) {
				for ( FormElementModel m : allElements ) {
					if ( !m.isMetadata() ) {
						elementList.add(m);
					}
				}
			}
		}
		populateFormattedValuesInRow(row, elementList, elemFormatter, cc);
	}

	public SubmissionElement resolveSubmissionKey(List<SubmissionKeyPart> parts) {
		
		if (parts == null || parts.size() == 0 ) {
			throw new IllegalArgumentException("submission key is empty");
		}

		if ( !parts.get(0).getElementName().equals(getFormId())) {
			throw new IllegalArgumentException(
					"formId of submissionKey does not match FormId");
		}
		
		return resolveSubmissionKeyBeginningAt(1, parts);
	}

	public static final Submission fetchSubmission(List<SubmissionKeyPart> parts,
			CallingContext cc)
			throws ODKFormNotFoundException, ODKDatastoreException {
		if (parts == null || parts.size() == 0 ) {
			throw new IllegalArgumentException("submission key is empty");
		}
		IForm form = FormFactory.retrieveFormByFormId(parts.get(0).getElementName(), cc);
	    if ( !form.hasValidFormDefinition() ) {
	    	throw new IllegalArgumentException("Form definition is ill-formed"); // ill-formed definition
	    }

		if (parts.size() < 2) {
			throw new IllegalArgumentException(
					"submission key does not have a top level group");
		}
		SubmissionKeyPart tlg = parts.get(1);
		if (!form.getTopLevelGroupElement().getElementName().equals(tlg.getElementName())) {
			throw new IllegalArgumentException("top level group name: " 
					+ tlg.getElementName()
					+ " is not as expected: "
					+ form.getTopLevelGroupElement().getElementName());
		}
		if ( tlg.getAuri() == null ) {
			throw new IllegalArgumentException("submission key does not have top level auri");
		}
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		TopLevelDynamicBase tle = (TopLevelDynamicBase) ds.getEntity(form
				.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), tlg.getAuri(), user);

		return new Submission(tle, form, cc);
	}
}
