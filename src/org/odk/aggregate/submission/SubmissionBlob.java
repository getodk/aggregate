package org.odk.aggregate.submission;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.odk.aggregate.constants.PersistConsts;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
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
  /**
   * Make size limit approximately 1MB, needs to be smaller to support storing the parent and contentType
   */
  private static final int BLOB_SPLIT_LIMIT = 990000;

  /**
   * Maximum size limit is not 1MB (1024*1024-1) but according to google error on 9/15/2010 the limit is 1000000
   */
  private static final int BLOB_MAX_SIZE = 999999;
  
  private Entity dbEntity;
  
  private Key key;
  
  private byte [] blob;
  
  private Key parentKey;
  
  private String contentType;
  
  /**
   * Construct an blob entity and store it in the data store
   *    
   */
  public SubmissionBlob(byte [] submissionBlob, Key parent, String contentType) {
    this.blob = submissionBlob;
    this.parentKey = parent;
    
    // construct entity
    dbEntity = new Entity(PersistConsts.BLOB_STORE_KIND);
    dbEntity.setProperty(PersistConsts.PARENT_KEY_PROPERTY, parent);    
    dbEntity.setProperty(PersistConsts.CONTENT_TYPE_PROPERTY, contentType);
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    
    if(blob.length > BLOB_SPLIT_LIMIT) {    
      // make many blob entities
      List<Key> blobKeys = new ArrayList<Key>();
      for(int index = 0; index < blob.length; index = index + BLOB_MAX_SIZE) {
        byte [] partialBlob = Arrays.copyOfRange(blob, index, index + BLOB_MAX_SIZE);
        Blob gaeBlob = new Blob(partialBlob);
        Entity partBlobEntity = new Entity(PersistConsts.MULTIPLE_BLOBS_KINDS);
        partBlobEntity.setProperty(PersistConsts.BLOB_PROPERTY, gaeBlob);
        partBlobEntity.setProperty(PersistConsts.BLOB_NUM_PROPERTY, new Long(index / BLOB_MAX_SIZE));
        Key partBlobKey = ds.put(partBlobEntity);
        blobKeys.add(partBlobKey);
      }
      
      dbEntity.setProperty(PersistConsts.BLOB_KEYS_PROPERTY, blobKeys);
    } else {
      // small enough to just add to be a single entry in datastore
      Blob gaeBlob = new Blob(blob);
      dbEntity.setProperty(PersistConsts.BLOB_PROPERTY, gaeBlob);
    }
    
    // put entity in the datastore
    key = ds.put(dbEntity);
  }

  public SubmissionBlob(Key blobKey) throws EntityNotFoundException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    
    dbEntity = ds.get(blobKey);
    key = blobKey;
    parentKey = (Key) dbEntity.getProperty(PersistConsts.PARENT_KEY_PROPERTY);
    contentType = (String) dbEntity.getProperty(PersistConsts.CONTENT_TYPE_PROPERTY);
 
    Blob gaeBlob = (Blob) dbEntity.getProperty(PersistConsts.BLOB_PROPERTY);
    if(gaeBlob != null) {
      // blob was small enough to be a single entry in datastore
      blob = gaeBlob.getBytes();
    } else {
      // reconstruct the multiple blobs into one
      ByteArrayOutputStream reconstructedBlob = new ByteArrayOutputStream();
      List<Key> blobKeys = (List<Key>) dbEntity.getProperty(PersistConsts.BLOB_KEYS_PROPERTY);
      List<Blob> partialBlobs = new ArrayList<Blob>(blobKeys.size());
      for(Key entityKey : blobKeys) {
        Entity partialBlobEntity = ds.get(entityKey);
        Long index = (Long) partialBlobEntity.getProperty(PersistConsts.BLOB_NUM_PROPERTY);
        partialBlobs.add(index.intValue(), (Blob) partialBlobEntity.getProperty(PersistConsts.BLOB_PROPERTY));
      }
      for(Blob partialBlob : partialBlobs) {
        byte [] partialByteArray = partialBlob.getBytes();
        reconstructedBlob.write(partialByteArray, 0, partialByteArray.length);
      }        
      blob = reconstructedBlob.toByteArray();
    }
  }

  public Key getKey() {
    return key;
  }

  public byte[] getBlob() {
    return blob;
  }

  public Key getParentKey() {
    return parentKey;
  }

  public String getContentType() {
      return contentType;
  }

}
