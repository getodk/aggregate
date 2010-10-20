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
package org.opendatakit.aggregate.format.element;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.common.constants.BasicConsts;

public class BasicHeaderFormatter implements HeaderFormatter {

  /**
   * separate the GPS coordinates of latitude and longitude into columns
   */
  private boolean separateCoordinates;

  /**
   * include GPS altitude data
   */
  private boolean includeAltitude;

  /**
   * include GPS accuracy data
   */
  private boolean includeAccuracy;
  
  private List<String> headers = new ArrayList<String>();
  private List<ElementType> types = new ArrayList<ElementType>();
  
  /**
   * Construct a Header Formatter
   * 
   * @param separateGpsCoordinates
   *        separate the GPS coordinates of latitude and longitude into columns
   * @param includeGpsAltitude
   *        include GPS altitude data
   * @param includeGpsAccuracy
   *        include GPS accuracy data
   */
  public BasicHeaderFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
      boolean includeGpsAccuracy) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
  }

  /**
   * Creates the
   * 
   * @param formDefinition
   *          the xform that is being used to create the header
   * @param rootNode
   *          the node of the xform that contains several values that will be
   *          used to generate the headers. The node should correspond to a
   *          SubmissionSet, not a SubmissionValue.
   * @param propertyNames
   *          list of properties to include in headers, if null is passed will
   *          return all properties for the SubmissionSet
   */

  @Override
  public List<String> generateHeaders(FormDefinition formDefinition,
	  		FormDataModel rootNode, List<FormDataModel> propertyNames) {
	
    if(formDefinition.getTopLevelGroup().equals(rootNode)) {
      headers.add(FormatConsts.SUBMISSION_DATE_HEADER);
      types.add(ElementType.JRDATETIME);
      headers.add(FormatConsts.SUBMISSION_ID_HEADER);
      types.add(ElementType.STRING);
    }
    
    processElementForColumnHead(rootNode, rootNode, BasicConsts.EMPTY_STRING, headers, types);
    return headers;
  }
  
  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnHead(FormDataModel node, FormDataModel root, String parentName,
	      List<String> headers, List<ElementType> types) {
    if (node == null)
      return;

    String nodeName = parentName + node.getElementName();
    String revisedParentName = parentName;

    switch ( node.getElementType() ) {
    case GROUP:
      if (!node.equals(root)) {
          // else skip and goto children as we do not know how to display
          // append parent name incase embedded tag
          revisedParentName = revisedParentName + node.getElementName() + BasicConsts.COLON;
      }
      break;
    case REPEAT:
      headers.add(nodeName);
      types.add(ElementType.REPEAT);
      return;
    case PHANTOM:
   	  break;
    case GEOPOINT:
      if (separateCoordinates) {
        headers.add(nodeName + BasicConsts.COLON + FormatConsts.LATITUDE);
        types.add(ElementType.DECIMAL);
        headers.add(nodeName + BasicConsts.COLON + FormatConsts.LONGITUDE);
        types.add(ElementType.DECIMAL);
        if (includeAltitude) {
            headers.add(nodeName + BasicConsts.COLON + FormatConsts.ALTITUDE);
            types.add(ElementType.DECIMAL);
        }

        if (includeAccuracy) {
            headers.add(nodeName + BasicConsts.COLON + FormatConsts.ACCURACY);
            types.add(ElementType.DECIMAL);
        }
      } else {
        headers.add(node.getElementName());
        types.add(ElementType.GEOPOINT);
      }
      break;
    default:
      headers.add(node.getElementName());
      types.add(node.getElementType());
    }

    // only recurse into the elements that are not binary, geopoint, 
    // repeat or choice elements
    if (( node.getElementType() != ElementType.BINARY ) && 
		(node.getElementType() != ElementType.REPEAT ) &&
		(node.getElementType() != ElementType.GEOPOINT ) &&
		(node.getElementType() != ElementType.SELECT1 ) &&
		(node.getElementType() != ElementType.SELECTN )) {
    	
	    List<FormDataModel> childDataElements = node.getChildren();
	    for (FormDataModel child : childDataElements) {
	      processElementForColumnHead(child, root, revisedParentName, headers, types);
	    }
    }
  }

@Override
public List<ElementType> getHeaderTypes() {
	return types;
}
}
