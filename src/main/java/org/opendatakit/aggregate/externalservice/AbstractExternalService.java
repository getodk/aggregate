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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
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
  protected final FormServiceCursor fsc;
  
  protected final Form form;
  
  protected final ElementFormatter formatter;
  
  protected final HeaderFormatter headerFormatter;
  
  protected AbstractExternalService(Form form, FormServiceCursor formServiceCursor, ElementFormatter formatter, HeaderFormatter headerFormatter, CallingContext cc) {
    this.form = form;
    this.formatter = formatter;
    this.headerFormatter = headerFormatter;
    this.fsc = formServiceCursor;
  }
  
  
  protected abstract CommonFieldsBase retrieveObjectEntity(); 
  
  protected abstract List<? extends CommonFieldsBase> retrieveRepateElementEntities();
  
  protected abstract void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException;
  
  @Override
  public void sendSubmissions(List<Submission> submissions, CallingContext cc) throws ODKExternalServiceException {
    for(Submission submission : submissions)  {
      insertData(submission, cc);
    }
  }
  
  @Override
  public void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    insertData(submission, cc);    
  }
    
  @Override
  public void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    fsc.setUploadCompleted(true);
    if (fsc.getExternalServicePublicationOption() == ExternalServicePublicationOption.UPLOAD_ONLY) {
      fsc.setOperationalStatus(OperationalStatus.COMPLETED);
    }
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.putEntity(fsc, user);
  }
  
  @Override
  public void abandon(CallingContext cc) throws ODKDatastoreException {
    if (fsc.getOperationalStatus() != OperationalStatus.COMPLETED) {
      fsc.setOperationalStatus(OperationalStatus.ABANDONED);
      persist(cc);
    }
  }
  
  @Override
  public void delete(CallingContext cc) throws ODKDatastoreException {
    CommonFieldsBase serviceEntity = retrieveObjectEntity();
    List<? extends CommonFieldsBase> repeats = retrieveRepateElementEntities();

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (repeats != null) {
      List<EntityKey> keys = new ArrayList<EntityKey>();
      for (CommonFieldsBase repeat : repeats) {
        keys.add(repeat.getEntityKey());
      }
      ds.deleteEntities(keys, user);
      repeats.clear();
    }

    ds.deleteEntity(serviceEntity.getEntityKey(), user);
    ds.deleteEntity(fsc.getEntityKey(), user);
  }
  
  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
   
    CommonFieldsBase serviceEntity = retrieveObjectEntity();
    List<? extends CommonFieldsBase> repeats = retrieveRepateElementEntities();
    
    if(repeats != null) {
      ds.putEntities(repeats, user);
    }
    ds.putEntity(serviceEntity, user);
    ds.putEntity(fsc, user);
  }
  
  @Override
  public FormServiceCursor getFormServiceCursor() {
    return fsc;
  }
  
  @Override
  public ExternServSummary transform() {
    return new ExternServSummary(fsc.getUri(), 
        fsc.getCreatorUriUser(), 
        fsc.getOperationalStatus(),
        fsc.getEstablishmentDateTime(), 
        fsc.getExternalServicePublicationOption(),
        fsc.getUploadCompleted(), 
        fsc.getLastUploadCursorDate(), 
        fsc.getLastStreamingCursorDate(),
        fsc.getExternalServiceType(), 
        getDescriptiveTargetString());
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if (retrieveObjectEntity() != null)
      hashCode += retrieveObjectEntity().hashCode();
    if (fsc != null)
      hashCode += fsc.hashCode();
    return hashCode;
  }
  
  /**
   * Helper function for constructors.
   * 
   */
  protected static FormServiceCursor createFormServiceCursor(Form form, CommonFieldsBase entity, ExternalServicePublicationOption externalServiceOption, ExternalServiceType type, CallingContext cc) throws ODKDatastoreException {
    FormServiceCursor formServiceCursor = FormServiceCursor.createFormServiceCursor(form, type, entity, cc);
    formServiceCursor.setExternalServiceOption(externalServiceOption);
    formServiceCursor.setIsExternalServicePrepared(false); 
    formServiceCursor.setOperationalStatus(OperationalStatus.ESTABLISHED);
    formServiceCursor.setEstablishmentDateTime(new Date());
    formServiceCursor.setUploadCompleted(false);
    return formServiceCursor;
  }
  
  /**
   * Helper function for constructors.
   * 
   * @param parameterTableRelation
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  protected static final <T extends CommonFieldsBase> T newEntity(T parameterTableRelation, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    return ds.createEntityUsingRelation(parameterTableRelation, user);
  }
  
  /**
   * Helper function for constructors.
   * 
   * @param parameterTableRelation
   * @param fsc
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  protected static final <T extends CommonFieldsBase> T retrieveEntity(T parameterTableRelation, FormServiceCursor fsc, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    return ds.getEntity(parameterTableRelation, fsc.getAuriService(), user);
  }
  
}
