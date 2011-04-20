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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

/**
 * Useful methods for parsing boolean and date values and formatting dates.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class WebUtils {

	private static final String PATTERN_DATE_TOSTRING = "EEE MMM dd HH:mm:ss zzz yyyy";
	private static final String PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
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
									   PATTERN_NO_DATE_TIME_ONLY} );
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
}
