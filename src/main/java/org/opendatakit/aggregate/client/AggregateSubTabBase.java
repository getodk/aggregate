package org.opendatakit.aggregate.client;

import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class AggregateSubTabBase extends VerticalPanel implements SubTabInterface {

  public abstract boolean canLeave();

  public abstract void update();

}
