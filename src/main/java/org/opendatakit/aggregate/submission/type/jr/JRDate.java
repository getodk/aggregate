/*
 * Copyright (C) 2018 Nafundi
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

package org.opendatakit.aggregate.submission.type.jr;

import static java.time.format.DateTimeFormatter.ofLocalizedDate;
import static java.time.format.FormatStyle.LONG;
import static java.util.Locale.ENGLISH;
import static org.opendatakit.common.utils.LocaleUtils.withLocale;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

public class JRDate implements JRTemporal<LocalDate> {
  private final Date parsed;
  private final LocalDate value;
  private final String raw;

  public JRDate(Date parsed, LocalDate value, String raw) {
    this.parsed = parsed;
    this.value = value;
    this.raw = raw;
  }

  @Override
  public Date getParsed() {
    return parsed;
  }

  @Override
  public String getRaw() {
    return raw;
  }

  @Override
  public LocalDate getValue() {
    return value;
  }

  @Override
  public String humanFormat(FormatStyle style) {
    return withLocale(ENGLISH, () -> value.format(ofLocalizedDate(style)));
  }
}
