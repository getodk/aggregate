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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.opendatakit.common.utils.LocaleUtils.withLocale;

import java.util.Locale;
import org.junit.Test;

public class JRTemporalFormattingTest {
  @Test
  public void formats_values_for_humans() {
    // Some JVMs can add the AM/PM designation in between
    assertThat(JRTemporal.dateTime("2018-01-01T10:20:30.123Z").humanFormat(), allOf(startsWith("January 1, 2018 10:20:30"), endsWith("Z")));
    assertThat(JRTemporal.date("2018-01-01").humanFormat(), is("January 1, 2018"));
    assertThat(JRTemporal.time("10:20:30.123Z").humanFormat(), allOf(startsWith("10:20:30"), endsWith("Z")));
  }

  @Test
  public void enforces_the_English_locale_when_formatting_values_for_humans() {
    withLocale(Locale.forLanguageTag("ES"), () -> {
      // Some JVMs can add the AM/PM designation in between
      assertThat(JRTemporal.dateTime("2018-01-01T10:20:30.123Z").humanFormat(), allOf(startsWith("January 1, 2018 10:20:30"), endsWith("Z")));
      assertThat(JRTemporal.date("2018-01-01").humanFormat(), is("January 1, 2018"));
      assertThat(JRTemporal.time("10:20:30.123Z").humanFormat(), allOf(startsWith("10:20:30"), endsWith("Z")));
    });
  }


}
