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

package org.opendatakit.aggregate.format.element;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.junit.Test;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.aggregate.submission.type.jr.JRTemporal;
import org.opendatakit.common.persistence.WrappedBigDecimal;

public class BasicElementFormatterTest {

  @Test
  public void formats_UIDs() {
    assertThat(formatUid(null), is(nullValue()));
    assertThat(formatUid(""), is(""));
    assertThat(formatUid("uuid:8030500e-12c6-40f4-badd-c9e32361f928"), is("uuid:8030500e-12c6-40f4-badd-c9e32361f928"));
  }

  @Test
  public void formats_booleans() {
    assertThat(format((Boolean) null), is(nullValue()));
    assertThat(format(true), is("true"));
    assertThat(format(false), is("false"));
  }

  @Test
  public void formats_choices() {
    assertThat(formatChoices((String[]) null), is(""));
    assertThat(formatChoices(""), is(""));
    assertThat(formatChoices("choice1"), is("choice1"));
    assertThat(formatChoices("choice1", "choice2"), is("choice1 choice2"));
  }

  @Test
  public void formats_dateTimes_from_metadata_and_generated_data() {
    OffsetDateTime now = OffsetDateTime.parse("2018-01-01T10:20:30.123Z");
    assertThat(format((Date) null), is(nullValue()));
    withOffset("Europe/Madrid", () ->
        assertThat(format(Date.from(now.toInstant())), is("Jan 1, 2018 11:20:30 AM"))
    );
  }

  private static void withOffset(String zoneId, Runnable block) {
    TimeZone backup = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
    block.run();
    TimeZone.setDefault(backup);
  }

  @Test
  public void formats_decimals() {
    assertThat(format((WrappedBigDecimal) null), is(nullValue()));
    assertThat(format(WrappedBigDecimal.fromDouble(1.234)), is("1.234"));
  }

  @Test
  public void formats_dates_from_user_input() {
    assertThat(formatJRDate(null), is(nullValue()));
    assertThat(formatJRDate(JRTemporal.date("2018-01-01")), is("January 1, 2018"));
  }

  @Test
  public void formats_times_from_user_input() {
    assertThat(formatJRTime(null), is(nullValue()));
    // Some JVMs can add the AM/PM designation in between
    assertThat(formatJRTime(JRTemporal.time("10:20:30.123Z")), allOf(startsWith("10:20:30"), endsWith("Z")));
    assertThat(formatJRTime(JRTemporal.time("10:20:30.123+01:00")), allOf(startsWith("10:20:30"), endsWith("+01:00")));
  }

  @Test
  public void formats_dateTimes_from_user_input() {
    assertThat(formatJRDateTime(null), is(nullValue()));
    // Some JVMs can add the AM/PM designation in between
    assertThat(formatJRDateTime(JRTemporal.dateTime("2018-01-01T10:20:30.123Z")), allOf(startsWith("January 1, 2018 10:20:30"), endsWith("Z")));
    assertThat(formatJRDateTime(JRTemporal.dateTime("2018-01-01T10:20:30.123+01:00")), allOf(startsWith("January 1, 2018 10:20:30"), endsWith("+01:00")));
  }

  @Test
  public void formatsgeopoints() {
    assertThat(format(null, false, false), is(nullValue()));
    assertThat(format(geopoint(1, 2, null, null), true, true), is("1.0, 2.0"));
    assertThat(format(geopoint(1, 2, 3, 4), false, false), is("1.0, 2.0"));
    assertThat(format(geopoint(1, 2, 3, 4), true, false), is("1.0, 2.0, 3.0"));
    assertThat(format(geopoint(1, 2, 3, 4), false, true), is("1.0, 2.0, 4.0")); // Oopsie, this doesn't make much sense...
    assertThat(format(geopoint(1, 2, 3, 4), true, true), is("1.0, 2.0, 3.0, 4.0"));
    assertThat(formatSeparated(null, false, false), contains(nullValue()));
    assertThat(formatSeparated(geopoint(1, 2, null, null), true, true), contains("1.0", "2.0", null, null));
    assertThat(formatSeparated(geopoint(1, 2, 3, 4), false, false), contains("1.0", "2.0"));
    assertThat(formatSeparated(geopoint(1, 2, 3, 4), true, false), contains("1.0", "2.0", "3.0"));
    assertThat(formatSeparated(geopoint(1, 2, 3, 4), false, true), contains("1.0", "2.0", "4.0")); // Oopsie, this doesn't make much sense...
    assertThat(formatSeparated(geopoint(1, 2, 3, 4), true, true), contains("1.0", "2.0", "3.0", "4.0"));
  }

  @Test
  public void formats_longs() {
    assertThat(format((Long) null), is(nullValue()));
    assertThat(format(1L), is("1"));
  }

  @Test
  public void formats_strings() {
    assertThat(format((String) null), is(nullValue()));
    assertThat(format("some string"), is("some string"));
  }

  private GeoPoint geopoint(Number lat, Number lon, Number alt, Number acc) {
    return new GeoPoint(
        Optional.ofNullable(lat).map(Number::doubleValue).map(WrappedBigDecimal::fromDouble).orElse(null),
        Optional.ofNullable(lon).map(Number::doubleValue).map(WrappedBigDecimal::fromDouble).orElse(null),
        Optional.ofNullable(alt).map(Number::doubleValue).map(WrappedBigDecimal::fromDouble).orElse(null),
        Optional.ofNullable(acc).map(Number::doubleValue).map(WrappedBigDecimal::fromDouble).orElse(null)
    );
  }

  private static String formatUid(String uid) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatUid(uid, null, row);
    return row.getFormattedValues().get(0);
  }

  private static String format(Boolean value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatBoolean(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String formatChoices(String... choices) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatChoices(choices != null ? Arrays.asList(choices) : null, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String format(Date value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatDateTime(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String format(WrappedBigDecimal value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatDecimal(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String formatJRDate(JRTemporal value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatJRDate(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String formatJRTime(JRTemporal value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatJRTime(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String formatJRDateTime(JRTemporal value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatJRDateTime(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String format(GeoPoint value, boolean includeAltitude, boolean includeAccuracy) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, includeAltitude, includeAccuracy);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatGeoPoint(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static List<String> formatSeparated(GeoPoint value, boolean includeAltitude, boolean includeAccuracy) {
    BasicElementFormatter formatter = new BasicElementFormatter(true, includeAltitude, includeAccuracy);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatGeoPoint(value, null, "0", row);
    return row.getFormattedValues();
  }

  private static String format(Long value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatLong(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }

  private static String format(String value) {
    BasicElementFormatter formatter = new BasicElementFormatter(false, false, false);
    Row row = new Row(new SubmissionKey("submission key"));
    formatter.formatString(value, null, "0", row);
    return row.getFormattedValues().get(0);
  }
}
