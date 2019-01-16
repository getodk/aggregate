/*
 * Copyright (C) 2011 University of Washington.
 * Copyright (C) 2018 Nafundi
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

package org.opendatakit.common.persistence.engine;

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.opendatakit.aggregate.submission.type.jr.JRTemporalUtils.parseDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.utils.WebUtils;

public class EngineUtils {

  public static String getDominantSortAttributeValueAsString(CommonFieldsBase cb, DataField dominantAttr) {
    String value;
    switch (dominantAttr.getDataType()) {
      case BINARY:
        throw new IllegalStateException("cannot sort on a binary field");
      case LONG_STRING:
        throw new IllegalStateException("cannot sort on a long text field");
      case URI:
      case STRING: {
        value = cb.getStringField(dominantAttr);
        break;
      }
      case INTEGER: {
        Long l = cb.getLongField(dominantAttr);
        if (l == null) {
          value = null;
        } else {
          value = Long.toString(l);
        }
        break;
      }
      case DECIMAL: {
        WrappedBigDecimal bd = cb.getNumericField(dominantAttr);
        if (bd == null) {
          value = null;
        } else {
          value = bd.toString();
        }
        break;
      }
      case BOOLEAN: {
        Boolean b = cb.getBooleanField(dominantAttr);
        if (b == null) {
          value = null;
        } else {
          value = b.toString();
        }
        break;
      }
      case DATETIME: {
        Date d = cb.getDateField(dominantAttr);
        if (d == null) {
          value = null;
        } else {
          value = OffsetDateTime.ofInstant(d.toInstant(), systemDefault()).format(ISO_OFFSET_DATE_TIME);
        }
        break;
      }
      default:
        throw new IllegalStateException("datatype not handled");
    }
    return value;
  }

  public static String getAttributeValueAsString(Object o, DataField dominantAttr) {
    String value;
    switch (dominantAttr.getDataType()) {
      case BINARY:
        throw new IllegalStateException("cannot sort on a binary field");
      case LONG_STRING:
        throw new IllegalStateException("cannot sort on a long text field");
      case URI:
      case STRING: {
        value = (String) o;
        break;
      }
      case INTEGER: {
        Long l = (Long) o;
        if (l == null) {
          value = null;
        } else {
          value = Long.toString(l);
        }
        break;
      }
      case DECIMAL: {
        WrappedBigDecimal bd;
        if (o == null) {
          bd = null;
        } else if (o instanceof Double) {
          bd = WrappedBigDecimal.fromDouble((Double) o);
        } else {
          bd = new WrappedBigDecimal(o.toString());
        }
        if (bd == null) {
          value = null;
        } else {
          if (!dominantAttr.isDoublePrecision() && !bd.isSpecialValue()) {
            bd = bd.setScale(dominantAttr.getNumericScale(), BigDecimal.ROUND_HALF_UP);
          }
          value = bd.toString();
        }
        break;
      }
      case BOOLEAN: {
        Boolean b = (Boolean) o;
        if (b == null) {
          value = null;
        } else {
          value = b.toString();
        }
        break;
      }
      case DATETIME: {
        Date d = (Date) o;
        if (d == null) {
          value = null;
        } else {
          value = OffsetDateTime.ofInstant(d.toInstant(), systemDefault()).format(ISO_OFFSET_DATE_TIME);
        }
        break;
      }
      default:
        throw new IllegalStateException("datatype not handled");
    }
    return value;
  }

  public static Object getDominantSortAttributeValueFromString(String v, DataField dominantAttr) {
    Object value;
    switch (dominantAttr.getDataType()) {
      case BINARY:
        throw new IllegalStateException("cannot sort on a binary field");
      case LONG_STRING:
        throw new IllegalStateException("cannot sort on a long text field");
      case URI:
      case STRING: {
        value = v;
        break;
      }
      case INTEGER: {
        if (v == null) {
          value = null;
        } else {
          value = Long.valueOf(v);
        }
        break;
      }
      case DECIMAL: {
        if (v == null) {
          value = null;
        } else {
          WrappedBigDecimal bd = new WrappedBigDecimal(v);
          if (!dominantAttr.isDoublePrecision() && !bd.isSpecialValue()) {
            bd = bd.setScale(dominantAttr.getNumericScale(), BigDecimal.ROUND_HALF_UP);
          }
          value = bd;
        }
        break;
      }
      case BOOLEAN: {
        if (v == null) {
          value = null;
        } else {
          value = WebUtils.parseBoolean(v);
        }
        break;
      }
      case DATETIME: {
        if (v == null) {
          value = null;
        } else {
          value = parseDate(v);
        }
        break;
      }
      default:
        throw new IllegalStateException("datatype not handled");
    }
    return value;
  }

}
