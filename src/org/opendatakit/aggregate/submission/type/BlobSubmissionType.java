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
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.BinaryContent;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.VersionedBinaryContent;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
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
 * 
 */
public class BlobSubmissionType extends SubmissionFieldBase<SubmissionKey> {
	
	List<BinaryContent> attachments = new ArrayList<BinaryContent>();
	Map<BinaryContent, List<VersionedBinaryContent> > versionedAttachments = new HashMap<BinaryContent, List<VersionedBinaryContent> >();
	Map<VersionedBinaryContent, SubmissionBlob> inMemoryAttachments = new HashMap<VersionedBinaryContent, SubmissionBlob>();

	private FormDefinition formDefinition;
	private EntityKey topLevelTableKey;
	private Datastore datastore;
	private User user;
	private String parentKey;
	private SubmissionKey submissionKey;
	
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
	    FormDataModel vbcDataModel = element.getChildren().get(0);
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
  public BlobSubmissionType(FormDataModel element, String parentKey, EntityKey topLevelTableKey, 
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
   * @throws ODKConversionException
 * @throws ODKDatastoreException 
   * 
   */
  @Override
  public void setValueFromByteArray(byte[] byteArray, String contentType, Long contentLength, String unrootedFilePath, Datastore datastore, User user) throws ODKConversionException, ODKDatastoreException{

	  BinaryContent matchedBc = null;
	  for ( BinaryContent bc : attachments ) {
		  String bcFilePath = bc.getUnrootedFilePath();
		  if ( (bcFilePath == null && unrootedFilePath == null) || (bcFilePath != null && bcFilePath.equals(unrootedFilePath))) {
			  matchedBc = bc;
			  break;
		  }
	  }
	  
	  final String version = CommonFieldsBase.newUri();
	  if ( matchedBc == null ) {
		  // adding a new file...
		  matchedBc = (BinaryContent) datastore.createEntityUsingRelation(element.getBackingObjectPrototype(), topLevelTableKey, user);
		  matchedBc.setParentAuri(parentKey);
		  matchedBc.setOrdinalNumber(attachments.size()+1L);
		  matchedBc.setVersion(version);
		  matchedBc.setUnrootedFilePath(unrootedFilePath);
		  attachments.add(matchedBc);
	  } else {
		  // updating an existing file...
		  matchedBc.setVersion(version);
	  }
	  
	  FormDataModel vbcDataModel = element.getChildren().get(0);
	  VersionedBinaryContent vbcReference = (VersionedBinaryContent) vbcDataModel.getBackingObjectPrototype();
	  VersionedBinaryContent vbc = (VersionedBinaryContent) datastore.createEntityUsingRelation(vbcReference, topLevelTableKey, user);
	  vbc.setParentAuri(matchedBc.getUri());
	  vbc.setOrdinalNumber(1L);
	  vbc.setContentLength(contentLength);
	  vbc.setContentType(contentType);
	  vbc.setVersion(version);
	  
	  List<VersionedBinaryContent> vcList = versionedAttachments.get(matchedBc);
	  if ( vcList == null ) {
		  vcList = new ArrayList<VersionedBinaryContent>();
		  versionedAttachments.put(matchedBc, vcList);
	  }
	  vcList.add(vbc);
	  // persist the top level linkages...
	  datastore.putEntity(vbc, user);
	  datastore.putEntity(matchedBc, user);
	  
	  // and create the SubmissionBlob (persisting it...)
      try {
		SubmissionBlob subBlob = new SubmissionBlob(byteArray, vbc.getUri(), vbcDataModel.getChildren().get(0), formDefinition, topLevelTableKey, datastore, user);
	  } catch (ODKDatastoreException e) {
		throw new ODKConversionException(e);
	  }
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


  /**
   * Get submission field value from database entity
   * 
   *  @param dbEntity entity to obtain value
 * @throws ODKDatastoreException 
   */
  @Override
  public void getValueFromEntity(CommonFieldsBase dbEntity, String uriAssociatedRow,
		  					EntityKey topLevelTableKey, Datastore datastore, User user, boolean fetchData) throws ODKDatastoreException {
		
		BinaryContent ctnt = (BinaryContent) element.getBackingObjectPrototype();
		VersionedBinaryContent vc = (VersionedBinaryContent) element.getChildren().get(0).getBackingObjectPrototype();
		Query q = datastore.createQuery(ctnt, user);
		q.addFilter(ctnt.parentAuri, FilterOperation.EQUAL, uriAssociatedRow);
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

	  // no-op...
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
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   */ 
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row) throws ODKDatastoreException {
    elemFormatter.formatBinary(getValue(), element.getElementName(), row);
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

        FormDataModel vbcDataModel = element.getChildren().get(0);
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
}
