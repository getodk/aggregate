package org.opendatakit.aggregate.client.popups;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class HelpBalloon extends PopupPanel {

  private static final int SPACE = 1;

  private int offsetX;
  private int offsetY;
  private Widget widget;
  
  public HelpBalloon(Widget sender, final String text) {
    this(sender, 0, 0, text);
  }

  public HelpBalloon(Widget sender, int offsetX, int offsetY, final String text) {
    super(true);
    this.widget = sender;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    add(new HTML(text));
  }

  public void display() {
    // determine position
    int x = widget.getAbsoluteLeft();
    int y = widget.getAbsoluteTop();

    int halfScreenSizeX = Window.getClientWidth() / 2;
    int halfScreenSizeY = Window.getClientHeight() / 2;

    // decide where the popup should go based on the quadrant of the screen
    // the popup should always appear towards the center
    if (x >= halfScreenSizeX && y >= halfScreenSizeY) { // 4
      // put ballon in the upper left corner
      x -= (this.getOffsetWidth()/2 + SPACE);
      y -= (this.getOffsetHeight() + SPACE);
    } else if (x < halfScreenSizeX && y >= halfScreenSizeY) { // 3
      // put balloon in upper right corner
      x += (widget.getOffsetWidth()/2 + SPACE);
      y -= (this.getOffsetHeight() + SPACE);
    } else if (x >= halfScreenSizeX && y < halfScreenSizeY) { // 2
      // put balloon in bottom left corner
      x -= (this.getOffsetWidth()/2 + SPACE);
      y += (widget.getOffsetHeight() + SPACE);
    } else {
      // put balloon in bottom right corner
      x += (widget.getOffsetWidth()/2 + SPACE);
      y += (widget.getOffsetHeight() + SPACE);
    }

    setPopupPosition(x + offsetX, y + offsetY);
    show();
  }
  
}
