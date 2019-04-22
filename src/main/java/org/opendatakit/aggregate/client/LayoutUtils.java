package org.opendatakit.aggregate.client;

import static org.opendatakit.aggregate.buildconfig.BuildConfig.VERSION;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.UIObject;

public class LayoutUtils {
  static HTML buildVersionNote(UIObject parent) {
    String shortVersion = VERSION.contains("-") ? VERSION.substring(0, VERSION.indexOf("-")) : VERSION;
    HTML html = new HTML("<small>" + shortVersion + " - Checking versions...</small>");
    Style style = html.getElement().getStyle();
    style.setProperty("position", "fixed");
    style.setProperty("bottom", "0");
    style.setProperty("left", "0");
    style.setProperty("padding", "5px 10px");
    style.setProperty("backgroundColor", "rgba(255,255,255,.9)");

    Style parentStyle = parent.getElement().getStyle();
    parentStyle.setProperty("paddingBottom", "40px");

    SecureGWT.getPreferenceService().getVersioNote(new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        html.setHTML("<small>" + shortVersion + " - Version check failed</small>");
      }

      @Override
      public void onSuccess(String versionNote) {
        html.setHTML("<small>" + versionNote + "</small>");
      }
    });

    return html;
  }
}
