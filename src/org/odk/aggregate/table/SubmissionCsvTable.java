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

import java.util.Arrays;
import java.util.Iterator;

import javax.jdo.PersistenceManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;

/**
 * Generates a CSV of submission data of a form 
 *
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionCsvTable extends SubmissionTable {

 
  /**
   * Construct a CSV table object for form with the specified ODK ID
   * 
   * @param odkIdentifier
   *    the ODK id of the form
   * @param persistenceManager
   *    the persistence manager used to manage generating the tables
   * @throws ODKFormNotFoundException 
   */
  public SubmissionCsvTable(String odkIdentifier, PersistenceManager persistenceManager) throws ODKFormNotFoundException {
    super(odkIdentifier, persistenceManager);
  }

  /**
   * Generates a CSV of submission data of the form specified by the ODK ID
   * 
   * @return
   *    a string that contains all submissions in CSV format
   *    
   * @throws ODKFormNotFoundException
   * @throws ODKIncompleteSubmissionData 
   */
  public String generateCsv() throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    String csv = BasicConsts.EMPTY_STRING;

    ResultTable resultTable = generateResultTable();

    // generate headers
    csv += generateCommaSeperatedRow(resultTable.getHeader().iterator());

    // generate rows
    for (String[] row : resultTable.getRows()) {
      csv += generateCommaSeperatedRow(Arrays.asList(row).iterator());
    }

    return csv;
  }

  /**
   * Helper function used to create the comma separated row
   * 
   * @param itr
   *    string values to be separated by commas
   * @return
   *    string containing comma separated values
   */
  private String generateCommaSeperatedRow(Iterator<String> itr) {
    String row = BasicConsts.EMPTY_STRING;
    while (itr.hasNext()) {
      row += itr.next();
      if (itr.hasNext()) {
        row += BasicConsts.CSV_DELIMITER;
      } else {
        row += BasicConsts.NEW_LINE;
      }
    }
    return row;
  }

}
