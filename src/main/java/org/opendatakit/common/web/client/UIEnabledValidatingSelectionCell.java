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
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a SelectionCell (code copied from 2.3.0) modified
 * to perform validation checking before allowing a selection
 * and to support disabling of the drop-down.  If you don't
 * supply any validation, visibility or enablement predicates,
 * this behaves the same as the SelectionCell.
 *
 * @param <T>
 * @author mitchellsundt@gmail.com
 */
public class UIEnabledValidatingSelectionCell<T> extends /* SelectionCell */
    AbstractInputCell<String, String> {

  private static OptionsTemplate template;
  final StringValidationPredicate<T> validationPredicate;
  final UIEnabledPredicate<T> isEnabledPredicate;
  private final ArrayList<String> options;
  private final UIVisiblePredicate<T> isVisiblePredicate;
  private HashMap<String, Integer> indexForOption = new HashMap<String, Integer>();

  /**
   * Construct a new {@link SelectionCell} with the specified options.
   *
   * @param options the options in the cell
   */
  public UIEnabledValidatingSelectionCell(
      StringValidationPredicate<T> validationPredicate,
      UIVisiblePredicate<T> isVisiblePredicate,
      UIEnabledPredicate<T> isEnabledPredicate, ArrayList<String> options) {
    super("change");
    this.validationPredicate = validationPredicate;
    this.isVisiblePredicate = isVisiblePredicate;
    this.isEnabledPredicate = isEnabledPredicate;
    if (template == null) {
      template = GWT.create(OptionsTemplate.class);
    }
    this.options = new ArrayList<String>(options);
    int index = 0;
    for (String option : options) {
      indexForOption.put(option, index++);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onBrowserEvent(Context context, Element parent, String value,
                             NativeEvent event, ValueUpdater<String> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    String type = event.getType();
    if ("change".equals(type)) {
      Object key = context.getKey();
      SelectElement select = parent.getFirstChild().cast();
      String newValue = options.get(select.getSelectedIndex());

      // Get the prior value of the view data.
      String lastValue = getViewData(key);
      boolean changed = true;
      if (lastValue != null && newValue != null && lastValue.equals(newValue)) {
        changed = false;
      }

      if (validationPredicate != null &&
          !validationPredicate.isValid(newValue, (T) key)) {
        // either there is no change in value
        // or the validation failed.
        // restore drop-down to prior value...
        newValue = lastValue;
        changed = false;
      }

      // update backing map with whatever we ended up with...
      setViewData(key, newValue);

      // redraw our parent with the value we ended up with...
      SafeHtmlBuilder sb = new SafeHtmlBuilder();
      render(context, newValue, sb);
      parent.setInnerHTML(sb.toSafeHtml().asString());
      // remove keyboard focus on this element...
      finishEditing(parent, newValue, key, valueUpdater);

      if (changed) {
        // do value-change action...
        if (valueUpdater != null) {
          valueUpdater.update(newValue);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Get the view data.
    Object key = context.getKey();
    String viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    if (isVisiblePredicate == null || isVisiblePredicate.isVisible((T) key)) {
      int selectedIndex = getSelectedIndex(viewData == null ? value
          : viewData);
      if (isEnabledPredicate == null || isEnabledPredicate.isEnabled((T) key)) {
        sb.appendHtmlConstant("<select tabindex=\"-1\">");
      } else {
        sb.appendHtmlConstant("<select tabindex=\"-1\" disabled>");
      }
      int index = 0;
      for (String option : options) {
        if (index++ == selectedIndex) {
          sb.append(template.selected(option));
        } else {
          sb.append(template.deselected(option));
        }
      }
      sb.appendHtmlConstant("</select>");
    }
  }

  private int getSelectedIndex(String value) {
    Integer index = indexForOption.get(value);
    if (index == null) {
      return -1;
    }
    return index.intValue();
  }

  interface OptionsTemplate extends SafeHtmlTemplates {
    @Template("<option value=\"{0}\">{0}</option>")
    SafeHtml deselected(String option);

    @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
    SafeHtml selected(String option);
  }
}
