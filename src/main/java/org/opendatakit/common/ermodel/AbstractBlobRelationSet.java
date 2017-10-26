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
import java.util.List;

import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Base class for manipulating blob sets. The constructors assume that the base
 * name of the table is UPPER_CASE only.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class AbstractBlobRelationSet implements BlobRelationSet {

  private static final String BLOB_SUFFIX = "_blb";
  private static final String VREF_SUFFIX = "_ref";
  private static final String CTNT_SUFFIX = "_bin";

  /**
   * Standard constructor. Use for tables your application knows about and
   * manipulates directly.
   *
   * @param tableName
   *          must be UPPER_CASE beginning with an upper case letter. The actual
   *          table names in the datastore will have 3 leading underscores. 3
   *          tables will be created with differing suffixes.
   * @param cc
   * @throws ODKDatastoreException
   */
  protected AbstractBlobRelationSet(String tableName, CallingContext cc)
      throws ODKDatastoreException {
    if (!tableName.matches(Relation.VALID_UPPER_CASE_NAME_REGEX) || tableName.contains("__")
        || tableName.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE table name beginning with an upper case letter.");
    }
    this.backingBaseTableName = "___" + tableName;
    if (backingBaseTableName.length() > Relation.MAX_PERSISTENCE_NAME_LENGTH - 4) {
      throw new IllegalArgumentException("Backing table name is too long: " + backingBaseTableName);
    }
    this.namespace = TableNamespace.EXTENSION;
    initialize(cc);
  }

  /**
   * Use this constructor to place tableNames in a new namespace. This is useful
   * if you are dynamically creating tables. It allows those tables to be in a
   * different namespace from the tables your app uses to keep track of
   * everything. Aggregate, for example, ensures that submission tables start
   * with an alphabetic character, and that internal tracking tables start with
   * a leading underscore ('_').
   *
   * TableNames cannot collide if their namespaces are different. Namespaces
   * should be short 2-4 character prefixes. The overall length of the table
   * names in the database are limited to about 64 characters, so you want to
   * use short names.
   *
   * @param namespace
   *          must be UPPER_CASE beginning with an upper case letter.
   * @param tableName
   *          must be UPPER_CASE beginning with an upper case letter. The actual
   *          table name in the datastore will be composed of 2 leading
   *          underscores, the namespace string, 2 underscores, and this
   *          tableName string. 3 tables will be created with differing
   *          suffixes.
   * @param cc
   * @throws ODKDatastoreException
   */
  protected AbstractBlobRelationSet(String namespace, String tableName, CallingContext cc)
      throws ODKDatastoreException {
    if (!namespace.matches(Relation.VALID_UPPER_CASE_NAME_REGEX) || namespace.contains("__")
        || namespace.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE namespace name beginning with an upper case letter.");
    }
    if (!tableName.matches(Relation.VALID_UPPER_CASE_NAME_REGEX) || tableName.contains("__")
        || tableName.startsWith("_")) {
      throw new IllegalArgumentException(
          "Expected an UPPER_CASE table name beginning with an upper case letter.");
    }
    this.backingBaseTableName = "__" + namespace + "__" + tableName;
    if (backingBaseTableName.length() > Relation.MAX_PERSISTENCE_NAME_LENGTH - 4) {
      throw new IllegalArgumentException("Backing base table name is too long: "
          + backingBaseTableName);
    }
    this.namespace = TableNamespace.EXTENSION;
    initialize(cc);
  }

  /**
   * This is primarily for accessing the existing tables of form submissions or
   * the Aggregate internal data model. If you aren't accessing those, you
   * should not be using this constructor.
   *
   * @param type
   * @param tableName
   * @param fields
   * @param cc
   * @throws ODKDatastoreException
   */
  protected AbstractBlobRelationSet(TableNamespace type, String tableName, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (!tableName.matches(Relation.VALID_UPPER_CASE_NAME_REGEX)) {
      throw new IllegalArgumentException("Expected an UPPER_CASE table name.");
    }

    if (tableName.length() > Relation.MAX_PERSISTENCE_NAME_LENGTH - 4) {
      throw new IllegalArgumentException("Backing table name is too long: " + tableName);
    }

    switch (type) {
    case SUBMISSIONS:
      // submissions tables never start with a leading underscore.
      if (tableName.charAt(0) == '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingBaseTableName = tableName;
      namespace = TableNamespace.SUBMISSIONS;
      // don't proceed if the binary content table doesn't exist
      if (!ds.hasRelation(ds.getDefaultSchemaName(), tableName + CTNT_SUFFIX, user)) {
        throw new IllegalArgumentException("Submissions table does not exist");
      }
      break;
    case INTERNALS:
      // internal tables to Aggregate start with an underscore
      // followed by an alphanumeric character.
      if (tableName.charAt(0) != '_' || tableName.charAt(1) == '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingBaseTableName = tableName;
      namespace = TableNamespace.INTERNALS;
      // don't proceed if the binary content table doesn't exist
      if (!ds.hasRelation(ds.getDefaultSchemaName(), tableName + CTNT_SUFFIX, user)) {
        throw new IllegalArgumentException("Submissions table does not exist");
      }
      break;
    case EXTENSION:
      // extensions start with at least two underscores...
      if (tableName.charAt(0) != '_' || tableName.charAt(1) != '_') {
        throw new IllegalArgumentException("Invalid Table namespace for tableName: " + tableName);
      }
      backingBaseTableName = tableName;
      namespace = TableNamespace.EXTENSION;
      break;
    default:
      throw new IllegalStateException("Unexpected TableNamespace value");
    }
    initialize(cc);
  }

  /**
   * Create a new blob entity. This entity does not exist in the database until
   * you put() it there.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  @Override
  public BlobEntitySet newBlobEntitySet(CallingContext cc) throws ODKDatastoreException {
    String uri = CommonFieldsBase.newUri();
    // Note that we refer to the BinaryContentManipulator's parentAuri as
    // the uri of the blob set.
    return new BlobEntitySetImpl(uri, uri, new BinaryContentManipulator(uri, uri, ctntRelation,
        vrefRelation, blobRelation), cc);
  }

  /**
   * Create a new blob entity. This entity does not exist in the database until
   * you put() it there.
   *
   * @param uri
   *          primary key of this Blob set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  @Override
  public BlobEntitySet newBlobEntitySet(String uri, CallingContext cc) throws ODKDatastoreException {
    // Note that we refer to the BinaryContentManipulator's parentAuri as
    // the uri of the blob set.
    return new BlobEntitySetImpl(uri, uri, new BinaryContentManipulator(uri, uri, ctntRelation,
        vrefRelation, blobRelation), cc);
  }

  /**
   * Get an existing blob entity.
   *
   * @param uri
   *          primary key of this Blob set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  @Override
  public BlobEntitySet getBlobEntitySet(String uri, CallingContext cc) throws ODKDatastoreException {
    // Note that we refer to the BinaryContentManipulator's parentAuri as
    // the uri of the blob set.
    return new BlobEntitySetImpl(uri, uri, new BinaryContentManipulator(uri, uri, ctntRelation,
        vrefRelation, blobRelation), cc);
  }

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
  @Override
  public BlobEntitySet getBlobEntitySet(String uri, String topLevelUri, CallingContext cc)
      throws ODKDatastoreException {
    // Note that we refer to the BinaryContentManipulator's parentAuri as
    // the uri of the blob set.
    return new BlobEntitySetImpl(uri, topLevelUri, new BinaryContentManipulator(uri, topLevelUri,
        ctntRelation, vrefRelation, blobRelation), cc);
  }

  @Override
  public void putBlobEntitySet(BlobEntitySet e, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException {
    e.persist(cc);
  }

  @Override
  public void deleteBlobEntitySet(BlobEntitySet e, CallingContext cc) throws ODKDatastoreException {
    e.remove(cc);
  }

  @Override
  public void putBlobEntitySets(List<BlobEntitySet> eList, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException {
    for (BlobEntitySet e : eList) {
      e.persist(cc);
    }
  }

  @Override
  public void deleteBlobEntitySets(List<BlobEntitySet> eList, CallingContext cc)
      throws ODKDatastoreException {
    for (BlobEntitySet e : eList) {
      e.remove(cc);
    }
  }

  /**
   * Numerous possibilities for failures here...
   */
  @Override
  public void dropBlobRelationSet(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    Query q = ds.createQuery(ctntRelation, "AbstractBlobRelationSet.dropBlobEntitySet", user);

    List<? extends CommonFieldsBase> bList = q.executeQuery();
    for (CommonFieldsBase b : bList) {
      BinaryContent bc = (BinaryContent) b;
      // NOTE: fetch the ParentAuri because that is what we've been
      // referring to as the uri of the blob set.
      String uri = bc.getParentAuri();
      String topLevelUri = bc.getTopLevelAuri();

      BlobEntitySet bs = getBlobEntitySet(uri, topLevelUri, cc);
      bs.remove(cc);
    }
    ds.dropRelation(blobRelation, user);
    ds.dropRelation(vrefRelation, user);
    ds.dropRelation(ctntRelation, user);
  }

  public static class BlobEntitySetImpl implements BlobEntitySet {

    @Override
    public String getUri() {
      return uri;
    }

    @Override
    public String getTopLevelUri() {
      return topLevelUri;
    }

    @Override
    public int getAttachmentCount(CallingContext cc) throws ODKDatastoreException {
      return m.getAttachmentCount(cc);
    }

    @Override
    public Date getCreationDate(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getCreationDate(ordinal, cc);
    }

    @Override
    public String getCreatorUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getCreatorUriUser(ordinal, cc);
    }

    @Override
    public Date getLastUpdateDate(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getLastUpdateDate(ordinal, cc);
    }

    @Override
    public String getLastUpdateUriUser(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getLastUpdateUriUser(ordinal, cc);
    }

    @Override
    public byte[] getBlob(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getBlob(ordinal, cc);
    }

    @Override
    public String getContentHash(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getContentHash(ordinal, cc);
    }

    @Override
    public Long getContentLength(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getContentLength(ordinal, cc);
    }

    @Override
    public String getContentType(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getContentType(ordinal, cc);
    }

    @Override
    public String getUnrootedFilename(int ordinal, CallingContext cc) throws ODKDatastoreException {
      return m.getUnrootedFilename(ordinal, cc);
    }

    @Override
    public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      m.persist(cc);
    }

    @Override
    public void remove(CallingContext cc) throws ODKDatastoreException {
      m.deleteAll(cc);
    }

    private final String uri;
    private final String topLevelUri;
    private final BinaryContentManipulator m;

    protected BlobEntitySetImpl(String uri, String topLevelUri, BinaryContentManipulator m,
        CallingContext cc) throws ODKDatastoreException {
      this.uri = uri;
      this.topLevelUri = topLevelUri;
      this.m = m;
    }

    @Override
    public BlobSubmissionOutcome addBlob(byte[] byteArray, String contentType,
        String unrootedFilePath, boolean overwriteOK, CallingContext cc)
        throws ODKDatastoreException {
      return m.setValueFromByteArray(byteArray, contentType, unrootedFilePath, overwriteOK, cc);
    }
  }

  public List<BinaryContent> getAllBinaryContents(CallingContext cc) throws ODKDatastoreException {
    Query q = cc.getDatastore().createQuery(ctntRelation, "getAllContents", cc.getCurrentUser());
    @SuppressWarnings("unchecked")
    List<BinaryContent> bc = (List<BinaryContent>) q.executeQuery();
    return bc;
  }

  @SuppressWarnings("unused")
  private final TableNamespace namespace;
  private final String backingBaseTableName;
  private BinaryContent ctntRelation;
  private BinaryContentRefBlob vrefRelation;
  private RefBlob blobRelation;

  private void initialize(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    String schemaName = ds.getDefaultSchemaName();

    synchronized (AbstractBlobRelationSet.class) {
      // create the 3 relation prototypes...
      BinaryContent protoCtntRelation = null;
      BinaryContentRefBlob protoVRefRelation = null;
      RefBlob protoRefBlobRelation = null;

      String ctntName = backingBaseTableName.toLowerCase() + CTNT_SUFFIX;
      String vrefName = backingBaseTableName.toLowerCase() + VREF_SUFFIX;
      String blobName = backingBaseTableName.toLowerCase() + BLOB_SUFFIX;

      // track which ones already exist.
      // if they don't yet exist, if we get a failure,
      // we'll attempt to drop them.
      boolean ctntExists = ds.hasRelation(schemaName, ctntName, user);
      boolean vrefExists = ds.hasRelation(schemaName, vrefName, user);
      boolean blobExists = ds.hasRelation(schemaName, blobName, user);

      protoCtntRelation = new BinaryContent(schemaName, ctntName);
      protoVRefRelation = new BinaryContentRefBlob(schemaName, vrefName);
      protoRefBlobRelation = new RefBlob(schemaName, blobName);

      // create them in the persistence layer
      try {
        ds.assertRelation(protoCtntRelation, user);
        try {
          ds.assertRelation(protoVRefRelation, user);

          try {
            ds.assertRelation(protoRefBlobRelation, user);
          } catch (ODKDatastoreException e) {
            if (!blobExists) {
              try {
                ds.dropRelation(protoRefBlobRelation, user);
              } catch (Exception e1) {
                // ignore
              }
            }
            throw e;
          }
        } catch (ODKDatastoreException e) {
          if (!vrefExists) {
            try {
              ds.dropRelation(protoVRefRelation, user);
            } catch (Exception e1) {
              // ignore
            }
          }
          throw e;
        }
      } catch (ODKDatastoreException e) {
        if (!ctntExists) {
          try {
            ds.dropRelation(protoCtntRelation, user);
          } catch (Exception e1) {
            // ignore
          }
        }
        throw e;
      }

      // OK we have them persisted -- remember the prototypes...
      ctntRelation = protoCtntRelation;
      vrefRelation = protoVRefRelation;
      blobRelation = protoRefBlobRelation;
    }
  }
}
