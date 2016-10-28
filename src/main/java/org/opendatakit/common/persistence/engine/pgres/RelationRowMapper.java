/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.pgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.security.User;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class RelationRowMapper implements RowMapper<CommonFieldsBase> {

  private final CommonFieldsBase relation;
  private final User user;

  RelationRowMapper(CommonFieldsBase relation, User user) {
    this.relation = relation;
    this.user = user;
  }

  @Override
  public CommonFieldsBase mapRow(ResultSet rs, int rowNum) throws SQLException {

    CommonFieldsBase row;
    try {
      row = relation.getEmptyRow(user);
      row.setFromDatabase(true);
    } catch (Exception e) {
      throw new IllegalStateException("failed to create empty row", e);
    }

    /**
     * Correct for the funky handling of nulls by the various accessors...
     */
    for (DataField f : relation.getFieldList()) {
      switch (f.getDataType()) {
      case BINARY:
        byte[] blobBytes = rs.getBytes(f.getName());
        row.setBlobField(f, blobBytes);
        break;
      case LONG_STRING:
      case URI:
      case STRING:
        row.setStringField(f, rs.getString(f.getName()));
        break;
      case INTEGER:
        long l = rs.getLong(f.getName());
        if (rs.wasNull()) {
          row.setLongField(f, null);
        } else {
          row.setLongField(f, Long.valueOf(l));
        }
        break;
      case DECIMAL: {
        String value = rs.getString(f.getName());
        if ( value == null ) {
          row.setNumericField(f, null);
        } else {
          row.setNumericField(f, new WrappedBigDecimal(value));
        }
      }
        break;
      case BOOLEAN:
        Boolean b = rs.getBoolean(f.getName());
        if (rs.wasNull()) {
          row.setBooleanField(f, null);
        } else {
          row.setBooleanField(f, b);
        }
        break;
      case DATETIME:
        Date d = rs.getTimestamp(f.getName());
        if (d == null) {
          row.setDateField(f, null);
        } else {
          row.setDateField(f, (Date) d.clone());
        }
        break;
      default:
        throw new IllegalStateException("Did not expect non-primitive type in column fetch");
      }
    }
    return row;
  }
}
