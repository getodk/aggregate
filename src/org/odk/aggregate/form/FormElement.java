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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionFieldType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Form Elements describe how to convert the data
 * from the XML submissions to a datastore type by 
 * mapping the element name to a submission field type. 
 *  
 *
 * @author wbrunette@gmail.com
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class FormElement {

  /**
   * GAE datastore key that uniquely identifies the form element
   */
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  /**
   * Name of submission element
   */
  @Persistent
  private String elementName;

  /**
   * Specifies Enum that can be used to determine the proper class that 
   * can handle the data conversion to the appengine datastore.
   * NOTE: stored as a string in the datastore
   */
  @Persistent
  private String submissionFieldType;

  /**
   * Specifies where the object can be repeated in the submission
   */
  @Persistent
  private Boolean isRepeatable;

  /**
   * A list of GAE datastore keys that point to the form elements children
   */
  // NOTE: delete may work differently because object is unowned
  @Persistent
  private List<Key> childKeys;

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
   * @param pm
   *    Persistence Manager to use to query
   * @return
   *    a list of Form Elements that is this form elements children
   */
  public List<FormElement> getChildren(PersistenceManager pm) {
    List<FormElement> childDataElements = new ArrayList<FormElement>();
    // TODO: do better error handling
    if (childKeys == null) {
      return childDataElements;
    }
    try {
      for (Key childKey : childKeys) {
        if (childKey == null) {
          continue;
        }
        FormElement element = pm.getObjectById(FormElement.class, childKey);
        if (element != null) {
          childDataElements.add(element);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return childDataElements;
  }

  /**
   * Add a Form Element as a child to this form element
   * 
   * @param child
   *    form element to be added as a child
   */
  public void addChild(FormElement child) {
    if (childKeys == null) {
      childKeys = new ArrayList<Key>();
    }
    childKeys.add(child.getKey());
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
    return other.key.equals(key) && other.isRepeatable.equals(isRepeatable)
        && other.submissionFieldType.equals(submissionFieldType)
        && other.childKeys.equals(childKeys);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    BigInteger hashCode = new BigInteger("13");
    hashCode = hashCode.add(BigInteger.valueOf(key.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(isRepeatable.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(submissionFieldType.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(childKeys.hashCode()));
    return hashCode.intValue();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (childKeys == null) {
      return submissionFieldType + " Repeatable:" + isRepeatable;
    } else {
      return submissionFieldType + " Repeatable:" + isRepeatable + " " + childKeys.toString();
    }
  }
}
