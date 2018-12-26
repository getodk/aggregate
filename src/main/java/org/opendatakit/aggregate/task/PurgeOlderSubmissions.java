/*
 * Copyright (C) 2011 University of Washington.
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
package org.opendatakit.aggregate.task;

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Optional;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.web.CallingContext;

public class PurgeOlderSubmissions {

  public static final String PURGE_DATE = "purgeBefore";
  private static final DateTimeFormatter PURGE_DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .append(ISO_LOCAL_DATE)
      .appendLiteral(' ')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .toFormatter();
  private static final ZoneOffset SYSTEM_OFFSET = OffsetDateTime.now().getOffset();


  public static String formatPurgeDate(Date value) {
    return Optional.ofNullable(value)
        .map(date -> LocalDateTime.ofInstant(date.toInstant(), systemDefault()).format(PURGE_DATE_TIME_FORMAT))
        .orElse(null);
  }

  static Date parsePurgeDate(String value) {
    return Optional.ofNullable(value)
        .map(date -> Date.from(LocalDateTime.parse(date, PURGE_DATE_TIME_FORMAT).toInstant(SYSTEM_OFFSET)))
        .orElse(null);
  }

  public final void createPurgeOlderSubmissionsTask(IForm form, SubmissionKey miscTasksKey, long attemptCount, CallingContext cc) {
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    PurgeOlderSubmissionsWorkerImpl worker = new PurgeOlderSubmissionsWorkerImpl(form, miscTasksKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(worker::purgeOlderSubmissions);
  }
}
