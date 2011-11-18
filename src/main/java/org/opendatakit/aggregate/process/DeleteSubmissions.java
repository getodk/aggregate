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

package org.opendatakit.aggregate.process;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Takes a list of submission keys and performs recursive delete on all elements
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class DeleteSubmissions {

  private List<SubmissionKey> submissionKeys;

  public DeleteSubmissions(List<SubmissionKey> keys) {
    this.submissionKeys = keys;
  }

  public void deleteSubmissions(CallingContext cc) throws ODKOverQuotaException, ODKFormNotFoundException, ODKDatastoreException {
    List<EntityKey> deleteKeys = new ArrayList<EntityKey>();

    for (SubmissionKey submissionKey : submissionKeys) {
      try {
		List<SubmissionKeyPart> parts = submissionKey.splitSubmissionKey();
  		Submission sub = Submission.fetchSubmission(parts, cc);
  		sub.recursivelyAddEntityKeys(deleteKeys, cc);
  		deleteKeys.add(sub.getKey());
      } catch (ODKEntityNotFoundException e) {
        // just move on
      } catch (ODKFormNotFoundException e) {
		  e.printStackTrace();
      }
    }
    cc.getDatastore().deleteEntities(deleteKeys, cc.getCurrentUser());
  }
}
