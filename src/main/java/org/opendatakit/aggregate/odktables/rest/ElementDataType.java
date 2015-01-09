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

/**
 * All the primitive data types in the ODK 2.0 storage model.
 * 
 * Note that because 'boolean' is a reserved word, we cannot use an enum (and we
 * shorten that type to 'bool' here).
 * 
 * It provides the name() and a modified valueOfOrNull() that acts as
 * 
 * to shorten that enum to 'bool'. To preserve the use of 'boolean' in the
 * transport, use the 'nameExternal()' and 'valueOfExternalOrNull(...)' methods
 * instead of the 'name()' and 'valueOf(...)' methods.
 * 
 * @author Administrator
 *
 */
public class ElementDataType {
  public static final ElementDataType integer = new ElementDataType("integer");
  public static final ElementDataType number = new ElementDataType("number");
  public static final ElementDataType bool = new ElementDataType("boolean");
  public static final ElementDataType rowpath = new ElementDataType("rowpath");
  public static final ElementDataType configpath = new ElementDataType("configpath");
  public static final ElementDataType array = new ElementDataType("array");
  public static final ElementDataType string = new ElementDataType("string");
  public static final ElementDataType object = new ElementDataType("object");

  private String nameExternal;

  private ElementDataType(String nameExternal) {
    this.nameExternal = nameExternal;
  }

  /**
   * Use this instead of name().
   * 
   * @return the external name of this enum for storage and transport.
   */
  public String name() {
    return nameExternal;
  }

  public String toString() {
    return name();
  }

  /**
   * Use this instead of valueOf(...)
   * 
   * @param name
   * @return the enum for the external name
   * @throws IllegalArgumentException
   */
  public static ElementDataType valueOf(String name) {
    if (name.equals(bool.name())) {
      return bool;
    } else if (name.equals(integer.name())) {
      return integer;
    } else if (name.equals(number.name())) {
      return number;
    } else if (name.equals(rowpath.name())) {
      return rowpath;
    } else if (name.equals(configpath.name())) {
      return configpath;
    } else if (name.equals(array.name())) {
      return array;
    } else if (name.equals(string.name())) {
      return string;
    } else if (name.equals(object.name())) {
      return object;
    }
    throw new IllegalArgumentException("unrecognized ElementDataType: " + name);
  }
}