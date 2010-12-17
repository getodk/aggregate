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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.RefBlob;
import org.opendatakit.aggregate.datamodel.VersionedBinaryContentRefBlob;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Defines a submission blob that can be converted into a data store entity. 
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionBlob {
  
  private final Datastore datastore;
  private final FormDefinition formDefinition;
  private final FormDataModel versionedBinaryContentRefBlobModel;
  private List<VersionedBinaryContentRefBlob> dbBcbEntityList = new ArrayList<VersionedBinaryContentRefBlob>();
  private List<RefBlob> dbRefBlobList = new ArrayList<RefBlob>();
  
  /**
   * Construct an blob entity and store it in the data store
   * @param datastore TODO
   * @throws ODKDatastoreException 
   *    
   */
  public SubmissionBlob(byte [] blob, String uriVersionedContent, FormDataModel versionedBinaryContentRefBlobModel, FormDefinition formDefinition, EntityKey colocationKey, Datastore datastore, User user) throws ODKDatastoreException {

	this.versionedBinaryContentRefBlobModel = versionedBinaryContentRefBlobModel;
	this.datastore = datastore;
	this.formDefinition = formDefinition;
	FormDataModel blobModel = versionedBinaryContentRefBlobModel.getChildren().get(0);
    // get prototype entities...
    VersionedBinaryContentRefBlob bcbRef = (VersionedBinaryContentRefBlob) versionedBinaryContentRefBlobModel.getBackingObjectPrototype();
    RefBlob ref = (RefBlob) blobModel.getBackingObjectPrototype();
    
    // loop to create the VBCRB and RB entries for each part of the larger blob
    long blobLimit = ref.value.getMaxCharLen();
    long i=1;
    for(long index = 0; index < blob.length; index = index + blobLimit) {
    	long endCopy = index + blobLimit;
    	if ( endCopy > blob.length ) endCopy = blob.length;
        byte [] partialBlob = Arrays.copyOfRange(blob, (int) index, (int) endCopy);
        RefBlob eBlob = datastore.createEntityUsingRelation(ref, user);
        eBlob.setTopLevelAuri(colocationKey.getKey());
        eBlob.setValue(partialBlob);
        dbRefBlobList.add(eBlob);
        VersionedBinaryContentRefBlob bcb = datastore.createEntityUsingRelation(bcbRef, user);
        bcb.setTopLevelAuri(colocationKey.getKey());
        bcb.setDomAuri(uriVersionedContent);
        bcb.setSubAuri(eBlob.getUri());
        bcb.setPart(i++);
        dbBcbEntityList.add(bcb);
    	datastore.putEntity(eBlob, user);
    	datastore.putEntity(bcb, user);
    }
  }

  public SubmissionBlob(String uriVersionedContent, FormDataModel versionedBinaryContentRefBlobModel, FormDefinition formDefinition, Datastore datastore, User user) throws ODKDatastoreException {
	  
	this.versionedBinaryContentRefBlobModel = versionedBinaryContentRefBlobModel;
	this.datastore = datastore;
	this.formDefinition = formDefinition;
	FormDataModel blobModel = versionedBinaryContentRefBlobModel.getChildren().get(0);
    // get prototype entities...
    VersionedBinaryContentRefBlob bcbRef = (VersionedBinaryContentRefBlob) versionedBinaryContentRefBlobModel.getBackingObjectPrototype();
    RefBlob ref = (RefBlob) blobModel.getBackingObjectPrototype();

    // gather the ordered list of parts...
    Query q = datastore.createQuery(bcbRef, user);
    q.addFilter(bcbRef.domAuri, FilterOperation.EQUAL, uriVersionedContent);
    q.addSort(bcbRef.part, Direction.ASCENDING);
    List<? extends CommonFieldsBase> bcbList = q.executeQuery(ServletConsts.FETCH_LIMIT);
    for ( CommonFieldsBase cb : bcbList ) {
    	dbBcbEntityList.add((VersionedBinaryContentRefBlob) cb);
    }
    
    // and gather the blob parts themselves...
    for ( VersionedBinaryContentRefBlob b : dbBcbEntityList ) {
    	RefBlob eBlob = datastore.getEntity(ref, b.getSubAuri(), user);
    	if ( eBlob == null ) {
    		throw new IllegalStateException("Missing blob part!");
    	}
    	dbRefBlobList.add(eBlob);
    }
  }

    public String getTopLevelAuri() {
	  if ( dbBcbEntityList.size() == 0 ) {
		  // blob does not exist!
		  return null;
	  }
	  return dbBcbEntityList.get(0).getTopLevelAuri();
    }

	public String getVersionedContentKey() {
		  if ( dbBcbEntityList.size() == 0 ) {
			  return null;
		  }
		  // by construction these should all have the same parent...
		return dbBcbEntityList.get(0).getDomAuri();
	}
  
  public byte [] getBlob() {
	  ByteArrayOutputStream reconstructedBlob = new ByteArrayOutputStream();
	  for ( RefBlob partialBlob : dbRefBlobList ) {
		  byte[] part = partialBlob.getValue();
		  reconstructedBlob.write(part, 0, part.length);
	  }
	  return reconstructedBlob.toByteArray();
  }

	public void recursivelyAddKeys(List<EntityKey> keyList) {
	    for ( VersionedBinaryContentRefBlob e : dbBcbEntityList ) {
	    	keyList.add(new EntityKey( e, e.getUri()));
	    }
	    for ( RefBlob r : dbRefBlobList ) {
	    	keyList.add(new EntityKey( r, r.getUri()));
	    }
	}

	public void persist(Datastore datastore, User user) throws ODKEntityPersistException {
		List<CommonFieldsBase> rows = new ArrayList<CommonFieldsBase>();
		rows.addAll(dbRefBlobList);
		rows.addAll(dbBcbEntityList);
		datastore.putEntities(rows, user);
	}
}
