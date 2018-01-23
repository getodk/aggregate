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
        this.stringField = someString;
        this.integerField = someInteger;
        this.doubleField = (someDouble == null) ? null : WrappedBigDecimal.fromDouble(someDouble);
        this.dateField = WebUtils.parseDate(someDate);
        this.booleanField = someBoolean;
    }


}
