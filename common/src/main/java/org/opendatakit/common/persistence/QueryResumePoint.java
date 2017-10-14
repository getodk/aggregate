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

import org.opendatakit.common.persistence.client.UIQueryResumePoint;
import org.opendatakit.common.utils.WebCursorUtils;

/**
 * Tracks the information needed to resume a query. Resumable queries must
 * specify at least one sort column. The first sort specified is the dominant
 * sort.
 * 
 * <p>
 * If not already defined, the persistence layer will add a sort against the PK
 * in the same direction (ascending/descending) as the dominant sort.
 * 
 * <p>
 * The resume point tracks the dominant sort column name, the last value of this
 * column from the prior query, and the PK of the last value returned by the
 * prior query.
 * 
 * <p>
 * The resume point can be used against the same Query or you can construct a
 * new query with inverted sort criteria (keeping the same sequence of sorted
 * columns but reversing the sort directions of each) and obtain the values
 * preceding the resume point.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryResumePoint {

  private final String attributeName;
  private final String value;
  private final String uriLastReturnedValue;
  private final boolean isForwardCursor;

  public QueryResumePoint(String attributeName, String value, String uriLast,
      boolean isForwardCursor) {
    this.attributeName = attributeName;
    this.value = value;
    this.uriLastReturnedValue = uriLast;
    this.isForwardCursor = isForwardCursor;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getValue() {
    return value;
  }

  public String getUriLastReturnedValue() {
    return uriLastReturnedValue;
  }

  public boolean isForwardCursor() {
    return isForwardCursor;
  }

  public UIQueryResumePoint transform() {
    UIQueryResumePoint qrp = new UIQueryResumePoint();
    qrp.setAttributeName(attributeName);
    qrp.setValue(value);
    qrp.setUriLastReturnedValue(uriLastReturnedValue);
    qrp.setIsForwardCursor(isForwardCursor);
    return qrp;
  }

  public String asWebsafeCursor() {
    return WebCursorUtils.formatCursorParameter(this);
  }

  public static final QueryResumePoint transform(UIQueryResumePoint qrp) {
    return new QueryResumePoint(qrp.getAttributeName(), qrp.getValue(),
        qrp.getUriLastReturnedValue(), qrp.getIsForwardCursor());
  }

  public static final QueryResumePoint fromWebsafeCursor(String cursor) {
    return WebCursorUtils.parseCursorParameter(cursor);
  }
}
