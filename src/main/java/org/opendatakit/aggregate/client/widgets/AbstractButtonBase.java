/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;

public abstract class AbstractButtonBase extends Button implements ClickHandler, MouseOverHandler, MouseOutHandler
{
  private String tooltipText;
  protected HelpBalloon helpBalloon;
  
  public AbstractButtonBase(String buttonText, String tooltipText) {
    super(buttonText);
    
    addClickHandler(this);
    
    // setup help system
    this.helpBalloon = null;
    this.tooltipText = tooltipText;
    setTitle(tooltipText);
    
    addMouseOverHandler(this);
    addMouseOutHandler(this);
  }

  public void onClick(ClickEvent event) {
    AggregateUI baseUI = AggregateUI.getUI();
    baseUI.clearError();
    baseUI.getTimer().restartTimer();
  }

  public void onMouseOver(MouseOverEvent event) {
    if(!AggregateUI.getUI().displayingHelpBalloons()) {
      return;
    }
    
    if(helpBalloon != null) {
      // hide the tool tip
      setTitle(UIConsts.EMPTY_STRING);
      
      // show the help balloon;
      helpBalloon.display();
    }
  }

  public void onMouseOut(MouseOutEvent event) {
    if(helpBalloon != null) {
      // restore the tool tip
      setTitle(tooltipText);
      
      // hide the help balloon;
      helpBalloon.hide();
    }
  }
}
