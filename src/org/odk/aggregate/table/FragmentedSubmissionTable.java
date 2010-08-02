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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.kxml2.kdom.*;
import org.kxml2.io.*;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.servlet.FragmentedCsvServlet;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionFieldType;
import org.odk.aggregate.submission.SubmissionRepeat;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.SubmissionValue;
import org.odk.aggregate.submission.type.StringSubmissionType;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * Fetches and returns an xml-wrapped CSV representation of a
 * specified number of records in a Form or in a repeated group
 * within a Form.
 * 
 * The xml-wrapped representation maintains a &lt;cursor/&gt; object so that 
 * follow-on requests passing in that &lt;cursor/&gt; object may retrieve 
 * subsequent records in the Form or in the repeated group within a Form.
 */
public class FragmentedSubmissionTable extends SubmissionTable {
	/**
	 * Cursor value that should be used to continue the query.
	 */
	protected String websafeCursorString = null;

	/**
	 * Constructor.
	 * 
	 * @see org.odk.aggregate.table.SubmissionTable(String webserverURL, String odkIdentifier,
      EntityManager entityManager, int fetchSizeLimit)
	 * 
	 * @param webserverURL
	 *            hostname:port/app-path of this webserver
	 * @param odkIdentifier
	 *            the ODK id of the form (form identity)
	 * @param entityManager
	 *            the persistence manager used to manage generating the tables
	 * @param fetchSizeLimit
	 * 			  the maximum number of entries to fetch
	 * 
	 * @throws ODKFormNotFoundException
	 */
	public FragmentedSubmissionTable(String webserverURL, String odkIdentifier,
			EntityManager entityManager, int fetchSizeLimit) throws ODKFormNotFoundException {
		super(webserverURL, odkIdentifier, entityManager, fetchSizeLimit);
	}

	/**
	 * Emits the xml wrapped cursor and csv output of a given resultTable.
	 * 
	 * @param out
	 * 			the output stream to write to
	 * @param resultTable
	 * 			the results to format and wrap in xml
	 * @throws IOException 
	 */
	public void emitXmlWrappedCsv(Writer out, ResultTable resultTable) throws IOException {

		Document d = new Document();
		d.setStandalone(true);
		d.setEncoding(ServletConsts.ENCODE_SCHEME);
		String namespace = "";
		Element e = d.createElement(namespace, "entries");
		d.addChild(0, Node.ELEMENT, e);
		int idx = 0;
		e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
		if ( websafeCursorString != null ) {
			Element cursor = d.createElement(namespace, "cursor");
			e.addChild(idx++, Node.ELEMENT, cursor);
			cursor.addChild(0, Node.TEXT, websafeCursorString);
			e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
		}
		Element header = d.createElement(namespace, "header");
		e.addChild(idx++, Node.ELEMENT, header);
		header.addChild(0, Node.TEXT, generateCommaSeperatedRow(resultTable.getHeader().iterator()));
		e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);

		Element resultRow;
		// generate rows
		for (List<String> row : resultTable.getRows()) {
			resultRow = d.createElement(namespace, "result");
			e.addChild(idx++, Node.ELEMENT, resultRow);
			String csvRow = generateCommaSeperatedRow(row.iterator());
			resultRow.addChild(0, Node.TEXT, csvRow);
			e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
		}

