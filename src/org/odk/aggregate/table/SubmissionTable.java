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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.servlet.FormMultipleValueServlet;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionFieldType;
import org.odk.aggregate.submission.SubmissionRepeat;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.SubmissionValue;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/**
 * Used to process submission results into a result table
 *
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionTable {
  
  protected String odkId;
    
  protected PersistenceManager pm;
  
  private Form form;

  
  /**
   * Constructs a table utils for the form
   * 
   * @param odkIdentifier
   *    the ODK id of the form
   * @param persistenceManager
   *    the persistence manager used to manage generating the tables
   * @throws ODKFormNotFoundException 
   */
  public SubmissionTable(String odkIdentifier, PersistenceManager persistenceManager) throws ODKFormNotFoundException {
    odkId = odkIdentifier;
    pm = persistenceManager;
    form = Form.retrieveForm(pm, odkId);
  }
  
  
  /**
   * Generates a result table that contains all the submission data 
   * of the form specified by the ODK ID
   * 
   * @return
   *    a result table containing submission data
   * @throws ODKFormNotFoundException
   * @throws ODKIncompleteSubmissionData 
   */
  protected ResultTable generateResultTable() throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    
    // create results table
    List<String> headers = new ArrayList<String>();
    headers.add(TableConsts.SUBMISSION_DATE_HEADER);
    processElementForColumnHead(headers, form.getElementTreeRoot(), form.getElementTreeRoot());
    ResultTable results = new ResultTable(headers);

    // create a row for each submission
    Query surveyQuery = new Query(odkId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(TableConsts.QUERY_ROWS_MAX));

    for (Entity subEntity : submissionEntities) {
      Submission sub = new Submission(subEntity);
      Map<String, SubmissionValue> valueMap = sub.getSubmissionValuesMap();
      String[] row = new String[results.getNumColumns()];
      int index = 0;
      for (String header : headers) {
        if (header.equals(TableConsts.SUBMISSION_DATE_HEADER)) {
          Date submittedTime = sub.getSubmittedTime();
          if(submittedTime != null) {
            row[index] = submittedTime.toString();
          }
        } else {
          processSubmissionFieldValue(sub.getKey(), valueMap, row, index, header);
        }
        index++;
      }
      results.addRow(row);
    }
    return results;
  }

  /**
   * Helper function to create the view link for repeat results
   * @param repeat
   *    the repeat object
   * @param parentSubmissionSetKey
   *    the submission set that contains the repeat value
   *    
   * @return
   *    the href that contains a link to repeat results
   */
  private String createRepeatLink(SubmissionRepeat repeat, Key parentSubmissionSetKey) { 
    FormElement element = form.getBeginningElement(repeat.getPropertyName(), pm);
    
    Map<String, String> properties = new HashMap<String,String>();
    properties.put(ServletConsts.ODK_ID, odkId);
    properties.put(ServletConsts.KIND, repeat.getKindId());
    properties.put(ServletConsts.FORM_ELEMENT_KEY, KeyFactory.keyToString(element.getKey()));
    properties.put(ServletConsts.PARENT_KEY, KeyFactory.keyToString(parentSubmissionSetKey));
    
    return HtmlUtil.createHrefWithProperties(FormMultipleValueServlet.ADDR, properties, TableConsts.VIEW_LINK_TEXT);
  }
  
  
  /**
   * Helper function to create the view link for images
   * @param subKey
   *    datastore key to the submission entity
   * @param porpertyName
   *    entity's property to retrieve and display
   * @return
   *    the href that contains a link to view the image
   */
  private String createViewLink(Key subKey, String porpertyName) {
    Map<String, String> properties = new HashMap<String,String>();
    properties.put(ServletConsts.SUBMISSION_KEY, KeyFactory.keyToString(subKey));
    properties.put(ServletConsts.PROPERTY_NAME, porpertyName);
    
    return HtmlUtil.createHrefWithProperties(ImageViewerServlet.ADDR, properties, TableConsts.VIEW_LINK_TEXT);
  }

  
  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   * @param columns
   *    list of column headings
   * @param node
   *    node to act recursively on
   */
  private void processElementForColumnHead(List<String> columns, FormElement node,
      FormElement root) {
    if (node == null) return;
    
    if (node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      if(node.isRepeatable() && !node.equals(root)) {
        columns.add(node.getElementName());
        return;
      }
      // else skip and goto children as we do not know how to display
    } else {
      columns.add(node.getElementName());
    }
    
    List<FormElement> childDataElements = node.getChildren(pm);
    // TODO: do better error handling
    if (childDataElements == null) {
      return;
    }
    for (FormElement child : childDataElements) {
      processElementForColumnHead(columns, child, root);
    }
  }

  protected ResultTable generateResultRepeatTable(String kind, Key elementKey, Key submissionParentKey) throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    FormElement element = pm.getObjectById(FormElement.class, elementKey);
    if (element == null) {
      throw new ODKIncompleteSubmissionData();
    }
    List<String> headers = new ArrayList<String>();
    processElementForColumnHead(headers, element, element);

    ResultTable results = new ResultTable(headers);

    // create a row for each submission
    Query surveyQuery = new Query(kind);
    surveyQuery.addFilter(PersistConsts.PARENT_KEY_PROPERTY, Query.FilterOperator.EQUAL, submissionParentKey);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(TableConsts.QUERY_ROWS_MAX));

    for (Entity subEntity : submissionEntities) {
      SubmissionSet sub = new SubmissionSet(subEntity);
      Map<String, SubmissionValue> valueMap = sub.getSubmissionValuesMap();
      String[] row = new String[results.getNumColumns()];
      int index = 0;
      for (String header : headers) {
        processSubmissionFieldValue(sub.getKey(), valueMap, row, index, header);
        index++;
      }
      results.addRow(row);
    }

    return results;
  }

  private void processSubmissionFieldValue(Key submissionSetKey,
      Map<String, SubmissionValue> submissionValueMap, String[] row, int index, String header) {
    SubmissionValue entry = submissionValueMap.get(header);
    if (entry != null) {
      if(entry instanceof SubmissionField<?>) {
        SubmissionField<?> field = (SubmissionField<?>) entry;
        if (field.isBinary()) {
          row[index] = createViewLink(submissionSetKey, header);
        } else {
          Object value = field.getValue();
          if (value != null) {
            row[index] = value.toString();
          }
        }
      } else if(entry instanceof SubmissionRepeat) {
        SubmissionRepeat repeat = (SubmissionRepeat) entry;
        row[index] = createRepeatLink(repeat, submissionSetKey);
      } else {
        // TODO: deal with error
        System.err.println(ErrorConsts.UNKNOWN_INTERFACE);
      }
    }
  }
  
}
