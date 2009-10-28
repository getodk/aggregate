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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.remoteserver.FusionTable;
import org.odk.aggregate.form.remoteserver.GoogleSpreadsheet;
import org.odk.aggregate.form.remoteserver.RemoteServer;
import org.odk.aggregate.form.remoteserver.RhizaInsight;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * Persistable definition of the XForm that defines how to store submissions to
 * the datastore. Includes form elements that know how to properly convert the
 * data to/from the datastore.
 * 
 * @author wbrunette@gmail.com
 * 
 */
@Entity
public class Form {
	/**
	 * GAE datastore key that uniquely identifies the form entity
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	/**
	 * ODK identifier that uniquely identifies the form
	 */
	@Enumerated
	private String odkId;

	/**
	 * The name that is viewable on ODK Aggregate
	 */
	@Enumerated
	private String viewableName;

	/**
	 * Root of the form elements that describe how to convert the data from the
	 * XML submissions to the gae datastore types
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private FormElement elementTreeRoot;

	/**
	 * The date the form was created
	 */
	@Enumerated
	private Date creationDate;

	/**
	 * The last date the form was updated
	 */
	@Enumerated
	private Date updateDate;

	/**
	 * The user who uploaded/created the form
	 */
	@Enumerated
	private String creationUser;

	/**
	 * The original XML that specified the form
	 */
	@Enumerated
	private Text originalForm;

	/**
	 * String used to specify the filename when outputting the XML to file
	 */
	@Enumerated
	private String fileName;

	/**
	 * Specifies whether the form is downloadable
	 */
	@Enumerated
	private Boolean downloadEnabled;

	/**
	 * Specifies whether the system can receive new form submissions
	 */
	@Enumerated
	private Boolean submissionEnabled;

	/**
	 * A list of spreadsheets to update
	 */
	@OneToMany(cascade = CascadeType.ALL)
	private List<GoogleSpreadsheet> spreadsheetExternalRepos;

	/**
	 * A list of rhiza insight servers to update
	 */
	@OneToMany(cascade = CascadeType.ALL)
	private List<RhizaInsight> insightExternalRepos;

	/**
	 * A list of fusion tables to update
	 */
	@OneToMany(cascade = CascadeType.ALL)
	private List<FusionTable> fusionTableExternalRepos;

	@Transient
	private Map<String, FormElement> repeatElementMap;

