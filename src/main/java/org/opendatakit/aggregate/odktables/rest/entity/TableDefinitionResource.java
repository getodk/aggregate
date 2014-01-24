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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class TableDefinitionResource extends TableDefinition {

  @Element(required = true)
  private String selfUri;

  @Element(required = true)
  private String tableUri;

  @SuppressWarnings("unused")
  private TableDefinitionResource() {
  }

  public TableDefinitionResource(TableDefinition definition) {
    this(definition.getTableId(), definition.getSchemaETag(), definition.getColumns());
  }

  public TableDefinitionResource(String tableId, String schemaETag, ArrayList<Column> columns) {
    super(tableId, schemaETag, columns);
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public String getTableUri() {
    return this.tableUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
  }

}
