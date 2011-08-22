package org.opendatakit.aggregate.odktables.command;

import org.opendatakit.common.persistence.ITaskLockType;

public enum ODKTablesTaskLockType implements ITaskLockType
{
  UPDATE_MODIFICATION_NUMBER(120000);
  
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