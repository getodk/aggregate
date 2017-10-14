package org.opendatakit.aggregate.odktables.api;


public interface SynchronizerStatus {
  /**
   * Status of this action.
   *
   * @param text
   * @param progressPercentage
   *          0..100
   * @param indeterminateProgress
   *          true if progressGrains is N/A
   */
  void updateNotification(SyncProgressState state, int textResource, Object[] formatArgVals,
      Double progressPercentage, boolean indeterminateProgress);
}
