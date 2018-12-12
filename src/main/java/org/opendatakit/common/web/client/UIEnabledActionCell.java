/*
 * Copyright 2010 Google Inc.
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

package org.opendatakit.common.web.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Implements a button that can be made not visible, enabled or disabled.
 *
 * @param <T>
 * @author mitchellsundt@gmail.com
 */
public class UIEnabledActionCell<T> extends AbstractCell<T> {

  private final Delegate<T> delegate;
  private final SafeHtml htmlEnabled;
  private final SafeHtml htmlDisabled;
  private final UIEnabledPredicate<T> isEnabledPredicate;
  private final UIVisiblePredicate<T> isVisiblePredicate;

  public UIEnabledActionCell(String text, UIVisiblePredicate<T> isVisiblePredicate,
                             UIEnabledPredicate<T> isEnabledPredicate,
                             UIEnabledActionCell.Delegate<T> delegate) {
    this(SafeHtmlUtils.fromString(text), isVisiblePredicate, isEnabledPredicate, delegate);
  }

  public UIEnabledActionCell(SafeHtml text, UIVisiblePredicate<T> isVisiblePredicate,
                             UIEnabledPredicate<T> isEnabledPredicate,
                             UIEnabledActionCell.Delegate<T> delegate) {
    super("click", "keydown");
    this.isEnabledPredicate = isEnabledPredicate;
    this.isVisiblePredicate = isVisiblePredicate;
    this.delegate = delegate;
    htmlEnabled = new SafeHtmlBuilder()
        .appendHtmlConstant(
            "<button class=\"gwt-Button\" type=\"button\" tabindex=\"-1\">").append(text)
        .appendHtmlConstant("</button>").toSafeHtml();
    htmlDisabled = new SafeHtmlBuilder()
        .appendHtmlConstant(
            "<button class=\"gwt-Button\" type=\"button\" tabindex=\"-1\" disabled>").append(text)
        .appendHtmlConstant("</button>").toSafeHtml();
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
                             ValueUpdater<T> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if ("click".equals(event.getType())) {
      EventTarget eventTarget = event.getEventTarget();
      if (!Element.is(eventTarget)) {
        return;
      }
      if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        // Ignore clicks that occur outside of the main element.
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, T value, NativeEvent event,
                                ValueUpdater<T> valueUpdater) {
    if (isEnabledPredicate == null || isEnabledPredicate.isEnabled(value)) {
      delegate.execute(value);
    }
  }

  @Override
  public void render(Context context, T object, SafeHtmlBuilder sb) {
    if (isVisiblePredicate == null || isVisiblePredicate.isVisible(object)) {
      if (isEnabledPredicate == null || isEnabledPredicate.isEnabled(object)) {
        sb.append(htmlEnabled);
      } else {
        sb.append(htmlDisabled);
      }
    }
  }

  /**
   * The delegate that will handle events from the cell.
   */
  public static interface Delegate<C> {
    /**
     * Perform the desired action on the given object.
     *
     * @param object the object to be acted upon
     */
    void execute(C object);
  }
}
