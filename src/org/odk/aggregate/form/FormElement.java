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

package org.odk.aggregate.form;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionFieldType;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Form Elements describe how to convert the data
 * from the XML submissions to a datastore type by 
 * mapping the element name to a submission field type. 
 *  
 *
 * @author wbrunette@gmail.com
 *
 */
@Entity
public class FormElement {

  /**
   * GAE datastore key that uniquely identifies the form element
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  private Key key;

  /**
   * Name of submission element
   */
  @Enumerated
  private String elementName;

  /**
   * Specifies Enum that can be used to determine the proper class that 
   * can handle the data conversion to the appengine datastore.
   * NOTE: stored as a string in the datastore
   */
  @Enumerated
  private String submissionFieldType;

  /**
   * Specifies where the object can be repeated in the submission
   */
  @Enumerated
  private Boolean isRepeatable;

  /**
   * A list of restored child objects
   */
  @OneToMany(cascade=CascadeType.ALL)
  @OrderBy("childNumber ASC")
  private List<FormElement> children;
  
  @Enumerated
  private int numChildren;
  
  @SuppressWarnings("unused") // used by JPA for sort order
  @Enumerated
  private int childNumber;
  
  /**
   * Construct a form element that defines an element in a submission
   * 
   * @param parent
   *    a datastore key to the parent
   * @param name
   *    name of the form element
   * @param odkId
   *    the OKD identifier associated with the form
   * @param type
   *    submission field type that should be used to convert submission
   * @param repeatable
   *    if element can have multiple values in a submission
   */
  public FormElement(Key parent, String name, String odkId, SubmissionFieldType type,
      Boolean repeatable) {
    if (parent == null) {
      key = KeyFactory.createKey(FormElement.class.getSimpleName(), name + odkId);
    } else {
      key =
          new KeyFactory.Builder(parent).addChild(FormElement.class.getSimpleName(), name + odkId)
              .getKey();
    }

    setSubmissionFieldType(type);
    elementName = name;
    isRepeatable = repeatable;
    numChildren = 0;
    childNumber = -1; // default to not part of the order
  }

  /**
   * Get the name of submission element
   * 
   * @return
   *    submission element name
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * Get a list of form elements that are children to this form element
   * 
   * @return
   *    a list of Form Elements that is this form elements children
   */
  public List<FormElement> getChildren() {
    return children;
  }

  /**
   * Add a Form Element as a child to this form element
   * 
   * @param child
   *    form element to be added as a child
   */
  public void addChild(FormElement child) {
    if(children == null) {
      children = new ArrayList<FormElement>();
    }
    children.add(child);
    child.childNumber = this.numChildren++;
  }

  /**
   * Get the Submission Field Type that can be used to determine 
   * the proper class that can handle the data conversion 
   * to the appengine datastore.
   * 
   * @return
   *    the submission field type enum object
   */
  public SubmissionFieldType getSubmissionFieldType() {
    return SubmissionFieldType.valueOf(submissionFieldType);
  }

  /**
   * Set the Submission Field Type to be used to determine 
   * the proper class that can handle the data conversion 
   * to the appengine datastore.
   * 
   * @param type
   *    the Submission Field Type
   * 
   */
  public void setSubmissionFieldType(SubmissionFieldType type) {
    submissionFieldType = type.name();
  }

  /**
   * Get the GAE datastore key that uniquely identifies the form element
   *
   * @return
   *    the datastore key for this form element
   */
  public Key getKey() {
    return key;
  }

  /**
   * Specifies whether element can appear multiple times in submission
   * 
   * @return
   *    true if element can appear multiple times in submission
   */
  public Boolean isRepeatable() {
    return isRepeatable;
  }

  /**
   * Create submission field object that can convert from type in the submission to the
   * GAE datastore type
   * 
   * @return
   *     an object that implements submission field 
   */
  public SubmissionField<?> createSubmissionField() {
    SubmissionField<?> submissionData = getSubmissionFieldType().createSubmissionField(elementName);
    return submissionData;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FormElement)) {
      return false;
    }
    FormElement other = (FormElement) obj;
    return (key == null ? (other.key == null) : (key.equals(other.key)))
        && (elementName == null ? (other.elementName == null) : (elementName.equals(other.elementName)))
        && (submissionFieldType == null ? (other.submissionFieldType == null) : (submissionFieldType.equals(other.submissionFieldType)))
        && (isRepeatable == null ? (other.isRepeatable == null) : (isRepeatable.equals(other.isRepeatable)))
        && (children == null ? (other.children == null) : (children.equals(other.children)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if(key != null) hashCode += key.hashCode();
    if(elementName != null) hashCode += elementName.hashCode();
    if(submissionFieldType != null) hashCode += submissionFieldType.hashCode();
    if(isRepeatable != null) hashCode += isRepeatable.hashCode();
    if(children != null) hashCode += children.hashCode();
    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if(children == null) {
      return submissionFieldType + " Repeatable:" + isRepeatable;
    } else {
      return submissionFieldType + " Repeatable:" + isRepeatable + " " + children.toString();
    }
  }
}
