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

package org.odk.aggregate.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.SubmissionFieldType;

public class FormProperties {

  protected String odkId;
  protected EntityManager em;
  protected Form form;
  protected List<String> headers;
  protected List<String> propertyNames;
  
  protected Map<String, SubmissionFieldType> headerTypes;
  
  // TODO: turns out I don't need this
  protected Map<String, SubmissionFieldType> propertyTypes;
  
  /**
   * Constructs a submission result base
   * @param xform TODO
   * @param entityManager
   *    the persistence manager used to manage generating the tables
   */
  public FormProperties(Form xform, EntityManager entityManager) {
    odkId = xform.getOdkId();
    em = entityManager;
    form = xform;

    headers = new ArrayList<String>();
    propertyNames = new ArrayList<String>();
    headerTypes = new HashMap<String, SubmissionFieldType>();
    propertyTypes = new HashMap<String, SubmissionFieldType>();
    
    headers.add(TableConsts.SUBMISSION_DATE_HEADER);
    propertyNames.add(TableConsts.SUBMISSION_DATE_HEADER);
    headerTypes.put(TableConsts.SUBMISSION_DATE_HEADER, SubmissionFieldType.DATE);
    propertyTypes.put(TableConsts.SUBMISSION_DATE_HEADER, SubmissionFieldType.DATE);
    
    FormElement root = xform.getElementTreeRoot();
    processElementForColumnHead(root, root, BasicConsts.EMPTY_STRING);
  }
 
  public Map<String, SubmissionFieldType> getHeaderTypes() {
    return headerTypes;
  }
  
  public String getOdkId() {
    return odkId;
  }
  
  public EntityManager getEm() {
    return em;
  }

  public Form getForm() {
    return form;
  }
  
  public Map<String, SubmissionFieldType> getPropertyTypes() {
    return propertyTypes;
  }
  
  public List<String> getPropertyNames() {
    return propertyNames;
  }

  
  public List<String> getHeaders() {
    return headers;
  }

  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnHead(FormElement node, FormElement root, String parentName) {
    if (node == null) return;
  
    if (node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      if (!node.equals(root)) {
        if (node.isRepeatable()) {
          addToHeaders(node.getElementName(), node.getSubmissionFieldType());
          addToPropertyNames(node.getElementName(), node.getSubmissionFieldType());
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
      addToPropertyNames(node.getElementName(), node.getSubmissionFieldType());
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
  
  private void addToPropertyNames(String name, SubmissionFieldType type) {
    propertyNames.add(name);
    propertyTypes.put(name, type);
  }
}
