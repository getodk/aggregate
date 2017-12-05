package org.opendatakit.aggregate.databaseRepair;

import java.io.Serializable;

public class FormCorruptionReport implements Serializable {
  private String uri;
  private String formId;
  private FilesetReport fileset;

  public FormCorruptionReport() {
  }

  public FormCorruptionReport(String uri, String formId, FilesetReport fileset) {
    this.uri = uri;
    this.formId = formId;
    this.fileset = fileset;
  }

  public String getUri() {
    return uri;
  }

  public String getFormId() {
    return formId;
  }

  public FilesetReport getFileset() {
    return fileset;
  }

  public boolean isOk() {
    return fileset.isOk();
  }

  public boolean hasToBeDeleted() {
    return fileset.canNotBeFixed();
  }
}
