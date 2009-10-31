package org.odk.aggregate.submission;

import org.odk.aggregate.constants.PersistConsts;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Defines a submission blob that can be converted into a data store entity. 
 *
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionBlob {
  
  private Entity dbEntity;
  
  private Key key;
  
  private Blob blob;
  
  private Key parentKey;
  
  private String contentType;
  
  /**
   * Construct an blob entity and store it in the data store
   *    
   */
  public SubmissionBlob(Blob blob, Key parent, String contentType) {
    this.blob = blob;
    this.parentKey = parent;
    
    // construct entity
    dbEntity = new Entity(PersistConsts.BLOB_STORE_KIND);
    dbEntity.setProperty(PersistConsts.BLOB_PROPERTY, blob);
    dbEntity.setProperty(PersistConsts.PARENT_KEY_PROPERTY, parent);    
    dbEntity.setProperty(PersistConsts.CONTENT_TYPE_PROPERTY, contentType);
    
    // put entity in the datastore
    key = DatastoreServiceFactory.getDatastoreService().put(dbEntity);
  }

  public SubmissionBlob(Key blobKey) throws EntityNotFoundException {
    dbEntity = DatastoreServiceFactory.getDatastoreService().get(blobKey);
    key = blobKey;
    blob = (Blob) dbEntity.getProperty(PersistConsts.BLOB_PROPERTY);
    parentKey = (Key) dbEntity.getProperty(PersistConsts.PARENT_KEY_PROPERTY);
    contentType = (String) dbEntity.getProperty(PersistConsts.CONTENT_TYPE_PROPERTY);
  }

  public Key getKey() {
    return key;
  }

  public Blob getBlob() {
    return blob;
  }

  public Key getParentKey() {
    return parentKey;
  }

  public String getContentType() {
      return contentType;
  }

}
