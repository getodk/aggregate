package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.Date;

import org.opendatakit.aggregate.constants.common.ExportStatus;
import org.opendatakit.aggregate.constants.common.ExportType;

public class ExportSummary implements Serializable {
  /**
   * Serialization Version ID
   */
  private static final long serialVersionUID = -8309784116473729045L;
  
  private ExportType fileType;
  private Date timeRequested;
  private ExportStatus status;
  private Date timeLastAction;
  private Date timeCompleted;
  private String resultFile;

  public ExportType getFileType() {
    return fileType;
  }

  public Date getTimeRequested() {
    return timeRequested;
  }

  public ExportStatus getStatus() {
    return status;
  }

  public Date getTimeLastAction() {
    return timeLastAction;
  }

  public Date getTimeCompleted() {
    return timeCompleted;
  }

  public String getResultFile() {
    return resultFile;
  }

  public void setFileType(ExportType fileType) {
    this.fileType = fileType;
  }

  public void setTimeRequested(Date timeRequested) {
    this.timeRequested = timeRequested;
  }

  public void setStatus(ExportStatus status) {
    this.status = status;
  }

  public void setTimeLastAction(Date timeLastAction) {
    this.timeLastAction = timeLastAction;
  }

  public void setTimeCompleted(Date timeCompleted) {
    this.timeCompleted = timeCompleted;
  }

  public void setResultFile(String resultFile) {
    this.resultFile = resultFile;
  }

  
  
}
