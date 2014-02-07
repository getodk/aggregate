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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is the client-side version of
 * org.opendatakit.aggregate.odktables.entity.TableProperties.java. <br>
 * The idea is that this will do the same thing, but for the server. The common
 * caveats apply for all of these objects, in that it is not yet clear how they
 * will work, and if there will need to be similar objects that are NOT the
 * original for-the-phone objects on the server.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TablePropertiesClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = 1977461163068996L;

  private String schemaETag;
  private String propertiesETag;
  private String tableId;
  private ArrayList<OdkTablesKeyValueStoreEntryClient> kvsEntries;

  protected TablePropertiesClient() {
  }

  public TablePropertiesClient(String schemaETag, String propertiesETag, String tableId,
      ArrayList<OdkTablesKeyValueStoreEntryClient> kvsEntries) {
    this.schemaETag = schemaETag;
    this.propertiesETag = propertiesETag;
    this.tableId = tableId;
    this.kvsEntries = kvsEntries;
  }

  public String getSchemaETag() {
    return schemaETag;
  }

  public void setSchemaETag(String schemaETag) {
    this.schemaETag = schemaETag;
  }

  public String getPropertiesETag() {
    return propertiesETag;
  }

  public void setPropertiesETag(String propertiesETag) {
    this.propertiesETag = propertiesETag;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public ArrayList<OdkTablesKeyValueStoreEntryClient> getKeyValueStoreEntries() {
    return this.kvsEntries;
  }

  public void setKeyValueStoreEntries(ArrayList<OdkTablesKeyValueStoreEntryClient> kvsEntries) {
    this.kvsEntries = kvsEntries;
  }

  @Override
  public String toString() {
    return "TableProperties [schemaETag=" + schemaETag + ", propertiesETag=" + propertiesETag + ", tableId=" + tableId
        + ", kvsEntries=" + kvsEntries.toString() + "]";
  }

}
