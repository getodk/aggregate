/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.serialization.OdkTablesKeyValueManifestManager;

/**
 * Super basic test to see if the jackson library worked as expected.
 * @author sudar.sam@gmail.com
 *
 */
public class ManifestTest {

	@Test
	public void test() {
		assertTrue(true);
	}

	@Test
	public void toJson() throws JsonGenerationException, JsonMappingException, IOException {
		OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
		entry.key = "list";
		entry.tableId = "this-is-a-uuid";
		entry.value = "{greetings, I am a json string (no i'm not) }";
		entry.type = "file";

		OdkTablesKeyValueStoreEntry entry2 = new OdkTablesKeyValueStoreEntry();
		entry2.key = "box";
		entry2.tableId = "this-is-a-uuid-TIMES-ONE-FREAKING-THOUSAND";
		entry2.value = "guess what's in the box...";
		entry2.type = "surprise";

		List<OdkTablesKeyValueStoreEntry> entryList = new ArrayList<OdkTablesKeyValueStoreEntry>();
		entryList.add(entry);
		entryList.add(entry2);

		OdkTablesKeyValueManifestManager manifest = new OdkTablesKeyValueManifestManager();
		manifest.addEntries(entryList);

		assertEquals("[{\"tableId\":\"this-is-a-uuid\",\"tableName\":\"brother_of_skyrim_weapons\",\"key\":\"list\",\"type\":\"file\",\"value\":\"{greetings, I am a json string (no i'm not) }\"},{\"tableId\":\"this-is-a-uuid-TIMES-ONE-FREAKING-THOUSAND\",\"tableName\":\"sister_of_skyrim_weapons\",\"key\":\"box\",\"type\":\"surprise\",\"value\":\"guess what's in the box...\"}]",
				manifest.getManifestForTesting());
	}

	/**
	 * This will hopefully take things from a manifest and convert them to objects.
	 * The manifest comes from a certain snapshot of the datastore.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@Test
	public void getObjectsFromManifest() throws JsonParseException, JsonMappingException, IOException {
		//String jsonManifest = "[{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"box\",\"type\":\"file\",\"value\":\"{\\\"filename\":\"hellodatastore.txt\",\"md5hash\":\"md5:21f818fff26882fc9251a2a7c85e2cb0\",\"downloadUrl\":\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A9ff2964c-266c-43f9-8d70-79adb281fc67&as_attachment=true\"}\"},{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"list\",\"type\":\"file\",\"value\":\"{\"filename\":\"hellodatastore.txt\",\"md5hash\":\"md5:21f818fff26882fc9251a2a7c85e2cb0\",\"downloadUrl\":\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A020c17df-22d1-4f9f-8990-393dd6a348cf&as_attachment=true\"}\"},{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"list\",\"type\":\"file\",\"value\":\"{\"filename\":\"dylan_thesis.pdf\",\"md5hash\":\"md5:328c97d2ad43cd9e5de37a113bee3500\",\"downloadUrl\":\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A7b6aa5ba-17af-4a13-adc8-2b7d507a9749&as_attachment=true\"}\"}]";
		//System.out.println(jsonManifest);
		//String singleEntry = "{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"box\",\"type\":\"file\",\"value\":\"{\"\"testesttest\"\"}\"\"}";
				//{\"filename\":\"hellodatastore.txt\",\"md5hash\":\"md5:21f818fff26882fc9251a2a7c85e2cb0\",\"downloadUrl\":\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A9ff2964c-266c-43f9-8d70-79adb281fc67&as_attachment=true\"}\"}";
		String escapedManifest = "[{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"detail\",\"type\":\"file\",\"value\":\"{\\\"filename\\\":\\\"emailPicsImport.py\\\",\\\"md5hash\\\":\\\"md5:333d7fa21642c1c8f1353d5f854631dd\\\",\\\"downloadUrl\\\":\\\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A15e8556d-a4a9-456c-ab67-107e2e4dda5d&as_attachment=true\\\"}\"},{\"tableId\":\"b6ca94aa-68e2-4e8f-adf8-94ced5780fe3\",\"tableName\":\"skyrim weapons\",\"key\":\"box\",\"type\":\"file\",\"value\":\"{\\\"filename\\\":\\\"hellodatastore.txt\\\",\\\"md5hash\\\":\\\"md5:21f818fff26882fc9251a2a7c85e2cb0\\\",\\\"downloadUrl\\\":\\\"http://172.28.7.25:8888/tableFileDownload?blobKey=uuid%3A9ff2964c-266c-43f9-8d70-79adb281fc67&as_attachment=true\\\"}\"}]";
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<ArrayList<OdkTablesKeyValueStoreEntry>> typeRef = new TypeReference<ArrayList<OdkTablesKeyValueStoreEntry>>() {};
		ArrayList<OdkTablesKeyValueStoreEntry> entries = mapper.readValue(escapedManifest, typeRef);
		//OdkTablesKeyValueStoreEntry entry = mapper.readValue(singleEntry, OdkTablesKeyValueStoreEntry.class);
		System.out.println("0");
		System.out.println(entries.get(0).key);
		System.out.println(entries.get(0).tableId);
		System.out.println(entries.get(0).type);
		System.out.println(entries.get(0).value);
		System.out.println("1");
		System.out.println(entries.get(1).key);
		System.out.println(entries.get(1).tableId);
		System.out.println(entries.get(1).type);
		System.out.println(entries.get(1).value);

		OdkTablesFileManifestEntry fileEntry = mapper.readValue(entries.get(0).value, OdkTablesFileManifestEntry.class);
		System.out.println(fileEntry.downloadUrl);
		System.out.println(fileEntry.filename);
		System.out.println(fileEntry.md5hash);
	}


}
