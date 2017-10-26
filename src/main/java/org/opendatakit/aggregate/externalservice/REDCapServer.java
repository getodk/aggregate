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

package org.opendatakit.aggregate.externalservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.ChoiceSubmissionType;
import org.opendatakit.aggregate.submission.type.DecimalSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.aggregate.submission.type.GeoPointSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.aggregate.submission.type.jr.JRDateTimeType;
import org.opendatakit.aggregate.submission.type.jr.JRDateType;
import org.opendatakit.aggregate.submission.type.jr.JRTimeType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class REDCapServer extends AbstractExternalService implements ExternalService {
  private static final Log logger = LogFactory.getLog(FusionTable.class.getName());

  /**
   * Datastore entity specific to this type of external service
   */
  private final REDCapServerParameterTable objectEntity;

  private REDCapServer(REDCapServerParameterTable entity, FormServiceCursor formServiceCursor,
                       IForm form, CallingContext cc) {
    super(form, formServiceCursor, new BasicElementFormatter(true, true, true, false),
        new BasicHeaderFormatter(true, true, true), cc);
    objectEntity = entity;
  }

  private REDCapServer(REDCapServerParameterTable entity, IForm form,
                       ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException {
    this(entity, createFormServiceCursor(form, entity, externalServiceOption,
        ExternalServiceType.REDCAP_SERVER, cc), form, cc);
    objectEntity.setOwnerEmail(ownerEmail);
  }

  public REDCapServer(FormServiceCursor formServiceCursor, IForm form, CallingContext cc)
      throws ODKDatastoreException {
    this(retrieveEntity(REDCapServerParameterTable.assertRelation(cc), formServiceCursor, cc),
        formServiceCursor, form, cc);
  }

  public REDCapServer(IForm form, String apiKey, String url,
                      ExternalServicePublicationOption externalServiceOption, String ownerEmail, CallingContext cc)
      throws ODKDatastoreException {
    this(newEntity(REDCapServerParameterTable.assertRelation(cc), cc), form, externalServiceOption,
        ownerEmail, cc);

    // createForm();
    objectEntity.setApiKey(apiKey);
    objectEntity.setUrl(url);
    persist(cc);
  }

  @Override
  protected String getOwnership() {
    return objectEntity.getOwnerEmail().substring(EmailParser.K_MAILTO.length());
  }

  public String getApiKey() {
    return objectEntity.getApiKey();
  }

  public void setApiKey(String apiKey) {
    objectEntity.setApiKey(apiKey);
  }

  public String getUrl() {
    return objectEntity.getUrl();
  }

  @Override
  public void initiate(CallingContext cc) throws ODKExternalServiceException,
      ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException {
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);

    // upload data to external service
    postUploadTask(cc);
  }

  private void submitPost(String actionType, HttpEntity postentity, List<NameValuePair> qparam,
                          CallingContext cc) {

    try {
      HttpResponse response = this.sendHttpRequest(POST, getUrl(), postentity, qparam, cc);
      int statusCode = response.getStatusLine().getStatusCode();
      String responseString = WebUtils.readResponse(response);

      if (responseString.length() != 0) {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(true);
        xmlFactory.setIgnoringComments(true);
        xmlFactory.setCoalescing(true);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(responseString.getBytes(UTF_CHARSET));
        Document doc;
        try {
          doc = builder.parse(is);
        } finally {
          is.close();
        }
        Element root = doc.getDocumentElement();

        NodeList errorNodes = root.getElementsByTagName("error");
        StringBuilder b = new StringBuilder();
        if (errorNodes != null) {
          for (int i = 0; i < errorNodes.getLength(); ++i) {
            if (i != 0) {
              b.append("\n");
            }
            Element e = (Element) errorNodes.item(i);
            b.append(e.getTextContent());
          }
          String error = b.toString();
          if (error.length() != 0) {
            throw new IllegalArgumentException(actionType
                + " to REDCap server failed. statusCode: " + statusCode + " error: " + error);
          }
        }
      } else {
        // this seems to be the common case???
        logger.info(actionType + " to REDCap server returned no body");
      }

      if (statusCode != HttpStatus.SC_OK) {
        throw new IllegalArgumentException(actionType
            + " to REDCap server failed - but no error content. Reason: "
            + response.getStatusLine().getReasonPhrase() + " status code: " + statusCode);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(actionType + " to REDCap server failed - " + e.toString());
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(actionType + " to REDCap server failed - " + e.toString());
    } catch (SAXException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(actionType + " to REDCap server failed - " + e.toString());
    }
  }

  public void submitFile(String recordID, String fileField, BlobSubmissionType blob_value,
                         CallingContext cc) throws MalformedURLException, IOException,
      ODKDatastoreException {

    String contentType = blob_value.getContentType(1, cc);
    String filename = blob_value.getUnrootedFilename(1, cc);
    filename = fileField + filename.substring(filename.lastIndexOf('.'));

    /**
     * REDCap server appears to be highly irregular in the structure of the
     * form-data submission it will accept from the client. The following should
     * work, but either resets the socket or returns a 403 error.
     */
    ContentType utf8Text = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), UTF_CHARSET);
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .setCharset(UTF_CHARSET);
    builder.addTextBody("token", getApiKey(), utf8Text)
        .addTextBody("content", "file", utf8Text)
        .addTextBody("action", "import", utf8Text)
        .addTextBody("record", recordID, utf8Text)
        .addTextBody("field", fileField, utf8Text)
        .addBinaryBody("file", blob_value.getBlob(1, cc), ContentType.create(contentType), filename);

    submitPost("File import", builder.build(), null, cc);
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {

    try {
      // an empty map to hold our values
      String study_id = null;

      // create a hash to hold the blobs for later processing
      Map<String, BlobSubmissionType> blobs = new HashMap<String, BlobSubmissionType>();

      StringBuilder b = new StringBuilder();

      Map<FormElementModel, SubmissionValue> valuesMap = submission.getSubmissionValuesMap();

      for (FormElementModel element : valuesMap.keySet()) {
        SubmissionValue value = valuesMap.get(element);
        if (value == null) {
          continue;
        }
        if (element.isMetadata()) {
          // handle metadata specially
        } else {
          switch (element.getElementType()) {
            case METADATA:
              // This keeps lint warnings down...
              break;
            case GEOSHAPE:
            case GEOTRACE:
            case STRING: {
              StringSubmissionType str = (StringSubmissionType) value;
              String strValue = str.getValue();
              if (element.getElementName().equals("study_id")) {
                // Piece of crap parser in REDCap requires study id to be first
                // element
                study_id = strValue;
              } else if (strValue != null) {
                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");
              }
            }
            break;

            case JRDATETIME: {
              JRDateTimeType dt = (JRDateTimeType) value;
              Date dtValue = dt.getValue();

              if (dtValue != null) {
                GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                g.setTime(dtValue);

                String strValue = String.format(FormatConsts.REDCAP_DATE_TIME_FORMAT_STRING,
                    g.get(Calendar.YEAR), g.get(Calendar.MONTH) + 1, g.get(Calendar.DAY_OF_MONTH),
                    g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE), g.get(Calendar.SECOND));

                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");

              }
            }
            break;

            case JRDATE: {
              JRDateType dt = (JRDateType) value;
              Date dtValue = dt.getValue();

              if (dtValue != null) {
                GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                g.setTime(dtValue);

                String strValue = String.format(FormatConsts.REDCAP_DATE_ONLY_FORMAT_STRING,
                    g.get(Calendar.YEAR), g.get(Calendar.MONTH) + 1, g.get(Calendar.DAY_OF_MONTH));

                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");
              }
            }
            break;

            case JRTIME: {
              JRTimeType dt = (JRTimeType) value;
              Date dtValue = dt.getValue();

              if (dtValue != null) {
                GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                g.setTime(dtValue);

                String strValue = String.format(FormatConsts.REDCAP_TIME_FORMAT_STRING,
                    g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE));

                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");
              }
            }
            break;

            case INTEGER: {
              LongSubmissionType longVal = (LongSubmissionType) value;
              if (longVal.getValue() != null) {
                String strValue = longVal.getValue().toString();

                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");
              }
            }
            break;

            case DECIMAL: {
              DecimalSubmissionType dec = (DecimalSubmissionType) value;
              if (dec.getValue() != null) {
                String strValue = dec.getValue().toString();

                b.append("<").append(element.getElementName()).append(">")
                    .append(StringEscapeUtils.escapeXml10(strValue)).append("</")
                    .append(element.getElementName()).append(">");
              }
            }
            break;

            case GEOPOINT: {
              // TODO: should not have gps_ prefix on tag...
              String strippedElementName = element.getElementName().replace("gps_", "");
              GeoPointSubmissionType submissionValue = (GeoPointSubmissionType) value;
              GeoPoint coors = submissionValue.getValue();
              if (coors.getLatitude() != null) {
                b.append("<").append("gps_lat_" + strippedElementName).append(">")
                    .append(StringEscapeUtils.escapeXml10(coors.getLatitude().toString())).append("</")
                    .append("gps_lat_" + strippedElementName).append(">");

                b.append("<").append("gps_lon_" + strippedElementName).append(">")
                    .append(StringEscapeUtils.escapeXml10(coors.getLongitude().toString()))
                    .append("</").append("gps_lon_" + strippedElementName).append(">");

                b.append("<").append("gps_alt_" + strippedElementName).append(">")
                    .append(StringEscapeUtils.escapeXml10(coors.getAltitude().toString())).append("</")
                    .append("gps_alt_" + strippedElementName).append(">");

                b.append("<").append("gps_acc_" + strippedElementName).append(">")
                    .append(StringEscapeUtils.escapeXml10(coors.getAccuracy().toString())).append("</")
                    .append("gps_acc_" + strippedElementName).append(">");
              }
            }
            break;

            case BINARY: {
              String file_field = element.getElementName();
              BlobSubmissionType blob_value = (BlobSubmissionType) value;
              if (blob_value.getAttachmentCount(cc) == 1) {
                blobs.put(file_field, blob_value);
              }
              // upload these after we have successfully imported the record
            }
            break;

            case BOOLEAN: {
              String strippedElementName = element.getElementName().replace("slct-", "");
              BooleanSubmissionType bType = (BooleanSubmissionType) value;
              if (bType.getValue() != null) {
                b.append("<").append(strippedElementName + "___" + bType.getValue().toString())
                    .append(">").append(StringEscapeUtils.escapeXml10("1")).append("</")
                    .append(strippedElementName + "___" + bType.getValue().toString()).append(">");
              }
            }
            break;

            case SELECT1:
            case SELECTN: {
              // TODO: it's not necessary to add (or remove) 'slct-' from
              // the field name anymore
              String formatElementName = element.getElementName().replace("slct-", "");
              ChoiceSubmissionType choice = (ChoiceSubmissionType) value;
              for (String choiceVal : choice.getValue()) {
                b.append("<").append(formatElementName + "___" + choiceVal).append(">")
                    .append(StringEscapeUtils.escapeXml10("1")).append("</")
                    .append(formatElementName + "___" + choiceVal).append(">");
              }
            }
            break;

            case REPEAT: {
              logger.warn("Unable to publish repeat groups to REDCap");
              // REDCap does not handle repeat groups.
            }
            break;

            case GROUP:
              logger.warn("The GROUP submission type is not implemented");
              break;

          }
        }
      }

      b.append("</item></records>");

      if (study_id == null) {
        throw new IllegalStateException("Form does not contain a study_id field -- cannot publish!");
      }

      String submissionsListString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><records><item><study_id>"
          + StringEscapeUtils.escapeXml10(study_id) + "</study_id>" + b.toString();

      List<NameValuePair> eparams = new ArrayList<NameValuePair>();
      eparams.add(new BasicNameValuePair("token", getApiKey()));
      eparams.add(new BasicNameValuePair("content", "record"));
      eparams.add(new BasicNameValuePair("format", "xml"));
      eparams.add(new BasicNameValuePair("overwriteBehavior", "overwrite"));
      eparams.add(new BasicNameValuePair("data", submissionsListString));
      eparams.add(new BasicNameValuePair("returnContent", "ids"));
      eparams.add(new BasicNameValuePair("returnFormat", "xml"));

      HttpEntity postentity = new UrlEncodedFormEntity(eparams, UTF_CHARSET);

      submitPost("Publishing", postentity, null, cc);

      // send the files if they exist
      for (Map.Entry<String, BlobSubmissionType> e : blobs.entrySet()) {
        System.out.println("Processing media attachment....");
        BlobSubmissionType blob = e.getValue();
        submitFile(study_id, e.getKey(), blob, cc);
      }

    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }

  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof REDCapServer)) {
      return false;
    }
    REDCapServer other = (REDCapServer) obj;
    return (objectEntity == null ? (other.objectEntity == null)
        : (other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (other.fsc != null && fsc.equals(other.fsc)));
  }

  @Override
  public String getDescriptiveTargetString() {
    // the apiKey, if supplied, is a secret.
    // Show only the first 4 characters, or,
    // if the string is less than 8 characters long, show less.
    String auth = getApiKey();
    if (auth != null && auth.length() != 0) {
      auth = " token: " + auth.substring(0, Math.min(4, auth.length() / 3)) + "...";
    }
    return getUrl() + auth;
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return null;
  }

}