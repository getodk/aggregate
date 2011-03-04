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

import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.BinaryContent;
import org.opendatakit.aggregate.datamodel.BinaryContentManipulator;
import org.opendatakit.aggregate.datamodel.BinaryContentRefBlob;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.RefBlob;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

/**
 * Data Storage Converter for Blob Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BlobSubmissionType extends SubmissionFieldBase<SubmissionKey> {

	private final CallingContext cc;
	private final String parentKey;
	private final SubmissionKey submissionKey;
	private final BinaryContentManipulator bcm;

	public int getAttachmentCount() {
		return bcm.getAttachmentCount();
	}

	public String getUnrootedFilename(int ordinal) {
		return bcm.getUnrootedFilename(ordinal);
	}

	public String getContentType(int ordinal) {
		return bcm.getContentType(ordinal);
	}

	public String getContentHash(int ordinal) {
		return bcm.getContentHash(ordinal);
	}

	public Long getContentLength(int ordinal) {
		return bcm.getContentLength(ordinal);
	}

	public byte[] getBlob(int ordinal)
			throws ODKDatastoreException {
		return bcm.getBlob(ordinal, cc);
	}

	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            Name of submission element
	 */
	public BlobSubmissionType(FormElementModel element, String parentKey,
			EntityKey topLevelTableKey, FormDefinition formDefinition,
			SubmissionKey submissionKey, CallingContext cc) {
		super(element);
		this.parentKey = parentKey;
		this.submissionKey = submissionKey;
		this.cc = cc;

		FormDataModel bnDataModel = element.getFormDataModel();
		BinaryContent ctnt = (BinaryContent) bnDataModel
				.getBackingObjectPrototype();
		FormDataModel ctntRefDataModel = bnDataModel.getChildren().get(0);
		BinaryContentRefBlob ref = (BinaryContentRefBlob) ctntRefDataModel
				.getBackingObjectPrototype();
		FormDataModel blobModel = ctntRefDataModel.getChildren().get(0);
		RefBlob blb = (RefBlob) blobModel.getBackingObjectPrototype();

		this.bcm = new BinaryContentManipulator(parentKey, topLevelTableKey
				.getKey(), ctnt, ref, blb);
	}

	/**
	 * Convert value from byte array to data store blob type. Store blob in blob
	 * storage and save the key of the blob storage into submission set. There
	 * can only be one un-named file.
	 * 
	 * @param byteArray
	 *            byte form of the value
	 * @param submissionSetKey
	 *            key of submission set that will reference the blob
	 * @param contentType
	 *            type of binary data (NOTE: only used for binary data)
	 * @return the outcome of the storage attempt. md5 hashes are used to
	 *         determine file equivalence.
	 * @throws ODKDatastoreException
	 * 
	 */
	@Override
	public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(
			byte[] byteArray, String contentType, Long contentLength,
			String unrootedFilePath) throws ODKDatastoreException {

		return bcm.setValueFromByteArray(byteArray, contentType, contentLength,
				unrootedFilePath, cc);
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
	public void getValueFromEntity(CallingContext cc)
			throws ODKDatastoreException {
		bcm.refreshFromDatabase(cc);
	}

	@Override
	public void persist(CallingContext cc) throws ODKEntityPersistException {
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
	 *            the element formatter that will convert the value to the
	 *            proper format for output
	 */
	@Override
	public void formatValue(ElementFormatter elemFormatter, Row row,
			String ordinalValue) throws ODKDatastoreException {
		elemFormatter.formatBinary(this, element.getGroupQualifiedElementName()
				+ ordinalValue, row);
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
		return (cc.equals(bt.cc) && parentKey.equals(bt.parentKey) && bcm
				.equals(bt.bcm));
	}

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList)
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
		return super.hashCode() + cc.hashCode() + parentKey.hashCode()
				+ bcm.hashCode();
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

	public SubmissionValue resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		// TODO: need to support qualifying the element we want.
		// For now, we assume the requested blob is the first element.
		return this;
	}

	public SubmissionKey generateSubmissionKey(int i) {
		return new SubmissionKey(submissionKey.toString() + "[@ordinal="
				+ Integer.toString(i) + "]");
	}
}
