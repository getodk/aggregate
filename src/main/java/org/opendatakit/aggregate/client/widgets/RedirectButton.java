package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.UrlHash;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class RedirectButton extends AButtonBase implements ClickHandler {
  private String url;

  public RedirectButton(String buttonText, String url) {
    super(buttonText);
    this.url = url;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    UrlHash.getHash().goTo(url);
  }
}