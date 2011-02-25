package org.opendatakit.aggregate.datamodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

public class BinaryContentManipulator {

	public static enum BlobSubmissionOutcome {
		FILE_UNCHANGED, NEW_FILE_VERSION, COMPLETELY_NEW_FILE
	}

	private final BinaryContent ctntRelation;
	private final VersionedBinaryContent vbcRelation;
	private final VersionedBinaryContentRefBlob vrefRelation;
	private final RefBlob blbRelation;

	private final String parentKey;
	private final String topLevelKey;

	List<BinaryContent> attachments = new ArrayList<BinaryContent>();
	Map<BinaryContent, List<VersionedBinaryContent>> versionedAttachments = new HashMap<BinaryContent, List<VersionedBinaryContent>>();
	Map<VersionedBinaryContent, BlobManipulator> inMemoryAttachments = new HashMap<VersionedBinaryContent, BlobManipulator>();

	public static class BlobManipulator {

		private List<VersionedBinaryContentRefBlob> dbBcbEntityList = new ArrayList<VersionedBinaryContentRefBlob>();
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
				VersionedBinaryContentRefBlob bcbRef, RefBlob ref,
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
				VersionedBinaryContentRefBlob bcb = ds
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
				VersionedBinaryContentRefBlob bcbRef, RefBlob ref,
				CallingContext cc) throws ODKDatastoreException {

			Datastore ds = cc.getDatastore();
			User user = cc.getCurrentUser();
			// gather the ordered list of parts...
			Query q = ds.createQuery(bcbRef, user);
			q.addFilter(bcbRef.domAuri, FilterOperation.EQUAL,
					uriVersionedContent);
			q.addSort(bcbRef.part, Direction.ASCENDING);
			List<? extends CommonFieldsBase> bcbList = q
					.executeQuery(ServletConsts.FETCH_LIMIT);
			for (CommonFieldsBase cb : bcbList) {
				dbBcbEntityList.add((VersionedBinaryContentRefBlob) cb);
			}

			// and gather the blob parts themselves...
			for (VersionedBinaryContentRefBlob b : dbBcbEntityList) {
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
			for (VersionedBinaryContentRefBlob e : dbBcbEntityList) {
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
			BinaryContent ctnt, VersionedBinaryContent vbc,
			VersionedBinaryContentRefBlob vref, RefBlob blb) {
		this.parentKey = parentKey;
		this.topLevelKey = topLevelKey;
		this.ctntRelation = ctnt;
		this.vbcRelation = vbc;
		this.vrefRelation = vref;
		this.blbRelation = blb;
	}

	public int getAttachmentCount() {
		return attachments.size();
	}

	public String getCurrentVersion(int ordinal) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		return b.getVersion();
	}

	public String getUnrootedFilename(int ordinal) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		return b.getUnrootedFilePath();
	}

	/**
	 * Gets the list of all versions for this binary content. The list is
	 * ordered from most-recent to oldest.
	 * 
	 * @param ordinal
	 *            identifying the binary content
	 * @return List<String> of the versions available.
	 */
	public List<String> getBinaryVersions(int ordinal) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		List<String> versionList = new ArrayList<String>();
		for (VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			versionList.add(vbc.getVersion());
		}

