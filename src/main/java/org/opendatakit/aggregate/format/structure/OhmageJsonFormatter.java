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
package org.opendatakit.aggregate.format.structure;

import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.OhmageJsonElementFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * TODO: delete?
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class OhmageJsonFormatter implements SubmissionFormatter {

	private List<FormElementModel> propertyNames;

	public OhmageJsonFormatter(List<FormElementModel> selectedColumnNames,
			CallingContext cc) {
		this.propertyNames = selectedColumnNames;
	}

	@Override
	public void beforeProcessSubmissions(CallingContext cc)
			throws ODKDatastoreException {
	}

	@Override
	public void processSubmissionSegment(List<Submission> submissions,
			CallingContext cc) throws ODKDatastoreException {
		for (Submission sub : submissions) {
			OhmageJsonElementFormatter formatter = new OhmageJsonElementFormatter();
			// use this method purely for its side effects
			sub.getFormattedValuesAsRow(propertyNames, formatter, false, cc);
		}
	}

	@Override
	public void afterProcessSubmissions(CallingContext cc)
			throws ODKDatastoreException {
	}

	@Override
	public void processSubmissions(List<Submission> submissions,
			CallingContext cc) throws ODKDatastoreException {
		beforeProcessSubmissions(cc);
		processSubmissionSegment(submissions, cc);
		afterProcessSubmissions(cc);
	}
}
