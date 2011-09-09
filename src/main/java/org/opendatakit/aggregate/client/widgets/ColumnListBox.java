package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.submission.Column;

public class ColumnListBox extends AbstractListBox {

  private final List<Column> headers;
  
  public ColumnListBox(String tooltipText, List<Column> headers, boolean multipleValueSelection) {
    super(tooltipText, multipleValueSelection);
    this.headers = headers;
    
    for (Column header : headers) {
      addItem(header.getDisplayHeader(), header.getColumnEncoding());
    }   
  }
  
  public ArrayList<Column> getColumnsForFilter() {
    ArrayList<Column> columnfilterheaders = new ArrayList<Column>();
    for (int i = getSelectedIndex(); i < getItemCount(); i++) {
      if (isItemSelected(i)) {
        String colname = getItemText(i);
        String colencode = getValue(i);
        Long colgpsIndex = null;
        for (Column column : headers) {
          if (colencode.compareTo(column.getColumnEncoding()) == 0) {
            colgpsIndex = column.getGeopointColumnCode();
            break;
          }
        }
        columnfilterheaders.add(new Column(colname, colencode, colgpsIndex));
      }
    }
    return columnfilterheaders;
  }
  
}
