package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.HelpBalloon;

public class BasicButton extends AbstractButtonBase {

  public BasicButton(String buttonText, String tooltipText, String helpBalloonText) {
    this(buttonText, tooltipText);
    helpBalloon = new HelpBalloon(this, helpBalloonText);
 }

  public BasicButton(String buttonText, String tooltipText) {
    super(buttonText, tooltipText);
 }
  
}
