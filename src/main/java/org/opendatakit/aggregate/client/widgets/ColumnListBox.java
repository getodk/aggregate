package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.UIDisplayType;

public final class ColumnListBox extends AggregateListBox {

  private final List<Column> headers;

  public ColumnListBox(List<Column> headers, boolean multipleValueSelection,
      boolean onlyIncludeTextColumns, String tooltipText) {
    this(headers, multipleValueSelection, onlyIncludeTextColumns, tooltipText, null);
  }

  public ColumnListBox(List<Column> headers, boolean multipleValueSelection,
      boolean onlyIncludeTextColumns, String tooltipText, String helpBalloonTxt) {
    super(tooltipText, multipleValueSelection, helpBalloonTxt);
    this.headers = headers;

    for (Column header : headers) {
      if (!onlyIncludeTextColumns || header.getUiDisplayType().equals(UIDisplayType.TEXT)) {
        addItem(header.getDisplayHeader(), header.getColumnEncoding());
      }
    }
  }

  public ArrayList<Column> getSelectedColumns() {
    ArrayList<Column> columnfilterheaders = new ArrayList<Column>();
    for (int i = getSelectedIndex(); i < getItemCount(); i++) {
      if (isItemSelected(i)) {
        String colname = getItemText(i);
        String colencode = getValue(i);
        Long colgpsIndex = null;
        for (Column column : headers) {
          if (colname.equals(column.getDisplayHeader())
              && colencode.equals(column.getColumnEncoding())) {
            colgpsIndex = column.getGeopointColumnCode();
            break;
          }
        }
        columnfilterheaders.add(new Column(colname, colencode, colgpsIndex));
      }
    }
    return columnfilterheaders;
  }

  public Column getSelectedColumn() {
    ArrayList<Column> columns = getSelectedColumns();
    if (columns.size() > 0) {
      return columns.get(0);
    } else {
      return null;
    }
  }

}
