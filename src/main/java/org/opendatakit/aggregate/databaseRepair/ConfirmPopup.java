package org.opendatakit.aggregate.databaseRepair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.popups.AbstractPopupBase;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public class ConfirmPopup extends AbstractPopupBase {
  public ConfirmPopup(String title, final Runnable onOk) {
    super();
    render(title, buildOkButton(onOk), buildCancelButton());
  }

  private void render(String title, Button okButton, Button cancelButton) {
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML(title));
    layout.setWidget(0, 1, okButton);
    layout.setWidget(0, 2, cancelButton);
    setWidget(layout);
  }

  private Button buildCancelButton() {
    return new ClosePopupButton(this);
  }

  private Button buildOkButton(final Runnable onOk) {
    AggregateButton button = new AggregateButton("<img src=\"images/green_right_arrow.png\"/> OK", "OK", "OK");
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        onOk.run();
      }
    });
    return button;
  }
}
