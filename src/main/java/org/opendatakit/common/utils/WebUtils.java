/**
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
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
public class WebUtils {

   private static final String IS_FORWARD_CURSOR_VALUE_TAG = "isForwardCursor";
	private static final String URI_LAST_RETURNED_VALUE_TAG = "uriLastReturnedValue";
	private static final String ATTRIBUTE_VALUE_TAG = "attributeValue";
	private static final String ATTRIBUTE_NAME_TAG = "attributeName";
	private static final String CURSOR_TAG = "cursor";
	private static final Log logger = LogFactory.getLog(WebUtils.class);
	private static final String PATTERN_DATE_TOSTRING = "EEE MMM dd HH:mm:ss zzz yyyy";
	private static final String PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final String PATTERN_YYYY_MM_DD_DATE_ONLY_NO_TIME_DASH = "yyyy-MM-dd";
	private static final String PATTERN_NO_DATE_TIME_ONLY = "HH:mm:ss.SSS";
	private static final SimpleDateFormat iso8601 = new SimpleDateFormat(PATTERN_ISO8601);
	
	private WebUtils(){};
	
	/**
	 * Parse a string into a boolean value. Any of:
	 * <ul><li>ok</li><li>yes</li><li>true</li><li>t</li><li>y</li></ul>
	 * are interpretted as boolean true.  
	 * 
	 * @param value
	 * @return
	 */
	public static final Boolean parseBoolean(String value) {
		Boolean b = null;
		if ( value != null ) {
			b = Boolean.parseBoolean(value);
			if ( value.compareToIgnoreCase("ok") == 0) {
				b = Boolean.TRUE;
			} else if ( value.compareToIgnoreCase("yes") == 0) {
				b = Boolean.TRUE;
			} else if ( value.compareToIgnoreCase("true") == 0 ) {
				b = Boolean.TRUE;
			} else if ( value.compareToIgnoreCase("T") == 0 ) {
				b = Boolean.TRUE;
			} else if ( value.compareToIgnoreCase("Y") == 0 ) {
				b = Boolean.TRUE;
			}
		}
		return b;
	}
	
	/**
	 * Parse a string into a datetime value.  Tries the common
	 * Http formats, the iso8601 format (used by Javarosa), the
	 * default formatting from Date.toString(), and a time-only
	 * format.
	 * 
	 * @param value
	 * @return
	 */
	public static final Date parseDate(String value) {
		Date d = null;
		if ( value != null ) {
			try {
				// try the common HTTP date formats
				d = DateUtils.parseDate(value,
						new String[] { DateUtils.PATTERN_RFC1123,
									   DateUtils.PATTERN_RFC1036,
									   DateUtils.PATTERN_ASCTIME,
									   PATTERN_ISO8601,
									   PATTERN_DATE_TOSTRING,
									   PATTERN_NO_DATE_TIME_ONLY,
									   PATTERN_YYYY_MM_DD_DATE_ONLY_NO_TIME_DASH} );
			} catch ( DateParseException e) {
				throw new IllegalArgumentException("Unparsable date: " + value, e);
			}
		}
		return d;
	}

	/**
	 * Useful static method for constructing a UPPER_CASE persistence
	 * layer name from a camelCase name. This inserts an underscore
	 * before a leading capital letter and toUpper()s the
	 * resulting string.  The transformation maps multiple camelCase
	 * names to the same UPPER_CASE name so it is not reversible.
	 * <ul><li>thisURL => THIS_URL</li>
	 * <li>thisUrl => THIS_URL</li>
	 * <li>myFirstObject => MY_FIRST_OBJECT</li></ul>
	 * 
	 * @param name
	 * @return
	 */
	public static final String unCamelCase(String name) {
		StringBuilder b = new StringBuilder();
		boolean lastCap = true;
		for ( int i = 0 ; i < name.length() ; ++i ) {
			char ch = name.charAt(i);
			if ( Character.isUpperCase(ch) ) {
				if ( !lastCap ) {
					b.append('_');
				}
				lastCap = true;
				b.append(ch);
			} else if ( Character.isLetterOrDigit(ch) ){
				lastCap = false;
				b.append(Character.toUpperCase(ch));
			} else {
				throw new IllegalArgumentException("Argument is not a valid camelCase name: " + name);
			}
		}
		return b.toString();
	}

	/**
	 * Return the ISO8601 string representation of a date.
	 * 
	 * @param d
	 * @return
	 */
	public static final String iso8601Date(Date d) {
		if ( d == null ) return null;
		return iso8601.format(d);
	}

	/**
	 * Return a string with utf-8 characters replaced with backslash-uxxxx codes.
	 * Useful for debugging.
	 * 
	 * @param str
	 * @return printable rendition of non-ASCII utf-8 characters.
	 */
	public static final String escapeUTF8String(String str) {
		StringBuilder b = new StringBuilder();
		for ( int i = 0 ; i < str.length() ; ++i ) {
			int code = str.codePointAt(i);
			if ( code < 127 ) {
				b.append(str.charAt(i));
			} else {
				String val = Integer.toHexString(code);
				while ( val.length() < 4 ) {
					val = '0' + val;
				}
				b.append("\\u" + val);
			}
		}
		return b.toString();
	}
		
	private static final String XML_TAG_NAMESPACE = "http://www.opendatakit.org/cursor";
	
	public static final String formatCursorParameter(QueryResumePoint cursor) {
		if ( cursor == null ) return null;
		Document d = new Document();
		d.setStandalone(true);
		d.setEncoding(HtmlConsts.UTF8_ENCODE);
		Element e = d.createElement(XML_TAG_NAMESPACE, CURSOR_TAG);
		e.setPrefix(null, XML_TAG_NAMESPACE);
		d.addChild(0, Node.ELEMENT, e);
		int idx = 0;
		Element c = d.createElement(XML_TAG_NAMESPACE, ATTRIBUTE_NAME_TAG);
		c.addChild(0, Node.TEXT, cursor.getAttributeName());
		e.addChild(idx++, Node.ELEMENT, c);
		c = d.createElement(XML_TAG_NAMESPACE, ATTRIBUTE_VALUE_TAG);
		c.addChild(0, Node.TEXT, cursor.getValue());
		e.addChild(idx++, Node.ELEMENT, c);
		c = d.createElement(XML_TAG_NAMESPACE, URI_LAST_RETURNED_VALUE_TAG);
		if ( cursor.getUriLastReturnedValue() != null ) {
		  c.addChild(0, Node.TEXT, cursor.getUriLastReturnedValue());
		}
		e.addChild(idx++, Node.ELEMENT, c);
      c = d.createElement(XML_TAG_NAMESPACE, IS_FORWARD_CURSOR_VALUE_TAG);
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
		if ( websafeCursorString == null || websafeCursorString.length() == 0 ) {
			return null;
		}
		// parse the document
		ByteArrayInputStream is;
		try {
			is = new ByteArrayInputStream( websafeCursorString.getBytes(HtmlConsts.UTF8_ENCODE));
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
        	logger.error("websafe cursor is not parseable as xml document");
            e.printStackTrace();
            throw new IllegalArgumentException("unable to parse websafeCursor");
        }

        Element manifestElement = doc.getRootElement();
        if (!manifestElement.getName().equals(CURSOR_TAG)) {
        	logger.error("websafe cursor root element is not <cursor> -- was " + manifestElement.getName());
            throw new IllegalArgumentException("websafe cursor root element is not <cursor>");
        }
        
        String namespace = manifestElement.getNamespace();
        if (!XML_TAG_NAMESPACE.equals(namespace)) {
        	logger.error("Root element Namespace is incorrect: " + namespace);
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
            if (!XML_TAG_NAMESPACE.equals(child.getNamespace())) {
                // someone else's extension?
                continue;
            }
            
            String name = child.getName();
            if (name.equalsIgnoreCase(ATTRIBUTE_NAME_TAG)) {
            	attributeName = XFormParser.getXMLText(child, true);
                if (attributeName != null && attributeName.length() == 0) {
                	attributeName = null;
                }
            }
            else if (name.equalsIgnoreCase(ATTRIBUTE_VALUE_TAG)) {
            	attributeValue = XFormParser.getXMLText(child, true);
                if (attributeValue != null && attributeValue.length() == 0) {
                	attributeValue = null;
                }
            }
            else if (name.equalsIgnoreCase(URI_LAST_RETURNED_VALUE_TAG)) {
            	uriLastReturnedValue = XFormParser.getXMLText(child, true);
                if (uriLastReturnedValue != null && uriLastReturnedValue.length() == 0) {
                	uriLastReturnedValue = null;
                }
            }
            else if (name.equalsIgnoreCase(IS_FORWARD_CURSOR_VALUE_TAG)) {
              String flag = XFormParser.getXMLText(child, true);
               if (flag != null && flag.length() == 0) {
                 isForwardCursor = WebUtils.parseBoolean(flag);
               }
           }
        }

        if ( attributeName == null || attributeValue == null ) {
        	throw new IllegalArgumentException("null value for websafeCursor element");
        }
		
        return new QueryResumePoint( attributeName, attributeValue, uriLastReturnedValue, isForwardCursor );
	}

}
