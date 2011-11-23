/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format.table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.LinkElementFormatter;
import org.opendatakit.aggregate.servlet.FragmentedCsvServlet;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FragmentedCsvFormatter extends TableFormatterBase implements SubmissionFormatter {
  private static final String PARENT_KEY_PROPERTY = "PARENT_KEY";
  private static final String SELF_KEY_PROPERTY = "KEY";
  private static final String XML_TAG_NAMESPACE = "";
  private static final String XML_TAG_RESULT = "result";
  private static final String XML_TAG_HEADER = "header";
  private static final String XML_TAG_CURSOR = "cursor";
  private static final String XML_TAG_ENTRIES = "entries";

  private final List<Row> formattedElements = new ArrayList<Row>();
  private final String websafeCursorString;
  private List<String> headers = null;
  private boolean includeParentKey = false;

  public FragmentedCsvFormatter(IForm xform, List<SubmissionKeyPart> submissionParts,
      String webServerUrl, String websafeCursorString, PrintWriter printWriter) {
    super(xform, printWriter, null);
    this.websafeCursorString = websafeCursorString;
    elemFormatter = new LinkElementFormatter(webServerUrl, FragmentedCsvServlet.ADDR, true, true,
        true, false);
  }

  /**
   * Create the comma separated row with proper doubling of embedded quotes.
   * 
   * @param itr
   *          string values to be separated by commas
   * @return string containing comma separated values
   */
  private String generateCommaSeperatedElements(List<String> elements) {
    StringBuilder row = new StringBuilder();
    boolean first = true;
    for (String original : elements) {
      // replace all quotes in the string with doubled-quotes
      // then wrap the whole thing with quotes. Nulls are
      // distinguished from empty strings by the lack of a
      // value in that position (e.g., ,, vs ,"",)
      if (!first) {
        row.append(FormatConsts.CSV_DELIMITER);
      }
      first = false;
      if (original != null) {
        row.append(BasicConsts.QUOTE);
        row.append(original.replace(BasicConsts.QUOTE, BasicConsts.QUOTE_QUOTE));
        row.append(BasicConsts.QUOTE);
      }
    }
    return row.toString();
  }

  private void emitXmlWrappedCsv(List<Row> resultTable, List<String> headers) throws IOException {

    Document d = new Document();
    d.setStandalone(true);
    d.setEncoding(HtmlConsts.UTF8_ENCODE);
    Element e = d.createElement(XML_TAG_NAMESPACE, XML_TAG_ENTRIES);
    d.addChild(0, Node.ELEMENT, e);
    int idx = 0;
    e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
    if (websafeCursorString != null) {
      Element cursor = d.createElement(XML_TAG_NAMESPACE, XML_TAG_CURSOR);
      e.addChild(idx++, Node.ELEMENT, cursor);
      cursor.addChild(0, Node.TEXT, websafeCursorString);
      e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
    }
    Element header = d.createElement(XML_TAG_NAMESPACE, XML_TAG_HEADER);
    e.addChild(idx++, Node.ELEMENT, header);
    header.addChild(0, Node.TEXT, generateCommaSeperatedElements(headers));
    e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);

    Element resultRow;
    // generate rows
    for (Row row : resultTable) {
      resultRow = d.createElement(XML_TAG_NAMESPACE, XML_TAG_RESULT);
      e.addChild(idx++, Node.ELEMENT, resultRow);
      String csvRow = generateCommaSeperatedElements(row.getFormattedValues());
      resultRow.addChild(0, Node.TEXT, csvRow);
      e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
    }

    KXmlSerializer serializer = new KXmlSerializer();
    serializer.setOutput(output);
    // setting the response content type emits the xml header.
    // just write the body here...
    d.writeChildren(serializer);
    serializer.flush();
  }

  @Override
  protected void beforeProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc)
      throws ODKDatastoreException {
    formattedElements.clear();
    
    headers = headerFormatter.generateHeaders(form, rootGroup, propertyNames);
    includeParentKey = false;
    {
      FormElementModel m = rootGroup;
      while (m != null && m.getElementType() != FormElementModel.ElementType.REPEAT) {
        m = m.getParent();
      }
      includeParentKey = (m != null && m.getElementType() == FormElementModel.ElementType.REPEAT);
    }

    if (includeParentKey) {
      headers.add(PARENT_KEY_PROPERTY);
    }
    headers.add(SELF_KEY_PROPERTY);
  }

  @Override
  protected void processSubmissionSetSegment(Collection<? extends SubmissionSet> submissions,
      FormElementModel rootGroup, CallingContext cc) throws ODKDatastoreException {

    // format row elements
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);

      if (includeParentKey) {
        ((LinkElementFormatter) elemFormatter).addFormattedLink(sub.getEnclosingSet()
            .constructSubmissionKey(null), FragmentedCsvServlet.ADDR, ServletConsts.FORM_ID, row); // PARENT_KEY
      }
      ((LinkElementFormatter) elemFormatter).addFormattedLink(sub.constructSubmissionKey(null),
          FragmentedCsvServlet.ADDR, ServletConsts.FORM_ID, row); // KEY
      formattedElements.add(row);
    }
  }

  @Override
  protected void afterProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc)
      throws ODKDatastoreException {
    try {
      emitXmlWrappedCsv(formattedElements, headers);
    } catch (IOException e) {
      e.printStackTrace();
    }
    headers = null;
    formattedElements.clear();
  }
}
