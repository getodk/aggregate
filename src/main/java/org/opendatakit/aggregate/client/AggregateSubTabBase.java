package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.constants.common.HelpSliderConsts;

import com.google.gwt.user.client.ui.FlowPanel;

public abstract class AggregateSubTabBase extends FlowPanel implements SubTabInterface {

  public abstract boolean canLeave();

  public abstract void update();
 
  public HelpSliderConsts[] getHelpSliderContent() {
    return null;
  }
}
