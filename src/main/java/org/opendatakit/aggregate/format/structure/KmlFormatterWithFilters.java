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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.KmlSelection;
import org.opendatakit.aggregate.client.form.KmlSelection.KmlOptionType;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.format.KmlConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author adam.lerer@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class KmlFormatterWithFilters implements SubmissionFormatter {

  private IForm form;
  private List<FormElementModel> propertyNames;

  private PrintWriter output;

  private boolean hasGeotraceOrGeoshape;
  private boolean hasImages;

  private List<AbstractKmlElementBase> geoformaters;

  public KmlFormatterWithFilters(IForm xform, String webServerUrl,
      List<KmlSelection> kmlElementsToInclude, PrintWriter printWriter,
      FilterGroup filterGroup, CallingContext cc) {

    form = xform;
    output = printWriter;

    // reusing other code to generate header names
    SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    propertyNames = headerGenerator.getIncludedElements();

    FormElementModel topElement = form.getTopLevelGroupElement();

    // will be udpated to true as kml element formatters are created if used
    hasGeotraceOrGeoshape = false;
    hasImages = false;
    
    // create kml element formatters
    geoformaters = new ArrayList<AbstractKmlElementBase>();
    for (KmlSelection kmlElement : kmlElementsToInclude) {
      if (kmlElement.getGeoType() == KmlOptionType.GEO_POINT) {
        FormElementModel geopointField = null;
        String geopointKey = kmlElement.getGeoElementKey();
        if (geopointKey != null) {
          FormElementKey geopointFEMKey = new FormElementKey(geopointKey);
          geopointField = FormElementModel.retrieveFormElementModel(form, geopointFEMKey);
        }

        FormElementModel titleField = null;
        String titleKey = kmlElement.getTitleElementKey();
        if (titleKey != null && !titleKey.equals(UIConsts.KML_NONE_ENCODE_KEY)) {
          FormElementKey titleFEMKey = new FormElementKey(titleKey);
          titleField = FormElementModel.retrieveFormElementModel(form, titleFEMKey);
        }

        FormElementModel imgField = null;
        String binaryKey = kmlElement.getBinaryElementKey();
        if (binaryKey != null && !binaryKey.equals(UIConsts.KML_NONE_ENCODE_KEY)) {
          hasImages = true;
          FormElementKey imageFEMKey = new FormElementKey(binaryKey);
          imgField = FormElementModel.retrieveFormElementModel(form, imageFEMKey);
        }

        geoformaters.add(new KmlGeoPointGenerator(geopointField, titleField, imgField,
            webServerUrl, topElement));
      }  else if (kmlElement.getGeoType() == KmlOptionType.GEO_TRACE_N_SHAPE) {
        hasGeotraceOrGeoshape = true;
        
        FormElementModel geoField = null;
        String geoKey = kmlElement.getGeoElementKey();
        if (geoKey != null) {
          FormElementKey geoFEMKey = new FormElementKey(geoKey);
          geoField = FormElementModel.retrieveFormElementModel(form, geoFEMKey);
        }

        FormElementModel nameField = null;
        String nameKey = kmlElement.getNameElementKey();
        if (nameKey != null && !nameKey.equals(UIConsts.KML_NONE_ENCODE_KEY)) {
          FormElementKey nameFEMKey = new FormElementKey(nameKey);
          nameField = FormElementModel.retrieveFormElementModel(form, nameFEMKey);
        }

        geoformaters.add(new KmlGeoTraceNGeoShapeGenerator(geoField, nameField, topElement));
      }
    }
  }

  @Override
  public void beforeProcessSubmissions(CallingContext cc) throws ODKDatastoreException {
    output.write(String.format(KmlConsts.KML_PREAMBLE_TEMPLATE,
        StringEscapeUtils.escapeXml10(form.getFormId()),
        StringEscapeUtils.escapeXml10(form.getViewableName()),
        StringEscapeUtils.escapeXml10(form.getViewableName())));
    output.write(generateStyle(hasImages, hasGeotraceOrGeoshape));
  }

  @Override
  public void processSubmissionSegment(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    // format row elements
    for (Submission sub : submissions) {
      for (AbstractKmlElementBase geoformat : geoformaters) {
        String placemark = geoformat.generatePlacemarkSubmission(sub, propertyNames, cc);
        output.write(placemark);
      }
    }
  }

  @Override
  public void afterProcessSubmissions(CallingContext cc) throws ODKDatastoreException {

    // output postamble
    output.write(KmlConsts.KML_POSTAMBLE_TEMPLATE);
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }

  private String generateStyle(boolean hasImage, boolean hasGeotraceGeoshape) {
    // add basic balloon without image to style string
    String style = KmlConsts.KML_BALLON_NO_IMAGE_STYLE_DEFN;

    // if any image, add style
    if (hasImage) {
      style += KmlConsts.KML_BALLON_WITH_IMAGE_STYLE_DEFN;
    }
    // if any geotrace and geoshape, add style
    if (hasGeotraceGeoshape) {
      style += String.format(KmlConsts.KML_GEOSHAPE_STYLE_TEMPLATE, KmlConsts.GEOSHAPE_STYLE);
    }

    return style;
  }

}
