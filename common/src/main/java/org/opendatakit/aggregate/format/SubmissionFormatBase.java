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

package org.opendatakit.aggregate.format;

import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.constants.BasicConsts;


/**
 * Base class used to process submission results into usable form
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class SubmissionFormatBase {
 
  protected IForm form;
  
  /**
   * url of aggregate instance, the base that appends to the servlet address
   */
  protected String baseWebServerUrl;

  /**
   * true if gps coordinates should be separated, false otherwise
   */
  protected boolean separateCoordinates;

  /**
   * Constructs a submission result base
   * @param xform 
   * 	 the form being formatted
   * @param webServerURL 
   *     url of aggregate instance, the base that appends to the servlet address
   * @param separateGeopoint 
   *     true if gps coordinates should be separated, false otherwise
   */
  protected SubmissionFormatBase(IForm xform, String webServerURL, boolean separateGeopoint) {
    form = xform;
    baseWebServerUrl = webServerURL + BasicConsts.FORWARDSLASH;
    separateCoordinates = separateGeopoint;
  }
  
  /**
   * Constructs a submission result base
   * @param xform 
   * 	 the form being formatted
   * @param webServerURL 
   *     url of aggregate instance, the base that appends to the servlet address
   */
  protected SubmissionFormatBase(IForm xform, String webServerURL) {
    this(xform, webServerURL, true);
  }
  
  /**
   * Constructs a submission result base
   * @param webServerURL 
   *    url of aggregate instance, the base that appends to the servlet address
   * @param form the form
   * @param datastore datastore to access
   * @param user the user initiating the request
   * @throws ODKFormNotFoundException 
   */
  protected SubmissionFormatBase(String webServerURL, IForm form, Datastore datastore, User user) throws ODKFormNotFoundException {
    this(form, webServerURL, true);
  }
  
  protected String getBaseServerUrl() {
    return baseWebServerUrl;
  }



}
