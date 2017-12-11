package org.opendatakit.aggregate.databaseRepair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.opendatakit.aggregate.client.popups.AbstractPopupBase;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public class ConfirmPopup extends AbstractPopupBase {
  public ConfirmPopup(String title, String message, final Runnable onOk) {
    super();
    render(title, message, buildOkButton(onOk), buildCancelButton());
  }

  private void render(String title, String message, Button okButton, Button cancelButton) {
    HTMLPanel panel = new HTMLPanel("div", message);
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML(title));
    layout.setWidget(0, 1, okButton);
    layout.setWidget(0, 2, cancelButton);
    panel.add(layout);
    setWidget(panel);
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
