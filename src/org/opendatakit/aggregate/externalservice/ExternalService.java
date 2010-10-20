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
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

public interface ExternalService {
  
  public void sendSubmission(Submission submission) throws ODKExternalServiceException;
  
  public void sendSubmissions(List<Submission> submissions) throws ODKExternalServiceException;
  
  public void setUploadCompleted() throws ODKEntityPersistException;
  
  public void delete() throws ODKDatastoreException;
  
  public void persist() throws ODKEntityPersistException;
}
