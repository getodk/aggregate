/*
 * Copyright (C) 2010 University of Washington
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


/**
 * Constants for the OpenRosa-compliant FormList api.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class XFormsTableConsts {

  // OpenRosa Compliant xforms list (discovery)
  public static final String XFORMS_TAG = "xforms";
  public static final String XFORM_TAG = "xform";
  public static final String FORM_ID_TAG = "formID";
  public static final String FORM_NAME_TAG = "name";
  public static final String MAJOR_MINOR_VERSION_TAG = "majorMinorVersion";
  public static final String VERSION_TAG = "version";
  public static final String DESCRIPTION_TEXT_TAG = "descriptionText";
  public static final String DESCRIPTION_URL_TAG = "descriptionUrl";
  public static final String DOWNLOAD_URL_TAG = "downloadUrl";
  public static final String MANIFEST_URL_TAG = "manifestUrl";

  // OpenRosa Compliant xforms manifest list
  public static final String MANIFEST_TAG = "manifest";
  public static final String MEDIA_FILE_TAG = "mediaFile";
  public static final String FILE_NAME_TAG = "filename";
  public static final String HASH_TAG = "hash";
}
