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
package org.opendatakit.aggregate.form;

import java.util.List;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;

/**
 * Helper class for the aggregate.opendatakit.org:FormInfo form which is used
 * to hold all the form definitions in Aggregate.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 */
public class FormInfo {
  // special values for bootstrapping
  private static final String URI_FORM_ID_VALUE_FORM_INFO = "aggregate.opendatakit.org:FormInfo";

  // Additional DataField -- the xformDefinition (binary content)
  public static final String FORM_ID = FormInfo.URI_FORM_ID_VALUE_FORM_INFO;

  // Additional DataField -- the manifest fileset (multivalued binary content)
  private static final String ELEMENT_NAME_XFORM_DEFINITION = "xformDefinition";
  private static final String ELEMENT_NAME_MANIFEST_FILESET = "manifestFileset";

  public static final boolean isFormInfoForm(String formId) {
    return URI_FORM_ID_VALUE_FORM_INFO.equals(formId);
  }

  public static SubmissionKey getSubmissionKey(String uri) {
    return new SubmissionKey(URI_FORM_ID_VALUE_FORM_INFO + "[@version=1 and @uiVersion=0]/" + FormInfoTable.TABLE_NAME + "[@key=" + uri + "]");
  }

  public static boolean validFormKey(List<SubmissionKeyPart> parts) {

    // only allow retrieval of binary data from within the FormInfo definition.
    // I.e., only files related to the definition of an XForm.
    if (!isFormInfoForm(parts.get(0).getElementName())) {
      return false;
    }

    if (parts.size() < 2) {
      return false;
    }

    SubmissionKeyPart part;

    // verify 2nd part is form info table
    part = parts.get(1);
    if (!part.getElementName().equals(FormInfoTable.TABLE_NAME)) {
      return false;
    }
    return true;
  }

  private static boolean validFormFilesetKey(List<SubmissionKeyPart> parts) {

    if (!validFormKey(parts)) {
      return false;
    }

    if (parts.size() != 4) {
      return false;
    }

    SubmissionKeyPart part;

    // verify 3rd part is fileset table, ordinal 1
    part = parts.get(2);
    if (!part.getElementName().equals(FormInfoFilesetTable.TABLE_NAME) ||
        part.getOrdinalNumber().compareTo(1L) != 0) {
      return false;
    }
    return true;
  }

  public static boolean validFormManifestKey(List<SubmissionKeyPart> parts) {

    if (!validFormFilesetKey(parts)) {
      return false;
    }

    SubmissionKeyPart part = parts.get(3);
    if (!part.getElementName().equals(ELEMENT_NAME_MANIFEST_FILESET)) {
      return false;
    }

    return true;
  }

  public static boolean validFormXformDefinitionKey(List<SubmissionKeyPart> parts) {

    if (!validFormFilesetKey(parts)) {
      return false;
    }

    SubmissionKeyPart part = parts.get(3);
    if (!part.getElementName().equals(ELEMENT_NAME_XFORM_DEFINITION) ||
        part.getOrdinalNumber().compareTo(1L) != 0) {
      return false;
    }

    return true;
  }

  public static SubmissionKey getManifestSubmissionKey(String uri, int i) {
    return new SubmissionKey(URI_FORM_ID_VALUE_FORM_INFO + "[@version=1 and @uiVersion=0]/" +
        FormInfoTable.TABLE_NAME + "[@key=" + uri + "]/" +
        FormInfoFilesetTable.TABLE_NAME + "[@ordinal=1]/" +
        ELEMENT_NAME_MANIFEST_FILESET + "[@ordinal=" + i + "]");
  }

  public static SubmissionKey getXformSubmissionKey(String uri, int i) {
    return new SubmissionKey(URI_FORM_ID_VALUE_FORM_INFO + "[@version=1 and @uiVersion=0]/" +
        FormInfoTable.TABLE_NAME + "[@key=" + uri + "]/" +
        FormInfoFilesetTable.TABLE_NAME + "[@ordinal=1]/" +
        ELEMENT_NAME_XFORM_DEFINITION + "[@ordinal=" + i + "]");
  }
}
