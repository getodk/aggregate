/*
 * Copyright (C) 2016 University of Washington
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

package org.opendatakit.aggregate.format.structure;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.format.KmlConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 *
 */
abstract class AbstractKmlElementBase {

  private FormElementModel geoElement;
  private FormElementModel geoElementParent;
  private FormElementModel rootElement;

  AbstractKmlElementBase(FormElementModel element, FormElementModel root) {
    geoElement = element;
    geoElementParent = geoElement.getParent();
    rootElement = root;
    
    // ignore semantically meaningless nesting groups
    while (geoElementParent.getParent() != null
        && geoElementParent.getElementType().equals(ElementType.GROUP)) {
      geoElementParent = geoElementParent.getParent();
    }
  }

  boolean verifyFieldsAreInList(List<FormElementModel> elements) {
    if (elements == null) {
      return false;
    }

    // check base class elements
    if (geoElement != null && !elements.contains(geoElement)) {
      return false;
    }

    // check child class elements
    return childVerifyFieldsArePresent(elements);
  }

  abstract boolean childVerifyFieldsArePresent(List<FormElementModel> elements);

  abstract String generatePlacemarkSubmission(Submission sub, List<FormElementModel> propertyNames,
      CallingContext cc) throws ODKDatastoreException;

  boolean verifyElementSameLevel(FormElementModel element) {
    if (element == null) {
      return true;
    }

    FormElementModel elementParent = element.getParent();
    // ignore semantically meaningless nesting groups
    while (elementParent.getParent() != null
        && elementParent.getElementType().equals(ElementType.GROUP)) {
      elementParent = elementParent.getParent();
    }

    if (geoElementParent.equals(elementParent)) {
      return true;
    }

    return false;
  }

  FormElementModel getGeoElement() {
    return geoElement;
  }

  FormElementModel getGeoElementParent() {
    return geoElementParent;
  }

  String generateDataElement(String name, String value) {
    return String.format(KmlConsts.KML_DATA_ELEMENT_TEMPLATE, StringEscapeUtils.escapeXml10(name),
        StringEscapeUtils.escapeXml10(value));
  }
  
  boolean geoParentRootSubmissionElement() {
      return (geoElementParent.equals(rootElement));
  }

}
