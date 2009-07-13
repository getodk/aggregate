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

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
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

  ResultTable resultTable;
  
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
    super(odkIdentifier, persistenceManager, ServletConsts.MAX_ENTITY_PER_PAGE);
  }



  public void generateHtmlSubmissionResultsTable(Date lastDate, boolean backward) throws ODKFormNotFoundException,
      ODKIncompleteSubmissionData {
    resultTable = generateResultTable(lastDate, backward);
  }
  
  public String getFirstDate() {
    List<String []> rows = resultTable.getRows();
    if(rows.isEmpty()) {
      return null;
    }
    return rows.get(0)[0];
  }

  public String getLastDate() {
    List<String []> rows = resultTable.getRows();
    if(rows.size() != ServletConsts.MAX_ENTITY_PER_PAGE) {
      return null;
    }
    return rows.get(ServletConsts.MAX_ENTITY_PER_PAGE-1)[0];
  }
  
  /**
   * Generate an string that contains an HTML table with the submission results for a form specified
   * 
   * @return submissions received defined by the form
   */
  public String getResultsHtml() {
    if(resultTable != null) {
      return HtmlUtil.wrapResultTableWithHtmlTags(resultTable);
    }
    return BasicConsts.EMPTY_STRING;
  }

  public String generateHtmlSubmissionRepeatResultsTable(String kind, Key elementKey,
      Key submissionParentKey) throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    ResultTable resultTable = generateResultRepeatTable(kind, elementKey, submissionParentKey);

    return HtmlUtil.wrapResultTableWithHtmlTags(resultTable);
  }

}
