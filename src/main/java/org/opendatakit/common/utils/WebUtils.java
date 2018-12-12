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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.javarosa.core.model.utils.DateUtils;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Useful methods for parsing boolean and date values and formatting dates.
 *
 * @author mitchellsundt@gmail.com
 */
public class WebUtils {

  static final String IS_FORWARD_CURSOR_VALUE_TAG = "isForwardCursor";
  static final String URI_LAST_RETURNED_VALUE_TAG = "uriLastReturnedValue";
  static final String ATTRIBUTE_VALUE_TAG = "attributeValue";
  static final String ATTRIBUTE_NAME_TAG = "attributeName";
  static final String CURSOR_TAG = "cursor";
  static final Logger logger = LoggerFactory.getLogger(WebUtils.class);
  private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
  private static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
  private static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
  private static final String PATTERN_DATE_TOSTRING = "EEE MMM dd HH:mm:ss zzz yyyy";
  private static final String PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final String PATTERN_ISO8601_WITHOUT_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  private static final String PATTERN_ISO8601_DATE = "yyyy-MM-ddZ";
  private static final String PATTERN_ISO8601_TIME = "HH:mm:ss.SSSZ";
  private static final String PATTERN_YYYY_MM_DD_DATE_ONLY_NO_TIME_DASH = "yyyy-MM-dd";
  private static final String PATTERN_NO_DATE_TIME_ONLY = "HH:mm:ss.SSS";
  private static final String PATTERN_GOOGLE_DOCS = "MM/dd/yyyy HH:mm:ss.SSS";
  private static final String PATTERN_GOOGLE_DOCS_DATE_ONLY = "MM/dd/yyyy";

  private static final String PURGE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private WebUtils() {
  }

  public static final Boolean parseBoolean(String value) {
    Boolean b = null;
    if (value != null && value.length() != 0) {
      b = Boolean.FALSE;
      if (value.compareToIgnoreCase("ok") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("yes") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("true") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("T") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("Y") == 0) {
        b = Boolean.TRUE;
      }
    }
    return b;
  }

  private static final Date parseDateSubset(String value, String[] parsePatterns, Locale l, TimeZone tz) {
    // borrowed from apache.commons.lang.DateUtils...
    Date d = null;
    SimpleDateFormat parser = null;
    ParsePosition pos = new ParsePosition(0);
    for (int i = 0; i < parsePatterns.length; i++) {
      if (i == 0) {
        if (l == null) {
          parser = new SimpleDateFormat(parsePatterns[0]);
        } else {
          parser = new SimpleDateFormat(parsePatterns[0], l);
        }
      } else {
        parser.applyPattern(parsePatterns[i]);
      }
      parser.setTimeZone(tz); // enforce UTC for formats without timezones
      pos.setIndex(0);
      d = parser.parse(value, pos);
      if (d != null && pos.getIndex() == value.length()) {
        return d;
      }
    }
    return d;
  }

