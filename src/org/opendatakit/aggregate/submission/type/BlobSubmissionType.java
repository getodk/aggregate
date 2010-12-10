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



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.BinaryContent;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.VersionedBinaryContent;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionBlob;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.constants.BasicConsts;
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
 * Data Storage Converter for Blob Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BlobSubmissionType extends SubmissionFieldBase<SubmissionKey> {
	
	List<BinaryContent> attachments = new ArrayList<BinaryContent>();
	Map<BinaryContent, List<VersionedBinaryContent> > versionedAttachments = new HashMap<BinaryContent, List<VersionedBinaryContent> >();
	Map<VersionedBinaryContent, SubmissionBlob> inMemoryAttachments = new HashMap<VersionedBinaryContent, SubmissionBlob>();

	private final FormDefinition formDefinition;
	private final EntityKey topLevelTableKey;
	private final Datastore datastore;
	private final User user;
	private final String parentKey;
	private final SubmissionKey submissionKey;
	
	public int getAttachmentCount() {
		return attachments.size();
	}
	
	public String getCurrentVersion( int ordinal ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		return b.getVersion();
	}
	
	public String getUnrootedFilename( int ordinal ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		return b.getUnrootedFilePath();
	}
	
	/**
	 * Gets the list of all versions for this binary content.
	 * The list is ordered from most-recent to oldest.
	 * 
	 * @param ordinal identifying the binary content
	 * @return List<String> of the versions available.
	 */
	public List<String> getBinaryVersions( int ordinal ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		List<String> versionList = new ArrayList<String>();
		for ( VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			versionList.add(vbc.getVersion());
		}
		
		Collections.reverse(versionList);
		return versionList;
	}
	
	public String getContentType( int ordinal, String version ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for ( VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if ( vbc.getVersion().equals(version) ) {
				return vbc.getContentType();
			}
		}
		throw new IllegalArgumentException("Version does not match a known version");
	}
	
	public String getContentHash( int ordinal, String version ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for ( VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if ( vbc.getVersion().equals(version) ) {
				return vbc.getContentHash();
			}
		}
		throw new IllegalArgumentException("Version does not match a known version");
	}
	
	public Long getContentLength( int ordinal, String version ) {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for ( VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if ( vbc.getVersion().equals(version) ) {
				return vbc.getContentLength();
			}
		}
		throw new IllegalArgumentException("Version does not match a known version");
	}
	

	public byte[] getBlob(int ordinal, String version) throws ODKDatastoreException {
		BinaryContent b = attachments.get(ordinal-1);
		if ( b.getOrdinalNumber() != ordinal ) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
	    FormDataModel vbcDataModel = element.getFormDataModel().getChildren().get(0);
		for ( VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if ( vbc.getVersion().equals(version) ) {
				SubmissionBlob blb = new SubmissionBlob(vbc.getUri(), vbcDataModel.getChildren().get(0), formDefinition, datastore, user);
				return blb.getBlob();
			}
		}
		throw new IllegalArgumentException("Version does not match a known version");
	}
  /**
   * Constructor
   * 
   * @param propertyName Name of submission element
   */
  public BlobSubmissionType(FormElementModel element, String parentKey, EntityKey topLevelTableKey, 
		  	FormDefinition formDefinition, SubmissionKey submissionKey, Datastore datastore, User user) {
    super(element);
    this.parentKey = parentKey;
    this.topLevelTableKey = topLevelTableKey;
    this.formDefinition = formDefinition;
    this.submissionKey = submissionKey;
    this.datastore = datastore;
    this.user = user;
  }

  /**
   * Convert value from byte array to data store blob type. Store blob in blob
   * storage and save the key of the blob storage into submission set.  There
   * can only be one un-named file.
   * 
   * @param byteArray byte form of the value
   * @param submissionSetKey key of submission set that will reference the blob
   * @param contentType type of binary data (NOTE: only used for binary data)
   * @return the outcome of the storage attempt.  md5 hashes are used to determine file equivalence. 
   * @throws ODKDatastoreException 
   * 
   */
  @Override
  public BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray, String contentType, Long contentLength, String unrootedFilePath, Datastore datastore, User user) throws ODKDatastoreException{

	  BlobSubmissionOutcome outcome = BlobSubmissionOutcome.FILE_UNCHANGED;
	  
	  String md5Hash = CommonFieldsBase.newMD5HashUri(byteArray);

	  boolean existingContent = false;
	  BinaryContent matchedBc = null;
	  
	  for ( BinaryContent bc : attachments ) {
		  String bcFilePath = bc.getUnrootedFilePath();
		  if ( (bcFilePath == null && unrootedFilePath == null) || (bcFilePath != null && bcFilePath.equals(unrootedFilePath))) {
			  matchedBc = bc;
			  existingContent = true;
			  break;
		  }
	  }

	  FormDataModel vbcDataModel = element.getFormDataModel().getChildren().get(0);
	  VersionedBinaryContent vbcReference = (VersionedBinaryContent) vbcDataModel.getBackingObjectPrototype();
	  
	  final String version = CommonFieldsBase.newUri();
	  if ( matchedBc == null ) {
		  // adding a new file...
		  outcome = BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
		  matchedBc = (BinaryContent) datastore.createEntityUsingRelation(element.getFormDataModel().getBackingObjectPrototype(), topLevelTableKey, user);
		  matchedBc.setParentAuri(parentKey);
		  matchedBc.setOrdinalNumber(attachments.size()+1L);
		  matchedBc.setVersion(version);
		  matchedBc.setUnrootedFilePath(unrootedFilePath);
		  // later: attachments.add(matchedBc);
	  } else {
		  outcome = BlobSubmissionOutcome.NEW_FILE_VERSION;
		  // updating an existing file... or a no-op if the hash value is the same...
		  List<VersionedBinaryContent> vcList = versionedAttachments.get(matchedBc);
		  if ( vcList != null ) {
			  for ( VersionedBinaryContent vc : vcList ) {
				  if ( vc.getContentHash().equals(md5Hash) ) {
					  // found a version of this content with the same hash (same file).
					  // The content is the same, so we don't need to save the binary.  
					  if ( vc.getVersion().equals(matchedBc.getVersion()) ) {
						  // the current version is this version -- no change
						  return BlobSubmissionOutcome.FILE_UNCHANGED;
					  } else {
						  // the current version is different -- update to this version.
						  matchedBc.setVersion(vc.getVersion());
						  datastore.putEntity(matchedBc, user);
						  return BlobSubmissionOutcome.NEW_FILE_VERSION;
					  }
				  }
			  }
		  }
		  // no version matches -- need to create a new version record and store the binary.
		  // later: matchedBc.setVersion(version);
	  }

	  VersionedBinaryContent vbc = (VersionedBinaryContent) datastore.createEntityUsingRelation(vbcReference, topLevelTableKey, user);
	  vbc.setParentAuri(matchedBc.getUri());
	  vbc.setOrdinalNumber(1L);
	  vbc.setContentLength(contentLength);
	  vbc.setContentType(contentType);
	  vbc.setContentHash(md5Hash);
	  vbc.setVersion(version);
	  
	  List<VersionedBinaryContent> vcList = versionedAttachments.get(matchedBc);
	  if ( vcList == null ) {
		  vcList = new ArrayList<VersionedBinaryContent>();
		  versionedAttachments.put(matchedBc, vcList);
	  }

	  // and create the SubmissionBlob (persisting it...)
      try {
		// persist the version linkage
	    datastore.putEntity(vbc, user);
	    vcList.add(vbc);
		matchedBc.setVersion(version);
		// persist the top level linkages...
		datastore.putEntity(matchedBc, user);
	    if ( !existingContent ) attachments.add(matchedBc);

	    // persist the binary data
		SubmissionBlob subBlob = new SubmissionBlob(byteArray, vbc.getUri(), vbcDataModel.getChildren().get(0), formDefinition, topLevelTableKey, datastore, user);
	    
	  } catch (ODKDatastoreException e) {
		// there may be trash in the database upon failure.
		vcList.remove(vbc);
		try {
			// try to clean up...
			datastore.deleteEntity(new EntityKey(vbc, vbc.getUri()), user);
		} catch ( ODKDatastoreException ex) {
			ex.printStackTrace();
		}
		throw e;
	  }
	  return outcome;
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

  private void refreshFromDatabase() throws ODKDatastoreException {
	// clear our mutable state.
	attachments.clear();
	versionedAttachments.clear();
	inMemoryAttachments.clear();
	  
	BinaryContent ctnt = (BinaryContent) element.getFormDataModel().getBackingObjectPrototype();
	VersionedBinaryContent vc = (VersionedBinaryContent) element.getFormDataModel().getChildren().get(0).getBackingObjectPrototype();
	Query q = datastore.createQuery(ctnt, user);
	q.addFilter(ctnt.parentAuri, FilterOperation.EQUAL, parentKey);
	q.addSort(ctnt.ordinalNumber, Direction.ASCENDING);

	List<? extends CommonFieldsBase> contentHits = q.executeQuery(0);
	attachments.clear();
	for ( CommonFieldsBase cb : contentHits ) {
		attachments.add((BinaryContent) cb);
	}

	for ( BinaryContent c : attachments ) {
		Query qv = datastore.createQuery(vc, user);
		qv.addFilter(vc.parentAuri, FilterOperation.EQUAL, c.getUri());
		qv.addSort(vc.creationDate, Direction.ASCENDING);
		// sort so that the list in earliest-to-latest order...
		List<VersionedBinaryContent> vcList = new ArrayList<VersionedBinaryContent>();
		List<? extends CommonFieldsBase> cbList = qv.executeQuery(0);
		for ( CommonFieldsBase cb : cbList ) {
			vcList.add((VersionedBinaryContent) cb);
		}
		versionedAttachments.put(c, vcList);
	}
  }

  @Override
  public void getValueFromEntity(Datastore datastore, User user) throws ODKDatastoreException {
	refreshFromDatabase();
  }
  
  @Override
  public void persist(Datastore datastore, User user) throws ODKEntityPersistException {
	  // the two items to store are the attachments vector.
	  // and the inMemoryAttachments map.
	  for ( SubmissionBlob b : inMemoryAttachments.values() ) {
		  b.persist(datastore, user);
	  }
	  datastore.putEntities(attachments, user);
  }
  
  /**
   * Restore to a BlobSubmissionType with no attachments at all.
   * 
   * @param datastore
   * @param user
 * @throws ODKDatastoreException 
   */
  public void deleteAll(Datastore datastore, User user) throws ODKDatastoreException {
	  
	  List<EntityKey> keys = new ArrayList<EntityKey>();
	  try {
		recursivelyAddEntityKeys(keys);
		datastore.deleteEntities(keys, user);
	  } finally {
		// re-initialize ourselves...
		refreshFromDatabase();
	  }
  }
  
  /**
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   */ 
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue) throws ODKDatastoreException {
    elemFormatter.formatBinary(this, element.getGroupQualifiedElementName() + ordinalValue, row);
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
    return ( formDefinition.equals(bt.formDefinition) &&
    		 datastore.equals(bt.datastore) &&
    		 user.equals(bt.user) &&
    		 parentKey.equals(bt.parentKey) &&
    		 attachments.equals(bt.attachments) &&
    		 versionedAttachments.equals(bt.versionedAttachments));
  }

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList) throws ODKDatastoreException {

        FormDataModel vbcDataModel = element.getFormDataModel().getChildren().get(0);
		for ( List<VersionedBinaryContent> vcList : versionedAttachments.values()) {
			for ( VersionedBinaryContent vc : vcList ) {
				SubmissionBlob b = new SubmissionBlob(vc.getUri(), vbcDataModel.getChildren().get(0), formDefinition, datastore, user);
				b.recursivelyAddKeys(keyList);
				keyList.add(new EntityKey(vc, vc.getUri()));
			}
		}
		for ( BinaryContent bc : attachments ) {
			keyList.add(new EntityKey(bc, bc.getUri()));
		}
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
	    return super.hashCode() + formDefinition.hashCode() +
	    		datastore.hashCode() + user.hashCode() + parentKey.hashCode()
	    		+ attachments.hashCode() + versionedAttachments.hashCode(); 
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

	  public static class BinaryDescriptor {
		  public final int ordinalNumber;
		  public final String version;
		  
		  public BinaryDescriptor(int ordinalNumber, String version) {
			  this.ordinalNumber = ordinalNumber;
			  this.version = version;
		  }
	  }
	  
	  public BinaryDescriptor findMatchingBinaryContent(String parentUri ) {
		  for ( Map.Entry<BinaryContent,List<VersionedBinaryContent>> bVcList : versionedAttachments.entrySet()) {
			  for ( VersionedBinaryContent vc : bVcList.getValue() ) {
				  if ( vc.getUri().equals(parentUri) ) {
					  BinaryDescriptor bd = new BinaryDescriptor(bVcList.getKey().getOrdinalNumber().intValue(), vc.getVersion());
					  return bd;
				  }
			  }
		  }
		  return null;
	}

	public SubmissionValue resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		// TODO: need to support qualifying the element we want.
		// For now, we assume the requested blob is the first element.
		return this;
	}

	public SubmissionKey generateSubmissionKey(
			int i, String currentVersion) {
		return new SubmissionKey(submissionKey.toString() +
				"[@ordinal=" + Integer.toString(i) + " and @version=" + currentVersion + "]");
	}
}
