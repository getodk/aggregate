package org.opendatakit.aggregate.odktables;

import org.opendatakit.common.persistence.ITaskLockType;

public enum ODKTablesTaskLockType implements ITaskLockType {
  UPDATE_DATA(30000), UPDATE_PROPERTIES(30000);

  private long timeout;

  private ODKTablesTaskLockType(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public long getLockExpirationTimeout() {
    return timeout;
  }

  @Override
  public String getName() {
    return name();
  }
}