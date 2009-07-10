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

import javax.jdo.PersistenceManager;

import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;

import com.google.appengine.api.datastore.Key;

/**
 * Used to generate submission results in html tables for the servlets
 * 
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionHtmlTable extends SubmissionTable {

  /**
   * Construct a HTML table object for form with the specified ODK ID
   * 
   * @param odkIdentifier
   *    the ODK id of the form
   * @param persistenceManager
   *    the persistence manager used to manage generating the tables
   * @throws ODKFormNotFoundException 
   */
  public SubmissionHtmlTable(String odkIdentifier, PersistenceManager persistenceManager) throws ODKFormNotFoundException {
    super(odkIdentifier, persistenceManager);
  }


  /**
   * Generate an HTML table with the submission results for a form specified
   * with odk identifier
   * 
   * @return submissions received defined by the form
   * @throws ODKFormNotFoundException
   * @throws ODKIncompleteSubmissionData
   */
  public String generateHtmlSubmissionResultsTable() throws ODKFormNotFoundException,
      ODKIncompleteSubmissionData {
    ResultTable resultTable = generateResultTable();

    return HtmlUtil.wrapResultTableWithHtmlTags(resultTable);
  }


  public String generateHtmlSubmissionRepeatResultsTable(String kind, Key elementKey,
      Key submissionParentKey) throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    ResultTable resultTable = generateResultRepeatTable(kind, elementKey, submissionParentKey);

    return HtmlUtil.wrapResultTableWithHtmlTags(resultTable);
  }

}
