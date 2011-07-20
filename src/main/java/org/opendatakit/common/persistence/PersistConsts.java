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

   /**
   * The shortest interval for which all datetime values are
   * preserved across all platforms.  MySql's TIMESTAMP
   * only keeps time to the nearest second... .
   */
  public static final Long MIN_DATETIME_RESOLUTION = 1000L;
  
  /**
   * The length of the longest simple string we declare.
   * These are used to hold things like the element name,
   * form name and form id.  The user can alter table to 
   * make this smaller if they need to.
   */
  public static final Long MAX_SIMPLE_STRING_LEN = 200L;

  /**
   * The maximum length of the URI strings.
   * An exception will be thrown if the program attempts
   * to store anything longer than this in the URI column.
   * The underlying storage layer must support this string
   * length natively.   
   */
  public static final Long URI_STRING_LEN = 80L;
  
  /**
   * If you need to search a string this is the guaranteed length of a
   * searchable string. A longer string may be searchable depending on the
   * datastore but this value is set to be the worst searchable length.
   * (Currently it's Google App Engine)
   */
  public static final Long GUARANTEED_SEARCHABLE_LEN = 249L;

}
