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

package org.opendatakit.common.utils;

import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

public class GwtShims {
  public static String gwtFormatDateHuman(Date date) {
    return DateTimeFormat.getFormat("MMM dd, yyyy").format(date);
  }

  public static String gwtFormatDateTimeHuman(Date date) {
    return DateTimeFormat.getFormat("MMM dd, yyyy HH:mm:ss a").format(date);
  }
}
