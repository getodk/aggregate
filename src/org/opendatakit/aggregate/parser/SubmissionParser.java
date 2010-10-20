/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData.Reason;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parsers submission xml and saves to datastore
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionParser {

	/**
	 * Odk Id of submission
	 */
	private String odkId;

	private FormDefinition formDefinition;

	/**
	 * Root of XML submission
	 */
	private Element root;

	/**
	 * Submission object created from xml submission
	 */
	private Submission submission;

	/**
	 * Data items obtained from a multipart submission
	 */
	private MultiPartFormData submissionFormItems;

	private final Datastore ds;
	private final User user;
	private final Realm realm;

	private EntityKey topLevelTableKey = null;

	/**
	 * Construct an ODK submission by processing XML submission to extract
	 * values
	 * 
	 * @param inputStreamXML
	 *            xml submission input stream
	 * @param datastore
	 *            TODO
	 * @throws IOException
	 * @throws ODKFormNotFoundException
	 *             thrown if a form is not found with a matching ODK ID
	 * @throws ODKParseException
	 * @throws ODKIncompleteSubmissionData
	 * @throws ODKConversionException
	 * @throws ODKDatastoreException
	 */
	public SubmissionParser(InputStream inputStreamXML, Datastore datastore,
			User user, Realm realm) throws IOException, ODKFormNotFoundException,
			ODKParseException, ODKIncompleteSubmissionData,
			ODKConversionException, ODKDatastoreException {
		this.ds = datastore;
		this.user = user;
		this.realm = realm;
		constructorHelper(inputStreamXML);
	}

	/**
	 * Construct an ODK submission by processing XML submission to extract
	 * values
	 * 
	 * @param submissionFormParser
	 *            multipart data submission that includes XML submission &
	 *            possibly other data
	 * @param datastore
	 *            TODO
	 * @throws IOException
	 * @throws ODKFormNotFoundException
	 *             thrown if a form is not found with a matching ODK ID
	 * @throws ODKParseException
	 * @throws ODKIncompleteSubmissionData
	 * @throws ODKConversionException
	 * @throws ODKDatastoreException
	 */
	public SubmissionParser(MultiPartFormData submissionFormParser,
			Datastore datastore, User user, Realm  realm) throws IOException,
			ODKFormNotFoundException, ODKParseException,
			ODKIncompleteSubmissionData, ODKConversionException,
			ODKDatastoreException {
		this.ds = datastore;
		this.user = user;
		this.realm = realm;
		if (submissionFormParser == null) {
			// TODO: review best error handling strategy
			throw new IOException("DID NOT GET A MULTIPARTFORMPARSER");
		}
		submissionFormItems = submissionFormParser;
		MultiPartFormItem submission = submissionFormItems
				.getFormDataByFieldName(ServletConsts.XML_SUBMISSION_FILE);
		if (submission == null) {
			// TODO: review best error handling strategy
			throw new IOException("DID NOT GET A SUBMISSION");
		}

		String submissionXML = submission.getStream().toString();
		System.out.println(submissionXML);
		constructorHelper(new ByteArrayInputStream(submissionXML.getBytes()));
	}

	/**
	 * Helper Constructor an ODK submission by processing XML submission to
	 * extract values
	 * 
	 * @param inputStreamXML
	 *            xml submission input stream
	 * 
	 * @throws IOException
	 * @throws ODKFormNotFoundException
	 *             thrown if a form is not found with a matching ODK ID
	 * @throws ODKParseException
	 * @throws ODKIncompleteSubmissionData
	 * @throws ODKConversionException
	 * @throws ODKDatastoreException
	 */
	private void constructorHelper(InputStream inputStreamXML)
			throws IOException, ODKFormNotFoundException, ODKParseException,
			ODKIncompleteSubmissionData, ODKConversionException,
			ODKDatastoreException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(inputStreamXML);
			root = doc.getDocumentElement();
			printNode(root);

			// check for odk id
			odkId = root.getAttribute(ParserConsts.ODK_ATTRIBUTE_NAME);

			// if odk id is not present use namespace
			if (odkId.equalsIgnoreCase(BasicConsts.EMPTY_STRING)) {
				String schema = root
						.getAttribute(ParserConsts.NAMESPACE_ATTRIBUTE);

				// TODO: move this into FormDefinition?
				if (schema == null) {
					throw new ODKIncompleteSubmissionData(Reason.ID_MISSING);
				}
				String httpPrefix = "http://";
				String httpsPrefix = "https://";

				odkId = schema;
				if (schema.startsWith(httpPrefix)) {
					odkId = schema.substring(httpPrefix.length());
				} else if (schema.startsWith(httpsPrefix)) {
					odkId = schema.substring(httpsPrefix.length());
				}
				odkId = odkId.replaceAll(
						"[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]",
						"_");
			}

		} catch (ParserConfigurationException e) {
			throw new IOException(e.getCause());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException(e.getCause());
		}
		String fullyQualifiedId = FormDefinition.extractWellFormedFormId(odkId, realm);

		formDefinition = FormDefinition.getFormDefinition(fullyQualifiedId, ds, user);

		submission = new Submission(formDefinition, ds, user);

		topLevelTableKey = submission.getKey();

		FormDataModel formRoot = formDefinition.getTopLevelGroup();
		processSubmissionElement(formRoot, root, submission, true, false);

		// save the elements inserted into the top-level submission
		submission.persist(ds, user);
		inputStreamXML.close();
	}

	/**
	 * Get submission object from parse
	 * 
	 * @return submission
	 */
	public Submission getSubmission() {
		return submission;
	}

	/**
	 * 
	 * Helper function to process submission by taking the form element and
	 * extracting the corresponding value from the XML submission. Recursively
	 * applies itself to children of the form element.
	 * 
	 * @param node
	 *            form element to parse from the XML submission
	 * @param submissionSet
	 *            the set of submission to add the submission value to
	 * @param submissionRoot
	 *            value true if root of submission, false otherwise
	 * @throws ODKParseException
	 * @throws ODKIncompleteSubmissionData
	 * @throws ODKConversionException
	 * @throws ODKDatastoreException
	 */
	private void processSubmissionElement(FormDataModel node,
			Element currentSubmissionElement, SubmissionSet submissionSet,
			boolean submissionRoot, boolean inRepeatList)
			throws ODKParseException, ODKIncompleteSubmissionData,
			ODKConversionException, ODKDatastoreException {
		if (node == null || currentSubmissionElement == null) {
			return;
		}

		// the element name of the fdm is the tag name...
		String submissionTag = node.getElementName();
		if (submissionTag == null) {
			return;
		}

		if (submissionRoot) {
			if (!currentSubmissionElement.getNodeName().equals(submissionTag)) {
				throw new ODKParseException(
						"Top level node does not match what was expected");
			}
		}

		if (inRepeatList) {
			// node is the repeat group outer tag...
			// currentSubmissionElement is an instance of the repeat group.
			// submissionSet is the set containing the repeat group.
			List<Element> elements = getElementsInTree(
					currentSubmissionElement, submissionTag);
			RepeatSubmissionType repeats = (RepeatSubmissionType) submissionSet
					.getElementValue(node);
			int i = 1;
			for (Element e : elements) {
				SubmissionSet repeatableSubmissionSet = new SubmissionSet(
						submissionSet, Long.valueOf(i++), node, formDefinition,
						topLevelTableKey, ds, user);
				processSubmissionElement(node, e, repeatableSubmissionSet,
						false, false);
				repeats.addSubmissionSet(repeatableSubmissionSet);
			}
		} else {
			// get the structure under the fdm tag name...
			List<Element> elements = getElements(currentSubmissionElement);
			if (elements.size() == 0) {
				throw new ODKParseException();
			}
			// and for each of these, they should be fields under the given fdm
			// and
			// values within the submissionSet
			for (Element e : elements) {
				// NOTE: this navigates into phantom tables...
				FormDataModel m = node.findElementByName(e.getNodeName());
				if (m == null) {
					continue;
					//throw new ODKParseException();
				}
				switch (m.getElementType()) {
				case PHANTOM: // if a relation needs to be divided in order to
								// fit
				case VERSIONED_BINARY: // association between BINARY and
										// VERSIONED_BINARY_CONTENT_BLOB
				case VERSIONED_BINARY_CONTENT_REF_BLOB: // association between
														// VERSIONED_BINARY and
														// REF_BLOB
				case REF_BLOB: // the table of the actual byte[] data (xxxBLOB)
				case LONG_STRING_REF_TEXT: // association between any field and
											// REF_TEXT
				case REF_TEXT: // the table of extended string values (xxxTEXT)
					throw new IllegalStateException(
							"Unexpectedly retrieved hidden elements!");
				case GROUP:
					// need to recurse on these elements keeping at the same
					// submissionSet...
					processSubmissionElement(m, e, submissionSet, false, false);
					break;
				case REPEAT:
					// recurse on these, keeping the same submissionSet so we
					// can find
					// the repeat element to store new submission sets under...
					processSubmissionElement(node, e, submissionSet, false,
							true);
					break;
				case STRING:
				case JRDATETIME:
				case JRDATE:
				case JRTIME:
				case INTEGER:
				case DECIMAL:
				case BOOLEAN:
				case SELECT1: // identifies SelectChoice table
				case SELECTN: // identifies SelectChoice table
				case GEOPOINT:
					String value = getSubmissionValue(e);
					((SubmissionField<?>) submissionSet.getElementValue(m))
							.setValueFromString(value);
					break;
				case BINARY: // identifies BinaryContent table
					value = getSubmissionValue(e);
					SubmissionField<?> submissionElement = ((SubmissionField<?>) submissionSet
							.getElementValue(m));
					processBinarySubmission(m, submissionElement, value);
					break;
				}
			}
		}
	}

	private void processBinarySubmission(FormDataModel node,
			SubmissionField<?> submissionElement, String value)
			throws ODKConversionException, ODKDatastoreException {
		
		// check to see if we received a multipart submission
		if (submissionFormItems == null) {
			// TODO: problem, only accept a base64 encoded in a direct XML post
			byte[] receivedBytes = Base64.decodeBase64(value.getBytes());
			// TODO: problem since we don't know how to tell what type of
			// binary without content type, defaulting to JPG
			submissionElement.setValueFromByteArray(receivedBytes,
					HtmlConsts.RESP_TYPE_IMAGE_JPEG, Long
							.valueOf(receivedBytes.length), null, ds, user);
		} else {
			// attempt to find binary data in multi-part form submission
			// first searching by file name, then field name
			MultiPartFormItem binaryData = submissionFormItems
					.getFormDataByFileName(value);
			if (binaryData == null) {
				binaryData = submissionFormItems.getFormDataByFieldName(value);
			}
			// after completing the search now check if found anything and
			// value, otherwise output error
			if (binaryData != null) {
				// determine the filename, if any...
				String fileName = binaryData.getFilename();
				if ( fileName == null || fileName.length() == 0 ) {
					fileName = null;
				}
				byte[] byteArray = binaryData.getStream().toByteArray();
				submissionElement.setValueFromByteArray(byteArray, binaryData
						.getContentType(), binaryData.getContentLength(), fileName,
						ds, user);
			} else {
				// TODO: decide if we want system to reject submission if file
				// is not found?
				System.err.println("UNABLE TO FIND VALUE OF " + value);
			}
		}
	}

	private List<Element> getElements(Element rootNode) {
		List<Element> elements = new ArrayList<Element>();

		NodeList nodeList = rootNode.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element) node);
			}
		}
		return elements;
	}

	private List<Element> getElementsInTree(Element rootNode,
			String submissionTag) {
		List<Element> elements = new ArrayList<Element>();

		// find the elements
		NodeList nodeList = rootNode.getElementsByTagName(submissionTag);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equals(submissionTag)) {
				elements.add((Element) node);
			}
		}
		return elements;
	}

	/**
	 * Extracts value from the XML submission element by getting value from the
	 * text node
	 * 
	 * @param element
	 *            element that has text child node that will contain the value
	 * 
	 * @return value contained in the XML submission
	 */
	private String getSubmissionValue(Element element) {
		// could not find element, return null
		if (element == null) {
			return null;
		}

		NodeList childNodeList = element.getChildNodes();
		// get element value
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node node = childNodeList.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String value = node.getNodeValue().trim();
				if (value.length() > 0) {
					return value;
				} // else go to next node
			}
		} // else has no value so continue

		return null;
	}

	/**
	 * Recursive function that prints the nodes from an XML tree
	 * 
	 * @param node
	 *            xml node to be recursively printed
	 */
	private void printNode(Element node) {
		System.out.println(ParserConsts.NODE_FORMATTED + node.getTagName());
		if (node.hasAttributes()) {
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				System.out.println(ParserConsts.ATTRIBUTE_FORMATTED
						+ attr.getNodeName() + BasicConsts.EQUALS
						+ attr.getNodeValue());
			}
		}
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					printNode((Element) child);
				} else if (child.getNodeType() == Node.TEXT_NODE) {
					String value = child.getNodeValue().trim();
					if (value.length() > 0) {
						System.out
								.println(ParserConsts.VALUE_FORMATTED + value);
					}
				}
			}
		}

	}

	/**
	 * Get the ODK Id of submission
	 * 
	 * @return ODK Id
	 */
	public String getOdkId() {
		return odkId;
	}

	public FormDefinition getFormDefinition() {
		return formDefinition;
	}
}
