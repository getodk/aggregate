package org.opendatakit.aggregate.databaseRepair;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendatakit.aggregate.client.SecureGWT;

class FilesetReviewPanel {
  private List<CheckBox> markForDeletionCheckBoxes;
  private Map<FilesetReport.Row, Boolean> rowsToDelete;
  private final Button saveButton;
  private final Button cancelButton;

  FilesetReviewPanel(FilesetReport fileset, final DatabaseRepairSubTab parent) {
    parent.reviewPanel.clear();

    saveButton = buildSaveButton(parent);
    cancelButton = buildCancelButton(parent);

    parent.reviewPanel.add(new HTML("<h4>Filesets</h4>"));

    FlexTable table = buildTable();
    fillTable(table, fileset);

    parent.reviewPanel.add(table);
    parent.reviewPanel.add(saveButton);
    parent.reviewPanel.add(cancelButton);
    parent.reviewPanel.setVisible(true);
    parent.reviewPanelTitle.setVisible(true);
  }

  private void fillTable(FlexTable table, FilesetReport fileset) {
    List<FilesetReport.Row> rows = fileset.getRows();
    rowsToDelete = new HashMap<>(rows.size());
    markForDeletionCheckBoxes = new ArrayList<>(rows.size());
    for (int i = 0, max = rows.size(); i < max; i++) {
      final FilesetReport.Row row = rows.get(i);
      rowsToDelete.put(row, false);
      table.setText(i + 1, 0, row.getURI());
      table.setWidget(i + 1, 1, buildIsDownloadAllowedButton(row));
      table.setText(i + 1, 2, buildLastUpdatedText(row));
      table.setWidget(i + 1, 3, buildMarkForDeletionButton(row, fileset.hasDupes()));
    }
  }

  private String buildLastUpdatedText(FilesetReport.Row row) {
    return row.getLastUpdateUser() != null
        ? row.getLastUpdateDate() + " by " + row.getLastUpdateUser()
        : row.getLastUpdateDate();
  }

  private CheckBox buildIsDownloadAllowedButton(final FilesetReport.Row row) {
    CheckBox isDownloadAllowed = new CheckBox();

    if (row.nullIsDownloadAllowed())
      ((InputElement) isDownloadAllowed.getElement().getChild(0)).setPropertyBoolean("indeterminate", true);
    else
      isDownloadAllowed.setValue(row.isDownloadAllowed(), false);

    isDownloadAllowed.setEnabled(false);

    return isDownloadAllowed;
  }

  private CheckBox buildMarkForDeletionButton(final FilesetReport.Row row, boolean hasDupes) {
    CheckBox markForDeletion = new CheckBox();
    markForDeletionCheckBoxes.add(markForDeletion);
    if (!hasDupes)
      markForDeletion.setEnabled(false);
    markForDeletion.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        updateMarkForDeletionCheckBoxes();
        rowsToDelete.put(row, event.getValue());
        refreshSaveButton();
      }
    });
    return markForDeletion;
  }

  private FlexTable buildTable() {
    FlexTable table = new FlexTable();
    table.addStyleName("dataTable");
    table.setText(0, 0, "URI");
    table.setText(0, 1, "Is download allowed?");
    table.setText(0, 2, "Last update");
    table.setText(0, 3, "Delete on save");
    table.getRowFormatter().addStyleName(0, "titleBar");
    return table;
  }

  private Button buildCancelButton(final DatabaseRepairSubTab parent) {
    Button cancelButton = new Button();
    cancelButton.setText("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        parent.reviewPanel.clear();
        parent.reviewPanel.setVisible(false);
        parent.reviewPanelTitle.setVisible(false);
      }
    });
    return cancelButton;
  }

  private Button buildSaveButton(final DatabaseRepairSubTab parent) {
    final Button saveButton = new Button();
    saveButton.setText("Save");
    saveButton.setEnabled(false);

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ConfirmPopup popup = new ConfirmPopup(
            "Execute this fix?",
            "Check the downloadable status of the form after applying this check",
            new Runnable() {
              @Override
              public void run() {
                SecureGWT.getDatabaseRepairService().fixFilesets(getTheRow(), new AsyncCallback<Void>() {
                  @Override
                  public void onFailure(Throwable caught) {

                  }

                  @Override
                  public void onSuccess(Void result) {
                    parent.update();
                  }
                });
              }
            }
        );
        popup.setPopupPositionAndShow(popup.getPositionCallBack());
      }
    });
    return saveButton;
  }

  private FilesetReport.Row getTheRow() {
    List<FilesetReport.Row> rowsNotMarkedForDeletion = new ArrayList<>();
    for (Map.Entry<FilesetReport.Row, Boolean> entry : rowsToDelete.entrySet())
      if (!entry.getValue())
        rowsNotMarkedForDeletion.add(entry.getKey());
    return rowsNotMarkedForDeletion.size() == 1 ? rowsNotMarkedForDeletion.get(0) : null;
  }

  private void updateMarkForDeletionCheckBoxes() {
    int count = 0;
    for (CheckBox cb : markForDeletionCheckBoxes)
      if (!cb.getValue())
        count++;
    for (CheckBox cb : markForDeletionCheckBoxes)
      cb.setEnabled(count > 1 || cb.getValue());
  }

  private void refreshSaveButton() {
    FilesetReport.Row theRow = getTheRow();
    saveButton.setEnabled(theRow != null && theRow.isOk());
  }
}
