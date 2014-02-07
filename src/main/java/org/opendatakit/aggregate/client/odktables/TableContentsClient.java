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
 * This represents the contents of a table. Essentially it is a wrapper of a
 * list of rows and the corresponding column names. <br>
 * It only exists so that you can essentially combine two service calls, one
 * that gets the column names and one that gets the rows, without having to
 * worry about the services returning at different times. For this reason there
 * is no corresponding client-side TableContents object.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableContentsClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -61478297837108751L;

  public TableContentsClient() {
    // necessary for gwt serialization
  }

  /**
   * The tables rows.
   */
  public ArrayList<RowClient> rows;

  /**
   * The names of the table's columns.
   */
  public ArrayList<String> columnNames;

}
