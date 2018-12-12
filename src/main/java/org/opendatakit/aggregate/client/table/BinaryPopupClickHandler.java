/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.BinaryPopup;

public class BinaryPopupClickHandler implements ClickHandler {
  private final String value;
  private final boolean larger;

  public BinaryPopupClickHandler(String value, boolean larger) {
    this.value = value;
    this.larger = larger;
  }

  @Override
  public void onClick(ClickEvent event) {
    BinaryPopup popup = new BinaryPopup(value, larger);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
    AggregateUI.getUI().getTimer().restartTimer();
  }
}