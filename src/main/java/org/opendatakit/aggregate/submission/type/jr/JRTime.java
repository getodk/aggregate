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

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import org.javarosa.core.model.utils.DateUtils;

public class JRTime {
  private final Date parsed;
  private final String raw;

  public JRTime(Date parsed, String raw) {
    this.parsed = parsed;
    this.raw = raw;
  }

  public static JRTime from(String value) {
    return new JRTime(
        Optional.ofNullable(DateUtils.parseTime(value)).orElseThrow(IllegalArgumentException::new),
        value
    );
  }

  public static JRTime from(Date parsed) {
    OffsetDateTime odt = OffsetDateTime.ofInstant(requireNonNull(parsed).toInstant(), systemDefault());
    return new JRTime(Date.from(odt.toInstant()), odt.format(ISO_OFFSET_TIME));
  }

  public static JRTime of(Date parsed, String raw) {
    return new JRTime(parsed, raw);
  }

  public Date getParsed() {
    return parsed;
  }

  public String getRaw() {
    return raw;
  }
}
