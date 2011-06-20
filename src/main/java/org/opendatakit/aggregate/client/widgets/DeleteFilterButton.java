package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.Filter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class DeleteFilterButton extends AButtonBase implements ClickHandler {

  private FilterSubTab parentSubTab;
  private Filter remove;

  public DeleteFilterButton(Filter remove, FilterSubTab parentSubTab) {
    super("<img src=\"images/red_x.png\" />");
    this.remove = remove;
    this.parentSubTab = parentSubTab;
    addStyleDependentName("negative");
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    parentSubTab.getDisplayedFilterGroup().removeFilter(remove);
    AggregateUI.getUI().getTimer().refreshNow();
  }

}