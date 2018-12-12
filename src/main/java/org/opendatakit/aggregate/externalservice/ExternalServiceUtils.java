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
package org.opendatakit.aggregate.externalservice;

import java.util.Date;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.submission.Submission;

/**
 * Consolidated state-transition logic for managing retries and backoff of
 * publishers.
 *
 * @author wbrunette@gmail.com
 */
public class ExternalServiceUtils {

  /**
   * If the FormServiceCursor is ACTIVE, transition to ACTIVE_PAUSE. If it is
   * ACTIVE_RETRY, transition to PAUSE. If it is ESTABLISHED, transition to
   * ABANDONED. Otherwise, retain its current (non-active) state.
   *
   * @param fsc
   */
  public static void pauseFscOperationalStatus(FormServiceCursor fsc) {
    OperationalStatus current = fsc.getOperationalStatus();
    if (current == OperationalStatus.ACTIVE) {
      fsc.setOperationalStatus(OperationalStatus.ACTIVE_PAUSE);
    } else if (current == OperationalStatus.ACTIVE_RETRY) {
      fsc.setOperationalStatus(OperationalStatus.PAUSED);
    } else if (current == OperationalStatus.ESTABLISHED) {
      fsc.setOperationalStatus(OperationalStatus.ABANDONED);
    }
  }

  /**
   * Update the FormServiceCursor to the MarkedAsCompleteDate and Uri of the
   * indicated submission; transition into the ACTIVE state. NOTE: the
   * FormServiceCursor may have been in an ACTIVE_RETRY state.
   *
   * @param fsc
   * @param submission
   * @param streaming
   */
  public static void updateFscToSuccessfulSubmissionDate(FormServiceCursor fsc,
                                                         Submission submission, boolean streaming) {
    // See QueryByDateRange
    // -- we are querying by the markedAsCompleteDate
    Date lastDateSent = submission.getMarkedAsCompleteDate();
    String lastKeySent = submission.getKey().getKey();

    if (streaming) {
      fsc.setLastStreamingCursorDate(lastDateSent);
      fsc.setLastStreamingKey(lastKeySent);
    } else {
      fsc.setLastUploadCursorDate(lastDateSent);
      fsc.setLastUploadKey(lastKeySent);
    }
    // submission got through so make sure we stay in active mode (or set back
    // to active mode)
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);

  }

}
