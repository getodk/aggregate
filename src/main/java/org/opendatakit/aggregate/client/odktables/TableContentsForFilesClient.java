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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This represents the contents of a table that will display information about
 * the files that are associated with a given table. It contains the column
 * names that should be displayed as well as a list of FileSummaryClient objects
 * that contain information about which <br>
 * It only exists so that you can essentially combine two service calls, one
 * that gets the column names and one that gets the rows, without having to
 * worry about the services returning at different times. For this reason there
 * is no corresponding client-side TableContents object.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableContentsForFilesClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -564495395828330964L;

  public TableContentsForFilesClient() {
    // necessary for gwt serialization
  }

  /**
   * The files for the table. The usage determines whether
   * these are table-level or instance files.
   */
  public ArrayList<FileSummaryClient> files;

}
