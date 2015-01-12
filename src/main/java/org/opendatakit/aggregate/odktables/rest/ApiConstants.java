/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables.rest;

public final class ApiConstants {

  private ApiConstants() {
  };

  public static final String OPEN_DATA_KIT_VERSION_HEADER = "X-OpenDataKit-Version";

  public static final String OPEN_DATA_KIT_VERSION = "2.0";

  public static final String DATE_HEADER = "Date";

  public static final String UTF8_ENCODE = "UTF-8";

  public static final String MEDIA_TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

  public static final String MEDIA_APPLICATION_XML_UTF8 = "application/xml;charset=UTF-8";

  public static final String ACCEPT_CONTENT_ENCODING_HEADER = "Accept-Encoding";

  public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

  public static final String GZIP_CONTENT_ENCODING = "gzip";

  public static final String DEFLATE_CONTENT_ENCODING = "deflate";
}
