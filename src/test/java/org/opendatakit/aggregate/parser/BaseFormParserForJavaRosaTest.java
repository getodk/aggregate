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

package org.opendatakit.aggregate.parser;

import static org.junit.Assert.assertThat;
import static org.opendatakit.aggregate.parser.BaseFormParserForJavaRosa.fixOffset;

import org.hamcrest.Matchers;
import org.junit.Test;

public class BaseFormParserForJavaRosaTest {

  @Test
  public void fixes_wrong_offsets_in_iso8601_datetimes() {
    assertThat(fixOffset("00:00:00.000Z"), Matchers.is("00:00:00.000Z"));
    assertThat(fixOffset("00:00:00.000+00"), Matchers.is("00:00:00.000+00:00"));
    assertThat(fixOffset("00:00:00.000+01"), Matchers.is("00:00:00.000+01:00"));
    assertThat(fixOffset("00:00:00.000+0000"), Matchers.is("00:00:00.000+00:00"));
    assertThat(fixOffset("00:00:00.000+1122"), Matchers.is("00:00:00.000+11:22"));
    assertThat(fixOffset("00:00:00.000-00"), Matchers.is("00:00:00.000-00:00"));
    assertThat(fixOffset("00:00:00.000-01"), Matchers.is("00:00:00.000-01:00"));
    assertThat(fixOffset("00:00:00.000-0000"), Matchers.is("00:00:00.000-00:00"));
    assertThat(fixOffset("00:00:00.000-1122"), Matchers.is("00:00:00.000-11:22"));
  }
}