	/**
	 * Construct a form definition that can be persisted
	 * 
	 * @param formOdkId
	 *            Form's unique ODK id
	 * @param viewableName
	 *            Name to be displayed
	 * @param user
	 *            User that created the form
	 * @param form
	 *            The xml definition or the form
	 * @param fileName
	 *            The name of xml used for outputting form
	 */
	public Form(String formOdkId, String viewableName, String user,
			String form, String fileName) {
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

	public List<RemoteServer> getExternalRepos() {
		List<RemoteServer> allExternalRepos = new ArrayList<RemoteServer>();
		if (spreadsheetExternalRepos != null)
			allExternalRepos.addAll(spreadsheetExternalRepos);
		if (insightExternalRepos != null)
			allExternalRepos.addAll(insightExternalRepos);
		if (fusionTableExternalRepos != null)
			allExternalRepos.addAll(fusionTableExternalRepos);
		return allExternalRepos;
	}

	public List<FusionTable> getFusionTableExternalRepos() {
		return fusionTableExternalRepos;
	}

	public void removeFusionTableExternalRepos() {
		fusionTableExternalRepos.clear();
	}

	public void addFusionExternalRepos(FusionTable fusionTableInstance) {
		if (fusionTableExternalRepos == null) {
			fusionTableExternalRepos = new ArrayList<FusionTable>();
		}
		fusionTableExternalRepos.add(fusionTableInstance);
	}

	public List<RhizaInsight> getInsightExternalRepos() {
		return insightExternalRepos;
	}

	public void removeInsightExternalRepos() {
		insightExternalRepos.clear();
	}

	public void addInsightExternalRepos(RhizaInsight insightInstance) {
		if (insightExternalRepos == null) {
			insightExternalRepos = new ArrayList<RhizaInsight>();
		}
		insightExternalRepos.add(insightInstance);
	}

	public void addGoogleSpreadsheet(GoogleSpreadsheet sheet) {
		if (spreadsheetExternalRepos == null) {
			spreadsheetExternalRepos = new ArrayList<GoogleSpreadsheet>();
		}
		spreadsheetExternalRepos.add(sheet);
	}

	public GoogleSpreadsheet getGoogleSpreadsheetWithName(String name) {
		if (spreadsheetExternalRepos == null) {
			return null;
		}

		for (GoogleSpreadsheet sheet : spreadsheetExternalRepos) {
			if (sheet.getSpreadsheetName().equals(name)) {
				return sheet;
			}
		}
		return null;
	}

	public void removeGoogleSpreadsheet(GoogleSpreadsheet sheet) {
		spreadsheetExternalRepos.remove(sheet);
	}

	/**
	 * Get the GAE datastore key that uniquely identifies the form entity
	 * 
	 * @return datastore key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * Get the ODK identifier that identifies the form
	 * 
	 * @return odk identifier
	 */
	public String getOdkId() {
		return odkId;
	}

	/**
	 * Get the name that is viewable on ODK Aggregate
	 * 
	 * @return viewable name
	 */
	public String getViewableName() {
		return viewableName;
	}

	/**
	 * Get the date the form was created
	 * 
	 * @return creation date
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Get the last date the form was updated
	 * 
	 * @return last date form was updated
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * Get the user who uploaded/created the form
	 * 
	 * @return user name
	 */
	public String getCreationUser() {
		return creationUser;
	}

	/**
	 * Set the name that is viewable on ODK Aggregate
	 * 
	 * @param viewableName
	 *            name to be displayed
	 */
	public void setViewableName(String viewableName) {
		this.viewableName = viewableName;
		this.updateDate = new Date();
	}

	/**
	 * Get the original XML that specified the form
	 * 
	 * @return get XML definition of XForm
	 */
	public String getOriginalForm() {
		return originalForm.getValue();
	}

	/**
	 * Get the file name to be used when generating the XML file describing from
	 * 
	 * @return xml file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Set the file name to be used when generating the XML file describing from
	 * 
	 * @param fileName
	 *            file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Set the root of the form elements that describe how to convert the data
	 * from the XML submissions to the gae datastore types
	 * 
	 * @param formElementTreeRoot
	 *            root of the form element tree
	 * 
	 */
	public void setElementTreeRoot(FormElement formElementTreeRoot) {
		this.elementTreeRoot = formElementTreeRoot;
	}

	/**
	 * Get the root of the form elements that describe how to convert the data
	 * from the XML submissions to the gae datastore types
	 * 
	 * @return form element root
	 */
	public FormElement getElementTreeRoot() {
		return elementTreeRoot;
	}

	/**
	 * Gets whether the form can be downloaded
	 * 
	 * @return true if form can be downloaded, false otherwise
	 */
	public Boolean getDownloadEnabled() {
		return downloadEnabled;
	}

	/**
	 * Sets a boolean value of whether the form can be downloaded
	 * 
	 * @param downloadEnabled
	 *            set to true if form can be downloaded, false otherwise
	 * 
	 */
	public void setDownloadEnabled(Boolean downloadEnabled) {
		this.downloadEnabled = downloadEnabled;
	}

	/**
	 * Gets whether a new submission can be received
	 * 
	 * @return true if a new submission can be received, false otherwise
	 */
	public Boolean getSubmissionEnabled() {
		return submissionEnabled;
	}

	/**
	 * Sets a boolean value of whether a new submission can be received
	 * 
	 * @param submissionEnabled
	 *            set to true if a new submission can be received, false
	 *            otherwise
	 * 
	 */
	public void setSubmissionEnabled(Boolean submissionEnabled) {
		this.submissionEnabled = submissionEnabled;
	}

	public FormElement getBeginningElement(String elementName) {

		// check if it's the root of the form
		if (elementTreeRoot.getElementName().equals(elementName)) {
			return elementTreeRoot;
		}

		// check to see if repeatRootMap needs to be created
		// NOTE: this assumes the form does NOT get altered!!!
		if (repeatElementMap == null) {
			repeatElementMap = new HashMap<String, FormElement>();
			populateRepeatElementMap(elementTreeRoot);
		}

		return repeatElementMap.get(elementName);
	}

	private void populateRepeatElementMap(FormElement node) {
		if (node == null) {
			return;
		}
		if (node.isRepeatable()) {
			repeatElementMap.put(node.getElementName(), node);
		}
		List<FormElement> children = node.getChildren();
		if (children == null) {
			return;
		}
		for (FormElement child : children) {
			populateRepeatElementMap(child);
		}
	}

	/**
	 * Prints the data element definitions to the print stream specified
	 * 
	 * @param out
	 *            Print stream to send the output to
	 */
	public void printDataTree(PrintStream out) {
		printTreeHelper(elementTreeRoot, out);
	}

	/**
	 * Recursive helper function that prints the data elements definitions to
	 * the print stream specified
	 * 
	 * @param node
	 *            node to be processed
	 * @param out
	 *            Print stream to send the output to
	 */
	private void printTreeHelper(FormElement node, PrintStream out) {
		if (node == null) {
			return;
		}
		out.println(node.toString());
		List<FormElement> children = node.getChildren();
		if (children == null) {
			return;
		}
		for (FormElement child : children) {
			printTreeHelper(child, out);
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
		return (key == null ? (other.key == null) : (key.equals(other.key)))
				&& (odkId == null ? (other.odkId == null) : (odkId
						.equals(other.odkId)))
				&& (viewableName == null ? (other.viewableName == null)
						: (viewableName.equals(other.viewableName)))
				&& (elementTreeRoot == null ? (other.elementTreeRoot == null)
						: (elementTreeRoot.equals(other.elementTreeRoot)))
				&& (creationDate == null ? (other.creationDate == null)
						: (creationDate.equals(other.creationDate)))
				&& (updateDate == null ? (other.updateDate == null)
						: (updateDate.equals(other.updateDate)))
				&& (creationUser == null ? (other.creationUser == null)
						: (creationUser.equals(other.creationUser)))
				&& (originalForm == null ? (other.originalForm == null)
						: (originalForm.equals(other.originalForm)))
				&& (fileName == null ? (other.fileName == null) : (fileName
						.equals(other.fileName)))
				&& (downloadEnabled == null ? (other.downloadEnabled == null)
						: (downloadEnabled.equals(other.downloadEnabled)))
				&& (submissionEnabled == null ? (other.submissionEnabled == null)
						: (submissionEnabled.equals(other.submissionEnabled)));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 13;
		if (key != null)
			hashCode += key.hashCode();
		if (odkId != null)
			hashCode += odkId.hashCode();
		if (viewableName != null)
			hashCode += viewableName.hashCode();
		if (elementTreeRoot != null)
			hashCode += elementTreeRoot.hashCode();
		if (creationDate != null)
			hashCode += creationDate.hashCode();
		if (updateDate != null)
			hashCode += updateDate.hashCode();
		if (creationUser != null)
			hashCode += creationUser.hashCode();
		if (originalForm != null)
			hashCode += originalForm.hashCode();
		if (fileName != null)
			hashCode += fileName.hashCode();
		if (downloadEnabled != null)
			hashCode += downloadEnabled.hashCode();
		if (submissionEnabled != null)
			hashCode += submissionEnabled.hashCode();
		return hashCode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return viewableName;
	}

	/**
	 * Static function to retrieve a form with the specified ODK id from the
	 * datastore
	 * 
	 * @param em
	 *            Entity Manager to use to retrieve form
	 * @param odkId
	 *            The ODK identifier that identifies the form
	 * 
	 * @return The ODK aggregate form definition/conversion object
	 * 
	 * @throws ODKFormNotFoundException
	 *             Thrown when a form was not able to be found with the
	 *             corresponding ODK ID
	 */
	public static Form retrieveForm(EntityManager em, String odkId)
			throws ODKFormNotFoundException {

		// TODO: consider using memcache to have survey info in memory for
		// faster
		// response times

		if (odkId == null) {
			return null;
		}

		Key formKey = KeyFactory.createKey(Form.class.getSimpleName(), odkId);
		Form form = null;

		try {
			form = em.getReference(Form.class, formKey);
		} catch (EntityNotFoundException e) {
			throw new ODKFormNotFoundException(e);
		} catch (Exception e) {
			// TODO: change for better error handling and report bug to GAE
			throw new ODKFormNotFoundException(e);
		}

		return form;
	}
}
