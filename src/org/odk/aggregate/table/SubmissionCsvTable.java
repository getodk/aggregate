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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.servlet.FormMultipleValueServlet;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.SubmissionRepeat;

import com.google.appengine.api.datastore.Key;

/**
 * Generates a CSV of submission data of a form 
 *
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionCsvTable extends SubmissionTable {

  /**
   * Construct a CSV table object for form with the specified ODK ID
   * @param serverName TODO
   * @param odkIdentifier
   *    the ODK id of the form
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * 
   * @throws ODKFormNotFoundException 
   */
  public SubmissionCsvTable(String serverName, String odkIdentifier, EntityManager entityManager) throws ODKFormNotFoundException {
    super(serverName, odkIdentifier, entityManager, TableConsts.QUERY_ROWS_MAX);
  }

  /**
   * Construct a CSV table object for form with the specified ODK ID
   * @param xform TODO
   * @param serverName TODO
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * 
   */
  protected SubmissionCsvTable(Form xform, String serverName, EntityManager entityManager) {
    super(xform, serverName, entityManager, TableConsts.QUERY_ROWS_MAX);
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

    ResultTable resultTable = generateResultTable(TableConsts.EPOCH, false);

    // generate headers
    csv += generateCommaSeperatedRow(resultTable.getHeader().iterator());

    // generate rows
    for (List<String> row : resultTable.getRows()) {
      csv += generateCommaSeperatedRow(row.iterator());
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
      row += BasicConsts.QUOTE + itr.next() + BasicConsts.QUOTE;
      if (itr.hasNext()) {
        row += BasicConsts.CSV_DELIMITER;
      } else {
        row += BasicConsts.NEW_LINE;
      }
    }
    return row;
  }

  /**
   * Helper function to create the view link for images
   * @param subKey
   *    datastore key to the submission entity
   * @param porpertyName
   *    entity's property to retrieve and display
   * 
   * @return
   *     link to view the image
   */
  @Override
  protected String createViewLink(Key subKey, String porpertyName) {
    Map<String, String> properties = createViewLinkProperties(subKey);
    return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl() + ImageViewerServlet.ADDR, properties);
  }
 
  /**
   * Helper function to create the link to repeat results
   * @param repeat
   *    the repeat object
   * @param parentSubmissionSetKey
   *    the submission set that contains the repeat value
   *    
   * @return
   *    link to repeat results
   */
  @Override
  protected String createRepeatLink(SubmissionRepeat repeat, Key parentSubmissionSetKey) { 
    Map<String, String> properties = createRepeatLinkProperties(repeat, parentSubmissionSetKey);
    return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl() + FormMultipleValueServlet.ADDR, properties);
  }
  
}
