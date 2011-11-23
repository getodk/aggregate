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

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BasicHeaderFormatter extends AbstractHeaderFormatter implements HeaderFormatter {

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


  /**
   * Construct a Header Formatter
   * 
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   */
  public BasicHeaderFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
      boolean includeGpsAccuracy) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
  }

  /**
   * Iterates the forms and creates the headers and types based off the passed
   * in FormElementModel
   * 
   * @param formDefinition
   *          the xform that is being used to create the header
   * @param rootGroup
   *          the group of the xform that contains several values that will be
   *          used to generate the headers. The node should correspond to a
   *          SubmissionSet, not a SubmissionValue.
   * @param propertyNames
   *          list of properties to include in headers, if null is passed will
   *          return all properties for the SubmissionSet
   */
  public List<String> generateHeaders(IForm form, FormElementModel rootGroup,
      List<FormElementModel> propertyNamesArg) {
	propertyNames = propertyNamesArg;
    headers = new ArrayList<String>();
    types = new ArrayList<ElementType>();

    processElementForColumnHead(rootGroup, rootGroup, BasicConsts.EMPTY_STRING);
    return headers;
  }

  protected void processGeoPoint(FormElementModel node, String nodeName) {
	if ((propertyNames != null) && !propertyNames.contains(node)) return;
    if (separateCoordinates) {
      headers.add(nodeName + BasicConsts.COLON + GeoPoint.LATITUDE);
      types.add(ElementType.DECIMAL);
      headers.add(nodeName + BasicConsts.COLON + GeoPoint.LONGITUDE);
      types.add(ElementType.DECIMAL);
    } else {
      headers.add(node.getElementName());
      types.add(ElementType.GEOPOINT);
    }
    if (includeAltitude) {
      headers.add(nodeName + BasicConsts.COLON + GeoPoint.ALTITUDE);
      types.add(ElementType.DECIMAL);
    }

    if (includeAccuracy) {
      headers.add(nodeName + BasicConsts.COLON + GeoPoint.ACCURACY);
      types.add(ElementType.DECIMAL);
    }
  }


  

}
