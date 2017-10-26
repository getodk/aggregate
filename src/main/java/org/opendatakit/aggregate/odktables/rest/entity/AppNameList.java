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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This holds a list of appNames.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="appNames")
public class AppNameList extends ArrayList<String> {

  /**
   * 
   */
  private static final long serialVersionUID = -8124923511487754240L;

  /**
   * Constructor used by Jackson
   */
  public AppNameList() {
  }

  public AppNameList(List<String> versions) {
    super.addAll(versions);
  }
}
