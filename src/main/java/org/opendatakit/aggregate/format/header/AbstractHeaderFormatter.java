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
package org.opendatakit.aggregate.format.header;

import java.util.List;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public abstract class AbstractHeaderFormatter implements HeaderFormatter {

  protected List<FormElementModel> propertyNames = null;
  protected List<String> headers = null;
  protected List<ElementType> types = null;

  @Override
  public List<ElementType> getHeaderTypes() {
    return types;
  }

  protected abstract void processGeoPoint(FormElementModel node, String nodeName);

  /**
   * Helper function to recursively go through the element tree and create the
   * column headings
   */
  protected void processElementForColumnHead(FormElementModel node, FormElementModel root,
                                             String parentName) {
    if (node == null)
      return;

    String nodeName = parentName + node.getElementName();
    String revisedParentName = parentName;

    switch (node.getElementType()) {
      case GROUP:
        if (node != root) {
          // else skip and goto children as we do not know how to display
          // append parent name incase embedded tag
          revisedParentName = revisedParentName + node.getElementName() + BasicConsts.COLON;
        }
        break;
      case REPEAT:
        if (node == root) {
          // we are processing this as a group...
          List<FormElementModel> childDataElements = node.getChildren();
          for (FormElementModel child : childDataElements) {
            processElementForColumnHead(child, root, revisedParentName);
          }
        } else {
          // we are processing this as a table element
          if ((propertyNames == null) || propertyNames.contains(node)) {
            headers.add(nodeName);
            types.add(ElementType.REPEAT);
          }
        }
        return;
      case GEOPOINT:
        processGeoPoint(node, nodeName);
        break;
      default:
        if ((propertyNames == null) || propertyNames.contains(node)) {
          headers.add(node.getElementName());
          types.add(node.getElementType());
        }
    }

    // only recurse into the elements that are not binary, geopoint,
    // repeat or choice elements
    if ((node.getElementType() != ElementType.BINARY)
        && (node.getElementType() != ElementType.REPEAT)
        && (node.getElementType() != ElementType.GEOPOINT)
        && (node.getElementType() != ElementType.SELECT1)
        && (node.getElementType() != ElementType.SELECTN)) {

      List<FormElementModel> childDataElements = node.getChildren();
      for (FormElementModel child : childDataElements) {
        processElementForColumnHead(child, root, revisedParentName);
      }
    }
  }

}
