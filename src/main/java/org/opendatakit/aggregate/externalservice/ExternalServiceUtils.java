package org.opendatakit.aggregate.externalservice;

import java.util.Date;

import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.submission.Submission;

public class ExternalServiceUtils {
  
  public static void pauseFscOperationalStatus(FormServiceCursor fsc) {
    OperationalStatus current = fsc.getOperationalStatus();
    if(current == OperationalStatus.ACTIVE) {
      fsc.setOperationalStatus(OperationalStatus.ACTIVE_PAUSE);
    } else {
      fsc.setOperationalStatus(OperationalStatus.PAUSED);
    }
  }

  public static void updateFscToSuccessfulSubmissionDate(FormServiceCursor fsc, Submission submission, boolean streaming) {
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
    // submission got through so make sure we stay in active mode (or set back to active mode)
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);

  }

}
