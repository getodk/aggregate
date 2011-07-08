package org.opendatakit.aggregate.client.table;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.BinaryPopup;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.widgets.RepeatViewButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class SubmissionTable extends FlexTable {
 
  private List<Column> tableHeaders;
  private List<SubmissionUI> tableSubmissions;
  
  public SubmissionTable() {
    tableHeaders = new ArrayList<Column>();
    tableSubmissions = new ArrayList<SubmissionUI>();
  }
  
  public List<Column> getHeaders() {
    return tableHeaders;
  }

  public List<SubmissionUI> getSubmissions() {
    return tableSubmissions;
  }

  public void update(SubmissionUISummary summary) {
    tableHeaders = summary.getHeaders();
    tableSubmissions = summary.getSubmissions();
    
    int headerIndex = 0;
    removeAllRows();

    for (Column column : tableHeaders) {
      setText(0, headerIndex++, column.getDisplayHeader());
    }
    
    getRowFormatter().addStyleName(0, "titleBar");
    addStyleName("dataTable");
    
    int i = 1;
    for (SubmissionUI row : tableSubmissions) {
      int j = 0;
      for (final String value : row.getValues()) {
        switch (tableHeaders.get(j).getUiDisplayType()) {
        case BINARY:
          if (value == null) {
            setText(i, j, UIConsts.EMPTY_STRING);
          } else {
            Image image = new Image(value + UIConsts.PREVIEW_SET);
            image.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                final PopupPanel popup = new BinaryPopup(value);
                popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                  @Override
                  public void setPosition(int offsetWidth, int offsetHeight) {
                    int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
                    int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
                    popup.setPopupPosition(left, top);
                  }
                });
                AggregateUI.getUI().getTimer().restartTimer();
              }
            });
            setWidget(i, j, image);
          }
          break;
        case REPEAT:
          if (value == null) {
            setText(i, j, UIConsts.EMPTY_STRING);
          } else {
            RepeatViewButton repeat = new RepeatViewButton(value);
            setWidget(i, j, repeat);
          }
          break;
        default:
          setText(i, j, value);
        }
        j++;
      }
      if (i % 2 == 0) {
        getRowFormatter().setStyleName(i, "evenTableRow");
      }
      i++;
    }
  }
}
