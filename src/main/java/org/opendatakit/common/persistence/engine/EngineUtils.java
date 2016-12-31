/**
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine;

import java.math.BigDecimal;
import java.util.Date;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.utils.WebUtils;

public class EngineUtils {

	public static final Object getDominantSortAttributeValue( CommonFieldsBase odkEntity, DataField dominantAttr) {
		switch ( dominantAttr.getDataType() ) {
		case BINARY: throw new IllegalStateException("unexpected sort on binary field");
		case LONG_STRING: throw new IllegalStateException("unexpected sort on long text field");
		case URI:
		case STRING:
			return odkEntity.getStringField(dominantAttr);
		case INTEGER:
			return odkEntity.getLongField(dominantAttr);
		case DECIMAL:
			return odkEntity.getNumericField(dominantAttr);
		case BOOLEAN:
			return odkEntity.getBooleanField(dominantAttr);
		case DATETIME:
			return odkEntity.getDateField(dominantAttr);
		default:
			throw new IllegalStateException("unexpected data type");
		}
	}
	
	public static final boolean hasMatchingDominantSortAttribute(CommonFieldsBase odkLastEntity, CommonFieldsBase odkEntity, DataField dominantAttr) {
		boolean matchingDominantAttr = false;
		switch ( dominantAttr.getDataType() ) {
		case BINARY: throw new IllegalStateException("unexpected sort on binary field");
		case LONG_STRING: throw new IllegalStateException("unexpected sort on long text field");
		case URI:
		case STRING:
		{
			String a1 = odkLastEntity.getStringField(dominantAttr);
			String a2 = odkEntity.getStringField(dominantAttr);
			matchingDominantAttr = (a1 == null) ? (a2 == null) : ((a2 != null) && a1.compareTo(a2) == 0);
		}
			break;
		case INTEGER:
		{
			Long a1 = odkLastEntity.getLongField(dominantAttr);
			Long a2 = odkEntity.getLongField(dominantAttr);
			matchingDominantAttr = (a1 == null) ? (a2 == null) : ((a2 != null) && a1.compareTo(a2) == 0);
		}
			break;
		case DECIMAL:
		{
			WrappedBigDecimal a1 = odkLastEntity.getNumericField(dominantAttr);
			WrappedBigDecimal a2 = odkEntity.getNumericField(dominantAttr);
			matchingDominantAttr = (a1 == null) ? (a2 == null) : ((a2 != null) && a1.compareTo(a2) == 0);
		}
			break;
		case BOOLEAN:
		{
			Boolean a1 = odkLastEntity.getBooleanField(dominantAttr);
			Boolean a2 = odkEntity.getBooleanField(dominantAttr);
			matchingDominantAttr = (a1 == null) ? (a2 == null) : ((a2 != null) && a1.compareTo(a2) == 0);
		}
			break;
		case DATETIME:
		{
			Date a1 = odkLastEntity.getDateField(dominantAttr);
			Date a2 = odkEntity.getDateField(dominantAttr);
			matchingDominantAttr = (a1 == null) ? (a2 == null) : ((a2 != null) && a1.compareTo(a2) == 0);
		}
			break;
		default:
			throw new IllegalStateException("unexpected data type");
		}
		return matchingDominantAttr;
	}

	public static final String getDominantSortAttributeValueAsString(CommonFieldsBase cb, DataField dominantAttr) {
		String value;
		switch ( dominantAttr.getDataType() ) {
		case BINARY: throw new IllegalStateException("cannot sort on a binary field");
		case LONG_STRING: throw new IllegalStateException("cannot sort on a long text field");
		case URI:
		case STRING: {
			value = cb.getStringField(dominantAttr); 
			break;
		}
		case INTEGER: {
			Long l = cb.getLongField(dominantAttr);
			if ( l == null ) {
				value = null;
			} else {
				value = Long.toString(l);
			}
			break;
		}
		case DECIMAL: {
			WrappedBigDecimal bd = cb.getNumericField(dominantAttr);
			if ( bd == null ) {
				value = null;
			} else {
				value = bd.toString();
			}
			break;
		}
		case BOOLEAN: {
			Boolean b = cb.getBooleanField(dominantAttr);
			if ( b == null ) {
				value = null;
			} else {
				value = b.toString();
			}
			break;
		}
		case DATETIME: {
			Date d = cb.getDateField(dominantAttr);
			if ( d == null ) {
				value = null;
			} else {
				value = WebUtils.iso8601Date(d);
			}
			break;
		}
		default:
			throw new IllegalStateException("datatype not handled");
		}
		return value;
	}

   public static final String getAttributeValueAsString(Object o, DataField dominantAttr) {
      String value;
      switch ( dominantAttr.getDataType() ) {
      case BINARY: throw new IllegalStateException("cannot sort on a binary field");
      case LONG_STRING: throw new IllegalStateException("cannot sort on a long text field");
      case URI:
      case STRING: {
         value = (String) o; 
         break;
      }
      case INTEGER: {
         Long l = (Long) o;
         if ( l == null ) {
            value = null;
         } else {
            value = Long.toString(l);
         }
         break;
      }
      case DECIMAL: {
    	 WrappedBigDecimal bd;
         if ( o == null ) {
           bd = null;
         } else if ( o instanceof Double ) {
           bd = WrappedBigDecimal.fromDouble((Double) o);
         } else {
           bd = new WrappedBigDecimal(o.toString());
         }
         if ( bd == null ) {
            value = null;
         } else {
            if ( !dominantAttr.isDoublePrecision() && !bd.isSpecialValue() ) {
              bd = bd.setScale(dominantAttr.getNumericScale(), BigDecimal.ROUND_HALF_UP);
            }
            value = bd.toString();
         }
         break;
      }
      case BOOLEAN: {
         Boolean b = (Boolean) o;
         if ( b == null ) {
            value = null;
         } else {
            value = b.toString();
         }
         break;
      }
      case DATETIME: {
         Date d = (Date) o;
         if ( d == null ) {
            value = null;
         } else {
            value = WebUtils.iso8601Date(d);
         }
         break;
      }
      default:
         throw new IllegalStateException("datatype not handled");
      }
      return value;
   }

	public static final Object getDominantSortAttributeValueFromString(String v, DataField dominantAttr) {
		Object value;
		switch ( dominantAttr.getDataType() ) {
		case BINARY: throw new IllegalStateException("cannot sort on a binary field");
		case LONG_STRING: throw new IllegalStateException("cannot sort on a long text field");
		case URI:
		case STRING: {
			value = v; 
			break;
		}
		case INTEGER: {
			if ( v == null ) {
				value = null;
			} else {
				value = Long.valueOf(v);
			}
			break;
		}
		case DECIMAL: {
			if ( v == null ) {
				value = null;
			} else {
				WrappedBigDecimal bd = new WrappedBigDecimal(v);
				if ( !dominantAttr.isDoublePrecision() && !bd.isSpecialValue() ) {
				  bd = bd.setScale(dominantAttr.getNumericScale(), BigDecimal.ROUND_HALF_UP);
				}
				value = bd;
			}
			break;
		}
		case BOOLEAN: {
			if ( v == null ) {
				value = null;
			} else {
				value = WebUtils.parseBoolean(v);
			}
			break;
		}
		case DATETIME: {
			if ( v == null ) {
				value = null;
			} else {
				value = WebUtils.parseDate(v);
			}
			break;
		}
		default:
			throw new IllegalStateException("datatype not handled");
		}
		return value;
	}

}
