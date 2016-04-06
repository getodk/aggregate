/*
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.aggregate.format.structure;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModelVisitor;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.XmlAttributeFormatter;
import org.opendatakit.aggregate.format.element.XmlElementFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Xml formatter for a given submission. Should roughly recreate the submission
 * xml as sent up from collect.  Does not respect namespaces of original Xml.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class XmlFormatter implements SubmissionFormatter {

  private IForm form;
  private XmlElementFormatter elemFormatter;
  private boolean firstMeta = true;
  private int metaDepth = 0;

  private PrintWriter output;

  public XmlFormatter(PrintWriter printWriter,
      IForm form, CallingContext cc) {
    output = printWriter;
    this.form = form;
    elemFormatter = new XmlElementFormatter(this);
  }

  public void writeXml(String xml) {
    output.write(xml);
  }

  class XmlFormElementModelVisitor implements FormElementModelVisitor {

    private static final String META_TAG = "meta";

    private static final String OPENROSA_META_TAG = "orx:meta";

    Submission sub;
    List<SubmissionSet> nesting = new ArrayList<SubmissionSet>();

    XmlFormElementModelVisitor(Submission sub) {
      this.sub = sub;
    }

    @Override
    public boolean traverse(FormElementModel element, CallingContext cc) {
      if ( element.isMetadata() ) return false; // handled in enter() method.
      try {
          SubmissionSet subSet = nesting.get(nesting.size()-1);
          SubmissionValue value = subSet.getElementValue(element);
          Row row = null;
          switch ( element.getElementType() ) {
            // xform tag types
          case STRING:
          case JRDATETIME:
          case JRDATE:
          case JRTIME:
          case INTEGER:
          case DECIMAL:
          case GEOTRACE:
          case GEOSHAPE:
          case GEOPOINT:
          case BINARY:  // identifies BinaryContent table
          case BOOLEAN:
          case SELECT1: // identifies SelectChoice table
          case SELECTN: // identifies SelectChoice table
            value.formatValue(elemFormatter, row, null, cc);
            break;
          default:
            throw new IllegalStateException("unhandled data type");
          }
        } catch ( ODKDatastoreException e ) {
          e.printStackTrace();
        }
      return false;
      }

      @Override
      public boolean enter(FormElementModel element, CallingContext cc) {
        if (element.getParent() == null) {
          // we are the top-level submission...
          Row attributeRow = new Row(sub.constructSubmissionKey(null));
          XmlAttributeFormatter attributeFormatter = new XmlAttributeFormatter();
          //
          // add what could be considered the form's metadata...
          //
          attributeRow.addFormattedValue("id=\"" + StringEscapeUtils.escapeXml10(form.getFormId().replace(ParserConsts.FORWARD_SLASH_SUBSTITUTION, ParserConsts.FORWARD_SLASH))
              + "\"");
          if (form.isEncryptedForm()) {
            attributeRow.addFormattedValue("encrypted=\"yes\"");
          }
          try {
            // add the submission's metadata...
            sub.getFormattedNamespaceValuesForRow(attributeRow,
                Collections.singletonList(FormElementNamespace.METADATA), attributeFormatter, false,
                cc);
          } catch (ODKDatastoreException e) {
            e.printStackTrace();
            return false;
          }

          // emit the open tag with all the metadata as attributes
          output.append("<");
          output.append(element.getElementName());
          Iterator<String> itrAttributes = attributeRow.getFormattedValues().iterator();
          while (itrAttributes.hasNext()) {
            String value = itrAttributes.next();
            output.append(" ");
            output.append(value);
          }
          output.append(">");

          // add the top-level submission set as the top set in the nesting stack.
          nesting.add(sub);

        } else if (element.getElementType() == FormElementModel.ElementType.GROUP ) {
          if ( element.getElementName().equals(META_TAG) ) {
            if ( firstMeta && ++metaDepth != 0 ) {
              elemFormatter.setPrefix("orx:");
            }
            output.append(HtmlUtil.createBeginTag(OPENROSA_META_TAG));
          } else {
            output.append(HtmlUtil.createBeginTag(element.getElementName()));
          }
        }
        return false;
      }

      @Override
      public boolean descendIntoRepeat(FormElementModel element, int ordinal, CallingContext cc) {
        SubmissionSet subSet = nesting.get(nesting.size()-1);
        SubmissionValue value = subSet.getElementValue(element);
        RepeatSubmissionType t = (RepeatSubmissionType) value;
        if ( t.getNumberRepeats() < ordinal ) {
          return false;
        }
        SubmissionSet newSet = t.getSubmissionSets().get(ordinal-1);
        nesting.add(newSet);

        output.append(HtmlUtil.createBeginTag(element.getElementName()));
        return true;
      }

      @Override
      public void ascendFromRepeat(FormElementModel element, int ordinal, CallingContext cc) {
        nesting.remove(nesting.size()-1);
        output.append(HtmlUtil.createEndTag(element.getElementName()));
      }

      @Override
      public void leave(FormElementModel element, CallingContext cc) {
        if ( element.getElementType() == FormElementModel.ElementType.GROUP ) {
          if ( element.getElementName().equals(META_TAG) ) {
            output.append(HtmlUtil.createEndTag(OPENROSA_META_TAG));
            if ( firstMeta && --metaDepth == 0 ) {
              elemFormatter.setPrefix("");
              firstMeta = false;
            }
          } else {
            output.append(HtmlUtil.createEndTag(element.getElementName()));
          }
        }
      }
  }

  @Override
  public void beforeProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
  }

  @Override
  public void processSubmissionSegment(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {

    // format row elements
    for (Submission sub : submissions) {
      FormElementModel fem = sub.getFormElementModel();
      XmlFormElementModelVisitor visitor = new XmlFormElementModelVisitor(sub);

      fem.depthFirstTraversal(visitor, cc);
    }
  }

  @Override
  public void afterProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }
}
