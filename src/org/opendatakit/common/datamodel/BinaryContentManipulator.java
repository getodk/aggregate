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
package org.opendatakit.common.datamodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Manipulator class for handling binary attachments.  To use, create an instance
 * of the manipulator class specifying the URI of the entity having the attachment,
 * the URI of the top-level entity that encloses that entity (pass the URI of the
 * parent entity if it is a top-level entity), and the 3 attachment relations
 * that are used to store the attachment -- {@link BinaryContent},
 * {@link BinaryContentRefBlob} and {@link RefBlob} 
 * <p>
 * These 3 attachment relations are able to hold multiple attachments for a given 
 * parent URI, distinguished by ordinal number.  In general, if you have two 
 * different attachments, you would have two separate sets of these 3 
 * attachment relations, one for each distinct attachment.  For submissions, for 
 * example, each binary form element gets its own set of 3 attachment relations.
 * <p>
 *  
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class BinaryContentManipulator {

   public static enum BlobSubmissionOutcome {
      FILE_UNCHANGED, NEW_FILE_VERSION, COMPLETELY_NEW_FILE
   }

   private final String parentKey;
   private final String topLevelKey;

   private final BinaryContent ctntRelation;
   private final BinaryContentRefBlob vrefRelation;
   private final RefBlob blbRelation;

   private final List<BinaryContent> attachments = new ArrayList<BinaryContent>();

   /**
    * Manipulator class for handling an in-memory blob
    * 
    * @author mitchellsundt@gmail.com
    *
    */
   public static class BlobManipulator {

      private List<BinaryContentRefBlob> dbBcbEntityList = new ArrayList<BinaryContentRefBlob>();
      private List<RefBlob> dbRefBlobList = new ArrayList<RefBlob>();

      /**
       * Construct an blob entity and persist it into the data store
       * 
       * @param blob
       * @param uriVersionedContent
       * @param versionedBinaryContentRefBlobModel
       * @param formDefinition
       * @param colocationKey
       * @param cc
       *            - the CallingContext of this request
       * @throws ODKDatastoreException
       */
      public BlobManipulator(byte[] blob, String uriVersionedContent,
            BinaryContentRefBlob bcbRef, RefBlob ref,
            String topLevelKey, CallingContext cc)
            throws ODKDatastoreException {

         // loop to create the VBCRB and RB entries for each part of the
         // larger blob
         long blobLimit = ref.value.getMaxCharLen();
         long i = 1;
         Datastore ds = cc.getDatastore();
         User user = cc.getCurrentUser();
         for (long index = 0; index < blob.length; index = index + blobLimit) {
            long endCopy = index + blobLimit;
            if (endCopy > blob.length)
               endCopy = blob.length;
            byte[] partialBlob = Arrays.copyOfRange(blob, (int) index,
                  (int) endCopy);
            RefBlob eBlob = ds.createEntityUsingRelation(ref, user);
            eBlob.setTopLevelAuri(topLevelKey);
            eBlob.setValue(partialBlob);
            dbRefBlobList.add(eBlob);
            BinaryContentRefBlob bcb = ds
                  .createEntityUsingRelation(bcbRef, user);
            bcb.setTopLevelAuri(topLevelKey);
            bcb.setDomAuri(uriVersionedContent);
            bcb.setSubAuri(eBlob.getUri());
            bcb.setPart(i++);
            dbBcbEntityList.add(bcb);
            ds.putEntity(eBlob, user);
            ds.putEntity(bcb, user);
         }
      }

      public BlobManipulator(String uriVersionedContent,
            BinaryContentRefBlob bcbRef, RefBlob ref,
            CallingContext cc) throws ODKDatastoreException {

         Datastore ds = cc.getDatastore();
         User user = cc.getCurrentUser();
         // gather the ordered list of parts...
         Query q = ds.createQuery(bcbRef, user);
         q.addFilter(bcbRef.domAuri, FilterOperation.EQUAL,
               uriVersionedContent);
         q.addSort(bcbRef.part, Direction.ASCENDING);
         List<? extends CommonFieldsBase> bcbList = q
               .executeQuery(0);
         for (CommonFieldsBase cb : bcbList) {
            dbBcbEntityList.add((BinaryContentRefBlob) cb);
         }

         // and gather the blob parts themselves...
         for (BinaryContentRefBlob b : dbBcbEntityList) {
            RefBlob eBlob = ds.getEntity(ref, b.getSubAuri(), user);
            if (eBlob == null) {
               throw new IllegalStateException("Missing blob part!");
            }
            dbRefBlobList.add(eBlob);
         }
      }

      public String getTopLevelAuri() {
         if (dbBcbEntityList.size() == 0) {
            // blob does not exist!
            return null;
         }
         return dbBcbEntityList.get(0).getTopLevelAuri();
      }

      public String getVersionedContentKey() {
         if (dbBcbEntityList.size() == 0) {
            return null;
         }
         // by construction these should all have the same parent...
         return dbBcbEntityList.get(0).getDomAuri();
      }

      public byte[] getBlob() {
         ByteArrayOutputStream reconstructedBlob = new ByteArrayOutputStream();
         for (RefBlob partialBlob : dbRefBlobList) {
            byte[] part = partialBlob.getValue();
            reconstructedBlob.write(part, 0, part.length);
         }
         return reconstructedBlob.toByteArray();
      }

      public void recursivelyAddKeys(List<EntityKey> keyList) {
         for (BinaryContentRefBlob e : dbBcbEntityList) {
            keyList.add(new EntityKey(e, e.getUri()));
         }
         for (RefBlob r : dbRefBlobList) {
            keyList.add(new EntityKey(r, r.getUri()));
         }
      }

      public void persist(CallingContext cc) throws ODKEntityPersistException {
         List<CommonFieldsBase> rows = new ArrayList<CommonFieldsBase>();
         rows.addAll(dbRefBlobList);
         rows.addAll(dbBcbEntityList);
         cc.getDatastore().putEntities(rows, cc.getCurrentUser());
      }

   }

   public BinaryContentManipulator(String parentKey, String topLevelKey,
         BinaryContent ctntRelation, 
         BinaryContentRefBlob vrefRelation, RefBlob blbRelation) {
      this.parentKey = parentKey;
      this.topLevelKey = topLevelKey;
      this.ctntRelation = ctntRelation;
      this.vrefRelation = vrefRelation;
      this.blbRelation = blbRelation;
   }

   public int getAttachmentCount() {
      return attachments.size();
   }

   /**
    * @param ordinal
    * @return the last update date of this attachment.
    */
   public Date getLastUpdateDate(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getLastUpdateDate();
   }

   /**
    * @param ordinal
    * @return the uri User performing the last update of this attachment.
    */
   public String getLastUpdateUriUser(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getLastUpdateUriUser();
   }

   /**
    * @param ordinal
    * @return the creation date of this attachment.
    */
   public Date getCreationDate(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getCreationDate();
   }

   /**
    * @param ordinal
    * @return the uri User who created this attachment.
    */
   public String getCreatorUriUser(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getCreatorUriUser();
   }

   /**
    * @param ordinal
    * @return the attachment's unrooted file path.
    */
   public String getUnrootedFilename(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getUnrootedFilePath();
   }

   /**
    * @param ordinal
    * @return the content type or null if no content is attached.
    */
   public String getContentType(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getContentType();
   }
   
   public String getContentHash(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getContentHash();
   }

   public Long getContentLength(int ordinal) {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      return b.getContentLength();
   }

   public byte[] getBlob(int ordinal, CallingContext cc)
   throws ODKDatastoreException {
      BinaryContent b = attachments.get(ordinal - 1);
      if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
         // we are somehow out of sync!
         throw new IllegalStateException("missing attachment declaration");
      }
      BlobManipulator blbManipulator = new BlobManipulator(b
            .getUri(), vrefRelation, blbRelation, cc);
      return blbManipulator.getBlob();
   }

   /**
    * Save the attachment to the database.
    *  
    * @param byteArray
    * @param contentType
    * @param contentLength
    * @param unrootedFilePath
    * @param cc
    * @return COMPLETELY_NEW_FILE on successful save; FILE_UNCHANGED on hash equivalence; NEW_FILE_VERSION on save not allowed.
    * @throws ODKDatastoreException
    */
   public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(
         byte[] byteArray, String contentType, Long contentLength,
         String unrootedFilePath, CallingContext cc)
         throws ODKDatastoreException {

      BinaryContentManipulator.BlobSubmissionOutcome outcome = BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;

      String md5Hash = CommonFieldsBase.newMD5HashUri(byteArray);

      boolean existingContent = false;
      BinaryContent matchedBc = null;
      String currentContentHash = null;

      for (BinaryContent bc : attachments) {
         String bcFilePath = bc.getUnrootedFilePath();
         if ((bcFilePath == null) ? (unrootedFilePath == null)
               : (unrootedFilePath != null && bcFilePath
                     .equals(unrootedFilePath))) {
            matchedBc = bc;
            currentContentHash = matchedBc.getContentHash();
            existingContent = true;
            break;
         }
      }

      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();

      if (matchedBc == null || currentContentHash == null) {
         // adding a new file...
         outcome = BinaryContentManipulator.BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
         if ( matchedBc == null ) {
            // create the record...
            matchedBc = (BinaryContent) ds
               .createEntityUsingRelation(ctntRelation, user);
         }
         matchedBc.setTopLevelAuri(topLevelKey);
         matchedBc.setParentAuri(parentKey);
         matchedBc.setOrdinalNumber(attachments.size() + 1L);
         matchedBc.setUnrootedFilePath(unrootedFilePath);
         matchedBc.setContentType(contentType);
         matchedBc.setContentLength(contentLength);
         matchedBc.setContentHash(md5Hash);
         // later: attachments.add(matchedBc);
      } else if ( currentContentHash.equals(md5Hash)) {
         return BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;
      } else {
         return BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION;
      }

      // and create the SubmissionBlob (persisting it...)
      try {
         // persist the top level linkages...
         ds.putEntity(matchedBc, user);
         if (!existingContent)
            attachments.add(matchedBc);

         // persist the binary data
         @SuppressWarnings("unused")
         BlobManipulator subBlob = new BlobManipulator(byteArray, 
               matchedBc.getUri(), vrefRelation, blbRelation, topLevelKey, cc);

      } catch (ODKDatastoreException e) {
         // there may be trash in the database upon failure.
         throw e;
      }
      return outcome;
   }

   public void refreshFromDatabase(CallingContext cc)
         throws ODKDatastoreException {
      // clear our mutable state.
      attachments.clear();

      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      Query q = ds.createQuery(ctntRelation, user);
      q.addFilter(ctntRelation.parentAuri, FilterOperation.EQUAL, parentKey);
      q.addSort(ctntRelation.ordinalNumber, Direction.ASCENDING);

      List<? extends CommonFieldsBase> contentHits = q.executeQuery(0);
      attachments.clear();
      for (CommonFieldsBase cb : contentHits) {
         attachments.add((BinaryContent) cb);
      }
   }

   public void persist(CallingContext cc) throws ODKEntityPersistException {
      // the items to store are the attachments vector.
      cc.getDatastore().putEntities(attachments, cc.getCurrentUser());
   }

   /**
    * Remove this binary content from the datastore.
    * 
    * @param datastore
    * @param user
    * @throws ODKDatastoreException
    */
   public void deleteAll(CallingContext cc) throws ODKDatastoreException {

      List<EntityKey> keys = new ArrayList<EntityKey>();
      try {
         recursivelyAddEntityKeys(keys, cc);
         cc.getDatastore().deleteEntities(keys, cc.getCurrentUser());
      } finally {
         // re-initialize ourselves...
         refreshFromDatabase(cc);
      }
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof BinaryContentManipulator)) {
         return false;
      }
      if (!super.equals(obj)) {
         return false;
      }

      BinaryContentManipulator bt = (BinaryContentManipulator) obj;

      // don't care about in-memory blobs -- they should be read-only
      return (parentKey.equals(bt.parentKey)
            && topLevelKey.equals(bt.topLevelKey)
            && attachments.equals(bt.attachments));
   }

   public void recursivelyAddEntityKeys(List<EntityKey> keyList,
         CallingContext cc) throws ODKDatastoreException {

      for (BinaryContent bc : attachments) {
         BlobManipulator b = new BlobManipulator(bc.getUri(), vrefRelation, blbRelation,
               cc);
         b.recursivelyAddKeys(keyList);
         keyList.add(new EntityKey(bc, bc.getUri()));
      }
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return super.hashCode() + parentKey.hashCode() + 3
            * topLevelKey.hashCode() + attachments.hashCode();
   }
}
