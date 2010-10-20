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

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

/**
 * Defines a form submission that can be converted into a datastore entity.
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class Submission extends SubmissionSet {

	/**
	 * Time submission was created/received
	 */
	private Date submittedTime;

	/**
	 * Construct an empty submission for the ODK ID form
	 * 
	 * @param formDefinition
	 *            the form to base the submission on
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public Submission(FormDefinition formDefinition, Datastore datastore,
			User user) throws ODKDatastoreException {
		super(null, 1L, formDefinition.getTopLevelGroup(), formDefinition,
				null, datastore, user);
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
	public Submission(InstanceDataBase submission,
			FormDefinition formDefinition, Datastore datastore, User user)
			throws ODKDatastoreException {
		super(null, submission, formDefinition.getTopLevelGroup(),
				formDefinition, datastore, user);
		submittedTime = submission.getCreationDate();
	}

	/**
	 * Get the time that the submission was created/received
	 * 
	 * @return date of submission
	 */
	public Date getSubmittedTime() {
		return submittedTime;
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
	public Row getFormattedValuesAsRow(List<FormDataModel> elements,
			ElementFormatter elemFormatter) throws ODKDatastoreException {

		Row row = super.getFormattedValuesAsRow(elements, elemFormatter);

		// TODO: this is a deviation consider revising as the element formatter
		// is not used
		// and order of submission date is not based on order of propertyNames
		if (elements == null
				|| elements.contains(FormatConsts.SUBMISSION_DATE_HEADER)) {
			row.setSubmissionDate(submittedTime);
		}

		return row;
	}

	public SubmissionValue resolveSubmissionKey(List<SubmissionKeyPart> parts) {
		
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
			Datastore datastore, User user, Realm realm)
			throws ODKFormNotFoundException, ODKDatastoreException {
		if (parts == null || parts.size() == 0 ) {
			throw new IllegalArgumentException("submission key is empty");
		}
		Form form = Form.retrieveForm(parts.get(0).getElementName(), datastore, user, realm);
		if (parts.size() < 2) {
			throw new IllegalArgumentException(
					"submission key does not have a top level group");
		}
		SubmissionKeyPart tlg = parts.get(1);
		if (!form.getTopLevelGroup().getElementName().equals(tlg.getElementName())) {
			throw new IllegalArgumentException("top level group name: " 
					+ tlg.getElementName()
					+ " is not as expected: "
					+ form.getTopLevelGroup().getElementName());
		}
		if ( tlg.getAuri() == null ) {
			throw new IllegalArgumentException("submission key does not have top level auri");
		}
		
		InstanceDataBase tle = (InstanceDataBase) datastore.getEntity(form
				.getTopLevelGroup().getBackingObjectPrototype(), tlg.getAuri(), user);

		return new Submission(tle, form.getFormDefinition(), datastore, user);
	}
}
