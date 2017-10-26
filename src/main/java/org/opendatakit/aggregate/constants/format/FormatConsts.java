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

package org.opendatakit.aggregate.constants.format;

import org.opendatakit.common.web.constants.BasicConsts;


/**
 * Constants used for formatting data
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class FormatConsts {

  // formatting constants
  public static final String CSV_DELIMITER = BasicConsts.COMMA;
  public static final String HEADER_CONCAT = BasicConsts.COLON;
  public static final String JSON_VALUE_DELIMITER = BasicConsts.COMMA;
  public static final String TO_STRING_DELIMITER = BasicConsts.COLON + BasicConsts.SPACE;
  public static final String HEADER_PARENT_UID = "*parent_uid*";
  public static final String TIME_FORMAT_STRING = "%02d:%02d:%02d";
  public static final String REDCAP_TIME_FORMAT_STRING = "%02d:%02d";
  public static final String REDCAP_DATE_ONLY_FORMAT_STRING = "%d-%02d-%02d";
  public static final String REDCAP_DATE_TIME_FORMAT_STRING = "%d-%02d-%02d %02d:%02d:%02d";

}
