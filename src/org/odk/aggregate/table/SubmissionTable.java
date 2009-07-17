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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionFieldType;
import org.odk.aggregate.submission.SubmissionRepeat;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.SubmissionValue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

/**
 * Used to process submission results into a result table
 *
 * @author wbrunette@gmail.com
 *
 */
public abstract class SubmissionTable {
  
  protected boolean moreRecords;

  protected String odkId;
    
  protected EntityManager em;
  
  private Form form;

  private int fetchLimit;
  
  protected String baseServerUrl;
  
  /**
   * Constructs a table utils for the form
   * @param webServerName TODO
   * @param odkIdentifier
   *    the ODK id of the form
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * @param fetchSizeLimit TODO
   * 
   * @throws ODKFormNotFoundException 
   */
  public SubmissionTable(String webServerName, String odkIdentifier, EntityManager entityManager, int fetchSizeLimit) throws ODKFormNotFoundException {
    odkId = odkIdentifier;
    em = entityManager;
    form = Form.retrieveForm(em, odkId);
    moreRecords = false;
    fetchLimit = fetchSizeLimit;
    baseServerUrl = HtmlUtil.createUrl(webServerName);
  }
  
  /**
   * Helper function to create the view link for images
   * @param subKey
   *    datastore key to the submission entity
   * @param porpertyName
   *    entity's property to retrieve and display
   * 
   * @return
   *    link to view the image
   */
  protected abstract String createViewLink(Key subKey, String porpertyName);
  
  /**
   * Helper function to create the view link for repeat results
   * @param repeat
   *    the repeat object
   * @param parentSubmissionSetKey
   *    the submission set that contains the repeat value
   *    
   * @return
   *    the link to repeat results
   */
  protected abstract String createRepeatLink(SubmissionRepeat repeat, Key parentSubmissionSetKey);
  
  /**
   * Helper function to create the properties for a view link for images
   * @param subKey
   *    datastore key to the submission entity
   * @return
   *    property map
   */
  protected Map<String, String> createViewLinkProperties(Key subKey) {
    Map<String, String> properties = new HashMap<String,String>();
    properties.put(ServletConsts.BLOB_KEY, KeyFactory.keyToString(subKey)); 
    return properties;
  }
  
  /**
   * Helper function to create the properties for a link to repeat results
   * @param repeat
   *    the repeat object
   * @param parentSubmissionSetKey
   *    the submission set that contains the repeat value
   *    
   * @return
   *    property map
   */
  protected Map<String, String> createRepeatLinkProperties(SubmissionRepeat repeat, Key parentSubmissionSetKey) {
    FormElement element = form.getBeginningElement(repeat.getPropertyName());
    
    Map<String, String> properties = new HashMap<String,String>();
    properties.put(ServletConsts.ODK_ID, odkId);
    properties.put(ServletConsts.KIND, repeat.getKindId());
    properties.put(ServletConsts.FORM_ELEMENT_KEY, KeyFactory.keyToString(element.getKey()));
    properties.put(ServletConsts.PARENT_KEY, KeyFactory.keyToString(parentSubmissionSetKey));
    return properties;
  }
  
  
  public boolean isMoreRecords() {
    return moreRecords;
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
  protected ResultTable generateResultTable(Date lastDate, boolean backward) throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    
    // create results table
    List<String> headers = new ArrayList<String>();
    headers.add(TableConsts.SUBMISSION_DATE_HEADER);
    processElementForColumnHead(headers, form.getElementTreeRoot(), form.getElementTreeRoot(), BasicConsts.EMPTY_STRING);
    ResultTable results = new ResultTable(headers);

    // retrieve submissions
    Query surveyQuery = new Query(odkId);
    if(backward) {
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.DESCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.FilterOperator.LESS_THAN, lastDate);
    } else {
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.ASCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.FilterOperator.GREATER_THAN, lastDate);
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(fetchLimit + 1));
    
    if(submissionEntities.size() > fetchLimit) {
      moreRecords = true;
      submissionEntities.remove(fetchLimit);
    }
    
    // create a row for each submission
    int count = 0;
    while(count < submissionEntities.size()) {
      Entity subEntity;
      if(backward) {
        subEntity = submissionEntities.get(submissionEntities.size() - 1 - count);
      } else {
        subEntity = submissionEntities.get(count);
      }
      Submission sub = new Submission(subEntity, form);
      Map<String, SubmissionValue> valueMap = sub.getSubmissionValuesMap();
      String[] row = new String[results.getNumColumns()];
      int index = 0;
      for (String header : headers) {
        if (header.equals(TableConsts.SUBMISSION_DATE_HEADER)) {
          Date submittedTime = sub.getSubmittedTime();
          if(submittedTime != null) {
            row[index] = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(submittedTime);
          }
        } else {
          processSubmissionFieldValue(sub.getKey(), valueMap, row, index, header);
        }
        index++;
      }
      results.addRow(row);
      count++;
    }
    return results;
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
      FormElement root, String parentName) {
    if (node == null) return;

    if (node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      if (!node.equals(root)) {
        if (node.isRepeatable()) {
          columns.add(node.getElementName());
          return;
        } else {
          // else skip and goto children as we do not know how to display
          // append parent name incase embedded tag
          parentName = node.getElementName() + BasicConsts.FORWARDSLASH;
        }
      }
    } else {
      columns.add(parentName + node.getElementName());
    }

    List<FormElement> childDataElements = node.getChildren();
    // TODO: do better error handling
    if (childDataElements == null) {
      return;
    }
    for (FormElement child : childDataElements) {
      processElementForColumnHead(columns, child, root, parentName);
    }
  }

  protected ResultTable generateResultRepeatTable(String kind, Key elementKey, Key submissionParentKey) throws ODKFormNotFoundException, ODKIncompleteSubmissionData {
    FormElement element = em.getReference(FormElement.class, elementKey);
    if (element == null) {
      throw new ODKIncompleteSubmissionData();
    }
    List<String> headers = new ArrayList<String>();
    processElementForColumnHead(headers, element, element, BasicConsts.EMPTY_STRING);

    ResultTable results = new ResultTable(headers);

    // create a row for each submission
    Query surveyQuery = new Query(kind);
    surveyQuery.addFilter(PersistConsts.PARENT_KEY_PROPERTY, Query.FilterOperator.EQUAL, submissionParentKey);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(fetchLimit));

    for (Entity subEntity : submissionEntities) {
      SubmissionSet sub = new SubmissionSet(subEntity, form);
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
    String propertyName = header.substring(header.lastIndexOf(BasicConsts.FORWARDSLASH) + 1);
    SubmissionValue entry = submissionValueMap.get(propertyName);
    if (entry != null) {
      if(entry instanceof SubmissionField<?>) {
        SubmissionField<?> field = (SubmissionField<?>) entry;
        Object value = field.getValue();
        if (value != null) {
          if (field.isBinary()) {
            if (value instanceof Key) {
              Key blobKey = (Key) value;
              row[index] = createViewLink(blobKey, header);
            } else {
              System.err.println(ErrorConsts.NOT_A_KEY);
            }
          } else {
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
