/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.server;

import java.util.LinkedList;
import java.util.Queue;

import org.opendatakit.aggregate.client.form.KmlGeoTraceNShapeOption;
import org.opendatakit.aggregate.client.form.KmlGeopointOption;
import org.opendatakit.aggregate.client.form.KmlOptionsSummary;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;

public class GenerateKmlOptions {

  private IForm form;

  public GenerateKmlOptions(IForm form) {
    this.form = form;
  }

  public KmlOptionsSummary generate() {
    KmlOptionsSummary options = new KmlOptionsSummary();

    // first find any geopoints and create a corresponding KmlGeopointOptions
    Queue<FormElementModel> elementLevelsToExamine = new LinkedList<FormElementModel>();
    elementLevelsToExamine.add(form.getTopLevelGroupElement());
    while (!elementLevelsToExamine.isEmpty()) {
      FormElementModel root = elementLevelsToExamine.remove();
      for (FormElementModel element : root.getChildren()) {
        if (element.getElementType() == FormElementModel.ElementType.GEOPOINT) {
          // create a kml geopoint option
          FormElementKey gpsElementKey = element.constructFormElementKey(form);
          String gpsElementName = gpsElementKey.userFriendlyString(form);
          KmlGeopointOption kmlGpsOption = new KmlGeopointOption(gpsElementName,
              gpsElementKey.toString());
          generateGeopointOptions(kmlGpsOption, element, root);
          options.addGeopointOptions(kmlGpsOption);

        } else if (element.getElementType() == FormElementModel.ElementType.GEOTRACE
            || element.getElementType() == FormElementModel.ElementType.GEOSHAPE) {

          // create a kml geotrace or geoshape option
          FormElementKey gpsElementKey = element.constructFormElementKey(form);
          String gpsElementName = gpsElementKey.userFriendlyString(form);
          KmlGeoTraceNShapeOption geoTraceNShapeOption;
          if (element.getElementType() == FormElementModel.ElementType.GEOTRACE) {
            geoTraceNShapeOption = new KmlGeoTraceNShapeOption(
                KmlGeoTraceNShapeOption.GeoTraceNShapeType.GEOTRACE, gpsElementName,
                gpsElementKey.toString());
          } else {
            geoTraceNShapeOption = new KmlGeoTraceNShapeOption(
                KmlGeoTraceNShapeOption.GeoTraceNShapeType.GEOSHAPE, gpsElementName,
                gpsElementKey.toString());
          }
          generateGeoTraceNShapeOptions(geoTraceNShapeOption, element, root);
          options.addGeoTraceNShapeOptions(geoTraceNShapeOption);

        } else if (element.getElementType() == FormElementModel.ElementType.REPEAT) {
          elementLevelsToExamine.add(element);
        }
      }
    }

    return options;
  }

  private void generateGeopointOptions(KmlGeopointOption kmlGpsOption, FormElementModel gpsElement,
      FormElementModel root) {
    for (FormElementModel node : root.getChildren()) {
      FormElementKey key = node.constructFormElementKey(form);
      String nodeName = key.userFriendlyString(form);
      switch (node.getElementType()) {
      case BINARY:
        kmlGpsOption.addBinaryNode(nodeName, key.toString());
        break;
      case GEOPOINT:
      case GEOSHAPE:
      case GEOTRACE:
      case REPEAT:
      case GROUP:
        break; // should not be in any list
      default:
        if (!node.isMetadata()) {
          kmlGpsOption.addTitleNode(nodeName, key.toString());
        }
      }
    }
  }

  private void generateGeoTraceNShapeOptions(KmlGeoTraceNShapeOption kmlGpsOption,
      FormElementModel gpsElement, FormElementModel root) {
    for (FormElementModel node : root.getChildren()) {
      FormElementKey key = node.constructFormElementKey(form);
      String nodeName = key.userFriendlyString(form);
      switch (node.getElementType()) {
      case BINARY:
      case GEOPOINT:
      case GEOSHAPE:
      case GEOTRACE:
      case REPEAT:
      case GROUP:
        break; // should not be in any list
      default:
        if (!node.isMetadata()) {
          kmlGpsOption.addNameNode(nodeName, key.toString());
        }
      }
    }
  }

}
