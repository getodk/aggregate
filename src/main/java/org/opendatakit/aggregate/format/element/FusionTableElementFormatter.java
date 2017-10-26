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

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FusionTableElementFormatter extends LinkElementFormatter {

  private static final String FT_PLACEMARK_POINT_TEMPLATE = "<Point><coordinates>%s</coordinates></Point>";
  
  public FusionTableElementFormatter(String webServerUrl) {
    super(webServerUrl, FormMultipleValueServlet.ADDR, true, true, true, true);
  }
  
  @Override
  public void formatGeoPoint(GeoPoint gp, FormElementModel element, String ordinalValue, Row row) {
    if (gp != null) {
      // TODO: fix geopoint null bug... the geopoint should be null if it was never in a submission
      if (gp.getLatitude() == null && gp.getLongitude() == null && gp.getAltitude() == null) {
        basicStringConversion(null, row);
      } else {
    	  WrappedBigDecimal latitude = WrappedBigDecimal.fromDouble(0.0);
        if (gp.getLatitude() != null) {
          latitude = gp.getLatitude();
        }

        WrappedBigDecimal longitude = WrappedBigDecimal.fromDouble(0.0);
        if (gp.getLongitude() != null) {
          longitude = gp.getLongitude();
        }

        WrappedBigDecimal altitude = WrappedBigDecimal.fromDouble(0.0);
        if (gp.getAltitude() != null) {
          altitude = gp.getAltitude();
        }
        String point = String.format(FT_PLACEMARK_POINT_TEMPLATE, longitude + BasicConsts.COMMA
            + latitude + BasicConsts.COMMA + altitude);
        basicStringConversion(point, row);
      }
      basicStringConversion(gp.getAccuracy(), row);
    } else {
      basicStringConversion(null, row);
      basicStringConversion(null, row);
    }
  }
  
  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {
    basicStringConversion(repeat.getUniqueKeyStr(), row);
  }
  

}
