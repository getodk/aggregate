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
package org.opendatakit.common.persistence;

import java.util.List;

/**
 * Result object that understands and supports resumable queries.
 * <p>
 * Once a query object is constructed, it can be repeatedly invoked with the
 * resumeCursor from any QueryResult to return the next set results from that
 * query.
 * <p>
 * Alternatively, the user can obtain the elements preceding the contents of the
 * result set by constructing a 'backward query' with the same filter criteria
 * but all sort directions inverted and pass the backwardCursor defined in this
 * QueryResult into that new query object to obtain the preceding elements.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryResult {

  private final QueryResumePoint startCursor;
  private final QueryResumePoint backwardCursor;
  private final QueryResumePoint resumeCursor;
  private final boolean hasMoreResults;
  private final boolean hasPriorResults;

  private final List<? extends CommonFieldsBase> resultList;

  public QueryResult(QueryResumePoint startCursor, List<? extends CommonFieldsBase> resultList,
      QueryResumePoint backwardCursor, QueryResumePoint resumeCursor, boolean moreResults) {
    this.startCursor = startCursor;
    this.resultList = resultList;
    this.backwardCursor = backwardCursor;
    this.resumeCursor = resumeCursor;
    this.hasMoreResults = moreResults;
    this.hasPriorResults = (startCursor != null);
  }

  public QueryResumePoint getResumeCursor() {
    return resumeCursor;
  }

  public List<? extends CommonFieldsBase> getResultList() {
    return resultList;
  }

  public QueryResumePoint getStartCursor() {
    return startCursor;
  }

  public QueryResumePoint getBackwardCursor() {
    return backwardCursor;
  }

  public boolean hasMoreResults() {
    return hasMoreResults;
  }

  public boolean hasPriorResults() {
    return hasPriorResults;
  }

}
