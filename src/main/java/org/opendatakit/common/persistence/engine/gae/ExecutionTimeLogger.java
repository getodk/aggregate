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

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that tracks the time it takes to execute a query in AppEngine.
 * Logs that time if the execution exceeds the cost logging threshold.
 *
 * @author mitchellsundt@gmail.com
 */
final class ExecutionTimeLogger {

  private final String loggingContextTag;
  private final CommonFieldsBase relation;
  private final long startApiTime;
  private final Logger logger;
  private final Logger queryStringLogger;
  String queryString = null;
  private long istartApiTime;

  ExecutionTimeLogger(DatastoreImpl datastore, String loggingContextTag, CommonFieldsBase relation) {
    this.loggingContextTag = loggingContextTag;
    this.relation = relation;
    this.logger = LoggerFactory.getLogger(ExecutionTimeLogger.class);
    this.queryStringLogger = LoggerFactory.getLogger("org.opendatakit.common.persistence.LogQueryString." + relation.getSchemaName() + "." + relation.getTableName());
    QueryImpl.updateCostLoggingThreshold(datastore);

    istartApiTime = startApiTime = System.currentTimeMillis();
  }

  void declareQuery(com.google.appengine.api.datastore.Query hack) {
    if (queryString != null) {
      intermediateLogging();
    }
    queryString = hack.toString();
    queryStringLogger.debug(queryString);

    // report intermediate results from when query is declared (i.e.,
    // execution steps only).
    istartApiTime = System.currentTimeMillis();
  }

  private void intermediateLogging() {
    long endApiTime = System.currentTimeMillis();
    long elapsed = endApiTime - istartApiTime;
    if (elapsed >= QueryImpl.costLoggingMinimumMegacyclesThreshold) {
      logger.warn(String.format("%1$06d **intermediate** %2$s[%3$s] %4$s", elapsed,
          loggingContextTag, relation.getTableName(), queryString));
    }
  }

  void wrapUp() {
    long endApiTime = System.currentTimeMillis();
    long elapsed = endApiTime - startApiTime;
    if (queryString != null) {
      intermediateLogging();
    }
    if (elapsed >= QueryImpl.costLoggingMinimumMegacyclesThreshold) {
      logger.warn(String.format("%1$06d **final** %2$s[%3$s]", elapsed, loggingContextTag,
          relation.getTableName()));
    }
  }
}
