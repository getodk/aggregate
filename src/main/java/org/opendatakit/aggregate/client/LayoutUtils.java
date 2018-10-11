package org.opendatakit.aggregate.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.buildconfig.BuildConfig;

public class LayoutUtils {
  private static String latestVersion;

  static native String getLatestVersion() /*-{
    var req = new XMLHttpRequest();
    req.open('GET', 'https://api.github.com/repos/opendatakit/aggregate/releases/latest', false);
    req.send(null);
    if (req.readyState === 4 && req.status === 200)
      return JSON.parse(req.responseText).tag_name;
  }-*/;

  static HTML buildVersionNote() {
    if (latestVersion == null)
      latestVersion = getLatestVersion();
    boolean needsUpdate = !BuildConfig.VERSION.startsWith(latestVersion);
    HTML versionNote = new HTML("" +
        "<small>ODK Aggregate " + BuildConfig.VERSION + "<br/>" +
        (needsUpdate
            ? "Update available: <a href=\"https://github.com/opendatakit/aggregate/releases/latest\" target=\"_blank\">" + latestVersion + "</a>"
            : "You're up to date"
        ) +
        "</small>");
    Style style = versionNote.getElement().getStyle();
    style.setProperty("position", "fixed");
    style.setProperty("bottom", "10px");
    style.setProperty("left", "10px");
    return versionNote;
  }
}
