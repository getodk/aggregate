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

import java.util.List;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * API for the manipulation of a Blob set (e.g., the set of 3 tables used to
 * represent an artibrarily large binary object) in the datastore. See
 * {@link AbstractBlobRelationSet} for the constructor choices.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public interface BlobRelationSet {

  /**
   * Create a new blob entity set. This blob entity set does not exist in the
   * database until you put() it there or add a blob.
   * 
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobEntitySet newBlobEntitySet(CallingContext cc) throws ODKDatastoreException;

  /**
   * Create a new blob entity set. This blob entity set does not exist in the
   * database until you put() it there or add a blob.
   * 
   * @param uri
   *          primary key of this Blob set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobEntitySet newBlobEntitySet(String uri, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get an existing blob entity.
   * 
   * @param uri
   *          primary key of this Blob set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobEntitySet getBlobEntitySet(String uri, CallingContext cc) throws ODKDatastoreException;

  /**
   * Get an existing blob entity. This API is useful for retrieving Blob sets in
   * the SUBMISSIONS or INTERNALS table namespaces.
   * 
   * @param uri
   *          primary key of this Blob set.
   * @param topLevelUri
   *          primary key of the top-level record linked to this Blob set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public BlobEntitySet getBlobEntitySet(String uri, String topLevelUri, CallingContext cc)
      throws ODKDatastoreException;

  /**
   * Persist the blob entity set to the datastore. Only useful if this is an
   * empty blob set. Equivalent to e.persist(cc).
   * 
   * @param e
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public void putBlobEntitySet(BlobEntitySet e, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException;

  /**
   * Remove the blob entity set from the datastore. Equivalent to e.remove(cc).
   * 
   * @param e
   * @param cc
   * @throws ODKDatastoreException
   */
  public void deleteBlobEntitySet(BlobEntitySet e, CallingContext cc) throws ODKDatastoreException;

  /**
   * This is just a convenience method. It may fail midway through saving the
   * list of blob entity sets.
   * 
   * @param e
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public void putBlobEntitySets(List<BlobEntitySet> e, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException;

  /**
   * This is just a convenience method. It may fail midway through deleting the
   * list of blob entity sets.
   * 
   * @param e
   * @param cc
   * @throws ODKDatastoreException
   */
  public void deleteBlobEntitySets(List<BlobEntitySet> e, CallingContext cc)
      throws ODKDatastoreException;

  /**
   * Delete all the blob entity sets and drop their underlying relations
   * (tables).
   * 
   * @param cc
   * @throws ODKDatastoreException
   */
  public void dropBlobRelationSet(CallingContext cc) throws ODKDatastoreException;
}
