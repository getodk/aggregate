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
package org.opendatakit.common.persistence;

import java.util.Date;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

class TestRow {
    public final String stringField;
    public final Integer integerField;
    public final WrappedBigDecimal doubleField;
    public final Date dateField;
    public final Boolean booleanField;

    TestRow(String someString, Integer someInteger, Double someDouble, String someDate, Boolean someBoolean) {
        stringField = someString;
        integerField = someInteger;
        doubleField = (someDouble == null) ? null : WrappedBigDecimal.fromDouble(someDouble);
        dateField = WebUtils.parseDate(someDate);
        booleanField = someBoolean;
    }


}
