package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.preferences.Preferences;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.TextBox;

public class UpdateGMapsKeyButton extends AButtonBase implements ClickHandler {
  private TextBox mapsApiKey;

  public UpdateGMapsKeyButton(TextBox mapsApiKey) {
    super("<img src=\"images/green_right_arrow.png\" /> Update");
    this.mapsApiKey = mapsApiKey;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    Preferences.setGoogleMapsApiKey(mapsApiKey.getText());
  }
}