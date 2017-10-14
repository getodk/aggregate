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
package org.opendatakit.aggregate.format.element;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.structure.XmlAttachmentFormatter;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Emits the list of media filenames and URLs for a given form.
 * Used for the new briefcase download.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class XmlMediaAttachmentFormatter implements ElementFormatter {
  XmlAttachmentFormatter xmlAttachmentFormatter;
  /**
   * Construct a XML Media Attachment Formatter
 * @param xmlAttachmentFormatter
   *
   */
  public XmlMediaAttachmentFormatter(XmlAttachmentFormatter xmlAttachmentFormatter) {
	  this.xmlAttachmentFormatter = xmlAttachmentFormatter;
  }

  @Override
  public void formatUid(String uri, String propertyName, Row row) {
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue,
      Row row, CallingContext cc) throws ODKDatastoreException {

    if( blobSubmission == null ||
    	(blobSubmission.getAttachmentCount(cc) == 0) ||
    	(blobSubmission.getContentHash(1, cc) == null) ) {
    	return;
    }

	String urlLink;
	{
		Map<String, String> properties = new HashMap<String, String>();
		SubmissionKey k = blobSubmission.generateSubmissionKey(1);
	    properties.put(ServletConsts.BLOB_KEY, k.toString());
	    properties.put(ServletConsts.AS_ATTACHMENT, "true");
	    String downloadRequestURL = cc.getServerURL() + BasicConsts.FORWARDSLASH + BinaryDataServlet.ADDR;
	    urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
	}
	// parallel to XFormsManifestXmlTable
    String xmlString = "<mediaFile>" +
    		"<filename>" + StringEscapeUtils.escapeXml10(blobSubmission.getUnrootedFilename(1, cc)) + "</filename>" +
    		"<hash>"	+ StringEscapeUtils.escapeXml10(blobSubmission.getContentHash(1, cc)) + "</hash>" +
    		"<downloadUrl>"	+ StringEscapeUtils.escapeXml10(urlLink) + "</downloadUrl>" +
    	"</mediaFile>\n";

    row.addFormattedValue(xmlString);
  }

  @Override
  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatChoices(List<String> choices, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatDecimal(WrappedBigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row,
      CallingContext cc) throws ODKDatastoreException {
	  xmlAttachmentFormatter.processRepeatedSubmssionSetsIntoRow(repeat.getSubmissionSets(), repeatElement, row, cc);
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
  }

}
