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

import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * Defines a form submission that can be converted into a datastore entity.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class Submission extends SubmissionSet {

	/**
	 * Time submission was created/received
	 */
	private Date submittedTime;

	/**
	 * Construct an empty submission for the given form definition
	 * 
	 * @param formDefinition
	 *            the form to base the submission on
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public Submission(Long modelVersion, Long uiVersion, 
			FormDefinition formDefinition, Datastore datastore,
			User user) throws ODKDatastoreException {
		this(modelVersion, uiVersion, null, formDefinition, datastore, user);
	}

	/**
	 * Construct an empty submission for the given form definition.
	 * 
	 * @param modelVersion
	 * @param uiVersion
	 * @param uriTopLevelGroup -- override the primary key for the top level table. 
	 * @param formDefinition
	 * @param datastore
	 * @param user
	 * @throws ODKDatastoreException
	 */
	public Submission(Long modelVersion, Long uiVersion, String uriTopLevelGroup,
			FormDefinition formDefinition, Datastore datastore,
			User user) throws ODKDatastoreException {
		super( modelVersion, uiVersion, uriTopLevelGroup, formDefinition, datastore, user);
		submittedTime = new Date();
	}

	/**
	 * Construct a submission from an entity from the data store
	 * 
	 * @param submission
	 *            submission entity that contains the data
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public Submission(TopLevelDynamicBase submission,
			FormDefinition formDefinition, Datastore datastore, User user)
			throws ODKDatastoreException {
		super(null, submission, formDefinition.getTopLevelGroupElement(),
				formDefinition, datastore, user);
		submittedTime = submission.getCreationDate();
	}
	
	public Submission(String uri, Form form, Datastore datastore, User user) throws ODKEntityNotFoundException, ODKDatastoreException {
		super(null, (TopLevelDynamicBase) datastore.getEntity(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), uri, user),
				form.getTopLevelGroupElement(), form.getFormDefinition(), datastore, user);
		submittedTime = super.getCreationDate();
	}

	/**
	 * Get the time that the submission was created/received
	 * 
	 * @return date of submission
	 */
	public Date getSubmittedTime() {
		return submittedTime;
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
	
	/**
	 * Format the submission set for output
	 * 
	 * @param propertyNames
	 *            if null includes all properties, otherwise will only include
	 *            property listed
	 * @param elemFormatter
	 *            formatter to use to properly format the values
	 * @throws ODKDatastoreException
	 */
	@Override
	public Row getFormattedValuesAsRow(List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter, boolean includeParentUid) throws ODKDatastoreException {

		Row row = new Row(constructSubmissionKey(null));
		if ( propertyNames == null ) {
			// we are a Submission -- emit the creation date and id...
			elemFormatter.formatString(getKey().getKey(),
					FormElementModel.Metadata.META_INSTANCE_ID.toString(), row);
			elemFormatter.formatLong(getModelVersion(), 
					FormElementModel.Metadata.META_MODEL_VERSION.toString(), row);
			elemFormatter.formatLong(getUiVersion(), 
					FormElementModel.Metadata.META_UI_VERSION.toString(), row);
			elemFormatter.formatDate(getCreationDate(), 
					FormElementModel.Metadata.META_SUBMISSION_DATE.toString(), row);
			// SubmissionSet handles submission-specific elements...
			List<SubmissionValue> values = getSubmissionValues();
			for (SubmissionValue value : values) {
				value.formatValue(elemFormatter, row, BasicConsts.EMPTY_STRING);
			}
		} else {
			for (FormElementModel element : propertyNames) {
				if ( element.isMetadata() ) {
					switch ( element.getType() ) {
					case META_INSTANCE_ID:
						elemFormatter.formatString(getKey().getKey(),
								element.getElementName(), row);
						break;
					case META_SUBMISSION_DATE:
						elemFormatter.formatDate(getCreationDate(), 
								element.getElementName(), row);
						break;
					case META_UI_VERSION:
						elemFormatter.formatLong(getUiVersion(), 
								element.getElementName(), row);
						break;
					case META_MODEL_VERSION:
						elemFormatter.formatLong(getModelVersion(), 
								element.getElementName(), row);
						break;
					}
				} else {
					populateFormattedValueInRow(row, element, elemFormatter);
				}
			}
		}
		return row;
	}

	public SubmissionElement resolveSubmissionKey(List<SubmissionKeyPart> parts) {
		
		if (parts == null || parts.size() == 0 ) {
			throw new IllegalArgumentException("submission key is empty");
		}

		if ( !parts.get(0).getElementName().equals(formDefinition.getFormId())) {
			throw new IllegalArgumentException(
					"formId of submissionKey does not match FormId");
		}
		
		return resolveSubmissionKeyBeginningAt(1, parts);
	}

	public static final Submission fetchSubmission(List<SubmissionKeyPart> parts,
			Datastore datastore, User user)
			throws ODKFormNotFoundException, ODKDatastoreException {
		if (parts == null || parts.size() == 0 ) {
			throw new IllegalArgumentException("submission key is empty");
		}
		Form form = Form.retrieveForm(parts.get(0).getElementName(), datastore, user);
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
		
		TopLevelDynamicBase tle = (TopLevelDynamicBase) datastore.getEntity(form
				.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), tlg.getAuri(), user);

		return new Submission(tle, form.getFormDefinition(), datastore, user);
	}
}
