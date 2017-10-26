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

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public interface ExternalService {

  public boolean canBatchSubmissions();
  
  public void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException;

  public void sendSubmissions(List<Submission> submissions, boolean streaming, CallingContext cc) throws ODKExternalServiceException;

  public void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  /**
   * Abandon the action.
   *
   * @throws ODKDatastoreException
   */
  public void abandon(CallingContext cc) throws ODKDatastoreException;

  /**
   * Delete the external service connection record.
   *
   * @throws ODKDatastoreException
   */
  public void delete(CallingContext cc) throws ODKDatastoreException;

  /**
   * Persist status changes to the persistence layer.
   *
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  /**
   * Initiate or restart (e.g., with updated credentials) the publishing attempt.
   *
   * @param cc
   * @throws ODKExternalServiceException
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  public void initiate(CallingContext cc) throws ODKExternalServiceException, ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException;

  /**
   * get the FormServiceCursor for this external service connection.
   *
   * @return
   */
  public FormServiceCursor getFormServiceCursor();

  /**
   * get the descriptive string for the target service (e.g., spreadsheet name)
   *
   * @return
   */
  public String getDescriptiveTargetString();

  /**
   * Transform to external service summary for the gwt interface
   *
   * @return
   */
  public ExternServSummary transform();
}
