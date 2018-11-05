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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

/**
 * Button that can be enabled or disabled.
 *
 * @param <T>
 * @author mitchellsundt@gmail.com
 */
public class UIEnabledActionColumn<T> extends Column<T, T> {
  public UIEnabledActionColumn(String label, UIVisiblePredicate<T> isVisiblePredicate,
                               UIEnabledActionCell.Delegate<T> action) {
    this(label, isVisiblePredicate, null, action);
  }

  public UIEnabledActionColumn(String label, UIEnabledPredicate<T> isEnabledPredicate,
                               UIEnabledActionCell.Delegate<T> action) {
    this(label, null, isEnabledPredicate, action);
  }

  public UIEnabledActionColumn(String label,
                               UIVisiblePredicate<T> isVisiblePredicate,
                               UIEnabledPredicate<T> isEnabledPredicate,
                               UIEnabledActionCell.Delegate<T> action) {
    super(new UIEnabledActionCell<T>(label,
        isVisiblePredicate, isEnabledPredicate,
        action));
  }

  public UIEnabledActionColumn(SafeHtml html,
                               UIVisiblePredicate<T> isVisiblePredicate,
                               UIEnabledPredicate<T> isEnabledPredicate,
                               UIEnabledActionCell.Delegate<T> action) {
    super(new UIEnabledActionCell<T>(html,
        isVisiblePredicate, isEnabledPredicate,
        action));
  }

  @Override
  public T getValue(T object) {
    return object;
  }
}
