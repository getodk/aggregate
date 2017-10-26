package org.opendatakit.aggregate.odktables.api;

import java.util.ArrayList;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;

/**
 * Tracks the data values for the local and server row so that we
 * can construct the appropriate database insert/update statements.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
final class SyncRowDataChanges {
  final SyncRow serverRow;
  final SyncRow localRow;
  boolean isRestPendingFiles;
  final int localRowConflictType;

  SyncRowDataChanges(SyncRow serverRow, SyncRow localRow, boolean isRestPendingFiles) {
    this.serverRow = serverRow;
    this.localRow = localRow;
    this.isRestPendingFiles = isRestPendingFiles;
    this.localRowConflictType = -1;
  }

  SyncRowDataChanges(SyncRow serverRow, SyncRow localRow, boolean isRestPendingFiles,
      int localRowConflictType) {
    this.serverRow = serverRow;
    this.localRow = localRow;
    this.isRestPendingFiles = isRestPendingFiles;
    this.localRowConflictType = localRowConflictType;
  }

  boolean identicalValuesExceptRowETagAndFilterScope(ArrayList<ColumnDefinition> orderedDefns) {
    if ((serverRow.getSavepointTimestamp() == null) ? (localRow.getSavepointTimestamp() != null)
        : !serverRow.getSavepointTimestamp().equals(localRow.getSavepointTimestamp())) {
      return false;
    }
    if ((serverRow.getSavepointCreator() == null) ? (localRow.getSavepointCreator() != null)
        : !serverRow.getSavepointCreator().equals(localRow.getSavepointCreator())) {
      return false;
    }
    if ((serverRow.getFormId() == null) ? (localRow.getFormId() != null) : !serverRow.getFormId()
        .equals(localRow.getFormId())) {
      return false;
    }
    if ((serverRow.getLocale() == null) ? (localRow.getLocale() != null) : !serverRow.getLocale()
        .equals(localRow.getLocale())) {
      return false;
    }
    if ((serverRow.getRowId() == null) ? (localRow.getRowId() != null) : !serverRow.getRowId()
        .equals(localRow.getRowId())) {
      return false;
    }
    if ((serverRow.getSavepointType() == null) ? (localRow.getSavepointType() != null)
        : !serverRow.getSavepointType().equals(localRow.getSavepointType())) {
      return false;
    }
    ArrayList<DataKeyValue> localValues = localRow.getValues();
    ArrayList<DataKeyValue> serverValues = serverRow.getValues();

    if (localValues == null && serverValues == null) {
      return true;
    } else if (localValues == null || serverValues == null) {
      return false;
    }

    if (localValues.size() != serverValues.size()) {
      return false;
    }

    for (int i = 0; i < localValues.size(); ++i) {
      DataKeyValue local = localValues.get(i);
      DataKeyValue server = serverValues.get(i);
      if (!local.column.equals(server.column)) {
        return false;
      }
      if (local.value == null && server.value == null) {
        continue;
      } else if (local.value == null || server.value == null) {
        return false;
      } else if (local.value.equals(server.value)) {
        continue;
      }

      // NOT textually identical.
      //
      // Everything must be textually identical except possibly number fields
      // which may have rounding due to different database implementations,
      // data representations, and marshaling libraries.
      //
      ColumnDefinition cd = ColumnDefinition.find(orderedDefns, local.column);
      if (cd.getType().getDataType() == ElementDataType.number) {
        // !!Important!! Double.valueOf(str) handles NaN and +/-Infinity
        Double localNumber = Double.valueOf(local.value);
        Double serverNumber = Double.valueOf(server.value);

        if (localNumber.equals(serverNumber)) {
          // simple case -- trailing zeros or string representation mix-up
          //
          continue;
        } else if (localNumber.isInfinite() && serverNumber.isInfinite()) {
          // if they are both plus or both minus infinity, we have a match
          if (Math.signum(localNumber) == Math.signum(serverNumber)) {
            continue;
          } else {
            return false;
          }
        } else if (localNumber.isNaN() || localNumber.isInfinite() || serverNumber.isNaN()
            || serverNumber.isInfinite()) {
          // one or the other is special1
          return false;
        } else {
          double localDbl = localNumber;
          double serverDbl = serverNumber;
          if (localDbl == serverDbl) {
            continue;
          }
          // OK. We have two values like 9.80 and 9.8
          // consider them equal if they are adjacent to each other.
          double localNear = localDbl;
          int idist = 0;
          int idistMax = 128;
          for (idist = 0; idist < idistMax; ++idist) {
            localNear = Math.nextAfter(localNear, serverDbl);
            if (localNear == serverDbl) {
              break;
            }
          }
          if (idist < idistMax) {
            continue;
          }
          return false;
        }
      } else {
        // textual identity is required!
        return false;
      }
    }
    if (!localValues.containsAll(serverValues)) {
      return false;
    }
    return true;
  }

  boolean identicalValues(ArrayList<ColumnDefinition> orderedDefns) {
    if ((serverRow.getFilterScope() == null) ? (localRow.getFilterScope() != null) : !serverRow
        .getFilterScope().equals(localRow.getFilterScope())) {
      return false;
    }
    if ((serverRow.getRowETag() == null) ? (localRow.getRowETag() != null) : !serverRow
        .getRowETag().equals(localRow.getRowETag())) {
      return false;
    }
    return identicalValuesExceptRowETagAndFilterScope(orderedDefns);
  }
}