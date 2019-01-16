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

package org.opendatakit.aggregate.submission.type.jr;

import java.time.OffsetDateTime;
import java.util.Date;

public final class JRTemporalUtils {
  private JRTemporalUtils() {
    // Prevent instantiation of this class
  }

  public static String fixOffset(String raw) {
    // Trim the input string to prevent errors from leading or trailing spaces that might be present
    raw = raw.trim();
    // Offsets can come in +00 or -00 format. They need to be converted to +00:00 and -00:00
    char thirdCharFromTheEnd = raw.charAt(raw.length() - 3);
    if (thirdCharFromTheEnd == '+' || thirdCharFromTheEnd == '-')
      return raw + ":00";
    // Offsets can come in +0000 or -0000 format. They need to be converted to +00:00 and -00:00
    char fifthCharFromTheEnd = raw.charAt(raw.length() - 5);
    if (fifthCharFromTheEnd == '+' || fifthCharFromTheEnd == '-')
      return raw.substring(0, raw.length() - 2) + ":" + raw.substring(raw.length() - 2);
    return raw;
  }

  public static Date parseDate(String raw) {
    return Date.from(OffsetDateTime.parse(fixOffset(raw)).toInstant());
  }
}
