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

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * This is a simple checkbox -- it is copied from CheckboxCell GWT 2.3.0
 * and the functionality to support radio buttons (SelectionManager) has
 * been removed.  This checkbox supports validation of the selection choice,
 * visibility of the control, and the enabled/disabled state of the checkbox.
 * If all of the predicates are null (any or all of them can be), this
 * behaves as a CheckboxCell not managed by a SelectionManager.
 *
 * @param <T>
 * @author mitchellsundt@gmail.com
 */
public class UIEnabledValidatingCheckboxCell<T> extends
    AbstractEditableCell<Boolean, Boolean> {

  /**
   * An html string representation of a checked input box.
   */
  private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils
      .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");
  /**
   * An html string representation of a disabled checked input box.
   */
  private static final SafeHtml INPUT_DISABLED_CHECKED = SafeHtmlUtils
      .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled/>");

  /**
   * An html string representation of an unchecked input box.
   */
  private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
      .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");

  /**
   * An html string representation of an disabled unchecked input box.
   */
  private static final SafeHtml INPUT_DISABLED_UNCHECKED = SafeHtmlUtils
      .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>");

  private final BooleanValidationPredicate<T> validationPredicate;
  private final UIEnabledPredicate<T> isEnabledPredicate;
  private final UIVisiblePredicate<T> isVisiblePredicate;

  /**
   * Construct a new {@link UIEnabledValidatingCheckboxCell}.
   */
  public UIEnabledValidatingCheckboxCell(BooleanValidationPredicate<T> validationPredicate,
                                         UIVisiblePredicate<T> isVisiblePredicate) {
    this(validationPredicate, isVisiblePredicate, null);
  }

  public UIEnabledValidatingCheckboxCell(BooleanValidationPredicate<T> validationPredicate,
                                         UIEnabledPredicate<T> isEnabledPredicate) {
    this(validationPredicate, null, isEnabledPredicate);
  }

  /**
   * Construct a new {@link UIEnabledValidatingCheckboxCell}.
   *
   * @param validationPredicate true if the supplied value is OK (will reset to the inverse if fails).
   *                            If null, will the supplied value is always be accepted.
   * @param isVisiblePredicate  true if the cell should be visible. If null, always visible.
   * @param isEnabledPredicate  true if the cell should be enabled. If null, always enabled.
   */
  public UIEnabledValidatingCheckboxCell(BooleanValidationPredicate<T> validationPredicate,
                                         UIVisiblePredicate<T> isVisiblePredicate,
                                         UIEnabledPredicate<T> isEnabledPredicate) {
    super("change", "keydown");
    this.validationPredicate = validationPredicate;
    this.isVisiblePredicate = isVisiblePredicate;
    this.isEnabledPredicate = isEnabledPredicate;
  }

  @Override
  public boolean dependsOnSelection() {
    return false;
  }

  @Override
  public boolean handlesSelection() {
    return false;
  }

  @Override
  public boolean isEditing(Context context, Element parent, Boolean value) {
    // A checkbox is never in "edit mode". There is no intermediate state
    // between checked and unchecked.
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onBrowserEvent(Context context, Element parent, Boolean value,
                             NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
    String type = event.getType();

    boolean enterPressed = "keydown".equals(type)
        && event.getKeyCode() == KeyCodes.KEY_ENTER;
    if ("change".equals(type) || enterPressed) {
      InputElement input = parent.getFirstChild().cast();
      Boolean isChecked = input.isChecked();

      Object key = context.getKey();
      if (isEnabledPredicate != null && !isEnabledPredicate.isEnabled((T) key)) {
        // disabled checkbox -- no changes!
        return;
      }
      /*
       * Toggle the value if the enter key was pressed; this brings
       * the key-press event path in line with a mouse-click event path.
       */
      if (enterPressed) {
        isChecked = !isChecked;
        input.setChecked(isChecked);
      }

      /*
       * See if this is a valid action for this checkbox.
       */
      boolean changed = true;
      if (validationPredicate != null && !validationPredicate.isValid(isChecked, (T) key)) {
        isChecked = !isChecked;
        changed = false;
      }

      /*
       * Save the new value.
       */
      if (value != isChecked) {
        setViewData(context.getKey(), isChecked);
      } else {
        clearViewData(context.getKey());
      }

      // redraw our parent with the value we ended up with...
      SafeHtmlBuilder sb = new SafeHtmlBuilder();
      render(context, isChecked, sb);
      parent.setInnerHTML(sb.toSafeHtml().asString());
      // apparently the element never gains focus...

      if (changed) {
        // do value-change action...
        if (valueUpdater != null) {
          valueUpdater.update(isChecked);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
    // Get the view data.
    Object key = context.getKey();
    Boolean viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    if (isVisiblePredicate == null || isVisiblePredicate.isVisible((T) key)) {
      if (value != null && ((viewData != null) ? viewData : value)) {
        if (isEnabledPredicate == null || isEnabledPredicate.isEnabled((T) key)) {
          sb.append(INPUT_CHECKED);
        } else {
          sb.append(INPUT_DISABLED_CHECKED);
        }
      } else {
        if (isEnabledPredicate == null || isEnabledPredicate.isEnabled((T) key)) {
          sb.append(INPUT_UNCHECKED);
        } else {
          sb.append(INPUT_DISABLED_UNCHECKED);
        }
      }
    }
  }
}
