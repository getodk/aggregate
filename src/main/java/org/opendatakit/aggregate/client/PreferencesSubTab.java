package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.preferences.Preferences;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PreferencesSubTab extends VerticalPanel implements SubTabInterface{

  // Preferences tab
  private static final String GOOGLE_MAPS_API_KEY_LABEL = "<h2>Google Maps API Key</h2> To obtain a key signup at <a href=\"http://code.google.com/apis/maps/signup.html\"> Google Maps </a>";
  private TextBox mapsApiKey = new TextBox();
  
  private AggregateUI baseUI;
  
  public PreferencesSubTab(AggregateUI baseUI) {
    this.baseUI = baseUI;
  }
  
  public VerticalPanel setupPreferencesPanel() {
    HTML labelMapsKey = new HTML(GOOGLE_MAPS_API_KEY_LABEL);
    String key = Preferences.getGoogleMapsApiKey();
    mapsApiKey.setText(key);

    Button updateMapsApiKeyButton = new Button("Update");
    updateMapsApiKeyButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        baseUI.clearError();
        Preferences.setGoogleMapsApiKey(mapsApiKey.getText());
        baseUI.getTimer().restartTimer();
      }

    });

    VerticalPanel preferencesPanel = new VerticalPanel();
    preferencesPanel.add(labelMapsKey);
    preferencesPanel.add(mapsApiKey);
    preferencesPanel.add(updateMapsApiKeyButton);
    return preferencesPanel;
  }

  public void update() {
    mapsApiKey.setText(Preferences.getGoogleMapsApiKey());
  }
  
}
