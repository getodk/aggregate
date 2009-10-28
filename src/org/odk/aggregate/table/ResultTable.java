/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.table;

import java.util.ArrayList;
import java.util.List;

import org.odk.aggregate.constants.ErrorConsts;

/**
 * Stores results from queries as strings so results can
 * be easily formatted for various types of output
 *
 * @author wbrunette@gmail.com
 *
 */
public class ResultTable {

  /**
   * List containing Column Headers
   */
  List<String> header;

  /**
   * List containing the rows of data 
   */
  List<List<String>> rows;

  /**
   * Construct a table based on the column headers
   * @param tableHeader
   *    list of column headers
   */
  public ResultTable(List<String> tableHeader) {
    header = tableHeader;
    rows = new ArrayList<List<String>>();
  }

  /**
   * Add a row of data to result table
   * @param row
   *    array of strings representing a row
   */
  public void addRow(List<String> row) {
    if (row.size() != header.size()) {
      System.err.println(ErrorConsts.ROW_SIZE_ERROR);
      return;
    }
    rows.add(row);
  }

  /**
   * Get a list of column headers
   * @return
   *    list of column headers
   */
  public List<String> getHeader() {
    return header;
  }

  /**
   * Get a list of data rows
   * @return
   *    list of string arrays that contain the data
   */
  public List<List<String>> getRows() {
    return rows;
  }

  /**
   * Get the number of columns in table
   * 
   * @return
   *    number of columns
   */
  public int getNumColumns() {
    return header.size();
  }

}
