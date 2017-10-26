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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Manipulator class for handling binary attachments. To use, create an instance
 * of the manipulator class specifying the URI of the entity having the
 * attachment, the URI of the top-level entity that encloses that entity (pass
 * the URI of the parent entity if it is a top-level entity), and the 3
 * attachment relations that are used to store the attachment --
 * {@link BinaryContent}, {@link BinaryContentRefBlob} and {@link RefBlob}
 * <p>
 * These 3 attachment relations are able to hold multiple attachments for a
 * given parent URI, distinguished by ordinal number. In general, if you have
 * two different attachments, you would have two separate sets of these 3
 * attachment relations, one for each distinct attachment. For submissions, for
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

  // these relations have already been asserted on the datastore...
  private final BinaryContent ctntRelation;
  private final BinaryContentRefBlob vrefRelation;
  private final RefBlob blbRelation;

  // implement lazy access to the attachment fields
  private boolean refreshBeforeUse = true;
  private final Map<Long,BinaryContent> attachments = new HashMap<Long,BinaryContent>();

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
     *          - the CallingContext of this request
     * @throws ODKDatastoreException
     */
    public BlobManipulator(byte[] blob, String uriVersionedContent, BinaryContentRefBlob bcbRef,
        RefBlob ref, String topLevelKey, CallingContext cc) throws ODKDatastoreException {

      // loop to create the VBCRB and RB entries for each part of the
      // larger blob
      long blobLimit = ref.value.getMaxCharLen();
      long part = 1L;
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      for (long index = 0; index < blob.length; index = index + blobLimit) {
        long endCopy = index + blobLimit;
        if (endCopy > blob.length)
          endCopy = blob.length;
        byte[] partialBlob = Arrays.copyOfRange(blob, (int) index, (int) endCopy);
        RefBlob eBlob = ds.createEntityUsingRelation(ref, user);
        eBlob.setTopLevelAuri(topLevelKey);
        eBlob.setValue(partialBlob);
        dbRefBlobList.add(eBlob);
        BinaryContentRefBlob bcb = ds.createEntityUsingRelation(bcbRef, user);
        bcb.setTopLevelAuri(topLevelKey);
        bcb.setDomAuri(uriVersionedContent);
        bcb.setSubAuri(eBlob.getUri());
        bcb.setPart(part++);
        dbBcbEntityList.add(bcb);
        ds.putEntity(eBlob, user);
        ds.putEntity(bcb, user);
      }
    }

    public BlobManipulator(String uriVersionedContent, BinaryContentRefBlob bcbRef, RefBlob ref,
        CallingContext cc) throws ODKDatastoreException {

      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      // gather the ordered list of parts...
      Query q = ds.createQuery(bcbRef, "BinaryContentManipulator.BlobManipulator.constructor", user);
      q.addFilter(bcbRef.domAuri, FilterOperation.EQUAL, uriVersionedContent);
      q.addSort(bcbRef.domAuri, Direction.ASCENDING); // gae optimization
      q.addSort(bcbRef.part, Direction.ASCENDING);
      List<? extends CommonFieldsBase> bcbList = q.executeQuery();
      long expectedPart = 1L;
      for (CommonFieldsBase cb : bcbList) {
        BinaryContentRefBlob bcref = (BinaryContentRefBlob) cb;
        Long part = bcref.getPart();
        if ( part == null || part.longValue() != expectedPart ) {
          String errString = "SELECT * FROM " + bcref.getTableName()
              + " WHERE _TOP_LEVEL_AURI = " + bcref.getTopLevelAuri()
              + " AND _DOM_AURI = " + bcref.getDomAuri() + " is missing a reference part OR has extra copies.";
          throw new ODKEnumeratedElementException(errString);
        }
        ++expectedPart;
        dbBcbEntityList.add(bcref);
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

    /**
     * Recursively add the keys for this entry to keyList.
     * Pay attention to the order of insertion so that if
     * we reverse the resulting keyList, we can delete the
     * entities in order and not get into a bad database
     * state. 
     *  
     * @param keyList
     */
    public void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList) {
      HashMap<String, RefBlob> blobs = new HashMap<String, RefBlob>();
      for ( RefBlob r : dbRefBlobList ) {
        blobs.put(r.getUri(), r);
      }
      
      for ( int i = 0 ; i < dbBcbEntityList.size() ; ++i ) {
        BinaryContentRefBlob e = dbBcbEntityList.get(i);
        String sub = e.getSubAuri();
        RefBlob r = blobs.get(sub);
        if ( r != null ) {
          keyList.add(r.getEntityKey());
          blobs.remove(sub);
        }
        keyList.add(e.getEntityKey());
      }
      for (RefBlob r : blobs.values()) {
        keyList.add(r.getEntityKey());
      }
    }

    public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      List<CommonFieldsBase> rows = new ArrayList<CommonFieldsBase>();
      rows.addAll(dbRefBlobList);
      rows.addAll(dbBcbEntityList);
      cc.getDatastore().putEntities(rows, cc.getCurrentUser());
    }

  }

  public BinaryContentManipulator(String parentKey, String topLevelKey, BinaryContent ctntRelation,
      BinaryContentRefBlob vrefRelation, RefBlob blbRelation) {
    this.parentKey = parentKey;
    this.topLevelKey = topLevelKey;
    this.ctntRelation = ctntRelation;
    this.vrefRelation = vrefRelation;
    this.blbRelation = blbRelation;
  }

  private int internalGetAttachmentCount() {
    Long max = 0L;
    for ( Long v : attachments.keySet() ) {
      max = Math.max(max, v);
    }
    return max.intValue();
  }

  public int getAttachmentCount(CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    return internalGetAttachmentCount();
  }

  /**
   * @param ordinal
   * @return the last update date of this attachment.
   */
  public Date getLastUpdateDate(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getLastUpdateDate();
  }

  /**
   * @param ordinal
   * @return the uri User performing the last update of this attachment.
   */
  public String getLastUpdateUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getLastUpdateUriUser();
  }

  /**
   * @param ordinal
   * @return the creation date of this attachment.
   */
  public Date getCreationDate(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getCreationDate();
  }

  /**
   * @param ordinal
   * @return the uri User who created this attachment.
   */
  public String getCreatorUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getCreatorUriUser();
  }

  /**
   * @param ordinal
   * @return the attachment's unrooted file path.
   */
  public String getUnrootedFilename(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getUnrootedFilePath();
  }

  /**
   * @param ordinal
   * @return the content type or null if no content is attached.
   */
  public String getContentType(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return (b.getContentHash() != null) ? b.getContentType() : null;
  }

  public String getContentHash(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return b.getContentHash();
  }

  public Long getContentLength(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    return (b.getContentHash() != null) ? b.getContentLength() : null;
  }

  public byte[] getBlob(int ordinal, CallingContext cc) throws ODKDatastoreException {
    updateAttachments(cc);
    BinaryContent b = attachments.get(Long.valueOf(ordinal));
    if (b == null) {
      // we are somehow out of sync!
      throw new IllegalStateException("missing attachment declaration");
    }
    BlobManipulator blbManipulator = new BlobManipulator(b.getUri(), vrefRelation, blbRelation, cc);
    return blbManipulator.getBlob();
  }

  /**
   * Atomically rename the given source file path to the destination path.
   * Will fail if the destination path already exists.
   *
   * @param unrootedFilePathSrc
   * @param unrootedFilePathDest
   * @param cc
   * @return true if unrootedFilePathSrc doesn't exist or if the rename succeeds
   * @throws ODKDatastoreException
   */
  public boolean renameFilePath( String unrootedFilePathSrc, String unrootedFilePathDest, CallingContext cc ) throws ODKDatastoreException {

    if ( (unrootedFilePathSrc == null) ? (unrootedFilePathDest == null) :
          (unrootedFilePathDest != null && unrootedFilePathSrc.equals(unrootedFilePathDest)) ) {
      // no-op
      return true;
    }

    updateAttachments(cc);

    // search for a matching entry for unrootedFilePath
    BinaryContent matchedBcSrc = null;
    BinaryContent matchedBcDest = null;
    for (BinaryContent bc : attachments.values()) {
      String bcFilePath = bc.getUnrootedFilePath();
      if ((bcFilePath == null) ? (unrootedFilePathSrc == null)
          : (unrootedFilePathSrc != null && bcFilePath.equals(unrootedFilePathSrc))) {
        matchedBcSrc = bc;
      }
      if ((bcFilePath == null) ? (unrootedFilePathDest == null)
          : (unrootedFilePathDest != null && bcFilePath.equals(unrootedFilePathDest))) {
        matchedBcDest = bc;
      }
    }

    if ( matchedBcSrc != null && matchedBcDest != null ) {
      // they both exist -- can't rename...
      return false;
    }

    if ( matchedBcSrc == null ) {
      // assume that this was already renamed...
      return true;
    }

    matchedBcSrc.setUnrootedFilePath(unrootedFilePathDest);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(matchedBcSrc, user);
    return true;
  }

  /**
   * Save the attachment to the database. This can be called in two ways.
   * Everything non-null or unrootedFilePath non-null and everything else null.
   *
   * @param byteArray
   * @param contentType
   * @param unrootedFilePath
   * @param overwriteOK -- if the file exists and is different, must be true to overwrite existing value.
   * @param cc
   * @return COMPLETELY_NEW_FILE on successful save; FILE_UNCHANGED on hash
   *         equivalence; NEW_FILE_VERSION on updating existing file (save not allowed unless overwriteOK).
   * @throws ODKDatastoreException
   */
  public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray,
      String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc)
      throws ODKDatastoreException {

	@SuppressWarnings("unused")
  Long contentLength = (byteArray == null) ? null : Long.valueOf(byteArray.length);
    // search for a matching entry for unrootedFilePath
    BinaryContent matchedBc = null;
    String currentContentHash = null;

    updateAttachments(cc);
    for (BinaryContent bc : attachments.values()) {
      String bcFilePath = bc.getUnrootedFilePath();
      if ((bcFilePath == null) ? (unrootedFilePath == null)
          : (unrootedFilePath != null && bcFilePath.equals(unrootedFilePath))) {
        matchedBc = bc;
        currentContentHash = matchedBc.getContentHash();
        break;
      }
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (byteArray == null && contentType == null) {
      // adding a file entry without any actual file...

      if (matchedBc == null) {
        // create the record...
        matchedBc = (BinaryContent) ds.createEntityUsingRelation(ctntRelation, user);
        matchedBc.setTopLevelAuri(topLevelKey);
        matchedBc.setParentAuri(parentKey);
        matchedBc.setOrdinalNumber(internalGetAttachmentCount() + 1L);
        matchedBc.setUnrootedFilePath(unrootedFilePath);

        // persist the top level linkages...
        ds.putEntity(matchedBc, user);
        attachments.put(matchedBc.getOrdinalNumber(), matchedBc);

        return BinaryContentManipulator.BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
      } else {
        // record already exists (and it might have file data, too)...
        return BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;
      }
    } else if (byteArray != null && contentType != null) {
      // adding a file entry with an actual file...

      String md5Hash = CommonFieldsBase.newMD5HashUri(byteArray);

      if (matchedBc == null || currentContentHash == null) {
        // either
        // - create a new entry with file data
        // or
        // - update an existing file entry that does not have file data...

        // (0) create entry if no matchedBc
        // (1) modify entry to be intermediate update state (null md5 hash).
        // (2) delete the database entries for any incomplete old data.
        // (3) create the database entries for the new data.
        // (4) update contentHash to indicate that data is properly stored.

        boolean newBc = (matchedBc == null);

        if (newBc) {
          // Step (0)
          // create the record...
          matchedBc = (BinaryContent) ds.createEntityUsingRelation(ctntRelation, user);
          matchedBc.setTopLevelAuri(topLevelKey);
          matchedBc.setParentAuri(parentKey);
          matchedBc.setOrdinalNumber(internalGetAttachmentCount() + 1L);
          matchedBc.setUnrootedFilePath(unrootedFilePath);
        }

        // Step (1)
        matchedBc.setContentType(contentType);
        matchedBc.setContentLength(Long.valueOf(byteArray.length));
        ds.putEntity(matchedBc, user);

        if (newBc) {
          // persist was successful -- remember this new record...
          attachments.put(matchedBc.getOrdinalNumber(), matchedBc);
        }

        // Step (2)
        // -- should not have any data. If it does, prior request failed before step 4 completed.
        BlobManipulator b = new BlobManipulator(matchedBc.getUri(), vrefRelation, blbRelation, cc);
        List<EntityKey> keyList = new ArrayList<EntityKey>();
        b.recursivelyAddEntityKeysForDeletion(keyList);
        DeleteHelper.deleteEntities(keyList, cc);

        // Step (3)
        // persist the binary data
        @SuppressWarnings("unused")
        BlobManipulator subBlob = new BlobManipulator(byteArray, matchedBc.getUri(), vrefRelation,
            blbRelation, topLevelKey, cc);

        // Step (4)
        matchedBc.setContentHash(md5Hash);
        ds.putEntity(matchedBc, user);

        return BinaryContentManipulator.BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
      } else if (currentContentHash.equals(md5Hash)) {
        return BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;
      } else {
        if ( !overwriteOK ) {
          return BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION;
        }
        // We are overwriting what was there.
        // We do this by:
        // (1) modify entry to be intermediate update state (null md5 hash).
        // (2) delete the database entries for the old data.
        // (3) create the database entries for the new data.
        // (4) update contentHash to indicate that data is properly stored.

        // Step (1)
        matchedBc.setContentHash(null);
        matchedBc.setContentType(contentType);
        matchedBc.setContentLength(Long.valueOf(byteArray.length));
        ds.putEntity(matchedBc, user);

        // Step (2)
        BlobManipulator b = new BlobManipulator(matchedBc.getUri(), vrefRelation, blbRelation, cc);
        List<EntityKey> keyList = new ArrayList<EntityKey>();
        b.recursivelyAddEntityKeysForDeletion(keyList);
        DeleteHelper.deleteEntities(keyList, cc);

        // Step (3)
        // persist the binary data
        @SuppressWarnings("unused")
        BlobManipulator subBlob = new BlobManipulator(byteArray, matchedBc.getUri(), vrefRelation,
            blbRelation, topLevelKey, cc);

        // Step (4)
        matchedBc.setContentHash(md5Hash);
        ds.putEntity(matchedBc, user);

        return BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION;
      }
    } else {
      throw new IllegalArgumentException("unexpected null values passed into method");
    }
  }

  public synchronized void updateAttachments(CallingContext cc) throws ODKDatastoreException {
    if ( refreshBeforeUse ) {
      // clear our mutable state.
      attachments.clear();

      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      Query q = ds.createQuery(ctntRelation, "BinaryContentManipulator.refreshFromDatabase", user);
      q.addFilter(ctntRelation.parentAuri, FilterOperation.EQUAL, parentKey);
      q.addSort(ctntRelation.parentAuri, Direction.ASCENDING); // GAE work-around
      q.addSort(ctntRelation.ordinalNumber, Direction.ASCENDING);

      List<String> errors = new ArrayList<String>();
      List<? extends CommonFieldsBase> contentHits = q.executeQuery();
      attachments.clear();
      long expectedOrdinal = 1L;
      for (CommonFieldsBase cb : contentHits) {
        BinaryContent bc = (BinaryContent) cb;
        Long ordinal = bc.getOrdinalNumber();
        if ( ordinal == null || ordinal.longValue() != expectedOrdinal ) {
          String errString = "SELECT * FROM " + bc.getTableName()
              + " WHERE _TOP_LEVEL_AURI = " + bc.getTopLevelAuri()
              + " AND _PARENT_AURI = " + bc.getParentAuri() + " is missing an attachment instance OR has extra copies.";
          errors.add(errString);
        }
        attachments.put(expectedOrdinal, bc);
        ++expectedOrdinal;
      }
      refreshBeforeUse = false;
      
      if ( !errors.isEmpty() ) {
        StringBuilder b = new StringBuilder();
        b.append("Attachment errors:");
        for ( String errString : errors ) {
          b.append("\n").append(errString);
        }
        throw new ODKEnumeratedElementException(b.toString());
      }
    }
  }

  public synchronized void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    // if we need to refresh, then we don't have anything to persist...
    if ( !refreshBeforeUse ) {
      // the items to store are the attachments vector.
      cc.getDatastore().putEntities(attachments.values(), cc.getCurrentUser());
    }
  }

  /**
   * Remove this binary content from the datastore.
   *
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public synchronized void deleteAll(CallingContext cc) throws ODKDatastoreException {

    // don't care if there are problems with the attachments -- we are deleting everything.
    try {
      updateAttachments(cc);
    } catch ( ODKEnumeratedElementException e ) {
      // ignore
    }
    boolean success = false;
    List<EntityKey> keys = new ArrayList<EntityKey>();
    try {
      recursivelyAddEntityKeysForDeletion(keys, cc);
      DeleteHelper.deleteEntities(keys, cc);
      success = true;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    } finally {
      refreshBeforeUse = !success;
      if (success) {
        attachments.clear();
      }
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
    return parentKey.equals(bt.parentKey) && topLevelKey.equals(bt.topLevelKey);
  }

  /**
   * Build up the list of entity keys for the attachments and their
   * references and blobs. This is done so that if we delete these in
   * reverse order, we don't get into a bad state.
   * 
   * @param keyList
   * @param cc
   * @throws ODKDatastoreException
   */
  public void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList, CallingContext cc)
      throws ODKDatastoreException {

    updateAttachments(cc);
    for (BinaryContent bc : attachments.values()) {
      if (bc.getContentHash() != null) {
        BlobManipulator b = new BlobManipulator(bc.getUri(), vrefRelation, blbRelation, cc);
        b.recursivelyAddEntityKeysForDeletion(keyList);
      }
      keyList.add(bc.getEntityKey());
    }
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + parentKey.hashCode() + 3 * topLevelKey.hashCode();
  }
}
