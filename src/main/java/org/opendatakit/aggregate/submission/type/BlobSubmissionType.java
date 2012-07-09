/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Data Storage Converter for Blob Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BlobSubmissionType extends SubmissionFieldBase<SubmissionKey> {

  private final String parentKey;
  private final SubmissionKey submissionKey;
  private final BinaryContentManipulator bcm;

  public int getAttachmentCount(CallingContext cc) throws ODKDatastoreException {
    return bcm.getAttachmentCount(cc);
  }

  public String getUnrootedFilename(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getUnrootedFilename(ordinal, cc);
  }

  public String getContentType(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getContentType(ordinal, cc);
  }

  public String getContentHash(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getContentHash(ordinal, cc);
  }

  public Long getContentLength(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getContentLength(ordinal, cc);
  }
  
  public Date getLastUpdateDate(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getLastUpdateDate(ordinal, cc);
  }

  public byte[] getBlob(int ordinal, CallingContext cc) throws ODKDatastoreException {
    return bcm.getBlob(ordinal, cc);
  }

  /**
   * Constructor
   * 
   * @param propertyName
   *          Name of submission element
   */
  public BlobSubmissionType(FormElementModel element, String parentKey, EntityKey topLevelTableKey,
      SubmissionKey submissionKey) {
    super(element);
    this.parentKey = parentKey;
    this.submissionKey = submissionKey;

    FormDataModel bnDataModel = element.getFormDataModel();
    BinaryContent ctnt = (BinaryContent) bnDataModel.getBackingObjectPrototype();
    FormDataModel ctntRefDataModel = bnDataModel.getChildren().get(0);
    BinaryContentRefBlob ref = (BinaryContentRefBlob) ctntRefDataModel.getBackingObjectPrototype();
    FormDataModel blobModel = ctntRefDataModel.getChildren().get(0);
    RefBlob blb = (RefBlob) blobModel.getBackingObjectPrototype();

    this.bcm = new BinaryContentManipulator(parentKey, topLevelTableKey.getKey(), ctnt, ref, blb);
  }

  /**
   * Convert value from byte array to data store blob type. Store blob in blob
   * storage and save the key of the blob storage into submission set. There can
   * only be one un-named file. If a value for the unrootedFilePath already exists,
   * and if it is different than the supplied byte array, the existing value will
   * not be changed unless overwiteOK is true.
   * 
   * @param byteArray
   *          byte form of the value
   * @param contentType
   *          type of binary data (NOTE: only used for binary data)
   * @param unrootedFilePath
   *          the filename for this byte array
   * @param overwriteOK
   *          true if overwriting an existing value is OK.
   * @param cc
   *          calling context
   * @return the outcome of the storage attempt. md5 hashes are used to
   *         determine file equivalence.
   * @throws ODKDatastoreException
   * 
   */
  @Override
  public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray,
      String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc)
      throws ODKDatastoreException {

    return bcm.setValueFromByteArray(byteArray, contentType, unrootedFilePath, overwriteOK, cc);
  }

  /**
   * Cannot convert blob from a string
   * 
   * @param value
   * @throws ODKConversionException
   */
  @Override
  public void setValueFromString(String value) throws ODKConversionException {
    throw new ODKConversionException(ErrorConsts.NO_STRING_TO_BLOB_CONVERT);
  }

  @Override
  public void getValueFromEntity(CallingContext cc) throws ODKDatastoreException {
    // lazy access when retrieving data from the database
  }

  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    bcm.persist(cc);
  }

  /**
   * Restore to a BlobSubmissionType with no attachments at all.
   * 
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public void deleteAll(CallingContext cc) throws ODKDatastoreException {
    bcm.deleteAll(cc);
  }

  /**
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   */
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue,
      CallingContext cc) throws ODKDatastoreException {
    elemFormatter.formatBinary(this, element, ordinalValue, row, cc);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BlobSubmissionType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }

    BlobSubmissionType bt = (BlobSubmissionType) obj;

    // don't care about in-memory blobs -- they should be read-only
    return (parentKey.equals(bt.parentKey) && bcm.equals(bt.bcm));
  }

  @Override
  public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc)
      throws ODKDatastoreException {
    bcm.recursivelyAddEntityKeys(keyList, cc);
  }

  @Override
  public SubmissionKey getValue() {
    return submissionKey;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + parentKey.hashCode() + bcm.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    SubmissionKey value = getValue();
    return super.toString() + FormatConsts.TO_STRING_DELIMITER
        + (value != null ? value.toString() : BasicConsts.EMPTY_STRING);
  }

  public SubmissionValue resolveSubmissionKeyBeginningAt(int i, List<SubmissionKeyPart> parts) {
    // TODO: need to support qualifying the element we want.
    // For now, we assume the requested blob is the first element.
    return this;
  }

  public SubmissionKey generateSubmissionKey(int i) {
    return new SubmissionKey(submissionKey.toString() + "[@ordinal=" + Integer.toString(i) + "]");
  }
}