		KXmlSerializer serializer = new KXmlSerializer();
		serializer.setOutput(out);
		// setting the response content type emits the xml header.
		// just write the body here...
		d.writeChildren(serializer); 
	}

	/**
	 * Create the comma separated row with proper doubling of embedded quotes.
	 * 
	 * @param itr
	 *            string values to be separated by commas
	 * @return string containing comma separated values
	 */
	private String generateCommaSeperatedRow(Iterator<String> itr) {
		String row = BasicConsts.EMPTY_STRING;
		while (itr.hasNext()) {
			// replace all quotes in the string with doubled-quotes
			// then wrap the whole thing with quotes.  Nulls are 
			// distinguished from empty strings by the lack of a 
			// value in that position (e.g., ,, vs ,"",)
			String original = itr.next();
			String str = "";
			if (original != null ) {
				str = BasicConsts.QUOTE 
					+ original.replace(BasicConsts.QUOTE, BasicConsts.QUOTE_QUOTE) 
					+ BasicConsts.QUOTE;
			}
			row += str;
			if (itr.hasNext()) {
				row += BasicConsts.CSV_DELIMITER;
			}
		}
		return row;
	}

	/**
	 * Create the link for blobs (images, audio, video).
	 * 
	 * @param subKey
	 *            key to the blob entity
	 * @param propertyName
	 *            entity's property to retrieve and display (unused)
	 * 
	 * @return link to access the blob
	 */
	@Override
	protected String createViewLink(Key subKey, String propertyName) {
		Map<String, String> properties = createViewLinkProperties(subKey);
		return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl()
				+ ImageViewerServlet.ADDR_BLOB, properties);
	}

	/**
	 * Create the link to retrieve all repeat records of the indicated
	 * type for the given parent record.
	 * 
	 * @param repeat
	 *            the repeat definition value.  Its property name 
	 *            identifies the FormElement containing all such repeats.

	 * @param parentSubmissionSetKey
	 *            the parent key for the submission set that contains 
	 *            the repeat value.
	 * 
	 * @return link to repeat results specific to this parent record
	 */
	@Override
	protected String createRepeatLink(SubmissionRepeat repeat,
			Key parentSubmissionSetKey) {
		FormElement element = form
				.getBeginningElement(repeat.getPropertyName());
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ServletConsts.ODK_ID, getKeySetReference(parentSubmissionSetKey, element.getKey()));
		return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl()
				+ FragmentedCsvServlet.ADDR, properties);
	}

	/**
	 * Construct a URL to access this one record.
	 * 
	 * @param selfKey
	 * @return
	 */
	protected String createSelfLink(Key selfKey, Key formElementKey) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ServletConsts.ODK_ID, getKeySetReference(selfKey, formElementKey));
		return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl()
				+ FragmentedCsvServlet.ADDR, properties);
	}

	/**
	 * Construct a URL to access this one record.
	 * 
	 * @param parentKey
	 * 			key for the parent (typically without type ancestry)
	 * @param formElementKey
	 * 			our well-formed type hierarchy
	 * @return
	 */
	protected String createParentLink(Key parentKey, Key formElementKey) {
		Map<String, String> properties = new HashMap<String, String>();
		String parentKeyName;
		if ( parentKey.getName() == null ) {
			// if the parent name is null, then we reconstruct the name using 
			// the kind field, which is the concatenation of (formName + elementName)
			// to construct the name field (elementName + formName)
			Key formKey = formElementKey;
			while ( formKey.getParent() != null ) formKey = formKey.getParent();
			String formName = formKey.getName();
			String elementName = parentKey.getKind().substring(formName.length());
			parentKeyName = elementName + formName;
			if ( parentKeyName.equals(formName) ) {
				// this is the form key... 
				// Display the form key under the form element root so that
				// it is more symmetric with the placement of the repeat group 
				// keys.  To make that happen, fudge the parentKeyName to be 
				// the name of the root element of the form.
				formKey = formElementKey;
				while ( formKey.getParent() != null && formKey.getParent().getParent() != null ) { 
					formKey = formKey.getParent();
				}
				parentKeyName = formKey.getName();
			}
		} else {
			parentKeyName = parentKey.getName();
		}
		// and now that we have the well-formed parent name, we need to find
		// that parent in the formElementKey hierarchy.
		while ( formElementKey != null &&
			    !parentKeyName.equals(formElementKey.getName()) ) {
			formElementKey = formElementKey.getParent();
		}
		
		if ( formElementKey == null ) {
			throw new IllegalStateException("unexpectedly cannot find parent");
		}

		properties.put(ServletConsts.ODK_ID, getKeySetReference(parentKey, formElementKey));
		return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl()
				+ FragmentedCsvServlet.ADDR, properties);
	}

	/**
	 * Construct the path for the key.
	 * 
	 * Key is to either a Form or a FormElement. The way the key is obtained
	 * alters some of the features of the key.  It is important to be consistent
	 * with the key construction.
	 * 
	 * The end result should be a slash-separated path of the repeating groups
	 * within a form. If the form is defined as:
	 * 
	 * <pre>
	 * {{{
	 * <model>
	 *   <instance>
	 *     <data id="myDataForm">
	 *        <field1/>
	 *        <field2/>
	 *        <nested>
	 *          <field3/>
	 *        </nested>
	 *        <nestedAnother>
	 *          <repeated1>
	 *            <field4/>
	 *            <repeated2>
	 *              <field5/>
	 *            </repeated2>
	 *          </repeated1>
	 *        </nestedAnother>
	 *      </data>
	 *    </instance>
	 *  </model>
	 *  ...
	 *  <repeat nodeset="/data/repeated1">
	 *    <repeat nodeset="/data/repeated2">
	 *       ...
	 *    </repeat>
	 *  </repeat>
	 * }}}
	 * </pre>
	 * 
	 * Then there are three FormElement structures, one for the top-level form,
	 * one for the repeated1 elements, and one for the repeated2 elements.
	 * 
	 * The paths inside the form are therefore: myDataForm/data
	 * myDataForm/data/repeated1 myDataForm/data/repeated1/repeated2
	 * 
	 * @param key
	 * @return
	 */
	private String getKeyPath(Key key) {
		String nameSuffix = form.getOdkId();
		List<String> path = new ArrayList<String>();
		// build path in reverse order
		while (key != null) {
			String name = key.getName();
			if (name == null) {
				// degenerate case of being passed a Form key vs. FormElement
				path.add(key.getKind());
			} else if (name.equals(nameSuffix)) {
				// terminating case for FormElement key
				path.add(name);
			} else {
				path
						.add(name.substring(0, name.length()
								- nameSuffix.length()));
			}
			key = key.getParent();
		}
		Collections.reverse(path);
		StringBuilder b = new StringBuilder();
		for (String s : path) {
			b.append(BasicConsts.FORWARDSLASH);
			b.append(s);
		}
		return b.substring(1); // drop the leading slash
	}

	/**
	 * Construct a key reference that will return the rows of the given
	 * form element key type that are associated with the given criteria key
	 * (if that functionality is implemented).
	 * 
	 * This is used to produce the xpath-like reference expressions for 
	 * both the parent_key restrictions and the self-key restrictions.
	 * 
	 * @param criteriaKey 
	 * 			the specific key that will be value-matched.
	 * @param formElementKey 
	 * 			the form element (type) being fetched.
	 * @return
	 */
	private String getKeySetReference(Key criteriaKey, Key formElementKey) {
		// otherwise, figure stuff out...
		String criteriaKeyName;
		if ( criteriaKey.getName() == null ) {
			// if the parent name is null, then we reconstruct the name using 
			// the kind field, which is the concatenation of (formName + elementName)
			// to construct the name field (elementName + formName)
			Key formKey = formElementKey;
			while ( formKey.getParent() != null ) formKey = formKey.getParent();
			String formName = formKey.getName();
			String elementName = criteriaKey.getKind().substring(formName.length());
			criteriaKeyName = elementName + formName;
			if ( criteriaKeyName.equals(formName) ) {
				// this is the form key... 
				// Display the form key under the form element root so that
				// it is more symmetric with the placement of the repeat group 
				// keys.  To make that happen, fudge the criteria key to be 
				// the name of the root element of the form.
				formKey = formElementKey;
				while ( formKey.getParent() != null && formKey.getParent().getParent() != null ) { 
					formKey = formKey.getParent();
				}
				criteriaKeyName = formKey.getName();
			}
		} else {
			criteriaKeyName = criteriaKey.getName();
		}
		
		String nameSuffix = form.getOdkId();
		List<String> path = new ArrayList<String>();
		// build path in reverse order
		while (formElementKey != null) {
			String term = formElementKey.getName();
			if (term == null) {
				// degenerate case of being passed a Form key vs. FormElement
				term = formElementKey.getKind();
			} else if (! term.equals(nameSuffix)) {
				term = term.substring(0, term.length()
								- nameSuffix.length());
			}
			if ( formElementKey.getName().equals(criteriaKeyName) ) {
				term = term
				+ ServletConsts.ELEMENT_REFERENCE_KEY_BEGIN_STRING
				+ KeyFactory.keyToString(criteriaKey) 
				+ ServletConsts.ELEMENT_REFERENCE_KEY_END_STRING;
			}
			path.add(term);
			formElementKey = formElementKey.getParent();
		}
		Collections.reverse(path);
		StringBuilder b = new StringBuilder();
		for (String s : path) {
			b.append(BasicConsts.FORWARDSLASH);
			b.append(s);
		}
		return b.substring(1); // drop the leading slash
	}
	
	/**
	 * Queries datastore entries and makes a Result Table for the given Odk ID.
	 * 
	 * Reworked from SubmissionTable.generateResultTable
	 * 
	 * @param elementReference
	 * 			  if specified, identifies a specific Form record
	 * @param websafeCursorString
	 *            the String for a cursor if this query is being repeated. is
	 *            null if this is the first attempt
	 * @return ResultTable of Submissions.
	 * @throws ODKIncompleteSubmissionData
	 */
	public ResultTable generateResultTableFrag( Key elementReference,
			String websafeCursorString)
			throws ODKIncompleteSubmissionData {

		// create results table
		generatePropertyNamesAndHeaders(form.getElementTreeRoot(), true);
		headers.add("KEY");
		propertyNames.add("KEY");
		headerTypes.put("KEY", SubmissionFieldType.STRING);

		ResultTable results = new ResultTable(headers);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		List<Entity> submissionEntities;
		
		if ( elementReference != null ) {
			Entity element = null;
			try {
				element = ds.get(elementReference);
			} catch (EntityNotFoundException e) {
				// e.printStackTrace();
			}
			submissionEntities = new ArrayList<Entity>();
			if ( element != null ) {
				submissionEntities.add(element);
			}
			setMoreRecords(false);
		} else {
			Cursor cursor = null;
			if (websafeCursorString != null) {
				cursor = Cursor.fromWebSafeString(websafeCursorString);
			}
	
			// retrieve submissions
			Query surveyQuery = new Query(odkId);
			surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG,
					Query.SortDirection.ASCENDING);
			surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG,
					Query.FilterOperator.GREATER_THAN, TableConsts.EPOCH);
	
			QueryResultList<Entity> rl;
			if (cursor == null) {
				rl = ds.prepare(surveyQuery).asQueryResultList(
						FetchOptions.Builder.withLimit(fetchLimit));
			} else {
				rl = ds.prepare(surveyQuery).asQueryResultList(
						FetchOptions.Builder.withLimit(fetchLimit).cursor(cursor));
			}
	
			// if we have a full set of records, return the 
			// cursor value for follow-on queries.  Otherwise, 
			// report the end of the dataset.
			if (rl.size() == fetchLimit) {
				setMoreRecords(true);
				this.websafeCursorString = rl.getCursor()
						.toWebSafeString();
			}
			submissionEntities = rl;
		}

		// create a row for each submission
		int count = 0;
		while (count < submissionEntities.size()) {
			Entity subEntity;
			subEntity = submissionEntities.get(count);

			Submission sub = new Submission(subEntity, form);
			Map<String, SubmissionValue> valueMap = sub
					.getSubmissionValuesMap();
	
			// and add the self-KEY to the map...
			StringSubmissionType submissionData = new StringSubmissionType(
					"KEY");
			submissionData.setValueFromString(createSelfLink(sub.getKey(), form.getElementTreeRoot().getKey()));
			valueMap.put("KEY", submissionData);

			List<String> row = new ArrayList<String>();
			processSubmissionIntoRow(valueMap, sub, row);
			results.addRow(sub.getKey(), row);

			count++;
		}

		return results;
	}

	/**
	 * Generates a result table that contains repeats of the table whose parent
	 * is specified by ODKId and datastore entry type is kind.
	 * 
	 * @param kind
	 *            datastore entry type.
	 * @param elementKey
	 *            the element type
	 * @param elementReference
	 *            if not null, the specific element instance to retrieve.
	 * @param parentKey
	 * 			  if not null, filter results to only include element types
	 *            with this value for their parent.
	 * @param websafeCursorString
	 *            cursor string to use if resuming a query. 
	 *            null if this is a new query.
	 * @return ResultTable containing the repeats
	 * @throws ODKIncompleteSubmissionData
	 * @throws ODKFormNotFoundException
	 */
	public ResultTable generateRepeatResultTableFrag(String kind,
			Key elementKey, Key elementReference, Key parentKey,
			String websafeCursorString) throws ODKIncompleteSubmissionData,
			ODKFormNotFoundException {
		
		FormElement formElement = em.getReference(FormElement.class, elementKey);
		if (formElement == null) {
			throw new ODKIncompleteSubmissionData();
		}

		// create results table
		generatePropertyNamesAndHeaders(formElement, false);
		// add the parent key and key so the tables can be linked later
		headers.add(PersistConsts.PARENT_KEY_PROPERTY);
		propertyNames.add(PersistConsts.PARENT_KEY_PROPERTY);
		headerTypes.put(PersistConsts.PARENT_KEY_PROPERTY, SubmissionFieldType.STRING);

		headers.add("KEY");
		propertyNames.add("KEY");
		headerTypes.put("KEY", SubmissionFieldType.STRING);

		ResultTable results = new ResultTable(headers);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		List<Entity> submissionEntities;

		if ( elementReference != null ) {
			submissionEntities = new ArrayList<Entity>();
			setMoreRecords(false);
			Entity element = null;
			try {
				element = ds.get(elementReference);
			} catch (EntityNotFoundException e) {
				// e.printStackTrace();
			}
			if ( parentKey == null ||
				 parentKey.equals(element.getProperty(PersistConsts.PARENT_KEY_PROPERTY)) ) {
				// add the (single) element to the result set...
				submissionEntities.add(element);
			}
		} else {
			// create a row for each submission
			Query surveyQuery = new Query(kind);
			if ( parentKey != null ) {
				surveyQuery.addFilter(PersistConsts.PARENT_KEY_PROPERTY, FilterOperator.EQUAL, parentKey);
			}
	
			// note: needs to be a QueryResultList so we can extract a cursor later.
			QueryResultList<Entity> rl;
	
			if (websafeCursorString != null) {
				Cursor cursor = Cursor.fromWebSafeString(websafeCursorString);
				rl = ds.prepare(surveyQuery).asQueryResultList(
						FetchOptions.Builder.withLimit(fetchLimit).cursor(cursor));
			} else {
				rl = ds.prepare(surveyQuery).asQueryResultList(
						FetchOptions.Builder.withLimit(fetchLimit));
			}

			// if we have a full set of records, return the 
			// cursor value for follow-on queries.  Otherwise, 
			// report the end of the dataset.
			if (rl.size() == fetchLimit) {
				setMoreRecords(true);
				this.websafeCursorString = rl.getCursor()
						.toWebSafeString();
			}
			submissionEntities = rl;
		}

		for (Entity subEntity : submissionEntities) {
			SubmissionSet sub = new SubmissionSet(subEntity, form);
			Map<String, SubmissionValue> valueMap = sub
					.getSubmissionValuesMap();

			StringSubmissionType submissionData;
			// and add the PARENT_KEY to the map...
			submissionData = new StringSubmissionType(PersistConsts.PARENT_KEY_PROPERTY);
			submissionData
					.setValueFromString(createParentLink(sub.getParentSubmissionSetKey(),elementKey.getParent()));
			valueMap.put(PersistConsts.PARENT_KEY_PROPERTY, submissionData);
			// and add the self-KEY to the map...
			submissionData = new StringSubmissionType("KEY");
			submissionData.setValueFromString(createSelfLink(sub.getKey(),elementKey));
			valueMap.put("KEY", submissionData);

			List<String> row = new ArrayList<String>();
			for (String propertyName : propertyNames) {
				processSubmissionFieldValue(sub.getKey(), valueMap, row,
						propertyName);
			}
			results.addRow(sub.getKey(), row);
		}

		return results;
	}

	/**
	 * getter for websafeCursorString
	 * 
	 * @return the websafeCursorString
	 */
	public String getCursorStr() {
		return websafeCursorString;
	}
}