  public static final Date parseDate(String value) {
    if (value == null || value.length() == 0)
      return null;

    String[] iso8601Pattern = new String[]{PATTERN_ISO8601};

    String[] localizedParsePatterns = new String[]{
        // try the common HTTP date formats that have time zones
        PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_DATE_TOSTRING};

    String[] localizedNoTzParsePatterns = new String[]{
        // ones without timezones... (will assume UTC)
        PATTERN_ASCTIME};

    String[] tzParsePatterns = new String[]{PATTERN_ISO8601, PATTERN_ISO8601_DATE,
        PATTERN_ISO8601_TIME};

    String[] noTzParsePatterns = new String[]{
        // ones without timezones... (will assume UTC)
        PATTERN_ISO8601_WITHOUT_ZONE, PATTERN_NO_DATE_TIME_ONLY,
        PATTERN_YYYY_MM_DD_DATE_ONLY_NO_TIME_DASH, PATTERN_GOOGLE_DOCS};

    Date d = null;
    // iso8601 parsing is sometimes off-by-one when JR does it...
    d = parseDateSubset(value, iso8601Pattern, null, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    // try to parse with the JavaRosa parsers
    d = DateUtils.parseDateTime(value);
    if (d != null)
      return d;
    d = DateUtils.parseDate(value);
    if (d != null)
      return d;
    d = DateUtils.parseTime(value);
    if (d != null)
      return d;
    // try localized and english text parsers (for Web headers and interactive
    // filter spec.)
    d = parseDateSubset(value, localizedParsePatterns, Locale.ENGLISH, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    d = parseDateSubset(value, localizedParsePatterns, null, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    d = parseDateSubset(value, localizedNoTzParsePatterns, Locale.ENGLISH,
        TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    d = parseDateSubset(value, localizedNoTzParsePatterns, null, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    // try other common patterns that might not quite match JavaRosa parsers
    d = parseDateSubset(value, tzParsePatterns, null, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    d = parseDateSubset(value, noTzParsePatterns, null, TimeZone.getTimeZone("GMT"));
    if (d != null)
      return d;
    // try the locale- and timezone- specific parsers
    {
      DateFormat formatter = DateFormat.getDateTimeInstance();
      ParsePosition pos = new ParsePosition(0);
      d = formatter.parse(value, pos);
      if (d != null && pos.getIndex() == value.length()) {
        return d;
      }
    }
    {
      DateFormat formatter = DateFormat.getDateInstance();
      ParsePosition pos = new ParsePosition(0);
      d = formatter.parse(value, pos);
      if (d != null && pos.getIndex() == value.length()) {
        return d;
      }
    }
    {
      DateFormat formatter = DateFormat.getTimeInstance();
      ParsePosition pos = new ParsePosition(0);
      d = formatter.parse(value, pos);
      if (d != null && pos.getIndex() == value.length()) {
        return d;
      }
    }
    throw new IllegalArgumentException("Unable to parse the date: " + value);
  }

  public static final String asSubmissionDateTimeString(Date d) {
    if (d == null)
      return null;
    return DateUtils.formatDateTime(d, DateUtils.FORMAT_ISO8601);
  }

  public static final String asSubmissionDateOnlyString(Date d) {
    if (d == null)
      return null;
    return DateUtils.formatDate(d, DateUtils.FORMAT_ISO8601);
  }

  public static final String asSubmissionTimeOnlyString(Date d) {
    if (d == null)
      return null;
    return DateUtils.formatTime(d, DateUtils.FORMAT_ISO8601);
  }

  public static final String googleDocsDateTime(Date d) {
    if (d == null)
      return null;
    SimpleDateFormat asGoogleDoc = new SimpleDateFormat(PATTERN_GOOGLE_DOCS);
    asGoogleDoc.setTimeZone(TimeZone.getTimeZone("GMT"));
    return asGoogleDoc.format(d);
  }

  public static final String googleDocsDateOnly(Date d) {
    if (d == null)
      return null;
    SimpleDateFormat asGoogleDocDateOnly = new SimpleDateFormat(PATTERN_GOOGLE_DOCS_DATE_ONLY);
    asGoogleDocDateOnly.setTimeZone(TimeZone.getTimeZone("GMT"));
    return asGoogleDocDateOnly.format(d);
  }

  public static final String iso8601Date(Date d) {
    if (d == null)
      return null;
    // SDF is not thread-safe
    SimpleDateFormat asGMTiso8601 = new SimpleDateFormat(PATTERN_ISO8601); // with
    // time
    // zone
    asGMTiso8601.setTimeZone(TimeZone.getTimeZone("GMT"));
    return asGMTiso8601.format(d);
  }

  public static final String rfc1123Date(Date d) {
    if (d == null)
      return null;
    // SDF is not thread-safe
    SimpleDateFormat asGMTrfc1123 = new SimpleDateFormat(PATTERN_RFC1123); // with
    // time
    // zone
    asGMTrfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
    return asGMTrfc1123.format(d);
  }

  public static final String purgeDateString(Date d) {
    if (d == null)
      return null;
    // SDF is not thread-safe
    SimpleDateFormat purgeDateFormat = new SimpleDateFormat(PURGE_DATE_FORMAT);
    return purgeDateFormat.format(d);
  }

  public static final Date parsePurgeDateString(String str) throws ParseException {
    if (str == null) {
      return null;
    }
    // SDF is not thread-safe
    SimpleDateFormat purgeDateFormat = new SimpleDateFormat(PURGE_DATE_FORMAT);
    return purgeDateFormat.parse(str);
  }

  public static String readResponse(HttpResponse resp) throws IOException {

    HttpEntity e = resp.getEntity();
    if (e != null) {
      return WebUtils.readResponseHelper(e.getContent());
    }

    return BasicConsts.EMPTY_STRING;
  }

  public static String readGoogleResponse(com.google.api.client.http.HttpResponse resp) throws IOException {
    if (resp != null) {
      return WebUtils.readResponseHelper(resp.getContent());
    }

    return BasicConsts.EMPTY_STRING;
  }

  private static String readResponseHelper(InputStream content) {
    StringBuffer response = new StringBuffer();

    if (content != null) {
      // TODO: this section of code is possibly causing 'WARNING: Going to
      // buffer
      // response body of large or unknown size. Using getResponseBodyAsStream
      // instead is recommended.'
      // The WARNING is most likely only happening when running appengine
      // locally,
      // but we should investigate to make sure
      BufferedReader reader = null;
      InputStreamReader isr = null;
      try {
        reader = new BufferedReader(isr = new InputStreamReader(content, HtmlConsts.UTF8_ENCODE));
        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
          response.append(responseLine);
        }
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      } catch (IllegalStateException ex) {
        ex.printStackTrace();
      } catch (IOException ex) {
        ex.printStackTrace();
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (IOException ex) {
          // no-op
        }
        try {
          if (isr != null) {
            isr.close();
          }
        } catch (IOException ex) {
          // no-op
        }
      }
    }
    return response.toString();
  }

}
