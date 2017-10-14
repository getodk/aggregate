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
 * org.opendatakit.aggregate.odktables.entity.api. <br>
 * The idea is that it will do the same thing, but on the client side. Usual
 * caveat that it is not yet clear if this is needed or if another thing needs
 * be created for the server to do non-phone things.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableDefinitionClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = -113634509888543150L;

  private String tableId;
  private ArrayList<ColumnClient> columns;

  @SuppressWarnings("unused")
  private TableDefinitionClient() {
    // necessary for gwt serialization
  }

  public TableDefinitionClient(final String tableId, final ArrayList<ColumnClient> columns) {
    this.tableId = tableId;
    this.columns = columns;
  }

  public String getTableId() {
    return this.tableId;
  }

  public ArrayList<ColumnClient> getColumns() {
    return this.columns;
  }

  public void setColumns(final ArrayList<ColumnClient> columns) {
    this.columns = columns;
  }

  @Override
  public String toString() {
    return "TableDefinitionClient[tableId=" + getTableId() + ", columns=" + getColumns().toString()
        + "]";
  }
}
