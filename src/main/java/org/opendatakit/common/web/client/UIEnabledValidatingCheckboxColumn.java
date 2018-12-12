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

package org.opendatakit.common.web.client;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;
import java.util.Comparator;

public abstract class UIEnabledValidatingCheckboxColumn<T> extends Column<T, Boolean>
    implements FieldUpdater<T, Boolean> {

  final UIEnabledPredicate<T> isEnabledPredicate;
  final Comparator<T> comparator;

  protected UIEnabledValidatingCheckboxColumn(BooleanValidationPredicate<T> validationPredicate,
                                              UIVisiblePredicate<T> isVisiblePredicate,
                                              UIEnabledPredicate<T> isEnabledPredicate,
                                              Comparator<T> comparator) {
    super(new UIEnabledValidatingCheckboxCell<T>(validationPredicate, isVisiblePredicate, isEnabledPredicate));
    this.setFieldUpdater(this);
    this.isEnabledPredicate = isEnabledPredicate;
    this.comparator = comparator;
    this.setSortable(comparator != null);
  }

  @Override
  public void onBrowserEvent(Context context, Element elem, T object,
                             NativeEvent event) {
    if (isEnabledPredicate == null || isEnabledPredicate.isEnabled(object)) {
      super.onBrowserEvent(context, elem, object, event);
    }
  }

  public Comparator<T> getComparator() {
    return comparator;
  }

  public abstract void setValue(T object, Boolean value);

  @Override
  public void update(int index, T object, Boolean value) {
    setValue(object, value);
  }
}
