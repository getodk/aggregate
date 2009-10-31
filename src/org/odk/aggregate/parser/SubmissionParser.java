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

package org.odk.aggregate.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ParserConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKParseException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.type.RepeatSubmissionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

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


  private Form form;
  
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

  EntityManager em;
  
  /**
   * Construct an ODK submission by processing XML submission to extract values
   * 
   * @param inputStreamXML xml submission input stream
   * @throws IOException
   * @throws ODKFormNotFoundException thrown if a form is not found with a
   *         matching ODK ID
   * @throws ODKParseException 
   */
  public SubmissionParser(InputStream inputStreamXML, EntityManager entityManager) throws IOException, 
    ODKFormNotFoundException, ODKParseException {
    em = entityManager;
    constructorHelper(inputStreamXML);
  }

  /**
   * Construct an ODK submission by processing XML submission to extract values
   * 
   * @param submissionFormParser multipart data submission that includes XML
   *        submission & possibly other data
   * @throws IOException
   * @throws ODKFormNotFoundException thrown if a form is not found with a
   *         matching ODK ID
   * @throws ODKParseException 
   */
  public SubmissionParser(MultiPartFormData submissionFormParser, EntityManager entityManager) throws IOException,
      ODKFormNotFoundException, ODKParseException {
    em = entityManager;
    if (submissionFormParser == null) {
      // TODO: review best error handling strategy
      throw new IOException("DID NOT GET A MULTIPARTFORMPARSER");
    }
    submissionFormItems = submissionFormParser;
    MultiPartFormItem submission =
        submissionFormItems.getFormDataByFieldName(ServletConsts.XML_SUBMISSION_FILE);
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
   * @param inputStreamXML xml submission input stream
   * 
   * @throws IOException
   * @throws ODKFormNotFoundException thrown if a form is not found with a
   *         matching ODK ID
   * @throws ODKParseException 
   */
  private void constructorHelper(InputStream inputStreamXML) throws IOException,
      ODKFormNotFoundException, ODKParseException {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = builder.parse(inputStreamXML);
      root = doc.getDocumentElement();
      printNode(root);

      // check for odk id
      odkId = root.getAttribute(ParserConsts.ODK_ATTRIBUTE_NAME);

      // if odk id is not present use namespace
      if (odkId.equalsIgnoreCase(BasicConsts.EMPTY_STRING)) {
        odkId = root.getAttribute(ParserConsts.NAMESPACE_ATTRIBUTE);
      }

      // if nothing present should throw an error
      // TODO: remove hack
      if (odkId.equalsIgnoreCase(BasicConsts.EMPTY_STRING)) {
        odkId = ParserConsts.DEFAULT_NAMESPACE;
      }

    } catch (ParserConfigurationException e) {
      throw new IOException(e.getCause());
    } catch (SAXException e) {
      e.printStackTrace();
      throw new IOException(e.getCause());
    }
    form = Form.retrieveForm(em, odkId);    

    submission = new Submission(form);
    
    FormElement formRoot = form.getElementTreeRoot();
    processSubmissionElement(formRoot, root, submission);

    // save the elements inserted into the submission
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(submission.getEntity());
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
   * @param node form element to parse from the XML submission
   * @param submissionSet the set of submission to add the submission value to
   * 
   * @throws ODKParseException 
   */
  private void processSubmissionElement(FormElement node, Element currentSubmissionElement,
      SubmissionSet submissionSet) throws ODKParseException {
    if (node == null || currentSubmissionElement == null) {
      return;
    }

    String submissionTag = node.getElementName();
    if (submissionTag == null) {
      return;
    }

    SubmissionField<?> submissionElement = node.createSubmissionField();

    if (submissionElement != null) {
      List<Element> elements = getElementsInTree(currentSubmissionElement, submissionTag);
      if (elements.isEmpty()) {
        // TODO: remove hack to get around root problem
        parseSubmissionElement(node, submissionElement, currentSubmissionElement, submissionSet);
      } else if (elements.size() == 1) {
        parseSubmissionElement(node, submissionElement, elements.remove(0), submissionSet);
      } else {
        // verify that this node is able to be repeatable
        if(!node.isRepeatable()) {
          throw new ODKParseException();
        }
        
        RepeatSubmissionType repeats = new RepeatSubmissionType(odkId, submissionTag);
        submissionSet.addSubmissionValues(repeats);
        for (Element element : elements) {
          SubmissionSet repeatableSubmissionSet = new SubmissionSet(odkId, submissionTag, submissionSet.getKey(), repeats.getNumberRepeats());
          repeats.addSubmissionSet(repeatableSubmissionSet);
          parseSubmissionElement(node, submissionElement, element, repeatableSubmissionSet);
        }
      }
    }
  }


  private void parseSubmissionElement(FormElement node, SubmissionField<?> submissionElement,
      Element elementNode, SubmissionSet submissionSet) throws ODKParseException {
    try {
      String value = getSubmissionValue(elementNode);
      if (value != null) {
        if (submissionElement.isBinary()) {
          // check to see if we received a multipart submission
          if (submissionFormItems == null) {
            // TODO: problem, only accept a base64 encoded in a direct XML post
            byte[] receivedBytes = Base64.decodeBase64(value.getBytes());
            // TODO: problem since we don't know how to tell what type of binary 
            // without content type, defaulting to JPG
            submissionElement.setValueFromByteArray(receivedBytes, null, ServletConsts.RESP_TYPE_IMAGE_JPEG);
          } else {
            // attempt to find binary data in multi-part form submission
            // first searching by file name, then field name
            MultiPartFormItem binaryData = submissionFormItems.getFormDataByFileName(value);
            if (binaryData == null) {
              binaryData = submissionFormItems.getFormDataByFieldName(value);
            }
            // after completing the search now check if found anything and
            // value, otherwise output error
            if (binaryData != null) {
              submissionElement.setValueFromByteArray(binaryData.getStream().toByteArray(), null, binaryData.getContentType());
            } else {
              // TODO: decide if we want system to reject submission if file is
              // not found?
              System.err.println("UNABLE TO FIND VALUE OF " + value);
            }
          }
        } else {
          submissionElement.setValueFromString(value);
        }
      }
    } catch (ODKConversionException e) {
      e.printStackTrace();
    }

    if (submissionElement != null) {
      submissionSet.addSubmissionValues(submissionElement);
    }

    List<FormElement> children = node.getChildren();

    // iterate through all children
    for (FormElement child : children) {
      processSubmissionElement(child, elementNode, submissionSet);
    }
  }

  private List<Element> getElementsInTree(Element rootNode, String submissionTag) {
    List<Element> elements = new ArrayList<Element>();

    // find the elements
    NodeList nodeList = rootNode.getElementsByTagName(submissionTag);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(submissionTag)) {
        elements.add((Element) node);
      }
    }
    return elements;
  }

  /**
   * Extracts value from the XML submission element by getting value from the
   * text node
   * 
   * @param element element that has text child node that will contain the value
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
   * @param node xml node to be recursively printed
   */
  private void printNode(Element node) {
    System.out.println(ParserConsts.NODE_FORMATTED + node.getTagName());
    if (node.hasAttributes()) {
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
        Node attr = attributes.item(i);
        System.out.println(ParserConsts.ATTRIBUTE_FORMATTED + attr.getNodeName()
            + BasicConsts.EQUALS + attr.getNodeValue());
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
            System.out.println(ParserConsts.VALUE_FORMATTED + value);
          }
        }
      }
    }

  }
  /**
   * Get the ODK Id of submission
   * @return ODK Id
   */
  public String getOdkId() {
    return odkId;
  }
  
  public Form getForm() {
    return form;
  }
}
