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
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.web.CallingContext;

/**
 * This class manages the creation of the entries in the manifest. It creates
 * this manifest, and then turns it into a JSON string to give down to the
 * phone. It will be a list of OdkTablesKeyValueStoreEntry objects.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesKeyValueManifestManager {

  private List<OdkTablesKeyValueStoreEntry> entries;

  private ObjectMapper mapper;

  private String appId;

  private String tableId;

  private CallingContext cc;

  private TablesUserPermissions userPermissions;

  private String manifest = null;

  /**
   * Get the manifest ready for a specific table.
   */
  public OdkTablesKeyValueManifestManager(String appId, String tableId, TablesUserPermissions userPermissions, CallingContext cc) {
    this.appId = appId;
    this.tableId = tableId;
    this.cc = cc;
    this.userPermissions = userPermissions;
    mapper = new ObjectMapper();
    entries = new ArrayList<OdkTablesKeyValueStoreEntry>();
  }


  /**
   * A single add method for testing json serialization.
   *
   * @param newEntry
   */
  public void addEntry(OdkTablesKeyValueStoreEntry newEntry) {
    entries.add(newEntry);
  }

  /**
   * Convenience method for adding a list of entries. Equivalent to calling
   * addEntry multiple times. Only for use in testing.
   */
  public void addEntries(List<OdkTablesKeyValueStoreEntry> newEntries) {
    for (OdkTablesKeyValueStoreEntry entry : newEntries) {
      addEntry(entry);
    }
  }

  /**
   * Get manifest for testing.
   *
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   */
  public String getManifestForTesting() throws JsonGenerationException, JsonMappingException,
      IOException {
    return mapper.writeValueAsString(entries);
  }

}
