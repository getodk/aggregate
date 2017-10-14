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
package org.opendatakit.aggregate.datamodel;

import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Simple wrapper class analogous to SubmissionKey that can be manipulated to
 * obtain the FormElementModel to which it refers.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public class FormElementKey {

  final String formElementKey;

  public FormElementKey(String formElementKey) {
    this.formElementKey = formElementKey;
  }

  public String toString() {
    return formElementKey;
  }

  public String userFriendlyString(IForm form) {
    String tmp = formElementKey;
    String formId = form.getFormId() + BasicConsts.FORWARDSLASH;
    if (tmp.startsWith(formId)) {
      tmp = tmp.substring(formId.length());
      String topLevel = form.getTopLevelGroupElement().getElementName() + BasicConsts.FORWARDSLASH;
      if (tmp.startsWith(topLevel)) {
        tmp = tmp.substring(topLevel.length());
      }

    }

    return tmp;
  }

  @Override
  public boolean equals(Object obj) {
    if ( ! (obj instanceof FormElementKey) ) return false;
    FormElementKey other = (FormElementKey) obj;
    return (formElementKey == null) ? (other.formElementKey == null) : formElementKey.equals(other.formElementKey);
  }
}
