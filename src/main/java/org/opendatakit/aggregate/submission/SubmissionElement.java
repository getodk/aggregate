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
package org.opendatakit.aggregate.submission;

import org.opendatakit.aggregate.datamodel.FormElementModel;

/**
 * Common base class to SubmissionSet, SubmissionValue
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 */
public interface SubmissionElement {

  /**
   * Get Property Name
   *
   * @return property name
   */
  public String getPropertyName();


  /**
   * Get the submission element's form element model
   *
   * @return form element model
   */
  public FormElementModel getFormElementModel();

  /**
   * Perform a left-to-right depth-first traversal of the submission.
   *
   * @param visitor to invoke on each element in the submission.
   * @return true if the traversal should end (short-circuit) immediately.
   */
  public boolean depthFirstTraversal(SubmissionVisitor visitor);
}
