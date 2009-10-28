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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.SubmissionFieldType;


/**
 * Base class used to process submission results into usable form
 *
 * @author wbrunette@gmail.com
 *
 */
public abstract class SubmissionResultBase {

  protected String odkId;
  protected EntityManager em;
  protected Form form;
  protected int fetchLimit;
  protected String baseServerUrl;
  protected List<String> headers;
  protected List<String> propertyNames;
  protected Map<String, SubmissionFieldType> headerTypes;

  protected boolean separateCoordinates;

  /**
   * Constructs a submission result base
   * @param xform TODO
   * @param webServerName TODO
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * @param fetchSizeLimit TODO
   */
  protected SubmissionResultBase(Form xform, String webServerName, EntityManager entityManager, int fetchSizeLimit, boolean separateGeopoint) {
    odkId = xform.getOdkId();
    em = entityManager;
    form = xform;
    fetchLimit = fetchSizeLimit;
    baseServerUrl = HtmlUtil.createUrl(webServerName);
    separateCoordinates = separateGeopoint;
  }
  
  /**
   * Constructs a submission result base
   * @param xform TODO
   * @param webServerName TODO
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * @param fetchSizeLimit TODO
   */
  protected SubmissionResultBase(Form xform, String webServerName, EntityManager entityManager, int fetchSizeLimit) {
    odkId = xform.getOdkId();
    em = entityManager;
    form = xform;
    fetchLimit = fetchSizeLimit;
    baseServerUrl = HtmlUtil.createUrl(webServerName);
    separateCoordinates = true;
  }
  
  /**
   * Constructs a submission result base
   * @param webServerName TODO
   * @param odkIdentifier
   *    the ODK id of the form
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   * @param fetchSizeLimit TODO
   * 
   * @throws ODKFormNotFoundException 
   */
  protected SubmissionResultBase(String webServerName, String odkIdentifier, EntityManager entityManager, int fetchSizeLimit) throws ODKFormNotFoundException {
    odkId = odkIdentifier;
    em = entityManager;
    form = Form.retrieveForm(em, odkId);
    fetchLimit = fetchSizeLimit;
    baseServerUrl = HtmlUtil.createUrl(webServerName);
    separateCoordinates = true;
  }
  

  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  protected void processElementForColumnHead(FormElement node, FormElement root, String parentName) {
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
      if(node.getSubmissionFieldType().equals(SubmissionFieldType.GEOPOINT) && separateCoordinates) {
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

  protected String getOdkId() {
    return odkId;
  }

  protected String getBaseServerUrl() {
    return baseServerUrl;
  }

  protected Map<String, SubmissionFieldType> getHeaderTypes() {
    return headerTypes;
  }

  protected void generatePropertyNamesAndHeaders(FormElement root, boolean submissionRoot) {
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

}
