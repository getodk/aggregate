package org.opendatakit.aggregate.databaseRepair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilesetReport implements Serializable {
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

  public FilesetReport add(String uri, String parentUri, Boolean isDownloadAllowed, String lastUpdateDate, String lastUpdateUser) {
    rows.add(new Row(uri, parentUri, isDownloadAllowed, lastUpdateDate, lastUpdateUser));
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
    private Boolean isDownloadAllowed;
    private String lastUpdateDate;
    private String lastUpdateUser;

    private Row() {

    }

    public Row(String uri, String parentUri, Boolean isDownloadAllowed, String lastUpdateDate, String lastUpdateUser) {
      this.uri = uri;
      this.parentUri = parentUri;
      this.isDownloadAllowed = isDownloadAllowed;
      this.lastUpdateDate = lastUpdateDate;
      this.lastUpdateUser = lastUpdateUser;
    }

    boolean isOk() {
      return true;
    }

    boolean isCorrupted() {
      return false;
    }

    boolean canBeFixed() {
      // If it's ok, it can't be fixed
      return isCorrupted();
    }

    String getCorruptionCause() {
      return null;
    }

    public String getURI() {
      return uri;
    }

    String getParentUri() {
      return parentUri;
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

    String getLastUpdateDate() {
      return lastUpdateDate;
    }

    String getLastUpdateUser() {
      return lastUpdateUser;
    }
  }

}
