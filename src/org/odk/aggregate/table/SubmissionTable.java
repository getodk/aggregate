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
import com.google.appengine.api.datastore.EntityNotFoundException;
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
import org.odk.aggregate.submission.type.GeoPoint;

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
  
  private boolean moreRecords;

  private String odkId;
    
  private EntityManager em;
  
  private Form form;

  private int fetchLimit;
  
  private String baseServerUrl;

  private List<String> headers;
  
  private List<String> propertyNames;
  
  private Map<String, SubmissionFieldType> headerTypes;
  
  /**
   * Constructs a table utils for the form
   * @param xform TODO
   * @param webServerName TODO
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * @param fetchSizeLimit TODO
   */
  protected SubmissionTable(Form xform, String webServerName, EntityManager entityManager, int fetchSizeLimit) {
    odkId = xform.getOdkId();
    em = entityManager;
    form = xform;
    moreRecords = false;
    fetchLimit = fetchSizeLimit;
    baseServerUrl = HtmlUtil.createUrl(webServerName);
  }
  
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
  protected SubmissionTable(String webServerName, String odkIdentifier, EntityManager entityManager, int fetchSizeLimit) throws ODKFormNotFoundException {
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
  
  private void generatePropertyNamesAndHeaders(FormElement root, boolean submissionRoot) {
    headers = new ArrayList<String>();
    propertyNames = new ArrayList<String>();
    headerTypes = new HashMap<String, SubmissionFieldType>();
    
    if(submissionRoot) {
      headers.add(TableConsts.SUBMISSION_DATE_HEADER);
      propertyNames.add(TableConsts.SUBMISSION_DATE_HEADER);
      headerTypes.put(TableConsts.SUBMISSION_DATE_HEADER, SubmissionFieldType.DATE);
    }
    
    processElementForColumnHead(root, root, BasicConsts.EMPTY_STRING);
  }
  
  protected ResultTable generateSingleEntryResultTable(Key submissionKey) throws ODKIncompleteSubmissionData {
    // create results table
    generatePropertyNamesAndHeaders(form.getElementTreeRoot(), true);
    ResultTable results = new ResultTable(headers);
    
    try {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();  
      getSubmissionRow(results, ds.get(submissionKey));
    } catch (EntityNotFoundException e) {
      throw new ODKIncompleteSubmissionData();
    }
    
    return results;
  }
  /**
   * Generates a result table that contains all the submission data 
   * of the form specified by the ODK ID
   * 
   * @return
   *    a result table containing submission data
   *
   * @throws ODKIncompleteSubmissionData 
   */
  protected ResultTable generateResultTable(Date lastDate, boolean backward) throws ODKIncompleteSubmissionData {
    
    // create results table
    generatePropertyNamesAndHeaders(form.getElementTreeRoot(), true);
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
      getSubmissionRow(results, subEntity);
      count++;
    }
    return results;
  }

  private void getSubmissionRow(ResultTable results, Entity subEntity)
      throws ODKIncompleteSubmissionData {
    Submission sub = new Submission(subEntity, form);
    Map<String, SubmissionValue> valueMap = sub.getSubmissionValuesMap();
    List<String> row = new ArrayList<String>();
    for (String propertyName : propertyNames) {
      if (propertyName.equals(TableConsts.SUBMISSION_DATE_HEADER)) {
        Date submittedTime = sub.getSubmittedTime();
        if(submittedTime != null) {
          row.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(submittedTime));
        }
      } else {
        processSubmissionFieldValue(sub.getKey(), valueMap, row, propertyName);
      }
    }
    results.addRow(row);
  }
  
  
  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnHead(FormElement node,
      FormElement root, String parentName) {
    if (node == null) return;

    if (node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      if (!node.equals(root)) {
        if (node.isRepeatable()) {
          addToHeaders(node.getElementName(), node.getSubmissionFieldType());
          propertyNames.add(node.getElementName()); 
          return;
        } else {
          // else skip and goto children as we do not know how to display
          // append parent name incase embedded tag
          parentName = node.getElementName() + BasicConsts.DASH;
        }
      }
    } else {
      if(node.getSubmissionFieldType().equals(SubmissionFieldType.GEOPOINT)) {
        addToHeaders(node.getElementName() + BasicConsts.DASH + BasicConsts.LATITUDE, node.getSubmissionFieldType());
        addToHeaders(node.getElementName() + BasicConsts.DASH + BasicConsts.LONGITUDE, node.getSubmissionFieldType());
      } else {
        addToHeaders(parentName + node.getElementName(), node.getSubmissionFieldType());
      }
      propertyNames.add(node.getElementName()); 
    }

    List<FormElement> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElement child : childDataElements) {
      processElementForColumnHead(child, root, parentName);
    }
  }
  
  private void addToHeaders(String name, SubmissionFieldType type) {
    headers.add(name);
    headerTypes.put(name, type);
  }

  protected ResultTable generateResultRepeatTable(String kind, Key elementKey, Key submissionParentKey) throws ODKIncompleteSubmissionData {
    FormElement element = em.getReference(FormElement.class, elementKey);
    if (element == null) {
      throw new ODKIncompleteSubmissionData();
    }
    
    // create results table
    generatePropertyNamesAndHeaders(element, false);
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
      List<String> row = new ArrayList<String>();
      for (String propertyName : propertyNames) {
        processSubmissionFieldValue(sub.getKey(), valueMap, row, propertyName);
      }
      results.addRow(row);
    }

    return results;
  }

  private void processSubmissionFieldValue(Key submissionSetKey,
      Map<String, SubmissionValue> submissionValueMap, List<String> row, String propertyName) {
    SubmissionValue entry = submissionValueMap.get(propertyName);
    if (entry != null) {
      if(entry instanceof SubmissionField<?>) {
        SubmissionField<?> field = (SubmissionField<?>) entry;
        Object value = field.getValue();
        if (value != null) {
          if (field.isBinary()) {
            if (value instanceof Key) {
              Key blobKey = (Key) value;
              row.add(createViewLink(blobKey, propertyName));
            } else {
              System.err.println(ErrorConsts.NOT_A_KEY);
            }
          } else {
            if(value instanceof GeoPoint) {
              GeoPoint coordinate = (GeoPoint) value;
              if(coordinate.getLatitude() != null)
              {
                row.add(coordinate.getLatitude().toString());
              } else {
                row.add(null);
              }
              
              if(coordinate.getLongitude() != null)
              {
                row.add(coordinate.getLongitude().toString());
              } else {
                row.add(null);
              }

            } else {
              row.add(value.toString());
            }
          }
        } else {
          row.add(null);
        }
      } else if(entry instanceof SubmissionRepeat) {
        SubmissionRepeat repeat = (SubmissionRepeat) entry;
        row.add(createRepeatLink(repeat, submissionSetKey));
      } else {
        // TODO: deal with error
        System.err.println(ErrorConsts.UNKNOWN_INTERFACE);
      }
    } else {
      row.add(null);
    }
  }
  
  public String getOdkId() {
    return odkId;
  }

  public String getBaseServerUrl() {
    return baseServerUrl;
  }

  public Map<String, SubmissionFieldType> getHeaderTypes() {
    return headerTypes;
  }
  
}
