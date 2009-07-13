/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.constants;

/**
 * Constants used in ODK aggregate to report errors
 *  
 * @author wbrunette@gmail.com
 *
 */
public class ErrorConsts {

  /**
   * Error message if the form with ODK ID is not found
   */
  public static final String ODKID_NOT_FOUND =
      "Unable to find form to locate form with matching ODK id as submission";
  /**
   * Error message for if key was not successfully part of the request
   */
  public static final String ODK_KEY_PROBLEM = "Encountered a problem receiving key";
  /**
   * Error message if the ODK ID in the form already exists
   */
  public static final String FORM_WITH_ODKID_EXISTS = "Form Already Exists for this Namespace/Open Data Kit Identifier";
  /**
   * Error message if not all information was received 
   */
  public static final String MISSING_FORM_INFO = "Did not receive Form Name and Form XML description";
  /**
   * Error message if request is not multi-part 
   */
  public static final String NO_MULTI_PART_CONTENT = "Request does not contain Multi Part Content";
  public static final String INCOMPLETE_DATA = "Problem locating part of the submission data needed to complete request";
  /**
   * Constant error string if child does not implement a setValueFromByteArray override
   */
  public static final String BINARY_ERROR = "System should have dispatched to a proper binary conversion method";
  public static final String PARSING_PROBLEM = "Problem parsing submission XML";
  /**
   * Constant used to log error if string array does not match column size
   */
  public static final String ROW_SIZE_ERROR = "Tried to add a row to result table that did not match the header size! DISCARDING!";
  /**
   * Error message if not all information was received
   */
  public static final String INSUFFIECENT_PARAMS = "Insuffiecent Parameters Received";
  public static final String SUBMISSION_NOT_FOUND =
  "Did NOT find submission matching the provided parameters";
  public static final String NO_STRING_TO_BLOB_CONVERT = "Blob cannot be created from string";
  public static final String UNKNOWN_INTERFACE = "Some how did not get a SubmissionField or SubmissionRepeat";
  /**
   * Constant string identifying XML stream
   */
  public static final String INPUTSTREAM_ERROR = "Problem obtaining submissionXML input stream!";
  public static final String NO_IMAGE_EXISTS = "No Image Exists for this Entry!";
  public static final String NOT_A_KEY = "Incorrect type was stored, expecting a key for the view link";

}
