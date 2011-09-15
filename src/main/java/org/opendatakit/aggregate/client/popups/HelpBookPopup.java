package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.BookHelpConsts;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelpBookPopup extends PopupPanel {
  private VerticalPanel panel;

  public HelpBookPopup() {
    super(false);
    panel = new VerticalPanel();

    // populate the panel
    panel.add(new ClosePopupButton(this));

    BookHelpConsts[] consts = BookHelpConsts.values();

    for (int i = 0; i < consts.length; i++) {
      panel.add(new HTML("<h2>" + consts[i].getTitle() + "</h2>"));
      if (consts[i].getVideoUrl() != null) {
        Anchor vid = new Anchor(consts[i].getTitle() + " Video Assistance", consts[i].getVideoUrl());
        vid.addStyleName("helpvid");
        panel.add(vid);
      }
      panel.add(new HTML(consts[i].getConcept() + "<br><br>"));
      panel.add(new HTML(consts[i].getProcedures() + "<br><br>"));
    }

    ScrollPanel scroll = new ScrollPanel(panel);
    scroll.setPixelSize((Window.getClientWidth() * 3 / 4), (Window.getClientHeight() * 3 / 4));
    setWidget(scroll);
    center();
  }
}