		Collections.reverse(versionList);
		return versionList;
	}

	public String getContentType(int ordinal, String version) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for (VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if (vbc.getVersion().equals(version)) {
				return vbc.getContentType();
			}
		}
		throw new IllegalArgumentException(
				"Version does not match a known version");
	}

	public String getContentHash(int ordinal, String version) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for (VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if (vbc.getVersion().equals(version)) {
				return vbc.getContentHash();
			}
		}
		throw new IllegalArgumentException(
				"Version does not match a known version");
	}

	public Long getContentLength(int ordinal, String version) {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for (VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if (vbc.getVersion().equals(version)) {
				return vbc.getContentLength();
			}
		}
		throw new IllegalArgumentException(
				"Version does not match a known version");
	}

	public byte[] getBlob(int ordinal, String version, CallingContext cc)
			throws ODKDatastoreException {
		BinaryContent b = attachments.get(ordinal - 1);
		if (!Long.valueOf(ordinal).equals(b.getOrdinalNumber())) {
			// we are somehow out of sync!
			throw new IllegalStateException("missing attachment declaration");
		}
		for (VersionedBinaryContent vbc : versionedAttachments.get(b)) {
			if (vbc.getVersion().equals(version)) {
				BlobManipulator blbManipulator = new BlobManipulator(vbc
						.getUri(), vrefRelation, blbRelation, cc);
				return blbManipulator.getBlob();
			}
		}
		throw new IllegalArgumentException(
				"Version does not match a known version");
	}

	public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(
			byte[] byteArray, String contentType, Long contentLength,
			String unrootedFilePath, CallingContext cc)
			throws ODKDatastoreException {

		BinaryContentManipulator.BlobSubmissionOutcome outcome = BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;

		String md5Hash = CommonFieldsBase.newMD5HashUri(byteArray);

		boolean existingContent = false;
		BinaryContent matchedBc = null;

		for (BinaryContent bc : attachments) {
			String bcFilePath = bc.getUnrootedFilePath();
			if ((bcFilePath == null) ? (unrootedFilePath == null)
					: (unrootedFilePath != null && bcFilePath
							.equals(unrootedFilePath))) {
				matchedBc = bc;
				existingContent = true;
				break;
			}
		}

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		final String version = CommonFieldsBase.newUri();
		if (matchedBc == null) {
			// adding a new file...
			outcome = BinaryContentManipulator.BlobSubmissionOutcome.COMPLETELY_NEW_FILE;
			matchedBc = (BinaryContent) ds
					.createEntityUsingRelation(ctntRelation, user);
			matchedBc.setTopLevelAuri(topLevelKey);
			matchedBc.setParentAuri(parentKey);
			matchedBc.setOrdinalNumber(attachments.size() + 1L);
			matchedBc.setVersion(version);
			matchedBc.setUnrootedFilePath(unrootedFilePath);
			// later: attachments.add(matchedBc);
		} else {
			outcome = BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION;
			// updating an existing file... or a no-op if the hash value is the
			// same...
			List<VersionedBinaryContent> vcList = versionedAttachments
					.get(matchedBc);
			if (vcList != null) {
				for (VersionedBinaryContent vc : vcList) {
					if (vc.getContentHash().equals(md5Hash)) {
						// found a version of this content with the same hash
						// (same file).
						// The content is the same, so we don't need to save the
						// binary.
						if (vc.getVersion().equals(matchedBc.getVersion())) {
							// the current version is this version -- no change
							return BinaryContentManipulator.BlobSubmissionOutcome.FILE_UNCHANGED;
						} else {
							// the current version is different -- update to
							// this version.
							matchedBc.setVersion(vc.getVersion());
							ds.putEntity(matchedBc, user);
							return BinaryContentManipulator.BlobSubmissionOutcome.NEW_FILE_VERSION;
						}
					}
				}
			}
			// no version matches -- need to create a new version record and
			// store the binary.
			// later: matchedBc.setVersion(version);
		}

		VersionedBinaryContent vbcEntry = (VersionedBinaryContent) ds
				.createEntityUsingRelation(vbcRelation, user);
		vbcEntry.setTopLevelAuri(topLevelKey);
		vbcEntry.setParentAuri(matchedBc.getUri());
		vbcEntry.setOrdinalNumber(1L);
		vbcEntry.setContentLength(contentLength);
		vbcEntry.setContentType(contentType);
		vbcEntry.setContentHash(md5Hash);
		vbcEntry.setVersion(version);

		List<VersionedBinaryContent> vcList = versionedAttachments
				.get(matchedBc);
		if (vcList == null) {
			vcList = new ArrayList<VersionedBinaryContent>();
			versionedAttachments.put(matchedBc, vcList);
		}

		// and create the SubmissionBlob (persisting it...)
		try {
			// persist the version linkage
			ds.putEntity(vbcEntry, user);
			vcList.add(vbcEntry);
			matchedBc.setVersion(version);
			// persist the top level linkages...
			ds.putEntity(matchedBc, user);
			if (!existingContent)
				attachments.add(matchedBc);

			// persist the binary data
			BlobManipulator subBlob = new BlobManipulator(byteArray, 
					vbcEntry.getUri(), vrefRelation, blbRelation, topLevelKey, cc);

		} catch (ODKDatastoreException e) {
			// there may be trash in the database upon failure.
			vcList.remove(vbcEntry);
			try {
				// try to clean up...
				ds.deleteEntity(new EntityKey(vbcEntry, vbcEntry.getUri()),
						user);
			} catch (ODKDatastoreException ex) {
				ex.printStackTrace();
			}
			throw e;
		}
		return outcome;
	}

	public void refreshFromDatabase(CallingContext cc)
			throws ODKDatastoreException {
		// clear our mutable state.
		attachments.clear();
		versionedAttachments.clear();
		inMemoryAttachments.clear();

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

		for (BinaryContent c : attachments) {
			Query qv = ds.createQuery(vbcRelation, user);
			qv.addFilter(vbcRelation.parentAuri, FilterOperation.EQUAL, c.getUri());
			qv.addSort(vbcRelation.creationDate, Direction.ASCENDING);
			// sort so that the list in earliest-to-latest order...
			List<VersionedBinaryContent> vcList = new ArrayList<VersionedBinaryContent>();
			List<? extends CommonFieldsBase> cbList = qv.executeQuery(0);
			for (CommonFieldsBase cb : cbList) {
				vcList.add((VersionedBinaryContent) cb);
			}
			versionedAttachments.put(c, vcList);
		}
	}

	public void persist(CallingContext cc) throws ODKEntityPersistException {
		// the two items to store are the attachments vector.
		// and the inMemoryAttachments map.
		for (BlobManipulator b : inMemoryAttachments.values()) {
			b.persist(cc);
		}
		cc.getDatastore().putEntities(attachments, cc.getCurrentUser());
	}

	/**
	 * Restore to a BlobSubmissionType with no attachments at all.
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
				&& attachments.equals(bt.attachments) && versionedAttachments
				.equals(bt.versionedAttachments));
	}

	public void recursivelyAddEntityKeys(List<EntityKey> keyList,
			CallingContext cc) throws ODKDatastoreException {

		for (List<VersionedBinaryContent> vcList : versionedAttachments
				.values()) {
			for (VersionedBinaryContent vc : vcList) {
				BlobManipulator b = new BlobManipulator(vc.getUri(), vrefRelation, blbRelation,
						cc);
				b.recursivelyAddKeys(keyList);
				keyList.add(new EntityKey(vc, vc.getUri()));
			}
		}
		for (BinaryContent bc : attachments) {
			keyList.add(new EntityKey(bc, bc.getUri()));
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + parentKey.hashCode() + 3
				* topLevelKey.hashCode() + +attachments.hashCode()
				+ versionedAttachments.hashCode();
	}

	public static class BinaryDescriptor {
		public final int ordinalNumber;
		public final String version;

		public BinaryDescriptor(int ordinalNumber, String version) {
			this.ordinalNumber = ordinalNumber;
			this.version = version;
		}
	}

	public BinaryDescriptor findMatchingBinaryContent(String parentUri) {
		for (Map.Entry<BinaryContent, List<VersionedBinaryContent>> bVcList : versionedAttachments
				.entrySet()) {
			for (VersionedBinaryContent vc : bVcList.getValue()) {
				if (vc.getUri().equals(parentUri)) {
					BinaryDescriptor bd = new BinaryDescriptor(bVcList.getKey()
							.getOrdinalNumber().intValue(), vc.getVersion());
					return bd;
				}
			}
		}
		return null;
	}

}
