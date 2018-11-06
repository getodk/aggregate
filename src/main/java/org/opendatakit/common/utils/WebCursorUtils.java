/*
  Copyright (C) 2011 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.xmlpull.v1.XmlPullParser;

/**
 * Useful methods for parsing boolean and date values and formatting dates.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class WebCursorUtils {

  private static final String XML_TAG_NAMESPACE = "http://www.opendatakit.org/cursor";

  public static final String formatCursorParameter(QueryResumePoint cursor) {
    if (cursor == null)
      return null;
    Document d = new Document();
    d.setStandalone(true);
    d.setEncoding(HtmlConsts.UTF8_ENCODE);
    Element e = d.createElement(WebCursorUtils.XML_TAG_NAMESPACE, WebUtils.CURSOR_TAG);
    e.setPrefix(null, WebCursorUtils.XML_TAG_NAMESPACE);
    d.addChild(0, Node.ELEMENT, e);
    int idx = 0;
    Element c = d.createElement(WebCursorUtils.XML_TAG_NAMESPACE, WebUtils.ATTRIBUTE_NAME_TAG);
    c.addChild(0, Node.TEXT, cursor.getAttributeName());
    e.addChild(idx++, Node.ELEMENT, c);
    c = d.createElement(WebCursorUtils.XML_TAG_NAMESPACE, WebUtils.ATTRIBUTE_VALUE_TAG);
    c.addChild(0, Node.TEXT, cursor.getValue());
    e.addChild(idx++, Node.ELEMENT, c);
    c = d.createElement(WebCursorUtils.XML_TAG_NAMESPACE, WebUtils.URI_LAST_RETURNED_VALUE_TAG);
    if (cursor.getUriLastReturnedValue() != null) {
      c.addChild(0, Node.TEXT, cursor.getUriLastReturnedValue());
    }
    e.addChild(idx++, Node.ELEMENT, c);
    c = d.createElement(WebCursorUtils.XML_TAG_NAMESPACE, WebUtils.IS_FORWARD_CURSOR_VALUE_TAG);
    c.addChild(0, Node.TEXT, Boolean.toString(cursor.isForwardCursor()));
    e.addChild(idx++, Node.ELEMENT, c);

    ByteArrayOutputStream ba = new ByteArrayOutputStream();

    KXmlSerializer serializer = new KXmlSerializer();
    try {
      serializer.setOutput(ba, HtmlConsts.UTF8_ENCODE);
      // setting the response content type emits the xml header.
      // just write the body here...
      d.writeChildren(serializer);
      serializer.flush();
    } catch (IOException e1) {
      e1.printStackTrace();
      throw new IllegalStateException("unexpected failure");
    }

    try {
      return ba.toString(HtmlConsts.UTF8_ENCODE);
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
      throw new IllegalStateException("unexpected failure");
    }
  }

  public static final QueryResumePoint parseCursorParameter(String websafeCursorString) {
    if (websafeCursorString == null || websafeCursorString.length() == 0) {
      return null;
    }
    // parse the document
    ByteArrayInputStream is;
    try {
      is = new ByteArrayInputStream(websafeCursorString.getBytes(HtmlConsts.UTF8_ENCODE));
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
      throw new IllegalStateException("Unexpected failure");
    }
    Document doc = null;
    try {
      InputStreamReader isr = null;
      try {
        isr = new InputStreamReader(is, HtmlConsts.UTF8_ENCODE);
        doc = new Document();
        KXmlParser parser = new KXmlParser();
        parser.setInput(isr);
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        doc.parse(parser);
        isr.close();
      } finally {
        if (isr != null) {
          try {
            isr.close();
          } catch (Exception e) {
            // no-op
          }
        }
        if (is != null) {
          try {
            is.close();
          } catch (Exception e) {
            // no-op
          }
        }
      }
    } catch (Exception e) {
      WebUtils.logger.error("websafe cursor is not parseable as xml document");
      e.printStackTrace();
      throw new IllegalArgumentException("unable to parse websafeCursor");
    }

    Element manifestElement = doc.getRootElement();
    if (!manifestElement.getName().equals(WebUtils.CURSOR_TAG)) {
      WebUtils.logger.error("websafe cursor root element is not <cursor> -- was "
          + manifestElement.getName());
      throw new IllegalArgumentException("websafe cursor root element is not <cursor>");
    }

    String namespace = manifestElement.getNamespace();
    if (!WebCursorUtils.XML_TAG_NAMESPACE.equals(namespace)) {
      WebUtils.logger.error("Root element Namespace is incorrect: " + namespace);
      throw new IllegalArgumentException("websafe cursor root element namespace invalid");
    }

    String attributeName = null;
    String attributeValue = null;
    String uriLastReturnedValue = null;
    boolean isForwardCursor = true;

    int nElements = manifestElement.getChildCount();
    for (int i = 0; i < nElements; ++i) {
      if (manifestElement.getType(i) != Element.ELEMENT) {
        // e.g., whitespace (text)
        continue;
      }

      Element child = (Element) manifestElement.getElement(i);
      if (!WebCursorUtils.XML_TAG_NAMESPACE.equals(child.getNamespace())) {
        // someone else's extension?
        continue;
      }

      String name = child.getName();
      if (name.equalsIgnoreCase(WebUtils.ATTRIBUTE_NAME_TAG)) {
        attributeName = XFormParser.getXMLText(child, true);
        if (attributeName != null && attributeName.length() == 0) {
          attributeName = null;
        }
      } else if (name.equalsIgnoreCase(WebUtils.ATTRIBUTE_VALUE_TAG)) {
        attributeValue = XFormParser.getXMLText(child, true);
        if (attributeValue != null && attributeValue.length() == 0) {
          attributeValue = null;
        }
      } else if (name.equalsIgnoreCase(WebUtils.URI_LAST_RETURNED_VALUE_TAG)) {
        uriLastReturnedValue = XFormParser.getXMLText(child, true);
        if (uriLastReturnedValue != null && uriLastReturnedValue.length() == 0) {
          uriLastReturnedValue = null;
        }
      } else if (name.equalsIgnoreCase(WebUtils.IS_FORWARD_CURSOR_VALUE_TAG)) {
        String flag = XFormParser.getXMLText(child, true);
        // CAL: Change to make backward web safe cursor work
        if (flag != null && flag.length() != 0) {
          isForwardCursor = WebUtils.parseBoolean(flag);
        }
      }
    }

    if (attributeName == null || attributeValue == null) {
      throw new IllegalArgumentException("null value for websafeCursor element");
    }

    return new QueryResumePoint(attributeName, attributeValue, uriLastReturnedValue,
        isForwardCursor);
  }

}
