/*
 * Copyright (C) 2012-2013 University of Washington
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rowResource")
public class RowOutcome extends Row {

  /**
   * Possible values:
   * <ul>
   * <li>UNKNOWN -- initial default value</li>
   * <li>SUCCESS -- rowETag, dataETagAtModification, filterScope updated</li>
   * <li>DENIED -- permission denied -- just the rowId is returned</li>
   * <li>IN_CONFLICT -- server record is returned (in full)</li>
   * <li>FAILED -- anonymous insert conflict (impossible?) or
   *               delete of non-existent row -- just rowId is returned</li>
   * </ul>
   */
  public enum OutcomeType {
    UNKNOWN, SUCCESS, DENIED, IN_CONFLICT, FAILED
  }

  /**
   * The URL that returns this RowResource.
   */
  @JsonProperty(required = false)
  private String selfUri;

  @JsonProperty(required = false)
  private OutcomeType outcome = OutcomeType.UNKNOWN;

  @SuppressWarnings("unused")
  private RowOutcome() {
  }

  public RowOutcome(Row row) {
    super(row);
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public OutcomeType getOutcome() {
    return this.outcome;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  public void setOutcome(final OutcomeType outcome) {
    this.outcome = outcome;
    if (outcome == OutcomeType.SUCCESS 
        || outcome == OutcomeType.DENIED
        || outcome == OutcomeType.FAILED) {
      // for these outcomes, we only need to preserve
      // the rowID, rowETag, deleted, dataETagAtModification,
      // and filterScope fields.
      //
      // Clear the others to reduce bandwidth needs for
      // the response packets.
      this.setLastUpdateUser(null);
      this.setCreateUser(null);
      this.setFormId(null);
      this.setLocale(null);
      this.setSavepointType(null);
      this.setSavepointTimestamp(null);
      this.setSavepointCreator(null);
      this.setValues(null);

      // additionally...
      if (outcome == OutcomeType.DENIED || outcome == OutcomeType.FAILED) {
        // for these outcomes, preserve only the 
        // rowId and deleted fields.

        this.setRowETag(null);
        this.setDataETagAtModification(null);
        this.setRowFilterScope(null);
      }
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this)
      return true;
    if (!(o instanceof RowOutcome))
      return false;
    final RowOutcome other = (RowOutcome) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (this.getSelfUri() == null ? other.getSelfUri() != null : !this.getSelfUri().equals(
        (java.lang.Object) other.getSelfUri()))
      return false;
    if (this.getOutcome() == null ? other.getOutcome() != null : !this.getOutcome().equals(
        other.getOutcome()))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof RowOutcome;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    result = result * PRIME + (this.getOutcome() == null ? 0 : this.getOutcome().hashCode());
    return result;
  }

  public String toString() {
    return "RowResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", outcome=" + this.getOutcome().name() + ")";
  }

}