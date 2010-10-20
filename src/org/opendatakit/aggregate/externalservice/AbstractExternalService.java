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

import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.Submission;

public abstract class AbstractExternalService implements ExternalService{

  public static String APP_NAME = "Aggregate";
  
  protected FormDefinition formDefinition;
  
  protected ElementFormatter formatter;

  protected AbstractExternalService(FormDefinition formDefinition, ElementFormatter elemFormatter) {
    this.formDefinition = formDefinition;
    formatter = elemFormatter;
  }
  
  public void sendSubmissions(List<Submission> submissions) throws ODKExternalServiceException {
    for(Submission submission : submissions)  {
      insertData(submission);
    }
  }
  
  public void sendSubmission(Submission submission) throws ODKExternalServiceException {
    insertData(submission);    
  }
  
  protected abstract void insertData(Submission submission) throws ODKExternalServiceException;
  
   
}
