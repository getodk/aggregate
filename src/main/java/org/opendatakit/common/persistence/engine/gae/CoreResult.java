/*
  Copyright (C) 2011 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.common.persistence.engine.gae;

import java.util.List;
import org.opendatakit.common.persistence.CommonFieldsBase;

/**
 * Returns results from QueryImpl.coreExecuteQuery(...).
 * The returned results are then used to build the value 
 * list and QueryResumePoint values of the QueryResult
 * object.
 *
 * @author mitchellsundt@gmail.com
 *
 */
final class CoreResult {
  final List<CommonFieldsBase> results;
  final boolean hasMoreResults;

  CoreResult(List<CommonFieldsBase> results, boolean hasMoreResults) {
    this.results = results;
    this.hasMoreResults = hasMoreResults;
  }
}