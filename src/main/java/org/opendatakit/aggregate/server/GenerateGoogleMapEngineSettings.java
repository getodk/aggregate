/*
 * Copyright (C) 2013 University of Washington
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

import java.util.List;

import org.opendatakit.aggregate.client.externalserv.GmeSettings;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.GoogleMapsEngine;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class GenerateGoogleMapEngineSettings {

  private GmeSettings gmeSettings;
  private IForm form;
  private boolean ignoreRepeats;

  public GenerateGoogleMapEngineSettings(IForm form, boolean ignoreRepeats) {
    this.form = form;
    this.ignoreRepeats = ignoreRepeats;
    this.gmeSettings = new GmeSettings();
  }

  public GmeSettings generate(CallingContext cc) throws ODKExternalServiceException,
      ODKDatastoreException {
    try {
      String gmeAssetId = GoogleMapsEngine.parseGmeAssetId(form, cc);
      gmeSettings.setGmeAssetId(gmeAssetId);
    } catch (ODKDatastoreException e1) {
      throw e1;
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }

    // get rid of old list and recalculate
    FormElementModel root = form.getTopLevelGroupElement();
    processElementForColumnHead(form, root, root);
    return gmeSettings;
  }

  /**
   * Helper function to recursively go through the element tree and create the
   * FormElementKeys
   *
   */
  private void processElementForColumnHead(IForm form, FormElementModel node, FormElementModel root) {
    if (node == null)
      return;

    FormElementKey key = node.constructFormElementKey(form);
    String nodeName = key.userFriendlyString(form);
    switch (node.getElementType()) {
    case GEOPOINT:
      gmeSettings.addGeoPoint(nodeName, key.toString());
      break;
    case REPEAT:
      if (ignoreRepeats) {
        return;
      }
    default:
      break; // should not be in any list
    }

    List<FormElementModel> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElementModel child : childDataElements) {
      processElementForColumnHead(form, child, root);
    }
  }
}
