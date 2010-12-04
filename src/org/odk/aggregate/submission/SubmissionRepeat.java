/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission;

import java.util.SortedSet;

/**
 * Interface for submission repeat that can be used to store
 * a submission repeat in the datastore 
 *
 * @author wbrunette@gmail.com
 */
public interface SubmissionRepeat extends SubmissionValue {
  
  public String getKindId();
  
  public SortedSet<SubmissionSet> getSubmissionSets();
  
  public void addSubmissionSet(SubmissionSet submissionSet);
  
}
