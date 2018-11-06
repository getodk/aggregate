package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

public enum GmePhotoHostType implements Serializable {
  GOOGLE_DRIVE("Google Drive"),
  AGGREGATE("ODK Aggregate");

  private String serviceName;

  private GmePhotoHostType() {
    // GWT
  }

  private GmePhotoHostType(String name) {
    serviceName = name;
  }

  public String toString() {
    return serviceName;
  }

}
