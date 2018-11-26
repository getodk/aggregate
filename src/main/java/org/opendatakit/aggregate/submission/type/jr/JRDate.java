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

import java.util.Date;
import java.util.Optional;
import org.javarosa.core.model.utils.DateUtils;

public class JRDate {
  private final Date parsed;
  private final String raw;

  public JRDate(Date parsed, String raw) {
    this.parsed = parsed;
    this.raw = raw;
  }

  public static JRDate from(String value) {
    return new JRDate(
        Optional.ofNullable(DateUtils.parseDate(value)).orElseThrow(IllegalArgumentException::new),
        value
    );
  }

  public static JRDate of(Date parsed, String raw) {
    return new JRDate(parsed, raw);
  }

  public Date getParsed() {
    return parsed;
  }

  public String getRaw() {
    return raw;
  }
}
