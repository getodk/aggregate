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

package org.opendatakit.aggregate.constants;

/**
 * Constants used in ODK aggregate to report errors
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class ErrorConsts {

  /**
   * Error message if the form with FORM ID is not found
   */
  public static final String ODKID_NOT_FOUND =
      "Unable to find form with matching Form Id as submission";
  public static final String FORM_NOT_FOUND =
      "Form not found";
  public static final String FORM_DEFINITION_INVALID =
      "Form definition incomplete or missing";
  /**
   * Error message for if key was not successfully part of the request
   */
  public static final String ODK_KEY_PROBLEM = "Encountered a problem receiving key";
  /**
   * Error message if the FORM ID in the form already exists
   */
  public static final String FORM_WITH_ODKID_EXISTS = "Form Already Exists for this Namespace/Id attribute";

  /**
   * Error message if responseURL returns null
   */
  public static final String ENKETOAPI_RETURN_NULL_RESPONSE = "Problem accessing Enketo. Please verify the Enketo Webform Integration settings on the Preferences tab and try again.";

  public static final String FORM_INVALID_SUBMISSION_ELEMENT = "Attributes of submission element do not match form attributes";
  /**
   * Error message if not all information was received
   */
  public static final String MISSING_FORM_INFO = "Did not receive Form Name and Form XML description";

  /**
   * Error message if form ID was not specified
   */
  public static final String MISSING_FORM_ID = "Form did not specify a Form ID. For information on Form ID please check the Open Data Kit FAQ";

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
  public static final String FORM_DOES_NOT_ALLOW_SUBMISSIONS = "Submissions have been disallowed on this form";
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
  public static final String INVALID_PARAMS = "Parameter(s) are not valid";
  public static final String MISSING_PARAMS = "One or more required parameters are missing";

  /**
   * Constant string identifying XML stream
   */
  public static final String INPUTSTREAM_ERROR = "Problem obtaining submissionXML input stream!";
  public static final String NO_IMAGE_EXISTS = "No Image Exists for this Entry!";
  public static final String NOT_A_KEY = "Incorrect type was stored, expecting a key for the view link";
  public static final String TASK_PROBLEM = "PROBLEM WITH TASK: ";

  public static final String QUOTA_EXCEEDED = "Quota exceeded";
  public static final String PERSISTENCE_LAYER_PROBLEM = "Problem persisting data or accessing data";
  public static final String UPLOAD_PROBLEM = "Upload transmission unexpectedly failed";
  public static final String EXPORTED_FILE_PROBLEM = "Problem accessing exported datafile";

  public static final String JAVA_ROSA_PARSING_PROBLEM = "Problem with JavaRosa Parsing Form:";
  public static final String ERROR_OBTAINING_FUSION_TABLE_ID = "ERROR CREATING FUSION TABLE - DID NOT GET A TABLE NUMBER";
}
