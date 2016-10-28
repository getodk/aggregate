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

package org.opendatakit.common.persistence;

/**
 * Constant strings used to identify properties in an entity
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PersistConsts {

  public static final int AUDIT_COLUMN_COUNT = 4;
  
  public static final String URI_COLUMN_NAME = "_URI";
  public static final String LAST_UPDATE_DATE_COLUMN_NAME = "_LAST_UPDATE_DATE";
  public static final String LAST_UPDATE_URI_USER_COLUMN_NAME = "_LAST_UPDATE_URI_USER";
  public static final String CREATION_DATE_COLUMN_NAME = "_CREATION_DATE";
  public static final String CREATOR_URI_USER_COLUMN_NAME = "_CREATOR_URI_USER";

  /**
   * This is the delay needed in streaming and briefcase applications to ensure
   * that we get all data forwarded to the remote server. It is the maximum
   * drift of the various webserver clocks relative to each other and the
   * database server plus the time it takes the datastore to reach global
   * consistency. We cannot stream records younger than this many seconds and
   * ensure that all data will be discovered and reported.
   */
  public static final long MAX_SETTLE_MILLISECONDS = 3000L; // 3 seconds...

  /**
   * This is the delay used when launching a background task to execute a
   * MiscTasks or PersistentResults action. The background task will fail if the
   * GAE infrastructure hasn't settled after propagating the data record with
   * the details of the request. This is most significant on the development
   * server.
   */
  public static final long MIN_SETTLE_MILLISECONDS = 1000L;

  /**
   * The shortest interval for which all datetime values are preserved across
   * all platforms. MySql's TIMESTAMP can keep time to microseconds but clock
   * time is only in multiples of a minimal millisecond.
   */
  public static final Long MIN_DATETIME_RESOLUTION = 10L;

  /**
   * The maximum length of the URI strings. An exception will be thrown if the
   * program attempts to store anything longer than this in the URI column. The
   * underlying storage layer must support this string length natively.
   */
  public static final Long URI_STRING_LEN = 80L;

  /**
   * If you need to search a string this is the guaranteed length of a
   * searchable string. A longer string may be searchable depending on the
   * datastore but this value is set to be the worst searchable length.
   * (Currently it's Google App Engine)
   */
  public static final Long GUARANTEED_SEARCHABLE_LEN = 249L;

  /**
   * Default string length for fields that do not specify an explicit string
   * length. Note that GAE has the stronger restriction on this length. See GAE_
   */
  public static final Long DEFAULT_MAX_STRING_LENGTH = 255L;

}
