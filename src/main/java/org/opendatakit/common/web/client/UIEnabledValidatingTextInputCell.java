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

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A TextInputCell that does validation of the input and supports disabling
 * input. It is parameterized by the class that is the backing object for the
 * row in which the cell appears. If you don't supply any validation, visibility
 * or isEnabled predicates, this behaves the same as the TextInputCell.
 * <p>
 * Copied from 2.3.0 sources and tweaked because of various method scoping issues.
 *
 * @param <T>
 * @author mitchellsundt@gmail.com
 */
public class UIEnabledValidatingTextInputCell<T> extends
    AbstractInputCell<String, UIEnabledValidatingTextInputCell.ViewData> {

  private static EnabledTemplate template;
  private static DisabledTemplate disabledTemplate;
  final StringValidationPredicate<T> validation;
  final UIEnabledPredicate<T> isEnabledPredicate;
  final UIVisiblePredicate<T> isVisiblePredicate;

  public UIEnabledValidatingTextInputCell(
      StringValidationPredicate<T> validation,
      UIEnabledPredicate<T> isEnabledPredicate) {
    this(validation, null, isEnabledPredicate);
  }
  public UIEnabledValidatingTextInputCell(
      StringValidationPredicate<T> validation,
      UIVisiblePredicate<T> isVisiblePredicate) {
    this(validation, isVisiblePredicate, null);
  }
  public UIEnabledValidatingTextInputCell(
      StringValidationPredicate<T> validation,
      UIVisiblePredicate<T> isVisiblePredicate,
      UIEnabledPredicate<T> isEnabledPredicate) {
    super("change", "keyup");
    this.validation = validation;
    this.isVisiblePredicate = isVisiblePredicate;
    this.isEnabledPredicate = isEnabledPredicate;
    if (template == null) {
      template = GWT.create(EnabledTemplate.class);
    }
    if (disabledTemplate == null) {
      disabledTemplate = GWT.create(DisabledTemplate.class);
    }
  }

  /**
   * Because of various scoping issues, we need to override the onBrowserEvent
   * method.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void onBrowserEvent(Context context, Element parent, String value,
                             NativeEvent event, ValueUpdater<String> valueUpdater) {

    // Ignore events that don't target the input.
    InputElement input = getInputElement(parent);
    Element target = event.getEventTarget().cast();
    if (!input.isOrHasChild(target)) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      return;
    }

    String eventType = event.getType();
    Object key = context.getKey();
    if ("keyup".equals(eventType)) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      // Record keys as they are typed.
      ViewData vd = getViewData(key);
      if (vd == null) {
        vd = new ViewData(value);
        setViewData(key, vd);
      }
      vd.setCurrentValue(input.getValue());
    } else if ("change".equals(eventType)) {
      String newValue = input.getValue();

      // Get the view data.
      ViewData vd = getViewData(key);
      if (vd == null) {
        // TODO: unclear whether on null we should store value in ViewData
        // or null
        vd = new ViewData(value);
        setViewData(key, vd);
      }

      String lastValue = vd.getLastValue();
      boolean changed = true;
      if (lastValue != null && newValue != null && lastValue.equals(newValue)) {
        changed = false;
      }

      // now do the logic to verify the value or restore it...
      if (validation != null && !validation.isValid(newValue, (T) key)) {
        // restore to prior value...
        newValue = lastValue;
        changed = false;
      }

      // restore the incoming value if validation failed
      // or update to the new value otherwise.
      input.setValue(newValue);
      vd.setLastValue(newValue);
      vd.setCurrentValue(newValue);

      // redraw our parent with the value we ended up with...
      SafeHtmlBuilder sb = new SafeHtmlBuilder();
      render(context, newValue, sb);
      parent.setInnerHTML(sb.toSafeHtml().asString());

      // Blur the element.
      super.finishEditing(parent, newValue, key, valueUpdater);

      if (changed) {
        // Fire the value updater if the value has changed.
        if (valueUpdater != null) {
          valueUpdater.update(newValue);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    Object key = context.getKey();
    ViewData viewData = getViewData(key);
    if (viewData != null && viewData.getCurrentValue().equals(value)) {
      clearViewData(key);
      viewData = null;
    }
    // Get the view data.
    String s = (viewData != null) ? viewData.getCurrentValue() : value;

    if (isVisiblePredicate == null || isVisiblePredicate.isVisible((T) key)) {
      if (isEnabledPredicate == null
          || isEnabledPredicate.isEnabled((T) key)) {
        if (s != null) {
          sb.append(template.input(s));
        } else {
          sb.appendHtmlConstant("<input type=\"text\" tabindex=\"-1\"></input>");
        }
      } else {
        if (s != null) {
          sb.append(disabledTemplate.input(s));
        } else {
          sb.appendHtmlConstant("<input type=\"text\" tabindex=\"-1\" disabled></input>");
        }
      }
    }
  }

  @Override
  protected InputElement getInputElement(Element parent) {
    return super.getInputElement(parent).<InputElement>cast();
  }

  ;

  interface EnabledTemplate extends SafeHtmlTemplates {
    @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
    SafeHtml input(String value);
  }

  interface DisabledTemplate extends SafeHtmlTemplates {
    @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" disabled></input>")
    SafeHtml input(String value);
  }

  /**
   * The {@code ViewData} for this cell.
   */
  public static class ViewData {
    /**
     * The last value that was updated.
     */
    private String lastValue;

    /**
     * The current value.
     */
    private String curValue;

    /**
     * Construct a ViewData instance containing a given value.
     *
     * @param value a String value
     */
    public ViewData(String value) {
      this.lastValue = value;
      this.curValue = value;
    }

    /**
     * Return true if the last and current values of this ViewData object
     * are equal to those of the other object.
     */
    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ViewData)) {
        return false;
      }
      ViewData vd = (ViewData) other;
      return equalsOrNull(lastValue, vd.lastValue)
          && equalsOrNull(curValue, vd.curValue);
    }

    /**
     * Return the current value of the input element.
     *
     * @return the current value String
     * @see #setCurrentValue(String)
     */
    public String getCurrentValue() {
      return curValue;
    }

    /**
     * Set the current value.
     *
     * @param curValue the current value
     * @see #getCurrentValue()
     */
    protected void setCurrentValue(String curValue) {
      this.curValue = curValue;
    }

    /**
     * Return the last value sent to the {@link ValueUpdater}.
     *
     * @return the last value String
     * @see #setLastValue(String)
     */
    public String getLastValue() {
      return lastValue;
    }

    /**
     * Set the last value.
     *
     * @param lastValue the last value
     * @see #getLastValue()
     */
    protected void setLastValue(String lastValue) {
      this.lastValue = lastValue;
    }

    /**
     * Return a hash code based on the last and current values.
     */
    @Override
    public int hashCode() {
      return (lastValue + "_*!@HASH_SEPARATOR@!*_" + curValue).hashCode();
    }

    private boolean equalsOrNull(Object a, Object b) {
      return (a != null) ? a.equals(b) : ((b == null) ? true : false);
    }
  }

}
