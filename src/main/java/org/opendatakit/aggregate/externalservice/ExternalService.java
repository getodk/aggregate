/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.externalservice;


import java.util.List;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface ExternalService {

  boolean canBatchSubmissions();

  void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException;

  void sendSubmissions(List<Submission> submissions, boolean streaming, CallingContext cc) throws ODKExternalServiceException;

  void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  void delete(CallingContext cc) throws ODKDatastoreException;

  void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  void initiate(CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException;

  FormServiceCursor getFormServiceCursor();

  String getDescriptiveTargetString();

  ExternServSummary transform();
}
