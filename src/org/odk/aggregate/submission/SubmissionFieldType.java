/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.javarosa.core.model.Constants;
import org.odk.aggregate.submission.type.BlobSubmissionType;
import org.odk.aggregate.submission.type.BooleanSubmissionType;
import org.odk.aggregate.submission.type.DateSubmissionType;
import org.odk.aggregate.submission.type.DecimalSubmissionType;
import org.odk.aggregate.submission.type.GeoPointSubmissionType;
import org.odk.aggregate.submission.type.IntegerSubmissionType;
import org.odk.aggregate.submission.type.StringSubmissionType;
import org.odk.aggregate.submission.type.SubmissionFieldBase;
import org.odk.aggregate.submission.type.jr.JRDateTimeType;
import org.odk.aggregate.submission.type.jr.JRDateType;
import org.odk.aggregate.submission.type.jr.JRTimeType;

/**
 * Enum that maps the value from an XForm definition to the
 * proper class that can handle the data conversion to the app engine datastore
 *
 * @author wbrunette@gmail.com
 *
 */
//TODO: decide how we should treat an unknown type?
public enum SubmissionFieldType {
  BOOLEAN(BooleanSubmissionType.class, RhizaInsightType.STRING), 
  DATE(DateSubmissionType.class, RhizaInsightType.DATE), 
  DECIMAL(DecimalSubmissionType.class, RhizaInsightType.NUMBER), 
  GEOPOINT(GeoPointSubmissionType.class, RhizaInsightType.GPS), 
  INTEGER(IntegerSubmissionType.class, RhizaInsightType.NUMBER),  
  JAVA_ROSA_DATE(JRDateType.class, RhizaInsightType.DATE),  
  JAVA_ROSA_DATETIME(JRDateTimeType.class, RhizaInsightType.DATE), 
  JAVA_ROSA_TIME(JRTimeType.class, RhizaInsightType.DATE),  
  PICTURE(BlobSubmissionType.class, RhizaInsightType.PICTURE), 
  STRING(StringSubmissionType.class, RhizaInsightType.STRING), 
  UNKNOWN(StringSubmissionType.class, RhizaInsightType.STRING);

  /**
   * Static conversion map from int (java rosa types) to the
   * SubmissionFieldType objects
   */
  private static Map<Integer, SubmissionFieldType> conversion;

  // initialize the java rosa types to SubmissionFieldType objects
  static {
    conversion = new HashMap<Integer, SubmissionFieldType>();
    conversion.put(Constants.DATATYPE_UNSUPPORTED, STRING);
    conversion.put(Constants.DATATYPE_NULL, UNKNOWN);
    conversion.put(Constants.DATATYPE_TEXT, STRING);
    conversion.put(Constants.DATATYPE_INTEGER, INTEGER);
    conversion.put(Constants.DATATYPE_DECIMAL, DECIMAL);
    conversion.put(Constants.DATATYPE_DATE, JAVA_ROSA_DATE);
    conversion.put(Constants.DATATYPE_TIME, JAVA_ROSA_TIME);
    conversion.put(Constants.DATATYPE_DATE_TIME, JAVA_ROSA_DATETIME);
    conversion.put(Constants.DATATYPE_CHOICE, STRING);
    conversion.put(Constants.DATATYPE_CHOICE_LIST, STRING);
    conversion.put(Constants.DATATYPE_BOOLEAN, STRING);
    conversion.put(Constants.DATATYPE_GEOPOINT, GEOPOINT);
    conversion.put(Constants.DATATYPE_BARCODE, STRING);
    conversion.put(Constants.DATATYPE_BINARY, PICTURE);
  }

  /**
   * Class that should be used to convert from XML submission to database entity
   * and vice versa
   */
  private Class<? extends SubmissionFieldBase<?>> submissionFieldTypeClass;

  /**
   * The rhiza insight type that maps to ODK aggregate submission type
   */
  private RhizaInsightType rhizaInsightType;
  
  /**
   * Constructor that assigns the mapping from SubmissionFieldType to the
   * class used for converting data from the XML submission to the datastore entity
   * 
   * @param submissionDataType
   *    class type used to convert to/from appengine datastore values
   * @param insightType
   *    data type mapping for rhiza insight
   */
  private SubmissionFieldType(Class<? extends SubmissionFieldBase<?>> submissionDataType,  RhizaInsightType insightType) {
    submissionFieldTypeClass = submissionDataType;
    rhizaInsightType = insightType;
  }

  /**
   * Get the class that should be used to convert the XML submission values to 
   * appengine datastore values (and vice versa)
   * 
   * @return
   *    class type used to convert to/from appengine datastore values
   */
  public Class<? extends SubmissionFieldBase<?>> getSubmissionFieldType() {
    return submissionFieldTypeClass;
  }

  /**
   * 
   * @return
   *    Rhiza Insight Type
   */
  public RhizaInsightType getRhizaInsightType() {
    return rhizaInsightType;
  }
  
  /**
   * Creates the submission conversion object based on enum value that will
   * take the value contained in the submission and convert it to the proper
   * appengine datastore type 
   * 
   * @param elementName
   *    submission element name 
   * @return
   *    instance of class to convert the submission to appengine datastore
   * 
   */
  public SubmissionField<?> createSubmissionField(String elementName) {
    SubmissionField<?> submissionData = null;

    // TODO: if performance becomes a problem switch from dynamic construction
    // to factory style
    try {
      Constructor<? extends SubmissionFieldBase<?>> constructor =
          submissionFieldTypeClass.getConstructor(String.class);
      submissionData = constructor.newInstance(elementName);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return submissionData;
  }

  /**
   * Convert from JavaRosa type to the correct Enum value
   * 
   * @param type
   *    JavaRosa type
   * @return
   *    ODK aggregate submission field type
   */
  public static SubmissionFieldType convertJavaRosaType(Integer type) {
    SubmissionFieldType fieldType = conversion.get(type);
    if (fieldType == null) {
      fieldType = UNKNOWN;
    }
    return fieldType;
  }

}
