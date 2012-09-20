/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.ermodel;

import java.util.Date;

import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * API for manipulating sets of blobs. Blobs themselves are stored using
 * multiple entities (rows) across 3 linked relations (tables). The details of
 * these tables is not exposed to the user of this API. Blobs themselves cannot
 * be individually created, updated or deleted.
 * <p>
 * Blob sets are distinguished by their URIs.
 * <p>
 * A blob set contains one or more blobs. The common case is that it contains
 * exactly one blob. Blob sets can be created, persisted {@link #persist}, blobs
 * can be added to them {@link #setValueFromByteArray} (this action immediately
 * persists any changes), and a blob set can be deleted {@link #remove}.
 * <p>
 * Blobs within a blob set are distinguished by their unrooted filepath (which
 * can be null). There can be at most one blob with any given unrooted filepath
 * (including null) within a given blob set.
 * <p>
 * Blobs are inserted into a blob set with the {@link #setValueFromByteArray}
 * method. If a blob with a matching unrooted filepath is already present in
 * the blob set, no update occurs and the method instead returns whether or not
 * the supplied byte array matches that of the stored blob. Thus, if you need to
 * replace a blob within a blob set, you must create a new blob set, copy the
 * retained blobs into that blob set, update links to point to that new blob set
 * (via updating them to the new blob set's URI), and remove the old blob set.
 * For the common case where a Blob set contains a single blob, this is the
 * 4-step process of create blob set, set blob (data), update references to the
 * new blob set, and remove old blob set.
 * <p>
 * The number of blobs in a blob set is determined by the attachment count of
 * the set. If the attachment count is 3, then there are 3 distinct blobs with
 * ordinal numbers { 1, 2, 3 }. Note that the ordinal number is one-based, not
 * zero-based.
 * <p>
 * Retrieval of information about a particular blob relies on the ordinal number
 * for that blob.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public interface BlobEntitySet {
  // primary key
  public String getUri();

  // top level key (used only in the INTERNALS and SUBMISSIONS table namespace).
  public String getTopLevelUri();

  /**
   * Get the number of blobs in this BlobEntitySet.
   * 
   * @return
   */
  public int getAttachmentCount(CallingContext cc) throws ODKDatastoreException;

  // metadata field
  public Date getLastUpdateDate(int ordinal, CallingContext cc) throws ODKDatastoreException;

  // metadata field; of the form: "mailto:user@uw.edu"
  public String getLastUpdateUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException;

  // metadata field
  public Date getCreationDate(int ordinal, CallingContext cc) throws ODKDatastoreException;

  // metadata field; of the form: "mailto:user@uw.edu"
  public String getCreatorUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the hash string of the "ordinal'th" blob. This is an md5 hash and is
   * used to detect equivalent content.
   * 
   * @param ordinal
   *          [1..]
   * @param cc
   *          - the calling context
   * @return
   */
  public String getContentHash(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the byte length of the "ordinal'th" blob.
   * 
   * @param ordinal
   *          [1..]
   * @param cc
   *          - the calling context
   * @return
   */
  public Long getContentLength(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the content type (mime type) of the "ordinal'th" blob.
   * 
   * @param ordinal
   *          [1..]
   * @param cc
   *          - the calling context
   * @return
   */
  public String getContentType(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the unrooted filepath of the "ordinal'th" blob. This can be null and
   * will be unique within this Blob set.
   * 
   * @param ordinal
   *          [1..]
   * @param cc
   *          - the calling context
   * @return
   */
  public String getUnrootedFilename(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get the contents of the "ordinal'th" blob.
   * 
   * @param ordinal
   *          [1..]
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public byte[] getBlob(int ordinal, CallingContext cc) throws ODKDatastoreException;

  /**
   * Save this BlobSet. Only useful for empty blob sets, as inserting a blob
   * always causes an immediate persist of the blob set.
   * 
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  /**
   * Remove the BlobSet from the datastore.
   * 
   * @param cc
   * @throws ODKDatastoreException
   */
  public void remove(CallingContext cc) throws ODKDatastoreException;

  /**
   * Insert the given blob into the Blob set and persist the change to the
   * datastore. If the blob already exists in the Blob set, as indicated by a
   * matching unrootedFilePath, the return value indicates whether or not the
   * byteArray is different from that in the datastore, and
   * <em>no update is performed</em> unless overwriteOK is true.
   * 
   * @param byteArray
   * @param contentType
   * @param unrootedFilePath
   * @param overwriteOK
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobSubmissionOutcome addBlob(byte[] byteArray, String contentType,
      String unrootedFilePath, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException;

}