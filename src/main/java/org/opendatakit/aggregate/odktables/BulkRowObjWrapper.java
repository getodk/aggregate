package org.opendatakit.aggregate.odktables;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome.OutcomeType;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.PersistenceUtils;

class BulkRowObjWrapper {
  private Row row;
  private Entity entity;
  private RowOutcome outcome;
  
  private Scope filterScope;
  
  private boolean newRowId;
  private boolean nullIncomingScope;
  
  BulkRowObjWrapper(Row inputRow) {
    outcome = null;
    row = inputRow;
    newRowId = false;
    if (row.getRowId() == null) {
      newRowId = true;
      row.setRowId(PersistenceUtils.newUri());
    }
    nullIncomingScope = false;
    filterScope = row.getFilterScope();
    if (filterScope == null) {
      nullIncomingScope = true;
      filterScope = Scope.EMPTY_SCOPE;
      row.setFilterScope(filterScope);
    }
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public RowOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(OutcomeType outcomeType) {
    if (outcomeType != OutcomeType.SUCCESS) {
      if (newRowId) {
        // restore it to null.
        row.setRowId(null);
      }
      if (nullIncomingScope) {
        // restore to null.
        row.setFilterScope(null);
      }
    }
    RowOutcome newOutcome = new RowOutcome(row);
    newOutcome.setOutcome(outcomeType);
    this.outcome = newOutcome;
  }

  public void setOutcome(Row revised, OutcomeType outcomeType) {
    RowOutcome newOutcome = new RowOutcome(revised);
    newOutcome.setOutcome(outcomeType);
    this.outcome = newOutcome;
  }

  public boolean outcomeAlreadySet() {
    return (outcome != null);
  }
  
  public Row getRow() {
    return row;
  }

  
  public String getRowId() {
    return row.getRowId();
  }

  public Scope getFilterScope() {
    return filterScope;
  }

  public boolean hasNewRowId() {
    return newRowId;
  }

  public boolean hasNullIncomingScope() {
    return nullIncomingScope;
  }
  

  
}
