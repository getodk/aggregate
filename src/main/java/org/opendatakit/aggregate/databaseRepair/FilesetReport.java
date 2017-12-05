package org.opendatakit.aggregate.databaseRepair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilesetReport implements Serializable {
  private static final String NULLS = "Nulls";
  private static final String MISSING = "Missing";
  private static final String DUPES = "Dupes";
  private List<FilesetReport.Row> rows = new ArrayList<>();

  private FilesetReport() {
  }

  public static FilesetReport empty() {
    return new FilesetReport();
  }

  boolean isOk() {
    return rows.size() == 1 && rows.get(0).isOk();
  }

  boolean canBeFixed() {
    return rows.size() == 1 && rows.get(0).canBeFixed() || rows.size() > 1;
  }

  boolean canNotBeFixed() {
    return rows.size() == 0;
  }

  private boolean isCorrupted() {
    return rows.size() == 0 || (rows.size() == 1 && rows.get(0).isCorrupted()) || rows.size() > 1;
  }

  String getCorruptionCause() {
    if (!isCorrupted())
      return null;
    if (rows.size() == 0)
      return MISSING;
    if (rows.size() == 1)
      return rows.get(0).getCorruptionCause();
    return DUPES;
  }

  public FilesetReport add(String uri, String parentUri, Boolean isEncryptedForm, Boolean isDownloadAllowed) {
    rows.add(new Row(uri, parentUri, isEncryptedForm, isDownloadAllowed));
    return this;
  }

  public List<Row> getRows() {
    return rows;
  }

  boolean hasDupes() {
    return getCorruptionCause().equals(DUPES);
  }

  public static class Row implements Serializable {
    private String uri;
    private String parentUri;
    private Boolean isEncryptedForm;
    private Boolean isDownloadAllowed;

    private Row() {

    }

    public Row(String uri, String parentUri, Boolean isEncryptedForm, Boolean isDownloadAllowed) {
      this.uri = uri;
      this.parentUri = parentUri;
      this.isEncryptedForm = isEncryptedForm;
      this.isDownloadAllowed = isDownloadAllowed;
    }

    boolean isOk() {
      return isEncryptedForm != null && isDownloadAllowed != null;
    }

    boolean isCorrupted() {
      return isEncryptedForm == null || isDownloadAllowed == null;
    }

    boolean canBeFixed() {
      // If it's ok, it can't be fixed
      return isCorrupted();
    }

    String getCorruptionCause() {
      if (isEncryptedForm == null || isDownloadAllowed == null)
        return NULLS;
      return null;
    }

    public String getURI() {
      return uri;
    }

    String getParentUri() {
      return parentUri;
    }

    boolean nullIsEncryptedForm() {
      return isEncryptedForm == null;
    }

    public Boolean isEncryptedForm() {
      return isEncryptedForm;
    }

    public void setIsEncryptedForm(boolean value) {
      isEncryptedForm = value;
    }

    boolean nullIsDownloadAllowed() {
      return isDownloadAllowed == null;
    }

    Boolean isDownloadAllowed() {
      return isDownloadAllowed;
    }

    void setIsDownloadAllowed(boolean value) {
      isDownloadAllowed = value;
    }
  }

}
