package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.widgets.UpdateGMapsKeyButton;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PreferencesSubTab extends VerticalPanel implements SubTabInterface{

  // Preferences tab
  private static final String GOOGLE_MAPS_API_KEY_LABEL = "<h2>Google Maps API Key</h2> To obtain a key signup at <a href=\"http://code.google.com/apis/maps/signup.html\"> Google Maps </a>";
  private TextBox mapsApiKey = new TextBox();
  
  public PreferencesSubTab() {
    HTML labelMapsKey = new HTML(GOOGLE_MAPS_API_KEY_LABEL);
    mapsApiKey.setText(Preferences.getGoogleMapsApiKey());

    add(labelMapsKey);
    add(mapsApiKey);
    add(new UpdateGMapsKeyButton(mapsApiKey));
  }

  public void update() {
    mapsApiKey.setText(Preferences.getGoogleMapsApiKey());
  }
  
}
