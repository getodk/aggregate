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

import java.math.BigDecimal;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FusionTableElementFormatter extends LinkElementFormatter {

  private static final String FT_PLACEMARK_POINT_TEMPLATE = "<Point><coordinates>%s</coordinates></Point>";
  
  public FusionTableElementFormatter(String webServerUrl) {
    super(webServerUrl, true, true, true);
  }
  
  @Override
  public void formatGeoPoint(GeoPoint gp, String propertyName, Row row) {
    if (gp != null) {
      BigDecimal latitude = new BigDecimal(0.0);
      if (gp.getLatitude() != null) {
        latitude = gp.getLatitude();
      }

      BigDecimal longitude = new BigDecimal(0.0);
      if (gp.getLongitude() != null) {
        longitude = gp.getLongitude();
      }

      BigDecimal altitude = new BigDecimal(0.0);
      if (gp.getAltitude() != null) {
        altitude = gp.getAltitude();
      }
      String point = String.format(FT_PLACEMARK_POINT_TEMPLATE, longitude + BasicConsts.COMMA + latitude + BasicConsts.COMMA + altitude);
      basicStringConversion(point, row);
      basicStringConversion(gp.getAccuracy(), row);
    } else {
      basicStringConversion(null, row);
      basicStringConversion(null, row);
    }
  }
  
  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row) throws ODKDatastoreException {
    basicStringConversion(repeat.getUniqueKeyStr(), row);
  }
  

}
