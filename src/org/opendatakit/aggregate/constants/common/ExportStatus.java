package org.opendatakit.aggregate.constants.common;

public enum ExportStatus {
 	GENERATION_IN_PROGRESS, // created or task is running
 	RETRY_IN_PROGRESS, // task is running
 	FAILED,    // task completed with failure; retry again later.
 	ABANDONED, // task completed with failure; no more retries should occur.
 	AVAILABLE; // task completed; results are available.
 	
 	public String toString() {
 		switch ( this ) {
 		case GENERATION_IN_PROGRESS:
 			return "Generation in progress";
 		case RETRY_IN_PROGRESS:
 			return "Retry in progress";
 		case FAILED:
 			return "Failure - will retry later";
 		case ABANDONED:
 			return "Failure - abandoned all retry attempts";
 		case AVAILABLE:
 			return "Dataset Available";
 		default:
 			throw new IllegalStateException("missing enum case");
 		}
 	}
 }