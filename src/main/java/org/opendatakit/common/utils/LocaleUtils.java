/*
 * Copyright (C) 2019 Nafundi
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

package org.opendatakit.common.utils;

import java.util.Locale;
import java.util.function.Supplier;

public final class LocaleUtils {
  private LocaleUtils() {
    // Prevent construction of this class
  }

  public synchronized static <T> T withLocale(Locale locale, Supplier<T> supplier) {
    Locale backup = Locale.getDefault();
    Locale.setDefault(locale);
    T t = supplier.get();
    Locale.setDefault(backup);
    return t;
  }
}
