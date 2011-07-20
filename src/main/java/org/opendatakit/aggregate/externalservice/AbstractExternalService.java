/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import java.util.List;

import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class AbstractExternalService implements ExternalService{
  
  /**
   * Datastore entity holding registration of an external service for a specific
   * form and the cursor position within that form that was last processed by
   * this service.
   */
  protected FormServiceCursor fsc;
  
  protected Form form;
  
  protected ElementFormatter formatter;
  
  protected final HeaderFormatter headerFormatter;
  
  protected AbstractExternalService(Form form, ElementFormatter elemFormatter, HeaderFormatter header, CallingContext cc) {
    this.form = form;
    this.formatter = elemFormatter;
    this.headerFormatter = header;
  }
  
  public void sendSubmissions(List<Submission> submissions, CallingContext cc) throws ODKExternalServiceException {
    for(Submission submission : submissions)  {
      insertData(submission, cc);
    }
  }
  
  public void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    insertData(submission, cc);    
  }
  
  public FormServiceCursor getFormServiceCursor() {
    return fsc;
  }
  
  protected abstract void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException;
  
   
}
