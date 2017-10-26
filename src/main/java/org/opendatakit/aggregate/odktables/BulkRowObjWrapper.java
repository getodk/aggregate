package org.opendatakit.aggregate.odktables;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome.OutcomeType;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.PersistenceUtils;

class BulkRowObjWrapper {
  private Row row;
  private Entity entity;
  private RowOutcome outcome;
  
  private RowFilterScope rowFilterScope;
  
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
    rowFilterScope = row.getRowFilterScope();
    if (rowFilterScope == null) {
      nullIncomingScope = true;
      rowFilterScope = RowFilterScope.EMPTY_ROW_FILTER;
      row.setRowFilterScope(rowFilterScope);
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
        row.setRowFilterScope(null);
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

  public RowFilterScope getRowFilterScope() {
    return rowFilterScope;
  }

  /**
   * Should only be called if the request didn't specify a rowFilterScope
   * 
   * @param rowFilterScope
   */
  public void setRowFilterScope(RowFilterScope rowFilterScope) {
    if ( !nullIncomingScope ) {
      throw new IllegalStateException("Should not be called");
    }
    this.rowFilterScope = rowFilterScope;
    row.setRowFilterScope(rowFilterScope);
  }

  public boolean hasNewRowId() {
    return newRowId;
  }

  public boolean hasNullIncomingScope() {
    return nullIncomingScope;
  }
  

  
}
