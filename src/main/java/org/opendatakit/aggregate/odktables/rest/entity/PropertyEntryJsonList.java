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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;

/**
 * This holds a list of {@link PropertyEntryJson}.
 * This holds the full JSON serialization of the properties.csv
 * 
 * See {@link PropertyEntryXmlList} for the XML variant of this.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class PropertyEntryJsonList extends ArrayList<PropertyEntryJson> {

  /**
   * 
   */
  private static final long serialVersionUID = -922490204257676096L;

  /**
   * Constructor used by Jackson
   */
  public PropertyEntryJsonList() {
  }

}
