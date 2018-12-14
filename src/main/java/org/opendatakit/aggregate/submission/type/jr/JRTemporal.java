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
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_TIME;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;

public interface JRTemporal<T extends Temporal> {

  static JRTemporal time(String raw) {
    OffsetTime value = OffsetTime.parse(Objects.requireNonNull(fixOffset(raw)));
    Date parsed = Date.from(value.atDate(LocalDate.of(1970, 1, 1)).toInstant());
    return new JRTime(parsed, value, raw);
  }

  static JRTemporal time(Date parsed) {
    OffsetTime value = OffsetTime.ofInstant(requireNonNull(parsed).toInstant(), systemDefault());
    return new JRTime(parsed, value, value.format(ISO_OFFSET_TIME));
  }

  static JRTemporal time(Date parsed, String raw) {
    OffsetTime value = OffsetTime.parse(Objects.requireNonNull(fixOffset(raw)));
    return new JRTime(parsed, value, raw);
  }

  static JRTemporal date(String raw) {
    LocalDate value = LocalDate.parse(raw);
    Date parsed = Date.from(value.atStartOfDay(systemDefault()).toInstant());
    return new JRDate(parsed, value, raw);
  }

  static JRTemporal date(Date parsed) {
    OffsetDateTime value = OffsetDateTime.ofInstant(requireNonNull(parsed).toInstant(), systemDefault()).truncatedTo(ChronoUnit.DAYS);
    return new JRDate(Date.from(value.toInstant()), value.toLocalDate(), value.format(ISO_LOCAL_DATE));
  }

  static JRTemporal date(Date parsed, String raw) {
    return new JRDate(parsed, LocalDate.parse(raw), raw);
  }

  static JRTemporal dateTime(String raw) {
    OffsetDateTime value = OffsetDateTime.parse(fixOffset(raw));
    return new JRDateTime(Date.from(value.toInstant()), value, raw);
  }

  static JRTemporal dateTime(Date parsed) {
    OffsetDateTime value = OffsetDateTime.ofInstant(requireNonNull(parsed).toInstant(), systemDefault());
    return new JRDateTime(Date.from(value.toInstant()), value, value.format(ISO_OFFSET_DATE_TIME));
  }

  static JRTemporal dateTime(Date parsed, String raw) {
    OffsetDateTime value = OffsetDateTime.parse(fixOffset(raw));
    return new JRDateTime(parsed, value, raw);
  }

  static String fixOffset(String raw) {
    char thirdCharFromTheEnd = raw.charAt(raw.length() - 3);
    return thirdCharFromTheEnd == '+' || thirdCharFromTheEnd == '-' ? raw + ":00" : raw;
  }

  Date getParsed();

  String getRaw();

  T getValue();
}
