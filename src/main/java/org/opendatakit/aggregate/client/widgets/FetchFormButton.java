package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.table.FormNFilterSelectionTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class FetchFormButton extends AButtonBase implements ClickHandler {
  private FormNFilterSelectionTable formNFilter;

  public FetchFormButton(FormNFilterSelectionTable formNFilter) {
    super("Fetch Form with Filter");
    this.formNFilter = formNFilter;
    
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    formNFilter.fetchClicked();
    AggregateUI.getUI().getTimer().refreshNow();
  }

}
