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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.CheckBox;

public class AggregateCheckBox extends CheckBox implements ValueChangeHandler<Boolean> {

  private final AggregateBaseHandlers handlers;

  public AggregateCheckBox(String label, boolean labelIsHTML, String tooltipText, String helpBalloonText) {
    super();

    if (label != null) {
      if (labelIsHTML) {
        setHTML(new SafeHtmlBuilder().appendHtmlConstant(label).toSafeHtml());
      } else {
        setText(label);
      }
    }
    addValueChangeHandler(this);

    handlers = new AggregateBaseHandlers(this, tooltipText, helpBalloonText);
    addMouseOverHandler(handlers);
    addMouseOutHandler(handlers);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    handlers.userAction();
  }

}
