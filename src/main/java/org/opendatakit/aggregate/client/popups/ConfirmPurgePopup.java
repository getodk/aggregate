package org.opendatakit.aggregate.client.popups;

import java.util.Date;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ConfirmPurgeButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmPurgePopup extends PopupPanel{

  public ConfirmPurgePopup(ExternServSummary e, Date earliest, String bodyText) {
    super(false);   
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML(bodyText));
    layout.setWidget(0, 1, new ConfirmPurgeButton(e.getUri(), earliest, this));
    layout.setWidget(0, 2, new ClosePopupButton(this));
    setWidget(layout);
  }
}