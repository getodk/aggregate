/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.servlet.OdkTablesTableFileDownloadServlet;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.utils.HtmlUtil;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * This class manages the creation of the entries in the
 * manifest. It creates this manifest, and then turns it into
 * a JSON string to give down to the phone. It will be a list
 * of OdkTablesKeyValueStoreEntry objects.
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesKeyValueManifestManager {

	private List<OdkTablesKeyValueStoreEntry> entries;

	private ObjectMapper mapper;

	private String tableId;

	private CallingContext cc;

	private String manifest = null;

	/**
	 * Get the manifest ready for a specific table.
	 */
	public OdkTablesKeyValueManifestManager(String tableId, CallingContext cc) {
		this.tableId = tableId;
		this.cc = cc;
		mapper = new ObjectMapper();
	}

	/**
	 * Generic constructor. Used mostly for testing the json serialization,
	 * not for use in actual manifest generation for tables.
	 */
	public OdkTablesKeyValueManifestManager() {
		mapper = new ObjectMapper();
		entries = new ArrayList<OdkTablesKeyValueStoreEntry>();
	}



	/**
	 * Get the manifest in the format of a JSON String. It generates the manifest
	 * the first time it is called. This meant if the object was created and something
	 * was changed in the datastore, it might not be up to date. This seems unlikely/
	 * unimportant.
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws AccessDeniedException
	 * @throws RequestFailureException
	 * @throws DatastoreFailureException
	 * @throws PermissionDeniedExceptionClient
	 */
	public String getManifest() throws JsonGenerationException, JsonMappingException, IOException,
			PermissionDeniedExceptionClient, DatastoreFailureException, RequestFailureException,
			AccessDeniedException {
		if (manifest == null) {
			entries = getEntries();
			manifest = mapper.writeValueAsString(entries);
		}
		return manifest;
	}

	/**
	 * Gets the entries in the manifest for the tableId.
	 */
	public List<OdkTablesKeyValueStoreEntry> getEntries() throws
			PermissionDeniedExceptionClient, DatastoreFailureException, RequestFailureException,
			AccessDeniedException, JsonGenerationException, IOException {

		try {
		    List<Row> infoRows = EntityConverter.toRowsFromFileInfo(DbTableFileInfo.query(tableId, cc));
			TableManager tm = new TableManager(cc);
			TableEntry table = tm.getTable(tableId);
			DbTableFiles blobSetRelation = new DbTableFiles(cc);
			List<OdkTablesKeyValueStoreEntry> entries = new ArrayList<OdkTablesKeyValueStoreEntry>();
			for (Row row : infoRows) {
				// we only want the non-deleted rows
				if (!row.isDeleted()) {
					// the KeyValueStoreEntry object is the same for every entry. However,
					// for files you need to create a FileManifestEntry for the value.
					OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
					entry.tableId = tableId;
					entry.key = row.getValues().get(DbTableFileInfo.KEY);
					entry.type = row.getValues().get(DbTableFileInfo.VALUE_TYPE);
					// if it's a file, make the file manifest entry.
					if (entry.type.equalsIgnoreCase(DbTableFileInfo.Type.FILE.name)) {
						OdkTablesFileManifestEntry fileEntry = new OdkTablesFileManifestEntry();
						fileEntry.filename = blobSetRelation.getBlobEntitySet(row.getValues().get(DbTableFileInfo.VALUE), cc)
								.getUnrootedFilename(1, cc);
						fileEntry.md5hash = blobSetRelation.getBlobEntitySet(row.getValues().get(DbTableFileInfo.VALUE), cc)
								.getContentHash(1, cc);
						// now generate the download url. look at XFormsManifestXmlTable as an
						// example of how Mitch did it.
						Map<String, String> properties = new HashMap<String, String>();
						properties.put(ServletConsts.BLOB_KEY, row.getValues().get(DbTableFileInfo.VALUE));
						properties.put(ServletConsts.AS_ATTACHMENT, "true");
						String url = cc.getServerURL() + BasicConsts.FORWARDSLASH +
								OdkTablesTableFileDownloadServlet.ADDR;
						fileEntry.downloadUrl = HtmlUtil.createLinkWithProperties(url, properties);
						// now convert this object to json and set it to the entry's value.
						ObjectMapper mapper = new ObjectMapper();
						entry.value = mapper.writeValueAsString(fileEntry);

					} else {
						// if it's not a file, we just set the value. as input.
						entry.value = row.getValues().get(DbTableFileInfo.VALUE);
					}
					// and now add the completed entry to the list of entries
					entries.add(entry);

				}
			}
			return entries;
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
	}

	/**
	 * A single add method for testing json serialization.
	 * @param newEntry
	 */
	public void addEntry(OdkTablesKeyValueStoreEntry newEntry) {
		entries.add(newEntry);
	}

	/**
	 * Convenience method for adding a list of entries. Equivalent to
	 * calling addEntry multiple times. Only for use in testing.
	 */
	public void addEntries(List<OdkTablesKeyValueStoreEntry> newEntries) {
		for (OdkTablesKeyValueStoreEntry entry : newEntries) {
			addEntry(entry);
		}
	}

	/**
	 * Get manifest for testing.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public String getManifestForTesting() throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(entries);
	}



}
