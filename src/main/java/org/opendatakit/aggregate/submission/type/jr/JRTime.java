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

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_TIME;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.Date;
import java.util.Objects;

public class JRTime {
  private final Date parsed;
  private final OffsetTime value;
  private final String raw;

  public JRTime(Date parsed, OffsetTime value, String raw) {
    this.parsed = parsed;
    this.value = value;
    this.raw = raw;
  }

  public static JRTime from(String raw) {
    if (raw.charAt(raw.length() - 3) == '+' || raw.charAt(raw.length() - 3) == '-')
      raw += ":00";
    OffsetTime value = OffsetTime.parse(Objects.requireNonNull(raw));
    Date parsed = Date.from(value.atDate(LocalDate.of(1970, 1, 1)).toInstant());
    return new JRTime(parsed, value, raw);
  }

  public static JRTime from(Date parsed) {
    OffsetTime value = OffsetTime.ofInstant(requireNonNull(parsed).toInstant(), systemDefault());
    return new JRTime(parsed, value, value.format(ISO_OFFSET_TIME));
  }

  public static JRTime of(Date parsed, String raw) {
    if (raw.charAt(raw.length() - 3) == '+' || raw.charAt(raw.length() - 3) == '-')
      raw += ":00";
    OffsetTime value = OffsetTime.parse(Objects.requireNonNull(raw));
    return new JRTime(parsed, value, raw);
  }

  public Date getParsed() {
    return parsed;
  }

  public String getRaw() {
    return raw;
  }

  public OffsetTime getValue() {
    return value;
  }
}
