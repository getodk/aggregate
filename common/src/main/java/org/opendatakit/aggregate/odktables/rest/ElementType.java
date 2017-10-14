/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.aggregate.odktables.rest;

public class ElementType {

  public static final String GEOPOINT = "geopoint";
  public static final String DATE = "date";
  public static final String DATETIME = "dateTime";
  public static final String TIME = "time";

  private final String elementType;
  private final ElementDataType dataType;
  private final String auxInfo;

  ElementType(String elementType, ElementDataType dataType, String auxInfo) {
    this.elementType = elementType;
    this.dataType = dataType;
    this.auxInfo = auxInfo;
  }

  public ElementDataType getDataType() {
    return dataType;
  }

  public String getElementType() {
    return elementType;
  }

  public String getAuxInfo() {
    return auxInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ElementType)) {
      return false;
    }
    ElementType other = (ElementType) o;
    return (getDataType() == other.getDataType())
        && getElementType().equals(other.getElementType())
        && ((getAuxInfo() == null) ? (other.getAuxInfo() == null) : getAuxInfo().equals(
            other.getAuxInfo()));
  }

  public String toString() {
    String suffix = (auxInfo == null) ? "" : auxInfo;
    String dataTypeString = ":" + dataType.name();
    if (dataType == ElementDataType.object || dataType.name().equals(elementType)
        || dataType == ElementDataType.string) {
      dataTypeString = "";
    }
    return elementType + dataTypeString + suffix;
  }

  public static final ElementType parseElementType(String type, boolean hasChildren) {
    int idxCloseParen = type.lastIndexOf(')');
    int idxOpenParen = type.indexOf('(');
    if ((idxOpenParen != -1) && (idxCloseParen < idxOpenParen)) {
      throw new IllegalArgumentException("malformed elementType string - no matching close paren.");
    }
    if (idxCloseParen != -1 && idxOpenParen == -1) {
      throw new IllegalArgumentException("malformed elementType string - no open paren.");
    }
    int idxColon = type.indexOf(':');
    if (idxOpenParen != -1 && idxColon > idxOpenParen) {
      // colon is within a term inside the auxInfo string.
      idxColon = -1;
    }

    String auxInfo = (idxCloseParen != -1 && idxOpenParen != -1) ? type.substring(idxOpenParen,
        idxCloseParen + 1) : null;

    String databaseType = null;
    if (idxColon != -1) {
      if (idxOpenParen == -1) {
        databaseType = type.substring(idxColon + 1);
      } else {
        databaseType = type.substring(idxColon + 1, idxOpenParen);
      }
      if (databaseType.length() == 0) {
        databaseType = null;
      }
    }

    int idxMax = type.length();
    if (idxColon != -1) {
      idxMax = idxColon;
    } else if (idxOpenParen != -1) {
      idxMax = idxOpenParen;
    }
    String elementType = type.substring(0, idxMax);

    ElementDataType elementDataType = null;
    try {
      elementDataType = ElementDataType.valueOf(elementType);
    } catch (IllegalArgumentException e) {
      // this is expected
    }

    ElementDataType dataType = null;
    if (databaseType != null) {
      // NOTE: throws IllegaArgumentException if unrecognized type
      dataType = ElementDataType.valueOf(databaseType);
    } else {
	  dataType = elementDataType;
	  if (dataType == null) {
        dataType = (hasChildren ? ElementDataType.object : ElementDataType.string);
	  }
	}

    if (hasChildren && !(dataType == ElementDataType.object || dataType == ElementDataType.array)) {
      throw new IllegalArgumentException(
          "malformed ElementType - invalid primitive datatype for elementType with children: " + type);
    }
	
    if (!hasChildren && (dataType == ElementDataType.object || dataType == ElementDataType.array)) {
	  throw new IllegalArgumentException("malformed elementType -- cannot declare an object or array having no children: " + type);
    }

    if (elementDataType != null && elementDataType != dataType) {
      throw new IllegalArgumentException(
          "malformed elementType -- attempting to explicitly re-type a primitive type: " + type);
    }

    return new ElementType(elementType, dataType, auxInfo);
  }

}