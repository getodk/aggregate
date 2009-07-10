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
import com.google.appengine.api.datastore.Text;

import org.odk.aggregate.exception.ODKFormNotFoundException;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Persistable definition of the XForm that defines how to store submissions
 * to the datastore. Includes form elements that know how to properly convert
 * the data to/from the datastore.
 *
 * @author wbrunette@gmail.com
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Form {
  /**
   * GAE datastore key that uniquely identifies the form entity 
   */
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  /**
   * ODK identifier that uniquely identifies the form
   */
  @Persistent
  private String odkId;

  /**
   * The name that is viewable on ODK Aggregate
   */
  @Persistent
  private String viewableName;

  /**
   * Root of the form elements that describe how to convert the data
   * from the XML submissions to the gae datastore types
   */
  @Persistent
  private FormElement elementTreeRoot;

  /**
   * The date the form was created
   */
  @Persistent
  private Date creationDate;

  /**
   * The last date the form was updated
   */
  @Persistent
  private Date updateDate;

  /**
   * The user who uploaded/created the form
   */
  @Persistent
  private String creationUser;

  /**
   * The original XML that specified the form
   */
  @Persistent
  private Text originalForm;

  /**
   * String used to specify the filename when outputting the XML to file
   */
  @Persistent
  private String fileName;
 

  /**
   * Specifies whether the form is downloadable
   */
  @Persistent
  private Boolean downloadEnabled;
 
  /**
   * Specifies whether the system can receive new form submissions
   */
  @Persistent
  private Boolean submissionEnabled;
  
  /**
   * List of objects that point to the elements that begin the repeats
   */
  @Persistent(mappedBy = "form")
  private List<FormRepeat> repeatElements;
  
  @NotPersistent
  private Map<String, FormRepeat> repeatElementMap;
  
  /**
   * Construct a form definition that can be persisted
   * 
   * @param formOdkId
   *    Form's unique ODK id 
   * @param viewableName
   *    Name to be displayed
   * @param user
   *    User that created the form
   * @param form
   *    The xml definition or the form
   * @param fileName
   *    The name of xml used for outputting form
   */
  public Form(String formOdkId, String viewableName, String user, String form, String fileName) {
    this.key = KeyFactory.createKey(Form.class.getSimpleName(), formOdkId);
    this.odkId = formOdkId;
    this.viewableName = viewableName;
    this.creationDate = new Date();
    this.updateDate = this.creationDate;
    this.creationUser = user;
    this.originalForm = new Text(form);
    this.fileName = fileName;
    this.downloadEnabled = true;
    this.submissionEnabled = true;
  }

  /**
   *  Get the GAE datastore key that uniquely identifies the form entity 
   *
   * @return
   *    datastore key
   */
  public Key getKey() {
    return key;
  }

  /**
   * Get the ODK identifier that identifies the form
   * 
   * @return
   *    odk identifier
   */
  public String getOdkId() {
    return odkId;
  }

  /**
   * Get the name that is viewable on ODK Aggregate
   * 
   * @return
   *    viewable name
   */
  public String getViewableName() {
    return viewableName;
  }

  /**
   * Get the date the form was created
   * 
   * @return
   *    creation date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  
  /**
   * Get the last date the form was updated
   * 
   * @return
   *    last date form was updated
   */
  public Date getUpdateDate() {
    return updateDate;
  }

  /**
   * Get the user who uploaded/created the form
   * @return
   *    user name
   */
  public String getCreationUser() {
    return creationUser;
  }

  /**
   * Set the name that is viewable on ODK Aggregate 
   *
   * @param viewableName
   *    name to be displayed
   */
  public void setViewableName(String viewableName) {
    this.viewableName = viewableName;
    this.updateDate = new Date();
  }

  /**
   * Get the original XML that specified the form
   * 
   * @return
   *    get XML definition of XForm
   */
  public String getOriginalForm() {
    return originalForm.getValue();
  }

  /**
   * Get the file name to be used when generating the XML file describing from
   * 
   * @return
   *    xml file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Set the file name to be used when generating the XML file describing from
   * 
   * @param fileName
   *    file name 
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  /**
   * Set the root of the form elements that describe how to convert the data
   * from the XML submissions to the gae datastore types
   * 
   * @param formElementTreeRoot
   *    root of the form element tree
   * 
   */
  public void setElementTreeRoot(FormElement formElementTreeRoot) {
    this.elementTreeRoot = formElementTreeRoot;
  }

  /**
   * Get the root of the form elements that describe how to convert the data
   * from the XML submissions to the gae datastore types
   * 
   * @return
   *    form element root
   */
  public FormElement getElementTreeRoot() {
    return elementTreeRoot;
  }

  
  /**
   * Gets whether the form can be downloaded
   * @return
   *    true if form can be downloaded, false otherwise
   */
  public Boolean getDownloadEnabled() {
    return downloadEnabled;
  }

  /**
   * Sets a boolean value of whether the form can be downloaded
   * @param downloadEnabled
   *    set to true if form can be downloaded, false otherwise
   *    
   */
  public void setDownloadEnabled(Boolean downloadEnabled) {
    this.downloadEnabled = downloadEnabled;
  }

  /**
   * Gets whether a new submission can be received
   * @return
   *    true if a new submission can be received, false otherwise
   */
  public Boolean getSubmissionEnabled() {
    return submissionEnabled;
  }

  /**
   * Sets a boolean value of whether a new submission can be received
   * @param submissionEnabled
   *    set to true if a new submission can be received, false otherwise
   *    
   */
  public void setSubmissionEnabled(Boolean submissionEnabled) {
    this.submissionEnabled = submissionEnabled;
  }  
  
  public void addRepeat(FormElement repeatElement, PersistenceManager pm) {
    FormRepeat repeat = new FormRepeat(repeatElement.getElementName(), repeatElement.getKey());
    if(repeatElements == null) {
      repeatElements = new ArrayList<FormRepeat>();
    }
    repeatElements.add(repeat);
    pm.makePersistent(repeat);
  }
  
  public FormElement getBeginningElement(String elementName, PersistenceManager pm) {
    
    // check if it's the root of the form
    if(elementTreeRoot.getElementName().equals(elementName)) {
      return elementTreeRoot;
    }
    
    // check to see if any repeatsRoots exist
    if(repeatElements == null) {
      return null;
    }
    
    // check to see if repeatRootMap needs to be created
    // NOTE: this assumes the form does NOT get altered!!!
    if(repeatElementMap == null) {
      repeatElementMap = new HashMap<String, FormRepeat> ();
      for(FormRepeat repeat : repeatElements) {
        repeatElementMap.put(repeat.getRepeatElementName(), repeat);
      }
    }
    
    // check if element is in repeat set
    FormRepeat repeatElement = repeatElementMap.get(elementName);
    if(repeatElement != null) {
      return repeatElement.getFormElement(pm);
    }
    return null;
  }
  
  /**
   * Prints the data element definitions to the print stream specified
   * 
   * @param pm
   *    Persistence Manager to access the data
   * @param out
   *    Print stream to send the output to
   */
  public void printDataTree(PersistenceManager pm, PrintStream out) {
    printTreeHelper(elementTreeRoot, pm, out);
  }

  /**
   * Recursive helper function that prints the data elements definitions to the 
   * print stream specified
   *  
   * @param node
   *    node to be processed
   * @param pm
   *    Persistence Manager to be used 
   * @param out
   *    Print stream to send the output to
   */
  private void printTreeHelper(FormElement node, PersistenceManager pm, PrintStream out) {
    if (node == null) {
      return;
    }
    out.println(node.toString());
    List<FormElement> children = node.getChildren(pm);
    for (FormElement child : children) {
      printTreeHelper(child, pm, out);
    }
  }

  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Form)) {
      return false;
    }
    Form other = (Form) obj;
    return other.key.equals(key) && other.odkId.equals(odkId)
        && other.viewableName.equals(viewableName) && other.elementTreeRoot.equals(elementTreeRoot)
        && other.creationDate.equals(creationDate) && other.updateDate.equals(updateDate)
        && other.creationUser.equals(creationUser) && other.originalForm.equals(originalForm);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // TODO: reduce so it does not use bigInt
    BigInteger hashCode = BigInteger.valueOf(13);
    hashCode = hashCode.add(BigInteger.valueOf(key.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(viewableName.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(elementTreeRoot.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(creationDate.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(updateDate.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(creationUser.hashCode()));
    hashCode = hashCode.add(BigInteger.valueOf(originalForm.hashCode()));
    return hashCode.intValue();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return viewableName;
  }

  /**
   * Static function to retrieve a form with the specified ODK id 
   * from the datastore
   *
   * @param pm
   *    Persistence Manager to use to retrieve form
   * @param odkId
   *    The ODK identifier that identifies the form
   * 
   * @return
   *    The ODK aggregate form definition/conversion object
   * 
   * @throws ODKFormNotFoundException
   * Thrown when a form was not able to be found with the
   * corresponding ODK ID
   */
  public static Form retrieveForm(PersistenceManager pm, String odkId)
      throws ODKFormNotFoundException {

    // TODO: consider using memcache to have survey info in memory for faster
    // response times

    if (odkId == null) {
      return null;
    }

    Key formKey = KeyFactory.createKey(Form.class.getSimpleName(), odkId);
    Form form = null;

    try {
      form = pm.getObjectById(Form.class, formKey);
    } catch (javax.jdo.JDOObjectNotFoundException e) {
      throw new ODKFormNotFoundException(e);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return form;
  }
}
