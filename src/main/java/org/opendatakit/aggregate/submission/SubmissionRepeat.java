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

import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementModel;

/**
 * Interface for submission repeat that can be used to store a submission repeat
 * in the datastore
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface SubmissionRepeat extends SubmissionValue {

	public FormElementModel getElement();

	public SubmissionKey constructSubmissionKey();

	public List<SubmissionSet> getSubmissionSets();

	public void addSubmissionSet(SubmissionSet submissionSet);

	public SubmissionElement resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts);
	
	public String getUniqueKeyStr();

}
